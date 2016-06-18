package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.IdType;
import com.sunline.ppy.dictionary.enums.Indicator;

public class S14000Resp implements Serializable {

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
	public IdType id_type;
	/**
	 * 账单年月
	 */
	public String id_no;
	
	public ArrayList<S14000Card> cards;
	
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

	public IdType getId_type() {
		return id_type;
	}

	public String getId_no() {
		return id_no;
	}

	public ArrayList<S14000Card> getCards() {
		return cards;
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

	public void setId_type(IdType id_type) {
		this.id_type = id_type;
	}

	public void setId_no(String id_no) {
		this.id_no = id_no;
	}

	public void setCards(ArrayList<S14000Card> cards) {
		this.cards = cards;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
