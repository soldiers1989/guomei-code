package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 积分查询
 * 
* @author fanghj
 * @date 2013-4-20 上午11:01:29
 * @version 1.0
 */
public class S17010Resp implements Serializable {

	private static final long serialVersionUID = -1875556636852405091L;

	/**
	 * 客户姓名
	 */
	public String name;

	/**
	 * 期初积分余额
	 */
	public BigDecimal point_begin_bal;

	/**
	 * 当期新增积分
	 */
	public BigDecimal ctd_earned_points;

	/**
	 * 积分余额
	 */
	public BigDecimal point_bal;

	/**
	 * 当期兑换积分
	 */
	public BigDecimal ctd_disb_points;

	/**
	 * 当期调整积分
	 */
	public BigDecimal ctd_adj_points;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getPoint_begin_bal() {
		return point_begin_bal;
	}

	public void setPoint_begin_bal(BigDecimal point_begin_bal) {
		this.point_begin_bal = point_begin_bal;
	}

	public BigDecimal getCtd_earned_points() {
		return ctd_earned_points;
	}

	public void setCtd_earned_points(BigDecimal ctd_earned_points) {
		this.ctd_earned_points = ctd_earned_points;
	}

	public BigDecimal getPoint_bal() {
		return point_bal;
	}

	public void setPoint_bal(BigDecimal point_bal) {
		this.point_bal = point_bal;
	}

	public BigDecimal getCtd_disb_points() {
		return ctd_disb_points;
	}

	public void setCtd_disb_points(BigDecimal ctd_disb_points) {
		this.ctd_disb_points = ctd_disb_points;
	}

	public BigDecimal getCtd_adj_points() {
		return ctd_adj_points;
	}

	public void setCtd_adj_points(BigDecimal ctd_adj_points) {
		this.ctd_adj_points = ctd_adj_points;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
