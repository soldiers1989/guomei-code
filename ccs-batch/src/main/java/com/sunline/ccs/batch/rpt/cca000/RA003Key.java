package com.sunline.ccs.batch.rpt.cca000;

import java.io.Serializable;

public class RA003Key implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Long id;
	private Boolean isRecovery;
	private Boolean isOrderHst;
	

	public RA003Key(Long id, Boolean isRecovery, Boolean isOrderHst) {
		super();
		this.id = id;
		this.isRecovery = isRecovery;
		this.isOrderHst = isOrderHst;
	}
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
	public Boolean getIsRecovery() {
		return isRecovery;
	}

	public void setIsRecovery(Boolean isRecovery) {
		this.isRecovery = isRecovery;
	}

}
