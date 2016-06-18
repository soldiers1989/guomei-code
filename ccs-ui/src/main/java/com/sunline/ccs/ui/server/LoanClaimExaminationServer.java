package com.sunline.ccs.ui.server;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.DateUtils;
import com.sunline.ccs.facility.AuthCommUtils.MsgParameter;

//import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
//import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
//import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
//import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
//import com.sunline.ccs.infrastructure.shared.model.CcsOpPrivilege;
import com.sunline.ccs.infrastructure.shared.model.CcsOpPrivilege;

import com.sunline.kylin.web.ark.client.JPAQueryFetchResponseBuilder;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanType;

/**
 * 贷款审核处理server
* @author dch
 *
 */
@Controller
@RequestMapping(value="/t3306Server")
public class LoanClaimExaminationServer{
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private RCcsLoanReg rTmLoanReg;
	@Autowired
	private GlobalManagementService globalManagementService;
	
	@Autowired
	private OperatorAuthUtil operatorAuthUtil;
	
	@Autowired
	private OpeLogUtil opeLogUtil;
	
	QCcsLoanReg qTmLoanReg = QCcsLoanReg.ccsLoanReg;
	QCcsCard qTmCard = QCcsCard.ccsCard;
	private BooleanExpression exp;
	
	/**
	 * 根据条件查询需要审核的贷款列表
	 */
	@Transactional
	@ResponseBody()
	@RequestMapping(value="/getNeedExamineList",method={RequestMethod.POST})
	public FetchResponse getNeedExamineList(@RequestBody FetchRequest request) throws FlatException{
//		,@RequestBody String loanType, @RequestBody String loanAction, @RequestBody Date beginDate, @RequestBody Date endDate
		logger.debug("贷款列表查询开始...");
		JPAQuery query = new JPAQuery(em);
		try {
			exp = qTmLoanReg.loanAction.ne(LoanAction.A).and(qTmLoanReg.loanAction.ne(LoanAction.P)).and(qTmLoanReg.loanRegStatus.eq(LoanRegStatus.N))
					.and(qTmLoanReg.acctType.eq(AccountType.E))
					.and(qTmLoanReg.org.eq(OrganizationContextHolder.getCurrentOrg()));
			
			if(request.getParameter(CcsLoanReg.P_LoanType) != null){
				String loanType = request.getParameter(CcsLoanReg.P_LoanType).toString();
				logger.debug("贷款审核查询的类型："+loanType);
				exp = exp.and(qTmLoanReg.loanType.eq(LoanType.valueOf(loanType)));
			}
			if(request.getParameter(CcsLoanReg.P_LoanAction) != null){
				String loanAction = request.getParameter(CcsLoanReg.P_LoanAction).toString();
				logger.debug("贷款审核查询的贷款代码："+loanAction);
				exp = exp.and(qTmLoanReg.loanAction.eq(LoanAction.valueOf(loanAction)));
			}
			if(request.getParameter("bDate") != null){
				Date beginDate = DateUtils.truncate(
						new Date(Long.parseLong(request.getParameter("bDate").toString())), Calendar.DATE);
				logger.debug("贷款审核查询的起始时间："+df.format(beginDate));
				exp = exp.and(qTmLoanReg.requestTime.goe(beginDate));
			}
			if(request.getParameter("eDate") != null){
				Date endDate = DateUtils.truncate(
						new Date(Long.parseLong(request.getParameter("eDate").toString())), Calendar.DATE);
				logger.debug("贷款审核查询的结束时间："+df.format(endDate));
				exp = exp.and(qTmLoanReg.requestTime.lt(endDate));
			}
			query = query.from(qTmLoanReg).where(exp).orderBy(new OrderSpecifier<Long>(Order.DESC, qTmLoanReg.registerId));
		} catch (Exception e) {
			logger.debug(""+e);
			throw new FlatException("贷款审核信息查询报错："+e.getMessage());
		}
		
		return new JPAQueryFetchResponseBuilder(request, query)
		.addFieldMapping(CcsLoanReg.P_Org, qTmLoanReg.org)
		.addFieldMapping(CcsLoanReg.P_AcctNbr, qTmLoanReg.acctNbr)
		.addFieldMapping(CcsLoanReg.P_AcctType, qTmLoanReg.acctType)
		.addFieldMapping(CcsLoanReg.P_RegisterId, qTmLoanReg.registerId)
		.addFieldMapping(CcsLoanReg.P_CardNbr, qTmLoanReg.cardNbr)
		.addFieldMapping(CcsLoanReg.P_LogicCardNbr, qTmLoanReg.logicCardNbr)
		.addFieldMapping(CcsLoanReg.P_RefNbr, qTmLoanReg.refNbr)
		.addFieldMapping(CcsLoanReg.P_OrigAuthCode, qTmLoanReg.origAuthCode)
		.addFieldMapping(CcsLoanReg.P_OrigTransDate, qTmLoanReg.origTransDate)
		.addFieldMapping(CcsLoanReg.P_OrigTxnAmt, qTmLoanReg.origTxnAmt)
		.addFieldMapping(CcsLoanReg.P_B007TxnTime, qTmLoanReg.b007TxnTime)
		.addFieldMapping(CcsLoanReg.P_B011Trace, qTmLoanReg.b011Trace)
		.addFieldMapping(CcsLoanReg.P_B032AcqInst, qTmLoanReg.b032AcqInst)
		.addFieldMapping(CcsLoanReg.P_B033FwdIns, qTmLoanReg.b033FwdIns)
		.addFieldMapping(CcsLoanReg.P_LoanCode, qTmLoanReg.loanCode)
		.addFieldMapping(CcsLoanReg.P_LoanFeeMethod, qTmLoanReg.loanFeeMethod)
		.addFieldMapping(CcsLoanReg.P_LoanFinalTermFee, qTmLoanReg.loanFinalTermFee)
		.addFieldMapping(CcsLoanReg.P_LoanFinalTermPrin, qTmLoanReg.loanFinalTermPrin)
		.addFieldMapping(CcsLoanReg.P_LoanFixedFee, qTmLoanReg.loanFixedFee)
		.addFieldMapping(CcsLoanReg.P_LoanFixedPmtPrin, qTmLoanReg.loanFixedPmtPrin)
		.addFieldMapping(CcsLoanReg.P_LoanFirstTermFee, qTmLoanReg.loanFirstTermFee)
		.addFieldMapping(CcsLoanReg.P_LoanFirstTermPrin, qTmLoanReg.loanFirstTermPrin)
		.addFieldMapping(CcsLoanReg.P_LoanInitFee, qTmLoanReg.loanInitFee)
		.addFieldMapping(CcsLoanReg.P_LoanInitPrin, qTmLoanReg.loanInitPrin)
		.addFieldMapping(CcsLoanReg.P_LoanInitTerm, qTmLoanReg.loanInitTerm)
		.addFieldMapping(CcsLoanReg.P_LoanType, qTmLoanReg.loanType)
		.addFieldMapping(CcsLoanReg.P_LoanAction, qTmLoanReg.loanAction)
		.addFieldMapping(CcsLoanReg.P_RequestTime, qTmLoanReg.requestTime)
		.addFieldMapping(CcsLoanReg.P_RegisterDate, qTmLoanReg.registerDate)
		.addFieldMapping(CcsLoanReg.P_Matched, qTmLoanReg.matched)
		.addFieldMapping(CcsLoanReg.P_InterestRate, qTmLoanReg.interestRate)
		.addFieldMapping(CcsLoanReg.P_PenaltyRate, qTmLoanReg.penaltyRate)
		.addFieldMapping(CcsLoanReg.P_CompoundRate, qTmLoanReg.compoundRate)
		.addFieldMapping(CcsLoanReg.P_FloatRate, qTmLoanReg.floatRate)
		.addFieldMapping(CcsLoanReg.P_AdvPmtAmt, qTmLoanReg.advPmtAmt)
		.addFieldMapping(CcsLoanReg.P_DueBillNo, qTmLoanReg.dueBillNo)
		.addFieldMapping(CcsLoanReg.P_ValidDate, qTmLoanReg.validDate)
		.addFieldMapping(CcsLoanReg.P_ExtendTerm, qTmLoanReg.extendTerm)
		.addFieldMapping(CcsLoanReg.P_ShortedTerm, qTmLoanReg.shortedTerm)
		.addFieldMapping(CcsLoanReg.P_ShortedPmtDue, qTmLoanReg.shortedPmtDue)
		.addFieldMapping(CcsLoanReg.P_ShortedType, qTmLoanReg.shortedType)
		.build();
	}

	/**
	 * 贷款审核处理
	 */
	@Transactional
	@ResponseBody()
	@RequestMapping(value="/setArgeeOrRefuse",method={RequestMethod.POST})
	public void setArgeeOrRefuse(@RequestBody String flag, @RequestBody CcsLoanReg values) throws FlatException{
	    	logger.info("===================================values:"+values.getRegisterId());
		Long registerId = values.getRegisterId();
		String remark = values.getRemark();
		
		CcsLoanReg tmLoanReg = rTmLoanReg.findOne(qTmLoanReg.registerId.eq(registerId));
		if(tmLoanReg == null){
			throw new FlatException("找不到贷款注册信息");
		}
		//true为通过，false为拒绝
		if("0".equals(flag)){
			CcsOpPrivilege tmOperAuth = operatorAuthUtil.getCurrentOperatorAuth();
//			if(null != tmOperAuth || tmOperAuth.getAdjLoanParamMax() != null ||
//					tmOperAuth.getAdjLoanParamMax().compareTo(tmLoanReg.getLoanInitPrin()) < 0) {
//				throw new FlatException("贷款金额已超过该操作员的操作权限范围！");
//			}
		
			tmLoanReg.setLoanRegStatus(LoanRegStatus.A);
			if(tmLoanReg.getValidDate() == null){
				tmLoanReg.setValidDate(globalManagementService.getSystemStatus().getBusinessDate());
			}
			
			tmLoanReg.setRemark(remark);
			logger.info("交易码["+LoanRegStatus.A+"审核通过");
		}else {
			tmLoanReg.setLoanRegStatus(LoanRegStatus.D);
			tmLoanReg.setRemark(remark);
			
			logger.info("交易码["+LoanRegStatus.D +"审核拒绝");
			//拒绝之后发送金融交易报文，恢复额度
			//查找主信用计划模板
//			TmCard tmCard = rTmCard.findOne(qTmCard.logicalCardNo.eq(tmLoanReg.getLogicalCardNo()));
//			ProductCredit productCredit = unifiedParameterService.loadParameter(tmCard.getProductCd(), ProductCredit.class);
//			String mcc = "5999"; 
//			
//			String b003 = getTransType(productCredit);
//			MsgParameter m = makeMsgParam(tmLoanReg, mcc, b003,generateFlowNo());
//			m.setB038(tmLoanReg.getOrigAuthCode());
//			m.setB090(AuthUtil.makeFeild90(AuthUtil.TPS_VAL_MTI_ADJUST, tmLoanReg.getB011(), tmLoanReg.getB007(), tmLoanReg.getB032(), tmLoanReg.getB033()));
//			YakMessage request = AuthUtil.makeCashLoanReverseRequestMsg(m);
//			
//			YakMessage response = authorizationService.authorize(request);
//			
//			String respCode;
//			if(response != null){
//				respCode = response.getBody(39);
//			}else{
//				throw new ProcessException("授权处理超时");
//			}
//			if(StringUtils.equals(respCode,"00") && !StringUtils.equals(respCode, "11")){
//				throw new ProcessException("恢复额度失败");
//			}
		}
		rTmLoanReg.save(tmLoanReg);
		
		//记录操作日志
        opeLogUtil.cardholderServiceLog(
	    		"3306", tmLoanReg.getRegisterId(), tmLoanReg.getCardNbr(),tmLoanReg.getAcctNbr()+"---"+tmLoanReg.getAcctType(),"贷款审核结果："+tmLoanReg.getLoanRegStatus()+" - "+tmLoanReg.getRemark());
	}
	
	/**
	 * 生成yakmessage参数对象
	 * @param reg
	 * @param mcc
	 * @param b003
	 * @return
	 */
	private MsgParameter makeMsgParam(CcsLoanReg reg, String mcc, String b003,String b037) {
		MsgParameter m = new MsgParameter();
		m.setB003(b003);
		m.setB025(b003 == "000000" ? "00" : "64");
		m.setBusiDate(globalManagementService.getSystemStatus().getBusinessDate());
		m.setCardNo(reg.getCardNbr());
		m.setCurrCd(reg.getAcctType().getCurrencyCode());
		m.setMcc(mcc);
		m.setTxnAmt(getAuthAmt(reg.getLoanInitPrin()));
		m.setB037(b037);
		return m;
	}
	
	/**
	 * 生成授权要用的金额值*100取整
	 * @param b
	 * @return
	 */
	private static String getAuthAmt(BigDecimal b){
		DecimalFormat df = new DecimalFormat("#0");
		return df.format(b.doubleValue()*100);
	}
	
//	private String generateFlowNo(){
//		DateFormat df = new SimpleDateFormat("yyDS");
//		Calendar c = Calendar.getInstance();
//		int hour = c.get(Calendar.HOUR_OF_DAY);
//		int minute = c.get(Calendar.MINUTE);
//		int second = c.get(Calendar.SECOND);
//		StringBuffer sb = new StringBuffer(df.format(c.getTime()));
//		sb.append(hour*60*60+minute*60+second);
//		return sb.substring(1);
//	}
	
	/**
	 * 根据使用额度类型获得交易类型
	 * @param tmCard 
	 * @return
	 * @throws ProcessException
	 */
//	private String getTransType(ProductCredit productCredit) throws ProcessException {
//		String transType = "010000";//取现 
//		if(productCredit.cashLoanLimitType == CashLoanLimitType.L){
//			transType="000000";//消费
//		}
//		return transType;
//	}

}
