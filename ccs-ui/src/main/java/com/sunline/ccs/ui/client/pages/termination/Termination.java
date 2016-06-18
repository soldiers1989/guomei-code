/**
 * 
 */
package com.sunline.ccs.ui.client.pages.termination;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsCustomer;
import com.sunline.ccs.infrastructure.client.ui.UCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.ui.client.commons.BtnName;
import com.sunline.ccs.ui.client.commons.CommonUiUtils;
import com.sunline.ccs.ui.client.commons.PublicConstants;
import com.sunline.ccs.ui.client.commons.UIUtilConstants;
import com.sunline.kylin.web.ark.client.helper.DecimalColumnHelper;
import com.sunline.kylin.web.ark.client.helper.EnumColumnHelper;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
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
import com.sunline.ui.dialog.client.listener.ConfirmCallbackFunction;
import com.sunline.ui.event.client.EventObjectHandler;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.ISelectRowEventListener;
/**
 * 分期交易中止
* @author yeyu
 *
 */
@Singleton
public class Termination extends Page {
	
	@Inject
	private TerminationConstants constants;
	
	@Inject
	private PublicConstants publicConstants;
	@Inject
	private UIUtilConstants uiUtilsConstants;
	@Inject
	private UCcsLoan uTmLoan;//分期信息表UI对象
	
	@Inject
	private UCcsCustomer uTmCustomer; //客户信息表UI对象
	private KylinGrid listGrid;
	private KylinForm tranForm;
	private KylinForm commitForm;
	private TextColumnHelper cardItem;
	private TextColumnHelper si;
	private String contrNbr;
//	private String cardAccountIndicator;
	@SuppressWarnings("rawtypes")
	private EnumColumnHelper idTypeItem; 
	private TextColumnHelper idNoItem;
	private TextColumnHelper custNameItem;
	private int flag=0;//  0:表示分期交易中止   1:撤销分期中止
	private KylinButton submitButton; //分期交易中止
	private KylinButton cancelButton; //撤销分期中止
	private KylinForm searchForm; // 搜索框表单
	private TextColumnHelper descriptionItem;//分期产品描述
	private String loanCode;//分期计划代码

	private KylinButton searchBtf;
	
	@Override
	public IsWidget createPage() {
			VerticalPanel gridLayout = new VerticalPanel();
			gridLayout.setWidth("100%");
			gridLayout.setHeight("100px");
			// 设置结果框
			listGrid = new KylinGrid();
			listGrid.setWidth("98%");
			listGrid.setHeight("350px");
			listGrid.setColumns(
					uTmLoan.LoanId().setColumnWidth(80),
					uTmLoan.AcctType().setColumnWidth(80),
					uTmLoan.RefNbr().setColumnWidth(80),
					uTmLoan.CardNbr().setColumnWidth(80),
					uTmLoan.RegisterDate().setColumnWidth(80),
					uTmLoan.LoanType().setColumnWidth(80),
					uTmLoan.LoanStatus().setColumnWidth(80),
					uTmLoan.LastLoanStatus().setColumnWidth(80),
					uTmLoan.LoanInitTerm().setColumnWidth(80),
					uTmLoan.CurrTerm().setColumnWidth(80),
					uTmLoan.RemainTerm().setColumnWidth(80),
					uTmLoan.CurrTerm().setColumnWidth(80),
					uTmLoan.RemainTerm().setColumnWidth(80),
					uTmLoan.LoanInitPrin().setColumnWidth(80),
					uTmLoan.LoanInitFee().setColumnWidth(80),
					uTmLoan.OrigTxnAmt().setColumnWidth(100),
					uTmLoan.OrigTransDate().setColumnWidth(80),
					uTmLoan.OrigAuthCode().setColumnWidth(100),
					uTmLoan.LoanCode().setColumnWidth(80)
					);
			// 添加查询结果列表点击事件
			listGrid.getSetting().onSelectRow(new ISelectRowEventListener() {
				
				@Override
				public void selectRow(MapData rowdata, String rowid,EventObjectHandler rowobj) {
					tranForm.getUi().clear();
					if(null == rowdata) {
						return;
					}
					loanCode =rowdata.getString(CcsLoan.P_LoanCode); 
					//当点击列表的值时，显示分期产品描述
					RPC.ajax("rpc/t1310Server/getLoanPlanDescription", new RpcCallback<Data>() {
						@Override
						public void onSuccess(Data result) {
							tranForm.setFieldValue("loanPlanDescription", result.asMapData().getString("desc"));
						}
					},loanCode);
					//显示所选择的交易明细
					tranForm.setFormData(rowdata);
				}
			}); 
			listGrid.getSetting().delayLoad(true);
			listGrid.loadDataFromUrl("rpc/t1310Server/getTmLoanList");
			gridLayout.add(createSearchForm());
			gridLayout.add(listGrid);
			VerticalPanel blank = new VerticalPanel();
			blank.setWidth("100%");
			blank.setHeight("10px");
			gridLayout.add(blank);
			
			StackPanel sPanel = new StackPanel();
			sPanel.setWidth("98%");
			sPanel.setHeight("300px");
			ScrollPanel sp = new ScrollPanel();
			sp.add(createTransDetailForm());
			sp.setHeight("300px");
			sPanel.add(sp);
        	gridLayout.add(sPanel);
        	
    		submitButton = new KylinButton(BtnName.STAGETERMINATION.getBtnName(),null);
    		submitButton.addClickEventListener(new IClickEventListener() {
    			
    			@Override
    			public void onClick() {
    				flag=0;
    				submit();
    				
    			}
    		});
    		submitButton.getSetting().width(100);
    		
            cancelButton = new KylinButton(BtnName.REVOKESTAGETERMINATION.getBtnName(),null);
            cancelButton.addClickEventListener(new IClickEventListener() {
    			
    			@Override
    			public void onClick() {
    				flag=1;
    				submit();
    				
    			}
    		});
            cancelButton.getSetting().width(100);
        	
            commitForm = new KylinForm();
            commitForm.setCol(4);
            commitForm.addButton(submitButton);
            commitForm.addButton(cancelButton);
            
            gridLayout.add(commitForm);
        	
        	return gridLayout;
	}
	
	/**
	 * 创建分期交易详细信息表单
	 * @return
	 */
	private KylinForm createTransDetailForm() {
		tranForm = new KylinForm();
		tranForm.setWidth("98%");
		tranForm.setHeight("300px");
		tranForm.getSetting().labelWidth(120);
		DecimalColumnHelper interestRateItem = uTmLoan.InterestRate();
		descriptionItem = new TextColumnHelper("loanPlanDescription",constants.loanPlanDescription(),12);
		tranForm.setField(
				uTmLoan.LoanId(),
				uTmLoan.AcctNbr(),
				uTmLoan.AcctType(),
				uTmLoan.ContrNbr(),
				interestRateItem,
				descriptionItem.asText(),
				uTmLoan.RefNbr().asText(),
				uTmLoan.CardNbr().asText(),
				uTmLoan.RegisterDate(),
				uTmLoan.LoanType(),
				uTmLoan.LoanStatus(),
				uTmLoan.LastLoanStatus(),
				uTmLoan.LoanInitTerm().asSelectItem(),
				uTmLoan.CurrTerm().asSelectItem(),
				uTmLoan.RemainTerm().asSelectItem(),
				uTmLoan.LoanInitPrin().asCurrency(),
				uTmLoan.LoanFixedPmtPrin().asCurrency(),
				uTmLoan.LoanFirstTermPrin().asCurrency(),
				uTmLoan.LoanFinalTermPrin().asCurrency(),
				uTmLoan.LoanInitFee().asCurrency(),
				uTmLoan.LoanFixedFee().asCurrency(),
				uTmLoan.LoanFirstTermFee().asCurrency(),
				uTmLoan.LoanFinalTermFee().asCurrency(),
				uTmLoan.UnstmtPrin().asCurrency(),
				uTmLoan.UnstmtFee().asCurrency(),
				uTmLoan.ActiveDate(),
				uTmLoan.PaidOutDate(),
				uTmLoan.TerminalDate(),
				uTmLoan.TerminalReasonCd(),
				uTmLoan.PaidPrincipal().asCurrency(),
				uTmLoan.PaidInterest().asCurrency(),
				uTmLoan.PaidFee().asCurrency(),
				uTmLoan.LoanCurrBal().asCurrency(),
				uTmLoan.LoanBalXfrout().asCurrency(),
				uTmLoan.LoanBalXfrin().asCurrency(),
				uTmLoan.LoanPrinXfrout().asCurrency(),
				uTmLoan.LoanPrinXfrin().asCurrency(),
				uTmLoan.LoanFeeXfrout().asCurrency(),
				uTmLoan.LoanFeeXfrin().asCurrency(),
				uTmLoan.OrigTxnAmt().asCurrency(),
				uTmLoan.OrigTransDate(),
				uTmLoan.OrigAuthCode().asText(),
				
				uTmLoan.TotLifeInsuAmt().readonly(true),
			    uTmLoan.UnstmtLifeInsuAmt().readonly(true),
			    uTmLoan.PastLifeInsuAmt().readonly(true),
			    uTmLoan.PaidLifeInsuAmt().readonly(true),
			    uTmLoan.LifeInsuFeeRate().readonly(true),
			    uTmLoan.LifeInsuFeeMethod().readonly(true),
			    uTmLoan.UnstmtPrepayPkgAmt().readonly(true),
			    uTmLoan.PrepayPkgFeeMethod().readonly(true),
			    uTmLoan.PrepayPkgFeeRate().readonly(true),
			    uTmLoan.PaidPrepayPkgAmt().readonly(true),
			    uTmLoan.PastPrepayPkgAmt().readonly(true),
			    uTmLoan.TotPrepayPkgAmt().readonly(true),
			    uTmLoan.CpdBeginDate().readonly(true)
		);
		tranForm.getSetting().readonly(true);
        tranForm.getSetting().labelWidth(130);
		return tranForm;
	}
	
	/**
	 * 创建搜索表单
	 * @return
	 */
	private KylinForm createSearchForm() {
		searchForm = new KylinForm();
		{
			searchForm.setCol(6);
			searchForm.setWidth("98%");
			//卡号换为合同号
			cardItem = uTmLoan.ContrNbr();
			
			Scheduler.get().scheduleFinally(new RepeatingCommand() {
				@Override
				public boolean execute() {
					searchForm.setFieldSelectData(UIUtilConstants.INDICATOR_NAME_CARD_ACCT, CommonUiUtils.cardOrAcctSelectItem(uiUtilsConstants));
					return false;
				}
			});
			si =  new TextColumnHelper(UIUtilConstants.INDICATOR_NAME_CARD_ACCT, uiUtilsConstants.cardAccIndTitle(), 19);
			si.required(true);
			si.asSelectItem();
			idTypeItem = uTmCustomer.IdType();
			idTypeItem.readonly(true);
			idTypeItem.asSelectItem(SelectType.KEY_LABLE);
			idNoItem =uTmCustomer.IdNo();
			idNoItem.readonly(true);
			custNameItem = uTmCustomer.Name();
			searchForm.setField(cardItem.required(true));
			searchBtf=ClientUtils.createSearchButton(new IClickEventListener() {
				@Override
				public void onClick() {
					if (searchForm.getSubmitData()!=null) {
						contrNbr =searchForm.getFieldValue(uTmLoan.ContrNbr().getName());
//						cardAccountIndicator =searchForm.getFieldValue(si.getName());
//						loadCustomerByCardNo(cardNo);
						listGrid.getUi().setParm(uTmLoan.ContrNbr().getName(), contrNbr);
//						listGrid.getUi().setParm("cardAccountIndicator", cardAccountIndicator);
						listGrid.loadDataFromUrl("rpc/t1310Server/getTmLoanList");
					} else {
						Dialog.alert(publicConstants.msgSearchConditionInvalid());
					}
				}
			});
		}
		searchForm.addButton(searchBtf);
		return searchForm;
	}
	
	/**
	 * 远程获取账户信息表-授权 信用额度和新额度
	 * @param objs
	 */
	private void loadCustomerByCardNo(final String mediaCardNo) {
		RPC.ajax("rpc/custServer/getCustInfoByCardNo", new RpcCallback<Data>() {
			@Override
			public void onSuccess(Data result) {
				searchForm.setFormData(result);
			}
		},mediaCardNo);
	}

	/* (non-Javadoc)
	 * @see com.sunline.kylin.web.core.client.res.Page#refresh()
	 */
	@Override
	public void refresh() {
	    tranForm.getUi().clear();
	    searchForm.getUi().clear();
	    listGrid.clearData();
	}

	private void submit() {
		if(tranForm.getFieldValue(uTmLoan.ContrNbr().getName())  == null){
			Dialog.alert("请选择一条数据!");
			return ;
		}
	if (tranForm.getSubmitData()!=null){
		String warningTitle="";
		if(flag == 0){
			warningTitle=constants.submitButtonWarning();
		}else{
			warningTitle=constants.CancelButtonWarning();
		}
		Dialog.confirm(warningTitle,"信息",new ConfirmCallbackFunction() {
			@SuppressWarnings("all")
			@Override
			public void corfimCallback(boolean value) {

				if(value){
					RPC.ajaxMask("rpc/t1310Server/saveInstalmentPlan", new RpcCallback<Data>() {
						@Override
						public void onSuccess(Data data) {
							int result=data.asMapData().getInteger("result");
							switch(result) {
							case 0:
//								tranForm.getUi().clear();
								listGrid.loadData(searchForm);
								Dialog.alertSuccess("操作成功", "信息");
								
								break;
							default:
								break;
							}
						
						}
					},tranForm.getSubmitData().asMapData(),flag);
					
				}
			
			}
		}); 
	}
}

	
}