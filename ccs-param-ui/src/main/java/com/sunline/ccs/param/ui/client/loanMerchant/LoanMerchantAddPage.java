package com.sunline.ccs.param.ui.client.loanMerchant;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.ULoanMerchant;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.validator.ChineseAddressHelper;
import com.sunline.kylin.web.core.client.Flat;
import com.sunline.kylin.web.core.client.mvp.Token;
import com.sunline.kylin.web.core.client.res.SavePage;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.kylin.web.core.client.util.SelectItemUtil;
import com.sunline.pcm.infrastructure.client.ui.UOfficialCardCorpInfo;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.function.IFunction;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.IClickEventListener;

/**
 * 分组商户维护管理添加页面
 * 
 * @author lisy
 * @version [版本号, Jun 23, 2015]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
@Singleton
public class LoanMerchantAddPage extends SavePage {

	@Inject
	private ULoanMerchant uLoanMerchant;
	@Inject
	private UOfficialCardCorpInfo uOfficialCardCorpInfo;
	
	private KylinForm addForm;
	private ChineseAddressHelper addressHelper;

	/**
	 * 省份，只有国家码为中国才有数据
	 */
	private TextColumnHelper provinceColumn;

	@Override
	public IsWidget createPage() {
		VerticalPanel panel = new VerticalPanel();
		addForm = new KylinForm();
		addForm.setWidth("100%");

		provinceColumn = uLoanMerchant.MerState().asSelectItem().bindEvent("change", new IFunction() {
			@Override
			public void function() {
				String province = addForm.getFieldValue(uLoanMerchant.MerState().getName());
				SelectItemUtil.initSelectItem(addForm,uLoanMerchant.MerCity().getName(), 
								SelectType.LABLE, "rpc/loanMerchantServer/getCities",province);
			}
		});
		addForm.setField(uLoanMerchant.MerId().required(true), 
				uLoanMerchant.MerName().required(true),
				uLoanMerchant.RecStatus().required(true), 
				uLoanMerchant.MerType().required(true),
				uLoanMerchant.MerGroup().required(true).asSelectItem(),
				provinceColumn.required(true),
				uLoanMerchant.MerCity().required(true).asSelectItem(),
				uLoanMerchant.MerAddr().required(true).asTextArea().setNewline(true), 
				uLoanMerchant.MerPstlCd().required(true).setNewline(true), 
				uLoanMerchant.MerLinkMan().required(true),
				// 此处涉及电话号码验证
				uLoanMerchant.MerPhone().required(true),//.setValidators(this.getPhone())
				uLoanMerchant.PosLoanSupportInd().required(true).asSelectItem(SelectType.LABLE), 
				uLoanMerchant.MotoLoanSupportInd().required(true).asSelectItem(SelectType.LABLE), 
				uLoanMerchant.EBankLoanSupportInd().required(true).asSelectItem(SelectType.LABLE), 
				uLoanMerchant.MacroLoanSupportInd().required(true).asSelectItem(SelectType.LABLE), 
				uLoanMerchant.PosLoanSingleAmtMin().required(true).setNewline(true), 
				uLoanMerchant.PosLoanSingleAmtMax().required(true), 
				uLoanMerchant.SpecLoanSingleAmtMin().required(true), 
				uLoanMerchant.SpecLoanSingleAmtMax().required(true), 
				uLoanMerchant.PosFeeIssPerc(), 
				uLoanMerchant.PosFeeAcqPerc(), 
				uLoanMerchant.MacroFeeIssPerc(), 
				uLoanMerchant.MacroFeeAcqPerc(),
				uLoanMerchant.MerBranche().asSelectItem(), 
				uLoanMerchant.MerBrand().required(true), 
				uLoanMerchant.Memo().asTextArea().setColumnWidth("100%"));
		addForm.setCol(2);
		addForm.getSetting().labelWidth(155);
		KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener() {

			@Override
			public void onClick() {
			    if(!addForm.valid()){
				return;
			    }
				// 保存数据
				RPC.ajax("rpc/loanMerchantServer/addLoanMerchant", new RpcCallback<Data>() {

					@Override
					public void onSuccess(Data result) {
						Dialog.tipNotice("增加成功！");
						notice(false);
						Token token = Flat.get().getCurrentToken();
						token.directPage(LoanMerchantPage.class);
						Flat.get().goTo(token);
					}
				}, addForm.getSubmitData().asMapData().toMap());
			}
		});
		KylinButton cBtn = ClientUtils.creCancelButton(new IClickEventListener() {

			@Override
			public void onClick() {
				Token token = Flat.get().getCurrentToken();
				token.directPage(LoanMerchantPage.class);
				Flat.get().goTo(token);
			}
		});
		addButton(submitBtn);
		addButton(cBtn);
		panel.add(addForm);
		return addForm;
	}

	public void refresh() {
	    addForm.getUi().clear();
		// 获取商户分组更新下拉框
		RPC.ajax("rpc/loanMerchantServer/getMerGroup", new RpcCallback<Data>() {

			@Override
			public void onSuccess(Data result) {
				SelectItem<String> si1 = new SelectItem<String>();
				si1.setValue(result.asListData());
				addForm.setFieldSelectData(uLoanMerchant.MerGroup().getName(), si1);
			}
		});
		RPC.ajax("rpc/loanMerchantServer/getProvince", new RpcCallback<Data>() {

			@Override
			public void onSuccess(Data result) {
				SelectItem<String> si1 = new SelectItem<String>();
				si1.setValue(result.asListData());
				addForm.setFieldSelectData(uLoanMerchant.MerState().getName(), si1);
			}
		});
		//获取所属分行更新下拉框
		RPC.ajax("rpc/ccsSelectOptionServer/getBranchList", new RpcCallback<Data>() {

			@Override
			public void onSuccess(Data result) {
				SelectItem<String> si1 = new SelectItem<String>();
				si1.setValue(result.asListData());
				addForm.setFieldSelectData(uLoanMerchant.MerBranche().getName(), si1);
			}
		});
	}
}