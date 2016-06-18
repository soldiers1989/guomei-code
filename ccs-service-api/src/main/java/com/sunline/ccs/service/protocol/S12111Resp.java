package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 账单日列表查询响应
 * 
* @author fanghj
 * @date 2013-6-4  上午11:10:51
 * @version 1.0
 */
public class S12111Resp implements Serializable{

	private static final long serialVersionUID = -8114004267640144369L;

	/*
	 * 账单日天数
	 */
	public ArrayList<S12111Item> items;

	public ArrayList<S12111Item> getItems() {
		return items;
	}

	public void setItems(ArrayList<S12111Item> items) {
		this.items = items;
	}
	
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
