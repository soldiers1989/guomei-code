package com.sunline.ccs.param.ui.client.currencyCtrl;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCurrencyCtrl;
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
import com.sunline.ccs.infrastructure.client.ui.UCurrencyCtrl;
import com.sunline.ccs.infrastructure.shared.map.CurrencyCtrlMapHelper;
import com.sunline.ccs.param.def.CurrencyCtrl;
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
public class CurrencyCtrlDetailPage extends SavePage
{
    /**
     * 编辑表单
     */
    private KylinForm editForm;
    
    /**
     * 币种代码控制
     */
    private String currencyCtrl;
    
    @Inject
    private UCurrencyCtrl uCurrencyCtrl;
    
    @Override
    public IsWidget createPage()
    {
        VerticalPanel panel = new VerticalPanel();
        editForm = new KylinForm();
        editForm.setWidth("100%");
        editForm.getSetting().labelWidth(120);
        editForm.setField(uCurrencyCtrl.CurrencyCd().readonly(true),
        		uCurrencyCtrl.ConversionRt().setNewline(true).required(true),
        		uCurrencyCtrl.ValidInd().asCheckBoxItem().setNewline(true),
        		uCurrencyCtrl.MaxTxnAmtLcl().setNewline(true).required(true),
        		uCurrencyCtrl.MaxTxnAmtFrg().setNewline(true).required(true));
        
        
        
        KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
        	if(!editForm.valid()){
        	    return;
        	}
                MapData data = editForm.getSubmitData().asMapData();
                RPC.ajax("rpc/currencyCtrlServer/updateCurrencyCtrl", new RpcCallback<Data>()
                {
                    @Override
                    public void onSuccess(Data result)
                    {
                        notice(false);
                        Dialog.tipNotice("修改成功！");
                        Token token = Flat.get().getCurrentToken();
                        token.directPage(CurrencyCtrlPage.class);
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
                token.directPage(CurrencyCtrlPage.class);
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
        currencyCtrl = token.getParam( uCurrencyCtrl.CurrencyCd().getName());
        RPC.ajax("rpc/currencyCtrlServer/getCurrencyCtrl", new RpcCallback<Data>()
        {
            @Override
            public void onSuccess(Data arg0)
            {
                editForm.setFormData(arg0);
               
            }
        }, currencyCtrl);
    }
}
/*	
	public static final String PAGE_ID = "auth-currencyCtrl-update";

	public final static String CURRENCYCD = "currencyCd";

	private String currencyCd;

	private YakDynamicForm detailForm;

	@Inject
	private ClientUtils clientUtils;

	@Inject
	private UCurrencyCtrl uCurrencyCtrl;

	@Inject
	private CurrencyCtrlConstants constants;

	@Inject
	private CurrencyCtrlInterAsync server;

	public CurrencyCtrlDetailPage() {

		super(PAGE_ID, false);
	}

	@Override
	public void updateView(ParamMapToken token) {

		detailForm.clearValues();
		detailForm.reset();
		// selectOptionValueUtil.getCurrencyCd(currencyCdItem);
		currencyCd = token.getParam(CURRENCYCD);
		RPCTemplate.call(new RPCExecutor<CurrencyCtrl>() {

			@Override
			public void execute(AsyncCallback<CurrencyCtrl> callback) {

				server.getCurrencyCd(currencyCd, callback);
			}

			@Override
			public void onSuccess(CurrencyCtrl result) {

				detailForm.setValues(CurrencyCtrlMapHelper.convertToMap(result));
			}

		}, detailForm);

	}

	@Override
	public String getTitle() {

		return constants.currenCyCtrlDetail();
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
			detailForm.setNumCols(2);
			detailForm.setWidth100();
			detailForm.setColWidths(200, "*");
			detailForm.setFields(uCurrencyCtrl.CurrencyCd().asLabel().createFormItem(), 
					uCurrencyCtrl.ConversionRt().required().createFormItem(), 
					uCurrencyCtrl.ValidInd().createFormItem(), 
					uCurrencyCtrl.MaxTxnAmtLcl().required().createFormItem(), 
					uCurrencyCtrl.MaxTxnAmtFrg().required().createFormItem());

		}
		addMember(detailForm);
		
		HLayout hLayout = clientUtils.createSubmitReturnLayout(detailForm, new ControlPanelPlace(CurrencyCtrlPage.PAGE_ID));
		hLayout.setWidth(300);
		addMember(hLayout);

		detailForm.addSubmitValuesHandler(new SubmitValuesHandler() {

			@Override
			public void onSubmitValues(SubmitValuesEvent event) {

				if (detailForm.validate()) {
					RPCTemplate.call(new RPCExecutor<Void>() {

						@SuppressWarnings("unchecked")
						@Override
						public void execute(AsyncCallback<Void> callback) {

							server.updateCurrencyCd(detailForm.getValues(), callback);
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

