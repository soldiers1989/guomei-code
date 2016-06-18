package com.sunline.ccs.ui.client.pages.prepaymentInfo;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsLoanReg;
import com.sunline.ccs.infrastructure.client.ui.UCcsLoanRegHst;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.event.client.EventObjectHandler;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.ISelectRowEventListener;
import com.sunline.ui.tab.client.Tab;
import com.sunline.ui.tab.client.TabItemSetting;
import com.sunline.ui.tab.client.TabSetting;

@Singleton
public class PrepaymentInfoPage extends Page{
	private KylinForm form;
	private KylinGrid grid;
	private KylinGrid hstGrid;
	private KylinForm detailForm;
	private KylinForm hstDetailForm;
	@Inject
	private UCcsLoanReg uLoanReg;
	@Inject
	private UCcsLoanRegHst uLoanRegHst;
	@Override
	public IsWidget createPage() {
		VerticalPanel panel=new VerticalPanel();
		panel.setWidth("100%");
		
		form=new KylinForm();
		{
			form.setWidth("98%");
			form.setField(uLoanReg.ContrNbr());
			form.setCol(2);
			form.getSetting().labelWidth(70);
			form.addButton(ClientUtils.createSearchButton(new IClickEventListener(){

				@Override
				public void onClick() {
					grid.clearData();
					hstGrid.clearData();
					detailForm.getUi().clear();
					hstDetailForm.getUi().clear();
					grid.loadData(form);
					hstGrid.loadData(form);
				}
				
			}));
		}
		
		grid=new KylinGrid();
		{
			grid.setWidth("98%");
			grid.setHeight(350);
			grid.setColumns(
					uLoanReg.AcctNbr().setColumnWidth(100), 
					uLoanReg.RegisterDate().setColumnWidth(100),
					uLoanReg.LoanType().columnRender().setColumnWidth(100),
					uLoanReg.LoanInitTerm().setColumnWidth(100),
					uLoanReg.LoanRegStatus().columnRender().setColumnWidth(100),
					uLoanReg.LoanFeeDefId().setColumnWidth(100),
					uLoanReg.ContrNbr().setColumnWidth(200)
                    );
			grid.getSetting().onSelectRow(new ISelectRowEventListener(){

				@Override
				public void selectRow(MapData rowdata, String rowid,EventObjectHandler rowobj) {
					detailForm.setFormData(rowdata);
				}
				
			});
		}
		
		hstGrid=new KylinGrid();
		{
			hstGrid.setWidth("98%");
			hstGrid.setHeight(350);
			hstGrid.setColumns(
					uLoanRegHst.AcctNbr().setColumnWidth(100), 
					uLoanRegHst.RegisterDate().setColumnWidth(100),
					uLoanRegHst.LoanType().columnRender().setColumnWidth(100),
					uLoanRegHst.LoanInitTerm().setColumnWidth(100),
					uLoanRegHst.LoanRegStatus().columnRender().setColumnWidth(100),
					uLoanRegHst.LoanFeeDefId().setColumnWidth(100),
					uLoanRegHst.ContrNbr().setColumnWidth(200)
                    );
			hstGrid.getSetting().onSelectRow(new ISelectRowEventListener(){

				@Override
				public void selectRow(MapData rowdata, String rowid,EventObjectHandler rowobj) {
					hstDetailForm.setFormData(rowdata);
				}
				
			});
		}
		
		ScrollPanel layout=new ScrollPanel();
		layout.setWidth("100%");
		layout.setHeight("200px");
		detailForm=new KylinForm();
		{
			detailForm.setWidth("98%");
//			detailForm.setHeight("200px");
			detailForm.setField(
					uLoanReg.DueBillNo().readonly(true),uLoanReg.ContrNbr().readonly(true),
					uLoanReg.GuarantyId().readonly(true),
					uLoanReg.Org().readonly(true), 
					uLoanReg.AcctNbr().readonly(true), 
					uLoanReg.LoanAction().readonly(true),
					uLoanReg.AcctType().readonly(true),
					uLoanReg.RefNbr().readonly(true), uLoanReg.LogicCardNbr().readonly(true), uLoanReg
							.CardNbr().readonly(true), uLoanReg.RegisterDate().readonly(true), uLoanReg.RegisterId().readonly(true),
							uLoanReg.RequestTime().readonly(true), uLoanReg.LoanType().readonly(true),
					uLoanReg.LoanInitTerm().readonly(true),
					uLoanReg.LoanInitPrin().readonly(true),
					uLoanReg.LoanFixedPmtPrin().readonly(true), uLoanReg
							.LoanFirstTermPrin().readonly(true),
					uLoanReg.LoanFinalTermPrin().readonly(true), uLoanReg.LoanInitFee().readonly(true),
					uLoanReg.LoanFixedFee().readonly(true), uLoanReg.LoanFirstTermFee().readonly(true),
				 uLoanReg.OrigTxnAmt().readonly(true), uLoanReg
							.OrigTransDate().readonly(true), uLoanReg.OrigAuthCode().readonly(true),
							uLoanReg.LoanCode().readonly(true),
							uLoanReg.LoanFeeMethod().readonly(true),
							uLoanReg.InterestRate().readonly(true), uLoanReg.PenaltyRate().readonly(true),
					uLoanReg.CompoundRate().readonly(true), uLoanReg.FloatRate().readonly(true), uLoanReg
							.DueBillNo().readonly(true), 
					uLoanReg.LoanCode().readonly(true), uLoanReg.AdvPmtAmt().readonly(true),
					uLoanReg.StampdutyRate().readonly(true), uLoanReg.InsuranceRate().readonly(true),
					uLoanReg.InsuranceAmt().readonly(true), 
							 uLoanReg.LoanInsFeeMethod().readonly(true), uLoanReg
							.StampdutyMethod().readonly(true),
							
							uLoanReg.TotLifeInsuAmt().readonly(true),
							uLoanReg.LifeInsuFeeRate().readonly(true),
							uLoanReg.LifeInsuFeeMethod().readonly(true),
							uLoanReg.PrepayPkgFeeMethod().readonly(true),
							uLoanReg.PrepayPkgFeeRate().readonly(true),
							uLoanReg.TotPrepayPkgAmt().readonly(true),
							uLoanReg.PreAdDate().readonly(true),
							uLoanReg.PreAdAmt().readonly(true),
							uLoanReg.PremiumAmt().readonly(true),  //添加趸交费、是否退还趸交费
							uLoanReg.PremiumInd().readonly(true),
							uLoanReg.ReplaceSvcFeeRate().readonly(true),     //代收服务费率
							uLoanReg.ReplaceSvcFeeAmt().readonly(true),		//代收服务费固定金额
							uLoanReg.ReplaceSvcFeeMethod().readonly(true),		//代收服务费收取方式
							uLoanReg.TotReplaceSvcFee().readonly(true),		//总代收服务费
							uLoanReg.ReplacePenaltyRate().readonly(true), 		//代收罚息利率
							uLoanReg.PrepayPkgInd().readonly(true),
							uLoanReg.ApplyTerm().readonly(true),
							uLoanReg.ApplyDelayTerm().readonly(true),
							uLoanReg.PayDateTerm().readonly(true),
							uLoanReg.PayDateBegDate().readonly(true),
							uLoanReg.PayDateAccu().readonly(true),
							uLoanReg.PayDateOrigin().readonly(true),
							uLoanReg.PayDateAfter().readonly(true)
			);
			detailForm.setCol(3);
			detailForm.getSetting().labelWidth(135);
			
		}
		
		ScrollPanel hstLayout=new ScrollPanel();
		hstLayout.setWidth("100%");
		hstLayout.setHeight("200px");
		hstDetailForm=new KylinForm();
		{
			hstDetailForm.setWidth("98%");
			hstDetailForm.setField(
					uLoanRegHst.DueBillNo().readonly(true),
					uLoanRegHst.ContrNbr().readonly(true),
					uLoanRegHst.GuarantyId().readonly(true),
					uLoanRegHst.Org().readonly(true), 
					uLoanRegHst.AcctNbr().readonly(true), 
					uLoanRegHst.LoanAction().readonly(true),
					uLoanRegHst.AcctType().readonly(true),
					uLoanRegHst.RefNbr().readonly(true), 
					uLoanRegHst.LogicCardNbr().readonly(true), 
					uLoanRegHst.CardNbr().readonly(true), 
					uLoanRegHst.RegisterDate().readonly(true), 
					uLoanRegHst.RegisterId().readonly(true),
					uLoanRegHst.RequestTime().readonly(true), 
					uLoanRegHst.LoanType().readonly(true),
					uLoanRegHst.LoanInitTerm().readonly(true),
					uLoanRegHst.LoanInitPrin().readonly(true),
					uLoanRegHst.LoanFixedPmtPrin().readonly(true),
					uLoanRegHst.LoanFirstTermPrin().readonly(true),
					uLoanRegHst.LoanFinalTermPrin().readonly(true), 
					uLoanRegHst.LoanInitFee().readonly(true),
					uLoanRegHst.LoanFixedFee().readonly(true), 
					uLoanRegHst.LoanFirstTermFee().readonly(true),
				    uLoanRegHst.OrigTxnAmt().readonly(true), 
				    uLoanRegHst.OrigTransDate().readonly(true), 
				    uLoanRegHst.OrigAuthCode().readonly(true),
					uLoanRegHst.LoanCode().readonly(true),
					uLoanRegHst.LoanFeeMethod().readonly(true),
					uLoanRegHst.InterestRate().readonly(true), 
					uLoanRegHst.PenaltyRate().readonly(true),
					uLoanRegHst.CompoundRate().readonly(true), 
					uLoanRegHst.FloatRate().readonly(true), 
					uLoanRegHst.DueBillNo().readonly(true), 
					uLoanRegHst.LoanCode().readonly(true),
					uLoanRegHst.AdvPmtAmt().readonly(true),
					uLoanRegHst.StampdutyRate().readonly(true),
					uLoanRegHst.InsuranceRate().readonly(true),
					uLoanRegHst.InsuranceAmt().readonly(true), 
					uLoanRegHst.LoanInsFeeMethod().readonly(true), 
					uLoanRegHst.StampdutyMethod().readonly(true),
							
					uLoanRegHst.TotLifeInsuAmt().readonly(true),
					uLoanRegHst.LifeInsuFeeRate().readonly(true),
					uLoanRegHst.LifeInsuFeeMethod().readonly(true),
					uLoanRegHst.PrepayPkgFeeMethod().readonly(true),
					uLoanRegHst.PrepayPkgFeeRate().readonly(true),
					uLoanRegHst.TotPrepayPkgAmt().readonly(true),
					uLoanRegHst.PreAdDate().readonly(true),
					uLoanRegHst.PreAdAmt().readonly(true),
					uLoanRegHst.PremiumAmt().readonly(true),  //添加趸交费、是否退还趸交费
					uLoanRegHst.PremiumInd().readonly(true),
					uLoanRegHst.ReplaceSvcFeeRate().readonly(true),     //代收服务费率
					uLoanRegHst.ReplaceSvcFeeAmt().readonly(true),		//代收服务费固定金额
					uLoanRegHst.ReplaceSvcFeeMethod().readonly(true),		//代收服务费收取方式
					uLoanRegHst.TotReplaceSvcFee().readonly(true),		//总代收服务费
					uLoanRegHst.ReplacePenaltyRate().readonly(true), 		//代收罚息利率
					uLoanRegHst.PrepayPkgInd().readonly(true),
					uLoanRegHst.ApplyTerm().readonly(true),
					uLoanRegHst.ApplyDelayTerm().readonly(true),
					uLoanRegHst.PayDateTerm().readonly(true),
					uLoanRegHst.PayDateBegDate().readonly(true),
					uLoanRegHst.PayDateAccu().readonly(true),
					uLoanRegHst.PayDateOrigin().readonly(true),
					uLoanRegHst.PayDateAfter().readonly(true)
			);
			hstDetailForm.setCol(3);
			hstDetailForm.getSetting().labelWidth(135);
			
		}
		
		layout.add(detailForm);
		hstLayout.add(hstDetailForm);
		
		TabSetting tabSetting = new TabSetting().dblClickToClose(true).dragToMove(true).showSwitch(true);
		Tab tab = new Tab(tabSetting);

		TabItemSetting loanRegTabSetting = new TabItemSetting(null, "提前还款信息");
		tab.addItem(loanRegTabSetting, layout);
		TabItemSetting loanRegHstTabSetting = new TabItemSetting(null, "提前还款历史信息");
		tab.addItem(loanRegHstTabSetting, hstLayout);
		
		HorizontalPanel hPanel = new HorizontalPanel();
		hPanel.setWidth("100%");
		hPanel.add(grid);
		hPanel.add(hstGrid);
		
		panel.add(form);
		panel.add(hPanel);
		panel.add(tab);
		return panel;
	}
	
	public void refresh(){
		form.getUi().clear();
		grid.clearData();
		hstGrid.clearData();
		detailForm.getUi().clear();
		hstDetailForm.getUi().clear();
		grid.loadDataFromUrl("rpc/prepaymentInfoServer/getPrepaymentList");
		hstGrid.loadDataFromUrl("rpc/prepaymentInfoServer/getPrepaymentHstList");
	}
	
}
