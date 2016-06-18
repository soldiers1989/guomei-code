package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.service.msentity.MsResponseInfo;

/**
 * 客户合同列表查询接口返回报文
 * @author zqx
 *
 */
public class STNQAAcctsbyCustUUIDResp extends MsResponseInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 保单号
	 */
	@JsonProperty(value="UUID")
	public String  internalCustomerId;

	@JsonProperty(value="CONTRACTS")
	private List<STNQAAcctsbyCustUUIDRESPSubContract> contracts ;


	public String getInternalCustomerId() {
		return internalCustomerId;
	}

	public void setInternalCustomerId(String internalCustomerId) {
		this.internalCustomerId = internalCustomerId;
	}

	public List<STNQAAcctsbyCustUUIDRESPSubContract> getContracts() {
		return contracts;
	}

	public void setContracts(List<STNQAAcctsbyCustUUIDRESPSubContract> contracts) {
		this.contracts = contracts;
	}


}
