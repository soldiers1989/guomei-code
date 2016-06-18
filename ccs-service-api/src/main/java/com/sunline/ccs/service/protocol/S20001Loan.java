package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ccs.param.def.enums.LoanPlanStatus;
import com.sunline.ccs.param.def.enums.PrepaymentInd;

/**
 *
* @author fanghj
 *@time 2014-3-24 下午4:23:50
 */
public class S20001Loan implements Serializable{

	private static final long serialVersionUID = 4500852466778613419L;

	/**
     * 贷款产品编号
     */
    public String loan_code;

    /**
     * 贷款产品描述
     */
    public String description;

    /**
     * 贷款产品类型
     */
    public LoanType loan_type;

    /**
     * 贷款产品有效期
     */
    public Date loan_validity;

    /**
     * 贷款产品状态
     */
    public LoanPlanStatus loan_status;

    /**
     * 提前还款支持标识
     */
    public PrepaymentInd prepaymentind;

    /**
     * 贷款支持最短周期
     */
    public Integer min_cyle;

    /**
     * 贷款支持最长周期
     */
    public Integer max_cyle;

	public String getLoan_code() {
		return loan_code;
	}

	public void setLoan_code(String loan_code) {
		this.loan_code = loan_code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LoanType getLoan_type() {
		return loan_type;
	}

	public void setLoan_type(LoanType loan_type) {
		this.loan_type = loan_type;
	}

	public Date getLoan_validity() {
		return loan_validity;
	}

	public void setLoan_validity(Date loan_validity) {
		this.loan_validity = loan_validity;
	}

	public LoanPlanStatus getLoan_status() {
		return loan_status;
	}

	public void setLoan_status(LoanPlanStatus loan_status) {
		this.loan_status = loan_status;
	}

	public PrepaymentInd getPrepaymentind() {
		return prepaymentind;
	}

	public void setPrepaymentind(PrepaymentInd prepaymentind) {
		this.prepaymentind = prepaymentind;
	}

	public Integer getMin_cyle() {
		return min_cyle;
	}

	public void setMin_cyle(Integer min_cyle) {
		this.min_cyle = min_cyle;
	}

	public Integer getMax_cyle() {
		return max_cyle;
	}

	public void setMax_cyle(Integer max_cyle) {
		this.max_cyle = max_cyle;
	}
    
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
