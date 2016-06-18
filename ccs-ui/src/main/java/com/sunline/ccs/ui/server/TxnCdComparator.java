/**
 * 
 */
package com.sunline.ccs.ui.server;

import java.util.Comparator;

import com.sunline.ccs.param.def.TxnCd;

/**
 * 交易码的比较类
* @author fanghj
 *
 */
public class TxnCdComparator implements Comparator<TxnCd> {
	@Override
	public int compare(TxnCd o1, TxnCd o2) {
		return o1.txnCd.compareTo(o2.txnCd);
	}
}
