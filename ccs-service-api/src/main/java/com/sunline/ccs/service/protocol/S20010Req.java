package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 贷款产品定价信息查询
* @author fanghj
 * @time 2014-3-24 下午4:27:33
 */
public class S20010Req implements Serializable {

	private static final long serialVersionUID = -8921755321817338740L;
	
	/**
	 * 贷款产品编号
	 */
	public String loan_code;

	public String getLoan_code() {
		return loan_code;
	}

	public void setLoan_code(String loan_code) {
		this.loan_code = loan_code;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

