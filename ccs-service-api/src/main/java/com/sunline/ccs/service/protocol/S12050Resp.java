package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.Indicator;

public class S12050Resp implements Serializable {

	private static final long serialVersionUID = 1L;
	
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
	 * 总数
	 */
	public Integer total_rows;
	
	public ArrayList<S12050Stmt> stmts;
	
	
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

	public ArrayList<S12050Stmt> getStmts() {
		return stmts;
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

	public void setStmts(ArrayList<S12050Stmt> stmts) {
		this.stmts = stmts;
	}
	
	public Integer getTotal_rows() {
		return total_rows;
	}

	public void setTotal_rows(Integer total_rows) {
		this.total_rows = total_rows;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
	
}
