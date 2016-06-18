package com.sunline.ccs.param.ui.client.pointplan;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UPointPlan;
import com.sunline.kylin.web.ark.client.helper.DateColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.core.client.Flat;
import com.sunline.kylin.web.core.client.mvp.Token;
import com.sunline.kylin.web.core.client.res.SavePage;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.tab.client.Tab;
import com.sunline.ui.tab.client.TabItemSetting;
import com.sunline.ui.tab.client.TabSetting;

@Singleton
public class PointPlanAddPage extends SavePage {
	
	private KylinForm detailForm;
	
	@Inject
    private UPointPlan uPointPlan;

	@Inject
	private PointPlanConstants constants;
	
	@Inject
	private TxnFilterRuleLayout txnFilterRuleLayout;
	
	@Inject
	private PointAccumRuleLayout pointAccumRuleLayout;
	private DateColumnHelper startDate;
	private DateColumnHelper endDate;

	@Override
	public void refresh() {
	    detailForm.getUi().clear();
	    txnFilterRuleLayout.clearValues();
	    pointAccumRuleLayout.clearValues();
	}

	@Override
	public IsWidget createPage() {

		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");

		// 计划编号面板
		VerticalPanel planPanel = new VerticalPanel();
		planPanel.setWidth("100%");
		planPanel.setHeight("30%");

		// 嵌套tab面板
		VerticalPanel tabPanel = new VerticalPanel();
		tabPanel.setWidth("100%");
		tabPanel.setHeight("70%");

		// 设置计划编号面板

		detailForm = new KylinForm();
		detailForm.setWidth("100%");
		startDate=new DateColumnHelper("startDate","计划开始日期", true, false).required(true);
		endDate=new DateColumnHelper("endDate","计划结束日期", true, false).required(true);

		detailForm.setField(uPointPlan.PlanNbr().required(true), uPointPlan
				.PlanType().required(true),
				startDate, 
				endDate, 
				uPointPlan.Description()
				.asTextArea().setFieldWidth(488));
		detailForm.setCol(2);

		planPanel.add(detailForm);

		TabSetting tabSetting = new TabSetting().dblClickToClose(true)
				.dragToMove(true).showSwitch(true);
		Tab tab = new Tab(tabSetting);

		tab.setHeight("100%");
		tab.setWidth("100%");

		// panel.add(tab);

		// 交易过滤规则layout

		TabItemSetting filterRuleTabSetting = new TabItemSetting(null,
				constants.pointTxnFilterRule());
		tab.addItem(filterRuleTabSetting, txnFilterRuleLayout.createCanvas());

		// 积分规则layout

		TabItemSetting accumRuleTabSetting = new TabItemSetting(null,
				constants.pointAccumRule());
		tab.addItem(accumRuleTabSetting, pointAccumRuleLayout.createCanvas());

		tabPanel.add(tab);

		panel.add(planPanel);
		panel.add(tabPanel);
		
		KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener(){
			@Override
			public void onClick() {
			    if(!(detailForm.valid()
			        &&txnFilterRuleLayout.valid()
			        &&pointAccumRuleLayout.valid())){
			        return; 
			    }
				MapData data = new MapData();
				data.put("filterRule", txnFilterRuleLayout.getValue());
				data.put("accumRule", pointAccumRuleLayout.getValue());
				MapData.extend(data, detailForm.getSubmitData().asMapData(), true);
				
				DateTimeFormat format = DateTimeFormat.getFormat("yyyy-MM-dd");
				data.put("startDate", format.format(new Date(Long.parseLong(data.getString("startDate")))));
				data.put("endDate", format.format(new Date(Long.parseLong(data.getString("endDate")))));

				//保存数据
				RPC.ajax("rpc/pointPlanServer/addPointPlan", new RpcCallback<Data>(){
					@Override
					public void onSuccess(Data result) {
						notice(false);
						Dialog.tipNotice("添加成功！");
						Token token = Flat.get().getCurrentToken();
						token.directPage(PointPlanPage.class);
						Flat.get().goTo(token);
					}
				}  ,data);
			}
		});
		KylinButton cBtn = ClientUtils.creCancelButton(new IClickEventListener(){
			@Override
			public void onClick() {
				Token token = Flat.get().getCurrentToken();
				token.directPage(PointPlanPage.class);
				Flat.get().goTo(token);
			}
		});
		addButton(submitBtn);
		addButton(cBtn); 

		return panel;
	}
}
