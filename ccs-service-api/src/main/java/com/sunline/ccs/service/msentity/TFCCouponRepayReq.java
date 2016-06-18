package com.sunline.ccs.service.msentity;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.param.def.enums.OffsetType;
import com.sunline.ppy.dictionary.entity.Check;

public class TFCCouponRepayReq extends MsRequestInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 合同号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="CONTR_NBR")
	public String contrNbr;
	
	/**
	 * 优惠券面值金额
	 */
	@Check(lengths=18,notEmpty=true,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
	@JsonProperty(value="AMOUNT")
	public BigDecimal amount;
	
	/**
	 * 优惠券编码
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="OFFSET_NO")
	public String offsetNO;
	
	/**
	 * 优惠券使用期数
	 */
	@Check(lengths=2,notEmpty=false)
	@JsonProperty(value="OFFSET_TERM")
	public int offsetTerm;
	
	/**
	 * 优惠券类型
	 */
	@Check(lengths=1,notEmpty=true)
	@JsonProperty(value="OFFSET_TYPE")
	public OffsetType offsetType;

	public String getContrNbr() {
		return contrNbr;
	}

	public void setContrNbr(String contrNbr) {
		this.contrNbr = contrNbr;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getOffsetNO() {
		return offsetNO;
	}

	public void setOffsetNO(String offsetNO) {
		this.offsetNO = offsetNO;
	}

	public int getOffsetTerm() {
		return offsetTerm;
	}

	public void setOffsetTerm(int offsetTerm) {
		this.offsetTerm = offsetTerm;
	}

	public OffsetType getOffsetType() {
		return offsetType;
	}

	public void setOffsetType(OffsetType offsetType) {
		this.offsetType = offsetType;
	}
	
}
