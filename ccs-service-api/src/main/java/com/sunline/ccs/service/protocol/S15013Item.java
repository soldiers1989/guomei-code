package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class S15013Item implements Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = 6548029869254377657L;
	/**
     * 操作时间
     */
    public Date oper_time ;
	/**
     * 操作员ID
     */
    public String opera_id ;
	/**
     * 原始信用额度
     */
    public BigDecimal credit_limit_old ;
	/**
     * 新信用额度
     */
    public BigDecimal credit_limit_new ;
	/**
     * 调整状态
     */
    public String rtf_state ;

    

	public Date getOper_time() {
		return oper_time;
	}



	public void setOper_time(Date oper_time) {
		this.oper_time = oper_time;
	}


	public BigDecimal getCredit_limit_old() {
		return credit_limit_old;
	}



	public void setCredit_limit_old(BigDecimal credit_limit_old) {
		this.credit_limit_old = credit_limit_old;
	}



	public BigDecimal getCredit_limit_new() {
		return credit_limit_new;
	}



	public void setCredit_limit_new(BigDecimal credit_limit_new) {
		this.credit_limit_new = credit_limit_new;
	}



	public String getOpera_id() {
		return opera_id;
	}



	public void setOpera_id(String opera_id) {
		this.opera_id = opera_id;
	}


	public String getRtf_state() {
		return rtf_state;
	}



	public void setRtf_state(String rtf_state) {
		this.rtf_state = rtf_state;
	}



	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
