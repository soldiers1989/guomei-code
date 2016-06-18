package com.sunline.ccs.param.ui.client.loanParam;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.ULoanPlan;
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
import com.sunline.ui.event.client.EventObjectHandler;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.IDblClickRowEventListener;
import com.sunline.ui.toolbar.client.ToolBarItem;

@Singleton
public class LoanParamPage extends Page
{
    
    public static final String PAGE_ID = "auth-loanParam";
    
    public static final String KEY = "loanCode";
    
    private KylinForm searchForm;
    
    private KylinGrid listGrid;
    
    @Inject
    private ULoanPlan uLoanPlan;
    
    @Inject
    private LoanParamConstants constants;
    
    /**
     * 刷新结果列表数据
     */
    @Override
    public void refresh()
    {
        searchForm.getUi().clear();
    	refreshGrid();
    }
    
    @Override
    public IsWidget createPage()
    {
        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        panel.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
        
        searchForm = new KylinForm();
        {
            searchForm.setCol(4);
            searchForm.setWidth("100%");
            
            searchForm.setField(uLoanPlan.LoanCode().setDisplay(constants.loanCode()),
                uLoanPlan.Description());
            
            searchForm.addButton(ClientUtils.createSearchButton(new IClickEventListener()
            {
                @Override
                public void onClick()
                {
                    listGrid.getGrid().setParm("loanType4Query", "loanParam");
                    listGrid.loadData(searchForm);
                }
            }));
        }
        
        listGrid = new KylinGrid();
        {
            listGrid.setWidth("98%");
            listGrid.setHeight("98%");
            
            listGrid.setColumns(uLoanPlan.LoanCode().setDisplay(constants.loanCode()),
                uLoanPlan.LoanType().setDisplay(constants.loanType()),
                uLoanPlan.Description());
            
            if (Authorization.accredit(LoanParamDetailPage.class))
            {
                listGrid.addDblClickListener(new IDblClickRowEventListener()
                {
                    
                    @Override
                    public void onDblClickRow(MapData data, String rowid, EventObjectHandler row)
                    {
                        String key = data.getString(uLoanPlan.LoanCode().getName());
                        Token token = Flat.get().getCurrentToken();
                        token.addParam(KEY, key);
                        token.directPage(LoanParamDetailPage.class);
                        Flat.get().goTo(token);
                    }
                });
            }
            
            listGrid.addHeader(ClientUtils.createRefreshItem(new IClickEventListener()
            {
                @Override
                public void onClick()
                {
                	listGrid.getGrid().setParm("loanType4Query", "loanParam");
                    listGrid.loadData(searchForm);
                }
            }));
            
            // 增加按钮
            if (Authorization.accredit(LoanParamAddPage.class))
            {
                listGrid.setHeader(ClientUtils.createAddItem(new IClickEventListener()
                {
                    @Override
                    public void onClick()
                    {
                        Token token = Flat.get().getCurrentToken();
                        token.directPage(LoanParamAddPage.class);
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
                    if (listGrid.getGrid().getSelectedRows().size() < 1)
                    {
                        Dialog.alert(constants.pleaseChoose());
                    }
                    else
                    {
                        ListData listData = listGrid.getGrid().getSelectedRows();
                        List<String> keys = new ArrayList<String>();
                        for (int i = 0; i < listData.size(); i++)
                        {
                            keys.add(listData.get(i).asMapData().getString(uLoanPlan.LoanCode().getName()));
                        }
                        RPC.ajax("rpc/loanPlanServer/deleteLoanPlan", new RpcCallback<Data>()
                        {
                            
                            @Override
                            public void onFailure(Throwable caught)
                            {
                                Dialog.alert(constants.operationFailed());
                            }
                            
                            @Override
                            public void onSuccess(Data result)
                            {
                                Dialog.tipNotice("操作成功！");
                                refreshGrid();
                            }
                            
                        }, keys);
                    }
                    
                }
            });
            Authorization.accredit(deleteButton, new ResourceUnit(this, "deleteBtnAuth"));
            listGrid.setHeader(deleteButton);
        }
        
        panel.add(searchForm);
        panel.add(listGrid);
        
        return panel;
    }
    
    public void refreshGrid()
    {
        listGrid.getGrid().setParm("loanType4Query", "loanParam");
        listGrid.loadDataFromUrl("rpc/loanPlanServer/getLoanPlanList");
    }
    
}
