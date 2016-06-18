package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.IdType;

/**
 *按照证件类型和证件号码查询贷款
* @author fanghj
 *@time 2014-3-31 下午5:38:41
 */
public class S20022Req implements Serializable{

	private static final long serialVersionUID = -9213213788910545296L;
	
	/**
	 * 证件类型
	 */
	public IdType id_type;
	
	/**
	 * 证件号码
	 */
	public String id_no;

	public IdType getId_type() {
		return id_type;
	}

	public void setId_type(IdType id_type) {
		this.id_type = id_type;
	}

	public String getId_no() {
		return id_no;
	}

	public void setId_no(String id_no) {
		this.id_no = id_no;
	}
	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
