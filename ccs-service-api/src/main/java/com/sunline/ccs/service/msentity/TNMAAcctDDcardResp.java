package com.sunline.ccs.service.msentity;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 客户银行卡信息修改接口返回报文
 * @author jjb
 *
 */
public class TNMAAcctDDcardResp extends MsResponseInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 客户号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="UUID")
	public String uuid;
	
	/**
	 * 合同列表
	 */
	@Check(lengths=22,notEmpty=true)
	@JsonProperty(value="CONTRA_LIST")
	public List<TNMAAcctDDcardRespSubContrNbr> contractList;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public List<TNMAAcctDDcardRespSubContrNbr> getContractList() {
		return contractList;
	}

	public void setContractList(List<TNMAAcctDDcardRespSubContrNbr> contractList) {
		this.contractList = contractList;
	}


}
