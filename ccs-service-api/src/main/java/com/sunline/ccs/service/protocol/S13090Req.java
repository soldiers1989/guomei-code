package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class S13090Req implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
     * 卡号
     */
    public String card_no;
    /**
     * 分期申请顺序号
     */
    public Long register_id;
    /**
     * 操作类型
     */
    public String opt;
    
    
	public String getCard_no() {
		return card_no;
	}
	public String getOpt() {
		return opt;
	}
	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}
	public Long getRegister_id() {
		return register_id;
	}
	public void setRegister_id(Long register_id) {
		this.register_id = register_id;
	}
	public void setOpt(String opt) {
		this.opt = opt;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

