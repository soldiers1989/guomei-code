package com.sunline.ccs.param.ui.client.countryctrl;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCountryCtrl;
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
 * import com.sunline.pcm.facility.client.selectopt.BmpSelectOptionUtils; import
 * com.sunline.pcm.ui.common.client.ClientUtils; import
 * com.sunline.pcm.ui.common.client.DispatcherPage; import
 * com.sunline.pcm.ui.cp.client.panel.ControlPanelPlace; import
 * com.sunline.ccs.infrastructure.client.ui.UCountryCtrl; import
 * com.sunline.ark.gwt.client.mvp.ParamMapToken; import
 * com.sunline.ark.gwt.client.ui.YakDynamicForm; import
 * com.sunline.ark.gwt.client.util.RPCExecutor; import
 * com.sunline.ark.gwt.client.util.RPCTemplate; import
 * com.google.gwt.resources.client.ImageResource; import
 * com.google.gwt.user.client.rpc.AsyncCallback; import
 * com.google.inject.Inject; import com.google.inject.Singleton; import
 * com.smartgwt.client.widgets.form.events.SubmitValuesEvent; import
 * com.smartgwt.client.widgets.form.events.SubmitValuesHandler; import
 * com.smartgwt.client.widgets.form.fields.SelectItem; import
 * com.smartgwt.client.widgets.layout.HLayout;
 */
/**
 * 
 * 
 * @author guxiaoyu
 * 
 */
@Singleton
public class AuthCountryAddPage extends SavePage {

    @Inject
    private UCountryCtrl uCountryCtrl;

    private KylinForm addForm;

    @Override
    public IsWidget createPage() {
	VerticalPanel panel = new VerticalPanel();
	addForm = new KylinForm();
	addForm.setWidth("100%");
	addForm.getSetting().labelWidth(120);
	addForm.setField(uCountryCtrl.CountryCode().asSelectItem().required(true), 
	                 uCountryCtrl.MaxTxnAmtLcl().setNewline(true).required(true),
			 uCountryCtrl.MaxTxnAmtFrg().setNewline(true).required(true), uCountryCtrl.ValidInd().asCheckBoxItem()
				 .setNewline(true));

	KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener(){
	    @Override
	    public void onClick() {
		
		if(!addForm.valid()){
		    return;}
		// 保存数据
		RPC.ajax("rpc/authCountryServer/addAuthCountry", new RpcCallback<Data>(){
		    @Override
		    public void onSuccess(Data result) {
			 Dialog.tipNotice("增加成功！");
			notice(false);
			Token token = Flat.get().getCurrentToken();
			token.directPage(AuthCountryListPage.class);
			Flat.get().goTo(token);
		    }
		}, addForm.getSubmitData().asMapData().toMap());
	    }
	});
	KylinButton cBtn = ClientUtils.creCancelButton(new IClickEventListener(){
	    @Override
	    public void onClick() {
		Token token = Flat.get().getCurrentToken();
		token.directPage(AuthCountryListPage.class);
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
	

	// 获取国家代码数据更新下拉框
	RPC.ajax("rpc/authCountryServer/getCountryCd", new RpcCallback<Data>(){
	    @Override
	    public void onSuccess(Data result) {
		// Dialog.tipNotice("获取数据成功！");
		SelectItem<String> si2 = new SelectItem<String>();
		si2.setValue(result.asListData());
		addForm.setFieldSelectData(uCountryCtrl.CountryCode().getName(), si2);
	    }
	});
    }
}
/*
 * public static final String PAGE_ID = "auth-country-add";
 * 
 * @Inject private ClientUtils clientUtils;
 * 
 * @Inject private AuthCountryInterAsync server;
 * 
 * @Inject private AuthCountryConstants constants;
 * 
 * @Inject private UCountryCtrl uCountryCtrl;
 * 
 * @Inject private BmpSelectOptionUtils bmpSelectOptUtils;
 * 
 * private SelectItem countryCd;
 * 
 * private YakDynamicForm form;
 * 
 * public AuthCountryAddPage() {
 * 
 * super(PAGE_ID, false);
 * 
 * }
 * 
 * @Override public void updateView(ParamMapToken token) { form.reset(); }
 * 
 * @Override public String getTitle() {
 * 
 * return constants.authCountryAdd(); }
 * 
 * @Override public ImageResource getIcon() { // TODO Auto-generated method stub
 * return null; }
 * 
 * @Override protected void createCanvas() {
 * 
 * setMembersMargin(10);
 * 
 * form = new YakDynamicForm(); { form.setNumCols(2); form.setWidth100();
 * form.setColWidths(200, "*"); countryCd =
 * uCountryCtrl.CountryCode().required().createSelectItem();
 * 
 * form.setItems(countryCd,
 * uCountryCtrl.MaxTxnAmtLcl().required().createFormItem(),
 * uCountryCtrl.MaxTxnAmtFrg().required().createFormItem(),
 * uCountryCtrl.ValidInd().createFormItem());
 * 
 * bmpSelectOptUtils.setCountryCd(countryCd); } addMember(form);
 * 
 * HLayout hLayout = clientUtils.createSubmitReturnLayout(form, new
 * ControlPanelPlace(AuthCountryListPage.PAGE_ID)); hLayout.setWidth(300);
 * addMember(hLayout);
 * 
 * form.addSubmitValuesHandler(new SubmitValuesHandler() {
 * 
 * @Override public void onSubmitValues(SubmitValuesEvent event) { if
 * (form.validate()) { RPCTemplate.call(new RPCExecutor<Void>() {
 * 
 * @SuppressWarnings("unchecked")
 * 
 * @Override public void execute(AsyncCallback<Void> callback) {
 * server.addNewAuthCountry(form.getValues(), callback); }
 * 
 * public void onSuccess(Void result) { clientUtils.showSuccess(); form.reset();
 * }; }, form); } } }); }
 */

