package com.sunline.ccs.service.msentity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;
/**
 * 去哪开户
 * @author  Mr.L
 *
 */
public class TFNTrustLoanSetupResp extends MsResponseInfo implements Serializable{

	private static final long serialVersionUID = 1L;
	
	/**
	 * 合同号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="CONTR_NBR")
	public String contractNo;
	/*
	*开户日期
	*/
	@JsonProperty(value="CONTR_SETUP_DATE")
	public String  contraSetupDate; 
	
	/*
	*合同到期日期
	*/
	@JsonProperty(value="CONTR_EXPIRE_DATE")
	public String  contraExpireDate; 

	public String getContraSetupDate() {
		return contraSetupDate;
	}

	public void setContraSetupDate(String contraSetupDate) {
		this.contraSetupDate = contraSetupDate;
	}

	public String getContraExpireDate() {
		return contraExpireDate;
	}

	public void setContraExpireDate(String contraExpireDate) {
		this.contraExpireDate = contraExpireDate;
	}

	public String getContractNo() {
		return contractNo;
	}

	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}
	
}
