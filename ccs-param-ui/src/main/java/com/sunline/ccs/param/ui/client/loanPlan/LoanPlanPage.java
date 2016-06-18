package com.sunline.ccs.param.ui.client.loanPlan;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.ULoanPlan;
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
import com.sunline.ui.event.client.EventObjectHandler;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.IDblClickRowEventListener;

@Singleton
public class LoanPlanPage extends Page {

	 private KylinGrid listGrid ;
	    
	 private KylinForm searchForm ;
	    
	 private ResourceUnit deleteBtnAuth = new ResourceUnit(this, "deleteBtnAuth");
	 
	 @Inject private ULoanPlan uLoanPlan;

	@Inject
	private LoanPlanConstants loanPlanConstants;
	
	@Override
	public IsWidget createPage() {
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");
		panel.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
		
		searchForm = new KylinForm();
		{
			searchForm.getSetting().labelWidth(70);
	        
	        // 搜索列表
			searchForm.setField(uLoanPlan.LoanCode(),uLoanPlan.Description());
	        
	        // 查询按钮
			searchForm.addButton(ClientUtils.createSearchButton(new IClickEventListener()
	        {
	            @Override
	            public void onClick()
	            {
                    listGrid.getGrid().setParm("loanType4Query", "loanPlan");
	            	listGrid.loadData(searchForm);
	            }
	        }));
		}
		
		listGrid = new KylinGrid();
		{
			// 记录显示网格
			listGrid.setWidth("98%");
			listGrid.setHeight("98%");
	        
	        // 刷新按钮
			listGrid.setHeader(ClientUtils.createRefreshItem(new IClickEventListener()
	        {
	            @Override
	            public void onClick()
	            {
	            	listGrid.getGrid().setParm("loanType4Query", "loanPlan");
	            	listGrid.loadData(searchForm);
	            }
	        }));
	        
	        // 编辑按钮
	        if (Authorization.accredit(LoanPlanDetailPage.class))
	        {
	        	listGrid.addDblClickListener(new IDblClickRowEventListener()
	            {
	                @Override
	                public void onDblClickRow(MapData data, String rowid, EventObjectHandler row)
	                {
	                    String loanCode = data.getString(uLoanPlan.LoanCode().getName());
	                    Token token = Flat.get().getCurrentToken();
	                    token.addParam(uLoanPlan.LoanCode().getName(), loanCode);
	                    token.directPage(LoanPlanDetailPage.class);
	                    Flat.get().goTo(token);
	                }
	            });
	        }
	        
	        // 增加按钮
	        if (Authorization.accredit(LoanPlanAddPage.class))
	        {
	        	listGrid.setHeader(ClientUtils.createAddItem(new IClickEventListener()
	            {
	                @Override
	                public void onClick()
	                {
	                    Token token = Flat.get().getCurrentToken();
	                    token.directPage(LoanPlanAddPage.class);
	                    Flat.get().goTo(token);
	                }
	            }));
	        }
	        
	        // 删除按钮
	        if (Authorization.accredit(deleteBtnAuth))
	        {
	        	listGrid.setHeader(ClientUtils.createDeleteItem(new IClickEventListener()
	            {
	                @Override
	                public void onClick()
	                {
	                	if(listGrid.getGrid().getSelectedRows().size() < 1){
							Dialog.alert(loanPlanConstants.pleaseChoose());
						}else{
							ListData listData = listGrid.getGrid().getSelectedRows();
							List<String> keys = new ArrayList<String>();
							for(int i = 0; i < listData.size(); i++) {
								keys.add(listData.get(i).asMapData().getString(uLoanPlan.LoanCode().getName()));
							}
							RPC.ajax("rpc/loanPlanServer/deleteLoanPlan", new RpcCallback<Data>(){

								@Override
								public void onFailure(Throwable caught) {
									Dialog.alert(loanPlanConstants.operationFailed());
								}

								@Override
								public void onSuccess(Data result) {
									Dialog.tipNotice("操作成功！");
									refreshGrid();
								}

							}, keys);				
						}
	                }
	            }));
	        }
	        
	        // 显示列表名
	        listGrid.setColumns(uLoanPlan.LoanCode(),
	        		uLoanPlan.LoanType().asSelectItem(SelectType.KEY_LABLE).columnRender(),
	        		uLoanPlan.Description()
	        		);
		}
		
        panel.add(searchForm);
        panel.add(listGrid);
        
        return panel;
	}

	/**
	 * 刷新结果列表数据
	 */
	@Override
	public void refresh() {
		searchForm.getUi().clear();
		refreshGrid();
	}
	public void refreshGrid(){
	    listGrid.clearData();
	    listGrid.getGrid().setParm("loanType4Query", "loanPlan");
	    listGrid.loadDataFromUrl("rpc/loanPlanServer/getLoanPlanList");
	}
}
