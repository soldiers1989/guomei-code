package com.sunline.ccs.service.util;

import java.util.ArrayList;
import java.util.List;

import com.sunline.ppy.dictionary.exchange.VCardApplyResponseItem;
import com.sunline.ppy.dictionary.report.ccs.ApplyResponseRptItem;
//import com.sunline.smsd.service.sdk.ApplyResponseSMItem;


/**
 * @see 类名：S0101Setup
 * @see 描述：建客建账建卡输出对象
 *
 * @see 创建日期：   2015-6-23下午7:29:28
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class S0101Setup {

	/**
	 * 建账制卡回盘文件接口
	 */
	private List<ApplyResponseRptItem> applyResponseRptItemList = new ArrayList<ApplyResponseRptItem>();
	
	/**
	 * 送短信接口
	 */
//	private ApplyResponseSMItem applyResponseSMItem;
	
	/**
	 * 虚拟卡申请回盘文件
	 */
	private VCardApplyResponseItem vCardApplyResponse;
	
	
	public List<ApplyResponseRptItem> getApplyResponseRptItemList() {
		return applyResponseRptItemList;
	}
/*	public ApplyResponseSMItem getApplyResponseSMItem() {
		return applyResponseSMItem;
	}*/
	public void setApplyResponseRptItemList(List<ApplyResponseRptItem> applyResponseRptItemList) {
		this.applyResponseRptItemList = applyResponseRptItemList;
	}
/*	public void setApplyResponseSMItem(ApplyResponseSMItem applyResponseSMItem) {
		this.applyResponseSMItem = applyResponseSMItem;
	}*/
	public VCardApplyResponseItem getvCardApplyResponse() {
		return vCardApplyResponse;
	}
	public void setvCardApplyResponse(VCardApplyResponseItem vCardApplyResponse) {
		this.vCardApplyResponse = vCardApplyResponse;
	}

}
