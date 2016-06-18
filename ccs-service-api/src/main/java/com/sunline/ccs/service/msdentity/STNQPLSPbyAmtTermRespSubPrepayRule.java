package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 根据金额、期数获取产品信息接口返回报文
 * 提前还款手续费计算规则列表
 * @author zqx
 *
 */
public class STNQPLSPbyAmtTermRespSubPrepayRule implements Serializable {
	private static final long serialVersionUID = 1L;

	/*
	*提前还款当前期数
	*/
	@JsonProperty(value="PREPAY_CURR_TERM")
	public Integer  prepayCurrTerm; 

	/*
	*提前还款手续费金额
	*/
	@JsonProperty(value="PREPAY_FEE_AMT")
	public BigDecimal  prepayFeeAmt; 

	/*
	*提前还款手续费费率
	*/
	@JsonProperty(value="PREPAY_FEE_RATE")
	public BigDecimal  prepayFeeRate;

	public Integer getPrepayCurrTerm() {
		return prepayCurrTerm;
	}

	public void setPrepayCurrTerm(Integer prepayCurrTerm) {
		this.prepayCurrTerm = prepayCurrTerm;
	}

	public BigDecimal getPrepayFeeAmt() {
		return prepayFeeAmt;
	}

	public void setPrepayFeeAmt(BigDecimal prepayFeeAmt) {
		this.prepayFeeAmt = prepayFeeAmt;
	}

	public BigDecimal getPrepayFeeRate() {
		return prepayFeeRate;
	}

	public void setPrepayFeeRate(BigDecimal prepayFeeRate) {
		this.prepayFeeRate = prepayFeeRate;
	} 
	
	

}
