package com.sunline.ccs.batch.common;

import java.util.ArrayList;
import java.util.List;

import com.sunline.ppy.dictionary.exchange.GlTxnItem;
import com.sunline.ppy.dictionary.report.ccs.RptTxnItem;

/**
 * @see 类名：TxnPrepareSet
 * @see 描述：授权交易预处理输出对象
 *
 * @see 创建日期：   2015-6-24下午5:39:24
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class TxnPrepareSet {

	/**
	 * 送报表接口
	 */
	private RptTxnItem rptTxnItem;
	
	/**
	 * 送总账接口 
	 */
	private List<GlTxnItem> glTxnItemList = new ArrayList<GlTxnItem>();

	public RptTxnItem getRptTxnItem() {
		return rptTxnItem;
	}

	public List<GlTxnItem> getGlTxnItemList() {
		return glTxnItemList;
	}

	public void setRptTxnItem(RptTxnItem rptTxnItem) {
		this.rptTxnItem = rptTxnItem;
	}

	public void setGlTxnItemList(List<GlTxnItem> glTxnItemList) {
		this.glTxnItemList = glTxnItemList;
	}

	
	
}
