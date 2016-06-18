package com.sunline.ccs.param.ui.client.txnCd;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UTxnCd;
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
public class TxnCdDetailPage extends SavePage {

	@Inject
	private UTxnCd txnCd;
    private KylinForm editForm;
    private String uTxnCd;
	@Override
	 public IsWidget createPage()
    {
        VerticalPanel panel = new VerticalPanel();
        editForm = new KylinForm();
        editForm.setWidth("100%");
        editForm.getSetting().labelWidth(120).labelAlign("right");
        editForm.setField(txnCd.TxnCd().readonly(true),
        		txnCd.Description().setNewline(true),
        		txnCd.ShortDesc(),
        		txnCd.TxnType().asSelectItem(SelectType.KEY_LABLE).setNewline(true).required(true),
        		txnCd.LogicMod().asSelectItem(SelectType.KEY_LABLE).required(true),
        		txnCd.PlanType().asSelectItem(SelectType.KEY_LABLE).required(true),
        		txnCd.AdjustInd().asSelectItem(SelectType.KEY_LABLE).required(true),
        		txnCd.BonusPntInd().asSelectItem(SelectType.KEY_LABLE).required(true),
        		txnCd.BlkcdCheckInd().asCheckBoxItem(),
        		txnCd.FeeWaiveInd().setNewline(true).asCheckBoxItem(),        		
        		txnCd.StmtInd().asCheckBoxItem() 
        		);
        editForm.setCol(3);
        
        KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
        	if(!editForm.valid()){
        	    return;
        	}
                MapData data = editForm.getSubmitData().asMapData();
                RPC.ajax("rpc/txnCdServer/updateTxnCd", new RpcCallback<Data>()
                {
                    @Override
                    public void onSuccess(Data result)
                    {
                        notice(false);
                        Dialog.tipNotice("修改成功！");
                        Token token = Flat.get().getCurrentToken();
                        token.directPage(TxnCdPage.class);
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
                token.directPage(TxnCdPage.class);
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
        uTxnCd = token.getParam(txnCd.TxnCd().getName());
        RPC.ajax("rpc/txnCdServer/getTxnCd", new RpcCallback<Data>()
        {
            @Override
            public void onSuccess(Data arg0)
            {
                editForm.setFormData(arg0);
            }
        }, uTxnCd);
	}
}
