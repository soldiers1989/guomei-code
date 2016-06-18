package com.sunline.ccs.service.handler.query;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.loan.McLoanProvideImpl;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.LoanFeeDefStatus;
import com.sunline.ccs.param.def.enums.LoanPlanStatus;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.handler.OpenAcctCommService;
import com.sunline.ccs.service.handler.QueryCommService;
import com.sunline.ccs.service.handler.RepayScheduleCalCommsSrvice;
import com.sunline.ccs.service.msdentity.STNTLLoanScheduleCalcReq;
import com.sunline.ccs.service.msdentity.STNTLLoanScheduleCalcResp;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;


/*
 * 计算产品还款详情
 * 还款计划试算(等额本息)
 * 
 */
@Service
public class TNTLLoanScheduleCalc {
	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	@Autowired
	private QueryCommService queryCommService;
	@Autowired
	private OpenAcctCommService openAcctCommService;
	@Autowired
	McLoanProvideImpl  loanProvideImpl;
	@Autowired
	McLoanProvideImpl mcLoanProvideImpl;
	@Autowired
	MicroCreditRescheduleUtils microCreditRescheduleUtils;
	@Value("#{env.respDateFormat}")
	private String respDateFormat;
	@Autowired
	private GlobalManagementService globalManageService;
	@Autowired
	AppapiCommService appapiCommService;
	@Autowired
	GlobalManagementService globalManagementService;
	@Autowired
	private RepayScheduleCalCommsSrvice repaymentTrial;
	
	public STNTLLoanScheduleCalcResp handler(STNTLLoanScheduleCalcReq req) throws ProcessException {
		LogTools.printLogger(log, "STNTLLoanScheduleCalcReq", "还款计划试算", req, true);
		LogTools.printObj(log, req, "请求参数STNTLLoanScheduleCalcReq");
		
		STNTLLoanScheduleCalcResp resp = new STNTLLoanScheduleCalcResp();
		TxnInfo txnInfo = new TxnInfo();
		try{
			if (req.getLoanTerm()==null) {
				throw new ProcessException(MsRespCode.E_1008.getCode(),"期数不能为空");
			}
			LoanPlan   loanPlan;
			LoanFeeDef loanFeeDef;
			ProductCredit productCredit;
			AccountAttribute acctAttr;
			try{
				loanPlan = unifiedParamFacilityProvide.loanPlan(req.getLoanCode());
				productCredit = unifiedParameterFacility.loadParameter(loanPlan.productCode, ProductCredit.class);
				acctAttr = unifiedParameterFacility.loadParameter(productCredit.accountAttributeId, AccountAttribute.class);
				loanFeeDef = unifiedParamFacilityProvide.loanFeeDef(req.getLoanCode(), req.getLoanTerm(), req.getLoanAmt());
				//货款产品有效期是否小于业务日期
				if(loanPlan.loanValidity.before(req.getBizDate())){
					throw new ProcessException(MsRespCode.E_1038.getCode(), MsRespCode.E_1038.getMessage());
				}
				//贷款产品活动状态是否为非使用中
				if(loanPlan==null || loanPlan.loanStaus != LoanPlanStatus.A){
					throw new ProcessException(MsRespCode.E_1039.getCode(),MsRespCode.E_1039.getMessage());
				}
				//loanFeeDef是否为空或者为非使用中
				if(loanFeeDef==null || LoanFeeDefStatus.A != loanFeeDef.loanFeeDefStatus){
					throw new ProcessException(MsRespCode.E_1068.getCode(),MsRespCode.E_1068.getMessage());
				}
			}
			catch(Exception e){
				throw new ProcessException(MsRespCode.E_1057.getCode(),MsRespCode.E_1057.getMessage());				
			}
			
			if(loanPlan.loanType.equals(LoanType.MCAT)){
				throw new ProcessException(MsRespCode.E_1052.getCode(),MsRespCode.E_1052.getMessage()+",不支持随借随还");
			}
			resp = repaymentTrial.Trial(productCredit, acctAttr, loanPlan, loanFeeDef, req);
		}
		catch(ProcessException pe){
			if(log.isErrorEnabled())
					log.error(pe.getMessage(),pe);
			appapiCommService.preException(pe, pe, txnInfo);
		}
		catch(Exception e){
			if(log.isErrorEnabled())
				log.error(e.getMessage(),e);
			appapiCommService.preException(e, null, txnInfo);				
		}finally{
			LogTools.printLogger(log, "TNTLLoanScheduleCalc", "还款计划试算", resp, false);
		}
		
		setResponse(resp, txnInfo);
		
		return resp;
	}



	/**
	 * 组装响应报文
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(MsResponseInfo resp, TxnInfo txnInfo) {
		
		resp.setErrorCode(txnInfo.getResponsCode());
		resp.setErrorMessage(txnInfo.getResponsDesc());
		if(StringUtils.equals(MsRespCode.E_0000.getCode(), txnInfo.getResponsCode())){
			resp.setStatus("S");//交易成功
		}else{
			resp.setStatus("F");//交易失败
		}
	}
}

