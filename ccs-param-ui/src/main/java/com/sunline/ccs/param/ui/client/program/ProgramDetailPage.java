package com.sunline.ccs.param.ui.client.program;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UProgram;
import com.sunline.kylin.web.ark.client.helper.DateColumnHelper;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.utils.StringUtils;
import com.sunline.kylin.web.core.client.Flat;
import com.sunline.kylin.web.core.client.mvp.Token;
import com.sunline.kylin.web.core.client.res.SavePage;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.ListData;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.core.client.function.IFunction;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.tab.client.Tab;
import com.sunline.ui.tab.client.TabItemSetting;
import com.sunline.ui.tab.client.TabSetting;

/**
 * 国家代码添加页面
 * 
 * @author lisy
 * @version [版本号, Jun 25, 2015]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
@Singleton
public class ProgramDetailPage extends SavePage
{
    
    private String programId;
    @Inject
    private ProgramFeeLayout programFeeLayout;
    @Inject
    private ProgramMerLayout programMerLayout;
    @Inject
    private CtrlMccListLayout ctrlMccListLayout;
    @Inject
    private CtrlProcreditListLayout ctrlProcreditListLayout;
    @Inject
    private CtrlBranchListLayout ctrlBranchListLayout;

    @Inject
    private UProgram uProgram;

    private KylinForm editForm;

    private TextColumnHelper loanPlanColumn;

    @Inject
    private ProgramConstants constants;
    private DateColumnHelper startDate;
    private DateColumnHelper endDate;

    @Override
    public IsWidget createPage()
    {
	VerticalPanel panel = new VerticalPanel();
	editForm = new KylinForm();
	editForm.getSetting().labelWidth(130);
	// 根据分期计划号更新分期类型域
	loanPlanColumn = uProgram.LoanPlanId().asSelectItem().bindEvent("change", new IFunction()
	{

	    @Override
	    public void function()
	    {
		String loanPlanId = editForm.getFieldValue(uProgram.LoanPlanId().getName());
		RPC.ajax("rpc/ccsSelectOptionServer/getLoanPlan", new RpcCallback<Data>()
		{

		    @Override
		    public void onSuccess(Data result)
		    {
			String loanType = result.asMapData().getString(uProgram.LoanType().getName());
			editForm.setFieldValue(uProgram.LoanType().getName(), loanType);
		    }
		}, loanPlanId);
	    }
	});
	startDate = new DateColumnHelper("programStartDate", constants.startDate(), true, false);// .required(true);
	endDate = new DateColumnHelper("programEndDate", constants.endDate(), true, false);
	editForm.setWidth("100%");
	editForm.setField(uProgram.LoanPlanId().required(true).readonly(true), 
	                  uProgram.ProgramId().required(true).readonly(true), 
	                  uProgram.ProgramDesc().required(true), 
	                  startDate.format("yyyy-MM-dd").required(true), 
	                  endDate.format("yyyy-MM-dd").required(true), 
	                  uProgram.ProgramStatus().required(true).asSelectItem(SelectType.KEY_LABLE),
			  uProgram.ProgramMinAmount().required(true), 
			  uProgram.ProgramMaxAmount().required(true),
			  uProgram.ProgramSuppInd().required(true).asSelectItem(SelectType.KEY_LABLE), 
			  uProgram.ProgramBranch().required(true).asSelectItem(), 
			  uProgram.LoanType().required(true).asSelectItem(SelectType.KEY_LABLE),
			  uProgram.CtrlBranchInd().required(true).asSelectItem(SelectType.KEY_LABLE), 
			  uProgram.CtrlProdCreditInd().required(true).asSelectItem(SelectType.KEY_LABLE), 
			  uProgram.CtrlMccInd().required(true).asSelectItem(SelectType.KEY_LABLE)
			  );
	editForm.setCol(3);
	editForm.getSetting().labelWidth(125);
	KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener()
	{
	    @Override
	    public void onClick()
	    {
		if (!editForm.valid())
		{
		    return;
		}
		MapData data = editForm.getSubmitData().asMapData();
		MapData.extend(data, ctrlBranchListLayout.sumbitData().asMapData(), true);
		MapData.extend(data, ctrlProcreditListLayout.sumbitData().asMapData(), true);
		MapData.extend(data, ctrlMccListLayout.sumbitData().asMapData(), true);
		MapData.extend(data, programMerLayout.sumbitData().asMapData(), true);
		MapData.extend(data, programFeeLayout.sumbitData(), true);
		RPC.ajax("rpc/programServer/updateProgram", new RpcCallback<Data>()
		{
		    @Override
		    public void onSuccess(Data result)
		    {
			notice(false);
			Dialog.tipNotice("修改成功！");
			Token token = Flat.get().getCurrentToken();
			token.directPage(ProgramPage.class);
			Flat.get().goTo(token);
		    }
		}, data);
	    }
	});
	KylinButton cBtn = ClientUtils.creCancelButton(new IClickEventListener()
	{
	    @Override
	    public void onClick()
	    {
		Token token = Flat.get().getCurrentToken();
		token.directPage(ProgramPage.class);
		Flat.get().goTo(token);
	    }
	});

	TabSetting tabSetting = new TabSetting().dblClickToClose(true).dragToMove(true).showSwitch(true);
	Tab tab = new Tab(tabSetting);

	// 活动参数panel
	VerticalPanel programParamPanel = new VerticalPanel();
	programParamPanel.setWidth("100%");
	programParamPanel.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
	programParamPanel.add(programFeeLayout.createCanvas());
	TabItemSetting programParamSetting = new TabItemSetting(null, constants.programParam());
	tab.addItem(programParamSetting, programParamPanel);

	// 分行列表panel
	VerticalPanel ctrlBranchListPanel = new VerticalPanel();
	ctrlBranchListPanel.setWidth("50%");
	ctrlBranchListPanel.add(ctrlBranchListLayout.createPage());
	TabItemSetting ctrlBranchListSetting = new TabItemSetting(null, constants.ctrlBranchList());
	tab.addItem(ctrlBranchListSetting, ctrlBranchListPanel);

	// 卡产品列表panel
	VerticalPanel ctrlCtrlProcreditListPanel = new VerticalPanel();
	ctrlCtrlProcreditListPanel.setWidth("50%");
	ctrlCtrlProcreditListPanel.add(ctrlProcreditListLayout.createPage());
	TabItemSetting ctrlCtrlProcreditListSetting = new TabItemSetting(null, constants.ctrlProdcreditList());
	tab.addItem(ctrlCtrlProcreditListSetting, ctrlCtrlProcreditListPanel);

	// 活动参与商户panel
	VerticalPanel programMerPanel = new VerticalPanel();
	programMerPanel.setWidth("50%");
	programMerPanel.add(programMerLayout.createPage());
	TabItemSetting programMerSetting = new TabItemSetting(null, constants.programMer());
	tab.addItem(programMerSetting, programMerPanel);

	// 活动参与商户panel
	VerticalPanel ctrlMccListPanel = new VerticalPanel();
	ctrlMccListPanel.setWidth("50%");
	ctrlMccListPanel.add(ctrlMccListLayout.createPage());
	TabItemSetting ctrlMccListSetting = new TabItemSetting(null, constants.ctrlMccList());
	tab.addItem(ctrlMccListSetting, ctrlMccListPanel);

	panel.add(editForm);
	panel.add(tab);
	addButton(submitBtn);
	addButton(cBtn);

	return panel;
    }

    public void refresh()
    {
	editForm.getUi().clear();
	programMerLayout.updataView(new ListData());
	ctrlMccListLayout.updataView(new ListData());
	ctrlBranchListLayout.updataView(new ListData());
	ctrlProcreditListLayout.updataView(new ListData());
	notice(true);
	Token token = Flat.get().getCurrentToken();
	programId = token.getParam(uProgram.ProgramId().getName());
	RPC.ajax("rpc/programServer/getProgram", new RpcCallback<Data>()
	{
	    @Override
	    public void onSuccess(Data arg0)
	    {
		String startDateTime = arg0.asMapData().getString(startDate.getName());
		String endDateTime = arg0.asMapData().getString(endDate.getName());
		DateTimeFormat format = DateTimeFormat.getFormat("yyyy-MM-dd");
		MapData mapdata = arg0.asMapData();
		
		if (StringUtils.isNotEmpty(startDateTime))
		{
		    mapdata.put(startDate.getName(), format.format(new Date(Long.parseLong(startDateTime))));
		}
		
		if (StringUtils.isNotEmpty(endDateTime))
		{
		    mapdata.put(endDate.getName(), format.format(new Date(Long.parseLong(endDateTime))));
		}
		
		editForm.setFormData(mapdata);

		MapData programFeeLayoutData;
		Data feeData = arg0.asMapData().getData("programFeeDef");
		if (feeData == null)
		{
		    programFeeLayoutData = new MapData();
		} 
		else
		{
		    programFeeLayoutData = feeData.asMapData();
		}
		programFeeLayout.setForm(editForm);
		programFeeLayout.setPageData(programFeeLayoutData);
		programFeeLayout.updateView();
		
		Data branchListData = arg0.asMapData().getData("ctrlBranchList");
		if (branchListData == null)
		{
		    ctrlBranchListLayout.updataView(new ListData());
		}else{
		    ctrlBranchListLayout.updataView(branchListData.asListData());
		}
		
		
		Data ctrlProdCreditListData = arg0.asMapData().getData("ctrlProdCreditList");
		if (ctrlProdCreditListData == null)
		{
		    ctrlProcreditListLayout.updataView(new ListData());
		}else{
		ctrlProcreditListLayout.updataView(ctrlProdCreditListData.asListData());
		}
		
		Data merListData = arg0.asMapData().getData("programMerList");
		if (merListData == null)
		{
		    programMerLayout.updataView(new ListData());
		}else{
		    programMerLayout.updataView(merListData.asListData());

		}
		
		
		Data ctrlMccListData = arg0.asMapData().getData("ctrlMccList");
		if (ctrlMccListData == null)
		{
		    ctrlMccListLayout.updataView(new ListData());
		}else{
		ctrlMccListLayout.updataView(ctrlMccListData.asListData());
	    }
	    }
	}, programId);
	// 获取所属分行更新下拉框
	RPC.ajax("rpc/ccsSelectOptionServer/getBranchList", new RpcCallback<Data>()
	{

	    @Override
	    public void onSuccess(Data result)
	    {
		SelectItem<String> si = new SelectItem<String>(SelectType.LABLE);
		si.setValue(result.asListData());
		editForm.setFieldSelectData(uProgram.ProgramBranch().getName(), si);
	    }
	});

    }

}
