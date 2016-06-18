package com.sunline.ccs.param.ui.client.ServerDef;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UNfServiceDef;
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
public class AddNfServiceDefPage extends SavePage{

	@Inject
	private UNfServiceDef uNfServiceDef;
	
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
        addForm.setField(uNfServiceDef.ServCode().required(true),
        		uNfServiceDef.ServDesc().required(true));
        KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
                if(!addForm.valid())return;
                //保存数据
                RPC.ajax("rpc/nfServiceDefServer/addNewNfServiceDef", new RpcCallback<Data>()
                {
                    @Override
                    public void onSuccess(Data result)
                    {
                        Dialog.tipNotice("增加成功！");
                        notice(false);
                        Token token = Flat.get().getCurrentToken();
                        token.directPage(NfServiceDefPage.class);
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
                token.directPage(NfServiceDefPage.class);
                Flat.get().goTo(token);
            }
        });
        addButton(submitBtn);
        addButton(cBtn);
        panel.add(addForm);
        return addForm;
        
	}

}
