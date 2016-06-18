//package com.sunline.ccs.ui.client.commons;
//
//
//import java.util.List;
//
//import com.google.gwt.user.client.rpc.AsyncCallback;
//import com.google.inject.Inject;
//import com.google.inject.Singleton;
//import com.sunline.ccs.param.def.TxnCd;
//
///**
// * 参数获取工具类
//* @author fanghj
// *
// */
//@Singleton
//public class ParamUtil {
//	@Inject
//	private UIUtil uiUtil;
//	
//	@Inject
//	private ParamInterAsync paramServer;
//	
//	/**
//	 * 获取交易码参数并填充交易码下拉框
//	 * @param txnCdItem	交易码下拉框
//	 * @param objs	获取交易码参数过程中需要禁用的控件
//	 */
//	public void setupTxnCdSelectItemData(final SelectItem txnCdItem, final Object... objs) {
//		RPCTemplate.call(new RPCExecutor<List<TxnCd>>() {
//
//			@Override
//			public void execute(
//					AsyncCallback<List<TxnCd>> callback) {
//				paramServer.getAdjTxnCdList(callback);
//			}
//
//			@Override
//			public void onSuccess(List<TxnCd> txnCdList) {
//				// 获取交易码列表并更新交易码下拉框
//				uiUtil.updateTxnCdSelectItemData(txnCdItem, txnCdList);
//			}
//
//		}, objs);
//	}
//	
//	/**
//	 * 获取交易码参数并填充交易码下拉框
//	 * @param txnCdItem	交易码下拉框
//	 * @param objs	获取交易码参数过程中需要禁用的控件
//	 */
//	public void setupTxnCdSelectItemData(final  List<TxnCd> txnList) {
//		RPCTemplate.call(new RPCExecutor<List<TxnCd>>() {
//
//			@Override
//			public void execute(
//					AsyncCallback<List<TxnCd>> callback) {
//				paramServer.getAdjTxnCdList(callback);
//			}
//
//			@Override
//			public void onSuccess(List<TxnCd> txnCdList) {
//				// 获取交易码列表并更新交易码下拉框
//				txnList.addAll(txnCdList);
//			}
//
//		});
//	}
//	
//}
