package com.sunline.ccs.ui.client.pages.accountinfomgr;

import java.util.LinkedHashMap;

import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsAcct;
import com.sunline.ccs.infrastructure.client.ui.UCcsCardLmMapping;
import com.sunline.ccs.infrastructure.client.ui.UCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.ui.client.commons.CommonSelectItemWrapper;
import com.sunline.ccs.ui.client.commons.CommonUiUtils;
import com.sunline.ccs.ui.client.commons.UIUtilConstants;
import com.sunline.kylin.web.ark.client.helper.ColumnHelper;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
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
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.ISelectRowEventListener;

/**
 * 
 * @说明 账户信息管理界面
 * 
 * @version 1.0
 *
 * @Date Jun 17, 2015
 *
 * @作者 yeyu
 *
 * @修改记录 [编号：20070101_01]，[修改人：yeyu ]，[修改说明：UI重构]
 */
@Singleton
public class AccountInfoMgr extends Page {

	@Inject
	private UCcsAcct uCcsAcct;

	@Override
	public void refresh() {
		dataGrid.clearData();
		idCardForm.getUi().clear();
		cardNoForm.getUi().clear();
		mobileForm.getUi().clear();
		acctDetailsForm.getUi().clear();
	}

	@Inject
	private CcsAcct ccsAcct;

	@Inject
	private UCcsCustomer uCcsCustomer;

	@Inject
	private CcsCustomer cCsCustomer;

	@Inject
	private UIUtilConstants uiConstants;

	@Inject
	private UCcsCardLmMapping uCcsCardLmMapping;

	private KylinGrid dataGrid;

	private KylinForm idCardForm;

	private KylinForm cardNoForm;

	private KylinForm mobileForm;
	
	private KylinForm acctDetailsForm;

	@Inject
	private AccountInfoMgrConstants constants;

	private VerticalPanel mainWindow;

	@Override
	public IsWidget createPage() {
		mainWindow = this.buildMainWindow();
		this.buildCardForm(mainWindow);
		this.buildIdentityCardForm(mainWindow);
		this.buildMobileForm(mainWindow);
		this.buildDataGrid(mainWindow);
		this.buildAcctDetailsForm(mainWindow);
		this.buildButton(mainWindow);
		return mainWindow;
	}

	/**
	 * 
	 * @see 方法名：buildButton
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：Jun 23, 20157:32:51 PM
	 * @author Liming.Feng
	 * 
	 * @param mainWindow
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private void buildButton(VerticalPanel mainWindow) {
		KylinButton submit = this.getButton("提交", null);
		KylinButton reset = this.getButton("重置", null);
		submit.addClickEventListener(new UpdateAcctInfoEvent());
		reset.addClickEventListener(new FormResetEvent());
		HorizontalPanel horizontalPanen = this.getHorizontalPanel();
		horizontalPanen.add(submit);
		horizontalPanen.add(reset);
		mainWindow.add(horizontalPanen);
	}

	/**
	 * 
	 * @see 方法名：getHorizontalPanel
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：Jun 23, 20157:32:48 PM
	 * @author Liming.Feng
	 * 
	 * @return
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private HorizontalPanel getHorizontalPanel() {
		HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.setWidth("98%");
		horizontalPanel.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
		return horizontalPanel;
	}

	/**
	 * 
	 * @see 类名：UpdateAcctInfoEvent
	 * @see 描述：TODO 中文描述
	 *
	 * @see 创建日期： Jun 23, 20157:32:44 PM
	 * @author Liming.Feng
	 * 
	 * @see 修改记录：
	 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
	 */
	class UpdateAcctInfoEvent implements IClickEventListener {

		@Override
		public void onClick() {
			Dialog.confirm("是否修改用户信息", "确认", new ConfirmCallbackFunction() {

				@Override
				public void corfimCallback(boolean value) {
					if (value) {
						if (acctDetailsForm.getSubmitData() != null) {
							RPC.ajax(
									"rpc/accountInfoMgrServer/updateAccountInfo",
									new RpcCallback<Data>() {
										@Override
										public void onSuccess(Data result) {
											Dialog.tipNotice("修改成功", 1000);
										}
									}, acctDetailsForm.getSubmitData()
											.asMapData().toMap());
						}
					}
				}
			});
		}

	}

	/**
	 * 
	 * @see 类名：FormResetEvent
	 * @see 描述：表单重置事件监听器
	 *
	 * @see 创建日期： Jun 18, 20157:06:36 PM
	 * @author yeyu
	 * 
	 * @see 修改记录：
	 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
	 */
	class FormResetEvent implements IClickEventListener {

		@Override
		public void onClick() {
			acctDetailsForm.getUi().clear();
		}
	}

	/**
	 * 
	 * @see 类名：FormSubmitEvent
	 * @see 描述：表单提交事件监听器
	 *
	 * @see 创建日期： Jun 18, 20157:07:07 PM
	 * @author yeyu
	 * 
	 * @see 修改记录：
	 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
	 */
	class FormSubmitEvent implements IClickEventListener {

		private KylinForm _form;

		@Override
		public void onClick() {
			if (_form.valid()) {
			    	MapData mapData = _form.getSubmitData().asMapData();
				if(mapData.getString(CcsCardLmMapping.P_CardNbr)!= null){
				    dataGrid.getUi().setParm(CcsCardLmMapping.P_CardNbr, mapData.getString(CcsCardLmMapping.P_CardNbr));
				}
				if(mapData.getString(CcsAcct.P_Currency)!= null){
				    dataGrid.getUi().setParm(CcsAcct.P_Currency, mapData.getString(CcsAcct.P_Currency));
				}
				if(mapData.getString(CcsCustomer.P_IdType)!= null){
				    dataGrid.getUi().setParm(CcsCustomer.P_IdType, mapData.getString(CcsCustomer.P_IdType));
				}
				if(mapData.getString(CcsCustomer.P_MobileNo)!= null){
				    dataGrid.getUi().setParm(CcsCustomer.P_MobileNo, mapData.getString(CcsCustomer.P_MobileNo));
				}
				if(mapData.getString(CcsCustomer.P_IdNo)!= null){
				    dataGrid.getUi().setParm(CcsCustomer.P_IdNo, mapData.getString(CcsCustomer.P_IdNo));
				}
				dataGrid.loadDataFromUrl("rpc/accountInfoMgrServer/getAcctList");
			} else {
				// TODO
			}
		}

		public FormSubmitEvent(KylinForm form) {
			this._form = form;
		}

	}

	/**
	 * 
	 * @see 方法名：buildAcctDetailsForm
	 * @see 描述：创建账户详情表单
	 * @see 创建日期：Jun 22, 20153:11:14 PM
	 * @author yeyu
	 * 
	 * @param mainWindow
	 * 
	 * @see 修改记录：
	 */
	private void buildAcctDetailsForm(VerticalPanel mainWindow) {
		acctDetailsForm = this.getFormInstance();
		ScrollPanel scrollPanel = this.buildScrollPanel("140px");
		scrollPanel.setWidth("98%");
		acctDetailsForm.setField(new ColumnHelper[] {
				uCcsAcct.AcctNbr().readonly(true),
				uCcsAcct.AcctType().readonly(true),
				uCcsAcct.DefaultLogicCardNbr().readonly(true),
				uCcsAcct.OwningBranch().readonly(true),
				uCcsAcct.Name().readonly(true),
				uCcsAcct.CycleDay().readonly(true),
				uCcsCustomer.IdType().readonly(true),
				uCcsAcct.StmtFlag().readonly(true),
				uCcsCustomer.IdNo().readonly(true),
				uCcsAcct.StmtMailAddrInd().readonly(true),
				uCcsAcct.Currency().asSelectItem().readonly(true),
				uCcsAcct.StmtMediaType().readonly(true),
				uCcsAcct.ProductCd().readonly(true), // 产品名称
				uCcsAcct.ProductCd().asSelectItem().readonly(true),
				uCcsAcct.BlockCode().readonly(true),
				uCcsAcct.CreditLmt().readonly(true),
				uCcsAcct.AgeCode().readonly(true),
				uCcsAcct.WaiveOvlfeeInd().readonly(false),
				uCcsAcct.WaiveCardfeeInd().readonly(false),
				uCcsAcct.WaiveLatefeeInd().readonly(false),
				uCcsAcct.WaiveSvcfeeInd().readonly(false),
				uCcsAcct.TempLmt().readonly(true),
				uCcsAcct.MemoDb().readonly(true),
				uCcsAcct.TempLmtBegDate().readonly(true),
				uCcsAcct.MemoCash().readonly(true),
				uCcsAcct.TempLmtEndDate().readonly(true),
				uCcsAcct.MemoCr().readonly(true),
				uCcsAcct.CashLmtRate().readonly(true),
				uCcsAcct.DualBillingFlag().readonly(true),
				uCcsAcct.OvrlmtRate().readonly(true),
				uCcsAcct.LastPmtAmt().readonly(true),
				uCcsAcct.CurrBal().readonly(true),
				uCcsAcct.LastStmtDate().readonly(true),
				uCcsAcct.CashBal().readonly(true),
				uCcsAcct.NextStmtDate().readonly(true),
				uCcsAcct.PrincipalBal().readonly(true),
				uCcsAcct.PmtDueDate().readonly(true),
				uCcsAcct.LoanBal().readonly(true),
				uCcsAcct.DdDate().readonly(true),
				uCcsAcct.DisputeAmt().readonly(true),
				uCcsAcct.GraceDate().readonly(true),
				uCcsAcct.BegBal().readonly(true),
				uCcsAcct.ClosedDate().readonly(true),
				uCcsAcct.PmtDueDayBal().readonly(true),
				uCcsAcct.FirstStmtDate().readonly(true),
				uCcsAcct.QualGraceBal().readonly(true),
				uCcsAcct.CloseDate().readonly(true),
				uCcsAcct.GraceDaysFullInd().readonly(true),
				uCcsAcct.ChargeOffDate().readonly(true),
				uCcsAcct.FirstRetlDate().readonly(true),
				uCcsAcct.FirstRetlAmt().readonly(true),
				uCcsAcct.SetupDate().readonly(true),
				uCcsAcct.TotDueAmt().readonly(true),
				uCcsAcct.AcctNbr().setHide(true),
				uCcsAcct.AcctType().setHide(true)
				});
		scrollPanel.add(acctDetailsForm);
		mainWindow.add(scrollPanel);
	}

	private VerticalPanel buildMainWindow() {
		mainWindow = new VerticalPanel();
		mainWindow.setWidth("98%");
		return mainWindow;
	}

	private ScrollPanel buildScrollPanel(String height) {
		ScrollPanel scrollPanel = new ScrollPanel();
		scrollPanel.setHeight(height);
		return scrollPanel;
	}

	private void buildMobileForm(VerticalPanel mainWindow) {
		mobileForm = this.getFormInstance();
		mobileForm.setField(new ColumnHelper[] {
				uCcsCustomer.MobileNo().required(true),
				uCcsAcct.Currency().asSelectItem().required(true) });
		LinkedHashMap<String, String> currencyType = new LinkedHashMap<String, String>();
		currencyType.put("156", "人民币");
		CommonSelectItemWrapper.getInstance().wrapper(mobileForm,
				uCcsAcct.Currency(), currencyType);
		KylinButton btnQueryAcctInfo = this.getButton("搜索", null);
		btnQueryAcctInfo.addClickEventListener(new FormSubmitEvent(mobileForm));
		mainWindow.add(CommonUiUtils.lineLayoutForm(mobileForm, btnQueryAcctInfo, null, null));
	}

	/**
	 * 
	 * @see 方法名：buildCardForm
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：Jun 23, 20156:26:30 PM
	 * @author Liming.Feng
	 * 
	 * @param mainWindow
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private void buildCardForm(VerticalPanel mainWindow) {
		TextColumnHelper cardNr = uCcsCardLmMapping.CardNbr();
		TextColumnHelper currCd = uCcsAcct.Currency();
		cardNoForm = this.getFormInstance();
		cardNoForm.setField(new ColumnHelper[] { cardNr.required(true),
				currCd.asSelectItem().required(true) });
		LinkedHashMap<String, String> currencyType = new LinkedHashMap<String, String>();
		currencyType.put("156", "人民币");
		CommonSelectItemWrapper.getInstance().wrapper(cardNoForm,
				uCcsAcct.Currency(), currencyType);
		KylinButton btnQueryAcctInfo = this.getButton("搜索", null);
		btnQueryAcctInfo.addClickEventListener(new FormSubmitEvent(cardNoForm));
		mainWindow.add(CommonUiUtils.lineLayoutForm(cardNoForm, btnQueryAcctInfo, null, null));
	}

	private void buildIdentityCardForm(VerticalPanel mainWindow) {
		idCardForm = this.getFormInstance();
		idCardForm.setCol(3);
		idCardForm.setField(new ColumnHelper[] {
				uCcsCustomer.IdType().asSelectItem(SelectType.GRID)
						.required(true), uCcsCustomer.IdNo().required(true),
				uCcsAcct.Currency().asSelectItem().required(true) });
		LinkedHashMap<String, String> currencyType = new LinkedHashMap<String, String>();
		currencyType.put("156", "人民币");
		CommonSelectItemWrapper.getInstance().wrapper(idCardForm,
				uCcsAcct.Currency(), currencyType);
		KylinButton btnQueryAcctInfo = this.getButton("搜索", null);
		btnQueryAcctInfo.addClickEventListener(new FormSubmitEvent(idCardForm));
		mainWindow.add(CommonUiUtils.lineLayoutForm(idCardForm, btnQueryAcctInfo, "100%", null));
	}

	private void buildDataGrid(VerticalPanel mainWindow) {
		dataGrid = this.getGridInstance(Boolean.TRUE);
		dataGrid.setColumns(new ColumnHelper[] { uCcsAcct.AcctNbr(),
				uCcsAcct.AcctType(), uCcsAcct.DefaultLogicCardNbr(),
				uCcsAcct.Currency(), uCcsAcct.Name(), uCcsAcct.ProductCd(),
				uCcsAcct.OwningBranch() });
		dataGrid.getSetting().onSelectRow(new ISelectRowEventListener() {

			@Override
			public void selectRow(MapData rowdata, String rowid,
					EventObjectHandler rowobj) {
				acctDetailsForm.getUi().setData(rowdata);
			}

		});
		dataGrid.getSetting().onUnSelectRow(new ISelectRowEventListener() {

			@Override
			public void selectRow(MapData rowdata, String rowid,
					EventObjectHandler rowobj) {
				acctDetailsForm.getUi().clear();
			}

		});
		/*
		 * dataGrid.addDblClickListener(new IDblClickRowEventListener(){
		 * 
		 * @Override public void onDblClickRow(MapData data, String rowid,
		 * EventObjectHandler row) { acctDetailsForm.getUi().setData(data); }
		 * 
		 * });
		 */
		mainWindow.add(dataGrid);
	}

	private KylinForm getFormInstance() {
		KylinForm form = new KylinForm();
		form.setWidth("98%");
		return form;
	}

	private KylinGrid getGridInstance(boolean isLazyLoad) {
		KylinGrid dataGrid = new KylinGrid();
		dataGrid.setWidth("98%");
		dataGrid.setHeight(230);
		dataGrid.getSetting().delayLoad(isLazyLoad);
		return dataGrid;
	}

	private KylinButton getButton(String btnName, String imgUrl) {
		KylinButton button = new KylinButton(btnName, imgUrl);
		return button;
	}
}
//
// public static final String PAGE_ID = "acct-3002";
//
// @Inject
// private T3002Constants constants;
// @Inject
// private T3002InterAsync server;
// @Inject
// private ClientUtils clientUtils;
// @Inject
// private UTmAccount uCcsAcct;
// @Inject
// private UTmCustomer uCcsCustomer;
// @Inject
// private UTmCardMediaMap uTmCardMediaMap;
// @Inject
// private UIUtil uiUtil;
//
// private ListGrid listGrid;
// private YakDynamicForm detailForm;
//
// private FormItem cardItem;
// private SelectItem idTypeItem;
// private FormItem idNoItem;
// private FormItem telPhoneItem;
//
// private Integer selectedAcctNo;
// private AccountType selectedAcctType;
// private String submitCardNo;
// private String submitIdType;
// private String submitIdNo;
// private String submitTelPhone;
// private String currencyCode;
// private SelectItem currencyItem;
//
//
//
// //保存卡片的所有账户记录
// private Map<TmAccountKey, TmAccount> tmAccountMap = new HashMap<TmAccountKey,
// TmAccount>();
// private Map<String, Serializable> maps = new HashMap<String, Serializable>();
//
// public T3002Page() {
// super(PAGE_ID, true, CPSAppAuthority.T3002);
// }
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
// @Override
// protected void createCanvas() {
// setMembersMargin(4);
// final SectionStack sectionStack = new SectionStack();
// {
// sectionStack.setVisibilityMode(VisibilityMode.MULTIPLE);
// sectionStack.setWidth100();
// sectionStack.setHeight100();
// }
//
// //增加现金分期审核的查询和显示stack
// SectionStackSection showListGirdStack = new
// SectionStackSection(constants.sectionTitleAcctList());
// {
// showListGirdStack.setExpanded(true);
//
// //创建要显示的申请的listGrid
// HLayout hLayout = new HLayout(4);
// hLayout.setWidth100();
// hLayout.setHeight100();
//
// listGrid = createListGrid();
//
// hLayout.addMember(listGrid);
// showListGirdStack.addItem(hLayout);
// }
// sectionStack.addSection(showListGirdStack);
//
// //账户信息详情显示
// SectionStackSection showDetailStack = new
// SectionStackSection(constants.sectionTitleAcctDetail());
// {
// showDetailStack.setExpanded(true);
//
// detailForm = createTmAccountForm();
// VLayout vLayout = new VLayout();
// vLayout.setWidth100();
// vLayout.setHeight100();
// vLayout.addMember(detailForm);
//
// final HLayout buttonsLayout =
// clientUtils.createSubmitResetLayout(detailForm);
// vLayout.addMember(buttonsLayout);
//
// detailForm.addSubmitValuesHandler(new SubmitValuesHandler() {
//
// @Override
// public void onSubmitValues(SubmitValuesEvent event) {
// if (detailForm.validate()){//验证表单信息
// SC.confirm(constants.msgOKConfirm(), new BooleanCallback() {
// @Override
// public void execute(Boolean value) {
// if(value != null && value) {
// RPCTemplate.call(new RPCExecutor<Void>() {
// @SuppressWarnings("unchecked")
// @Override
// public void execute(AsyncCallback<Void> callback) {
// server.updateAccountInfo(
// detailForm.getValues(),
// callback);
// }
//
// @Override
// public void onSuccess(Void result) {
// clientUtils.showSuccess();
// }
//
// }, buttonsLayout);
// }
// }
// });
//
// }
//
// }
// });
//
//
// showDetailStack.addItem(vLayout);
//
// }
// sectionStack.addSection(showDetailStack);
// addMember(createSearchForm());
// addMember(createSearchFormToCust());
// addMember(createSearchFormToTel());
// addMember(sectionStack);
//
// }
//
// /**
// * 创建账户列表的listGrid
// * @return
// */
// private ListGrid createListGrid() {
// final ListGrid listGrid = new ListGrid();
// listGrid.setShowRecordComponents(true);
// listGrid.setShowRecordComponentsByCell(true);
// listGrid.setHeight100();
// listGrid.setWidth100();
// listGrid.setRecordComponentPoolingMode(RecordComponentPoolingMode.DATA);
//
// listGrid.setFields(
// uCcsAcct.AcctNo().width(100).createLGField(),
// uCcsAcct.AcctType().width(200).createLGField(),
// uCcsAcct.DefaultLogicalCardNo().width(150).createLGField(),
// uCcsAcct.CurrCd().width(100).createLGField(),
// uCcsAcct.Name().width(100).createLGField(),
// uCcsAcct.ProductCd().width(120).createLGField(),
// uCcsAcct.OwningBranch().width("*").createLGField()
// );
//
// listGrid.setData(new ListGridRecord[]{});
// //选中listGird记录时为detailForm赋值
// listGrid.addSelectionChangedHandler(new SelectionChangedHandler() {
//
// @Override
// public void onSelectionChanged(SelectionEvent event) {
//
// ListGridRecord record = event.getSelectedRecord();
//
// if(null == record) {
// detailForm.clearValues();
// return;
// }
// selectedAcctNo = record.getAttributeAsInt(TmAccount.P_AcctNo);
// selectedAcctType =
// AccountType.valueOf(record.getAttribute(TmAccount.P_AcctType));
//
//
// TmAccountKey acctKey = new TmAccountKey();
// acctKey.setAcctNo(selectedAcctNo);
// acctKey.setAcctType(selectedAcctType);
//
// detailForm.setValues(maps);
// detailForm.setValues(tmAccountMap.get(acctKey).convertToMap());
// // detailForm.setValues(record.getValueMap());
// }
// });
//
// return listGrid;
// }
// /**
// * 创建查询表单
// * */
// private YakDynamicForm createSearchForm() {
// final YakDynamicForm searchForm = new YakDynamicForm();
// {
// searchForm.setWidth100();
// searchForm.setNumCols(5);
// searchForm.setColWidths(70, 170, 70, 170,"*");
//
// cardItem = uTmCardMediaMap.CardNo().createFormItem();
// currencyItem = uiUtil.createCurrencySelectItem();
// currencyItem.setRequired(false);
// final ButtonItem searchBtn = clientUtils.createSearchItem();
// {
// searchBtn.setStartRow(false);
// searchBtn.addClickHandler(new ClickHandler() {
//
// @Override
// public void onClick(ClickEvent event) {
// cardItem.setRequired(true);
// if(idTypeItem != null || idNoItem != null || telPhoneItem != null){
// idTypeItem.clearValue();
// idNoItem.clearValue();
// telPhoneItem.clearValue();
// }
// searchForm.submit();
// }
//
// });
// }
//
// searchForm.setFields(cardItem, currencyItem, searchBtn);
//
// searchForm.addSubmitValuesHandler(new SubmitValuesHandler() {
// @Override
// public void onSubmitValues(SubmitValuesEvent event) {
// if(searchForm.validate()) {
//
// submitCardNo = UIUtil.getCardNo(cardItem);
// currencyCode = currencyItem.getValueAsString();
// submitTelPhone = null;
// submitIdType = null;
// submitIdNo = null;
// RPCTemplate.call(new RPCExecutor<List<Map<String, Serializable>>>() {
// @Override
// public void execute(AsyncCallback<List<Map<String, Serializable>>> callback)
// {
// server.getAcctList(submitCardNo, submitIdType, submitIdNo, submitTelPhone,
// currencyCode, callback);
// }
//
// @Override
// public void onSuccess(List<Map<String, Serializable>> result) {
// updateAcctLst(result);
// }
//
// }, searchForm);
//
// }
// }
// });
// }
//
// return searchForm;
// }
//
// /**
// * 创建查询表单，根据证据类型和证据号码查询账户列表
// * @return
// */
// private YakDynamicForm createSearchFormToCust() {
// final YakDynamicForm searchForm = new YakDynamicForm();
// {
// searchForm.setWidth100();
// searchForm.setNumCols(4);
// searchForm.setColWidths(70, 170, 70, "*");
//
// idTypeItem = uCcsCustomer.IdType().startRow(true).createSelectItem();
// idNoItem = uCcsCustomer.IdNo().createFormItem();
// currencyItem = uiUtil.createCurrencySelectItem();
// currencyItem.setRequired(false);
//
// final ButtonItem searchBtn = clientUtils.createSearchItem();
// {
// searchBtn.setStartRow(false);
// searchBtn.addClickHandler(new ClickHandler() {
//
// @Override
// public void onClick(ClickEvent event) {
// idTypeItem.setRequired(true);
// idNoItem.setRequired(true);
// if(cardItem != null || telPhoneItem != null){
// cardItem.clearValue();
// telPhoneItem.clearValue();
// }
// searchForm.submit();
// }
//
// });
// }
//
// searchForm.setFields(idTypeItem, idNoItem, currencyItem, searchBtn);
//
// searchForm.addSubmitValuesHandler(new SubmitValuesHandler() {
// @Override
// public void onSubmitValues(SubmitValuesEvent event) {
// if(searchForm.validate()) {
//
// submitIdNo = (String) idNoItem.getValue();
// submitIdType = idTypeItem.getValueAsString();
// currencyCode = currencyItem.getValueAsString();
// submitCardNo = null;
// submitTelPhone = null;
// RPCTemplate.call(new RPCExecutor<List<Map<String, Serializable>>>() {
// @Override
// public void execute(AsyncCallback<List<Map<String, Serializable>>> callback)
// {
// server.getAcctList(submitCardNo, submitIdType, submitIdNo, submitTelPhone,
// currencyCode, callback);
// }
//
// @Override
// public void onSuccess(List<Map<String, Serializable>> result) {
// updateAcctLst(result);
// }
//
// }, searchForm);
//
// }
// }
// });
// }
//
// return searchForm;
// }
//
//
// /**
// * 创建查询表单，根据客户手机号码查询账户信息
// * @return
// */
// private YakDynamicForm createSearchFormToTel() {
// final YakDynamicForm searchForm = new YakDynamicForm();
// {
// searchForm.setWidth100();
// searchForm.setNumCols(5);
// searchForm.setColWidths(70, 170, 70, 170,"*");
//
// telPhoneItem = uCcsCustomer.MobileNo().createFormItem();
// currencyItem = uiUtil.createCurrencySelectItem();
// currencyItem.setRequired(false);
//
// final ButtonItem searchBtn = clientUtils.createSearchItem();
// {
// searchBtn.setStartRow(false);
// searchBtn.addClickHandler(new ClickHandler() {
//
// @Override
// public void onClick(ClickEvent event) {
// telPhoneItem.setRequired(true);
// if(idTypeItem != null || idNoItem != null || cardItem != null){
// idTypeItem.clearValue();
// idNoItem.clearValue();
// cardItem.clearValue();
// }
// searchForm.submit();
// }
//
// });
// }
//
// searchForm.setFields(telPhoneItem, currencyItem, searchBtn);
//
// searchForm.addSubmitValuesHandler(new SubmitValuesHandler() {
// @Override
// public void onSubmitValues(SubmitValuesEvent event) {
// if(searchForm.validate()) {
//
// submitTelPhone = (String) telPhoneItem.getValue();
// currencyCode = currencyItem.getValueAsString();
// submitCardNo = null;
// submitIdType = null;
// submitIdNo = null;
// RPCTemplate.call(new RPCExecutor<List<Map<String, Serializable>>>() {
// @Override
// public void execute(AsyncCallback<List<Map<String, Serializable>>> callback)
// {
// server.getAcctList(submitCardNo, submitIdType, submitIdNo, submitTelPhone,
// currencyCode, callback);
// }
//
// @Override
// public void onSuccess(List<Map<String, Serializable>> result) {
// updateAcctLst(result);
// }
//
//
// }, searchForm);
//
// }
// }
// });
// }
//
// return searchForm;
// }
//
// /**
// * 获取账户明细
// * @param result
// */
// private void updateAcctLst(
// List<Map<String, Serializable>> result) {
// for (Map<String, Serializable> map : result) {
// TmAccountKey tmAccountKey = new TmAccountKey();
// TmAccount tmAccount = new TmAccount();
// String pro = (String) map.get("productName");
// IdType idType = (IdType) map.get("idType");
// String idNo = (String) map.get("idNo");
// BigDecimal realOtb = (BigDecimal) map.get(CPSServProConstants.KEY_REAL_OTB);
// BigDecimal cashLimit = (BigDecimal)
// map.get(CPSServProConstants.KEY_CASH_LIMIT);
// tmAccount.updateFromMap(map);
// tmAccountKey.setAcctNo(tmAccount.getAcctNo());
// tmAccountKey.setAcctType(tmAccount.getAcctType());
// tmAccountMap.put(tmAccountKey, tmAccount);
// maps.put("productName", pro);
// maps.put("idType", idType);
// maps.put("idNo", idNo);
// maps.put(CPSServProConstants.KEY_REAL_OTB, realOtb);
// maps.put(CPSServProConstants.KEY_CASH_LIMIT, cashLimit);
// }
// T3002Page.this.updateAcctListGrid(tmAccountMap.values());
// }
//
// private void updateAcctListGrid(Collection<TmAccount> tmAccountList) {
// ListGridRecord[] acctRecords;
// if(null == tmAccountList || tmAccountList.isEmpty()) {
// listGrid.setData(new ListGridRecord[]{});
// return;
// }
//
// acctRecords = new ListGridRecord[tmAccountList.size()];
// int i = 0;
// for(TmAccount tmAccount : tmAccountList) {
// ListGridRecord record = makeAcctListGridRecord(tmAccount);
// acctRecords[i] = record;
// i++;
// }
//
// listGrid.setData(acctRecords);
//
// }
//
// /**
// * 创建账户列表的记录对象
// * @param tmAccount
// * @return
// */
// private ListGridRecord makeAcctListGridRecord(TmAccount tmAccount) {
// ListGridRecord record = new ListGridRecord();
// record.setAttribute(TmAccount.P_AcctNo, tmAccount.getAcctNo());
// record.setAttribute(TmAccount.P_AcctType, tmAccount.getAcctType());
// record.setAttribute(TmAccount.P_CurrCd, tmAccount.getCurrCd());
// record.setAttribute(TmAccount.P_DefaultLogicalCardNo,
// tmAccount.getDefaultLogicalCardNo());
// record.setAttribute(tmAccount.P_Name, tmAccount.getName());
// record.setAttribute(TmAccount.P_ProductCd, tmAccount.getProductCd());
// record.setAttribute(TmAccount.P_CustId, tmAccount.getCustId());
// record.setAttribute(TmAccount.P_OwningBranch, tmAccount.getOwningBranch());
//
// return record;
// }
// /**
// * 创建账户详细信息(批量)显示的表单
// * @return
// */
// private YakDynamicForm createTmAccountForm() {
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
// detailForm.setNumCols(4);
// detailForm.setWidth100();
// detailForm.setHeight100();
// detailForm.setColWidths(150, 200, 150, "*");
// detailForm.setOverflow(Overflow.AUTO);
// detailForm.setTitleWidth(150);
// // form.setIsGroup(true);
// // form.setGroupTitle(constants.acctBatchInfoTitle());
//
//
// StaticTextItem realOtb =new StaticTextItem(
// CPSServProConstants.KEY_REAL_OTB, constants.realOtb());
//
// StaticTextItem cashLimit =new StaticTextItem(
// CPSServProConstants.KEY_CASH_LIMIT, constants.cashLimit());
//
// StaticTextItem productNameItem =new StaticTextItem(
// "productName", constants.productName());
//
//
// detailForm.setFields(
//
// uCcsAcct.AcctNo().asLabel().hidden().createFormItem(),
// uCcsAcct.AcctType().asLabel().hidden().createFormItem(),
//
// uCcsAcct.DefaultLogicalCardNo().asLabel().createFormItem(),
// uCcsAcct.OwningBranch().asLabel().createFormItem(),
//
// uCcsAcct.Name().asLabel().createFormItem(),
// uCcsAcct.BillingCycle().asLabel().createFormItem(),
// //证件类型
// uCcsCustomer.IdType().asLabel().createFormItem(),
// uCcsAcct.StmtFlag().asLabel().createFormItem(),
//
//
// //证件号码
// uCcsCustomer.IdNo().asLabel().createFormItem(),
// uCcsAcct.StmtMailAddrInd().asLabel().createFormItem(),
//
// uCcsAcct.CurrCd().asLabel().createFormItem(),
// uCcsAcct.StmtMediaType().asLabel().createFormItem(),
//
// // uCcsAcct.ProductCd().asLabel().createFormItem(), //产品名称
// productNameItem,
// uCcsAcct.BlockCode().asLabel().createFormItem(),
//
// uCcsAcct.CreditLimit().asLabel().createFormItem(),
// uCcsAcct.AgeCd().asLabel().createFormItem(),
//
// uCcsAcct.WaiveOvlfeeInd().required().createFormItem(),
// uCcsAcct.WaiveCardfeeInd().required().createFormItem(),
//
// uCcsAcct.WaiveLatefeeInd().required().createFormItem(),
// uCcsAcct.WaiveSvcfeeInd().required().createFormItem(),
//
// uCcsAcct.TempLimit().asLabel().createFormItem(),
// uCcsAcct.UnmatchDb().asLabel().createFormItem(),
//
// uCcsAcct.TempLimitBeginDate().asLabel().createFormItem(),
// uCcsAcct.UnmatchCash().asLabel().createFormItem(),
//
// uCcsAcct.TempLimitEndDate().asLabel().createFormItem(),
// uCcsAcct.UnmatchCr().asLabel().createFormItem(),
//
// uCcsAcct.CashLimitRt().asLabel().createFormItem(),
// uCcsAcct.DualBillingFlag().asLabel().createFormItem(),
//
//
// uCcsAcct.OvrlmtRate().asLabel().createFormItem(),
// uCcsAcct.LastPmtAmt().asLabel().createFormItem(),
//
// uCcsAcct.CurrBal().asLabel().createFormItem(),
// uCcsAcct.LastStmtDate().asLabel().createFormItem(),
//
// uCcsAcct.CashBal().asLabel().createFormItem(),
// uCcsAcct.NextStmtDate().asLabel().createFormItem(),
//
// uCcsAcct.PrincipalBal().asLabel().createFormItem(),
// uCcsAcct.PmtDueDate().asLabel().createFormItem(),
//
// uCcsAcct.LoanBal().asLabel().createFormItem(),
// uCcsAcct.DdDate().asLabel().createFormItem(),
//
// uCcsAcct.DisputeAmt().asLabel().createFormItem(),
// uCcsAcct.GraceDate().asLabel().createFormItem(),
//
// uCcsAcct.BeginBal().asLabel().createFormItem(),
// uCcsAcct.ClosedDate().asLabel().createFormItem(),
//
// uCcsAcct.PmtDueDayBal().asLabel().createFormItem(),
// uCcsAcct.FirstStmtDate().asLabel().createFormItem(),
//
// uCcsAcct.QualGraceBal().asLabel().createFormItem(),
// uCcsAcct.CancelDate().asLabel().createFormItem(),
//
// uCcsAcct.GraceDaysFullInd().asLabel().createFormItem(),
// uCcsAcct.ChargeOffDate().asLabel().createFormItem(),
//
// //账户层取现额度
// cashLimit,
// uCcsAcct.FirstPurchaseDate().asLabel().createFormItem(),
//
// //综合信用额度
// realOtb,
// uCcsAcct.FirstPurchaseAmt().asLabel().createFormItem(),
//
// uCcsAcct.SetupDate().asLabel().createFormItem(),
// uCcsAcct.TotDueAmt().asLabel().createFormItem()
// );
//
// return detailForm;
// }
// }
