package com.sunline.ccs.param.ui.client.authMccStateCurrXVerify;

import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UAuthMccStateCurrXVerify;
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
public class AuthMccStateCurrXVerifyDetailPage extends SavePage {

    @Inject
    private UAuthMccStateCurrXVerify uAuthMccStateCurrXVerify;

    // private KylinGrid grid = new KylinGrid();

    private KylinForm form;

    private String key;

    @Override
    public IsWidget createPage() {
	VerticalPanel panel = new VerticalPanel();
	panel.setWidth("100%");
	panel.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);

	form = new KylinForm();
	form.getSetting().labelWidth(115);
	form.setField(uAuthMccStateCurrXVerify.InputSource().readonly(true), uAuthMccStateCurrXVerify.CountryCode()
			      .readonly(true), uAuthMccStateCurrXVerify.MccCode().readonly(true),
		      uAuthMccStateCurrXVerify.TransCurrencyCode().readonly(true), uAuthMccStateCurrXVerify
			      .CurrentActiveFlag().asCheckBoxItem(),
		      uAuthMccStateCurrXVerify.ForbiddenFlag().asCheckBoxItem(), uAuthMccStateCurrXVerify.MaxAmtLcl()
			      .required(true), uAuthMccStateCurrXVerify.MaxAmtFrg().required(true));

	form.setCol(2);

	KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener(){
	    @Override
	    public void onClick() {
		if (!form.valid()) {
		    return;
		}
		MapData formData = form.getSubmitData().asMapData();

		RPC.ajax("rpc/authMccStateCurrXVerifyServer/updateAuthMccStateCurrXVerify", new RpcCallback<Data>(){
		    @Override
		    public void onSuccess(Data result) {
			notice(false);
			Dialog.tipNotice("修改成功！");
			Token token = Flat.get().getCurrentToken();
			token.directPage(AuthMccStateCurrXVerifyPage.class);
			Flat.get().goTo(token);
		    }
		}, formData.toMap());
	    }
	});

	KylinButton cBtn = ClientUtils.creCancelButton(new IClickEventListener(){
	    @Override
	    public void onClick() {
		Token token = Flat.get().getCurrentToken();
		token.directPage(AuthMccStateCurrXVerifyPage.class);
		Flat.get().goTo(token);
	    }
	});

	addButton(submitBtn);
	addButton(cBtn);
	panel.add(form);
	return panel;

    }

    @Override
    public void refresh() {
	form.getUi().clear();
	notice(true);

	key = Flat.get().getCurrentToken().getParam("AuthMccStateCurrXVerifyKey");

	RPC.ajax("rpc/authMccStateCurrXVerifyServer/getAuthMccStateCurrXVerify", new RpcCallback<Data>(){
	    @Override
	    public void onSuccess(Data arg0) {
//		Dialog.alert(arg0.toString());
		form.setFormData(arg0);
	    }
	}, key);
    }

}
