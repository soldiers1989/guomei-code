package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanType;

/**
 *
* @author fanghj
 *@time 2014-3-31 下午11:36:45
 */
public class S20026LoanReg implements Serializable{

	private static final long serialVersionUID = 894709603074771161L;

    /**
     * 贷款申请顺序号
     */
    public Long register_id;

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
     * 贷款注册状态
     */
    public LoanRegStatus loan_reg_status;

    /**
     * 贷款总期数
     */
    public Integer loan_init_term;

    /**
     * 贷款总本金
     */
    public BigDecimal loan_init_prin;

    /**
     * 贷款总手续费
     */
    public BigDecimal loan_init_fee1;

    /**
     * 贷款手续费收取方式
     */
    public LoanFeeMethod loan_fee_method;

    /**
     * 贷款产品代码
     */
    public String loan_code;

    /**
     * 贷款交易行动码
     */
    public LoanAction loan_action;

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
     * 提前还款金额
     */
    public BigDecimal adv_pmt_amt;

    /**
     * 生效日期
     */
    public Date valid_date;

    /**
     * 展期前总期数
     */
    public Integer bef_resch_init_term;

    /**
     * 展期前总本金
     */
    public BigDecimal bef_resch_init_prin;

    /**
     * 展期期数
     */
    public Integer reschedule_term;

    /**
     * 缩期前总期数
     */
    public Integer bef_shorted_init_term;

    /**
     * 缩期前总本金
     */
    public BigDecimal bef_shorted_init_prin;

    /**
     * 缩期方式
     */
    public String shorted_resc_type;

    /**
     * 缩期期数
     */
    public Integer shorted_term;

    /**
     * 缩期还款金额
     */
    public BigDecimal shorted_pmt_due;

    /**
     * 备注
     */
    public String remark;

	public Long getRegister_id() {
		return register_id;
	}

	public void setRegister_id(Long register_id) {
		this.register_id = register_id;
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

	public LoanRegStatus getLoan_reg_status() {
		return loan_reg_status;
	}

	public void setLoan_reg_status(LoanRegStatus loan_reg_status) {
		this.loan_reg_status = loan_reg_status;
	}

	public Integer getLoan_init_term() {
		return loan_init_term;
	}

	public void setLoan_init_term(Integer loan_init_term) {
		this.loan_init_term = loan_init_term;
	}

	public BigDecimal getLoan_init_prin() {
		return loan_init_prin;
	}

	public void setLoan_init_prin(BigDecimal loan_init_prin) {
		this.loan_init_prin = loan_init_prin;
	}

	public BigDecimal getLoan_init_fee1() {
		return loan_init_fee1;
	}

	public void setLoan_init_fee1(BigDecimal loan_init_fee1) {
		this.loan_init_fee1 = loan_init_fee1;
	}

	public LoanFeeMethod getLoan_fee_method() {
		return loan_fee_method;
	}

	public void setLoan_fee_method(LoanFeeMethod loan_fee_method) {
		this.loan_fee_method = loan_fee_method;
	}

	public String getLoan_code() {
		return loan_code;
	}

	public void setLoan_code(String loan_code) {
		this.loan_code = loan_code;
	}

	public LoanAction getLoan_action() {
		return loan_action;
	}

	public void setLoan_action(LoanAction loan_action) {
		this.loan_action = loan_action;
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

	public BigDecimal getAdv_pmt_amt() {
		return adv_pmt_amt;
	}

	public void setAdv_pmt_amt(BigDecimal adv_pmt_amt) {
		this.adv_pmt_amt = adv_pmt_amt;
	}

	public Date getValid_date() {
		return valid_date;
	}

	public void setValid_date(Date valid_date) {
		this.valid_date = valid_date;
	}

	public Integer getBef_resch_init_term() {
		return bef_resch_init_term;
	}

	public void setBef_resch_init_term(Integer bef_resch_init_term) {
		this.bef_resch_init_term = bef_resch_init_term;
	}

	public BigDecimal getBef_resch_init_prin() {
		return bef_resch_init_prin;
	}

	public void setBef_resch_init_prin(BigDecimal bef_resch_init_prin) {
		this.bef_resch_init_prin = bef_resch_init_prin;
	}

	public Integer getReschedule_term() {
		return reschedule_term;
	}

	public void setReschedule_term(Integer reschedule_term) {
		this.reschedule_term = reschedule_term;
	}

	public Integer getBef_shorted_init_term() {
		return bef_shorted_init_term;
	}

	public void setBef_shorted_init_term(Integer bef_shorted_init_term) {
		this.bef_shorted_init_term = bef_shorted_init_term;
	}

	public BigDecimal getBef_shorted_init_prin() {
		return bef_shorted_init_prin;
	}

	public void setBef_shorted_init_prin(BigDecimal bef_shorted_init_prin) {
		this.bef_shorted_init_prin = bef_shorted_init_prin;
	}

	public String getShorted_resc_type() {
		return shorted_resc_type;
	}

	public void setShorted_resc_type(String shorted_resc_type) {
		this.shorted_resc_type = shorted_resc_type;
	}

	public Integer getShorted_term() {
		return shorted_term;
	}

	public void setShorted_term(Integer shorted_term) {
		this.shorted_term = shorted_term;
	}

	public BigDecimal getShorted_pmt_due() {
		return shorted_pmt_due;
	}

	public void setShorted_pmt_due(BigDecimal shorted_pmt_due) {
		this.shorted_pmt_due = shorted_pmt_due;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
    
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
