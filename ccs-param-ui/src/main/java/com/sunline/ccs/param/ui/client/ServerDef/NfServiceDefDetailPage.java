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
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.IClickEventListener;

/*
import com.sunline.pcm.ui.common.client.ClientUtils;
import com.sunline.pcm.ui.common.client.DispatcherPage;
import com.sunline.pcm.ui.cp.client.panel.ControlPanelPlace;
import com.sunline.ccs.infrastructure.client.ui.UNfServiceDef;
import com.sunline.ccs.infrastructure.shared.map.NfServiceDefMapHelper;
import com.sunline.ccs.param.def.NfServiceDef;
import com.sunline.ccs.param.ui.client.loanPlan.LoanPlanPage;
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
 * 非金融服务定义明细修改页面
* @author fanghj
 *
 */
@Singleton
public class NfServiceDefDetailPage extends SavePage{

	public final static String KEY = "key";
	
	@Inject
	private UNfServiceDef uNfServiceDef;
	
	private KylinForm detailForm;
	
	private String key;
	
	@Override
    public void refresh()
    {
	    detailForm.getUi().clear();
        notice(true);
        Token token = Flat.get().getCurrentToken();
        key = token.getParam(uNfServiceDef.ServCode().getName());
        
        RPC.ajax("rpc/nfServiceDefServer/getNfServiceDef", new RpcCallback<Data>()
        {
            @Override
            public void onSuccess(Data arg0)
            {
                detailForm.setFormData(arg0);
            }
        }, key);
    }
	
	@Override
	public IsWidget createPage() {
		VerticalPanel panel = new VerticalPanel();
		detailForm = new KylinForm();
		detailForm.setWidth("100%");
		detailForm.getSetting().labelWidth(120);
		detailForm.setField(uNfServiceDef.ServCode().readonly(true).setNewline(true),
				uNfServiceDef.ServDesc().setNewline(true));
        
		KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
                if(!detailForm.valid())return;
                
            	MapData data = detailForm.getSubmitData().asMapData();
            	//保存数据
                RPC.ajax("rpc/nfServiceDefServer/updateNfServiceDef", new RpcCallback<Data>()
                {
                    @Override
                    public void onSuccess(Data result)
                    {
                    	notice(false);
                        Dialog.tipNotice("修改成功！");
                        Token token = Flat.get().getCurrentToken();
                        token.directPage(NfServiceDefPage.class);
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
                token.directPage(NfServiceDefPage.class);
                Flat.get().goTo(token);
            }
        });
		
		addButton(submitBtn);
        addButton(cBtn);
        panel.add(detailForm);
		return panel;
	}

}
