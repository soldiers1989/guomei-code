package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>分期申请状态查询接口</p>
* @author fanghj
 *
 */
@SuppressWarnings("serial")
public class S13085Req implements Serializable{
	
	/**
	 * 卡号
	 */
	public String card_no;
	
	/**
	 * 分期类型
	 */
	public String loan_type;
	
	/**
	 * 起始日期
	 */
	public Date start_date;
	
	/**
	 * 截止日期
	 */
	public Date end_date;
	
	/**
	 * 页大小
	 */
	public Long pagesize;
	/**
	 * 最后一条记录键值
	 */
	public Long last_row_key;
	
	/**
	 * 最后一条记录类型
	 */
	public String last_row_type;

	/**
	 * 预留域
	 */
	public String reserved;

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	public String getLoan_type() {
		return loan_type;
	}

	public void setLoan_type(String loan_type) {
		this.loan_type = loan_type;
	}

	public Date getStart_date() {
		return start_date;
	}

	public void setStart_date(Date start_date) {
		this.start_date = start_date;
	}

	public Date getEnd_date() {
		return end_date;
	}

	public void setEnd_date(Date end_date) {
		this.end_date = end_date;
	}

	public Long getPagesize() {
		return pagesize;
	}

	public void setPagesize(Long pagesize) {
		this.pagesize = pagesize;
	}

	public Long getLast_row_key() {
		return last_row_key;
	}

	public void setLast_row_key(Long last_row_key) {
		this.last_row_key = last_row_key;
	}

	public String getLast_row_type() {
		return last_row_type;
	}

	public void setLast_row_type(String last_row_type) {
		this.last_row_type = last_row_type;
	}

	public String getReserved() {
		return reserved;
	}

	public void setReserved(String reserved) {
		this.reserved = reserved;
	}

}
