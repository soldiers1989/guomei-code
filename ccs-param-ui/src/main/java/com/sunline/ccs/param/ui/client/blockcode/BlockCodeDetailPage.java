package com.sunline.ccs.param.ui.client.blockcode;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UBlockCode;
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
public class BlockCodeDetailPage extends SavePage {

	/**
     * 编辑表单
     */
    private KylinForm detailForm;
    
    public final static String PAGE_ID = "auth-blockcode-update";
    
    private String key;
    
    @Inject
    private UBlockCode ublockcode;
    
	@Override
	public IsWidget createPage() {
		 VerticalPanel panel = new VerticalPanel();
	     detailForm = new KylinForm();
	     detailForm.setWidth("100%");
	     detailForm.getSetting().labelWidth(130).inputWidth(160).space(40);
	     detailForm.setField(ublockcode.BlockCode().readonly(true),
	    		 ublockcode.Description().required(true), 
	    		 ublockcode.Priority().required(true), 
	    		 ublockcode.AgentAction().asSelectItem(SelectType.KEY_LABLE).required(true), 	
	    		 ublockcode.AuthReason().asSelectItem(SelectType.KEY_LABLE).required(true), 
	    		 ublockcode.CashAction().asSelectItem(SelectType.KEY_LABLE).required(true), 
	    		 ublockcode.DebitAdjustAction().asSelectItem(SelectType.KEY_LABLE).required(true), 
	    		 ublockcode.InquireAction().asSelectItem(SelectType.KEY_LABLE).required(true), 
	    		 ublockcode.MotoElecAction().asSelectItem(SelectType.KEY_LABLE).required(true), 
	    		 ublockcode.MotoRetailAction().asSelectItem(SelectType.KEY_LABLE).required(true), 
	    		 ublockcode.NonMotoRetailAction().asSelectItem(SelectType.KEY_LABLE).required(true), 
	    		 ublockcode.PaymentInd().asSelectItem(SelectType.KEY_LABLE).required(true), 
	    		 ublockcode.PostInd().asSelectItem(SelectType.KEY_LABLE).required(true),
	    		 ublockcode.SpecAction().asSelectItem(SelectType.KEY_LABLE).required(true), 
	    		 ublockcode.SysInd().asSelectItem(SelectType.KEY_LABLE).required(true), 
	    		 ublockcode.BlockLevel().asSelectItem(SelectType.KEY_LABLE).required(true),
	    		 ublockcode.LetterCd().required(true),
	    		 ublockcode.StmtInd().asCheckBoxItem(), 
	    		 ublockcode.CardFeeWaiveInd().asCheckBoxItem(),
	    		 ublockcode.CollectionInd().asCheckBoxItem(), 
	    		 ublockcode.IntAccuralInd().asCheckBoxItem(), 
	    		 ublockcode.IntWaiveInd().asCheckBoxItem(), 
	    		 ublockcode.LateFeeWaiveInd().asCheckBoxItem(), 
	    		 ublockcode.LoanInd().asCheckBoxItem(), 
	    		 ublockcode.OvrlmtFeeWaiveInd().asCheckBoxItem(), 
	    		 ublockcode.PointEarnInd().asCheckBoxItem(),
	    		 ublockcode.RenewInd().asCheckBoxItem(), 
	    		 ublockcode.TxnFeeWaiveInd().asCheckBoxItem(),
	    		 ublockcode.MulctWaiveIND().asCheckBoxItem(),                  //免除罚金，免除服务费，免除其他费用     2015.11.18  chenpy
	    		 ublockcode.SvcfeeWaiveIND().asCheckBoxItem(),
	    		 ublockcode.OtherfeeWaiveIND().asCheckBoxItem()
	    		 );
	     
	     detailForm.setCol(3);
	     
	     KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener()
	        {
	            @Override
	            public void onClick()
	            {
	                MapData data = detailForm.getSubmitData().asMapData();
	                if (detailForm.valid()) {
	                RPC.ajax("rpc/blcokCodeServer/updateBlockCode", new RpcCallback<Data>()
	                {
	                    @Override
	                    public void onSuccess(Data result)
	                    {
	                        notice(false);
	                        Dialog.tipNotice("修改成功！");
	                        Token token = Flat.get().getCurrentToken();
	                        token.directPage(BlockCodePage.class);
	                        Flat.get().goTo(token);
	                    }
	                }, data);
	                }
	            }
	        });
	        KylinButton cBtn = ClientUtils.creCancelButton(new IClickEventListener()
	        {
	            @Override
	            public void onClick()
	            {
	                Token token = Flat.get().getCurrentToken();
	                token.directPage(BlockCodePage.class);
	                Flat.get().goTo(token);
	            }
	        });
	        addButton(submitBtn);
	        addButton(cBtn);
	        panel.add(detailForm);
	        return panel;
	}
	
	@Override
    public void refresh()
    {
        notice(true);
        detailForm.getUi().clear();
        Token token = Flat.get().getCurrentToken();
        key = token.getParam(ublockcode.BlockCode().getName());
        RPC.ajax("rpc/blcokCodeServer/getBlockCode", new RpcCallback<Data>()
        {
            @Override
            public void onSuccess(Data arg0)
            {
                detailForm.setFormData(arg0);
            }
        }, key);
    }
	
}
