package com.sunline.ccs.service.msentity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.param.def.enums.RequiredFlag;
import com.sunline.pcm.param.def.enums.ElectronicTempletType;
/**
 * 电子资料信息
 * @author yuemk
 *
 */
public class TNQPLoanPlanAPPLeDataEMaterial implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 电子资料类型
	 */
	@JsonProperty(value="E_MATERIAL_TYPE")
	public	ElectronicTempletType eMaterialType;
	/**
	 * 填写标志
	 */
	@JsonProperty(value="REQUIRED_FLAG")
	public  RequiredFlag requiredFlag;
	
	public ElectronicTempletType geteMaterialType() {
		return eMaterialType;
	}
	public void seteMaterialType(ElectronicTempletType eMaterialType) {
		this.eMaterialType = eMaterialType;
	}
	public RequiredFlag getRequiredFlag() {
		return requiredFlag;
	}
	public void setRequiredFlag(RequiredFlag requiredFlag) {
		this.requiredFlag = requiredFlag;
	}
	
	
}
