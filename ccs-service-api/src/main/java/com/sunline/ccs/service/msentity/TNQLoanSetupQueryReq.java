package com.sunline.ccs.service.msentity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 开户查询
 * @author zhengjf
 *
 */
public class TNQLoanSetupQueryReq extends MsRequestInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 申请单编号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="APPLY_NO")
	public String applyNo;
	
	/**
	 * 唯一客户号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="UUID")
	public String uuid;

	public String getApplyNo() {
		return applyNo;
	}

	public void setApplyNo(String applyNo) {
		this.applyNo = applyNo;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
}
