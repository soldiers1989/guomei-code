package com.sunline.ccs.ui.client.pages.frzaccount;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsAcct;
import com.sunline.ccs.infrastructure.client.ui.UCcsCardLmMapping;
import com.sunline.ccs.infrastructure.client.ui.UCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.ui.client.commons.CommonUiUtils;
import com.sunline.kylin.web.ark.client.helper.ColumnHelper;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.dialog.client.listener.ConfirmCallbackFunction;
import com.sunline.ui.event.client.IClickEventListener;

/**
 * 
 * @see 类名：FrozenUserAccount
 * @see 描述：用户账户冻结
 *
 * @see 创建日期： Jun 22, 20157:22:33 PM
 * @author yeyu
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Singleton
public class FrozenUserAccount extends Page {

    @Inject
    private UCcsAcct uCcsAcct;
    @Inject
    private UCcsCustomer uCcsCustomer;
    @Inject
    private UCcsCardLmMapping cardMapping;
    @Inject
    private FrozenUserAccountConstants constants;

    private VerticalPanel mainWindow;

    private KylinForm queryForm;

    private KylinForm updForm;

    @Override
    public IsWidget createPage() {
	this.getMainWindow();
	this.buildQueryForm(mainWindow);
	this.buildUpdForm(mainWindow);
	return mainWindow;
    }

    @Override
    public void refresh() {
	queryForm.getUi().clear();
	updForm.getUi().clear();
    }

    /**
     * 
     * @see 方法名：buildQueryForm
     * @see 描述：创建查询表单
     * @see 创建日期：Jun 22, 20157:28:59 PM
     * @author yeyu
     * 
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private void buildQueryForm(VerticalPanel mainWindow) {
	queryForm = new KylinForm();
	queryForm.setWidth("100%");
	queryForm.setField(new ColumnHelper[]{cardMapping.CardNbr().required(true), uCcsAcct.AcctType().asSelectItem(SelectType.KEY_LABLE).required(true)});
	KylinButton btnSearch = new KylinButton("查询",null);
	btnSearch.addClickEventListener(new IClickEventListener(){
	    @Override
	    public void onClick() {
		if (queryForm.valid()) {
		    RPC.ajax("rpc/acctServer/getAcctInfo", new RpcCallback<Data>(){
			@Override
			public void onSuccess(Data result) {
			    final MapData acctInfo = result.asMapData();
			    RPC.ajax("rpc/custServer/getCustInfoByCardNo",
				     new RpcCallback<Data>(){
					 @Override
					 public void onSuccess(Data result) {
					     final MapData custInfo = result.asMapData();
					     updForm.getUi().setData(custInfo);
					     updForm.getUi().setData(acctInfo);
					     RPC.ajax("rpc/frozenUserAccountServer/getOpeLogAndRemark", new RpcCallback<Data>(){
							@Override
							public void onSuccess(Data result) {
								updForm.getUi().setData(result);
							}
					     }, new Object[]{queryForm.getUi().getSubmitData().asMapData().getString(CcsCardLmMapping.P_CardNbr),"3301"});
					 }
				     },
				     new Object[]{queryForm.getUi().getSubmitData().asMapData().getString(CcsCardLmMapping.P_CardNbr)});
			}
		    }, new Object[]{queryForm.getUi().getSubmitData().asMapData().getString(CcsAcct.P_AcctType),
			    queryForm.getUi().getSubmitData().asMapData().getString(CcsCardLmMapping.P_CardNbr)});
		}
	    }
	});
	HorizontalPanel hPanel = CommonUiUtils.lineLayoutForm(queryForm, btnSearch, null, null);
	VerticalPanel queryFormPanel = new VerticalPanel();
	queryFormPanel.setWidth("100%");
	queryFormPanel.add(hPanel);
	mainWindow.add(queryFormPanel);
    }

    /**
     * 
     * @see 方法名：buildUpdForm
     * @see 描述：创建修改表单
     * @see 创建日期：Jun 22, 20157:50:53 PM
     * @author yeyu
     * 
     * @param mainWindow
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private void buildUpdForm(VerticalPanel mainWindow) {
	updForm = new KylinForm();
	updForm.setField(new ColumnHelper[]{uCcsCustomer.Name().readonly(true), uCcsCustomer.IdType().readonly(true),
		uCcsCustomer.IdNo().readonly(true), uCcsAcct.Currency().readonly(true),
		new TextColumnHelper(constants.frostInd(), constants.frostInd(), 50).readonly(true),
		new TextColumnHelper("remark", "备注", 300)});
	KylinButton frozenAcct = new KylinButton("冻结",null);
	frozenAcct.addClickEventListener(new IClickEventListener(){

	    @Override
	    public void onClick() {
		Dialog.confirm("是否确认修改", "", new ConfirmCallbackFunction(){

		    @Override
		    public void corfimCallback(boolean value) {
			if (value) {
			    MapData mapData = updForm.getUi().getSubmitData().asMapData();
			    MapData searchData = queryForm.getUi().getSubmitData().asMapData();
			    final String remark = mapData.getString("remark");
			    final String acctType = searchData.getString(CcsAcct.P_AcctType);
			    final String cardNo = searchData.getString(CcsCardLmMapping.P_CardNbr);
			    RPC.ajax("rpc/frozenUserAccountServer/personAccountFrozen", new RpcCallback<Data>(){
				@Override
				public void onSuccess(Data result) {
				    Dialog.tipNotice("修改成功", 1000);
				}
			    }, new Object[]{cardNo, acctType, remark});
			}
		    }
		});
	    }
	});
	updForm.addButton(frozenAcct);
	VerticalPanel updFormPanel = new VerticalPanel();
	updFormPanel.setWidth("100%");
	updFormPanel.add(updForm);
	mainWindow.add(updFormPanel);
    }

    /**
     * 
     * @see 方法名：getMainWindow
     * @see 描述：初始化主窗体
     * @see 创建日期：Jun 22, 20157:25:50 PM
     * @author yeyu
     * 
     * @return com.google.gwt.user.client.ui.VerticalPanel
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private VerticalPanel getMainWindow() {
	mainWindow = new VerticalPanel();
	mainWindow.setWidth("100%");
	return mainWindow;
    }

}
