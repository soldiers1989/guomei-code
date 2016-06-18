package com.sunline.ccs.service.msentity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;
import com.sunline.ppy.dictionary.enums.Indicator;

/**
 * 非循环现金贷开户放款响应接口
 * @author wangz
 *
 */
@SuppressWarnings("serial")
public class TNRAAcctSetupWithDrawResp extends MsResponseInfo {
	/**
	 * 合同号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="CONTR_NBR")
	public String contractNo;
	
	/**
	 * 借据号
	 */
	@Check(lengths=22,notEmpty=true)
	@JsonProperty(value="DUE_BILL_NO")
	public String dueBillNo;

	/**
	 * 是否已经成功开户
	 */
	@Check(lengths=1,notEmpty=true)
	@JsonProperty(value="ACCT_SETUP_IND")
	public Indicator acctSetupInd;
	
	
	public Indicator getAcctSetupInd() {
		return acctSetupInd;
	}

	public void setAcctSetupInd(Indicator acctSetupInd) {
		this.acctSetupInd = acctSetupInd;
	}

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
	
}
