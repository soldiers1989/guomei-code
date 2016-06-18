package com.sunline.ccs.ui.client.pages.limitadj;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsAcct;
import com.sunline.ccs.infrastructure.client.ui.UCcsAcctCrlmtAdjLog;
import com.sunline.ccs.infrastructure.client.ui.UCcsAcctO;
import com.sunline.ccs.infrastructure.client.ui.UCcsCardLmMapping;
import com.sunline.ccs.infrastructure.client.ui.UCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctCrlmtAdjLog;
import com.sunline.ccs.param.def.enums.AdjState;
import com.sunline.ccs.ui.client.commons.CommonCcsDateUtils;
import com.sunline.ccs.ui.client.commons.CommonUiUtils;
import com.sunline.ccs.ui.client.commons.CommonSelectItemWrapper;
import com.sunline.ccs.ui.client.commons.PublicConstants;
import com.sunline.kylin.web.ark.client.helper.ButtonColumnHelper;
import com.sunline.kylin.web.ark.client.helper.ButtonColumnHelper.ButtonClickHandler;
import com.sunline.kylin.web.ark.client.helper.DateColumnHelper;
import com.sunline.kylin.web.ark.client.helper.DecimalColumnHelper;
import com.sunline.kylin.web.ark.client.helper.EnumColumnHelper;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.ark.client.validator.ClientDateUtil;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ui.button.client.ButtonSetting;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.dialog.client.listener.ConfirmCallbackFunction;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.tab.client.Tab;
import com.sunline.ui.tab.client.TabItemSetting;
import com.sunline.ui.tab.client.TabSetting;

/**
 * 
 * @see 类名：LimitAdjustment
 * @see 描述：主动调额
 *
 * @see 创建日期：   Jun 29, 20152:53:58 PM
 * @author tangls
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Singleton
public class LimitAdjustment extends Page {

	public static final String PAGE_ID = "risk-3303";

	@Inject
	private LimitAdjustmentConstants constants;

	@Inject
	private PublicConstants publicConstants;

	@Inject
	private UCcsAcctCrlmtAdjLog uTmLimitAdjLog;
	
	private KylinForm acctForm; // 账户信息表单
	private TextColumnHelper cardItem;
	
	private TextColumnHelper checkCardItem;
	
	private TextColumnHelper queryCardItem;
	
	private DecimalColumnHelper newLimitItem;
	
	@Inject
	private UCcsAcctO uTmAccount;
	
	@Inject
	private UCcsCustomer uTmCustomer;
	
	private KylinButton searchBtnItem;
	
	private Date beginDate;
	private Date endDate;
	private KylinGrid listGrid;
	private KylinGrid listGridRecheck;
	private Tab limitTabSet;
	private TextColumnHelper custName;
	private EnumColumnHelper idType;
	private TextColumnHelper idNo;
	private DecimalColumnHelper creditLimit;
	private KylinGrid listGridLimit;
	private TextColumnHelper currencyItem;
	@Inject
	private UCcsAcct uTmAccount2;

	private KylinForm timeSearchForm;
	
	@Inject
	private UCcsCardLmMapping uTmCardMediaMap;

	private KylinForm searchForm;
	
	private KylinForm recheckForm;
	
	public LimitAdjustment(){}
	
	
	@Override
	public void refresh() {
	    searchForm.getUi().clear();
	    recheckForm.getUi().clear();
	    timeSearchForm.getUi().clear();
	    acctForm.getUi().clear();
	    listGrid.clearData();
	}
	
	@Override
	public IsWidget createPage() {
		VerticalPanel vp=new VerticalPanel();
		vp.setWidth("100%");
		vp.setHeight("100%");
		beginDate = new Date();
		endDate = new Date();
		vp.add(createTimeSearchForm());
		VerticalPanel gridPanel = new VerticalPanel();
		gridPanel.setWidth("98%");
		gridPanel.setHeight("350px");
		listGrid = new KylinGrid();
		{
			 
			listGrid.setHeight("350px");
			listGrid.setWidth("98%");
	        
			listGrid.setColumns(
					uTmLimitAdjLog.Org(),
					uTmLimitAdjLog.OpId(),
					uTmLimitAdjLog.OpTime(),
					uTmLimitAdjLog.CreditLmtOrig(),
					uTmLimitAdjLog.CreditLmtNew(),
					uTmLimitAdjLog.AcctNbr(),
					uTmLimitAdjLog.AcctType().columnRender(),
					uTmLimitAdjLog.AdjState().columnRender()
					);
		}
		gridPanel.add(listGrid);
		vp.add(gridPanel);
		vp.add(createLimitTabSet());
		listGrid.loadDataFromUrl("rpc/limitAdjustmentServer/getCurrentTranAdjLogList");
		listGrid.getSetting().delayLoad(true);
		return vp;

	}
	
	/**
	 * 创建额度调整和额度调整复核的TabSet
	 * @return
	 */
	private Tab createLimitTabSet() {
		TabSetting tabSetting = new TabSetting().dblClickToClose(true)
				.dragToMove(true).showSwitch(true);
		limitTabSet = new Tab(tabSetting);
		limitTabSet.setHeight("450px");
		limitTabSet.setWidth("98%");

		final TabItemSetting limitAdjustTab = new TabItemSetting("limitAdjustTab",constants.limitAdjust());
		 
		limitTabSet.addItem(limitAdjustTab,createLimitAdjustTab());

		final TabItemSetting limitAdjustRecheckTab = new TabItemSetting("limitAdjustRecheckTab",constants.limitAdjustRecheck());
		 
		limitTabSet.addItem(limitAdjustRecheckTab,createLimitAdjustRecheckTab());
		
		final TabItemSetting limitAdjustLogTab = new TabItemSetting("limitAdjustLogTab",constants.limitAdjustLog());
		 
		limitTabSet.addItem(limitAdjustLogTab,createLimitAdjustLogTab());
		
		return limitTabSet;
	}
	
	/**
	 * 创建额度调整tab
	 * @return
	 */
	public ScrollPanel createLimitAdjustTab(){
		ScrollPanel scrollPanel = new ScrollPanel();
		VerticalPanel layout = new VerticalPanel();
		layout.setWidth("98%");
		KylinForm searchForm = createSearchForm();
		
		layout.add(searchForm);

		acctForm = new KylinForm();
		{
			acctForm.setWidth(600);
			acctForm.setCol(4);
			// 新额度
			newLimitItem = new DecimalColumnHelper("newLimit", constants.newLimit(),  13, 
			                                       BigDecimal.ZERO, BigDecimal.valueOf(9999999999999.), 0);
			//持卡人姓名
			custName = uTmCustomer.Name();
			custName.setName(constants.custName());
			idType = uTmCustomer.IdType();
			idNo = uTmCustomer.IdNo();
			creditLimit = uTmAccount.CreditLmt();
			acctForm.setField(
					idType,
					idNo.readonly(true),
					custName.readonly(true),
					creditLimit.readonly(true), 
					newLimitItem
			);
			KylinButton btnAdj = new KylinButton("提交",null);
			KylinButton clearForm = new KylinButton("清空",null);
			btnAdj.addClickEventListener(new CreditLimitAdj());
			clearForm.addClickEventListener(new ClearForm());
			acctForm.addButton(btnAdj);
			acctForm.addButton(clearForm);
		}
		layout.add(acctForm);
		scrollPanel.add(layout);
		return scrollPanel;
	}
	
	/**
	 * 
	 * @see 类名：ClearForm
	 * @see 描述：清空表单
	 *
	 * @see 创建日期：   Jun 29, 20158:37:36 PM
	 * @author yeyu
	 * 
	 * @see 修改记录：
	 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
	 */
	class ClearForm implements IClickEventListener {
		@Override
		public void onClick() {
			acctForm.getUi().clear();
			searchForm.getUi().clear();
		}
	}
	
	/**
	 * 
	 * @see 类名：CreditLimitAdj
	 * @see 描述：额度调整点击事件处理函数
	 *
	 * @see 创建日期：   Jun 29, 20154:37:25 PM
	 * @author yeyu
	 * 
	 * @see 修改记录：
	 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
	 */
	class CreditLimitAdj implements IClickEventListener{

		@Override
		public void onClick() {
			Dialog.confirm("确定要调整账户额度吗?", "确认", new ConfirmCallbackFunction(){
				@Override
				public void corfimCallback(boolean value) {
					if(value){
						final MapData searchFormData = searchForm.getSubmitData().asMapData();
						final MapData acctFormData = acctForm.getSubmitData().asMapData();
						RPC.ajax("rpc/limitAdjustmentServer/adjustAcctCreditLimit", new RpcCallback<Data>(){
							@Override
							public void onSuccess(Data result) {
//								loadAcctCreditLimit(acctFormData.getString(CcsAcct.P_ContrNbr));
								Dialog.tipNotice("保存成功", 1000);
							}
						}, new Object[]{
								searchFormData.getString(CcsAcct.P_ContrNbr),
								acctFormData.getString("newLimit")
						});
					}
				}
			});
		}
	}
	
	/**
	 * 
	 * @see 方法名：createLimitAdjustRecheckTab 
	 * @see 描述：创建额度调整复核Tab
	 * @see 创建日期：Jun 29, 20158:44:22 PM
	 * @author yeyu
	 *  
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public VerticalPanel createLimitAdjustRecheckTab(){
		VerticalPanel layout = new VerticalPanel();
		layout.setWidth("98%");
		recheckForm = createCheckSearchForm();
		listGridRecheck = this.createListGrid();
		layout.add(recheckForm);
		layout.add(listGridRecheck);
		return layout;
	}
	
	/**
	 * 
	 * @see 描述：账户额度调整复核查询
	 * @see 创建日期：Jun 29, 20159:03:15 PM
	 * @author yeyu
	 *  
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private KylinForm createCheckSearchForm() {
		final KylinForm searchForm = new KylinForm();
		{
			searchForm.setCol(3);
			searchForm.setWidth("98%");
			searchForm.setHeight("140px");
			
			KylinButton searchBtnItem = ClientUtils.createSearchButton(new IClickEventListener() {
				@Override
				public void onClick() {
					if(searchForm.getSubmitData()!=null) {
						//积分余额填充信息
						listGridRecheck.loadData(recheckForm);
					}
				}
			});
			searchForm.setField(uTmAccount.ContrNbr().required(true));
			searchForm.addButton(searchBtnItem);
		}
		return searchForm;
	}
	
	/**
	 * 
	 * @see 方法名：buildGridButton 
	 * @see 描述：创建DataGrid中的按钮组件
	 * @see 创建日期：Jun 30, 20153:33:05 PM
	 * @author yeyu
	 *  
	 * @return {@link com.sunline.kylin.web.ark.client.helper.ButtonColumnHelper}
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private ButtonColumnHelper buildGridButton(){
		ButtonColumnHelper btnGrup = new ButtonColumnHelper("operate","操作").setColumnWidth(200);
		btnGrup.asButtonItem(new ButtonClickHandler(){
			@Override
			public List<ButtonSetting> buttons(MapData rowData) {
				final long optSeq = Long.parseLong(rowData.getString(CcsAcctCrlmtAdjLog.P_OpSeq));
				List<ButtonSetting> btnSettings = new ArrayList<ButtonSetting>();
				ButtonSetting aggree = new ButtonSetting();
				aggree.text("同意");
				aggree.click(new IClickEventListener(){
					@Override
					public void onClick() {
						btnAgreeOnClick(optSeq, AdjState.A);
					}
				});
				btnSettings.add(aggree);
				ButtonSetting refuse = new ButtonSetting();
				refuse.text("拒绝");
				refuse.click(new IClickEventListener(){
					@Override
					public void onClick() {
						btnrefuseOnClick(optSeq,AdjState.R);
					}
				});
				btnSettings.add(refuse);
				return btnSettings;
			}
		});
		return btnGrup;
	}
	
	/**
	 * 
	 * @see 方法名：createListGrid 
	 * @see 描述：创建额度调整复核的ListGrid
	 * @see 创建日期：Jun 30, 20153:31:56 PM
	 * @author tangls
	 *  
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private KylinGrid createListGrid(){
		listGridRecheck = new KylinGrid();
		listGridRecheck.setWidth("98%");
		listGridRecheck.setHeight("200px");
		listGridRecheck.setColumns(
				uTmLimitAdjLog.Org().setColumnWidth(150),
				uTmLimitAdjLog.OpId().setColumnWidth(40),
				uTmLimitAdjLog.OpTime().setColumnWidth(250),
				uTmLimitAdjLog.CardNbr().setColumnWidth(150),
				uTmLimitAdjLog.CreditLmtOrig().setColumnWidth(120).asCurrency(),
				uTmLimitAdjLog.CreditLmtNew().setColumnWidth(160),
				uTmLimitAdjLog.AcctNbr().setColumnWidth(120),
				uTmLimitAdjLog.AcctType().setColumnWidth(120),
				this.buildGridButton()
		);
		listGridRecheck.loadDataFromUrl("rpc/limitAdjustmentServer/getCurrentTranAdjLogListRecheck");
//		listGridRecheck.getSetting().delayLoad(true);
		return listGridRecheck;
	}
	/**
	 * 创建所搜表单
	 * @return
	 */
	private KylinForm createSearchForm() {
		 searchForm = new KylinForm();
		{
			searchForm.setCol(5);
			searchForm.setWidth("98%");
			cardItem = uTmAccount.ContrNbr();
			
			KylinButton searchBt = ClientUtils.createSearchButton(new IClickEventListener() {
				@Override
				public void onClick() {
						//积分余额填充信息
					String contrNbr =searchForm.getFieldValue(uTmAccount.ContrNbr().getName());
						loadAcctCreditLimit(contrNbr);
				}
			});
			searchForm.setField(cardItem);
			searchForm.addButton(searchBt);
		}
		return searchForm;
	}
	
	/**
	 * 创建时间搜索表单
	 */
	private HorizontalPanel createTimeSearchForm() {
		HorizontalPanel hPanel;
		 timeSearchForm = new KylinForm();
		{
			timeSearchForm.setCol(5);
			timeSearchForm.setWidth("98%");
			//查询起始日期
			DateColumnHelper beginDateItem = new DateColumnHelper("beginDate", publicConstants.labelBeginDate(),true,false);
			{
				beginDateItem.required(true);
				beginDateItem.startDate(beginDate);
				Date date = new Date();
				beginDateItem.startDate(date);
				beginDateItem.endDate(date);
				beginDateItem.format("yyyy-MM-dd");
			}
			
			//查询截止日期
			DateColumnHelper endDateItem = new DateColumnHelper("endDate", publicConstants.labelEndDate(),true,false);
			{
				Date date = new Date();
				endDateItem.startDate(date);
				endDateItem.endDate(date);
				endDateItem.format("yyyy-MM-dd");
			}
			KylinButton timeSearchBt=ClientUtils.createSearchButton(new IClickEventListener() {
				
				@Override
				public void onClick() {
				    	if (timeSearchForm.valid()) {
						beginDate = ClientDateUtil.truncateTime(beginDate);
						endDate = ClientDateUtil.truncateTime(endDate);
						listGrid.loadDataFromUrl("rpc/limitAdjustmentServer/getCurrentTranAdjLogList");
				    	}
				}
			});
			timeSearchForm.setField(beginDateItem, endDateItem);
			hPanel = CommonUiUtils.lineLayoutForm(timeSearchForm, timeSearchBt, null, null);
		}
		
		return hPanel;
	} 
	
	/**
	 * 远程获取账户信息表-授权 信用额度和新额度
	 * @param objs
	 */
	private void loadAcctCreditLimit(final String contrNbr) {
		
		RPC.ajax("rpc/custServer/getCustByContrNbr",
				new RpcCallback<Data>() {
					@Override
					public void onSuccess(Data result) {
						acctForm.setFieldValue(idType.getName(), (result.asMapData().getString("idType")));
						acctForm.setFieldValue(idNo.getName(), (result.asMapData().getString("idNo")));
						acctForm.setFieldValue(custName.getName(), (result.asMapData().getString("name")));
					}
				}, contrNbr);
				RPC.ajax("rpc/limitAdjustmentServer/getTmAcountAmountCurrency",
				new RpcCallback<Data>() {
					@Override
					public void onSuccess(Data result) {
						acctForm.setFieldValue(creditLimit.getName(), (result.asMapData().getString("creditLmt")));
						acctForm.setFieldValue(newLimitItem.getName(), "");
					}
				},contrNbr);
	}
	
	public String getTitle() {
		return constants.pageTitle();
	}

	
	public ImageResource getIcon() {
		return null;
	}
	
	/**
	 * listgrid点击同意按钮，复核同意调额操作
	 * @param operSeq
	 * @param adj
	 */
	private void btnAgreeOnClick(final Long operSeq, final AdjState adj){
		Dialog.confirm(constants.agreeBtnMsg(),"系统提示",new ConfirmCallbackFunction() {
			@Override
			public void corfimCallback(boolean value) {
				if(value){
					RPC.ajax("rpc/limitAdjustmentServer/adjustOperateAgreeOrRefuse",
							new RpcCallback<Data>() {
								@Override
								public void onSuccess(Data result) {
									Dialog.alert("保存成功");
									listGridRecheck.loadData(recheckForm);
								}
							}, operSeq, adj);
				}
			}
		});
	}
	
	/**
	 * listgrid点击拒绝按钮，复核拒绝调额操作
	 * @param opserSeq
	 * @param adj
	 */
	private void btnrefuseOnClick(final Long opserSeq, final AdjState adj){
		Dialog.confirm(constants.refuseBtnMsg(),"系统提示",new ConfirmCallbackFunction() {
			@Override
			public void corfimCallback(boolean value) {
				if(value){
					RPC.ajax("rpc/limitAdjustmentServer/adjustOperateAgreeOrRefuse",
							new RpcCallback<Data>() {
								@Override
								public void onSuccess(Data result) {

									listGridRecheck.loadData(recheckForm);
									Dialog.alert("保存成功");
								
								}
							}, opserSeq, adj);
				}
			
				
			}
		});
	}
	
	/**
	 * 
	 * @see 方法名：createLimitAdjustLogTab 
	 * @see 描述： 永久调额记录查询tab
	 * @see 创建日期：Jun 29, 20159:29:30 PM
	 * @author tangls
	 *  
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public VerticalPanel createLimitAdjustLogTab(){
		VerticalPanel layout = new VerticalPanel();
		layout.setWidth("98%");
		KylinForm searchForm = createFormLimitLog();
		KylinGrid listGridRe = this.createLimitAdjLog();
		layout.add(searchForm);
		layout.add(listGridRe);
		return layout;
	}
	
	/**
	 * 
	 * @see 方法名：createFormLimitLog 
	 * @see 描述：永久调额记录查询表单
	 * @see 创建日期：Jun 30, 20154:38:22 PM
	 * @author tangls
	 *  
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private KylinForm createFormLimitLog(){

		final KylinForm searchForm = new KylinForm();
		{
			searchForm.setCol(7);
			searchForm.setWidth("98%");
			searchForm.setHeight("140px");
			
			queryCardItem = uTmAccount.ContrNbr();
		  //查询起始日期
			DateColumnHelper beginDateItem = new DateColumnHelper("beginDate", publicConstants.labelBeginDate(),true,false);
			{
				Date date = new Date();
				date = new CommonCcsDateUtils(date.getTime()).addYear(CommonCcsDateUtils.Unit.YEAR, -3).asDate();
				beginDateItem.startDate(date);
				date = new CommonCcsDateUtils(date.getTime()).addYear(CommonCcsDateUtils.Unit.YEAR, 23).asDate();
				beginDateItem.endDate(date);
			}
			searchBtnItem = ClientUtils.createSearchButton(new IClickEventListener() {
				@Override
				public void onClick() {
					if(searchForm.getSubmitData()!=null) {
						listGridLimit.loadData(searchForm);
					}
				}
			});
			searchForm.setField(queryCardItem, beginDateItem);
			searchForm.addButton(searchBtnItem);
		}
		return searchForm;
	}
	
	//永久调额记录查询list
	private KylinGrid createLimitAdjLog(){
		 
		listGridLimit = new KylinGrid();
		listGridLimit.setWidth("98%");
		listGridLimit.setHeight("200px");
		listGridLimit.setColumns(
				uTmLimitAdjLog.Org(),
				uTmLimitAdjLog.OpId(),
				uTmLimitAdjLog.OpTime(),
				uTmLimitAdjLog.AdjState().columnRender(),
				uTmLimitAdjLog.CreditLmtOrig(),
				uTmLimitAdjLog.CreditLmtNew(),
				uTmLimitAdjLog.AcctNbr(),
				uTmLimitAdjLog.AcctType().columnRender()
				);
		listGridLimit.loadDataFromUrl("rpc/limitAdjustmentServer/getLimitAdjustLog");
		listGridLimit.getSetting().delayLoad(true);
		return listGridLimit;
	}
	
}
