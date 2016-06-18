package com.sunline.ccs.param.ui.client.accountattr;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.Flat;
import com.sunline.kylin.web.core.client.authority.Authorization;
import com.sunline.kylin.web.core.client.authority.ResourceUnit;
import com.sunline.kylin.web.core.client.mvp.Token;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ccs.infrastructure.client.ui.UAccountAttribute;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.ListData;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.dialog.client.listener.ConfirmCallbackFunction;
import com.sunline.ui.event.client.EventObjectHandler;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.IDblClickRowEventListener;

/**
 * 账户参数代码页面
 * 
 * @author lisy
 * @version [版本号, Jun 19, 2015]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */

@Singleton
public class AccountAttributePage extends Page {

	@Inject
	private UAccountAttribute uAccountAttribute;

	private KylinGrid grid = new KylinGrid();

	private KylinForm form;

	private ResourceUnit deleteBtnAuth = new ResourceUnit(this, "deleteBtnAuth");

	@Override
	public IsWidget createPage() {
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");
		panel.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
		form = new KylinForm();
		form.getSetting().labelWidth(90);
		// 搜索列表
		form.setField(uAccountAttribute.AccountAttributeId());
		form.setCol(1);
		// 查询按钮
		form.addButton(ClientUtils.createSearchButton(new IClickEventListener() {

			@Override
			public void onClick() {
				//Dialog.alert(form.getSubmitData().toString());
				grid.loadData(form);
			}
		}));
		// 记录显示网格
		grid.setWidth("98%");
		grid.setHeight("98%");
		// 刷新按钮
		grid.setHeader(ClientUtils.createRefreshItem(new IClickEventListener() {

			@Override
			public void onClick() {
				grid.loadData(form);
				form.getUi().clear();
			}
		}));
		// 编辑按钮
		if(Authorization.accredit(AccountAttributeDetailPage.class)) {
			grid.addDblClickListener(new IDblClickRowEventListener() {

				@Override
				public void onDblClickRow(MapData data, String rowid, EventObjectHandler row) {
					String accountAttributeId = data.getString(uAccountAttribute.AccountAttributeId().getName());
					Token token = Flat.get().getCurrentToken();
					token.addParam(uAccountAttribute.AccountAttributeId().getName(), accountAttributeId);
					token.directPage(AccountAttributeDetailPage.class);
					Flat.get().goTo(token);
				}
			});
		}
		// 增加按钮
		if(Authorization.accredit(AccountAttributeAddPage.class)) {
			grid.setHeader(ClientUtils.createAddItem(new IClickEventListener() {

				@Override
				public void onClick() {
					Token token = Flat.get().getCurrentToken();
					token.directPage(AccountAttributeAddPage.class);
					Flat.get().goTo(token);
				}
			}));
		}
		// 删除按钮
		if(Authorization.accredit(deleteBtnAuth)) {
			grid.setHeader(ClientUtils.createDeleteItem(new IClickEventListener() {

				@Override
				public void onClick() {
					delete();
				}
			}));
		}
		// 显示列表名
		grid.setColumns(uAccountAttribute.AccountAttributeId(), 
				uAccountAttribute.AccountType(),
				uAccountAttribute.CashLimitRate(),
				uAccountAttribute.CollMinpmt(),
				uAccountAttribute.CollOnAge(),
				uAccountAttribute.AcctDescription());     //grid中新增账户描述  2015-11-11 9:35  chenpy
		panel.add(form);
		panel.add(grid);
		return panel;
	}

	// 删除
	public void delete() {
		if(grid.getGrid().getSelectedRows().size() <= 0) {
			Dialog.alert("请选择您需要删除的记录");
		} else {
			Dialog.confirm("是否确认要删除？", "提示", new ConfirmCallbackFunction() {

				@Override
				public void corfimCallback(boolean value) {
					if(value) {
						final List<Integer> keys = new ArrayList<Integer>();
						ListData listData = grid.getGrid().getSelectedRows();
						for(int i = 0; i < listData.size(); i++) {
							keys.add(listData.get(i).asMapData()
									.getInteger(uAccountAttribute.AccountAttributeId().getName()));
						}
						RPC.ajax("rpc/accountAttributeServer/deleteAccountAttr", new RpcCallback<Data>()
								{
									@Override
									public void onSuccess(Data result) {
										Dialog.tipNotice("删除成功！");
										grid.loadData(form);
									}
								}, keys);
					}
				}
			});
		}
	}

	@Override
	public void refresh() {
		form.getUi().clear();
		grid.loadDataFromUrl("rpc/accountAttributeServer/getAccountAttrList");
	}
}
/*
 * import com.sunline.pcm.ui.common.client.ClientUtils; import
 * com.sunline.pcm.ui.common.client.DispatcherPage; import
 * com.sunline.pcm.ui.common.client.GridHeader; import
 * com.sunline.pcm.ui.cp.client.panel.ControlPanelPlace; import
 * com.sunline.ccs.infrastructure.client.ui.UAccountAttribute; import
 * com.sunline.ccs.param.def.enums.CPSAuthority; import
 * com.sunline.ark.gwt.client.datasource.YakDataSource; import
 * com.sunline.ark.gwt.client.mvp.ParamMapToken; import
 * com.sunline.ark.gwt.client.util.ClientFactory; import
 * com.sunline.ark.gwt.client.util.RPCExecutor; import
 * com.sunline.ark.gwt.client.util.RPCTemplate; import
 * com.sunline.ark.gwt.shared.datasource.FetchRequest; import
 * com.sunline.ark.gwt.shared.datasource.FetchResponse; import
 * com.google.gwt.resources.client.ImageResource; import
 * com.google.gwt.user.client.rpc.AsyncCallback; import
 * com.google.inject.Inject; import com.google.inject.Singleton; import
 * com.smartgwt.client.types.FetchMode; import
 * com.smartgwt.client.types.SelectionAppearance; import
 * com.smartgwt.client.widgets.events.ClickEvent; import
 * com.smartgwt.client.widgets.events.ClickHandler; import
 * com.smartgwt.client.widgets.form.DynamicForm; import
 * com.smartgwt.client.widgets.form.events.SubmitValuesEvent; import
 * com.smartgwt.client.widgets.form.events.SubmitValuesHandler; import
 * com.smartgwt.client.widgets.grid.ListGrid; import
 * com.smartgwt.client.widgets.grid.ListGridRecord; import
 * com.smartgwt.client.widgets.grid.events.RecordDoubleClickEvent; import
 * com.smartgwt.client.widgets.grid.events.RecordDoubleClickHandler; import
 * com.smartgwt.client.widgets.toolbar.ToolStripButton;
 */
/*
 * public static final String PAGE_ID = "auth-accountattr-list";
 * 
 * private ListGrid listGrid;
 * 
 * private DynamicForm searchForm;
 * 
 * @Inject private AccountAtrributeConstants constants;
 * 
 * @Inject private AccountAtrributeServerInterAsync server;
 * 
 * @Inject private UAccountAttribute uAccountAttribute;
 * 
 * @Inject private ClientUtils clientUtils;
 * 
 * @Inject private ClientFactory clientFactory;
 * 
 * public AccountAttributePage() {
 * 
 * super(PAGE_ID, true, CPSAuthority.CPSAccountAttr); }
 * 
 * @Override public void updateView(ParamMapToken token) {
 * 
 * listGrid.invalidateCache(); listGrid.fetchData(); }
 * 
 * @Override public String getTitle() {
 * 
 * return constants.accountAtrributeTitle(); }
 * 
 * @Override public ImageResource getIcon() {
 * 
 * return null; }
 * 
 * @Override protected void createCanvas() {
 * 
 * GridHeader header = clientUtils.createGridHeader(); { searchForm = new
 * DynamicForm(); { searchForm.setNumCols(4); searchForm.setWidth100();
 * 
 * // TODO
 * searchForm.setFields(uAccountAttribute.AccountAttributeId().addClearPicker
 * ().createFormItem());
 * 
 * searchForm.addSubmitValuesHandler(new SubmitValuesHandler() {
 * 
 * @Override public void onSubmitValues(SubmitValuesEvent event) {
 * 
 * listGrid.filterData(searchForm.getValuesAsCriteria()); } }); }
 * header.createSearchArea(searchForm); } addMember(header);
 * 
 * listGrid = new ListGrid(); {
 * 
 * listGrid.setWidth100(); listGrid.setHeight100();
 * listGrid.setDataFetchMode(FetchMode.LOCAL);
 * listGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
 * 
 * // TODO listGrid.setFields(uAccountAttribute.AccountAttributeId().width(150
 * ).createLGField(),
 * uAccountAttribute.AccountType().width(200).createLGField(),
 * uAccountAttribute.CashLimitRate().width(150).createLGField(),
 * uAccountAttribute.CollMinpmt().width(150).createLGField(),
 * uAccountAttribute.CollOnAge().width("*").createLGField());
 * 
 * YakDataSource ds = new YakDataSource() {
 * 
 * @Override public void fetchData(FetchRequest fetchRequest,
 * AsyncCallback<FetchResponse> callback) {
 * 
 * server.getAccountAttrList(fetchRequest, callback); } };
 * 
 * // TODO ds.setFields(uAccountAttribute.AccountAttributeId().createField(),
 * uAccountAttribute.AccountType().createField(),
 * uAccountAttribute.CashLimitRate().createField(),
 * uAccountAttribute.CollMinpmt().createField(),
 * uAccountAttribute.CollOnAge().createField());
 * 
 * listGrid.setDataSource(ds); listGrid.addRecordDoubleClickHandler(new
 * RecordDoubleClickHandler() {
 * 
 * @Override public void onRecordDoubleClick(RecordDoubleClickEvent event) {
 * 
 * ControlPanelPlace place = new
 * ControlPanelPlace(AccountAttributeDetailPage.PAGE_ID); Integer accountAttrId
 * = uAccountAttribute.AccountAttributeId().fromRecord(event.getRecord());
 * place.getToken().addParam(AccountAttributeDetailPage.ACCOUNT_ATRR_ID,
 * accountAttrId); clientFactory.goTo(place); } }); } addMember(listGrid);
 * 
 * // 添加刷新按钮 header.createRefreshButton(listGrid);
 * 
 * ToolStripButton addButton = clientUtils.createToolStripAddButton();
 * header.getToolStrip().addButton(addButton);
 * 
 * addButton.addClickHandler(new ClickHandler() {
 * 
 * @Override public void onClick(ClickEvent event) { clientFactory.goTo(new
 * ControlPanelPlace(AccountAttributeAddPage.PAGE_ID)); } });
 * 
 * //添加删除按钮 final ToolStripButton deleteButton =
 * clientUtils.createToolStripDeleteButton(); deleteButton.addClickHandler(new
 * ClickHandler() {
 * 
 * @Override public void onClick(ClickEvent event) {
 * 
 * final List<String> keys = new ArrayList<String>(); ListGridRecord[] records =
 * listGrid.getSelectedRecords(); if(records.length > 0){
 * 
 * if(clientUtils.confirmWindow()){
 * 
 * for (ListGridRecord record : records) {
 * 
 * String key =
 * uAccountAttribute.AccountAttributeId().fromRecord(record).toString();
 * keys.add(key); }
 * 
 * RPCTemplate.call(new RPCExecutor<Void>() {
 * 
 * @Override public void execute(AsyncCallback<Void> callback) {
 * 
 * server.deleteAccountAttr(keys, callback); }
 * 
 * @Override public void onSuccess(Void result) {
 * 
 * clientUtils.showSuccess(); listGrid.invalidateCache(); }
 * 
 * }, listGrid); } }else clientUtils.promptForDelete(); } });
 * header.addToolStripButton(deleteButton); }
 */
