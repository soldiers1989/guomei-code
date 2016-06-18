package com.sunline.ccs.service.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

//付款审批响应报文
public class S30005PaymentCheckResp  extends SunshineResponseInfo implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 订单号
	 */
	@JsonProperty(value="ORDERID")
	private Long orderid;

	public Long getOrderid() {
		return orderid;
	}

	public void setOrderid(Long orderid) {
		this.orderid = orderid;
	}
	
}