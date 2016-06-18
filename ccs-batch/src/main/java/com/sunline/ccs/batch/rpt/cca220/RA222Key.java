package com.sunline.ccs.batch.rpt.cca220;

import java.io.Serializable;

public class RA222Key implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String contrNbr;
	
	public RA222Key(String contrNbr, Boolean isHst) {
		super();
		this.contrNbr = contrNbr;
	}
	
	public String getContrNbr() {
		return contrNbr;
	}
	public void setContrNbr(String contrNbr) {
		this.contrNbr = contrNbr;
	}
}
