package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * 根据金额、期数获取产品信息接口返回报文
 * 贷款产品详情查询中-------合作方罚金规则
 * @author Mr.L
 *
 */

public class STNQPLSPbyAmtTermRespCooperMulctRule implements  Serializable  {
	
	private static final long serialVersionUID = 1L;
	
	/*
	*合作方罚金——逾期天数
	*/
	@JsonProperty(value="COOPER_DAY_PAST_DUE")
	public Integer  cooperDayPastDue; 

	/*
	*合作方罚金——进入催收天数
	*/
	@JsonProperty(value="COOPER_COL_PAST_DUE")
	public Integer  cooperColPastDue; 

	/*
	*合作方罚金——罚金金额
	*/
	@JsonProperty(value="COOPER_MULCT_AMT")
	public BigDecimal  cooperMulctAmt; 

	/*
	*合作方罚金——罚金费率
	*/
	@JsonProperty(value="COOPER_MULCT_RATE")
	public BigDecimal  cooperMulctRate;

	public Integer getCooperDayPastDue() {
		return cooperDayPastDue;
	}

	public void setCooperDayPastDue(Integer cooperDayPastDue) {
		this.cooperDayPastDue = cooperDayPastDue;
	}

	public Integer getCooperColPastDue() {
		return cooperColPastDue;
	}

	public void setCooperColPastDue(Integer cooperColPastDue) {
		this.cooperColPastDue = cooperColPastDue;
	}

	public BigDecimal getCooperMulctAmt() {
		return cooperMulctAmt;
	}

	public void setCooperMulctAmt(BigDecimal cooperMulctAmt) {
		this.cooperMulctAmt = cooperMulctAmt;
	}

	public BigDecimal getCooperMulctRate() {
		return cooperMulctRate;
	}

	public void setCooperMulctRate(BigDecimal cooperMulctRate) {
		this.cooperMulctRate = cooperMulctRate;
	}
	
	
}
