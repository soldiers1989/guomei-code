package com.sunline.ccs.service.msentity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 循环现金贷提款响应接口(随借随换)
 * @author wangz
 *
 */
@SuppressWarnings("serial")
public class TFDWithDrawResp extends MsResponseInfo {
	/**
	 * 合同号
	 */
	@Check(lengths=22,notEmpty=true)
	@JsonProperty(value="CONTR_NBR")
	public String contractNo;
	
	/**
	 * 借据号
	 */
	@Check(lengths=22,notEmpty=true)
	@JsonProperty(value="DUE_BILL_NO")
	public String dueBillNo;

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
