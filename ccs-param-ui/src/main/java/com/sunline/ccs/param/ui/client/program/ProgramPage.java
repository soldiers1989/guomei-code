package com.sunline.ccs.param.ui.client.program;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UProgram;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.Flat;
import com.sunline.kylin.web.core.client.authority.ResourceUnit;
import com.sunline.kylin.web.core.client.mvp.Token;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.ListData;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.core.client.util.DataUtil;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.dialog.client.listener.ConfirmCallbackFunction;
import com.sunline.ui.event.client.EventObjectHandler;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.IDblClickRowEventListener;

/**
 * 国家代码页面
 * 
 * @author lindh
 * @version [版本号, Jun 4, 2015]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
@Singleton
public class ProgramPage extends Page
{
    @Inject
    private UProgram uProgram;
    
    private KylinGrid grid = new KylinGrid();
    
    private KylinForm form;
    
    private ResourceUnit deleteBtnAuth = new ResourceUnit(this, "deleteBtnAuth");
    
    @Override
    public IsWidget createPage()
    {
        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        panel.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
        
        form = new KylinForm();
        form.getSetting().labelWidth(100);
        
        // 搜索列表
        form.setField(uProgram.LoanPlanId().asSelectItem(),uProgram.ProgramId());
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
        //if (Authorization.accredit(ProgramDetailPage.class))
        //{
            grid.addDblClickListener(new IDblClickRowEventListener()
            {
                @Override
                public void onDblClickRow(MapData data, String rowid, EventObjectHandler row)
                {
                    String programId = data.getString(uProgram.ProgramId().getName());
                    Token token = Flat.get().getCurrentToken();
                    token.addParam(uProgram.ProgramId().getName(), programId);
                    token.directPage(ProgramDetailPage.class);
                    Flat.get().goTo(token);
                }
            });
        //}
        
        // 增加按钮
        //if (Authorization.accredit(CountryCdAddPage.class))
        //{
            grid.setHeader(ClientUtils.createAddItem(new IClickEventListener()
            {
                @Override
                public void onClick()
                {
                    Token token = Flat.get().getCurrentToken();
                    token.directPage(ProgramAddPage.class);
                    Flat.get().goTo(token);
                }
            }));
        //}
        
        // 删除按钮
        //if (Authorization.accredit(deleteBtnAuth))
        //{
            grid.setHeader(ClientUtils.createDeleteItem(new IClickEventListener()
            {
                @Override
                public void onClick()
                {
                    delete();
                }
            }));
        //}
        
        // 显示列表名
        grid.setColumns(uProgram.LoanPlanId(),
        		 uProgram.ProgramId(), 
        		 uProgram.ProgramDesc(),
        		 uProgram.ProgramStartDate().format("yyyy-MM-dd"),
        		 uProgram.ProgramEndDate().format("yyyy-MM-dd"),
        		 uProgram.ProgramStatus());
        
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
                            keys.add(listData.get(i).asMapData().getString(uProgram.ProgramId().getName()));
                        }
                        RPC.ajax("rpc/programServer/deleteProgram", new RpcCallback<Data>()
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
    
    @Override
    public void refresh()
    {
        form.getUi().clear();
        grid.loadDataFromUrl("rpc/programServer/getProgramList");
        RPC.ajax("rpc/ccsSelectOptionServer/getLoanPlanForSelect", new RpcCallback<Data>(){

	    @Override
	    public void onSuccess(Data result) {
	    if(result !=null){
	    	Map <String,String> map = (Map<String, String>) result.asMapData().toMap();
	    	LinkedHashMap<String,String> selectItem = new LinkedHashMap<String,String>();
	    	selectItem.putAll(map);
			SelectItem<String> si = new SelectItem<String>();
			si.setValue(selectItem);
			form.setFieldSelectData(uProgram.LoanPlanId().getName(), si);
		    }
	    }
	});
    }

}

/*
 * import com.sunline.pcm.ui.common.client.ClientUtils; import
 * com.sunline.pcm.ui.common.client.DispatcherPage; import
 * com.sunline.pcm.ui.common.client.GridHeader; import
 * com.sunline.pcm.ui.cp.client.panel.ControlPanelPlace; import
 * com.sunline.ccs.infrastructure.client.ui.UProgram; import
 * com.sunline.ccs.param.def.enums.CPSAuthority; import
 * com.sunline.ccs.param.ui.client.util.CpsSelectOptionUtils; import
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
 * com.smartgwt.client.widgets.form.fields.SelectItem; import
 * com.smartgwt.client.widgets.grid.ListGrid; import
 * com.smartgwt.client.widgets.grid.ListGridRecord; import
 * com.smartgwt.client.widgets.grid.events.RecordDoubleClickEvent; import
 * com.smartgwt.client.widgets.grid.events.RecordDoubleClickHandler; import
 * com.smartgwt.client.widgets.toolbar.ToolStripButton;
// */
//@Singleton
//public class ProgramPage extends Page {
//
//	@Override
//	public IsWidget createPage() {
//		VerticalPanel panel = new VerticalPanel();
//		panel.setWidth("100%");
//		panel.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
//		return panel;
//	}
//
//	/**
//	 * 刷新结果列表数据
//	 */
//	@Override
//	public void refresh() {}
	/*
	 * public static final String PAGE_ID = "auth-loanProgram";
	 * 
	 * private YakDynamicForm searchForm;
	 * 
	 * private ListGrid listGrid;
	 * 
	 * @Inject private ClientFactory clientFactory;
	 * 
	 * @Inject private ClientUtils clientUtils;
	 * 
	 * @Inject private UProgram uProgram;
	 * 
	 * @Inject private ProgramConstants constants;
	 * 
	 * @Inject private ProgramInterAsync server;
	 * 
	 * @Inject private CpsSelectOptionUtils selectUtils;
	 * 
	 * private SelectItem loanPlanIdItem; public ProgramPage() {
	 * 
	 * super(PAGE_ID, true, CPSAuthority.CPSProgram); }
	 * 
	 * @Override public void updateView(ParamMapToken token) {
	 * 
	 * listGrid.invalidateCache(); listGrid.fetchData();
	 * 
	 * selectUtils.setLoanCdForType(loanPlanIdItem); }
	 * 
	 * @Override public String getTitle() {
	 * 
	 * return constants.program(); }
	 * 
	 * @Override public ImageResource getIcon() { return null; }
	 * 
	 * @Override protected void createCanvas() {
	 * 
	 * GridHeader header = clientUtils.createGridHeader(); { searchForm = new
	 * YakDynamicForm(); { searchForm.setNumCols(4); searchForm.setWidth100();
	 * 
	 * searchForm.setItems(loanPlanIdItem =
	 * uProgram.LoanPlanId().addClearPicker().createSelectItem(),
	 * uProgram.ProgramId().addClearPicker().createFormItem());
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
	 * listGrid.setFields(uProgram.LoanPlanId().width(150).createLGField(),
	 * uProgram.ProgramId().width(150).createLGField(),
	 * uProgram.ProgramDesc().width("*").createLGField(),
	 * uProgram.ProgramStartDate().showTime(false).width(120).createLGField(),
	 * uProgram.ProgramEndDate().showTime(false).width(120).createLGField(),
	 * uProgram.ProgramStatus().width(150).createLGField() );
	 * 
	 * YakDataSource ds = new YakDataSource() {
	 * 
	 * @Override public void fetchData(FetchRequest fetchRequest,
	 * AsyncCallback<FetchResponse> callback) {
	 * 
	 * server.getProgramList(fetchRequest, callback); } };
	 * 
	 * ds.setFields(uProgram.LoanPlanId().createField(),
	 * uProgram.ProgramId().createField(), uProgram.ProgramDesc().createField(),
	 * uProgram.ProgramStartDate().createField(),
	 * uProgram.ProgramEndDate().createField(),
	 * uProgram.ProgramStatus().createField() );
	 * 
	 * listGrid.setDataSource(ds);
	 * 
	 * listGrid.addRecordDoubleClickHandler(new RecordDoubleClickHandler() {
	 * 
	 * @Override public void onRecordDoubleClick(RecordDoubleClickEvent event) {
	 * 
	 * String programId = uProgram.ProgramId().fromRecord(event.getRecord());
	 * ControlPanelPlace place = new
	 * ControlPanelPlace(ProgramDetailPage.PAGE_ID);
	 * place.getToken().addParam(ProgramDetailPage.PROGRAMID, programId);
	 * clientFactory.goTo(place); } }); } addMember(listGrid);
	 * 
	 * header.createRefreshButton(listGrid);
	 * 
	 * // 添加 按钮 ToolStripButton addButton =
	 * clientUtils.createToolStripAddButton(); addButton.addClickHandler(new
	 * ClickHandler() {
	 * 
	 * @Override public void onClick(ClickEvent event) { clientFactory.goTo(new
	 * ControlPanelPlace(ProgramAddPage.PAGE_ID));
	 * 
	 * } }); header.addToolStripButton(addButton);
	 * 
	 * final ToolStripButton deleteButton =
	 * clientUtils.createToolStripDeleteButton();
	 * deleteButton.addClickHandler(new ClickHandler() {
	 * 
	 * @Override public void onClick(ClickEvent event) {
	 * 
	 * final List<String> keys = new ArrayList<String>(); ListGridRecord[]
	 * records = listGrid.getSelectedRecords(); if(records.length > 0){
	 * 
	 * if(clientUtils.confirmWindow()){
	 * 
	 * for (ListGridRecord record : records) {
	 * 
	 * String key = uProgram.ProgramId().fromRecord(record); keys.add(key); }
	 * 
	 * RPCTemplate.call(new RPCExecutor<Void>() {
	 * 
	 * @Override public void execute(AsyncCallback<Void> callback) {
	 * 
	 * server.deleteProgram(keys, callback); }
	 * 
	 * @Override public void onSuccess(Void result) {
	 * 
	 * clientUtils.showSuccess(); listGrid.invalidateCache(); }
	 * 
	 * }, listGrid); } }else clientUtils.promptForDelete(); } });
	 * header.addToolStripButton(deleteButton); }
	 */
//}
