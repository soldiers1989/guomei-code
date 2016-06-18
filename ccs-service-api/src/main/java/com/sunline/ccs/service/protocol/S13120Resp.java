package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 分期提前还款响应接口
 * 
* @author fanghj
 * @date 2013-6-5  上午10:31:53
 * @version 1.0
 */
public class S13120Resp implements Serializable {

	private static final long serialVersionUID = 1295503943005022049L;

	/**
     * 卡号
     */
    public String card_no ;

    /**
     * 分期申请顺序号
     */
    public Long register_id ;

    /**
     * 总期数
     */
    public Integer loan_init_term ;

    /**
     * 已执行期数
     */
    public Integer curr_term ;

    /**
     * 剩余本金
     */
    public BigDecimal unearned_prin ;

    /**
     * 提前还款手续费
     */
    public BigDecimal prepayment_fee ;

    /**
     * 总应还金额
     */
    public BigDecimal total_amt ;

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	public Long getRegister_id() {
		return register_id;
	}

	public void setRegister_id(Long register_id) {
		this.register_id = register_id;
	}

	public Integer getLoan_init_term() {
		return loan_init_term;
	}

	public void setLoan_init_term(Integer loan_init_term) {
		this.loan_init_term = loan_init_term;
	}

	public Integer getCurr_term() {
		return curr_term;
	}

	public void setCurr_term(Integer curr_term) {
		this.curr_term = curr_term;
	}

	public BigDecimal getUnearned_prin() {
		return unearned_prin;
	}

	public void setUnearned_prin(BigDecimal unearned_prin) {
		this.unearned_prin = unearned_prin;
	}

	public BigDecimal getPrepayment_fee() {
		return prepayment_fee;
	}

	public void setPrepayment_fee(BigDecimal prepayment_fee) {
		this.prepayment_fee = prepayment_fee;
	}

	public BigDecimal getTotal_amt() {
		return total_amt;
	}

	public void setTotal_amt(BigDecimal total_amt) {
		this.total_amt = total_amt;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

