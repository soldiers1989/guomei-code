package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 根据金额、期数获取产品信息接口返回报文
 * 贷款产品详情查询中罚金规则
 * @author zqx
 *
 */
public class STNQPLSPbyAmtTermRespSubMulctRule implements Serializable{
	
	private static final long serialVersionUID = 1L;

	/*
	*逾期天数
	*/
	@JsonProperty(value="DAY_PAST_DUE")
	public Integer  dayPastDue; 

	/*
	*进入催收天数
	*/
	@JsonProperty(value="COL_PAST_DUE")
	public Integer  colPastDue; 

	/*
	*罚金金额
	*/
	@JsonProperty(value="MULCT_AMT")
	public BigDecimal  mulctAmt; 

	/*
	*罚金费率
	*/
	@JsonProperty(value="MULCT_RATE")
	public BigDecimal  mulctRate;

	
	public Integer getDayPastDue() {
		return dayPastDue;
	}

	public void setDayPastDue(Integer dayPastDue) {
		this.dayPastDue = dayPastDue;
	}

	public Integer getColPastDue() {
		return colPastDue;
	}

	public void setColPastDue(Integer colPastDue) {
		this.colPastDue = colPastDue;
	}

	public BigDecimal getMulctAmt() {
		return mulctAmt;
	}

	public void setMulctAmt(BigDecimal mulctAmt) {
		this.mulctAmt = mulctAmt;
	}

	public BigDecimal getMulctRate() {
		return mulctRate;
	}

	public void setMulctRate(BigDecimal mulctRate) {
		this.mulctRate = mulctRate;
	} 


}
