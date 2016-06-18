package com.sunline.ccs.ui.client.pages.avibalance;

import java.util.LinkedHashMap;

import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnAdjLog;
import com.sunline.ccs.ui.client.commons.CommonKylinForm;
import com.sunline.ccs.ui.client.commons.CommonSelectItemWrapper;
import com.sunline.ccs.ui.client.commons.CommonUiUtils;
import com.sunline.ccs.ui.client.commons.CommonVerticalPanel;
import com.sunline.kylin.web.ark.client.helper.ColumnHelper;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ppy.api.CcServProConstants;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.event.client.IClickEventListener;

/**
 * @see 类名：AvailableBalance
 * @see 描述：可用额度查询. 显示客户卡片层、账户层、客户层的可用额度。
 *
 * @see 创建日期： 2015年6月29日上午10:27:03
 * @author songyanchao
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Singleton
public class AvailableBalance extends Page {

	@Inject
	private AvailableBalanceConstants constants;

	@Inject
	private UCcsCardO uCcsCardO;

	private CommonVerticalPanel mainWindow;

	private CommonVerticalPanel formPanel;

	private ScrollPanel topFormPanel;

	private ScrollPanel middleFormPanel;

	private ScrollPanel bottomFormPanel;

	private KylinForm form;

	private KylinForm topForm;

	private KylinForm middleForm;

	private KylinForm bottomForm;

	@Override
	public void refresh() {
		form.getUi().clear();
		topForm.getUi().clear();
		middleForm.getUi().clear();
		bottomForm.getUi().clear();
	}

	@Override
	public IsWidget createPage() {
		mainWindow = new CommonVerticalPanel();
		form = new CommonKylinForm();
		formPanel = new CommonVerticalPanel();
		form.setField(new ColumnHelper[] {
				new TextColumnHelper(CcsTxnAdjLog.P_CardNbr,
						constants.cardNo(), 50).required(true),
				new TextColumnHelper(CcsTxnAdjLog.P_Currency, constants
						.currType(), 50).asSelectItem().required(true), });
		KylinButton button = ClientUtils
				.createSearchButton(new QueryAvailableAccount());
		form = this.buildSelectItem(form);
		formPanel.setHorizontalAlignment(HasAlignment.ALIGN_LEFT);
		formPanel.add(CommonUiUtils.lineLayoutForm(form, button, null, null));
		formPanel.setWidth("98%");
		mainWindow.add(formPanel);
		topForm = new CommonKylinForm();
		TextColumnHelper cardOtp = new TextColumnHelper(
				CcServProConstants.KEY_OTB, constants.canUseAccount(), 50);
		TextColumnHelper cardCashOtb = new TextColumnHelper(
				CcServProConstants.KEY_CASH_OTB, constants.canCashUseAccount(),
				50);
		TextColumnHelper cardCtdOtb = new TextColumnHelper(
				CcServProConstants.KEY_CASH_OTB, constants.canCashUseAccount(),
				50);
		TextColumnHelper cardCtdCashOt = new TextColumnHelper(
				CcServProConstants.KEY_CARD_CTD_CASH_OTB,
				constants.cardCtdCashOtbTitle(), 50);
		TextColumnHelper cardCtdNetOtb = new TextColumnHelper(
				CcServProConstants.KEY_CARD_CTD_NET_OTB,
				constants.cardCtdNetOtbTitle(), 50);
		TextColumnHelper cardDayATMOtb = new TextColumnHelper(
				CcServProConstants.KEY_CARD_DAY_ATM_OTB,
				constants.cardDayATMOtbTitle(), 50);
		TextColumnHelper cardDayAtmOpenNbr = new TextColumnHelper(
				CcServProConstants.KEY_DAY_ATM_OPEN_NBR,
				constants.cardDayAtmOpenNbrTitle(), 50);
		TextColumnHelper accountComprehensiveLimit = new TextColumnHelper(
				CcServProConstants.KEY_REAL_OTB,
				constants.accountComprehensiveLimit(), 50);
		StackPanel stkTopPanel = new StackPanel();
		stkTopPanel.setHeight("200px");
		stkTopPanel.setWidth("98%");
		topFormPanel = new ScrollPanel();
		topFormPanel.setWidth("100%");
		topFormPanel.setHeight("200px");
		topForm.setField(new ColumnHelper[] {
				uCcsCardO.CycleRetailLmt().readonly(true),
				uCcsCardO.TxnLmt().readonly(true),
				uCcsCardO.CycleCashLmt().readonly(true),
				uCcsCardO.TxnCashLmt().readonly(true),
				uCcsCardO.CycleNetLmt().readonly(true),
				uCcsCardO.TxnNetLmt().readonly(true), cardOtp.readonly(true),
				cardCashOtb.readonly(true), cardCtdOtb.readonly(true),
				cardCtdCashOt.readonly(true), cardCtdNetOtb.readonly(true),
				cardDayATMOtb.readonly(true), cardDayAtmOpenNbr.readonly(true),
				accountComprehensiveLimit.readonly(true) });
		topFormPanel.add(topForm);
		topForm.setCol(3);
		middleForm = new CommonKylinForm();
		StackPanel midPanel = new StackPanel();
		midPanel.setWidth("98%");
		midPanel.setHeight("200px");
		middleFormPanel = new ScrollPanel();
		middleFormPanel.setWidth("100%");
		middleFormPanel.setHeight("200px");
		middleForm.setField(new ColumnHelper[] {
				uCcsCardO.DayUsedAtmNbr().readonly(true),
				uCcsCardO.DayUsedAtmAmt().readonly(true),
				uCcsCardO.DayUsedRetailNbr().readonly(true),
				uCcsCardO.DayUsedRetailAmt().readonly(true),
				uCcsCardO.DayUsedCashNbr().readonly(true),
				uCcsCardO.DayUsedCashAmt().readonly(true),
				uCcsCardO.DayUsedXfroutNbr().readonly(true),
				uCcsCardO.DayUsedXfroutAmt().readonly(true),
				uCcsCardO.CtdUsedAmt().readonly(true),
				uCcsCardO.CtdCashAmt().readonly(true),
				uCcsCardO.CtdNetAmt().readonly(true) });
		middleFormPanel.add(middleForm);
		middleForm.setCol(3);
		TextColumnHelper accoutOtb = new TextColumnHelper(
				CcServProConstants.KEY_OTB, constants.canUseAccount(), 50);
		TextColumnHelper accountCashOtb = new TextColumnHelper(
				CcServProConstants.KEY_CASH_OTB, constants.canCashUseAccount(),
				50);
		TextColumnHelper accoutExtraPayment = new TextColumnHelper(
				CcServProConstants.KEY_DRPOSITE_CASHOTB,
				constants.accoutExtraPayment(), 50);
		TextColumnHelper cashLimitItem = new TextColumnHelper(
				CcServProConstants.KEY_CASH_LIMIT, constants.labelCashLimit(),
				50);
		TextColumnHelper loanLimitItem = new TextColumnHelper(
				CcServProConstants.KEY_LOAN_LIMIT, constants.labelLoanLimit(),
				50);
		bottomForm = new CommonKylinForm();
		StackPanel btmPanel = new StackPanel();
		btmPanel.setWidth("98%");
		btmPanel.setHeight("200px");
		bottomFormPanel = new ScrollPanel();
		bottomFormPanel.setWidth("100%");
		bottomForm.setField(new ColumnHelper[] { accoutOtb.readonly(true),
				accountCashOtb.readonly(true),
				accoutExtraPayment.readonly(true),
				cashLimitItem.readonly(true), loanLimitItem.readonly(true) });
		bottomFormPanel.add(bottomForm);
		stkTopPanel.add(topFormPanel, constants.cardOTBTitle());
		mainWindow.add(stkTopPanel);
		midPanel.add(middleFormPanel, constants.cardUsedInfoTitle());
		mainWindow.add(midPanel);
		btmPanel.add(bottomFormPanel, constants.acctOTBTitle());
		mainWindow.add(btmPanel);
		return mainWindow;
	}

	public KylinForm buildSelectItem(final KylinForm target) {
		LinkedHashMap<String, String> values = new LinkedHashMap<String, String>();
		values.put("156", "人民币");
		values.put("804", "美元");
		return CommonSelectItemWrapper.getInstance().wrapper(
				target,
				new TextColumnHelper(CcsTxnAdjLog.P_Currency, constants
						.currType(), 50), values);
	}

	class QueryAvailableAccount implements IClickEventListener {

		@Override
		public void onClick() {
			if (form.valid()) {
				// 清空表单
				topForm.getUi().clear();
				middleForm.getUi().clear();
				bottomForm.getUi().clear();
				// 清空表单
				callRemoteServer(
						"rpc/availableBalanceServer/getAcctCardCustInfo",
						new RpcCallback<Data>() {
							@Override
							public void onSuccess(Data result) {
								topForm.setFormData(result);
								middleForm.setFormData(result);
								bottomForm.setFormData(result);
							}
						},
						new Object[] {
								form.getSubmitData().asMapData()
										.getString(CcsTxnAdjLog.P_CardNbr),
								form.getSubmitData().asMapData()
										.getString(CcsTxnAdjLog.P_Currency) });
			}
		}
	}

	private void callRemoteServer(String url, RpcCallback<Data> callback, Object... params) {
		RPC.ajax(url, callback, params);
	}

	// private FormItem cardItem;
	// private String cardNo;
	// private String currencyCode;
	// private SelectItem currencyItem;
	// private YakDynamicForm cardForm;
	// private YakDynamicForm cardUsedForm;
	// private YakDynamicForm acctForm;
	// private YakDynamicForm custForm;
	// private ButtonItem btnSearch;
	// //
	// // @Override
	// // public void updateView(CustomerContext context) {
	// // currentContext = context;
	// // uiUtil.updateCardSelectItem(cardItem, context);
	// // cardForm.clearValues();
	// // cardUsedForm.clearValues();
	// // acctForm.clearValues();
	// // custForm.clearValues();
	// // }
	// //
	// public T1502Page() {
	// super(PAGE_ID, true, CPSAppAuthority.T1502);
	// }
	//
	// @Override
	// protected void createCanvas() {
	// VLayout container = new VLayout(6);
	// {
	// container.setWidth100();
	// container.setHeight100();
	// }
	//
	// DynamicForm searchForm = this.createSearchForm();
	// {
	// searchForm.setIsGroup(true);
	// searchForm.setGroupTitle(constants.searchFormTitle());
	// }
	// container.addMember(searchForm);
	//
	// final SectionStack sectionStack = new SectionStack();
	// {
	// sectionStack.setVisibilityMode(VisibilityMode.MULTIPLE);
	// sectionStack.setWidth100();
	// sectionStack.setHeight100();
	// }
	//
	// SectionStackSection cardSection = new
	// SectionStackSection(constants.cardOTBTitle());
	// {
	// cardSection.setExpanded(true);
	//
	// //卡片层可用额度信息
	// cardForm = new YakDynamicForm();
	// {
	// cardForm.setNumCols(4);
	// cardForm.setWidth100();
	// cardForm.setTitleWidth(150);
	// // StaticTextItem cardOtb = new
	// StaticTextItem(CcServProConstants.KEY_OTB,constants.canUseAccount());
	// // StaticTextItem cardCashOtb = new
	// StaticTextItem(CcServProConstants.KEY_CASH_OTB,constants.canCashUseAccount());
	//
	// StaticTextItem cardCtdOtb = new
	// StaticTextItem(CcServProConstants.KEY_CARD_CTD_OTB,constants.cardCtdOtbTitle());
	// StaticTextItem cardCtdCashOt = new
	// StaticTextItem(CcServProConstants.KEY_CARD_CTD_CASH_OTB,constants.cardCtdCashOtbTitle());
	// StaticTextItem cardCtdNetOtb = new
	// StaticTextItem(CcServProConstants.KEY_CARD_CTD_NET_OTB,constants.cardCtdNetOtbTitle());
	// StaticTextItem cardDayATMOtb = new
	// StaticTextItem(CcServProConstants.KEY_CARD_DAY_ATM_OTB,constants.cardDayATMOtbTitle());
	// StaticTextItem cardDayAtmOpenNbr = new
	// StaticTextItem(CcServProConstants.KEY_DAY_ATM_OPEN_NBR,constants.cardDayAtmOpenNbrTitle());
	// // StaticTextItem cardDayCashOtb = new
	// StaticTextItem(CcServProConstants.KEY_CARD_DAY_CASH_OTB,constants.cardDayCashOtbTitle());
	// // StaticTextItem cardDayCashOpenNbr = new
	// StaticTextItem(CcServProConstants.KEY_CARD_DAY_CASH_OPEN_NBR,constants.cardDayCashOpenNbrTitle());
	// // StaticTextItem cardDayRetailOtb = new
	// StaticTextItem(CcServProConstants.KEY_CARD_DAY_RETAIL_OTB,constants.cardDayRetailOtbTitle());
	// // StaticTextItem cardDayRetailOtbNbr = new
	// StaticTextItem(CcServProConstants.KEY_CARD_DAY_RETAIL_OPEN_NBR,
	// // constants.cardDayRetailOtbNbrTitle());
	// // StaticTextItem cardDayXfroutOtb = new
	// StaticTextItem(CcServProConstants.KEY_CARD_DAY_XFROUT_OTB,constants.cardDayXfroutOtbTitle());
	// // StaticTextItem cardDayXfroutOpenNbr = new
	// StaticTextItem(CcServProConstants.KEY_CARD_DAY_XFROUT_OPEN_NBR,
	// // constants.cardDayXfroutOpenNbrTitle());
	//
	// StaticTextItem accountComprehensiveLimit =new
	// StaticTextItem(CcServProConstants.KEY_REAL_OTB,
	// constants.accountComprehensiveLimit());
	//
	// cardForm.setFields(
	// // cardOtb,
	// // cardCashOtb,
	// uTmCardO.CycleLimit().asLabel().createFormItem(),
	// uTmCardO.TxnLimit().asLabel().createFormItem(),
	// uTmCardO.CycleCashLimit().asLabel().createFormItem(),
	// uTmCardO.TxnCashLimit().asLabel().createFormItem(),
	// uTmCardO.CycleNetLimit().asLabel().createFormItem(),
	// uTmCardO.TxnNetLimit().asLabel().createFormItem(),
	// cardCtdCashOt,
	// cardCtdNetOtb,
	// accountComprehensiveLimit,
	// cardCtdOtb,
	// cardDayATMOtb,
	// cardDayAtmOpenNbr
	// // cardDayCashOtb,
	// // cardDayCashOpenNbr
	// // cardDayRetailOtb,
	// // cardDayRetailOtbNbr,
	// // cardDayXfroutOtb,
	// // cardDayXfroutOpenNbr
	// );
	// }
	// cardSection.addItem(cardForm);
	//
	// }
	// sectionStack.addSection(cardSection);
	//
	// //卡片层已用额度笔数信息
	// SectionStackSection cardUsedSection = new
	// SectionStackSection(constants.cardUsedInfoTitle());
	// {
	// cardUsedSection.setExpanded(true);
	//
	// //卡片层已用额度笔数信息
	// cardUsedForm = new YakDynamicForm();
	// {
	// cardUsedForm.setNumCols(4);
	// cardUsedForm.setWidth100();
	// cardUsedForm.setTitleWidth(150);
	//
	// cardUsedForm.setFields(
	// uTmCardO.DayUsedAtmNbr().asLabel().createFormItem(),
	// uTmCardO.DayUsedAtmAmt().asLabel().createFormItem(),
	// uTmCardO.DayUsedRetailNbr().asLabel().createFormItem(),
	// uTmCardO.DayUsedRetailAmt().asLabel().createFormItem(),
	// uTmCardO.DayUsedCashNbr().asLabel().createFormItem(),
	// uTmCardO.DayUsedCashAmt().asLabel().createFormItem(),
	// uTmCardO.DayUsedXfroutNbr().asLabel().createFormItem(),
	// uTmCardO.DayUsedXfroutAmt().asLabel().createFormItem(),
	// uTmCardO.CtdUsedAmt().asLabel().createFormItem(),
	// uTmCardO.CtdCashAmt().asLabel().createFormItem(),
	// uTmCardO.CtdNetRetlAmt().asLabel().createFormItem()
	// );
	// }
	// cardUsedSection.addItem(cardUsedForm);
	//
	// }
	// sectionStack.addSection(cardUsedSection);
	//
	// SectionStackSection acctSection = new
	// SectionStackSection(constants.acctOTBTitle());
	// {
	// acctSection.setExpanded(true);
	//
	// //账户层可用额度信息
	// acctForm = new YakDynamicForm();
	// {
	// acctForm.setNumCols(4);
	// acctForm.setWidth100();
	// acctForm.setTitleWidth(150);
	// StaticTextItem accoutOtb =new StaticTextItem(
	// CcServProConstants.KEY_OTB, constants.canUseAccount());
	// StaticTextItem accountCashOtb =new StaticTextItem(
	// CcServProConstants.KEY_CASH_OTB, constants.canCashUseAccount());
	//
	// StaticTextItem accoutExtraPayment =new StaticTextItem(
	// CcServProConstants.KEY_DRPOSITE_CASHOTB, constants.accoutExtraPayment());
	//
	// StaticTextItem cashLimitItem = new
	// StaticTextItem(CcServProConstants.KEY_CASH_LIMIT,
	// constants.labelCashLimit());
	// StaticTextItem loanLimitItem = new
	// StaticTextItem(CcServProConstants.KEY_LOAN_LIMIT,
	// constants.labelLoanLimit());
	//
	//
	// acctForm.setFields(
	// uTmAccount.AcctType().asLabel().createFormItem(),
	// accoutOtb,
	// accountCashOtb,
	// uTmAccount.CreditLimit().asLabel().createFormItem(),
	// accoutExtraPayment,
	// uTmAccount.TempLimit().asLabel().createFormItem(),
	// cashLimitItem,
	// loanLimitItem,
	// uTmAccount.TempLimitBeginDate().asLabel().createFormItem(),
	// uTmAccount.TempLimitEndDate().asLabel().createFormItem(),
	// uTmAccount.CurrBal().asLabel().createFormItem(),
	// uTmAccount.LoanBal().asLabel().createFormItem(),
	// uTmAccount.DisputeAmt().asLabel().createFormItem(),
	// uTmAccount.PointBal().asLabel().createFormItem(),
	// uTmAccount.BillingCycle().asLabel().createFormItem(),
	// uTmAccount.PmtDueDate().asLabel().createFormItem(),
	// uTmAccount.TotDueAmt().asLabel().createFormItem(),
	// uTmAccount.CtdCashAmt().asLabel().createFormItem(),
	// uTmAccount.CtdRetailAmt().asLabel().createFormItem(),
	// uTmAccount.CtdPaymentAmt().asLabel().createFormItem()
	// );
	// }
	//
	// acctSection.addItem(acctForm);
	// }
	// sectionStack.addSection(acctSection);
	// SectionStackSection custSection = new
	// SectionStackSection(constants.custOTBTitle());
	// {
	// custSection.setExpanded(true);
	//
	// //客户层可用额度信息
	// custForm = new YakDynamicForm();
	// {
	// custForm.setNumCols(4);
	// custForm.setWidth100();
	// custForm.setTitleWidth(150);
	// custForm.setOverflow(Overflow.AUTO);
	//
	// StaticTextItem custOtb = new StaticTextItem();
	// custOtb.setTitle(constants.canUseAccount());
	// custOtb.setName("CUST_OTB");
	//
	// //客户层可用取现额度和可用额度取一样的值
	// StaticTextItem custCashOtb = new StaticTextItem();
	// custCashOtb.setTitle(constants.canCashUseAccount());
	// custCashOtb.setName("CUST_CASH_OTB");
	// custForm.setFields(
	// uTmCustomer.CustId().asLabel().createFormItem(),
	// uTmCustomer.IdNo().asLabel().createFormItem(),
	// uTmCustomer.IdType().asLabel().createFormItem(),
	//
	// uTmCustomer.Name().asLabel().createFormItem(),
	// //
	// uTmCustLimitO.LimitType().asLabel().startRow(true).endRow(true).colSpan(3).createFormItem(),
	// // uTmCustLimitO.CreditLimit().asLabel().createFormItem(),
	// // uTmCustLimitO.LimitCategory().asLabel().createFormItem,
	// // uTmCustLimitO.CreditLimitRmb().asLabel().createFormItem(),
	// // uTmCustLimitO.CreditLimitUsd().asLabel().createFormItem(),
	// custOtb,
	// custCashOtb
	// // uTmCustLimitO.LoanLimit().asLabel().createFormItem(),
	// // uTmCustLimitO.BalloonLimit().asLabel().createFormItem(),
	// //
	// // uTmCustLimitO.OtherLimit1().asLabel().createFormItem(),
	// // uTmCustLimitO.OtherLimit2().asLabel().createFormItem(),
	// // uTmCustLimitO.OtherLimit3().asLabel().createFormItem()
	// );
	// }
	//
	// custSection.addItem(custForm);
	// }
	// sectionStack.addSection(custSection);
	// container.addMember(sectionStack);
	// addMember(container);
	// }
	// /**
	// // * 卡片层额度信息显示
	// //// */
	// // private void fillValuesInCardForm(TmCardO cssTmCardO) {
	// // cardForm.setValues(cssTmCardO.convertToMap());
	// // }
	// ////
	// //// /**
	// //// * 卡片层已用额度笔数信息
	// //// */
	// // private void fillValuesInCardUsedForm(CssTmCardO cssTmCardO) {
	// // cardUsedForm.setValues(cssTmCardO.convertToMap());
	// // }
	// // private void fillValuesInCardUsedForm(TmCardO cssTmCardO) {
	// // cardUsedForm.setValues(cssTmCardO.convertToMap());
	// //}
	// // /**
	// // * 卡号改变则联动改变账户余额的显示信息
	// // */
	// // private void fillValuesInAcctForm(TmCardO cssTmAcct, TmAccount
	// tmAccount) {
	// // Map<String,Serializable> values = cssTmAcct.convertToMap();
	// // values.putAll(tmAccount.convertToMap());
	// // acctForm.setValues(values);
	// // }
	// //
	// // /**
	// // * 客户层信息显示
	// // */
	// // private void fillValuesInCustForm(TmCustomer tmCustomer){
	// // custForm.setValues(tmCustomer.convertToMap());
	// //
	// // }
	// /**
	// * 卡片层额度信息显示
	// */
	// private void fillValuesInCardForm(Map<String,Serializable> map) {
	// cardForm.setValues(map);
	// }
	//
	// /**
	// * 卡片层已用额度笔数信息
	// */
	// private void fillValuesInCardUsedForm(Map<String,Serializable> map) {
	// cardUsedForm.setValues(map);
	// }
	//
	// /**
	// * 卡号改变则联动改变账户余额的显示信息
	// */
	// private void fillValuesInAcctForm(Map<String, Serializable> map) {
	//
	// acctForm.setValues(map);
	// }
	//
	// /**
	// * 客户层信息显示
	// */
	// private void fillValuesInCustForm(Map<String, Serializable> map){
	// custForm.setValues(map);
	// }
	// /**
	// * 创建额度查询表单
	// * @return
	// */
	// private YakDynamicForm createSearchForm() {
	// final YakDynamicForm searchForm = new YakDynamicForm();
	// {
	// searchForm.setNumCols(4);
	// searchForm.setWidth100();
	// cardItem = uiUtil.createCardNoItem();
	// currencyItem = uiUtil.createCurrencySelectItem();
	// btnSearch = new ButtonItem(constants.searchButton());
	// btnSearch.addClickHandler(new ClickHandler() {
	//
	// @Override
	// public void onClick(ClickEvent event) {
	// if(searchForm.validate()) {
	// cardNo = UIUtil.getCardNo(cardItem);
	// currencyCode = currencyItem.getValueAsString();
	// if(StringUtils.isNotBlank(cardNo) &&
	// StringUtils.isNotBlank(currencyCode)){
	// cardForm.clearValues();
	// cardUsedForm.clearValues();
	// acctForm.clearValues();
	// custForm.clearValues();
	// RPCTemplate.call(new RPCExecutor<List<Map<String, Serializable>>>() {
	// @Override
	// public void execute(AsyncCallback<List<Map<String, Serializable>>>
	// callback) {
	// server.getAcctCardCustInfo(cardNo, currencyCode, callback);
	// }
	//
	// @Override
	// public void onSuccess(List<Map<String, Serializable>> result) {
	// //clientUtils.showSuccess();
	// // TmAccount tmAccount = (TmAccount)
	// result.get(PublicConst.P_TM_ACCOUNT);
	// // TmCardO tmCardO = (TmCardO) result.get(PublicConst.P_TM_CARD_O);
	// // TmCustomer customer = (TmCustomer)
	// result.get(PublicConst.P_TM_CUSTOMER);
	// // CssTmAccountO cssTmAccountO = (CssTmAccountO)
	// result.get(PublicConst.P_CSS_TM_ACCOUNT_O);
	// // CssTmCardO cssTmCardO = (CssTmCardO)
	// result.get(PublicConst.P_CSS_TM_CARD_O);
	// //账户信息
	// Map<String, Serializable> acctInfoMap = result.get(0);
	//
	// //客户信息
	// Map<String, Serializable> custInfoMap = result.get(1);
	//
	// //卡片信息
	// Map<String, Serializable> cardInfoMap = result.get(2);
	//
	// if(acctInfoMap.get(TmAccount.P_AcctNo) == null &&
	// acctInfoMap.get(TmAccount.P_AcctType) == null){
	// clientUtils.showWarning(constants.msgGetAccountTypeInfoFail());
	// }else{
	// // //卡片改变联动卡片层信息变动
	// fillValuesInCardForm(cardInfoMap);
	// fillValuesInCardUsedForm(cardInfoMap);
	// //卡号改变则联动改变账户余额的显示信息
	// fillValuesInAcctForm(acctInfoMap);
	// //卡号变动则联动改变客户账号余额信息显示
	// Map<String,Serializable> map = new HashMap<String,
	// Serializable>(acctInfoMap);
	// map.putAll(custInfoMap);
	// fillValuesInCustForm(map);
	//
	//
	// }
	// }
	//
	// }, btnSearch);
	// }
	// }
	// }
	// });
	// searchForm.setFields(cardItem,currencyItem,btnSearch);
	// }
	// return searchForm;
	// }
	//
	//
	// @Override
	// public void updateView(ParamMapToken token) {
	// //清空表单緩存
	// cardForm.clearValues();
	//
	// cardUsedForm.clearValues();
	//
	// acctForm.clearValues();
	//
	// custForm.clearValues();
	// }
	//
	// @Override
	// public String getTitle() {
	//
	// return constants.pageTitle();
	// }
	//
	// @Override
	// public ImageResource getIcon() {
	//
	// return null;
	// }

}
