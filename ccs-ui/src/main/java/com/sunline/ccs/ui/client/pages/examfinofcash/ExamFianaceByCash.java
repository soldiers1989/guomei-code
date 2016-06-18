package com.sunline.ccs.ui.client.pages.examfinofcash;

import java.util.Date;

import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsLoanReg;
import com.sunline.ccs.ui.client.commons.BtnName;
import com.sunline.ccs.ui.client.commons.CommonGlobalConstants;
import com.sunline.ccs.ui.client.commons.CommonUiUtils;
import com.sunline.kylin.web.ark.client.helper.DateColumnHelper;
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
import com.sunline.ui.grid.client.listener.IDblClickRowEventListener;

/**
 * 现金分期审核页面
 * 
 * @author songyanchao
 * @date 2013-7-12 上午11:24:45
 * @version 1.0
 */
@Singleton
public class ExamFianaceByCash extends Page {
    @Inject
    private UCcsLoanReg uCcsLoanReg;
    private KylinForm searchForm;
    @Inject
    private ExamFianaceByCashConstants constants;
    @Inject
    private CommonGlobalConstants publicConstants;
    private KylinForm detailForm;
    private KylinGrid searchGrid = new KylinGrid();
    // 同意
    private KylinButton agreeButton;
    // 拒绝
    private KylinButton refuseButton;

    @Inject
    private ClientUtils clientUtils;
    private Date beginDate;
    private Date endDate;
    
    @Override
    public IsWidget createPage() {

	// 返回组件
	VerticalPanel panel = new VerticalPanel();
	panel.setWidth("98%");

	// 搜索
	panel.add(createTimeSearchForm());

	// 列表
	StackPanel gridSpanel = new StackPanel();
	gridSpanel.setWidth("100%");
	gridSpanel.add(createListGrid(), constants.cashStage());
	gridSpanel.setHeight("270px");
	panel.add(gridSpanel);

	// 明细
	StackPanel detailSpanel = new StackPanel();
	detailSpanel.setWidth("100%");
	ScrollPanel sPanel = new ScrollPanel();
	sPanel.add(showDetailMsg());
	sPanel.setHeight("300px");
	detailSpanel.add(sPanel, constants.showDetailStack());
	detailSpanel.setHeight("300px");
	panel.add(detailSpanel);

	// 同意/拒绝
	panel.add(agreeOrRefuseBtn());
	return panel;
    }

    // 搜索form
    private HorizontalPanel createTimeSearchForm() {
    	HorizontalPanel hPanel;
		searchForm = new KylinForm();
		{
		    searchForm.setWidth("98%");
		    searchForm.setCol(3);
		    // 起始日期
		    DateColumnHelper beginDateItem =
			    new DateColumnHelper("beginDate", publicConstants.labelBeginDate(), true, false);
		    // 截止日期
		    DateColumnHelper endDateItem = new DateColumnHelper("endDate", publicConstants.labelEndDate(), true, false);
		    searchForm.setField(beginDateItem, endDateItem);
		    
		    KylinButton btn = ClientUtils.createSearchButton(new IClickEventListener(){
				@Override
				public void onClick() {
				    agreeButton.setVisible(false);
				    refuseButton.setVisible(false);
				    detailForm.getUi().clear();
				    searchGrid.loadData(searchForm);
				}
			    });
		    searchForm.setHeight("20%");
		    hPanel= CommonUiUtils.lineLayoutForm(searchForm, btn, null, null);
		}
		return hPanel;
    }

    @Override
    public void refresh() {
		searchForm.getUi().clear();
		searchGrid.clearData();
		detailForm.getUi().clear();
		agreeButton.setVisible(false);
		refuseButton.setVisible(false);
    }

    private KylinGrid createListGrid() {

	searchGrid = new KylinGrid();
	searchGrid.checkbox(false);
	searchGrid.setWidth("98%");
	searchGrid.setHeight("270px");
	searchGrid.loadDataFromUrl("rpc/t3305Server/getNeedExamineList");
	searchGrid.getSetting().delayLoad(true);
	searchGrid.setColumns(uCcsLoanReg.AcctNbr(), uCcsLoanReg.AcctType(), uCcsLoanReg.CardNbr(),
			      uCcsLoanReg.LoanInitTerm(), uCcsLoanReg.LoanInitFee(), uCcsLoanReg.LoanType(),
			      uCcsLoanReg.RegisterDate());

	// 选中listGird记录时为detailForm赋值
	searchGrid.getSetting().onDblClickRow(new IDblClickRowEventListener(){

	    @Override
	    public void onDblClickRow(MapData data, String rowid, EventObjectHandler row) {
		detailForm.getUi().clear();
		detailForm.setFormData(data);
		agreeButton.setVisible(true);
		refuseButton.setVisible(true);
	    }
	});

	return searchGrid;
    }

    /**
     * 创建显示现金分期申请的详细信息
     * 
     * @return
     */
    private KylinForm showDetailMsg() {

		detailForm = new KylinForm();
		detailForm.setWidth("98%");
		detailForm.setTitle(constants.showDetailStack());
		detailForm.setCol(3);
		
		detailForm.setField(uCcsLoanReg.RegisterId().readonly(true), uCcsLoanReg.AcctNbr().readonly(true), uCcsLoanReg
					    .AcctType().readonly(true), uCcsLoanReg.CardNbr().readonly(true),
				    uCcsLoanReg.LogicCardNbr().readonly(true), uCcsLoanReg.RefNbr().readonly(true), uCcsLoanReg
					    .LoanCode().readonly(true), uCcsLoanReg.LoanFeeMethod().readonly(true), uCcsLoanReg
					    .LoanFinalTermPrin().readonly(true), uCcsLoanReg.LoanFinalTermFee().readonly(true),
				    uCcsLoanReg.LoanFixedFee().readonly(true), uCcsLoanReg.LoanFixedPmtPrin().readonly(true),
				    uCcsLoanReg.Remark());
		detailForm.getSetting().labelWidth(160);
		return detailForm;
    }

    // 同意拒绝按钮
    private HorizontalPanel agreeOrRefuseBtn() {

	HorizontalPanel buttonPanel = new HorizontalPanel();
	{
	 // 同意
	    agreeButton =
		    CommonUiUtils.createButton(BtnName.AGREE, "skins/icons/ok.gif",
					       new IClickEventListener(){
						   @Override
						   public void onClick() {
						       btnClick("0", detailForm.getSubmitData());
						       detailForm.getUi().clear();
						   }
					       });

	    // 拒绝
	    refuseButton =
		    CommonUiUtils.createButton(BtnName.REFUSE, "skins/icons/cancel.gif",
					       new IClickEventListener(){
						   @Override
						   public void onClick() {
						       btnClick("1", detailForm.getSubmitData());
						       detailForm.getUi().clear();
						   }
					       });
	    buttonPanel.add(agreeButton);
	    buttonPanel.add(refuseButton);
	}

	return buttonPanel;
    }

    // 点同意或者拒绝按钮的操作
    private void btnClick(final String flag, final Data values) {
	// 验证表单信息
	String msg = "";
	if ("0".equals(flag)) {
	    msg = constants.agreeBtnMsg();
	} else {
	    msg = constants.refuseBtnMsg();
	}

	Dialog.confirm(msg, "提示", new ConfirmCallbackFunction(){

	    @Override
	    public void corfimCallback(boolean value) {
		if (value) {
		    RPC.ajax("rpc/t3305Server/setArgeeOrRefuse", new RpcCallback<Data>(){
			@Override
			public void onSuccess(Data result) {
			    Dialog.tipNotice("操作成功！");
			    searchGrid.loadData(searchForm);
			    searchForm.getUi().clear();
			    agreeButton.setVisible(false);
			    refuseButton.setVisible(false);
			}

		    }, flag, values);
		}
	    }
	});

    }

}
