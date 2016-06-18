package com.sunline.ccs.ui.client.pages.unfrzaccount;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsAcct;
import com.sunline.ccs.infrastructure.client.ui.UCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.ui.client.commons.UIUtil;
import com.sunline.kylin.web.ark.client.helper.EnumColumnHelper;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.utils.StringUtils;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.IClickEventListener;

/**
 * 
 * @see 类名：UnfrozenUserAccount
 * @see 描述：个人账户解除冻结
 *
 * @see 创建日期：   Jun 30, 20157:29:59 PM
 * @author lxc
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Singleton
public class UnfrozenUserAccount extends Page {

	public static final String PAGE_ID = "risk-3302";
	
	public static final String  T_BLOCKCODE = "T";

	@Inject
	private UnfrozenUserAccountConstants constants;
	
	@Inject
	private UIUtil  uiUtil;
	
	@Inject
	private UCcsAcct uCcsAcct;
	
	@Inject
	private UCcsCustomer uCcsCustomer;
	
	private KylinForm cardMediaMapForm; //个人账户解除冻结表单
	
	private KylinForm acctInfoForm;

	@Override
	public IsWidget createPage() {
		VerticalPanel page = new VerticalPanel();
		
		TextColumnHelper cardItem = uiUtil.createCardNoItem();
		 //个人账户解除冻结
		cardMediaMapForm = new KylinForm();
		cardMediaMapForm.setWidth("100%");
		cardMediaMapForm.setHeight("100px");
		acctInfoForm = new KylinForm();
		acctInfoForm.setWidth("100%");
		acctInfoForm.setHeight("100px");
		EnumColumnHelper accountTypeItem = uCcsAcct.AcctType().required(true).asSelectItem(SelectType.KEY_LABLE);
		TextColumnHelper custNameItem = uCcsAcct.Name().readonly(true).setDisplay(constants.custName());
		// 是否已冻结
		TextColumnHelper frostInd = new TextColumnHelper("frostInd", constants.frostInd(), 10).readonly(true);
		//冻结原因
		TextColumnHelper blockedReasItem = new TextColumnHelper("blockedReas", constants.blockedReason(), 128).readonly(true);
		//解冻原因
		TextColumnHelper remarkItem = new TextColumnHelper("remark", constants.remark(), 1000).asTextArea().required(true).setFieldWidth(490).setNewline(true);
		
		cardMediaMapForm.setField(
				cardItem,
				accountTypeItem
		);
		cardMediaMapForm.setCol(2);
		cardMediaMapForm.addButton(ClientUtils.createSearchButton(new IClickEventListener(){
			@Override
			public void onClick() {
				MapData data = cardMediaMapForm.getSubmitData().asMapData();
				String cardNbr = data.getString(CcsCardLmMapping.P_CardNbr);
				if (StringUtils.isBlank(data.getString(CcsAcct.P_AcctType))) {
				    Dialog.alertWarn(uCcsAcct.AcctType().getDisplay() + ": 该字段不能为空", "提示");
				    return;
				}
				AccountType acctType = AccountType.valueOf(data.getString(CcsAcct.P_AcctType));
				loadInfo(cardNbr,acctType);
			}
		}));
		acctInfoForm.setField(
				custNameItem,
				uCcsCustomer.IdType().readonly(true),
				uCcsCustomer.IdNo().readonly(true),
				uCcsAcct.Currency().readonly(true),
				frostInd,
				blockedReasItem,
				remarkItem
		);
		acctInfoForm.setCol(2);
		HorizontalPanel buttonPanel = new HorizontalPanel();
		buttonPanel.setWidth("650px");
		// 个人账户解除冻结表单提交时处理
		KylinButton cancelFrozenAcct = new KylinButton("解冻",null);
		cancelFrozenAcct.addClickEventListener(new IClickEventListener() {
			@Override
			public void onClick() {
				final MapData acctInfoData = acctInfoForm.getSubmitData().asMapData();
				final MapData cardInfoData = cardMediaMapForm.getSubmitData().asMapData();
				RPC.ajax("rpc/unfrozenUserAccountServer/cancelPersonAccountFrozen", new RpcCallback<Data>(){
					@Override
					public void onSuccess(Data result) {
						Dialog.tipNotice("保存成功", 1000);
					}
				}, new Object[]{
						cardInfoData.getString(CcsCardLmMapping.P_CardNbr),
						cardInfoData.getString(CcsAcct.P_AcctType),
						acctInfoData.getString("remark")
				});
			}
		});
		acctInfoForm.addButton(cancelFrozenAcct);
		page.add(cardMediaMapForm);
		page.add(acctInfoForm);
		return page;
	}
	
	/* (non-Javadoc)
	 * @see com.sunline.kylin.web.core.client.res.Page#refresh()
	 */
	@Override
	public void refresh() {
	    cardMediaMapForm.getUi().clear();
	    acctInfoForm.getUi().clear();
	}

	/**
	 * 
	 * @see 方法名：loadInfo 
	 * @see 描述：远程获取客户信息
	 * @see 创建日期：Jun 30, 20158:03:46 PM
	 * @author lxc
	 *  
	 * @param CardNo
	 * @param acctType
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private void loadInfo(final String CardNo, final AccountType acctType) {
		RPC.ajax("rpc/acctServer/getAcctInfo", new RpcCallback<Data>() {
			@Override
			public void onSuccess(Data result) {
				if(result.asMapData().toMap().keySet().contains(CcsAcct.P_BlockCode)){
					String blockCode = result.asMapData().getString(CcsAcct.P_BlockCode) == null ? "" : result.asMapData().getString(CcsAcct.P_BlockCode);
					if(hasBlockCode(blockCode, T_BLOCKCODE)){
						acctInfoForm.setFieldValue("frostInd", constants.yes());
						acctInfoForm.setFieldVisible("blockedReas", true);
					}else {
						acctInfoForm.setFieldValue("frostInd", constants.no());
						acctInfoForm.setFieldValue("blockedReas", null);
						acctInfoForm.setFieldVisible("blockedReas", false);
					}
				}
				acctInfoForm.setFormData(result);
			}
		}, acctType, CardNo);
		RPC.ajax("rpc/custServer/getCustInfoByCardNo", new RpcCallback<Data>() {
			@Override
			public void onSuccess(Data result) {
				acctInfoForm.setFormData(result);
			}
		}, CardNo);
	}

	/**
	 * 判断对应的锁定码是否存在
	 * 
	 * @param blockCode
	 * @param searchCodes
	 * @return
	 */
	public boolean hasBlockCode(String blockCode, String... searchCodes) {
		if (blockCode == null || blockCode.trim().length() == 0) {
			return false;
		}
		for (int i = 0; i < searchCodes.length; i++) {
			if (blockCode.indexOf(searchCodes[i]) != -1) {
				return true;
			}
		}
		return false;
	}
}
