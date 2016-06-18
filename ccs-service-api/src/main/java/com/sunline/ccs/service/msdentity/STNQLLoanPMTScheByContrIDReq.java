package com.sunline.ccs.service.msdentity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.service.msentity.MsRequestInfo;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 客户合同详情查询接口申请报文
 * @author zqx
 *
 */
public class STNQLLoanPMTScheByContrIDReq extends MsRequestInfo implements Serializable {
	public static final long serialVersionUID = 1L;
	
	/**
	 * 合同号
	 */
	@Check(lengths=22,notEmpty=true)
	@JsonProperty(value="CONTRA_NBR")
	public String  contraNbr;

	public String getContraNbr() {
		return contraNbr;
	}

	public void setContraNbr(String contraNbr) {
		this.contraNbr = contraNbr;
	}
	
}
