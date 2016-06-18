package com.sunline.ccs.param.ui.client.interestTable;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UInterestTable;
import com.sunline.ccs.infrastructure.client.ui.URateDef;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.Flat;
import com.sunline.kylin.web.core.client.mvp.Token;
import com.sunline.kylin.web.core.client.res.SavePage;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ui.core.client.common.Editor;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.ListData;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.core.client.enums.EditorType;
import com.sunline.ui.core.client.util.DataUtil;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.IClickEventListener;
/**
 * 利率表明细管理页面
 * 
 * @author liuky
 * @version [版本号, Jun 18, 2015]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
@Singleton
public class InterestTableDetailPage extends SavePage {

	
	
	
	public String key;
	 /**
     * 编辑表单
     */
    private KylinForm editForm;
 
    
    private KylinGrid grid;
    
    @Inject
    private UInterestTable uInterestTable ;
	
    
    @Inject
   private URateDef uRateDef;
    
	@Override
	public IsWidget createPage() {
		
		VerticalPanel panel = new VerticalPanel();
		 grid = new KylinGrid();
        editForm = new KylinForm();
        
        editForm.setWidth("100%");
        editForm.getSetting().labelWidth(115);
        editForm.setField(uInterestTable.IntTableId().required(true).setNewline(true).readonly(true),
        		uInterestTable.BaseYear().required(true).setNewline(true),
        		uInterestTable.TierInd().required(true).setNewline(true),
        		uInterestTable.Description().asTextArea().setNewline(true)
        		);
        
        
        grid.getSetting().enabledEdit(true);
        grid.setWidth("100%");
        
        grid.setHeader( ClientUtils.createAddItem(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
               grid.getUi().addEditRow();
            }
        }
        ), 
        ClientUtils.createDeleteItem(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
                grid.getUi().deleteSelectedRow();
            }
        }));
        
        
        grid.getSetting().usePager(false);
        grid.setHeight("35%");
        
        KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
        	if(!editForm.valid()){
             	    return;
             	}
        		grid.getUi().endEdit();
            	MapData formData = editForm.getSubmitData().asMapData();
            	ListData data = grid.getData();
            	
            	formData.put("chargeRates",data);

                RPC.ajax("rpc/interestTableServer/updateInterestTable", new RpcCallback<Data>()
                {
                    @Override
                    public void onSuccess(Data result)
                    {
                        notice(false);
                        Dialog.tipNotice("修改成功！");
                        Token token = Flat.get().getCurrentToken();
                        token.directPage(InterestTablePage.class);
                        Flat.get().goTo(token);

                    }
                },formData.toMap());
            }
        });
        KylinButton cBtn = ClientUtils.creCancelButton(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
                Token token = Flat.get().getCurrentToken();
                token.directPage(InterestTablePage.class);
                Flat.get().goTo(token);
            }
        });
        addButton(submitBtn);
        addButton(cBtn);
        panel.add(editForm);
    
        grid.setColumns(
                uRateDef.Rate()
                .setLength(5).setColumnWidth("29%")
                
                .setColunmEditor(new Editor().type(EditorType.NUMBER)),
                uRateDef.RateCeil().setColumnWidth("29%")
                .setColunmEditor(new Editor().type(EditorType.NUMBER))
               
        		);
        
        panel.add(grid);
        return panel;
	}
	
	
	 @Override
	    public void refresh()
	    {
	       
		    editForm.getUi().clear();
		    
		    grid.clearData();
		    notice(true);
			key = Flat.get().getCurrentToken().getParam(uInterestTable.IntTableId().getName());
			RPC.ajax("rpc/interestTableServer/getInterestTable", new RpcCallback<Data>() {
				@Override
				public void onSuccess(Data arg0) {
					
					editForm.setFormData(arg0);
					String rateList = arg0.asMapData().getData("chargeRates").toString();
					grid.loadData(StringToData(rateList));	 
				}
			}, key);
	    }

		public Data StringToData(String gridDataString){
			Data data = new Data();
			data.setJsData(DataUtil.convertDataType(gridDataString));
			return data;
		}
	 
}
