package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.IdType;

public class S14000Req implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 证件类型
	 */
	public IdType id_type;
	/**
	 * 证件号码
	 */
	public String id_no;
	/**
	 * 开始位置
	 */
	public Integer firstrow;
	/**
	 * 结束位置
	 */
	public Integer lastrow;
	
	
	public IdType getId_type() {
		return id_type;
	}
	public String getId_no() {
		return id_no;
	}
	public Integer getFirstrow() {
		return firstrow;
	}
	public Integer getLastrow() {
		return lastrow;
	}
	public void setId_type(IdType id_type) {
		this.id_type = id_type;
	}
	public void setId_no(String id_no) {
		this.id_no = id_no;
	}
	public void setFirstrow(Integer firstrow) {
		this.firstrow = firstrow;
	}
	public void setLastrow(Integer lastrow) {
		this.lastrow = lastrow;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
