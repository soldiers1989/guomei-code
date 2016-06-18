package com.sunline.ccs.param.ui.client.program;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UProgram;
import com.sunline.kylin.web.ark.client.helper.DateColumnHelper;
import com.sunline.kylin.web.ark.client.helper.EnumColumnHelper;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.core.client.Flat;
import com.sunline.kylin.web.core.client.mvp.Token;
import com.sunline.kylin.web.core.client.res.SavePage;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ppy.dictionary.enums.LoanType;
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
 * 分期活动添加页面
 * 
 * @author lisy
 * @version [版本号, Jun 26, 2015]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
@Singleton
public class ProgramAddPage extends SavePage {

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

    private KylinForm addForm;

    private TextColumnHelper loanPlanColumn;
    private EnumColumnHelper<LoanType> loanTypeItem;

    @Inject
    private ProgramConstants constants;
    private DateColumnHelper startDate;
    private DateColumnHelper endDate;

    
    @Override
    public IsWidget createPage() {
	VerticalPanel panel = new VerticalPanel();
	panel.setWidth("100%");
	panel.setHeight("100%");
	
	addForm = new KylinForm();
	loanTypeItem=uProgram.LoanType().asSelectItem(SelectType.KEY_LABLE).readonly(true);
	// 根据分期计划号更新分期类型域
	loanPlanColumn = uProgram.LoanPlanId().asSelectItem().bindEvent("change", new IFunction(){

	    @Override
	    public void function() {
		String loanPlanId = addForm.getFieldValue(uProgram.LoanPlanId().getName());
		if(loanPlanId==null){
		    return;
		}else{
		RPC.ajax("rpc/ccsSelectOptionServer/getLoanPlanForType", new RpcCallback<Data>(){

		    @Override
		    public void onSuccess(Data result) {
			addForm.setFieldValue(loanTypeItem.getName(),result.asMapData().getString(loanTypeItem.getName()));
		    }
		}, loanPlanId);
		
		}
	    }
	});
	startDate = new DateColumnHelper("programStartDate", constants.startDate(), true, false);//.required(true);
	endDate = new DateColumnHelper("programEndDate", constants.endDate(), true, false);//.required(true);
	addForm.setWidth("100%");
	addForm.setField(loanPlanColumn.required(true), uProgram.ProgramId().required(true), uProgram.ProgramDesc()
				 .required(true),
			 startDate.required(true), endDate.required(true), uProgram.ProgramStatus().required(true).asSelectItem(SelectType.LABLE),
			 uProgram.ProgramMinAmount().required(true), uProgram.ProgramMaxAmount().required(true),
			 uProgram.ProgramSuppInd().required(true).asSelectItem(SelectType.LABLE), uProgram
				 .ProgramBranch().required(true).asSelectItem(),loanTypeItem, uProgram.CtrlBranchInd()
				 .required(true).asSelectItem(SelectType.LABLE),
			 uProgram.CtrlProdCreditInd().required(true).asSelectItem(SelectType.LABLE), uProgram
				 .CtrlMccInd().required(true).asSelectItem(SelectType.LABLE));
	addForm.setCol(3);
	addForm.getSetting().labelWidth(125);
	KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener(){

	    @Override
	    public void onClick() {
		if(!addForm.valid()){
		    return;
		}
		// 保存数据
		MapData data=new MapData();
		data= addForm.getSubmitData().asMapData();
		MapData.extend(data,ctrlBranchListLayout.sumbitData().asMapData(), true);
		MapData.extend(data,ctrlProcreditListLayout.sumbitData().asMapData(), true);
		MapData.extend(data,ctrlMccListLayout.sumbitData().asMapData(), true);
		MapData.extend(data,programMerLayout.sumbitData().asMapData(), true);
		MapData.extend(data, programFeeLayout.sumbitData(), true);
		RPC.ajax("rpc/programServer/addProgram", new RpcCallback<Data>(){

		    @Override
		    public void onSuccess(Data result) {
			Dialog.tipNotice("增加成功！");
			notice(false);
			Token token = Flat.get().getCurrentToken();
			token.directPage(ProgramPage.class);
			Flat.get().goTo(token);
		    }
		},data);
	    }
	});
	KylinButton cBtn = ClientUtils.creCancelButton(new IClickEventListener(){

	    @Override
	    public void onClick() {
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

	panel.add(addForm);
	panel.add(tab);
	addButton(submitBtn);
	addButton(cBtn);

	return panel;
    }
    @Override
    public void refresh() {
	addForm.getUi().clear();
	programFeeLayout.setPageData(new MapData());
	programFeeLayout.setForm(addForm);
	programFeeLayout.updateView();
	programMerLayout.updataView(new ListData());
	ctrlMccListLayout.updataView(new ListData());
	ctrlProcreditListLayout.updataView(new ListData());
	ctrlBranchListLayout.updataView(new ListData());
	// 获取所属分行更新下拉框
	RPC.ajax("rpc/ccsSelectOptionServer/getBranchList", new RpcCallback<Data>(){

	    @Override
	    public void onSuccess(Data result) {
		SelectItem<String> si = new SelectItem<String>();
		si.setValue(result.asListData());
		addForm.setFieldSelectData(uProgram.ProgramBranch().getName(), si);
	    }
	});
	// 获取分期计划号更新下拉框
	RPC.ajax("rpc/ccsSelectOptionServer/getLoanPlanForSelect", new RpcCallback<Data>(){

	    @Override
	    public void onSuccess(Data result) {
	    Map <String,String> map = (Map<String, String>) result.asMapData().toMap();
	    LinkedHashMap<String,String> selectItem = new LinkedHashMap<String,String>();
	    selectItem.putAll(map);
		SelectItem<String> si = new SelectItem<String>();
		si.setValue(selectItem);
		addForm.setFieldSelectData(uProgram.LoanPlanId().getName(), si);	    	
	    }
	});
    }
}
