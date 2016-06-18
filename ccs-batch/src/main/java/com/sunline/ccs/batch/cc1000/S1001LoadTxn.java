package com.sunline.ccs.batch.cc1000;

import com.sunline.ppy.dictionary.exchange.FmInterfaceItem;
import com.sunline.ppy.dictionary.report.ccs.UnmatchedLoanReturnRptItem;


/**
 * @see 类名：S1001LoadTxn
 * @see 描述：交易单转双输出对象
 *
 * @see 创建日期：   2015-6-23下午7:18:32
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class S1001LoadTxn {

	private FmInterfaceItem tpsInterface;
	private UnmatchedLoanReturnRptItem unmatchedLoanReturnRpt;
	

	public UnmatchedLoanReturnRptItem getUnmatchedLoanReturnRpt() {
		return unmatchedLoanReturnRpt;
	}
	public void setUnmatchedLoanReturnRpt(
			UnmatchedLoanReturnRptItem unmatchedLoanReturnRpt) {
		this.unmatchedLoanReturnRpt = unmatchedLoanReturnRpt;
	}
	public FmInterfaceItem getTpsInterface() {
		return tpsInterface;
	}
	public void setTpsInterface(FmInterfaceItem tpsInterface) {
		this.tpsInterface = tpsInterface;
	}
	
}
