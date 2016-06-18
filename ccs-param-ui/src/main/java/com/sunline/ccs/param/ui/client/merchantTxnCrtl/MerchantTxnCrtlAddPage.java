package com.sunline.ccs.param.ui.client.merchantTxnCrtl;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UMerchantTxnCrtl;
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

/*
import com.sunline.pcm.ui.common.client.ClientUtils;
import com.sunline.pcm.ui.common.client.DispatcherPage;
import com.sunline.pcm.ui.cp.client.panel.ControlPanelPlace;
import com.sunline.ccs.infrastructure.client.ui.UMerchantTxnCrtl;
import com.sunline.ark.gwt.client.mvp.ParamMapToken;
import com.sunline.ark.gwt.client.ui.YakDynamicForm;
import com.sunline.ark.gwt.client.util.RPCExecutor;
import com.sunline.ark.gwt.client.util.RPCTemplate;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.smartgwt.client.widgets.form.events.SubmitValuesEvent;
import com.smartgwt.client.widgets.form.events.SubmitValuesHandler;
import com.smartgwt.client.widgets.layout.HLayout;
*/



@Singleton
public class MerchantTxnCrtlAddPage extends SavePage {

	@Inject
	private UMerchantTxnCrtl uMerchantTxnCrtl;
    
    
    private KylinForm addForm;
    

	@Override
	public IsWidget createPage() {
		VerticalPanel panel = new VerticalPanel();
        addForm = new KylinForm();
        addForm.setWidth("100%");
        addForm.getSetting().labelWidth(140);
        addForm.setField(uMerchantTxnCrtl.MerchantId().required(true),
        		uMerchantTxnCrtl.MerchantName(),
        		uMerchantTxnCrtl.CountryCd(),
        		
        		uMerchantTxnCrtl.Mcc().setNewline(true),
        		uMerchantTxnCrtl.MerchantAddr(),
        		uMerchantTxnCrtl.MerchantClass(),
        		
        		uMerchantTxnCrtl.MerchantContact().setNewline(true),
        		uMerchantTxnCrtl.MerchantPostcode(),
        		uMerchantTxnCrtl.MerchantShortName(),
        	
        		uMerchantTxnCrtl.MerchantTel().setNewline(true),
        		uMerchantTxnCrtl.ForceMotoRetailCvv2Ind().required(true),
        		uMerchantTxnCrtl.SupportEmotoInd().required(true),
        		
        		uMerchantTxnCrtl.SupportLoanInd().setNewline(true).required(true),
        		uMerchantTxnCrtl.SupportMotoPosInd().required(true),
        		uMerchantTxnCrtl.SupportSpecloan().required(true)
        		);
        
        
        KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
        	if(!addForm.valid()){
        	    return;
        	}
                //保存数据
                RPC.ajax("rpc/merchantTxnCrtlServer/addMerchantTxnCrtl", new RpcCallback<Data>()
                {
                    @Override
                    public void onSuccess(Data result)
                    {
                        Dialog.tipNotice("增加成功！");
                        notice(false);
                        Token token = Flat.get().getCurrentToken();
                        token.directPage(MerchantTxnCrtlPage.class);
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
                token.directPage(MerchantTxnCrtlPage.class);
                Flat.get().goTo(token);
            }
        });
        addButton(submitBtn);
        addButton(cBtn);
        panel.add(addForm);
        return panel;
	}
	   @Override
	    public void refresh()
	    {
	       addForm.getUi().clear();
	    }
}
/*	
	public static final String PAGE_ID = "auth-merchantTxnCrtl-add";

	private YakDynamicForm addForm;

	@Inject
	private ClientUtils clientUtils;

	@Inject
	private UMerchantTxnCrtl uMerchantTxnCtrl;

	@Inject
	private MerchantTxnCrtlConstants constants;

	@Inject
	private MerchantTxnCrtlInterAsync server;

	public MerchantTxnCrtlAddPage() {

		super(PAGE_ID, false);
	}

	@Override
	public void updateView(ParamMapToken token) {
		addForm.reset();
	}

	@Override
	public String getTitle() {

		return constants.merchantTxnCrtlAdd();
	}

	@Override
	public ImageResource getIcon() {

		return null;
	}

	@Override
	protected void createCanvas() {

		setMembersMargin(10);

		addForm = new YakDynamicForm();
		{
			addForm.setNumCols(6);
			addForm.setWidth100();
			addForm.setColWidths(150, 150, 150, 150, 150, "*");
			addForm.setFields(uMerchantTxnCtrl.MerchantId().required().createFormItem(), 
					uMerchantTxnCtrl.MerchantName().createFormItem(), 
					uMerchantTxnCtrl.CountryCd().createFormItem(), 
					uMerchantTxnCtrl.Mcc().createFormItem(),
					uMerchantTxnCtrl.MerchantAddr().createFormItem(), 
					uMerchantTxnCtrl.MerchantClass().createFormItem(), 
					uMerchantTxnCtrl.MerchantContact().createFormItem(), 
					uMerchantTxnCtrl.MerchantPostcode().createFormItem(), 
					uMerchantTxnCtrl.MerchantShortName().createFormItem(), 
					uMerchantTxnCtrl.MerchantTel().createFormItem(), 
					uMerchantTxnCtrl.ForceMotoRetailCvv2Ind().required().createSelectItem(), 
					uMerchantTxnCtrl.SupportEmotoInd().required().createSelectItem(), 
					uMerchantTxnCtrl.SupportLoanInd().required().createSelectItem(), 
					uMerchantTxnCtrl.SupportMotoPosInd().required().createSelectItem(), 
					uMerchantTxnCtrl.SupportSpecloan().required().createSelectItem());
		}
		addMember(addForm);
		
		HLayout hLayout = clientUtils.createSubmitReturnLayout(addForm, new ControlPanelPlace(MerchantTxnCrtlPage.PAGE_ID));
		addMember(hLayout);

		addForm.addSubmitValuesHandler(new SubmitValuesHandler() {

			@Override
			public void onSubmitValues(SubmitValuesEvent event) {

				if (addForm.validate()) {
					RPCTemplate.call(new RPCExecutor<Void>() {

						@SuppressWarnings("unchecked")
						@Override
						public void execute(AsyncCallback<Void> callback) {

							server.addMerchantTxnCrtl(addForm.getValues(), callback);
						}

						@Override
						public void onSuccess(Void result) {
							clientUtils.showSuccess();
							addForm.clearValues();
						}
					});
				}
			}
		});
	}
*/

