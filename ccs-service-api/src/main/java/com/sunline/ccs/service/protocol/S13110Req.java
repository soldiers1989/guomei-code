package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class S13110Req implements Serializable {

    /**  
	 * serialVersionUID:TODO（用一句话描述这个变量表示什么）  
	 * @since 1.0.0  
	*/  
	    
	private static final long serialVersionUID = -8733222624271921203L;

	/**
     * 卡号
     */
    public String card_no ;

    /**
     * 币种
     */
    public String curr_cd ;

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

