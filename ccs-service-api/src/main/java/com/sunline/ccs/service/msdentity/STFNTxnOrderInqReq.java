package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.service.msentity.MsRequestInfo;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 交易结果查询
 * @author zhengjf
 */
public class STFNTxnOrderInqReq extends MsRequestInfo implements Serializable  {

	public static final long serialVersionUID = 1L;
	
	/**
	 * 合同号
	 */
	@Check(lengths=32)
	@JsonProperty(value="CONTR_NBR")
	public String  contrNbr;
	
	/**
	 * 原交易服务码
	 */
	@Check(lengths=32)
	@JsonProperty(value="ORIG_SERVICE_ID")
	public String  origServiceId;
	
	/**
	 * 原交易流水号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="ORIG_SERVICESN")
	public String  origServiceSn;
	
	/**
	 * 原交易收单机构编号
	 */
	@Check(lengths=8,notEmpty=true)
	@JsonProperty(value="ORIG_ACQ_ID")
	public String  origAcqId;

	public String getContrNbr() {
		return contrNbr;
	}

	public void setContrNbr(String contrNbr) {
		this.contrNbr = contrNbr;
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
