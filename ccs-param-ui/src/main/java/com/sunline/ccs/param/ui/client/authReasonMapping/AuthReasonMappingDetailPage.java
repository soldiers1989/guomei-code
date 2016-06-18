package com.sunline.ccs.param.ui.client.authReasonMapping;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UAuthReasonMapping;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
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
public class AuthReasonMappingDetailPage extends SavePage {

	public static final String PAGE_ID = "auth-authReasonMapping-Detail";
	
	public final static String KEY = "key";
	
	@Inject
	private UAuthReasonMapping uAuthReasonMapping;
	
	private String key;
	
	private KylinForm detailForm;
	
	@Override
    public void refresh()
    {
	    detailForm.getUi().clear();  
        notice(true);
        key = Flat.get().getCurrentToken().getParam(KEY);
        
        RPC.ajax("rpc/authReasonMappingServer/getAuthReasonMapping", new RpcCallback<Data>()
        {
            @Override
            public void onSuccess(Data arg0)
            {
                detailForm.setFormData(arg0);
            }
        }, key);
    }
	
	@Override
	public IsWidget createPage() 
	{
		VerticalPanel panel = new VerticalPanel();
		detailForm = new KylinForm();
		detailForm.setWidth("100%");
		detailForm.getSetting().labelWidth(120);
		detailForm.setField(uAuthReasonMapping.Reason().readonly(true), 
				uAuthReasonMapping.InputSource().readonly(true), 
				uAuthReasonMapping.ApproveResponse().required(true), 
				uAuthReasonMapping.DeclineResponse().required(true), 
				uAuthReasonMapping.BusinessErrorReasonType(), 
				uAuthReasonMapping.CallResponse(),
				uAuthReasonMapping.PickupResponse(),
				uAuthReasonMapping.DefaultAction().asSelectItem(SelectType.KEY_LABLE).required(true),
				uAuthReasonMapping.ReasonCodeRank(), 
				uAuthReasonMapping.ManualIsRepeal().asCheckBoxItem(), 
				uAuthReasonMapping.ParameterIsConfigurable().asCheckBoxItem(), 
				uAuthReasonMapping.ReasonCodeDescribe().asTextArea().setFieldWidth(523).setNewline(true));
		detailForm.setCol(2);
		
		KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
        	if(!detailForm.valid()){
        	    return;
        	}
            	MapData data = detailForm.getSubmitData().asMapData();
            	//保存数据
                RPC.ajax("rpc/authReasonMappingServer/updateAuthReasonMapping", new RpcCallback<Data>()
                {
                    @Override
                    public void onSuccess(Data result)
                    {
                    	notice(false);
                        Dialog.tipNotice("修改成功！");
                        Token token = Flat.get().getCurrentToken();
                        token.directPage(AuthReasonMappingPage.class);
                        Flat.get().goTo(token);
                    }
                }, data);
            }
        });
        KylinButton cBtn = ClientUtils.creCancelButton(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
                Token token = Flat.get().getCurrentToken();
                token.directPage(AuthReasonMappingPage.class);
                Flat.get().goTo(token);
            }
        });
        addButton(submitBtn);
        addButton(cBtn);
        panel.add(detailForm);
		return panel;
	}
	
	

}
