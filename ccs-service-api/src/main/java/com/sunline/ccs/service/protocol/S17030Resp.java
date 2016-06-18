package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.Indicator;

public class S17030Resp implements Serializable {

	private static final long serialVersionUID = 5248280126279132618L;
	/**
	 * 兑换积分下限
	 */
	public Integer min_bonus;

	/**
	 * 兑换积分上限
	 */
	public Integer max_bonus;
	/**
	 * 开始位置
	 */
	public Integer firstrow;

	/**
	 * 结束位置
	 */
	public Integer lastrow;

	/**
	 * 是否有下一页
	 */
	public Indicator nextpage_flg;

	public ArrayList<S17030Item> items;

	public Integer total_rows;

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

	public Integer getTotal_rows() {
		return total_rows;
	}

	public void setTotal_rows(Integer total_rows) {
		this.total_rows = total_rows;
	}

	public Integer getFirstrow() {
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

	public Indicator getNextpage_flg() {
		return nextpage_flg;
	}

	public void setNextpage_flg(Indicator nextpage_flg) {
		this.nextpage_flg = nextpage_flg;
	}

	public ArrayList<S17030Item> getItems() {
		return items;
	}

	public void setItems(ArrayList<S17030Item> items) {
		this.items = items;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
