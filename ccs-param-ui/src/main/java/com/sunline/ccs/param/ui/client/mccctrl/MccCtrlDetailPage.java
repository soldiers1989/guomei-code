package com.sunline.ccs.param.ui.client.mccctrl;

import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UMccCtrl;
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


/**
* @author liuky
 * 
 */
@Singleton
public class MccCtrlDetailPage extends SavePage {

     private KylinForm form;
	 
	 @Inject private UMccCtrl uMccCtrl;
	 
	 private String key;
	 
	@Override
	public IsWidget createPage() {
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");
		panel.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
		
		form = new KylinForm();
		form.getSetting().labelWidth(115);
        form.setField(uMccCtrl.Mcc().required(true).readonly(true).setNewline(true),
        		uMccCtrl.InputSource().required(true).readonly(true).setNewline(true),
        		uMccCtrl.MaxTxnAmtLcl().required(true).setNewline(true),
        		uMccCtrl.MaxTxnAmtFrg().required(true).setNewline(true),
        		uMccCtrl.ValidInd().setNewline(true).asCheckBoxItem(),
        		uMccCtrl.BonusPntInd().setNewline(true).asCheckBoxItem());  
        form.setCol(1);
        
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
                RPC.ajax("rpc/mccCtrlServer/updateMccCtrl", new RpcCallback<Data>()
                {
                    @Override
                    public void onSuccess(Data result)
                    {
                        notice(false);
                        Dialog.tipNotice("修改成功！");
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
   		notice(true);
		
		key = 
				Flat.get().getCurrentToken().getParam("MccCtrlKey");
		
		RPC.ajax("rpc/mccCtrlServer/getMccCtrl", new RpcCallback<Data>() {
			@Override
			public void onSuccess(Data arg0) {		
				form.setFormData(arg0);
			}
		}, key);
    }
	
}
