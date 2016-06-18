package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class S20040Req implements Serializable {

	private static final long serialVersionUID = 3625728474919048899L;

	/**
     * 贷款卡号
     */
    public String card_no;

    /**
     * 操作类型
     */
    public String opt;

    /**
     * 浮动比例
     */
    public BigDecimal float_rate;

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	public String getOpt() {
		return opt;
	}

	public void setOpt(String opt) {
		this.opt = opt;
	}

	public BigDecimal getFloat_rate() {
		return float_rate;
	}

	public void setFloat_rate(BigDecimal float_rate) {
		this.float_rate = float_rate;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

