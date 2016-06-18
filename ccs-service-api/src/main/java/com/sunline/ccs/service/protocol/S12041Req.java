package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.DerateReason;

/**
 * 已收年费减免请求接口
 * 
* @author fanghj
 * @date 2013-6-5  上午10:18:37
 * @version 1.0
 */
public class S12041Req implements Serializable {

	private static final long serialVersionUID = 7005958994780889200L;

	/**
     * 卡号
     */
    public String card_no ;

    /**
     * 减免金额
     */
    public BigDecimal derate_card_fee_amt ;

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

	public BigDecimal getDerate_card_fee_amt() {
		return derate_card_fee_amt;
	}

	public void setDerate_card_fee_amt(BigDecimal derate_card_fee_amt) {
		this.derate_card_fee_amt = derate_card_fee_amt;
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

