package com.sunline.ccs.param.ui.client.util;

import java.util.LinkedHashMap;

import com.google.inject.Singleton;
/*
import com.sunline.pcm.facility.client.selectopt.BmpSelectOptionInterAsync;
import com.sunline.ccs.infrastructure.client.domain.i18n.AuthReasonConstants;
import com.sunline.ccs.param.def.consts.AuthReasonGroups;
import com.sunline.ccs.param.def.enums.AuthReason;
import com.sunline.ark.gwt.client.util.RPCExecutor;
import com.sunline.ark.gwt.client.util.RPCTemplate;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.grid.ListGridField;
*/
/**
 * 常用的下拉框存放在公共的方法当中
 * 
* @author fanghj
 * 
 */
@Singleton
public class CcsSelectOptionUtils {
/*	
	@Inject
	private BmpSelectOptionInterAsync bmpSelectServer;

	@Inject
	private CpsSelectOptionInterAsync server;

	@Inject
	private AuthReasonConstants authReasonConstants;

	*//**
	 * 获取国家代码的下拉框
	 * 
	 * @param countryCd
	 *//*
//	public void getCountryCd(final SelectItem countryCd) {
//
//		RPCTemplate.call(new RPCExecutor<LinkedHashMap<String, String>>() {
//
//			@Override
//			public void execute(AsyncCallback<LinkedHashMap<String, String>> callback) {
//
//				server.getCountryCd(callback);
//			}
//
//			@Override
//			public void onSuccess(LinkedHashMap<String, String> result) {
//
//				countryCd.setValueMap(result);
//			}
//		});
//
//	}

	*//**
	 * 获取产品代码的下拉框
	 * 
	 * @param productCd
	 *//*
//	public void getProductCd(final SelectItem productCd) {
//
//		RPCTemplate.call(new RPCExecutor<LinkedHashMap<String, String>>() {
//
//			@Override
//			public void execute(AsyncCallback<LinkedHashMap<String, String>> callback) {
//
//				server.getProductCd(callback);
//			}
//
//			@Override
//			public void onSuccess(LinkedHashMap<String, String> result) {
//
//				productCd.setValueMap(result);
//			}
//		});
//	}

	*//**
	 * 获取货币代码的下拉框
	 * 
	 * @param currencyCd
	 *//*
//	public void getCurrencyCd(final SelectItem currencyCd) {
//
//		RPCTemplate.call(new RPCExecutor<LinkedHashMap<String, String>>() {
//
//			@Override
//			public void execute(AsyncCallback<LinkedHashMap<String, String>> callback) {
//
//				server.getCurrencyCd(callback);
//			}
//
//			@Override
//			public void onSuccess(LinkedHashMap<String, String> result) {
//
//				currencyCd.setValueMap(result);
//			}
//		});
//
//	}

	*//**
	 * 获取交易代码
	 * 
	 * @param txnCd
	 *//*
	public void setTxnCd(final SelectItem... txnCds) {

		RPCTemplate.call(new RPCExecutor<LinkedHashMap<String, String>>() {

			@Override
			public void execute(AsyncCallback<LinkedHashMap<String, String>> callback) {

				server.getTxnCd(callback);
			}

			@Override
			public void onSuccess(LinkedHashMap<String, String> result) {

				for(SelectItem item : txnCds){
					
					item.setValueMap(result);
				}
			}
		});

	}

	*//**
	 * 获取信用计划模板号码
	 * 
	 * @param planTemplate
	 *//*
	public void setPlantemplateCd(final SelectItem... planTemplates) {

		RPCTemplate.call(new RPCExecutor<LinkedHashMap<String, String>>() {

			@Override
			public void execute(AsyncCallback<LinkedHashMap<String, String>> callback) {

				server.getPlantemplateCd(callback);
			}

			@Override
			public void onSuccess(LinkedHashMap<String, String> result) {

				for(SelectItem item : planTemplates){
					
					item.setValueMap(result);
				}
			}
		});

	}

	public void setPaymentHierarchy(final ListGridField... pmtHierIdGridFields) {
		RPCTemplate.call(new RPCExecutor<LinkedHashMap<String, String>>() {

			@Override
			public void execute(AsyncCallback<LinkedHashMap<String, String>> callback) {
				server.getPaymentHierarchy(callback);
			}

			@Override
			public void onSuccess(LinkedHashMap<String, String> paymentHierarchies) {
				
				for(ListGridField field : pmtHierIdGridFields){
					
					field.setValueMap(paymentHierarchies);
				}	
			}
		});

	}

	*//**
	 * 账户参数代码
	 *//*
	public void setAcctAttrId(final SelectItem... acctAttrIdItems) {

		RPCTemplate.call(new RPCExecutor<LinkedHashMap<String, String>>() {

			@Override
			public void execute(AsyncCallback<LinkedHashMap<String, String>> callback) {

				server.getAcctAtrrId(callback);
			}

			@Override
			public void onSuccess(LinkedHashMap<String, String> result) {

				for(SelectItem item : acctAttrIdItems){
					
					item.setValueMap(result);
				}
			}
		});
	}
	
	*//**
	 * 分期代码下拉框的值
	 * @param loanCdItem
	 *//*
	public void setLoanCd(final SelectItem... loanCdItems){
		RPCTemplate.call(new RPCExecutor<LinkedHashMap<String, String>>() {

			@Override
			public void execute(AsyncCallback<LinkedHashMap<String, String>> callback) {

				server.getLoanPLan(callback);
			}

			@Override
			public void onSuccess(LinkedHashMap<String, String> result) {
				
				for(SelectItem item : loanCdItems){
					item.setValueMap(result);
				}
			}
		});
	}
	
	*//**
	 * 锁定码相关的授权原因码下拉框
	 * @param blockCodeAuthReasonItems
	 *//*
	public void setBlockCodeAuthReasons(final SelectItem... blockCodeAuthReasonItems){
		LinkedHashMap<String, String> blockCodeAuthReasons = new LinkedHashMap<String, String>();
		for (AuthReason r : AuthReasonGroups.BLOCKCODE_REASONS){
			blockCodeAuthReasons.put(r.toString(), r.toString() + " - " + authReasonConstants.getString(r.toString()));
		}
		for (SelectItem item : blockCodeAuthReasonItems){
			item.setValueMap(blockCodeAuthReasons);
		}
	}

	public void setMcc(final SelectItem... mccItems){
		RPCTemplate.call(new RPCExecutor<LinkedHashMap<String, String>>() {

			@Override
			public void execute(AsyncCallback<LinkedHashMap<String, String>> callback) {

				bmpSelectServer.getMcc(callback);
			}

			@Override
			public void onSuccess(LinkedHashMap<String, String> result) {
				
				for(SelectItem item : mccItems){
					item.setValueMap(result);
				}
			}
		});
	}
	
	*//**
	 * 分期代码下拉框的值--有分期类型限制
	 * @param loanCdItem
	 *//*
	public void setLoanCdForType(final SelectItem... loanCdItems){
		RPCTemplate.call(new RPCExecutor<LinkedHashMap<String, String>>() {

			@Override
			public void execute(AsyncCallback<LinkedHashMap<String, String>> callback) {

				server.getLoanPLanForType(callback);
			}

			@Override
			public void onSuccess(LinkedHashMap<String, String> result) {
				
				for(SelectItem item : loanCdItems){
					item.setValueMap(result);
				}
			}
		});
	}*/
}
