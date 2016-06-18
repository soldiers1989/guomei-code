package com.sunline.ccs.service.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 放款重提接口接受报文
 * @author jjb
 *
 */
public class S30002RecommitReq extends SunshineRequestInfo implements Serializable{
	private static final long serialVersionUID = 1L;
	
	/**
	 * 保单号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="GUARANTYID")
	private String guarantyid;

	public String getGuarantyid() {
		return guarantyid;
	}

	public void setGuarantyid(String guarantyid) {
		this.guarantyid = guarantyid;
	}

}
