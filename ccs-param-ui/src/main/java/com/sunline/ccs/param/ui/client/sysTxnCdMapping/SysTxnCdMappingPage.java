package com.sunline.ccs.param.ui.client.sysTxnCdMapping;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.USysTxnCdMapping;
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
/**
 * 
 * 系统交易码映射管理页面
 * 
 * @Date:Jun 17, 2015
 * @author RuiHua.Zhong
 * 
 * 修改记录： 
 * [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 *
 */
@Singleton
public class SysTxnCdMappingPage extends Page {
	
	 @Inject
	 private USysTxnCdMapping uSysTxnCdMapping;
	
	 private KylinGrid grid = new KylinGrid();
	
	 private KylinForm form;
	
	 private ResourceUnit deleteBtnAuth = new ResourceUnit(this, "deleteBtnAuth");
	
	@Override
	public IsWidget createPage() {
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");
		panel.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
		form = new KylinForm();
	    form.getSetting().labelWidth(100);
	        
	        // 搜索列表
	    form.setField(uSysTxnCdMapping.SysTxnCd(), uSysTxnCdMapping.TxnCd());
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
        // 增加按钮
        if (Authorization.accredit(SysTxnCdMappingAddPage.class))
        {
            grid.setHeader(ClientUtils.createAddItem(new IClickEventListener()
            {
                @Override
                public void onClick()
                {
                    Token token = Flat.get().getCurrentToken();
                    token.directPage(SysTxnCdMappingAddPage.class);
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
        // 编辑按钮
        if (Authorization.accredit(SysTxnCdMappingDetailPage.class))
        {
            grid.addDblClickListener(new IDblClickRowEventListener()
            {
                @Override
                public void onDblClickRow(MapData data, String rowid, EventObjectHandler row)
                {
                    String uSysTxnCd = data.getString(uSysTxnCdMapping.SysTxnCd().getName());
                    Token token = Flat.get().getCurrentToken();
                    token.addParam(uSysTxnCdMapping.SysTxnCd().getName(), uSysTxnCd);
                    token.directPage(SysTxnCdMappingDetailPage.class);
                    Flat.get().goTo(token);
                }
            });
        }
       grid.setColumns(
    		   uSysTxnCdMapping.SysTxnCd(),
    		   uSysTxnCdMapping.TxnCd()
    		   );
	    panel.add(form);
	    panel.add(grid);
		return panel;
	}

	/**
	 * 刷新结果列表数据
	 */
	@Override
	public void refresh() {
		form.getUi().clear();
        grid.loadDataFromUrl("rpc/sysTxnCdServer/getSysTxnCdMappingList");
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
                            keys.add(listData.get(i).asMapData().getString(uSysTxnCdMapping.SysTxnCd().getName()));
                        }
                        RPC.ajax("rpc/sysTxnCdServer/deleteSysTxnCdMapping", new RpcCallback<Data>()
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
