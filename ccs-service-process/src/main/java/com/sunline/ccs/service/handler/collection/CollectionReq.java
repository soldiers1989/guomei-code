package com.sunline.ccs.service.handler.collection;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 催收还款
 * @author zhengjf
 *
 */
public class CollectionReq implements Serializable {

	private static final long serialVersionUID = 1L;
	/**
	 * 客户unique_id
	 */
	@JsonProperty(value="customerCode")
	private String unique_id;
	
	/**
	 * 合同号
	 */
	@JsonProperty(value="contractCode")
	private String contr_nbr;
	
	/**
	 * CPD值（逾期天数）
	 */
	@JsonProperty(value="overduePeriod")
	private int overdue_preiod;
	
	/**
	 * 合同全部应还款额
	 */
	@JsonProperty(value="overdueTotalAmount")
	private BigDecimal overdueTotalAmount;
	
	/**
	 * 欠款总额
	 */
	@JsonProperty(value="totalRemainingAmount")
	private BigDecimal totalRemainingAmount;
	
	/**
	 * 合同总罚金
	 */
	@JsonProperty(value="penalty")
	private BigDecimal penalty;

	public String getUnique_id() {
		return unique_id;
	}

	public void setUnique_id(String unique_id) {
		this.unique_id = unique_id;
	}

	public String getContr_nbr() {
		return contr_nbr;
	}

	public void setContr_nbr(String contr_nbr) {
		this.contr_nbr = contr_nbr;
	}

	public int getOverdue_preiod() {
		return overdue_preiod;
	}

	public void setOverdue_preiod(int overdue_preiod) {
		this.overdue_preiod = overdue_preiod;
	}

	public BigDecimal getOverdueTotalAmount() {
		return overdueTotalAmount;
	}

	public void setOverdueTotalAmount(BigDecimal overdueTotalAmount) {
		this.overdueTotalAmount = overdueTotalAmount;
	}

	public BigDecimal getTotalRemainingAmount() {
		return totalRemainingAmount;
	}

	public void setTotalRemainingAmount(BigDecimal totalRemainingAmount) {
		this.totalRemainingAmount = totalRemainingAmount;
	}

	public BigDecimal getPenalty() {
		return penalty;
	}

	public void setPenalty(BigDecimal penalty) {
		this.penalty = penalty;
	}
	
	
}
