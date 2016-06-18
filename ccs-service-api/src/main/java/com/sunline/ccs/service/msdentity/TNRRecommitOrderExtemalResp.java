package com.sunline.ccs.service.msdentity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ppy.dictionary.entity.Check;
import com.sunline.ppy.dictionary.enums.Indicator;
@SuppressWarnings("serial")
public class TNRRecommitOrderExtemalResp extends MsResponseInfo implements Serializable {
	
	/**
	 * 合同号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="CONTR_NBR")
	private String contractNo;
	
	/**
	 * 借据号
	 * 
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="DUE_BILL_NO")
	private String dueBillNo;
	
	/**
	 * 是否已成功开户
	 */
	@JsonProperty(value="ACCT_SETUP_IND")
	public Indicator  acctSetupInd;

	public String getContractNo() {
		return contractNo;
	}

	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}

	public String getDueBillNo() {
		return dueBillNo;
	}

	public void setDueBillNo(String dueBillNo) {
		this.dueBillNo = dueBillNo;
	}

	public Indicator getAcctSetupInd() {
		return acctSetupInd;
	}

	public void setAcctSetupInd(Indicator acctSetupInd) {
		this.acctSetupInd = acctSetupInd;
	}
	
	
	
	

}
