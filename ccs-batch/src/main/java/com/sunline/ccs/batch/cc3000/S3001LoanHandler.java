package com.sunline.ccs.batch.cc3000;

import java.util.ArrayList;
import java.util.List;

import com.sunline.ppy.dictionary.report.ccs.LoanSuccessRptItem;
//import com.sunline.smsd.service.sdk.LoanRescheduleMsgItem;


/**
 * 分期预处理输出对象
 * 
* @author fanghj
 *
 */
public class S3001LoanHandler {

	/**
	 * 分期注册成功报表文件
	 */
	private List<LoanSuccessRptItem> loanSuccessRpt = new ArrayList<LoanSuccessRptItem>();

	public List<LoanSuccessRptItem> getLoanSuccessRpt() {
		return loanSuccessRpt;
	}

	public void setLoanSuccessRpt(List<LoanSuccessRptItem> loanSuccessRpt) {
		this.loanSuccessRpt = loanSuccessRpt;
	}
	
	/**
	 * 展期缩期批量短信文件
	 */
//	private LoanRescheduleMsgItem rescheduleMsg;
	
	


/*	public LoanRescheduleMsgItem getRescheduleMsg() {
		return rescheduleMsg;
	}

	public void setRescheduleMsg(LoanRescheduleMsgItem rescheduleMsg) {
		this.rescheduleMsg = rescheduleMsg;
	}*/
	
}
