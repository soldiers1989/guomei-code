package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.service.msentity.MsResponseInfo;
/**
 * 可用子产品列表还款详情接口返回报文
 * @author zhengjf
 *
 */
public class STNTLLoanUsableCalcResp extends MsResponseInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	 /**
	 * 还款详情列表
	 */
	@JsonProperty(value="LOAN_LIST")
	private List<STNTLLoanUsableCalcRespSubLoan> loanList;

	public List<STNTLLoanUsableCalcRespSubLoan> getLoanList() {
		return loanList;
	}

	public void setLoanList(List<STNTLLoanUsableCalcRespSubLoan> loanList) {
		this.loanList = loanList;
	}

}
