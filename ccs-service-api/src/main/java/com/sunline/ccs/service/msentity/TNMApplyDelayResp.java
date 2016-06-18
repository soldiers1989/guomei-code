package com.sunline.ccs.service.msentity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;


/**
 * 变更换款日 响应体
 * @author zhengjf
 *
 */
public class TNMApplyDelayResp extends MsResponseInfo implements Serializable {

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
	
	/**
	*下一到期还款日期
	*/
	@JsonProperty(value="NEXT_PMT_DUE_DATE")
	public String  nextPmtDueDate; 
	
	
	public String getNextPmtDueDate() {
		return nextPmtDueDate;
	}

	public void setNextPmtDueDate(String nextPmtDueDate) {
		this.nextPmtDueDate = nextPmtDueDate;
	}


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
	
