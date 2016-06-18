/**
 * 
 */
package com.sunline.ccs.ui.client.pages.historybill;

import java.util.Date;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsAcct;
//import com.sunline.ccs.infrastructure.client.ui.UCcsCardLmMapping;
import com.sunline.ccs.infrastructure.client.ui.UCcsStatement;
import com.sunline.ccs.infrastructure.client.ui.UCcsTxnHst;
//import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.CcsStatement;
import com.sunline.ccs.ui.client.commons.CommonCcsDateUtils;
import com.sunline.ccs.ui.client.commons.CommonGlobalConstants;
import com.sunline.ccs.ui.client.commons.CommonUiUtils;
import com.sunline.kylin.web.ark.client.helper.DateColumnHelper;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.event.client.EventObjectHandler;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.IDblClickRowEventListener;
import com.sunline.ui.grid.client.listener.ISelectRowEventListener;
import com.sunline.ui.tab.client.Tab;
import com.sunline.ui.tab.client.TabItemSetting;
import com.sunline.ui.tab.client.TabSetting;

/**
 * 
 * @see 类名：QueryHistoryBill
 * @see 描述：查询历史订单
 *
 * @see 创建日期：   Jun 27, 201512:52:02 PM
 * @author Liming.Feng
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Singleton
public class QueryHistoryBill extends Page {
	
	@Inject
	private CommonGlobalConstants publicConstants;
	@Inject
	private QueryHistoryBillConstants constants;
	@Inject
	private UCcsStatement uCcsStatement;
	@Inject
	private UCcsTxnHst  uCcsTxnHst;
	@Inject UCcsAcct uCcsAcct;
	private KylinForm searchForm;
	private KylinForm stmtDetailForm;
	private KylinForm transDetailForm;
	private KylinGrid searchGrid = new KylinGrid();
	private KylinGrid transListGrid = new KylinGrid();
	// 账单明细信息
	private TabItemSetting stmtTabSetting;
	// 账单明细信息列表
	private TabItemSetting txnListTabSetting;
	// 账单明细
	private TabItemSetting txnDetailTabSetting;
	
	private Tab tab;

	@Override
	public IsWidget createPage() {
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");
		panel.setHeight("100%");
		
		panel.add(createSearchForm());
		panel.add(createListGrid());
		panel.add(createStmtInfoTabSet());
		
		return panel;
	}
	
	@Override
	public void refresh() {
		searchForm.getUi().clear();
		stmtDetailForm.getUi().clear();
		searchGrid.clearData();
		transListGrid.clearData();
	}
	
	/**
	 * 
	 * @see 方法名：createSearchForm 
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：Jun 27, 20151:07:36 PM
	 * @author Liming.Feng
	 *  
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@SuppressWarnings("all")
	private HorizontalPanel createSearchForm() {
		HorizontalPanel hPanel = new HorizontalPanel();
		searchForm = new KylinForm();
		{	
			searchForm.setWidth("98%");
			searchForm.getSetting().labelWidth(120);
			searchForm.setCol(5);
			// 起始日期
	        DateColumnHelper beginDateItem = new DateColumnHelper("beginDate", publicConstants.labelBeginDate(), true, false);
	        
			Date bdate = new Date();
			bdate = new CommonCcsDateUtils(bdate.getTime()).addYear(CommonCcsDateUtils.Unit.YEAR, -3).asDate();
			beginDateItem.startDate(bdate);
			bdate = new CommonCcsDateUtils(bdate.getTime()).addYear(CommonCcsDateUtils.Unit.YEAR, 23).asDate();
			beginDateItem.endDate(bdate);
			// 截止日期
			DateColumnHelper endDateItem = new DateColumnHelper("endDate", publicConstants.labelEndDate(), true, false);
			Date edate = new Date();
			edate = new CommonCcsDateUtils(edate.getTime()).addYear(CommonCcsDateUtils.Unit.YEAR, -3).asDate();
			endDateItem.startDate(edate);
			edate = new CommonCcsDateUtils(edate.getTime()).addYear(CommonCcsDateUtils.Unit.YEAR, 23).asDate();
			endDateItem.endDate(edate);
			
			searchForm.setField(uCcsAcct.ContrNbr().required(true), beginDateItem, endDateItem);
			
	    	KylinButton btnSearch = ClientUtils.createSearchButton(new IClickEventListener(){
				@Override
				public void onClick() {
					if(!searchForm.valid()) return;
					Data submitData = searchForm.getSubmitData();
					if (submitData.asMapData().getString("beginDate") != null) {
						searchGrid.getUi().setParm("bDate", submitData.asMapData().getString("beginDate"));
					}else{
						searchGrid.getUi().setParm("bDate",null);
					}
					if (submitData.asMapData().getString("endDate") != null) {
						searchGrid.getUi().setParm("eDate", submitData.asMapData().getString("endDate"));
					}else{
						searchGrid.getUi().setParm("eDate",null);
					}
					if(submitData.asMapData().getString(uCcsAcct.ContrNbr().getName()) != null){
						searchGrid.getUi().setParm(uCcsAcct.ContrNbr().getName(), submitData.asMapData().getString(uCcsAcct.ContrNbr().getName()));
					}
					searchGrid.clearData();
					searchGrid.loadDataFromUrl("rpc/t1201Server/getStmtList");
				}
			});
	    	hPanel = CommonUiUtils.lineLayoutForm(searchForm, btnSearch, null, null);
		}
		return hPanel;
	}
	
	/**
	 * 创建列表list显示
	 * @return
	 */
	private KylinGrid createListGrid() {
		searchGrid = new KylinGrid();
		searchGrid.setWidth("100%");
		searchGrid.setHeight("350px");
//		searchGrid.getSetting().usePager(false);
		searchGrid.setColumns(
				uCcsStatement.AcctNbr(),
				uCcsStatement.AcctType().columnRender(),
				uCcsStatement.StmtDate(),
				uCcsStatement.AgeCode(),
				uCcsStatement.LastStmtBal(),
				uCcsStatement.CtdAmtDb(),
				uCcsStatement.CtdAmtCr(),
				uCcsStatement.QualGraceBal(),
				uCcsStatement.StmtCurrBal(),
				uCcsStatement.TotDueAmt(),
				uCcsStatement.PmtDueDate()
				);
		
		searchGrid.getSetting().onSelectRow(new ISelectRowEventListener() {
			@Override
			public void selectRow(MapData rowData, String rowid, EventObjectHandler rowobj) {
				stmtDetailForm.getUi().clear();
				stmtDetailForm.setFormData(rowData);
				//更新账单交易列表
				transListGrid.getUi().setParm(uCcsAcct.AcctNbr().getName(), rowData.getString(uCcsAcct.AcctNbr().getName())); // 卡号
				transListGrid.getUi().setParm(CcsStatement.P_StmtDate, rowData.getString(CcsStatement.P_StmtDate)); // 账单日期
				transListGrid.loadData();
				transListGrid.loadDataFromUrl("rpc/t1201Server/getStmtTxnList");
			}
		});
		return searchGrid;
	}
	
	/**
	 * 创建历史账单详细信息显示的表单
	 * @return
	 */
	private KylinForm createStmtInfoForm() {
		
		stmtDetailForm = new KylinForm();
		stmtDetailForm.setCol(4);
		stmtDetailForm.setWidth("98%");
		
		stmtDetailForm.setField(
				uCcsStatement.AcctNbr().readonly(true),
				uCcsStatement.AcctType().readonly(true).asSelectItem(SelectType.KEY_LABLE),
				uCcsStatement.StmtDate().readonly(true),
				uCcsStatement.Name().readonly(true),
				uCcsStatement.PmtDueDate().readonly(true),
				uCcsStatement.Currency().readonly(true),
				uCcsStatement.StmtFlag().readonly(true).asSelectItem(SelectType.KEY_LABLE),
				uCcsStatement.AgeCode().readonly(true),
				uCcsStatement.PointsBegBal().readonly(true),
				uCcsStatement.PointsBal().readonly(true),
				uCcsStatement.CtdPoints().readonly(true),
				uCcsStatement.CtdAdjPoints().readonly(true),
				uCcsStatement.CtdSpendPoints().readonly(true),
				uCcsStatement.QualGraceBal().readonly(true),
				uCcsStatement.TotDueAmt().readonly(true),
				uCcsStatement.GraceDaysFullInd().readonly(true).asSelectItem(SelectType.KEY_LABLE),
				uCcsStatement.StmtBegBal().readonly(true),
				uCcsStatement.StmtCurrBal().readonly(true),
				uCcsStatement.LastStmtDate().readonly(true),
				uCcsStatement.CreditLmt().readonly(true),
				uCcsStatement.TempLmt().readonly(true),
				uCcsStatement.TempLmtBegDate().readonly(true),
				uCcsStatement.TempLmtEndDate().readonly(true),
				uCcsStatement.OvrlmtDate().readonly(true),
				uCcsStatement.CtdCashAmt().readonly(true),
				uCcsStatement.CtdCashCnt().readonly(true),
				uCcsStatement.CtdRetailAmt().readonly(true),
				uCcsStatement.CtdRetailCnt().readonly(true),
				uCcsStatement.CtdRepayAmt().readonly(true),
				uCcsStatement.CtdRepayCnt().readonly(true),
				uCcsStatement.CtdDbAdjAmt().readonly(true),
				uCcsStatement.CtdDbAdjCnt().readonly(true),
				uCcsStatement.CtdCrAdjAmt().readonly(true),
				uCcsStatement.CtdCrAdjCnt().readonly(true),
				uCcsStatement.CtdFeeAmt().readonly(true),
				uCcsStatement.CtdFeeCnt().readonly(true),
				uCcsStatement.CtdRefundAmt().readonly(true),
				uCcsStatement.CtdRefundCnt().readonly(true),
				uCcsStatement.CtdAmtDb().readonly(true),
				uCcsStatement.CtdNbrDb().readonly(true),
				uCcsStatement.CtdAmtCr().readonly(true),
				uCcsStatement.CtdNbrCr().readonly(true)
				);
		stmtDetailForm.getSetting().labelWidth(120);
		return stmtDetailForm;
	}
	
	/**
	 * 创建历史账单详细信息显示的tabset，包含账单明细和账单交易
	 * @return
	 */
	private Tab createStmtInfoTabSet() {
		TabSetting tabSetting = new TabSetting().dragToMove(true).showSwitch(true);
		tab = new Tab(tabSetting);
		tab.setWidth("100%");
		tab.setHeight("500px");
		// 账单汇总信息
		stmtTabSetting = new TabItemSetting("stmtInfotab", constants.stmtInfoTabTitle());
		ScrollPanel stmtSpanel = new ScrollPanel();
		stmtSpanel.setWidth("100%");
		stmtSpanel.setHeight("350px");
		stmtSpanel.add(createStmtInfoForm());
		tab.addItem(stmtTabSetting, stmtSpanel);
		// 账单明细信息列表
		txnListTabSetting = new TabItemSetting("transInfoTab", constants.tranListTitle());
		tab.addItem(txnListTabSetting, createTransListGrid());
		
		// 账单明细
		txnDetailTabSetting = new TabItemSetting("transDetailInfoTab", constants.tranInfoTitle());
		ScrollPanel tsmSpanel = new ScrollPanel();
		tsmSpanel.setWidth("98%");
		tsmSpanel.setHeight("350px");
		tsmSpanel.add(createTransInfoForm());
		tab.addItem(txnDetailTabSetting, tsmSpanel);
		return tab;
	}
	
	/**
	 * 创建账单交易列表的表格
	 * @return
	 */
	private KylinGrid createTransListGrid() {
		transListGrid = new KylinGrid();
		transListGrid.setWidth("98%");
		transListGrid.setHeight("340px");
		transListGrid.setColumns(
				uCcsTxnHst.AcctNbr(),
				uCcsTxnHst.AcctType().columnRender(),
				uCcsTxnHst.TxnDate(),
				uCcsTxnHst.AcqAddress(),
				uCcsTxnHst.TxnCode(),
				uCcsTxnHst.TxnCurrency(),
				uCcsTxnHst.DbCrInd(),
				uCcsTxnHst.TxnAmt(),
				uCcsTxnHst.TxnShortDesc(),
				uCcsTxnHst.PostDate(),
				uCcsTxnHst.PostCurrency(),
				uCcsTxnHst.PostAmt(),
				uCcsTxnHst.Points()
				);
		
		//选中listGird记录时为detailForm赋值
		transListGrid.getSetting().onDblClickRow(new IDblClickRowEventListener() {
			@Override
			public void onDblClickRow(MapData data, String rowid, EventObjectHandler row) {
				transDetailForm.getUi().clear();
				transDetailForm.setFormData(data);
				txnDetailTabSetting.isLselected();
				tab.selectTabItem(txnDetailTabSetting.getId());
			}
		});
		return transListGrid;
	}
	
	/**
	 * 创建账单交易详细信息显示的表单
	 * @return
	 */
	private KylinForm createTransInfoForm() {
		transDetailForm = new KylinForm();
		stmtDetailForm.setCol(4);
		stmtDetailForm.setWidth("100%");
		transDetailForm.setField(
				uCcsTxnHst.Term().readonly(true),
				uCcsTxnHst.TxnSeq().readonly(true),
				uCcsTxnHst.AcctNbr().readonly(true),
				uCcsTxnHst.AcctType().readonly(true).asSelectItem(SelectType.KEY_LABLE),
//				uCcsTxnHst.CardNbr().readonly(true),
//				uCcsTxnHst.LogicCardNbr().readonly(true),
//				uCcsTxnHst.CardBasicNbr().readonly(true),
				uCcsTxnHst.TxnDate().readonly(true),
				uCcsTxnHst.TxnTime().readonly(true),
				uCcsTxnHst.TxnCode().readonly(true),
				uCcsTxnHst.DbCrInd().readonly(true).asSelectItem(SelectType.KEY_LABLE),
				uCcsTxnHst.TxnAmt().readonly(true),
				uCcsTxnHst.PostAmt().readonly(true),
				uCcsTxnHst.PostDate().readonly(true),
				uCcsTxnHst.AuthCode().readonly(true),
				uCcsTxnHst.TxnCurrency().readonly(true),
				uCcsTxnHst.PostCurrency().readonly(true),
				uCcsTxnHst.OrigTransDate().readonly(true),
				uCcsTxnHst.PlanNbr().readonly(true),
				uCcsTxnHst.RefNbr().readonly(true),
				uCcsTxnHst.Points().readonly(true),
				uCcsTxnHst.TxnShortDesc().readonly(true),
				uCcsTxnHst.PostingFlag().readonly(true).asSelectItem(SelectType.KEY_LABLE),
				uCcsTxnHst.RelPmtAmt().readonly(true),
				uCcsTxnHst.OrigPmtAmt().readonly(true),
				uCcsTxnHst.AcqBranchIq().readonly(true),
				uCcsTxnHst.AcqAddress().readonly(true),
				uCcsTxnHst.AcqTerminalId().readonly(true),
				uCcsTxnHst.Mcc().readonly(true),
				uCcsTxnHst.OrigTxnCode().readonly(true),
				uCcsTxnHst.OrigTxnAmt().readonly(true),
				uCcsTxnHst.OrigSettAmt().readonly(true),
				uCcsTxnHst.InterchangeFee().readonly(true),
				uCcsTxnHst.FeePayout().readonly(true),
				uCcsTxnHst.FeeProfit().readonly(true),
				uCcsTxnHst.LoanIssueProfit().readonly(true),
				uCcsTxnHst.StmtDate().readonly(true),
				uCcsTxnHst.VoucherNo().readonly(true)
				);
		transDetailForm.getSetting().labelWidth(130);
		return transDetailForm; 
	}
	
	
}
