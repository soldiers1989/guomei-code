package com.sunline.ccs.service.msentity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 客户银行卡信息修改接口返回报文
 * @author jjb
 *
 */
public class TNMAAcctDDcardRespSubContrNbr implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@JsonProperty(value="CONTR_NBR")
	public String contrNbr;

	public String getContrNbr() {
		return contrNbr;
	}

	public void setContrNbr(String contrNbr) {
		this.contrNbr = contrNbr;
	}
}
