/**
 * 
 */
package com.sunline.ccs.ui.client.pages.settleinfo;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsLoan;
import com.sunline.ccs.param.def.enums.LoanTerminateReason;
import com.sunline.ccs.ui.client.commons.UIUtilConstants;
import com.sunline.kylin.web.ark.client.helper.ButtonColumnHelper;
import com.sunline.kylin.web.ark.client.helper.ButtonColumnHelper.ButtonClickHandler;
import com.sunline.kylin.web.ark.client.helper.ColumnHelper;
import com.sunline.kylin.web.ark.client.helper.DateColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ui.button.client.ButtonSetting;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.core.client.enums.Alignment;
import com.sunline.ui.event.client.EventObjectHandler;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.IDblClickRowEventListener;
import com.sunline.ui.grid.client.listener.ISelectRowEventListener;

/**
 * 结清信息页面查询
* @author songyanchao
 *
 */
@Singleton
public class SettleInfo extends Page {
	
	private final int A4_PRIXEL_WIDTH=992;
	private final int A4_PRIXEL_HEIGHT=1044;
	private KylinForm searchForm;
	private KylinForm detailForm;
	private KylinGrid searchGrid = new KylinGrid();
	@Inject
	private UCcsLoan uCcsLoan;
	@Inject
	private SettleInfoConstants constants;
	private VerticalPanel panel;
	@Inject
    private PrintWindow printWindow = GWT.create(PrintWindow.class);
	@Override
	public IsWidget createPage() {
	 // 返回组件
		panel = new VerticalPanel();
		panel.setWidth("100%");
		panel.setHeight("100%");

		// 搜索
		panel.add(createSearchForm());

		// 列表
		StackPanel gridSpanel = new StackPanel();
		gridSpanel.setWidth("98%");
		gridSpanel.add(createListGrid(), constants.settleInfoList());
		panel.add(gridSpanel);

		// 明细
		ScrollPanel sPanel = new ScrollPanel();
		sPanel.add(showDetailMsg());
		sPanel.setWidth("98%");
		sPanel.setHeight("350px");
		panel.add(sPanel);
		return panel;
	    
	}
	
	@Override
	public void refresh() {
		searchForm.getUi().clear();
		searchGrid.clearData();
		detailForm.getUi().clear();
		searchGrid.loadDataFromUrl("rpc/settleInfoServer/getSettleInfoList");

	}
	
	/**
	 * 创建列表list显示
	 * @return
	 */
	private KylinGrid createListGrid() {
		searchGrid = new KylinGrid();
		searchGrid.setWidth("98%");
		searchGrid.setHeight("350px");
		ButtonColumnHelper printButton = new ButtonColumnHelper("button",constants.printButtonColumn());
		printButton.asButtonItem(new ButtonClickHandler(){

			@Override
			public List<ButtonSetting> buttons(final MapData rowData) {
				List<ButtonSetting> buttonSetting=new ArrayList<ButtonSetting>();
				ButtonSetting button=new ButtonSetting();
				button.text(constants.printButton());
				button.click(new IClickEventListener(){
					@Override
					public void onClick() {
						RPC.ajax("rpc/settleInfoServer/printSettleInfo",new RpcCallback<Data>(){

							@Override
							public void onSuccess(Data result) {
								printWindow.setHtml(result);
								//A4尺寸
								printWindow.getDialogSetting().width(A4_PRIXEL_WIDTH);
								printWindow.getDialogSetting().height(A4_PRIXEL_HEIGHT);
								printWindow.getDialogSetting().modal(true);
								printWindow.getDialogSetting().title("打印预览");
								printWindow.show();
							}
							
						}, rowData.getString("loanId"));
					}
				});
				if(rowData.getDate(uCcsLoan.PaidOutDate().getName())==null) button.disabled(true);
				buttonSetting.add(button);
				return buttonSetting;
			}
		});
		searchGrid.setColumns(
		                     uCcsLoan.ContrNbr(),
		                     uCcsLoan.GuarantyId(),
		                     uCcsLoan.LoanType().columnRender(),
		                     uCcsLoan.LoanStatus().columnRender(),
		                     uCcsLoan.TerminalReasonCd().columnRender(),
		                     uCcsLoan.PaidOutDate(),
		                     uCcsLoan.TerminalDate(),
		                     printButton.setColumnWidth(70)
				);
		
		// 选中listGird记录时为detailForm赋值
		searchGrid.getSetting().onSelectRow(new ISelectRowEventListener(){

			@Override
			public void selectRow(MapData rowdata, String rowid,EventObjectHandler rowobj) {
			detailForm.getUi().clear();
			detailForm.setFormData(rowdata);
			
		    }
		});
		return searchGrid;
	}
	
	/**
	 * 创建搜索表单
	 * @return
	 */
	private KylinForm createSearchForm() {
		//搜索框
		searchForm = new KylinForm();
		{
			searchForm.setWidth("98%");
			searchForm.setCol(3);
			searchForm.setField(uCcsLoan.ContrNbr(), uCcsLoan.GuarantyId(),uCcsLoan.TerminalDate());
        	    	searchForm.addButton(ClientUtils.createSearchButton(new IClickEventListener(){
        				@Override
        				public void onClick() {
        				    if(searchForm.valid()){
        				    //
        				    searchGrid.getUi().setParm(uCcsLoan.ContrNbr().getName(),searchForm.getFieldValue(uCcsLoan.ContrNbr().getName()));
        				    searchGrid.getUi().setParm(uCcsLoan.GuarantyId().getName(),searchForm.getFieldValue(uCcsLoan.GuarantyId().getName()));
        				    searchGrid.getUi().setParm(uCcsLoan.TerminalDate().getName(),searchForm.getFieldValue(uCcsLoan.TerminalDate().getName()));
        					searchGrid.loadDataFromUrl("rpc/settleInfoServer/getSettleInfoList");
        				    }
        				}
        			}));
			
		}
		return searchForm;
	}
	
	private KylinForm showDetailMsg() {

		detailForm = new KylinForm();
		detailForm.setWidth("98%");
		detailForm.setHeight("350px");
		detailForm.getSetting().labelWidth(140);
		detailForm.setTitle(constants.showDetailStack());
		detailForm.setField(uCcsLoan.Org().readonly(true),
		                    uCcsLoan.LoanId().readonly(true),
		                    uCcsLoan.AcctNbr().readonly(true),
		                    uCcsLoan.AcctType().readonly(true),
		                    uCcsLoan.RefNbr().readonly(true),
		                    uCcsLoan.DueBillNo().readonly(true),
		                    uCcsLoan.LogicCardNbr().readonly(true),
		                    uCcsLoan.CardNbr().readonly(true),
		                    uCcsLoan.RegisterDate().readonly(true),
		                    uCcsLoan.RequestTime().readonly(true),
		                    uCcsLoan.LoanType().readonly(true),
		                    uCcsLoan.LoanStatus().readonly(true),
		                    uCcsLoan.LastLoanStatus().readonly(true),
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
		                    uCcsLoan.TerminalDate().readonly(true),
		                    uCcsLoan.PaidOutDate().readonly(true),
		                    uCcsLoan.TerminalReasonCd().readonly(true),
		                    uCcsLoan.PaidPrincipal().readonly(true),
		                    uCcsLoan.PaidInterest().readonly(true),
		                    uCcsLoan.PaidFee().readonly(true),
		                    uCcsLoan.LoanCurrBal().readonly(true),
		                    uCcsLoan.LoanBalXfrout().readonly(true),
		                    uCcsLoan.LoanBalXfrin().readonly(true),
		                    uCcsLoan.LoanPrinXfrout().readonly(true),
		                    uCcsLoan.LoanPrinXfrin().readonly(true),
		                    uCcsLoan.LoanFeeXfrout().readonly(true),
		                    uCcsLoan.LoanFeeXfrin().readonly(true),
		                    uCcsLoan.OrigTxnAmt().readonly(true),
		                    uCcsLoan.OrigTransDate().readonly(true),
		                    uCcsLoan.OrigAuthCode().readonly(true),
		                    uCcsLoan.JpaVersion().readonly(true),
		                    uCcsLoan.LoanCode().readonly(true),
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
				    uCcsLoan.LastActionType().readonly(true),
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
				    uCcsLoan.LifeInsuFeeMethod().readonly(true),
				    uCcsLoan.UnstmtPrepayPkgAmt().readonly(true),
				    uCcsLoan.PrepayPkgFeeMethod().readonly(true),
				    uCcsLoan.PrepayPkgFeeRate().readonly(true),
				    uCcsLoan.PaidPrepayPkgAmt().readonly(true),
				    uCcsLoan.PastPrepayPkgAmt().readonly(true),
				    uCcsLoan.TotPrepayPkgAmt().readonly(true),
				    uCcsLoan.CpdBeginDate().readonly(true),
				    
				    uCcsLoan.StampdutyRate().readonly(true),
				    uCcsLoan.InsuranceRate().readonly(true),
				    uCcsLoan.OverdueDate().readonly(true),
				    uCcsLoan.LastPenaltyDate().readonly(true),
				    uCcsLoan.InsuranceAmt().readonly(true),
				    uCcsLoan.UnstmtInsuranceAmt().readonly(true),
				    uCcsLoan.LoanInsuranceAmt().readonly(true),
				    uCcsLoan.PaidInsuranceAmt().readonly(true),
				    uCcsLoan.LoanInsFeeMethod().readonly(true),
				    uCcsLoan.StampdutyMethod().readonly(true),
				    uCcsLoan.PremiumAmt().readonly(true),		//添加趸交费、是否退还趸交费
				    uCcsLoan.PremiumInd().readonly(true),
				    uCcsLoan.ReplaceSvcFeeRate().readonly(true),     //代收服务费率
				    uCcsLoan.ReplaceSvcFeeAmt().readonly(true),		//代收服务费固定金额
				    uCcsLoan.ReplaceSvcFeeMethod().readonly(true),		//代收服务费收取方式
				    uCcsLoan.TotReplaceSvcFee().readonly(true),			//总代收服务费
				    uCcsLoan.UnstmtReplaceSvcFee().readonly(true),		//未出账单代收服务费
				    uCcsLoan.PastReplaceSvcFee().readonly(true),		//已出账单代收服务费
				    uCcsLoan.PaidReplaceSvcFee().readonly(true),			//已偿还代收服务费
				    uCcsLoan.ReplacePenaltyRate().readonly(true), 		//代收罚息利率
				    uCcsLoan.PrepayPkgInd().readonly(true),
				    uCcsLoan.ApplyTerm().readonly(true),
				    uCcsLoan.ApplyDelayTerm().readonly(true),
				    uCcsLoan.AccuApplyCount().readonly(true),
				    uCcsLoan.AccuDelayCount().readonly(true),
				    uCcsLoan.PayDateTerm().readonly(true),
				    uCcsLoan.PayDateBegDate().readonly(true),
				    uCcsLoan.PayDateAccu().readonly(true),
				    uCcsLoan.CompensateAmtSum().readonly(true),
				    uCcsLoan.CompensateCount().readonly(true),
				    uCcsLoan.CompensateRefundAmtSum().readonly(true),
				    uCcsLoan.CompensateRefundCount().readonly(true)
				    );
	                detailForm.getSetting().labelWidth(160);
			return detailForm; 
	}              
}