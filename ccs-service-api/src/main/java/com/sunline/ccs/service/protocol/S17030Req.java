package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class S17030Req implements Serializable {

	private static final long serialVersionUID = -6836692687092617079L;

	/**
     * 查询方式
     */
    public String opt ;

    /**
     * 礼品编号
     */
    public String item_id ;

	/**
     * 兑换积分下限
     */
    public Integer min_bonus ;

    /**
     * 兑换积分上限
     */
    public Integer max_bonus ;

    /**
     * 开始位置
     */
    public Integer firstrow ;

    /**
     * 结束位置
     */
    public Integer lastrow ;

	public String getOpt() {
		return opt;
	}

	public void setOpt(String opt) {
		this.opt = opt;
	}

	public String getItem_id() {
		return item_id;
	}

	public void setItem_id(String item_id) {
		this.item_id = item_id;
	}

	public Integer getMin_bonus() {
		return min_bonus;
	}

	public void setMin_bonus(Integer min_bonus) {
		this.min_bonus = min_bonus;
	}

	public Integer getMax_bonus() {
		return max_bonus;
	}

	public void setMax_bonus(Integer max_bonus) {
		this.max_bonus = max_bonus;
	}

	public Integer getFirstrow() {
		BigDecimal b = new BigDecimal("0");
		b.setScale(2);
		return firstrow;
	}

	public void setFirstrow(Integer firstrow) {
		this.firstrow = firstrow;
	}

	public Integer getLastrow() {
		return lastrow;
	}

	public void setLastrow(Integer lastrow) {
		this.lastrow = lastrow;
	}
    
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

