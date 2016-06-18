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
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.IClickEventListener;


@Singleton
public class AuthReasonMappingAddPage extends SavePage {

	public static final String PAGE_ID = "auth-authReasonMapping-add";
	
	@Inject
	private UAuthReasonMapping uAuthReasonMapping;
	
	private KylinForm addForm;
	
	@Override
	public void refresh() {
		addForm.getUi().clear();
	}
	
	@Override
	public IsWidget createPage() {
		VerticalPanel panel = new VerticalPanel();
        addForm = new KylinForm();
        addForm.setWidth("100%");
        addForm.getSetting().labelWidth(120);
        addForm.setField(uAuthReasonMapping.Reason().asSelectItem(SelectType.KEY_LABLE).required(true), 
				uAuthReasonMapping.InputSource().asSelectItem(SelectType.KEY_LABLE).required(true), 
				uAuthReasonMapping.ApproveResponse().required(true), 
				uAuthReasonMapping.DeclineResponse().required(true), 
				uAuthReasonMapping.BusinessErrorReasonType(), 
				uAuthReasonMapping.CallResponse(),
				uAuthReasonMapping.PickupResponse(),
				uAuthReasonMapping.DefaultAction().asSelectItem(SelectType.KEY_LABLE).required(true),
				uAuthReasonMapping.ReasonCodeRank(), 
				uAuthReasonMapping.ManualIsRepeal().asCheckBoxItem(), 
				uAuthReasonMapping.ParameterIsConfigurable().asCheckBoxItem(), 
				uAuthReasonMapping.ReasonCodeDescribe().asTextArea().setFieldWidth(483).setNewline(true));
        addForm.setCol(2);
        
        KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
        	if(!addForm.valid()){
        	    return;
        	}
                //保存数据
                RPC.ajax("rpc/authReasonMappingServer/addNewAuthReasonMapping", new RpcCallback<Data>()
                {
                    @Override
                    public void onSuccess(Data result)
                    {
                        Dialog.tipNotice("增加成功！");
                        notice(false);
                        Token token = Flat.get().getCurrentToken();
                        token.directPage(AuthReasonMappingPage.class);
                        Flat.get().goTo(token);
                    }
                }, addForm.getSubmitData().asMapData().toMap());
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
        panel.add(addForm);
        return panel;
			
	}
	

}
