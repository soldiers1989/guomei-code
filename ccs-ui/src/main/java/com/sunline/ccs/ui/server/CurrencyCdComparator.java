/**
 * 
 */
package com.sunline.ccs.ui.server;

import java.util.Comparator;

import com.sunline.pcm.param.def.CurrencyCd;

/**
 * 币种代码的比较类
* @author fanghj
 *
 */
public class CurrencyCdComparator implements Comparator<CurrencyCd> {
	@Override
	public int compare(CurrencyCd o1, CurrencyCd o2) {
		return o1.currencyCd.compareTo(o2.currencyCd);
	}
}
