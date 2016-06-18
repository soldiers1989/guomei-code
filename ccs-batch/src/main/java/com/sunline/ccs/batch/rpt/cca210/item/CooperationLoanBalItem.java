package com.sunline.ccs.batch.rpt.cca210.item;

import java.io.Serializable;
import java.math.BigDecimal;

import com.sunline.ark.support.cstruct.CChar;

public class CooperationLoanBalItem implements Serializable {
	private static final long serialVersionUID = -5228918794852286449L;
	
	@CChar(value = 32, order = 100)
	public String cooperationId;
	
	@CChar( value = 20, order = 200)
	public BigDecimal bal; 

}
