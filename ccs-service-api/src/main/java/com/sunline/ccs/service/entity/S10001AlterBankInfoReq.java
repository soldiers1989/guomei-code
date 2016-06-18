package com.sunline.ccs.service.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 客户银行卡信息修改接口接受报文
 * @author jjb
 *
 */
public class S10001AlterBankInfoReq extends SunshineRequestInfo implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 保单号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="GUARANTYID")
	private String guarantyid;
	/**
	 * 新的放款/还款卡号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="NEWPUTPAYCARDID")
	private String newputpaycardid;
	/**
	 * 开户行名称
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="BANKNAME")
	private String bankname;
	/**
	 * 开户行code
	 */
	@Check(lengths=10,notEmpty=true)
	@JsonProperty(value="BANKCODE")
	private String bankcode;
	/**
	 * 开户行省
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="BANKPROVINCE")
	private String bankprovince;
	/**
	 * 开户行省code
	 */
	@Check(lengths=10,notEmpty=true)
	@JsonProperty(value="BANKPROVINCECODE")
	private String bankprovincecode;
	/**
	 * 开户行市
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="BANKCITY")
	private String bankcity;
	/**
	 * 开户行市code
	 */
	@Check(lengths=10,notEmpty=true)
	@JsonProperty(value="BANKCITYCODE")
	private String bankcitycode;
	/**
	 * 银行卡开户人 名称
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="BANKOWNER")
	private String bankowner;
	
	public String getGuarantyid() {
		return guarantyid;
	}
	public void setGuarantyid(String guarantyid) {
		this.guarantyid = guarantyid;
	}
	public String getNewputpaycardid() {
		return newputpaycardid;
	}
	public void setNewputpaycardid(String newputpaycardid) {
		this.newputpaycardid = newputpaycardid;
	}
	public String getBankname() {
		return bankname;
	}
	public void setBankname(String bankname) {
		this.bankname = bankname;
	}
	public String getBankcode() {
		return bankcode;
	}
	public void setBankcode(String bankcode) {
		this.bankcode = bankcode;
	}
	public String getBankprovince() {
		return bankprovince;
	}
	public void setBankprovince(String bankprovince) {
		this.bankprovince = bankprovince;
	}
	public String getBankprovincecode() {
		return bankprovincecode;
	}
	public void setBankprovincecode(String bankprovincecode) {
		this.bankprovincecode = bankprovincecode;
	}
	public String getBankcity() {
		return bankcity;
	}
	public void setBankcity(String bankcity) {
		this.bankcity = bankcity;
	}
	public String getBankcitycode() {
		return bankcitycode;
	}
	public void setBankcitycode(String bankcitycode) {
		this.bankcitycode = bankcitycode;
	}
	public String getBankowner() {
		return bankowner;
	}
	public void setBankowner(String bankowner) {
		this.bankowner = bankowner;
	}
}
