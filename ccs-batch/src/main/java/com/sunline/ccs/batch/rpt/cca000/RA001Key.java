package com.sunline.ccs.batch.rpt.cca000;

import java.io.Serializable;

public class RA001Key implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Long id;
	private Boolean isLoanEstablished;
	
	public RA001Key(Long id, Boolean isLoanEstablished) {
		super();
		this.id = id;
		this.isLoanEstablished = isLoanEstablished;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Boolean getIsLoanEstablished() {
		return isLoanEstablished;
	}
	public void setIsLoanEstablished(Boolean isLoanEstablished) {
		this.isLoanEstablished = isLoanEstablished;
	}
	
}
