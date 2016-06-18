package com.sunline.ccs.ui.client.pages.uyereduction;
///**
// * 
// */
//package com.sunline.ccs.ui.client.txn.uyereduction;
//
//
//import java.io.Serializable;
//import java.util.LinkedHashMap;
//import java.util.Map;
//
//import com.sunline.pcm.ui.common.client.ClientUtils;
//import com.sunline.pcm.ui.common.client.DispatcherPage;
//import com.sunline.ccs.ui.client.pub.CardInterAsync;
//import com.sunline.ccs.ui.client.pub.CustInterAsync;
//import com.sunline.ccs.ui.client.pub.UIUtil;
//import com.sunline.ccs.infrastructure.client.ui.UTmCard;
//import com.sunline.ccs.infrastructure.client.ui.UTmCustomer;
//import com.sunline.ccs.infrastructure.shared.model.TmCard;
//import com.sunline.ccs.infrastructure.shared.model.TmCustomer;
//import com.sunline.ccs.param.def.enums.CPSAppAuthority;
//import com.sunline.ark.gwt.client.mvp.ParamMapToken;
//import com.sunline.ark.gwt.client.ui.YakDynamicForm;
//import com.sunline.ark.gwt.client.util.RPCExecutor;
//import com.sunline.ark.gwt.client.util.RPCTemplate;
//import com.google.gwt.resources.client.ImageResource;
//import com.google.gwt.user.client.rpc.AsyncCallback;
//import com.google.inject.Inject;
//import com.google.inject.Singleton;
//import com.smartgwt.client.types.Overflow;
//import com.smartgwt.client.util.BooleanCallback;
//import com.smartgwt.client.util.SC;
//import com.smartgwt.client.widgets.form.events.SubmitValuesEvent;
//import com.smartgwt.client.widgets.form.events.SubmitValuesHandler;
//import com.smartgwt.client.widgets.form.fields.ButtonItem;
//import com.smartgwt.client.widgets.form.fields.FormItem;
//import com.smartgwt.client.widgets.form.fields.SelectItem;
//import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
//import com.smartgwt.client.widgets.form.fields.events.ClickHandler;
//import com.smartgwt.client.widgets.layout.HLayout;
//
///**
// * 未收年费减免信息
//* @author fanghj
// *
// */
//@Singleton
//public class UncollectedYearFee extends DispatcherPage {
//	
//	public static final String PAGE_ID = "acct-1602";
//
//	@Inject
//	private UncollectedYearFeeConstants constants;
//	
//	@Inject
//	private T1602InterAsync server;
//	
//	@Inject
//	private UIUtil uiUtil;
//
//	@Inject
//	private UTmCustomer uTmCust;
//
//	@Inject
//	private CustInterAsync custServer;
//	
//	@Inject
//	private CardInterAsync cardInterAsync;
//	
//	@Inject
//	private UTmCard uTmCard;
//	
//	@Inject
//	private ClientUtils clientUtils;
//	
//	private YakDynamicForm uncollectionRedCanForm; //未收减免信息表单
//	private FormItem cardItem;
//	private SelectItem redCanReasonItem;
//	
//	private FormItem custNameItem;
//	private FormItem idTypeItem;
//	private FormItem idNoItem;
//	private ButtonItem searchBtn;
//    private FormItem  nextYearDate;
//	
//	public UncollectedYearFee() {
//		super(PAGE_ID, true, CPSAppAuthority.T1602);
//	}
//
//	@Override
//	public void updateView(ParamMapToken token) {
//		uncollectionRedCanForm.clearValues();
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
//	@Override
//	public void createCanvas() {
//		addMember(createSearchForm());
//		
//		//创建减免信息表单
//		uncollectionRedCanForm = new YakDynamicForm() {
//			@Override
//			public boolean validate() {
//				if(!cardItem.validate()) {
//					return false;
//				}
//				if(!super.validate()) {
//					return false; 
//				}
//				
//				return true;
//			}
//		};
//		{
//			uncollectionRedCanForm.setWidth(450);
//			uncollectionRedCanForm.setNumCols(2);
//			uncollectionRedCanForm.setColWidths(150, "*");
//			uncollectionRedCanForm.setLeft(10);
//			uncollectionRedCanForm.setTitleWidth(130);
//			uncollectionRedCanForm.setMargin(10);
//
//			// 持卡人姓名
//			custNameItem = uTmCust.Name().asLabel().createFormItem();
//			custNameItem.setTitle(constants.custName());
//			redCanReasonItem = new SelectItem();
//			{
//		
//			    LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(); 
//			    map.put("A", constants.aTitle());
//			    map.put("B",constants.bTitle());
//			    map.put("C",constants.cTitle());
//			    redCanReasonItem.setTitle(constants.interestRedCanRes());
//			    redCanReasonItem.setWidth(200);
//				redCanReasonItem.setRequired(true);
//				redCanReasonItem.setValueMap(map);
//				
//			
//			}
//		
//			nextYearDate=uTmCard.NextCardFeeDate().asLabel().createFormItem();
//			uncollectionRedCanForm.setFields(
//					 custNameItem,
//					 idTypeItem = uTmCust.IdType().asLabel().createFormItem(),
//					 idNoItem = uTmCust.IdNo().asLabel().createFormItem(),
//					 nextYearDate,
//					 redCanReasonItem
//					);
//		}
//		
//		addMember(uncollectionRedCanForm);
//		final HLayout buttonsLayout = uiUtil.createSubmitClearLayout(uncollectionRedCanForm, cardItem);//clientUtils.createSubmitResetLayout(uncollectionRedCanForm);
//		buttonsLayout.setWidth(450);
//		addMember(buttonsLayout);
//				
//		//减免信息表单提交时处理
//		uncollectionRedCanForm.addSubmitValuesHandler(new SubmitValuesHandler() {
//			
//			@Override
//			public void onSubmitValues(SubmitValuesEvent event) {
//				if (uncollectionRedCanForm.validate()){//验证减免信息表单
//					SC.confirm(constants.msgOKConfirm(), new BooleanCallback() {
//						@Override
//						public void execute(Boolean value) {
//							if(value != null && value) {
//								RPCTemplate.call(new RPCExecutor<Void>() {
//									@Override
//									public void execute(AsyncCallback<Void> callback) {
//										server.uncollectionRedCanInfo(
//												UIUtil.getCardNo(cardItem), 
//												redCanReasonItem.getValueAsString(), 
//												callback);
//									}
//									
//									@Override
//									public void onSuccess(Void result) {
//										clientUtils.showSuccess();
//									}
//									
//								}, buttonsLayout);
//							}
//						}
//					});
//				}
//			}
//		}); 
//		
//	}
//	
//	/**
//	 * 创建查找表单
//	 * @return
//	 */
//	private YakDynamicForm createSearchForm() {
//		final YakDynamicForm searchForm = new YakDynamicForm();
//		searchForm.setWidth(450);
//		searchForm.setHeight(80);
//		searchForm.setNumCols(3);
//		searchForm.setColWidths(150, 200, "*");
//		searchForm.setLeft(10);
//		searchForm.setTitleWidth(100);
//		searchForm.setMargin(10);
//		searchForm.setOverflow(Overflow.AUTO);
//		searchForm.setIsGroup(true);
//		
//		//卡号
//		cardItem = uiUtil.createCardNoItem();
//		cardItem.setRequired(true);
//		cardItem.setWidth(200);
//		//查找按钮
//		searchBtn = new ButtonItem();
//		searchBtn.setTitle(constants.btnTitleSearch());
//		searchBtn.setStartRow(false);
//		searchBtn.addClickHandler(new ClickHandler() {
//
//			@Override
//			public void onClick(ClickEvent event) {
//				if(searchForm.validate()) {
//					//查找客户信息
//					loadCustomerInfo(UIUtil.getCardNo(cardItem), cardItem, searchBtn);
//					loadCardInfo(UIUtil.getCardNo(cardItem), cardItem, searchBtn);
//				}
//			}
//		});
//		
//		searchForm.setFields(cardItem, searchBtn);
//
//		return searchForm;
//	}
//	
//	/**
//	 * 加载卡号对应的客户信息
//	 */
//	private void loadCustomerInfo(final String cardNo, final Object ... objs) {
//		RPCTemplate.call(new RPCExecutor<Map<String, Serializable>>() {
//
//			@Override
//			public void execute(
//					AsyncCallback<Map<String, Serializable>> callback) {
//				custServer.getCustInfoByCardNo(cardNo, callback);
//			}
//			
//			@Override
//			public void onSuccess(Map<String, Serializable> result) {
//				TmCustomer tmCustomer = new TmCustomer();
//				tmCustomer.updateFromMap(result);
//				custNameItem.setValue(tmCustomer.getName());
//				idTypeItem.setValue(tmCustomer.getIdType());
//				idNoItem.setValue(tmCustomer.getIdNo());
//			}
//			
//		}, objs);
//	}
//	
//	/**
//	 * 加载卡片信息
//	 * @param cardNo
//	 * @param objs
//	 */
//	private  void loadCardInfo(final String cardNo, final Object ... objs){
//		RPCTemplate.call(new RPCExecutor<Map<String, Serializable>>() {
//
//			@Override
//			public void execute(
//					AsyncCallback<Map<String, Serializable>> callback) {
//				//custServer.getCustInfoByCardNo(cardNo, callback);
//				cardInterAsync.getCardInfo(cardNo,callback);
//			}
//			
//			@Override
//			public void onSuccess(Map<String, Serializable> result) {
//				TmCard tmCard= new TmCard();
//				tmCard.updateFromMap(result);
//			    nextYearDate.setValue(tmCard.getNextCardFeeDate());
//			}
//			
//		}, objs);
//		
//		
//		
//	}
//
//}
