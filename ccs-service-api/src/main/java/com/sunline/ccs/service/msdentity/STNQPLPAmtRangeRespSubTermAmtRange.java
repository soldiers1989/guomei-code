package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 分期定价参数 期数金额支持区间
 * 获取产品期数、金额范围响应接口类
 * @author zqx
 */

public class STNQPLPAmtRangeRespSubTermAmtRange implements Serializable {
	private static final long serialVersionUID = 1L;
	public STNQPLPAmtRangeRespSubTermAmtRange(Integer term, BigDecimal minAmount, BigDecimal maxAmount) {
		// TODO Auto-generated constructor stub
		this.term= term;
		this.maxAmount=maxAmount;
		this.minAmount=minAmount;
	}
	@JsonProperty(value="LOAN_TERM")
	public Integer term;
	@JsonProperty(value="MIN_AMT")
	public BigDecimal minAmount;
	@JsonProperty(value="MAX_AMT")
	public BigDecimal maxAmount;
}