package com.sunline.ccs.param.ui.client.merchantTxnCrtl;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UMerchantTxnCrtl;
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
import com.sunline.ccs.infrastructure.client.ui.UMerchantTxnCrtl;
import com.sunline.ccs.infrastructure.shared.map.MerchantTxnCrtlMapHelper;
import com.sunline.ccs.param.def.MerchantTxnCrtl;
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
public  class MerchantTxnCrtlDetailPage extends SavePage {
	
	    /**
	     * 编辑表单
	     */
	    private KylinForm editForm;
	    
	    /**
	     * 指定商户交易授权控制详情界面
	     */
	    private String merchantTxnCrtl;
	    
	    @Inject
	    private UMerchantTxnCrtl uMerchantTxnCrtl;
	    
	    @Override
	    public IsWidget createPage()
	    {
	        VerticalPanel panel = new VerticalPanel();
	        editForm = new KylinForm();
	        editForm.setWidth("100%");
	        editForm.getSetting().labelWidth(140);
	        editForm.setField(uMerchantTxnCrtl.MerchantId().readonly(true),
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
	        		uMerchantTxnCrtl.SupportSpecloan().required(true));
	        
	        
	        
	        KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener()
	        {
	            @Override
	            public void onClick()
	            {
	        	if(!editForm.valid()){
	        	    return;
	        	}
	                MapData data = editForm.getSubmitData().asMapData();
	                RPC.ajax("rpc/merchantTxnCrtlServer/updateMerchantTxnCrtl", new RpcCallback<Data>()
	                {
	                    @Override
	                    public void onSuccess(Data result)
	                    {
	                        notice(false);
	                        Dialog.tipNotice("修改成功！");
	                        Token token = Flat.get().getCurrentToken();
	                        token.directPage(MerchantTxnCrtlPage.class);
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
	                token.directPage(MerchantTxnCrtlPage.class);
	                Flat.get().goTo(token);
	            }
	        });
	        addButton(submitBtn);
	        addButton(cBtn);
	        panel.add(editForm);
	        return panel;
	    }
	    
	    @Override
	    public void refresh()
	    {
		editForm.getUi().clear();
	        notice(true);
	        Token token = Flat.get().getCurrentToken();
	        merchantTxnCrtl = token.getParam( uMerchantTxnCrtl.MerchantId().getName());
	        RPC.ajax("rpc/merchantTxnCrtlServer/getMerchantTxnCrtl", new RpcCallback<Data>()
	        {
	            @Override
	            public void onSuccess(Data arg0)
	            {
	                editForm.setFormData(arg0);
	               
	            }
	        }, merchantTxnCrtl);
	    }
	}
/*	
	public static final String PAGE_ID = "auth-merchantTxnCrtl-update";

	public final static String MERCHANTID = "merchantId";

	private String merchantId;

	private YakDynamicForm detailForm;

	@Inject
	private ClientUtils clientUtils;

	@Inject
	private UMerchantTxnCrtl uMerchantTxnCtrl;

	@Inject
	private MerchantTxnCrtlConstants constants;

	@Inject
	private MerchantTxnCrtlInterAsync server;

	public MerchantTxnCrtlDetailPage() {

		super(PAGE_ID, false);
	}

	@Override
	public void updateView(ParamMapToken token) {

		merchantId = token.getParam(MERCHANTID);
		RPCTemplate.call(new RPCExecutor<MerchantTxnCrtl>() {

			@Override
			public void execute(AsyncCallback<MerchantTxnCrtl> callback) {

				server.getMerchantTxnCrtl(merchantId, callback);
			}

			@Override
			public void onSuccess(MerchantTxnCrtl result) {

				detailForm.setValues(MerchantTxnCrtlMapHelper.convertToMap(result));
			}

		}, detailForm);

	}

	@Override
	public String getTitle() {

		return constants.merchantTxnCrtlDetail();
	}

	@Override
	public ImageResource getIcon() {

		return null;
	}

	@Override
	protected void createCanvas() {

		setMembersMargin(10);

		detailForm = new YakDynamicForm();
		{
			detailForm.setNumCols(6);
			detailForm.setWidth100();
			detailForm.setColWidths(150, 150, 150, 150, 150, "*");
			detailForm.setFields(uMerchantTxnCtrl.MerchantId().asLabel().createFormItem(), 
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
		addMember(detailForm);
		
		HLayout hLayout = clientUtils.createSubmitReturnLayout(detailForm, new ControlPanelPlace(MerchantTxnCrtlPage.PAGE_ID));
		addMember(hLayout);

		detailForm.addSubmitValuesHandler(new SubmitValuesHandler() {

			@Override
			public void onSubmitValues(SubmitValuesEvent event) {

				if (detailForm.validate()) {
					RPCTemplate.call(new RPCExecutor<Void>() {

						@SuppressWarnings("unchecked")
						@Override
						public void execute(AsyncCallback<Void> callback) {

							server.updateMerchantTxnCrtl(detailForm.getValues(), callback);
						}

						@Override
						public void onSuccess(Void result) {

							clientUtils.showSuccess();
						}
					});
				}
			}
		});
	}
*/

