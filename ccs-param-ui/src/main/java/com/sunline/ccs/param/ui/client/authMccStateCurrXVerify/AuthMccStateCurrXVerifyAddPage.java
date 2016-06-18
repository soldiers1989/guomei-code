package com.sunline.ccs.param.ui.client.authMccStateCurrXVerify;

import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UAuthMccStateCurrXVerify;
import com.sunline.ccs.infrastructure.client.ui.UInterestTable;
import com.sunline.ccs.param.ui.client.interestTable.InterestTablePage;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.Flat;
import com.sunline.kylin.web.core.client.mvp.Token;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.kylin.web.core.client.res.SavePage;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.pcm.facility.client.selectopt.BmpSelectOptionUtils;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.ListData;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.IClickEventListener;


@Singleton
public class AuthMccStateCurrXVerifyAddPage extends SavePage {

	    @Inject
	    private UAuthMccStateCurrXVerify uAuthMccStateCurrXVerify;
	    
	    
	    private KylinForm form;
    
	@Override
	public IsWidget createPage() {
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");
		panel.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
		
		form = new KylinForm();
        form.getSetting().labelWidth(135);
        form.setField(uAuthMccStateCurrXVerify.InputSource().required(true),uAuthMccStateCurrXVerify.CountryCode().asSelectItem().required(true)
        		,uAuthMccStateCurrXVerify.MccCode().required(true).asSelectItem(),uAuthMccStateCurrXVerify.TransCurrencyCode().required(true).asSelectItem()
        		,uAuthMccStateCurrXVerify.CurrentActiveFlag().asCheckBoxItem(),uAuthMccStateCurrXVerify.ForbiddenFlag().asCheckBoxItem()
        		,uAuthMccStateCurrXVerify.MaxAmtLcl().required(true),uAuthMccStateCurrXVerify.MaxAmtFrg().required(true));
        
        form.setCol(2);
       
        //动态获取MCC下拉框 
       RPC.ajax("rpc/pcmSelectOptionServer/getMcc", new RpcCallback<Data>() {

    		@Override
    		public void onSuccess(Data result) {  			
    			SelectItem<String> mccSelect = new SelectItem<String>();
    			mccSelect.setValue(result.asListData());
    			 form.setFieldSelectData(uAuthMccStateCurrXVerify.MccCode().getName(), mccSelect);
    		}
    	});	
       
        //动态获取国家下拉框 
        RPC.ajax("rpc/pcmSelectOptionServer/getCountryCd", new RpcCallback<Data>() {

    		@Override
    		public void onSuccess(Data result) {  			
    			SelectItem<String> mccSelect = new SelectItem<String>();
    			mccSelect.setValue(result.asListData());
    			 form.setFieldSelectData(uAuthMccStateCurrXVerify.CountryCode().getName(), mccSelect);
    		}
    	});	
        //动态获取交易币种
        RPC.ajax("rpc/pcmSelectOptionServer/getCurrencyCd", new RpcCallback<Data>() {

    		@Override
    		public void onSuccess(Data result) {  			
    			SelectItem<String> mccSelect = new SelectItem<String>();
    			mccSelect.setValue(result.asListData());
    			 form.setFieldSelectData(uAuthMccStateCurrXVerify.TransCurrencyCode().getName(),mccSelect);
    		}
    	});	
        
        
        
        panel.add(form);
        KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
            	
                if(!form.valid()){
            	    return;
            	}
        	MapData formData = form.getSubmitData().asMapData();          	            	
            	
                RPC.ajax("rpc/authMccStateCurrXVerifyServer/addAuthMccStateCurrXVerify", new RpcCallback<Data>()
                {
                    @Override
                    public void onSuccess(Data result)
                    {
                        notice(false);
                        Dialog.tipNotice("添加成功！");
                        Token token = Flat.get().getCurrentToken();
                        token.directPage(AuthMccStateCurrXVerifyPage.class);
                        Flat.get().goTo(token);
                    }
                },formData.toMap());
            	
            }
        });
        
        KylinButton cBtn = ClientUtils.creCancelButton(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
                Token token = Flat.get().getCurrentToken();
                token.directPage(AuthMccStateCurrXVerifyPage.class);
                Flat.get().goTo(token);
            }
        });
        
        addButton(submitBtn);
        addButton(cBtn);
	return panel;
		
	}
	
	@Override
    public void refresh()
    {
   	form.getUi().clear();
   		
    }

}
