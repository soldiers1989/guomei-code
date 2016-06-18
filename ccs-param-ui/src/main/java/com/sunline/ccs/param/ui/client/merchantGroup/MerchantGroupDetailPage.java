package com.sunline.ccs.param.ui.client.merchantGroup;

import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UMerchantGroup;
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
public class MerchantGroupDetailPage extends SavePage {
	@Inject
    private  UMerchantGroup uMerchantGroup;
	    
	    private String key;
	    
	    private KylinForm form;
	@Override
	public IsWidget createPage() {
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");
		panel.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
	
		form = new KylinForm();
        form.getSetting().labelWidth(140);
       
        form.setField(uMerchantGroup.MerGroupId().required(true).readonly(true)
       		,uMerchantGroup.MerGroupName().required(true)
       		,uMerchantGroup.MerGroupDesc().required(true).asTextArea().setLength(150));
       
        form.setCol(2);
       
        KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener()
        {
           @Override
           public void onClick()
           {
               if(!form.valid()){
            	    return;
            	}
           	MapData formData = form.getSubmitData().asMapData();
               RPC.ajax("rpc/merchantGroupServer/updateMerchantGroup", new RpcCallback<Data>()
               {
                   @Override
                   public void onSuccess(Data result)
                   {
                       notice(false);
                       Dialog.tipNotice("修改成功！");
                       Token token = Flat.get().getCurrentToken();
                       token.directPage(MerchantGroupPage.class);
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
               token.directPage(MerchantGroupPage.class);
               Flat.get().goTo(token);
           }
       });
       addButton(submitBtn);
       addButton(cBtn);
       panel.add(form);
       
		return panel;
		
	}
	
	@Override
   public void refresh()
   {
  		form.getUi().clear();
  		key = Flat.get().getCurrentToken().getParam(uMerchantGroup.MerGroupId().getName());
		RPC.ajax("rpc/merchantGroupServer/getMerchantGroup", new RpcCallback<Data>() {
			@Override
			public void onSuccess(Data arg0) {		
				form.setFormData(arg0);				 
			}
		}, key);
   }

}
