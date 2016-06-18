package com.sunline.ccs.service.msentity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 商品贷退货接口响应报文
 * @author wangz
 *
 */
@SuppressWarnings("serial")
public class TFCRefundReq extends MsRequestInfo {
	
	/**
	 * 合同号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="CONTR_NBR")
	public String contractNo;
	
	/**
	 * 退款金额
	 */
	@Check(lengths=18,notEmpty=true,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
	@JsonProperty(value="AMOUNT")
	public BigDecimal amount;
	
	/**
	 * 原交易服务码
	 */
	@Check(lengths=32)
	@JsonProperty(value="ORIG_SERVICE_ID")
	public String origServiceId;
	
	/**
	 * 原交易流水号
	 * 对应原交易报文请求公共部分的交易流水号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="ORIG_SERVICESN")
	public String origServiceSn;
	
	/**
	 * 原交易所属方id
	 * 对应原交易报文交易所属方id
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="ORIG_ACQ_ID")
	public String origAcqId;

	public String getContractNo() {
		return contractNo;
	}

	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getOrigServiceId() {
		return origServiceId;
	}

	public void setOrigServiceId(String origServiceId) {
		this.origServiceId = origServiceId;
	}

	public String getOrigServiceSn() {
		return origServiceSn;
	}

	public void setOrigServiceSn(String origServiceSn) {
		this.origServiceSn = origServiceSn;
	}

	public String getOrigAcqId() {
		return origAcqId;
	}

	public void setOrigAcqId(String origAcqId) {
		this.origAcqId = origAcqId;
	}

	
}
