package com.sunline.ccs.service.handler.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;




import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.enums.LoanPlanStatus;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.handler.QueryCommService;
import com.sunline.ccs.service.msdentity.STNQPLPAllLoanPlanReq;
import com.sunline.ccs.service.msdentity.STNQPLPAllLoanPlanResp;
import com.sunline.ccs.service.msdentity.STNQPLPAllLoanPlanRespSubTermAmtRange;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;
/**
 * 所有贷款产品参数查询
 * @author zhengjf
 *
 */
@Service
public class TNQPLPAllLoanPlan {
	private Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
    QueryCommService queryCommService;
	@Autowired
	UnifiedParameterFacility unifiedParameterService;
	@Autowired
	AppapiCommService appapiCommService;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	@Autowired
    GlobalManagementService globalManagementService;
	
	TxnInfo txnInfo;
	
	public STNQPLPAllLoanPlanResp handler(STNQPLPAllLoanPlanReq req) {
		
		log.debug("所有贷款产品参数查询接口请求报文:" + req);
		STNQPLPAllLoanPlanResp resp=new STNQPLPAllLoanPlanResp();
		//创建产品的集合
		List<STNQPLPAllLoanPlanRespSubTermAmtRange> loanPlanList=new ArrayList<STNQPLPAllLoanPlanRespSubTermAmtRange>();
		txnInfo  = new TxnInfo();
		//如果传进来的loancode为空则查找全部的产品
		if (req.getLoanCode()==null) {
			Map<String, LoanPlan> productMap = unifiedParameterService.retrieveParameterObject(LoanPlan.class);
			try {
				for (String loanCode : productMap.keySet()) {
					STNQPLPAllLoanPlanRespSubTermAmtRange termAmtRange=new STNQPLPAllLoanPlanRespSubTermAmtRange();
					log.debug("产品Code:" + loanCode);
					LoanPlan loanPlan=productMap.get(loanCode);
					//判断贷款产品是否为空或者是非活动状态或者有效期小于业务日期
					if(loanPlan == null	
							|| loanPlan.loanStaus != LoanPlanStatus.A
							|| loanPlan.loanValidity.before(req.getBizDate())){
						
						continue;				
					}
					if (LoanType.MCAT != loanPlan.loanType
							&& LoanType.MCEI != loanPlan.loanType
							&& LoanType.MCEP != loanPlan.loanType) {
						continue ;
					}
					termAmtRange.setLoanCode(loanCode);
					termAmtRange.setDesc(loanPlan.description);
					termAmtRange.setLoantype(loanPlan.loanType);
					termAmtRange.setProdTermList(queryCommService.getLoanFeeDefAmtRangeSetByLoanCd(loanCode));
					loanPlanList.add(termAmtRange);
					}
					resp.setLoanPlanList(loanPlanList);

				}catch(ProcessException pe){
					if(log.isErrorEnabled())
						log.error(pe.getMessage(),pe);
						appapiCommService.preException(pe, pe, txnInfo);
					}
				catch(Exception e){
					if(log.isErrorEnabled())
						log.error(e.getMessage(),e);
						appapiCommService.preException(e, null, txnInfo);				
				}finally{
					LogTools.printLogger(log, "STNQPLPAllLoanPlanResp", "所有贷款产品参数查询结束", resp, false);
				}
		}else {
			log.debug("单个产品查询:" + req);
			try{
				STNQPLPAllLoanPlanRespSubTermAmtRange termAmtRange=new STNQPLPAllLoanPlanRespSubTermAmtRange();
				LoanPlan loanPlan;
				try{
					loanPlan = unifiedParamFacilityProvide.loanPlan(req.getLoanCode());	
					//判断贷款产品是否为空或者是非活动状态或者有效期小于业务日期
					if(loanPlan == null	
							|| loanPlan.loanStaus != LoanPlanStatus.A
							|| loanPlan.loanValidity.before(req.getBizDate())){
						
						throw new ProcessException(MsRespCode.E_1052.getCode(),MsRespCode.E_1052.getMessage());			
					}
				}
				catch(Exception e){
					throw new ProcessException(MsRespCode.E_1052.getCode(),MsRespCode.E_1052.getMessage()+",找不到贷款产品");
				}
				if (LoanType.MCAT != loanPlan.loanType
						&& LoanType.MCEI != loanPlan.loanType
						&& LoanType.MCEP != loanPlan.loanType) {
					throw new ProcessException(MsRespCode.E_1052.getCode(),"所查产品 非随借随还/等额本息/等额本金的产品");
				}
				termAmtRange.setLoanCode(req.getLoanCode());
				termAmtRange.setLoantype(loanPlan.loanType);
				termAmtRange.setDesc(loanPlan.description);
				termAmtRange.setProdTermList(queryCommService.getLoanFeeDefAmtRangeSetByLoanCd(req.getLoanCode()));
				loanPlanList.add(termAmtRange);
				resp.setLoanPlanList(loanPlanList);
			}catch(ProcessException pe){
				if(log.isErrorEnabled())
						log.error(pe.getMessage(),pe);
				appapiCommService.preException(pe, pe, txnInfo);
			}
			catch(Exception e){
				if(log.isErrorEnabled())
					log.error(e.getMessage(),e);
				appapiCommService.preException(e, null, txnInfo);				
			}finally{
				LogTools.printLogger(log, "STNQPLPAllLoanPlanResp", "所有贷款产品参数查询结束", resp, false);
			}
		}
		setResponse(resp,  txnInfo);

		return resp;
	}
	
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
