package com.sunline.ccs.ui.client.pages.rpointsadj;

import java.util.LinkedHashMap;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsAcct;
import com.sunline.ccs.infrastructure.client.ui.UCcsCardLmMapping;
import com.sunline.ccs.infrastructure.client.ui.UCcsCustomer;
import com.sunline.ccs.infrastructure.client.ui.UCcsPointsAdjLog;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.CcsPointsAdjLog;
import com.sunline.ccs.ui.client.commons.CommonUiUtils;
import com.sunline.kylin.web.ark.client.helper.ColumnHelper;
import com.sunline.kylin.web.ark.client.helper.DateColumnHelper;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
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
import com.sunline.ui.grid.client.listener.IDblClickRowEventListener;

/**
 * 
 * @see 类名：RewardPointsAdjustment
 * @see 描述：积分调整申请
 *
 * @see 创建日期： Jun 23, 20158:19:09 PM
 * @author yeyu
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Singleton
public class RewardPointsAdjustment extends Page {

	@Inject
	private UCcsCardLmMapping uCcsCardLmMediaMap;

	@Inject
	private UCcsAcct uCcsAcct;

	@Inject
	private UCcsPointsAdjLog uCcsPntAdjLog;

	@Inject
	private RewardPointsAdjustmentConstants constants;

	@Inject
	private UCcsCustomer uCcsCustomer;

	@Inject
	private UCcsPointsAdjLog uCcsPointsAdjLog;

	private VerticalPanel mainWindow;

	private KylinForm queryHstForm;

	private KylinGrid grid;

	private KylinForm adjForm;

	private KylinForm detailsForm;

	@Override
	public IsWidget createPage() {
		this.getMainWindow();
		this.buildQueryForm(mainWindow);
		this.buildGrid(mainWindow);
		this.buildAdjForm(mainWindow);
		return mainWindow;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sunline.kylin.web.core.client.res.Page#refresh()
	 */
	@Override
	public void refresh() {
		queryHstForm.getUi().clear();
		grid.clearData();
		adjForm.getUi().clear();
		detailsForm.getUi().clear();
	}

	/**
	 * 
	 * @see 方法名：getMainWindow
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：Jun 23, 20159:00:46 PM
	 * @author Liming.Feng
	 * 
	 * @return
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private VerticalPanel getMainWindow() {
		mainWindow = new VerticalPanel();
		mainWindow.setWidth("98%");
		return mainWindow;
	}

	/**
	 * 
	 * @see 方法名：buildQueryForm
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：Jun 23, 20159:01:16 PM
	 * @author Liming.Feng
	 * 
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@SuppressWarnings("all")
	private void buildQueryForm(VerticalPanel mainWindow) {
		queryHstForm = new KylinForm();
		queryHstForm.setField(new ColumnHelper[] {
				new DateColumnHelper("beginDate", "开始日期", true, false),
				new DateColumnHelper("endDate", "结束日期", true, false) });
		KylinButton btnSearch = new KylinButton("查询",null);
		btnSearch.addClickEventListener(new IClickEventListener() {

					@Override
					public void onClick() {
						if (queryHstForm.valid()) {
							MapData queryHstDataMap = queryHstForm
									.getSubmitData().asMapData();

							String bdate = queryHstDataMap.toMap().containsKey(
									"beginDate") ? queryHstDataMap
									.getString("beginDate") : null;
							String edate = queryHstDataMap.toMap().containsKey(
									"endDate") ? queryHstDataMap
									.getString("endDate") : null;
							if (bdate != null) {
								grid.getUi().setParm("begDate", bdate);
							}
							if (edate != null) {
								grid.getUi().setParm("endDate", edate);
							}
							grid.loadDataFromUrl("rpc/rPtsAdjustmentServer/getCurrentPointAdjLogList");
						}

					}
				});
		HorizontalPanel hPanel = CommonUiUtils.lineLayoutForm(queryHstForm, btnSearch, null, null);
		StackPanel queryHstFormPanel = new StackPanel();
		queryHstFormPanel.setWidth("100%");
		queryHstFormPanel.add(hPanel);
		mainWindow.add(queryHstFormPanel);
	}

	private void buildGrid(VerticalPanel mainWindow) {
		grid = new KylinGrid();
		grid.setColumns(new ColumnHelper[] { uCcsPointsAdjLog.Org(),
				uCcsPointsAdjLog.OpId(), uCcsPointsAdjLog.OpTime(),
				uCcsPointsAdjLog.CardNbr(), uCcsPointsAdjLog.AdjInd(),
				uCcsPointsAdjLog.Points(), uCcsPointsAdjLog.AcctNbr(),
				uCcsPointsAdjLog.Memo() });
		grid.getSetting().delayLoad(Boolean.TRUE);
		grid.setHeight("230px");
		grid.setWidth("100%");
		grid.addDblClickListener(new IDblClickRowEventListener() {
			@Override
			public void onDblClickRow(MapData data, String rowid,
					EventObjectHandler row) {
				adjForm.setFormData(data);
			}
		});
		StackPanel gridPanel = new StackPanel();
		gridPanel.setHeight("230px");
		gridPanel.setWidth("100%");
		gridPanel.add(grid);
		mainWindow.add(gridPanel);
	}

	/**
	 * 
	 * @see 方法名：buildAdjForm
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：Jun 23, 20159:29:14 PM
	 * @author Liming.Feng
	 * 
	 * @param mainWindow
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private void buildAdjForm(VerticalPanel mainWindow) {
		VerticalPanel adjFormPanel = new VerticalPanel();
		VerticalPanel detailsPanel = new VerticalPanel();
		adjFormPanel.setWidth("100%");
		ScrollPanel scrollPanel = new ScrollPanel();
		scrollPanel.setHeight("150px");
		scrollPanel.setWidth("100%");
		adjForm = new KylinForm();
		adjForm.setHeight("100%");
		adjForm.setWidth("100%");
		adjForm.setField(new ColumnHelper[] { uCcsCardLmMediaMap.CardNbr().required(true) });
		KylinButton btnAdj = new KylinButton("查询",null);
		btnAdj.addClickEventListener(new IClickEventListener() {
			@Override
			public void onClick() {
				RPC.ajax(
						"rpc/rPtsAdjustmentServer/getTmAcountPointBal",
						new RpcCallback<Data>() {

							@Override
							public void onSuccess(Data result) {
								detailsForm.setFormData(result);
							}

						},
						new Object[] { adjForm.getSubmitData()
								.asMapData()
								.getString(CcsCardLmMapping.P_CardNbr) });
			}
		});
		HorizontalPanel hPanel = CommonUiUtils.lineLayoutForm(adjForm, btnAdj, null, null);
		
		detailsForm = new KylinForm();
		detailsForm.setField(new ColumnHelper[] {
				uCcsAcct.Name().readonly(true),
				uCcsAcct.PointsBal().readonly(true),
				new TextColumnHelper(CcsPointsAdjLog.P_Points, "调整积分", 25)
						.required(true),
				uCcsPntAdjLog.AdjInd().asSelectItem(SelectType.KEY_LABLE)
						.required(true),
				uCcsPntAdjLog.Memo().required(true),
				new TextColumnHelper(CcsAcct.P_AcctType, "hidden", 32)
						.setHide(true) });
		detailsForm.setFieldSelectData("pointInd", this.createSelectItem());
		detailsForm.addButton(ClientUtils
				.createAddButton(new IClickEventListener() {

					@Override
					public void onClick() {
						Dialog.confirm("确认要调整积分吗?", "确认",
								new ConfirmCallbackFunction() {

									@Override
									public void corfimCallback(boolean value) {
										if (value) {
											RPC.ajax(
													"rpc/rPtsAdjustmentServer/adjPts",
													new RpcCallback<Data>() {
														@Override
														public void onSuccess(
																Data result) {
															detailsForm.getUi()
																	.clear();
															Dialog.tipNotice(
																	"操作成功",
																	1000);
														}
													},
													new Object[] {
															adjForm.getSubmitData()
																	.asMapData()
																	.getString(
																			CcsCardLmMapping.P_CardNbr),
															detailsForm
																	.getSubmitData()
																	.asMapData()
																	.getString(
																			CcsPointsAdjLog.P_Points),
															detailsForm
																	.getSubmitData()
																	.asMapData()
																	.getString(
																			CcsPointsAdjLog.P_Memo),
															detailsForm
																	.getSubmitData()
																	.asMapData()
																	.getString(
																			CcsPointsAdjLog.P_AdjInd),
															detailsForm
																	.getSubmitData()
																	.asMapData()
																	.getString(
																			CcsAcct.P_AcctType) });

										}
									}
								});

					}
				}));
		detailsForm.addButton(ClientUtils
				.createRefreshButton(new IClickEventListener() {
					@Override
					public void onClick() {
						adjForm.getUi().clear();
						detailsForm.getUi().clear();
					}
				}));
		detailsPanel.add(hPanel);
		scrollPanel.add(detailsForm);
		adjFormPanel.add(detailsPanel);
		adjFormPanel.add(scrollPanel);
		mainWindow.add(adjFormPanel);
	}

	/**
	 * 
	 * @see 方法名：createSelectItem
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：Jun 23, 201510:14:17 PM
	 * @author Liming.Feng
	 * 
	 * @return
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private SelectItem<String> createSelectItem() {
		SelectItem<String> item = new SelectItem<String>();
		LinkedHashMap<String, String> valueMap = new LinkedHashMap<String, String>();
		valueMap.put(RewardPointsAdjustmentConstants.INCREASE,
				constants.pointIncrease());
		valueMap.put(RewardPointsAdjustmentConstants.DISCREASE,
				constants.pointDiscrease());
		item.setValue(valueMap);
		return item;
	}

}