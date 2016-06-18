package com.sunline.ccs.ui.client.pages.financeadj;

import java.util.LinkedHashMap;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsAcct;
import com.sunline.ccs.infrastructure.client.ui.UCcsTxnAdjLog;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnAdjLog;
import com.sunline.ccs.ui.client.commons.CommonKylinForm;
import com.sunline.ccs.ui.client.commons.CommonKylinGrid;
import com.sunline.ccs.ui.client.commons.CommonUiUtils;
import com.sunline.ccs.ui.client.commons.CommonVerticalPanel;
import com.sunline.ccs.ui.client.commons.UIUtilConstants;
import com.sunline.kylin.web.ark.client.helper.ColumnHelper;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.dialog.client.listener.ConfirmCallbackFunction;
import com.sunline.ui.event.client.EventObjectHandler;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.IDblClickRowEventListener;
import com.sunline.ui.tab.client.Tab;
import com.sunline.ui.tab.client.TabItemSetting;
import com.sunline.ui.tab.client.TabSetting;

/**
 * 
 * 账务调整页面
 * 
* @author yeyu
 * 
 */
@Singleton
public class FinanceAdjustment extends Page {
	
	@Inject
	private UCcsTxnAdjLog uCcsTxnAdjLog;
	
	@Inject 
	private UCcsAcct uCcsAcct;
	
	@Inject
	private FinanceAdjustmentConstants constants;
	
	@Inject
	private UIUtilConstants uiConstants;
	
	/*
	 * 初始全局部变量
	 */
	private CommonVerticalPanel mainWindow;
	@Override
	public void refresh() {
		detailsForm.getUi().clear();
		userGrid.clearData();
		form.getUi().clear();
		//交易码下拉框
		RPC.ajax("rpc/t3201Server/getTxnCd",new RpcCallback<Data>(){

			@Override
			public void onSuccess(Data result) {
				updTranParamsForm.setFieldSelectData(uCcsTxnAdjLog.TxnCode().getName(),result.asListData());
				
			}
			
		});
	}

	private VerticalPanel topPanel;
	private VerticalPanel footPanel;
	private CommonVerticalPanel dividePanel;
	private KylinForm form;
	private CommonKylinGrid userGrid;
	private Tab tab;
	private TabSetting tabInitSetting;
	private KylinForm updTranParamsForm;
	private VerticalPanel tranPanel;
	private CommonKylinForm cardNoForm;
	private CommonKylinForm cardNoSubForm;
	
	private CommonVerticalPanel middPanel;
	private KylinForm detailsForm;
	
	/**
	 * 初始化T3201(账务调整)主界面
	 */
	@Override
	public IsWidget createPage() {
		//主操作界面整体布局(垂直布局)
		mainWindow = new CommonVerticalPanel();
		//顶部布局
		topPanel = new VerticalPanel();
		topPanel.setWidth("98%");
		//底部布局
		footPanel = new VerticalPanel();
		footPanel.setWidth("98%");
		//分割线(因框架中没有margin top的实现所以使用布局代替)
		dividePanel = new CommonVerticalPanel("10px");
		//实例化提交查询按钮
		//实例化kylinweb表单
		form = new KylinForm();
		form.setField(uCcsTxnAdjLog.OpTime().showDate(false).showTime(false),
				uCcsTxnAdjLog.VoidTime().showDate(false).showTime(false));
		KylinButton btnSearch = new KylinButton("查询",null);
		btnSearch.addClickEventListener(new FinancialAdjHandler(form));
		HorizontalPanel hPanel = CommonUiUtils.lineLayoutForm(form, btnSearch, null, null);
		/*
		 * 初始化Grid
		 */
		userGrid = new CommonKylinGrid("250px","98%");
		userGrid.setColumns(
				uCcsTxnAdjLog.OpTime(),
				uCcsTxnAdjLog.CardNbr(),
				uCcsTxnAdjLog.TxnAmt(),
				uCcsTxnAdjLog.Currency(),
				uCcsTxnAdjLog.B039RtnCode(),
				uCcsTxnAdjLog.RefNbr(),
				uCcsTxnAdjLog.VoidInd()
		);
		userGrid.addDblClickListener(new DataGridDBClick());
		userGrid.getSetting().delayLoad(true);
		//初始化选项卡组件
		tabInitSetting = new TabSetting();
		tabInitSetting.dblClickToClose(Boolean.FALSE).dragToMove(Boolean.FALSE).showSwitch(Boolean.FALSE).changeHeightOnResize(Boolean.FALSE);
		tab = new Tab(tabInitSetting);
		VerticalPanel tabPanel = new VerticalPanel();
		tabPanel.setWidth("98%");
		tabPanel.add(tab);
		TabItemSetting logSetting = new TabItemSetting(null, constants.tabTitleLogForm());
		TabItemSetting tranSetting = new TabItemSetting(null, constants.tabTitleTranForm());
		tab.addItem(logSetting, createLogLayout());
		tab.addItem(tranSetting, createTranLayout());
		topPanel.add(hPanel);
		topPanel.add(userGrid);
		footPanel.add(tabPanel);
		mainWindow.add(topPanel);
		mainWindow.add(dividePanel);
		mainWindow.add(footPanel);
		return mainWindow;
	}
	
	//创建选项卡页面
	private ScrollPanel createLogLayout (){
		ScrollPanel logPanel = new ScrollPanel();
		logPanel.setHeight("220px");
		detailsForm = new KylinForm();
		detailsForm.setWidth("98%");
		detailsForm.setHeight("100%");
		detailsForm.setCol(3);
		TextColumnHelper op_seq = new TextColumnHelper(CcsTxnAdjLog.P_OpSeq,"序列号",12);
		detailsForm.setField(new ColumnHelper[]{
				op_seq.setHide(true),
				uCcsTxnAdjLog.Org().readonly(true),
				uCcsTxnAdjLog.TxnCode().readonly(true),
				uCcsTxnAdjLog.B039RtnCode().readonly(true),
				uCcsTxnAdjLog.AcctNbr().readonly(true),
				uCcsTxnAdjLog.CardNbr().readonly(true),
				uCcsTxnAdjLog.Currency().readonly(true),
				uCcsTxnAdjLog.RefNbr().readonly(true),
				uCcsTxnAdjLog.DbCrInd().readonly(true),
				uCcsTxnAdjLog.OpId().readonly(true),
				uCcsTxnAdjLog.OpTime().readonly(true),
				uCcsTxnAdjLog.Mcc().readonly(true),
				uCcsTxnAdjLog.TxnAmt().readonly(true),
				uCcsTxnAdjLog.TxnCode().readonly(true),
				uCcsTxnAdjLog.TxnDate().readonly(true),
				uCcsTxnAdjLog.Remark().readonly(true),
				uCcsTxnAdjLog.VoidInd().readonly(true),
				uCcsTxnAdjLog.VoidOpId().readonly(true),
				uCcsTxnAdjLog.VoidTime().readonly(true),
				uCcsTxnAdjLog.VoidReason()
		});
		KylinButton btnCancel = new KylinButton("撤销",null);
		btnCancel.addClickEventListener(new AdjTranslog());
		detailsForm.addButton(btnCancel);
		logPanel.add(detailsForm);
		return logPanel;
	}
	
	private VerticalPanel createTranLayout (){
		
		tranPanel = new VerticalPanel();
		
		tranPanel.setHorizontalAlignment(HasAlignment.ALIGN_LEFT);
		
		tranPanel.setWidth("98%");
		
		tranPanel.setHeight("100%");
		
		middPanel = new CommonVerticalPanel();
		
		VerticalPanel topPanel = new VerticalPanel();
		
		cardNoForm = new CommonKylinForm();
		
		cardNoSubForm = new CommonKylinForm();
		
		updTranParamsForm = new CommonKylinForm();
		
		topPanel.add(cardNoForm.addFileds(new Object[]{
				uCcsTxnAdjLog.CardNbr()
		}));
		
		KylinButton button = new KylinButton(constants.btnTextForQuery(),"");
		button.addClickEventListener(new QueryCustomerInfoByCardNo(cardNoForm,updTranParamsForm));
		topPanel.add(cardNoSubForm.addFileds(new Object[]{
				button
		}));
		updTranParamsForm.setField(new ColumnHelper[]{
				uCcsAcct.Name().readonly(true),
				uCcsTxnAdjLog.TxnCode().asSelectItem().required(true),
				new TextColumnHelper("adjAmount",constants.adjAmount(),50).required(true),
				uCcsTxnAdjLog.OpTime().required(true),
				uCcsTxnAdjLog.RefNbr().required(true),
				uCcsTxnAdjLog.Remark().required(true),
				uCcsTxnAdjLog.Currency().asSelectItem().required(true),
				uCcsTxnAdjLog.Term().required(true)
		});
		Scheduler.get().scheduleFinally(new ScheduledCommand(){

			@Override
			public void execute() {
				SelectItem selectItem = new SelectItem();
				LinkedHashMap kvPair = new LinkedHashMap();
				kvPair.put("156", "人民币");
				selectItem.setValue(kvPair);
				updTranParamsForm.setFieldSelectData(CcsTxnAdjLog.P_Currency, selectItem);
			}
		});
		KylinButton cancel = new KylinButton("新增",null);
		cancel.addClickEventListener(new CreateAdjTransLog());
		updTranParamsForm.addButton(cancel);
		middPanel.add(updTranParamsForm);
		tranPanel.add(topPanel);
		tranPanel.add(middPanel);
		return tranPanel;
	}
	//创建选项卡页面
	
	class CreateAdjTransLog implements IClickEventListener {
		
		@Override
		public void onClick() {
			final MapData md = updTranParamsForm.getSubmitData().asMapData();
			md.put(CcsTxnAdjLog.P_CardNbr, cardNoForm.getSubmitData().asMapData().getString(CcsTxnAdjLog.P_CardNbr));
			//测试桩代码
			Dialog.confirm("是否新增调账日志记录", "确认", new ConfirmCallbackFunction(){
				@Override
				public void corfimCallback(boolean value) {
					if(value){
						RPC.ajax("rpc/t3201Server/writeTranAdjLog", new RpcCallback<Data>(){
							//TODO
							@Override
							public void onSuccess(Data result) {
								Dialog.tipNotice(constants.tipsUpdateSucc(), 1000);
								//userGrid.loadDataFromUrl("rpc/t3201Server/getCurrentUserAdjLogList");
							}
						},md);
					}
				}
				
			});
		}
		
	}
	
	class AdjTranslog implements IClickEventListener {
		
		@Override
		public void onClick() {
			Dialog.confirm("确认要撤消吗?", "确认 ", new ConfirmCallbackFunction(){
				
				@Override
				public void corfimCallback(boolean value) {
					if(value){
						final MapData formData = detailsForm.getSubmitData().asMapData();
						String reason = formData.getString(CcsTxnAdjLog.P_VoidReason);
						String seq = formData.getString(CcsTxnAdjLog.P_OpSeq);
						RPC.ajax("rpc/t3201Server/cancelAdjustTran", new RpcCallback<Data>(){
							@Override
							public void onSuccess(Data result) {
								Dialog.tipNotice("保存成功！", 1000);
							}
						}, new Object[]{seq,reason});
					}
				}
				
			});
		}
		
	}
	
	class DataGridDBClick implements IDblClickRowEventListener {

		@Override
		public void onDblClickRow(MapData data, String rowid, EventObjectHandler row) {
			detailsForm.setFormData(data);
		}
		
	}
	
	/**
	 * 
	 * @see 类名：QueryCustomerInfoByCardNo
	 * @see 描述：根据持卡人卡号查询持卡人信息功能事件监听器
	 *
	 * @see 创建日期：   Jun 25, 201512:12:37 PM
	 * @author yeyu
	 * 
	 * @see 修改记录：
	 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
	 */
	class QueryCustomerInfoByCardNo implements IClickEventListener {
		
		private KylinForm _form;
		
		private KylinForm _targetForm;
		
		public QueryCustomerInfoByCardNo(KylinForm form,KylinForm targetForm){
			this._form = form;
			this._targetForm = targetForm;
		}

		@Override
		public void onClick() {
			RPC.ajax("rpc/t3201Server/loadCustInfoByCardNo", new RpcCallback<Data>(){
				
				@Override
				public void onSuccess(Data result) {
					_targetForm.setFormData(result);
				}
				
			}, _form.getSubmitData().asMapData().getString(CcsTxnAdjLog.P_CardNbr));
		}
		
	}
	
	/**
	 * 
	 * @see 类名：FinancialAdjHandler
	 * @see 描述：TODO 中文描述
	 *
	 * @see 创建日期：   Jun 25, 201511:05:50 AM
	 * @author Liming.Feng
	 * 
	 * @see 修改记录：
	 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
	 */
	class FinancialAdjHandler implements IClickEventListener{
		
		KylinForm _form;
		
		public FinancialAdjHandler(KylinForm form){
			this._form = form;
		}
		
		/**
		 * @param
		 * @return void
		 * 
		 */
		@SuppressWarnings("all")
		@Override
		public void onClick() {
			Data submitData = _form.getSubmitData();
			if(submitData.asMapData().getString(CcsTxnAdjLog.P_OpTime) != null){
				userGrid.getUi().setParm(CcsTxnAdjLog.P_OpTime,submitData.asMapData().getString(CcsTxnAdjLog.P_OpTime));
			}
			if(submitData.asMapData().getString(CcsTxnAdjLog.P_VoidTime) != null){
				userGrid.getUi().setParm(CcsTxnAdjLog.P_VoidTime,submitData.asMapData().getString(CcsTxnAdjLog.P_VoidTime));
			}
			userGrid.loadDataFromUrl("rpc/t3201Server/getCurrentUserAdjLogList");
		}
	}
}
