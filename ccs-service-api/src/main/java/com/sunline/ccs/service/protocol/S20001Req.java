package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ccs.param.def.enums.LoanPlanStatus;

/**
 *贷款产品信息查询
* @author fanghj
 *@time 2014-3-24 下午3:29:18
 */
public class S20001Req implements Serializable{
	
	private static final long serialVersionUID = -630430852692071869L;

	/**
	 * 贷款卡号
	 */
	public String card_no;
	
	/**
	 * 产品状态
	 */
	public LoanPlanStatus loan_status;
	
	/**
	 * 贷款产品类型
	 */
	public LoanType loan_type;

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	public LoanPlanStatus getLoan_status() {
		return loan_status;
	}

	public void setLoan_status(LoanPlanStatus loan_status) {
		this.loan_status = loan_status;
	}

	public LoanType getLoan_type() {
		return loan_type;
	}

	public void setLoan_type(LoanType loan_type) {
		this.loan_type = loan_type;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
