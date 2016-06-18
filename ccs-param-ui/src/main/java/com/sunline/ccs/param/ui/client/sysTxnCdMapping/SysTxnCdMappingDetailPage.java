package com.sunline.ccs.param.ui.client.sysTxnCdMapping;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.USysTxnCdMapping;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
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
public class SysTxnCdMappingDetailPage extends SavePage {
	  /**
     * 编辑表单
     */
	@Inject
	private USysTxnCdMapping uSysTxnCdMapping;
	private SelectItem<String> columnitem;
    private KylinForm editForm;
    private String sysTxnCd;
	@Override
	 public IsWidget createPage()
    {
        VerticalPanel panel = new VerticalPanel();
        columnitem = new SelectItem<String>(SelectType.KEY_LABLE);
        editForm = new KylinForm();
        editForm.setWidth("100%");
        editForm.setField(uSysTxnCdMapping.SysTxnCd().readonly(true),
        		uSysTxnCdMapping.TxnCd().asSelectItem().setNewline(true).required(true));
        editForm.setCol(2);
        
        KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
                MapData data = editForm.getSubmitData().asMapData();
                RPC.ajax("rpc/sysTxnCdServer/updateSysTxnCdMapping", new RpcCallback<Data>()
                {
                    @Override
                    public void onSuccess(Data result)
                    {
                        notice(false);
                        Dialog.tipNotice("修改成功！");
                        Token token = Flat.get().getCurrentToken();
                        token.directPage(SysTxnCdMappingPage.class);
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
                token.directPage(SysTxnCdMappingPage.class);
                Flat.get().goTo(token);
            }
        });
        addButton(submitBtn);
        addButton(cBtn);
        panel.add(editForm);
        return panel;
    }
	@Override
	public void refresh() {
	    editForm.getUi().clear();
        notice(true);
        Token token = Flat.get().getCurrentToken();
        sysTxnCd = token.getParam(uSysTxnCdMapping.SysTxnCd().getName());
        RPC.ajax("rpc/sysTxnCdServer/getSysTxnCdMapping", new RpcCallback<Data>()
        {
            @Override
            public void onSuccess(Data arg0)
            {
                editForm.setFormData(arg0);
            }
        }, sysTxnCd);
        
        RPC.ajax("rpc/txnCdServer/getAllTxnCd", new RpcCallback<Data>(){
		@Override
		public void onSuccess(Data result) {
			columnitem.setValue(result.asListData());
			editForm.setFieldSelectData(uSysTxnCdMapping.TxnCd().getName(), columnitem);
		}
	});
	}
}
