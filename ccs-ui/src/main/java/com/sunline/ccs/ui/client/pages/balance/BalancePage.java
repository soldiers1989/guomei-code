/**
 * 
 */
package com.sunline.ccs.ui.client.pages.balance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import com.sunline.ccs.ui.client.commons.BtnName;
import com.sunline.ccs.ui.client.commons.CommonUiUtils;
import com.sunline.kylin.web.ark.client.helper.ButtonColumnHelper;
import com.sunline.kylin.web.ark.client.helper.ButtonColumnHelper.ButtonClickHandler;
import com.sunline.kylin.web.ark.client.helper.DateColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ui.button.client.ButtonSetting;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.dialog.client.listener.ConfirmCallbackFunction;
import com.sunline.ui.event.client.EventObjectHandler;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.ISelectRowEventListener;

/**
 * 结算异常处理
 * 
 * @author lisy
 *
 */
@Singleton
public class BalancePage extends Page {
	private KylinForm searchForm;
	private KylinForm detailForm;
	private KylinGrid searchGrid = new KylinGrid();
	@Inject
	private UCcsOrder uCcsOrder;
	private Set<String> orderIdSet;
	// 再次结算
	private KylinButton reSettleButton;
	@Inject
	private BalanceConstants constants;
	@Override
	public IsWidget createPage() {
		orderIdSet = new HashSet<String>();
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");
		panel.setHeight("100%");
		
		panel.add(createSearchForm());

		StackPanel gridSpanel = new StackPanel();
		gridSpanel.setWidth("98%");
		gridSpanel.add(createListGrid());
		panel.add(gridSpanel);

		ScrollPanel sPanel = new ScrollPanel();
		sPanel.add(showDetailMsg());
		sPanel.setWidth("98%");
		sPanel.setHeight("350px");
		panel.add(sPanel);

		// 按钮
		panel.add(Buttons());
		return panel;

	}

	@Override
	public void refresh() {
		searchForm.getUi().clear();
		detailForm.getUi().clear();
		reSettleButton.setVisible(false);
		searchGrid.removeAllParm();
		searchGrid.clearData();
		searchGrid.getUi().setParm(uCcsOrder.LoanUsage().getName(),LoanUsage.B.name());
		searchGrid.loadDataFromUrl("rpc/unusalServer/getUnusalList");
	}

	/**
	 * 创建列表list显示
	 * 
	 * @return
	 */
	private KylinGrid createListGrid() {
		searchGrid = new KylinGrid();
		searchGrid.setWidth("98%");
		searchGrid.setHeight("350px");

		// 操作按钮
		ButtonColumnHelper operationButton = new ButtonColumnHelper("button",
				"操作");
		operationButton.setSort(false);

		operationButton.asButtonItem(new ButtonClickHandler() {
			@Override
			public List<ButtonSetting> buttons(final MapData rowData) {
				String orderStatus = rowData.getString(CcsOrder.P_OrderStatus);
				final String loanUsage = rowData
						.getString(CcsOrder.P_LoanUsage);
				final Long orderId = Long.parseLong(rowData
						.getString(CcsOrder.P_OrderId));
				final String inputSource = rowData.asMapData().getString(CcsOrder.P_ChannelId);
				final String acqId = rowData.asMapData().getString(CcsOrder.P_AcqId);
				List<ButtonSetting> buttonSettings = new ArrayList<ButtonSetting>();
				// 操作按钮
				ButtonSetting button = new ButtonSetting();
				button.text(constants.statusButton());
				button.click(new IClickEventListener() {
					@Override
					public void onClick() {
						RPC.ajaxMask("rpc/unusalServer/queryPayment",
								new RpcCallback<Data>() {
									@Override
									public void onSuccess(Data result) {
										orderIdSet.remove(orderId + "");
										if (result != null) {
											detailForm.getUi().clear();
											searchGrid.loadData(searchForm);
											Dialog.alertWarn("订单编号："+orderId+"<br>"+"结算结果："+result,"提示");
										} else {
											detailForm.getUi().clear();
											searchGrid.loadData(searchForm);
											Dialog.alertWarn("操作失败，请重试","提示");
										}
									}

									@Override
									public void onFailure(Throwable caught) {
										orderIdSet.remove(orderId);
									}
								}, orderId, loanUsage,inputSource,acqId);
						orderIdSet.add(orderId + "");
					}
				});

				button.disabled(true);
				if (orderStatus.equals(OrderStatus.W.name())
						&& !orderIdSet.contains(orderId + "")) {
					button.disabled(false);
				}
				buttonSettings.add(button);
				return buttonSettings;
			}
		});

		searchGrid.setColumns(uCcsOrder.OrderId(),
				uCcsOrder.OrderStatus().columnRender(), uCcsOrder.BusinessDate(),
				uCcsOrder.TxnType().columnRender(), uCcsOrder.TxnAmt(),
				operationButton);

		// 选中listGird记录时为detailForm赋值
		searchGrid.getSetting().onSelectRow(new ISelectRowEventListener() {

			@Override
			public void selectRow(MapData rowdata, String rowid,
					EventObjectHandler rowobj) {
				detailForm.getUi().clear();
				detailForm.setFormData(rowdata);
				reSettleButton.setVisible(false);

				String orderStatus = rowdata.getString(uCcsOrder.OrderStatus()
						.getName());
				String loanUsage = rowdata.getString(uCcsOrder.LoanUsage()
						.getName());
				// 判断

				if (!orderIdSet.contains(rowdata.asMapData().getString(
								CcsOrder.P_OrderId))) {
					if (OrderStatus.E.toString().equals(orderStatus)||OrderStatus.P.toString().equals(orderStatus)){
					if (LoanUsage.B.toString().equals(loanUsage)) {
						reSettleButton.setVisible(true);
					}
					}
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
			searchForm.setCol(3);
			DateColumnHelper beginDate = new DateColumnHelper("beginDate",
					"订单起始时间", true, true);
			DateColumnHelper endDate = new DateColumnHelper("endDate",
					"订单结束时间", true, true);
			searchForm.setField(uCcsOrder.GuarantyId(),uCcsOrder.OrderId(),uCcsOrder.OrderStatus(),
					beginDate, endDate);

			searchForm.addButton(ClientUtils
					.createSearchButton(new IClickEventListener() {
						@Override
						public void onClick() {
							if (searchForm.valid()) {
								searchGrid.getUi().setParm(uCcsOrder.GuarantyId().getName(),searchForm.getFieldValue(uCcsOrder.GuarantyId().getName()));
								searchGrid.getUi().setParm(uCcsOrder.OrderStatus().getName(),searchForm.getFieldValue(uCcsOrder.OrderStatus().getName()));
								searchGrid.getUi().setParm("beginDate",searchForm.getFieldValue("beginDate"));
								searchGrid.getUi().setParm("endDate",searchForm.getFieldValue("endDate"));
								searchGrid.getUi().setParm(uCcsOrder.LoanUsage().getName(),LoanUsage.B.name());
								searchGrid.getUi().setParm(uCcsOrder.OrderId().getName(),searchForm.getFieldValue(uCcsOrder.OrderId().getName()));
								searchGrid.loadDataFromUrl("rpc/unusalServer/getUnusalList");
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

		detailForm.setField(
				uCcsOrder.AcctNbr().readonly(true),
				uCcsOrder	.AcctType().readonly(true),
				uCcsOrder.AcqId().readonly(true),
				uCcsOrder.BusinessDate().readonly(true), 
				uCcsOrder.CardNo().readonly(true), 
				uCcsOrder.CardType().readonly(true),
				uCcsOrder.CertId().readonly(true), 
				uCcsOrder.CertType().readonly(true),
				uCcsOrder.RefNbr().readonly(true),
				uCcsOrder.ChannelId().readonly(true),
				uCcsOrder.Code().readonly(true), 
				uCcsOrder.City()	.readonly(true),
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
				uCcsOrder.Message()	.readonly(true), 
				uCcsOrder.OnlineFlag().readonly(true),
				uCcsOrder.OpenBank().readonly(true), 
				uCcsOrder.OpenBankId().readonly(true),
				uCcsOrder.OptDatetime().readonly(true), 
				uCcsOrder	.OrberFailTime().readonly(true), 
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
				uCcsOrder.LoanAmt().readonly(true),   //添加放款金额、趸交费金额
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
			// 再次结算
			reSettleButton = CommonUiUtils.createButton(BtnName.RESETTLE,
					"skins/icons/cancel.gif", new IClickEventListener() {
						@Override
						public void onClick() {
							reSettleClick(detailForm.getSubmitData());
						}
					});
			reSettleButton.setVisible(false);
			buttonPanel.add(reSettleButton);
		}

		return buttonPanel;
	}

	// 点击再次结算按钮的操作
	private void reSettleClick(final Data values) {
		String msg = constants.reSettleButton();
		final String orderId = values.asMapData().getString(CcsOrder.P_OrderId);
		final String inputSource=values.asMapData().getString(CcsOrder.P_ChannelId);
		final String acqId = values.asMapData().getString(CcsOrder.P_AcqId);
		Dialog.confirm(msg, "提示", new ConfirmCallbackFunction() {

			@Override
			public void corfimCallback(boolean value) {
				if (value) {
					RPC.ajax("rpc/unusalServer/reSettle",
							new RpcCallback<Data>() {
								@Override
								public void onSuccess(Data result) {
									Dialog.alertWarn("订单编号：" + orderId + "<br>"
											+ "结算结果:" + result,"提示");
									searchGrid.loadData(searchForm);
									searchForm.getUi().clear();
									reSettleButton.setVisible(false);
									orderIdSet.remove(orderId);
								}

								@Override
								public void onFailure(Throwable caught) {
									orderIdSet.remove(orderId);
								}
							}, orderId,inputSource,acqId);
					detailForm.getUi().clear();
					reSettleButton.setVisible(false);
					orderIdSet.add(orderId);
				}

			}
		});

	}

}
