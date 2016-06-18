package com.sunline.ccs.batch.rpt.cca250;

import java.io.Serializable;

public class RA253Key implements Serializable {
	private static final long serialVersionUID = -4001179435283707596L;
	
	private Long orderId;
	private Boolean isOrderHst;
	/**
	 * @return the orderId
	 */
	public Long getOrderId() {
		return orderId;
	}
	/**
	 * @param orderId the orderId to set
	 */
	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}
	/**
	 * @return the isOrderHst
	 */
	public Boolean getIsOrderHst() {
		return isOrderHst;
	}
	/**
	 * @param isOrderHst the isOrderHst to set
	 */
	public void setIsOrderHst(Boolean isOrderHst) {
		this.isOrderHst = isOrderHst;
	}
	
}
