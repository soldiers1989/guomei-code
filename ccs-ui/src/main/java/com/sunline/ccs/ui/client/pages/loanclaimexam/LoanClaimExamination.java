package com.sunline.ccs.ui.client.pages.loanclaimexam;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsLoanReg;
import com.sunline.ccs.ui.client.commons.BtnName;
import com.sunline.ccs.ui.client.commons.CommonGlobalConstants;
import com.sunline.ccs.ui.client.commons.CommonUiUtils;
import com.sunline.kylin.web.ark.client.helper.DateColumnHelper;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.dialog.client.listener.ConfirmCallbackFunction;
import com.sunline.ui.event.client.EventObjectHandler;
//import com.smartgwt.client.data.fields.DataSourceSequenceField;
//import com.smartgwt.client.types.Alignment;
//import com.smartgwt.client.types.Overflow;
//import com.smartgwt.client.types.RecordComponentPoolingMode;
//import com.smartgwt.client.types.SelectionStyle;
//import com.smartgwt.client.types.VisibilityMode;
//import com.smartgwt.client.util.BooleanCallback;
//import com.smartgwt.client.util.SC;
//import com.smartgwt.client.widgets.IButton;
//import com.smartgwt.client.widgets.form.fields.ButtonItem;
//import com.smartgwt.client.widgets.form.fields.DateItem;
//import com.smartgwt.client.widgets.form.fields.FormItem;
//import com.smartgwt.client.widgets.form.fields.SelectItem;
//import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
//import com.smartgwt.client.widgets.form.fields.events.ClickHandler;
//import com.smartgwt.client.widgets.grid.ListGrid;
//import com.smartgwt.client.widgets.grid.ListGridField;
//import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
//import com.smartgwt.client.widgets.grid.events.SelectionEvent;
//import com.smartgwt.client.widgets.layout.HLayout;
//import com.smartgwt.client.widgets.layout.SectionStack;
//import com.smartgwt.client.widgets.layout.SectionStackSection;
//import com.sunline.pcm.ui.common.client.ClientUtils;
//import com.sunline.pcm.ui.common.client.DispatcherPage;
//import com.sunline.pcm.ui.common.client.validator.ClientDateUtil;
//import com.sunline.pcm.ui.common.client.validator.DatesComparedValidator;
//import com.sunline.ark.gwt.client.datasource.YakDataSource;
//import com.sunline.ark.gwt.client.datasource.YakDataSourceRecord;
//import com.sunline.ark.gwt.client.mvp.ParamMapToken;
//import com.sunline.ark.gwt.client.ui.YakDynamicForm;
//import com.sunline.ark.gwt.client.util.RPCExecutor;
//import com.sunline.ark.gwt.client.util.RPCTemplate;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.IDblClickRowEventListener;

/**
 * 贷款申请审核
* @author dch
 *
 */
@Singleton
public class LoanClaimExamination extends Page{
	
	@Inject
	private UCcsLoanReg uTmLoanReg;
	private KylinForm searchForm;
	@Inject
	private LoanClaimExaminationConstants constants;
	@Inject
	private CommonGlobalConstants publicConstants;
	private KylinForm detailForm;
	private KylinGrid searchGrid = new KylinGrid();
	// 同意
	private KylinButton agreeButton;
	// 拒绝
	private KylinButton refuseButton;
	
	@Override
	public IsWidget createPage() {
		
		// 返回组件
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");
		panel.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
		
		// 搜索
		panel.add(createTimeSearchForm());
		
		// 列表
		StackPanel gridSpanel = new StackPanel();
		gridSpanel.setWidth("98%");
		VerticalPanel gridVPanel = new VerticalPanel();
		gridVPanel.setWidth("98%");
		gridVPanel.add(createListGrid());
		gridSpanel.add(gridVPanel, constants.cashStageList());
		panel.add(gridSpanel);
		
		// 明细
		StackPanel detailSpanel = new StackPanel();
		detailSpanel.setWidth("98%");
		ScrollPanel sPanel = new ScrollPanel();
		sPanel.add(showDetailMsg());
		sPanel.setHeight("250px");
		detailSpanel.add(sPanel, constants.showDetailStack());
		panel.add(detailSpanel);
		// 按钮
		panel.add(agreeOrRefuseBtn());
		
		return panel;
	}
	
	// 搜索form
	private KylinForm createTimeSearchForm() {
		searchForm = new KylinForm();
		{
			searchForm.setWidth("98%");
			searchForm.setCol(4);
			// 起始日期
	        DateColumnHelper beginDateItem = new DateColumnHelper("beginDate", publicConstants.labelBeginDate(), true, false);
			// 截止日期
			DateColumnHelper endDateItem = new DateColumnHelper("endDate", publicConstants.labelEndDate(), true, false);
			searchForm.setField(uTmLoanReg.LoanType().asSelectItem(SelectType.KEY_LABLE)
					, uTmLoanReg.LoanAction().asSelectItem(SelectType.KEY_LABLE), beginDateItem, endDateItem);
			searchForm.getSetting().labelWidth(120);
			
			searchForm.addButton(ClientUtils.createSearchButton(new IClickEventListener(){
				@Override
				public void onClick() {
					agreeButton.setVisible(false);
					refuseButton.setVisible(false);
					Data submitData = searchForm.getSubmitData();
					if (submitData.asMapData().getString("beginDate") != null) {
						searchGrid.getUi().setParm("bDate", submitData.asMapData().getString("beginDate"));
					}
					if (submitData.asMapData().getString("endDate") != null) {
						searchGrid.getUi().setParm("eDate", submitData.asMapData().getString("endDate"));
					}
					searchGrid.loadData();
					searchGrid.loadData(searchForm);
				}
			}));
	    searchForm.setHeight("20%");
		}
		return searchForm;
	}
	
	@Override
	public void refresh() {
		searchForm.getUi().clear();
		searchGrid.clearData();
		detailForm.getUi().clear();
		agreeButton.setVisible(false);
		refuseButton.setVisible(false);
	}
	
	private KylinGrid createListGrid() {
		searchGrid = new KylinGrid();
		searchGrid.checkbox(false);
		searchGrid.setWidth("100%");
		searchGrid.setHeight("280px");
		searchGrid.loadDataFromUrl("rpc/t3306Server/getNeedExamineList");
		searchGrid.getSetting().delayLoad(true);
		searchGrid.setColumns(
				uTmLoanReg.AcctNbr().setColumnWidth("10%"),
				uTmLoanReg.AcctType().columnRender().setColumnWidth("15%"),
				uTmLoanReg.CardNbr().setColumnWidth("10%"),
				uTmLoanReg.LoanType().columnRender().setColumnWidth("10%"),
				uTmLoanReg.LoanAction().columnRender().setColumnWidth("10%"),
				uTmLoanReg.LoanInitTerm().setColumnWidth("10%"),
				uTmLoanReg.LoanInitFee().setColumnWidth("10%"),
				uTmLoanReg.LoanInitPrin().setColumnWidth("5%"),
				uTmLoanReg.LoanFeeMethod().columnRender().setColumnWidth("10%"),
				uTmLoanReg.RegisterDate()
		);
		
		searchGrid.getSetting().onDblClickRow(new IDblClickRowEventListener() {
			
			@Override
			public void onDblClickRow(MapData data, String rowid, EventObjectHandler row) {
				detailForm.getUi().clear();
				detailForm.setFormData(data);
				agreeButton.setVisible(true);
				refuseButton.setVisible(true);
			}
		});
		
		return searchGrid;
	}


	
	/**
	 * 创建显示现金分期申请的详细信息
	 * @return
	 */
	private KylinForm showDetailMsg() {
		
		detailForm = new KylinForm();
		detailForm.setWidth("98%");
		detailForm.setHeight("30%");
		detailForm.setTitle(constants.showDetailStack());
		
		detailForm.setField(
				uTmLoanReg.RegisterId().readonly(true),
				uTmLoanReg.Matched().asSelectItem(SelectType.KEY_LABLE).readonly(true),
				uTmLoanReg.InterestRate().readonly(true),
				uTmLoanReg.PenaltyRate().readonly(true),
				uTmLoanReg.CompoundRate().readonly(true),
				uTmLoanReg.FloatRate().readonly(true),
				uTmLoanReg.AdvPmtAmt().readonly(true),
				uTmLoanReg.DueBillNo().readonly(true),
				uTmLoanReg.ValidDate().readonly(true),
				uTmLoanReg.ExtendTerm().readonly(true),
				uTmLoanReg.ShortedType().readonly(true),
				uTmLoanReg.ShortedTerm().readonly(true),
				uTmLoanReg.ShortedPmtDue().readonly(true),
				uTmLoanReg.Remark().asTextArea()
				);
		detailForm.getSetting().labelWidth(120);
		return detailForm;
	}
	
	//同意拒绝按钮
	private HorizontalPanel agreeOrRefuseBtn(){
		
		HorizontalPanel buttonPanel = new HorizontalPanel();
		{//buttonPanel.setAlign(Alignment.CENTER);
			// 同意
			agreeButton = CommonUiUtils.createButton(BtnName.AGREE, "skins/icons/ok.gif", new IClickEventListener(){
				@Override
				public void onClick() {
				    btnClick("0", detailForm.getSubmitData());
				}
			});
			
			// 拒绝
		    refuseButton = CommonUiUtils.createButton(BtnName.REFUSE, "skins/icons/cancel.gif", new IClickEventListener(){
				@Override
				public void onClick() {
					btnClick("1", detailForm.getSubmitData());
				}
			});
			buttonPanel.add(agreeButton);
			buttonPanel.add(refuseButton);
		}
		
		return buttonPanel;
	}
	
	//点同意或者拒绝按钮的操作
	private void btnClick(final String flag, final Data values){
		//验证表单信息
		String msg = "";
		if("0".equals(flag)){
			msg = constants.agreeBtnMsg();
		}else {
			msg = constants.refuseBtnMsg();
		}
		
		Dialog.confirm(msg, "提示", new ConfirmCallbackFunction(){

			@Override
			public void corfimCallback(boolean value) {
				if (value) {
					RPC.ajax("rpc/t3306Server/setArgeeOrRefuse", new RpcCallback<Data>(){
						@Override
						public void onSuccess(Data result) {
							Dialog.tipNotice("操作成功！");
							searchGrid.loadData(searchForm);
							searchForm.getUi().clear();
							detailForm.getUi().clear();
							agreeButton.setVisible(false);
							refuseButton.setVisible(false);
						}
					} , flag, values);
				}
			}
		});
	
	}
	
}
