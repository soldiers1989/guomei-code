package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.param.def.enums.ContraBizSituation;
import com.sunline.ccs.param.def.enums.ContraStatus;
import com.sunline.ccs.param.def.enums.PaymentIntervalUnit;
import com.sunline.ccs.service.msentity.MsResponseInfo;

/**
 * 客户合同详情查询接口返回报文
 * @author zqx
 *
 */
public class STNQLLoanPMTScheByContrIDResp extends MsResponseInfo implements Serializable {
	public static final long serialVersionUID = 1L;
	/*
	*合同号
	*/
	@JsonProperty(value="CONTR_NBR")
	public String  contrNbr; 
	/*
	 * 客户uuid
	 */
	@JsonProperty(value="CUST_UUID")
	public String  custUuid;
	/*
	*默认贷款产品代码
	*/
	@JsonProperty(value="DEFAULT_LOAN_CODE")
	public String  defaultLoanCode; 

	/*
	*合同额度
	*/
	@JsonProperty(value="CONTR_LMT")
	public BigDecimal  contrLmt; 

	/*
	*合同状态
	*/
	@JsonProperty(value="CONTRA_STATUS")
	public ContraStatus contraStatus; 


	/*
	*合同到期日期
	*/
	@JsonProperty(value="CONTRA_EXPIRE_DATE")
	public String  contraExpireDate; 

	/*
	*协议利率
	*/
	@JsonProperty(value="CONTR_AGREE_RATE")
	public BigDecimal  contrAgreeRate; 

	/*
	*协议利率有效期
	*/
	@JsonProperty(value="AGREE_RATE_EXPIRE_DATE")
	public String  agreeRateExpireDate; 

	/*
	*欠款总额
	*/
	@JsonProperty(value="CONTRA_BAL")
	public BigDecimal  contraBal; 

	/*
	*欠款本金
	*/
	@JsonProperty(value="CONTRA_BAL_PRIN")
	public BigDecimal  contraBalPrin; 
	
	/*
	 *合同全部应还款额
	*/
	@JsonProperty(value="QUAL_GRACE_BAL")
	public BigDecimal  qualGraceBal; 
	/*
	*当期应还欠款
	*/
	@JsonProperty(value="CURR_DUE_AMT")
	public BigDecimal  currDueAmt; 	
	/*
	*往期应还款额
	*/
	@JsonProperty(value="PAST_DUE_AMT")
	public BigDecimal  pastDueAmt;
	/*
	*在途放款金额
	*/
	@JsonProperty(value="CONTRA_MEMO_DB")
	public BigDecimal  contraMemoDb; 	
	
	/*
	*未入账还款金额
	*/
	@JsonProperty(value="CONTRA_MEMO_CR")
	public BigDecimal  contraMemoCr; 

	/*
	*可用额度
	*/
	@JsonProperty(value="CONTRA_REMAIN")
	public BigDecimal  contraRemain; 

	/*
	*开户日期
	*/
	@JsonProperty(value="CONTRA_SETUP_DATE")
	public String  contraSetupDate; 

	
	/*
	*下一账单日
	*/
	@JsonProperty(value="NEXT_STMT_DATE")
	public String  nextStmtDate; 
	/*
	*上一账单日
	*/
	@JsonProperty(value="LAST_STMT_DATE")
	public String  lastStmtDate; 
	/*
	*当天业务日期
	*/
	@JsonProperty(value="BIZ_DATE")
	public String  bizDate; 
	/*
	*当期到期还款日期
	*/
	@JsonProperty(value="PMT_DUE_DATE")
	public String  pmtDueDate; 
	/*
	*下一到期还款日期
	*/
	@JsonProperty(value="NEXT_PMT_DUE_DATE")
	public String  nextPmtDueDate; 
	/*
	*下一约定扣款日
	*/
	@JsonProperty(value="DD_DATE")
	public String  ddDate; 

	/*
	*约定还款账号
	*/
	@JsonProperty(value="DD_BANK_ACCT_NBR")
	public String  ddBankAcctNbr; 

	/*
	*约定还款账户姓名
	*/
	@JsonProperty(value="DD_BANK_ACCT_NAME")
	public String  ddBankAcctName; 

	/*
	*约定还款开户行号
	*/
	@JsonProperty(value="DD_BANK_BRANCH")
	public String  ddBankBranch; 

	/*
	*约定还款银行名称
	*/
	@JsonProperty(value="DD_BANK_NAME")
	public String  ddBankName; 

	/*
	*约定还款开户行省
	*/
	@JsonProperty(value="DD_BANK_PROVINCE")
	public String  ddBankProvince; 

	/*
	*约定还款开户行省code
	*/
	@JsonProperty(value="DD_BANK_PROV_CODE")
	public String  ddBankProvCode; 

	/*
	*约定还款开户行市
	*/
	@JsonProperty(value="DD_BANK_CITY")
	public String  ddBankCity; 

	/*
	*约定还款开户行市code
	*/
	@JsonProperty(value="DD_BANK_CITY_CODE")
	public String  ddBankCityCode; 
	
	/*
	* 手机号
	*/
	@JsonProperty(value="MOBILE_NO")
	public String  mobileNo; 
	
	/*
	* 合作方id
	*/
	@JsonProperty(value="COOPERATOR_ID")
	public String  cooperatorId; 

	
	/*
	 * 申请日期
	 */
	@JsonProperty(value="APPLY_DATE")
	public String  applyDate;
	
	/*
	 * 放款日期
	 */
	@JsonProperty(value="LOAN_DATE")
	public String  loanDate;

	/*
	 * 贷款原因
	 */
	@JsonProperty(value="PURPOSE")
	public String  purpose;
	
	/*
	 * 销售渠道（终端类型）
	 */
	@JsonProperty(value="SUB_TERMINAL_TYPE")
	public String  subTerminalType;
	
	/*
	 * 逾期天数dpd
	 */
	@JsonProperty(value="DPD_DAYS_NUMBER")
	public int  dpdDaysNumber;
	
	/*
	 * 催收逾期天数cpd
	 */
	@JsonProperty(value="CPD_DAYS_NUMBER")
	public int  cpdDaysNumber;

	/*
	 * 还款频率（还款间隔单位）
	 */
	@JsonProperty(value="PAYMENT_INTERVAL_UNIT")
	public PaymentIntervalUnit  paymentIntervalUnit;
	
	/*
	*合同业务场景
	*/
	@JsonProperty(value="CONTRA_BIZ_SITUATION")
	public ContraBizSituation  contraBizSituation; 
	
	/*
	*申请单号
	*/
	@JsonProperty(value="APPLICATION_NO")
	public String  applicationNo; 
	
	/*
	 * 贷款列表
	 */
	@JsonProperty(value="LOAN_LIST")
	public List<STNQLLoanPMTScheByContrIDRespSubLoan>  subLoanList;

	public String getContrNbr() {
		return contrNbr;
	}

	public void setContrNbr(String contrNbr) {
		this.contrNbr = contrNbr;
	}

	public String getDefaultLoanCode() {
		return defaultLoanCode;
	}

	public void setDefaultLoanCode(String defaultLoanCode) {
		this.defaultLoanCode = defaultLoanCode;
	}

	public BigDecimal getContrLmt() {
		return contrLmt;
	}

	public void setContrLmt(BigDecimal contrLmt) {
		this.contrLmt = contrLmt;
	}

	public ContraStatus getContraStatus() {
		return contraStatus;
	}

	public void setContraStatus(ContraStatus contraStatus) {
		this.contraStatus = contraStatus;
	}

	public String getContraExpireDate() {
		return contraExpireDate;
	}

	public void setContraExpireDate(String contraExpireDate) {
		this.contraExpireDate = contraExpireDate;
	}

	public BigDecimal getContrAgreeRate() {
		return contrAgreeRate;
	}

	public void setContrAgreeRate(BigDecimal contrAgreeRate) {
		this.contrAgreeRate = contrAgreeRate;
	}

	public String getAgreeRateExpireDate() {
		return agreeRateExpireDate;
	}

	public void setAgreeRateExpireDate(String agreeRateExpireDate) {
		this.agreeRateExpireDate = agreeRateExpireDate;
	}

	public BigDecimal getContraBal() {
		return contraBal;
	}

	public void setContraBal(BigDecimal contraBal) {
		this.contraBal = contraBal;
	}

	public BigDecimal getContraBalPrin() {
		return contraBalPrin;
	}

	public void setContraBalPrin(BigDecimal contraBalPrin) {
		this.contraBalPrin = contraBalPrin;
	}

	public BigDecimal getQualGraceBal() {
		return qualGraceBal;
	}

	public void setQualGraceBal(BigDecimal qualGraceBal) {
		this.qualGraceBal = qualGraceBal;
	}

	public BigDecimal getCurrDueAmt() {
		return currDueAmt;
	}

	public void setCurrDueAmt(BigDecimal currDueAmt) {
		this.currDueAmt = currDueAmt;
	}

	public BigDecimal getPastDueAmt() {
		return pastDueAmt;
	}

	public void setPastDueAmt(BigDecimal pastDueAmt) {
		this.pastDueAmt = pastDueAmt;
	}

	public BigDecimal getContraMemoDb() {
		return contraMemoDb;
	}

	public void setContraMemoDb(BigDecimal contraMemoDb) {
		this.contraMemoDb = contraMemoDb;
	}

	public BigDecimal getContraMemoCr() {
		return contraMemoCr;
	}

	public void setContraMemoCr(BigDecimal contraMemoCr) {
		this.contraMemoCr = contraMemoCr;
	}

	public BigDecimal getContraRemain() {
		return contraRemain;
	}

	public void setContraRemain(BigDecimal contraRemain) {
		this.contraRemain = contraRemain;
	}

	public String getContraSetupDate() {
		return contraSetupDate;
	}

	public void setContraSetupDate(String contraSetupDate) {
		this.contraSetupDate = contraSetupDate;
	}

	public String getNextStmtDate() {
		return nextStmtDate;
	}

	public void setNextStmtDate(String nextStmtDate) {
		this.nextStmtDate = nextStmtDate;
	}

	public String getLastStmtDate() {
		return lastStmtDate;
	}

	public void setLastStmtDate(String lastStmtDate) {
		this.lastStmtDate = lastStmtDate;
	}

	public String getBizDate() {
		return bizDate;
	}

	public void setBizDate(String bizDate) {
		this.bizDate = bizDate;
	}

	public String getPmtDueDate() {
		return pmtDueDate;
	}

	public void setPmtDueDate(String pmtDueDate) {
		this.pmtDueDate = pmtDueDate;
	}

	public String getNextPmtDueDate() {
		return nextPmtDueDate;
	}

	public void setNextPmtDueDate(String nextPmtDueDate) {
		this.nextPmtDueDate = nextPmtDueDate;
	}

	public String getDdDate() {
		return ddDate;
	}

	public void setDdDate(String ddDate) {
		this.ddDate = ddDate;
	}

	public String getDdBankAcctNbr() {
		return ddBankAcctNbr;
	}

	public void setDdBankAcctNbr(String ddBankAcctNbr) {
		this.ddBankAcctNbr = ddBankAcctNbr;
	}

	public String getDdBankAcctName() {
		return ddBankAcctName;
	}

	public void setDdBankAcctName(String ddBankAcctName) {
		this.ddBankAcctName = ddBankAcctName;
	}

	public String getDdBankBranch() {
		return ddBankBranch;
	}

	public void setDdBankBranch(String ddBankBranch) {
		this.ddBankBranch = ddBankBranch;
	}

	public String getDdBankName() {
		return ddBankName;
	}

	public void setDdBankName(String ddBankName) {
		this.ddBankName = ddBankName;
	}

	public String getDdBankProvince() {
		return ddBankProvince;
	}

	public void setDdBankProvince(String ddBankProvince) {
		this.ddBankProvince = ddBankProvince;
	}

	public String getDdBankProvCode() {
		return ddBankProvCode;
	}

	public void setDdBankProvCode(String ddBankProvCode) {
		this.ddBankProvCode = ddBankProvCode;
	}

	public String getDdBankCity() {
		return ddBankCity;
	}

	public void setDdBankCity(String ddBankCity) {
		this.ddBankCity = ddBankCity;
	}

	public String getDdBankCityCode() {
		return ddBankCityCode;
	}

	public void setDdBankCityCode(String ddBankCityCode) {
		this.ddBankCityCode = ddBankCityCode;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	public String getCooperatorId() {
		return cooperatorId;
	}

	public void setCooperatorId(String cooperatorId) {
		this.cooperatorId = cooperatorId;
	}

	public String getApplyDate() {
		return applyDate;
	}

	public void setApplyDate(String applyDate) {
		this.applyDate = applyDate;
	}



	public String getLoanDate() {
		return loanDate;
	}

	public void setLoanDate(String loanDate) {
		this.loanDate = loanDate;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	
	public String getSubTerminalType() {
		return subTerminalType;
	}

	public void setSubTerminalType(String subTerminalType) {
		this.subTerminalType = subTerminalType;
	}

	public int getDpdDaysNumber() {
		return dpdDaysNumber;
	}

	public void setDpdDaysNumber(int dpdDaysNumber) {
		this.dpdDaysNumber = dpdDaysNumber;
	}

	public int getCpdDaysNumber() {
		return cpdDaysNumber;
	}

	public void setCpdDaysNumber(int cpdDaysNumber) {
		this.cpdDaysNumber = cpdDaysNumber;
	}

	public PaymentIntervalUnit getPaymentIntervalUnit() {
		return paymentIntervalUnit;
	}

	public void setPaymentIntervalUnit(PaymentIntervalUnit paymentIntervalUnit) {
		this.paymentIntervalUnit = paymentIntervalUnit;
	}

	public ContraBizSituation getContraBizSituation() {
		return contraBizSituation;
	}

	public void setContraBizSituation(ContraBizSituation contraBizSituation) {
		this.contraBizSituation = contraBizSituation;
	}

	public String getApplicationNo() {
		return applicationNo;
	}

	public void setApplicationNo(String applicationNo) {
		this.applicationNo = applicationNo;
	}

	public List<STNQLLoanPMTScheByContrIDRespSubLoan> getSubLoanList() {
		return subLoanList;
	}

	public void setSubLoanList(
			List<STNQLLoanPMTScheByContrIDRespSubLoan> subLoanList) {
		this.subLoanList = subLoanList;
	}

	public String getCustUuid() {
		return custUuid;
	}

	public void setCustUuid(String custUuid) {
		this.custUuid = custUuid;
	}
}
