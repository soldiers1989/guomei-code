package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ccs.param.def.enums.LoanTerminateReason;

/**
 *  据贷款借据号查询贷款信息
* @author fanghj
 * @time 2014-4-1 下午8:13:16
 */
public class S20021Resp implements Serializable {

	private static final long serialVersionUID = 3521552635721720754L;

	/**
     * 借据号
     */
    public String loan_receipt_nbr;

    /**
     * 贷款计划ID
     */
    public Long loan_id;

    /**
     * 贷款注册日期
     */
    public Date register_date;

    /**
     * 请求日期时间
     */
    public Date request_time;

    /**
     * 贷款类型
     */
    public LoanType loan_type;

    /**
     * 贷款状态
     */
    public LoanStatus loan_status;

    /**
     * 贷款上次状态
     */
    public LoanStatus last_loan_status;

    /**
     * 贷款总期数
     */
    public Integer loan_init_term;

    /**
     * 当前期数
     */
    public Integer curr_term;

    /**
     * 剩余期数
     */
    public Integer remain_term;

    /**
     * 贷款总本金
     */
    public BigDecimal loan_init_prin;

    /**
     * 激活日期
     */
    public Date activate_date;

    /**
     * 还清日期
     */
    public Date paid_out_date;

    /**
     * 提前终止日期
     */
    public Date terminate_date;

    /**
     * 贷款终止原因代码
     */
    public LoanTerminateReason terminate_reason_cd;

    /**
     * 已偿还本金
     */
    public BigDecimal prin_paid;

    /**
     * 已偿还利息
     */
    public BigDecimal int_paid;

    /**
     * 已偿还费用
     */
    public BigDecimal fee_paid;

    /**
     * 贷款当前总余额
     */
    public BigDecimal loan_curr_bal;

    /**
     * 贷款已出账单本金
     */
    public BigDecimal loan_prin_xfrin;

    /**
     * 贷款未到期手续费
     */
    public BigDecimal loan_fee1_xfrout;

    /**
     * 贷款已出账单手续费
     */
    public BigDecimal loan_fee1_xfrin;

    /**
     * 贷款产品代码
     */
    public String loan_code;

    /**
     * 贷款申请顺序号
     */
    public Long register_id;

    /**
     * 贷款手续费收取方式
     */
    public LoanFeeMethod loan_fee_method;

    /**
     * 基础利率
     */
    public BigDecimal interest_rate;

    /**
     * 罚息利率
     */
    public BigDecimal penalty_rate;

    /**
     * 复利利率
     */
    public BigDecimal compound_rate;

    /**
     * 浮动比例
     */
    public BigDecimal float_rate;

    /**
     * 贷款到期日期
     */
    public Date loan_expire_date;

    /**
     * 贷款逾期最大期数
     */
    public String loan_cd;

    /**
     * 24个月还款状态
     */
    public String payment_hist;

    /**
     * 当期还款额
     */
    public BigDecimal ctd_payment_amt;

    /**
     * 已展期次数
     */
    public Integer past_resch_cnt;

    /**
     * 已缩期次数
     */
    public Integer past_systolic_cnt;

    /**
     * 提前还款金额
     */
    public BigDecimal adv_pmt_amt;

	public String getLoan_receipt_nbr() {
		return loan_receipt_nbr;
	}

	public void setLoan_receipt_nbr(String loan_receipt_nbr) {
		this.loan_receipt_nbr = loan_receipt_nbr;
	}

	public Long getLoan_id() {
		return loan_id;
	}

	public void setLoan_id(Long loan_id) {
		this.loan_id = loan_id;
	}

	public Date getRegister_date() {
		return register_date;
	}

	public void setRegister_date(Date register_date) {
		this.register_date = register_date;
	}

	public Date getRequest_time() {
		return request_time;
	}

	public void setRequest_time(Date request_time) {
		this.request_time = request_time;
	}

	public LoanType getLoan_type() {
		return loan_type;
	}

	public void setLoan_type(LoanType loan_type) {
		this.loan_type = loan_type;
	}

	public LoanStatus getLoan_status() {
		return loan_status;
	}

	public void setLoan_status(LoanStatus loan_status) {
		this.loan_status = loan_status;
	}

	public LoanStatus getLast_loan_status() {
		return last_loan_status;
	}

	public void setLast_loan_status(LoanStatus last_loan_status) {
		this.last_loan_status = last_loan_status;
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

	public Integer getRemain_term() {
		return remain_term;
	}

	public void setRemain_term(Integer remain_term) {
		this.remain_term = remain_term;
	}

	public BigDecimal getLoan_init_prin() {
		return loan_init_prin;
	}

	public void setLoan_init_prin(BigDecimal loan_init_prin) {
		this.loan_init_prin = loan_init_prin;
	}

	public Date getActivate_date() {
		return activate_date;
	}

	public void setActivate_date(Date activate_date) {
		this.activate_date = activate_date;
	}

	public Date getPaid_out_date() {
		return paid_out_date;
	}

	public void setPaid_out_date(Date paid_out_date) {
		this.paid_out_date = paid_out_date;
	}

	public Date getTerminate_date() {
		return terminate_date;
	}

	public void setTerminate_date(Date terminate_date) {
		this.terminate_date = terminate_date;
	}

	public LoanTerminateReason getTerminate_reason_cd() {
		return terminate_reason_cd;
	}

	public void setTerminate_reason_cd(LoanTerminateReason terminate_reason_cd) {
		this.terminate_reason_cd = terminate_reason_cd;
	}

	public BigDecimal getPrin_paid() {
		return prin_paid;
	}

	public void setPrin_paid(BigDecimal prin_paid) {
		this.prin_paid = prin_paid;
	}

	public BigDecimal getInt_paid() {
		return int_paid;
	}

	public void setInt_paid(BigDecimal int_paid) {
		this.int_paid = int_paid;
	}

	public BigDecimal getFee_paid() {
		return fee_paid;
	}

	public void setFee_paid(BigDecimal fee_paid) {
		this.fee_paid = fee_paid;
	}

	public BigDecimal getLoan_curr_bal() {
		return loan_curr_bal;
	}

	public void setLoan_curr_bal(BigDecimal loan_curr_bal) {
		this.loan_curr_bal = loan_curr_bal;
	}

	public BigDecimal getLoan_prin_xfrin() {
		return loan_prin_xfrin;
	}

	public void setLoan_prin_xfrin(BigDecimal loan_prin_xfrin) {
		this.loan_prin_xfrin = loan_prin_xfrin;
	}

	public BigDecimal getLoan_fee1_xfrout() {
		return loan_fee1_xfrout;
	}

	public void setLoan_fee1_xfrout(BigDecimal loan_fee1_xfrout) {
		this.loan_fee1_xfrout = loan_fee1_xfrout;
	}

	public BigDecimal getLoan_fee1_xfrin() {
		return loan_fee1_xfrin;
	}

	public void setLoan_fee1_xfrin(BigDecimal loan_fee1_xfrin) {
		this.loan_fee1_xfrin = loan_fee1_xfrin;
	}

	public String getLoan_code() {
		return loan_code;
	}

	public void setLoan_code(String loan_code) {
		this.loan_code = loan_code;
	}

	public Long getRegister_id() {
		return register_id;
	}

	public void setRegister_id(Long register_id) {
		this.register_id = register_id;
	}

	public LoanFeeMethod getLoan_fee_method() {
		return loan_fee_method;
	}

	public void setLoan_fee_method(LoanFeeMethod loan_fee_method) {
		this.loan_fee_method = loan_fee_method;
	}

	public BigDecimal getInterest_rate() {
		return interest_rate;
	}

	public void setInterest_rate(BigDecimal interest_rate) {
		this.interest_rate = interest_rate;
	}

	public BigDecimal getPenalty_rate() {
		return penalty_rate;
	}

	public void setPenalty_rate(BigDecimal penalty_rate) {
		this.penalty_rate = penalty_rate;
	}

	public BigDecimal getCompound_rate() {
		return compound_rate;
	}

	public void setCompound_rate(BigDecimal compound_rate) {
		this.compound_rate = compound_rate;
	}

	public BigDecimal getFloat_rate() {
		return float_rate;
	}

	public void setFloat_rate(BigDecimal float_rate) {
		this.float_rate = float_rate;
	}

	public Date getLoan_expire_date() {
		return loan_expire_date;
	}

	public void setLoan_expire_date(Date loan_expire_date) {
		this.loan_expire_date = loan_expire_date;
	}

	public String getLoan_cd() {
		return loan_cd;
	}

	public void setLoan_cd(String loan_cd) {
		this.loan_cd = loan_cd;
	}

	public String getPayment_hist() {
		return payment_hist;
	}

	public void setPayment_hist(String payment_hist) {
		this.payment_hist = payment_hist;
	}

	public BigDecimal getCtd_payment_amt() {
		return ctd_payment_amt;
	}

	public void setCtd_payment_amt(BigDecimal ctd_payment_amt) {
		this.ctd_payment_amt = ctd_payment_amt;
	}

	public Integer getPast_resch_cnt() {
		return past_resch_cnt;
	}

	public void setPast_resch_cnt(Integer past_resch_cnt) {
		this.past_resch_cnt = past_resch_cnt;
	}

	public Integer getPast_systolic_cnt() {
		return past_systolic_cnt;
	}

	public void setPast_systolic_cnt(Integer past_systolic_cnt) {
		this.past_systolic_cnt = past_systolic_cnt;
	}

	public BigDecimal getAdv_pmt_amt() {
		return adv_pmt_amt;
	}

	public void setAdv_pmt_amt(BigDecimal adv_pmt_amt) {
		this.adv_pmt_amt = adv_pmt_amt;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

