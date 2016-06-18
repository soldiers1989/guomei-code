package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 分期展期响应接口
 * 
* @author fanghj
 * @date 2013-6-5  上午10:41:47
 * @version 1.0
 */
public class S13130Resp implements Serializable {

	private static final long serialVersionUID = -3856360310904670L;

	/**
     * 卡号
     */
    public String card_no ;

    /**
     * 分期顺序号
     */
    public Long register_id ;

    /**
     * 原总期数
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
     * 展期期数
     */
    public Integer reschedule_term ;

    /**
     * 展期手续费
     */
    public BigDecimal reschedule_fee ;

    /**
     * 展期后每期应还本金
     */
    public BigDecimal reschedule_fixed_pmt_prin ;

    /**
     * 展期后首期应还本金
     */
    public BigDecimal reschedule_first_term_prin ;

    /**
     * 展期后末期应还本金
     */
    public BigDecimal reschedule_final_term_prin ;

    /**
     * 展期后总手续费
     */
    public BigDecimal reschedule_init_fee1 ;

    /**
     * 展期后每期手续费
     */
    public BigDecimal reschedule_fixed_fee1 ;

    /**
     * 展期后首期手续费
     */
    public BigDecimal reschedule_first_term_fee1 ;

    /**
     * 展期后末期手续费
     */
    public BigDecimal reschedule_final_term_fee1 ;

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

	public Integer getReschedule_term() {
		return reschedule_term;
	}

	public void setReschedule_term(Integer reschedule_term) {
		this.reschedule_term = reschedule_term;
	}

	public BigDecimal getReschedule_fee() {
		return reschedule_fee;
	}

	public void setReschedule_fee(BigDecimal reschedule_fee) {
		this.reschedule_fee = reschedule_fee;
	}

	public BigDecimal getReschedule_fixed_pmt_prin() {
		return reschedule_fixed_pmt_prin;
	}

	public void setReschedule_fixed_pmt_prin(BigDecimal reschedule_fixed_pmt_prin) {
		this.reschedule_fixed_pmt_prin = reschedule_fixed_pmt_prin;
	}

	public BigDecimal getReschedule_first_term_prin() {
		return reschedule_first_term_prin;
	}

	public void setReschedule_first_term_prin(BigDecimal reschedule_first_term_prin) {
		this.reschedule_first_term_prin = reschedule_first_term_prin;
	}

	public BigDecimal getReschedule_final_term_prin() {
		return reschedule_final_term_prin;
	}

	public void setReschedule_final_term_prin(BigDecimal reschedule_final_term_prin) {
		this.reschedule_final_term_prin = reschedule_final_term_prin;
	}

	public BigDecimal getReschedule_init_fee1() {
		return reschedule_init_fee1;
	}

	public void setReschedule_init_fee1(BigDecimal reschedule_init_fee1) {
		this.reschedule_init_fee1 = reschedule_init_fee1;
	}

	public BigDecimal getReschedule_fixed_fee1() {
		return reschedule_fixed_fee1;
	}

	public void setReschedule_fixed_fee1(BigDecimal reschedule_fixed_fee1) {
		this.reschedule_fixed_fee1 = reschedule_fixed_fee1;
	}

	public BigDecimal getReschedule_first_term_fee1() {
		return reschedule_first_term_fee1;
	}

	public void setReschedule_first_term_fee1(BigDecimal reschedule_first_term_fee1) {
		this.reschedule_first_term_fee1 = reschedule_first_term_fee1;
	}

	public BigDecimal getReschedule_final_term_fee1() {
		return reschedule_final_term_fee1;
	}

	public void setReschedule_final_term_fee1(BigDecimal reschedule_final_term_fee1) {
		this.reschedule_final_term_fee1 = reschedule_final_term_fee1;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

