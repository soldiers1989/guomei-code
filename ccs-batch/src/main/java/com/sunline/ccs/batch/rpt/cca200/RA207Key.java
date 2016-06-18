package com.sunline.ccs.batch.rpt.cca200;

import java.io.Serializable;

public class RA207Key implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private Long orderId;
	private Boolean isHstOrder;
	public Long getOrderId() {
		return orderId;
	}
	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}
	public Boolean getIsHstOrder() {
		return isHstOrder;
	}
	public void setIsHstOrder(Boolean isHstOrder) {
		this.isHstOrder = isHstOrder;
	}
	
}
