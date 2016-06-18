package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.DerateReason;

/**
 * 已收年费减免返回接口
 * 
* @author fanghj
 * @date 2013-6-5  上午10:18:13
 * @version 1.0
 */
public class S12041Resp implements Serializable {

	private static final long serialVersionUID = 4326678129571051323L;

	/**
     * 卡号
     */
    public String card_no ;

    /**
     * 年费收取日期
     */
    public Date card_fee_date ;

    /**
     * 年费收取金额
     */
    public BigDecimal card_fee_amt ;

    /**
     *减免金额
     */
    public BigDecimal derate_card_fee_amt;

    /**
     * 下次年费收取日期
     */
    public Date next_card_fee_date ;

    /**
     * 减免原因
     */
    public DerateReason derate_reason ;

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	public Date getCard_fee_date() {
		return card_fee_date;
	}

	public void setCard_fee_date(Date card_fee_date) {
		this.card_fee_date = card_fee_date;
	}

	public BigDecimal getCard_fee_amt() {
		return card_fee_amt;
	}

	public void setCard_fee_amt(BigDecimal card_fee_amt) {
		this.card_fee_amt = card_fee_amt;
	}

	public BigDecimal getDerate_card_fee_amt() {
		return derate_card_fee_amt;
	}

	public void setDerate_card_fee_amt(BigDecimal derate_card_fee_amt) {
		this.derate_card_fee_amt = derate_card_fee_amt;
	}

	public Date getNext_card_fee_date() {
		return next_card_fee_date;
	}

	public void setNext_card_fee_date(Date next_card_fee_date) {
		this.next_card_fee_date = next_card_fee_date;
	}

	public DerateReason getDerate_reason() {
		return derate_reason;
	}

	public void setDerate_reason(DerateReason derate_reason) {
		this.derate_reason = derate_reason;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

