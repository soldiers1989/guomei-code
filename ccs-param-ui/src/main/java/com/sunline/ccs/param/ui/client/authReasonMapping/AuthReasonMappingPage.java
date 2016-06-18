package com.sunline.ccs.param.ui.client.authReasonMapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.domain.AuthReasonDomainClient;
import com.sunline.ccs.infrastructure.client.ui.UAuthReasonMapping;
import com.sunline.ccs.param.def.enums.AuthReason;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
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
import com.sunline.ui.grid.client.listener.IColumnRenderFunctionListener;
import com.sunline.ui.grid.client.listener.IDblClickRowEventListener;
import com.sunline.ui.toolbar.client.ToolBarItem;

/**
 * 授权原因决定码决定页面
 * 
 * @author pangbiao
 * @version [版本号, Jun 16, 2015]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */

@Singleton
public class AuthReasonMappingPage extends Page
{

    @Inject
    private UAuthReasonMapping uAuthReasonMapping;

    private KylinGrid grid = new KylinGrid();

    private KylinForm form;

    private static final String DELIM = "|";

    private ResourceUnit deleteBtnAuth = new ResourceUnit(this, "deleteBtnAuth");
    
    private AuthReason authreason;
    
    @Inject
    private AuthReasonDomainClient authReasonDomainClient;

    /**
     * 刷新结果列表数据
     */
    @Override
    public void refresh()
    {
	form.getUi().clear();
	grid.loadDataFromUrl("rpc/authReasonMappingServer/getAuthReasonMappingList");
    }

    @Override
    public IsWidget createPage()
    {
	VerticalPanel panel = new VerticalPanel();
	panel.setWidth("100%");
	panel.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);

	form = new KylinForm();
	form.getSetting().labelWidth(100);

	// 搜索列表
	form.setField(uAuthReasonMapping.InputSource().asSelectItem(SelectType.KEY_LABLE), 
	              uAuthReasonMapping.Reason().asSelectItem(SelectType.KEY_LABLE)
	              );
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
	if (Authorization.accredit(AuthReasonMappingDetailPage.class))
	{
	    grid.addDblClickListener(new IDblClickRowEventListener()
	    {

		@Override
		public void onDblClickRow(MapData data, String rowid, EventObjectHandler row)
		{
		    String key =
			    data.getString(uAuthReasonMapping.InputSource().getName()) + "|"
				    + data.getString(uAuthReasonMapping.Reason().getName());
		    Token token = Flat.get().getCurrentToken();
		    token.addParam("key", key);
		    token.directPage(AuthReasonMappingDetailPage.class);
		    Flat.get().goTo(token);
		}
	    });
	}

	// 增加按钮
	if (Authorization.accredit(AuthReasonMappingAddPage.class))
	{
	    grid.setHeader(ClientUtils.createAddItem(new IClickEventListener()
	    {
		@Override
		public void onClick()
		{
		    Token token = Flat.get().getCurrentToken();
		    token.directPage(AuthReasonMappingAddPage.class);
		    Flat.get().goTo(token);
		}
	    }));
	}
	// 删除按钮
	ToolBarItem deleteButton = ClientUtils.createDeleteItem(new IClickEventListener()
	{
	    @Override
	    public void onClick()
	    {
		deleteuAuthReasonMapping();
	    }
	});

	Authorization.accredit(deleteButton, new ResourceUnit(this, "deleteBtnAuth"));
	grid.setHeader(deleteButton);
	grid.setColumns();
	


	// 显示列表名
	grid.setColumns(uAuthReasonMapping.InputSource(),
	                uAuthReasonMapping.Reason().columnRender(new IColumnRenderFunctionListener(){

		    			@Override
		    			public String render(MapData rowdata, int rowindex, String value, EventObjectHandler column)
		    			{
		    				LinkedHashMap<String,String> authReasonMap = authReasonDomainClient.asLinkedHashMap(true);
		    			    String itemVal = authReasonMap.get(value);
		    			    return itemVal == null ? value : itemVal;
		    			}
					}));

	panel.add(form);
	panel.add(grid);
	return panel;
    }

    public void deleteuAuthReasonMapping()
    {
	if (grid.getGrid().getSelectedRows().size() <= 0)
	{
	    Dialog.alert("请选择您需要删除的授权原因决定码");
	} else
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
			    MapData md = listData.get(i).asMapData();
			    keys.add(md.getString(uAuthReasonMapping.InputSource().getName()) + DELIM
				    + md.getString(uAuthReasonMapping.Reason().getName()));
			}
			RPC.ajax("rpc/authReasonMappingServer/deleteAuthReasonMapping", new RpcCallback<Data>()
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
}
