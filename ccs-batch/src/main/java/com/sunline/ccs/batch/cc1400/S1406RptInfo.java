package com.sunline.ccs.batch.cc1400;

import com.sunline.ppy.dictionary.report.ccs.MsxfMerchantMatchErrRpt;
import com.sunline.ppy.dictionary.report.ccs.MsxfMerchantMatchSuccRpt;

public class S1406RptInfo {
	public MsxfMerchantMatchErrRpt getMsxfMerchantMatchErrRpt() {
		return msxfMerchantMatchErrRpt;
	}
	public void setMsxfMerchantMatchErrRpt(
			MsxfMerchantMatchErrRpt msxfMerchantMatchErrRpt) {
		this.msxfMerchantMatchErrRpt = msxfMerchantMatchErrRpt;
	}
	public MsxfMerchantMatchSuccRpt getMsxfMerchantMatchSuccRpt() {
		return msxfMerchantMatchSuccRpt;
	}
	public void setMsxfMerchantMatchSuccRpt(
			MsxfMerchantMatchSuccRpt msxfMerchantMatchSuccRpt) {
		this.msxfMerchantMatchSuccRpt = msxfMerchantMatchSuccRpt;
	}
	private MsxfMerchantMatchErrRpt msxfMerchantMatchErrRpt;
	private MsxfMerchantMatchSuccRpt msxfMerchantMatchSuccRpt;
}
