package com.sunline.ccs.ui.server;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;
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
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.DateUtils;
import com.sunline.ccs.facility.AuthCommUtils.MsgParameter;
import com.sunline.ccs.infrastructure.server.repos.RCcsCard;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsOpPrivilege;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.CashLoanLimitType;
import com.sunline.kylin.web.ark.client.JPAQueryFetchResponseBuilder;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.api.AuthorizationService;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;

/**
 * 现金分期审核
 * 
* @author songyc
 * @date 2013-7-23  上午9:38:33
 * @version 1.0
 */
@Controller
@RequestMapping(value = "/t3305Server")
public class CashStagingServer{
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private GlobalManagementService globalManagementService;
	@Autowired
	private UnifiedParameterFacility unifiedParameterService;
	
	@Resource(name="authorizationService")
	private AuthorizationService authorizationService;
	@Autowired
	private RCcsCard rCcsCard;
	@Autowired
	private RCcsLoanReg rCcsLoanReg;
	
	@Autowired
	private OperatorAuthUtil operatorAuthUtil;
	
	QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;
	QCcsCard qCcsCard = QCcsCard.ccsCard;
	
	

	@Transactional
	@ResponseBody()
	@RequestMapping(value = "/getNeedExamineList", method = {RequestMethod.POST})
	public FetchResponse getNeedExamineList(FetchRequest request, Date beginDate, Date endDate) throws FlatException {
		
		JPAQuery query = new JPAQuery(em).from(qCcsLoanReg);
		{
			if(beginDate != null){
				beginDate = DateUtils.truncate(beginDate, Calendar.DATE);
				logger.debug("现金分期审核查询的起始时间："+beginDate);
				query = query.where(qCcsLoanReg.requestTime.goe(beginDate));
			}
			if(endDate != null){
				endDate = DateUtils.truncate(DateUtils.addDays(endDate, 1), Calendar.DATE);
				logger.debug("现金分期审核查询的结束时间："+endDate);
				query = query.where(qCcsLoanReg.requestTime.lt(endDate));
			}
			
			query = query.where(qCcsLoanReg.loanAction.eq(LoanAction.A).and(qCcsLoanReg.loanRegStatus.eq(LoanRegStatus.N))
					.and(qCcsLoanReg.org.eq(OrganizationContextHolder.getCurrentOrg())))
					.orderBy(new OrderSpecifier<Long>(Order.DESC, qCcsLoanReg.registerId));
		}
				
		return new JPAQueryFetchResponseBuilder(request, query)
		.addFieldMapping(CcsLoanReg.P_Org, qCcsLoanReg.org)
		.addFieldMapping(CcsLoanReg.P_AcctNbr, qCcsLoanReg.acctNbr)
		.addFieldMapping(CcsLoanReg.P_AcctType, qCcsLoanReg.acctType)
		.addFieldMapping(CcsLoanReg.P_RegisterId, qCcsLoanReg.registerId)
		.addFieldMapping(CcsLoanReg.P_CardNbr, qCcsLoanReg.cardNbr)
		.addFieldMapping(CcsLoanReg.P_LogicCardNbr, qCcsLoanReg.logicCardNbr)
		.addFieldMapping(CcsLoanReg.P_RefNbr, qCcsLoanReg.refNbr)
		.addFieldMapping(CcsLoanReg.P_OrigAuthCode, qCcsLoanReg.origAuthCode)
		.addFieldMapping(CcsLoanReg.P_OrigTransDate, qCcsLoanReg.origTransDate)
		.addFieldMapping(CcsLoanReg.P_OrigTxnAmt, qCcsLoanReg.origTxnAmt)
		.addFieldMapping(CcsLoanReg.P_B011Trace, qCcsLoanReg.b011Trace)
		.addFieldMapping(CcsLoanReg.P_B032AcqInst, qCcsLoanReg.b032AcqInst)
		.addFieldMapping(CcsLoanReg.P_B033FwdIns, qCcsLoanReg.b032AcqInst)
		.addFieldMapping(CcsLoanReg.P_LoanCode, qCcsLoanReg.loanCode)
		.addFieldMapping(CcsLoanReg.P_LoanFeeMethod, qCcsLoanReg.loanFeeMethod)
		.addFieldMapping(CcsLoanReg.P_LoanFinalTermFee, qCcsLoanReg.loanFinalTermFee)
		.addFieldMapping(CcsLoanReg.P_LoanFinalTermPrin, qCcsLoanReg.loanFinalTermPrin)
		.addFieldMapping(CcsLoanReg.P_LoanFixedFee, qCcsLoanReg.loanFixedFee)
		.addFieldMapping(CcsLoanReg.P_LoanFixedPmtPrin, qCcsLoanReg.loanFixedPmtPrin)
		.addFieldMapping(CcsLoanReg.P_LoanFirstTermFee, qCcsLoanReg.loanFirstTermFee)
		.addFieldMapping(CcsLoanReg.P_LoanFirstTermPrin, qCcsLoanReg.loanFirstTermPrin)
		.addFieldMapping(CcsLoanReg.P_LoanInitFee, qCcsLoanReg.loanInitFee)
		.addFieldMapping(CcsLoanReg.P_LoanInitPrin, qCcsLoanReg.loanInitPrin)
		.addFieldMapping(CcsLoanReg.P_LoanInitTerm, qCcsLoanReg.loanInitTerm)
		.addFieldMapping(CcsLoanReg.P_LoanType, qCcsLoanReg.loanType)
		.addFieldMapping(CcsLoanReg.P_RequestTime, qCcsLoanReg.requestTime)
		.addFieldMapping(CcsLoanReg.P_RegisterDate, qCcsLoanReg.registerDate)
		.build();
	}

	@Transactional
	@ResponseBody()
	@RequestMapping(value = "/setArgeeOrRefuse", method = {RequestMethod.POST})
	public void setArgeeOrRefuse(@RequestBody String bool,@RequestBody Map values) throws FlatException {
		Long registerId = Long.parseLong((String)values.get(CcsLoanReg.P_RegisterId));
		logger.info("===================================values:"+values.toString());
		String remark = "";
		
		CcsLoanReg ccsLoanReg = rCcsLoanReg.findOne(qCcsLoanReg.registerId.eq(registerId));
		if(ccsLoanReg == null){
			throw new FlatException("找不到分期注册信息");
		}
		//true为通过，false为拒绝
		if("0".equals(bool)){
		    
		    /*CcsOpPrivilege ccsOpPrivilege = operatorAuthUtil.getCurrentOperatorAuth();
			if(null == ccsOpPrivilege || ccsOpPrivilege.getMaxCashloanAdj() == null ||
				ccsOpPrivilege.getMaxCashloanAdj().compareTo(ccsLoanReg.getLoanInitPrin()) < 0) {
				throw new FlatException("现金分期金额已超过该操作员的操作权限范围！");
			}*/
		
			ccsLoanReg.setLoanRegStatus(LoanRegStatus.A);
			ccsLoanReg.setRemark(remark);
			
		}else if("1".equals(bool)){
		    	ccsLoanReg.setLoanRegStatus(LoanRegStatus.D);
		    	ccsLoanReg.setRemark(remark);
			//拒绝之后发送金融交易报文，恢复额度
			//查找主信用计划模板
			/*CcsCard tmCard = rCcsCard.findOne(qCcsCard.logicCardNbr.eq(ccsLoanReg.getLogicCardNbr()));
			ProductCredit productCredit = unifiedParameterService.loadParameter(tmCard.getProductCd(), ProductCredit.class);
			String mcc = "5999"; 
			
			String b003 = getTransType(productCredit);
			MsgParameter m = makeMsgParam(ccsLoanReg, mcc, b003,generateFlowNo());
			m.setB038(ccsLoanReg.getOrigAuthCode());
			m.setB090(AuthUtils.makeFeild90(AuthUtils.TPS_VAL_MTI_ADJUST, ccsLoanReg.getB011Trace(), ccsLoanReg.getB007TxnTime(), ccsLoanReg.getB032AcqInst(), ccsLoanReg.getB033FwdIns()));
			YakMessage request = AuthUtils.makeCashLoanReverseRequestMsg(m);
			
			YakMessage response = authorizationService.authorize(request);
			
			String respCode;
			if(response != null){
				respCode = response.getBody(39);
			}else{
				throw new FlatException("授权处理超时");
			}
			if(!StringUtils.equals(respCode,"00") && !StringUtils.equals(respCode, "11")){
				throw new FlatException("恢复额度失败");
			}*/
		}
		rCcsLoanReg.save(ccsLoanReg);
	}
	
	/**
	 * 根据使用额度类型获得交易类型
	 * @param tmCard 
	 * @return
	 * @throws FlatException
	 */
	private String getTransType(ProductCredit productCredit) throws FlatException {
		String transType = "010000";//取现 
		if(productCredit.cashLoanLimitType == CashLoanLimitType.L){
			transType="000000";//消费
		}
		return transType;
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

	private String generateFlowNo(){
		DateFormat df = new SimpleDateFormat("yyDS");
		Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		int second = c.get(Calendar.SECOND);
		StringBuffer sb = new StringBuffer(df.format(c.getTime()));
		sb.append(hour*60*60+minute*60+second);
		return sb.substring(1);
	}
}
