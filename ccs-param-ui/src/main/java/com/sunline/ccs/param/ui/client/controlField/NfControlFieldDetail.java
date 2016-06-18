package com.sunline.ccs.param.ui.client.controlField;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UNfControlField;
import com.sunline.ccs.param.ui.client.authReasonMapping.AuthReasonMappingPage;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.core.client.Flat;
import com.sunline.kylin.web.core.client.mvp.Token;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.kylin.web.core.client.res.SavePage;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.IClickEventListener;

@Singleton
public class NfControlFieldDetail extends SavePage {

    public final static String KEY = "key";

    @Inject
    private UNfControlField uNfControlField;

    private String key;

    private KylinForm detailForm;

    

    @Override
    public IsWidget createPage() {

	VerticalPanel panel = new VerticalPanel();
	detailForm = new KylinForm();
	detailForm.setWidth("100%");
	detailForm.getSetting().labelWidth(120);
	detailForm.setField(uNfControlField.FieldCode().readonly(true).required(true).setNewline(true), uNfControlField
		.Priority().required(true).setNewline(true), uNfControlField.FieldName().required(true)
		.setNewline(true));

	KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener(){
	    @Override
	    public void onClick() {
		MapData data = detailForm.getSubmitData().asMapData();
		// 保存数据
		if (detailForm.valid()) {
		    RPC.ajax("rpc/nfControlFieldServer/updateNfControlField", new RpcCallback<Data>(){
			@Override
			public void onSuccess(Data result) {
			    notice(false);
			    Dialog.tipNotice("修改成功！");
			    Token token = Flat.get().getCurrentToken();
			    token.directPage(NfControlFieldPage.class);
			    Flat.get().goTo(token);
			}
		    }, data);
		}
	    }
	});
	KylinButton cBtn = ClientUtils.creCancelButton(new IClickEventListener(){
	    @Override
	    public void onClick() {
		Token token = Flat.get().getCurrentToken();
		token.directPage(NfControlFieldPage.class);
		Flat.get().goTo(token);
	    }
	});
	// TODO Auto-generated method stub
	addButton(submitBtn);
	addButton(cBtn);
	panel.add(detailForm);
	return panel;
    }
    @Override
    public void refresh() {
	key = Flat.get().getCurrentToken().getParam(KEY);

	RPC.ajax("rpc/nfControlFieldServer/getNfControlField", new RpcCallback<Data>(){
	    @Override
	    public void onSuccess(Data arg0) {
		detailForm.getUi().clear();

		detailForm.setFormData(arg0);
	    }
	}, key);
    }
    /*
     * public static final String PAGE_ID = "auth-nfControlField-update";
     * 
     * public final static String FIELDCODE = "fieldCode";
     * 
     * @Inject private NfControlFieldConstants constants;
     * 
     * @Inject private UNfControlField uNfControlField;
     * 
     * @Inject private ClientFactory clientFactory;
     * 
     * @Inject private ClientUtils clientUtils;
     * 
     * @Inject private NfControlFieldInterAsync server;
     * 
     * private NfControlField nfControlField;
     * 
     * private String fieldCode;
     * 
     * private YakDynamicForm detailForm;
     * 
     * public NfControlFieldDetail() { super(PAGE_ID, false); }
     * 
     * @Override public void updateView(ParamMapToken token) {
     * detailForm.clearValues(); detailForm.reset(); fieldCode =
     * token.getParam(FIELDCODE); RPCTemplate.call(new
     * RPCExecutor<NfControlField>() {
     * 
     * @Override public void execute(AsyncCallback<NfControlField> callback) {
     * 
     * server.getNfControlField(fieldCode, callback); }
     * 
     * @Override public void onSuccess(NfControlField result) {
     * 
     * if(result == null) nfControlField = new NfControlField(); else
     * nfControlField = result;
     * 
     * detailForm.setValues(NfControlFieldMapHelper.convertToMap(nfControlField))
     * ;
     * 
     * }
     * 
     * });
     * 
     * }
     * 
     * @Override public String getTitle() { return
     * constants.nfControlFieldDetail(); }
     * 
     * @Override public ImageResource getIcon() { // TODO Auto-generated method
     * stub return null; }
     * 
     * @Override protected void createCanvas() { setMembersMargin(10);
     * 
     * detailForm = new YakDynamicForm(); { detailForm.setNumCols(2);
     * detailForm.setWidth100();
     * 
     * 
     * detailForm.setFields(uNfControlField.FieldCode().required().asLabel().
     * createFormItem(),
     * uNfControlField.Priority().required().required().createFormItem(),
     * uNfControlField
     * .FieldName().required().asTextArea().colSpan(6).createTextAreaItem() ); }
     * addMember(detailForm);
     * 
     * 
     * final IButton saveButton = clientUtils.createSaveButton(); final IButton
     * returnButton = clientUtils.createReturnButton();
     * returnButton.addClickHandler(new ClickHandler() {
     * 
     * @Override public void onClick(ClickEvent event) { ControlPanelPlace place
     * = new ControlPanelPlace(NfControlFieldPage.PAGE_ID);
     * clientFactory.goTo(place); } });
     * 
     * HLayout hLayout = new HLayout(); hLayout.setAlign(Alignment.CENTER);
     * hLayout.setMembersMargin(10); hLayout.addMember(saveButton);
     * hLayout.addMember(returnButton); addMember(hLayout);
     * 
     * saveButton.addClickHandler(new ClickHandler() {
     * 
     * @Override public void onClick(ClickEvent event) {
     * 
     * if (detailForm.validate()) {
     * 
     * RPCTemplate.call(new RPCExecutor<Void>() {
     * 
     * @SuppressWarnings("unchecked")
     * 
     * @Override public void execute(AsyncCallback<Void> callback) {
     * 
     * NfControlFieldMapHelper.updateFromMap(nfControlField,
     * detailForm.getValues()); server.updateNfServiceDef(nfControlField,
     * callback); }
     * 
     * @Override public void onSuccess(Void result) {
     * 
     * clientUtils.showSuccess(); } });
     * 
     * } } });
     * 
     * }
     */
}
