/**
 * 
 */
package com.sunline.ccs.ui.client.pages.collectpayunusual;

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
import com.sunline.ark.support.meta.EnumInfo;
import com.sunline.ccs.infrastructure.client.ui.UCcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.ui.client.commons.BtnName;
import com.sunline.ccs.ui.client.commons.CommonUiUtils;
import com.sunline.kylin.web.ark.client.helper.ButtonColumnHelper;
import com.sunline.kylin.web.ark.client.helper.ButtonColumnHelper.ButtonClickHandler;
import com.sunline.kylin.web.ark.client.helper.DateColumnHelper;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ui.button.client.ButtonSetting;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.ListData;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.dialog.client.listener.ConfirmCallbackFunction;
import com.sunline.ui.event.client.EventObjectHandler;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.ISelectRowEventListener;

/**
 * 代收代付管理
 * 
 * @author songyanchao
 *
 */
@Singleton
public class Unusal extends Page {
	private KylinForm searchForm;
	private KylinForm detailForm;
	private KylinGrid searchGrid = new KylinGrid();
	@Inject
	private UCcsOrder uCcsOrder;
	@Inject
	private UnusalConstants constants;
	private Set<String> orderIdSet;
	// 再次放款
	private KylinButton reLoanButton;

	@Override
	public IsWidget createPage() {
		// 返回组件
		orderIdSet = new HashSet<String>();
		// orderIdSet=new ArrayList<String>();
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("98%");
		// 搜索
		panel.add(createSearchForm());

		// 列表
		StackPanel gridSpanel = new StackPanel();
		gridSpanel.setWidth("100%");
		gridSpanel.add(createListGrid(), constants.unusalList());
		panel.add(gridSpanel);

		// 明细
		StackPanel detailSpanel = new StackPanel();
		detailSpanel.setWidth("100%");
		detailSpanel.setHeight("120px");
		ScrollPanel sPanel = new ScrollPanel();
		sPanel.add(showDetailMsg());
		sPanel.setHeight("120px");
		detailSpanel.add(sPanel, constants.showDetailStack());
		panel.add(detailSpanel);

		// 按钮
		panel.add(Buttons());
		return panel;

	}

	@Override
	public void refresh() {
		searchForm.setFieldSelectData("loanUsage",setFieldSelectData());
		searchForm.setFieldSelectData("certType", setFieldDate());
		searchForm.getUi().clear();
		detailForm.getUi().clear();
		reLoanButton.setVisible(false);
		searchGrid.removeAllParm();
		searchGrid.clearData();
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
		searchGrid.setHeight(350);

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
				final String inputSource = rowData
						.getString(CcsOrder.P_ChannelId);
				final String acqId = rowData.getString(CcsOrder.P_AcqId);
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
											Dialog.alertWarn("订单编号："+orderId+"<br>"+"操作结果："+result,"提示");
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
						&& !orderIdSet.contains(orderId + "") /*&& !loanUsage.equals(LoanUsage.D.name())*/) {
					button.disabled(false);
				}
				
				//已提交也可以操作
				if (orderStatus.equals(OrderStatus.C.name()) && !orderIdSet.contains(orderId + "")/*&& !loanUsage.equals(LoanUsage.D.name())*/) {
					button.disabled(false);
				}
				buttonSettings.add(button);
				return buttonSettings;
			}
		});

		searchGrid.setColumns(uCcsOrder.ContrNbr(),uCcsOrder.GuarantyId(), uCcsOrder.OrderId(),
				uCcsOrder.OrderStatus().columnRender(), uCcsOrder.BusinessDate(),
				uCcsOrder.TxnType().columnRender(), uCcsOrder.TxnAmt(),
				uCcsOrder.LoanUsage().columnRender(), operationButton);

		// 选中listGird记录时为detailForm赋值
		searchGrid.getSetting().onSelectRow(new ISelectRowEventListener() {

			@Override
			public void selectRow(final MapData rowdata, String rowid,
					EventObjectHandler rowobj) {
				detailForm.getUi().clear();
				detailForm.setFormData(rowdata);
				reLoanButton.setVisible(false);

				final String orderStatus = rowdata.getString(uCcsOrder.OrderStatus()
						.getName());
				final String loanUsage = rowdata.getString(uCcsOrder.LoanUsage()
						.getName());
				RPC.ajaxMask("rpc/unusalServer/getLoanType", new RpcCallback<Data>(){

					@Override
					public void onSuccess(Data result) {
						//获取不到贷款类型，也不允许重提
						if(result==null) return;
						//失败订单才允许重提
						if (OrderStatus.E.toString().equals(orderStatus)
								&& !orderIdSet.contains(rowdata.asMapData().getString(
										CcsOrder.P_OrderId))) {
							if (LoanUsage.L.toString().equals(loanUsage)
									|| LoanUsage.A.toString().equals(loanUsage)) {
								reLoanButton.setVisible(true);
							}
						}
						if(LoanType.MCAT.equals(LoanType.valueOf(result+""))){
							reLoanButton.setVisible(false);
						}
					}
					
				}, rowdata.getString(uCcsOrder.ContrNbr().getName()));
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
			TextColumnHelper loanUsageItem=new TextColumnHelper("loanUsage", constants.loanUsage(),1).asSelectItem();
			searchForm.setField(uCcsOrder.GuarantyId(),uCcsOrder.OrderId(),uCcsOrder.ContrNbr(),
					uCcsOrder.OrderStatus(),loanUsageItem, beginDate, endDate,
					uCcsOrder.CertType().asSelectItem(),uCcsOrder.CertId(),uCcsOrder.MobileNumber(),uCcsOrder.TxnType());//新加查询条件：证件类型，证件号，手机号，交易类型
			//自定义贷款用途下拉框
			searchForm.addButton(ClientUtils
					.createSearchButton(new IClickEventListener() {
						@Override
						public void onClick() {
							if (searchForm.valid()) {
								searchGrid.getUi().setParm(uCcsOrder.GuarantyId().getName(),searchForm.getFieldValue(uCcsOrder.GuarantyId().getName()));
								searchGrid.getUi().setParm(uCcsOrder.OrderStatus().getName(),searchForm.getFieldValue(uCcsOrder.OrderStatus().getName()));
								searchGrid.getUi().setParm("beginDate",searchForm.getFieldValue("beginDate"));
								searchGrid.getUi().setParm("endDate",searchForm.getFieldValue("endDate"));
								searchGrid.getUi().setParm(uCcsOrder.LoanUsage().getName(),searchForm.getFieldValue(uCcsOrder.LoanUsage().getName()));
								searchGrid.getUi().setParm(uCcsOrder.OrderId().getName(),searchForm.getFieldValue(uCcsOrder.OrderId().getName()));
								searchGrid.getUi().setParm(uCcsOrder.ContrNbr().getName(), searchForm.getFieldValue(uCcsOrder.ContrNbr().getName()));
								searchGrid.getUi().setParm(uCcsOrder.CertType().getName(),searchForm.getFieldValue(uCcsOrder.CertType().getName()));
								searchGrid.getUi().setParm(uCcsOrder.CertId().getName(),searchForm.getFieldValue(uCcsOrder.CertId().getName()));
								searchGrid.getUi().setParm(uCcsOrder.MobileNumber().getName(),searchForm.getFieldValue(uCcsOrder.MobileNumber().getName()));
								searchGrid.getUi().setParm(uCcsOrder.TxnType().getName(),searchForm.getFieldValue(uCcsOrder.TxnType().getName()));
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
				uCcsOrder.ChannelId().readonly(true),
				uCcsOrder.Code().readonly(true),
				uCcsOrder.City().readonly(true),
				uCcsOrder.RefNbr().readonly(true),
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
				uCcsOrder.LoanAmt().readonly(true),		//添加放款金额、趸交费金额
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
			// 再次放款
			reLoanButton = CommonUiUtils.createButton(BtnName.RELOAN,
					"skins/icons/ok.gif", new IClickEventListener() {
						@Override
						public void onClick() {
							reLoanClick(detailForm.getSubmitData());
						}
					});
			reLoanButton.setVisible(false);
			buttonPanel.add(reLoanButton);
		}

		return buttonPanel;
	}

	// 点击再次放款按钮的操作
	private void reLoanClick(final Data values) {
		String msg = constants.reLoanButton();
		final String orderId = values.asMapData().getString(CcsOrder.P_OrderId);
		final String guarantyId = values.asMapData().getString(
				CcsOrder.P_GuarantyId);
		final String inputSource = values.asMapData().getString(CcsOrder.P_ChannelId);
		final String dueBillNo = values.asMapData()
				.getString(CcsOrder.P_DueBillNo);
		final String acqId = values.asMapData().getString(CcsOrder.P_AcqId);
		Dialog.confirm(msg, "提示", new ConfirmCallbackFunction() {

			@Override
			public void corfimCallback(boolean value) {
				if (value) {
					RPC.ajaxMask("rpc/unusalServer/reLoan",
							new RpcCallback<Data>() {
								@Override
								public void onSuccess(Data result) {
									Dialog.alertWarn("订单号：" + orderId + "<br>"
											+ "处理结果：" + result,"提示");
									searchGrid.loadData(searchForm);
//									searchForm.getUi().clear();
									reLoanButton.setVisible(false);
									orderIdSet.remove(orderId);
								}

								@Override
								public void onFailure(Throwable caught) {
									orderIdSet.remove(orderId);
								}
							}, orderId, guarantyId,dueBillNo,inputSource,acqId);
					detailForm.getUi().clear();
					searchGrid.getUi().unselect(values.asMapData());
					reLoanButton.setVisible(false);
					orderIdSet.add(orderId);
				}
			}
		});
	}
	private ListData setFieldSelectData(){
		ListData uiShowData = new ListData();
			for(LoanUsage loanUsage : LoanUsage.values()) {
				if(LoanUsage.valueOf(loanUsage+"")!=LoanUsage.B&&LoanUsage.valueOf(loanUsage+"")!=LoanUsage.G){
					MapData mapdata=new MapData();
					mapdata.put("id",loanUsage+"");
					mapdata.put("text",loanUsage.getDesc());
					uiShowData.add(mapdata);
			}
		}
		return uiShowData;
	}
	
	private ListData setFieldDate(){
		ListData showData = new ListData();
		String[] idList = {"01","02","03","04","05","06"};
		String[] textList = {"身份证", "军官证","护照", "户口簿", "回乡证", "其他"};
		for (int i = 0; i < textList.length; i++) {
			MapData mData = new MapData();
			mData.put("id", idList[i]);
			mData.put("text", textList[i]);
			showData.add(mData);
		}
	
		return showData;
	}
}
