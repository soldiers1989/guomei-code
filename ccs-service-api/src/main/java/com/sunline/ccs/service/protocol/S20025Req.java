package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 根据借据号查询贷款还款分配历史
* @author fanghj
 * @time 2014-3-31 下午11:30:29
 */
public class S20025Req implements Serializable {

	private static final long serialVersionUID = -3000213769635717139L;

	/**
     * 卡号
     */
    public String card_no;

    /**
     * 贷款借据号
     */
    public String loan_receipt_nbr;

    /**
     * 贷款申请顺序号
     */
    public Integer register_hst_id;
    
	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	public String getLoan_receipt_nbr() {
		return loan_receipt_nbr;
	}

	public void setLoan_receipt_nbr(String loan_receipt_nbr) {
		this.loan_receipt_nbr = loan_receipt_nbr;
	}

	public Integer getRegister_hst_id() {
		return register_hst_id;
	}

	public void setRegister_hst_id(Integer register_hst_id) {
		this.register_hst_id = register_hst_id;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

