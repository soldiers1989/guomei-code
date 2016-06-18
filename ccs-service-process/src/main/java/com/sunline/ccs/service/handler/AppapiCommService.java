package com.sunline.ccs.service.handler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.serializable.JsonSerializeUtil;
import com.sunline.ccs.facility.FeeTrailUtil;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.facility.ServJsonUtil;
import com.sunline.ccs.facility.contract.AcctUnpostAmtCalUtil;
import com.sunline.ccs.facility.order.OrderFacility;
import com.sunline.ccs.facility.order.MerchandiseFacility;
import com.sunline.ccs.facility.order.TrustLoanTxnFacility;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctO;
import com.sunline.ccs.infrastructure.server.repos.RCcsAuthmemoO;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardO;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanRegHst;
import com.sunline.ccs.infrastructure.server.repos.RCcsPlan;
import com.sunline.ccs.infrastructure.server.repos.RCcsRepaySchedule;
import com.sunline.ccs.infrastructure.server.repos.RCcsTrustLoanSchedule;
import com.sunline.ccs.infrastructure.server.repos.RCcsTrustLoanTxn;
import com.sunline.ccs.infrastructure.server.repos.RCcsTxnPost;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsMerchandiseOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsOrderHst;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.CcsSettleClaim;
import com.sunline.ccs.infrastructure.shared.model.CcsSettleLoanHst;
import com.sunline.ccs.infrastructure.shared.model.CcsTrustLoanSchedule;
import com.sunline.ccs.infrastructure.shared.model.CcsTrustLoanTxn;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnPost;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardO;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsMerchandiseOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnPost;
import com.sunline.ccs.otb.AcctOTB;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.BlockCode;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.TxnFee;
import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.query.TNTLMCATWithdrawCalc;
import com.sunline.ccs.service.msentity.TNMTrustLoanSchedReqSubPlan;
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
public class AppapiCommService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private RCcsLoan rCcsLoan;
	@Autowired
	private RCcsLoanReg rCcsLoanReg;
	@Autowired
	private RCcsLoanRegHst rCcsLoanRegHst;
	@Autowired
	private RCcsAcct rCcsAcct;
	@Autowired
	private RCcsAcctO rCcsAccto;
	@Autowired
	private RCcsCustomer rCcsCustomer;
	@Autowired
	private QueryCommService queryCommService;
	@Autowired
	private RCcsAuthmemoO rCcsAuthmemoO;
	@Autowired
	private RCcsRepaySchedule rCcsRepaySchedule;
	@Autowired
	private RCcsPlan rCcsPlan;
	@Autowired
	private RCcsTxnPost rCcsTxnPost;
	@Autowired
	private RCcsCardO rCcsCardO;
	@Autowired
	private GlobalManagementService globalManagementService;
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
	private FeeTrailUtil calculator;
//	@Autowired
//	private UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	@Resource(name = "bankService")
	private BankServiceForAps bankServiceForApsImpl;
	@Autowired
	private OpenAcctCommService openAcctCommService;
	@Autowired
	private MerchandiseFacility MerchandiseFacility;
	@Autowired
	private TNTLMCATWithdrawCalc tNTLMCATWithdrawCalc;
	@Autowired
	private AcctUnpostAmtCalUtil acctUnpostAmtCalUtil;
	@Autowired
	private TrustLoanTxnFacility trustLoanTxnFacility;
	@Autowired
	private RCcsTrustLoanTxn rCcsTrustLoanTxn;
	@Autowired
	private RCcsTrustLoanSchedule rCcsTrustLoanSchedule;
	@Autowired
	private TrustLoanTxnFacility loanTxnFacility;

	//受理机构号，目前硬编码
	private static final String FWD_INS = "99999999";
	private final static String MCAT_LOANING_TXN_CD = "L835";

	/**
	 * 加载贷款注册信息
	 * @param context
	 * @param loanRegStatus
	 * @param nullException
	 */
	public void loadLoanRegByContrNo(TxnContext context,String refNbr,LoanRegStatus loanRegStatus, LoanAction loanAction, boolean nullException) {
		if(logger.isDebugEnabled())
			logger.debug("加载贷款注册信息--loadLoanReg");
		TxnInfo txnInfo = context.getTxnInfo();
		try {
			QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;
			CcsLoanReg loanReg = null;
			BooleanExpression expression = qCcsLoanReg.contrNbr.eq(txnInfo.getContrNo());
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
	 * 加载贷款注册信息
	 * @param context
	 * @param loanRegStatus
	 * @param nullException
	 */
	public void loadLoanRegByDueBillNo(TxnContext context,String refNbr,LoanRegStatus loanRegStatus, LoanAction loanAction, boolean nullException) {
		if(logger.isDebugEnabled())
			logger.debug("加载贷款注册信息--loadLoanReg");
		TxnInfo txnInfo = context.getTxnInfo();
		try {
			QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;
			CcsLoanReg loanReg = null;
			BooleanExpression expression = qCcsLoanReg.dueBillNo.eq(txnInfo.getDueBillNo());
			if(null != loanRegStatus){
				expression = expression.and(qCcsLoanReg.loanRegStatus.eq(loanRegStatus));
			}
			if(null != loanAction){
				expression = expression.and(qCcsLoanReg.loanAction.eq(loanAction));
			}
			if(null != txnInfo.getContrNo()){
				expression = expression.and(qCcsLoanReg.contrNbr.eq(txnInfo.getContrNo()));
			}
			if(StringUtils.isNotBlank(refNbr)){
				expression = expression.and(qCcsLoanReg.refNbr.eq(refNbr));
			}
			
			loanReg = rCcsLoanReg.findOne(expression);
			
			if (nullException && null == loanReg) {
				throw new ProcessException(MsRespCode.E_1011.getCode(),MsRespCode.E_1011.getMessage());
			}
			
			if(null != loanReg){
				txnInfo.setAcctNbr(null == txnInfo.getAcctNbr()?loanReg.getAcctNbr():txnInfo.getAcctNbr());
				txnInfo.setAcctType(null == txnInfo.getAcctType()?loanReg.getAcctType():txnInfo.getAcctType());
				txnInfo.setDueBillNo(StringUtils.isBlank(txnInfo.getDueBillNo())?loanReg.getDueBillNo():txnInfo.getDueBillNo());
				txnInfo.setLoanCode(StringUtils.isBlank(txnInfo.getLoanCode())?loanReg.getLoanCode():txnInfo.getLoanCode());
				txnInfo.setContrNo(StringUtils.isBlank(txnInfo.getContrNo())?loanReg.getContrNbr():txnInfo.getContrNo());
				txnInfo.setLoanCode(loanReg.getLoanCode());
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
	public void loadLoanByDueBillNo(TxnContext context, LoanStatus loanStatus, boolean nullException) {
		if(logger.isDebugEnabled())
			logger.debug("加载贷款信息--loadLoan");
		TxnInfo txnInfo = context.getTxnInfo();
		try {
			QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
			BooleanExpression expression = qCcsLoan.dueBillNo.eq(txnInfo.getDueBillNo());
			
			if(null != loanStatus){
				expression = expression.and(qCcsLoan.loanStatus.eq(loanStatus));
			}
			if(null != txnInfo.getContrNo()){
				expression = expression.and(qCcsLoan.contrNbr.eq(txnInfo.getContrNo()));
			}
			CcsLoan loan = rCcsLoan.findOne(expression);
			
			if (nullException && null == loan) {
				throw new ProcessException(MsRespCode.E_1015.getCode(), MsRespCode.E_1015.getMessage());
			}
			
			if(null != loan){
				txnInfo.setAcctNbr(null == txnInfo.getAcctNbr()?loan.getAcctNbr():txnInfo.getAcctNbr());
				txnInfo.setAcctType(null == txnInfo.getAcctType()?loan.getAcctType():txnInfo.getAcctType());
				txnInfo.setDueBillNo(StringUtils.isBlank(txnInfo.getDueBillNo())?loan.getDueBillNo():txnInfo.getDueBillNo());
				txnInfo.setLoanCode(StringUtils.isBlank(txnInfo.getLoanCode())?loan.getLoanCode():txnInfo.getLoanCode());
				txnInfo.setContrNo(StringUtils.isBlank(txnInfo.getContrNo())?loan.getContrNbr():txnInfo.getContrNo());
				txnInfo.setLoanCode(loan.getLoanCode());
			}
			context.setLoan(loan);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1015.getCode(), MsRespCode.E_1015.getMessage());
		}
	}
	
	/**
	 * 加载贷款信息
	 * 
	 * @param context
	 * @param nullException
	 */
	public void loadLoanByContrNo(TxnContext context, LoanStatus loanStatus, boolean nullException) {
		if(logger.isDebugEnabled())
			logger.debug("加载贷款信息--loadLoan");
		TxnInfo txnInfo = context.getTxnInfo();
		try {
			QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
			BooleanExpression expression = qCcsLoan.contrNbr.eq(txnInfo.getContrNo());
			if(null != loanStatus){
				expression = expression.and(qCcsLoan.loanStatus.eq(loanStatus));
			}
			
			CcsLoan loan = rCcsLoan.findOne(expression);
			
			if (nullException && null == loan) {
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
	 * 加载联机卡信息
	 * @param context
	 * @param nullException
	 */
	public void loadCardOByAcct(TxnContext context, boolean nullException){
		if(logger.isDebugEnabled())
			logger.debug("加载卡信息--loadCard");
		CcsAcct acct = context.getAccount();
		TxnInfo txnInfo = context.getTxnInfo();
		try {
			QCcsCardO qCcsCardo = QCcsCardO.ccsCardO;
			CcsCardO cardo = rCcsCardO.findOne(qCcsCardo.acctNbr.eq(acct.getAcctNbr()));

			if (nullException && null == cardo) {
				throw new ProcessException(MsRespCode.E_1003.getCode(), MsRespCode.E_1003.getMessage());
			}
			
			if(null != cardo)
				txnInfo.setCardNo(cardo.getLogicCardNbr());
			context.setCardo(cardo);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1003.getCode(), MsRespCode.E_1003.getMessage());
		}
	}
	
	/**
	 * 加载贷款注册信息
	 * @param context
	 * @param loanRegStatus
	 * @param nullException
	 */
	public CcsLoanReg loadLoanRegByDueBillNo(TxnContext context,String refNbr,LoanRegStatus loanRegStatus, LoanAction loanAction) {
		if(logger.isDebugEnabled())
			logger.debug("加载贷款注册信息--loadLoanReg");
		TxnInfo txnInfo = context.getTxnInfo();
		try {
			QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;
			CcsLoanReg loanReg = null;
			BooleanExpression expression = qCcsLoanReg.dueBillNo.eq(txnInfo.getDueBillNo());
			if(null != loanRegStatus){
				expression = expression.and(qCcsLoanReg.loanRegStatus.eq(loanRegStatus));
			}
			if(null != loanAction){
				expression = expression.and(qCcsLoanReg.loanAction.eq(loanAction));
			}
			if(null != txnInfo.getContrNo()){
				expression = expression.and(qCcsLoanReg.contrNbr.eq(txnInfo.getContrNo()));
			}
			if(StringUtils.isNotBlank(refNbr)){
				expression = expression.and(qCcsLoanReg.refNbr.eq(refNbr));
			}
			
			loanReg = rCcsLoanReg.findOne(expression);
			return loanReg;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1011.getCode(), MsRespCode.E_1011.getMessage());
		}

	}

	/**
	 * 加载核心账户信息
	 * 
	 * @param context
	 * @param nullException
	 */
	public void loadAcctByContrNo(TxnContext context, boolean nullException) {
		if(logger.isDebugEnabled())
			logger.debug("加载核心账户信息--loadAcct");
		TxnInfo txnInfo = context.getTxnInfo();

		try {
			
			if(nullException && (StringUtils.isEmpty(txnInfo.getContrNo()))){
				throw new ProcessException(MsRespCode.E_1003.getCode(), MsRespCode.E_1003.getMessage());
			}else if(!nullException && (StringUtils.isEmpty(txnInfo.getContrNo()))){
				context.setAccount(null);
				return ;
			}

			QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
			CcsAcct acct = rCcsAcct.findOne(qCcsAcct.contrNbr.eq(txnInfo.getContrNo()));

			if (nullException && null == acct) {
				throw new ProcessException(MsRespCode.E_1003.getCode(), MsRespCode.E_1003.getMessage());
			}
			txnInfo.setAcctNbr(acct.getAcctNbr());
			txnInfo.setAcctType(acct.getAcctType());
			txnInfo.setLoanFeeDefId(acct.getLoanFeeDefId());//20151127
			txnInfo.setJionLifeInsuInd(acct.getJoinLifeInsuInd());//20151204
			txnInfo.setPrepayPkgInd(acct.getPrepayPkgInd());//20160513
			context.setAccount(acct);
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
			if(acctNbr != null && acctType != null){
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
	public void loadAcctOByAcct(TxnContext context, boolean nullException) {
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
	public void loadCustomerByCustId(TxnContext context, boolean nullException) {
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
	 * 获取金融交易流水
	 * @param context
	 * @param nullException
	 */
	public void loadAuthmemoLogKv(TxnContext context,Long logKv, boolean nullException){
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
			context.getTxnInfo().setLogKv(memo.getLogKv());
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
	public List<CcsPlan> loadPlansByAcct(TxnContext context, boolean nullException) {
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
	 * 重复交易检查 交易流水/请求时间
	 * 7 32 37 保单
	 * @param context
	 */
	public void checkRepeatTxn(TxnContext context) {
		if(logger.isDebugEnabled())
			logger.debug("重复交易检查 交易流水/请求时间--checkRepeatTxn");
		TxnInfo txnInfo = context.getTxnInfo();
		String serviceSn = txnInfo.getServiceSn();
		String acqId = txnInfo.getAcqId();
		
		orderFacility.valiRepeatOrder(serviceSn, acqId);
	}
	
	/**
	 * 检查在途代扣交易，有则异常
	 * @param context
	 */
	public void checkWaitCreditOrder(TxnContext context){
		if(logger.isDebugEnabled())
			logger.debug("检查在途代扣交易，有则异常--checkWaitDebitOrder");
		CcsAcct acct = context.getAccount();
		
		Long orderCount = orderFacility.countOrderByAcct(acct.getAcctNbr(),acct.getAcctType(), OrderStatus.W, AuthTransType.AgentCredit);
		
		if (null != orderCount && orderCount > 0) {
			throw new ProcessException(MsRespCode.E_1046.getCode(), 
					MsRespCode.E_1046.getMessage().replace("N", orderCount.toString()));
		}
	}
	
//	/**
//	 * 检查在途代付交易，有则异常
//	 * @param context
//	 */
//	public void checkWaitDebitOrder(TxnContext context){
//		if(logger.isDebugEnabled())
//			logger.debug("检查在途代付交易，有则异常--checkWaitCreditOrder");
//		CcsLoan loan = context.getLoan();
//		
//		Long orderCount = orderFacility.countOrder(loan.getDueBillNo(), OrderStatus.W, AuthTransType.AgentDebit);
//		
//		if (null != orderCount && orderCount > 0) {
//			throw new ProcessException(MsRespCode.E_1047.getCode(), 
//					MsRespCode.E_1047.getMessage().replace("N", orderCount.toString()));
//		}
//	}

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
	public void computOrderAmt(TxnContext context, OrderStatus orderStatus, AuthTransType transType) {
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
				null,txnInfo.getDueBillNo(),origOrderId,
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
	public void paymentProc(TxnContext context,String payJson) {
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
					this.updateAcctoMemoAmt(accto, txnInfo,true);
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
	 * 更新账户未入账金额
	 * @param accto
	 * @param txnInfo
	 * @param compFlag 金额计算标识  true 加 false 减
	 */
	public void updateAcctoMemoAmt(CcsAcctO accto, TxnInfo txnInfo,boolean amtFlag) {
		//代付
		if(AuthTransType.AgentDebit  == txnInfo.getTransType()){
			if(amtFlag){
				accto.setMemoDb(accto.getMemoDb()!=null?accto.getMemoDb().add(txnInfo.getTransAmt()):txnInfo.getTransAmt());
			}else{
				//金额为空或负数时，修正为0
				accto.setMemoDb(accto.getMemoDb()!=null && accto.getMemoDb().compareTo(txnInfo.getTransAmt())>=0?
						accto.getMemoDb().subtract(txnInfo.getTransAmt()):BigDecimal.ZERO);
			}
		}else if(AuthTransType.AgentCredit == txnInfo.getTransType()){
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
	 * 放款处理
	 * @param context
	 * @param payJson 
	 * @param mainResp
	 * @param msPayfrontError
	 */
	public void loanProc(TxnContext context, String payJson) {
		CcsOrder order = context.getOrder();
		TxnInfo txnInfo = context.getTxnInfo();
		CcsLoanReg loanReg = context.getLoanReg();
		CcsAcctO accto = context.getAccounto();
		PaySinPaymentResp data = new PaySinPaymentResp();
		LoanPlan loanPlan = context.getLoanPlan();
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
					//随借随还不更新账户信息
					if(loanPlan!=null && loanPlan.loanType != LoanType.MCAT 
							&& txnInfo.getLoanUsage() != LoanUsage.D){
						this.updateAcct(context);
					}
					this.updateAcctO(context);
					this.updateLoanReg(context, loanReg,LoanRegStatus.S);
					this.updateAcctoMemoAmt(accto, txnInfo,true);
					this.updateAuthmemo(context, AuthTransStatus.N);
					context.setTxnPost(savePosting(txnInfo));// 入账记录
					
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
	 * 放款审批
	 * @param context
	 * @param payJson 
	 * @param mainResp
	 * @param msPayfrontError
	 */
	public void loanProcAppro(TxnContext context, String payJson) {
		CcsOrder order = context.getOrder();
		TxnInfo txnInfo = context.getTxnInfo();
		CcsLoanReg loanReg = context.getLoanReg();
		PaySinPaymentResp data = new PaySinPaymentResp();
		LoanPlan loanPlan = context.getLoanPlan();
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
				txnInfo.setPayRespCode(data.getErrorCode());
				txnInfo.setPayRespMessage(data.getErrorMessage());
				txnInfo.setPayStatus(data.getStatus());
				// 公共部分为成功时，报文体响应码覆盖公共部分响应码
				msPayfrontError = this.getErrorEnum(data.getErrorCode(), MsPayfrontError.class);
				// 报文体响应码为0，放款成功
				if (msPayfrontError == MsPayfrontError.S_0) {
					orderFacility.updateOrder(order, mainResp, data.getStatus(),OrderStatus.S, txnInfo.getLogKv(),txnInfo.getBizDate(),MsRespCode.E_0000);
					//随借随还不更新账户信息
					if(loanPlan.loanType != LoanType.MCAT){
						this.updateAcct(context);
					}
					this.updateAcctO(context);
					this.updateLoanReg(context, loanReg,LoanRegStatus.S);
//					this.updateAcctoMemoAmt(accto, txnInfo,true);//待审批已冻结额度
					this.updateAuthmemo(context, AuthTransStatus.N);
					//修正未入账交易日期
					context.getAuthmemo().setB007TxnTime(txnInfo.getRequestTime().substring(4));//MMddHHmmss;
					context.getAuthmemo().setRequestTime(txnInfo.getRequestTime());
					txnPost = savePosting(txnInfo);// 入账记录
					txnPost.setRefNbr(context.getOrder().getRefNbr());//设置原订单的流水
					
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
	public void updateLoanReg(TxnContext context, CcsLoanReg loanReg,LoanRegStatus loanRegStatus) {
		if(null == loanReg)
			return ;
		
		//放款成功时,更新生效日期
		if(LoanRegStatus.S == loanRegStatus){
			loanReg.setValidDate(context.getTxnInfo().getBizDate());// 生效日期为放款成功日期
		}
		loanReg.setRefNbr(context.getOrder().getRefNbr());//每次交易更新refNbr
		loanReg.setLoanRegStatus(loanRegStatus);
	}

	/**
	 * 更新账户 账单日
	 * @param context
	 */
	public void updateAcct(TxnContext context) {
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
//			logger.debug("首次消费日期:[{}]",acct.getFirstRetlDate());
		}
	}

	/**
	 * 检查借据状态
	 * @param context
	 */
	public void checkLoan(TxnContext context) {
		if(logger.isDebugEnabled())
			logger.debug("检查借据状态--checkLoan");
		CcsLoan loan = context.getLoan();
		
		if(null != loan){
			LogTools.printObj(logger, loan.getLoanStatus(), "贷款状态");
			
			if(loan.getLoanStatus()==LoanStatus.T ){
				throw new ProcessException(MsRespCode.E_1018.getCode(),MsRespCode.E_1018.getMessage());
			}else if(loan.getLoanStatus()==LoanStatus.F ){
				throw new ProcessException(MsRespCode.E_1019.getCode(),MsRespCode.E_1019.getMessage());
			}else if(loan.getLoanStatus()!=LoanStatus.A ){//一期不考虑展期缩期
				throw new ProcessException(MsRespCode.E_1015.getCode(),MsRespCode.E_1015.getMessage());
			}
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
		txnPost.setTxnSource("MS");
		txnPost.setSrcChnl(txnInfo.getInputSource());

		txnPost.setTxnType(txnInfo.getTransType());
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
		txnPost.setAcqAddress("");
		txnPost.setMerchCategoryCode("");
		// 应付手续费
		txnPost.setTxnFeeAmt(BigDecimal.ZERO);
		// 应收手续费
		txnPost.setFeeProfit(BigDecimal.ZERO);
		txnPost.setB003ProcCode(txnInfo.getProcCode());
		txnPost.setB011Trace(txnInfo.getRefNbr().substring(txnInfo.getRefNbr().length()-9,txnInfo.getRefNbr().length()-3));//内部交易流水倒数9-3位数字
		txnPost.setB022Entrymode("");
		txnPost.setB025Entrycond("");
		txnPost.setB032AcqInst(txnInfo.getAcqId());
		txnPost.setB033FwdIns(FWD_INS);
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
		
		rCcsTxnPost.save(txnPost);
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
					||StringUtils.equals(pe.getErrorCode(),MsPayfrontError.E_90003.getCode())
					||StringUtils.equals(pe.getErrorCode(),MsPayfrontError.E_90004.getCode())){
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
		//特殊处理 达到人工审核放款的订单
		if(StringUtils.isNotBlank(txnInfo.getPayRespCode())
				&& StringUtils.equals(txnInfo.getResponsCode(), MsRespCode.E_1055.getCode())){
			setErrorInfo(context,AuthTransStatus.P,OrderStatus.Q,LoanRegStatus.N);
		}else if(StringUtils.isNotBlank(txnInfo.getPayRespCode())
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
		
		CcsOrder order = context.getOrder();
		CcsLoanReg loanReg = context.getLoanReg();
		TxnInfo txnInfo = context.getTxnInfo();
		CcsAcctO accto = context.getAccounto();
		CcsAuthmemoO authmemoO = context.getAuthmemo();
		//放款  失败  实时代扣失败  代偿失败
		
		if(txnInfo.getLoanUsage() == LoanUsage.A || txnInfo.getLoanUsage() == LoanUsage.L 
				|| txnInfo.getLoanUsage() == LoanUsage.D){
			//更新loanReg
			this.updateLoanReg(context, loanReg, loanRegStatus);
			
			//处理中 更新冻结金额  冻结在途借记
			if(null!=order && null != accto 
					&& (order.getOrderStatus() != OrderStatus.W || order.getOrderStatus() != OrderStatus.Q)
					&& (orderStatus == OrderStatus.W || orderStatus == OrderStatus.Q)
					&& txnInfo.getTransType() == AuthTransType.AgentDebit){//放款及重提交易
				this.updateAcctoMemoAmt(accto, txnInfo,true);
				this.updateAcctO(context);
			}else if(null!=order && null != accto 
					&& (order.getOrderStatus() == OrderStatus.Q)
					&& orderStatus == OrderStatus.E){ //原来是处理中，说明已经冻结
				//兼容审批，待审批，当前状态为失败，则对未匹配金额进行反向处理
				this.updateAcctoMemoAmt(accto, txnInfo,false);
				this.updateAcctO(context);
			}
			
			//放款成功、失败都记memo  实时代扣和代位追偿只有成功时记memo
			if(null != authmemoO){
				this.updateAuthmemo(context,authTransStatus);
			}
		}
		
		CommResp commResp = new CommResp();
		commResp.setCode(txnInfo.getPayRespCode());
		commResp.setMessage(txnInfo.getPayRespMessage());
		MsRespCode msRespCode = null;
		if(StringUtils.isNotBlank(txnInfo.getResponsCode())){
			msRespCode = getErrorEnum(txnInfo.getResponsCode(), MsRespCode.class);
		}
		
		//交易更新订单状态
		orderFacility.updateOrder(context.getOrder(), commResp, txnInfo.getPayStatus(),orderStatus,txnInfo.getLogKv(),txnInfo.getBizDate(),msRespCode);
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
			if(txnInfo.getLoanUsage() != LoanUsage.D){
				context = this.getLoanOtb(context);
				authmemo.setOtb(txnInfo.getAccountOTB());
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
//			um.setB011Trace(txnInfo.getServiceSn().substring(txnInfo.getServiceSn().length()-6));//交易流水后6位随机数
			um.setB011Trace(txnInfo.getRefNbr().substring(txnInfo.getRefNbr().length()-9,txnInfo.getRefNbr().length()-3));//内部交易流倒数9-3位数字
			um.setB037RefNbr(txnInfo.getRefNbr());//交易流水号
			um.setServicesn(txnInfo.getServiceSn());
			um.setB032AcqInst(txnInfo.getAcqId());
			um.setB033FwdIns(FWD_INS);
			um.setFwdInstId(FWD_INS);
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
		//保险费收取方式
		loanReg.setLoanInsFeeMethod(LoanFeeMethod.E);
		//代收服务费收取方式
		loanReg.setReplaceSvcFeeMethod(LoanFeeMethod.F);
		
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
	 * 获取otb
	 * @param context
	 * @return
	 */
	public TxnContext getLoanOtb(TxnContext context){
		if(logger.isDebugEnabled())
			logger.debug("检查放款otb--valiLoanOtb");
		TxnInfo txnInfo = context.getTxnInfo();
		CcsAcct acct = context.getAccount();
		CcsAcctO accto = context.getAccounto();
		LoanPlan loanPlan = context.getLoanPlan();
		BigDecimal loanOTB = BigDecimal.ZERO;
		
		if(null == loanPlan){
			try{
				ProductCredit productCredit = unifiedParameterFacility.loadParameter(acct.getProductCd(), ProductCredit.class);
				txnInfo.setLoanCode(productCredit.loanPlansMap.get(productCredit.defaultLoanType));
				loanPlan = unifiedParameterFacility.loadParameter(txnInfo.getLoanCode(), LoanPlan.class);
				context.setProductCredit(productCredit);
			}catch (Exception e){
				throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage() + ",找不到贷款产品定价");
			}
		}
		
		if(loanPlan.loanMold.equals(LoanMold.C)){
			//待确认 fix ok ?
			//loanOTB = accountOTB.acctCashLoanOTB(txnInfo.getCardNo(), acct.getAcctType(), txnInfo.getBizDate());
			loanOTB = accountOTB.acctCashLoanOTB(context.getAccounto(),context.getProductCredit(),txnInfo.getBizDate());
			if(logger.isDebugEnabled())
				logger.debug("循环贷款--loanOTB:[{}]",loanOTB);
		}else{
			//TODO 账户额度-放款累计本金-未入账借记金额 = 账户可用金额
			loanOTB = accountOTB.loanNoCycleOTB(accto, acct);
			
			if(logger.isDebugEnabled())
				logger.debug("非循环贷款--loanOTB:[{}]",loanOTB);
		}
		
		txnInfo.setAccountOTB(loanOTB);
		context.setTxnInfo(txnInfo);
		
		return context;
	}
	
	/**
	 * 检查放款OTB
	 * @param context
	 */
	public void valiLoanOtb(TxnContext context){
		context = this.getLoanOtb(context);
		BigDecimal sumTxnFee = BigDecimal.ZERO;
		TxnInfo txnInfo = context.getTxnInfo();
		
		if(txnInfo.getTransAmt().compareTo(txnInfo.getAccountOTB())>0){
			throw new ProcessException(MsRespCode.E_1010.getCode(),MsRespCode.E_1010.getMessage());
		}
		
		if (context.getLoanPlan().loanType.equals(LoanType.MCAT)){
			if(null != context.getLoan()){
				//获取loan下plan
				List<CcsPlan> planList = queryCommService.getPlanlistByLoan(context.getAccount().getAcctNbr(),context.getLoan().getRefNbr());
				//获取loan下schedule
				List<CcsRepaySchedule> scheduleList = queryCommService.getSchedulelistByLoan(context.getLoan().getLoanId());
				//计算账户下必定会入账但还未入账的息费类金额 (不包括提现手续费)
				BigDecimal acctUnpostAmtCalPro = acctUnpostAmtCalUtil.AcctUnpostAmtCal(context.getLoanFeeDef(), context.getAccount(), context.getLoan(), planList, scheduleList, context.getTxnInfo().getBizDate());
				//计算提现手续费
				sumTxnFee = sumWithDrawTxnFee(context.getAccount());
				//提现的算可用额度的时候，只减掉本次提现之前的所有未入账的手续费   by lizz 20160229
//						.add(loadWithDrawTxnFee(context.getAccount().getProductCd(), context.getAccount().getOrg(), context.getTxnInfo().getTransAmt()));
				//费用总和
				BigDecimal sumFees =  acctUnpostAmtCalPro.add(sumTxnFee).add(txnInfo.getTransAmt()).setScale(2, RoundingMode.HALF_UP);
				//息费类金额+提现手续费+提现金额      与账户余额比较
				if(sumFees.compareTo(txnInfo.getAccountOTB())>0){
					throw new ProcessException(MsRespCode.E_1010.getCode(),MsRespCode.E_1010.getMessage());
			}
			}else {
				sumTxnFee = sumWithDrawTxnFee(context.getAccount());
				//提现的算可用额度的时候，只减掉本次提现之前的所有未入账的手续费   by lizz 20160301
//						.add(loadWithDrawTxnFee(context.getAccount().getProductCd(), context.getAccount().getOrg(), context.getTxnInfo().getTransAmt()));	
				//费用总和
				BigDecimal sumFee = sumTxnFee.add(txnInfo.getTransAmt());
				if(sumFee.compareTo(txnInfo.getAccountOTB())>0){
					throw new ProcessException(MsRespCode.E_1010.getCode(),MsRespCode.E_1010.getMessage());
				}
			}
		}
		
	}
	/**
	 * 提现手续费
	 */
	public BigDecimal loadWithDrawTxnFee(String productCd, String org, BigDecimal transAmt) {
		
		OrganizationContextHolder.setCurrentOrg(org);
		ProductCredit productCr = unifiedParameterFacility.loadParameter(productCd, ProductCredit.class);
		Map<String, List<TxnFee>> txnFeeMap = productCr.txnFeeList;
		if(txnFeeMap == null){
			return BigDecimal.ZERO;
		}
		List<TxnFee> feeList = txnFeeMap.get(MCAT_LOANING_TXN_CD);
		
		if(feeList != null && feeList.size() > 0){
			TxnFee txnFee = feeList.get(0);
			return calculator.getFeeAmount(txnFee.tierInd, txnFee.chargeRates, transAmt);
			
		}else
			return BigDecimal.ZERO;
	}

	/**
	 * 试算当天提现手续费之和
	 */
//	public BigDecimal sumWithDrawTxnFee(CcsAcct ccsAcct,String productCd,String org){
//		
//		if(logger.isDebugEnabled())
//			logger.debug("试算当天提现手续费之和");
//		JPAQuery query = new JPAQuery(em);
//		QCcsOrder qCcsOrder = QCcsOrder.ccsOrder;
//		BigDecimal txnFee = BigDecimal.ZERO;
//		BooleanExpression expression = qCcsOrder.optDatetime.eq(globalManagementService.getSystemStatus().getBusinessDate())
//				.and(qCcsOrder.acctNbr.eq(ccsAcct.getAcctNbr()))
//				.and(qCcsOrder.acctType.eq(ccsAcct.getAcctType()))
//				.and(qCcsOrder.orderStatus.in(OrderStatus.S,OrderStatus.W,OrderStatus.C,OrderStatus.P))
//				.and((qCcsOrder.loanUsage.in(LoanUsage.A,LoanUsage.L)));
//		
//		List<CcsOrder> ccsOrderList = query.from(qCcsOrder).where(expression).list(qCcsOrder);
//		
//		BigDecimal sumTxnFee = BigDecimal.ZERO;
//		if (null != ccsOrderList) {
//			for (CcsOrder ccsOrder : ccsOrderList) {
//				txnFee = loadWithDrawTxnFee(ccsAcct.getProductCd(), ccsAcct.getOrg(), ccsOrder.getTxnAmt());
//				sumTxnFee = sumTxnFee.add(txnFee);
//			}
//		}
//		if(logger.isDebugEnabled())
//			logger.debug("提现手续费之和試算結果[{}]" , sumTxnFee);
//
//		return sumTxnFee;
//	}
	
	/**
	 * 试算提现手续费之和
	 */
	public BigDecimal sumWithDrawTxnFee(CcsAcct ccsAcct){
		
		if(logger.isDebugEnabled())
			logger.debug("试算提现手续费之和");
		JPAQuery query = new JPAQuery(em);
		QCcsAuthmemoO qCcsAuthmemoO = QCcsAuthmemoO.ccsAuthmemoO;
		BigDecimal txnFee = BigDecimal.ZERO;
		BooleanExpression expression = qCcsAuthmemoO.acctNbr.eq(ccsAcct.getAcctNbr())
				.and(qCcsAuthmemoO.acctType.eq(ccsAcct.getAcctType()))
				.and(qCcsAuthmemoO.authTxnStatus.in(AuthTransStatus.P,AuthTransStatus.N))
				.and((qCcsAuthmemoO.txnType.in(AuthTransType.AgentDebit)));
		
		List<CcsAuthmemoO> ccsAuthmemoOList = query.from(qCcsAuthmemoO).where(expression).list(qCcsAuthmemoO);
		
		BigDecimal sumTxnFee = BigDecimal.ZERO;
		if (null != ccsAuthmemoOList) {
			for (CcsAuthmemoO ccsAuthmemoO : ccsAuthmemoOList) {
				txnFee = loadWithDrawTxnFee(ccsAcct.getProductCd(), ccsAcct.getOrg(), ccsAuthmemoO.getTxnAmt());
				sumTxnFee = sumTxnFee.add(txnFee);
			}
		}
		if(logger.isDebugEnabled())
			logger.debug("提现手续费之和試算結果[{}]" , sumTxnFee);

		sumTxnFee=sumTxnFee.setScale(2, RoundingMode.HALF_UP);
		
		return sumTxnFee;
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
	 * 获取合同号下所有LoanReg
	 * @param context
	 * @param nullException
	 */
	public void loadLoanRegList(TxnContext context,LoanType loanType) {
		TxnInfo txnInfo = context.getTxnInfo();
		JPAQuery query = new JPAQuery(em);
		
		QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;
		
		BooleanExpression expression = qCcsLoanReg.contrNbr.eq(txnInfo.getContrNo());
		if(null != loanType){
			expression = expression.and(qCcsLoanReg.loanType.eq(loanType));
		}
		
		List<CcsLoanReg> regList = query.from(qCcsLoanReg).where(expression).list(qCcsLoanReg);
		
		context.setRegList(regList);
	}
	
	/**
	 * 更新联机账户信息
	 * @param context
	 */
	public void updateAcctO(TxnContext context){
		CcsAcctO accto = context.getAccounto();
		TxnInfo txnInfo = context.getTxnInfo();
		//Date1.before(Date2)，当Date1小于Date2时，返回TRUE，当大于等于时，返回false；
		if(null == accto.getLastUpdateBizDate()
				||accto.getLastUpdateBizDate().before(txnInfo.getBizDate())){
			accto.setLastUpdateBizDate(txnInfo.getBizDate());
		}
	}
	/**
	 * 商品贷信息管理表
	 * @param context
	 */
	public void saveMerchandiseOrder(TxnContext context){
		TxnInfo txnInfo = context.getTxnInfo();
		MerchandiseFacility.installMerchandiseOrder(txnInfo.getServiceSn(), txnInfo.getRefNbr(),txnInfo.getContrNo(),
				context.getAccount().getApplicationNo(), txnInfo.merId, txnInfo.getAuthTxnTerminal(), txnInfo.getRaId(),
				null, txnInfo.getMerchandiseAmt(), null, null, null, null, null, txnInfo.getDownPaymentAmt(), null, null);
	}
	/**
	 * 根据原订单信息查询订单
	 * @param context
	 */
	public void findOrigOrder(TxnContext context){
		TxnInfo txnInfo = context.getTxnInfo();
		try{
			CcsOrder ccsOrder = orderFacility.findByOrig(txnInfo.getOrigTransAmt(), txnInfo.getOrigMerId(), 
					txnInfo.getContrNo(), txnInfo.getOrigServiceSn(), txnInfo.getOrigServiceId(), 
					txnInfo.getOrigAcqId(),OrderStatus.S,AuthTransType.AgentDebit,txnInfo.getOrigMerOrderId());
			
			if(null != ccsOrder){
				txnInfo.setOrigRefNbr(ccsOrder.getRefNbr());
				txnInfo.setDueBillNo(ccsOrder.getDueBillNo());
				context.setOrigOrder(ccsOrder);
			}else{
				CcsOrderHst ccsOrderHst = orderFacility.findByOrigHst(txnInfo.getOrigTransAmt(), txnInfo.getOrigMerId(), 
						txnInfo.getContrNo(), txnInfo.getOrigServiceSn(), txnInfo.getOrigServiceId(), 
						txnInfo.getOrigAcqId(),OrderStatus.S,AuthTransType.AgentDebit,txnInfo.getOrigMerOrderId());
				
				if(null == ccsOrderHst){
					throw new ProcessException(MsRespCode.E_1007.getCode(),MsRespCode.E_1007.getMessage());
				}
				txnInfo.setOrigRefNbr(ccsOrderHst.getRefNbr());
				txnInfo.setDueBillNo(ccsOrderHst.getDueBillNo());
				context.setOrigOrderHst(ccsOrderHst);
			}
		}catch(ProcessException pe){
			if (logger.isErrorEnabled())
				logger.error(pe.getMessage(), pe);
			throw new ProcessException(pe.getErrorCode(),pe.getMessage());
		}catch(Exception e){
			if (logger.isErrorEnabled())
				logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1072.getCode(),MsRespCode.E_1072.getMessage());
		}
	}
	
	/**
	 * 根据原订单信息查询撤销订单
	 * @param context
	 */
	public void findVoidOrigOrder(TxnContext context){
		TxnInfo txnInfo = context.getTxnInfo();
		try{
			CcsOrder ccsOrder = orderFacility.findByOrig(txnInfo.getOrigTransAmt(), txnInfo.getOrigMerId(), 
					txnInfo.getContrNo(), txnInfo.getOrigServiceSn(), txnInfo.getOrigServiceId(), 
					txnInfo.getOrigAcqId(),OrderStatus.S,AuthTransType.AgentDebit,txnInfo.getOrigMerOrderId());
			
			if(null != ccsOrder){
				txnInfo.setOrigRefNbr(ccsOrder.getRefNbr());
				txnInfo.setDueBillNo(ccsOrder.getDueBillNo());
				context.setOrigOrder(ccsOrder);
			}else{
				throw new ProcessException(MsRespCode.E_1007.getCode(),MsRespCode.E_1007.getMessage());
			}
		}catch(ProcessException pe){
			if (logger.isErrorEnabled())
				logger.error(pe.getMessage(), pe);
			throw new ProcessException(pe.getErrorCode(),pe.getMessage());
		}catch(Exception e){
			if (logger.isErrorEnabled())
				logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1072.getCode(),MsRespCode.E_1072.getMessage());
		}
	}
	
	/**
	 * 查询原交易txnPost
	 * @param cardNbr
	 * @param refNbr
	 * @return
	 */
	public CcsTxnPost findOrigTxnPost(String cardNbr,String refNbr){
		QCcsTxnPost qCcsTxnPost = QCcsTxnPost.ccsTxnPost;
		
		BooleanExpression expression = qCcsTxnPost.cardNbr.eq(cardNbr).and(qCcsTxnPost.refNbr.eq(refNbr));
		
		CcsTxnPost ccsTxnPost = rCcsTxnPost.findOne(expression);
		return ccsTxnPost;
	}
	
	/**
	 * 异常后保存订单
	 * 目前暂不处理订单已存在的情况
	 * @param context
	 */
	@Transactional
	public void saveErrorOrder(TxnContext context){
		TxnInfo txnInfo = context.getTxnInfo();
		try{
			//正常处理流程事务退出后，没有保存当前流水对应的订单，则需要异常后再次处理订单
			CcsOrder ccsOrder = orderFacility.findOrderBySersn(txnInfo.getServiceSn(), txnInfo.getAcqId());
		
			if(null == ccsOrder){
				CommResp mainResp = new CommResp();
				mainResp.setCode(txnInfo.getPayRespCode());
				mainResp.setCode(txnInfo.getPayRespMessage());
				MsRespCode msRespCode = null;
				if(StringUtils.isNotBlank(txnInfo.getResponsCode())){
					msRespCode = getErrorEnum(txnInfo.getResponsCode(), MsRespCode.class);
				}
				this.installOrder(context);
				context.getOrder().setMatchInd(Indicator.N);//不对账
				orderFacility.updateOrder(context.getOrder(), mainResp, null, OrderStatus.E, txnInfo.getLogKv(), txnInfo.getBizDate(),msRespCode);
			}
		}catch(Exception e){
			if(logger.isWarnEnabled()){
				logger.warn(e.getMessage()+"--异常后保存订单失败,不再往外抛异常", e);
			}
		}
	}
	
	/**
	 * 查商品订单表
	 * @param context
	 * @return
	 */
	public CcsMerchandiseOrder loadMerchandiseOrder(TxnContext context) {
		
		QCcsMerchandiseOrder qCcsMerchandiseOrder = QCcsMerchandiseOrder.ccsMerchandiseOrder;
		String contrNbr = null;
		String refNbr = null;
		BooleanExpression expression=null;
		
		if (context.getOrigOrder() == null) {
			contrNbr = context.getOrigOrderHst().getContrNbr();
			refNbr = context.getOrigOrderHst().getRefNbr();
		}else {
			contrNbr = context.getOrigOrder().getContrNbr();
			refNbr = context.getOrigOrder().getRefNbr();
		}
		if(null != contrNbr){
			expression = qCcsMerchandiseOrder.contrNbr.eq(contrNbr);
			
		}
		if(null != refNbr){
			expression = expression.and(qCcsMerchandiseOrder.refNbr.eq(refNbr));
		}
		
		CcsMerchandiseOrder merchan = new JPAQuery(em).from(qCcsMerchandiseOrder).where(expression).singleResult(qCcsMerchandiseOrder);
		
		return merchan;
	}
	/**
	 * 新建  拿去花流水表  流水
	 * @param context
	 */
	public void installTrustLoanTxn(TxnContext context) {
		TxnInfo txnInfo = context.getTxnInfo();
		CcsAcct acct = context.getAccount();
		
		CcsTrustLoanTxn tlx = trustLoanTxnFacility.installTrustLoanTxn(acct.getAcctNbr(),
				acct.getAcctType(), txnInfo.getContrNo(), txnInfo.getBizDate(),
				txnInfo.getTransType(), txnInfo.getTransAmt(),txnInfo.getServiceSn() ,
				txnInfo.getLoanNo(), txnInfo.getServiceId(), txnInfo.getAcqId());
		
		tlx.setDownPaymentAmt(txnInfo.getDownPaymentAmt());
		tlx.setFeeAmount(txnInfo.getFeeAmt());
		tlx.setLoanSource(txnInfo.getLoanSource());
		tlx.setMerchandiseAmt(txnInfo.getMerchandiseAmt());
		tlx.setLoanTerm(txnInfo.getCdTerms());
		tlx.setMerchandiseOrder(txnInfo.getMerchandiseOrder());
		tlx.setMerId(txnInfo.getMerId());
		tlx.setMobile(txnInfo.getMobile());
		tlx.setOrg(txnInfo.getOrg());
		tlx.setPaidFeeAmount(txnInfo.getFeeAmt());
		tlx.setPaidTerm(txnInfo.getCdTerms());
		tlx.setPenalty(txnInfo.getPenalty());
		tlx.setPrincipal(txnInfo.getPrincipal());
		tlx.setRaId(txnInfo.getRaId());
		tlx.setRepayType(txnInfo.getRepayType());
		tlx.setReturnAmount(txnInfo.getReturnAmt());
		tlx.setTerminalId(txnInfo.getAuthTxnTerminal());
		tlx.setTlReverseNo(txnInfo.getReNo());
		tlx.setTlTxnStatus(txnInfo.getOrderStatus());
		
//		tlx.setContrNbr(txnInfo.getContrNo());//合同号
//		tlx.setTlLoanNo(txnInfo.getLoanNo());//贷款流水号
//		tlx.setTlReverseNo(txnInfo.getReNo());//还款流水号
//		tlx.setTlTxnAmt(txnInfo.getTransAmt());//还款金额
//		tlx.setPaidTerm(txnInfo.getCdTerms());//还款期数
//		tlx.setPrincipal(txnInfo.getPrincipal());//已还本金
//		tlx.setPaidFeeAmount(txnInfo.getFeeAmt());//已还分期服务费
//		tlx.setPenalty(txnInfo.getPenalty());//已还罚金
//		tlx.setRepayType(txnInfo.getRepayType());//还款类型
		
		rCcsTrustLoanTxn.save(tlx);
	}
	/**
	 * 新建  拿去花还款计划明细表  流水
	 * @param context
	 * @throws ParseException 
	 */
	public void installTrustLoanSchedule(TxnContext context) throws ParseException {
		TxnInfo txnInfo = context.getTxnInfo();
		CcsAcct acct = context.getAccount();
		Date date = new Date();
		
		List<TNMTrustLoanSchedReqSubPlan> scheduleDetails = txnInfo.getScheduleDetails();
		for(TNMTrustLoanSchedReqSubPlan schedule : scheduleDetails ){
			CcsTrustLoanSchedule ccsTrustLoanSchedule = new CcsTrustLoanSchedule();
			ccsTrustLoanSchedule.setServicesn(txnInfo.getServiceSn());
			ccsTrustLoanSchedule.setAcctNbr(acct.getAcctNbr());//账户编号
			ccsTrustLoanSchedule.setContrNbr(txnInfo.getContrNo());
			ccsTrustLoanSchedule.setAcctType(acct.getAcctType());//账户类型
			ccsTrustLoanSchedule.setAmount(txnInfo.getTransAmt());//贷款总金额
			ccsTrustLoanSchedule.setTlLoanNo(txnInfo.getLoanNo());//贷款流水号
			ccsTrustLoanSchedule.setOrg(txnInfo.getAcqId());//合作机构编号
			ccsTrustLoanSchedule.setTerm(txnInfo.getCdTerms());//分期总期数
			ccsTrustLoanSchedule.setCurrTerm(Integer.parseInt(schedule.termNo));//当前期数
			ccsTrustLoanSchedule.setDueDate(new SimpleDateFormat("yyyyMMddHHmmss").parse(schedule.dueDate));//最后还款日期
			ccsTrustLoanSchedule.setCapitalAmount(schedule.capitalAmount);//单期本金
			ccsTrustLoanSchedule.setFeeAmount(schedule.feeAmount);//单期手续费
			ccsTrustLoanSchedule.setOverdueFine(schedule.overdueFine);
			ccsTrustLoanSchedule.setIsOverdue(schedule.isOverdue);
			ccsTrustLoanSchedule.setPaidCapital(schedule.paidCapital);
			ccsTrustLoanSchedule.setPaidFee(schedule.paidFee);
			ccsTrustLoanSchedule.setPaidFine(schedule.paidFine);
			ccsTrustLoanSchedule.setChangeTime(new SimpleDateFormat("yyyyMMddHHmmss").parse(txnInfo.getChangeTime()));
			ccsTrustLoanSchedule.setPaidDate(schedule.paidDate==null? null : new SimpleDateFormat("yyyyMMddHHmmss").parse(schedule.paidDate));
			ccsTrustLoanSchedule.setStatus(schedule.status);
			ccsTrustLoanSchedule.setCreateTime(date);
			ccsTrustLoanSchedule.setCreateUser(Constants.OP_USER_BATCH);
			ccsTrustLoanSchedule.setLstUpdTime(date);
			ccsTrustLoanSchedule.setLstUpdUser(Constants.OP_USER_BATCH);
			rCcsTrustLoanSchedule.save(ccsTrustLoanSchedule);
		}
	}
	
	/**
	 * 检查请求报文
	 * 重复交易检查 交易流水/请求时间
	 * 7 32 37 保单
	 * @param context
	 */
	public void checkRepeatLoanTxn(TxnContext context) {
		if(logger.isDebugEnabled())
			logger.debug("重复交易检查 交易流水/请求时间--checkRepeatTxn");
		TxnInfo txnInfo = context.getTxnInfo();
		String serviceSn = txnInfo.getServiceSn();
		String acqId = txnInfo.getAcqId();
		
		loanTxnFacility.valiRepeatLoanTxn(serviceSn, acqId);
	}
}
