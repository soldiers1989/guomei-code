package com.sunline.ccs.batch.cc1400;

import com.sunline.ppy.dictionary.report.ccs.MsxfOrderExpiredRpt;
import com.sunline.ppy.dictionary.report.ccs.MsxfOrderHstDaygenRpt;

public class S1404RptInfo {
	/**
	 * 转移历史报表
	 */
	private MsxfOrderHstDaygenRpt msxfOrderHstDaygenRpt;
	/**
	 * 对账失效报表
	 */
	private MsxfOrderExpiredRpt msxfOrderExpiredRpt;
	
	
	public MsxfOrderHstDaygenRpt getMsxfOrderHstDaygenRpt() {
		return msxfOrderHstDaygenRpt;
	}
	public void setMsxfOrderHstDaygenRpt(MsxfOrderHstDaygenRpt msxfOrderHstDaygenRpt) {
		this.msxfOrderHstDaygenRpt = msxfOrderHstDaygenRpt;
	}
	public MsxfOrderExpiredRpt getMsxfOrderExpiredRpt() {
		return msxfOrderExpiredRpt;
	}
	public void setMsxfOrderExpiredRpt(MsxfOrderExpiredRpt msxfOrderExpiredRpt) {
		this.msxfOrderExpiredRpt = msxfOrderExpiredRpt;
	}


	
}
