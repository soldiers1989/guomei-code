package com.sunline.ccs.ui.client.pages.exemptapprove;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsTxnWaiveLog;
import com.sunline.ccs.infrastructure.client.ui.UCcsTxnWaiveLogHst;
import com.sunline.ccs.param.def.enums.AdjState;
import com.sunline.ccs.ui.client.commons.CommonUiUtils;
import com.sunline.kylin.web.ark.client.helper.ButtonColumnHelper;
import com.sunline.kylin.web.ark.client.helper.ButtonColumnHelper.ButtonClickHandler;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ui.button.client.ButtonSetting;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.EventObjectHandler;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.ISelectRowEventListener;
import com.sunline.ui.tab.client.Tab;
import com.sunline.ui.tab.client.TabItemSetting;
import com.sunline.ui.tab.client.TabSetting;

/**
* @Description 手工豁免审批页面
* @author 鹏宇
* @date 2015-11-13 下午8:02:55
 */
@Singleton
public class ExemptApprovePage extends Page{
	
	private KylinForm searchForm;
	private KylinGrid searchGrid;
	private KylinGrid searchHstGrid;           //豁免的历史记录
	private KylinForm detailForm;
	
	@Inject
	private ExemptApproveConstants constants;
	
	@Inject
	private UCcsTxnWaiveLog uCcsTxnWaiveLog;
	@Inject
	private UCcsTxnWaiveLogHst uCcsTxnWaiveLogHst;
	
	private Tab tab;
	private TabItemSetting logSetting;             //分期计划详情Tab的Setting
	private TabItemSetting logHstSetting;             //信用计划详情的Tab的Setting
	
	@Override
	public IsWidget createPage() {
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");
		panel.setHeight("100%");
		
		panel.add(createSearchForm());
		
		panel.add(createTab());
		
		panel.add(showDetailForm());
		
		return panel;
	}
	
	/**
	* @Description 创建搜索表单
	* @author 鹏宇
	* @date 2015-11-13 下午8:40:25
	 */
	private HorizontalPanel createSearchForm(){
		HorizontalPanel hPanel = new HorizontalPanel();
		searchForm = new KylinForm();
		searchForm.setWidth("100%");
		searchForm.setHeight("100%");
		searchForm.setCol(3);
		
		searchForm.setField(uCcsTxnWaiveLog.ContrNbr().required(true));              //合同号
		KylinButton searchBtn = ClientUtils.createSearchButton(new IClickEventListener() {
			
			@Override
			public void onClick() {
				if(searchForm.valid()){
					searchGrid.loadData(searchForm);
					searchHstGrid.loadData(searchForm);
				}
			}
		});
		hPanel = CommonUiUtils.lineLayoutForm(searchForm, searchBtn, null, null);
		
		return hPanel;
	}
	
	@Override
	public void refresh() {
		searchForm.getUi().clear();
		detailForm.getUi().clear();
		searchGrid.clearData();
		searchGrid.loadDataFromUrl("rpc/ExemptApproveServer/getWaiveLogList");
		searchHstGrid.loadDataFromUrl("rpc/ExemptApproveServer/getWaiveLogHstList");
	}
	
	private Tab createTab(){
		TabSetting tabSetting = new TabSetting().dblClickToClose(true).dragToMove(true).showSwitch(true);
		tab = new Tab(tabSetting);
		tab.setWidth("100%");
		tab.setHeight("340px");
		
		//当天豁免
//		ScrollPanel logScrollPanel = new ScrollPanel();
//		logScrollPanel.setWidth("100%");
//		logScrollPanel.setHeight("300px");
//		logScrollPanel.add(createSearchGrid());
		logSetting = new TabItemSetting("loanDetailTab", constants.shougong());
		tab.addItem(logSetting, createSearchGrid());
		
		//历史豁免
//		ScrollPanel logHstScrollPanel = new ScrollPanel();
//		logHstScrollPanel.setWidth("100%");
//		logHstScrollPanel.setHeight("300px");
//		logHstScrollPanel.add(createSearchHstGrid());
		logHstSetting = new TabItemSetting("planDetailTab", constants.logHst());
		tab.addItem(logHstSetting,createSearchHstGrid());
		
		return tab;
	}
	
	private KylinGrid createSearchGrid(){
		searchGrid = new KylinGrid();
		searchGrid.setWidth("100%");
		searchGrid.setHeight("300px");
		
		ButtonColumnHelper allowButton = new ButtonColumnHelper("allowButton","通过");
		allowButton.setSort(false);
		ButtonColumnHelper refuseButton = new ButtonColumnHelper("refuseButton", "拒绝");
		refuseButton.setSort(false);
		
		allowButton.asButtonItem(new ButtonClickHandler() {
			
			@Override
			public List<ButtonSetting> buttons(MapData rowData) {
				final String contrNbr = rowData.getString(uCcsTxnWaiveLog.ContrNbr().getName());
				final String acctNbr = rowData.getString(uCcsTxnWaiveLog.AcctNbr().getName());
				final String refNbr = rowData.getString(uCcsTxnWaiveLog.RefNbr().getName());
				final String txnCd = rowData.getString(uCcsTxnWaiveLog.TxnCode().getName());
				final String opSeq = rowData.getString(uCcsTxnWaiveLog.OpSeq().getName());
				
				AdjState adjState = AdjState.valueOf(rowData.getString(uCcsTxnWaiveLog.AdjState().getName()));
				
				List<ButtonSetting> buttonSettings = new ArrayList<ButtonSetting>();
				ButtonSetting button = new ButtonSetting();
				button.text(constants.allowbutton());
				button.click(new IClickEventListener() {
					
					@Override
					public void onClick() {
						RPC.ajaxMask("rpc/ExemptApproveServer/allowWaive", new RpcCallback<Data>() {             //允许

							@Override
							public void onSuccess(Data result) {
								//豁免的逻辑
								detailForm.getUi().clear();
								Dialog.tipNotice(result.toString());
								searchGrid.loadData(searchForm);
								
							}
						}, contrNbr,acctNbr,refNbr,txnCd,opSeq);
					}});
				
				button.disabled(false);
				
				if(! adjState.equals(AdjState.W)){
					button.disabled(true);
				}
				
				buttonSettings.add(button);
				
				return buttonSettings;
			}
		});
		
		refuseButton.asButtonItem(new ButtonClickHandler() {
			
			@Override
			public List<ButtonSetting> buttons(MapData rowData) {
				final String contrNbr = rowData.getString(uCcsTxnWaiveLog.ContrNbr().getName());
				final String acctNbr = rowData.getString(uCcsTxnWaiveLog.AcctNbr().getName());
				final String refNbr = rowData.getString(uCcsTxnWaiveLog.RefNbr().getName());
				final String txnCd = rowData.getString(uCcsTxnWaiveLog.TxnCode().getName());
				final String opSeq = rowData.getString(uCcsTxnWaiveLog.OpSeq().getName());
				
				AdjState adjState = AdjState.valueOf(rowData.getString(uCcsTxnWaiveLog.AdjState().getName()));
				
				List<ButtonSetting> buttonSettings = new ArrayList<ButtonSetting>();
				ButtonSetting button = new ButtonSetting();
				button.text(constants.refusebutton());
				button.click(new IClickEventListener() {
					
					@Override
					public void onClick() {
						RPC.ajaxMask("rpc/ExemptApproveServer/refuseWaive", new RpcCallback<Data>() {

							@Override
							public void onSuccess(Data result) {
								//豁免的逻辑
								detailForm.getUi().clear();
								Dialog.tipNotice(result.toString());
								searchGrid.loadData(searchForm);
								
							}
						}, contrNbr,acctNbr,refNbr,txnCd,opSeq);
					}});
				
				button.disabled(false);
				
				if(! adjState.equals(AdjState.W)){
					button.disabled(true);
				}
				
				buttonSettings.add(button);
				
				return buttonSettings;
			}
		});
		
		searchGrid.setColumns(
				uCcsTxnWaiveLog.AcctNbr().readonly(true),
				uCcsTxnWaiveLog.ContrNbr().readonly(true),
				uCcsTxnWaiveLog.CardNbr().readonly(true),
				uCcsTxnWaiveLog.TxnCode().readonly(true),
				uCcsTxnWaiveLog.TxnDate().readonly(true),
				uCcsTxnWaiveLog.TxnAmt().readonly(true),
				allowButton,refuseButton
				);
		
		searchGrid.getSetting().columnWidth(150);
		
		searchGrid.getSetting().onSelectRow(new ISelectRowEventListener() {
			
			@Override
			public void selectRow(MapData rowdata, String rowid,EventObjectHandler rowobj) {
				detailForm.getUi().clear();
				detailForm.setFormData(rowdata);
			}
		});
		
		return searchGrid;
	}
	
	private KylinGrid createSearchHstGrid(){
		searchHstGrid = new KylinGrid();
		searchHstGrid.setWidth("100%");
		searchHstGrid.setHeight("300px");
		
		searchHstGrid.setColumns(
				uCcsTxnWaiveLogHst.AcctNbr().readonly(true),
				uCcsTxnWaiveLogHst.ContrNbr().readonly(true),
				uCcsTxnWaiveLogHst.TxnAmt().readonly(true),
				uCcsTxnWaiveLogHst.TxnCode().readonly(true),
				uCcsTxnWaiveLogHst.Currency().readonly(true),
				uCcsTxnWaiveLogHst.OpSeq().readonly(true),
				uCcsTxnWaiveLogHst.OpTime().readonly(true),
				uCcsTxnWaiveLogHst.OpId().readonly(true),
				uCcsTxnWaiveLogHst.Remark().readonly(true)
				);
		
		searchHstGrid.getSetting().columnWidth(150);
		
		searchHstGrid.getSetting().onSelectRow(new ISelectRowEventListener() {
			
			@Override
			public void selectRow(MapData rowdata, String rowid,EventObjectHandler rowobj) {
				detailForm.getUi().clear();
				detailForm.setFormData(rowdata);
			}
		});
		
		
		return searchHstGrid;
	}
	
	private VerticalPanel showDetailForm(){
		VerticalPanel kPanel = new VerticalPanel();
		detailForm = new KylinForm();
		detailForm.setWidth("100%");
		detailForm.setHeight("350px");
		detailForm.setCol(3);
		
		detailForm.setField(
				uCcsTxnWaiveLog.AcctNbr().readonly(true),
				uCcsTxnWaiveLog.ContrNbr().readonly(true),
				uCcsTxnWaiveLog.CardNbr().readonly(true),
				uCcsTxnWaiveLog.Org().readonly(true),
				uCcsTxnWaiveLog.TxnAmt().readonly(true),
				uCcsTxnWaiveLog.TxnCode().readonly(true),
				uCcsTxnWaiveLog.RefNbr().readonly(true),
				uCcsTxnWaiveLog.Currency().readonly(true),
				uCcsTxnWaiveLog.OpSeq().readonly(true),
				uCcsTxnWaiveLog.OpTime().readonly(true),
				uCcsTxnWaiveLog.OpId().readonly(true),
				uCcsTxnWaiveLog.DbCrInd().readonly(true),
				uCcsTxnWaiveLog.TxnDate().readonly(true),
				uCcsTxnWaiveLog.CreateTime().readonly(true),
				uCcsTxnWaiveLog.CreateUser().readonly(true),
				uCcsTxnWaiveLog.LstUpdTime().readonly(true),
				uCcsTxnWaiveLog.LstUpdUser().readonly(true),
				uCcsTxnWaiveLog.AdjState().readonly(true),
				uCcsTxnWaiveLog.LogBizDate().readonly(true),
				uCcsTxnWaiveLog.Term().readonly(true),
				uCcsTxnWaiveLog.JpaVersion().readonly(true),
				uCcsTxnWaiveLog.RejReason().readonly(true),
				uCcsTxnWaiveLog.CheckOpId().readonly(true),
				uCcsTxnWaiveLog.CheckOpSeq().readonly(true),
				uCcsTxnWaiveLog.CheckOpTime().readonly(true),
				uCcsTxnWaiveLog.Remark().readonly(true)
				);
		
		detailForm.getSetting().labelWidth(160);
		kPanel.add(detailForm);
		return kPanel;
	}
	
}
