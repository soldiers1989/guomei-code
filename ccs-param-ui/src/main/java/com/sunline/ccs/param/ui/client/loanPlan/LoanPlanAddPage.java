package com.sunline.ccs.param.ui.client.loanPlan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.ULoanPlan;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.core.client.Flat;
import com.sunline.kylin.web.core.client.mvp.Token;
import com.sunline.kylin.web.core.client.res.SavePage;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ui.core.client.common.Field;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.tab.client.Tab;
import com.sunline.ui.tab.client.TabItemSetting;
import com.sunline.ui.tab.client.TabSetting;
import com.thoughtworks.xstream.core.util.Fields;

@Singleton
public class LoanPlanAddPage extends SavePage
{
    private static final String FORMAL_YYYYMMDD = "yyyy-MM-dd";

    private KylinForm detailForm;

    @Inject
    private ULoanPlan uLoanPlan;

    @Inject
    private LoanPlanConstants loanPlanConstants;

    @Inject
    private TxnCdParamLayout txnCdParamLayout;

    @Inject
    private LoanFeeLayout loanFeeLayout;

    private Map<String, MapData> loanPlanFeeInfoMap;

    @Override
    public IsWidget createPage()
    {
	VerticalPanel panel = new VerticalPanel();
	panel.setWidth("100%");

	detailForm = new KylinForm();
	{
	    detailForm.getSetting().labelWidth(140);
	    detailForm.setField(uLoanPlan.LoanCode().required(true),
				uLoanPlan.LoanType().setNewline(true).required(true),
				uLoanPlan.Description().required(true), 
				uLoanPlan.TerminateAgeCd().required(true),
				uLoanPlan.LoanValidity().required(true).format(FORMAL_YYYYMMDD).showDate(true).showTime(false), 
				uLoanPlan.LoanStaus().required(true), 
				uLoanPlan.ReturnInd().required(true),
				uLoanPlan.ReturnPointInd().required(true), 
				uLoanPlan.ReturnFeeInd().required(true),
				uLoanPlan.PrepaymentInd().required(true), 
				uLoanPlan.PrepaymentFeeInd().required(true),
				uLoanPlan.UseTemplimit().asCheckBoxItem());
	    detailForm.setCol(3);
	}

	panel.add(detailForm);

	// 添加tab框
	TabSetting tabSetting = new TabSetting().dblClickToClose(true).dragToMove(true).showSwitch(true);
	Tab tab = new Tab(tabSetting);
	panel.add(tab);

	TabItemSetting loanFeeTabSetting = new TabItemSetting(null, loanPlanConstants.loanParam());
	TabItemSetting txnCdParamSetting = new TabItemSetting(null, loanPlanConstants.txnCdParam());

	// 分期参数
	tab.addItem(loanFeeTabSetting, loanFeeLayout.createPage());
	// 交易代码参数
	tab.addItem(txnCdParamSetting, txnCdParamLayout.createPage());

	KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener()
	{
	    @Override
	    public void onClick()
	    {
		MapData loanPlanData = new MapData();
		loanPlanData = detailForm.getSubmitData().asMapData();
		MapData loanFeeDefGridData = new MapData();
		for (Entry<String, MapData> entry : loanPlanFeeInfoMap.entrySet())
		{
		    loanFeeDefGridData.put(entry.getKey(), entry.getValue());
		}
		loanPlanData.put("loanFeeDefMap", loanFeeDefGridData);
		loanPlanData.put("txnCdList", txnCdParamLayout.getSubmitData());
		if (detailForm.valid())
		{
		    RPC.ajax("rpc/loanPlanServer/addLoanPlan", new RpcCallback<Data>()
		    {
			@Override
			public void onSuccess(Data result)
			{
			    Token token = Flat.get().getCurrentToken();
			    notice(false);
			    Dialog.tipNotice("添加成功");
			    cleanValues();
			    token.directPage(LoanPlanPage.class);
			    Flat.get().goTo(token);
			}
		    }, loanPlanData);
		}
	    }
	});

	KylinButton cBtn = ClientUtils.creCancelButton(new IClickEventListener()
	{
	    @Override
	    public void onClick()
	    {
		Token token = Flat.get().getCurrentToken();
		token.directPage(LoanPlanPage.class);
		Flat.get().goTo(token);
	    }
	});

	addButton(submitBtn);
	addButton(cBtn);
	return panel;
    }

    public void cleanValues()
    {
	// 清空表格数据
	detailForm.getUi().clear();
	loanFeeLayout.clearValues();
	txnCdParamLayout.clearValues();
    }

    @Override
    public void refresh()
    {
	cleanValues();
	loanPlanFeeInfoMap = new HashMap<String, MapData>();
	loanFeeLayout.setLoanFeeInfoMap(loanPlanFeeInfoMap);
    }
}
