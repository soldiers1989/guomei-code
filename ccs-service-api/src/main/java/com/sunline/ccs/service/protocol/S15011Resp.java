package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.Indicator;

/**
 * 永久额度调整（直接调整，仅限于内管使用）响应接口
 * 
* @author fanghj
 * @date 2013-6-5  上午11:08:31
 * @version 1.0
 */
public class S15011Resp implements Serializable {

	private static final long serialVersionUID = 1868427198754369828L;

	/**
     * 卡号
     */
    public String card_no ;

    /**
     * 币种
     */
    public String curr_cd ;

    /**
     * 调整前信用额度
     */
    public BigDecimal prev_credit_limit ;

    /**
     *信用额度
     */
    public BigDecimal credit_limit ;

    /**
     * 是否存在外币标识
     */
    public Indicator dual_curr_ind ;

    /**
     * 外币币种
     */
    public String dual_curr_cd ;

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	public String getCurr_cd() {
		return curr_cd;
	}

	public void setCurr_cd(String curr_cd) {
		this.curr_cd = curr_cd;
	}

	public BigDecimal getPrev_credit_limit() {
		return prev_credit_limit;
	}

	public void setPrev_credit_limit(BigDecimal prev_credit_limit) {
		this.prev_credit_limit = prev_credit_limit;
	}

	public BigDecimal getCredit_limit() {
		return credit_limit;
	}

	public void setCredit_limit(BigDecimal credit_limit) {
		this.credit_limit = credit_limit;
	}

	public Indicator getDual_curr_ind() {
		return dual_curr_ind;
	}

	public void setDual_curr_ind(Indicator dual_curr_ind) {
		this.dual_curr_ind = dual_curr_ind;
	}

	public String getDual_curr_cd() {
		return dual_curr_cd;
	}

	public void setDual_curr_cd(String dual_curr_cd) {
		this.dual_curr_cd = dual_curr_cd;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

