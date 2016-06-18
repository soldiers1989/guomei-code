package com.sunline.ccs.param.ui.client.pointplan;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UPointPlan;
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

@Singleton
public class PointPlanPage extends Page {

	@Inject
	private UPointPlan uPointPlan;
	
	private KylinForm form;
	
	private KylinGrid grid = new KylinGrid();
	
	private ResourceUnit deleteBtnAuth = new ResourceUnit(this, "deleteBtnAuth");
	
	@Override
	public IsWidget createPage() {
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");
		panel.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
		
		form = new KylinForm();
        form.getSetting().labelWidth(120);
		
        // 搜索列表
        form.setField(uPointPlan.PlanNbr());
        
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
        if (Authorization.accredit(PointPlanDetailPage.class))
        {
            grid.addDblClickListener(new IDblClickRowEventListener()
            {
                @Override
                public void onDblClickRow(MapData data, String rowid, EventObjectHandler row)
                {
                    String plan = data.getString(uPointPlan.PlanNbr().getName());
                    Token token = Flat.get().getCurrentToken();
                    token.addParam(uPointPlan.PlanNbr().getName(), plan);
                    token.directPage(PointPlanDetailPage.class);
                    Flat.get().goTo(token);
                }
            });
        }
        
        // 增加按钮
        if (Authorization.accredit(PointPlanAddPage.class))
        {
            grid.setHeader(ClientUtils.createAddItem(new IClickEventListener()
            {
                @Override
                public void onClick()
                {
                    Token token = Flat.get().getCurrentToken();
                    token.directPage(PointPlanAddPage.class);
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
        grid.setColumns(uPointPlan.PlanNbr(),
            uPointPlan.Description(),
            uPointPlan.PlanType());
        
        
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
                            keys.add(listData.get(i).asMapData().getString(uPointPlan.PlanNbr().getName()));
                        }
                        RPC.ajax("rpc/pointPlanServer/deletePointPlan", new RpcCallback<Data>()
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
	public void refresh() 
	{
		form.getUi().clear();
        grid.loadDataFromUrl("rpc/pointPlanServer/getPointPlanList");
	}
	
}
