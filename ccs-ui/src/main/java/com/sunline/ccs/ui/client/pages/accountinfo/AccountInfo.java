package com.sunline.ccs.ui.client.pages.accountinfo;


import java.math.BigDecimal;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsAcct;
import com.sunline.ccs.infrastructure.client.ui.UCcsCardLmMapping;
import com.sunline.ccs.infrastructure.client.ui.UCcsCustomer;
import com.sunline.ccs.infrastructure.client.ui.UCcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.ui.client.commons.CommonUiUtils;
import com.sunline.ccs.ui.shared.PublicConst;
import com.sunline.kylin.web.ark.client.helper.ColumnHelper;
import com.sunline.kylin.web.ark.client.helper.EventEnum;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.core.client.function.IFunction;
import com.sunline.ui.event.client.EventObjectHandler;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.IColumnRenderFunctionListener;
import com.sunline.ui.grid.client.listener.IDblClickRowEventListener;
import com.sunline.ui.grid.client.listener.ISelectRowEventListener;
import com.sunline.ui.layout.client.Layout;
import com.sunline.ui.layout.client.LayoutSetting;
import com.sunline.ui.tab.client.Tab;
import com.sunline.ui.tab.client.TabItemSetting;
import com.sunline.ui.tab.client.TabSetting;

/**
 * 
 * @see 类名：AccountInfo
 * @see 描述：用户账户信息查询
 *
 * @see 创建日期：   Jun 17, 20159:24:38 PM
 * @author yeyu
 * 
 * @see 修改记录：
 */
@Singleton
public class AccountInfo extends Page {
	
	@Inject
	private UCcsAcct uCcsAcct;
	@Inject
	private UCcsCustomer uCcsCustomer;
	@Inject
	private UCcsPlan uCcsPlan;
	@Inject
	private UCcsCardLmMapping uCcsCardLmMapping;
	@Inject 
	private AccountInfoConstants constants;
	
	private KylinGrid grid;
	
	private KylinGrid creditGrid;
	
	private KylinForm custInfoForm;
	
	private KylinForm userInfoDetailsForm;
	
	private KylinForm creditPlanForm;
	
	private HorizontalPanel hPanel;
	/**
	 * 界面主窗体
	 */
	private VerticalPanel mainWindow;
	
	@Override
	public IsWidget createPage() {
		mainWindow = new VerticalPanel();
		mainWindow.setWidth("100%");
		mainWindow.setHeight("100%");
		
		this.buildCustInfoForm(mainWindow);
		//this.buildDataGrid(mainWindow);
		this.buildGrid(mainWindow);
		return mainWindow;
	}
	
	private void buildGrid(VerticalPanel mainWindow){
		grid = this.getAcctInfoDataGrid();
		VerticalPanel gridPanel = new VerticalPanel();
		gridPanel.setWidth("98%");
		grid.setHeight("350px");
		gridPanel.add(grid);
		creditGrid = this.getCreditPlanDataGrid();
		creditGrid.setWidth("100%");
		creditGrid.setHeight("350px");
		hPanel = new HorizontalPanel();
		hPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		hPanel.setWidth("100%");
		hPanel.setHeight("350px");
		grid.loadDataFromUrl("rpc/accountInfoServer/getAcctList");
		grid.getSetting().delayLoad(true);
		grid.addDblClickListener(new IDblClickRowEventListener(){
			@Override
			public void onDblClickRow(MapData data, String rowid,EventObjectHandler row) {
				//TODO
				userInfoDetailsForm.getUi().setData(data);
				blockcodeListGrid.loadData(data.getData(PublicConst.KEY_BLOCKCODE_LIST).asMapData());
			}
		});
		creditGrid.addDblClickListener(new IDblClickRowEventListener(){
			@Override
			public void onDblClickRow(MapData data, String rowid,EventObjectHandler row) {
				creditPlanForm.getUi().setData(data);
			}
		});
		KylinButton btnSearch = new KylinButton("获取信用计划",null);
		btnSearch.addClickEventListener(new IClickEventListener(){
			@Override
			public void onClick() {
				int gotSelected = grid.getUi().getSelectedRows().size();
				if(gotSelected > 0 && gotSelected <= 1){
					MapData selectedRow = grid.getUi().getSelectedRow();
					String acctNbr = selectedRow.getString(CcsAcct.P_AcctNbr);
					String acctType = selectedRow.getString(CcsAcct.P_AcctType);
					creditGrid.getUi().setParm(CcsAcct.P_AcctNbr,acctNbr);
					creditGrid.getUi().setParm(CcsAcct.P_AcctType,acctType);
					creditGrid.loadDataFromUrl("rpc/accountInfoServer/getPlanList");
				}else{
					Window.alert("请至少选择一条记录");
				}
			}
		});
		btnSearch.updateWidth(110);
//		buttonGroup.add(btnSearch);
		hPanel.add(grid);
		hPanel.add(btnSearch);
		hPanel.add(creditGrid);
		
		Tab tabPanel = this.getTabPanel();
		tabPanel = this.buildTabPanelSetting(tabPanel);
		
//		mainWindow.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		
		mainWindow.add(hPanel);
		
		VerticalPanel tPanel = new VerticalPanel();
		tPanel.setWidth("100%");
		tPanel.add(tabPanel);
		mainWindow.add(tPanel);
	}
	
	@Override
	public void refresh() {
		creditPlanForm.getUi().clear();
		userInfoDetailsForm.getUi().clear();
		custInfoForm.getUi().clear();
		creditGrid.clearData();
		grid.clearData();
		blockcodeListGrid.clearData();
		grid.loadDataFromUrl("rpc/accountInfoServer/getAcctList");
	}

	/**
	 * 
	 * @see 方法名：buildCustInfoForm 
	 * @see 描述：构造客户信息查询表单
	 * @see 创建日期：Jun 17, 20159:34:58 PM
	 * @author yeyu
	 *  
	 * @param mainWindow 主窗体组件
	 * 
	 * @see 修改记录： 
	 * 
	 */
	private void buildCustInfoForm(VerticalPanel mainWindow){
		custInfoForm = new KylinForm();
		custInfoForm.setField(new ColumnHelper[]{
				uCcsAcct.ContrNbr(),
				uCcsAcct.GuarantyId(),
				uCcsCustomer.IdType().setNewline(true),
				uCcsCustomer.IdNo(),
				uCcsCustomer.MobileNo()
		});
		KylinButton btnSearch = new KylinButton("查询",null);
		btnSearch.addClickEventListener(new IClickEventListener(){
			@Override
			public void onClick() {
			    creditPlanForm.getUi().clear();
			    userInfoDetailsForm.getUi().clear();
			    creditGrid.clearData();
			    grid.loadData(custInfoForm);
			}
		});
		mainWindow.add(CommonUiUtils.lineLayoutForm(custInfoForm, btnSearch, null, null));
	}
	
	/**
	 * 
	 * @see 方法名：buildDataGrid 
	 * @see 描述：创建账户列表
	 * @see 创建日期：Jun 17, 20159:53:23 PM
	 * @author yeyu
	 *  
	 * @param mainWindow 主窗体实例对象
	 * 
	 * @see 修改记录： 
	 */
	private void buildDataGrid(VerticalPanel mainWindow){
		Layout layout = this.getSplitLayout();
		layout.addLeftWidget(this.getAcctInfoDataGrid());
		layout.addRightWidget(this.getCreditPlanDataGrid());
		VerticalPanel buttonGroup = new VerticalPanel();
		buttonGroup.setHeight("100%");
		buttonGroup.setWidth("120px");
		KylinButton btnCreditPlan = new KylinButton("获取信用计划",null);
		btnCreditPlan.updateWidth(120);
		btnCreditPlan.addClickEventListener(new IClickEventListener(){
			@Override
			public void onClick() {
				int gotSelected = grid.getUi().getSelectedRows().size();
				if(gotSelected > 0 && gotSelected <= 1){
					MapData selectedRow = grid.getUi().getSelectedRow();
					String acctNbr = selectedRow.getString(CcsAcct.P_AcctNbr);
					String acctType = selectedRow.getString(CcsAcct.P_AcctType);
					creditGrid.getUi().setParm(CcsAcct.P_AcctNbr,acctNbr);
					creditGrid.getUi().setParm(CcsAcct.P_AcctType,acctType);
					creditGrid.loadDataFromUrl("rpc/accountInfoServer/getPlanList");
				}else{
					Window.alert("请至少选择一条记录");
				}
			}
		});
		buttonGroup.add(btnCreditPlan);
		layout.addWidget(buttonGroup);
		this.buildTabPanel(layout);
		grid.getSetting().delayLoad(true);
		grid.getSetting().onSelectRow(new ISelectRowEventListener(){

			@Override
			public void selectRow(MapData rowdata, String rowid,EventObjectHandler rowobj) {
				userInfoDetailsForm.getUi().setData(rowdata);
			}
		});
		creditGrid.getSetting().onSelectRow(new ISelectRowEventListener(){

			@Override
			public void selectRow(MapData rowdata, String rowid,EventObjectHandler rowobj) {
				creditPlanForm.getUi().setData(rowdata);
			}
		});
		mainWindow.add(layout);
	}
	
	/**
	 * 
	 * @see 方法名：getAcctInfoDataGrid 
	 * @see 描述：获取DataGrid实例对象
	 * @see 创建日期：Jun 17, 20159:54:07 PM
	 * @author yeyu
	 *  
	 * @return
	 * 
	 * @see 修改记录： 
	 */
	private KylinGrid getAcctInfoDataGrid(){
		grid = new KylinGrid();
		grid.getSetting().delayLoad(true);
		grid.setColumns(new ColumnHelper[]{
				uCcsAcct.AcctNbr().setColumnWidth(150),
				uCcsAcct.AcctType().setColumnWidth(200).columnRender(),
				uCcsAcct.ProductCd().setColumnWidth(200)
		});
		return grid;
	}
	
	/**
	 * 
	 * @see 方法名：getCreditPlanDataGrid 
	 * @see 描述：创建信用计划列表
	 * @see 创建日期：Jun 18, 20151:54:04 PM
	 * @author yeyu
	 *  
	 * @return com.sunline.kylin.web.ark.client.ui.KylinGrid
	 * 
	 * @see 修改记录： 
	 */
	private KylinGrid getCreditPlanDataGrid(){
		creditGrid = new KylinGrid();
		creditGrid.getSetting().delayLoad(true);
		creditGrid.setColumns(new ColumnHelper[]{
				uCcsPlan.PlanId().setColumnWidth(100),
				uCcsPlan.PlanNbr().setColumnWidth(150),
				uCcsPlan.PlanType().setColumnWidth(150).columnRender(),
				uCcsPlan.AcctNbr().setColumnWidth(150)
		});
		return creditGrid;
	}
	
	/**
	 * 
	 * @see 方法名：getSplitLayout 
	 * @see 描述：获取Layout实例对象
	 * @see 创建日期：Jun 17, 20159:54:32 PM
	 * @author yeyu
	 *  
	 * @return
	 * 
	 * @see 修改记录： 
	 */
	private Layout getSplitLayout(){
		LayoutSetting setting = new LayoutSetting();
		setting.allowBottomResize(false).allowCenterBottomResize(false)
		.allowLeftCollapse(false).allowLeftResize(false).allowRightCollapse(false)
		.allowRightResize(false).allowTopResize(false).hasTop(false).hasBottom(true).leftWidth(675).rightWidth(675).centerWidth(120).bottomHeight(180);
		Layout layout = new Layout(setting);
		layout.setWidth("100%");
		layout.setHeight("200px");
		return layout;
	}
	
	/**
	 * 
	 * @see 方法名：buildTabPanel 
	 * @see 描述：构造TabPanel
	 * @see 创建日期：Jun 18, 20152:04:56 PM
	 * @author yeyu
	 *  
	 * @param layout 布局对象实例
	 * 
	 * @see 修改记录： 
	 */
	private void buildTabPanel(Layout layout){
		Tab tabPanel = this.getTabPanel();
		tabPanel = this.buildTabPanelSetting(tabPanel);
		layout.addBottomWidget(tabPanel);
	}
	
	/**
	 * 
	 * @see 方法名：buildTabPanelSetting 
	 * @see 描述：构造TabPanelSetting
	 * @see 创建日期：Jun 18, 20152:03:59 PM
	 * @author yeyu
	 *  
	 * @param tabPanel TabPanel实例
	 * @return com.sunline.ui.tab.client.Tab
	 * 
	 * @see 修改记录： 
	 */
	private Tab buildTabPanelSetting(Tab tabPanel){
		TabItemSetting userInfoDetails = new TabItemSetting("userInfoDetails","用户详细信息");
		userInfoDetailsForm = new KylinForm();
		TextColumnHelper tmepLmtBegDate = new TextColumnHelper("tmepLmtBegDate","临时额度开始日期",10).columnRender(new IColumnRenderFunctionListener(){
		    @Override
		    public String render(MapData rowdata, int rowindex, String value,
			    EventObjectHandler column) {
			if(rowdata.asMapData().getString("tempLmtBegDate").equals("")){
			    return "";
			}else{
			    return rowdata.asMapData().getString("tempLmtBegDate");
			}
			
		    }
		});
		TextColumnHelper tempLmtEndDate = new TextColumnHelper("tempLmtEndDate","临时额度结束日期",10).columnRender(new IColumnRenderFunctionListener(){
		    @Override
		    public String render(MapData rowdata, int rowindex, String value,
			    EventObjectHandler column) {
			if(rowdata.asMapData().getString("tempLmtEndDate").equals("")){
			    return "";
			}else{
			    return rowdata.asMapData().getString("tempLmtEndDate");
			}
			
		    }
		});
		TextColumnHelper setupDate = new TextColumnHelper("setupDate","创建日期",10).columnRender(new IColumnRenderFunctionListener(){
		    @Override
		    public String render(MapData rowdata, int rowindex, String value,
			    EventObjectHandler column) {
			if(rowdata.asMapData().getString("setupDate").equals("")){
			    return "";
			}else{
			    return rowdata.asMapData().getString("setupDate");
			}
			
		    }
		});
		TextColumnHelper dormentDate = new TextColumnHelper("dormentDate","账户睡眠日期",10).columnRender(new IColumnRenderFunctionListener(){
		    @Override
		    public String render(MapData rowdata, int rowindex, String value,
			    EventObjectHandler column) {
			if(rowdata.asMapData().getString("dormentDate").equals("")){
			    return "";
			}else{
			    return rowdata.asMapData().getString("dormentDate");
			}
			
		    }
		});
		TextColumnHelper ovrlmtDate = new TextColumnHelper("ovrlmtDate","超限日期",10).columnRender(new IColumnRenderFunctionListener(){
		    @Override
		    public String render(MapData rowdata, int rowindex, String value,
			    EventObjectHandler column) {
			if(rowdata.asMapData().getString("ovrlmtDate").equals("")){
			    return "";
			}else{
			    return rowdata.asMapData().getString("ovrlmtDate");
			}
			
		    }
		});
		TextColumnHelper userDate1 = new TextColumnHelper("userDate1","用户自定义日期1",10).columnRender(new IColumnRenderFunctionListener(){
		    @Override
		    public String render(MapData rowdata, int rowindex, String value,
			    EventObjectHandler column) {
			if(rowdata.asMapData().getString("userDate1").equals("")){
			    return "";
			}else{
			    return rowdata.asMapData().getString("userDate1");
			}
			
		    }
		});
		TextColumnHelper userDate2 = new TextColumnHelper("userDate2","用户自定义日期2",10).columnRender(new IColumnRenderFunctionListener(){
		    @Override
		    public String render(MapData rowdata, int rowindex, String value,
			    EventObjectHandler column) {
			if(rowdata.asMapData().getString("userDate2").equals("")){
			    return "";
			}else{
			    return rowdata.asMapData().getString("userDate2");
			}
			
		    }
		});
		TextColumnHelper userDate3 = new TextColumnHelper("userDate3","用户自定义日期3",10).columnRender(new IColumnRenderFunctionListener(){
		    @Override
		    public String render(MapData rowdata, int rowindex, String value,
			    EventObjectHandler column) {
			if(rowdata.asMapData().getString("userDate3").equals("")){
			    return "";
			}else{
			    return rowdata.asMapData().getString("userDate3");
			}
			
		    }
		});
		TextColumnHelper userDate4 = new TextColumnHelper("userDate4","用户自定义日期4",10).columnRender(new IColumnRenderFunctionListener(){
		    @Override
		    public String render(MapData rowdata, int rowindex, String value,
			    EventObjectHandler column) {
			if(rowdata.asMapData().getString("userDate4").equals("")){
			    return "";
			}else{
			    return rowdata.asMapData().getString("userDate4");
			}
			
		    }
		});
		TextColumnHelper userDate5 = new TextColumnHelper("userDate5","用户自定义日期5",10).columnRender(new IColumnRenderFunctionListener(){
		    @Override
		    public String render(MapData rowdata, int rowindex, String value,
			    EventObjectHandler column) {
			if(rowdata.asMapData().getString("userDate5").equals("")){
			    return "";
			}else{
			    return rowdata.asMapData().getString("userDate5");
			}
			
		    }
		});
		//userInfoDetailsForm.setField(tmepLmtBegDate.readonly(true));
		userInfoDetailsForm.setField(new ColumnHelper[]{
				uCcsAcct.Org().readonly(true),
				uCcsAcct.AcctNbr().readonly(true),
				uCcsAcct.AcctType().readonly(true),
				uCcsAcct.ContrNbr().readonly(true),
				
				uCcsAcct.CustId().readonly(true),
				uCcsAcct.CustLmtId().readonly(true),
				uCcsAcct.ProductCd().readonly(true),
				uCcsAcct.DefaultLogicCardNbr().readonly(true),
				uCcsAcct.Currency().readonly(true),
				uCcsAcct.CreditLmt().readonly(true),
				uCcsAcct.TempLmt().readonly(true),
				tmepLmtBegDate.readonly(true),
				tempLmtEndDate.readonly(true),
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
				setupDate.readonly(true),
				dormentDate.readonly(true),
				//uCcsAcct.SetupDate().readonly(true),
				//uCcsAcct.DormentDate().readonly(true),
				ovrlmtDate.readonly(true),
				//uCcsAcct.OvrlmtDate().readonly(true),
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
				uCcsAcct.CollectInDate().readonly(true),
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
				/*uCcsAcct.UserDate1().readonly(true),
				uCcsAcct.UserDate2().readonly(true),
				uCcsAcct.UserDate3().readonly(true),
				uCcsAcct.UserDate4().readonly(true),
				uCcsAcct.UserDate5().readonly(true),
				uCcsAcct.UserDate6().readonly(true),*/
				userDate1.readonly(true),
				userDate2.readonly(true),
				userDate3.readonly(true),
				userDate4.readonly(true),
				userDate5.readonly(true),
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
				uCcsAcct.YtdRepayCnt().readonly(true),
				uCcsAcct.LtdRepayAmt().readonly(true),
				uCcsAcct.LtdRepayCnt().readonly(true),
				uCcsAcct.LastAgingDate().readonly(true),
				uCcsAcct.AgreementRateInd().readonly(true),
				uCcsAcct.AgreementRateExpireDate().readonly(true),
				uCcsAcct.AcctExpireDate().readonly(true),
				uCcsAcct.LtdLoanAmt().readonly(true),
				uCcsAcct.ApplicationNo().readonly(true),
				uCcsAcct.CustSource().readonly(true),
				uCcsAcct.AgreementRateInd().readonly(true),
				uCcsAcct.FeeRate().readonly(true),
				uCcsAcct.FeeAmt().readonly(true),
				uCcsAcct.LifeInsuFeeRate().readonly(true),
				uCcsAcct.LifeInsuFeeAmt().readonly(true),
				uCcsAcct.InsuranceRate().readonly(true),
				uCcsAcct.InsAmt().readonly(true),
				uCcsAcct.InstallmentFeeRate().readonly(true),
				uCcsAcct.InstallmentFeeAmt().readonly(true),
				uCcsAcct.PrepayPkgFeeRate().readonly(true),
				uCcsAcct.PrepayPkgFeeAmt().readonly(true),
				uCcsAcct.PenaltyRate().readonly(true),
				uCcsAcct.CompoundRate().readonly(true),
				uCcsAcct.InterestRate().readonly(true),
				uCcsAcct.StampAmt().readonly(true),
				uCcsAcct.StampdutyRate().readonly(true),
				uCcsAcct.ReplaceSvcFeeAmt().readonly(true),
				uCcsAcct.ReplaceSvcFeeRate().readonly(true),
				uCcsAcct.ReplacePenaltyRate().readonly(true),   //代收罚息利率
				uCcsAcct.PrepayPkgInd().readonly(true)
		});
		
		userInfoDetailsForm.getSetting().labelWidth(120);
		ScrollPanel userInfoDetialsFormPanel = this.getScrollPanel();
		userInfoDetialsFormPanel.add(userInfoDetailsForm);
		tabPanel.addItem(userInfoDetails, userInfoDetialsFormPanel);
		
		TabItemSetting creditPlan = new TabItemSetting("creditPlan","信用计划详细信息");
		ScrollPanel creditPlanFormPanel = this.getScrollPanel();
		creditPlanForm = new KylinForm();
		creditPlanForm.setField(new ColumnHelper[]{
						uCcsPlan.CompoundRate().readonly(true),
						uCcsPlan.CompoundAcru().readonly(true),
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
						uCcsPlan.PastMulctAmt().readonly(true),
						uCcsPlan.PastPrepayPkgFee().readonly(true),
						uCcsPlan.PastStampdutyAmt().readonly(true),
						uCcsPlan.PastPenalty().readonly(true),
						uCcsPlan.PastLifeInsuAmt().readonly(true),
						
						uCcsPlan.CtdPenalty().readonly(true),
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
						uCcsPlan.CtdDefbnpIntAcru().readonly(true),

						uCcsPlan.NodefbnpIntAcru().readonly(true),
						uCcsPlan.BegDefbnpIntAcru().readonly(true),
						uCcsPlan.UserAmt1().readonly(true),
						uCcsPlan.UserAmt2().readonly(true),
						uCcsPlan.UserAmt3().readonly(true),
						uCcsPlan.UserAmt4().readonly(true),
						uCcsPlan.UserAmt5().readonly(true),
						uCcsPlan.UserAmt6().readonly(true),
						uCcsPlan.CtdInsurance().readonly(true),
						uCcsPlan.CtdStampdutyAmt().readonly(true),
						uCcsPlan.CtdMulctAmt().readonly(true),
						uCcsPlan.CtdLifeInsuAmt().readonly(true),
						uCcsPlan.CtdPrepayPkgFee().readonly(true),
						uCcsPlan.InterestRate().readonly(true),
						uCcsPlan.AccruPrinSum().readonly(true),
						uCcsPlan.LastAccruPrinSum().readonly(true),
						uCcsPlan.PenaltyAcru().readonly(true),
						uCcsPlan.PenaltyRate().readonly(true),
						uCcsPlan.PastReplaceSvcFee().readonly(true),
						uCcsPlan.CtdReplaceSvcFee().readonly(true),
						uCcsPlan.PastCompound().readonly(true),
						uCcsPlan.CtdCompound().readonly(true),
						uCcsPlan.Term().readonly(true),
						uCcsPlan.CtdReplacePenalty().readonly(true),   //未出账单代收罚息
						uCcsPlan.PastReplacePenalty().readonly(true),  	//已出账单代收罚息
						uCcsPlan.ReplacePenaltyRate().readonly(true),   //代收罚息利率
						uCcsPlan.ReplacePenaltyAcru().readonly(true),   //代收罚息累计
						uCcsPlan.CtdReplaceMulct().readonly(true),		//未出账单代收罚金
						uCcsPlan.PastReplaceMulct().readonly(true), 		//已出账单代收罚金
						uCcsPlan.CtdReplaceLateFee().readonly(true), 		//未出账单代收滞纳金
						uCcsPlan.PastReplaceLateFee().readonly(true), 		//已出账单代收滞纳金
						uCcsPlan.PastReplaceTxnFee().readonly(true),
						uCcsPlan.CompensateAmt().readonly(true),
						uCcsPlan.CompensateStatus().readonly(true)
		});
		
		creditPlanForm.setFormReadOnly(true);
		creditPlanForm.getSetting().labelWidth(120);
		creditPlanFormPanel.add(creditPlanForm);
		tabPanel.addItem(creditPlan, creditPlanFormPanel);
		
		TabItemSetting lockingCode = new TabItemSetting("lockingCode","账户锁定码信息");
		blockcodeListGrid = this.getBlockcodeListGrid();
		blockcodeListGrid.setColumns(new ColumnHelper[]{
				new TextColumnHelper("blockcode", constants.blockCode(),100).setColumnWidth("20%"),
				new TextColumnHelper("description", constants.descTitle(),100).setColumnWidth("80%")
		});
		blockcodeListGrid.setHeight("300px");
		blockcodeListGrid.setWidth("98%");
		tabPanel.addItem(lockingCode, blockcodeListGrid);
		return tabPanel;
	}
	
	/**
	 * 
	 * @see 方法名：getButton 
	 * @see 描述：获取KylinButton实例
	 * @see 创建日期：Jun 18, 20153:58:08 PM
	 * @author Liming.Feng
	 *  
	 * @param btnText 按钮展示名称
	 * @param imgUrl 背景图片URL
	 * @return com.sunline.kylin.web.ark.client.ui.KylinButton
	 * 
	 * @see 修改记录： 
	 */
	public KylinButton getButton(String btnText,String imgUrl){
		KylinButton button = new KylinButton(btnText,imgUrl);
		return button;
	}
	
	/**
	 * 
	 * @see 方法名：getScrollPanel 
	 * @see 描述：获取ScrollPanel对象实例
	 * @see 创建日期：Jun 18, 20153:33:43 PM
	 * @author yeyu
	 *  
	 * @return
	 * 
	 * @see 修改记录： 
	 */
	private ScrollPanel getScrollPanel(){
		ScrollPanel scrollPanel = new ScrollPanel();
		scrollPanel.setHeight("300px");
		scrollPanel.setWidth("100%");
		return scrollPanel;
	}
	
	/**
	 * 
	 * @see 方法名：getTabPanel 
	 * @see 描述：获取TabPanel实例
	 * @see 创建日期：Jun 18, 20152:03:18 PM
	 * @author yeyu
	 *  
	 * @return com.sunline.ui.tab.client.Tab
	 * 
	 * @see 修改记录： 
	 */
	public Tab getTabPanel(){
		TabSetting setting = new TabSetting();
		setting.contextmenu(false);
		setting.dragToMove(false);
		setting.dblClickToClose(false);
		Tab tabPanel = new Tab(setting);
		tabPanel.setHeight("350px");
		tabPanel.setWidth("100%");
		return tabPanel;
	}
	
	private KylinGrid blockcodeListGrid;
	
	/**
	 * 
	 * @see 方法名：getBlockcodeListGrid 
	 * @see 描述：锁定码列表
	 * @see 创建日期：Jun 29, 201510:18:14 AM
	 * @author yeyu
	 *  
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private KylinGrid getBlockcodeListGrid(){
		blockcodeListGrid = new KylinGrid();
		blockcodeListGrid.setHeight("100%");
		blockcodeListGrid.setWidth("100%");
		return blockcodeListGrid;
	}
	
}
