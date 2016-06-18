package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.Indicator;

public class S13080Resp implements Serializable {
	    
	private static final long serialVersionUID = 4345964771499862067L;

	/**
	 * 是否有下一页标志
	 */
	public Indicator nextpage_flg;
	/**
	 * 开始位置
	 */
	public Integer firstrow;
	/**
	 * 结束位置
	 */
	public Integer lastrow;
	/**
	 * 卡号
	 */
	public String card_no;
	/**
	 * 开始位置
	 */
	public Date start_date;
	/**
	 * 结束位置
	 */
	public Date end_date;
	
	public ArrayList<S13080Loan> loans;
	
	public Integer total_rows;

	public Integer getTotal_rows() {
			return total_rows;
		}

		public void setTotal_rows(Integer total_rows) {
			this.total_rows = total_rows;
		}
	

	public Indicator getNextpage_flg() {
		return nextpage_flg;
	}

	public Integer getFirstrow() {
		return firstrow;
	}

	public Integer getLastrow() {
		return lastrow;
	}

	public String getCard_no() {
		return card_no;
	}

	public Date getStart_date() {
		return start_date;
	}

	public Date getEnd_date() {
		return end_date;
	}

	public ArrayList<S13080Loan> getLoans() {
		return loans;
	}

	public void setNextpage_flg(Indicator nextpage_flg) {
		this.nextpage_flg = nextpage_flg;
	}

	public void setFirstrow(Integer firstrow) {
		this.firstrow = firstrow;
	}

	public void setLastrow(Integer lastrow) {
		this.lastrow = lastrow;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	public void setStart_date(Date start_date) {
		this.start_date = start_date;
	}

	public void setEnd_date(Date end_date) {
		this.end_date = end_date;
	}

	public void setLoans(ArrayList<S13080Loan> loans) {
		this.loans = loans;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

