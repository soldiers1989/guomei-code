package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.Indicator;

public class S12011Resp implements Serializable {

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
	 * 账单年月
	 */
	public String stmt_date;
	/**
	 * 币种
	 */
	public String curr_cd;
	
	public ArrayList<S12011Txn> txns;
	
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

	public String getStmt_date() {
		return stmt_date;
	}

	public String getCurr_cd() {
		return curr_cd;
	}

	public ArrayList<S12011Txn> getTxns() {
		return txns;
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

	public void setStmt_date(String stmt_date) {
		this.stmt_date = stmt_date;
	}

	public void setCurr_cd(String curr_cd) {
		this.curr_cd = curr_cd;
	}

	public void setTxns(ArrayList<S12011Txn> txns) {
		this.txns = txns;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
}
