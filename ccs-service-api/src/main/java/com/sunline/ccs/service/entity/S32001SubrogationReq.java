package com.sunline.ccs.service.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 代位追偿接口接收报文
 * @author jjb
 *
 */
public class S32001SubrogationReq extends SunshineRequestInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 保单号
	 */
	
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="GUARANTYID")
	private String guarantyid;
	/**
	 * 代扣金额
	 */
	@Check(lengths=18,notEmpty=true)
	@JsonProperty(value="BUSINESSSUM")
	private BigDecimal businesssum;
	
	public String getGuarantyid() {
		return guarantyid;
	}
	public void setGuarantyid(String guarantyid) {
		this.guarantyid = guarantyid;
	}
	public BigDecimal getBusinesssum() {
		return businesssum;
	}
	public void setBusinesssum(BigDecimal businesssum) {
		this.businesssum = businesssum;
	}

}
