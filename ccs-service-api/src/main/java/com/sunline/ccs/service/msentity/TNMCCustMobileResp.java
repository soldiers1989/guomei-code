package com.sunline.ccs.service.msentity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;


/**
 * 客户银行卡信息修改接口返回报文
 * @author jjb
 *
 */
public class TNMCCustMobileResp extends MsResponseInfo  implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -797147533002975784L;

	/**
	 * 客户号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="UUID")
	public String uuid;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
}
