package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class S13110Resp implements Serializable {

    /**  
	 * serialVersionUID:TODO（用一句话描述这个变量表示什么）  
	 * @since 1.0.0  
	*/  
	    
	private static final long serialVersionUID = 90437164068305181L;

	/**
     * 卡号
     */
    public String card_no ;

    /**
     * 币种
     */
    public String curr_cd ;

    /**
     * 账单日期
     */
    public String stmt_date ;

    public ArrayList<S13110Term> terms;

	public ArrayList<S13110Term> getTerms() {
		return terms;
	}

	public void setTerms(ArrayList<S13110Term> trems) {
		this.terms = trems;
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

	public String getStmt_date() {
		return stmt_date;
	}

	public void setStmt_date(String stmt_date) {
		this.stmt_date = stmt_date;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}


}

