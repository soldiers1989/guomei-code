package com.sunline.ccs.service.handler.query;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.enums.RequiredFlag;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ccs.service.msentity.TNQPLoanPlanAPPLeDataEMaterial;
import com.sunline.ccs.service.msentity.TNQPLoanPlanAPPLeDataReq;
import com.sunline.ccs.service.msentity.TNQPLoanPlanAPPLeDataResp;
import com.sunline.pcm.param.def.ElectronicTemplet;
import com.sunline.pcm.param.def.enums.ElectronicTempletType;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 查询产品需上传电子资料清单
 * @author ymk
 *
 */
@Service
public class TNQPLoanPlanAppleDataEMaterialQuery {
	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	@Autowired
	UnifiedParameterFacility unifiedParameterFacility;
	@Autowired
	AppapiCommService appapiCommService;
	TxnInfo txnInfo ;
	public TNQPLoanPlanAPPLeDataResp handler(TNQPLoanPlanAPPLeDataReq req) throws ProcessException {
		
		TNQPLoanPlanAPPLeDataResp resp = new TNQPLoanPlanAPPLeDataResp();
		txnInfo = new TxnInfo();
//		TNQPLoanPlanAPPLeDataEMaterial eMaterial = new TNQPLoanPlanAPPLeDataEMaterial();
		LoanFeeDef loanFeeDef;
		ElectronicTemplet electronicTemplet;
		
		try{
			try{
				
				loanFeeDef = unifiedParamFacilityProvide.loanFeeDef(req.getLoanCode(), Integer.parseInt(req.getLoanTerm()), req.getLoanAmt());
				electronicTemplet = unifiedParameterFacility.loadParameter(loanFeeDef.riskTableId,ElectronicTemplet.class);
			}
			catch(Exception pe){
				throw new ProcessException(MsRespCode.E_1057.getCode(),MsRespCode.E_1057.getMessage());
			}
			
			List<TNQPLoanPlanAPPLeDataEMaterial> eMaterialList = new ArrayList<TNQPLoanPlanAPPLeDataEMaterial>();
			
			for(String eleType:electronicTemplet.templetList.keySet()){
				
				TNQPLoanPlanAPPLeDataEMaterial eMaterial = new TNQPLoanPlanAPPLeDataEMaterial();
				
				eMaterial.seteMaterialType(
						ElectronicTempletType.valueOf(eleType));
				eMaterial.setRequiredFlag(
						RequiredFlag.valueOf(electronicTemplet.templetList.get(eleType)));
				eMaterialList.add(eMaterial);
			}
			
			resp.seteMaterialList(eMaterialList);
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
			LogTools.printLogger(log, "TNQPLoanPlanAppleDataEMaterialQuery", "查询产品需上传电子资料清单", resp, false);
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

