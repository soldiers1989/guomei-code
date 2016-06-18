/**
 * 
 */
package com.sunline.ccs.ui.client.pages.badaccount;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsAcct;
import com.sunline.ccs.infrastructure.client.ui.UCcsAcctO;
import com.sunline.ccs.infrastructure.client.ui.UCcsCardLmMapping;
import com.sunline.ccs.infrastructure.client.ui.UCcsCustomer;
import com.sunline.ccs.infrastructure.client.ui.UCcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.ccs.ui.client.commons.CommonUiUtils;
import com.sunline.ccs.ui.shared.PublicConst;
import com.sunline.kylin.web.ark.client.helper.ColumnHelper;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.EventObjectHandler;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.IDblClickRowEventListener;
import com.sunline.ui.grid.client.listener.ISelectRowEventListener;
import com.sunline.ui.tab.client.Tab;
import com.sunline.ui.tab.client.TabItemSetting;
import com.sunline.ui.tab.client.TabSetting;

/**
 * 呆账核销
 * 按照卡号查询账户信息，显示账户列表、账户的信用计划列表、账户详细信息、信用计划的详细信息等。
* @author yeyu
 *
 */
@Singleton
public class BadAccountCancelExamination extends Page {
	
	@Inject
	private UCcsAcct uCcsAcct;
	@Inject
	private UCcsCustomer uCcsCustomer;
	@Inject
	private UCcsAcctO uCcsAcctO;
	@Inject
	private UCcsPlan uCcsPlan;
	@Inject
	private UCcsCardLmMapping uCcsCardLmMediaMap;
	
	@Inject
	private BadAccountCancelExaminationConstants constants;

	private VerticalPanel mainWindow;
	
	private KylinForm queryForm1;
	
	private KylinForm queryForm2;
	
	private KylinForm queryForm3;
	
	private HorizontalPanel splitPanel;
	
	private VerticalPanel buttonGroup;
	
	private final KylinGrid acctList = new KylinGrid();
	
	private KylinGrid creditList;
	
	private Tab tab;
	
	private KylinForm acctDetailForm;
	
	private KylinForm authDetailForm;
	
	private KylinForm creditPlanForm;
	
	private KylinForm loanPlanForm;
	
	@Override
	public void refresh() {
		queryForm1.getUi().clear();
		queryForm2.getUi().clear();
		queryForm3.getUi().clear();
		acctList.clearData();
		creditList.clearData();
		acctDetailForm.getUi().clear();
		authDetailForm.getUi().clear();
		creditPlanForm.getUi().clear();
		loanPlanForm.getUi().clear();
	}

	@Override
	public IsWidget createPage() {
		
		mainWindow = new VerticalPanel();
		//构造请求表单
		queryForm1 = new KylinForm();
		queryForm1.setField(new ColumnHelper[]{
			uCcsCardLmMediaMap.CardNbr()
		});
		queryForm2 = new KylinForm();
		queryForm2.setField(new ColumnHelper[]{
			uCcsCustomer.IdType().asSelectItem(SelectType.KEY_LABLE),
			uCcsCustomer.IdNo()
		});
		queryForm3 = new KylinForm();
		queryForm3.setField(new ColumnHelper[]{
			uCcsCustomer.MobileNo()
		});
		splitPanel = new HorizontalPanel();
		splitPanel.setHeight("190px");
		splitPanel.setWidth("98%");
		acctList.setHeight("190px");
		creditList = new KylinGrid();
		creditList.setHeight("190px");
		buttonGroup = new VerticalPanel();
		KylinButton searchBtn = new KylinButton("获取信用计划","skins/icons/search.gif");
		searchBtn.addClickEventListener(new FetchCreditPlan());
		searchBtn.updateWidth(80);
		KylinButton badDebtBtn = new KylinButton("呆账核销","skins/icons/edit.gif");
		badDebtBtn.addClickEventListener(new ShowBadDebtTab());
		badDebtBtn.updateWidth(80);
		buttonGroup.add(searchBtn);
		buttonGroup.add(badDebtBtn);
		acctList.loadDataFromUrl("rpc/t3309Server/getAcctList");
		acctList.getSetting().delayLoad(true);
		splitPanel.add(this.initAcctList(acctList));
		splitPanel.add(buttonGroup);
		splitPanel.add(this.initPlanList(creditList));
		
		mainWindow.add(CommonUiUtils.lineLayoutForm(queryForm1, ClientUtils.createSearchButton(new SubmitFormEvent(queryForm1)), null, null));
		mainWindow.add(CommonUiUtils.lineLayoutForm(queryForm2, ClientUtils.createSearchButton(new SubmitFormEvent(queryForm2)), null, null));
		mainWindow.add(CommonUiUtils.lineLayoutForm(queryForm3, ClientUtils.createSearchButton(new SubmitFormEvent(queryForm3)), null, null));
		mainWindow.add(splitPanel);
		
		mainWindow.add(this.initTabPanel());
		return mainWindow;
	}
	
	/**
	 * 
	 * @see 方法名：initTabPanel 
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：Jun 25, 20152:50:34 PM
	 * @author yeyu
	 *  
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private Tab initTabPanel(){
		tab = new Tab(new TabSetting());
		TabItemSetting tabAcctDetail = new TabItemSetting("AcctDetail", constants.titleAcctDetail());
		TabItemSetting tabAcctLockCodeDetail = new TabItemSetting("AcctLockCodeDetail", constants.titleAcctLockCodeDetail());
		TabItemSetting tabAuthDetail = new TabItemSetting("AuthDetail", constants.titleAuthDetail());
		TabItemSetting tabBadAcctCheckRequire = new TabItemSetting("BadAcctCheckRequire", constants.titleBadAcctCheckRequire());
		TabItemSetting tabCreditPlanDetail = new TabItemSetting("CreditPlanDetail", constants.titleCreditPlanDetail());
		tab.setWidth("98%");
		tab.addItem(tabAcctDetail, this.createAcctDetailsPanel());
		tab.addItem(tabAcctLockCodeDetail, this.getAcctLockCodeDetailPanel());
		tab.addItem(tabAuthDetail, this.getAuthDetailPanel());
		tab.addItem(tabCreditPlanDetail, this.getCreditPlanDetailPanel());
		tab.addItem(tabBadAcctCheckRequire, this.getBadDebtsPanel());
		return tab;
	}
	
	/**
	 * 
	 * @see 方法名：getAcctLockCodeDetailPanel 
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：Jun 25, 20153:00:39 PM
	 * @author yeyu
	 *  
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private ScrollPanel getAcctLockCodeDetailPanel(){
		ScrollPanel acctLockcodePanel = new ScrollPanel();
		acctLockcodePanel.setWidth("100%");
		acctLockcodePanel.setHeight("190px");
		acctLockcodePanel.add(this.buildBlockcodeListGrid());
		return acctLockcodePanel;
	}
	
	/**
	 * 
	 * @see 方法名：getBadDetbsPanel 
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：Jun 25, 20153:35:07 PM
	 * @author yeyu
	 *  
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private ScrollPanel getBadDebtsPanel(){
		ScrollPanel badDebtsPanel = new ScrollPanel();
		VerticalPanel vBadDebtsPanel = new VerticalPanel();
		badDebtsPanel.setWidth("100%");
		badDebtsPanel.setHeight("190px");
		KylinGrid badDebtsGrid = this.buildToWriteOffBadDebtsGrid();
		vBadDebtsPanel.add(badDebtsGrid);
		vBadDebtsPanel.add(this.buildAdjForm());
		badDebtsPanel.add(vBadDebtsPanel);
		return badDebtsPanel;
	}
	
	/**
	 * 
	 * @see 方法名：createAcctDetailsPanel 
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：Jun 25, 20152:50:29 PM
	 * @author Liming.Feng
	 *  
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public ScrollPanel createAcctDetailsPanel () {
		ScrollPanel acctDetailPanel = new ScrollPanel();
		acctDetailPanel.setHeight("190px");
		acctDetailForm = new KylinForm();
		//TODO
		acctDetailForm.setField(new ColumnHelper[]{
				uCcsAcct.Org().readonly(true),
				uCcsAcct.AcctNbr().readonly(true),
				uCcsAcct.AcctType().readonly(true),
				uCcsAcct.CustId().readonly(true),
				uCcsAcct.CustLmtId().readonly(true),
				uCcsAcct.ProductCd().readonly(true),
				uCcsAcct.DefaultLogicCardNbr().readonly(true),
				uCcsAcct.Currency().readonly(true),
				uCcsAcct.CreditLmt().readonly(true),
				uCcsAcct.TempLmt().readonly(true),
				uCcsAcct.TempLmtBegDate().readonly(true),
				uCcsAcct.TempLmtEndDate().readonly(true),
				uCcsAcct.CashLmtRate().readonly(true),
				uCcsAcct.OvrlmtRate().readonly(true),
				uCcsAcct.LoanLmtRate().readonly(true),
				uCcsAcct.CurrBal().readonly(true),
				uCcsAcct.CashBal().readonly(true),
				uCcsAcct.PrincipalBal().readonly(true),
				uCcsAcct.LoanBal().readonly(true),
				uCcsAcct.DisputeAmt().readonly(true),
				uCcsAcct.BegBal().readonly(true),
				uCcsAcct.PmtDueDayBal().readonly(true),
				uCcsAcct.QualGraceBal().readonly(true),
				uCcsAcct.GraceDaysFullInd().readonly(true),
				uCcsAcct.PointsBegBal().readonly(true),
				uCcsAcct.CtdPoints().readonly(true),
				uCcsAcct.CtdSpendPoints().readonly(true),
				uCcsAcct.CtdAdjPoints().readonly(true),
				uCcsAcct.PointsBal().readonly(true),
				uCcsAcct.SetupDate().readonly(true),
				uCcsAcct.DormentDate().readonly(true),
				uCcsAcct.EactiveDate().readonly(true),
				uCcsAcct.OvrlmtDate().readonly(true),
				uCcsAcct.OvrlmtNbrOfCyc().readonly(true),
				uCcsAcct.Name().readonly(true),
				uCcsAcct.Gender().readonly(true),
				uCcsAcct.OwningBranch().readonly(true),
				uCcsAcct.MobileNo().readonly(true),
				uCcsAcct.CycleDay().readonly(true),
				uCcsAcct.CorpName().readonly(true),
				uCcsAcct.StmtFlag().readonly(true),
				uCcsAcct.StmtMailAddrInd().readonly(true),
				uCcsAcct.StmtMediaType().readonly(true),
				uCcsAcct.StmtCountryCode().readonly(true),
				uCcsAcct.StmtState().readonly(true),
				uCcsAcct.StmtCity().readonly(true),
				uCcsAcct.StmtDistrict().readonly(true),
				uCcsAcct.StmtPostcode().readonly(true),
				uCcsAcct.StmtAddress().readonly(true),
				uCcsAcct.Email().readonly(true),
				uCcsAcct.BlockCode().readonly(true),
				uCcsAcct.DualBillingFlag().readonly(true),
				uCcsAcct.AgeCode().readonly(true),
				uCcsAcct.MemoDb().readonly(true),
				uCcsAcct.MemoCash().readonly(true),
				uCcsAcct.MemoCr().readonly(true),
				uCcsAcct.DdInd().readonly(true),
				uCcsAcct.DdBankName().readonly(true),
				uCcsAcct.DdBankBranch().readonly(true),
				uCcsAcct.DdBankAcctNbr().readonly(true),
				uCcsAcct.DdBankAcctName().readonly(true),
				uCcsAcct.LastDdAmt().readonly(true),
				uCcsAcct.LastDdDate().readonly(true),
				uCcsAcct.LastPmtAmt().readonly(true),
				uCcsAcct.LastPmtDate().readonly(true),
				uCcsAcct.LastStmtDate().readonly(true),
				uCcsAcct.LastPmtDueDate().readonly(true),
				uCcsAcct.LastAgingDate().readonly(true),
				uCcsAcct.CollectInDate().readonly(true),
				uCcsAcct.CollectOutDate().readonly(true),
				uCcsAcct.NextStmtDate().readonly(true),
				uCcsAcct.PmtDueDate().readonly(true),
				uCcsAcct.DdDate().readonly(true),
				uCcsAcct.GraceDate().readonly(true),
				uCcsAcct.DualBillingDate().readonly(true),
				uCcsAcct.ClosedDate().readonly(true),
				uCcsAcct.FirstStmtDate().readonly(true),
				uCcsAcct.CloseDate().readonly(true),
				uCcsAcct.ChargeOffDate().readonly(true),
				uCcsAcct.FirstRetlDate().readonly(true),
				uCcsAcct.FirstRetlAmt().readonly(true),
				uCcsAcct.TotDueAmt().readonly(true),
				uCcsAcct.CurrDueAmt().readonly(true),
				uCcsAcct.PastDueAmt1().readonly(true),
				uCcsAcct.PastDueAmt2().readonly(true),
				uCcsAcct.PastDueAmt3().readonly(true),
				uCcsAcct.PastDueAmt4().readonly(true),
				uCcsAcct.PastDueAmt5().readonly(true),
				uCcsAcct.PastDueAmt6().readonly(true),
				uCcsAcct.PastDueAmt7().readonly(true),
				uCcsAcct.PastDueAmt8().readonly(true),
				uCcsAcct.CtdCashAmt().readonly(true),
				uCcsAcct.CtdCashCnt().readonly(true),
				uCcsAcct.CtdRetailAmt().readonly(true),
				uCcsAcct.CtdRetailCnt().readonly(true),
				uCcsAcct.CtdRepayAmt().readonly(true),
				uCcsAcct.CtdRepayCnt().readonly(true),
				uCcsAcct.CtdDbAdjAmt().readonly(true),
				uCcsAcct.CtdDbAdjCnt().readonly(true),
				uCcsAcct.CtdCrAdjAmt().readonly(true),
				uCcsAcct.CtdCrAdjCnt().readonly(true),
				uCcsAcct.CtdFeeAmt().readonly(true),
				uCcsAcct.CtdFeeCnt().readonly(true),
				uCcsAcct.CtdInterestAmt().readonly(true),
				uCcsAcct.CtdInterestCnt().readonly(true),
				uCcsAcct.CtdRefundAmt().readonly(true),
				uCcsAcct.CtdRefundCnt().readonly(true),
				uCcsAcct.CtdMaxOvrlmtAmt().readonly(true),
				uCcsAcct.MtdRetailAmt().readonly(true),
				uCcsAcct.MtdRetailCnt().readonly(true),
				uCcsAcct.MtdCashAmt().readonly(true),
				uCcsAcct.MtdCashCnt().readonly(true),
				uCcsAcct.MtdRefundAmt().readonly(true),
				uCcsAcct.MtdRefundCnt().readonly(true),
				uCcsAcct.YtdRetailAmt().readonly(true),
				uCcsAcct.YtdRetailCnt().readonly(true),
				uCcsAcct.YtdCashAmt().readonly(true),
				uCcsAcct.YtdCashCnt().readonly(true),
				uCcsAcct.YtdRefundAmt().readonly(true),
				uCcsAcct.YtdRefundCnt().readonly(true),
				uCcsAcct.YtdOvrlmtFeeAmt().readonly(true),
				uCcsAcct.YtdOvrlmtFeeCnt().readonly(true),
				uCcsAcct.YtdLateFeeAmt().readonly(true),
				uCcsAcct.YtdLateFeeCnt().readonly(true),
				uCcsAcct.LtdRetailAmt().readonly(true),
				uCcsAcct.LtdRetailCnt().readonly(true),
				uCcsAcct.LtdCashAmt().readonly(true),
				uCcsAcct.LtdCashCnt().readonly(true),
				uCcsAcct.LtdRefundAmt().readonly(true),
				uCcsAcct.LtdRefundCnt().readonly(true),
				uCcsAcct.LtdHighestPrin().readonly(true),
				uCcsAcct.LtdHighestCrBal().readonly(true),
				uCcsAcct.LtdHighestBal().readonly(true),
				uCcsAcct.CollectCnt().readonly(true),
				uCcsAcct.Collector().readonly(true),
				uCcsAcct.CollectReason().readonly(true),
				uCcsAcct.AgeHst().readonly(true),
				uCcsAcct.PaymentHst().readonly(true),
				uCcsAcct.WaiveOvlfeeInd().readonly(true),
				uCcsAcct.WaiveCardfeeInd().readonly(true),
				uCcsAcct.WaiveLatefeeInd().readonly(true),
				uCcsAcct.WaiveSvcfeeInd().readonly(true),
				uCcsAcct.UserCode1().readonly(true),
				uCcsAcct.UserCode2().readonly(true),
				uCcsAcct.UserCode3().readonly(true),
				uCcsAcct.UserCode4().readonly(true),
				uCcsAcct.UserCode5().readonly(true),
				uCcsAcct.UserCode6().readonly(true),
				uCcsAcct.UserDate1().readonly(true),
				uCcsAcct.UserDate2().readonly(true),
				uCcsAcct.UserDate3().readonly(true),
				uCcsAcct.UserDate4().readonly(true),
				uCcsAcct.UserDate5().readonly(true),
				uCcsAcct.UserDate6().readonly(true),
				uCcsAcct.UserNumber1().readonly(true),
				uCcsAcct.UserNumber2().readonly(true),
				uCcsAcct.UserNumber3().readonly(true),
				uCcsAcct.UserNumber4().readonly(true),
				uCcsAcct.UserNumber5().readonly(true),
				uCcsAcct.UserNumber6().readonly(true),
				uCcsAcct.UserField1().readonly(true),
				uCcsAcct.UserField2().readonly(true),
				uCcsAcct.UserField3().readonly(true),
				uCcsAcct.UserField4().readonly(true),
				uCcsAcct.UserField5().readonly(true),
				uCcsAcct.UserField6().readonly(true),
				uCcsAcct.UserAmt1().readonly(true),
				uCcsAcct.UserAmt2().readonly(true),
				uCcsAcct.UserAmt3().readonly(true),
				uCcsAcct.UserAmt4().readonly(true),
				uCcsAcct.UserAmt5().readonly(true),
				uCcsAcct.UserAmt6().readonly(true),
				uCcsAcct.MtdPaymentAmt().readonly(true),
				uCcsAcct.MtdPaymentCnt().readonly(true),
				uCcsAcct.YtdRepayAmt().readonly(true),
				uCcsAcct.YtdRepayCnt().readonly(true)
		});
		acctDetailForm.getSetting().labelWidth(140);
		acctDetailPanel.add(acctDetailForm);
		return acctDetailPanel;
	}
	
	/**
	 * 
	 * @see 类名：FetchCreditPlan
	 * @see 描述：TODO 中文描述
	 *
	 * @see 创建日期：   Jun 25, 20152:50:23 PM
	 * @author Liming.Feng
	 * 
	 * @see 修改记录：
	 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
	 */
	class FetchCreditPlan implements IClickEventListener {

		@Override
		public void onClick() {
			MapData selectedItem = acctList.getGrid().getSelectedRow();
			creditPlanForm.getUi().clear();
			if(selectedItem.getJsData() == null){
				Dialog.alertWarn(StringUtils.EMPTY , constants.titleWarning());
			}else{
				RPC.ajax("rpc/t3309Server/getCreditList", new RpcCallback<Data>(){

					@Override
					public void onSuccess(Data result) {
						creditList.loadData(result);
					}

					@Override
					public void onFailure(Throwable caught) {
						Dialog.alertError(StringUtils.EMPTY, caught.getMessage());
					}
					
				}, new Object[]{
						selectedItem.getString(CcsPlan.P_AcctNbr),
						selectedItem.getString(CcsPlan.P_AcctType)
				});
			}
		}
		
	}
	
	class ShowBadDebtTab implements IClickEventListener {

		@Override
		public void onClick() {
		    // FIXME
//		    tab.selectTabItem("BadAcctCheckRequire");
		    tab.selectTabItem("tabitem5");
		}
		
	}
	
	/**
	 * 
	 * @see 方法名：initAcctList 
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：Jun 25, 20152:50:18 PM
	 * @author Liming.Feng
	 *  
	 * @param grid
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private KylinGrid initAcctList(KylinGrid grid){
	    	grid.setWidth("100%");
		grid.setColumns(new ColumnHelper[]{
			uCcsAcct.AcctNbr(),
			uCcsAcct.AcctType(),
			uCcsAcct.ProductCd(),
			uCcsAcct.BlockCode(),
			uCcsAcct.Currency(),
			uCcsAcct.OwningBranch()
		});
		grid.getSetting().onSelectRow(new ISelectRowEventListener() {

		    @Override
		    public void selectRow(MapData rowdata, String rowid, EventObjectHandler rowobj) {
			tab.selectTabItem("tabitem1");
			acctDetailForm.getUi().clear();
			acctDetailForm.getUi().setData(rowdata);
			badDebtsGrid.loadData(rowdata.getData(BadAccountCancelExaminationConstants.TOWRITEOFFBADDEBTS));
			blockGrid.loadData(rowdata.getData(PublicConst.KEY_BLOCKCODE_LIST));
			String bjAndFy = rowdata.getString(BadAccountCancelExaminationConstants.BJANDFY);
			adjForm.setFieldValue(BadAccountCancelExaminationConstants.PRINCIPALBALANCE, bjAndFy.split("--")[0]);
			adjForm.setFieldValue(BadAccountCancelExaminationConstants.COSTBALANCE, bjAndFy.split("--")[1]);
			authDetailForm.setFormData(rowdata);
			if (AccountType.E.toString().equals(rowdata.getString(CcsAcct.P_AcctType))) {
			    loanPlanForm.setVisible(true);
			}
		    }
		    
		});
//		grid.addDblClickListener(new AcctGridDBClickEvent());
		return grid;
	}
	
	/**
	 * 
	 * @see 类名：AcctGridDBClickEvent
	 * @see 描述：TODO 中文描述
	 *
	 * @see 创建日期：   Jun 25, 20152:50:15 PM
	 * @author Liming.Feng
	 * 
	 * @see 修改记录：
	 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
	 */
	class AcctGridDBClickEvent implements IDblClickRowEventListener {

		@Override
		public void onDblClickRow(MapData data, String rowid,EventObjectHandler row) {
			acctDetailForm.getUi().clear();
			acctDetailForm.getUi().setData(data);
		}
		
	}
	
	/**
	 * 
	 * @see 方法名：initPlanList 
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：Jun 25, 20152:50:08 PM
	 * @author Liming.Feng
	 *  
	 * @param grid
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private KylinGrid initPlanList(KylinGrid grid){
	    	grid.setWidth("100%");
		grid.setColumns(new ColumnHelper[]{
				uCcsPlan.PlanId(),
        		uCcsPlan.PlanNbr(),
        		uCcsPlan.PlanType(),
        		uCcsPlan.AcctNbr(),
        		uCcsPlan.AcctType()
		});
		grid.getSetting().onSelectRow(new ISelectRowEventListener() {

		    @Override
		    public void selectRow(MapData rowdata, String rowid, EventObjectHandler rowobj) {
			tab.selectTabItem("tabitem4");
			creditPlanForm.getUi().clear();
			loanPlanForm.getUi().clear();
			creditPlanForm.setFormData(rowdata);
			loanPlanForm.setFormData(rowdata);
		    }
		    
		});
		return grid;
	}
	
	/**
	 * 
	 * @see 类名：SubmitFormEvent
	 * @see 描述：TODO 中文描述
	 *
	 * @see 创建日期：   Jun 25, 20152:50:12 PM
	 * @author Liming.Feng
	 * 
	 * @see 修改记录：
	 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
	 */
	class SubmitFormEvent implements IClickEventListener {

	    private KylinForm _form;
		@Override
		public void onClick() {
			adjForm.getUi().clear();
			authDetailForm.getUi().clear();
			blockGrid.clearData();
			badDebtsGrid.clearData();
			creditList.clearData();
			creditPlanForm.getUi().clear();
			acctDetailForm.getUi().clear();
			acctList.loadData(_form);
		}
		/** 
		 * Creates a new instance of SubmitFormEvent. 
		 * 
		 * @param _form 
		 */
		public SubmitFormEvent(KylinForm _form) {
		    this._form = _form;
		}
	}

//	public static final String PAGE_ID = "risk-3309";
//	
//	@Inject
//	private T3309Constants constants;
//	@Inject
//	private T3309InterAsync server;
//	@Inject
//	private ClientUtils clientUtils;
//	@Inject
//	private UTmAccount uCcsAcct;
//	@Inject
//	private UTmCustomer uCcsCustomer;
//	@Inject
//	private UTmAccountO uTmAccountO;
//	@Inject
//	private UTmPlan uCcsPlan;
//	@Inject
//	private UTmCardMediaMap uCcsCardLmMediaMap;
//	@Inject
//	private UIUtil uiUtil;
//	
//	private ListGrid acctListGrid;
//	private ListGrid planListGrid;
//	private FormItem cardItem;
//	private SelectItem idTypeItem;
//	private FormItem idNoItem;
//	private FormItem telPhoneItem;
//	private YakDynamicForm tmAccountDetailForm;
//	private YakDynamicForm tmAccountODetailForm;
//	
////	private Map<String, YakDynamicForm> forms;
//	
//	private YakDynamicForm planForm;
//	private YakDynamicForm loanPlanForm;
//	private ListGrid blockcodeListGrid; 
//	private ListGrid toWriteOffBadDebtsListGrid;
//	private FormItem selectedCardNoItem;
//	private FormItem selectedCardAcctNameItem;
//	private YakDynamicForm cardForm;
//	
//	private TabSet detailTabSet;
//	private Tab tmAccountDetailTab;
//	private Tab acctAuthDetailTab;
//	private Tab planDetailTab;
//	private Tab blockcodeDetailTab;
//	private Tab toWriteOffBadDebtsTab;//呆账核销tab
//	private Tab accountInfoAlterTab;
//	
//	private IButton fetchPlanListBtn;//获取信用计划>>
//	private IButton toWriteOffBadDebts;//呆账核销
//	
//	private String submitCardNo;
//	private String submitIdType;
//	private String submitIdNo;
//	private String submitTelPhone;
//	private Integer selectedAcctNo;
//	private AccountType selectedAcctType;
////	-------------------
//	private String selectedCurrCd;
//	private String selectedOwningBranch;
//	
//	private ListGridField  blockCode;
//	private ListGridField description;
//	
//	private YakDynamicForm transForm; // 呆账核销
//	
//	private FormItem principalBalanceItem;
//	private FormItem writeOffAmoutForBJItem;
//	private FormItem costBalanceItem;
//	private FormItem writeOffAmoutForFyItem;
//	
//	private Map<TmAccountKey, Map<String, String>> blockCodeList = new HashMap<TmAccountKey, Map<String, String>>();//锁定码的信息
//	private Map<TmAccountKey, List<GlTxnAdj>> toWriteOffBadDebtsList = new HashMap<TmAccountKey, List<GlTxnAdj>>();//选中账户呆账核销按钮
//	private Map<TmAccountKey, String> bjAndFyList = new HashMap<TmAccountKey, String>();//本金余额和费用余额
//	
//	//保存卡片的所有账户记录
//	private Map<TmAccountKey, TmAccount> tmAccountMap = new HashMap<TmAccountKey, TmAccount>();
//	private Map<TmAccountOKey, TmAccountO> tmAccountOMap = new HashMap<TmAccountOKey, TmAccountO>();
//	//保存账户的所有信用计划记录
//	private Map<Integer, CcsPlan> tmPlanMap = new HashMap<Integer, CcsPlan>();
//	
//	public T3309Page()
//	{
//		super(PAGE_ID, true, CPSAppAuthority.T3309);
//	}
//
//	@Override
//	protected void createCanvas() {
//		setMembersMargin(4);
//		
//		final SectionStack sectionStack = new SectionStack();
//		{
//			sectionStack.setVisibilityMode(VisibilityMode.MULTIPLE);
//			sectionStack.setWidth100();
//			sectionStack.setHeight100();
//		}
//		
//		
//		//信用计划列表查询区域
//		SectionStackSection gridSection = new SectionStackSection(constants.sectionTitleAcctList());
//		{
//			gridSection.setExpanded(true);
//
//			cardForm = new YakDynamicForm();
//			{
//				cardForm.setNumCols(4);
//				cardForm.setWidth(600);
//				cardForm.setTitleWidth(40);
//
//				selectedCardNoItem = uCcsCardLmMediaMap.CardNo();
//				selectedCardAcctNameItem = uCcsAcct.Name();
//				cardForm.setFields(selectedCardNoItem, selectedCardAcctNameItem);
//			}
//			gridSection.setControls(cardForm);
//			
//			HLayout hLayout = new HLayout(4);
//			hLayout.setWidth100();
//			hLayout.setHeight100();
//			
//			acctListGrid = createAcctListGrid();
//			
//			hLayout.addMember(acctListGrid);
//			
//			VLayout vLayout = new VLayout(4);
//			
//			//增加获取信用计划按钮
//			fetchPlanListBtn = ClientUtils.createIButton(constants.btnTitleFetchPlanList(), null);
//			fetchPlanListBtn.setLayoutAlign(VerticalAlignment.CENTER);
//			fetchPlanListBtn.setLayoutAlign(Alignment.CENTER);
//			fetchPlanListBtn.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
//
//				@Override
//				public void onClick(
//						com.smartgwt.client.widgets.events.ClickEvent event) {
//					if(acctListGrid.getSelectedRecord() == null) {
//						clientUtils.showWarning(constants.msgSelectNoAcct());
//						return;
//					}
//					
//					loadPlanListFromServer();
//				}
//
//			});
//			vLayout.addMember(fetchPlanListBtn);
////			hLayout.addMember(fetchPlanListBtn);
//			//增加呆账核销按钮 
//			toWriteOffBadDebts = ClientUtils.createIButton(constants.toWriteOffBadDebts(), null);
//			toWriteOffBadDebts.setLayoutAlign(VerticalAlignment.CENTER);
//			toWriteOffBadDebts.setLayoutAlign(Alignment.CENTER);
//			toWriteOffBadDebts.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
//
//				@Override
//				public void onClick(
//						com.smartgwt.client.widgets.events.ClickEvent event) {
//					if(acctListGrid.getSelectedRecord() == null) {
//						clientUtils.showWarning(constants.msgSelectNoAcct());
//						return;
//					}
//					showWindowsForToWriteOffBadDebts();
//				}
//
//			});
////			hLayout.addMember(toWriteOffBadDebts);
//			vLayout.addMember(toWriteOffBadDebts);
//			hLayout.addMember(vLayout);
//			
//			planListGrid = createPlanListGrid();
//			
//			hLayout.addMember(planListGrid);
//			
//			gridSection.addItem(hLayout);
//		}
//		sectionStack.addSection(gridSection);
//		//信用计划详细信息的显示区域
//		SectionStackSection planSection = new SectionStackSection(constants.sectionTitleAcctDetail());   
//		{
//			planSection.setExpanded(true);
//			
//			VLayout vLayout = new VLayout();
//			vLayout.setWidth100();
//			vLayout.setHeight100();
//			
//			detailTabSet = createDetailInfoTabSet();
//			
//			vLayout.addMember(detailTabSet);
//			
//			planSection.addItem(vLayout);
//			
//		}
//		sectionStack.addSection(planSection);
//		
//		addMember(createSearchForm());
//		addMember(createSearchFormToCust());
//		addMember(createSearchFormToTel());
//		addMember(sectionStack);
//	}
//
//	/**
//	 * 创建账户详细信息和信用计划详细信息显示的TabSet
//	 * @return
//	 */
//	private TabSet createDetailInfoTabSet() {
//		TabSet mainTabSet = new TabSet();
//		mainTabSet.setWidth100();
//		mainTabSet.setHeight100();
//		
//		tmAccountDetailForm = createTmAccountForm();
//		tmAccountODetailForm = createTmAccountOForm();
////		accountInfoAlterForm = createTmAccountAlterForm();
//		planForm = createPlanForm();
//		blockcodeListGrid = createBlockcodeListGrid();
//		
//		tmAccountDetailTab = new Tab(constants.acctBatchInfoTitle());
//		{
//			tmAccountDetailTab.setPane(tmAccountDetailForm);
//		}
//		mainTabSet.addTab(tmAccountDetailTab);
//		
//		acctAuthDetailTab = new Tab(constants.acctAuthInfoTitle());
//		{
//			acctAuthDetailTab.setPane(tmAccountODetailForm);
//		}
//		mainTabSet.addTab(acctAuthDetailTab);
//		
//		planDetailTab = new Tab(constants.planInfoTitle());
//		{
//			planDetailTab.setPane(planForm);
////			planDetailTab.setPane(loanPlanForm);
//		}
//		mainTabSet.addTab(planDetailTab);
//		
//		blockcodeDetailTab = new Tab(constants.blockcodeTitle());//账户锁定码页面
//		{
//			blockcodeDetailTab.setPane(blockcodeListGrid);
//		}
//		mainTabSet.addTab(blockcodeDetailTab);
//		
//		toWriteOffBadDebtsTab = new Tab(constants.toWriteOffBadDebtsApply());//呆账核销页面
//		{
//			toWriteOffBadDebtsTab.setPane(createToWriteOffBadDebts());
////			q
//		}
//		mainTabSet.addTab(toWriteOffBadDebtsTab);
//		
//		return mainTabSet;
//	}
//	/**
//	 * 设置呆账核销页面
//	 * @return
//	 */
//	private Layout createToWriteOffBadDebts(){
//		VLayout layout = new VLayout();
//		layout.setWidth100();
//		layout.setHeight100();
//		
//		toWriteOffBadDebtsListGrid = createToWriteOffBadDebtsListGrid();//显示呆账核销页面
//
//		layout.addMember(toWriteOffBadDebtsListGrid);
//		
//		
//		transForm = new YakDynamicForm(){
//			@Override
//			public boolean validate() {
//				String writeOffAmoutForBJ = writeOffAmoutForBJItem.getValue()==null||"".equals(writeOffAmoutForBJItem.getValue())?"0":(String) writeOffAmoutForBJItem.getValue();
//				String writeOffAmoutForFy = writeOffAmoutForFyItem.getValue()==null||"".equals(writeOffAmoutForFyItem.getValue())?"0":(String) writeOffAmoutForFyItem.getValue();
//				System.out.println(writeOffAmoutForBJ+"()"+writeOffAmoutForFy+"()"+(new BigDecimal(writeOffAmoutForBJ)).compareTo(BigDecimal.ZERO)+"()"+(new BigDecimal(writeOffAmoutForFy)).compareTo(BigDecimal.ZERO));
////				if(((writeOffAmoutForBJ.length()!=0&& StringUtils.isNumeric(writeOffAmoutForBJ) &&Integer.parseInt(writeOffAmoutForBJ)>0)&&
////					(writeOffAmoutForFy.length()!=0&& StringUtils.isNumeric(writeOffAmoutForFy) &&Integer.parseInt(writeOffAmoutForFy)>0))
////					||
////					((writeOffAmoutForBJ.length()!=0&& StringUtils.isNumeric(writeOffAmoutForBJ) &&Integer.parseInt(writeOffAmoutForBJ)>0)&&
////					(writeOffAmoutForFy.length()==0))
////					||
////					((writeOffAmoutForFy.length()!=0&& StringUtils.isNumeric(writeOffAmoutForFy) &&Integer.parseInt(writeOffAmoutForFy)>0)&&
////					(writeOffAmoutForBJ.length()==0))
////					){
//				
////				if((!"0".equals(writeOffAmoutForBJ)&& (new BigDecimal(writeOffAmoutForBJ)).compareTo(BigDecimal.ZERO)==1 &&
////						!"0".equals(writeOffAmoutForFy)&& (new BigDecimal(writeOffAmoutForFy)).compareTo(BigDecimal.ZERO)==1)
////						||
////						(!"0".equals(writeOffAmoutForBJ)&& new BigDecimal(writeOffAmoutForBJ).compareTo(BigDecimal.ZERO)==1&&
////						"0".equals(writeOffAmoutForFy))
////						||
////						(!"0".equals(writeOffAmoutForFy)&& new BigDecimal(writeOffAmoutForFy).compareTo(BigDecimal.ZERO)==1&&
////						"0".equals(writeOffAmoutForBJ))
////						){
////					return true;
////				}else{
////					clientUtils.showWarning(constants.judgePrincipalBalanceOrCostBalance());
////					return false;
////				}
//				if("0".equals(writeOffAmoutForBJ) && "0".equals(writeOffAmoutForFy)){
//					clientUtils.showWarning(constants.judgePrincipalBalanceOrCostBalance());
//					return false;
//				}else{
////					if((new BigDecimal(writeOffAmoutForBJ)).compareTo(BigDecimal.ZERO)!=1 || (new BigDecimal(writeOffAmoutForFy)).compareTo(BigDecimal.ZERO)!=1){
////						clientUtils.showWarning(constants.judgePrincipalBalanceOrCostBalance());
////						return false;
////					}
//					if((new BigDecimal(writeOffAmoutForBJ)).compareTo(BigDecimal.ZERO)==-1 || (new BigDecimal(writeOffAmoutForFy)).compareTo(BigDecimal.ZERO)==-1){
//						clientUtils.showWarning(constants.judgePrincipalBalanceOrCostBalance());
//						return false;
//					}
//				}
//				return true;
//				
//				
//			}
//		};
	//TODO
	private KylinForm adjForm;
	private KylinForm buildAdjForm(){
		adjForm = new KylinForm();
		adjForm.setCol(2);
		adjForm.setField(new ColumnHelper[]{
				new TextColumnHelper(BadAccountCancelExaminationConstants.PRINCIPALBALANCE, constants.principalBalance(), 50).readonly(true),
				new TextColumnHelper("writeOffAmout1", constants.writeOffAmout(), 50),
				new TextColumnHelper(BadAccountCancelExaminationConstants.COSTBALANCE, constants.costBalance(), 50).readonly(true),
				new TextColumnHelper("writeOffAmout2", constants.writeOffAmout(), 50)
		});
		KylinButton btnSubmit = new KylinButton("提交", null);
		btnSubmit.addClickEventListener(new IClickEventListener(){
			@Override
			public void onClick() {
				MapData customParams = adjForm.getSubmitData().asMapData();
				String writeOffAmoutForBJ = customParams.getString("writeOffAmout1");
				String writeOffAmoutForFy = customParams.getString("writeOffAmout2");
				if (!validate(writeOffAmoutForBJ, writeOffAmoutForFy)) {
				    Dialog.alertWarn(constants.judgePrincipalBalanceOrCostBalance(), constants.titleWarning());
				    return;
				}
				MapData selectedAcct = acctList.getUi().getSelectedRow();
				String selectedAcctNo = selectedAcct.getString(CcsAcct.P_AcctNbr);
				String selectedAcctType = selectedAcct.getString(CcsAcct.P_AcctType);
				String selectedOwningBranch = selectedAcct.getString(CcsAcct.P_OwningBranch);
				String selectedCurrCd = selectedAcct.getString(CcsAcct.P_Currency);
				String selectedCardNo = selectedAcct.getString(CcsAcct.P_DefaultLogicCardNbr);
				RPC.ajax("rpc/t3309Server/saveWriteOffApply",new RpcCallback<Data>(){

					@Override
					public void onSuccess(Data result) {
						Dialog.tipNotice("修改成功", 1000);
						badDebtsGrid.loadData(result);
//						String bjAndFy = result.getString(BadAccountCancelExaminationConstants.BJANDFY);
//						adjForm.setFieldValue(BadAccountCancelExaminationConstants.PRINCIPALBALANCE, bjAndFy.split("-")[0]);
//						adjForm.setFieldValue(BadAccountCancelExaminationConstants.COSTBALANCE, bjAndFy.split("-")[1]);
					}
					
				}, new Object[]{
					SysTxnCd.S65, DbCrInd.C,
						selectedCurrCd, writeOffAmoutForBJ,
						writeOffAmoutForFy, selectedOwningBranch,
						selectedAcctNo, selectedAcctType,
						selectedCardNo
				});
			}
		});
		KylinButton btnClear = new KylinButton("重置", null);
		btnClear.addClickEventListener(new IClickEventListener(){

		    @Override
		    public void onClick() {
			adjForm.setFieldValue("writeOffAmout1", "");
			adjForm.setFieldValue("writeOffAmout2", "");
		    }
		    
		});
		adjForm.addButton(btnSubmit);
		adjForm.addButton(btnClear);
		return adjForm;
	}
	private boolean validate(String writeOffAmoutForBJ, String writeOffAmoutForFy) {
	    writeOffAmoutForBJ = writeOffAmoutForBJ==null||"".equals(writeOffAmoutForBJ)?"0":writeOffAmoutForBJ;
	    writeOffAmoutForFy = writeOffAmoutForFy==null||"".equals(writeOffAmoutForFy)?"0":writeOffAmoutForFy;
	    if("0".equals(writeOffAmoutForBJ) && "0".equals(writeOffAmoutForFy)){
		return false;
	    }else{
		try {
			if((new BigDecimal(writeOffAmoutForBJ)).compareTo(BigDecimal.ZERO)==-1 || (new BigDecimal(writeOffAmoutForFy)).compareTo(BigDecimal.ZERO)==-1){
				return false;
			}
		} catch (NumberFormatException e) {
		    return false;
		}
	    }
	    return true;
	}
	
//		{
//			transForm.setWidth(600);
//			transForm.setHeight(60);
//			transForm.setNumCols(4);
//			transForm.setColWidths(100, 150, 100, "*");
//			
//			principalBalanceItem = new FormItem();//本金余额
//			principalBalanceItem.setName(T3309Constants.PRINCIPALBALANCE);
//			principalBalanceItem.setTitle(constants.principalBalance());
//			principalBalanceItem.setCanEdit(false);
//			
//			writeOffAmoutForBJItem = new FormItem();//核销金额
//			writeOffAmoutForBJItem.setName("writeOffAmout1");
//			writeOffAmoutForBJItem.setTitle(constants.writeOffAmout());
//			writeOffAmoutForBJItem.setCanEdit(true);
////			writeOffAmoutForBJItem.setRequired(true);
//			
//			costBalanceItem = new FormItem();//费用余额
//			costBalanceItem.setName(T3309Constants.COSTBALANCE);
//			costBalanceItem.setTitle(constants.costBalance());
//			costBalanceItem.setCanEdit(false);
//			
//			writeOffAmoutForFyItem = new FormItem();//核销金额
//			writeOffAmoutForFyItem.setName("writeOffAmout2");
//			writeOffAmoutForFyItem.setTitle(constants.writeOffAmout());
//			writeOffAmoutForFyItem.setCanEdit(true);
////			writeOffAmoutForFyItem.setRequired(true);
//			
//			transForm.setFields(principalBalanceItem,writeOffAmoutForBJItem,costBalanceItem,writeOffAmoutForFyItem);
////			transForm.setFields(writeOffAmoutForBJItem,writeOffAmoutForFyItem);
//		}
//		
//		layout.addMember(transForm);
//		
////		final HLayout buttonsLayout = uiUtil.createSubmitClearLayout(transForm);
//		final HLayout buttonsLayout = createSubmitClearLayout(transForm,writeOffAmoutForBJItem,writeOffAmoutForFyItem);
//		
//		
//		buttonsLayout.setWidth(600);
//		buttonsLayout.setLayoutTopMargin(10);
//		layout.addMember(buttonsLayout);
//		
//		//呆账核销表单提交时处理
//		transForm.addSubmitValuesHandler(new SubmitValuesHandler() {
//
//					@Override
//					public void onSubmitValues(SubmitValuesEvent event) {
//						if (transForm.validate()){//验证表单信息
//							SC.confirm(constants.msgWriteOffApply(), new BooleanCallback() {
//								@Override
//								public void execute(Boolean value) {
//									if(value != null && value) {
//										RPCTemplate.call(new RPCExecutor<List<GlTxnAdj>>() {
//											@Override
//											public void execute(AsyncCallback<List<GlTxnAdj>> callback) {
//												System.out
//														.println("-------------------------------------------123");
//												server.saveWriteOffApply(SysTxnCd.S65,DbCrInd.C,selectedCurrCd
////														,writeOffAmoutForBJItem.getValue().toString()
//														,writeOffAmoutForBJItem.getValue()==null||"".equals(writeOffAmoutForBJItem.getValue())?"":(String) writeOffAmoutForBJItem.getValue()
////														,writeOffAmoutForFyItem.getValue().toString()
//														,writeOffAmoutForFyItem.getValue()==null||"".equals(writeOffAmoutForFyItem.getValue())?"":(String) writeOffAmoutForFyItem.getValue()
//														,selectedOwningBranch,
//														selectedAcctNo.toString(),selectedAcctType,selectedCardNoItem.getValue().toString(),callback);
////												server.adjustCreditLimit(
////														UIUtil.getCardNo(cardItem), 
////														acctTypeItem.getValueAsString(),
////														(BigDecimal)newLimitItem.getValue(), callback);
//											}
//											@Override
//											public void onSuccess(List<GlTxnAdj> result) {
////													clientUtils.showSuccess();
////													listGrid.invalidateCache();
//													if(result != null && result.size()>0){
//														setGlTxnAdjList(result);
//														toWriteOffBadDebtsListGrid.fetchData();
//													}
//													toWriteOffBadDebtsListGrid.invalidateCache();
//													clientUtils.showSuccess();
//											}
//										}, buttonsLayout);
//									}
//								}
//							});
//							
//						}
//					
//					}
//				});
//		
//		return layout;
//	}
//	/*
//	 * 设置提交和清除按钮
//	 * 
//	 * */
//	public HLayout createSubmitClearLayout(final DynamicForm form, final FormItem ... clearValueFormItems)
//	{
//		HLayout layout = new HLayout(10);
//		{
//			layout.setWidth100();
//			layout.setAlign(Alignment.CENTER);
//			IButton submitButton = clientUtils.createSubmitButton();
//			{
//				submitButton.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
//					
//					@Override
//					public void onClick(com.smartgwt.client.widgets.events.ClickEvent event) {
//						form.submit();
//					}
//				});
//			}
//			layout.addMember(submitButton);
//			
//			IButton clearButton = clientUtils.createResetButton();
//			{
//				clearButton.setTitle(constants.btnTitleClear());
//				clearButton.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
//					
//					@Override
//					public void onClick(com.smartgwt.client.widgets.events.ClickEvent event) {
////						form.clearValues();
//						for(FormItem item: clearValueFormItems) {
//							item.clearValue();
//						}
//					}
//				});
//			}
//			layout.addMember(clearButton);
//		}
//		return layout;
//	}
////	/**
////	 * 判断是否为数字
////	 * @return
////	 */
////	public boolean isNumeric(String str){ 
////		   Pattern pattern = Pattern.compile("[0-9]*"); 
////		   Matcher isNum = pattern.matcher(str);
////		   if( !isNum.matches() ){
////		       return false; 
////		   } 
////		   return true; 
////		}
//	/**
//	 * 调用窗口显示呆账核销
//	 * @return
//	 */
//	private void showWindowsForToWriteOffBadDebts() {
//		detailTabSet.selectTab(toWriteOffBadDebtsTab);
//		
//		
////		AbstractActionWindow window = T3309Window;
////		//window.setID(makeViewPageId(transCode));
////		window.ensureCanvas();
////		window.setTitle(handler.getPageTitle()+"("+transCode+")");
////		handler.updateView(getCustomerContext());
////		window.show();
////		window.centerInPage();
////		openedWindows.put(transCode, window);
//	}
//	
//	/**
//	 * 创建账户列表（zrx）
//	 * @return
//	 */
//	private ListGrid createAcctListGrid() {
//		final ListGrid listGrid = new ListGrid();
//		listGrid.setShowRecordComponents(true);
//		listGrid.setShowRecordComponentsByCell(true);
//		listGrid.setHeight100();
//		listGrid.setWidth100();
//		listGrid.setRecordComponentPoolingMode(RecordComponentPoolingMode.DATA);
//		listGrid.setSelectionType(SelectionStyle.SINGLE);
//		listGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
////		listGrid.setIsGroup(true);
////		listGrid.setGroupTitle(constants.acctListTitle());
//		
//        listGrid.setFields(
//        		uCcsAcct.AcctNo().width(80).createLGField(),
//				uCcsAcct.AcctType().width("*").createLGField(),
//				uCcsAcct.ProductCd().width(80).createLGField(),
//				uCcsAcct.BlockCode().width(80).createLGField(),
//				uCcsAcct.CurrCd().width(80).createLGField(),
//				uCcsAcct.OwningBranch().width(80).createLGField()
//        );
//        
//		listGrid.setData(new ListGridRecord[]{});
//		
//		listGrid.addSelectionChangedHandler(new SelectionChangedHandler() {
//
//			@Override
//			public void onSelectionChanged(SelectionEvent event) {
//				ListGridRecord record = event.getSelectedRecord();
//				
//				if(null == record) {
//					clearAcctInfo();
//					return;
//				}
//				
//				selectedAcctNo = record.getAttributeAsInt(TmAccount.P_AcctNo);//获得选择的账户编号
//				selectedAcctType = AccountType.valueOf(record.getAttribute(TmAccount.P_AcctType));//获得选择的账户类型
////				zrx
//				selectedCurrCd = record.getAttributeAsString(TmAccount.P_CurrCd);//获得币种
//				selectedOwningBranch = record.getAttribute(TmAccount.P_OwningBranch);//发卡网点
//				//账户编号
//				//账户类型
//				//介质卡号selectedCardNoItem
//				
//				
//				Tab selectedTab = detailTabSet.getSelectedTab();
//				if(selectedTab != tmAccountDetailTab && selectedTab != acctAuthDetailTab
//						&& selectedTab != blockcodeDetailTab && selectedTab != accountInfoAlterTab && selectedTab != toWriteOffBadDebtsTab) {
//					detailTabSet.selectTab(tmAccountDetailTab);
//				}
//				
//				TmAccountKey acctKey = new TmAccountKey();
//				acctKey.setAcctNo(selectedAcctNo);
//				acctKey.setAcctType(selectedAcctType);
//				tmAccountDetailForm.setValues(tmAccountMap.get(acctKey).convertToMap());//账户详细信息
//				
//				if(selectedAcctType.equals(AccountType.E)){
//					loanPlanForm.show();
//				}else{
//					loanPlanForm.hide();
//				}
//				
//				TmAccountOKey acctOKey = new TmAccountOKey();
//				acctOKey.setAcctNo(selectedAcctNo);
//				acctOKey.setAcctType(selectedAcctType);
//				
//				tmAccountODetailForm.setValues(tmAccountOMap.get(acctOKey).convertToMap());//账户授权信息
//				
//				//账户锁定码信息
//				Map<String, String> blockCodes = blockCodeList.get(acctKey);
//				
//				ListGridRecord[] records = new ListGridRecord[blockCodes.size()];
//				
//				int i = 0;
//				for(Entry<String, String> entry : blockCodeList.get(acctKey).entrySet()) {
//					records[i] = new BlockCodeLGRecord(entry.getKey(), entry.getValue());
//					i++;
//				}
//				
//				blockcodeListGrid.setRecords(records);
//				
//				//呆账核销查询页面
////				private Map<TmAccountKey, List<GlTxnAdj>> toWriteOffBadDebtsList = new HashMap<TmAccountKey, List<GlTxnAdj>>();//选中账户呆账核销按钮
//				List<GlTxnAdj> toWriteOffBadDebtsCodes = toWriteOffBadDebtsList.get(acctKey);
//				setGlTxnAdjList(toWriteOffBadDebtsCodes);
//				//本金余额和费用余额
//				String bjAndFyStr = bjAndFyList.get(acctKey);
//				System.out.println(bjAndFyStr);
//				principalBalanceItem.setValue(bjAndFyStr.split("-")[0]);	
//				costBalanceItem.setValue(bjAndFyStr.split("-")[1]);
//			}
//			
//		});
//		
//		return listGrid;
//	}
//	private void setGlTxnAdjList(List<GlTxnAdj> result) {
//		T3309Page.this.updateCardList(result);
//	}
//	/**
//	 * 更新计提列表数据账户
//	 */
//	private void updateCardList(Collection<GlTxnAdj> glTxnAdjLst) {
//		ListGridRecord[] accLitRecords;
//		if(null == glTxnAdjLst || glTxnAdjLst.isEmpty()) {
//			toWriteOffBadDebtsListGrid.setData(new ListGridRecord[]{});
//			return;
//		}
//		acctRecords =  new ListGridRecord[glTxnAdjLst.size()];
//		int i = 0;
//		for(GlTxnAdj glTxnAdj : glTxnAdjLst) {
//			ListGridRecord record = makeCardListGridRecord(glTxnAdj);
//			acctRecords[i] = record;
//			i++;
//		}
//		toWriteOffBadDebtsListGrid.setData(acctRecords);
//	}
//	
//	private ListGridRecord makeCardListGridRecord(GlTxnAdj glTxnAdj) {
//		ListGridRecord record = new ListGridRecord();
//		record.setAttribute(T3309Constants.ADJID, glTxnAdj.getAdjId());//顺序号
//		record.setAttribute(T3309Constants.PROVISIONORREMIT, glTxnAdj.getDbCrInd());
//		//借贷记标志
//		record.setAttribute(T3309Constants.CURRENCY, glTxnAdj.getPostCurrCd());//币种
//		record.setAttribute(T3309Constants.POSTAMT, glTxnAdj.getPostAmt());//金额
//		record.setAttribute(T3309Constants.THECARDNETWORK, glTxnAdj.getOwningBranch());//发卡网点
//		record.setAttribute(T3309Constants.TXNCODE, glTxnAdj.getTxnCode());//交易码
//		record.setAttribute(T3309Constants.AGEGROUP, glTxnAdj.getAgeGroup());//账龄组
//		record.setAttribute(T3309Constants.BUCKETTYPE, glTxnAdj.getBucketType());//余额成分
//		record.setAttribute(T3309Constants.PLANNBR, glTxnAdj.getPlanNbr());//信用计划号
//		record.setAttribute(T3309Constants.POSTGLIND, glTxnAdj.getPostGlInd());//总账入账标志
//		return record;
//	}
//	/**
//	 * 创建信用计划列表(zrx)显示信用计划按钮
//	 * @return
//	 */
//	private ListGrid createPlanListGrid() {
//		final ListGrid listGrid = new ListGrid();
//		listGrid.setShowRecordComponents(true);
//		listGrid.setShowRecordComponentsByCell(true);
//		listGrid.setHeight100();
//		listGrid.setWidth100();
//		listGrid.setRecordComponentPoolingMode(RecordComponentPoolingMode.DATA);
//		listGrid.setSelectionType(SelectionStyle.SINGLE);
//		listGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
////		listGrid.setIsGroup(true);
////		listGrid.setGroupTitle(constants.planListTitle());
//  
//        listGrid.setFields(
//        		uCcsPlan.PlanId().width(80).createLGField(),
//        		uCcsPlan.PlanNbr().width(80).createLGField(),
//        		uCcsPlan.PlanType().width("*").createLGField(),
//        		uCcsPlan.AcctNo().width(65).createLGField(),
//        		uCcsPlan.AcctType().hidden().createLGField()
//        );   
//        
//		listGrid.setData(new ListGridRecord[]{});
//		
//		listGrid.addSelectionChangedHandler(new SelectionChangedHandler() {
//
//			@Override
//			public void onSelectionChanged(SelectionEvent event) {
//				ListGridRecord record = event.getSelectedRecord();
//				
//				if(null == record) {
//					planForm.clearValues();
//					loanPlanForm.clearValues();
//					return;
//				}
//
//				detailTabSet.selectTab(planDetailTab);
//				
//				Integer planId = record.getAttributeAsInt(CcsPlan.P_PlanId);
//				
//				planForm.setValues(tmPlanMap.get(planId).convertToMap());
//				loanPlanForm.setValues(tmPlanMap.get(planId).convertToMap());
//				
//				
//				/*for(FormItem item : planForm.getFields()) {
//				    item.clearValue();
//					if(record.toMap().get(item.getName())instanceof Date){
//						item.setValue(record.getAttributeAsDate(item.getName()));
//						
//					}else{
//						item.setValue(record.getAttribute(item.getName()));
//					}
//				}*/
//				
//			}
//			
//		});
//		
//		return listGrid;
//	}
//	
//	/**
//	 * 创建账户详细信息(批量)显示的表单
//	 * @return
//	 */
	//TODO
//	private YakDynamicForm createTmAccountForm() {
//		YakDynamicForm form = new YakDynamicForm();
//		form.setNumCols(4);
//		form.setWidth100();
//		form.setHeight100();
//		form.setColWidths(150, 200, 150, "*");
//		form.setOverflow(Overflow.AUTO);
//		form.setTitleWidth(150);
////		form.setIsGroup(true);
////		form.setGroupTitle(constants.acctBatchInfoTitle());
//		
//		form.setFields(
//				uCcsAcct.Org(),
//				uCcsAcct.AcctNo(),
//				uCcsAcct.AcctType(),
//				uCcsAcct.CustId(),
//				uCcsAcct.CustLimitId(),
//				uCcsAcct.ProductCd(),
//				uCcsAcct.DefaultLogicalCardNo(),
//				uCcsAcct.CurrCd(),
//				uCcsAcct.CreditLimit(),
//				uCcsAcct.TempLimit(),
//				uCcsAcct.TempLimitBeginDate(),
//				uCcsAcct.TempLimitEndDate(),
//				uCcsAcct.CashLimitRt(),
//				uCcsAcct.OvrlmtRate(),
//				uCcsAcct.LoanLimitRt(),
//				uCcsAcct.CurrBal(),
//				uCcsAcct.CashBal(),
//				uCcsAcct.PrincipalBal(),
//				uCcsAcct.LoanBal(),
//				uCcsAcct.DisputeAmt(),
//				uCcsAcct.BeginBal(),
//				uCcsAcct.PmtDueDayBal(),
//				uCcsAcct.QualGraceBal(),
//				uCcsAcct.GraceDaysFullInd(),
//				uCcsAcct.PointBeginBal(),
//				uCcsAcct.CtdEarnedPoints(),
//				uCcsAcct.CtdDisbPoints(),
//				uCcsAcct.CtdAdjPoints(),
//				uCcsAcct.PointBal(),
//				uCcsAcct.SetupDate(),
//				uCcsAcct.DormentDate(),
//				uCcsAcct.ReinstateDate(),
//				uCcsAcct.OvrlmtDate(),
//				uCcsAcct.OvrlmtNbrOfCyc(),
//				uCcsAcct.Name(),
//				uCcsAcct.Gender(),
//				uCcsAcct.OwningBranch(),
//				uCcsAcct.MobileNo(),
//				uCcsAcct.BillingCycle(),
//				uCcsAcct.CorpName(),
//				uCcsAcct.StmtFlag(),
//				uCcsAcct.StmtMailAddrInd(),
//				uCcsAcct.StmtMediaType(),
//				uCcsAcct.StmtCountryCd(),
//				uCcsAcct.StmtState(),
//				uCcsAcct.StmtCity(),
//				uCcsAcct.StmtDistrict(),
//				uCcsAcct.StmtZip(),
//				uCcsAcct.StmtAddress(),
//				uCcsAcct.Email(),
//				uCcsAcct.BlockCode(),
//				uCcsAcct.DualBillingFlag(),
//				uCcsAcct.AgeCd(),
//				uCcsAcct.UnmatchDb(),
//				uCcsAcct.UnmatchCash(),
//				uCcsAcct.UnmatchCr(),
//				uCcsAcct.DdInd(),
//				uCcsAcct.DdBankName(),
//				uCcsAcct.DdBankBranch(),
//				uCcsAcct.DdBankAcctNo(),
//				uCcsAcct.DdBankAcctName(),
//				uCcsAcct.LastDdAmt(),
//				uCcsAcct.LastDdDate(),
//				uCcsAcct.LastPmtAmt(),
//				uCcsAcct.LastPmtDate(),
//				uCcsAcct.LastStmtDate(),
//				uCcsAcct.LastPmtDueDate(),
//				uCcsAcct.LastAgingDate(),
//				uCcsAcct.CollectDate(),
//				uCcsAcct.CollectOutDate(),
//				uCcsAcct.NextStmtDate(),
//				uCcsAcct.PmtDueDate(),
//				uCcsAcct.DdDate(),
//				uCcsAcct.GraceDate(),
//				uCcsAcct.DlblDate(),
//				uCcsAcct.ClosedDate(),
//				uCcsAcct.FirstStmtDate(),
//				uCcsAcct.CancelDate(),
//				uCcsAcct.ChargeOffDate(),
//				uCcsAcct.FirstPurchaseDate(),
//				uCcsAcct.FirstPurchaseAmt(),
//				uCcsAcct.TotDueAmt(),
//				uCcsAcct.CurrDueAmt(),
//				uCcsAcct.PastDueAmt1(),
//				uCcsAcct.PastDueAmt2(),
//				uCcsAcct.PastDueAmt3(),
//				uCcsAcct.PastDueAmt4(),
//				uCcsAcct.PastDueAmt5(),
//				uCcsAcct.PastDueAmt6(),
//				uCcsAcct.PastDueAmt7(),
//				uCcsAcct.PastDueAmt8(),
//				uCcsAcct.CtdCashAmt(),
//				uCcsAcct.CtdCashCnt(),
//				uCcsAcct.CtdRetailAmt(),
//				uCcsAcct.CtdRetailCnt(),
//				uCcsAcct.CtdPaymentAmt(),
//				uCcsAcct.CtdPaymentCnt(),
//				uCcsAcct.CtdDbAdjAmt(),
//				uCcsAcct.CtdDbAdjCnt(),
//				uCcsAcct.CtdCrAdjAmt(),
//				uCcsAcct.CtdCrAdjCnt(),
//				uCcsAcct.CtdFeeAmt(),
//				uCcsAcct.CtdFeeCnt(),
//				uCcsAcct.CtdInterestAmt(),
//				uCcsAcct.CtdInterestCnt(),
//				uCcsAcct.CtdRefundAmt(),
//				uCcsAcct.CtdRefundCnt(),
//				uCcsAcct.CtdHiOvrlmtAmt(),
//				uCcsAcct.MtdRetailAmt(),
//				uCcsAcct.MtdRetailCnt(),
//				uCcsAcct.MtdCashAmt(),
//				uCcsAcct.MtdCashCnt(),
//				uCcsAcct.MtdRefundAmt(),
//				uCcsAcct.MtdRefundCnt(),
//				uCcsAcct.YtdRetailAmt(),
//				uCcsAcct.YtdRetailCnt(),
//				uCcsAcct.YtdCashAmt(),
//				uCcsAcct.YtdCashCnt(),
//				uCcsAcct.YtdRefundAmt(),
//				uCcsAcct.YtdRefundCnt(),
//				uCcsAcct.YtdOvrlmtFeeAmt(),
//				uCcsAcct.YtdOvrlmtFeeCnt(),
//				uCcsAcct.YtdLpcAmt(),
//				uCcsAcct.YtdLpcCnt(),
//				uCcsAcct.LtdRetailAmt(),
//				uCcsAcct.LtdRetailCnt(),
//				uCcsAcct.LtdCashAmt(),
//				uCcsAcct.LtdCashCnt(),
//				uCcsAcct.LtdRefundAmt(),
//				uCcsAcct.LtdRefundCnt(),
//				uCcsAcct.LtdHighestPrincipal(),
//				uCcsAcct.LtdHighestCrBal(),
//				uCcsAcct.LtdHighestBal(),
//				uCcsAcct.CollectTimes(),
//				uCcsAcct.CollectColr(),
//				uCcsAcct.CollectReason(),
//				uCcsAcct.AgeHist(),
//				uCcsAcct.PaymentHist(),
//				uCcsAcct.WaiveOvlfeeInd(),
//				uCcsAcct.WaiveCardfeeInd(),
//				uCcsAcct.WaiveLatefeeInd(),
//				uCcsAcct.WaiveSvcfeeInd(),
//				uCcsAcct.UserCode1(),
//				uCcsAcct.UserCode2(),
//				uCcsAcct.UserCode3(),
//				uCcsAcct.UserCode4(),
//				uCcsAcct.UserCode5(),
//				uCcsAcct.UserCode6(),
//				uCcsAcct.UserDate1(),
//				uCcsAcct.UserDate2(),
//				uCcsAcct.UserDate3(),
//				uCcsAcct.UserDate4(),
//				uCcsAcct.UserDate5(),
//				uCcsAcct.UserDate6(),
//				uCcsAcct.UserNumber1(),
//				uCcsAcct.UserNumber2(),
//				uCcsAcct.UserNumber3(),
//				uCcsAcct.UserNumber4(),
//				uCcsAcct.UserNumber5(),
//				uCcsAcct.UserNumber6(),
//				uCcsAcct.UserField1(),
//				uCcsAcct.UserField2(),
//				uCcsAcct.UserField3(),
//				uCcsAcct.UserField4(),
//				uCcsAcct.UserField5(),
//				uCcsAcct.UserField6(),
//				uCcsAcct.UserAmt1(),
//				uCcsAcct.UserAmt2(),
//				uCcsAcct.UserAmt3(),
//				uCcsAcct.UserAmt4(),
//				uCcsAcct.UserAmt5(),
//				uCcsAcct.UserAmt6(),
//				uCcsAcct.MtdPaymentAmt(),
//				uCcsAcct.MtdPaymentCnt(),
//				uCcsAcct.YtdPaymentAmt(),
//				uCcsAcct.YtdPaymentCnt(),
//				uCcsAcct.LtdPaymentAmt(),
//				uCcsAcct.LtdPaymentCnt()
//		);
//		
//		return form;
//	}
//	
//
	private ScrollPanel getAuthDetailPanel(){
		ScrollPanel acctOPanel = new ScrollPanel();
		acctOPanel.setWidth("100%");
		acctOPanel.setHeight("190px");
		acctOPanel.add(this.buildCcsAccountOForm());
		return acctOPanel;
	}

	private KylinForm buildCcsAccountOForm() {
	    authDetailForm = new KylinForm();
	    authDetailForm.setField(
				uCcsAcctO.Org().readonly(true),
				uCcsAcctO.AcctNbr().readonly(true),
				uCcsAcctO.AcctType().readonly(true),
				uCcsAcctO.CustLmtId().readonly(true),
				uCcsAcctO.CustId().readonly(true),
				uCcsAcctO.ProductCd().readonly(true),
				uCcsAcctO.CreditLmt().readonly(true),
				uCcsAcctO.TempLmt().readonly(true),
				uCcsAcctO.TempLmtBegDate().readonly(true),
				uCcsAcctO.TempLmtEndDate().readonly(true),
				uCcsAcctO.OvrlmtRate().readonly(true),
				uCcsAcctO.CashLmtRate().readonly(true),
				uCcsAcctO.LoanLmtRate().readonly(true),
				uCcsAcctO.CurrBal().readonly(true),
				uCcsAcctO.CashBal().readonly(true),
				uCcsAcctO.LoanBal().readonly(true),
				uCcsAcctO.DisputeAmt().readonly(true),
				uCcsAcctO.BlockCode().readonly(true),
				uCcsAcctO.MemoCash().readonly(true),
				uCcsAcctO.MemoCr().readonly(true),
				uCcsAcctO.MemoDb().readonly(true),
				uCcsAcctO.LastMatchedOvernightDate().readonly(true),
				uCcsAcctO.LastUpdateBizDate().readonly(true),
				uCcsAcctO.OwningBranch().readonly(true),
				uCcsAcctO.CycleDay().readonly(true)
		);
	    authDetailForm.getSetting().labelWidth(160);
		return authDetailForm;
	}

//	/**
//	 * 创建账户详细信息(授权)显示的表单
//	 * @return
//	 */
//	private YakDynamicForm createTmAccountOForm() {
//		YakDynamicForm form = new YakDynamicForm();
//		form.setNumCols(4);
//		form.setWidth100();
//		form.setColWidths(150, 200, 150, "*");
//		form.setHeight100();
//		form.setOverflow(Overflow.AUTO);
//		form.setTitleWidth(150);
////		form.setIsGroup(true);
////		form.setGroupTitle(constants.acctAuthInfoTitle());
//		
//		form.setFields(
//				uTmAccountO.Org(),
//				uTmAccountO.AcctNo(),
//				uTmAccountO.AcctType(),
//				uTmAccountO.CustLimitId(),
//				uTmAccountO.CustId(),
//				uTmAccountO.ProductCd(),
//				uTmAccountO.CreditLimit(),
//				uTmAccountO.TempLimit(),
//				uTmAccountO.TempLimitBeginDate(),
//				uTmAccountO.TempLimitEndDate(),
//				uTmAccountO.OvrlmtRate(),
//				uTmAccountO.CashLimitRt(),
//				uTmAccountO.LoanLimitRt(),
//				uTmAccountO.CurrBal(),
//				uTmAccountO.CashBal(),
//				uTmAccountO.LoanBal(),
//				uTmAccountO.DisputeAmt(),
//				uTmAccountO.BlockCode(),
//				uTmAccountO.UnmatchCash(),
//				uTmAccountO.UnmatchCr(),
//				uTmAccountO.UnmatchDb(),
//				uTmAccountO.LastMatchedOvernightDate(),
//				uTmAccountO.LastUpdateBizDate(),
//				uTmAccountO.OwningBranch(),
//				uTmAccountO.BillingCycle()
//		);
//		
//		return form;
//	}
//	

	private ScrollPanel getCreditPlanDetailPanel(){
	    	ScrollPanel planDetailPanel = new ScrollPanel();
		planDetailPanel.setWidth("100%");
		planDetailPanel.setHeight("190px");
		VerticalPanel verticalPanel = new VerticalPanel();
		verticalPanel.add(this.buildPlanForm());
		verticalPanel.add(this.buildeloanPlanForm());
		planDetailPanel.add(verticalPanel);
		return planDetailPanel;
	}

	private KylinForm buildeloanPlanForm() {
		loanPlanForm = new KylinForm();
		loanPlanForm.setField(
				uCcsPlan.PastPenalty().readonly(true),
				uCcsPlan.CtdPenalty().readonly(true),
				uCcsPlan.PastCompound().readonly(true),
				uCcsPlan.CtdCompound().readonly(true),
				uCcsPlan.PenaltyAcru().readonly(true),
				uCcsPlan.CompoundAcru().readonly(true),
				uCcsPlan.InterestRate().readonly(true),
				uCcsPlan.PenaltyRate().readonly(true),
				uCcsPlan.CompoundRate().readonly(true),
				uCcsPlan.UsePlanRate().readonly(true),
				uCcsPlan.LastPmtDate().readonly(true)
		);
		loanPlanForm.getSetting().labelWidth(140);
	loanPlanForm.setVisible(false);
	return loanPlanForm;
	}
	
	private KylinForm buildPlanForm() {
	    creditPlanForm = new KylinForm();
	    creditPlanForm.setField(
					uCcsPlan.Org().readonly(true),
					uCcsPlan.PlanId().readonly(true),
					uCcsPlan.AcctNbr().readonly(true),
					uCcsPlan.AcctType().readonly(true),
					uCcsPlan.LogicCardNbr().readonly(true),
					uCcsPlan.PlanNbr().readonly(true),
					uCcsPlan.PlanType().readonly(true),
					uCcsPlan.ProductCd().readonly(true),
					uCcsPlan.RefNbr().readonly(true),
					uCcsPlan.CurrBal().readonly(true),
					uCcsPlan.BegBal().readonly(true),
					uCcsPlan.DisputeAmt().readonly(true),
		//				uCcsPlan.TotDueAmt().readonly(true),
					uCcsPlan.PlanAddDate().readonly(true),
					uCcsPlan.PaidOutDate().readonly(true),
					uCcsPlan.PastPrincipal().readonly(true),
					uCcsPlan.PastInterest().readonly(true),
					uCcsPlan.PastCardFee().readonly(true),
					uCcsPlan.PastOvrlmtFee().readonly(true),
					uCcsPlan.PastLateFee().readonly(true),
					uCcsPlan.PastNsfundFee().readonly(true),
					uCcsPlan.PastTxnFee().readonly(true),
					uCcsPlan.PastSvcFee().readonly(true),
					uCcsPlan.PastInsurance().readonly(true),
					uCcsPlan.PastUserFee1().readonly(true),
					uCcsPlan.PastUserFee2().readonly(true),
					uCcsPlan.PastUserFee3().readonly(true),
					uCcsPlan.PastUserFee4().readonly(true),
					uCcsPlan.PastUserFee5().readonly(true),
					uCcsPlan.PastUserFee6().readonly(true),
					uCcsPlan.CtdPrincipal().readonly(true),
					uCcsPlan.CtdInterest().readonly(true),
					uCcsPlan.CtdCardFee().readonly(true),
					uCcsPlan.CtdOvrlmtFee().readonly(true),
					uCcsPlan.CtdLateFee().readonly(true),
					uCcsPlan.CtdNsfundFee().readonly(true),
					uCcsPlan.CtdSvcFee().readonly(true),
					uCcsPlan.CtdTxnFee().readonly(true),
					uCcsPlan.CtdInsurance().readonly(true),
					uCcsPlan.CtdUserFee1().readonly(true),
					uCcsPlan.CtdUserFee2().readonly(true),
					uCcsPlan.CtdUserFee3().readonly(true),
					uCcsPlan.CtdUserFee4().readonly(true),
					uCcsPlan.CtdUserFee5().readonly(true),
					uCcsPlan.CtdUserFee6().readonly(true),
					uCcsPlan.CtdAmtDb().readonly(true),
					uCcsPlan.CtdAmtCr().readonly(true),
					uCcsPlan.CtdNbrDb().readonly(true),
					uCcsPlan.CtdNbrCr().readonly(true),
					uCcsPlan.NodefbnpIntAcru().readonly(true),
					uCcsPlan.BegDefbnpIntAcru().readonly(true),
					uCcsPlan.CtdDefbnpIntAcru().readonly(true),
					uCcsPlan.UserCode1().readonly(true),
					uCcsPlan.UserCode2().readonly(true),
					uCcsPlan.UserCode3().readonly(true),
					uCcsPlan.UserCode4().readonly(true),
					uCcsPlan.UserCode5().readonly(true),
					uCcsPlan.UserCode6().readonly(true),
					uCcsPlan.UserNumber1().readonly(true),
					uCcsPlan.UserNumber2().readonly(true),
					uCcsPlan.UserNumber3().readonly(true),
					uCcsPlan.UserNumber4().readonly(true),
					uCcsPlan.UserNumber5().readonly(true),
					uCcsPlan.UserNumber6().readonly(true),
					uCcsPlan.UserField1().readonly(true),
					uCcsPlan.UserField2().readonly(true),
					uCcsPlan.UserField3().readonly(true),
					uCcsPlan.UserField4().readonly(true),
					uCcsPlan.UserField5().readonly(true),
					uCcsPlan.UserField6().readonly(true),
					uCcsPlan.UserDate1().readonly(true),
					uCcsPlan.UserDate2().readonly(true),
					uCcsPlan.UserDate3().readonly(true),
					uCcsPlan.UserDate4().readonly(true),
					uCcsPlan.UserDate5().readonly(true),
					uCcsPlan.UserDate6().readonly(true),
					uCcsPlan.UserAmt1().readonly(true),
					uCcsPlan.UserAmt2().readonly(true),
					uCcsPlan.UserAmt3().readonly(true),
					uCcsPlan.UserAmt4().readonly(true),
					uCcsPlan.UserAmt5().readonly(true),
					uCcsPlan.UserAmt6().readonly(true)
		);
	    creditPlanForm.getSetting().labelWidth(140);
	return creditPlanForm;
	}
	
//	/**
//	 * 创建信用计划详细信息显示的表单
//	 * @return
//	 */
//	private YakDynamicForm createPlanForm() {
//		//forms = new HashMap<String, YakDynamicForm>();
//		planForm = new YakDynamicForm();
//		{
//			planForm.setNumCols(4);
//			planForm.setWidth100();
//			planForm.setColWidths(150, 200, 150, "*");
//			planForm.setHeight100();
//			planForm.setOverflow(Overflow.AUTO);
//			planForm.setTitleWidth(150);
////			planForm.show();
//		//		form.setIsGroup(true);
//		//		form.setGroupTitle(constants.planInfoTitle());
//			
//			planForm.setFields(
//					uCcsPlan.Org(),
//					uCcsPlan.PlanId(),
//					uCcsPlan.AcctNo(),
//					uCcsPlan.AcctType(),
//					uCcsPlan.LogicalCardNo(),
//					uCcsPlan.PlanNbr(),
//					uCcsPlan.PlanType(),
//					uCcsPlan.ProductCd(),
//					uCcsPlan.RefNbr(),
//					uCcsPlan.CurrBal(),
//					uCcsPlan.BeginBal(),
//					uCcsPlan.DisputeAmt(),
//		//				uCcsPlan.TotDueAmt(),
//					uCcsPlan.PlanAddDate(),
//					uCcsPlan.PaidOutDate(),
//					uCcsPlan.PastPrincipal(),
//					uCcsPlan.PastInterest(),
//					uCcsPlan.PastCardFee(),
//					uCcsPlan.PastOvrlmtFee(),
//					uCcsPlan.PastLpc(),
//					uCcsPlan.PastNsfFee(),
//					uCcsPlan.PastTxnFee(),
//					uCcsPlan.PastSvcFee(),
//					uCcsPlan.PastIns(),
//					uCcsPlan.PastUserFee1(),
//					uCcsPlan.PastUserFee2(),
//					uCcsPlan.PastUserFee3(),
//					uCcsPlan.PastUserFee4(),
//					uCcsPlan.PastUserFee5(),
//					uCcsPlan.PastUserFee6(),
//					uCcsPlan.CtdPrincipal(),
//					uCcsPlan.CtdInterest(),
//					uCcsPlan.CtdCardFee(),
//					uCcsPlan.CtdOvrlmtFee(),
//					uCcsPlan.CtdLpc(),
//					uCcsPlan.CtdNsfFee(),
//					uCcsPlan.CtdSvcFee(),
//					uCcsPlan.CtdTxnFee(),
//					uCcsPlan.CtdIns(),
//					uCcsPlan.CtdUserFee1(),
//					uCcsPlan.CtdUserFee2(),
//					uCcsPlan.CtdUserFee3(),
//					uCcsPlan.CtdUserFee4(),
//					uCcsPlan.CtdUserFee5(),
//					uCcsPlan.CtdUserFee6(),
//					uCcsPlan.CtdAmtDb(),
//					uCcsPlan.CtdAmtCr(),
//					uCcsPlan.CtdNbrDb(),
//					uCcsPlan.CtdNbrCr(),
//					uCcsPlan.NodefbnpIntAcru(),
//					uCcsPlan.BegDefbnpIntAcru(),
//					uCcsPlan.CtdDefbnpIntAcru(),
//					uCcsPlan.UserCode1(),
//					uCcsPlan.UserCode2(),
//					uCcsPlan.UserCode3(),
//					uCcsPlan.UserCode4(),
//					uCcsPlan.UserCode5(),
//					uCcsPlan.UserCode6(),
//					uCcsPlan.UserNumber1(),
//					uCcsPlan.UserNumber2(),
//					uCcsPlan.UserNumber3(),
//					uCcsPlan.UserNumber4(),
//					uCcsPlan.UserNumber5(),
//					uCcsPlan.UserNumber6(),
//					uCcsPlan.UserField1(),
//					uCcsPlan.UserField2(),
//					uCcsPlan.UserField3(),
//					uCcsPlan.UserField4(),
//					uCcsPlan.UserField5(),
//					uCcsPlan.UserField6(),
//					uCcsPlan.UserDate1(),
//					uCcsPlan.UserDate2(),
//					uCcsPlan.UserDate3(),
//					uCcsPlan.UserDate4(),
//					uCcsPlan.UserDate5(),
//					uCcsPlan.UserDate6(),
//					uCcsPlan.UserAmt1(),
//					uCcsPlan.UserAmt2(),
//					uCcsPlan.UserAmt3(),
//					uCcsPlan.UserAmt4(),
//					uCcsPlan.UserAmt5(),
//					uCcsPlan.UserAmt6()
//			);
//		}
//		//forms.put("planForm", planForm);
//		
//		
//		loanPlanForm = new YakDynamicForm();
//		{
//			loanPlanForm.setNumCols(4);
//			loanPlanForm.setTop(735);
//			loanPlanForm.setWidth100();
//			loanPlanForm.setColWidths(150, 200, 150, "*");
////			loanPlanForm.setHeight(200);
////			loanPlanForm.setOverflow(Overflow.AUTO);
//			
//			loanPlanForm.setFields(
//					uCcsPlan.PastPenalty(),
//					uCcsPlan.CtdPenalty(),
//					uCcsPlan.PastCompound(),
//					uCcsPlan.CtdCompound(),
//					uCcsPlan.PenaltyAcru(),
//					uCcsPlan.CompoundAcru(),
//					uCcsPlan.InterestRate(),
//					uCcsPlan.PenaltyRate(),
//					uCcsPlan.CompoundRate(),
//					uCcsPlan.UsePlanRate(),
//					uCcsPlan.LastPmtDate()
//			);
//		}
//		loanPlanForm.hide();
//	
//		planForm.addChild(loanPlanForm);
//		return planForm;
//	}
////	createToWriteOffBadDebtsListGrid
//	/**
//	 * 创建呆账核销表单（zrx）
//	 * @return
//	 */
	//TODO
	private KylinGrid badDebtsGrid;
	private KylinGrid buildToWriteOffBadDebtsGrid(){
		badDebtsGrid = new KylinGrid();
		badDebtsGrid.setWidth("1120px");
		badDebtsGrid.setHeight("150px");
		badDebtsGrid.setColumns(new ColumnHelper[]{
				new TextColumnHelper(BadAccountCancelExaminationConstants.ADJID, constants.adjId(), 80),
				
				new TextColumnHelper(BadAccountCancelExaminationConstants.PROVISIONORREMIT, constants.dbCrInd(), 80),
				
				new TextColumnHelper(BadAccountCancelExaminationConstants.CURRENCY, constants.postCurrCd(), 80),
				
				new TextColumnHelper(BadAccountCancelExaminationConstants.POSTAMT, constants.postAmt(), 80),
				
				new TextColumnHelper(BadAccountCancelExaminationConstants.THECARDNETWORK, constants.owningBranch(), 80),
				
				new TextColumnHelper(BadAccountCancelExaminationConstants.TXNCODE, constants.txnCode(), 80),
				
				new TextColumnHelper(BadAccountCancelExaminationConstants.AGEGROUP, constants.ageGroup(), 80),
				
				new TextColumnHelper(BadAccountCancelExaminationConstants.BUCKETTYPE, constants.bucketType(), 80),
				
				new TextColumnHelper(BadAccountCancelExaminationConstants.PLANNBR, constants.planNbr(), 80),
				
				new TextColumnHelper(BadAccountCancelExaminationConstants.POSTGLIND, constants.postGlInd(), 80),
			
		});
		return badDebtsGrid;
	}
	
//	private ListGrid createToWriteOffBadDebtsListGrid() {
//		ListGrid listGrid = new ListGrid();
//		{
//			listGrid.setShowRecordComponents(true);
//			listGrid.setShowRecordComponentsByCell(true);
//			listGrid.setWidth100();
//			listGrid.setHeight100();
//			listGrid.setRecordComponentPoolingMode(RecordComponentPoolingMode.DATA);
//			
//			ListGridField adjIdItem = new ListGridField();//顺序号
//            adjIdItem.setName(T3309Constants.ADJID);
//            adjIdItem.setTitle(constants.adjId());
//            adjIdItem.setWidth(80);
//            
//            ListGridField provisionOrRemitItem = new ListGridField();//借贷记标志
//            provisionOrRemitItem.setName(T3309Constants.PROVISIONORREMIT);
//            provisionOrRemitItem.setTitle(constants.provisionOrRemit());
//            provisionOrRemitItem.setWidth(80);
//            
//            ListGridField currency = new ListGridField();//币种
//            currency.setName(T3309Constants.CURRENCY);
//            currency.setTitle(constants.currency());
//            currency.setWidth(80);
//            
//            ListGridField postAmt = new ListGridField();//金额
//            postAmt.setName(T3309Constants.POSTAMT);
//            postAmt.setTitle(constants.postAmt());
//            postAmt.setWidth(80);
//            
//            ListGridField theCardNetwork = new ListGridField();//发卡网点
//            theCardNetwork.setName(T3309Constants.THECARDNETWORK);
//            theCardNetwork.setTitle(constants.theCardNetwork());
//            theCardNetwork.setWidth(80);
//            
//            ListGridField txnCode = new ListGridField();//交易码
//            txnCode.setName(T3309Constants.TXNCODE);
//            txnCode.setTitle(constants.txnCode());
//            txnCode.setWidth(80);
//            
//    		ListGridField ageGroup = new ListGridField();//账龄组
//            ageGroup.setName(T3309Constants.AGEGROUP);
//            ageGroup.setTitle(constants.ageGroup());	
//            ageGroup.setWidth(80);
//            
//    		ListGridField bucketType = new ListGridField();//余额成分
//            bucketType.setName(T3309Constants.BUCKETTYPE);
//            bucketType.setTitle(constants.bucketType());
//            bucketType.setWidth(80);
//            
//            ListGridField planNbr = new ListGridField();//信用计划号
//            planNbr.setName(T3309Constants.PLANNBR);
//            planNbr.setTitle(constants.planNbr());	
//            planNbr.setWidth(80);
//            
//            ListGridField postGlInd = new ListGridField();//总账入账标志
//            postGlInd.setName(T3309Constants.POSTGLIND);
//            postGlInd.setTitle(constants.postGlInd());
//            postGlInd.setWidth("*");
//            
//            listGrid.setFields(adjIdItem,provisionOrRemitItem,currency,postAmt
//            		,theCardNetwork,txnCode,ageGroup,bucketType,planNbr,postGlInd);
//			listGrid.setAutoFetchData(true);
//            
////			blockCode = new ListGridField("blockcode", constants.blockCode());
////			blockCode.setWidth(100);
////			
////			description = new ListGridField("description", constants.descTitle());
////			description.setWidth("*");
////			listGrid.setFields(
////					blockCode,
////					description
////					);
//		}
//		return listGrid;
//	}	
	//TODO
	private KylinGrid blockGrid;
	private KylinGrid buildBlockcodeListGrid(){
		blockGrid = new KylinGrid();
		blockGrid.setHeight("100%");
		blockGrid.setWidth("100%");
		blockGrid.setColumns(new ColumnHelper[]{
				new TextColumnHelper("blockcode", constants.blockCode(), 100),
				new TextColumnHelper("description", constants.descTitle(), 100)
		});
		return blockGrid;
	}
	
//	/**
//	 * 创建锁定码信息表单
//	 * @return
//	 */
//	private ListGrid createBlockcodeListGrid() {
//		ListGrid listGrid = new ListGrid();
//		{
//			listGrid.setShowRecordComponents(true);
//			listGrid.setShowRecordComponentsByCell(true);
//			listGrid.setWidth100();
//			listGrid.setRecordComponentPoolingMode(RecordComponentPoolingMode.DATA);
//			
//			blockCode = new ListGridField("blockcode", constants.blockCode());
//			blockCode.setWidth(100);
//			
//			description = new ListGridField("description", constants.descTitle());
//			description.setWidth("*");
//			listGrid.setFields(
//					blockCode,
//					description
//					);
//		}
//		
//		return listGrid;
//	}
//	
//	/**
//	 * 创建账户信息编辑的表单
//	 * @return
//	 */
////	private YakDynamicForm createTmAccountAlterForm() {
////		YakDynamicForm form = new YakDynamicForm();
////		form.setNumCols(4);
////		form.setWidth100();
////		form.setOverflow(Overflow.AUTO);
////		form.setTitleWidth(150);
////		
////		RegExpValidator regExpValidator = new RegExpValidator("^[A-Z0-9]*$");
////		regExpValidator.setErrorMessage(constants.msgBlockCodeError());
////		
////		form.setFields(
////				uCcsAcct.AcctNo(),
////				uCcsAcct.AcctType(),
////				uCcsAcct.WaiveOvlfeeInd().required().createFormItem(),
////				uCcsAcct.WaiveCardfeeInd().required().createFormItem(),
////				uCcsAcct.WaiveLatefeeInd().required().createFormItem(),
////				uCcsAcct.WaiveSvcfeeInd().required().createFormItem(),
////				uCcsAcct.BlockCode().validators(regExpValidator).createFormItem()
////		);
////		
////		
////		return form;
////	}
//	
//	/**
//	 * 创建查询表单
//	 * */
//	private YakDynamicForm createSearchForm() {
//		final YakDynamicForm searchForm = new YakDynamicForm();
//		{
//			searchForm.setWidth100();
//			searchForm.setNumCols(3);
//			searchForm.setColWidths(70, 160, "*");
//						
//			cardItem = uCcsCardLmMediaMap.CardNo().createFormItem();
//			final ButtonItem searchBtn = clientUtils.createSearchItem();
//			{
//				searchBtn.setStartRow(false);
//				searchBtn.addClickHandler(new ClickHandler() {
//
//					@Override
//					public void onClick(ClickEvent event) {
//						cardItem.setRequired(true);
//						if(idTypeItem != null || idNoItem != null || telPhoneItem != null){
//							idTypeItem.clearValue();
//							idNoItem.clearValue();
//							telPhoneItem.clearValue();
//						}
//						searchForm.submit();
//					}
//					
//				});
//			}
//			
//			searchForm.setFields(cardItem, searchBtn);//第一行
			// TODO 账户信息查询
//			searchForm.addSubmitValuesHandler(new SubmitValuesHandler() {
//				@Override
//				public void onSubmitValues(SubmitValuesEvent event) {
//					if(searchForm.validate()) {
//
//						submitCardNo = UIUtil.getCardNo(cardItem);
//						submitTelPhone = null;
//						submitIdType = null;
//						submitIdNo = null;
//						RPCTemplate.call(new RPCExecutor<List<Map<String, Serializable>>>() {
//							@Override
//							public void execute(AsyncCallback<List<Map<String, Serializable>>> callback) {
//								server.getAcctList(submitCardNo, submitIdType, submitIdNo, submitTelPhone, callback);
//							}
//							
//							@Override
//							public void onSuccess(List<Map<String, Serializable>> result) {
//								updateAcctLst(result);
//							}
//						}, searchForm);
//						
//					}
//				}
//			});
//		}
//		
//		return searchForm;
//	}
//	
//	
//	/**
//	 * 创建查询表单，根据证据类型和证据号码查询账户列表
//	 * @return
//	 */
//	private YakDynamicForm createSearchFormToCust() {
//		final YakDynamicForm searchForm = new YakDynamicForm();
//		{
//			searchForm.setWidth100();
//			searchForm.setNumCols(5);
//			searchForm.setColWidths(70, 160, 120, 160, "*");
//			
//			idTypeItem = uCcsCustomer.IdType().startRow(true).createSelectItem();
//			idNoItem = uCcsCustomer.IdNo().createFormItem();
//			
//			final ButtonItem searchBtn = clientUtils.createSearchItem();
//			{
//				searchBtn.setStartRow(false);
//				searchBtn.addClickHandler(new ClickHandler() {
//					
//					@Override
//					public void onClick(ClickEvent event) {
//						idTypeItem.setRequired(true);
//						idNoItem.setRequired(true);
//						if(cardItem != null || telPhoneItem != null){
//							cardItem.clearValue();
//							telPhoneItem.clearValue();
//						}
//						searchForm.submit();
//					}
//					
//				});
//			}
//			
//			searchForm.setFields(idTypeItem, idNoItem, searchBtn);
//			
//			searchForm.addSubmitValuesHandler(new SubmitValuesHandler() {
//				@Override
//				public void onSubmitValues(SubmitValuesEvent event) {
//					if(searchForm.validate()) {
//
//						submitIdNo = (String) idNoItem.getValue();
//						submitIdType =  idTypeItem.getValueAsString();
//						submitCardNo = null;
//						submitTelPhone = null;
//						RPCTemplate.call(new RPCExecutor<List<Map<String, Serializable>>>() {
//							@Override
//							public void execute(AsyncCallback<List<Map<String, Serializable>>> callback) {
//								server.getAcctList(submitCardNo, submitIdType, submitIdNo, submitTelPhone, callback);
//							}
//							
//							@Override
//							public void onSuccess(List<Map<String, Serializable>> result) {
//								updateAcctLst(result);
//							}
//							
//						}, searchForm);
//						
//					}
//				}
//			});
//		}
//		return searchForm;
//	}
//	
//	
//	/**
//	 * 创建查询表单，根据客户手机号码查询账户信息
//	 * @return
//	 */
//	private YakDynamicForm createSearchFormToTel() {
//		final YakDynamicForm searchForm = new YakDynamicForm();
//		{
//			searchForm.setWidth100();
//			searchForm.setNumCols(3);
//			searchForm.setColWidths(70, 160, "*");
//			
//			telPhoneItem = uCcsCustomer.MobileNo().startRow(true).createFormItem();
//			
//			final ButtonItem searchBtn = clientUtils.createSearchItem();
//			{
//				searchBtn.setStartRow(false);
//				searchBtn.addClickHandler(new ClickHandler() {
//
//					@Override
//					public void onClick(ClickEvent event) {
//						telPhoneItem.setRequired(true);
//						if(idTypeItem != null || idNoItem != null || cardItem != null){
//							idTypeItem.clearValue();
//							idNoItem.clearValue();
//							cardItem.clearValue();
//						}
//						searchForm.submit();
//					}
//					
//				});
//			}
//			
//			searchForm.setFields(telPhoneItem, searchBtn);
//			
//			searchForm.addSubmitValuesHandler(new SubmitValuesHandler() {
//				@Override
//				public void onSubmitValues(SubmitValuesEvent event) {
//					if(searchForm.validate()) {
//
//						submitTelPhone = (String) telPhoneItem.getValue();
//						submitCardNo = null;
//						submitIdType = null;
//						submitIdNo = null;
//						RPCTemplate.call(new RPCExecutor<List<Map<String, Serializable>>>() {
//							@Override
//							public void execute(AsyncCallback<List<Map<String, Serializable>>> callback) {
//								server.getAcctList(submitCardNo, submitIdType, submitIdNo, submitTelPhone, callback);
//							}
//							@Override
//							public void onSuccess(List<Map<String, Serializable>> result) {
//								updateAcctLst(result);
//							}
//						}, searchForm);
//					}
//				}
//			});
//		}
//		
//		return searchForm;
//	}
//	
//	/**
//	 * 更新账户列表 获取卡号以及客户名称
//	 * @param result
//	 */
//	@SuppressWarnings("unchecked")
//	private void updateAcctLst(
//			List<Map<String, Serializable>> result) {
//		//显示查询的介质卡号
//		cardForm.clearValues();
////		selectedCardNoItem.setValue(submitCardNo);
//		String acctName = null;
//		String cardNo = null;
//		
//		//更新账户列表
//		tmAccountMap.clear();
//		tmAccountOMap.clear();
//		blockCodeList.clear();
//		toWriteOffBadDebtsList.clear();
//		bjAndFyList.clear();
//		
//		for(Map<String, Serializable> entry : result) {
//			TmAccountKey tmAccountKey = new TmAccountKey();
//			TmAccount tmAccount = new TmAccount();
//			//账户详细信息列表
//			tmAccount.updateFromMap(entry);
//			tmAccountKey.setAcctNo(tmAccount.getAcctNo());
//			tmAccountKey.setAcctType(tmAccount.getAcctType());
//			tmAccountMap.put(tmAccountKey, tmAccount); 
//			//账户锁定码信息列表
//			blockCodeList.put(tmAccountKey, (Map<String, String>)entry.get(PublicConst.KEY_BLOCKCODE_LIST));
//			//呆账核销查询
//			toWriteOffBadDebtsList.put(tmAccountKey, (List<GlTxnAdj>)entry.get(T3309Constants.TOWRITEOFFBADDEBTS));
//			//本金余额和费用余额
//			bjAndFyList.put(tmAccountKey, (String)entry.get(T3309Constants.BJANDFY));
//			
//			if(null == acctName) {
//				acctName = tmAccount.getName();
//			}
//			
//			cardNo = tmAccount.getDefaultLogicalCardNo();
//			//账户授权信息列表
//			TmAccountOKey tmAccountOKey = new TmAccountOKey();
//			TmAccountO tmAccountO = new TmAccountO();
//			tmAccountO.updateFromMap(entry);
//			tmAccountOKey.setAcctNo(tmAccountO.getAcctNo());
//			tmAccountOKey.setAcctType(tmAccountO.getAcctType());
//			tmAccountOMap.put(tmAccountOKey, tmAccountO);
//		}
//		selectedCardAcctNameItem.setValue(acctName);
//		selectedCardNoItem.setValue(cardNo);
//		
//		T3309Page.this.updateAcctListGrid(tmAccountMap.values());//设置列表（左）账户信息查询列表
//		
//	}
//	
//	/**
//	 * 清除与账户相关的信息
//	 */
//	private void clearAcctInfo() {
//		tmAccountDetailForm.clearValues();
//		tmAccountODetailForm.clearValues();
//		selectedAcctNo = null;
//		blockcodeListGrid.setRecords(new ListGridRecord[]{});
//		toWriteOffBadDebtsListGrid.setRecords(new ListGridRecord[]{});//清除呆账核销列表
//		planListGrid.setData(new ListGridRecord[]{});
//	}
//	
//	
//	private void updateAcctListGrid(Collection<TmAccount> tmAccountList) {
//		ListGridRecord[] acctRecords;
//		if(null == tmAccountList || tmAccountList.isEmpty()) {
//			acctListGrid.setData(new ListGridRecord[]{});
//			return;
//		}
//		
//		acctRecords =  new ListGridRecord[tmAccountList.size()];
//		int i = 0;
//		for(TmAccount tmAccount : tmAccountList) {
//			ListGridRecord record = makeAcctListGridRecord(tmAccount);
//			acctRecords[i] = record;
//			i++;
//		}
//		
//		acctListGrid.setData(acctRecords);
//	}
//	
//	private void updatePlanListGrid(Collection<CcsPlan> tmPlanList) {
//		ListGridRecord[] planRecords;
//		if(null == tmPlanList || tmPlanList.isEmpty()) {
//			planListGrid.setData(new ListGridRecord[]{});
//			return;
//		}
//		
//		planRecords =  new ListGridRecord[tmPlanList.size()];
//		int i = 0;
//		for(CcsPlan tmPlan : tmPlanList) {
//			ListGridRecord record = makePlanListGridRecord(tmPlan);
//			planRecords[i] = record;
//			i++;
//		}
//		
//		planListGrid.setData(planRecords);
//	}
//	
//	/**
//	 * 创建账户列表的记录对象
//	 * @param tmAccount
//	 * @return
//	 */
//	private ListGridRecord makeAcctListGridRecord(TmAccount tmAccount) {
//		ListGridRecord record = new ListGridRecord();
//		record.setAttribute(TmAccount.P_AcctNo, tmAccount.getAcctNo());
//		record.setAttribute(TmAccount.P_AcctType, tmAccount.getAcctType());
//		record.setAttribute(TmAccount.P_CurrCd, tmAccount.getCurrCd());
//		record.setAttribute(TmAccount.P_ProductCd, tmAccount.getProductCd());
//		record.setAttribute(TmAccount.P_BlockCode, tmAccount.getBlockCode());//锁定码
//		record.setAttribute(TmAccount.P_OwningBranch, tmAccount.getOwningBranch());//发卡网点
//		record.setAttribute(TmAccount.P_CustId, tmAccount.getCustId());
//		
//		return record;
//	}
//	
//	/**
//	 * 创建信用计划列表的记录对象
//	 * @param tmPlan
//	 * @return
//	 */
//	private ListGridRecord makePlanListGridRecord(CcsPlan tmPlan) {
//		ListGridRecord record = new ListGridRecord();
//		record.setAttribute(CcsPlan.P_PlanId, tmPlan.getPlanId());
//		record.setAttribute(CcsPlan.P_PlanNbr, tmPlan.getPlanNbr());
//		record.setAttribute(CcsPlan.P_PlanType, tmPlan.getPlanType());
//		record.setAttribute(CcsPlan.P_AcctNo, tmPlan.getAcctNo());
//		record.setAttribute(CcsPlan.P_AcctType, tmPlan.getAcctType());
//		
//		return record;
//	}
//	
//	/**
//	 * 加载信用计划列表
//	 */
//	private void loadPlanListFromServer() {
//		RPCTemplate.call(new RPCExecutor<List<CcsPlan>>() {
//			@Override
//			public void execute(AsyncCallback<List<CcsPlan>> callback) {
//				planListGrid.setData(new ListGridRecord[]{});
//				tmPlanMap.clear();
//				server.getPlanList(selectedAcctNo, selectedAcctType, callback);
//			}
//			
//			@Override
//			public void onSuccess(List<CcsPlan> result) {
//				planForm.clearValues();
//				
//				loanPlanForm.clearValues();
//				//更新信用计划列表
//				T3309Page.this.updatePlanListGrid(result);
//				
//				tmPlanMap.clear();
//				for(CcsPlan tmPlan : result) {
//					tmPlanMap.put(tmPlan.getPlanId(), tmPlan);
//				}
//			}
//			
//		}, fetchPlanListBtn);
//	}
//
//	@Override
//	public void updateView(ParamMapToken token) {
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
//	/**
//	 * 
//* @author fanghj
//	 *
//	 */
//	private class BlockCodeLGRecord extends ListGridRecord{
//		public BlockCodeLGRecord(String blockCode, String description){
//			setAttribute("blockcode", blockCode);
//			setAttribute("description", description);
//		}
//	}
//	
}
