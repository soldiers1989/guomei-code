package com.sunline.ccs.ui.client.security.risk;

import com.sunline.ccs.infrastructure.client.ui.UCcsOpPrivilege;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.utils.StringUtils;
import com.sunline.kylin.web.core.client.res.SavePage;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.kylin.web.flat.client.data.ClientContext;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.IClickEventListener;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * 操作员权限控制页面
 *
 */
@Singleton
public class RiskPage extends SavePage{
	
	public static final String PAGE_ID = "sec-risk";
	
	@Inject
	private RiskConstants constants;
	
	@Inject
	private UCcsOpPrivilege uTmOperAuth;
	
	private KylinForm searchForm; //操作员查询表单
	private KylinButton searchBtn;
	
	private KylinForm operForm; //操作员信息表单
	
	@Override
	public IsWidget createPage() {
		//操作员查询表单
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");
		searchForm = new KylinForm();
		{
			searchForm.setWidth("98%");
			searchForm.setCol(4);
           
			searchBtn = ClientUtils.createSearchButton(new IClickEventListener(){
					@Override
					public void onClick() {
						if(searchForm.valid()){
							RPC.ajax("rpc/riskServer/getOperatorInfo",new RpcCallback<Data>(){
								@Override
								public void onSuccess(Data result) {
									operForm.setFormData(result);
								}
							},searchForm.getFieldValue(uTmOperAuth.OpId().getName()));
						}
					}});
			
			searchForm.setField(
					uTmOperAuth.OpId().required(true).setGroup(constants.titleSearchForm()).setGroupicon("skins/icons/communication.gif")
					);
			searchForm.addButton(searchBtn);
			
		}
		
		panel.add(searchForm);
		
		
		//操作员信息维护表单
		operForm = new KylinForm();
		{
			operForm.setWidth("98%");
			operForm.setCol(3);
			
			operForm.setField(
					uTmOperAuth.OpId().readonly(true).setGroup(constants.titleOperInfoForm()).setGroupicon("skins/icons/communication.gif"),
					uTmOperAuth.MaxLmtAdj().required(true).setNewline(true),
					uTmOperAuth.MaxPointsAdj().required(true),
					uTmOperAuth.MaxAcctTxnAdj().required(true),
					uTmOperAuth.MaxCashloanAdj().required(true),
					uTmOperAuth.MaxLoanApproveAdj().required(true),
					uTmOperAuth.LstUpdTime().readonly(true).setNewline(true),
					uTmOperAuth.LstUpdUser().readonly(true)
					);
		}
		
		panel.add(operForm);
		
		addButton(ClientUtils.creConfirmButton(new IClickEventListener(){

			@Override
			public void onClick() {
				if(!operForm.valid()){
					return;
				}
				String operatorId = operForm.getFieldValue(uTmOperAuth.OpId().getName());
				//不允许修改自己的权限
//				if(new ClientContext().getCurrentUser().getId().equals(operatorId)){
//					Dialog.alert(constants.cannotModifyOwn());
//					return;
//				}
				//先查询操作员合法性再修改保存
				if(StringUtils.isEmpty(operatorId)) {
					Dialog.alert(constants.msgOperIdEmptyForSave());
					return;
				}
				RPC.ajax("rpc/riskServer/saveOperatorAuth",new RpcCallback<Data>(){

					@Override
					public void onSuccess(Data result) {
						Dialog.alert("操作成功");
					}
				},operForm.getSubmitData().asMapData());
			}
		}));
		return panel;
	}
	
	@Override
	public void refresh(){
		searchForm.getUi().clear();
		operForm.getUi().clear();
	}

}
