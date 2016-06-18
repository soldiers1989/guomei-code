package com.sunline.ccs.ui.client.pages.incomemaintain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
//import com.sunline.ccs.infrastructure.client.ui.UTmCardMediaMap;
import com.sunline.ccs.infrastructure.client.ui.UCcsCardLmMapping;
//import com.sunline.ccs.infrastructure.client.ui.UTmUnmatchO;
import com.sunline.ccs.infrastructure.client.ui.UCcsAuthmemoO;
//import com.sunline.ccs.infrastructure.shared.model.CcsAuthO;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoHst;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.ui.client.commons.CommonUiUtils;
import com.sunline.ccs.ui.client.commons.CommonVerticalPanel;
import com.sunline.ccs.ui.client.commons.CommonGlobalConstants;
import com.sunline.ccs.ui.client.commons.UIUtilConstants;
import com.sunline.kylin.web.ark.client.helper.ButtonColumnHelper;
import com.sunline.kylin.web.ark.client.helper.ButtonColumnHelper.ButtonClickHandler;
import com.sunline.kylin.web.ark.client.helper.DateColumnHelper;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ui.button.client.ButtonSetting;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.dialog.client.listener.ConfirmCallbackFunction;
import com.sunline.ui.event.client.IClickEventListener;

/**
 * 授权未入账交易维护
* @author dch
 *
 */
@Singleton
public class AuthIncomesMaintain extends Page {

	@Inject
	private AuthIncomesMaintainConstants constants;
	@Inject
	private CommonGlobalConstants publicConstants;
	@Inject
	private UIUtilConstants utilConstants;
	private KylinForm searchForm;
	private KylinGrid searchGrid = new KylinGrid();
	private KylinForm detailForm;
	//@Inject
	//private UCcsCardLmMapping uCcsCardLmMapping;
	@Inject
	private UCcsAuthmemoO uCcsAuthO;


	@Override
	public IsWidget createPage() {
		// 返回组件
		CommonVerticalPanel layoutPanel = new CommonVerticalPanel();
		CommonVerticalPanel formPanel = new CommonVerticalPanel();
		CommonVerticalPanel gridPanel = new CommonVerticalPanel();
		CommonVerticalPanel detailPanel = new CommonVerticalPanel();
		
		// 搜索
		formPanel.add(createSearchForm());
		
		// 列表
		StackPanel gridSpanel = new StackPanel();
		gridSpanel.setWidth("98%");
		gridSpanel.add(createListGrid(), constants.authorListGridRecord());
		gridPanel.add(gridSpanel);
		
		// 明细
		ScrollPanel sPanel = new ScrollPanel();
		sPanel.setHeight("300px");
		sPanel.setWidth("100%");
		sPanel.add(showDetailMsg());
		detailPanel.add(sPanel);
		
		layoutPanel.add(formPanel);
		layoutPanel.add(gridPanel);
		layoutPanel.add(detailPanel);
		return layoutPanel;
	}
	
	@Override
	public void refresh() {
		searchForm.getUi().clear();
		searchGrid.loadData();
		detailForm.getUi().clear();
	}
	
	/**
	 * 创建收索框
	 * */
	private KylinForm createSearchForm() {
		//搜索框
		searchForm = new KylinForm();
		{	
			searchForm.setWidth("98%");
			searchForm.setCol(3);
			// 卡账标识
			//TextColumnHelper cardAcct = new TextColumnHelper(UIUtilConstants.INDICATOR_NAME_CARD_ACCT, utilConstants.cardAccIndTitle(), 19);
			
			//cardAcct.asSelectItem();
			// 渠道
			//TextColumnHelper siChannal = new TextColumnHelper("channel", constants.selectItemChannel(), 19);
			//siChannal.asSelectItem();
			// 起始日期
	        DateColumnHelper beginDateItem = new DateColumnHelper("beginDate", publicConstants.labelBeginDate(), true, false);
			Date bdate = new Date();
			beginDateItem.startDate(bdate);
			beginDateItem.endDate(bdate);
	        // 截止日期
			DateColumnHelper endDateItem = new DateColumnHelper("endDate", publicConstants.labelEndDate(), true, false);
			Date edate = new Date();
			endDateItem.startDate(edate);
			endDateItem.endDate(edate);
			
			searchForm.setField(uCcsAuthO.AcctNbr().required(true)//, cardAcct.required(true), siChannal
					, beginDateItem, endDateItem);
			
			// 页面加载后执行
			/*弃用-0915lsy
			Scheduler.get().scheduleFinally(new ScheduledCommand() {
				@Override
				public void execute() {
					// 设置卡账标识下拉框值
					searchForm.setFieldSelectData(UIUtilConstants.INDICATOR_NAME_CARD_ACCT, CommonUiUtils.cardOrAcctSelectItem(utilConstants));
					// 设置渠道
			    	//searchForm.setFieldSelectData("channel", CommonUiUtils.channalSelectItem(utilConstants));
				}
			});
			*/
	    	searchForm.addButton(ClientUtils.createSearchButton(new IClickEventListener(){
				@Override
				public void onClick() {
					MapData submitData = searchForm.getSubmitData().asMapData();
					if (submitData.getString("beginDate") != null) {
						searchGrid.getUi().setParm("bDate", submitData.getString("beginDate"));
					}
					if (submitData.getString("endDate") != null) {
						searchGrid.getUi().setParm("eDate", submitData.getString("endDate"));
					}
					if (submitData.getString(uCcsAuthO.AcctNbr().getName()) != null) {
						searchGrid.getUi().setParm(uCcsAuthO.AcctNbr().getName(), submitData.getString(uCcsAuthO.AcctNbr().getName()));
					}
					if (submitData.getString(UIUtilConstants.INDICATOR_NAME_CARD_ACCT) != null) {
						searchGrid.getUi().setParm(UIUtilConstants.INDICATOR_NAME_CARD_ACCT, submitData.getString(UIUtilConstants.INDICATOR_NAME_CARD_ACCT));
					}
					if (submitData.getString("inputSource") != null) {
						searchGrid.getUi().setParm("inputSource", submitData.getString("inputSource"));
					}
					searchGrid.loadDataFromUrl("rpc/t3402Server/getAuthUnTranList");
				}
			}));
		}
		return searchForm;
	}
	
	/**
	 * 创建列表list显示
	 * @return
	 */
	private KylinGrid createListGrid() {
		
		searchGrid = new KylinGrid();
		searchGrid.setWidth("100%");
		searchGrid.setHeight("350px");
		searchGrid.checkbox(false);
		//searchGrid.getSetting().delayLoad(true);
		//searchGrid.loadDataFromUrl("rpc/t3402Server/getAuthUnTranList");
		
		// 获取详细信息按钮
		ButtonColumnHelper viewBtn = new ButtonColumnHelper("viewBtn", constants.btnTitleViewTrans());
		viewBtn.asButtonItem(new ButtonClickHandler() {
			@Override
			public List<ButtonSetting> buttons(final MapData rowData) {
				
				List<ButtonSetting> btn = new ArrayList<ButtonSetting>();
				ButtonSetting btnSetting = new ButtonSetting();
				btnSetting.text(constants.btnTitleViewTrans());
				btnSetting.click(new IClickEventListener() {
					@Override
					public void onClick() {
						fetchTransDetail(rowData.getInteger(CcsAuthmemoHst.P_LogKv));
					}
					
				});
				btn.add(btnSetting);
				return btn;
			}
		});
		
		 // 撤销按钮
		 ButtonColumnHelper cancelBtn = new ButtonColumnHelper("cancelBtn", constants.btnTitleCancelTrans());
		 cancelBtn.asButtonItem(new ButtonClickHandler() {
				@Override
				public List<ButtonSetting> buttons(final MapData rowData) {
					
					List<ButtonSetting> btn = new ArrayList<ButtonSetting>();
					ButtonSetting btnSetting = new ButtonSetting();
					btnSetting.text(constants.btnTitleCancelTrans());
					btnSetting.click(new IClickEventListener() {
						@Override
						public void onClick() {
							cancelAuthTrans(rowData.getInteger(CcsAuthmemoHst.P_LogKv));
						}
						
					});
					btn.add(btnSetting);
					return btn;
				}
			});
		
		searchGrid.setColumns(
				/*uCcsAuthO.GuarantyId(),*/
				uCcsAuthO.AcctType().columnRender(),
				uCcsAuthO.TransportTime(),
				uCcsAuthO.TxnCurrency(),
				uCcsAuthO.TxnAmt(),
				uCcsAuthO.AuthCode(),
				uCcsAuthO.AuthTxnStatus().columnRender(),
				uCcsAuthO.TxnType().columnRender(),
				uCcsAuthO.InputSource().columnRender(),
				uCcsAuthO.AcqAddress(), 
				viewBtn,
				cancelBtn
				);
		
//		listGrid.setShowRecordComponents(true);
//		listGrid.setShowRecordComponentsByCell(true);
//		listGrid.setHeight100();
//		listGrid.setWidth100();
//		listGrid.setCanHover(true);
//		listGrid.setShowHover(true);
//		listGrid.setHoverWidth(300);
//		listGrid.setRecordComponentPoolingMode(RecordComponentPoolingMode.DATA);
//		listGrid.setSelectionType(SelectionStyle.SINGLE);
//		listGrid.setOverflow(Overflow.AUTO);
		
		
//		ListGridField buttonField = new ListGridField("buttonField", constants.titleOperation());
//		buttonField.setWidth(150);
//		ListGridField cardField=uCcsAuthO.B002().width(100).createLGField();
//		cardField.setTitle(constants.cardNoTitle());
		
		//交易传输日期格式转换 MMDDhhmmss -> MM-DD hh:mm:ss
//		ListGridField transmissionTimestampField = uCcsAuthO.TransmissionTimestamp().width(100).createLGField();
//		{
//			transmissionTimestampField.setCellFormatter(new TransmissionTimestampCellFormatter());
//		}
//		
//		listGrid.setFields(
//				cardField,
//				uCcsAuthO.AcctType().width(130).createLGField(),
//				transmissionTimestampField,
//				uCcsAuthO.TxnCurrCd().width(90).createLGField(),
//				uCcsAuthO.TxnAmt().width(90).createLGField(),
//				uCcsAuthO.AuthCode().width(90).createLGField(),
//				uCcsAuthO.TxnStatus().width(90).createLGField(),
//				uCcsAuthO.TxnType().width(120).createLGField(),
//				uCcsAuthO.Channel().width(90).createLGField(),
//				uCcsAuthO.AcqNameAddr().width(120).createLGField(),
//				buttonField
//				);
//		
//		listGrid.setDataSource(this.createTmUnMatchListGrideDataSource());
		
		return searchGrid;
	}
	
	/**
	 * 创建历史账单详细信息显示的tabset，包含账单明细和账单交易
	 * @return
	 */
	private KylinForm  showDetailMsg() {
		detailForm = new KylinForm();
		detailForm.setHeight("300px");
		detailForm.setWidth("98%");
//		transDetailForm = new YakDynamicForm();
//		transDetailForm.setWidth100();
//		transDetailForm.setHeight100();
//		transDetailForm.setOverflow(Overflow.AUTO);
//		transDetailForm.setNumCols(4);
//		transDetailForm.setTitleWidth(180);
		
		detailForm.setField(
				uCcsAuthO.GuarantyId().readonly(true),
				uCcsAuthO.AcctType().readonly(true),
				uCcsAuthO.AcqRefNbr().readonly(true),
				uCcsAuthO.TxnAmt().readonly(true),
				uCcsAuthO.TxnCurrency().readonly(true),
				uCcsAuthO.AuthCode().readonly(true),
				uCcsAuthO.AcqAddress().readonly(true),
				uCcsAuthO.ChbTxnAmt().readonly(true),
				uCcsAuthO.ChbCurrency().readonly(true),
//				uCcsAuthO.Channel().readonly(true),
				uCcsAuthO.Mcc().readonly(true),
				uCcsAuthO.AcqBranchIq().readonly(true),
				uCcsAuthO.FwdInstId().readonly(true),
				uCcsAuthO.TransportTime().readonly(true),
				uCcsAuthO.SettleDate().readonly(true),
//				uCcsAuthO.TpsTxnSeq().readonly(true),
				uCcsAuthO.TxnDirection().readonly(true),
				uCcsAuthO.AuthTxnStatus().readonly(true),
				uCcsAuthO.TxnType().readonly(true),
				uCcsAuthO.InputSource().readonly(true),
				uCcsAuthO.AuthTxnTerminal().readonly(true),
				uCcsAuthO.LogOlTime().readonly(true),
				uCcsAuthO.LogBizDate().readonly(true),
				//uCcsAuthO.log.readonly(true),
				uCcsAuthO.Mti().readonly(true),
				uCcsAuthO.OrigTxnType().readonly(true),
				uCcsAuthO.OrigFwdInstId().readonly(true),
				uCcsAuthO.OrigAcqInstId().readonly(true),
				uCcsAuthO.OrigMti().readonly(true),
				uCcsAuthO.OrigTransDate().readonly(true),
				uCcsAuthO.OrigTraceNo().readonly(true),
				uCcsAuthO.OrigTxnProc().readonly(true),
				uCcsAuthO.OrigTxnAmt().readonly(true),
				uCcsAuthO.OrigLogKv().readonly(true),
				uCcsAuthO.OrigTxnVal1().readonly(true),
				uCcsAuthO.OrigTxnVal2().readonly(true),
				uCcsAuthO.OrigChbTxnAmt().readonly(true),
				//uCcsAuthO.Orig.readonly(true),
				uCcsAuthO.LastReversalDate().readonly(true),
				uCcsAuthO.VoidCnt().readonly(true),
				uCcsAuthO.ManualAuthFlag().readonly(true),
				uCcsAuthO.OpId().readonly(true),
				uCcsAuthO.CardAssociation().readonly(true),
				uCcsAuthO.ProductCd().readonly(true),
				uCcsAuthO.MccType().readonly(true),
				uCcsAuthO.FinalReason().readonly(true),
				uCcsAuthO.FinalAction().readonly(true),
				uCcsAuthO.FinalAuthAmt().readonly(true),
				//uCcsAuthO..readonly(true),
				uCcsAuthO.FinalUpdDirection().readonly(true),
				uCcsAuthO.FinalUpdAmt().readonly(true),
				uCcsAuthO.IcInd().readonly(true),
				uCcsAuthO.The3dsecureType().readonly(true),
				uCcsAuthO.VipStatus().readonly(true),
				uCcsAuthO.CurrBal().readonly(true),
				uCcsAuthO.CashAmt().readonly(true),
				uCcsAuthO.Otb().readonly(true),
				uCcsAuthO.CashOtb().readonly(true),
				uCcsAuthO.CustOtb().readonly(true),
				uCcsAuthO.InBlacklistInd().readonly(true),
				uCcsAuthO.MerInBlacklistInd().readonly(true),
				uCcsAuthO.ExpireDate().readonly(true),
				uCcsAuthO.Megt1Result().readonly(true),
				uCcsAuthO.Megt2Result().readonly(true),
				uCcsAuthO.Megt3Result().readonly(true),
				uCcsAuthO.PwdType().readonly(true),
				uCcsAuthO.PwdResult().readonly(true),
				uCcsAuthO.PayPwdErrNum().readonly(true),
				uCcsAuthO.CvvResult().readonly(true),
				uCcsAuthO.Cvv2Result().readonly(true),
				uCcsAuthO.IcvnResult().readonly(true),
				uCcsAuthO.ArqcResult().readonly(true),
				uCcsAuthO.AtcResult().readonly(true),
				uCcsAuthO.CvrResult().readonly(true),
				uCcsAuthO.TvrResult().readonly(true),
				uCcsAuthO.RejReason().readonly(true),
				uCcsAuthO.MemoCr().readonly(true),
				uCcsAuthO.MemoDb().readonly(true),
				uCcsAuthO.B002CardNbr().readonly(true),
				uCcsAuthO.B003ProcCode().readonly(true),
				uCcsAuthO.B007TxnTime().readonly(true),
				uCcsAuthO.B011Trace().readonly(true),
				uCcsAuthO.B022Entrymode().readonly(true),
				uCcsAuthO.B025Entrycond().readonly(true),
				uCcsAuthO.B032AcqInst().readonly(true),
				uCcsAuthO.B033FwdIns().readonly(true),
				uCcsAuthO.B039RtnCode().readonly(true),
				uCcsAuthO.B042MerId().readonly(true),
//				uCcsAuthO.B060().startRow(true).colSpan(transDetailForm.getNumCols()-1).readonly(true),
//				uCcsAuthO.B061().startRow(true).colSpan(transDetailForm.getNumCols()-1).readonly(true),
				uCcsAuthO.B060Reserved().readonly(true),
				uCcsAuthO.B061CustInfo().readonly(true),
				uCcsAuthO.B090OrigData().readonly(true)
				);
		
		return detailForm;
	}
	
	/**
	 * 获取并显示所选择的交易详细信息
	 * @param logKey
	 */
	private void fetchTransDetail(final int logKey) {
		
		RPC.ajax("rpc/t3402Server/getTmUnmatchO", new RpcCallback<Data>(){
			@Override
			public void onSuccess(Data result) {
				detailForm.getUi().clear();
				detailForm.getUi().setData(result);
			}
		}  , logKey);
	}
	
	/**
	 * 点击撤消按钮时触发撤消授权交易的处理逻辑
	 * @param logKey
	 */
	private void cancelAuthTrans(final int logKey) {
		Dialog.confirm(constants.msgConfirmCancel(), "提示", new ConfirmCallbackFunction(){
			
			@Override
			public void corfimCallback(boolean value) {
				if (value) {
					RPC.ajax("rpc/t3402Server/cancelTrans", new RpcCallback<Data>(){
						@Override
						public void onSuccess(Data result) {
							Dialog.tipNotice("操作成功！");
							detailForm.getUi().clear();
							searchGrid.loadData(searchForm);
						}
					}  , logKey);
				}
			}
		});
		
	}
	
}

