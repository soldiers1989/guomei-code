package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.param.def.enums.ContraBizSituation;
import com.sunline.ccs.param.def.enums.ContraStatus;
import com.sunline.ppy.dictionary.enums.LoanMold;
/**
 * 客户合同列表查询接口返回报文
 * 合同列表-单个合同
 * @author zqx
 */
public class STNQAAcctsbyCustUUIDRESPSubContract implements Serializable  {
	
	private static final long serialVersionUID = 1L;
	/*
	*合同号
	*/
	@JsonProperty(value="CONTR_NBR")
	public String  contrNbr; 
	/*
	*默认贷款产品代码
	*/
	@JsonProperty(value="DEFAULT_LOAN_CODE")
	public String  defaultLoanCode; 
	
	/*
	*默认放款类型
	*/
	@JsonProperty(value="DEFAULT_LOAN_MOLD")
	public LoanMold  defaultLoanMold; 
	

	/*
	*合同额度
	*/
	@JsonProperty(value="CONTR_LMT")
	public BigDecimal  contrLmt; 

	/*
	*合同状态
	*/
	@JsonProperty(value="CONTRA_STATUS")
	public ContraStatus  contraStatus; 

	/*
	*合同到期日期
	*/
	@JsonProperty(value="CONTRA_EXPIRE_DATE")
	public String  contraExpireDate; 

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
	*上一账单日
	*/
	@JsonProperty(value="LAST_STMT_DATE")
	public String  lastStmtDate; 
	/*
	*下一账单日
	*/
	@JsonProperty(value="NEXT_STMT_DATE")
	public String  nextStmtDate; 
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
	*当期到期还款日
	*/
	@JsonProperty(value="BIZ_DATE")
	public String  bizDate; 
	/*
	*当期到期还款日
	*/
	@JsonProperty(value="PMT_DUE_DATE")
	public String  pmtDueDate; 
	/*
	*下一到期还款日
	*/
	@JsonProperty(value="NEXT_PMT_DUE_DATE")
	public String  nextPmtDueDate; 

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
	*创建时间
	*/
	@JsonProperty(value="CREAT_TIME")
	public String  creatTime;
	/*
	*合同业务场景
	*/
	@JsonProperty(value="CONTRA_BIZ_SITUATION")
	public ContraBizSituation  contraBizSituation; 
	/*
	* 申请号
	*/
	@JsonProperty(value="APPLICATION_NO")
	public String  applicationNo;
	
	/*
	* 最近更新时间
	* yyyyMMddHHmmssSSSS
	*/
	@JsonProperty(value="CONTR_LST_UPD_TIME")
	public String  contrLstUpdTime;
	
	
	@JsonProperty(value="LOAN_LIST")
	public List<STNQAAcctsbyCustUUIDRESPSubLoan> loanList;

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

	public String getContrNbr() {
		return contrNbr;
	}

	public void setContrNbr(String contrNbr) {
		this.contrNbr = contrNbr;
	}

	public ContraStatus getContraStatus() {
		return contraStatus;
	}

	public void setContraStatus(ContraStatus contraStatus) {
		this.contraStatus = contraStatus;
	}

	public BigDecimal getContrLmt() {
		return contrLmt;
	}

	public void setContrLmt(BigDecimal contrLmt) {
		this.contrLmt = contrLmt;
	}

	public String getContraExpireDate() {
		return contraExpireDate;
	}

	public void setContraExpireDate(String contraExpireDate) {
		this.contraExpireDate = contraExpireDate;
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

	public String getNextPmtDueDate() {
		return nextPmtDueDate;
	}

	public void setNextPmtDueDate(String nextPmtDueDate) {
		this.nextPmtDueDate = nextPmtDueDate;
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

	public List<STNQAAcctsbyCustUUIDRESPSubLoan> getLoanList() {
		return loanList;
	}

	public void setLoanList(List<STNQAAcctsbyCustUUIDRESPSubLoan> loanList) {
		this.loanList = loanList;
	}

	public BigDecimal getQualGraceBal() {
		return qualGraceBal;
	}

	public void setQualGraceBal(BigDecimal qualGraceBal) {
		this.qualGraceBal = qualGraceBal;
	}

	public String getDefaultLoanCode() {
		return defaultLoanCode;
	}

	public void setDefaultLoanCode(String defaultLoanCode) {
		this.defaultLoanCode = defaultLoanCode;
	}


	public LoanMold getDefaultLoanMold() {
		return defaultLoanMold;
	}

	public void setDefaultLoanMold(LoanMold defaultLoanMold) {
		this.defaultLoanMold = defaultLoanMold;
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

	public String getCreatTime() {
		return creatTime;
	}

	public void setCreatTime(String creatTime) {
		this.creatTime = creatTime;
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

	public String getLastStmtDate() {
		return lastStmtDate;
	}

	public void setLastStmtDate(String lastStmtDate) {
		this.lastStmtDate = lastStmtDate;
	}

	public String getNextStmtDate() {
		return nextStmtDate;
	}

	public void setNextStmtDate(String nextStmtDate) {
		this.nextStmtDate = nextStmtDate;
	}

	public String getContrLstUpdTime() {
		return contrLstUpdTime;
	}

	public void setContrLstUpdTime(String contrLstUpdTime) {
		this.contrLstUpdTime = contrLstUpdTime;
	}
	
}
