package com.sunline.ccs.param.ui.client.txnCd;

import com.google.gwt.user.client.ui.HasAlignment;
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
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.IClickEventListener;

@Singleton
public class TxnCdAddPage extends SavePage {

	@Inject
	private UTxnCd uTxnCd;
	
	private KylinForm addForm;
	@Override
	public IsWidget createPage() {
		VerticalPanel panel = new VerticalPanel();
		panel.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
        addForm = new KylinForm();
        addForm.setWidth("100%");
        addForm.getSetting().labelWidth(120).labelAlign("right");
        addForm.setField(
        		uTxnCd.TxnCd().required(true),
        		uTxnCd.Description(),
        		uTxnCd.ShortDesc(),
        		uTxnCd.TxnType().asSelectItem(SelectType.KEY_LABLE).setNewline(true).required(true),
        		uTxnCd.LogicMod().asSelectItem(SelectType.KEY_LABLE).required(true),
        		uTxnCd.PlanType().asSelectItem(SelectType.KEY_LABLE).required(true),
        		uTxnCd.AdjustInd().asSelectItem(SelectType.KEY_LABLE).required(true),
        		uTxnCd.BonusPntInd().asSelectItem(SelectType.KEY_LABLE).required(true),
        		uTxnCd.BlkcdCheckInd().asCheckBoxItem(),
        		uTxnCd.FeeWaiveInd().setNewline(true).asCheckBoxItem(),        		
        		uTxnCd.StmtInd().asCheckBoxItem()     		
           );
        addForm.setCol(3);
        KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
        	if(!addForm.valid()){
        	    return;
        	}
                //保存数据
                RPC.ajax("rpc/txnCdServer/addTxnCd", new RpcCallback<Data>()
                {
                    @Override
                    public void onSuccess(Data result)
                    {
                        Dialog.tipNotice("增加成功！");
                        notice(false);
                        Token token = Flat.get().getCurrentToken();
                        token.directPage(TxnCdPage.class);
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
                token.directPage(TxnCdPage.class);
                Flat.get().goTo(token);
            }
        });
        addButton(submitBtn);
        addButton(cBtn);
        panel.add(addForm);
        return addForm;
    }
	@Override
	public void refresh() {
	    addForm.getUi().clear();
	}
}
