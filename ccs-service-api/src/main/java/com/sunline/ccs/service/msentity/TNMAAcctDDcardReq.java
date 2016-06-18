package com.sunline.ccs.service.msentity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 客户银行卡信息修改接口接受报文
 * @author ymk
 *
 */
public class TNMAAcctDDcardReq extends MsRequestInfo implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	
	/**
	 * 客户号
	 */
	@Check(lengths=32,notEmpty=false)
	@JsonProperty(value="UUID")
	public String uuid;
	/**
	 * 合同号
	 */
	@Check(lengths=22,notEmpty=false)
	@JsonProperty(value="CONTR_NBR")
	public String contractNo;
	/**
	 * 新的放款/还款卡号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="NEW_BANK_ACCT_NBR")
	private String newBankAcctNbr;
	/**
	 * 约定还款账户姓名
	 */
	@Check(lengths=160,notEmpty=true)
	@JsonProperty(value="DD_BANK_ACCT_NAME")
	private String ddBankAcctName;
	/**
	 * 约定还款开户行号
	 */
	@Check(lengths=9,notEmpty=true)
	@JsonProperty(value="DD_BANK_BRANCH")
	private String ddBankBranch;
	/**
	 * 约定还款银行名称
	 */
	@Check(lengths=160,notEmpty=false)
	@JsonProperty(value="DD_BANK_NAME")
	private String ddBankName;
	/**
	 * 开户行省
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="DD_BANK_PROVINCE")
	private String ddBankProvince;
	/**
	 * 开户行省code
	 */
	@Check(lengths=10,notEmpty=true)
	@JsonProperty(value="DD_BANK_PROV_CODE")
	private String ddBankProvCode;
	/**
	 * 开户行市
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="DD_BANK_CITY")
	private String ddBankCity;
	/**
	 * 开户行市code
	 */
	@Check(lengths=10,notEmpty=true)
	@JsonProperty(value="DD_BANK_CITY_CODE")
	private String ddBankCityCode;
	
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getContractNo() {
		return contractNo;
	}
	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}
	public String getNewBankAcctNbr() {
		return newBankAcctNbr;
	}
	public void setNewBankAcctNbr(String newBankAcctNbr) {
		this.newBankAcctNbr = newBankAcctNbr;
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
	
}
