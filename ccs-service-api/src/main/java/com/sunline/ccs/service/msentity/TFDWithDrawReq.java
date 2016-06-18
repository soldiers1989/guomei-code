package com.sunline.ccs.service.msentity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 循环现金贷提款请求接口(随借随换)
 * @author wangz
 *
 */
@SuppressWarnings("serial")
public class TFDWithDrawReq extends MsRequestInfo {
	/**
	 * 合同号
	 */
	@Check(lengths=22,notEmpty=true)
	@JsonProperty(value="CONTR_NBR")
	public String contractNo;

	/**
	 * 期数
	 */
	@Check(lengths=2,notEmpty=false,isNumber=true)
	@JsonProperty(value="TERM")
	public Integer term;
	/**
	 * 提现金额
	 */
	@Check(lengths=18,notEmpty=true,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
	@JsonProperty(value="AMOUNT")
	public BigDecimal amount;
	/**
	 * 手机号
	 */
	@Check(lengths=11,notEmpty=true,fixed=true,isNumber=true)
	@JsonProperty(value="MOBILE")
	public String mobile;

	/**
	 * 放款/还款卡号
	 */
	@Check(lengths=32)
	@JsonProperty(value="BANK_CARD_NBR")
	public String bankCardNbr;
	/**
	 * 银行卡开户人 名称
	 */
	@Check(lengths=32)
	@JsonProperty(value="BANK_CARD_NAME")
	public String bankCardName;
	
	/**
	 * 开户行名称
	 */
	@Check(lengths=32)
	@JsonProperty(value="BANK_NAME")
	public String bankName;
	/**
	 * 开户行code
	 */
	@Check(lengths=10)
	@JsonProperty(value="BANK_CODE")
	public String bankCode;
	/**
	 * 开户行省
	 */
	@Check(lengths=32)
	@JsonProperty(value="BANK_PROVINCE")
	public String bankProvince;
	/**
	 * 开户行省code
	 */
	@Check(lengths=10)
	@JsonProperty(value="BANK_PROV_CODE")
	public String bankProvinceCode;
	/**
	 * 开户行市
	 */
	@Check(lengths=32)
	@JsonProperty(value="BANK_CITY")
	public String bankCity;
	/**
	 * 开户行市code
	 */
	@Check(lengths=10)
	@JsonProperty(value="BANK_CITY_CODE")
	public String bankCityCode;

	public String getContractNo() {
		return contractNo;
	}

	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
	public Integer getTerm() {
		return term;
	}

	public void setTerm(Integer term) {
		this.term = term;
	}

	public String getBankCardNbr() {
		return bankCardNbr;
	}

	public void setBankCardNbr(String bankCardNbr) {
		this.bankCardNbr = bankCardNbr;
	}

	public String getBankCardName() {
		return bankCardName;
	}

	public void setBankCardName(String bankCardName) {
		this.bankCardName = bankCardName;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getBankCode() {
		return bankCode;
	}

	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}

	public String getBankProvince() {
		return bankProvince;
	}

	public void setBankProvince(String bankProvince) {
		this.bankProvince = bankProvince;
	}

	public String getBankProvinceCode() {
		return bankProvinceCode;
	}

	public void setBankProvinceCode(String bankProvinceCode) {
		this.bankProvinceCode = bankProvinceCode;
	}

	public String getBankCity() {
		return bankCity;
	}

	public void setBankCity(String bankCity) {
		this.bankCity = bankCity;
	}

	public String getBankCityCode() {
		return bankCityCode;
	}

	public void setBankCityCode(String bankCityCode) {
		this.bankCityCode = bankCityCode;
	}
	
}
