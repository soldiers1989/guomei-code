package com.sunline.ccs.batch.cc2100;

import com.sunline.ppy.dictionary.report.ccs.TranAdjLogRptItem;
import com.sunline.ccs.batch.common.TxnPrepareSet;

/**
 * 
 * @see 类名：S2101TxnAdjImp
 * @see 描述：调账批量入账输出对象
 *
 * @see 创建日期：   2015-6-23下午7:28:06
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class S2101TxnAdjImp extends TxnPrepareSet{

	/**
	 * 调账日志报表
	 */
	private TranAdjLogRptItem tranAdjLogRptItem;

	public TranAdjLogRptItem getTranAdjLogRptItem() {
		return tranAdjLogRptItem;
	}

	public void setTranAdjLogRptItem(TranAdjLogRptItem tranAdjLogRptItem) {
		this.tranAdjLogRptItem = tranAdjLogRptItem;
	}
	
}
