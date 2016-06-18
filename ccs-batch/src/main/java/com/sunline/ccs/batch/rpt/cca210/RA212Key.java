package com.sunline.ccs.batch.rpt.cca210;

import java.io.Serializable;

public class RA212Key implements Serializable {
	private static final long serialVersionUID = 1L;
	public RA212Key(Long orderId, Long orderHstId) {
		this.orderId = orderId;
		this.orderHstId = orderHstId;
	}
	private Long orderId;
	private Long orderHstId;
	public Long getOrderId() {
		return orderId;
	}
	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}
	public Long getOrderHstId() {
		return orderHstId;
	}
	public void setOrderHstId(Long orderHstId) {
		this.orderHstId = orderHstId;
	}
	

}
