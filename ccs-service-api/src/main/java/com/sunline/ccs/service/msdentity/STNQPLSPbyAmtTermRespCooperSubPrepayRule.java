package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 根据金额、期数获取产品信息接口返回报文
 * 合作方-----提前还款手续费计算规则列表
 * @author Mr.L
 *
 */

public class STNQPLSPbyAmtTermRespCooperSubPrepayRule  implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/*
	*合作方提前还款当前期数
	*/
	@JsonProperty(value="COOPER_PREPAY_CURR_TERM")
	public Integer  cooperPrepayCurrTerm; 

	/*
	*合作方提前还款手续费金额
	*/
	@JsonProperty(value="COOPER_PREPAY_FEE_AMT")
	public BigDecimal  cooperPrepayFeeAmt; 

	/*
	*合作方提前还款手续费费率
	*/
	@JsonProperty(value="COOPER_PREPAY_FEE_RATE")
	public BigDecimal  cooperPrepayFeeRate;
	


	public Integer getCooperPrepayCurrTerm() {
		return cooperPrepayCurrTerm;
	}

	public void setCooperPrepayCurrTerm(Integer cooperPrepayCurrTerm) {
		this.cooperPrepayCurrTerm = cooperPrepayCurrTerm;
	}

	public BigDecimal getCooperPrepayFeeAmt() {
		return cooperPrepayFeeAmt;
	}

	public void setCooperPrepayFeeAmt(BigDecimal cooperPrepayFeeAmt) {
		this.cooperPrepayFeeAmt = cooperPrepayFeeAmt;
	}

	public BigDecimal getCooperPrepayFeeRate() {
		return cooperPrepayFeeRate;
	}

	public void setCooperPrepayFeeRate(BigDecimal cooperPrepayFeeRate) {
		this.cooperPrepayFeeRate = cooperPrepayFeeRate;
	}

}
