package com.sunline.ccs.ui.client.pages.waive;

import java.math.BigDecimal;

import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.domain.i18n.BucketTypeConstants;
import com.sunline.ccs.infrastructure.client.ui.UCcsLoan;
import com.sunline.ccs.infrastructure.client.ui.UCcsPlan;
import com.sunline.ccs.infrastructure.client.ui.UCcsTxnWaiveLog;
import com.sunline.ccs.infrastructure.client.ui.UTxnCd;
import com.sunline.ccs.ui.client.commons.CommonUiUtils;
import com.sunline.kylin.web.ark.client.helper.DecimalColumnHelper;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.ListData;
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
* @Description 手工豁免页面
* @author chenpy
 */
@Singleton
public class WaivePage extends Page{
	
	@Inject
	private WaivePageConstants constants;
	@Inject
	private BucketTypeConstants bucketTypeConstants;         //非本金期款类型（余额成分类型）
	
	@Inject
	private UCcsLoan uCcsLoan;                 //贷款交易
	@Inject
	private UCcsPlan uCcsPlan;                   //信用计划
	@Inject
	private UTxnCd uTxnCd;                       //交易码
	
	private KylinForm contrNbrsearchForm;                 //根据合同号查询贷款交易
	private KylinForm acctNbrSearchForm;                    //根据账户号/账户类型查询贷款交易
	
	private KylinGrid loanGrid;                           //分期信息表
	private KylinGrid planGrid;                           //信用计划表
	
	private KylinButton fetchPlanListBtn;                     //获取信用计划列表的按钮
	
	private KylinForm loanDetailForm;                 //分期信息详情
	private KylinForm planDetailForm;                 //信用计划详情
	private KylinGrid exemptGrid;                       //显示手工豁免后待审核的信息计划
	
	private Tab tab;
	private TabItemSetting loanItemSetting;             //分期计划详情Tab的Setting
	private TabItemSetting planItemSetting;             //信用计划详情的Tab的Setting
	private TabItemSetting exemptItemSetting;        //豁免的Tab
	
	private KylinButton exemptButton;                      //手工豁免的Button
	
	private TextColumnHelper bucketTypeColumnHelper;    //余额成分类型的下拉框
	private TextColumnHelper remarkColumnHelper;        //备注
	private DecimalColumnHelper inputMoney;                      //金额的输入框
	
	@Inject
	private UCcsTxnWaiveLog uCcsTxnWaiveLog;
	
	//赋值
	protected void updateLoanKylinGrid(Data data){
		MapData result = new MapData();
		result.put("rows", data);
		result.put("total", data.asListData().size());
		loanGrid.loadData(result);
	}
	
	protected void updatePlanKylinGrid(Data data){
		MapData result = new MapData();
		result.put("rows", data);
		result.put("total", data.asListData().size());
		planGrid.loadData(result);
	}
	
	/**
	* @Description 创建根据合同号查询贷款交易的form
	* @author 鹏宇
	* @date 2015-11-12 下午8:47:59
	 */
	private HorizontalPanel createcontrNbrsearchForm(){
		HorizontalPanel hPanel;
		contrNbrsearchForm= new KylinForm();
		contrNbrsearchForm.setWidth("98%");
		contrNbrsearchForm.setCol(3);
		contrNbrsearchForm.setField(uCcsLoan.ContrNbr().required(true));
		
		KylinButton searchButton = ClientUtils.createSearchButton(new IClickEventListener() {
			
			@Override
			public void onClick() {
				if(contrNbrsearchForm.valid()){
					RPC.ajax("rpc/waiveServer/getLoanList", new RpcCallback<Data>() {

						@Override
						public void onSuccess(Data result) {
							if(result != null){
								loanGrid.clearData();
								planGrid.clearData();
								loanDetailForm.getUi().clear();
								planDetailForm.getUi().clear();
								exemptGrid.clearData();
								WaivePage.this.updateLoanKylinGrid(result);
							}
						}
					}, contrNbrsearchForm.getFieldValue(uCcsLoan.ContrNbr().getName()));
				}
			}
		});
		hPanel = CommonUiUtils.lineLayoutForm(contrNbrsearchForm, searchButton, null, null);
		return hPanel;
	}
	
	/**
	* @Description 创建根据账户号/账户类型查询贷款交易的form
	* @author 鹏宇
	* @date 2015-11-13 上午9:22:47
	 */
	private HorizontalPanel createacctNbrSearchForm(){
		HorizontalPanel hPanel;
		
		acctNbrSearchForm = new KylinForm();
		acctNbrSearchForm.setWidth("98%");
		acctNbrSearchForm.setCol(3);
		acctNbrSearchForm.setField(uCcsLoan.AcctNbr().required(true),uCcsLoan.AcctType().columnRender().required(true));
		
		KylinButton searchButton = ClientUtils.createSearchButton(new IClickEventListener() {
			
			@Override
			public void onClick() {
				if(acctNbrSearchForm.valid()){
					final String acctNbr = acctNbrSearchForm.getFieldValue(uCcsLoan.AcctNbr().getName());
					final String acctType = acctNbrSearchForm.getFieldValue(uCcsLoan.AcctType().getName());
					RPC.ajax("rpc/waiveServer/getLoanListByAcctNbr", new RpcCallback<Data>() {

						@Override
						public void onSuccess(Data result) {
							if(result != null){
								loanGrid.clearData();
								planGrid.clearData();
								loanDetailForm.getUi().clear();
								planDetailForm.getUi().clear();
								exemptGrid.clearData();
								WaivePage.this.updateLoanKylinGrid(result);
							}
						}
					},acctNbr,acctType);
				}
			}
		});
		
		hPanel = CommonUiUtils.lineLayoutForm(acctNbrSearchForm, searchButton, null, null);
		return hPanel;
	}
	
	/**
	* @Description 分期计划的Grid
	* @author 鹏宇
	* @date 2015-11-4 上午11:23:38
	 */
	private KylinGrid createLoanGrid(){
		loanGrid = new KylinGrid();
		loanGrid.setHeight("350px");
		loanGrid.setWidth("100%");
		
		loanGrid.setColumns(
				uCcsLoan.LoanId(),
				uCcsLoan.AcctNbr(),
				uCcsLoan.AcctType().columnRender(),
				uCcsLoan.LogicCardNbr()
				);
		
		loanGrid.loadData();
		loanGrid.getSetting().onSelectRow(new ISelectRowEventListener() {
			
			@Override
			public void selectRow(MapData rowdata, String rowid,EventObjectHandler rowobj) {
				planDetailForm.getUi().clear();
				loanDetailForm.setFormData(rowdata);
			}
		});
		
		return loanGrid;
	}
	
	/**
	* @Description 信用计划Grid
	* @author 鹏宇
	* @date 2015-11-4 下午1:58:28
	 */
	private KylinGrid createPlanGrid(){
		planGrid = new KylinGrid();
		planGrid.setHeight("350px");
		planGrid.setWidth("100%");
		planGrid.setColumns(
				uCcsPlan.AcctNbr(),
				uCcsPlan.PlanType().columnRender(),
				uCcsPlan.PlanId(),
				uCcsPlan.LogicCardNbr(),
				uCcsPlan.RefNbr());
		
		planGrid.loadData();
		planGrid.getSetting().onSelectRow(new ISelectRowEventListener() {
			
			@Override
			public void selectRow(MapData rowdata, String rowid,EventObjectHandler rowobj) {
				planDetailForm.getUi().clear();
				planDetailForm.setFormData(rowdata);
			}
		});
		
		return planGrid;		
	}

	@Override
	public IsWidget createPage() {
		VerticalPanel panel = new VerticalPanel();
		panel.setHeight("100%");
		panel.setWidth("100%");
		
		panel.add(createcontrNbrsearchForm());
		panel.add(createacctNbrSearchForm());
		
		//中间的Grid区域
		HorizontalPanel hLayout = new HorizontalPanel();
		hLayout.setWidth("98%");
		hLayout.setHeight("350px");
		hLayout.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
		

		//分期计划的Grid塞进hlayout里
		hLayout.add(createLoanGrid());
		
		//双击时给豁免Gird赋值
		loanGrid.addDblClickListener(new IDblClickRowEventListener() {
			
			@Override
			public void onDblClickRow(MapData data, String rowid, EventObjectHandler row) {
				RPC.ajax("rpc/waiveServer/getExempt", new RpcCallback<Data>() {

					@Override
					public void onSuccess(Data result) {
						if(result != null){
							exemptGrid.loadData(result);
						}
					}
				}, data.getDate(uCcsLoan.AcctNbr().getName()));
			}
		});
		
		//中间的查询按钮
		fetchPlanListBtn = ClientUtils.createSearchButton(new IClickEventListener() {
			
			@Override
			public void onClick() {
				if(loanGrid.getUi().getSelectedRows().size() <= 0){
					Dialog.alert(constants.chooseOneItem());                             //没有选择时提示选择
					return;
				}
				RPC.ajax("rpc/waiveServer/getPlanList", new RpcCallback<Data>() {

					@Override
					public void onSuccess(Data result) {
						planGrid.clearData();
						WaivePage.this.updatePlanKylinGrid(result);
					}
				}, loanGrid.getUi().getSelectedRow().getString(uCcsLoan.AcctNbr().getName()),loanGrid.getUi().getSelectedRow().getString(uCcsLoan.AcctType().getName()));
			}
		});
		
		hLayout.add(fetchPlanListBtn);
		
		//创建右边的Grid

		hLayout.add(createPlanGrid());
		
		//把搜索表单添加到页面上
		panel.add(hLayout);
		tab = createTab();
		panel.add(tab);
		
		return panel;
	}
	
	/**
	* @Description 分期信息详情的form
	* @author 鹏宇
	* @date 2015-11-4 下午3:18:29
	 */
	private KylinForm createLoanDetailForm(){
		loanDetailForm =  new KylinForm();
		loanDetailForm.getSetting().labelWidth(140);

		loanDetailForm.setCol(4);
		loanDetailForm.setWidth("98%");
		
		loanDetailForm.setField(
				uCcsLoan.Org().readonly(true),
				uCcsLoan.LoanId().readonly(true),
				uCcsLoan.AcctNbr().readonly(true),
				uCcsLoan.AcctType().columnRender().readonly(true),
				uCcsLoan.RefNbr().readonly(true),
				uCcsLoan.DueBillNo().readonly(true),
				uCcsLoan.LogicCardNbr().readonly(true),
				uCcsLoan.CardNbr().readonly(true),
				uCcsLoan.RegisterDate().readonly(true),
				uCcsLoan.RequestTime().readonly(true),
				uCcsLoan.LoanType().columnRender().readonly(true),
				uCcsLoan.LoanStatus().columnRender().readonly(true),
				uCcsLoan.LoanInitTerm().readonly(true),
				uCcsLoan.CurrTerm().readonly(true),
				uCcsLoan.RemainTerm().readonly(true),
				uCcsLoan.LoanInitPrin().readonly(true),
				uCcsLoan.LoanFixedPmtPrin().readonly(true),
				uCcsLoan.LoanFirstTermPrin().readonly(true),
				uCcsLoan.LoanFinalTermPrin().readonly(true),
				uCcsLoan.LoanInitFee().readonly(true),
				uCcsLoan.LoanFixedFee().readonly(true),
				uCcsLoan.LoanFirstTermFee().readonly(true),
				uCcsLoan.LoanFinalTermFee().readonly(true),
				uCcsLoan.UnstmtPrin().readonly(true),
				uCcsLoan.UnstmtFee().readonly(true),
				uCcsLoan.ActiveDate().readonly(true),
				uCcsLoan.PaidOutDate().readonly(true),
				uCcsLoan.TerminalDate().readonly(true),
				uCcsLoan.TerminalReasonCd().columnRender().readonly(true),
				uCcsLoan.PaidPrincipal().readonly(true),
				uCcsLoan.PaidInterest().readonly(true),
				uCcsLoan.PaidFee().readonly(true),
				uCcsLoan.LoanCurrBal().readonly(true),
				uCcsLoan.LoanBalXfrout().readonly(true),
				uCcsLoan.LoanBalXfrin().readonly(true),
				uCcsLoan.LoanPrinXfrout().readonly(true),
				uCcsLoan.LoanPrinXfrin().readonly(true),
				uCcsLoan.LoanFeeXfrin().readonly(true),
				uCcsLoan.LoanFeeXfrout().readonly(true),
				uCcsLoan.OrigTxnAmt().readonly(true),
				uCcsLoan.OrigTransDate().readonly(true),
				uCcsLoan.OrigAuthCode().readonly(true),
				uCcsLoan.LoanCode().readonly(true),
				uCcsLoan.RegisterId().readonly(true),
				uCcsLoan.ExtendInitPrin().readonly(true),
				uCcsLoan.ExtendDate().readonly(true),
				uCcsLoan.BefExtendFixedPmtPrin().readonly(true),
				uCcsLoan.BefExtendInitTerm().readonly(true),
				uCcsLoan.BefExtendFirstTermPrin().readonly(true),
				uCcsLoan.BefExtendFinalTermPrin().readonly(true),
				uCcsLoan.BefExtendInitFee().readonly(true),
				uCcsLoan.BefExtendFixedFee().readonly(true),
				uCcsLoan.BefExtendFirstTermFee().readonly(true),
				uCcsLoan.BefExtendFinalTermFee().readonly(true),
				uCcsLoan.ExtendFirstTermFee().readonly(true),
				uCcsLoan.LoanFeeMethod().readonly(true),
				uCcsLoan.InterestRate().readonly(true),
				uCcsLoan.PenaltyRate().readonly(true),
				uCcsLoan.CompoundRate().readonly(true),
				uCcsLoan.FloatRate().readonly(true),
				uCcsLoan.LoanExpireDate().readonly(true),
				uCcsLoan.LoanAgeCode().readonly(true),
				uCcsLoan.PaymentHst().readonly(true),
				uCcsLoan.CtdRepayAmt().readonly(true),
				uCcsLoan.PastExtendCnt().readonly(true),
				uCcsLoan.PastShortenCnt().readonly(true),
				uCcsLoan.AdvPmtAmt().readonly(true),
				uCcsLoan.LastActionDate().readonly(true),
				uCcsLoan.LastActionType().columnRender().readonly(true),
				uCcsLoan.ContrNbr().readonly(true),
				uCcsLoan.GuarantyId().readonly(true),
				uCcsLoan.StampdutyAmt().readonly(true),
				uCcsLoan.UnstmtStampdutyAmt().readonly(true),
				uCcsLoan.LoanStampdutyAmt().readonly(true),
				uCcsLoan.PaidStampdutyAmt().readonly(true),
				uCcsLoan.TotLifeInsuAmt().readonly(true),
				uCcsLoan.UnstmtLifeInsuAmt().readonly(true),
				uCcsLoan.PastLifeInsuAmt().readonly(true),
				uCcsLoan.PaidLifeInsuAmt().readonly(true),
				uCcsLoan.LifeInsuFeeRate().readonly(true),
				uCcsLoan.StampdutyRate().readonly(true),
				uCcsLoan.InsuranceRate().readonly(true),
				uCcsLoan.OverdueDate().readonly(true),
				uCcsLoan.LastPenaltyDate().readonly(true),
				uCcsLoan.InsuranceAmt().readonly(true),
				uCcsLoan.UnstmtInsuranceAmt().readonly(true),
				uCcsLoan.LoanInsuranceAmt().readonly(true),
				uCcsLoan.PaidInsuranceAmt().readonly(true),
				uCcsLoan.LoanInsFeeMethod().readonly(true),
				uCcsLoan.StampdutyMethod().columnRender().readonly(true),
				uCcsLoan.LifeInsuFeeMethod().columnRender().readonly(true),
				uCcsLoan.CreateTime().readonly(true),
				uCcsLoan.CreateUser().readonly(true),
				uCcsLoan.LstUpdTime().readonly(true),
				uCcsLoan.LstUpdUser().readonly(true),
				uCcsLoan.JpaVersion().readonly(true),
				uCcsLoan.MaxCpd().readonly(true),
				uCcsLoan.MaxCpdDate().readonly(true),
				uCcsLoan.MaxDpd().readonly(true),
				uCcsLoan.MaxDpdDate().readonly(true),
				uCcsLoan.StampCustomInd().readonly(true),
				uCcsLoan.IsOffsetRate().readonly(true),
				uCcsLoan.CpdBeginDate().readonly(true),
				uCcsLoan.TotPrepayPkgAmt().readonly(true),
				uCcsLoan.UnstmtPrepayPkgAmt().readonly(true),
				uCcsLoan.PastPrepayPkgAmt().readonly(true),
				uCcsLoan.PaidPrepayPkgAmt().readonly(true),
				uCcsLoan.PrepayPkgFeeRate().readonly(true),
				uCcsLoan.PrepayPkgFeeMethod().columnRender().readonly(true),
				uCcsLoan.LoanFeeDefId().readonly(true));
		
		return loanDetailForm;
	}
	
	/**
	* @Description 创建Tab
	* @author 鹏宇
	* @date 2015-11-13 上午10:12:06
	 */
	private Tab createTab(){
		
		TabSetting tabSetting = new TabSetting().dblClickToClose(true).dragToMove(true).showSwitch(true);
		tab = new Tab(tabSetting);
		tab.setWidth("98%");
		tab.setHeight("350px");
		
		//分期信息详情
		ScrollPanel loanDetailScrollPanel = new ScrollPanel();
		loanDetailScrollPanel.add(createLoanDetailForm());
		loanDetailScrollPanel.setHeight("300px");
		loanItemSetting = new TabItemSetting("loanDetailTab", constants.loanDetail());
		tab.addItem(loanItemSetting, loanDetailScrollPanel);
		
		//信用计划详情
		ScrollPanel planDetailScrollPanel = new ScrollPanel();
		planDetailScrollPanel.add(createPlanDetailForm());
		planDetailScrollPanel.setHeight("300px");
		planItemSetting = new TabItemSetting("planDetailTab", constants.planDetail());
		tab.addItem(planItemSetting,planDetailScrollPanel);
		
		exemptItemSetting = new TabItemSetting("exemptDetailTab",constants.exemptDetailTab());
		tab.addItem(exemptItemSetting, createexemptGrid());
		
		return tab;
	}
	
	public KylinGrid createexemptGrid(){
		exemptGrid = new KylinGrid();
		exemptGrid.setWidth("100%");
		exemptGrid.setHeight("300px");
		exemptGrid.setColumns(
				uCcsTxnWaiveLog.ContrNbr().readonly(true).setColumnWidth(140),
				uCcsTxnWaiveLog.AcctNbr().readonly(true).setColumnWidth(140),
				uCcsTxnWaiveLog.Currency().readonly(true).setColumnWidth(140),
				uCcsTxnWaiveLog.TxnAmt().readonly(true).setColumnWidth(140),
				uCcsTxnWaiveLog.BucketType().columnRender().readonly(true).setColumnWidth(140),
				uCcsTxnWaiveLog.CardNbr().readonly(true).setColumnWidth(140),
				uCcsTxnWaiveLog.CreateTime().readonly(true).setColumnWidth(140),
				uCcsTxnWaiveLog.AdjState().readonly(true).setColumnWidth(140)
				);
		return exemptGrid;
	}
	
	/**
	* @Description 信用计划详情的form
	* @author 鹏宇
	* @date 2015-11-4 下午3:19:58
	 */
	public KylinForm createPlanDetailForm(){
		
		planDetailForm = new KylinForm();
		planDetailForm.getSetting().labelWidth(130);
		planDetailForm.setCol(4);
		planDetailForm.setWidth("98%");
		
		bucketTypeColumnHelper = new TextColumnHelper("bucketType","余额成分类型",50);
		remarkColumnHelper = new TextColumnHelper("remark", "备注", 120);
		
		inputMoney = new DecimalColumnHelper("inputMoney", "金额", 20, new BigDecimal(0), new BigDecimal(999999999) ,  2);
		
		planDetailForm.setField(
				uCcsPlan.Org().readonly(true),
				uCcsPlan.PlanId().readonly(true),
				uCcsPlan.AcctNbr().readonly(true),
				uCcsPlan.AcctType().columnRender().readonly(true),
				uCcsPlan.LogicCardNbr().readonly(true),
				uCcsPlan.PlanNbr().readonly(true),
				uCcsPlan.PlanType().columnRender().readonly(true),
				uCcsPlan.ProductCd().readonly(true),
				uCcsPlan.RefNbr().readonly(true),
				uCcsPlan.CurrBal().readonly(true),
				uCcsPlan.BegBal().readonly(true),
				uCcsPlan.DisputeAmt().readonly(true),
				uCcsPlan.TotDueAmt().readonly(true),
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
				uCcsPlan.UserAmt6().readonly(true),
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
				uCcsPlan.LastPmtDate().readonly(true),
				uCcsPlan.Term().readonly(true),
				uCcsPlan.CtdStampdutyAmt().readonly(true),
				uCcsPlan.PastStampdutyAmt().readonly(true),
				uCcsPlan.CtdLifeInsuAmt().readonly(true),
				uCcsPlan.PastLifeInsuAmt().readonly(true),
				uCcsPlan.CtdMulctAmt().readonly(true),
				uCcsPlan.PastMulctAmt().readonly(true),
				uCcsPlan.CreateTime().readonly(true),
				uCcsPlan.CreateUser().readonly(true),
				uCcsPlan.LstUpdTime().readonly(true),
				uCcsPlan.LstUpdUser().readonly(true),
				uCcsPlan.JpaVersion().readonly(true),
				uCcsPlan.CtdPrepayPkgFee().readonly(true),
				uCcsPlan.PastPrepayPkgFee().readonly(true),
				bucketTypeColumnHelper.asSelectItem().required(true),
				inputMoney.required(true),                   //输入金额
				remarkColumnHelper.asTextArea().required(true)
	        	);
		
		exemptButton = new KylinButton(constants.shougonghuomian(),null);
		exemptButton.addClickEventListener(new IClickEventListener() {
			@Override
			public void onClick() {
				if(! planDetailForm.valid() || planDetailForm.getFieldValue("planId") == null){
					return ;
				}
				
				String money = planDetailForm.getFieldValue("inputMoney");
				if(money.indexOf(".") != -1){           //金额包含小数点
					if((money.length()-money.indexOf(".")-1) > 2){
						Dialog.alert("豁免金额小数点后不能超过两位");
						return ;
					}
				}
				
				String loanId = loanDetailForm.getFieldValue(uCcsLoan.LoanId().getName());
				RPC.ajax("rpc/waiveServer/exemptWaive", new RpcCallback<Data>() {

					@Override
					public void onSuccess(Data result) {
//						planDetailForm.getUi().clear();
						Dialog.tipNotice(result.toString());                     //业务处理
					}
				}, planDetailForm.getSubmitData().asMapData(),loanId);
			}
		});
		
		planDetailForm.addButton(exemptButton);
		
		return planDetailForm;
	}
	
	public ListData setFieldSelectData(){
		ListData uiShowData = new ListData();
		for(BucketType bucketType : BucketType.values()){
			if(bucketType.equals(BucketType.Mulct) || bucketType.equals(BucketType.Penalty) || bucketType.equals(BucketType.CardFee) ||  bucketType.equals(BucketType.SVCFee) || bucketType.equals(BucketType.LatePaymentCharge) || bucketType.equals(BucketType.LifeInsuFee) ||  bucketType.equals(BucketType.Interest) ||   bucketType.equals(BucketType.TXNFee)){
			MapData mapdata = new MapData();
			mapdata.put("id", bucketType+"");
			mapdata.put("text" , gettext(bucketType+""));
			uiShowData.add(mapdata);
			}
		}
		return uiShowData;
	}
	
	private String gettext(String id){
		if(id.equals("CardFee")){
			return bucketTypeConstants.CARDFEE();
		}
		if(id.equals("SVCFee")){
//			return bucketTypeConstants.SVCFEE();
			return "分期手续费";
		}
		if(id.equals("LatePaymentCharge")){
			return bucketTypeConstants.LATEPAYMENTCHARGE();
		}
		if(id.equals("LifeInsuFee")){
			return bucketTypeConstants.LIFEINSUFEE();
		}
		if(id.equals("Interest")){
			return bucketTypeConstants.INTEREST();
		}
		if(id.equals("TXNFee")){
			return bucketTypeConstants.TXNFEE();
		}
		if(id.equals("Mulct")){
			return bucketTypeConstants.MULCT();
		}
		if(id.equals("Penalty")){
			return bucketTypeConstants.PENALTY();
		}
		else{
			return null;
		}
	}
	
	@Override
	public void refresh() {
		contrNbrsearchForm.getUi().clear();
		acctNbrSearchForm.getUi().clear();
		loanGrid.clearData();
		planGrid.clearData();
		exemptGrid.clearData();
		loanDetailForm.getUi().clear();
		planDetailForm.getUi().clear();
		exemptButton.setVisible(true);
		planDetailForm.setFieldSelectData("bucketType", setFieldSelectData());
		
//		RPC.ajax("rpc/t3201Server/getTxnCd", new RpcCallback<Data>() {
//
//			@Override
//			public void onSuccess(Data result) {
//				planDetailForm.setFieldSelectData(uTxnCd.TxnCd().getName(), result.asListData());
//			}
//		});                      //获取交易码的方法
	}
	
}
