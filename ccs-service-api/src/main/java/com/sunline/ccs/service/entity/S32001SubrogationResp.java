package com.sunline.ccs.service.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 代位追偿接口返回报文
 * @author jjb
 *
 */
public class S32001SubrogationResp extends SunshineResponseInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/**
	 * 保单号
	 */
	@JsonProperty(value="GUARANTYID")
	private String guarantyid;
	/**
	 * 代扣到的金额
	 */
	@JsonProperty(value="MONEY")
	private BigDecimal money;
	public String getGuarantyid() {
		return guarantyid;
	}
	public void setGuarantyid(String guarantyid) {
		this.guarantyid = guarantyid;
	}
	public BigDecimal getMoney() {
		return money;
	}
	public void setMoney(BigDecimal money) {
		this.money = money;
	}
	
}
