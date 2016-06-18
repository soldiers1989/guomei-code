package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.IdType;

/**
 *按照证件类型和证件号码查询贷款
* @author fanghj
 *@time 2014-3-31 下午5:38:56
 */
public class S20022Resp implements Serializable{

	private static final long serialVersionUID = -5144969063272606200L;

	/**
	 * 证件类型
	 */
	public IdType id_type;
	
	/**
	 * 证件号码
	 */
	public String id_no;
	
	public ArrayList<S20022Loan>loans;

	public IdType getId_type() {
		return id_type;
	}

	public void setId_type(IdType id_type) {
		this.id_type = id_type;
	}

	public String getId_no() {
		return id_no;
	}

	public void setId_no(String id_no) {
		this.id_no = id_no;
	}

	public ArrayList<S20022Loan> getLoans() {
		return loans;
	}

	public void setLoans(ArrayList<S20022Loan> loans) {
		this.loans = loans;
	}
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
