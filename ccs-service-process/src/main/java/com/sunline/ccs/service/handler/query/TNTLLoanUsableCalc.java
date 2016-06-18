package com.sunline.ccs.service.handler.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.LoanFeeDefStatus;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.handler.RepayScheduleCalCommsSrvice;
import com.sunline.ccs.service.msdentity.STNTLLoanScheduleCalcReq;
import com.sunline.ccs.service.msdentity.STNTLLoanScheduleCalcResp;
import com.sunline.ccs.service.msdentity.STNTLLoanUsableCalcResp;
import com.sunline.ccs.service.msdentity.STNTLLoanUsableCalcRespSubLoan;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;
/**
 * 可用产品还款试算
 * @author zhengjf
 *
 */
@Service
public class TNTLLoanUsableCalc {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private UnifiedParameterFacility unifiedParameterService;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	@Autowired
	private RepayScheduleCalCommsSrvice repaymentTrial;
	@Autowired
	private AppapiCommService appapiCommService;
	
	public STNTLLoanUsableCalcResp handler(STNTLLoanScheduleCalcReq req) {
		
		LogTools.printLogger(log, "STNTLLoanScheduleCalcReq", "可用产品还款试算)", req, true);
		LogTools.printObj(log, req, "请求参数STNTLLoanScheduleCalcReq");
		
		LoanPlan loanPlan = unifiedParamFacilityProvide.loanPlan(req.getLoanCode());	
		ProductCredit productCredit = unifiedParameterFacility.loadParameter(loanPlan.productCode, ProductCredit.class);
		AccountAttribute acctAttr = unifiedParameterFacility.loadParameter(productCredit.accountAttributeId, AccountAttribute.class);
		STNTLLoanUsableCalcResp resp = new STNTLLoanUsableCalcResp();
		List<STNTLLoanUsableCalcRespSubLoan> loanList =new ArrayList<STNTLLoanUsableCalcRespSubLoan>();
		TxnInfo txnInfo = new TxnInfo();
			
		try{
			if(loanPlan.loanType.equals(LoanType.MCAT)){
				throw new ProcessException(MsRespCode.E_1052.getCode(),MsRespCode.E_1052.getMessage()+",不支持随借随还");
			}
			
			Map<Integer, LoanFeeDef> loanFeeDefMap = loanPlan.loanFeeDefMap;
			
			for (Integer key : loanFeeDefMap.keySet()) {
				LoanFeeDef loanFeeDef = loanFeeDefMap.get(key);
				if(loanFeeDef == null	
						|| loanFeeDef.loanFeeDefStatus != LoanFeeDefStatus.A
						|| !(req.loanAmt.compareTo(loanFeeDef.minAmount)>=0 && req.loanAmt.compareTo(loanFeeDef.maxAmount)<=0)){
					
					continue;				
				}
				req.setLoanTerm(key);
				STNTLLoanUsableCalcRespSubLoan subLoan = new STNTLLoanUsableCalcRespSubLoan();
				STNTLLoanScheduleCalcResp scheduleResp = repaymentTrial.Trial(productCredit, acctAttr, loanPlan, loanFeeDef, req);
				setSubLoan(subLoan,scheduleResp);
				loanList.add(subLoan);
			}
			
			resp.setLoanList(loanList);
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
			LogTools.printLogger(log, "STNTLLoanUsableCalcResp", "可用产品还款计划试算", resp, false);
		}
		
		setResponse(resp, txnInfo);
		
		return resp;
	}
	/**
	 * 对loan层赋值
	 */
	private void setSubLoan(STNTLLoanUsableCalcRespSubLoan subLoan,STNTLLoanScheduleCalcResp scheduleResp) {
		subLoan.setLoanFeeSum(scheduleResp.loanFeeSum);
		subLoan.setLoanInitPrin(scheduleResp.loanInitPrin);
		subLoan.setLoanIntSum(scheduleResp.getLoanIntSum());
		subLoan.setLoanTerm(scheduleResp.getLoanTerm());
		subLoan.setSubScheduleList(scheduleResp.getSubScheduleList());
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
