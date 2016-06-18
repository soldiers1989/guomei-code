package com.sunline.ccs.param.ui.client.merchantTxnCrtl;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UMerchantTxnCrtl;
import com.sunline.ccs.param.ui.client.currencyCtrl.CurrencyCtrlDetailPage;
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
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.ListData;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.dialog.client.listener.ConfirmCallbackFunction;
import com.sunline.ui.event.client.EventObjectHandler;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.IDblClickRowEventListener;

/*
 * import com.sunline.pcm.ui.common.client.ClientUtils; import
 * com.sunline.pcm.ui.common.client.DispatcherPage; import
 * com.sunline.pcm.ui.common.client.GridHeader; import
 * com.sunline.pcm.ui.cp.client.panel.ControlPanelPlace; import
 * com.sunline.ccs.infrastructure.client.ui.UMerchantTxnCrtl; import
 * com.sunline.ccs.param.def.enums.CPSAuthority; import
 * com.sunline.ark.gwt.client.datasource.YakDataSource; import
 * com.sunline.ark.gwt.client.mvp.ParamMapToken; import
 * com.sunline.ark.gwt.client.ui.YakDynamicForm; import
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
 * com.smartgwt.client.types.SelectionStyle; import
 * com.smartgwt.client.widgets.events.ClickEvent; import
 * com.smartgwt.client.widgets.events.ClickHandler; import
 * com.smartgwt.client.widgets.form.events.SubmitValuesEvent; import
 * com.smartgwt.client.widgets.form.events.SubmitValuesHandler; import
 * com.smartgwt.client.widgets.grid.ListGrid; import
 * com.smartgwt.client.widgets.grid.ListGridRecord; import
 * com.smartgwt.client.widgets.grid.events.RecordDoubleClickEvent; import
 * com.smartgwt.client.widgets.grid.events.RecordDoubleClickHandler; import
 * com.smartgwt.client.widgets.toolbar.ToolStripButton;
 */
@Singleton
public class MerchantTxnCrtlPage extends Page {
	@Inject
    private UMerchantTxnCrtl uMerchantTxnCrtl;
    
    private KylinGrid grid = new KylinGrid();
    
    private KylinForm form;
    
    private ResourceUnit deleteBtnAuth = new ResourceUnit(this, "deleteBtnAuth");
    

	@Override
	public IsWidget createPage() {
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");
		panel.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
		
		form = new KylinForm();
        form.getSetting().labelWidth(70);
        
        // 搜索列表
        form.setField(uMerchantTxnCrtl.MerchantId(),uMerchantTxnCrtl.MerchantName());
        form.setCol(2);
        
        // 查询按钮
        form.addButton(ClientUtils.createSearchButton(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
                grid.loadData(form);
            }
        }));
        
        // 记录显示网格
        grid.setWidth("98%");
        grid.setHeight("98%");
        
        // 刷新按钮
        grid.setHeader(ClientUtils.createRefreshItem(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
                grid.loadData(form);
            }
        }));
        
        // 编辑按钮
        if (Authorization.accredit(MerchantTxnCrtlDetailPage.class))
        {
            grid.addDblClickListener(new IDblClickRowEventListener()
            {
                @Override
                public void onDblClickRow(MapData data, String rowid, EventObjectHandler row)
                {
                    String merchantId = data.getString(uMerchantTxnCrtl.MerchantId().getName());
                    Token token = Flat.get().getCurrentToken();
                    token.addParam(uMerchantTxnCrtl.MerchantId().getName(), merchantId);
                    token.directPage(MerchantTxnCrtlDetailPage.class);
                    Flat.get().goTo(token);
                }
            });
        }
        
        // 增加按钮
        if (Authorization.accredit(MerchantTxnCrtlAddPage.class))
        {
            grid.setHeader(ClientUtils.createAddItem(new IClickEventListener()
            {
                @Override
                public void onClick()
                {
                    Token token = Flat.get().getCurrentToken();
                    token.directPage(MerchantTxnCrtlAddPage.class);
                    Flat.get().goTo(token);
                }
            }));
        }
        
        // 删除按钮
        if (Authorization.accredit(deleteBtnAuth))
        {
            grid.setHeader(ClientUtils.createDeleteItem(new IClickEventListener()
            {
                @Override
                public void onClick()
                {
                    delete();
                }
            }));
        }
        
        // 显示列表名
        grid.setColumns(uMerchantTxnCrtl.MerchantId(),
        		uMerchantTxnCrtl.MerchantName(),
        		uMerchantTxnCrtl.CountryCd());
        
        panel.add(form);
        panel.add(grid);
		
		return panel;
	}
	
	
	
	
	 
    public void delete()
    {
        if (grid.getGrid().getSelectedRows().size() <= 0)
        {
            Dialog.alert("请选择您需要删除的记录");
        }
        else
        {
            Dialog.confirm("是否确认要删除？", "提示", new ConfirmCallbackFunction()
            {
                @Override
                public void corfimCallback(boolean value)
                {
                    if (value)
                    {
                        final List<String> keys = new ArrayList<String>();
                        ListData listData = grid.getGrid().getSelectedRows();
                        for (int i = 0; i < listData.size(); i++)
                        {
                            keys.add(listData.get(i).asMapData().getString( uMerchantTxnCrtl.MerchantId().getName()));
                        }
                        RPC.ajax("rpc/merchantTxnCrtlServer/deleteMerchantTxnCrtl", new RpcCallback<Data>()
                        {
                            @Override
                            public void onSuccess(Data result)
                            {
                                Dialog.tipNotice("删除成功！");
                                grid.loadData(form);
                            }
                        }, keys);
                    }
                }
            });
        }
    }
    

	/**
	 * 刷新结果列表数据
	 */
	@Override
	public void refresh() {
		form.getUi().clear();
        grid.loadDataFromUrl("rpc/merchantTxnCrtlServer/getMerchantTxnCrtlList");
	}
	/*
	 * public static final String PAGE_ID = "auth-merchantTxnCrtl";
	 * 
	 * private YakDynamicForm searchForm;
	 * 
	 * private ListGrid listGrid;
	 * 
	 * @Inject private ClientFactory clientFactory;
	 * 
	 * @Inject private ClientUtils clientUtils;
	 * 
	 * @Inject private UMerchantTxnCrtl uMerchantTxnCrtl;
	 * 
	 * @Inject private MerchantTxnCrtlConstants constants;
	 * 
	 * @Inject private MerchantTxnCrtlInterAsync server;
	 * 
	 * public MerchantTxnCrtlPage() {
	 * 
	 * super(PAGE_ID, true, CPSAuthority.CPSMerchantTxncrtl); }
	 * 
	 * @Override public void updateView(ParamMapToken token) {
	 * 
	 * listGrid.invalidateCache(); listGrid.fetchData(); }
	 * 
	 * @Override public String getTitle() {
	 * 
	 * return constants.merchantTxnCrtl(); }
	 * 
	 * @Override public ImageResource getIcon() { // TODO Auto-generated method
	 * stub return null; }
	 * 
	 * @Override protected void createCanvas() {
	 * 
	 * GridHeader header = clientUtils.createGridHeader(); { searchForm = new
	 * YakDynamicForm(); { searchForm.setNumCols(4); searchForm.setWidth100();
	 * 
	 * searchForm.setItems(uMerchantTxnCrtl.MerchantId().addClearPicker().
	 * createFormItem(),
	 * uMerchantTxnCrtl.MerchantName().addClearPicker().createFormItem());
	 * searchForm.addSubmitValuesHandler(new SubmitValuesHandler() {
	 * 
	 * @Override public void onSubmitValues(SubmitValuesEvent event) {
	 * 
	 * listGrid.filterData(searchForm.getValuesAsCriteria()); } }); }
	 * header.createSearchArea(searchForm); } addMember(header);
	 * 
	 * listGrid = new ListGrid(); { listGrid.setAutoFetchData(true);
	 * listGrid.setWidth100(); listGrid.setHeight100();
	 * listGrid.setSelectionType(SelectionStyle.SIMPLE);
	 * listGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
	 * listGrid.setDataFetchMode(FetchMode.LOCAL);
	 * listGrid.setFields(uMerchantTxnCrtl
	 * .MerchantId().width(150).createLGField(),
	 * uMerchantTxnCrtl.MerchantName().width(200).createLGField(),
	 * uMerchantTxnCrtl.CountryCd().width("*").createLGField());
	 * 
	 * YakDataSource ds = new YakDataSource() {
	 * 
	 * @Override public void fetchData(FetchRequest fetchRequest,
	 * AsyncCallback<FetchResponse> callback) {
	 * 
	 * server.getMerchantTxnCrtlList(fetchRequest, callback); } };
	 * ds.setFields(uMerchantTxnCrtl.MerchantId().createField(),
	 * uMerchantTxnCrtl.MerchantName().createField(),
	 * uMerchantTxnCrtl.CountryCd().createField());
	 * 
	 * listGrid.setDataSource(ds);
	 * 
	 * listGrid.addRecordDoubleClickHandler(new RecordDoubleClickHandler() {
	 * 
	 * @Override public void onRecordDoubleClick(RecordDoubleClickEvent event) {
	 * 
	 * String merchantId =
	 * uMerchantTxnCrtl.MerchantId().fromRecord(event.getRecord());
	 * ControlPanelPlace place = new
	 * ControlPanelPlace(MerchantTxnCrtlDetailPage.PAGE_ID);
	 * place.getToken().addParam(MerchantTxnCrtlDetailPage.MERCHANTID,
	 * merchantId); clientFactory.goTo(place); } }); } addMember(listGrid);
	 * 
	 * header.createRefreshButton(listGrid);
	 * 
	 * // 添加 按钮 ToolStripButton addButton =
	 * clientUtils.createToolStripAddButton(); addButton.addClickHandler(new
	 * ClickHandler() {
	 * 
	 * @Override public void onClick(ClickEvent event) {
	 * 
	 * clientFactory.goTo(new
	 * ControlPanelPlace(MerchantTxnCrtlAddPage.PAGE_ID));
	 * 
	 * } }); header.addToolStripButton(addButton);
	 * 
	 * //添加删除按钮 final ToolStripButton deleteButton =
	 * clientUtils.createToolStripDeleteButton();
	 * deleteButton.addClickHandler(new ClickHandler() {
	 * 
	 * @Override public void onClick(ClickEvent event) {
	 * 
	 * final List<String> keys = new ArrayList<String>(); ListGridRecord[]
	 * records = listGrid.getSelectedRecords(); if(records.length > 0){
	 * if(clientUtils.confirmWindow()){
	 * 
	 * for (ListGridRecord record : records) { String key =
	 * uMerchantTxnCrtl.MerchantId().fromRecord(record); keys.add(key); }
	 * 
	 * RPCTemplate.call(new RPCExecutor<Void>() {
	 * 
	 * @Override public void execute(AsyncCallback<Void> callback) {
	 * 
	 * server.deleteMerchantTxnCrtl(keys, callback); }
	 * 
	 * @Override public void onSuccess(Void result) {
	 * 
	 * clientUtils.showSuccess(); listGrid.invalidateCache(); }
	 * 
	 * }, listGrid); } }else clientUtils.promptForDelete(); } });
	 * header.addToolStripButton(deleteButton); }
	 */
}
