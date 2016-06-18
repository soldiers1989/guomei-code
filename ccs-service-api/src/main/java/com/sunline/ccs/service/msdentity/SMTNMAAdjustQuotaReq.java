package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 调额请求报文
 * @author Liming.Feng
 *
 */
public class SMTNMAAdjustQuotaReq implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 合同号
	 */
	@Check(lengths=22,notEmpty=true)
	@JsonProperty(value="CONTR_NBR")
	private String contractNo;
	/**
	 * 操作员
	 */
	@JsonProperty(value="OP_ID")
	private String Operator;
	/**
	*可用额度
	*/
	@Check(lengths=15)
	@JsonProperty(value="CREDIT_LMT")
	public BigDecimal  creditLmt; 
	/**
	 * 客户姓名
	 */
	@JsonProperty(value="NAME")
	private String name;
	/**
	 * 调额日期
	 */
	@JsonProperty(value="DATE")
	private Date date;
	
	public String getContractNo() {
		return contractNo;
	}
	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}
	public String getOperator() {
		return Operator;
	}
	public void setOperator(String operator) {
		Operator = operator;
	}
	public BigDecimal getCreditLmt() {
		return creditLmt;
	}
	public void setCreditLmt(BigDecimal creditLmt) {
		this.creditLmt = creditLmt;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
}
