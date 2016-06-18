package com.sunline.ccs.service.msentity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 查询产品需上传电子资料清单返回报文
 * @author yuemk
 *
 */
@SuppressWarnings("serial")
public class TNQPLoanPlanAPPLeDataResp extends MsResponseInfo {
	/**
	 * 电子资料清单列表
	 */
	@Check(lengths=4,notEmpty=false)
	@JsonProperty(value="E_MATERIAL_LIST")
	public List<TNQPLoanPlanAPPLeDataEMaterial> eMaterialList;

	public List<TNQPLoanPlanAPPLeDataEMaterial> geteMaterialList() {
		return eMaterialList;
	}

	public void seteMaterialList(List<TNQPLoanPlanAPPLeDataEMaterial> eMaterialList) {
		this.eMaterialList = eMaterialList;
	}

}
