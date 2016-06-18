package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ccs.param.def.enums.PrepaymentFeeInd;
import com.sunline.ccs.param.def.enums.PrepaymentInd;

/**
 *现金分期参数查询响应接口
* @author fanghj
 *@time 2014-6-4 下午9:40:37
 */
public class S13002Resp implements Serializable{

	private static final long serialVersionUID = -2976381485701210076L;
	
	/**
	 * 卡号
	 */
	public String card_no;
	
	/**
	 * 分期类型
	 */
	public LoanType loan_type;
	
	/**
	 * 分期计划描述
	 */
	public String description;
	
	/**
	 * 提前还款支持标识
	 */
	public PrepaymentInd prepayment_ind;
	
	/**
	 * 提前还款退手续费标志
	 */
	public PrepaymentFeeInd prepayment_fee_ind;
	
	public ArrayList<S13002LoanFeeDef> loanfeedefs;

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	public LoanType getLoan_type() {
		return loan_type;
	}

	public void setLoan_type(LoanType loan_type) {
		this.loan_type = loan_type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public PrepaymentInd getPrepayment_ind() {
		return prepayment_ind;
	}

	public void setPrepayment_ind(PrepaymentInd prepayment_ind) {
		this.prepayment_ind = prepayment_ind;
	}

	public PrepaymentFeeInd getPrepayment_fee_ind() {
		return prepayment_fee_ind;
	}

	public void setPrepayment_fee_ind(PrepaymentFeeInd prepayment_fee_ind) {
		this.prepayment_fee_ind = prepayment_fee_ind;
	}

	public ArrayList<S13002LoanFeeDef> getLoanfeedefs() {
		return loanfeedefs;
	}

	public void setLoanfeedefs(ArrayList<S13002LoanFeeDef> loanfeedefs) {
		this.loanfeedefs = loanfeedefs;
	}
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
