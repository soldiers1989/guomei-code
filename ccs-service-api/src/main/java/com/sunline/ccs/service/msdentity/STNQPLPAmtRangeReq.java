package com.sunline.ccs.service.msdentity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.service.msentity.MsRequestInfo;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 获取产品期数、金额范围请求接口类
 * @author zqx
 *
 */
public class STNQPLPAmtRangeReq extends MsRequestInfo implements Serializable {
	public static final long serialVersionUID = 1L;
	
	/**
	 * 姓名
	 */
	@Check(lengths=4,notEmpty=true)
	@JsonProperty(value="LOAN_CODE")
	public String  loanCode;

	public String getLoanCode() {
		return loanCode;
	}

	public void setLoanCode(String loanCode) {
		this.loanCode = loanCode;
	}

}
