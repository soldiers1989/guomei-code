package com.sunline.ccs.ui.client.pages.extraction;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.param.def.enums.SysTxnCd;
//import com.sunline.ccs.ui.client.pub.BtnName;
import com.sunline.ccs.ui.client.commons.CommonUiUtils;
import com.sunline.ccs.ui.client.commons.PublicConstants;
import com.sunline.ccs.ui.client.commons.UIUtilConstants;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ppy.dictionary.entity.GlTxnAdj;
import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.dialog.client.listener.ConfirmCallbackFunction;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.tab.client.Tab;
import com.sunline.ui.tab.client.TabItemSetting;
import com.sunline.ui.tab.client.TabSetting;

/**
 * 
 * 计提申请页面
 * 
 * @author fanghj
 * 
 */
@Singleton
public class Extraction extends Page {

	public static final String PAGE_ID = "risk-3308";
	@Inject
	private ExtractionConstants constants;// 计提申请页面字符串常量

	private KylinForm transForm; // 计提申请信息表单
	private KylinForm transForm1; // 解缴申请信息表单
	private TextColumnHelper adjustAmtItem;
	@Inject
	private UIUtilConstants ccsUiUtil;// 储存客户端运行时需要的额外信息，如日期和当前登录用户等
	@Inject
	private ClientUtils clientUtils;
	@Inject
	private PublicConstants publicConstants;
	// @Autowired
	// private UnifiedParameterFacility unifiedParameter;

	private KylinGrid KylinGrid;// 计提列表显示
	private KylinGrid trialGrid;// 试算列表显示
	private KylinGrid trialForRemitGrid;// 解缴试算列表显示

	private Tab tranTabSet;
	private KylinButton cancelAdjButton;
	private TextColumnHelper voidReasonItem;

	private TextColumnHelper provisionOrRemitItem;// 借贷记标志(借记-解缴 贷记-计提)
	private TextColumnHelper currencyItem;// 币种(计提)
	private TextColumnHelper currencyItem1;// 币种(解缴)
	// private FormItem theCardNetworkItem;//发卡网点
	private TextColumnHelper theCardNetworkItem;// 发卡网点
	private TextColumnHelper theCardNetworkItem1;// 发卡网点(计提)
	private TextColumnHelper theCardNetworkItem2;// 发卡网点(解缴)
	private TextColumnHelper totalItem;// 合计(计提)
	private TextColumnHelper totalItem1;// 合计(解缴)

	private String provisionOrRemit;// 借贷记标志(借记-解缴 贷记-计提)
	private String theCardNetwork;// 发卡网点
	private KylinButton provisionButton;// 计提试算按钮
	private KylinButton provisionApplyButton;// 计提申请按钮
	private KylinButton remitButton;// 解缴试算按钮
	private KylinButton remitApplyButton;// 解缴申请按钮
	private KylinForm searchForm;// 查询form

	private HorizontalPanel buttonLayout;// 按钮列表
	private Map<String, BigDecimal> trialMapForRequest;// 点击试算按钮 执行方法

	// private DbCrInd dbCrInd;
	private SysTxnCd sysTxnCd;
	private LinkedHashMap<String, String> rs;
	

	@Override
	public void refresh() {
		transForm.getUi().refresh();
		transForm1.getUi().refresh();
		KylinGrid.clearData();
		trialGrid.clearData();
		trialForRemitGrid.clearData();
	}

	public void updateView() {}

	/**
	 * 开始创建 zrx
	 * 
	 * @return
	 */

	public VerticalPanel createCanvas() {
		VerticalPanel vp = new VerticalPanel();
		vp.setWidth("100%");
		vp.setHeight("100%");
		vp.add(createLogList());
		vp.add(createTranTabSet());
		return vp;

	}

	public LinkedHashMap<String, String> getBranchList() {
		// 调用RPC显示-显示发卡网点列表
		rs = new LinkedHashMap<String, String>();
		RPC.ajax("rpc/t3308Server/getBranchList", new RpcCallback<Data>() {
			@Override
			public void onSuccess(Data result) {
				MapData data = result.asMapData();
				Map<String, String> map = (Map<String, String>) data.toMap();
				for (String key : map.keySet()) {
					rs.put(key, map.get(key));
				}

				SelectItem<String> branch = new SelectItem<String>();
				branch.setValue(rs);
				searchForm.setFieldSelectData("theCardNetworkItem", branch);
			}
		});
		return rs;
	}

	/**
	 * 创建计提申请列表 zrx
	 * 
	 * @return
	 */
	private VerticalPanel createLogList() {
		// 创建查询项
		searchForm = new KylinForm();
		{
			searchForm.setCol(5);
			searchForm.setWidth("100%");

			provisionOrRemitItem = new TextColumnHelper("provisionOrRemitItem",
					"计提/解缴/核销", 10);// 计提/解缴/核销
			theCardNetworkItem = new TextColumnHelper("theCardNetworkItem",
					constants.theCardNetwork(), 10);

			{
				provisionOrRemitItem.asSelectItem();
				theCardNetworkItem.asSelectItem();
			}
			Scheduler.get().scheduleFinally(new RepeatingCommand() {

				@Override
				public boolean execute() {

					SelectItem<String> currencyType = new SelectItem<String>();
					LinkedHashMap<String, String> value = new LinkedHashMap<String, String>();
					value.put(ExtractionConstants.PROVISION, constants.provision());
					value.put(ExtractionConstants.REMIT, constants.remit());
					value.put(ExtractionConstants.VERIFICATION,
							constants.verification());
					currencyType.setValue(value);

					searchForm.setFieldSelectData("provisionOrRemitItem",
							currencyType);
					getBranchList();
					return false;
				}
			});
			searchForm.setField(provisionOrRemitItem, theCardNetworkItem);
			searchForm.getSetting().labelWidth(100);
		}
		KylinButton searchBt = ClientUtils
			.createSearchButton(new IClickEventListener() {

				@Override
				public void onClick() {
					provisionOrRemit = searchForm
							.getFieldValue("provisionOrRemitItem");
					if (provisionOrRemit != null
							&& !"".equals(provisionOrRemit)) {
						if (provisionOrRemit
								.equals(ExtractionConstants.PROVISION)) {// 计提
							sysTxnCd = SysTxnCd.S63;
						}
						if (provisionOrRemit
								.equals(ExtractionConstants.REMIT)) {// 解缴
							sysTxnCd = SysTxnCd.S64;
						}
						if (provisionOrRemit
								.equals(ExtractionConstants.VERIFICATION)) {// 核销
							sysTxnCd = SysTxnCd.S65;
						}
					}
					theCardNetwork = searchForm
							.getFieldValue("theCardNetworkItem");
					RPC.ajax("rpc/t3308Server/getGlTxnAdjList",
							new RpcCallback<Data>() {
								@Override
								public void onSuccess(Data result) {
									KylinGrid.loadData(result);
								}
							}, sysTxnCd, theCardNetwork);
				}
			});
		// 开始文件，搜索，刷新。计提
		VerticalPanel gridLayout = new VerticalPanel();
		{
			gridLayout.add(CommonUiUtils.lineLayoutForm(searchForm, searchBt, null, null));
			// 查询列表
			KylinGrid = new KylinGrid() {};
			{
				KylinGrid.setWidth("100%");
				KylinGrid.setHeight("280px");
				TextColumnHelper adjIdItem = new TextColumnHelper(
						ExtractionConstants.ADJID, constants.adjId(), 6);// 顺序号

				TextColumnHelper provisionOrRemitItem = new TextColumnHelper(
						ExtractionConstants.PROVISIONORREMIT,
						constants.provisionOrRemit(), 6);// 借贷记标志

				TextColumnHelper currency = new TextColumnHelper(
						ExtractionConstants.CURRENCY, constants.currency(), 6);// 币种

				TextColumnHelper postAmt = new TextColumnHelper(
						ExtractionConstants.POSTAMT, constants.postAmt(), 10);// 金额

				TextColumnHelper theCardNetwork = new TextColumnHelper(
						ExtractionConstants.THECARDNETWORK,
						constants.theCardNetwork(), 6);// 发卡网点

				TextColumnHelper txnCode = new TextColumnHelper(
						ExtractionConstants.TXNCODE, constants.txnCode(), 6);// 交易码

				TextColumnHelper ageGroup = new TextColumnHelper(
						ExtractionConstants.AGEGROUP, constants.ageGroup(), 6);// 账龄组

				TextColumnHelper bucketType = new TextColumnHelper(
						ExtractionConstants.BUCKETTYPE, constants.bucketType(), 6);// 余额成分

				TextColumnHelper planNbr = new TextColumnHelper(
						ExtractionConstants.PLANNBR, constants.planNbr(), 10);// 信用计划号

				TextColumnHelper postGlInd = new TextColumnHelper(
						ExtractionConstants.POSTGLIND, constants.postGlInd(), 10);// 总账入账标志

				// 操作
				TextColumnHelper titleOperation = new TextColumnHelper(
						"titleOperation", constants.titleOperation(), 6);

				KylinGrid.setColumns(adjIdItem, provisionOrRemitItem, currency,
						postAmt, theCardNetwork, txnCode, ageGroup, bucketType,
						planNbr, postGlInd, titleOperation);

			}
			gridLayout.add(KylinGrid);
			gridLayout.setWidth("100%");
		}
		return gridLayout;
	}

	// 添加刷新按钮

	/**
	 * 点击解缴按钮时触发解缴处理
	 * 
	 * @param logKey
	 */
	private void cancelAuthTrans(Data record) {
		Dialog.confirm(constants.msgRemitConfirm(), "信息",
				new ConfirmCallbackFunction() {

					@Override
					public void corfimCallback(boolean value) {

						if (value) {
							// RPCTemplate.call(new
							// RPCExecutor<List<GlTxnAdj>>() {
							// @Override
							// public void execute(AsyncCallback<List<GlTxnAdj>>
							// callback) {
							// GlTxnAdj glTxnAdj = new GlTxnAdj();
							// glTxnAdj.setAdjId(record.getAttributeAsInt(ExtractionConstants.ADJID));
							// glTxnAdj.setDbCrInd((DbCrInd)
							// record.getAttributeAsObject(ExtractionConstants.PROVISIONORREMIT));
							// glTxnAdj.setPostCurrCd(record.getAttributeAsString(ExtractionConstants.CURRENCY));
							// glTxnAdj.setPostAmt(new
							// BigDecimal(record.getAttributeAsString(ExtractionConstants.POSTAMT)));
							// glTxnAdj.setOwningBranch(record.getAttributeAsString(ExtractionConstants.THECARDNETWORK));
							// glTxnAdj.setTxnCode(record.getAttributeAsString(ExtractionConstants.TXNCODE));
							// glTxnAdj.setAgeGroup(record.getAttributeAsString(ExtractionConstants.AGEGROUP));
							// glTxnAdj.setBucketType((BucketType)
							// record.getAttributeAsObject(ExtractionConstants.BUCKETTYPE));
							// glTxnAdj.setPlanNbr(record.getAttributeAsString(ExtractionConstants.PLANNBR));
							// glTxnAdj.setPostGlInd((PostGlIndicator)
							// record.getAttributeAsObject(ExtractionConstants.POSTGLIND));
							// server.deleteRecord(glTxnAdj, callback);
							// }
							// @Override
							// public void onSuccess(List<GlTxnAdj> result) {
							// if(result != null && result.size()>0){
							// setGlTxnAdjList(result);
							// }
							// KylinGrid.fetchData();
							// KylinGrid.invalidateCache();
							// clientUtils.showSuccess();
							// }
							//
							// }, KylinGrid);
						}

					}
				});

	}

	private void setGlTxnAdjList(Data result) {
		Extraction.this.updateCardList(result);
	}

	/**
	 * 更新计提列表数据
	 */
	private void updateCardList(Data glTxnAdjLst) {

		if (null == glTxnAdjLst || glTxnAdjLst.asListData().size() == 0) {
			KylinGrid.loadData();
			return;
		}
		MapData result = new MapData();
		result.put("rows", glTxnAdjLst);
		result.put("total", glTxnAdjLst.asListData().size());
		KylinGrid.loadData(result);
	}

	private MapData makeCardListGridRecord(GlTxnAdj glTxnAdj) {
		MapData record = new MapData();
		record.put(ExtractionConstants.ADJID, glTxnAdj.getAdjId());// 顺序号
		record.put(ExtractionConstants.PROVISIONORREMIT, glTxnAdj.getDbCrInd()
				.name());
		// 借贷记标志
		record.put(ExtractionConstants.CURRENCY, glTxnAdj.getPostCurrCd());// 币种
		record.put(ExtractionConstants.POSTAMT, glTxnAdj.getPostAmt().toString());// 金额
		record.put(ExtractionConstants.THECARDNETWORK, glTxnAdj.getOwningBranch());// 发卡网点
		record.put(ExtractionConstants.TXNCODE, glTxnAdj.getTxnCode());// 交易码
		record.put(ExtractionConstants.AGEGROUP, glTxnAdj.getAgeGroup());// 账龄组
		record.put(ExtractionConstants.BUCKETTYPE, glTxnAdj.getBucketType().name());// 余额成分
		record.put(ExtractionConstants.PLANNBR, glTxnAdj.getPlanNbr());// 信用计划号
		record.put(ExtractionConstants.POSTGLIND, glTxnAdj.getPostGlInd().name());// 总账入账标志
		return record;
	}

	/**
	 * 创建显示计提申请/解缴申请操作表单的(zrx)
	 * 
	 * @return
	 */
	private Tab createTranTabSet() {
		TabSetting tabSetting = new TabSetting().dblClickToClose(true)
				.dragToMove(true).showSwitch(true);
		tranTabSet = new Tab(tabSetting);
		// 试算申请
		final TabItemSetting tranFormTab = new TabItemSetting("tranFormTab",
				constants.tabTitleTranForm());

		tranTabSet.addItem(tranFormTab, createTranAdjForm());
		final TabItemSetting remitDetailTab = new TabItemSetting(
				"remitDetailTab", constants.tabTitleRemitForm());

		tranTabSet.addItem(remitDetailTab, createRemitDetailForm());
		tranTabSet.setHeight("400px");

		return tranTabSet;
	}

	/**
	 * 创建计提申请表单
	 * 
	 * @return
	 */

	private VerticalPanel createTranAdjForm() {
		VerticalPanel layout = new VerticalPanel();
		layout.setWidth("100%");

		// 增加试算账龄组列表 zrx
		trialGrid = new KylinGrid();
		{
			trialGrid.setWidth("100%");
			trialGrid.setHeight("200px");
			TextColumnHelper ageGroupAndDescriptionItem = new TextColumnHelper(
					ExtractionConstants.AGEGROUPANDDESCRIPTION,
					constants.ageGroupAndDescription(), 140);// 账龄组-账龄组描述

			TextColumnHelper percentageItem = new TextColumnHelper(
					ExtractionConstants.PERCENTAGE, constants.percentage(), 140);// 百分比(试算)

			TextColumnHelper countResultItem = new TextColumnHelper(
					ExtractionConstants.COUNTRESULT, constants.countResult(), 140);// 计算结果

			TextColumnHelper modifyResultItem = new TextColumnHelper(
					ExtractionConstants.MODIFYRESULT, constants.modifyResult(), 140);// 修改结果

			trialGrid.setColumns(ageGroupAndDescriptionItem, percentageItem,
					countResultItem, modifyResultItem);
		}
		layout.add(trialGrid);

		transForm = new KylinForm() {
		};
		{
			transForm.setCol(6);
			transForm.setWidth("100%");

			// 交易币种代码

			currencyItem = new TextColumnHelper("currencyItem",
					constants.currency(), 6);
			currencyItem.asSelectItem();
			Scheduler.get().scheduleFinally(new ScheduledCommand() {
				
				@Override
				public void execute() {
					transForm.setFieldSelectData("currencyItem", CommonUiUtils.currencySelectItem(ccsUiUtil));
				}
			});
			theCardNetworkItem1 = new TextColumnHelper("theCardNetworkItem1",
					constants.theCardNetwork(), 6);

			// 合计
			totalItem = new TextColumnHelper("totalItem", constants.total(), 6);

			transForm.setField(currencyItem, theCardNetworkItem1, totalItem);
		}

		layout.add(transForm);

		buttonLayout = new HorizontalPanel();
		{
			provisionButton = new KylinButton(constants.trialButton(),null);
			provisionButton.addClickEventListener(new IClickEventListener() {
					@Override
					public void onClick() {
						// 调用RPC显示-后台试算结果
						RPC.ajax("rpc/t3308Server/provisionForTrial",
								new RpcCallback<Data>() {
									@SuppressWarnings("unchecked")
									@Override
									public void onSuccess(Data data) {

										String trialAgeGroup = "";
										BigDecimal total = BigDecimal.ZERO;
										Map result = data.asMapData().toMap();
										if (result != null && result.size() > 0) {
											for (int i = 0; i < trialGrid.getData().size(); i++) {
												MapData row = trialGrid.getGrid().getRow(i);
												if (row.getString("percentage") != null) {
													if (result
															.get(row.getString(
																	"ageGroupAndDescription")
																	.toString().split("-")[0]) == null) {
														trialAgeGroup = trialAgeGroup
																+ ","
																+ row.getString(
																		"ageGroupAndDescription")
																		.toString()
																		.split("-")[0];
													} else {
														result.put(
																"countResult",
																result.get(row
																		.getString(
																				"ageGroupAndDescription")
																		.toString()
																		.split("-")[0]));

													}

												}
												// 计算合计-（计算结果+修改结果）
												if (row.getString("modifyResult") != null) {
													total = total.add(new BigDecimal(row
															.getString("modifyResult")
															.toString()));
												} else if (row.getString("modifyResult") == null
														&& row.getString("percentage") != null
														&& result
																.get(row.getString(
																		"ageGroupAndDescription")
																		.toString()
																		.split("-")[0]) != null) {
													total = total.add(new BigDecimal(row
															.getString("countResult")
															.toString()));
												}
											}
											transForm.setFieldValue(totalItem.getName(),
													total.toString());
											if (!"".equals(trialAgeGroup)) {
												Dialog.alert(constants.ageGroup()
														+ ("".equals(trialAgeGroup) ? ""
																: trialAgeGroup.substring(
																		1, trialAgeGroup
																				.length()))
														+ constants.trialMoney());
											}

										} else {
											for (int i = 0; i < trialGrid.getData().size(); i++) {
												// 计算合计-（计算结果+修改结果）
												MapData record = trialGrid.getGrid()
														.getRow(i);
												if (record.getString("modifyResult") != null) {
													total = total.add(new BigDecimal(record
															.getString("modifyResult")
															.toString()));
												} else if (record.getString("modifyResult") == null
														&& record.getString("percentage") != null
														&& record.getString("countResult") != null) {
													total = total.add(new BigDecimal(record
															.getString("countResult")
															.toString()));
												}
											}
											transForm.setFieldValue(totalItem.getName(),
													total.toString());
											Dialog.alert(constants.ageGroup()
													+ ("".equals(trialAgeGroup) ? ""
															: trialAgeGroup.substring(1,
																	trialAgeGroup.length()))
													+ constants.trialMoney());
										}

									}
								}, trialMapForRequest);
					}
			});
		}
		
		provisionApplyButton = new KylinButton("确定",null);
		provisionApplyButton.addClickEventListener(new IClickEventListener() {

					@Override
					public void onClick() {
						if (transForm.getSubmitData() != null) {
							// 验证表单信息
							Dialog.confirm(publicConstants.msgSubmitConfirm(),
									"信息", new ConfirmCallbackFunction() {

										@SuppressWarnings("unchecked")
										@Override
										public void corfimCallback(boolean value) {
											if (value) {
												Map param=new HashMap();
												param.put("txnCd", SysTxnCd.S63);
												param.put("dbcrInd", DbCrInd.C);
												param.put("currcy", transForm
														.getFieldValue(currencyItem
																.getName()));
												param.put("cardNetWorkItem", transForm
														.getFieldValue(theCardNetworkItem1
																.getName()));
												param.put("total", transForm
														.getFieldValue(totalItem
																.getName()));
												RPC.ajax(
														"rpc/t3308Server/provisionForApply",
														new RpcCallback<Data>() {
															@Override
															public void onSuccess(
																	Data data) {

																if (data != null
																		&& data.asListData()
																				.size() > 0) {
																	setGlTxnAdjList(data);
																	Dialog.alert("操作成功");
																}

															}
														},
														param);
											}
										}
									});

						}

					}
				});
		buttonLayout.add(provisionButton);
		buttonLayout.add(provisionApplyButton);
		layout.add(buttonLayout);
		return layout;
	}

	/**
	 * 创建解缴申请表单(zrx)
	 * 
	 * @return
	 */
	@SuppressWarnings("all")
	private VerticalPanel createRemitDetailForm() {
		VerticalPanel layout = new VerticalPanel();
		layout.setWidth("100%");

		// 增加试算账龄组列表 zrx
		trialForRemitGrid = new KylinGrid();
		{

			trialForRemitGrid.setWidth("100%");
			trialForRemitGrid.setHeight("280px");

			TextColumnHelper ageGroupAndDescriptionItem = new TextColumnHelper(
					ExtractionConstants.AGEGROUPANDDESCRIPTION,
					constants.ageGroupAndDescription(), 10);// 账龄组-账龄组描述

			TextColumnHelper percentageItem = new TextColumnHelper(
					ExtractionConstants.PERCENTAGE, constants.percentage(), 6);// 百分比(试算)

			TextColumnHelper countResultItem = new TextColumnHelper(
					ExtractionConstants.COUNTRESULT, constants.countResult(), 10);// 计算结果

			TextColumnHelper modifyResultItem = new TextColumnHelper(
					ExtractionConstants.MODIFYRESULT, constants.modifyResult(), 6);// 修改结果

			trialForRemitGrid.setColumns(ageGroupAndDescriptionItem,
					percentageItem, countResultItem, modifyResultItem);
		}
		layout.add(trialForRemitGrid);

		transForm1 = new KylinForm() {
			// @Override
			// public boolean validate() {
			// String totalForJJ =
			// totalItem1.getValue()==null||"".equals(totalItem1.getValue())?"0":totalItem1.getValue().toString();
			// if("0".equals(totalForJJ)){
			// clientUtils.showWarning(constants.warningForTotal());
			// return false;
			// }
			// if(!super.validate()) {
			// return false;
			// }
			// return true;
			// }
		};
		{
			transForm1.setCol(6);
			transForm1.setWidth("100%");

			// 交易币种代码
			currencyItem1 = new TextColumnHelper("currencyItem1",
					constants.currency(), 10);
			currencyItem1.asSelectItem();
			transForm1.setFieldSelectData("currencyItem1",
					CommonUiUtils.currencySelectItem(ccsUiUtil));
			// currencyItem1.setRequired(true);
			// 发卡网点
			// (能输能选择)
			// theCardNetworkItem2 = new ComboBoxItem();
			// theCardNetworkItem2.setName("theCardNetworkItem2");
			// theCardNetworkItem2.setTitle(constants.theCardNetwork());
			// theCardNetworkItem2.setType("comboBox");
			// theCardNetworkItem2.setRequired(true);
			theCardNetworkItem2 = new TextColumnHelper("theCardNetworkItem2",
					constants.theCardNetwork(), 6);
			// 合计
			totalItem1 = new TextColumnHelper("totalItem1", constants.total(),
					6);

			transForm1.setField(currencyItem1, theCardNetworkItem2, totalItem1);
		}
		layout.add(transForm1);

		buttonLayout = new HorizontalPanel();
		{
			// buttonLayout.setWidth(1200);
			// buttonLayout.setAlign(Alignment.CENTER);
			// // 解缴试算-按钮 zrx
			// remitButton = new IButton(constants.remitButton());
			// remitButton.setLayoutAlign(Alignment.CENTER);
			// remitButton
			// .addClickHandler(new
			// com.smartgwt.client.widgets.events.ClickHandler() {
			// @Override
			// public void onClick(
			// com.smartgwt.client.widgets.events.ClickEvent event) {
			// trialMapForRequest = new HashMap<String, BigDecimal>();
			// for (ListGridRecord gr : trialForRemitGrid
			// .getRecords()) {
			// if (gr.getAttribute("percentage") != null
			// && Integer.parseInt(gr
			// .getAttribute("percentage")) > 0) {
			// trialMapForRequest.put(
			// gr.getAttribute(
			// "ageGroupAndDescription")
			// .toString().split("-")[0],
			// new BigDecimal(gr.getAttribute(
			// "percentage").toString()));
			// }
			// }
			// // 调用RPC显示-后台试算结果
			// RPCTemplate.call(
			// new RPCExecutor<Map<String, BigDecimal>>() {
			// @Override
			// public void execute(
			// AsyncCallback<Map<String, BigDecimal>> callback) {
			// server.provisionForTrial(
			// trialMapForRequest,
			// callback);
			// }
			//
			// @Override
			// public void onSuccess(
			// Map<String, BigDecimal> result) {
			// String trialAgeGroup = "";
			// BigDecimal total = BigDecimal.ZERO;
			// if (result != null
			// && result.size() > 0) {
			// for (ListGridRecord record : trialForRemitGrid
			// .getRecords()) {
			// if (record
			// .getAttribute("percentage") != null) {
			// if (result
			// .get(record
			// .getAttribute(
			// "ageGroupAndDescription")
			// .toString()
			// .split("-")[0]) == null) {
			// trialAgeGroup = trialAgeGroup
			// + ","
			// + record.getAttribute(
			// "ageGroupAndDescription")
			// .toString()
			// .split("-")[0];
			// } else {
			// record.setAttribute(
			// "countResult",
			// result.get(record
			// .getAttribute(
			// "ageGroupAndDescription")
			// .toString()
			// .split("-")[0]));
			// trialGrid
			// .updateData(record);
			// }
			// // record.setAttribute("countResult",
			// //
			// result.get(record.getAttribute("ageGroupAndDescription").toString().split("-")[0]));
			// // trialForRemitGrid.updateData(record);
			// }
			// // 计算合计-（计算结果+修改结果）
			// if (record
			// .getAttribute("modifyResult") != null) {
			// total = total
			// .add(new BigDecimal(
			// record.getAttribute(
			// "modifyResult")
			// .toString()));
			// } else if (record
			// .getAttribute("modifyResult") == null
			// && record
			// .getAttribute("percentage") != null
			// && result
			// .get(record
			// .getAttribute(
			// "ageGroupAndDescription")
			// .toString()
			// .split("-")[0]) != null) {
			// total = total
			// .add(new BigDecimal(
			// record.getAttribute(
			// "countResult")
			// .toString()));
			// }
			// }
			// totalItem1.setValue(total);
			// trialForRemitGrid.fetchData();
			// if (!"".equals(trialAgeGroup)) {
			// clientUtils
			// .showWarning(constants
			// .ageGroup()
			// + ("".equals(trialAgeGroup) ? ""
			// : trialAgeGroup
			// .substring(
			// 1,
			// trialAgeGroup
			// .length()))
			// + constants
			// .trialMoney());
			// }
			// } else {
			// for (ListGridRecord record : trialForRemitGrid
			// .getRecords()) {
			// // 计算合计-（计算结果+修改结果）
			// if (record
			// .getAttribute("modifyResult") != null) {
			// total = total
			// .add(new BigDecimal(
			// record.getAttribute(
			// "modifyResult")
			// .toString()));
			// } else if (record
			// .getAttribute("modifyResult") == null
			// && record
			// .getAttribute("percentage") != null
			// && record
			// .getAttribute("countResult") != null) {
			// total = total
			// .add(new BigDecimal(
			// record.getAttribute(
			// "countResult")
			// .toString()));
			// }
			// }
			// totalItem1.setValue(total);
			// clientUtils
			// .showWarning(constants
			// .ageGroup()
			// + ("".equals(trialAgeGroup) ? ""
			// : trialAgeGroup
			// .substring(
			// 1,
			// trialAgeGroup
			// .length()))
			// + constants
			// .trialMoney());
			// }
			//
			// }
			//
			// }, trialForRemitGrid);
			// }
			//
			// });
			//
			// // 解缴申请-按钮
			// remitApplyButton = new IButton(constants.remitApply());
			// remitApplyButton.setLayoutAlign(Alignment.CENTER);
			// remitApplyButton
			// .addClickHandler(new
			// com.smartgwt.client.widgets.events.ClickHandler() {
			//
			// @Override
			// public void onClick(
			// com.smartgwt.client.widgets.events.ClickEvent event) {
			// // transForm1.submit();
			// if (transForm1.validate()) {// 验证表单信息
			// SC.confirm(publicConstants.msgSubmitConfirm(),
			// new BooleanCallback() {
			// @Override
			// public void execute(Boolean value) {
			// if (value != null && value) {
			// RPCTemplate
			// .call(new RPCExecutor<List<GlTxnAdj>>() {
			// @Override
			// public void execute(
			// AsyncCallback<List<GlTxnAdj>> callback) {
			// server.provisionForApply(
			// SysTxnCd.S64,
			// DbCrInd.D,
			// currencyItem1
			// .getValueAsString(),
			// (String) theCardNetworkItem2
			// .getValue(),
			// new BigDecimal(
			// totalItem1
			// .getValue()
			// .toString()),
			// callback);
			// }
			//
			// @Override
			// public void onSuccess(
			// List<GlTxnAdj> result) {
			// if (result != null
			// && result
			// .size() > 0) {
			// setGlTxnAdjList(result);
			// KylinGrid
			// .fetchData();
			// }
			// KylinGrid
			// .invalidateCache();
			// clientUtils
			// .showSuccess();
			// }
			// },
			// remitApplyButton,
			// tranTabSet);
			// }
			// }
			//
			// });
			// }
			// }
			//
			// });
			// buttonLayout.addMember(remitButton);
			// buttonLayout.addMember(remitApplyButton);
		}
		layout.add(buttonLayout);
		return layout;
	}

	/*
	 * 加载标题
	 */
	// @Override
	// public String getTitle() {
	// return constants.pageTitle();
	// }

	// @Override
	// public ImageResource getIcon() {
	// return null;
	// }

	@Override
	public IsWidget createPage() {
		return createCanvas();
	}
}
