package com.sunline.ccs.service.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 实时代扣查询接口接受报文
 * @author zqx
 *
 */
public class S39001PaySinPaymentQueryReq extends SunshineRequestInfo implements Serializable{
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
