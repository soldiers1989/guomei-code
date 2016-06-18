package com.sunline.ccs.batch.rpt.cca200;

import java.io.Serializable;
import java.math.BigDecimal;

public class RA204Key implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private BigDecimal insAmt = BigDecimal.ZERO.setScale(2);
	private BigDecimal fineAmt = BigDecimal.ZERO.setScale(2);
	private String contrNbr;
	
	public BigDecimal getInsAmt() {
		return insAmt;
	}
	public void setInsAmt(BigDecimal insAmt) {
		this.insAmt = insAmt;
	}
	public BigDecimal getFineAmt() {
		return fineAmt;
	}
	public void setFineAmt(BigDecimal fineAmt) {
		this.fineAmt = fineAmt;
	}
	public String getContrNbr() {
		return contrNbr;
	}
	public void setContrNbr(String contrNbr) {
		this.contrNbr = contrNbr;
	}

	
}
