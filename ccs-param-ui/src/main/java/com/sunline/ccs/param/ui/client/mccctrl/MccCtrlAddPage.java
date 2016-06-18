package com.sunline.ccs.param.ui.client.mccctrl;

import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UMccCtrl;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.core.client.Flat;
import com.sunline.kylin.web.core.client.mvp.Token;
import com.sunline.kylin.web.core.client.res.SavePage;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.IClickEventListener;


@Singleton
public class MccCtrlAddPage extends SavePage {
	
	 private KylinForm form;
	 
	 @Inject private UMccCtrl uMccCtrl;
	 
	@Override
	public IsWidget createPage() {
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");
		panel.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
		
		form = new KylinForm();
        form.getSetting().labelWidth(115);
        form.setField(uMccCtrl.Mcc().required(true).asSelectItem().setNewline(true),
        		uMccCtrl.InputSource().required(true).setNewline(true),
        		uMccCtrl.MaxTxnAmtLcl().required(true).setNewline(true),
        		uMccCtrl.MaxTxnAmtFrg().required(true).setNewline(true),
        		uMccCtrl.ValidInd().setNewline(true).asCheckBoxItem(),
        		uMccCtrl.BonusPntInd().setNewline(true).asCheckBoxItem());  
        form.setCol(1);
        //动态获取MCC下拉框 
        RPC.ajax("rpc/pcmSelectOptionServer/getMcc", new RpcCallback<Data>() {

     		@Override
     		public void onSuccess(Data result) {  			
     			SelectItem<String> mccSelect = new SelectItem<String>();
     			mccSelect.setValue(result.asListData());
     			 form.setFieldSelectData(uMccCtrl.Mcc().getName(), mccSelect);
     		}
     	});	
        panel.add(form);
        
        KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
        	if(!form.valid()){
             	    return;
             	}
            	MapData formData = form.getSubmitData().asMapData();          	            	
            	 
                RPC.ajax("rpc/mccCtrlServer/addMccCtrl", new RpcCallback<Data>()
                {
                    @Override
                    public void onSuccess(Data result)
                    {
                        notice(false);
                        Dialog.tipNotice("添加成功！");
                        Token token = Flat.get().getCurrentToken();
                        token.directPage(MccCtrlPage.class);
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
                token.directPage(MccCtrlPage.class);
                Flat.get().goTo(token);
            }
        });
        
        addButton(submitBtn);
        addButton(cBtn);
		return panel;
	}
	
	@Override
    public void refresh()
    {
   		form.getUi().clear();
   		
    }

}
