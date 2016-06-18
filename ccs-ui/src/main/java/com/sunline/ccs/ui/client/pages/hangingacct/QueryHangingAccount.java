/**
 * 
 */
package com.sunline.ccs.ui.client.pages.hangingacct;

import java.util.LinkedHashMap;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsAcct;
import com.sunline.ccs.infrastructure.client.ui.UCcsTxnReject;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnReject;
import com.sunline.ccs.ui.client.commons.CommonKylinForm;
import com.sunline.ccs.ui.client.commons.CommonKylinGrid;
import com.sunline.ccs.ui.client.commons.CommonSelectItemWrapper;
import com.sunline.ccs.ui.client.commons.CommonUiUtils;
import com.sunline.ccs.ui.client.commons.PublicConstants;
import com.sunline.ccs.ui.client.commons.UIUtilConstants;
import com.sunline.kylin.web.ark.client.helper.ColumnHelper;
import com.sunline.kylin.web.ark.client.helper.DateColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.EventObjectHandler;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.IDblClickRowEventListener;
import com.sunline.ui.grid.client.listener.ISelectRowEventListener;

/**
 * 挂账交易查询
 * 
 * @author fanghj
 *
 */
@Singleton
public class QueryHangingAccount extends Page {

    @Inject
    private QueryHangingAccountConstants constants;
    @Inject
    private PublicConstants publicConstants;

    @Inject
    UIUtilConstants uiconst;

    @Inject
    private UCcsTxnReject uCcsTxnRejectHst;
    @Inject
    private UCcsAcct uCcsAcct;

    private VerticalPanel mainWindow;

    private CommonKylinGrid userGrid;

    private KylinForm searchForm;

    private CommonKylinForm detailsForm;

    private ScrollPanel scrollPanel;

    @Override
    public void refresh() {
	userGrid.clearData();
	searchForm.getUi().clear();
	detailsForm.getUi().clear();
	RPC.ajax("rpc/t3403Server/getTxnCd", new RpcCallback<Data>(){

		@Override
		public void onSuccess(Data result) {
			detailsForm.setFieldSelectData(CcsTxnReject.P_TxnCode, result.asListData());
		}
		
		});
    }
    
    @Override
    public IsWidget createPage() {
	mainWindow = new VerticalPanel();
	mainWindow.setWidth("100%");
	mainWindow.setHeight("100%");
	searchForm = new KylinForm();
	searchForm.setCol(3);
	searchForm.setField(
			uCcsAcct.ContrNbr(),
			uCcsAcct.GuarantyId(),
			new DateColumnHelper("beginDate", constants.beginDateLabel(), true, false),
			new DateColumnHelper("endDate", constants.endDateLabel(), true, false));
	userGrid = new CommonKylinGrid();
	userGrid.setColumns(new ColumnHelper[]{uCcsTxnRejectHst.CardNbr(), uCcsTxnRejectHst.AcctType(),
		uCcsTxnRejectHst.TxnDate(), uCcsTxnRejectHst.TxnCode(), uCcsTxnRejectHst.TxnCurrency(),
		uCcsTxnRejectHst.TxnAmt(), uCcsTxnRejectHst.AuthCode(), uCcsTxnRejectHst.TxnDesc()});
	userGrid.loadDataFromUrl("rpc/t3403Server/getAuthUnTranList");
	userGrid.getSetting().delayLoad(false);
	userGrid.setHeight("350px");
	userGrid.setWidth("98%");
	userGrid.getSetting().onSelectRow(new ISelectRowEventListener() {

	    @Override
	    public void selectRow(MapData rowdata, String rowid, EventObjectHandler rowobj) {
		detailsForm.getUi().setData(rowdata);
	    }
	    
	});
	userGrid.getSetting().onUnSelectRow(new ISelectRowEventListener() {

	    @Override
	    public void selectRow(MapData rowdata, String rowid, EventObjectHandler rowobj) {
		detailsForm.getUi().clear();
	    }
	    
	});
//	userGrid.addDblClickListener(new DBClickEvent());
	scrollPanel = new ScrollPanel();
	detailsForm = new CommonKylinForm();
	/**
	 * 交易参考号，入账币种金额改为只读  2015.11.06 12:04 chenpy
	 */
	detailsForm.setField(new ColumnHelper[]{uCcsTxnRejectHst.CardNbr(), uCcsTxnRejectHst.PostAmt().readonly(true),
		uCcsTxnRejectHst.PostCurrency().asSelectItem(), uCcsTxnRejectHst.RefNbr().readonly(true), uCcsTxnRejectHst.TxnDate(),
		uCcsTxnRejectHst.TxnSeq().readonly(true), uCcsTxnRejectHst.TxnCode().asSelectItem(), uCcsTxnRejectHst.AcctNbr().readonly(true),
		uCcsTxnRejectHst.AcctType().readonly(true), uCcsTxnRejectHst.AcqAcceptorId().readonly(true), uCcsTxnRejectHst.AcqBranchIq().readonly(true),
		uCcsTxnRejectHst.AcqAddress().readonly(true), uCcsTxnRejectHst.AcqTerminalId().readonly(true), uCcsTxnRejectHst.AuthCode().readonly(true),
		uCcsTxnRejectHst.CardBasicNbr().readonly(true), uCcsTxnRejectHst.CardBlockCode().readonly(true), uCcsTxnRejectHst.DbCrInd().readonly(true),
		uCcsTxnRejectHst.FeeProfit().readonly(true), uCcsTxnRejectHst.OrigSettAmt().readonly(true), uCcsTxnRejectHst.OrigTxnAmt().readonly(true),
		uCcsTxnRejectHst.OrigTxnCode().readonly(true), uCcsTxnRejectHst.FeePayout().readonly(true), uCcsTxnRejectHst.InterchangeFee().readonly(true),
		uCcsTxnRejectHst.LoanIssueProfit().readonly(true), uCcsTxnRejectHst.LogicCardNbr().readonly(true), uCcsTxnRejectHst.Mcc().readonly(true),
		uCcsTxnRejectHst.Org().readonly(true), uCcsTxnRejectHst.OrigPmtAmt().readonly(true), uCcsTxnRejectHst.OrigTransDate().readonly(true),
		uCcsTxnRejectHst.PlanNbr().readonly(true), uCcsTxnRejectHst.Points().readonly(true), uCcsTxnRejectHst.PostDate().readonly(true),
		uCcsTxnRejectHst.PostingFlag().readonly(true), uCcsTxnRejectHst.ProductCd().readonly(true), uCcsTxnRejectHst.RelPmtAmt().readonly(true),
		uCcsTxnRejectHst.StmtDate().readonly(true), uCcsTxnRejectHst.TxnAmt().readonly(true), uCcsTxnRejectHst.TxnCurrency().readonly(true),
		uCcsTxnRejectHst.TxnShortDesc().readonly(true), uCcsTxnRejectHst.TxnTime().readonly(true), uCcsTxnRejectHst.VoucherNo().readonly(true),
		uCcsTxnRejectHst.TxnDesc().readonly(true)});
	mainWindow.add(CommonUiUtils.lineLayoutForm(searchForm, ClientUtils.createSearchButton(new QueryEvent()), null , null));
	mainWindow.add(userGrid);
	LinkedHashMap<String, String> currencyType = new LinkedHashMap<String, String>();
	currencyType.put("156", "人民币");
	CommonSelectItemWrapper.getInstance().wrapper(detailsForm, uCcsTxnRejectHst.PostCurrency(), currencyType);
	KylinButton submit = ClientUtils.createEditButton(new UpdateEvent());
	scrollPanel.add(detailsForm);
	scrollPanel.setWidth("98%");
	scrollPanel.setHeight("380px");
	mainWindow.add(scrollPanel);
	mainWindow.add(submit);
	return mainWindow;
    }

    class UpdateEvent implements IClickEventListener {

	@SuppressWarnings("all")
	@Override
	public void onClick() {
	    final MapData parameters = detailsForm.getSubmitData().asMapData();
	    RPC.ajax("rpc/t3403Server/alterInfo",
		     new RpcCallback<Data>(){

			 @Override
			 public void onSuccess(Data result) {
			     Dialog.tipNotice(constants.updateSucc(), 1000);
			      userGrid.loadDataFromUrl("rpc/t3403Server/getAuthUnTranList");
			     detailsForm.getUi().clear();
			 }

			 @Override
			 public void onFailure(Throwable caught) {
			     super.onFailure(caught);
			 }

		     },
		     new Object[]{parameters.getString(CcsTxnReject.P_CardNbr),
			     parameters.getString(CcsTxnReject.P_TxnCode),
			     parameters.getString(CcsTxnReject.P_TxnDate),
			     parameters.getString(CcsTxnReject.P_PostAmt),
			     parameters.getString(CcsTxnReject.P_PostCurrency),
			     parameters.getString(CcsTxnReject.P_RefNbr), parameters.getString(CcsTxnReject.P_TxnSeq)});

	}

    }

    class QueryEvent implements IClickEventListener {

	@SuppressWarnings("all")
	@Override
	public void onClick() {
	    if (searchForm.valid()) {
	    	userGrid.loadData(searchForm);
	    }
	}

    }

    class DBClickEvent implements IDblClickRowEventListener {

	@Override
	public void onDblClickRow(MapData data, String rowid, EventObjectHandler row) {
	    detailsForm.getUi().setData(data);
	}

    }

}
