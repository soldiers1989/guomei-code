package com.sunline.ccs.ui.client.pages.artificialdeposit;

import java.math.BigDecimal;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ark.support.meta.RPCVersion;
import com.sunline.ccs.infrastructure.client.ui.UCcsLoanReg;
import com.sunline.ccs.infrastructure.client.ui.UCcsOrder;
import com.sunline.ccs.infrastructure.client.ui.UCcsPlan;
import com.sunline.kylin.web.ark.client.helper.DecimalColumnHelper;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.EventObjectHandler;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.ISelectRowEventListener;
import com.sunline.ui.tab.client.Tab;
import com.sunline.ui.tab.client.TabItemSetting;
import com.sunline.ui.tab.client.TabSetting;
/**
 * 手工溢缴款转出页面
 * @author xubin
 *
 */
@Singleton
public class ArtificialDepositPage extends Page{
	private KylinForm form;
	
	private KylinGrid planGrid;
	
	private KylinGrid orderGrid;
	
	private KylinForm planForm;
	
	private KylinForm orderForm;
	
	@Inject
	private UCcsPlan uCcsPlan;
	
	@Inject
	private UCcsOrder uCcsOrder;
	
	private KylinButton button; //试算按钮
	
	private KylinButton depositButton; //溢缴款转出按钮
	
	private KylinForm depositForm;
	
	@Inject
	private ArtificialDepositDialog artificialDepositDialog;
	
	
	public void refresh(){
		form.getUi().clear();
		planForm.getUi().clear();
		orderForm.getUi().clear();
		planGrid.clearData();
		orderGrid.clearData();
		planGrid.loadDataFromUrl("rpc/artificialDepositServer/getPlanList");
		orderGrid.loadDataFromUrl("rpc/artificialDepositServer/getOrderList");
	}

	@Override
	public IsWidget createPage() {
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");
		
		form = new KylinForm();
		{
			form.setWidth("98%");
			form.setField(uCcsOrder.ContrNbr());
			form.setCol(2);
			form.getSetting().labelWidth(70);
			form.addButton(ClientUtils.createSearchButton(new IClickEventListener() {
				
				@Override
				public void onClick() {
					planGrid.clearData();
					orderGrid.clearData();
					planForm.getUi().clear();
					orderForm.getUi().clear();
					planGrid.loadData(form);
					orderGrid.loadData(form);
					
				}
			}));
			
		}
		
		//信用计划列表
		planGrid = new KylinGrid();
		{
			planGrid.setWidth("98%");
			planGrid.setHeight(350);
			planGrid.setColumns(uCcsPlan.AcctNbr().setColumnWidth(100),
								uCcsPlan.PlanType().columnRender().setColumnWidth(100),
								uCcsPlan.CurrBal().setColumnWidth(100),
								uCcsPlan.Term().setColumnWidth(100),
								uCcsPlan.PlanId().setColumnWidth(100),
								uCcsPlan.PlanNbr().setColumnWidth(150)
								);
			planGrid.getSetting().onSelectRow(new ISelectRowEventListener() {
				
				@Override
				public void selectRow(MapData rowdata, String rowid,
						EventObjectHandler rowobj) {
					planForm.setFormData(rowdata);
					
				}
			});
		}
		
		//溢缴款转出申请列表
		orderGrid = new KylinGrid();
		{
			orderGrid.setWidth("98%");
			orderGrid.setHeight(350);
			orderGrid.setColumns(uCcsOrder.AcctNbr().setColumnWidth(100),
								uCcsOrder.OriOrderId().setColumnWidth(100),
								uCcsOrder.OrderStatus().columnRender().setColumnWidth(100),
								uCcsOrder.TxnAmt().setColumnWidth(100),
								uCcsOrder.LoanUsage().columnRender().setColumnWidth(100),
								uCcsOrder.BusinessDate().format("yyyyMMdd").setColumnWidth(150),
								uCcsOrder.ContrNbr().setColumnWidth(200)
								);
			orderGrid.getSetting().onSelectRow(new ISelectRowEventListener() {
				
				@Override
				public void selectRow(MapData rowdata, String rowid,
						EventObjectHandler rowobj) {
					orderForm.setFormData(rowdata);
					
				}
			});
		}
		
		//信用计划详情表单
		ScrollPanel planLayout = new ScrollPanel();
		planLayout.setWidth("100%");
		planLayout.setHeight("200px");
		planForm = new KylinForm();
		{
			planForm.setWidth("98%");
			planForm.setField(uCcsPlan.CompoundRate().readonly(true),
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
					uCcsPlan.PastReplaceTxnFee().readonly(true) 		
					);
			planForm.setCol(3);
			planForm.getSetting().labelWidth(135);
		}
		
		//贷款信息详情表单
		ScrollPanel loanRegLayout = new ScrollPanel();
		loanRegLayout.setWidth("100%");
		loanRegLayout.setHeight("200px");
		orderForm = new KylinForm();
		{
			orderForm.setWidth("98%");
			orderForm.setField(uCcsOrder.AcctNbr().readonly(true),
                    uCcsOrder.AcctType().readonly(true),
                    uCcsOrder.ContrNbr().readonly(true),
                    uCcsOrder.BusinessDate().readonly(true),
                    uCcsOrder.CardNo().readonly(true),
                    uCcsOrder.CardType().readonly(true),
                    uCcsOrder.CertId().readonly(true),
                    uCcsOrder.CertType().readonly(true),
                    uCcsOrder.RefNbr().readonly(true),
                    uCcsOrder.ChannelId().readonly(true),
                    uCcsOrder.Code().readonly(true),
                    uCcsOrder.City().readonly(true),
                    uCcsOrder.CommandType().readonly(true),
                    uCcsOrder.Currency().readonly(true),
                    uCcsOrder.DueBillNo().readonly(true),
                    uCcsOrder.FailureAmt().readonly(true),
                    uCcsOrder.Flag().readonly(true),
                    uCcsOrder.GuarantyId().readonly(true),
                    uCcsOrder.LoanUsage().readonly(true),
                    uCcsOrder.LogKv().readonly(true),
                    uCcsOrder.MerId().readonly(true),
                    uCcsOrder.MerName().readonly(true),
                    uCcsOrder.Message().readonly(true),
                    uCcsOrder.OnlineFlag().readonly(true),
                    uCcsOrder.OpenBank().readonly(true),
                    uCcsOrder.OpenBankId().readonly(true),
                    uCcsOrder.OptDatetime().readonly(true),
                    uCcsOrder.OrberFailTime().readonly(true),
                    uCcsOrder.OrderBrief().readonly(true),
                    uCcsOrder.OrderId().readonly(true),
                    uCcsOrder.OrderStatus().readonly(true),
                    uCcsOrder.OrderTime().readonly(true),
                    uCcsOrder.OriOrderId().readonly(true),
                    uCcsOrder.PayBizCode().readonly(true),
                    uCcsOrder.PayChannelId().readonly(true),
                    uCcsOrder.ProductType().readonly(true),
                    uCcsOrder.Purpose().readonly(true),
                    uCcsOrder.SendTime().readonly(true),
                    uCcsOrder.SetupDate().readonly(true),
                    uCcsOrder.State().readonly(true),
                    uCcsOrder.Status().readonly(true),
                    uCcsOrder.SubBank().readonly(true),
                    uCcsOrder.SuccessAmt().readonly(true),
                    uCcsOrder.TxnAmt().readonly(true),
                    uCcsOrder.TxnType().readonly(true),
                    uCcsOrder.UsrName().readonly(true),
                    uCcsOrder.LoanAmt().readonly(true),//添加放款金额、趸交费金额
                    uCcsOrder.PremiumAmt().readonly(true)
					);
			orderForm.setCol(3);
			orderForm.getSetting().labelWidth(135);
		}
		
		planLayout.add(planForm);
		loanRegLayout.add(orderForm);
		
		//手工溢缴款试算按钮
		button = new KylinButton("溢缴款试算", null);
		{
			button.updateWidth(70);
			button.addClickEventListener(new IClickEventListener() {
				
				@Override
				public void onClick() {
					RPC.ajaxMask("rpc/artificialDepositServer/trialDeposiet", new RpcCallback<Data>() {
						
						@Override
						public void onSuccess(Data result) {
							if(result.asMapData().getString("errorCode").equals("0000")){
								artificialDepositDialog.setMapData(result.asMapData());
								artificialDepositDialog.show();
							}else{
								Dialog.alert(result.asMapData().getString("errorMessage"));
							}
							
						}
					}, depositForm.getFieldValue("contrNbr"));
				}
			});
		}
		
		//手工溢缴款转出按钮
		depositButton =new KylinButton("溢缴款转出", null);
		{
			depositButton.updateWidth(70);
			depositButton.addClickEventListener(new IClickEventListener() {
				
				@Override
				public void onClick() {
					RPC.ajaxMask("rpc/artificialDepositServer/rollOutDeposiet", new RpcCallback<Data>() {
						
						@Override
						public void onSuccess(Data result) {
							
								Dialog.alert("处理结果：" + result);
							
						}
					},depositForm.getFieldValue("contrNbr"),depositForm.getFieldValue("transferLmt"));
					
				}
			});
		}
		
		//溢缴款转出按钮表单
		depositForm = new KylinForm();
		{
			depositForm.setWidth("100%");
			depositForm.setField(new TextColumnHelper("contrNbr", "合同号", 20).required(true),
					new DecimalColumnHelper("transferLmt", "转出金额", 15, new BigDecimal(-999999999), new BigDecimal(999999999) , 20));
			depositForm.addButton(button);
			depositForm.addButton(depositButton);
		}
		
		
		
		
		TabSetting tabSetting = new TabSetting().dblClickToClose(true).dragToMove(true).showSwitch(true);
		Tab tab = new Tab(tabSetting);
		
		TabItemSetting planTabSetting = new TabItemSetting(null, "信用计划信息");
		tab.addItem(planTabSetting, planLayout);
		TabItemSetting loanRegTabSetting = new TabItemSetting(null, "贷款信息详情");
		tab.addItem(loanRegTabSetting, loanRegLayout);
		
		HorizontalPanel hPanel = new HorizontalPanel();
		hPanel.setWidth("100%");
		hPanel.add(planGrid);
		hPanel.add(orderGrid);
		
		panel.add(form);
		panel.add(hPanel);
		panel.add(tab);
		panel.add(depositForm);
		
		return panel;
	}
	
	

}
