package com.sunline.ccs.service.msentity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 商品贷退货接口请求报文
 * @author wangz
 *
 */
@SuppressWarnings("serial")
public class TFCRefundResp extends MsResponseInfo {
	/**
	 * 合同号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="CONTR_NBR")
	public String contractNo;
	
	/**
	 * 借据号
	 */
	@Check(lengths=32,notEmpty=false)
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
