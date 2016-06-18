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
import com.sunline.ui.listbox.client.ListBox;
import com.sunline.ui.listbox.client.ListBoxSetting;


@Singleton
public class PaymentHierarchyDetailPage extends SavePage {

	private KylinGrid editGrid = new KylinGrid();

	private KylinForm editForm = new KylinForm();

	private String pmtHierId;

	private ListBox b = new ListBox(new ListBoxSetting().data(null));

	@Inject
	private BucketObjectDomainClient bucketObjectDomainClient;

	@Inject
	private UPaymentHierarchy uPaymentHierarchy;

	@Override
	public IsWidget createPage() {
		//设置页面参数
		VerticalPanel panel = new VerticalPanel();
		editForm.setWidth("100%");
		editForm.setField(uPaymentHierarchy.PmtHierId().required(true).readonly(true), 
		                  uPaymentHierarchy.Description().asTextArea().setNewline(true));
		editForm.setCol(1);
		editGrid.setColumns(new TextColumnHelper("paymentHierarchy", "余额成分", 40));
		editGrid.rownumbers(true);
		editGrid.getSetting().usePager(false);
		editGrid.getSetting().checkbox(false);
		editGrid.setWidth(500);
		editGrid.getSetting().rowDraggable(true);
		
		
		KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener() {

			@Override
			public void onClick() {
			    if(!editForm.valid())return;
			    
				LinkedHashMap<String, String> Map = bucketObjectDomainClient.asLinkedHashMap(false);
				List<BucketObject> objList = new ArrayList<BucketObject>();
				ListData data = editGrid.getData();
				for(int i = 0; i < data.size(); i++) {
					String s = data.get(i).asMapData().getString("paymentHierarchy");
					for(String key : Map.keySet()) {
						if(s.equals(Map.get(key))) {
							
							BucketObject bucketObject = BucketObject.valueOf(key);
						
							objList.add(bucketObject);
						}
					}
				}
				RPC.ajax("rpc/paymentHierarchyServer/updatePaymentHierarchy", new RpcCallback<Data>() {

					@Override
					public void onSuccess(Data result) {
						notice(false);
						Dialog.tipNotice("修改成功！");
						Token token = Flat.get().getCurrentToken();
						token.directPage(PaymentHierarchyPage.class);
						Flat.get().goTo(token);
					}
				}, editForm.getSubmitData().asMapData().toMap(), objList);
			}
		});
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
		panel.add(editForm);
		panel.add(editGrid);
		return panel;
	}

	@Override
	public void refresh() {
		notice(true);
		editForm.getUi().clear();
		editGrid.loadData(new Data());
		Token token = Flat.get().getCurrentToken();
		pmtHierId = token.getParam(uPaymentHierarchy.PmtHierId().getName());
		RPC.ajax("rpc/paymentHierarchyServer/getPaymentHierarchy", new RpcCallback<Data>() {

			@Override
			public void onSuccess(Data arg0) {
				editForm.setFormData(arg0);
				editGrid.loadData(StringToData(buildGridData(arg0.asMapData().toMap().get("paymentHier").toString())));
				editGrid.rownumbers(true);
			}
		}, pmtHierId);
	}

	private String buildGridData(String tmd) {
		LinkedHashMap<String, String> Map = bucketObjectDomainClient.asLinkedHashMap(false);
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		tmd = tmd.substring(1, tmd.length() - 1);

		String tmd2[] = tmd.split(",");
		for(String payH : tmd2) {

			payH = payH.trim();
			sb.append("{\"").append("paymentHierarchy").append("\":").append("\"").append(Map.get(payH)).append("\"}")
					.append(",");
			//兼容新增bucketType数据
			Map.remove(payH);
		}
		//存在新增的bucketType
		if(!Map.isEmpty()){
			for(String key:Map.keySet()){
				sb.append("{\"").append("paymentHierarchy").append("\":").append("\"").append(Map.get(key)).append("\"}")
				.append(",");
			}
		}
		sb.deleteCharAt(sb.lastIndexOf(","));
		sb.append("]");
		String testMap = sb.toString();
		return testMap;
	}

	
	public Data StringToData(String gridDataString){
		Data data = new Data();
		data.setJsData(DataUtil.convertDataType(gridDataString));
		return data;
	}
}
