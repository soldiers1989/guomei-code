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
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.IClickEventListener;


@Singleton
public class BlockCodeAddPage extends SavePage {

    private KylinForm addForm;
    
    public final static String PAGE_ID = "auth-blockcode-update";
    
    
    @Inject
    private UBlockCode ublockcode;
    
    @Override
	public void refresh() {
		addForm.getUi().clear();
	}
	    
    @Override
	public IsWidget createPage() {
    	VerticalPanel panel = new VerticalPanel();
    	panel.setHeight("98%");
	     addForm = new KylinForm();
	     addForm.setWidth("100%");
	     addForm.getSetting().labelWidth(130).inputWidth(160).space(40);
	     
	     addForm.setField(ublockcode.BlockCode().required(true),
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
	     addForm.setCol(3);
	     
	     KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener()
	        {
	            @Override
	            public void onClick()
	            {
	                //保存数据
	            	if (addForm.valid()) {
	                RPC.ajax("rpc/blockCodeServer/addBlockCode", new RpcCallback<Data>()
	                {
	                    @Override
	                    public void onSuccess(Data result)
	                    {
	                        Dialog.tipNotice("增加成功！");
	                        notice(false);
	                        Token token = Flat.get().getCurrentToken();
	                        token.directPage(BlockCodePage.class);
	                        Flat.get().goTo(token);
	                    }
	                }, addForm.getSubmitData().asMapData().toMap());
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
	        panel.add(addForm);
	        return addForm;
    	
		
	}

}
