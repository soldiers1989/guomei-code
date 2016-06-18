package com.sunline.ccs.batch.rpt.cca200;

import java.io.Serializable;

public class RA203Key implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Boolean isOrderHst;
	private Long id;
	
	
	public Boolean getIsOrderHst() {
		return isOrderHst;
	}
	public void setIsOrderHst(Boolean isOrderHst) {
		this.isOrderHst = isOrderHst;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	

}
