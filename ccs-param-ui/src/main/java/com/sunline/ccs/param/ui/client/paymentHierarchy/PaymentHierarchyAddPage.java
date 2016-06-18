package com.sunline.ccs.param.ui.client.paymentHierarchy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.domain.BucketObjectDomainClient;
import com.sunline.ccs.infrastructure.client.ui.UPaymentHierarchy;
import com.sunline.ccs.param.def.enums.BucketObject;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.Flat;
import com.sunline.kylin.web.core.client.mvp.Token;
import com.sunline.kylin.web.core.client.res.SavePage;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.ListData;
import com.sunline.ui.core.client.util.DataUtil;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.IClickEventListener;


@Singleton
public class PaymentHierarchyAddPage extends SavePage {

	@Inject
	private UPaymentHierarchy uPaymentHierarchy;

	@Inject
	private BucketObjectDomainClient bucketObjectDomainClient;

	private KylinForm addForm = new KylinForm();

	private KylinGrid addGrid = new KylinGrid();

	@Override
	public IsWidget createPage() {
		// 页面参数设置
		VerticalPanel panel = new VerticalPanel();
		addForm.setWidth("100%");
		addForm.setField(uPaymentHierarchy.PmtHierId().required(true), uPaymentHierarchy.Description().asTextArea().setNewline(true));
		addForm.setCol(2);
		addGrid.setColumns(new TextColumnHelper("paymentHierarchy", "余额成分", 40));
		addGrid.rownumbers(true);
		addGrid.getSetting().usePager(false);
		addGrid.getSetting().checkbox(false);
		addGrid.setWidth(500);
		addGrid.getSetting().rowDraggable(true);
		// 填充grid数据
		LinkedHashMap<String, String> Map = bucketObjectDomainClient.asLinkedHashMap(false);
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for(String key : Map.keySet()) {
			sb.append("{\"").append("paymentHierarchy").append("\":").append("\"").append(Map.get(key)).append("\"}")
					.append(",");
		}
		sb.deleteCharAt(sb.lastIndexOf(","));
		sb.append("]");
		addGrid.loadData(StringToData(sb.toString()));
		
		//提交按钮
		KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener() {

			@Override
			public void onClick() {
			    if(!addForm.valid())return;
				LinkedHashMap<String, String> Map = bucketObjectDomainClient.asLinkedHashMap(false);
				List<BucketObject> objList = new ArrayList<BucketObject>();
				ListData data = addGrid.getData();
				for(int i = 0; i < data.size(); i++) {
					String s = data.get(i).asMapData().getString("paymentHierarchy");
					for(String key : Map.keySet()) {
						if(s.equals(Map.get(key))) {
					
							BucketObject bucketObject = BucketObject.valueOf(key);
						
							objList.add(bucketObject);
						}
					}
				}
				RPC.ajax("rpc/paymentHierarchyServer/addPaymentHierarchy", new RpcCallback<Data>() {

					@Override
					public void onSuccess(Data result) {
						notice(false);
						Dialog.tipNotice("添加成功！");
						Token token = Flat.get().getCurrentToken();
						token.directPage(PaymentHierarchyPage.class);
						Flat.get().goTo(token);
					}
				}, addForm.getSubmitData().asMapData().toMap(), objList);
			}
		});
		
		//取消按钮
		KylinButton cBtn = ClientUtils.creCancelButton(new IClickEventListener() {

			@Override
			public void onClick() {
			    Token token = Flat.get().getCurrentToken();
				token.directPage(PaymentHierarchyPage.class);
				Flat.get().goTo(token);
			}
		});
		addButton(submitBtn);
		addButton(cBtn);
		panel.add(addForm);
		panel.add(addGrid);
		return panel;
		
	}
	
	public Data StringToData(String gridDataString){
		Data data = new Data();
		data.setJsData(DataUtil.convertDataType(gridDataString));
		return data;
	}
	
	@Override
	public void refresh(){
	    addForm.getUi().clear();
	}
}
