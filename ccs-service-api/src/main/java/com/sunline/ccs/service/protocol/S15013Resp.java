package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.Indicator;

public class S15013Resp implements Serializable {


   /**
	 * 
	 */
	private static final long serialVersionUID = -1962953140223304169L;

/**
     * 开始位置
     */
    public Integer firstrow ;

    /**
     * 结束位置
     */
    public Integer lastrow ;
    
    /**
     * 是否有下一页
     */
    public Indicator nextpage_flg;
    /**
     * 卡号
     */
    public String card_no ;

    /**
     * 币种
     */
    public String curr_cd ;
    
    public ArrayList<S15013Item> items ;
    
    public Integer total_rows;

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

	public ArrayList<S15013Item> getItems() {
		return items;
	}

	public void setItems(ArrayList<S15013Item> items) {
		this.items = items;
	}
    
	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	public String getCurr_cd() {
		return curr_cd;
	}

	public void setCurr_cd(String curr_cd) {
		this.curr_cd = curr_cd;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}

}

