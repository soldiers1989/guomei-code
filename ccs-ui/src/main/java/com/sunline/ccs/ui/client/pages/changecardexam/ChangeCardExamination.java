package com.sunline.ccs.ui.client.pages.changecardexam;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsCardExpList;
import com.sunline.ccs.infrastructure.client.ui.UCcsCustomer;
import com.sunline.ccs.ui.client.commons.CommonUiUtils;
import com.sunline.ccs.ui.client.commons.PublicConstants;
import com.sunline.ccs.ui.client.commons.UIUtil;
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
import com.sunline.ui.event.client.EventObjectHandler;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.ISelectRowEventListener;

/**
 * @see 类名：ChangeCardExamination
 * @see 描述：到期换卡审核
 *
 * @see 创建日期：   2015年6月24日上午11:14:40
 * @author linxc
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Singleton
public class ChangeCardExamination extends Page {

	@Inject
	private ChangeCardExaminationConstants constants;
	
	@Inject
	private PublicConstants publicConstants;

	@Inject
	private UIUtil uiUtil;

	@Inject
	private UCcsCardExpList uCcsCardExpList;

	@Inject
	private UCcsCustomer uCcsCustomer;

	private KylinGrid listGrid;

	private KylinForm submitForm;

	private KylinForm searchForm;

	private TextColumnHelper cardItem;
	
	@Override
	public void refresh() {
		listGrid.clearData();
		submitForm.getUi().clear();
		searchForm.getUi().clear();
	}

	@Override
	public IsWidget createPage() {
		VerticalPanel page = new VerticalPanel();
		page.setHeight("100%");
		page.setWidth("100%");
		StackPanel showListGirdStack = new StackPanel();
		showListGirdStack.setWidth("100%");
		VerticalPanel showListGirdPanel = new VerticalPanel();
		showListGirdPanel.setWidth("100%");
		showListGirdPanel.add(createSearchForm());
		showListGirdPanel.add(createListGrid());
		showListGirdStack.add(showListGirdPanel, constants.expirySearch());
		page.add(showListGirdStack);
		StackPanel showDetailStack = new StackPanel();
		showDetailStack.setWidth("100%");
		VerticalPanel showDetailPanel = new VerticalPanel();
		showDetailPanel.setWidth("100%");
		showDetailPanel.add(showDetailMsg());
		showDetailStack.add(showDetailPanel, constants.showDetailStack());
		page.add(showDetailStack);
		return page;
	}

	private KylinGrid createListGrid() {
		listGrid = new KylinGrid();
//		listGrid.setShowRecordComponents(true);
//		listGrid.setShowRecordComponentsByCell(true);
		listGrid.setWidth("100%");
		listGrid.setHeight("350px");
		listGrid.getSetting().checkbox(false);
//		listGrid.setRecordComponentPoolingMode(RecordComponentPoolingMode.DATA);
//		listGrid.setSelectionType(SelectionStyle.SINGLE);
//		listGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
		listGrid.setColumns(
				uCcsCardExpList.CardNbr().setColumnWidth(150), 
				uCcsCustomer.Name().setColumnWidth(120),
				uCcsCustomer.IdType().setColumnWidth(150), 
				uCcsCustomer.IdNo().setColumnWidth(150), 
				uCcsCardExpList.LogicCardNbr().setColumnWidth(180), 
				uCcsCardExpList.CardExpireDate());
		// 选中listGird记录时为detailForm赋值
		listGrid.getSetting().onSelectRow(new ISelectRowEventListener() {
			
			@Override
			public void selectRow(MapData rowdata, String rowid, EventObjectHandler rowobj) {
//				if(!event.getState()) {
//					return;
//				}
				submitForm.getUi().clear();
				MapData data = listGrid.getUi().getSelectedRow();
				submitForm.setFormData(data);
			}
		});
		return listGrid;
	}

	private HorizontalPanel createSearchForm() {
		searchForm = new KylinForm();
		searchForm.setCol(1);
		searchForm.setWidth("100%");
		cardItem = uiUtil.createCardNoItem();
//		ButtonItem searchBtnItem = new ButtonItem(constants.searchBtnTitle());
		searchForm.setField(cardItem);
		KylinButton searchButton = ClientUtils.createSearchButton(new IClickEventListener() {
			@Override
			public void onClick() {
				if(searchForm.valid()) {
					listGrid.getUi().setParm("cardNbr", UIUtil.getCardNo(searchForm));
					listGrid.loadDataFromUrl("rpc/changeCardExaminationServer/getExpiryCheckList");
				} else {
				    // TODO 两个弹出框
//					Dialog.alertWarn(publicConstants.msgSearchConditionInvalid(), "提示");
				}
			}
		});
		 return CommonUiUtils.lineLayoutForm(searchForm, searchButton, null, null);
	}

	/**
	 * 创建显示授信到期审核原因的详细信息form
	 * 
	 * @return
	 */
	private KylinForm showDetailMsg() {
		submitForm = new KylinForm();
		submitForm.setWidth("100%");
		submitForm.setHeight("65px");
		submitForm.setCol(1);
//		submitForm.setOverflow(Overflow.AUTO);
		EnumColumnHelper remarkItem = uCcsCardExpList.RenewRejectCd().required(true).asSelectItem(SelectType.KEY_LABLE);
		remarkItem.setFieldWidth(260);
		submitForm.getSetting().labelWidth(130);
		submitForm.setField(uCcsCardExpList.ListId().setHide(true).readonly(true), remarkItem);
		submitForm.addButton(submitButton());
		return submitForm;
	}

	// 同意拒绝按钮
	private KylinButton submitButton() {
		KylinButton submitButton = new KylinButton("确定","skins/icons/true.gif");
		submitButton.addClickEventListener(new IClickEventListener() {

			@Override
			public void onClick() {
				if(listGrid.getUi().getSelectedRow() == null) {
					Dialog.alertWarn(constants.msgNonselectRecord(), "提示");
					return;
				}
				if(submitForm.valid()) {
					RPC.ajax("rpc/changeCardExaminationServer/updateRenewRejectCd", new RpcCallback<Data>() {

						@Override
						public void onSuccess(Data result) {
							Dialog.tip("操作成功！");
							listGrid.loadDataFromUrl("rpc/changeCardExaminationServer/getExpiryCheckList");
							submitForm.getUi().clear();;
						}
						
					}, submitForm.getSubmitData().asMapData().toMap());
				}
			}
		});
		return submitButton;
	}
	
//	@Override
//	protected void createCanvas() {
//		final SectionStack sectionStack = new SectionStack();
//		{
//			sectionStack.setVisibilityMode(VisibilityMode.MULTIPLE);
//			sectionStack.setWidth100();
//			sectionStack.setHeight100();
//		}
//
//		//增加授信到期审核的查询和显示stack
//		SectionStackSection showListGirdStack = new SectionStackSection(constants.expirySearch());
//		{
//			showListGirdStack.setExpanded(true);
//			
//			//创建搜索Form
//			showListGirdStack.addItem(createSearchForm());
//			
//			//创建要显示的申请的listGrid
//			showListGirdStack.addItem(createListGrid());
//		}
//		sectionStack.addSection(showListGirdStack);
//		
//		//增加贷款申请的详细信息stack
//		SectionStackSection showDetailStack = new SectionStackSection(constants.showDetailStack());
//		{
//			showDetailStack.setExpanded(true);
//			
//			//创建申请的详细信息
//			showDetailStack.addItem(showDetailMsg());
//			
//			showDetailStack.addItem(submitButton());
//			
//		}
//		sectionStack.addSection(showDetailStack);
//		
//		addMember(sectionStack);
//		
//	}
//
//	private ListGrid createListGrid() {
//		
//		//先定义DataSource
//		YakDataSource fds = new YakDataSource() {
//			
//			@Override
//			public void fetchData(FetchRequest request, AsyncCallback<FetchResponse> callback) {
//				server.getExpiryCheckList(request, cardNo,callback);
//			}
//
//		};
//		
//		fds.setFields(
//			uTmExpiryCheckList.ListId().createField(),
//			uTmExpiryCheckList.CardNo().createField(),
//			uTmCustomer.Name().createField(),
//			uTmCustomer.IdType().createField(),
//			uTmCustomer.IdNo().createField(),
//			uTmExpiryCheckList.CardExpireDate().createField(),
//			uTmExpiryCheckList.LogicalCardNo().createField()
//		);
//		
//		listGrid = new ListGrid();
//		{
//			listGrid.setShowRecordComponents(true);
//			listGrid.setShowRecordComponentsByCell(true);
//			listGrid.setHeight100();
//			listGrid.setWidth100();
//			listGrid.setRecordComponentPoolingMode(RecordComponentPoolingMode.DATA);
//			listGrid.setSelectionType(SelectionStyle.SINGLE);
//			listGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
//			
//			listGrid.setFields(
//					uTmExpiryCheckList.CardNo().width(150).createLGField(),
//					uTmCustomer.Name().width(120).createLGField(),
//					uTmCustomer.IdType().width(150).createLGField(),
//					uTmCustomer.IdNo().width(150).createLGField(),
//					uTmExpiryCheckList.LogicalCardNo().width(180).createLGField(),
//					uTmExpiryCheckList.CardExpireDate().width("*").createLGField()
//					);
//			
//			listGrid.setDataSource(fds);
////			listGrid.setAutoFetchData(true);
//			
//			//选中listGird记录时为detailForm赋值
//			listGrid.addSelectionChangedHandler(new SelectionChangedHandler() {
//				
//				@Override
//				public void onSelectionChanged(SelectionEvent event) {
//		
//					if(!event.getState()){
//						return;
//					}
//					submitForm.clearValues();
//					YakDataSourceRecord record = (YakDataSourceRecord)event.getSelectedRecord();
//					if(record == null){
//						remarkItem.setCanEdit(false);
//					}
//					submitForm.setValues(record.getValueMap());
//				}
//			});
//		}
//		return listGrid;
//	}
//
//	private YakDynamicForm createSearchForm() {
//		searchForm = new YakDynamicForm();
//		{
//			searchForm.setNumCols(3);
//			searchForm.setWidth100();
//			searchForm.setColWidths(80, 150, "*");
//			
//			cardItem = uiUtil.createCardNoItem();
//			
//			ButtonItem searchBtnItem = new ButtonItem(constants.searchBtnTitle());
//			{
//				searchBtnItem.setStartRow(false);
//				searchBtnItem.addClickHandler(new ClickHandler() {
//
//					@Override
//					public void onClick(ClickEvent event) {
//						if(searchForm.validate()) {
//						
//							cardNo = UIUtil.getCardNo(cardItem);
//							listGrid.fetchData();
//							listGrid.invalidateCache();
//						} else {
//							clientUtils.showWarning(publicConstants.msgSearchConditionInvalid());
//						}
//					}
//					
//				});
//			}
//			
//			searchForm.setFields(cardItem,searchBtnItem);
//			
//		}
//		
//		return searchForm;
//	}
//
//	
//	/**
//	 * 创建显示授信到期审核原因的详细信息form
//	 * @return
//	 */
//	private YakDynamicForm showDetailMsg() {
//		
//		submitForm = new YakDynamicForm()
//		{
//			@Override
//			public boolean validate() {
//				if(listGrid.getSelectedRecord() == null) {
//					clientUtils.showWarning(constants.msgNonselectRecord());
//					return false;
//				}
//				
//				if(!super.validate()) {
//					return false; 
//				}
//				
//				return true;
//			}
//		};
//		submitForm.setWidth100();
//		submitForm.setHeight(90);
//		submitForm.setNumCols(2);
//		submitForm.setColWidths(130,"*");
//		submitForm.setOverflow(Overflow.AUTO);
//		submitForm.setTitleWidth(140);
//		remarkItem = uTmExpiryCheckList.RenewRejectCd().startRow(true).required().createSelectItem();
//		remarkItem.setWidth(260);
//		submitForm.setFields(
//				uTmExpiryCheckList.ListId().hidden().asLabel().createFormItem(),
//				remarkItem);
//		
//		
//		return submitForm;
//	}
//	//同意拒绝按钮
//	private HLayout submitButton(){
//		
//		buttonLayout = new HLayout(10);
//		{
//			buttonLayout.setWidth(800);
//			buttonLayout.setAlign(Alignment.CENTER);
//			
//			IButton submitButton = clientUtils.createSubmitButton();
//			{
//				submitButton.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
//					
//					@Override
//					public void onClick(com.smartgwt.client.widgets.events.ClickEvent event) {
//						if(submitForm.validate()){
//							RPCTemplate.call(new RPCExecutor<Void>() {
//								
//								@Override
//								public void execute(AsyncCallback<Void> callback) {
//									server.updateRenewRejectCd(submitForm.getValues(), callback);
//								}
//								@Override
//								public void onSuccess(Void result){
//									clientUtils.showSuccess();
//									listGrid.invalidateCache();
//									submitForm.clearValues();
//								}
//								
//							}, submitForm);
//						}
//					}
//				});
//			}
//			buttonLayout.addMember(submitButton);
//		}
//		
//		return buttonLayout;
//	}
}
