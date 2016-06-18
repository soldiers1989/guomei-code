package com.sunline.ccs.param.ui.client.currencyCtrl;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCurrencyCtrl;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
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
import com.sunline.pcm.facility.client.selectopt.BmpSelectOptionUtils;
import com.sunline.pcm.ui.common.client.ClientUtils;
import com.sunline.pcm.ui.common.client.DispatcherPage;
import com.sunline.pcm.ui.cp.client.panel.ControlPanelPlace;
import com.sunline.ccs.infrastructure.client.ui.UCurrencyCtrl;
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
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.layout.HLayout;
*/
@Singleton
public class CurrencyCtrlAddPage extends SavePage {
	
	
	@Inject
	private UCurrencyCtrl uCurrencyCtrl;
    
    
    private KylinForm addForm;
    

	@Override
	public IsWidget createPage() {
		VerticalPanel panel = new VerticalPanel();
        addForm = new KylinForm();
        addForm.setWidth("100%");
        addForm.getSetting().labelWidth(120);
        addForm.setField(uCurrencyCtrl.CurrencyCd().asSelectItem().required(true),
        		uCurrencyCtrl.ConversionRt().setNewline(true).required(true),
        		uCurrencyCtrl.ValidInd().asCheckBoxItem().setNewline(true),
        		uCurrencyCtrl.MaxTxnAmtLcl().setNewline(true).required(true),
        		uCurrencyCtrl.MaxTxnAmtFrg().setNewline(true).required(true)
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
                RPC.ajax("rpc/currencyCtrlServer/addCurrencyCtrl", new RpcCallback<Data>()
                {
                    @Override
                    public void onSuccess(Data result)
                    {
                        Dialog.tipNotice("增加成功！");
                        notice(false);
                        Token token = Flat.get().getCurrentToken();
                        token.directPage(CurrencyCtrlPage.class);
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
                token.directPage(CurrencyCtrlPage.class);
                Flat.get().goTo(token);
            }
        });
        addButton(submitBtn);
        addButton(cBtn);
        panel.add(addForm);
        return panel;
	}

	@Override
	public void refresh() {
		
	    addForm.getUi().clear();
	
		//获取币种代码数据更新下拉框
		RPC.ajax("rpc/currencyCtrlServer/getCurrencyCd", new RpcCallback<Data>() {
			@Override
			public void onSuccess(Data result) {
				//Dialog.tipNotice("获取数据成功！");
				SelectItem<String> si2 = new SelectItem<String>();
				si2.setValue(result.asListData());
				addForm.setFieldSelectData(uCurrencyCtrl.CurrencyCd().getName(), si2);
			}
		});
	}
}
/*	
	public static final String PAGE_ID = "auth-currencyCtrl-add";

	private YakDynamicForm addForm;

	private SelectItem currencyCd;

	@Inject
	private ClientUtils clientUtils;

	@Inject
	private BmpSelectOptionUtils bmpSelectOptUtils;

	@Inject
	private UCurrencyCtrl uCurrencyCtrl;

	@Inject
	private CurrencyCtrlConstants constants;

	@Inject
	private CurrencyCtrlInterAsync server;

	public CurrencyCtrlAddPage() {

		super(PAGE_ID, false);
	}

	@Override
	public void updateView(ParamMapToken token) {
		addForm.reset();
		bmpSelectOptUtils.setCurrencyCd(currencyCd);
	}

	@Override
	public String getTitle() {

		return constants.currenCyCtrlAdd();
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
			addForm.setNumCols(2);
			addForm.setWidth100();
			addForm.setColWidths(200, "*");
			currencyCd = uCurrencyCtrl.CurrencyCd().required().createSelectItem();

			addForm.setFields(currencyCd, 
					uCurrencyCtrl.ConversionRt().required().createFormItem(), 
					uCurrencyCtrl.ValidInd().createFormItem(), 
					uCurrencyCtrl.MaxTxnAmtLcl().required().createFormItem(), 
					uCurrencyCtrl.MaxTxnAmtFrg().required().createFormItem());
			
		}
		addMember(addForm);
		
		HLayout hLayout = clientUtils.createSubmitReturnLayout(addForm, new ControlPanelPlace(CurrencyCtrlPage.PAGE_ID));
		hLayout.setWidth(300);
		addMember(hLayout);

		addForm.addSubmitValuesHandler(new SubmitValuesHandler() {

			@Override
			public void onSubmitValues(SubmitValuesEvent event) {

				if (addForm.validate()) {
					RPCTemplate.call(new RPCExecutor<Void>() {

						@SuppressWarnings("unchecked")
						@Override
						public void execute(AsyncCallback<Void> callback) {

							server.addCurrencyCd(addForm.getValues(), callback);
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

