package com.sunline.ccs.batch.rpt.cca230;

import java.io.Serializable;

public class RA232YZFKey implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 订单id
	 */
	private long orderId;
	/**
	 * 数据类型（O代表是订单表的数据，H代表订单历史表数据）
	 */
	private String type;
	public long getOrderId() {
		return orderId;
	}
	public void setOrderId(long orderId) {
		this.orderId = orderId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
}
