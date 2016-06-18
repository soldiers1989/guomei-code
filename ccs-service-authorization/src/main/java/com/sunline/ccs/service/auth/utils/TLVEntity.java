package com.sunline.ccs.service.auth.utils;

/**
 * 
 * @see 类名：TLVEntity
 * @see 描述：Tag、Length、Value实体类
 *
 * @see 创建日期：   2015年6月24日下午3:44:52
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class TLVEntity {

	public String tag;

	public int length;

	public String value;

	public TLVEntity(String tag, int length, String value) {
		this.length = length;
		this.tag = tag;
		this.value = value;
	}

	@Override
	public String toString() {
		return "tag=[" + this.tag + "]," + "length=[" + this.length + "]," + "value=[" + this.value + "]";
	}
	
	public String tlv(){
		return tag + String.format("%02d", length) + value;
	}
}
