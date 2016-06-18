package com.sunline.ccs.param.ui.client.countryctrl;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCountryCtrl;
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
import com.sunline.ccs.infrastructure.client.ui.UCountryCtrl;
import com.sunline.ccs.infrastructure.shared.map.CountryCtrlMapHelper;
import com.sunline.ccs.param.def.CountryCtrl;
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
/**
 * 国家代码控制详细信息
 * 
* @author guxiaoyu
 */
@Singleton
public class AuthCountryDetailPage extends SavePage
{
    /**
     * 编辑表单
     */
    private KylinForm editForm;
    
    /**
     * 国家代码控制
     */
    private String authCountry;
    
    @Inject
    private UCountryCtrl uCountryCtrl;
    
    @Override
    public IsWidget createPage()
    {
        VerticalPanel panel = new VerticalPanel();
        editForm = new KylinForm();
        editForm.setWidth("100%");
        editForm.getSetting().labelWidth(120);
        editForm.setField(uCountryCtrl.CountryCode().readonly(true),
        		uCountryCtrl.MaxTxnAmtLcl().setNewline(true).required(true),
        		uCountryCtrl.MaxTxnAmtFrg().setNewline(true).required(true),
        		uCountryCtrl.ValidInd().asCheckBoxItem().setNewline(true));
        
        
        KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
        	
        	if(!editForm.valid()){
        	    return;
        	}
                MapData data = editForm.getSubmitData().asMapData();
                RPC.ajax("rpc/authCountryServer/updateAuthCountry", new RpcCallback<Data>()
                {
                    @Override
                    public void onSuccess(Data result)
                    {
                        notice(false);
                        Dialog.tipNotice("修改成功！");
                        Token token = Flat.get().getCurrentToken();
                        token.directPage(AuthCountryListPage.class);
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
                token.directPage(AuthCountryListPage.class);
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
	Token token = Flat.get().getCurrentToken();
        authCountry = token.getParam( uCountryCtrl.CountryCode().getName());
        RPC.ajax("rpc/authCountryServer/getAuthCountry", new RpcCallback<Data>()
        {
            @Override
            public void onSuccess(Data arg0)
            {
                editForm.setFormData(arg0);
               
            }
        }, authCountry);
    }
}

/*	
	public static final String PAGE_ID = "auth-country-update";

	public static String AUTHCOUNTRY_CODE_PARAM = "authCode";

	private String countryCode;
	
	@Inject
	private AuthCountryConstants constants;

	@Inject
	private UCountryCtrl uCountryCtrl;

	@Inject
	private AuthCountryInterAsync server;

	@Inject
	private ClientUtils clientUtils;

	private YakDynamicForm basicInfoForm;

	public AuthCountryDetailPage() {
		
		super(PAGE_ID, false);
	}

	@Override
	protected void createCanvas() {
		
				basicInfoForm = new YakDynamicForm();
				basicInfoForm.setNumCols(2);
				basicInfoForm.setWidth100();
				basicInfoForm.setColWidths(200, "*");
				basicInfoForm.setItems(uCountryCtrl.CountryCode().asLabel().colSpan(6).createFormItem(), 
						uCountryCtrl.MaxTxnAmtLcl().required().createFormItem(), 
						uCountryCtrl.MaxTxnAmtFrg().required().createFormItem(),
						uCountryCtrl.ValidInd().createFormItem());
				
				addMember(basicInfoForm);
				
				HLayout hLayout = clientUtils.createSubmitReturnLayout(basicInfoForm, new ControlPanelPlace(AuthCountryListPage.PAGE_ID));
				hLayout.setWidth(300);
				addMember(hLayout);

				basicInfoForm.addSubmitValuesHandler(new SubmitValuesHandler() {

					@Override
					public void onSubmitValues(SubmitValuesEvent event) {
						if (basicInfoForm.validate()) {
							RPCTemplate.call(new RPCExecutor<Void>() {
								@SuppressWarnings("unchecked")
								@Override
								public void execute(AsyncCallback<Void> callback) {

									server.updateAuthCountry(basicInfoForm.getValues(), callback);
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

	@Override
	public void updateView(final ParamMapToken token) {
		basicInfoForm.clearValues();
		basicInfoForm.reset();
		countryCode = token.getParam(AUTHCOUNTRY_CODE_PARAM);
		RPCTemplate.call(new RPCExecutor<CountryCtrl>() {

			@Override
			public void execute(AsyncCallback<CountryCtrl> callback) {
				server.getAuthCountry(countryCode, callback);
			}

			@Override
			public void onSuccess(CountryCtrl result) {

				basicInfoForm.setValues(CountryCtrlMapHelper.convertToMap(result));
			}

		}, basicInfoForm);

	}

	@Override
	public String getTitle() {
		
		return constants.authCountryUpdate();
	}

	@Override
	public ImageResource getIcon() {
		// TODO Auto-generated method stub
		return null;
	}
*/
