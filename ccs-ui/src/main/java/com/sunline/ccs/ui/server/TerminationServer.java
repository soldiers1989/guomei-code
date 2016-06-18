/**
 * 
 */
package com.sunline.ccs.ui.server;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.time.DateFormatUtils;
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
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
//import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
//import com.sunline.ccs.infrastructure.shared.model.TmCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
//import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
//import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.enums.LoanTerminateReason;
import com.sunline.ccs.ui.server.commons.BlockCodeUtil;
import com.sunline.ccs.ui.server.commons.CPSBusProvide;
import com.sunline.ccs.ui.server.commons.CheckUtil;
import com.sunline.kylin.web.ark.client.JPAQueryFetchResponseBuilder;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.OrderStatus;


/**
 * 分期交易中止
* @author fanghj
 *
 */
@Controller
@RequestMapping(value="/t1310Server")
public class TerminationServer  {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private GlobalManagementService globalManagementService;
	
//	@Autowired
//	private CPSLoanService cpsLoanService;
	@Autowired
	private OpeLogUtil opeLogUtil;
	@Autowired
	private RCcsLoan rTmLoan;
	@Autowired
	private RCcsAcct rCcsAcct;
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private CPSBusProvide cpsBusProvide;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;
	
	private QCcsOrder qCcsOrder = QCcsOrder.ccsOrder;
	
	@SuppressWarnings("all")
	@ResponseBody()
	@RequestMapping(value="/saveInstalmentPlan",method={RequestMethod.POST})
	public Map<String,Integer> saveInstalmentPlan(@RequestBody Map loanMap,@RequestBody int flag) throws FlatException {
		CcsLoan tmLoan=new CcsLoan();
		//{currTerm=2, registerDate=1.4324832E12, acctType=C, paidOutDate=-2.88E7, loanInitTerm=3, paidFee=0, loanFinalTermPrin=1000, loanPrinXfrout=0, loanInitPrin=3000, loanFixedPmtPrin=1000, loanType=R, origTxnAmt=3000, lastLoanStatus=A, loanInitFee=150, loanFirstTermPrin=1000, loanFixedFee=0, unstmtFee=0, loanFeeXfrin=150, origAuthCode=101202, remainTerm=1, terminalDate=1.4351616E12, loanPlanDescription=普通消费分期-分次收取, loanFirstTermFee=150, cardNbr=6251560010000889, loanCurrBal=3216.93, activeDate=1.4324832E12, refNbr=07333887848788964524738, interestRate=0.0065, paidPrincipal=0, unstmtPrin=0, loanStatus=T, loanFinalTermFee=0, origTransDate=1.4320512E12, loanPrinXfrin=3000, loanBalXfrout=0, loanFeeXfrout=0, terminalReasonCd=M, loanBalXfrin=3216.93, loanId=29}
		//long lRegisterDate = Long.parseLong(String.valueOf(loanMap.get("registerDate")));
		if(loanMap.get("registerDate")!=null){
		long lRegisterDate = new BigDecimal(String.valueOf(loanMap.get("registerDate"))).longValue();
		Date date1 = new Date();
		date1.setTime(lRegisterDate);
		loanMap.put("registerDate", date1);
		}
		//long lPaidOutDate = Long.parseLong(String.valueOf(loanMap.get("paidOutDate")));
		if(loanMap.get("paidOutDate")!=null){
		long lPaidOutDate = new BigDecimal(String.valueOf(loanMap.get("paidOutDate"))).longValue();
		Date date2 = new Date();
		date2.setTime(lPaidOutDate);
		loanMap.put("paidOutDate", date2);
		}
		//long lTerminalDate = Long.parseLong(String.valueOf(loanMap.get("terminalDate")));
		if(loanMap.get("terminalDate")!=null){
		long lTerminalDate = new BigDecimal(String.valueOf(loanMap.get("terminalDate"))).longValue();
		Date date3 = new Date();
		date3.setTime(lTerminalDate);
		loanMap.put("terminalDate", date3);
		}
		//long lOrigTransDate = Long.parseLong(String.valueOf(loanMap.get("origTransDate")));
		if(loanMap.get("origTransDate")!=null){
		long lOrigTransDate = new BigDecimal(String.valueOf(loanMap.get("origTransDate"))).longValue();
		Date date4 = new Date();
		date4.setTime(lOrigTransDate);
		loanMap.put("origTransDate", date4);
		}
		//long lActiveDate = Long.parseLong(String.valueOf(loanMap.get("activeDate")));
		if(loanMap.get("activeDate")!=null){
		long lActiveDate = new BigDecimal(String.valueOf(loanMap.get("activeDate"))).longValue();
		Date date5 = new Date();
		date5.setTime(lActiveDate);
		loanMap.put("activeDate", date5);
		}
//		tmLoan.updateFromMap((Map<String, Serializable>)loanMap);
		
		tmLoan.setContrNbr((String) loanMap.get(CcsLoan.P_ContrNbr));
		tmLoan.setAcctNbr(Long.parseLong((String) loanMap.get(CcsLoan.P_AcctNbr)));
		tmLoan.setAcctType(AccountType.valueOf((String) loanMap.get(CcsLoan.P_AcctType)));
		tmLoan.setLoanId(Long.parseLong(loanMap.get(CcsLoan.P_LoanId)+""));
		tmLoan.setLoanStatus(LoanStatus.valueOf(loanMap.get(CcsLoan.P_LoanStatus)+""));
		Map<String,Integer> data=new HashMap<String, Integer>();
		String logDesc = null;
//		Object loanId = tmLoan.getLoanId();
//		String loanId = loanMap.get(CcsLoan.P_LoanId);
		if(flag==0){//分期提前终止-按原交易
			this.NF6303(tmLoan);
			logDesc = "分期提前终止: 分期计划ID["+loanMap.get(CcsLoan.P_LoanId)+"]";
		}else{//分期提前终止当日撤销
			this.NF6304(tmLoan);
			logDesc = "分期提前终止当日撤销: 分期计划ID["+loanMap.get(CcsLoan.P_LoanId)+"]";
		}
		//记录操作日志，位置错误
//		opeLogUtil.cardholderServiceLog("1310", null,loanMap.get(CcsLoan.P_LoanId)+"",loanMap.get(CcsLoan.P_LoanId)+"", logDesc);
		data.put("result", 0);
		return data;
	}

	@SuppressWarnings("all")
	@ResponseBody()
	@RequestMapping(value="/getTmLoanList",method={RequestMethod.POST})
	public FetchResponse getTmLoanList(@RequestBody FetchRequest request) throws FlatException{
		String contrNbr=(String) request.getParameter("contrNbr");
		FetchResponse rsp = new FetchResponse();
	    rsp =this.NF6101(request,contrNbr);
		return rsp;
	}

	//分期交易查询
	@Transactional
	public FetchResponse NF6101(FetchRequest queryRequest,String contrNbr) throws FlatException {
		logger.info("NF6101:卡号后四位[" + CodeMarkUtils.subCreditCard(contrNbr) + "]");
		CheckUtil.checkCardNo(contrNbr);
//		CheckUtil.rejectNull(cardAccountIndicator, "功能码不能为空");
		
		QCcsCardLmMapping qTmCardMediaMap = QCcsCardLmMapping.ccsCardLmMapping;
		QCcsCard qTmCard = QCcsCard.ccsCard;
		QCcsLoan qTmLoan = QCcsLoan.ccsLoan;
		
		JPAQuery query = new JPAQuery(em);
		
		BooleanExpression booleanExpression;
		booleanExpression = qTmLoan.contrNbr.eq(contrNbr).and(qTmLoan.org.eq(OrganizationContextHolder.getCurrentOrg()));
//		if (cardAccountIndicator.equals("A")) {
			query = query.from(qTmLoan);
//			booleanExpression = 
//					qTmCardMediaMap.logicCardNbr.eq(qTmCard.logicCardNbr)
//					.and(qTmCard.acctNbr.eq(qTmLoan.acctNbr)
//							.and(qTmCardMediaMap.cardNbr.eq(cardNo)
//							.and(qTmCardMediaMap.org.eq(OrganizationContextHolder.getCurrentOrg()))));
			
			//query = query.from(qTmLoan);
			
			//booleanExpression = qTmLoan.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qTmLoan.acctNbr.eq(tmCard.getAcctNbr()));
			
			query=query.where(booleanExpression);					
			// 按介质卡查询
		/*} else if (cardAccountIndicator.equals("C")) {
			
			query=query.from(qTmCardMediaMap,qTmLoan);
			booleanExpression = qTmCardMediaMap.org.eq(qTmLoan.org)
					.and(qTmCardMediaMap.org.eq(OrganizationContextHolder.getCurrentOrg())
							.and(qTmLoan.cardNbr.eq(cardNo)));
			
			//query=query.from(qTmLoan);
			//booleanExpression = qTmLoan.org.eq(OrganizationContextHolder.getCurrentOrg())
			//		 .and(qTmLoan.logicCardNbr.eq(tmCard.getLogicCardNbr()));
			
			query=query.where(booleanExpression);		
		} else{
			throw new FlatException("无效的功能码[" + cardAccountIndicator + "]");
		}*/
	
		return new JPAQueryFetchResponseBuilder(queryRequest, query)
		        .addFieldMapping(CcsLoan.P_LoanId,qTmLoan.loanId)
		        .addFieldMapping(CcsLoan.P_AcctNbr,qTmLoan.acctNbr)
				.addFieldMapping(CcsLoan.P_AcctType,qTmLoan.acctType)
				.addFieldMapping(CcsLoan.P_RefNbr,qTmLoan.refNbr)
				.addFieldMapping(CcsLoan.P_ContrNbr,qTmLoan.contrNbr)
				.addFieldMapping(CcsLoan.P_CardNbr,qTmLoan.cardNbr)
				.addFieldMapping(CcsLoan.P_RegisterDate,qTmLoan.registerDate)
				.addFieldMapping(CcsLoan.P_LoanType,qTmLoan.loanType)
				.addFieldMapping(CcsLoan.P_LoanStatus,qTmLoan.loanStatus)
				.addFieldMapping(CcsLoan.P_LastLoanStatus,qTmLoan.lastLoanStatus)
				.addFieldMapping(CcsLoan.P_LoanInitTerm,qTmLoan.loanInitTerm)
				.addFieldMapping(CcsLoan.P_CurrTerm,qTmLoan.currTerm)
				.addFieldMapping(CcsLoan.P_RemainTerm,qTmLoan.remainTerm)
				.addFieldMapping(CcsLoan.P_LoanInitPrin,qTmLoan.loanInitPrin)
				.addFieldMapping(CcsLoan.P_LoanFixedPmtPrin,qTmLoan.loanFixedPmtPrin)
				.addFieldMapping(CcsLoan.P_LoanFirstTermPrin,qTmLoan.loanFirstTermPrin)
				.addFieldMapping(CcsLoan.P_LoanFinalTermPrin,qTmLoan.loanFinalTermPrin)
				.addFieldMapping(CcsLoan.P_LoanInitFee,qTmLoan.loanInitFee)
				.addFieldMapping(CcsLoan.P_LoanFixedFee,qTmLoan.loanFixedFee)
				.addFieldMapping(CcsLoan.P_LoanFirstTermFee,qTmLoan.loanFirstTermFee)
				.addFieldMapping(CcsLoan.P_LoanFinalTermFee,qTmLoan.loanFinalTermFee)
				.addFieldMapping(CcsLoan.P_UnstmtPrin,qTmLoan.unstmtPrin)
				.addFieldMapping(CcsLoan.P_UnstmtFee,qTmLoan.unstmtFee)
				.addFieldMapping(CcsLoan.P_ActiveDate,qTmLoan.activeDate)
				.addFieldMapping(CcsLoan.P_PaidOutDate,qTmLoan.paidOutDate)
				.addFieldMapping(CcsLoan.P_TerminalDate,qTmLoan.terminalDate)
				.addFieldMapping(CcsLoan.P_TerminalReasonCd,qTmLoan.terminalReasonCd)
				.addFieldMapping(CcsLoan.P_PaidPrincipal,qTmLoan.paidPrincipal)
				.addFieldMapping(CcsLoan.P_InterestRate,qTmLoan.paidInterest)
				.addFieldMapping(CcsLoan.P_PaidFee,qTmLoan.paidFee)
				.addFieldMapping(CcsLoan.P_LoanCurrBal,qTmLoan.loanCurrBal)
				.addFieldMapping(CcsLoan.P_LoanBalXfrout,qTmLoan.loanBalXfrout)
				.addFieldMapping(CcsLoan.P_LoanBalXfrin,qTmLoan.loanBalXfrin)
				.addFieldMapping(CcsLoan.P_LoanPrinXfrout,qTmLoan.loanPrinXfrout)
				.addFieldMapping(CcsLoan.P_LoanPrinXfrin,qTmLoan.loanPrinXfrin)
				.addFieldMapping(CcsLoan.P_LoanFeeXfrout,qTmLoan.loanFeeXfrout)
				.addFieldMapping(CcsLoan.P_LoanFeeXfrin,qTmLoan.loanFeeXfrin)
				.addFieldMapping(CcsLoan.P_OrigTxnAmt,qTmLoan.origTxnAmt)
				.addFieldMapping(CcsLoan.P_OrigTransDate,qTmLoan.origTransDate)
				.addFieldMapping(CcsLoan.P_OrigAuthCode,qTmLoan.origAuthCode)
				.addFieldMapping(CcsLoan.P_InterestRate,qTmLoan.interestRate)
				.addFieldMapping(CcsLoan.P_LoanCode,qTmLoan.loanCode)
				
				.addFieldMapping(CcsLoan.P_TotLifeInsuAmt, qTmLoan.totLifeInsuAmt)
				      .addFieldMapping(CcsLoan.P_TotPrepayPkgAmt,qTmLoan.totPrepayPkgAmt)
				    .addFieldMapping(CcsLoan.P_UnstmtLifeInsuAmt, qTmLoan.unstmtLifeInsuAmt)
				    .addFieldMapping(CcsLoan.P_PastLifeInsuAmt, qTmLoan.pastLifeInsuAmt)
				    .addFieldMapping(CcsLoan.P_LifeInsuFeeMethod, qTmLoan.lifeInsuFeeMethod)
				    .addFieldMapping(CcsLoan.P_PaidLifeInsuAmt, qTmLoan.paidLifeInsuAmt)
				    .addFieldMapping(CcsLoan.P_LifeInsuFeeRate, qTmLoan.lifeInsuFeeRate)
				  .addFieldMapping(CcsLoan.P_CpdBeginDate,qTmLoan.cpdBeginDate)
				  .addFieldMapping(CcsLoan.P_UnstmtPrepayPkgAmt,qTmLoan.unstmtPrepayPkgAmt)
				  .addFieldMapping(CcsLoan.P_PaidPrepayPkgAmt,qTmLoan.paidPrepayPkgAmt)
				  .addFieldMapping(CcsLoan.P_PastPrepayPkgAmt,qTmLoan.pastPrepayPkgAmt)
				  .addFieldMapping(CcsLoan.P_PrepayPkgFeeMethod,qTmLoan.prepayPkgFeeMethod)
				  .addFieldMapping(CcsLoan.P_PrepayPkgFeeRate,qTmLoan.prepayPkgFeeRate)
				
			    .build();
	}

	//分期产品描述查询
	@Transactional
	@ResponseBody()
	@RequestMapping(value="/getLoanPlanDescription",method={RequestMethod.POST})
	public Map<String,String> getLoanPlanDescription(@RequestBody String loanCode) throws FlatException {
		logger.info("getLoanPlanDescription:分期计划代码[" + loanCode + "]");
		LoanPlan loanPlan = unifiedParameterFacilityProvide.loanPlan(loanCode);
		String loanPlanDescription = loanPlan.description == null||"".equals(loanPlan.description)?"":loanPlan.description;
		Map<String,String> result=new HashMap<String,String>();
		result.put("desc", loanPlanDescription);
		return result;
	}
	
	@Transactional
	public void NF6303(CcsLoan tmLoan) throws FlatException {
		CcsLoan tmLoantemp = tmLoan;
		QCcsLoan qTmLoan = QCcsLoan.ccsLoan;
		
		JPAQuery query = new JPAQuery(em);
		List<CcsOrder> ccsOrderList = query.from(qCcsOrder).where(qCcsOrder.acctNbr.eq(tmLoantemp.getAcctNbr())
				.and(qCcsOrder.acctType.eq(tmLoantemp.getAcctType()))
				.and(qCcsOrder.contrNbr.eq(tmLoantemp.getContrNbr()))
				.and(qCcsOrder.orderStatus.eq(OrderStatus.W))
				.and(qCcsOrder.txnType.eq(AuthTransType.TransferCredit))                 //判断是否存在在途的扣款
				).list(qCcsOrder);
		
		if(ccsOrderList != null && ccsOrderList.size() > 0){
			throw new FlatException("存在处理中扣款,无法终止");
		}
		
		CcsLoan tmLoanSearch = rTmLoan.findOne(qTmLoan.org.trim().eq(OrganizationContextHolder.getCurrentOrg()).and(qTmLoan.loanId.eq(tmLoantemp.getLoanId())));
		CheckUtil.rejectNull(tmLoanSearch, "分期交易不存在，无法终止");
		if(LoanStatus.F.equals(tmLoanSearch.getLoanStatus())){
			throw new FlatException("贷款已完成，无法终止");
		}
		if (tmLoanSearch.getLoanStatus().equals(LoanStatus.T)) {
			throw new FlatException("分期终止失败，该分期已为终止状态");
		}
		
		//对应账户上强制结清锁定码
		CcsAcctKey key = new CcsAcctKey();
		key.setAcctNbr(tmLoanSearch.getAcctNbr());
		key.setAcctType(tmLoanSearch.getAcctType());

		CcsAcct ccsAcct = rCcsAcct.findOne(key);
		//如果账户为阳光，则拒绝
		if(ccsAcct.getCustSource().equals(InputSource.SUNS)){
			throw new FlatException("不支持贷款类型");
		}
		String blockCode = ccsAcct.getBlockCode();
		ccsAcct.setBlockCode(BlockCodeUtil.addBlockCode(blockCode, "I"));
		
		tmLoanSearch.setLoanStatus(LoanStatus.T);
		tmLoanSearch.setTerminalDate(globalManagementService.getSystemStatus().getBusinessDate());
		tmLoanSearch.setLastLoanStatus(tmLoantemp.getLoanStatus());
		tmLoanSearch.setTerminalReasonCd(LoanTerminateReason.M);
		
		//记录操作日志
		opeLogUtil.cardholderServiceLog("1310", null,tmLoanSearch.getLoanId()+"",tmLoanSearch.getLoanId()+"", "分期提前终止: 分期计划ID["+tmLoanSearch.getLoanId()+"]");
		
//		rTmLoan.save(tmLoanSearch);
	}

	@Transactional
	public void NF6304(CcsLoan tmLoan) throws FlatException {
		CcsLoan tmLoantemp = tmLoan;
		QCcsLoan qTmLoan = QCcsLoan.ccsLoan;
		
		JPAQuery query = new JPAQuery(em);
		List<CcsOrder> ccsOrderList = query.from(qCcsOrder).where(qCcsOrder.acctNbr.eq(tmLoantemp.getAcctNbr())
				.and(qCcsOrder.acctType.eq(tmLoantemp.getAcctType()))
				.and(qCcsOrder.contrNbr.eq(tmLoantemp.getContrNbr()))
				.and(qCcsOrder.orderStatus.eq(OrderStatus.W))
				.and(qCcsOrder.txnType.eq(AuthTransType.TransferCredit))                 //判断是否存在在途的扣款
				).list(qCcsOrder);
		
		if(ccsOrderList != null && ccsOrderList.size() > 0){
			throw new FlatException("存在处理中扣款,无法终止撤销");
		}
		
		CcsLoan tmLoanSearch = rTmLoan.findOne(qTmLoan.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qTmLoan.loanId.eq(tmLoantemp.getLoanId())));
		CheckUtil.rejectNull(tmLoanSearch, "分期交易不存在，无法终止撤销");
		if (!tmLoanSearch.getLoanStatus().equals(LoanStatus.T)) {
			throw new FlatException("分期不是终止状态，无法进行撤销");
		}
		if (tmLoanSearch.getTerminalDate() != null && !DateFormatUtils.ISO_DATE_FORMAT.format(tmLoanSearch.getTerminalDate()).equals(DateFormatUtils.ISO_DATE_FORMAT.format(globalManagementService.getSystemStatus().getBusinessDate()))) {
			throw new FlatException("隔日的分期终止，无法进行撤销");
		}
		tmLoanSearch.setLoanStatus(tmLoanSearch.getLastLoanStatus());
		tmLoanSearch.setLastLoanStatus(null);
		tmLoanSearch.setTerminalReasonCd(null);
		tmLoanSearch.setTerminalDate(null);
		
		// 对应账户上移除强制结清锁定码
		CcsAcct ccsAcct = rCcsAcct.findOne(new CcsAcctKey(tmLoanSearch.getAcctNbr(),tmLoanSearch.getAcctType()));
		String blockCode = ccsAcct.getBlockCode();
		ccsAcct.setBlockCode(BlockCodeUtil.removeBlockCode(blockCode, "I"));
		
		//记录操作日志
		opeLogUtil.cardholderServiceLog("1310", null,tmLoanSearch.getLoanId()+"",tmLoanSearch.getLoanId()+"", "分期提前终止当日撤销: 分期计划ID["+tmLoanSearch.getLoanId()+"]");
		
	}
	
}
