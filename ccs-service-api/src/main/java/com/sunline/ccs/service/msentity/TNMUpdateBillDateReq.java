package com.sunline.ccs.service.msentity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 变更换款日 请求体
 * @author zhengjf
 *
 */
public class TNMUpdateBillDateReq extends MsRequestInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 合同号	VARCHAR2(32)	Y
	 */
	@Check(lengths=22,notEmpty=true)
	@JsonProperty(value="CONTR_NBR")
	public String contrNbr;
	
	/**
	 * 变更后还款日
	 */
	@Check(lengths=2,notEmpty=true,isNumber=true,fixed=true)
	@JsonProperty(value="PAY_STMT_DATE")
	public String payStmtDate;

	public String getContrNbr() {
		return contrNbr;
	}

	public void setContrNbr(String contrNbr) {
		this.contrNbr = contrNbr;
	}

	public String getPayStmtDate() {
		return payStmtDate;
	}

	public void setPayStmtDate(String payStmtDate) {
		this.payStmtDate = payStmtDate;
	}
	
}
