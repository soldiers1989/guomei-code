package com.sunline.ccs.service.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 实时代扣查询接口接受报文
 * @author zqx
 *
 */
public class S39003SettleQueryReq extends SunshineRequestInfo implements Serializable{
	private static final long serialVersionUID = 1L;
	
	/**
	 * 订单号
	 */
	@Check(lengths=16,notEmpty=true)
	@JsonProperty(value="ORDERID")
	private Long orderid;

	public Long getOrderid() {
		return orderid;
	}

	public void setOrderid(Long orderid) {
		this.orderid = orderid;
	}

}
