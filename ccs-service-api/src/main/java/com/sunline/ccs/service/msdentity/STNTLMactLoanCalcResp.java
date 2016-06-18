package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.service.msentity.MsResponseInfo;

/**
 * 计算随借随还详情接口返回报文
 * @author zqx
 *
 */
public class STNTLMactLoanCalcResp extends MsResponseInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/*
	*额度有效期开始日期（年月日）
	*/
	@JsonProperty(value="CONTRA_BEGIN_DATE")
	public String  contraBeginDate; 

	/*
	*额度有效期结束日期（年月日）
	*/
	@JsonProperty(value="ACCT_EXPIRE_DATE")
	public String  acctExpireDate; 

	/*
	*账单日（每月X日）
	*/
	@JsonProperty(value="BILL_CYCLE_DAY")
	public String  billCycleDay; 

	/*
	*最后还款日（每月X日）
	*/
	@JsonProperty(value="PMT_DUE_DAY")
	public String  pmtDueDay; 

	/*
	*日罚息率
	*/
	@JsonProperty(value="DAILY_PENALTY_RATE")
	public BigDecimal  dailyPenaltyRate;

	public String getContraBeginDate() {
		return contraBeginDate;
	}

	public void setContraBeginDate(String contraBeginDate) {
		this.contraBeginDate = contraBeginDate;
	}

	public String getAcctExpireDate() {
		return acctExpireDate;
	}

	public void setAcctExpireDate(String acctExpireDate) {
		this.acctExpireDate = acctExpireDate;
	}

	public String getBillCycleDay() {
		return billCycleDay;
	}

	public void setBillCycleDay(String billCycleDay) {
		this.billCycleDay = billCycleDay;
	}

	public String getPmtDueDay() {
		return pmtDueDay;
	}

	public void setPmtDueDay(String pmtDueDay) {
		this.pmtDueDay = pmtDueDay;
	}

	public BigDecimal getDailyPenaltyRate() {
		return dailyPenaltyRate;
	}

	public void setDailyPenaltyRate(BigDecimal dailyPenaltyRate) {
		this.dailyPenaltyRate = dailyPenaltyRate;
	} 
	
}
