package com.sunline.ccs.ui.client.pages.balance;
///**
// * 
// */
//package com.sunline.ccs.ui.client.txn.balance;
//
//import com.sunline.pcm.ui.common.client.ClientUtils;
//import com.sunline.pcm.ui.common.client.DispatcherPage;
//import com.sunline.ccs.ui.client.pub.UIUtil;
//import com.sunline.ccs.infrastructure.client.ui.UTmCardMediaMap;
//import com.sunline.ccs.infrastructure.client.ui.UTmPlan;
//import com.sunline.ccs.infrastructure.client.ui.i18n.TmCardMediaMapConstants;
//import com.sunline.ccs.infrastructure.shared.model.TmCardMediaMap;
//import com.sunline.ccs.param.def.enums.CPSAppAuthority;
//import com.sunline.ark.gwt.client.datasource.YakDataSource;
//import com.sunline.ark.gwt.client.datasource.YakDataSourceRecord;
//import com.sunline.ark.gwt.client.mvp.ParamMapToken;
//import com.sunline.ark.gwt.client.ui.YakDynamicForm;
//import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
//import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
//import com.google.gwt.resources.client.ImageResource;
//import com.google.gwt.user.client.rpc.AsyncCallback;
//import com.google.inject.Inject;
//import com.google.inject.Singleton;
//import com.smartgwt.client.types.Overflow;
//import com.smartgwt.client.types.RecordComponentPoolingMode;
//import com.smartgwt.client.types.SelectionStyle;
//import com.smartgwt.client.types.VisibilityMode;
//import com.smartgwt.client.widgets.form.events.SubmitValuesEvent;
//import com.smartgwt.client.widgets.form.events.SubmitValuesHandler;
//import com.smartgwt.client.widgets.form.fields.ButtonItem;
//import com.smartgwt.client.widgets.form.fields.FormItem;
//import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
//import com.smartgwt.client.widgets.form.fields.events.ClickHandler;
//import com.smartgwt.client.widgets.grid.CellFormatter;
//import com.smartgwt.client.widgets.grid.ListGrid;
//import com.smartgwt.client.widgets.grid.ListGridField;
//import com.smartgwt.client.widgets.grid.ListGridRecord;
//import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
//import com.smartgwt.client.widgets.grid.events.SelectionEvent;
//import com.smartgwt.client.widgets.layout.SectionStack;
//import com.smartgwt.client.widgets.layout.SectionStackSection;
//
///**
// * 余额成份汇总查询
// * 查询客户账户欠款的余额成份。
//* @author fanghj
// *
// */
//@Singleton
//public class AccountBalance extends DispatcherPage {
//	
//	public static final String PAGE_ID = "acct-1205";
//
//	@Inject
//	private AccountBalanceConstants constants;
//	@Inject
//	private T1205InterAsync server;
//	@Inject
//	private UIUtil uiUtil;	
//	
//	@Inject
//	private ClientUtils clientUtils;
//	
//	@Inject
//	private UTmPlan uTmPlan;
//	@Inject
//	private UTmCardMediaMap uTmCardMediaMap;
//	@Inject
//	private TmCardMediaMapConstants cardConstants;
//	
//	//private GridHeader gridHeader;
//	private ListGrid listGrid;
//	private FormItem cardItem;
//	private YakDynamicForm planForm; 
//	private FormItem selectedCardItem;
//	private YakDynamicForm cardForm;
//	private String submitCardNo;
//	private ListGridField cardNoField ;
//	
//	public AccountBalance()
//	{
//		//FIXME 需要修改为T1205权限
//		super(PAGE_ID, true, CPSAppAuthority.T1201);
//	}
//	
//	@Override
//	protected void createCanvas() {
//		final SectionStack sectionStack = new SectionStack();
//		{
//			sectionStack.setVisibilityMode(VisibilityMode.MULTIPLE);
//			sectionStack.setWidth100();
//			sectionStack.setHeight100();
//		}
//		
//		//先定义DataSource
//		YakDataSource fds = new YakDataSource() {
//
//			@Override
//			public void fetchData(FetchRequest fetchRequest,
//					AsyncCallback<FetchResponse> callback) {
//				server.getPlanList(fetchRequest,submitCardNo, callback);
//			}
//
//		};
//		
//		fds.setFields(
//				uTmPlan.PlanId().asPrimaryKey().createField(),
//				uTmPlan.AcctNo().createField(),
//				uTmPlan.AcctType().createField(),
//				uTmPlan.PlanType().createField(),
//				uTmPlan.ProductCd().createField(),
//				
//				uTmPlan.CurrBal().createField(),
//				uTmPlan.BeginBal().createField(),
//				uTmPlan.DisputeAmt().createField(),
//				
//				uTmPlan.TotDueAmt().createField(),
//				uTmPlan.PlanAddDate().createField(),
//				uTmPlan.PaidOutDate().createField(),
//				
//				uTmPlan.PastPrincipal().createField(),
//				uTmPlan.PastInterest().createField(),
//				uTmPlan.PastCardFee().createField(),
//				
//				uTmPlan.PastOvrlmtFee().createField(),
//				uTmPlan.PastLpc().createField(),
//				uTmPlan.PastNsfFee().createField(),
//				
//				uTmPlan.PastTxnFee().createField(),
//				uTmPlan.PastSvcFee().createField(),
//				uTmPlan.PastIns().createField(),
//				
//				uTmPlan.CtdPrincipal().createField(),
//				uTmPlan.CtdInterest().createField(),
//				uTmPlan.CtdCardFee().createField(),
//				
//				uTmPlan.CtdOvrlmtFee().createField(),
//				uTmPlan.CtdLpc().createField(),
//				uTmPlan.CtdNsfFee().createField(),
//				
//				uTmPlan.CtdTxnFee().createField(),
//				uTmPlan.CtdSvcFee().createField(),
//				uTmPlan.CtdIns().createField(),
//				
//				uTmPlan.CtdAmtDb().createField(),
//				uTmPlan.CtdAmtCr().createField(),
//				
//				uTmPlan.CtdNbrDb().createField(),
//				uTmPlan.CtdNbrCr().createField(),
//				
//				uTmPlan.NodefbnpIntAcru().createField(),
//				uTmPlan.BegDefbnpIntAcru().createField(),
//				uTmPlan.CtdDefbnpIntAcru().createField()
//				);
//		
//		
//		//信用计划列表查询区域
//		SectionStackSection gridSection = new SectionStackSection(constants.planListTitle());
//		{
//			gridSection.setExpanded(true);
//			
//			gridSection.addItem(createSearchForm());
//			
//			listGrid = new ListGrid();
//			{
//				listGrid.setShowRecordComponents(true);
//				listGrid.setShowRecordComponentsByCell(true);
//				listGrid.setHeight100();
//				listGrid.setWidth100();
//				listGrid.setRecordComponentPoolingMode(RecordComponentPoolingMode.DATA);
//				listGrid.setSelectionType(SelectionStyle.SINGLE);
//				
//				//显示查询的介质卡号
//				cardNoField= new ListGridField(
//						TmCardMediaMap.P_CardNo, cardConstants.cardNo());
//				cardNoField.setWidth(130);   
//				cardNoField.setCellFormatter(new CellFormatter() {   
//					public String format(Object value, ListGridRecord record,
//							int rowNum, int colNum) {
//		                return submitCardNo;   
//		            }
//		        });
//				
//				listGrid.setFields(
//						cardNoField,
////						uTmPlan.PlanId().hidden().createLGField(),
////						uTmPlan.AcctNo().width(100).createLGField(),
//						uTmPlan.AcctType().width(200).createLGField(),
////						uTmPlan.PlanType().width(100).createLGField(),
////						uTmPlan.ProductCd().width(100).createLGField(),
//						uTmPlan.CurrBal().width(100).createLGField(),
//						uTmPlan.BeginBal().width(100).createLGField(),
////						uTmPlan.DisputeAmt().width(100).createLGField(),
//						uTmPlan.TotDueAmt().width(100).createLGField(),
//						uTmPlan.PlanAddDate().width(150).createLGField()
//						);
//				listGrid.setDataSource(fds);
//				
//				listGrid.addSelectionChangedHandler(new SelectionChangedHandler() {
//
//					@Override
//					public void onSelectionChanged(SelectionEvent event) {
//						if(!event.getState()) {
//							return;
//						}
//						
//						//显示查询的介质卡号
//						selectedCardItem.setValue(UIUtil.getCardNo(cardItem));
//						
//						//显示所选择的信用计划的详细信息
//						YakDataSourceRecord record = (YakDataSourceRecord)event.getSelectedRecord();
//						planForm.setValues(record.getValueMap());
////						ListGridRecord record = event.getSelectedRecord();
//						
//						/*for(FormItem item : planForm.getFields()) {
//						    item.clearValue();
//							if(record.toMap().get(item.getName())instanceof Date){
//								item.setValue(record.getAttributeAsDate(item.getName()));
//								
//							}else{
//								item.setValue(record.getAttribute(item.getName()));
//							}
//						}*/
//						
//					}
//					
//				});
//			}
//			
//			gridSection.addItem(listGrid);
//			
//		}
//		
//		sectionStack.addSection(gridSection);
//		
//		//信用计划详细信息的显示区域
//		SectionStackSection planSection = new SectionStackSection(constants.planInfoTitle());   
//		{
//			planSection.setExpanded(true);
//			
//			cardForm = new YakDynamicForm();
//			{
//				cardForm.setNumCols(2);
//				cardForm.setWidth(250);
//				cardForm.setTitleWidth(100);
//
//				selectedCardItem = uTmCardMediaMap.CardNo().asLabel().createFormItem();
//				cardForm.setFields(selectedCardItem);
//			}
//			planSection.setControls(cardForm);
//			
//			planForm = new YakDynamicForm();
//			{
//				planForm.setWidth100();
//				planForm.setNumCols(4);
//				planForm.setColWidths(150, 200, 150, "*");
//				planForm.setHeight100();
//				planForm.setOverflow(Overflow.AUTO);
//				planForm.setTitleWidth(140);
//				
//				planForm.setFields(
//						uTmPlan.AcctNo().asLabel().createFormItem(),
//						uTmPlan.AcctType().asLabel().createFormItem(),
////						uTmPlan.PlanType().asLabel().createFormItem(),
//						uTmPlan.ProductCd().asLabel().createFormItem(),
//						
//						uTmPlan.CurrBal().asLabel().createFormItem(),
//						uTmPlan.BeginBal().asLabel().createFormItem(),
//						uTmPlan.DisputeAmt().asLabel().createFormItem(),
//						
//						uTmPlan.TotDueAmt().asLabel().createFormItem(),
//						uTmPlan.PlanAddDate().asLabel().createFormItem(),
//						uTmPlan.PaidOutDate().asLabel().createFormItem(),
//						
//						uTmPlan.PastPrincipal().asLabel().createFormItem(),
//						uTmPlan.PastInterest().asLabel().createFormItem(),
//						uTmPlan.PastCardFee().asLabel().createFormItem(),
//						
//						uTmPlan.PastOvrlmtFee().asLabel().createFormItem(),
//						uTmPlan.PastLpc().asLabel().createFormItem(),
//						uTmPlan.PastNsfFee().asLabel().createFormItem(),
//						
//						uTmPlan.PastTxnFee().asLabel().createFormItem(),
//						uTmPlan.PastSvcFee().asLabel().createFormItem(),
//						uTmPlan.PastIns().asLabel().createFormItem(),
//						
//						uTmPlan.CtdPrincipal().asLabel().createFormItem(),
//						uTmPlan.CtdInterest().asLabel().createFormItem(),
//						uTmPlan.CtdCardFee().asLabel().createFormItem(),
//						
//						uTmPlan.CtdOvrlmtFee().asLabel().createFormItem(),
//						uTmPlan.CtdLpc().asLabel().createFormItem(),
//						uTmPlan.CtdNsfFee().asLabel().createFormItem(),
//						
//						uTmPlan.CtdTxnFee().asLabel().createFormItem(),
//						uTmPlan.CtdSvcFee().asLabel().createFormItem(),
//						uTmPlan.CtdIns().asLabel().createFormItem(),
//						
//						uTmPlan.CtdAmtDb().asLabel().createFormItem(),
//						uTmPlan.CtdAmtCr().asLabel().createFormItem(),
//						
//						uTmPlan.CtdNbrDb().asLabel().createFormItem(),
//						uTmPlan.CtdNbrCr().asLabel().createFormItem(),
//						
//						uTmPlan.NodefbnpIntAcru().asLabel().createFormItem(),
//						uTmPlan.BegDefbnpIntAcru().asLabel().createFormItem(),
//						uTmPlan.CtdDefbnpIntAcru().asLabel().createFormItem()
//						
//						);
//				
//				planSection.addItem(planForm);
//			}
//		}
//		sectionStack.addSection(planSection);
//		
//		addMember(sectionStack);
//	}
//	
//	/**
//	 * 创建查询下拉列表
//	 * */
//	private YakDynamicForm createSearchForm() {
//		final YakDynamicForm searchForm = new YakDynamicForm();
//		{
//			searchForm.setWidth100();
//			searchForm.setNumCols(3);
//			searchForm.setColWidths(80, 160, "*");
//			cardItem = uiUtil.createCardNoItem();
//			final ButtonItem searchBtn = clientUtils.createSearchItem();
//			{
//				searchBtn.setStartRow(false);
//				searchBtn.addClickHandler(new ClickHandler() {
//
//					@Override
//					public void onClick(ClickEvent event) {
//						searchForm.submit();
//					}
//					
//				});
//			}
//			searchForm.setFields(cardItem, searchBtn);
//			
//			searchForm.addSubmitValuesHandler(new SubmitValuesHandler() {
//				@Override
//				public void onSubmitValues(SubmitValuesEvent event) {
//					if(searchForm.validate()) {
//
//						submitCardNo=UIUtil.getCardNo(cardItem);
//						 
//						listGrid.fetchData();
//						listGrid.invalidateCache();
//					}
//				}
//			});
//		}
//		
//		return searchForm;
//	}
//
//	@Override
//	public void updateView(ParamMapToken token) {
//		
//	}
//
//	@Override
//	public String getTitle() {
//		return constants.pageTitle();
//	}
//
//	@Override
//	public ImageResource getIcon() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	
//}
