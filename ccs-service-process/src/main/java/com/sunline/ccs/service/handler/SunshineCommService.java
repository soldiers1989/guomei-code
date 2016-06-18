package com.sunline.ccs.service.handler;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.serializable.JsonSerializeUtil;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.facility.ServJsonUtil;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.facility.order.OrderFacility;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctO;
import com.sunline.ccs.infrastructure.server.repos.RCcsAuthmemoO;
import com.sunline.ccs.infrastructure.server.repos.RCcsCard;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.server.repos.RCcsPlan;
import com.sunline.ccs.infrastructure.server.repos.RCcsRepaySchedule;
import com.sunline.ccs.infrastructure.server.repos.RCcsSettleClaim;
import com.sunline.ccs.infrastructure.server.repos.RCcsSettleLoanHst;
import com.sunline.ccs.infrastructure.server.repos.RCcsTxnPost;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.CcsSettleClaim;
import com.sunline.ccs.infrastructure.shared.model.CcsSettleLoanHst;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnPost;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.QCcsSettleClaim;
import com.sunline.ccs.infrastructure.shared.model.QCcsSettleLoanHst;
import com.sunline.ccs.otb.AcctOTB;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.BlockCode;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.payEntity.CommResp;
import com.sunline.ccs.service.payEntity.PayCutPaymentResp;
import com.sunline.ccs.service.payEntity.PaySinPaymentResp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.api.BankServiceForAps;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.DecisionCode;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ppy.dictionary.enums.LoanMold;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.ManualAuthFlag;
import com.sunline.ppy.dictionary.enums.MsPayBank;
import com.sunline.ppy.dictionary.enums.MsPayfrontError;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @author wangz
 * 
 */
@Service
public class SunshineCommService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private RCcsLoan rCcsLoan;
	@Autowired
	private RCcsLoanReg rCcsLoanReg;
	@Autowired
	private RCcsAcct rCcsAcct;
	@Autowired
	private RCcsAcctO rCcsAccto;
	@Autowired
	private RCcsCustomer rCcsCustomer;
	@Autowired
	private RCcsAuthmemoO rCcsAuthmemoO;
	@Autowired
	private RCcsRepaySchedule rCcsRepaySchedule;
	@Autowired
	private RCcsPlan rCcsPlan;
	@Autowired
	private RCcsTxnPost rCcsTxnPost;
	@Autowired
	private RCcsCard rCcsCard;
	@Autowired
	private RCcsSettleClaim rCcsSettleClaim;
	@Autowired
	private RCcsSettleLoanHst rCcsSettleLoanHst;
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	@Autowired
	private OrderFacility orderFacility;
	@Autowired
	private ServJsonUtil servJsonUtil;
	@Autowired
	private AcctOTB accountOTB;
	@Autowired
	private MicroCreditRescheduleUtils microCreditRescheduleUtils;
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	@Resource(name = "bankService")
	private BankServiceForAps bankServiceForApsImpl;
	@Autowired
	private OpenAcctCommService openAcctCommService;
	
	//受理机构号，目前硬编码
	private static final String FWD_INS = "99999999";

	/**
	 * 加载贷款注册信息
	 * @param context
	 * @param loanRegStatus
	 * @param nullException
	 */
	public void loadLoanReg(TxnContext context,String refNbr,LoanRegStatus loanRegStatus, LoanAction loanAction, boolean nullException) {
		if(logger.isDebugEnabled())
			logger.debug("加载贷款注册信息--loadLoanReg");
		TxnInfo txnInfo = context.getTxnInfo();
		try {
			QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;
			CcsLoanReg loanReg = null;
			//阳光保险 用保单号查 马上贷用借据号查
			BooleanExpression expression = null;
			if(txnInfo.getGuarantyid() != null){
				expression = qCcsLoanReg.guarantyId.eq(txnInfo.getGuarantyid());				
			}
			if(txnInfo.getDueBillNo() != null){
				expression = qCcsLoanReg.dueBillNo.eq(txnInfo.getDueBillNo());				
			}
			
			if(null != expression){
				if(null != loanRegStatus){
					expression = expression.and(qCcsLoanReg.loanRegStatus.eq(loanRegStatus));
				}
				if(null != loanAction){
					expression = expression.and(qCcsLoanReg.loanAction.eq(loanAction));
				}
				if(StringUtils.isNotBlank(refNbr)){
					expression = expression.and(qCcsLoanReg.refNbr.eq(refNbr));
				}
	
				loanReg = rCcsLoanReg.findOne(expression);
			}else if(nullException){
				throw new ProcessException(MsRespCode.E_1011.getCode(),MsRespCode.E_1011.getMessage());
			}
			
			if (nullException && null == loanReg) {
				throw new ProcessException(MsRespCode.E_1011.getCode(),MsRespCode.E_1011.getMessage());
			}
			
			if(null != loanReg){
				txnInfo.setAcctNbr(loanReg.getAcctNbr());
				txnInfo.setAcctType(loanReg.getAcctType());
			}
			context.setLoanReg(loanReg);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1011.getCode(), MsRespCode.E_1011.getMessage());
		}

	}

	/**
	 * 加载贷款信息
	 * 
	 * @param context
	 * @param nullException
	 */
	public void loadLoan(TxnContext context, boolean nullException) {
		if(logger.isDebugEnabled())
			logger.debug("加载贷款信息--loadLoan");
		TxnInfo txnInfo = context.getTxnInfo();
		try {
			QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
			CcsLoan loan = null;
			//阳光贷--根据保单号查询
			if(StringUtils.isNotBlank(txnInfo.getGuarantyid())) {
				loan = rCcsLoan.findOne(qCcsLoan.guarantyId.eq(txnInfo
						.getGuarantyid()));
			}else if(StringUtils.isNotBlank(txnInfo.getDueBillNo())) {
				//兼容马上贷 -- 根据借据号查询
				loan = rCcsLoan.findOne(qCcsLoan.dueBillNo.eq(txnInfo.getDueBillNo()));
			}else if(nullException){
				throw new ProcessException(MsRespCode.E_1015.getCode(), MsRespCode.E_1015.getMessage());
			}
			
			if(null != loan){
				txnInfo.setAcctNbr(loan.getAcctNbr());
				txnInfo.setAcctType(loan.getAcctType());
				txnInfo.setDueBillNo(loan.getDueBillNo());
			}
			context.setLoan(loan);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1015.getCode(), MsRespCode.E_1015.getMessage());
		}
	}
	
	/**
	 * 加载贷款信息（使用借据号查询）--马上贷使用
	 * 
	 * @param context
	 * @param nullException
	 */
	public void loadLoanByDueBillNo(TxnContext context, boolean nullException) {
		if(logger.isDebugEnabled())
			logger.debug("加载贷款信息--loadLoan");
		TxnInfo txnInfo = context.getTxnInfo();
		try {
			QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
			CcsLoan loan = rCcsLoan.findOne(qCcsLoan.dueBillNo.eq(txnInfo
					.getDueBillNo()));

			if (nullException && null == loan) {
				throw new ProcessException(MsRespCode.E_1015.getCode(), MsRespCode.E_1015.getMessage());
			}
			
			if(null != loan){
				txnInfo.setAcctNbr(loan.getAcctNbr());
				txnInfo.setAcctType(loan.getAcctType());
//				txnInfo.setDueBillNo(loan.getDueBillNo());
			}
			context.setLoan(loan);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1015.getCode(), MsRespCode.E_1015.getMessage());
		}
	}
	
	/**
	 * 加载卡信息
	 * @param context
	 * @param nullException
	 */
	public void loadCard(TxnContext context, boolean nullException){
		if(logger.isDebugEnabled())
			logger.debug("加载卡信息--loadCard");
		CcsAcct acct = context.getAccount();
		TxnInfo txnInfo = context.getTxnInfo();
		try {
			QCcsCard qCcsCard = QCcsCard.ccsCard;
			CcsCard card = rCcsCard.findOne(qCcsCard.acctNbr.eq(acct.getAcctNbr()));

			if (nullException && null == card) {
				throw new ProcessException(MsRespCode.E_1003.getCode(), MsRespCode.E_1003.getMessage());
			}
			
			if(null != card)
				txnInfo.setCardNo(card.getLogicCardNbr());
			context.setCard(card);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1003.getCode(), MsRespCode.E_1003.getMessage());
		}
	}

	/**
	 * 加载核心账户信息
	 * 
	 * @param context
	 * @param nullException
	 */
	public void loadAcct(TxnContext context, boolean nullException) {
		if(logger.isDebugEnabled())
			logger.debug("加载核心账户信息--loadAcct");
		TxnInfo txnInfo = context.getTxnInfo();
		Long acctNbr = txnInfo.getAcctNbr();
		AccountType acctType = txnInfo.getAcctType();

		try {
			
			if(nullException && (null == acctNbr || null == acctType)){
				throw new ProcessException(MsRespCode.E_1003.getCode(), MsRespCode.E_1003.getMessage());
			}else if(!nullException && (null == acctNbr || null == acctType)){
				context.setAccount(null);
				return ;
			}

			QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
			CcsAcct acct = rCcsAcct.findOne(qCcsAcct.acctNbr.eq(acctNbr).and(
					qCcsAcct.acctType.eq(acctType)));

			if (nullException && null == acct) {
				throw new ProcessException(MsRespCode.E_1003.getCode(), MsRespCode.E_1003.getMessage());
			}
			context.setAccount(acct);
			if(null != acct){
				context.getTxnInfo().setContrNo(acct.getContrNbr());
				context.getTxnInfo().setAcctNbr(acctNbr);
				context.getTxnInfo().setAcctType(acctType);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1003.getCode(), MsRespCode.E_1003.getMessage());
		}
	}
	
	/**
	 * 根据订单表加载核心账户信息
	 * 
	 * @param context
	 * @param nullException
	 */
	public void loadAcct(TxnContext context, Long acctNbr,AccountType acctType,boolean nullException) {
		if(logger.isDebugEnabled())
			logger.debug("根据订单表加载核心账户信息--loadAcctByOrder");

		try {

			QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
			CcsAcct acct = rCcsAcct.findOne(qCcsAcct.acctNbr.eq(acctNbr).and(
					qCcsAcct.acctType.eq(acctType)));

			if (nullException && null == acct) {
				throw new ProcessException(MsRespCode.E_1003.getCode(), MsRespCode.E_1003.getMessage());
			}
			context.setAccount(acct);
			if(null != acct){
				context.getTxnInfo().setContrNo(acct.getContrNbr());
				context.getTxnInfo().setAcctNbr(acctNbr);
				context.getTxnInfo().setAcctType(acctType);
			}
			
				
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1003.getCode(), MsRespCode.E_1003.getMessage());
		}
	}
	/**
	 * 加载联机账户信息
	 * 
	 * @param context
	 * @param nullException
	 */
	public void loadAcctO(TxnContext context, boolean nullException) {
		if(logger.isDebugEnabled())
			logger.debug("加载联机账户信息--loadAcctO");
		TxnInfo txnInfo = context.getTxnInfo();
		Long acctNbr = txnInfo.getAcctNbr();
		AccountType acctType = txnInfo.getAcctType();

		try {
			
			if(nullException && (null == acctNbr || null == acctType)){
				throw new ProcessException(MsRespCode.E_1003.getCode(), MsRespCode.E_1003.getMessage());
			}else if(!nullException && (null == acctNbr || null == acctType)){
				context.setAccounto(null);
				return ;
			}

			QCcsAcctO qCcsAcctO = QCcsAcctO.ccsAcctO;
			CcsAcctO acctO = rCcsAccto.findOne(qCcsAcctO.acctNbr.eq(acctNbr)
					.and(qCcsAcctO.acctType.eq(acctType)));

			if (nullException && null == acctO) {
				throw new ProcessException(MsRespCode.E_1003.getCode(), MsRespCode.E_1003.getMessage());
			}
			context.setAccounto(acctO);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1003.getCode(), MsRespCode.E_1003.getMessage());
		}
	}
	
	/**
	 * 加载联机账户信息
	 * 
	 * @param context
	 * @param nullException
	 */
	public void loadAcctO(TxnContext context, Long acctNbr,AccountType acctType,boolean nullException) {
		if(logger.isDebugEnabled())
			logger.debug("根据订单加载联机账户信息--loadAcctOByOrder");

		try {
			
			QCcsAcctO qCcsAcctO = QCcsAcctO.ccsAcctO;
			CcsAcctO acctO = rCcsAccto.findOne(qCcsAcctO.acctNbr.eq(acctNbr)
					.and(qCcsAcctO.acctType.eq(acctType)));

			if (nullException && null == acctO) {
				throw new ProcessException(MsRespCode.E_1003.getCode(), MsRespCode.E_1003.getMessage());
			}
			context.setAccounto(acctO);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1003.getCode(), MsRespCode.E_1003.getMessage());
		}
	}
	/**
	 * 加载客户信息
	 * 
	 * @param context
	 * @param nullException
	 */
	public void loadCustomer(TxnContext context, boolean nullException) {
		if(logger.isDebugEnabled())
			logger.debug("加载客户信息--loadCustomer");
		CcsAcct acct = context.getAccount();
		try {
			if(nullException && null == acct){
				throw new ProcessException(MsRespCode.E_1023.getCode(), MsRespCode.E_1023.getMessage());
			}else if(!nullException && null == acct){
				context.setCustomer(null);
				return ;
			}
			
			QCcsCustomer qCcsCustomer = QCcsCustomer.ccsCustomer;
			CcsCustomer customer = rCcsCustomer.findOne(qCcsCustomer.custId
					.eq(acct.getCustId()));
			if (nullException && null == customer) {
				throw new ProcessException(MsRespCode.E_1023.getCode(), MsRespCode.E_1023.getMessage());
			}
			context.setCustomer(customer);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1023.getCode(), MsRespCode.E_1023.getMessage());
		}
	}
	
	/**
	 * 加载AuthMemo信息
	 * 
	 * @param context
	 * @param nullException
	 */
	public void loadAuthmemo(TxnContext context, boolean nullException) {
		if(logger.isDebugEnabled())
			logger.debug("加载AuthMemo信息--loadAuthmemo");
		CcsAcct acct = context.getAccount();
		try {
			if(nullException && null == acct){
				throw new ProcessException(MsRespCode.E_1023.getCode(), MsRespCode.E_1023.getMessage());
			}else if(!nullException && null == acct){
				context.setAuthmemo(null);
				return ;
			}
			
			QCcsAuthmemoO qCcsAuthmemoO = QCcsAuthmemoO.ccsAuthmemoO;
			CcsAuthmemoO authmemo = rCcsAuthmemoO.findOne(qCcsAuthmemoO.acctNbr.eq(acct.getAcctNbr()));
			if (nullException && null == authmemo) {
				throw new ProcessException(MsRespCode.E_1023.getCode(), MsRespCode.E_1023.getMessage());
			}
			context.setAuthmemo(authmemo);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1023.getCode(), MsRespCode.E_1023.getMessage());
		}
	}
	
	/**
	 * 加载理赔信息
	 * @param context
	 * @param nullException
	 */
	public void loadClaim(TxnContext context, boolean nullException) {
		if(logger.isDebugEnabled())
			logger.debug("加载理赔信息--loadClaim");
		TxnInfo txnInfo = context.getTxnInfo();
		String dueBillNo = txnInfo.getDueBillNo();
		Long acctNbr = txnInfo.getAcctNbr();
		try {
			if(nullException && (StringUtils.isEmpty(dueBillNo)|| null == acctNbr)){
				throw new ProcessException(MsRespCode.E_1041.getCode(), MsRespCode.E_1041.getMessage());
			}else if(!nullException && (StringUtils.isEmpty(dueBillNo)|| null == acctNbr)){
				context.setClaim(null);
				return ;
			}
			
			QCcsSettleClaim qCcsSettleClaim = QCcsSettleClaim.ccsSettleClaim;
			CcsSettleClaim claim = rCcsSettleClaim.findOne(qCcsSettleClaim.acctNbr.eq(acctNbr).and(qCcsSettleClaim.dueBillNo.eq(dueBillNo)));
			if (nullException && null == claim) {
				throw new ProcessException(MsRespCode.E_1041.getCode(), MsRespCode.E_1041.getMessage());
			}
			context.setClaim(claim);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1041.getCode(), MsRespCode.E_1041.getMessage());
		}
	}
	
	/**
	 * 获取金融交易流水
	 * @param context
	 * @param nullException
	 */
	public void loadAuthmemo(TxnContext context,Long logKv, boolean nullException){
		if(logger.isDebugEnabled())
			logger.debug("获取金融交易流水--loadAuthmemo,logkv:[{}]",logKv);
		try {
			if(nullException && null == logKv){
				throw new ProcessException(MsRespCode.E_1007.getCode(), MsRespCode.E_1007.getMessage());
			}else if(!nullException && null == logKv){
				context.setAuthmemo(null);
				return ;
			}
			
			CcsAuthmemoO memo = rCcsAuthmemoO.findOne(logKv);
			if (nullException && null == memo) {
				throw new ProcessException(MsRespCode.E_1007.getCode(), MsRespCode.E_1007.getMessage());
			}
			context.setAuthmemo(memo);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1007.getCode(), MsRespCode.E_1007.getMessage());
		}
	}
	
	/**
	 * 加载信用计划列表
	 * 
	 * @param context
	 * @param nullException
	 */
	public List<CcsPlan> loadPlans(TxnContext context, boolean nullException) {
		if(logger.isDebugEnabled())
			logger.debug("加载信用计划列表--loadPlans");
		CcsAcct acct = context.getAccount();
		List<CcsPlan> planList = new ArrayList<CcsPlan>();
		try {
			QCcsPlan qCcsPlan = QCcsPlan.ccsPlan;
			Iterable<CcsPlan> plans = rCcsPlan.findAll(qCcsPlan.acctNbr.eq(acct.getAcctNbr()).
					and(qCcsPlan.acctType.eq(acct.getAcctType())));
			if (nullException && null == plans) {
				throw new ProcessException(MsRespCode.E_1015.getCode(), MsRespCode.E_1015.getMessage());
			}
			if(null != plans ){
				for (CcsPlan ccsPlan : plans) {
					planList.add(ccsPlan);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1015.getCode(), MsRespCode.E_1015.getMessage());
		}
		return planList;
		
	}
	
	/**
	 * 
	 * @see 方法名：checkBlockCode
	 * @see 描述：获取账户上BlockCode
	 * @author wangz
	 * @param account
	 * @param txnInfo
	 * @throws AuthException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void checkBlockCode(TxnContext context) throws ProcessException {
		if(logger.isDebugEnabled())
			logger.debug("获取账户上BlockCode--checkBlockCode");
		CcsAcctO accounto = context.getAccounto();
		TxnInfo txnInfo = context.getTxnInfo();
		
		LogTools.printObj(logger, accounto.getBlockCode(), "账户锁定码为");

		if (txnInfo.getTransType() == AuthTransType.Credit
				|| txnInfo.getTransType() == AuthTransType.AgentCredit
				|| txnInfo.getTransType() == AuthTransType.TransferCredit) {
			// 账户锁定码包含"P"
			if (StringUtils.isNotEmpty(accounto.getBlockCode())
					&& accounto.getBlockCode().toUpperCase().indexOf("P") > -1) {
				throw new ProcessException(MsRespCode.E_1014.getCode(), MsRespCode.E_1014.getMessage());
			}
		} else {
			/**
			 * 获取账户层每一位BlockCode(BlockCode存储格式为ABCD...)
			 */
			if (StringUtils.isNotEmpty(accounto.getBlockCode())) {
				setActionFromBlockCode(txnInfo, accounto.getBlockCode().toCharArray());
			}
		}
	}

	/**
	 * 
	 * @see 方法名：setActionFromBlockCode
	 * @see 描述：根据交易类型获取对应的行动。
	 * @author wangz
	 * 
	 * @param txnInfo
	 * @param reasonMap
	 * @param BlockCodeArray
	 * @throws AuthException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private void setActionFromBlockCode(TxnInfo txnInfo, char... BlockCodeArray)
			throws ProcessException {
		if(logger.isDebugEnabled())
			logger.debug("根据交易类型获取对应的行动--setActionFromBlockCode");
		BlockCode blockCode = null;

		for (char bc : BlockCodeArray) {
			blockCode = unifiedParameterFacility.loadParameter(bc,
					BlockCode.class);

			/**
			 * 交易类型中，人工授权优先级最高； 当人工授权标志=强制调整，并且交易类型=消费/取现/查询...，都按人工授权的行动码为准；
			 * 人工授权标志 == 强制调整
			 */
			if (txnInfo.getManualAuthFlag() == ManualAuthFlag.F) {
				// 根据交易类型获取对应的action
				if (blockCode.debitAdjustAction != AuthAction.A) {
					throw new ProcessException(MsRespCode.E_1004.getCode(), MsRespCode.E_1004.getMessage()+",锁定码"+blockCode.description);
				}
			}
			/**
			 * 交易类型 = 取现 行动码 = BlockCode参数中的取现行动码
			 */
			else if (txnInfo.getTransType() == AuthTransType.Cash
					|| txnInfo.getTransType() == AuthTransType.TransferDeditDepos) {
				// 根据交易类型获取对应的action
				if (blockCode.cashAction != AuthAction.A) {
					throw new ProcessException(MsRespCode.E_1004.getCode(), MsRespCode.E_1004.getMessage()+",锁定码"+blockCode.description);
				}
			}
			/**
			 * 交易类型 = 代付 行动码 = BlockCode参数中的代付行动码
			 */
			else if (txnInfo.getTransType() == AuthTransType.AgentDebit) {
				// 根据交易类型获取对应的action
				if (blockCode.cashAction != AuthAction.A) {
					throw new ProcessException(MsRespCode.E_1004.getCode(), MsRespCode.E_1004.getMessage()+",锁定码"+blockCode.description);
				}
			}
			/**
			 * 交易类型 = 查询 行动码 = BlockCode参数中的查询行动码
			 */
			else if (txnInfo.getTransType() == AuthTransType.Inq) {
				if (blockCode.inquireAction != AuthAction.A) {
					throw new ProcessException(MsRespCode.E_1004.getCode(), MsRespCode.E_1004.getMessage()+",锁定码"+blockCode.description);
				}
			}
			/**
			 * 交易类型 = 消费|普通(额度内)分期|预授权|圈存
			 */
			else {
				// 非moto普通消费对应的action
				if (blockCode.nonMotoRetailAction != AuthAction.A) {
					throw new ProcessException(MsRespCode.E_1004.getCode(), MsRespCode.E_1004.getMessage()+",锁定码"+blockCode.description);
				}
			}
		}
	}

	/**
	 * 获取下一期期款
	 * 
	 * @param context
	 */
	public CcsRepaySchedule loadSchedule(TxnContext context, boolean nullException) {
		if(logger.isDebugEnabled())
			logger.debug("获取下一期期款--loadSchedule");
		CcsLoan loan = context.getLoan();
		try {
			QCcsRepaySchedule qCcsRepaySchedule = QCcsRepaySchedule.ccsRepaySchedule;
			CcsRepaySchedule ccsRepaySchedule = rCcsRepaySchedule
					.findOne(qCcsRepaySchedule.loanId.eq(loan.getLoanId())
							.and(qCcsRepaySchedule.currTerm.eq(loan
									.getCurrTerm() + 1)));
			if (nullException && null == ccsRepaySchedule) {
				throw new ProcessException(MsRespCode.E_1015.getCode(), MsRespCode.E_1015.getMessage());
			}
			return ccsRepaySchedule;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1015.getCode(), MsRespCode.E_1015.getMessage());
		}

	}

	/**
	 * 检查请求报文
	 * 重复交易检查 交易流水/请求时间/保单号
	 * 7 32 37 保单
	 * @param context
	 */
	public void checkReqAndRepeatTxn(TxnContext context) {
		if(logger.isDebugEnabled())
			logger.debug("重复交易检查 交易流水/请求时间/保单号--checkRepeatTxn");
		TxnInfo txnInfo = context.getTxnInfo();
		String serviceSn = txnInfo.getServiceSn();
//		String requestTime = txnInfo.getRequestTime();
		String acqId = txnInfo.getAcqId();
		//兼容马上贷BANK
//		if(txnInfo.getInputSource() != InputSource.SUNS && txnInfo.getInputSource() != InputSource.BANK){
//			throw new ProcessException(MsRespCode.E_1043.getCode(),MsRespCode.E_1043 .getMessage()+ ",字段名称{CHANNEL_ID},不支持"+txnInfo.getInputSource().name());
//		}
		
		//TODO 阳光一期硬代码
//		if(txnInfo.getInputSource() == InputSource.SUNS && !StringUtils.equals(txnInfo.getAcqId(), "00130000")){
//			throw new ProcessException(MsRespCode.E_1043.getCode(),MsRespCode.E_1043 .getMessage()+ ",字段名称{ACQ_ID},不支持"+txnInfo.getAcqId());
//		}
		
		orderFacility.valiRepeatOrder(serviceSn, acqId);
	}
	
	/**
	 * 检查在途代扣交易，有则异常
	 * @param context
	 */
	public void checkWaitCreditOrder(TxnContext context){
		if(logger.isDebugEnabled())
			logger.debug("检查在途代扣交易，有则异常--checkWaitCreditOrder");
		CcsAcct acct = context.getAccount();
		
		Long orderCount = orderFacility.countOrderByAcct(acct.getAcctNbr(),acct.getAcctType(), OrderStatus.W, AuthTransType.AgentCredit);
		
		if (null != orderCount && orderCount > 0) {
			throw new ProcessException(MsRespCode.E_1046.getCode(), 
					MsRespCode.E_1046.getMessage().replace("N", orderCount.toString()));
		}
	}
	
	/**
	 * 检查在途代付交易，有则异常
	 * @param context
	 */
	public void checkWaitDebitOrder(TxnContext context){
		if(logger.isDebugEnabled())
			logger.debug("检查在途代付交易，有则异常--checkWaitDebitOrder");
		CcsAcct acct = context.getAccount();
		
		Long orderCount = orderFacility.countOrderByAcct(acct.getAcctNbr(),acct.getAcctType(), OrderStatus.W, AuthTransType.AgentDebit);
		
		if (null != orderCount && orderCount > 0) {
			throw new ProcessException(MsRespCode.E_1047.getCode(), 
					MsRespCode.E_1047.getMessage().replace("N", orderCount.toString()));
		}
	}

	/**
	 * 获取订单累计金额
	 * 
	 * @param context
	 * @param orderStatus
	 *            订单状态
	 * @param agentdebit
	 *            交易类型
	 * @param nullException
	 */
	public void computOrderAmt(TxnContext context, OrderStatus orderStatus,
			AuthTransType transType) {
		if(logger.isDebugEnabled())
			logger.debug("获取订单累计金额--computOrderAmt");
		CcsLoan loan = context.getLoan();
		TxnInfo txnInfo = context.getTxnInfo();
		BigDecimal onWayCrAmt = BigDecimal.ZERO;

		Iterable<CcsOrder> ccsOrderList = orderFacility.findByDueBillNo(
				loan.getDueBillNo(), orderStatus, transType,loan.getAcctNbr(),loan.getAcctType());

		if (null != ccsOrderList) {
			for (CcsOrder ccsOrder : ccsOrderList) {
				onWayCrAmt = onWayCrAmt.add(ccsOrder.getTxnAmt());
			}
		}
		txnInfo.setOnWayCrAmt(onWayCrAmt);
	}

	/**
	 * 新建订单
	 * @param context
	 */
	public void installOrder(TxnContext context) {
		if(logger.isDebugEnabled())
			logger.debug("新建订单--installOrder");
		TxnInfo txnInfo = context.getTxnInfo();
		CcsAcct acct = context.getAccount();
		CcsCustomer cust = context.getCustomer();
		Long origOrderId =null;
		LogTools.printObj(logger, txnInfo, "交易中间信息txnInfo");


		if(null != context.getOrigOrder()){
			origOrderId = context.getOrigOrder().getOrderId();
		}
		else if(null != context.getOrigOrderHst()){
			origOrderId = context.getOrigOrderHst().getOrderId();
		}
		BigDecimal amt = BigDecimal.ZERO;
		if (txnInfo.getPremiumInd()==Indicator.Y) {
			amt = txnInfo.getPremiumAmt()==null?BigDecimal.ZERO:txnInfo.getPremiumAmt();
		}
		// 组装订单
		CcsOrder order = orderFacility.installOrder(acct, cust,
				txnInfo.getLoanUsage(), txnInfo.getTransAmt().subtract(amt),
				txnInfo.getBizDate(), txnInfo.getInputSource(),
				txnInfo.getOnlineFlag(),txnInfo.getAcqId(),
				txnInfo.getSubTerminalType(),txnInfo.getOnlineFlag(),
				txnInfo.getRequestTime(),txnInfo.getServiceSn(),txnInfo.getRefNbr(),
				txnInfo.getGuarantyid(),txnInfo.getDueBillNo(),origOrderId,
				txnInfo.getContrNo(),txnInfo.getMobile(),txnInfo.getServiceId(),
				txnInfo.getMerId(),txnInfo.getAuthTxnTerminal());
		
		order.setTxnType(txnInfo.getTransType());
		order.setLogKv(txnInfo.getLogKv());
		order.setMerchandiseOrder(txnInfo.getMerchandiseOrder());
		order.setPremiumAmt(txnInfo.getPremiumAmt());
		order.setPremiumInd(txnInfo.getPremiumInd());
		order.setLoanAmt(txnInfo.getLoanAmt());
		
		txnInfo.setOrderId(order.getOrderId());
		context.setOrder(order);
		
		LogTools.printObj(logger,  order.getOrderId(), "订单号orderId");
	}

	/**
	 * 处理支付结果
	 * 
	 * @param context
	 * @param payJson
	 * @param resp
	 */
	@Transactional
	public void payResultProc(TxnContext context, String payJson) {
//		throw new ProcessException(MsRespCode.E_1009.getCode(), MsRespCode.E_1009.getMessage());
		CcsOrder order = context.getOrder();
		TxnInfo txnInfo = context.getTxnInfo();
		try{
			if(logger.isDebugEnabled())
				logger.debug("独立事务，处理支付结果--payResultProc,支付报文：[{}]",payJson);
			//结算交易不载入账户
			if(order.getLoanUsage() != LoanUsage.G 
			&& order.getLoanUsage() != LoanUsage.B ){
				this.loadAcctO(context, true);
				this.loadAcct(context, true);				
			}

			// 分交易处理结果
			switch (order.getLoanUsage()) {
			case A:// 放款重提
			case L:// 放款申请
				if(null != context.getOrigOrder()){
					context.setOrigOrder(orderFacility.findById(context.getOrigOrder().getOrderId(), true));
				}
				this.loadLoanReg(context,txnInfo.getRefNbr(), LoanRegStatus.C, null, true);
				this.loadAuthmemo(context, context.getTxnInfo().getLogKv(),
						false);
				this.loanProc(context,payJson);
				break;
			case S:// 联机追偿
				this.subrogationProc(context,payJson);
				break;
			case N:// 正常扣款
			case O:// 逾期扣款
				this.paymentProc(context,payJson);
				break;
			case M:// 预约结清扣款
				this.loadLoanReg(context,null, LoanRegStatus.A, LoanAction.O, true);
				this.paymentProc(context,payJson);
				context.getLoanReg().setDdRspFlag(Indicator.Y);// 预约注册-扣款成功
				context.getLoanReg().setValidDate(txnInfo.getBizDate());
				break;
			case E:// 代付查询
				this.loadAuthmemo(context, context.getTxnInfo().getLogKv(),false);
				if (context.getOrigOrder().getLoanUsage() == LoanUsage.D) {
					this.loadLoanReg(context,context.getOrigOrder().getRefNbr(), LoanRegStatus.C, null, false);
				}else {
					this.loadLoanReg(context,context.getOrigOrder().getRefNbr(), LoanRegStatus.C, null, true);
				}
				this.paySinPaymentQueryProc(context,payJson);
				break;
			case F:// 代扣查询
				this.payCutPaymentQueryProc(context,payJson);
				break;
			case B:// 结算
				//获取jpa_version
				context.setOrigOrder(orderFacility.findById(context.getOrigOrder().getOrderId(), true));
				context.setOrder(orderFacility.findById(context.getOrder().getOrderId(), true));
				this.paySettleProc(context,payJson);
				break;
			case G://结算查询
				this.settleQueryProc(context,payJson);
				break;
			default:
				if (logger.isErrorEnabled())
					logger.error("不支持贷款用途：[{}]", order.getLoanUsage());
				// 设置核心响应码
				txnInfo.setResponsCode(MsRespCode.E_1009.getCode());
				txnInfo.setResponsDesc(MsRespCode.E_1009.getMessage());
				throw new ProcessException(MsRespCode.E_1009.getCode(),
						MsRespCode.E_1009.getMessage());
			}
		}catch (ProcessException pe){
			if(logger.isErrorEnabled())
				logger.error(pe.getMessage(), pe);
			
			ProcessException newPe = this.preException(pe, pe, txnInfo);
			if(txnInfo.getLoanUsage() == LoanUsage.E 
			|| txnInfo.getLoanUsage() == LoanUsage.F
			|| txnInfo.getLoanUsage() == LoanUsage.G){
				this.queryExceptionProc(context, newPe);
			}else{
				this.exceptionProc(context, newPe);
			}
			
		}catch (Exception e) {
			if(logger.isErrorEnabled())
				logger.error(e.getMessage(), e);
			
			ProcessException pe = new ProcessException();
			ProcessException newPe = this.preException(e, pe, txnInfo);;//e不是pe，处理逻辑与没有code的pe一样
			if(txnInfo.getLoanUsage() == LoanUsage.E 
			 ||txnInfo.getLoanUsage() == LoanUsage.F
			 ||txnInfo.getLoanUsage() == LoanUsage.G){
				this.queryExceptionProc(context, newPe);
			}else{
				this.exceptionProc(context, newPe);
			}
		}
		//保存交易数据
		this.mergeProc(context);
	}
	
	/**
	 * 更新账户
	 * @param accto
	 */
	public void mergeProc(TxnContext context){
		if(logger.isDebugEnabled())
			logger.debug("更新--mergeProc");
		CcsAcctO accto = context.getAccounto();
		CcsAuthmemoO authmemoO = context.getAuthmemo();
		CcsSettleClaim claim = context.getClaim();
		CcsLoanReg loanReg = context.getLoanReg();
		CcsOrder order = context.getOrder();
		CcsOrder origOrder = context.getOrigOrder();
		CcsAcct acct = context.getAccount();
		
		CcsSettleLoanHst ccsSettleLoanHst = context.getCcsSettleLoanHst();
		
		if(null != accto){
			if(logger.isDebugEnabled())
				logger.debug("更联机新账户--accto");
			em.merge(accto);	
		}
		if(null != authmemoO){
			if(logger.isDebugEnabled())
				logger.debug("更新金融流水--authmemoO");
			em.merge(authmemoO);	
		}
		if(null != claim){
			if(logger.isDebugEnabled())
				logger.debug("更新理赔结清表--claim");
			em.merge(claim);	
		}
		if(null != loanReg){
			if(logger.isDebugEnabled())
				logger.debug("更新贷款注册--loanReg");
			em.merge(loanReg);	
		}
		if(null != order){
			if(logger.isDebugEnabled())
				logger.debug("更新订单--order");
			em.merge(order);	
		}
		if(null != origOrder){
			if(logger.isDebugEnabled())
				logger.debug("更新原始订单--origOrder");
			em.merge(origOrder);	
		}
		if(null != acct){
			if(logger.isDebugEnabled())
				logger.debug("更新账户--acct");
			em.merge(acct);	
		}
		if( null != ccsSettleLoanHst){
			if(logger.isDebugEnabled())
				logger.debug("更新结算历史表--ccsSettleLoanHst");
			em.merge(ccsSettleLoanHst);
		}
	}

	/**
	 * 实时代扣交易处理
	 * @param context
	 * @param mainResp
	 * @param msPayfrontError
	 */
	private void paymentProc(TxnContext context,String payJson) {
		CcsOrder order = context.getOrder();
		CcsAcctO accto = context.getAccounto();
		TxnInfo txnInfo = context.getTxnInfo();
		
		PayCutPaymentResp data = new PayCutPaymentResp();
		MsRespCode respCode = null;
		CommResp mainResp = null;

		try {
			//发送支付指令
			String retJson = bankServiceForApsImpl.sendMsPayFront(payJson, txnInfo.getLoanUsage());
			if(logger.isDebugEnabled())
				logger.debug("loanProc,支付报文：[{}]",retJson);
			
			mainResp = JsonSerializeUtil.jsonReSerializerNoType(retJson, CommResp.class);
			
			//设置公共报文响应码，用于落订单表
			txnInfo.setPayRespCode(mainResp.getCode());
			txnInfo.setPayRespMessage(mainResp.getMessage());
			//公共报文响应码检查
			MsPayfrontError msPayfrontError = this.getErrorEnum(mainResp.getCode(), MsPayfrontError.class);
			txnInfo.setResponsCode(msPayfrontError.getRespCode());
			txnInfo.setResponsDesc(msPayfrontError.getDesc());
			
			//报文头响应码为0成功，需要检查报文体的响应码
			if(msPayfrontError == MsPayfrontError.S_0){
				// 报文头响应码为0成功，还需要判断报文体响应码
				servJsonUtil.setBeanProperty(mainResp, data);
				
				// 设置支付前置返回码，报文体响应码覆盖报文头响应码,用于设置订单状态
				txnInfo.setPayRespCode(data.getErrorCode());
				txnInfo.setPayRespMessage(data.getErrorMessage());
				txnInfo.setPayStatus(data.getStatus());
				// 公共部分为成功时，报文体响应码覆盖公共部分响应码
				msPayfrontError = this.getErrorEnum(data.getErrorCode(), MsPayfrontError.class);
				// 报文体响应码为0
				if (msPayfrontError == MsPayfrontError.S_0) {
					this.saveCcsAuthmemoO(context);
					orderFacility.updateOrder(order, mainResp, data.getStatus(),OrderStatus.S,txnInfo.getLogKv(),txnInfo.getBizDate(),MsRespCode.E_0000);
					this.updateAcctO(context);
					this.updateAcctoMemoAmt(accto, txnInfo,txnInfo.getTransType(),true);
					this.updateAuthmemo(context, AuthTransStatus.N);//交易流水
					
					savePosting(txnInfo);//入账记录
					
				}else{//失败  处理中
					if(logger.isErrorEnabled()){
						logger.error("实时代扣失败/处理中：[{}]  [{}]", mainResp.getCode(),mainResp.getMessage());
					}
					respCode = this.getErrorEnum(msPayfrontError.getRespCode(), MsRespCode.class);
				
					txnInfo.setResponsCode(respCode.getCode());
					txnInfo.setResponsDesc(respCode.getMessage());
					throw new ProcessException(respCode.getCode(),respCode.getMessage());
				}
			}else{
				if(logger.isErrorEnabled())
					logger.error(mainResp.getCode() +"----"+ mainResp.getMessage());
				respCode = this.getErrorEnum(msPayfrontError.getRespCode(), MsRespCode.class);
				//设置核心响应码
				txnInfo.setResponsCode(respCode.getCode());
				txnInfo.setResponsDesc(respCode.getMessage());
				throw new ProcessException(respCode.getCode(),respCode.getMessage());
			}
		} catch(ProcessException pe){
			if(logger.isErrorEnabled())
				logger.error(pe.getErrorCode()+":"+pe.getMessage(), pe);
			throw pe;
		}
	}
	
	/**
	 * 结算重提
	 * @param context
	 * @param payJson
	 */
	private void paySettleProc(TxnContext context, String payJson) {
		CcsOrder order = context.getOrder();
		PaySinPaymentResp data = new PaySinPaymentResp();
		TxnInfo txnInfo = context.getTxnInfo();
		MsRespCode respCode = null;
		CommResp mainResp = null;
		
		try {
			//发送支付指令
			String retJson = bankServiceForApsImpl.sendMsPayFront(payJson, txnInfo.getLoanUsage());
			if(logger.isDebugEnabled())
				logger.debug("loanProc,支付报文：[{}]",retJson);
			
			mainResp = JsonSerializeUtil.jsonReSerializerNoType(retJson, CommResp.class);
			
			//设置公共报文响应码，用于落订单表
			txnInfo.setPayRespCode(mainResp.getCode());
			txnInfo.setPayRespMessage(mainResp.getMessage());
			//公共报文响应码检查
			MsPayfrontError msPayfrontError = this.getErrorEnum(mainResp.getCode(), MsPayfrontError.class);
			txnInfo.setResponsCode(msPayfrontError.getRespCode());
			txnInfo.setResponsDesc(msPayfrontError.getDesc());
			
			//报文头响应码为0成功，需要检查报文体的响应码
			if(msPayfrontError == MsPayfrontError.S_0){
				// 报文头响应码为0成功，还需要判断报文体响应码
				servJsonUtil.setBeanProperty(mainResp, data);
				
				// 设置支付前置返回码，报文体响应码覆盖报文头响应码,用于设置订单状态
				txnInfo.setPayRespCode(data.getErrorCode());
				txnInfo.setPayRespMessage(data.getErrorMessage());
				txnInfo.setPayStatus(data.getStatus());
				// 公共部分为成功时，报文体响应码覆盖公共部分响应码
				msPayfrontError = this.getErrorEnum(data.getErrorCode(), MsPayfrontError.class);
				// 报文体响应码为0，放款成功
				if (msPayfrontError == MsPayfrontError.S_0) {
					context.setCcsSettleLoanHst(updateSettleLoanHst(context.getOrder(),context.getOrder(), Indicator.Y));
					orderFacility.updateOrder(order, mainResp, data.getStatus(),OrderStatus.S, txnInfo.getLogKv(),txnInfo.getBizDate(),MsRespCode.E_0000);
				} else {
					if (logger.isErrorEnabled())
						logger.error(mainResp.getCode()+"----"+mainResp.getMessage());
					
					respCode = this.getErrorEnum(msPayfrontError.getRespCode(), MsRespCode.class);
					txnInfo.setResponsCode(respCode.getCode());
					txnInfo.setResponsDesc(respCode.getMessage());
					throw new ProcessException(respCode.getCode(),respCode.getMessage());
				}
			}else{
				if(logger.isErrorEnabled())
					logger.error(mainResp.getCode() +"----"+ mainResp.getMessage());
				respCode = this.getErrorEnum(msPayfrontError.getRespCode(), MsRespCode.class);
				//设置核心响应码
				txnInfo.setResponsCode(respCode.getCode());
				txnInfo.setResponsDesc(respCode.getMessage());
				throw new ProcessException(respCode.getCode(),respCode.getMessage());
			}
		} catch(ProcessException pe){
			if(logger.isErrorEnabled())
				logger.error(pe.getErrorCode()+":"+pe.getMessage(), pe);
			throw pe;
		}
	}
	
	/**
	 * 更新结算历史表结算状态 根据结算原订单信息
	 * @param order
	 * @param settleInd  结算成功 结算失败
	 */
	public CcsSettleLoanHst updateSettleLoanHst(CcsOrder origOrder,CcsOrder order,Indicator settleInd){
		if(logger.isDebugEnabled())
			logger.debug("更新结算历史表结算状态 根据结算原订单信息--updateSettleLoanHst");
		CcsSettleLoanHst ccsSettleLoanHst = null;
		if(null != origOrder&& null != order){
			QCcsSettleLoanHst qCcsSettleLoanHst = QCcsSettleLoanHst.ccsSettleLoanHst;
			ccsSettleLoanHst = rCcsSettleLoanHst.findOne(qCcsSettleLoanHst.orderId.eq(origOrder.getOrderId()));
			if(null != ccsSettleLoanHst){
				ccsSettleLoanHst.setOrderId(order.getOrderId());
				ccsSettleLoanHst.setIsSettle(null == settleInd?ccsSettleLoanHst.getIsSettle():settleInd);
			}
		}
		return ccsSettleLoanHst;
	}
	/**
	 * 更新账户未入账金额
	 * @param accto
	 * @param txnInfo
	 * @param compFlag 金额计算标识  true 加 false 减
	 */
	private void updateAcctoMemoAmt(CcsAcctO accto, TxnInfo txnInfo,AuthTransType authTransType,boolean amtFlag) {
		//代付
		if(AuthTransType.AgentDebit  ==authTransType){
			if(amtFlag){
				accto.setMemoDb(accto.getMemoDb()!=null?accto.getMemoDb().add(txnInfo.getTransAmt()):txnInfo.getTransAmt());
			}else{
				//金额为空或负数时，修正为0
				accto.setMemoDb(accto.getMemoDb()!=null && accto.getMemoDb().compareTo(txnInfo.getTransAmt())>=0?
						accto.getMemoDb().subtract(txnInfo.getTransAmt()):BigDecimal.ZERO);
			}
		}else if(AuthTransType.AgentCredit == authTransType){
			//代收
			if(amtFlag){
				accto.setMemoCr(accto.getMemoCr()!=null?accto.getMemoCr().add(txnInfo.getTransAmt()):txnInfo.getTransAmt());
			}else{
				//金额为空时，修正为0
				accto.setMemoCr(accto.getMemoCr()!=null && accto.getMemoCr().compareTo(txnInfo.getTransAmt())>=0?
						accto.getMemoCr().subtract(txnInfo.getTransAmt()):BigDecimal.ZERO);
			}
		}
	}

	/**
	 * 代偿交易处理
	 * @param context
	 * @param mainResp
	 * @param msPayfrontError
	 */
	private void subrogationProc(TxnContext context, String payJson) {
		CcsOrder order = context.getOrder();
		TxnInfo txnInfo = context.getTxnInfo();
		PayCutPaymentResp data = new PayCutPaymentResp();
		MsRespCode respCode = null;
		CommResp mainResp = null;
		

		try {
			//发送支付指令
			String retJson = bankServiceForApsImpl.sendMsPayFront(payJson, txnInfo.getLoanUsage());
			if(logger.isDebugEnabled())
				logger.debug("loanProc,支付报文：[{}]",retJson);
			
			mainResp = JsonSerializeUtil.jsonReSerializerNoType(retJson, CommResp.class);
			
			//设置公共报文响应码，用于落订单表
			txnInfo.setPayRespCode(mainResp.getCode());
			txnInfo.setPayRespMessage(mainResp.getMessage());
			//公共报文响应码检查
			MsPayfrontError msPayfrontError = this.getErrorEnum(mainResp.getCode(), MsPayfrontError.class);
			txnInfo.setResponsCode(msPayfrontError.getRespCode());
			txnInfo.setResponsDesc(msPayfrontError.getDesc());
			
			//报文头响应码为0成功，需要检查报文体的响应码
			if(msPayfrontError == MsPayfrontError.S_0){
				// 报文头响应码为0成功，还需要判断报文体响应码
				servJsonUtil.setBeanProperty(mainResp, data);
				
				// 设置支付前置返回码，报文体响应码覆盖报文头响应码,用于设置订单状态
				txnInfo.setPayRespCode(data.getErrorCode());
				txnInfo.setPayRespMessage(data.getErrorMessage());
				txnInfo.setPayStatus(data.getStatus());
				// 公共部分为成功时，报文体响应码覆盖公共部分响应码
				msPayfrontError = this.getErrorEnum(data.getErrorCode(), MsPayfrontError.class);
				// 报文体响应码为0，代位追偿成功
				if (msPayfrontError == MsPayfrontError.S_0) {
					orderFacility.updateOrder(order, mainResp, data.getStatus(),OrderStatus.S,txnInfo.getLogKv(),txnInfo.getBizDate(),MsRespCode.E_0000);//更新订单
					updateSettleClaim(context);//更新理赔追偿
				}else{//失败  处理中
					if(logger.isErrorEnabled()){
						logger.error("代位追偿失败/处理中：[{}]  [{}]", mainResp.getCode(),mainResp.getMessage());
					}
					txnInfo.setResponsCode(MsRespCode.E_1001.getCode());
					txnInfo.setResponsDesc(MsRespCode.E_1001.getMessage());
					//代位追偿，所有失败/处理中都修正为处理中
					throw new ProcessException(MsRespCode.E_1001.getCode(),MsRespCode.E_1001.getMessage());
				}
			} else {
				if (logger.isErrorEnabled())
					logger.error(mainResp.getCode() + "----" + mainResp.getMessage());
				respCode = this.getErrorEnum(msPayfrontError.getRespCode(),
						MsRespCode.class);
				// 设置核心响应码
				txnInfo.setResponsCode(respCode.getCode());
				txnInfo.setResponsDesc(respCode.getMessage());
				throw new ProcessException(respCode.getCode(), respCode.getMessage());
			}
		}catch(ProcessException pe){
			if(logger.isErrorEnabled())
				logger.error(pe.getErrorCode()+":"+pe.getMessage(), pe);
			//代位追偿，所有失败/处理中都修正为处理中
			txnInfo.setResponsCode(MsRespCode.E_1001.getCode());
			txnInfo.setResponsDesc(MsRespCode.E_1001.getMessage());
			throw pe;
		} catch (Exception e) {
			if (logger.isErrorEnabled())
				logger.error(e.getMessage(), e);

			txnInfo.setResponsCode(MsRespCode.E_1001.getCode());
			txnInfo.setResponsDesc(MsRespCode.E_1001.getMessage());
			//代位追偿，所有失败/处理中都修正为处理中
			throw new ProcessException(MsRespCode.E_1001.getCode(),MsRespCode.E_1001.getMessage());
		}
	}

	/**
	 * 代付查询响应处理
	 * @param context
	 * @param mainResp
	 * @param msPayfrontError
	 */
	private void paySinPaymentQueryProc(TxnContext context, String payJson) {
		CcsOrder order = context.getOrder();
		CcsOrder origOrder = context.getOrigOrder();
		TxnInfo txnInfo = context.getTxnInfo();
		CcsLoanReg loanReg = context.getLoanReg();
		PaySinPaymentResp data = new PaySinPaymentResp();
		MsRespCode respCode = null;
		CommResp mainResp = null;
		CcsTxnPost txnPost = null;

		try {
			//发送支付指令
			String retJson = bankServiceForApsImpl.sendMsPayFront(payJson, txnInfo.getLoanUsage());
			if(logger.isDebugEnabled())
				logger.debug("loanProc,支付报文：[{}]",retJson);
			
			mainResp = JsonSerializeUtil.jsonReSerializerNoType(retJson, CommResp.class);
			
			//设置公共报文响应码，用于落订单表
			txnInfo.setPayRespCode(mainResp.getCode());
			txnInfo.setPayRespMessage(mainResp.getMessage());
			//公共报文响应码检查
			MsPayfrontError msPayfrontError = this.getErrorEnum(mainResp.getCode(), MsPayfrontError.class);
			txnInfo.setResponsCode(msPayfrontError.getRespCode());
			txnInfo.setResponsDesc(msPayfrontError.getDesc());
			
			//报文头响应码为0成功，需要检查报文体的响应码
			if(msPayfrontError == MsPayfrontError.S_0){
				// 报文头响应码为0成功，还需要判断报文体响应码
				servJsonUtil.setBeanProperty(mainResp, data);
				
				// 设置支付前置返回码，报文体响应码覆盖报文头响应码,用于设置订单状态
				txnInfo.setPayOthRespCode(data.getErrorCode());
				txnInfo.setPayOthRespMessage(data.getErrorMessage());
				txnInfo.setPayStatus(data.getStatus());
				// 公共部分为成功时，报文体响应码覆盖公共部分响应码
				msPayfrontError = this.getErrorEnum(data.getErrorCode(), MsPayfrontError.class);
				txnInfo.setResponsCode(msPayfrontError.getRespCode());
				txnInfo.setResponsDesc(msPayfrontError.getDesc());
				// 报文体响应码为0，放款成功
				if (msPayfrontError == MsPayfrontError.S_0) {
					//更新订单表状态
					orderFacility.updateOrder(order, mainResp, data.getStatus(),OrderStatus.S, txnInfo.getLogKv(),txnInfo.getBizDate(),MsRespCode.E_0000);
					//更新原订单交易订单状态
					orderFacility.updateOrder(origOrder, mainResp,data.getStatus(),OrderStatus.S, context.getOrigOrder().getLogKv(),txnInfo.getBizDate(),MsRespCode.E_0000);
					//随借随还不更新账户
					if(loanReg != null && loanReg.getLoanType()!=LoanType.MCAT){
						this.updateAcct(context);
					}
					//更新loanReg状态
					this.updateAcctO(context);
					this.updateLoanReg(txnInfo, loanReg,LoanRegStatus.S);
					//代付查询交易，已经是处理中，额度已经冻结，不应该再累加
//					this.updateAcctoMemoAmt(accto, txnInfo,context.getOrigOrder().getTxnType(),true);
					this.updateAuthmemo(context, AuthTransStatus.N);
					txnPost = savePosting(txnInfo);// 入账记录
					txnPost.setTxnTime(origOrder.getRequestTime().substring(4));//设置原订单的交易时间，批量用于匹配authmemo
					txnPost.setRefNbr(origOrder.getRefNbr());//设置原订单的流水
					txnPost.setB011Trace(origOrder.getServicesn().substring(origOrder.getServicesn().length()-6));//原订单流水后6位随机数
				} else {
					if (logger.isErrorEnabled())
						logger.error(mainResp.getCode()+"----"+mainResp.getMessage());
					respCode = this.getErrorEnum(msPayfrontError.getRespCode(), MsRespCode.class);
	
					txnInfo.setResponsCode(respCode.getCode());
					txnInfo.setResponsDesc(respCode.getMessage());
					throw new ProcessException(respCode.getCode(),respCode.getMessage());
				}
			}else{
				if(logger.isErrorEnabled())
					logger.error(mainResp.getCode() +"----"+ mainResp.getMessage());
				respCode = this.getErrorEnum(msPayfrontError.getRespCode(), MsRespCode.class);
				//设置核心响应码
				txnInfo.setResponsCode(respCode.getCode());
				txnInfo.setResponsDesc(respCode.getMessage());
				throw new ProcessException(respCode.getCode(),respCode.getMessage());
			}
		} catch(ProcessException pe){
			if(logger.isErrorEnabled())
				logger.error(pe.getErrorCode()+":"+pe.getMessage(), pe);
			throw pe;
		}
	}
	/**
	 * 结算查询响应处理
	 * @param context
	 * @param mainResp
	 * @param msPayfrontError
	 */
	private void settleQueryProc(TxnContext context, String payJson) {
		CcsOrder order = context.getOrder();
		TxnInfo txnInfo = context.getTxnInfo();
		PaySinPaymentResp data = new PaySinPaymentResp();
		MsRespCode respCode = null;
		CommResp mainResp = null;

		try {
			//发送支付指令
			String retJson = bankServiceForApsImpl.sendMsPayFront(payJson, txnInfo.getLoanUsage());
			if(logger.isDebugEnabled())
				logger.debug("settleQueryProc,支付报文：[{}]",retJson);
			
			mainResp = JsonSerializeUtil.jsonReSerializerNoType(retJson, CommResp.class);
			
			//设置公共报文响应码，用于落订单表
			txnInfo.setPayRespCode(mainResp.getCode());
			txnInfo.setPayRespMessage(mainResp.getMessage());
			//公共报文响应码检查
			MsPayfrontError msPayfrontError = this.getErrorEnum(mainResp.getCode(), MsPayfrontError.class);
			txnInfo.setResponsCode(msPayfrontError.getRespCode());
			txnInfo.setResponsDesc(msPayfrontError.getDesc());
			
			//报文头响应码为0成功，需要检查报文体的响应码
			if(msPayfrontError == MsPayfrontError.S_0){
				// 报文头响应码为0成功，还需要判断报文体响应码
				servJsonUtil.setBeanProperty(mainResp, data);
				
				// 设置支付前置返回码，报文体响应码覆盖报文头响应码,用于设置订单状态
				txnInfo.setPayOthRespCode(data.getErrorCode());
				txnInfo.setPayOthRespMessage(data.getErrorMessage());
				txnInfo.setPayStatus(data.getStatus());
				// 公共部分为成功时，报文体响应码覆盖公共部分响应码
				msPayfrontError = this.getErrorEnum(data.getErrorCode(), MsPayfrontError.class);
				txnInfo.setResponsCode(msPayfrontError.getRespCode());
				txnInfo.setResponsDesc(msPayfrontError.getDesc());

				// 报文体响应码为0，放款成功
				if (msPayfrontError == MsPayfrontError.S_0) {
					//更新订单表状态
					orderFacility.updateOrder(order, mainResp, null,OrderStatus.S, txnInfo.getLogKv(),txnInfo.getBizDate(),MsRespCode.E_0000);
					//更新原订单交易订单状态
					orderFacility.updateOrder(context.getOrigOrder(), mainResp,data.getStatus(),OrderStatus.S, context.getOrigOrder().getLogKv(),txnInfo.getBizDate(),MsRespCode.E_0000);
					context.setCcsSettleLoanHst(updateSettleLoanHst(context.getOrigOrder(),context.getOrigOrder(), Indicator.Y));
				} else {
					if (logger.isErrorEnabled())
						logger.error(mainResp.getCode()+"----"+mainResp.getMessage());
					respCode = this.getErrorEnum(msPayfrontError.getRespCode(), MsRespCode.class);
	
					txnInfo.setResponsCode(respCode.getCode());
					txnInfo.setResponsDesc(respCode.getMessage());
					throw new ProcessException(respCode.getCode(),respCode.getMessage());
				}
			}else{
				if(logger.isErrorEnabled())
					logger.error(mainResp.getCode() +"----"+ mainResp.getMessage());
				respCode = this.getErrorEnum(msPayfrontError.getRespCode(), MsRespCode.class);
				//设置核心响应码
				txnInfo.setResponsCode(respCode.getCode());
				txnInfo.setResponsDesc(respCode.getMessage());
				throw new ProcessException(respCode.getCode(),respCode.getMessage());
			}
		} catch(ProcessException pe){
			if(logger.isErrorEnabled())
				logger.error(pe.getErrorCode()+":"+pe.getMessage(), pe);
			throw pe;
		}
	}
	/**
	 * 实时代扣查询响应处理
	 * @param context
	 * @param mainResp
	 * @param msPayfrontError
	 */
	private void payCutPaymentQueryProc(TxnContext context, String payJson) {
		CcsOrder order = context.getOrder();
		CcsOrder origOrder = context.getOrigOrder();
		CcsAcctO accto = context.getAccounto();
		TxnInfo txnInfo = context.getTxnInfo();
		
		PayCutPaymentResp data = new PayCutPaymentResp();
		MsRespCode respCode = null;
		CommResp mainResp = null;
		CcsTxnPost txnPost = null;

		try {
			//发送支付指令
			String retJson = bankServiceForApsImpl.sendMsPayFront(payJson, txnInfo.getLoanUsage());
			if(logger.isDebugEnabled())
				logger.debug("loanProc,支付报文：[{}]",retJson);
			
			mainResp = JsonSerializeUtil.jsonReSerializerNoType(retJson, CommResp.class);
			
			//设置公共报文响应码，用于落订单表
			txnInfo.setPayRespCode(mainResp.getCode());
			txnInfo.setPayRespMessage(mainResp.getMessage());
			//公共报文响应码检查
			MsPayfrontError msPayfrontError = this.getErrorEnum(mainResp.getCode(), MsPayfrontError.class);
			txnInfo.setResponsCode(msPayfrontError.getRespCode());
			txnInfo.setResponsDesc(msPayfrontError.getDesc());
			
			//报文头响应码为0成功，需要检查报文体的响应码
			if(msPayfrontError == MsPayfrontError.S_0){
				// 报文头响应码为0成功，还需要判断报文体响应码
				servJsonUtil.setBeanProperty(mainResp, data);
				
				// 设置支付前置返回码，报文体响应码覆盖报文头响应码,用于设置订单状态
				txnInfo.setPayOthRespCode(data.getErrorCode());
				txnInfo.setPayOthRespMessage(data.getErrorMessage());
				txnInfo.setPayStatus(data.getStatus());
				// 公共部分为成功时，报文体响应码覆盖公共部分响应码
				msPayfrontError = this.getErrorEnum(data.getErrorCode(), MsPayfrontError.class);
				txnInfo.setResponsCode(msPayfrontError.getRespCode());
				txnInfo.setResponsDesc(msPayfrontError.getDesc());

				// 报文体响应码为0
				if (msPayfrontError == MsPayfrontError.S_0) {
					//更新授权未匹配金额
					this.updateAcctoMemoAmt(accto, txnInfo,context.getOrigOrder().getTxnType(),true);
					if(origOrder.getLoanUsage() == LoanUsage.S){
						//代位追偿只更新settleClaim ,不记录authmemo
						this.loadClaim(context, true);
						this.updateSettleClaim(context);//更新理赔追偿
					}
					else{
						this.updateAcctO(context);
						this.saveCcsAuthmemoO(context);	
						this.updateAuthmemo(context, AuthTransStatus.N);
						context.getAuthmemo().setTxnType(origOrder.getTxnType());//修正交易类型
		                context.getAuthmemo().setAuthCode(this.generateAuthCode(context, context.getAuthmemo().getLogKv().toString()));
		                if(origOrder.getLoanUsage() == LoanUsage.M){
		                	context.getLoanReg().setDdRspFlag(Indicator.Y);// 预约注册-扣款成功
		                	context.getLoanReg().setValidDate(txnInfo.getBizDate());
		                }
		                txnPost = savePosting(txnInfo);//入账记录
						txnPost.setRefNbr(origOrder.getRefNbr());//设置原订单的流水
					}
	                //生成授权号
					//更新订单与原订单表状态
					orderFacility.updateOrder(order, mainResp, data.getStatus(),OrderStatus.S,txnInfo.getLogKv(),txnInfo.getBizDate(),MsRespCode.E_0000);
					orderFacility.updateOrder(context.getOrigOrder(), mainResp, data.getStatus(),OrderStatus.S,txnInfo.getLogKv(),txnInfo.getBizDate(),MsRespCode.E_0000);
	
				}else{//失败  处理中
					if(logger.isErrorEnabled()){
						logger.error("实时代扣失败/处理中：[{}]  [{}]", mainResp.getCode(),mainResp.getMessage());
					}
					respCode = this.getErrorEnum(msPayfrontError.getRespCode(), MsRespCode.class);
				
					txnInfo.setResponsCode(respCode.getCode());
					txnInfo.setResponsDesc(respCode.getMessage());
					throw new ProcessException(respCode.getCode(),respCode.getMessage());
				}
			}else{
				if(logger.isErrorEnabled())
					logger.error(mainResp.getCode() +"----"+ mainResp.getMessage());
				respCode = this.getErrorEnum(msPayfrontError.getRespCode(), MsRespCode.class);
				//设置核心响应码
				txnInfo.setResponsCode(respCode.getCode());
				txnInfo.setResponsDesc(respCode.getMessage());
				throw new ProcessException(respCode.getCode(),respCode.getMessage());
			}
		} catch(ProcessException pe){
			if(logger.isErrorEnabled())
				logger.error(pe.getErrorCode()+":"+pe.getMessage(), pe);
			throw pe;
		}
	}
	/**
	 * 放款处理
	 * @param context
	 * @param payJson 
	 * @param mainResp
	 * @param msPayfrontError
	 */
	private void loanProc(TxnContext context, String payJson) {
		CcsOrder order = context.getOrder();
		TxnInfo txnInfo = context.getTxnInfo();
		CcsLoanReg loanReg = context.getLoanReg();
		CcsAcctO accto = context.getAccounto();
		PaySinPaymentResp data = new PaySinPaymentResp();
		MsRespCode respCode = null;
		CommResp mainResp = null;

		try {
			//发送支付指令
			String retJson = bankServiceForApsImpl.sendMsPayFront(payJson, txnInfo.getLoanUsage());
			if(logger.isDebugEnabled())
				logger.debug("loanProc,支付报文：[{}]",retJson);
			
			mainResp = JsonSerializeUtil.jsonReSerializerNoType(retJson, CommResp.class);
			
			//设置公共报文响应码，用于落订单表
			txnInfo.setPayRespCode(mainResp.getCode());
			txnInfo.setPayRespMessage(mainResp.getMessage());
			//公共报文响应码检查
			MsPayfrontError msPayfrontError = this.getErrorEnum(mainResp.getCode(), MsPayfrontError.class);
			txnInfo.setResponsCode(msPayfrontError.getRespCode());
			txnInfo.setResponsDesc(msPayfrontError.getDesc());
			
			//报文头响应码为0成功，需要检查报文体的响应码
			if(msPayfrontError == MsPayfrontError.S_0){
				// 报文头响应码为0成功，还需要判断报文体响应码
				servJsonUtil.setBeanProperty(mainResp, data);
				
				// 设置支付前置返回码，报文体响应码覆盖报文头响应码,用于设置订单状态
				txnInfo.setPayRespCode(data.getErrorCode());
				txnInfo.setPayRespMessage(data.getErrorMessage());
				txnInfo.setPayStatus(data.getStatus());
				// 公共部分为成功时，报文体响应码覆盖公共部分响应码
				msPayfrontError = this.getErrorEnum(data.getErrorCode(), MsPayfrontError.class);
				// 报文体响应码为0，放款成功
				if (msPayfrontError == MsPayfrontError.S_0) {
					orderFacility.updateOrder(order, mainResp, data.getStatus(),OrderStatus.S, txnInfo.getLogKv(),txnInfo.getBizDate(),MsRespCode.E_0000);
					if(loanReg.getLoanType()!=LoanType.MCAT){
						this.updateAcct(context);
					}
					this.updateLoanReg(txnInfo, loanReg,LoanRegStatus.S);
					this.updateAcctO(context);
					this.updateAcctoMemoAmt(accto, txnInfo,txnInfo.getTransType(),true);
					this.updateAuthmemo(context, AuthTransStatus.N);
					savePosting(txnInfo);// 入账记录
				} else {
					if (logger.isErrorEnabled())
						logger.error(mainResp.getCode()+"----"+mainResp.getMessage());
					respCode = this.getErrorEnum(msPayfrontError.getRespCode(), MsRespCode.class);
	
					txnInfo.setResponsCode(respCode.getCode());
					txnInfo.setResponsDesc(respCode.getMessage());
					throw new ProcessException(respCode.getCode(),respCode.getMessage());
				}
			}else{
				if(logger.isErrorEnabled())
					logger.error(mainResp.getCode() +"----"+ mainResp.getMessage());
				respCode = this.getErrorEnum(msPayfrontError.getRespCode(), MsRespCode.class);
				//设置核心响应码
				txnInfo.setResponsCode(respCode.getCode());
				txnInfo.setResponsDesc(respCode.getMessage());
				throw new ProcessException(respCode.getCode(),respCode.getMessage());
			}
		} catch(ProcessException pe){
			if(logger.isErrorEnabled())
				logger.error(pe.getMessage(), pe);
			throw pe;
		}
	}

	/**
	 * 更新贷款注册信息
	 * @param txnInfo
	 * @param loanReg
	 * @param loanRegStatus
	 */
	private void updateLoanReg(TxnInfo txnInfo, CcsLoanReg loanReg,LoanRegStatus loanRegStatus) {
		if(null == loanReg)
			return ;
		
		//放款成功时,更新生效日期
		if(LoanRegStatus.S == loanRegStatus){
			loanReg.setValidDate(txnInfo.getBizDate());// 生效日期为放款成功日期
		}
		if(txnInfo.getTransType() != AuthTransType.Inq){//查询交易成功/失败,不能修改loanReg的refNbr
			loanReg.setRefNbr(txnInfo.getRefNbr());//每次交易更新refNbr
		}
		loanReg.setLoanRegStatus(loanRegStatus);
	}

	/**
	 * 更新账户 账单日
	 * @param context
	 */
	private void updateAcct(TxnContext context) {
		CcsAcct acct = context.getAccount();
		CcsAcctO accto = context.getAccounto();
		TxnInfo txnInfo = context.getTxnInfo();
		LoanFeeDef loanFeeDef = context.getLoanFeeDef();
		LoanPlan loanPlan = context.getLoanPlan();
		AccountAttribute acctAttr = context.getAccAttr();
		//首笔放款成功后，更新账单日，下一账单日期
		if(null == acct.getLtdLoanAmt() || acct.getLtdLoanAmt().compareTo(BigDecimal.ZERO)==0){
		    acct = openAcctCommService.setNextStmtDate(acct, txnInfo, loanFeeDef, acctAttr,loanPlan);
		    acct.setFirstStmtDate(acct.getNextStmtDate());// 建账时为首个账单日期
		    acct.setPmtDueDate(microCreditRescheduleUtils.getNextPaymentDay(acct.getProductCd(),  acct.getNextStmtDate()));// 计算下个还款日期
		    acct.setGraceDate(microCreditRescheduleUtils.getNextGraceDay(acct.getProductCd(),acct.getNextStmtDate()));// 计算下个还款日期
//		    acct.setFirstRetlDate(txnInfo.getBizDate());//首次消费日期
		    
		    if(null != accto){
		    	accto.setCycleDay(acct.getCycleDay());
		    }
		}
		
		if(logger.isDebugEnabled()){
			logger.debug("首个账单日期:[{}]",acct.getFirstStmtDate());
			logger.debug("下一账单日期:[{}]",acct.getNextStmtDate());
			logger.debug("到期还款日期:[{}]",acct.getPmtDueDate());
			logger.debug("宽限日期:[{}]",acct.getGraceDate());
		}
	}

	/**
	 * 更新理赔代偿信息
	 * @param txnInfo
	 */
	private void updateSettleClaim(TxnContext context) {
		TxnInfo txnInfo = context.getTxnInfo();
		CcsSettleClaim claim = context.getClaim();
		claim.setCompensatoryAmt(claim.getCompensatoryAmt()==null? txnInfo.getTransAmt():claim.getCompensatoryAmt().add(txnInfo.getTransAmt()));
		claim.setLastCompensatoryDate(txnInfo.getBizDate());
	}
	
	/**
	 * 检查借据状态
	 * @param context
	 */
	public void checkLoan(TxnContext context) {
		if(logger.isDebugEnabled())
			logger.debug("检查借据状态--checkLoan");
		CcsLoan loan = context.getLoan();
		
		LogTools.printObj(logger, loan.getLoanStatus(), "贷款状态");
		
		if(loan.getLoanStatus()==LoanStatus.T ){
			throw new ProcessException(MsRespCode.E_1018.getCode(),MsRespCode.E_1018.getMessage());
		}else if(loan.getLoanStatus()==LoanStatus.F ){
			throw new ProcessException(MsRespCode.E_1019.getCode(),MsRespCode.E_1019.getMessage());
		}else if(loan.getLoanStatus()!=LoanStatus.A ){//一期不考虑展期缩期
			throw new ProcessException(MsRespCode.E_1015.getCode(),MsRespCode.E_1015.getMessage());
		}
	}

	/**
	 * 新增入账流水
	 * @param txnInfo
	 */
	public CcsTxnPost savePosting(TxnInfo txnInfo) {
		if(logger.isDebugEnabled())
			logger.debug("新增入账流水--savePosting");
		
		CcsTxnPost txnPost = new CcsTxnPost();
		txnPost.setMti(txnInfo.getMti());
		txnPost.setOrg(OrganizationContextHolder.getCurrentOrg());
		if(txnInfo.getInputSource().compareTo(InputSource.SUNS)==0){
			txnPost.setTxnSource("YG");
			txnPost.setAcqAddress("阳光");		
		}
		else{
			txnPost.setTxnSource("MS");
			txnPost.setAcqAddress("马上");		
		}
		txnPost.setB033FwdIns(FWD_INS);			
		txnPost.setSrcChnl(txnInfo.getInputSource());
		//查询交易 重新赋值交易类型，其他用txnInfo中的交易类型
		if(txnInfo.getLoanUsage() == LoanUsage.E
		 ||txnInfo.getLoanUsage() == LoanUsage.G){
			txnPost.setTxnType(AuthTransType.AgentDebit);			
		}
		else if(txnInfo.getLoanUsage() == LoanUsage.F){
			txnPost.setTxnType(AuthTransType.AgentCredit);			
		}
		else {
			txnPost.setTxnType(txnInfo.getTransType());			
		}
		txnPost.setTxnDirection(txnInfo.getTransDirection());
		txnPost.setCardNbr(txnInfo.getCardNo());
		txnPost.setTxnAmt(txnInfo.getTransAmt());
		txnPost.setTxnCurrenctCode(txnInfo.getCurrencyCode());
		txnPost.setSettAmt(txnInfo.getTransAmt());
		txnPost.setSettCurrencyCode(txnInfo.getCurrencyCode());
		txnPost.setTxnTime(txnInfo.getRequestTime().substring(4));
		txnPost.setRefNbr(txnInfo.getRefNbr());
		txnPost.setAuthCode(txnInfo.getAuthCode());
		txnPost.setAcqTermId("");
		txnPost.setAcqAcceptorId(txnInfo.getAcqId());
		txnPost.setMerchCategoryCode("");
		// 应付手续费
		txnPost.setTxnFeeAmt(BigDecimal.ZERO);
		// 应收手续费
		txnPost.setFeeProfit(BigDecimal.ZERO);
		txnPost.setB003ProcCode(txnInfo.getProcCode());
		txnPost.setB011Trace(txnInfo.getServiceSn().substring(txnInfo.getServiceSn().length()-6));//交易流水后6位随机数
		txnPost.setB022Entrymode("");
		txnPost.setB025Entrycond("");
		txnPost.setB032AcqInst(txnInfo.getAcqId());
		txnPost.setB039RtnCode(txnInfo.getResponsCode());
		txnPost.setB060("");
		txnPost.setB061("");
		txnPost.setBatchDate(txnInfo.getBizDate());
		txnPost.setComparedInd(Indicator.N);
		txnPost.setIsRevoke(Indicator.N);
		txnPost.setExpInd(Indicator.N);
		txnPost.setErrInd(Indicator.N);
		//txnPost.setAuthTxnTerminal(txnInfo.getAuthTransTerminal());
		/** 第三方交易终端-改造增加40域*/
		txnPost.setB040TermId("");
		
		txnPost = rCcsTxnPost.save(txnPost);
		
		return txnPost;
	}
	
	/**
	 * 
	 * 异常预处理
	 * @param e
	 * @param pe
	 * @param txnInfo
	 * @param retJson
	 */
	public ProcessException preException(Exception e,ProcessException pe,TxnInfo txnInfo){
		if(logger.isDebugEnabled())
			logger.debug("异常预处理--preException");
		ProcessException newPe = pe;
		if(e instanceof ProcessException){
			//支付调用异常特殊处理   为处理中
			if(StringUtils.equals(pe.getErrorCode(),MsPayfrontError.E_90001.getCode())
					||StringUtils.equals(pe.getErrorCode(),MsPayfrontError.E_90002.getCode())
					||StringUtils.equals(pe.getErrorCode(),MsPayfrontError.E_90003.getCode())){
				MsPayfrontError msPayfrontError = this.getErrorEnum(pe.getErrorCode(), MsPayfrontError.class);
				MsRespCode msRespCode = this.getErrorEnum(msPayfrontError.getRespCode(), MsRespCode.class);
				txnInfo.setPayRespCode(pe.getErrorCode());
				txnInfo.setPayRespMessage(pe.getMessage());
				txnInfo.setResponsCode(msRespCode.getCode());
				txnInfo.setResponsDesc(msRespCode.getMessage());
				newPe = new ProcessException(msRespCode.getCode(), msRespCode.getMessage());
			}else if(StringUtils.equals(pe.getErrorCode(),MsPayfrontError.E_90000.getCode())){//手动赋予修正结果，支付返回结果前面已经赋值，不能修改
				MsRespCode msRespCode = this.getErrorEnum(MsPayfrontError.E_90000.getRespCode(), MsRespCode.class);txnInfo.setResponsCode(MsRespCode.E_1001.getCode());
				txnInfo.setResponsDesc(msRespCode.getMessage());
				newPe = new ProcessException(msRespCode.getCode(), msRespCode.getMessage());
			}else if(StringUtils.isBlank(pe.getErrorCode())){//没有错误码，异常    
				txnInfo.setResponsCode(MsRespCode.E_9998.getCode());
				txnInfo.setResponsDesc(MsRespCode.E_9998.getMessage());//暂定
				newPe = new ProcessException(MsRespCode.E_9998.getCode(),MsRespCode.E_9998.getMessage());
			}else{
				txnInfo.setResponsCode(pe.getErrorCode());
				txnInfo.setResponsDesc(pe.getMessage());
			}
		}else{
			//系统异常
			txnInfo.setResponsCode(MsRespCode.E_9998.getCode());
			txnInfo.setResponsDesc(MsRespCode.E_9998.getMessage());//暂定
			txnInfo.setPayRespCode(MsRespCode.E_9998.getCode());
			txnInfo.setPayRespMessage(MsRespCode.E_9998.getMessage());//非支付错误，若已有订单，更新订单异常描述
			newPe = new ProcessException(MsRespCode.E_9998.getCode(),MsRespCode.E_9998.getMessage());
		}
		
		return newPe;
	}

	/**
	 * 异常处理
	 * @param context
	 * @param retJson
	 * @param e
	 */
	public void exceptionProc(TxnContext context, ProcessException pe) {
		if(logger.isDebugEnabled())
			logger.debug("异常处理--exceptionProc");
		TxnInfo txnInfo = context.getTxnInfo();
		
		if(StringUtils.isNotBlank(txnInfo.getPayRespCode())
				&&!StringUtils.equals(txnInfo.getPayRespCode(), MsPayfrontError.S_0.getCode())
				&&!StringUtils.equals(txnInfo.getPayRespCode(), MsRespCode.E_9998.getCode())){
			MsPayfrontError msPayfrontError = this.getErrorEnum(txnInfo.getPayRespCode(), MsPayfrontError.class);
			//代付处理中
			if(StringUtils.equals(msPayfrontError.getDecision(),DecisionCode.P.name())){
					setErrorInfo(context,AuthTransStatus.P,OrderStatus.W,LoanRegStatus.C);
			}else{//代付处理中 ,失败  
				setErrorInfo(context,AuthTransStatus.E,OrderStatus.E,LoanRegStatus.F);
			}
		}else{//非支付前置返回异常
			setErrorInfo(context,AuthTransStatus.E,OrderStatus.E,LoanRegStatus.F);
		}
		
	}
	
	/**
	 * 代扣代付查询交易异常处理
	 * @param context
	 * @param retJson
	 * @param ek
	 */
	public void queryExceptionProc(TxnContext context, ProcessException pe) {
		if(logger.isDebugEnabled())
			logger.debug("异常处理--exceptionProc");
		TxnInfo txnInfo = context.getTxnInfo();
		CommResp commResp = new CommResp();
		commResp.setCode(txnInfo.getPayRespCode());
		commResp.setMessage(txnInfo.getPayRespMessage());
		CcsOrder order = context.getOrder();
		
		//系统内部错误，保持原交易处理中状态，只更新查询流水为失败
		if( StringUtils.equals(txnInfo.getPayRespCode(), MsRespCode.E_9998.getCode())){
			MsRespCode msRespCode = null;
			if(StringUtils.isNotBlank(txnInfo.getResponsCode())){
				msRespCode = getErrorEnum(txnInfo.getResponsCode(), MsRespCode.class);
			}
			orderFacility.updateOrder(order, commResp,null,OrderStatus.E,null,txnInfo.getBizDate(),msRespCode);	
		}
		else if( null != txnInfo.getPayOthRespCode() ){		//如果业务返回码不为空则根据业务返回码做异常处理，否则根据报文外层返回码做异常处理

			//支付前置返回原交易处理状态 ，原交易处理结果非成功
			MsPayfrontError msPayfrontError = this.getErrorEnum(txnInfo.getPayOthRespCode(), MsPayfrontError.class);
			//处理结果为处理中
			if(StringUtils.equals(msPayfrontError.getDecision(),DecisionCode.P.name())){
				//原交易状态仍为处理中，不更新原交易，更新新交易流水
				orderFacility.updateOrder(order, commResp, null, OrderStatus.S, null, txnInfo.getBizDate(),MsRespCode.E_0000);	
			}else{//处理结果为处理失败
				//原交易状态处理失败
				setErrorInfo(context,AuthTransStatus.E,OrderStatus.E,LoanRegStatus.F);
			}
		}
		else if(StringUtils.isNotBlank(txnInfo.getPayRespCode())){
			//根据报文外层返回码做异常处理
//			MsPayfrontError msPayfrontError = this.getErrorEnum(txnInfo.getPayRespCode(), MsPayfrontError.class);
			//外层报文相应码中20001-未查找到对应支付业务流水 20003-未查找到对应批次流水
			//为原交易失败，其他情况不对原交易做处理
			if(StringUtils.equals(txnInfo.getPayRespCode(),MsPayfrontError.E_20001.getCode())
			|| StringUtils.equals(txnInfo.getPayRespCode(),MsPayfrontError.E_20003.getCode())){
				//原交易状态处理失败
				setErrorInfo(context,AuthTransStatus.E,OrderStatus.E,LoanRegStatus.F);
			}else{
				MsRespCode msRespCode = null;
				if(StringUtils.isNotBlank(txnInfo.getResponsCode())){
					msRespCode = getErrorEnum(txnInfo.getResponsCode(), MsRespCode.class);
				}
				//原交易状态仍为处理中，不更新原交易，更新新交易流水
				orderFacility.updateOrder(order, commResp, null, OrderStatus.E, null, txnInfo.getBizDate(),msRespCode);	
			}
		}
		else{
			MsRespCode msRespCode = null;
			if(StringUtils.isNotBlank(txnInfo.getResponsCode())){
				msRespCode = getErrorEnum(txnInfo.getResponsCode(), MsRespCode.class);
			}
			//支付前置未正常处理，保持原交易处理中状态，只更新查询流水为失败
			orderFacility.updateOrder(order, commResp,null,OrderStatus.E,null,txnInfo.getBizDate(),msRespCode);	
		}
	
	}
		

		
	
	/**
	 * 失败交易处理
	 * @param txnInfo2
	 * @param order
	 * @param authmemo
	 * @param loanReg
	 * @param txnInfo
	 * @param accto
	 */
	private void setErrorInfo(TxnContext context,AuthTransStatus authTransStatus,
			OrderStatus orderStatus, LoanRegStatus loanRegStatus) {
		if(logger.isDebugEnabled())
			logger.debug("异常处理--exceptionProc--authTransStatus：[{}],orderStatus:[{}],loanRegStatus[{}]",authTransStatus,orderStatus,loanRegStatus);
		
		CcsOrder origOrder = context.getOrigOrder();
		CcsOrder order = context.getOrder();
		CcsLoanReg loanReg = context.getLoanReg();
		TxnInfo txnInfo = context.getTxnInfo();
		CcsAcctO accto = context.getAccounto();
		CcsAuthmemoO authmemoO = context.getAuthmemo();
		//放款  失败  实时代扣失败  代偿失败
		
		if(txnInfo.getLoanUsage() == LoanUsage.A || txnInfo.getLoanUsage() == LoanUsage.L ){	
			//更新loanReg
			this.updateLoanReg(txnInfo, loanReg, loanRegStatus);
			
			//处理中 更新冻结金额  冻结在途借记
			if(null!=order && null != accto 
					&& (order.getOrderStatus() != OrderStatus.W && order.getOrderStatus() != OrderStatus.Q)
					&& (orderStatus == OrderStatus.W || orderStatus == OrderStatus.Q)
					&& txnInfo.getTransType() == AuthTransType.AgentDebit){//放款及重提交易
				this.updateAcctoMemoAmt(accto, txnInfo,txnInfo.getTransType(),true);
				this.updateAcctO(context);
			}
						
			//放款成功、失败都记memo  实时代扣和代位追偿只有成功时记memo
			if(null != authmemoO){
				this.updateAuthmemo(context,authTransStatus);
			}
		}
		else if( txnInfo.getLoanUsage() == LoanUsage.E ){
			//更新loanReg
			this.updateLoanReg(txnInfo, loanReg, loanRegStatus);

			//查询交易 ：若原订单是处理中，当前交易处理失败，则对未匹配金额进行反向处理
			if(null!=origOrder && null != accto 
					&& (origOrder.getOrderStatus() == OrderStatus.W || origOrder.getOrderStatus() == OrderStatus.Q)
					&& orderStatus == OrderStatus.E){ //原来是处理中，说明已经冻结
				this.updateAcctoMemoAmt(accto, txnInfo,origOrder.getTxnType(),false);
				this.updateAcctO(context);
			}
			//放款成功、失败都记memo  实时代扣和代位追偿只有成功时记memo
			if(null != authmemoO){
				this.updateAuthmemo(context,authTransStatus);
			}

		}
		//代扣交易，交易成功才会记memoCr
//		else if( txnInfo.getLoanUsage() == LoanUsage.F ){
//			//查询交易：若原订单是处理中，当前交易处理失败，则对未匹配金额进行反向处理
//			if(null!=origOrder && null != accto 
//					&& origOrder.getOrderStatus() == OrderStatus.W 
//					&& orderStatus == OrderStatus.E){ //原来是处理中，说明已经冻结
//				this.updateAcctoMemoAmt(accto, txnInfo,origOrder.getTxnType(),false);
//			}
//		}
		
		
		CommResp commResp = new CommResp();
		commResp.setCode(txnInfo.getPayRespCode());
		commResp.setMessage(txnInfo.getPayRespMessage());
		
		//代扣 代付查询 更新原订单状态
		if(txnInfo.getLoanUsage() == LoanUsage.F
		|| txnInfo.getLoanUsage() == LoanUsage.E
		|| txnInfo.getLoanUsage() == LoanUsage.G){
			CommResp othResp = new CommResp();
			othResp.setCode(txnInfo.getPayOthRespCode());
			othResp.setMessage(txnInfo.getPayOthRespMessage());
			
			MsRespCode msRespCode = null;
			if(StringUtils.isNotBlank(txnInfo.getPayOthRespCode())){
				MsPayfrontError mspay = getErrorEnum(txnInfo.getPayOthRespCode(), MsPayfrontError.class);
				msRespCode = getErrorEnum(mspay.getRespCode(), MsRespCode.class);
			}
			orderFacility.updateOrder(context.getOrigOrder(), othResp, txnInfo.getPayStatus(),orderStatus,txnInfo.getLogKv(),txnInfo.getBizDate(),msRespCode);	
			
			orderFacility.updateOrder(context.getOrder(), commResp, null,OrderStatus.S,txnInfo.getLogKv(),txnInfo.getBizDate(),MsRespCode.E_0000);	
		}
		else{
			MsRespCode msRespCode = null;
			if(StringUtils.isNotBlank(txnInfo.getResponsCode())){
				msRespCode = getErrorEnum(txnInfo.getResponsCode(), MsRespCode.class);
			}
			//其他交易更新订单状态
			orderFacility.updateOrder(context.getOrder(), commResp, txnInfo.getPayStatus(),orderStatus,txnInfo.getLogKv(),txnInfo.getBizDate(),msRespCode);
		}
	}

	/**
	 * 更新联机流水
	 * @param authmemo
	 * @param authTransStatus
	 */
	public void updateAuthmemo(TxnContext context, AuthTransStatus authTransStatus) {
		if(logger.isDebugEnabled())
			logger.debug("更新联机流水--updateAuthmemo");
		CcsAuthmemoO authmemo = context.getAuthmemo();
		CcsAcctO accto = context.getAccounto();
		TxnInfo txnInfo = context.getTxnInfo();
		
		if(null != authmemo){
			authmemo.setAuthTxnStatus(authTransStatus);
			authmemo.setRejReason("");
			authmemo.setB039RtnCode(txnInfo.getResponsCode());
			
			//查询交易使用payOth更新原交易
//			if(txnInfo.getTransType() == AuthTransType.Inq){
//				authmemo.setRejReason(txnInfo.getPayOthRespMessage());
//			}
			
			if(authTransStatus == AuthTransStatus.N){
				//生成授权号
				authmemo.setAuthCode(this.generateAuthCode(context, context.getAuthmemo().getLogKv().toString()));
				authmemo.setFinalAction(AuthAction.A);//通过
				authmemo.setFinalUpdDirection(txnInfo.getDirection().name());
				authmemo.setFinalUpdAmt(txnInfo.getTransAmt());
				
			}else if(authTransStatus == AuthTransStatus.E){
				authmemo.setFinalAction(AuthAction.D);//拒绝
			}
			if(null != accto){
				authmemo.setMemoCr(accto.getMemoCr());
				authmemo.setMemoDb(accto.getMemoDb());
			}
		}
		
		context.setAuthmemo(authmemo);
	}
	/**
	 * 保持联机交易流水
	 * 
	 * @return
	 */
	public CcsAuthmemoO saveCcsAuthmemoO(TxnContext context) {
		if(logger.isDebugEnabled())
			logger.debug("保持联机交易流水--saveCcsAuthmemoO");
		CcsAuthmemoO um = new CcsAuthmemoO();
		um.setOrg(OrganizationContextHolder.getCurrentOrg());
		if (null != context.getAccounto()) {
			CcsAcctO accto = context.getAccounto();
			um.setAcctNbr(accto.getAcctNbr());
			um.setAcctType(accto.getAcctType());
			um.setCurrBal(accto.getCurrBal());
			um.setMemoCr(accto.getMemoCr());
			um.setMemoDb(accto.getMemoDb());
			um.setProductCd(accto.getProductCd());

			um.setAcctBlockcode(accto.getBlockCode());
		}
		
		if (null != context.getAccount()) {
			CcsAcct acct = context.getAccount();
			um.setB002CardNbr(acct.getDefaultLogicCardNbr());
		}
		
		if (null != context.getProductCredit()) {
			ProductCredit productCredit = context.getProductCredit();
			um.setProductCd(productCredit.productCd);
		}
		if (null != context.getTxnInfo()) {
			TxnInfo txnInfo = context.getTxnInfo();
			um.setLogOlTime(txnInfo.getSysOlTime());
//			um.setB039RtnCode(txnInfo.getResponsCode());
		
			um.setManualAuthFlag(txnInfo.getManualAuthFlag());
			um.setChbTxnAmt(txnInfo.getTransAmt());
			um.setChbCurrency(txnInfo.getCountryCd());
			um.setTxnAmt(txnInfo.getTransAmt());
			um.setTxnCurrency(txnInfo.getCountryCd());
			um.setTxnType(txnInfo.getTransType());
			
			um.setCustOtb(txnInfo.getCustomerOTB());
			um.setCashOtb(txnInfo.getCashOTB());
			um.setOtb(txnInfo.getAccountOTB());
			um.setLogBizDate(txnInfo.getBizDate());
			um.setTxnDirection(txnInfo.getTransDirection());
			um.setRejReason("");
			um.setFinalAuthAmt(BigDecimal.ZERO);
			um.setFinalUpdDirection(txnInfo.getDirection().name());
			um.setCashAmt(txnInfo.getTransAmt());
			
			um.setAuthTxnStatus(txnInfo.getAuthTransStatus());
			um.setSubTerminalType(txnInfo.getSubTerminalType());
			
			um.setInputSource(txnInfo.getInputSource());
			um.setB004Amt(this.getB004(txnInfo.getTransAmt()));
			
			um.setB007TxnTime(txnInfo.getRequestTime().substring(4));//MMddHHmmss
			um.setB011Trace(txnInfo.getServiceSn().substring(txnInfo.getServiceSn().length()-6));//交易流水后6位随机数
			um.setB037RefNbr(txnInfo.getRefNbr());//交易流水号
			um.setB032AcqInst(txnInfo.getAcqId());
			um.setB033FwdIns(FWD_INS);
			um.setFwdInstId(FWD_INS);
			um.setGuarantyId(txnInfo.getGuarantyid());
			um.setRequestTime(txnInfo.getRequestTime());
			
			um.setMti(txnInfo.getMti());
			um.setB003ProcCode(txnInfo.getProcCode());
			um.setInputSource(txnInfo.getInputSource());
			um.setLogicCardNbr(txnInfo.getCardNo());
		}
		um.setVipStatus(null);
		um = rCcsAuthmemoO.save(um);
		context.getTxnInfo().setLogKv(um.getLogKv());
		context.setAuthmemo(um);
		return um;
	}
	
	/**
	 * 设置b004
	 * @param txnAmt
	 * @return
	 */
	public BigDecimal getB004(BigDecimal txnAmt) {
		BigDecimal b004 = new BigDecimal(0);
		try{
			DecimalFormat df = new DecimalFormat("########.00");
	        String dff = df.format(txnAmt);
	        dff = dff.replace(".", "");
	        b004 = new BigDecimal(dff);
		}catch (Exception e){
			//不处理
			b004 = txnAmt;
		}
        return b004;
	}
	
	/**
	 * 生成授权码
	 * @return
	 */
	public String generateAuthCode(TxnContext context, String txnSeq) {
		if(logger.isDebugEnabled())
			logger.debug("生成授权码--generateAuthCode memo主键：[{}]",txnSeq);
		TxnInfo txnInfo = context.getTxnInfo();
		String ac = "";
		if (txnInfo != null && StringUtils.equals(txnInfo.getResponsCode(), MsRespCode.E_0000.getCode())) {
			ac = txnSeq.length() < 6 ? String.format("%06d", Integer.valueOf(txnSeq)) : StringUtils.right(txnSeq, 6);
		}
		if(logger.isDebugEnabled())
			logger.debug("授权码-autCode:[{}]",ac);
		txnInfo.setAuthCode(ac);
		return ac;
	}
	

	/**
	 * 生成贷款注册信息
	 * @param term 贷款期数
	 * @param initPrin 贷款总金额
	 * @param refNbr  
	 * @param logicCardNbr 逻辑号
	 * @param cardNbr  卡号
	 * @param LoanPlan
	 * @param acctNbr TODO
	 * @param acctType TODO
	 * @return
	 * @throws ProcessException   
	 * @exception   
	 * @since  1.0.0
	 */
	public CcsLoanReg genLoanReg(Integer term, BigDecimal initPrin, String refNbr, String logicCardNbr, 
			String cardNbr, Long acctNbr, AccountType acctType, String loanCode, Date busDate) throws ProcessException {
		if(logger.isDebugEnabled())
			logger.debug("生成贷款注册信息--genLoanReg");
		CcsLoanReg loanReg = new CcsLoanReg();
		loanReg.setOrg(OrganizationContextHolder.getCurrentOrg());
		loanReg.setAcctNbr(acctNbr);
		loanReg.setAcctType(acctType);
		loanReg.setRegisterDate(busDate);
		loanReg.setRequestTime(new Date());
		loanReg.setLogicCardNbr(logicCardNbr);
		loanReg.setCardNbr(cardNbr);
		loanReg.setRefNbr(refNbr);
		loanReg.setLoanType(LoanType.MCEI);
		loanReg.setLoanRegStatus(LoanRegStatus.C);
		loanReg.setLoanInitTerm(term);// 分期期数
		loanReg.setLoanInitPrin(initPrin);// 分期总本金
		loanReg.setLoanCode(loanCode);//分期计划代码
		loanReg.setLoanAction(LoanAction.A);
		loanReg.setMatched(Indicator.N);
		loanReg.setInterestRate(BigDecimal.ZERO);
		loanReg.setFloatRate(BigDecimal.ZERO);
		
		loanReg.setLoanFixedPmtPrin(BigDecimal.ZERO);
		loanReg.setLoanFirstTermPrin(BigDecimal.ZERO);
		loanReg.setLoanFinalTermPrin(BigDecimal.ZERO);
		loanReg.setLoanInitFee(BigDecimal.ZERO);// 分期总手续费
		loanReg.setLoanFirstTermFee(BigDecimal.ZERO);// 分期首期手续费
		loanReg.setLoanFixedFee(BigDecimal.ZERO);// 分期每期手续费
		loanReg.setLoanFinalTermFee(BigDecimal.ZERO);// 分期末期手续费
		loanReg.setLoanFeeMethod(LoanFeeMethod.F);
		loanReg.setOrigTxnAmt(BigDecimal.ZERO);// 原始交易金额
		//申请信息如果有保费费率,印花税率、寿险计划包费率，保存到reg中
		//这里不考虑参数变更
		loanReg.setInsuranceRate(BigDecimal.ZERO);
		loanReg.setLoanFirstTermPrin(BigDecimal.ZERO);
		loanReg.setLoanFixedPmtPrin(BigDecimal.ZERO);
		loanReg.setLoanFinalTermPrin(BigDecimal.ZERO);
		loanReg.setLoanInitFee(BigDecimal.ZERO);// 分期总手续费
		loanReg.setLoanFirstTermFee(BigDecimal.ZERO);// 分期首期手续费
		loanReg.setLoanFixedFee(BigDecimal.ZERO);// 分期每期手续费
		loanReg.setLoanFinalTermFee(BigDecimal.ZERO);// 分期末期手续费
		loanReg.setOrigTxnAmt(BigDecimal.ZERO);// 原始交易金额
		
		loanReg.setInterestRate(BigDecimal.ZERO);//基础利率
		loanReg.setLoanFeeMethod(LoanFeeMethod.F);//分期手续费收取方式
		loanReg.setLifeInsuFeeRate(BigDecimal.ZERO);//寿险计划包费率
		loanReg.setLifeInsuFeeMethod(LoanFeeMethod.F);//寿险计划包费收取方式
		loanReg.setInsuranceRate(BigDecimal.ZERO);//保险月费率
		loanReg.setLoanInsFeeMethod(LoanFeeMethod.F);//保险费收取方式
		loanReg.setStampdutyRate(BigDecimal.ZERO);//印花税率
		loanReg.setStampdutyMethod(LoanFeeMethod.F);//印花税率收取方式
		loanReg.setCompoundRate(BigDecimal.ZERO);//复利利率
		loanReg.setPenaltyRate(BigDecimal.ZERO);//罚息利率
		//总保费=贷款金额*总期数*保费率
		loanReg.setInsuranceAmt(BigDecimal.ZERO);
		//总寿险计划包费
		loanReg.setTotLifeInsuAmt(BigDecimal.ZERO);
		//总手续费
		loanReg.setLoanInitFee(BigDecimal.ZERO);
		//总印花税
		loanReg.setStampdutyAmt(BigDecimal.ZERO);
		//代收服务费费率
		loanReg.setReplaceSvcFeeRate(BigDecimal.ZERO);
		//代收服务费固定金额
		loanReg.setReplaceSvcFeeAmt(BigDecimal.ZERO);
		//总代收服务费
		loanReg.setTotReplaceSvcFee(BigDecimal.ZERO);
		
		loanReg.setStampdutyMethod(LoanFeeMethod.F);
		loanReg.setLoanInsFeeMethod(LoanFeeMethod.E);
		
		return loanReg;
	}
	
	/**
	 * 获取小额贷期数<br>
	 * <b>判断期数是否在计价方式区间内</b>
	 * @param loanInitTerm
	 * @param loanPlan
	 * @return
	 * @throws AuthException
	 */
	public LoanFeeDef getLoanFeeDef(Integer loanInitTerm, LoanPlan loanPlan) throws ProcessException{
		if(logger.isDebugEnabled())
			logger.debug("获取小额贷期数定价--getLoanFeeDef");
		if (loanPlan.loanType == LoanType.MCEI || loanPlan.loanType == LoanType.MCEP) {
			if ((loanPlan.minCycle != null && loanInitTerm < loanPlan.minCycle) || (loanPlan.maxCycle != null && loanInitTerm > loanPlan.maxCycle)) {
				throw new ProcessException("报文上送的小额贷期数不在项目活动小额贷期数列表中");
			}
		}
		
		int key = -1;
		for (int i : loanPlan.loanFeeDefMap.keySet()) {
			// 期数相等直接返回
			if (i == loanInitTerm) {
				key = i;
				break;
			} else {
				// 判断期数，获取当前期数的下一个期数
				if (i > loanInitTerm) {
					if (key == -1) {
						key = i;
					} else {
						if (i < key) {
							key = i;
						}
					}
				}
			}
		}
		if (key == -1) {
			throw new ProcessException("报文上送的小额贷期数不在项目活动小额贷期数列表中");
		}
		return loanPlan.loanFeeDefMap.get(key);
	}
	
	/**
	 * 检查放款OTB
	 * @param context
	 */
	public void valiLoanOtb(TxnContext context){
		if(logger.isDebugEnabled())
			logger.debug("检查放款otb--valiLoanOtb");
		TxnInfo txnInfo = context.getTxnInfo();
		CcsAcct acct = context.getAccount();
		CcsAcctO accto = context.getAccounto();
		LoanPlan loanPlan = context.getLoanPlan();
		BigDecimal loanOTB = BigDecimal.ZERO;
		
		if(loanPlan.loanMold.equals(LoanMold.C)){
			loanOTB = accountOTB.acctCashLoanOTB(txnInfo.getCardNo(), acct.getAcctType(), txnInfo.getBizDate());
		
			if(logger.isDebugEnabled())
				logger.debug("循环贷款--loanOTB:[{}]",loanOTB);
		}else{
			//账户额度-历史最高余额-未入账借记金额 = 账户可用金额
			loanOTB = accountOTB.loanNoCycleOTB(accto, acct);
			
			if(logger.isDebugEnabled())
				logger.debug("非循环贷款--loanOTB:[{}]",loanOTB);
		}
		
		if(txnInfo.getTransAmt().compareTo(loanOTB)>0){
			throw new ProcessException(MsRespCode.E_1010.getCode(),MsRespCode.E_1010.getMessage());
		}
		
		txnInfo.setAccountOTB(loanOTB);
	}
	
	/**
	 * 检查开户行号
	 * @param bankCode
	 */
	public void checkBankCode(String bankCode){
		try{
			MsPayBank.valueOf("B_"+bankCode);
		}catch (Exception e){
			if (logger.isErrorEnabled())
				logger.error("开户行号错误[{"+bankCode+"}]", e);

			throw new ProcessException(MsRespCode.E_1043.getCode(),MsRespCode.E_1043.getMessage()+",字段名称{BANKCODE},不支持"+bankCode);
		}
	}
	
	/**
	 * 获取异常枚举
	 * @param value
	 * @param clazz
	 * @return
	 */
	public <T extends Enum<T>> T getErrorEnum(String value, Class<T> clazz) {
		try{
			String tip = "S_";
			if(!StringUtils.equals("0", value)){
				tip = "E_";
			}
			
			return Enum.valueOf(clazz, tip + value);
		}catch(Exception e){
			if (logger.isErrorEnabled())
				logger.error("无效响应码[{"+value+"}]", e);

			throw new ProcessException(MsPayfrontError.E_90000.getCode(),MsPayfrontError.E_90000.getDesc());
		}
	}
	
	/**
	 * 更新联机账户信息
	 * @param context
	 */
	public void updateAcctO(TxnContext context){
		CcsAcctO accto = context.getAccounto();
		TxnInfo txnInfo = context.getTxnInfo();
		if(null == accto.getLastUpdateBizDate()
				||accto.getLastUpdateBizDate().before(txnInfo.getBizDate())){
			accto.setLastUpdateBizDate(txnInfo.getBizDate());
		}
	}
	
	/**
	 * 根据证件号，证件类型加载客户信息
	 * 
	 * @param context
	 * @param nullException
	 */
	public CcsCustomer loadCustomerByIdNo(TxnContext context, boolean nullException) {
		if(logger.isDebugEnabled())
			logger.debug("加载客户信息--loadCustomer");
		TxnInfo txnInfo = context.getTxnInfo();
		try {
			QCcsCustomer qCcsCustomer = QCcsCustomer.ccsCustomer;
			CcsCustomer customer = rCcsCustomer.findOne(qCcsCustomer.idNo.eq(txnInfo.getIdNo()).
					and(qCcsCustomer.idType.eq(txnInfo.getIdType())).
					and(qCcsCustomer.org.eq(CheckUtil.addCharForNum(txnInfo.getOrg(), 12, " "))));
			if (nullException && null == customer) {
				throw new ProcessException(MsRespCode.E_1023.getCode(), MsRespCode.E_1023.getMessage());
			}
			
			return customer;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1023.getCode(), MsRespCode.E_1023.getMessage());
		}
	}
	
	/**
	 * 根据证件号，证件类型加载客户信息
	 * 
	 * @param context
	 * @param nullException
	 */
	public CcsCustomer loadCustomerByUuid(TxnContext context, boolean nullException) {
		if(logger.isDebugEnabled())
			logger.debug("加载客户信息--loadCustomer");
		TxnInfo txnInfo = context.getTxnInfo();
		try {
			QCcsCustomer qCcsCustomer = QCcsCustomer.ccsCustomer;
			CcsCustomer customer = rCcsCustomer.findOne(qCcsCustomer.internalCustomerId.eq(txnInfo.getUuid()).and(
					qCcsCustomer.org.eq(CheckUtil.addCharForNum(txnInfo.getOrg(), 12, " "))));
			if (nullException && null == customer) {
				throw new ProcessException(MsRespCode.E_1023.getCode(), MsRespCode.E_1023.getMessage());
			}
			return customer;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1023.getCode(), MsRespCode.E_1023.getMessage());
		}
	}
	
	/**
	 * 老客户申请，uuid和证件号/证件类型不匹配
	 * @param context
	 */
	public void checkCustomer(TxnContext context){
		TxnInfo txnInfo = context.getTxnInfo();
		
		CcsCustomer customer1 = this.loadCustomerByIdNo(context, false);
		CcsCustomer customer2 = this.loadCustomerByUuid(context, false);
		
		if(null != customer1){
			if (logger.isDebugEnabled())
				logger.debug("老客户byid-- idType:[{}],certid:[{}],custId[{}],internalCustomerId[{}]",
						customer1.getIdType(), customer1.getIdNo(),
						customer1.getCustId(), customer1.getInternalCustomerId());
			if(!StringUtils.equals(customer1.getInternalCustomerId(),txnInfo.getUuid())){
				throw new ProcessException(MsRespCode.E_1023.getCode(), MsRespCode.E_1023.getMessage()+",DATAID和原客户信息不一致");
			}
		}
		
		if(null != customer2){
			if (logger.isDebugEnabled())
				logger.debug("老客户byuuid idType:[{}],certid:[{}],custId[{}],internalCustomerId[{}]",
						customer2.getIdType(), customer2.getIdNo(),
						customer2.getCustId(), customer2.getInternalCustomerId());
			if(!StringUtils.equals(customer2.getIdNo(),txnInfo.getIdNo())
					|| customer2.getIdType() != txnInfo.getIdType()){
				throw new ProcessException(MsRespCode.E_1023.getCode(), MsRespCode.E_1023.getMessage()+",CERTID/CERTTYPE和原客户信息不一致");
			}
		}
		
		//设置客户号
		if(null != customer1 && null != customer2){
			txnInfo.setCustId(customer1.getCustId());
		}
		
	}
}
