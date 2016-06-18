package com.sunline.ccs.param.ui.client.countryctrl;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCountryCtrl;
import com.sunline.kylin.web.ark.client.helper.BooleanColumnHelper;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
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
import com.sunline.ui.checkBoxList.client.CheckBoxListSetting;
import com.sunline.ui.core.client.common.Editor;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.ListData;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.core.client.enums.EditorType;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.dialog.client.listener.ConfirmCallbackFunction;
import com.sunline.ui.event.client.EventObjectHandler;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.IColumnRenderFunctionListener;
import com.sunline.ui.grid.client.listener.IDblClickRowEventListener;

/*
 * import com.sunline.pcm.ui.common.client.ClientUtils; import
 * com.sunline.pcm.ui.common.client.DispatcherPage; import
 * com.sunline.pcm.ui.common.client.GridHeader; import
 * com.sunline.pcm.ui.cp.client.panel.ControlPanelPlace; import
 * com.sunline.ccs.infrastructure.client.ui.UCountryCtrl; import
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
/**
 * 国家授权控制页面
 * 
 * @author guxiaoyu
 * 
 */
@Singleton
public class AuthCountryListPage extends Page {
    @Inject
    private AuthCountryConstants constants;
    @Inject
    private UCountryCtrl uCountryCtrl;
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
	form.setField(uCountryCtrl.CountryCode());
	form.setCol(1);

	// 查询按钮
	form.addButton(ClientUtils.createSearchButton(new IClickEventListener(){
	    @Override
	    public void onClick() {
		grid.loadData(form);
	    }
	}));

	// 记录显示网格
	grid.setWidth("98%");
	grid.setHeight("98%");

	// 刷新按钮
	grid.setHeader(ClientUtils.createRefreshItem(new IClickEventListener(){
	    @Override
	    public void onClick() {
		grid.loadData(form);
	    }
	}));

	// 编辑按钮
	if (Authorization.accredit(AuthCountryDetailPage.class)) {
	    grid.addDblClickListener(new IDblClickRowEventListener(){
		@Override
		public void onDblClickRow(MapData data, String rowid, EventObjectHandler row) {
		    String countryCode = data.getString(uCountryCtrl.CountryCode().getName());
		    Token token = Flat.get().getCurrentToken();
		    token.addParam(uCountryCtrl.CountryCode().getName(), countryCode);
		    token.directPage(AuthCountryDetailPage.class);
		    Flat.get().goTo(token);
		}
	    });
	}

	// 增加按钮
	if (Authorization.accredit(AuthCountryAddPage.class)) {
	    grid.setHeader(ClientUtils.createAddItem(new IClickEventListener(){
		@Override
		public void onClick() {
		    Token token = Flat.get().getCurrentToken();
		    token.directPage(AuthCountryAddPage.class);
		    Flat.get().goTo(token);
		}
	    }));
	}

	// 删除按钮
	if (Authorization.accredit(deleteBtnAuth)) {
	    grid.setHeader(ClientUtils.createDeleteItem(new IClickEventListener(){
		@Override
		public void onClick() {
		    delete();
		}
	    }));
	}

	BooleanColumnHelper validIndItem = new BooleanColumnHelper("validInd", constants.validInd());
	validIndItem.asCheckBoxItem().render(null);
	grid.setColumns(uCountryCtrl.CountryCode(), validIndItem.readonly(true), uCountryCtrl.MaxTxnAmtLcl(),
			uCountryCtrl.MaxTxnAmtFrg());

	panel.add(form);
	panel.add(grid);

	return panel;
    }

    public void delete() {
	if (grid.getGrid().getSelectedRows().size() <= 0) {
	    Dialog.alert("请选择您需要删除的记录");
	} else {
	    Dialog.confirm("是否确认要删除？", "提示", new ConfirmCallbackFunction(){
		@Override
		public void corfimCallback(boolean value) {
		    if (value) {
			final List<String> keys = new ArrayList<String>();
			ListData listData = grid.getGrid().getSelectedRows();
			for (int i = 0; i < listData.size(); i++) {
			    keys.add(listData.get(i).asMapData().getString(uCountryCtrl.CountryCode().getName()));
			}
			RPC.ajax("rpc/authCountryServer/deleteAuthCountry", new RpcCallback<Data>(){
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

    /**
     * 刷新结果列表数据
     */
    @Override
    public void refresh() {
	form.getUi().clear();
	grid.loadDataFromUrl("rpc/authCountryServer/getAuthCountryList");
    }
}