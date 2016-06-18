package com.sunline.ccs.service.handler.query;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.handler.QueryCommService;
import com.sunline.ccs.service.msdentity.STNQPLPAmtRangeReq;
import com.sunline.ccs.service.msdentity.STNQPLPAmtRangeResp;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

/*
 * 获取产品期数、金额范围接口
 */
@Service
public class TNQPLPAmtRange {
	private Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    AppapiCommService appapiCommService;
    @Autowired
    QueryCommService queryCommService;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	
	TxnInfo txnInfo;
	public STNQPLPAmtRangeResp handler(STNQPLPAmtRangeReq req) throws ProcessException {
		LogTools.printLogger(log, "STNQPLPAmtRangeReq", "获取产品期数、金额范围接口", req, true);
		LogTools.printObj(log, req, "请求参数STNQPLPAmtRangeReq");
		txnInfo = new TxnInfo();
		STNQPLPAmtRangeResp resp = new STNQPLPAmtRangeResp();
		try{
			resp.setLoanCode(req.getLoanCode());
			resp.setProdTermList(queryCommService.getLoanFeeDefAmtRangeSetByLoanCd(req.getLoanCode()));
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
			LogTools.printLogger(log, "STNQAAcctsbyCustUUIDResp", "合同列表查询", resp, false);
		}
		setResponse(resp,  txnInfo);
		
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

