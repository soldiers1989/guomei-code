package com.sunline.ccs.ui.client.pages.lendingapply;

import java.util.LinkedHashMap;

import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsLoanReg;
import com.sunline.ccs.ui.client.commons.CommonGlobalConstants;
import com.sunline.ccs.ui.client.commons.CommonKylinForm;
import com.sunline.ccs.ui.client.commons.CommonSelectItemWrapper;
import com.sunline.kylin.web.ark.client.helper.DateColumnHelper;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.event.client.EventObjectHandler;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.IDblClickRowEventListener;


/** 
 * @see 类名：LendingApply
 * @see 描述：分期申请状态查询
 *
 * @see 创建日期：   2015年6月30日下午4:53:00
 * @author yanjingfeng
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Singleton
public class LendingApply  extends Page {

    @Inject
    private LendingApplyConstants constants;
    @Inject
    private CommonGlobalConstants publicConstants;
    @Inject
    private UCcsLoanReg uCcsLoanReg;

    private KylinForm searchForm;
    private KylinForm detailForm;
    private KylinGrid searchGrid = new KylinGrid();

    
    @Override
    public void refresh() {
	searchForm.getUi().clear();
	detailForm.getUi().clear();
	searchGrid.clearData();
    }

    /* (non-Javadoc)
     * @see com.sunline.kylin.web.core.client.res.Page#createPage()
     */
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
	gridSpanel.add(createListGrid(), constants.cashStage());
	panel.add(gridSpanel);

	// 明细
	StackPanel detailSpanel = new StackPanel();
	detailSpanel.setWidth("98%");
	ScrollPanel sPanel = new ScrollPanel();
	sPanel.add(showDetailMsg());
	// sPanel.setHeight("250px");
	detailSpanel.add(sPanel, constants.showDetailStack());
	detailSpanel.setHeight("250px");
	panel.add(detailSpanel);

	return panel;
    }

// 搜索form
private KylinForm createTimeSearchForm() {
	searchForm = new CommonKylinForm();
	{
	    // 起始日期
	    DateColumnHelper beginDateItem =
		    new DateColumnHelper("beginDate", publicConstants.labelBeginDate(), true, true);
	    // 截止日期
	    DateColumnHelper endDateItem = new DateColumnHelper("endDate", publicConstants.labelEndDate(), true, true);
	    TextColumnHelper isOnThatDayItem = new TextColumnHelper("isOnThatDay",constants.isOnThatDay(),80);
	    LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
	    map.put(Indicator.Y.toString(), constants.isYesOnThatDay());
	    map.put(Indicator.N.toString(), constants.isNotOnThatDay());
	    searchForm.setField(uCcsLoanReg.CardNbr(), isOnThatDayItem.asSelectItem(),uCcsLoanReg.LoanType().asSelectItem(SelectType.GRID),beginDateItem, endDateItem);
	    CommonSelectItemWrapper.getInstance().wrapper(searchForm, isOnThatDayItem, map);
	    searchForm.addButton(ClientUtils.createSearchButton(new IClickEventListener(){
		@Override
		public void onClick() {
		    searchGrid.loadData(searchForm);
		}
	    }));
	    searchForm.setHeight("20%");
	}
	return searchForm;
}
private KylinGrid createListGrid() {

	searchGrid = new KylinGrid();
//	searchGrid.checkbox(false);
	searchGrid.setWidth("98%");
	searchGrid.setHeight("30%");
	searchGrid.loadDataFromUrl("rpc/lendingApplyServer/getLoanRegList");
	searchGrid.getSetting().delayLoad(true);
	searchGrid.setColumns(uCcsLoanReg.AcctNbr(), uCcsLoanReg.AcctType(), uCcsLoanReg.CardNbr(),
			      uCcsLoanReg.LoanInitTerm(), uCcsLoanReg.LoanInitFee(), uCcsLoanReg.LoanRegStatus(),
			      uCcsLoanReg.LoanType(),uCcsLoanReg.RegisterDate());

	// 选中listGird记录时为detailForm赋值
	/*searchGrid.getSetting().onSelectRow(new ISelectRowEventListener() {

	    @Override
	    public void selectRow(MapData rowdata, String rowid, EventObjectHandler rowobj) {
		detailForm.getUi().clear();
		detailForm.setFormData(rowdata);
	    }
	    
	});*/
	searchGrid.getSetting().onDblClickRow(new IDblClickRowEventListener(){

	    @Override
	    public void onDblClickRow(MapData data, String rowid, EventObjectHandler row) {
		detailForm.getUi().clear();
		detailForm.setFormData(data);
	    }
	});

	return searchGrid;
}

private KylinForm showDetailMsg() {

	detailForm = new KylinForm();
	detailForm.setWidth("98%");
	detailForm.setHeight("20%");
	detailForm.setTitle(constants.showDetailStack());
	detailForm.setField(uCcsLoanReg.RegisterId().readonly(true), uCcsLoanReg.AcctNbr().readonly(true), uCcsLoanReg
				    .AcctType().readonly(true), uCcsLoanReg.CardNbr().readonly(true),
			    uCcsLoanReg.LogicCardNbr().readonly(true), uCcsLoanReg.RefNbr().readonly(true), uCcsLoanReg
				    .LoanCode().readonly(true), uCcsLoanReg.LoanFeeMethod().readonly(true), uCcsLoanReg
				    .LoanFinalTermPrin().readonly(true), uCcsLoanReg.LoanFinalTermFee().readonly(true),
			    uCcsLoanReg.LoanFixedFee().readonly(true), uCcsLoanReg.LoanFixedPmtPrin().readonly(true),
			    uCcsLoanReg.LoanFinalTermFee().readonly(true),uCcsLoanReg.LoanFirstTermPrin().readonly(true),
			    uCcsLoanReg.LoanInitFee().readonly(true), uCcsLoanReg.LoanRegStatus().readonly(true),
			    uCcsLoanReg.LoanInitPrin().readonly(true),uCcsLoanReg.LoanInitTerm().readonly(true),
			    uCcsLoanReg.LoanType().readonly(true),uCcsLoanReg.RequestTime().readonly(true),
			    uCcsLoanReg.RegisterDate().readonly(true),uCcsLoanReg.Remark().readonly(true),
			    uCcsLoanReg.TotLifeInsuAmt().readonly(true),uCcsLoanReg.TotPrepayPkgAmt().readonly(true),
			    uCcsLoanReg.LifeInsuFeeMethod().columnRender().readonly(true),uCcsLoanReg.LifeInsuFeeRate().readonly(true),
			    uCcsLoanReg.PrepayPkgFeeMethod().columnRender().readonly(true),
			    uCcsLoanReg.PrepayPkgFeeRate().readonly(true),
			    uCcsLoanReg.JoinLifeInsuInd().columnRender().readonly(true),
			    uCcsLoanReg.JoinPrepayPkgInd().columnRender().readonly(true)
			    
			    
			    );
	return detailForm;
}
}
//package com.sunline.ccs.ui.client.txn.lendingapply;
//
//import java.util.Date;
//import java.util.LinkedHashMap;
//
//import com.sunline.ppy.dictionary.enums.Indicator;
//import com.sunline.pcm.ui.common.client.ClientUtils;
//import com.sunline.pcm.ui.common.client.DispatcherPage;
//import com.sunline.pcm.ui.common.client.validator.ClientDateUtil;
//import com.sunline.pcm.ui.common.client.validator.DatesComparedValidator;
//import com.sunline.ccs.ui.client.pub.PublicConstants;
//import com.sunline.ccs.ui.client.pub.UIUtil;
//import com.sunline.ccs.infrastructure.client.ui.UTmLoanReg;
//import com.sunline.ccs.infrastructure.shared.model.TmLoanReg;
//import com.sunline.ccs.param.def.enums.CPSAppAuthority;
//import com.sunline.ark.gwt.client.datasource.YakDataSource;
//import com.sunline.ark.gwt.client.datasource.YakDataSourceRecord;
//import com.sunline.ark.gwt.client.mvp.ParamMapToken;
//import com.sunline.ark.gwt.client.ui.YakDynamicForm;
//import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
//import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
//import com.google.gwt.resources.client.ImageResource;
//import com.google.gwt.user.client.rpc.AsyncCallback;
//import com.google.inject.Inject;
//import com.smartgwt.client.data.fields.DataSourceSequenceField;
//import com.smartgwt.client.types.Alignment;
//import com.smartgwt.client.types.Overflow;
//import com.smartgwt.client.types.RecordComponentPoolingMode;
//import com.smartgwt.client.types.SelectionStyle;
//import com.smartgwt.client.types.VisibilityMode;
//import com.smartgwt.client.widgets.form.fields.ButtonItem;
//import com.smartgwt.client.widgets.form.fields.DateItem;
//import com.smartgwt.client.widgets.form.fields.FormItem;
//import com.smartgwt.client.widgets.form.fields.SelectItem;
//import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
//import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
//import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
//import com.smartgwt.client.widgets.form.fields.events.ClickHandler;
//import com.smartgwt.client.widgets.grid.ListGrid;
//import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
//import com.smartgwt.client.widgets.grid.events.SelectionEvent;
//import com.smartgwt.client.widgets.layout.SectionStack;
//import com.smartgwt.client.widgets.layout.SectionStackSection;
//
///**
// * 现金分期放款页面
//* @author fanghj
// * @date 2015-1-30 17:00:00
// * @version 1.0
// */
//public class LendingApply extends DispatcherPage{
//	
//	public static final String PAGE_ID = "risk-3311";
//	
//	@Inject
//	private LendingApplyConstants constants;
//	@Inject
//	private PublicConstants publicConstants;
//	@Inject
//	private ClientUtils clientUtils;
//	@Inject
//	private UTmLoanReg uTmLoanReg;
//	@Inject
//	private T3311InterAsync server;
//	@Inject
//	private UIUtil uiUtil;
//	
//	private ListGrid listGrid;
//	private YakDynamicForm detailForm;
//	private FormItem remarkItem;
//	
//	private SelectItem loanTypeItem;
//	private SelectItem isOnThatDayItem;
//	private FormItem cardItem;
//	private DateItem beginDateItem;
//	private DateItem endDateItem;
//	private Date beginDate;
//	private Date endDate;
//	private boolean isOnThatDay;
//	
//	private String submitCardNo;
//	private String submitLoanType;
//	
//	public LendingApply(){
//		super(PAGE_ID, true, CPSAppAuthority.T3311);
//	}
//	
//	@Override
//	protected void createCanvas() {
//		
//		beginDate = new Date();
//		
//		final SectionStack sectionStack = new SectionStack();
//		{
//			sectionStack.setVisibilityMode(VisibilityMode.MULTIPLE);
//			sectionStack.setWidth100();
//			sectionStack.setHeight100();
//		}
//
//		//增加分期状态的查询和显示stack
//		SectionStackSection showListGirdStack = new SectionStackSection(constants.cashStage());
//		{
//			showListGirdStack.setExpanded(true);
//			
//			//创建搜索Form
//			showListGirdStack.addItem(createTimeSearchForm());
//			
//			//创建要显示的申请的listGrid
//			showListGirdStack.addItem(createListGrid());
//		}
//		sectionStack.addSection(showListGirdStack);
//		
//		//增加现金分期申请的详细信息stack
//		SectionStackSection showDetailStack = new SectionStackSection(constants.showDetailStack());
//		{
//			showDetailStack.setExpanded(true);
//			
//			//创建申请的详细信息
//			showDetailStack.addItem(showDetailMsg());
//			
//		}
//		sectionStack.addSection(showDetailStack);
//		
//		addMember(sectionStack);
//		
//	}
//	
//	/**
//	 * 创建时间搜索表单
//	 */
//	private YakDynamicForm createTimeSearchForm() {
//		final YakDynamicForm timeSearchForm = new YakDynamicForm();
//		{
//			timeSearchForm.setNumCols(6);
//			timeSearchForm.setWidth100();
//			
//			cardItem = uiUtil.createCardNoItem();
//			
//			//分期时间:用来区分查询TM_LOAN_REG还是TM_LOAN_REG_HST表
//			isOnThatDayItem = new SelectItem("loanReg", constants.isOnThatDay());
//			{
//				LinkedHashMap<String, String> valueMap = new LinkedHashMap<String, String>();
//				valueMap.put(Indicator.Y.name(), constants.isYesOnThatDay());
//				valueMap.put(Indicator.N.name(), constants.isNotOnThatDay());
//				isOnThatDayItem.setValueMap(valueMap);
//				isOnThatDayItem.setDefaultValue(Indicator.Y.name());
//				
//				isOnThatDayItem.addChangedHandler(new ChangedHandler() {
//					
//					@Override
//					public void onChanged(ChangedEvent event) {
//						if (Indicator.Y.name().equals(isOnThatDayItem.getValueAsString())) {
//							beginDateItem.clearValue();
//							endDateItem.clearValue();
//							beginDateItem.setDisabled(true);
//							endDateItem.setDisabled(true);
//						} else {
//							beginDateItem.setDisabled(false);
//							endDateItem.setDisabled(false);
//						}
//					}
//				});
//			}
//			
//			loanTypeItem = uTmLoanReg.LoanType().createSelectItem();
//			
//			//查询起始日期
//			beginDateItem = new DateItem("beginDate", publicConstants.labelBeginDate());
//			{
//				beginDateItem.setStartRow(true);
//				beginDateItem.setUseTextField(true);
//				beginDateItem.setDisabled(true);
//				Date date = new Date();
//				date.setYear(date.getYear() - 3);
//				beginDateItem.setStartDate(date);
//				date.setYear(date.getYear() + 23);
//				beginDateItem.setEndDate(date);
//			}
//			
//			//查询截止日期
//			endDateItem = new DateItem("endDate", publicConstants.labelEndDate());
//			{
//				endDateItem.setUseTextField(true);
//				endDateItem.setDisabled(true);
//				endDateItem.setValidators(new DatesComparedValidator(beginDateItem, endDateItem));
//				Date date = new Date();
//				date.setYear(date.getYear() - 3);
//				endDateItem.setStartDate(date);
//				date.setYear(date.getYear() + 23);
//				endDateItem.setEndDate(date);
//			}
//			
//
//			ButtonItem timeSearchBtnItem = new ButtonItem(constants.searchBtnTitle());
//			{
//				timeSearchBtnItem.setStartRow(false);
//				timeSearchBtnItem.setWidth(60);
//				timeSearchBtnItem.setAlign(Alignment.RIGHT);
//				timeSearchBtnItem.addClickHandler(new ClickHandler() {
//
//					@Override
//					public void onClick(ClickEvent event) {
//						if(timeSearchForm.validate()) {
//							
//							submitCardNo = UIUtil.getCardNo(cardItem);
//							isOnThatDay = Indicator.Y.name().equals(isOnThatDayItem.getValueAsString()) ? true : false;
//							submitLoanType =  loanTypeItem.getValueAsString();
//							beginDate = (Date)timeSearchForm.getValue("beginDate");
//							endDate = (Date)timeSearchForm.getValue("endDate");
//							beginDate = ClientDateUtil.truncateTime(beginDate);
//							endDate = ClientDateUtil.truncateTime(endDate);
//							
//							listGrid.invalidateCache();
//						} else {
//							clientUtils.showWarning(publicConstants.msgSearchConditionInvalid());
//						}
//					}
//					
//				});
//			}
//			
//			timeSearchForm.setFields(cardItem, isOnThatDayItem, loanTypeItem, beginDateItem, endDateItem, timeSearchBtnItem);
//			
//		}
//		
//		return timeSearchForm;
//	} 
//	
//	/**
//	 * 创建需要放款的listGrid
//	 * @return
//	 */
//	private ListGrid createListGrid(){
//		
//		YakDataSource yds = new YakDataSource() {
//			
//			@Override
//			public void fetchData(FetchRequest fetchRequest, AsyncCallback<FetchResponse> callback) {
//				server.getLoanRegList(fetchRequest, submitCardNo, isOnThatDay, submitLoanType, beginDate, endDate, callback);
//			}
//		};
//		
//		DataSourceSequenceField seqField = new DataSourceSequenceField(TmLoanReg.P_RegisterId, constants.registerId());
//		seqField.setPrimaryKey(true);
//		seqField.setHidden(true);
//		
//		yds.setFields(
//				seqField,
//				uTmLoanReg.Org().createField(),
//				uTmLoanReg.AcctNo().createField(),
//				uTmLoanReg.AcctType().createField(),
//				uTmLoanReg.RegisterId().createField(),
//				uTmLoanReg.CardNo().createField(),
//				uTmLoanReg.LogicalCardNo().createField(),
//				uTmLoanReg.RefNbr().createField(),
//				uTmLoanReg.OrigAuthCode().createField(),
//				uTmLoanReg.OrigTransDate().createField(),
//				uTmLoanReg.OrigTxnAmt().createField(),
//				uTmLoanReg.B007().createField(),
//				uTmLoanReg.B011().createField(),
//				uTmLoanReg.B032().createField(),
//				uTmLoanReg.B033().createField(),
//				uTmLoanReg.LoanCode().createField(),
//				uTmLoanReg.LoanFeeMethod().createField(),
//				uTmLoanReg.LoanFinalTermFee1().createField(),
//				uTmLoanReg.LoanFinalTermPrin().createField(),
//				uTmLoanReg.LoanFixedFee1().createField(),
//				uTmLoanReg.LoanFixedPmtPrin().createField(),
//				uTmLoanReg.LoanFirstTermFee1().createField(),
//				uTmLoanReg.LoanFirstTermPrin().createField(),
//				uTmLoanReg.LoanInitFee1().createField(),
//				uTmLoanReg.LoanRegStatus().createField(),
//				uTmLoanReg.LoanInitPrin().createField(),
//				uTmLoanReg.LoanInitTerm().createField(),
//				uTmLoanReg.LoanType().createField(),
//				uTmLoanReg.RequestTime().createField(),
//				uTmLoanReg.RegisterDate().createField()
//				);
//		
//		listGrid = new ListGrid();
//		{
//			listGrid.setShowRecordComponents(true);
//			listGrid.setShowRecordComponentsByCell(true);
//			listGrid.setHeight(200);
//			listGrid.setWidth100();
//			listGrid.setRecordComponentPoolingMode(RecordComponentPoolingMode.DATA);
//			listGrid.setSelectionType(SelectionStyle.SINGLE);
//			
//			listGrid.setFields(
//					uTmLoanReg.RegisterId().hidden().createLGField(),
//					uTmLoanReg.AcctNo().width(90).createLGField(),
//					uTmLoanReg.AcctType().width(90).createLGField(),
//					uTmLoanReg.CardNo().width(100).createLGField(),
//					uTmLoanReg.LoanInitTerm().width(90).createLGField(),
//					uTmLoanReg.LoanInitFee1().width(90).createLGField(),
//					uTmLoanReg.LoanRegStatus().width(90).createLGField(),
//					uTmLoanReg.LoanType().width(90).createLGField(),
//					uTmLoanReg.RegisterDate().width("*").createLGField()
//					);
//			listGrid.setDataSource(yds);
//			listGrid.setAutoFetchData(true);
//			//选中listGird记录时为detailForm赋值
//			listGrid.addSelectionChangedHandler(new SelectionChangedHandler() {
//				
//				@Override
//				public void onSelectionChanged(SelectionEvent event) {
//
//					if(!event.getState()){
//						return;
//					}
//					detailForm.clearValues();
//					YakDataSourceRecord record = (YakDataSourceRecord)event.getSelectedRecord();
//					if(record == null){
//						remarkItem.setCanEdit(false);
//					}
//					detailForm.setValues(record.getValueMap());
//				}
//			});
//		}
//		
//		return listGrid;
//	}
//	
//	/**
//	 * 创建显示现金分期放款的详细信息form
//	 * @return
//	 */
//	private YakDynamicForm showDetailMsg() {
//		
//		detailForm = new YakDynamicForm()
//		{
//			@Override
//			public boolean validate() {
//				if(listGrid.getSelectedRecord() == null) {
//					clientUtils.showWarning(constants.msgNonselectRecord());
//					return false;
//				}
//				
//				if(!super.validate()) {
//					return false; 
//				}
//				
//				return true;
//			}
//		};
//		detailForm.setWidth100();
//		detailForm.setHeight100();
//		detailForm.setNumCols(4);
//		detailForm.setColWidths(150,200,150,"*");
//		detailForm.setOverflow(Overflow.AUTO);
//		detailForm.setTitleWidth(140);
//		detailForm.setFields(
//				uTmLoanReg.RegisterId().asPrimaryKey().asLabel().createFormItem(),
//				uTmLoanReg.AcctNo().asLabel().createFormItem(),
//				uTmLoanReg.AcctType().asLabel().createFormItem(),
//				uTmLoanReg.RegisterId().asLabel().createFormItem(),
//				uTmLoanReg.CardNo().asLabel().createFormItem(),
//				uTmLoanReg.LogicalCardNo().asLabel().createFormItem(),
//				uTmLoanReg.RefNbr().asLabel().createFormItem(),
//				uTmLoanReg.LoanCode().asLabel().createFormItem(),
//				uTmLoanReg.LoanFeeMethod().asLabel().createFormItem(),
//				uTmLoanReg.LoanFinalTermFee1().asLabel().createFormItem(),
//				uTmLoanReg.LoanFinalTermPrin().asLabel().createFormItem(),
//				uTmLoanReg.LoanFixedFee1().asLabel().createFormItem(),
//				uTmLoanReg.LoanFixedPmtPrin().asLabel().createFormItem(),
//				uTmLoanReg.LoanFirstTermFee1().asLabel().createFormItem(),
//				uTmLoanReg.LoanFirstTermPrin().asLabel().createFormItem(),
//				uTmLoanReg.LoanInitFee1().asLabel().createFormItem(),
//				uTmLoanReg.LoanRegStatus().asLabel().createFormItem(),
//				uTmLoanReg.LoanInitPrin().asLabel().createFormItem(),
//				uTmLoanReg.LoanInitTerm().asLabel().createFormItem(),
//				uTmLoanReg.LoanType().asLabel().createFormItem(),
//				uTmLoanReg.RequestTime().asLabel().createFormItem(),
//				uTmLoanReg.RegisterDate().asLabel().createFormItem(),
//				uTmLoanReg.Remark().startRow(true).asLabel().createFormItem()
//				);
//		
//		
//		return detailForm;
//	}
//	
//	@Override
//	public void updateView(ParamMapToken token) {
//		
//	}
//
//	@Override
//	public String getTitle() {
//		return constants.pageTitle();
//	}
//
//	@Override
//	public ImageResource getIcon() {
//		return null;
//	}
//
//
//}
