package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.Indicator;

/**
 * 积分兑换明细查询
 * 
* @author fanghj
 * @date 2013-4-20 上午11:20:18
 * @version 1.0
 */
public class S17012Resp implements Serializable {

	private static final long serialVersionUID = -7698094077571767467L;
	
	/**
	 * 卡号
	 */
	public String card_no;

	/**
	 * 起始日期
	 */
	public Date start_date;

	/**
	 * 截止日期
	 */
	public Date end_date;

	/**
	 * 开始位置
	 */
	public Integer firstrow;

	/**
	 * 结束位置
	 */
	public Integer lastrow;
	
	/**
	 * 是否有下页标志
	 */
	public Indicator nextpage_flg;
	
	public ArrayList<S17012Exch> exchs;
	
	public Integer total_rows;

	public Integer getTotal_rows() {
			return total_rows;
		}

		public void setTotal_rows(Integer total_rows) {
			this.total_rows = total_rows;
		}

	public ArrayList<S17012Exch> getExchs() {
		return exchs;
	}

	public void setExchs(ArrayList<S17012Exch> exchs) {
		this.exchs = exchs;
	}

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
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

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
