package com.sunline.ccs.service.msentity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 延期还款 请求体
 * @author Mr.L
 *
 */

public class TNMApplyDelayReq extends MsRequestInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 合同号	
	 */
	@Check(lengths=22,notEmpty=true)
	@JsonProperty(value="CONTR_NBR")
	public String contrNbr;
	
	/**
	 * 延期还款期数
	 */
	@Check(lengths=2,notEmpty=true,isNumber=true)
	@JsonProperty(value="APPLY_DELAY_TERM")
	public int applyDelayTerm;

	public String getContrNbr() {
		return contrNbr;
	}

	public void setContrNbr(String contrNbr) {
		this.contrNbr = contrNbr;
	}

	public int getApplyDelayTerm() {
		return applyDelayTerm;
	}

	public void setApplyDelayTerm(int applyDelayTerm) {
		this.applyDelayTerm = applyDelayTerm;
	}

}
