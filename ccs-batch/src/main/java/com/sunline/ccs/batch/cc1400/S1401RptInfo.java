package com.sunline.ccs.batch.cc1400;

import com.sunline.ppy.dictionary.report.ccs.MsxfMatchErrRpt;
import com.sunline.ppy.dictionary.report.ccs.MsxfMatchSuccRpt;

public class S1401RptInfo {
	private MsxfMatchErrRpt msxfTransMatchErrRpt;
	private MsxfMatchSuccRpt msxfTransMatchSucRpt;
	public MsxfMatchErrRpt getMsxfTransMatchErrRpt() {
		return msxfTransMatchErrRpt;
	}
	public void setMsxfTransMatchErrRpt(MsxfMatchErrRpt msxfTransMatchErrRpt) {
		this.msxfTransMatchErrRpt = msxfTransMatchErrRpt;
	}
	public MsxfMatchSuccRpt getMsxfTransMatchSucRpt() {
		return msxfTransMatchSucRpt;
	}
	public void setMsxfTransMatchSucRpt(MsxfMatchSuccRpt msxfTransMatchSucRpt) {
		this.msxfTransMatchSucRpt = msxfTransMatchSucRpt;
	}
	
	
}
