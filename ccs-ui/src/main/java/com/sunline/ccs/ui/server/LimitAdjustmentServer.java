/**
 * 
 */
package com.sunline.ccs.ui.server;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.Order;
import com.mysema.query.types.OrderSpecifier;
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ark.support.utils.MapBuilder;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.DateUtils;
import com.sunline.ccs.facility.FetchSmsNbrFacility;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctCrlmtAdjLog;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctO;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctCrlmtAdjLog;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsOpPrivilege;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcctCrlmtAdjLog;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcctCrlmtAdjLogHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.Organization;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.AdjState;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.service.api.MsQueryService;
import com.sunline.ccs.service.msdentity.SMTNMAAdjustQuotaReq;
import com.sunline.ccs.ui.server.commons.BlockCodeUtil;
import com.sunline.ccs.ui.server.commons.CPSBusProvide;
import com.sunline.ccs.ui.server.commons.CheckUtil;
import com.sunline.kylin.web.ark.client.JPAQueryFetchResponseBuilder;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.param.def.CurrencyCd;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.LoanType;
//import com.sunline.smsd.service.sdk.DownMsgFacility;
import com.sunline.ppy.dictionary.enums.LoanStatus;


/**
 * 
 * @see 类名：LimitAdjustmentServer
 * @see 描述：主动调额
 *
 * @see 创建日期：   Jun 29, 20158:18:23 PM
 * @author tangls
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Controller
@RequestMapping(value="/limitAdjustmentServer")
public class LimitAdjustmentServer  {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private RCcsAcct rCcsAcct;
	
	@Autowired
	private RCcsAcctO rCcsAcctO;
	
	@Autowired
	private RCcsLoan rCcsLoan;
	
	@Autowired
	private RCcsCustomerCrlmt rCcsCustomerCrlmt;
	
	@Autowired
	private OpeLogUtil opeLogUtil;

	@Autowired
	private CPSBusProvide cpsBusProvide;
	 
	@Autowired
	private AcctServer accInter;
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private RCcsAcctCrlmtAdjLog rTmLimitAdjLog;
	
	@Autowired 
	private CustAcctCardFacility custAcctCardQueryFacility;
	
	@Autowired
	private MsQueryService msQueryService;

	@Autowired
	private UnifiedParameterFacility unifiedParameterService;
	
	@Autowired
	private UnifiedParamFacilityProvide unifiedParaFacilityProvide;
	
	@Autowired
	private OperatorAuthUtil operatorAuthUtil;
	
	@Autowired
	private GlobalManagementService globalManagementService;
	
	@Transactional
	@ResponseBody()
	@RequestMapping(value="/adjustAcctCreditLimit",method={RequestMethod.POST})
	public void adjustCreditLimit(@RequestBody String contrNbr,
			@RequestBody String newCreditLimit) throws FlatException {
//		logger.info("adjustCreditLimit: cardNo后四位=["+CodeMarkUtils.subCreditCard(cardNo)+"], acctType=["+acctType+"]");
		
		/*---- 1.校验输入项目是否合法 ---*/
		CheckUtil.rejectNull(newCreditLimit, "永久额度不允许为空");
		
		//如果调整额度包含小数，则直接拒绝
		if(newCreditLimit.contains("."))
			throw new FlatException("调整额度不能包含小数");
		//检查操作员可调整的最大永久额度
		checkOprAuth(new BigDecimal(newCreditLimit));
		adjustAcctCreditLimit(contrNbr, newCreditLimit);

		//记录操作日志
//		opeLogUtil.cardholderServiceLog("3303", null, cardNo, acctType, "主动调额, 新额度："+newCreditLimit);
	}
	
	/**
	 * 调整账户永久额度-提交待审批的调额记录，通过审批的调额才能更改账户/客户级额度
	 * @param acctType
	 * @param cardNo
	 * @param creditLimit
	 * @throws FlatException
	 */
	@Transactional
	public void adjustAcctCreditLimit(String contrNbr, String strCreditLimit) throws FlatException {
		BigDecimal creditLimit = new BigDecimal(strCreditLimit);
		QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
		QCcsAcctO qCcsAcctO = QCcsAcctO.ccsAcctO;
		QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;

//		logger.info("adjustAcctCreditLimit:账户类型[" + acctType + "]," +
//				"卡号后四位[" + CodeMarkUtils.subCreditCard(cardNo) + "],永久额度[" + creditLimit + "]");
//		
//		CheckUtil.checkCardNo(cardNo);
		
//		CcsCard tmCard = custAcctCardQueryFacility.getCardByCardNbr(cardNo);
//		CheckUtil.rejectNull(tmCard, "卡片信息不存在");
		
		CcsAcct tmAccount = rCcsAcct.findOne(qCcsAcct.contrNbr.eq(contrNbr));
		CcsAcctO tmAccountO = rCcsAcctO.findOne(qCcsAcctO.contrNbr.eq(contrNbr));
		if(tmAccount==null||tmAccountO==null){
			throw new FlatException("查询不到账户信息，合同号["+contrNbr+"]");
		}
		if(tmAccount.getAcctExpireDate()!=null&&globalManagementService.getSystemStatus().getBusinessDate().after(tmAccount.getAcctExpireDate())){
			throw new FlatException("无效账户");
		}
		
		CcsLoan loan = rCcsLoan.findOne(qCcsLoan.contrNbr.eq(tmAccount.getContrNbr()));
		
		if(loan != null){
			if(LoanStatus.F.equals(loan.getLoanStatus())||LoanStatus.T.equals(loan.getLoanStatus())){
				throw new FlatException("无效合同");
				}
			LoanType loanType = loan.getLoanType();
			if(! loanType.equals(LoanType.MCAT)){
				throw new FlatException("只有随借随还类型的合同才能调额");
				}
			}else{               //产品参数
				ProductCredit product = unifiedParameterService.retrieveParameterObject(tmAccount.getProductCd(), ProductCredit.class);
				if(! product.defaultLoanType.equals(LoanType.MCAT)){
					throw new FlatException("只有随借随还类型的合同才能调额");
				}
			}
		
		//旧的信用额度
		BigDecimal oldCreditLimlt=tmAccount.getCreditLmt();
		//如果调整额度与当前额度一致，则直接拒绝
		if(oldCreditLimlt.compareTo(creditLimit)==0)
			throw new FlatException("调整额度与当前额度一致");
		if(creditLimit.compareTo(BigDecimal.ZERO)<0)
			throw new FlatException("调整额度不应小于0");
		
		//合同被冻结
		if(BlockCodeUtil.hasBlockCode(tmAccountO.getBlockCode(),"T"))
			throw new FlatException("账户已冻结");
		
		//距离上次额度调整日期间隔
		AccountAttribute accountAttr=unifiedParaFacilityProvide.acct_attribute(tmAccount.getProductCd());
		if(tmAccount.getLastLimitAdjDate()!=null&&DateUtils.getIntervalDays(tmAccount.getLastLimitAdjDate(),globalManagementService.getSystemStatus().getBusinessDate())
				<accountAttr.creditLimitAdjustInterval.intValue())
			throw new FlatException("上次账户调整额度日期["+tmAccount.getLastLimitAdjDate()+"]后"
				+accountAttr.creditLimitAdjustInterval.intValue()+"天可调整额度");
		//查询重复记录
//		QCcsAcctCrlmtAdjLog qCcsAcctCrlmtAdjLog = QCcsAcctCrlmtAdjLog.ccsAcctCrlmtAdjLog;
//		if(rTmLimitAdjLog.findOne(qCcsAcctCrlmtAdjLog.acctNbr.eq(tmAccount.getAcctNbr())
//				.and(qCcsAcctCrlmtAdjLog.acctType.eq(tmAccount.getAcctType())))!=null)
//			throw new FlatException("已存在调额记录"+"账户号["+tmAccount.getAcctNbr()+"]");
		 //添加账户额度调整日志
		CcsAcctCrlmtAdjLog tmLimitAdjLog=new CcsAcctCrlmtAdjLog();
		tmLimitAdjLog.setAcctNbr(tmAccount.getAcctNbr());
		tmLimitAdjLog.setAcctType(tmAccount.getAcctType());
		tmLimitAdjLog.setCardNbr(tmAccount.getDefaultLogicCardNbr());
		tmLimitAdjLog.setAdjState(AdjState.W);
		tmLimitAdjLog.setCreditLmtNew(creditLimit);
		tmLimitAdjLog.setCreditLmtOrig(oldCreditLimlt);
		tmLimitAdjLog.setOpId(OrganizationContextHolder.getUsername());
		tmLimitAdjLog.setProcDate(unifiedParaFacilityProvide.BusinessDate());
		tmLimitAdjLog.setOpTime(new Date());
		tmLimitAdjLog.setOrg(OrganizationContextHolder.getCurrentOrg());
	    addTmLimitAdjLog(tmLimitAdjLog);
	}
	
	/**
	 * 查询当日调额流水
	 * @param request
	 * @return
	 * @throws FlatException
	 */
	@ResponseBody()
	@RequestMapping(value="/getCurrentTranAdjLogList",method={RequestMethod.POST})
	public FetchResponse getCurrentTranAdjLogList(@RequestBody FetchRequest request) throws FlatException {
		//增加服务查询
		QCcsAcctCrlmtAdjLog qTmLimitAdjLog = QCcsAcctCrlmtAdjLog.ccsAcctCrlmtAdjLog;
		JPAQuery query = new JPAQuery(em).from(qTmLimitAdjLog); 
		{
			//条件限于当前操作员
			query = query.where(qTmLimitAdjLog.org.eq(OrganizationContextHolder.getCurrentOrg()))
					.where(qTmLimitAdjLog.opId.eq(OrganizationContextHolder.getUsername()));
			Date beginDate=(Date) request.getParameter("beginDate");
			if(null != beginDate) {
				beginDate = DateUtils.truncate(beginDate, Calendar.DATE);
				query = query.where(qTmLimitAdjLog.opTime.goe(beginDate));
			}
			Date endDate=(Date) request.getParameter("endDate");
			if(null != endDate) {
				endDate = DateUtils.truncate(DateUtils.addDays(endDate, 1), Calendar.DATE);
				query = query.where(qTmLimitAdjLog.opTime.lt(endDate));
			}
			
			query = query.orderBy(new OrderSpecifier<Long>(Order.DESC, qTmLimitAdjLog.opSeq));
		}

		return new JPAQueryFetchResponseBuilder(request, query)
		.addFieldMapping(CcsAcctCrlmtAdjLog.P_Org, qTmLimitAdjLog.org)
		.addFieldMapping(CcsAcctCrlmtAdjLog.P_OpSeq , qTmLimitAdjLog.opSeq)
		.addFieldMapping(CcsAcctCrlmtAdjLog.P_OpTime, qTmLimitAdjLog.opTime)
		.addFieldMapping(CcsAcctCrlmtAdjLog.P_OpId, qTmLimitAdjLog.opId)
		.addFieldMapping(CcsAcctCrlmtAdjLog.P_AcctNbr, qTmLimitAdjLog.acctNbr)
		.addFieldMapping(CcsAcctCrlmtAdjLog.P_AcctType, qTmLimitAdjLog.acctType)
		.addFieldMapping(CcsAcctCrlmtAdjLog.P_CardNbr, qTmLimitAdjLog.cardNbr)
		.addFieldMapping(CcsAcctCrlmtAdjLog.P_CreditLmtOrig, qTmLimitAdjLog.creditLmtOrig)
		.addFieldMapping(CcsAcctCrlmtAdjLog.P_CreditLmtNew, qTmLimitAdjLog.creditLmtNew)
		.addFieldMapping(CcsAcctCrlmtAdjLog.P_AdjState,qTmLimitAdjLog.adjState)
		.build();
	}
	
	/**
	 * 查询当前复核中的调额申请列表
	 */
	@ResponseBody()
	@RequestMapping(value="/getCurrentTranAdjLogListRecheck",method={RequestMethod.POST})
	public FetchResponse getCurrentTranAdjLogListRecheck(@RequestBody FetchRequest request) throws FlatException {
//		logger.info("卡号[" + CodeMarkUtils.subCreditCard((String)request.getParameter(CcsCardLmMapping.P_CardNbr)) + "]");
		QCcsAcctCrlmtAdjLog qTmLimitAdjLog = QCcsAcctCrlmtAdjLog.ccsAcctCrlmtAdjLog;
		QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
		JPAQuery query = new JPAQuery(em).from(qTmLimitAdjLog); 
		{
			//条件限于当前机构操作员
			query = query.where(qTmLimitAdjLog.org.eq(OrganizationContextHolder.getCurrentOrg()));
		if(!"".equals((String)request.getParameter(CcsLoan.P_ContrNbr)) && StringUtils.isNotEmpty((String)request.getParameter(CcsLoan.P_ContrNbr))){
			String cardNbr = rCcsAcct.findOne(qCcsAcct.contrNbr.eq((String)request.getParameter(CcsLoan.P_ContrNbr))).getDefaultLogicCardNbr();
			query = query.where(qTmLimitAdjLog.adjState.eq(AdjState.W).and(qTmLimitAdjLog.cardNbr.eq(cardNbr)));
		}else{
			query = query.where(qTmLimitAdjLog.adjState.eq(AdjState.W));
		}
		//仅具有审批金额权限的操作员可以查看记录
		CcsOpPrivilege tmOperAuth = operatorAuthUtil.getCurrentOperatorAuth();
		query=query.where(qTmLimitAdjLog.creditLmtNew.loe(tmOperAuth.getMaxLmtAdj()));
			query = query.orderBy(new OrderSpecifier<Long>(Order.DESC, qTmLimitAdjLog.opSeq));
		}
		return new JPAQueryFetchResponseBuilder(request, query)
			.addFieldMapping(CcsAcctCrlmtAdjLog.P_Org, qTmLimitAdjLog.org)
			.addFieldMapping(CcsAcctCrlmtAdjLog.P_OpSeq , qTmLimitAdjLog.opSeq)
			.addFieldMapping(CcsAcctCrlmtAdjLog.P_OpTime, qTmLimitAdjLog.opTime)
			.addFieldMapping(CcsAcctCrlmtAdjLog.P_OpId, qTmLimitAdjLog.opId)
			.addFieldMapping(CcsAcctCrlmtAdjLog.P_AcctNbr, qTmLimitAdjLog.acctNbr)
			.addFieldMapping(CcsAcctCrlmtAdjLog.P_AcctType, qTmLimitAdjLog.acctType)
			.addFieldMapping(CcsAcctCrlmtAdjLog.P_CardNbr, qTmLimitAdjLog.cardNbr)
			.addFieldMapping(CcsAcctCrlmtAdjLog.P_CreditLmtOrig, qTmLimitAdjLog.creditLmtOrig)
			.addFieldMapping(CcsAcctCrlmtAdjLog.P_CreditLmtNew, qTmLimitAdjLog.creditLmtNew)
			.build();
	} 

	@SuppressWarnings("rawtypes")
	@ResponseBody()
	@RequestMapping(value="/getTmAcountAmountCurrency",method={RequestMethod.POST})
	public Map getTmAcountAmountCurrency (
			@RequestBody String contrNbr) throws FlatException{
		Map<String, Serializable> result =new HashMap<String, Serializable>();
		//这段代码调用了本应剔除的函数
		//暂时屏蔽
		result =accInter.getAcctInfoByContrNbr(contrNbr);
		if(result==null) throw new FlatException("查询不到对应账户信息,合同号["+contrNbr+"]");
		return result;
	}
   
	
	/**
	 * 保存额度调整记录
	 * @param tmLimitAdjLog
	 */
	@Transactional
    public void addTmLimitAdjLog(CcsAcctCrlmtAdjLog tmLimitAdjLog){
    	rTmLimitAdjLog.save(tmLimitAdjLog);
   }

	/**
	 * 额度调整审批
	 * @param operSeq
	 * @param adjState
	 * @throws FlatException
	 */
	@Transactional
	@ResponseBody()
	@RequestMapping(value="/adjustOperateAgreeOrRefuse",method={RequestMethod.POST})
	public void adjustOperateAgreeOrRefuse(@RequestBody Long operSeq,@RequestBody  AdjState adjState) throws FlatException {
		QCcsAcctCrlmtAdjLog qTmLimitAdjLog = QCcsAcctCrlmtAdjLog.ccsAcctCrlmtAdjLog;
		JPAQuery query = new JPAQuery(em); 
		
		CcsAcctCrlmtAdjLog tmLimitAdjLog = query.from(qTmLimitAdjLog).where(qTmLimitAdjLog.org.eq(OrganizationContextHolder.getCurrentOrg())
				.and(qTmLimitAdjLog.opSeq.eq(operSeq)))
				.singleResult(qTmLimitAdjLog);
		
		//同意调额
		if(adjState.equals(AdjState.A)){
			
			checkOprAuth(tmLimitAdjLog.getCreditLmtNew());
			
			
			//获取账户信息修改额度
			CcsAcct tmAccount = cpsBusProvide.getTmAccountTocardNbr(tmLimitAdjLog.getCardNbr(), tmLimitAdjLog.getAcctType());
			
			CcsAcctO tmAccountO = cpsBusProvide.getTmAccountOTocardNbr(tmLimitAdjLog.getCardNbr(), tmLimitAdjLog.getAcctType());
			//距离上次额度调整日期间隔
			AccountAttribute accountAttr=unifiedParaFacilityProvide.acct_attribute(tmAccount.getProductCd());
			if(tmAccount.getLastLimitAdjDate()!=null&&DateUtils.getIntervalDays(tmAccount.getLastLimitAdjDate(),globalManagementService.getSystemStatus().getBusinessDate())
					<accountAttr.creditLimitAdjustInterval.intValue())
				throw new FlatException("上次账户调整额度日期["+tmAccount.getLastLimitAdjDate()+"]后"
					+accountAttr.creditLimitAdjustInterval.intValue()+"天可调整额度");
			CcsCard tmCard = custAcctCardQueryFacility.getCardByCardNbr(tmLimitAdjLog.getCardNbr());
			
			tmAccount.setCreditLmt(tmLimitAdjLog.getCreditLmtNew());
			tmAccountO.setCreditLmt(tmLimitAdjLog.getCreditLmtNew());
			CcsCustomerCrlmt tmCustLimitO = cpsBusProvide.getTmCustLimitOToCustLimitId(tmAccount.getCustLmtId());
			//修改该条记录的调整状态和操作员和当前操作时间
			tmLimitAdjLog.setAdjState(AdjState.A);
			tmLimitAdjLog.setOpId(OrganizationContextHolder.getUsername());
			tmLimitAdjLog.setProcDate(unifiedParaFacilityProvide.BusinessDate());
			tmLimitAdjLog.setOpTime(new Date());
			
			BigDecimal creditLimit = tmLimitAdjLog.getCreditLmtNew();
			//获取账户层参数
			AccountAttribute accountAttribute = unifiedParaFacilityProvide.acct_attribute(tmCard.getProductCd());
			//获取默认超限比例
			BigDecimal vorlmRate = accountAttribute.ovrlmtRate;
			//当调整的账户额度大于客户级额度时，客户级额度 = 账户额度 * 账户层默认超限比例
			creditLimit = creditLimit.add(creditLimit.multiply(vorlmRate)).setScale(0, BigDecimal.ROUND_HALF_UP);
			if (creditLimit.compareTo(tmCustLimitO.getCreditLmt()) > 0){
				tmCustLimitO.setCreditLmt(creditLimit);
			}
			tmAccount.setLastLimitAdjDate(globalManagementService.getSystemStatus().getBusinessDate());

			rTmLimitAdjLog.save(tmLimitAdjLog);
			rCcsAcct.save(tmAccount);
			rCcsAcctO.save(tmAccountO);
			rCcsCustomerCrlmt.save(tmCustLimitO);
			//调额成功发送短信
			SMTNMAAdjustQuotaReq req=new SMTNMAAdjustQuotaReq();
			req.setContractNo(tmAccount.getContrNbr());
			req.setOperator(OrganizationContextHolder.getUsername());
			req.setCreditLmt(tmAccount.getCreditLmt());
			req.setDate(tmAccount.getAcctExpireDate());
			req.setName(tmAccount.getName());
			msQueryService.tnmAAdjustQuota(req);
			
			//记录操作日志
			opeLogUtil.cardholderServiceLog("3303", null, tmLimitAdjLog.getCardNbr(), tmLimitAdjLog.getAcctType().name(), "复核通过, 新额度："+tmLimitAdjLog.getCreditLmtNew());
		}
		
		//拒绝调额
		if(adjState.equals(AdjState.R)){
			tmLimitAdjLog.setAdjState(adjState);
			tmLimitAdjLog.setOpId(OrganizationContextHolder.getUsername());
			tmLimitAdjLog.setOpTime(new Date());
			tmLimitAdjLog.setRejReason("操作员"+operatorAuthUtil.getCurrentOperatorAuth().getOpId());
			//记录操作日志
			opeLogUtil.cardholderServiceLog("3303", null, tmLimitAdjLog.getCardNbr(), tmLimitAdjLog.getAcctType().name(), "复核拒绝，申请额度："+tmLimitAdjLog.getCreditLmtNew());
		}
	}

	private void checkOprAuth(BigDecimal newCredit){
		
		//检查操作员可调整的最大永久额度
		CcsOpPrivilege tmOperAuth = operatorAuthUtil.getCurrentOperatorAuth();
		Organization org = unifiedParameterService.loadParameter(null, Organization.class);
	
		if(tmOperAuth == null || tmOperAuth.getMaxLoanApproveAdj() == null || org == null || org.maxCreditLimit == null ||
				tmOperAuth.getMaxLoanApproveAdj().compareTo(newCredit) < 0 || org.maxCreditLimit.compareTo(newCredit) < 0) {
			throw new FlatException("调整的永久额度已超出操作权限范围！");
		}
	}

	/**
	 * 调额历史记录查询
	 * @param request
	 * @return
	 * @throws FlatException
	 */
	@ResponseBody()
	@RequestMapping(value="/getLimitAdjustLog",method={RequestMethod.POST})
	public FetchResponse getLimitAdjustLog(@RequestBody FetchRequest request) throws FlatException {
		QCcsAcct qTmAccount = QCcsAcct.ccsAcct;
		QCcsAcctCrlmtAdjLogHst qTmLimitAdjLog = QCcsAcctCrlmtAdjLogHst.ccsAcctCrlmtAdjLogHst;
		
		String contrNbr = (String)request.getParameter(CcsAcct.P_ContrNbr);
		Long beginDateStr = null;
		
		if(request.getParameter("beginDate") != null){
			beginDateStr = Long.parseLong((String)request.getParameter("beginDate"));
		}
	    
		JPAQuery q = new JPAQuery(em);
		
		List<CcsAcct> list = q.from(qTmAccount).where(qTmAccount.contrNbr.eq(contrNbr)).list(qTmAccount);
		
		if (!  (list != null && list.size() > 0) ) {
			throw new FlatException("合同号查询不到对应的账户信息");
		}
		AccountType accountType = list.get(0).getAcctType();
		
		JPAQuery q1 = new JPAQuery(em);
		q1 = q1.from(qTmLimitAdjLog).where(qTmLimitAdjLog.org.eq(OrganizationContextHolder.getCurrentOrg())
				.and(qTmLimitAdjLog.acctNbr.eq(list.get(0).getAcctNbr()))
				.and(qTmLimitAdjLog.acctType.eq(accountType)))
				.orderBy(new OrderSpecifier<Long>(Order.DESC, qTmLimitAdjLog.opSeq));
		
		if (beginDateStr != null ) {
		    	Date beginDate = new Date(beginDateStr);
			beginDate = DateUtils.truncate(beginDate, Calendar.DATE);
			q1 = q1.where(qTmLimitAdjLog.opTime.goe(beginDate));
		}	
		
		return new JPAQueryFetchResponseBuilder(request, q1)
			.addFieldMapping(qTmLimitAdjLog).build();
			
	}
}
