package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.IdType;

public class S20020Req implements Serializable {

	private static final long serialVersionUID = 470664670926287572L;

	/**
     * 贷款卡号
     */
    public String card_no;

    /**
     * 证件类型
     */
    public IdType id_type;

    /**
     * 证件号码
     */
    public String id_no;

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

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

