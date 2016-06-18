package com.sunline.ccs.service.msdentity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.service.msentity.MsRequestInfo;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 客户合同列表查询接口申请报文
 * @author zqx
 *
 */
public class STNQAAcctsbyCustUUIDReq extends MsRequestInfo implements Serializable {
	public static final long serialVersionUID = 1L;
	
	/**
	 * 用户中心公用客户id
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="UUID")
	public String  internalCustomerId;
	/**
	 * 查询范围：A所有合同、E所有有效合同
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="QUERY_RANGE_IND")
	public String  queryRangeInd;

	public String getInternalCustomerId() {
		return internalCustomerId;
	}

	public void setInternalCustomerId(String internalCustomerId) {
		this.internalCustomerId = internalCustomerId;
	}

	public String getQueryRangeInd() {
		return queryRangeInd;
	}

	public void setQueryRangeInd(String queryRangeInd) {
		this.queryRangeInd = queryRangeInd;
	}
	
}
