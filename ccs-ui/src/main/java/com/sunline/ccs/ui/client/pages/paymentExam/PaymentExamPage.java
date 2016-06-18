package com.sunline.ccs.ui.client.pages.paymentExam;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.kylin.web.ark.client.helper.DateColumnHelper;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.dialog.client.listener.ConfirmCallbackFunction;
import com.sunline.ui.event.client.EventObjectHandler;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.ISelectRowEventListener;

//付款审批页面
@Singleton
public class PaymentExamPage extends Page{
	private KylinForm searchForm;
	private KylinForm detailForm;
	//审批意见表单
	private KylinForm descForm;
	private KylinGrid searchGrid = new KylinGrid();
	@Inject
	private UCcsOrder uCcsOrder;
	private Set<String> orderIdSet;
	// 审批付款
	private KylinButton allowButton;
	// 审批拒绝
	private KylinButton rejectButton;
//	@Inject
//	private PaymentExamConstants constants;
	@Override
	public IsWidget createPage() {
		// 返回组件
		orderIdSet = new HashSet<String>();
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("98%");
		// 搜索
		panel.add(createSearchForm());

		// 列表
		StackPanel gridSpanel = new StackPanel();
		gridSpanel.setWidth("100%");
		gridSpanel.add(createListGrid());
		panel.add(gridSpanel);
		// 明细
		StackPanel detailSpanel = new StackPanel();
		detailSpanel.setWidth("100%");
		detailSpanel.setHeight("120px");
		ScrollPanel sPanel = new ScrollPanel();
		sPanel.add(showDetailMsg());
		sPanel.setHeight("120px");
		detailSpanel.add(sPanel);
		panel.add(detailSpanel);
		descForm=new KylinForm();
		{
			descForm.setWidth("100%");
			TextColumnHelper remarksItem=new TextColumnHelper("remarks","审批意见",200);
			descForm.setField(remarksItem.asTextArea().required(true).setFieldWidth(800));
		}
		panel.add(descForm);
		// 按钮
		panel.add(Buttons());
		return panel;

	}

	@Override
	public void refresh() {
		searchForm.getUi().clear();
		detailForm.getUi().clear();
		allowButton.setVisible(false);
		rejectButton.setVisible(false);
		searchGrid.removeAllParm();
		searchGrid.clearData();
//		searchGrid.getUi().setParm(uCcsOrder.LoanUsage().getName(),LoanUsage.B.name());
		searchGrid.loadDataFromUrl("rpc/paymentExamServer/getPaymentExamList");
	}

	/**
	 * 创建列表list显示
	 * 
	 * @return
	 */
	private KylinGrid createListGrid() {
		searchGrid = new KylinGrid();
		searchGrid.setWidth("98%");
		searchGrid.setHeight(350);
		searchGrid.setColumns(uCcsOrder.OrderId(),uCcsOrder.ContrNbr(),uCcsOrder.DueBillNo(),uCcsOrder.LoanUsage().columnRender(),uCcsOrder.CommandType().columnRender(),
				uCcsOrder.OrderStatus().columnRender(), uCcsOrder.BusinessDate(),
				uCcsOrder.TxnType().columnRender(), uCcsOrder.TxnAmt()
				);

		// 选中listGird记录时为detailForm赋值
		searchGrid.getSetting().onSelectRow(new ISelectRowEventListener() {

			@Override
			public void selectRow(MapData rowdata, String rowid,
					EventObjectHandler rowobj) {
				detailForm.getUi().clear();
				detailForm.setFormData(rowdata);
				allowButton.setVisible(false);
				rejectButton.setVisible(false);

				String orderStatus = rowdata.getString(uCcsOrder.OrderStatus()
						.getName());

				if (OrderStatus.Q.toString().equals(orderStatus)
						&& !orderIdSet.contains(rowdata.asMapData().getString(
								CcsOrder.P_OrderId))) {
						allowButton.setVisible(true);
						rejectButton.setVisible(true);
				}

			}

		});
		return searchGrid;
	}

	/**
	 * 创建搜索表单
	 * 
	 * @return
	 */
	private KylinForm createSearchForm() {
		// 搜索框
		searchForm = new KylinForm();
		{
			searchForm.setWidth("98%");
			searchForm.setCol(4);
			DateColumnHelper beginDate = new DateColumnHelper("beginDate",
					"业务起始时间", true, true);
			DateColumnHelper endDate = new DateColumnHelper("endDate",
					"业务结束时间", true, true);
			searchForm.setField(uCcsOrder.DueBillNo(),uCcsOrder.GuarantyId(),uCcsOrder.OrderId(),
					uCcsOrder.ContrNbr(),
					beginDate, endDate);

			searchForm.addButton(ClientUtils
					.createSearchButton(new IClickEventListener() {
						@Override
						public void onClick() {
							if (searchForm.valid()) {
								searchGrid.getUi().setParm(uCcsOrder.DueBillNo().getName(),searchForm.getFieldValue(uCcsOrder.DueBillNo().getName()));
								searchGrid.getUi().setParm(uCcsOrder.GuarantyId().getName(),searchForm.getFieldValue(uCcsOrder.GuarantyId().getName()));
								searchGrid.getUi().setParm(uCcsOrder.ContrNbr().getName(),searchForm.getFieldValue(uCcsOrder.ContrNbr().getName()));
								searchGrid.getUi().setParm("beginDate",searchForm.getFieldValue("beginDate"));
								searchGrid.getUi().setParm("endDate",searchForm.getFieldValue("endDate"));
								searchGrid.getUi().setParm(uCcsOrder.LoanUsage().getName(),searchForm.getFieldValue(uCcsOrder.LoanUsage().getName()));
								searchGrid.getUi().setParm(uCcsOrder.OrderId().getName(),searchForm.getFieldValue(uCcsOrder.OrderId().getName()));
								searchGrid.loadDataFromUrl("rpc/paymentExamServer/getPaymentExamList");
							}
						}
					}));

		}
		return searchForm;
	}

	private KylinForm showDetailMsg() {

		detailForm = new KylinForm();
		detailForm.setWidth("98%");
		detailForm.setHeight("150px");
		detailForm.setCol(3);
		detailForm.setField(uCcsOrder.AcctNbr().readonly(true),
				uCcsOrder.ContrNbr().readonly(true),
				uCcsOrder.AcctType().readonly(true),
				uCcsOrder.AcqId().readonly(true),
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
				uCcsOrder.LoanAmt().readonly(true),    //添加放款金额、趸交费金额
				uCcsOrder.PremiumAmt().readonly(true),
				uCcsOrder.PremiumInd().readonly(true),
                uCcsOrder.Term().readonly(true),
                uCcsOrder.CouponId().readonly(true),
                uCcsOrder.OffsetType().readonly(true)
				);
		detailForm.getSetting().labelWidth(160);
		return detailForm;
	}

	// 按钮
	private HorizontalPanel Buttons() {

		HorizontalPanel buttonPanel = new HorizontalPanel();
		{
			// 审批通过
			allowButton = new KylinButton("审批付款","skins/icons/cancel.gif");
			allowButton.addClickEventListener(new IClickEventListener() {
						@Override
						public void onClick() {
							if(descForm.valid())
							buttonClick(detailForm.getSubmitData(),"allowExam");
						}
					});
			rejectButton = new KylinButton("审批拒绝","skins/icons/cancel.gif");
			rejectButton.addClickEventListener(new IClickEventListener() {
						@Override
						public void onClick() {
							if(descForm.valid())
							buttonClick(detailForm.getSubmitData(),"rejectExam");
						}
					});
			allowButton.setVisible(false);
			rejectButton.setVisible(false);
			buttonPanel.add(allowButton);
			buttonPanel.add(rejectButton);

		}

		return buttonPanel;
	}

	// 点击付款审批/拒绝的按钮
	private void buttonClick(final Data values,final String ind) {
		String msg;
		if("allowExam".equals(ind)){
		msg = "是否同意审批付款？";
		}else{
			msg= "是否确认审批拒绝？";
		}
		final String orderId = values.asMapData().getString(CcsOrder.P_OrderId);
		final String contrNbr = values.asMapData().getString(CcsOrder.P_ContrNbr);
		final String dueBillNo = values.asMapData().getString(CcsOrder.P_DueBillNo);
		final String remarks=descForm.getFieldValue("remarks");
		final String inputSource = values.asMapData().getString(CcsOrder.P_ChannelId);
		final String acqId = values.asMapData().getString(CcsOrder.P_AcqId);
		Dialog.confirm(msg, "提示", new ConfirmCallbackFunction() {

			@Override
			public void corfimCallback(boolean value) {
				if (value) { 
					String url="rpc/paymentExamServer/";
					RPC.ajaxMask(url+ind,
							new RpcCallback<Data>() {
								@Override
								public void onSuccess(Data result) {
									Dialog.alertWarn("订单编号：" + orderId + "\n"
											+ "结算结果:" + result,"提示");
									searchGrid.loadData(searchForm);
									searchForm.getUi().clear();
									allowButton.setVisible(false);
									rejectButton.setVisible(false);
									orderIdSet.remove(orderId);
								}

								@Override
								public void onFailure(Throwable caught) {
									orderIdSet.remove(orderId);
								}
							},orderId,dueBillNo,remarks,inputSource,contrNbr,acqId);
					detailForm.getUi().clear();
					allowButton.setVisible(false);
					orderIdSet.add(orderId);
				}

			}
		});

	}
}
