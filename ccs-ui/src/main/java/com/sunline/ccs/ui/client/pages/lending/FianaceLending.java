package com.sunline.ccs.ui.client.pages.lending;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsLoanReg;
import com.sunline.ccs.ui.client.commons.BtnName;
import com.sunline.ccs.ui.client.commons.CommonUiUtils;
import com.sunline.kylin.web.ark.client.helper.ColumnHelper;
import com.sunline.kylin.web.ark.client.helper.DateColumnHelper;
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
import com.sunline.ui.event.client.EventObjectHandler;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.IDblClickRowEventListener;

/**
 * 
 * @see 类名：FianaceLending
 * @see 描述：现金分期放款
 *
 * @see 创建日期： Jun 22, 20158:06:47 PM
 * @author yeyu
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Singleton
public class FianaceLending extends Page {

    @Inject
    private FianaceLendingConstants constants;
    @Inject
    private UCcsLoanReg uCcsLoanReq;

    private VerticalPanel mainWindow;

    private KylinForm queryForm;

    private KylinForm detialsForm;

    private KylinGrid grid;

    
    
    @Override
    public void refresh() {
		queryForm.getUi().clear();
		detialsForm.getUi().clear();
		grid.clearData();
    }

    @Override
    public IsWidget createPage() {
		this.getMainWindow();
		this.buildQueryForm(mainWindow);
		this.buildGrid(mainWindow);
		this.buildDetailsForm(mainWindow);
		return mainWindow;
    }

    /**
     * 
     * @see 方法名：getMainWindow
     * @see 描述：获取主面板
     * @see 创建日期：Jun 22, 20158:09:39 PM
     * @author yeyu
     * 
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private VerticalPanel getMainWindow() {
	mainWindow = new VerticalPanel();
	mainWindow.setWidth("98%");
	mainWindow.setHeight("100%");
	return mainWindow;
    }

    /**
     * 
     * @see 方法名：buildQueryForm
     * @see 描述：TODO 创建查询表单
     * @see 创建日期：Jun 22, 20158:17:38 PM
     * @author Liming.Feng
     * 
     * @param mainWindow
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private void buildQueryForm(VerticalPanel mainWindow) {
	queryForm = new KylinForm();
	queryForm.setWidth("100%");
	queryForm.setField(new ColumnHelper[]{new DateColumnHelper("beginDate", "开始日期", true, false), new DateColumnHelper("endDate", "结束日期", true, false),});
	KylinButton btnSearch = ClientUtils.createSearchButton(new IClickEventListener(){
	    @Override
	    public void onClick() {
			MapData parameters = queryForm.getSubmitData().asMapData();
			if(parameters.getString("beginDate") != null){
			    grid.getUi().setParm("beginDate",parameters.getString("beginDate"));
			}
			if(parameters.getString("endDate") != null){
			    grid.getUi().setParm("endDate",parameters.getString("endDate"));
			}
			grid.loadDataFromUrl("rpc/fianaceLendingServer/getNeedLendList");
	    }
	});
	HorizontalPanel hPanel = CommonUiUtils.lineLayoutForm(queryForm, btnSearch, null, null);
	mainWindow.add(hPanel);
    }

    /**
     * 
     * @see 方法名：buildGrid
     * @see 描述：TODO 方法描述
     * @see 创建日期：Jun 23, 20154:52:57 PM
     * @author Liming.Feng
     * 
     * @param mainWindow
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private void buildGrid(VerticalPanel mainWindow) {
	grid = new KylinGrid();
	grid.setWidth("98%");
	grid.setHeight("250px");
	grid.setColumns(new ColumnHelper[]{uCcsLoanReq.RegisterId(), uCcsLoanReq.AcctNbr(), uCcsLoanReq.AcctType(),
		uCcsLoanReq.CardNbr(), uCcsLoanReq.LoanInitTerm(), uCcsLoanReq.LoanInitFee(),
		uCcsLoanReq.RegisterDate()});
	grid.addDblClickListener(new IDblClickRowEventListener(){

	    @Override
	    public void onDblClickRow(MapData data, String rowid, EventObjectHandler row) {
	    	detialsForm.getUi().setData(data);
	    }

	});
	grid.loadDataFromUrl("rpc/fianaceLendingServer/getNeedLendList");
	grid.getSetting().delayLoad(Boolean.TRUE);
	mainWindow.add(grid);
    }

    /**
     * 
     * @see 方法名：buildDetailsForm
     * @see 描述：TODO 方法描述
     * @see 创建日期：Jun 23, 20154:52:53 PM
     * @author Liming.Feng
     * 
     * @param mainWindow
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private void buildDetailsForm(VerticalPanel mainWindow) {
    	VerticalPanel panel = new VerticalPanel();
    	panel.setWidth("98%");
    	panel.setHeight("370px");
		ScrollPanel detailFormPanel = new ScrollPanel();
		detailFormPanel.setHeight("370px");
		detialsForm = new KylinForm();
		detialsForm.setWidth("100%");
		detialsForm.getSetting().labelWidth(130);
		detialsForm.setField(new ColumnHelper[]{uCcsLoanReq.RegisterId().readonly(true),
			uCcsLoanReq.AcctNbr().readonly(true), uCcsLoanReq.AcctType().readonly(true),
			uCcsLoanReq.RegisterId().readonly(true), uCcsLoanReq.CardNbr().readonly(true),
			uCcsLoanReq.LogicCardNbr().readonly(true), uCcsLoanReq.RefNbr().readonly(true),
			uCcsLoanReq.LoanCode().readonly(true), uCcsLoanReq.LoanFeeMethod().readonly(true),
			uCcsLoanReq.LoanFinalTermFee().readonly(true), uCcsLoanReq.LoanFinalTermPrin().readonly(true),
			uCcsLoanReq.LoanFixedFee().readonly(true), uCcsLoanReq.LoanFixedPmtPrin().readonly(true),
			uCcsLoanReq.LoanFirstTermFee().readonly(true), uCcsLoanReq.LoanFirstTermPrin().readonly(true),
			uCcsLoanReq.LoanInitFee().readonly(true), uCcsLoanReq.LoanInitPrin().readonly(true),
			uCcsLoanReq.LoanInitTerm().readonly(true), uCcsLoanReq.LoanType().readonly(true),
			uCcsLoanReq.RequestTime().readonly(true), uCcsLoanReq.RegisterDate().readonly(true),
			uCcsLoanReq.Remark().readonly(true)
		});
		detialsForm.addButton(CommonUiUtils.createButton(BtnName.AGREE, "skins/icons/ok.gif",new IClickEventListener(){
		    @Override
		    public void onClick() {
			RPC.ajax("rpc/fianaceLendingServer/setLending", new RpcCallback<Data>(){
			    @Override
			    public void onSuccess(Data result) {
				String rtn = result.asMapData().getString("lendingReturn");
				if (rtn.equalsIgnoreCase("SUCCESS")) {
				    Dialog.alert("放款成功");
				}
			    }
			}, new Object[]{detialsForm.getSubmitData()});
		    }
		}));
		detailFormPanel.add(detialsForm);
		panel.add(detailFormPanel);
		mainWindow.add(panel);
    }
}

// package com.sunline.ccs.ui.client.txn.lending;
//
// import java.io.Serializable;
// import java.util.Date;
// import java.util.Map;
//
// import com.sunline.pcm.ui.common.client.ClientUtils;
// import com.sunline.pcm.ui.common.client.DispatcherPage;
// import com.sunline.pcm.ui.common.client.validator.ClientDateUtil;
// import com.sunline.pcm.ui.common.client.validator.DatesComparedValidator;
// import com.sunline.ccs.ui.client.pub.PublicConstants;
// import com.sunline.ccs.infrastructure.client.ui.UTmLoanReg;
// import com.sunline.ccs.infrastructure.shared.model.TmLoanReg;
// import com.sunline.ccs.param.def.enums.CPSAppAuthority;
// import com.sunline.ark.gwt.client.datasource.YakDataSource;
// import com.sunline.ark.gwt.client.datasource.YakDataSourceRecord;
// import com.sunline.ark.gwt.client.mvp.ParamMapToken;
// import com.sunline.ark.gwt.client.ui.YakDynamicForm;
// import com.sunline.ark.gwt.client.util.RPCExecutor;
// import com.sunline.ark.gwt.client.util.RPCTemplate;
// import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
// import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
// import com.google.gwt.resources.client.ImageResource;
// import com.google.gwt.user.client.rpc.AsyncCallback;
// import com.google.inject.Inject;
// import com.smartgwt.client.data.fields.DataSourceSequenceField;
// import com.smartgwt.client.types.Alignment;
// import com.smartgwt.client.types.Overflow;
// import com.smartgwt.client.types.RecordComponentPoolingMode;
// import com.smartgwt.client.types.SelectionStyle;
// import com.smartgwt.client.types.VisibilityMode;
// import com.smartgwt.client.util.BooleanCallback;
// import com.smartgwt.client.util.SC;
// import com.smartgwt.client.widgets.IButton;
// import com.smartgwt.client.widgets.form.fields.ButtonItem;
// import com.smartgwt.client.widgets.form.fields.DateItem;
// import com.smartgwt.client.widgets.form.fields.FormItem;
// import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
// import com.smartgwt.client.widgets.form.fields.events.ClickHandler;
// import com.smartgwt.client.widgets.grid.ListGrid;
// import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
// import com.smartgwt.client.widgets.grid.events.SelectionEvent;
// import com.smartgwt.client.widgets.layout.HLayout;
// import com.smartgwt.client.widgets.layout.SectionStack;
// import com.smartgwt.client.widgets.layout.SectionStackSection;
//
// /**
// * 现金分期放款页面
// * @author fanghj
// * @date 2015-1-30 17:00:00
// * @version 1.0
// */
// public class FianaceLending extends DispatcherPage{
//
// public static final String PAGE_ID = "risk-3310";
//
// @Inject
// private FianaceLendingConstants constants;
// @Inject
// private PublicConstants publicConstants;
// @Inject
// private ClientUtils clientUtils;
// @Inject
// private UTmLoanReg uCcsLoanReq;
// @Inject
// private T3310InterAsync server;
// /**
// * <p>放款成功，弹窗标识</p>
// */
// private static final String RPC_SUCCESS = "TRUE";
//
// private ListGrid listGrid;
// private YakDynamicForm detailForm;
// private HLayout buttonLayout;
// private FormItem remarkItem;
//
// private Date beginDate;
// private Date endDate;
// private IButton agreeButton;
//
// public FianaceLending(){
// super(PAGE_ID, true, CPSAppAuthority.T3310);
// }
//
// @Override
// protected void createCanvas() {
//
// beginDate = new Date();
//
// final SectionStack sectionStack = new SectionStack();
// {
// sectionStack.setVisibilityMode(VisibilityMode.MULTIPLE);
// sectionStack.setWidth100();
// sectionStack.setHeight100();
// }
//
// //增加现金分期放款的查询和显示stack
// SectionStackSection showListGirdStack = new
// SectionStackSection(constants.cashStage());
// {
// showListGirdStack.setExpanded(true);
//
// //创建时间搜索Form
// showListGirdStack.addItem(createTimeSearchForm());
//
// //创建要显示的申请的listGrid
// showListGirdStack.addItem(createListGrid());
// }
// sectionStack.addSection(showListGirdStack);
//
// //增加现金分期申请的详细信息stack
// SectionStackSection showDetailStack = new
// SectionStackSection(constants.showDetailStack());
// {
// showDetailStack.setExpanded(true);
//
// //创建申请的详细信息
// showDetailStack.addItem(showDetailMsg());
//
// showDetailStack.addItem(agreeOrRefuseBtn());
//
// }
// sectionStack.addSection(showDetailStack);
//
// addMember(sectionStack);
//
// }
//
// /**
// * 创建时间搜索表单
// */
// private YakDynamicForm createTimeSearchForm() {
// final YakDynamicForm timeSearchForm = new YakDynamicForm();
// {
// timeSearchForm.setNumCols(5);
// timeSearchForm.setWidth100();
// timeSearchForm.setColWidths(100, 150, 100, 150, "*");
// //查询起始日期
// DateItem beginDateItem = new DateItem("beginDate",
// publicConstants.labelBeginDate());
// {
// beginDateItem.setUseTextField(true);
// beginDateItem.setRequired(true);
//
// beginDateItem.setValue(beginDate);
// Date date = new Date();
// date.setYear(date.getYear() - 3);
// beginDateItem.setStartDate(date);
// date.setYear(date.getYear() + 23);
// beginDateItem.setEndDate(date);
// }
//
// //查询截止日期
// DateItem endDateItem = new DateItem("endDate",
// publicConstants.labelEndDate());
// {
// endDateItem.setUseTextField(true);
// endDateItem.setValidators(new DatesComparedValidator(beginDateItem,
// endDateItem));
// Date date = new Date();
// date.setYear(date.getYear() - 3);
// endDateItem.setStartDate(date);
// date.setYear(date.getYear() + 23);
// endDateItem.setEndDate(date);
// }
//
// ButtonItem timeSearchBtnItem = new ButtonItem(constants.searchBtnTitle());
// {
// timeSearchBtnItem.setStartRow(false);
// timeSearchBtnItem.addClickHandler(new ClickHandler() {
//
// @Override
// public void onClick(ClickEvent event) {
// if(timeSearchForm.validate()) {
//
// beginDate = (Date)timeSearchForm.getValue("beginDate");
// endDate = (Date)timeSearchForm.getValue("endDate");
// beginDate = ClientDateUtil.truncateTime(beginDate);
// endDate = ClientDateUtil.truncateTime(endDate);
// listGrid.invalidateCache();
// } else {
// clientUtils.showWarning(publicConstants.msgSearchConditionInvalid());
// }
// }
//
// });
// }
//
// timeSearchForm.setFields(beginDateItem, endDateItem, timeSearchBtnItem);
//
// }
//
// return timeSearchForm;
// }
//
// /**
// * 创建需要放款的listGrid
// * @return
// */
// private ListGrid createListGrid(){
//
// YakDataSource yds = new YakDataSource() {
//
// @Override
// public void fetchData(FetchRequest fetchRequest, AsyncCallback<FetchResponse>
// callback) {
// server.getNeedLendList(fetchRequest, beginDate, endDate, callback);
//
// }
// };
//
// DataSourceSequenceField seqField = new
// DataSourceSequenceField(TmLoanReg.P_RegisterId, constants.registerId());
// seqField.setPrimaryKey(true);
// seqField.setHidden(true);
//
// yds.setFields(
// seqField,
// uCcsLoanReq.Org().createField(),
// uCcsLoanReq.AcctNo().createField(),
// uCcsLoanReq.AcctType().createField(),
// uCcsLoanReq.RegisterId().createField(),
// uCcsLoanReq.CardNo().createField(),
// uCcsLoanReq.LogicalCardNo().createField(),
// uCcsLoanReq.RefNbr().createField(),
// uCcsLoanReq.OrigAuthCode().createField(),
// uCcsLoanReq.OrigTransDate().createField(),
// uCcsLoanReq.OrigTxnAmt().createField(),
// uCcsLoanReq.B007().createField(),
// uCcsLoanReq.B011().createField(),
// uCcsLoanReq.B032().createField(),
// uCcsLoanReq.B033().createField(),
// uCcsLoanReq.LoanCode().createField(),
// uCcsLoanReq.LoanFeeMethod().createField(),
// uCcsLoanReq.LoanFinalTermFee1().createField(),
// uCcsLoanReq.LoanFinalTermPrin().createField(),
// uCcsLoanReq.LoanFixedFee1().createField(),
// uCcsLoanReq.LoanFixedPmtPrin().createField(),
// uCcsLoanReq.LoanFirstTermFee1().createField(),
// uCcsLoanReq.LoanFirstTermPrin().createField(),
// uCcsLoanReq.LoanInitFee1().createField(),
// uCcsLoanReq.LoanInitPrin().createField(),
// uCcsLoanReq.LoanInitTerm().createField(),
// uCcsLoanReq.LoanType().createField(),
// uCcsLoanReq.RequestTime().createField(),
// uCcsLoanReq.RegisterDate().createField(),
// uCcsLoanReq.Remark().createField()
// );
//
// listGrid = new ListGrid();
// {
// listGrid.setShowRecordComponents(true);
// listGrid.setShowRecordComponentsByCell(true);
// listGrid.setHeight(200);
// listGrid.setWidth100();
// listGrid.setRecordComponentPoolingMode(RecordComponentPoolingMode.DATA);
// listGrid.setSelectionType(SelectionStyle.SINGLE);
//
// listGrid.setFields(
// uCcsLoanReq.RegisterId().hidden().createLGField(),
// uCcsLoanReq.AcctNo().width(90).createLGField(),
// uCcsLoanReq.AcctType().width(90).createLGField(),
// uCcsLoanReq.CardNo().width(100).createLGField(),
// uCcsLoanReq.LoanInitTerm().width(90).createLGField(),
// uCcsLoanReq.LoanInitFee1().width(90).createLGField(),
// uCcsLoanReq.RegisterDate().width("*").createLGField()
// );
// listGrid.setDataSource(yds);
// listGrid.setAutoFetchData(true);
//
// //选中listGird记录时为detailForm赋值
// listGrid.addSelectionChangedHandler(new SelectionChangedHandler() {
//
// @Override
// public void onSelectionChanged(SelectionEvent event) {
//
// if(!event.getState()){
// return;
// }
// detailForm.clearValues();
// YakDataSourceRecord record = (YakDataSourceRecord)event.getSelectedRecord();
// if(record == null){
// remarkItem.setCanEdit(false);
// }
// detailForm.setValues(record.getValueMap());
// }
// });
// }
//
// return listGrid;
// }
//
// /**
// * 创建显示现金分期放款的详细信息form
// * @return
// */
// private YakDynamicForm showDetailMsg() {
//
// detailForm = new YakDynamicForm()
// {
// @Override
// public boolean validate() {
// if(listGrid.getSelectedRecord() == null) {
// clientUtils.showWarning(constants.msgNonselectRecord());
// return false;
// }
//
// if(!super.validate()) {
// return false;
// }
//
// return true;
// }
// };
// detailForm.setWidth100();
// detailForm.setHeight100();
// detailForm.setNumCols(4);
// detailForm.setColWidths(150,200,150,"*");
// detailForm.setOverflow(Overflow.AUTO);
// detailForm.setTitleWidth(140);
// remarkItem =
// uCcsLoanReq.Remark().startRow(true).colSpan(3).width(650).required().createFormItem();
// detailForm.setFields(
// uCcsLoanReq.RegisterId().asPrimaryKey(),
// uCcsLoanReq.AcctNo(),
// uCcsLoanReq.AcctType(),
// uCcsLoanReq.RegisterId(),
// uCcsLoanReq.CardNo(),
// uCcsLoanReq.LogicalCardNo(),
// uCcsLoanReq.RefNbr(),
// uCcsLoanReq.LoanCode(),
// uCcsLoanReq.LoanFeeMethod(),
// uCcsLoanReq.LoanFinalTermFee1(),
// uCcsLoanReq.LoanFinalTermPrin(),
// uCcsLoanReq.LoanFixedFee1(),
// uCcsLoanReq.LoanFixedPmtPrin(),
// uCcsLoanReq.LoanFirstTermFee1(),
// uCcsLoanReq.LoanFirstTermPrin(),
// uCcsLoanReq.LoanInitFee1(),
// uCcsLoanReq.LoanInitPrin(),
// uCcsLoanReq.LoanInitTerm(),
// uCcsLoanReq.LoanType(),
// uCcsLoanReq.RequestTime(),
// uCcsLoanReq.RegisterDate(),
// remarkItem
// );
//
//
// return detailForm;
// }
//
// //同意按钮
// private HLayout agreeOrRefuseBtn(){
//
// buttonLayout = new HLayout(10);
// {
// buttonLayout.setWidth(800);
// buttonLayout.setAlign(Alignment.CENTER);
//
// agreeButton = new IButton(constants.agreeBtn());
// {
// agreeButton.addClickHandler(new
// com.smartgwt.client.widgets.events.ClickHandler() {
//
// @SuppressWarnings("unchecked")
// @Override
// public void onClick(com.smartgwt.client.widgets.events.ClickEvent event) {
// btnClick(detailForm.getValues());
// }
// });
//
// }
//
// buttonLayout.addMember(agreeButton);
//
// }
//
// return buttonLayout;
// }
//
// //点放款按钮的操作
// private void btnClick(final Map<String, Serializable> values){
// if(detailForm.validate()){//验证表单信息
// String Msg = constants.agreeBtnMsg();
// SC.confirm(Msg, new BooleanCallback() {
//
// @Override
// public void execute(Boolean value) {
// if(value != null && value){
// agreeButton.setDisabled(true);
// RPCTemplate.call(new RPCExecutor<String>() {
//
// @Override
// public void execute(AsyncCallback<String> callback) {
// server.setLending(values, callback);
// }
// @Override
// public void onSuccess(String result){
// if (RPC_SUCCESS.equals(result)) {
// clientUtils.showSuccess();
// } else {
// clientUtils.showWarning(result);
// }
// listGrid.invalidateCache();
// detailForm.clearValues();
// agreeButton.setDisabled(false);
// }
//
// @Override
// public void onFailure() {
// listGrid.invalidateCache();
// detailForm.clearValues();
// agreeButton.setDisabled(false);
// super.onFailure();
// }
//
// }, detailForm);
// }
//
// }
// });
// }
// agreeButton.setDisabled(false);
// }
//
// @Override
// public void updateView(ParamMapToken token) {
// // TODO Auto-generated method stub
//
// }
//
// @Override
// public String getTitle() {
// return constants.pageTitle();
// }
//
// @Override
// public ImageResource getIcon() {
// // TODO Auto-generated method stub
// return null;
// }
//
//
// }
