package com.sunline.ccs.param.ui.client.controlField;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UNfControlField;
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

/*
import com.sunline.pcm.ui.common.client.ClientUtils;
import com.sunline.pcm.ui.common.client.DispatcherPage;
import com.sunline.pcm.ui.cp.client.panel.ControlPanelPlace;
import com.sunline.ccs.infrastructure.client.ui.UNfControlField;
import com.sunline.ccs.infrastructure.shared.map.NfControlFieldMapHelper;
import com.sunline.ccs.param.def.NfControlField;
import com.sunline.ark.gwt.client.mvp.ParamMapToken;
import com.sunline.ark.gwt.client.ui.YakDynamicForm;
import com.sunline.ark.gwt.client.util.ClientFactory;
import com.sunline.ark.gwt.client.util.RPCExecutor;
import com.sunline.ark.gwt.client.util.RPCTemplate;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
*/
/**
 * 非金融控制字段定义增加页面配置
* @author fanghj
 *
 */
@Singleton
public class NfControlFieldAdd extends SavePage{

	@Inject
	private UNfControlField uNfControlField;
	
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
        addForm.setField(uNfControlField.FieldCode().required(true).setNewline(true),
        		uNfControlField.Priority().required(true).setNewline(true),
        		uNfControlField.FieldName().required(true).setNewline(true));
        KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener()
        {
            @Override
            public void onClick()
            
            {
        	if(!addForm.valid()){
        	    return;
        	}
                //保存数据
                RPC.ajax("rpc/nfControlFieldServer/addNewNfControlField", new RpcCallback<Data>()
                {
                    @Override
                    public void onSuccess(Data result)
                    {
                        Dialog.tipNotice("增加成功！");
                        notice(false);
                        Token token = Flat.get().getCurrentToken();
                        token.directPage(NfControlFieldPage.class);
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
                token.directPage(NfControlFieldPage.class);
                Flat.get().goTo(token);
            }
        });
        addButton(submitBtn);
        addButton(cBtn);
        panel.add(addForm);
        return addForm;
	}

}
