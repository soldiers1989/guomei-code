package com.sunline.ccs.ui.client.pages.posapply;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsAuthmemoO;
import com.sunline.ccs.infrastructure.client.ui.UCcsLoan;
import com.sunline.ccs.param.def.Program;
import com.sunline.ccs.ui.client.commons.UIUtil;
import com.sunline.kylin.web.ark.client.helper.BooleanColumnHelper;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ui.tab.client.Tab;
import com.sunline.ui.tab.client.TabSetting;

/**
 *特定POS分期申请
 *
* @author fanghj
 *@time 2014-7-24 上午9:31:51
 */
@Singleton
public class SpecificPOSFianaceApply extends Page{

	public static final String PAGE_ID = "tran-3500";

	@Inject
	private SpecificPOSFianaceApplyConstants constants;
//
//	@Inject
//	private T3500InterAsync server;

	@Inject
	private ClientUtils clientUtils;

	@Inject
	private UIUtil uiUtil;

	@Inject
	private UCcsAuthmemoO uCcsAuthmemoO;

	@Inject
	private UCcsLoan uCcsLoan;

	private TextColumnHelper cardItem;

	private KylinForm applyForm;

	private SelectItem merChantNoItem;

	private KylinGrid listGrid;

	private TextColumnHelper amountItem;

	private TextColumnHelper merchantMCCItem;

	private TextColumnHelper commodityNameAddrInfoItem;

	private SelectItem feesChargedItem;

	private KylinButton applyButton;

	private String cardNo;

	private String merId;

	private BigDecimal loanAmt;

	private String mcc;

	private LoanFeeMethod flag;

	private String address;

	private Tab tranTabSet;

	private KylinForm unmatchDetileForm;

	private BooleanColumnHelper queryItem;

	private LinkedHashMap<String, String> valueMap = new LinkedHashMap<String, String>();

	private LinkedHashMap<String, String> valueMap2 = new LinkedHashMap<String, String>();

	private String cardNoQuery;

	private TextColumnHelper cardNoItem;

	private int term;

	private SelectItem programIdItem;

	private String programId;

	private SelectItem loanTermItem;

	private LinkedHashMap<String, String> valueMapProId = new LinkedHashMap<String, String>();

	private Map<String, Program> map = new LinkedHashMap<String, Program>();

	private KylinForm unmatchSearchForm;
	

	@Override
	public IsWidget createPage() {
		VerticalPanel page = new VerticalPanel();
//		final SectionStack transSectionStack = new SectionStack();
//		{
//			transSectionStack.setVisibilityMode(VisibilityMode.MULTIPLE);
//			transSectionStack.setWidth100();
//			transSectionStack.setHeight100();
//			transSectionStack.setOverflow(Overflow.AUTO);
//		}
//		//	 授权未入账交易列表
//		SectionStackSection gridSection = new SectionStackSection(constants.regApplyList());
//		{
//			gridSection.setExpanded(true);
//		    applyForm=applyForm();
//			gridSection.addItem(applyForm);
//		}
//				
//		transSectionStack.addSection(gridSection);
//		
//		
//		
//		SectionStackSection detailSection = new SectionStackSection(constants.loanRegDetile());   
//		detailSection.setExpanded(true);
//		unmatchSearchForm = new YakDynamicForm();
//		{
//			unmatchSearchForm.setHeight(40);
//			unmatchSearchForm.setWidth100();
//			unmatchSearchForm.setNumCols(3);
//			unmatchSearchForm.setColWidths(100,100,"*");
//			
//		}
//		cardNoItem=uiUtil.createCardNoItem();
//		cardNoItem.setRequired(true);
//		
//		queryItem = new ButtonItem("query",constants.query());
//		queryItem.setWidth(70);
//		queryItem.setHeight(25);
//		queryItem.setStartRow(false);
//		queryItem.setAlign(Alignment.LEFT);
//		queryItem.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler() {
//			
//			@Override
//			public void onClick(ClickEvent event) {
//				if(unmatchSearchForm.validate()){
//					cardNoQuery = UIUtil.getCardNo(cardNoItem);
//					unmatchDetileForm.clearValues();
//					listGrid.invalidateCache();
//					listGrid.fetchData();
//				}
//			}
//		});
//		unmatchSearchForm.setItems(cardNoItem,queryItem);
//		detailSection.setItems(unmatchSearchForm);
//		
//		listGrid=createUnmatchList();
//		tranTabSet = createTranTabSet();
//		detailSection.addItem(tranTabSet);
//		transSectionStack.addSection(detailSection);
//		
//		addMember(transSectionStack);
		return page;
	}
	
	/* (non-Javadoc)
	     * @see com.sunline.kylin.web.core.client.res.Page#refresh()
	     */
	    @Override
	    public void refresh() {
		//super.refresh();
	    }


	/**
	 * 申请表单
	 */
	private KylinForm applyForm(){
		applyForm = new KylinForm();
//		{
//			applyForm.setNumCols(4);
//			applyForm.setWidth100();
//			
//			cardItem=uiUtil.createCardNoItem();
//			
//			merChantNoItem = new SelectItem();
//			merChantNoItem.setName("merchantNo");
//			merChantNoItem.setTitle(constants.merchantNo());
//			merChantNoItem.setRequired(true);
//			merChantNoItem.addChangedHandler(new ChangedHandler() {
//				
//				@Override
//				public void onChanged(ChangedEvent event) {
//					
//					if(event.getValue()!=null){
//						merchantMCCItem.clearValue();
//						merchantMCCItem.setDisabled(false);
//						merchantMCCItem.setValue(valueMap2.get(merChantNoItem.getValue()));
//						merchantMCCItem.setCanEdit(false);
//						
//					}else{
//						merchantMCCItem.setDisabled(true);
//                        merchantMCCItem.clearValue();
//					}
//					
//					
//				}
//			});
//			
//			amountItem = new DecimalColumnHelper("amount", constants.amount(), 15, BigDecimal.ZERO, BigDecimal.valueOf(9999999999999.99), 2).required().createFormItem();
//			
//			loanTermItem = new SelectItem();
//			loanTermItem.setName("loanTerm");
//			loanTermItem.setTitle(constants.loanTerm());
//			loanTermItem.setDisabled(true);
//			loanTermItem.setRequired(true);
//			
//			merchantMCCItem = new TextItem();
//			merchantMCCItem.setName("merchantMCC");
//			merchantMCCItem.setTitle(constants.merchantMCC());
//			merchantMCCItem.setDisabled(true);
//			merchantMCCItem.setRequired(true);
//			
//			commodityNameAddrInfoItem = new TextItem();
//			commodityNameAddrInfoItem.setName("commodityNameAddrInfo");
//			commodityNameAddrInfoItem.setTitle(constants.commodityNameAddrInfo());
//			commodityNameAddrInfoItem.setLength(20);
//			commodityNameAddrInfoItem.setWidth(300);
//			commodityNameAddrInfoItem.setRequired(true);
//			
//			feesChargedItem = uCcsLoan.LoanFeeMethod().createSelectItem();
//			feesChargedItem.setRequired(true);
//			
//			programIdItem = new SelectItem();
//			programIdItem.setName("programId");
//			programIdItem.setTitle(constants.programId());
//			programIdItem.setRequired(true);
//			programIdItem.addChangedHandler(new ChangedHandler() {
//				
//				@Override
//				public void onChanged(ChangedEvent event) {
//					if(event.getValue()!=null){
//						LinkedHashMap<Integer, Integer> valueMap=new LinkedHashMap<Integer, Integer>();
//						for(Entry<String, Program>entry:map.entrySet()){
//							if(event.getValue().equals(entry.getValue().programId)){
//								for(Entry<Integer, ProgramFeeDef>enty:entry.getValue().programFeeDef.entrySet()){
//									valueMap.put(enty.getKey(), enty.getKey());
//								}
//							}
//						}
//						loanTermItem.clearValue();
//						loanTermItem.setDisabled(false);
//						loanTermItem.setValueMap(valueMap);
//						
//					}else{
//						loanTermItem.setDisabled(true);
//                        loanTermItem.clearValue();
//					}
//				}
//			});
//			
//			applyButton = new ButtonItem("apply",constants.apply());
//			applyButton.setStartRow(true);
//			applyButton.setAttribute("align", "center");
//			applyButton.setWidth(70);
//			applyButton.setHeight(25);	
//			applyButton.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler(){
//
//				@Override
//				public void onClick(ClickEvent event) {
//					if(applyForm.validate()){
//						cardNo = UIUtil.getCardNo(cardItem);
//						merId = merChantNoItem.getValueAsString();
//						mcc = merchantMCCItem.getValueAsString();
//						loanAmt =new BigDecimal(amountItem.getValue().toString()) ;
//						flag =LoanFeeMethod.valueOf(feesChargedItem.getValueAsString());
//						address = commodityNameAddrInfoItem.getValueAsString();
//						programId = programIdItem.getValueAsString();
//						term = Integer.parseInt(loanTermItem.getValue().toString());
//						
//						RPCTemplate.call(new RPCExecutor<Void>(){
//							
//							@Override
//							public void execute(
//									AsyncCallback<Void > callback) {
//								server.regPosLoan(cardNo, merId, mcc, loanAmt, flag, address, term,programId,callback);
//							}
//							
//							@Override
//							public void onSuccess(Void result) {
//								clientUtils.showSuccess();
//							}
//							
//						});
//					}
//				}
//			});
//			applyForm.setFields(cardItem,feesChargedItem,merChantNoItem,merchantMCCItem,
//					programIdItem,loanTermItem,amountItem,commodityNameAddrInfoItem,applyButton);
//			
//			
//
//		}
		return applyForm;
	}
	/**
	 * 授权信息数据源
	 */
//	public YakDataSource createTmUnMatchListGrideDataSource(){
//		//先定义DataSource
//		YakDataSource fds = new YakDataSource() {
//			@Override
//			public void fetchData(FetchRequest fetchRequest,
//					AsyncCallback<FetchResponse> callback) {
//				server.getAuthUnTranList(fetchRequest, cardNoQuery, callback);
//			}
//		};
//		
//		return fds;
//	}
	/**
	 * 授权信息列表
	 */
	private KylinGrid createUnmatchList(){
		listGrid = new KylinGrid();
//		{

//			@Override  
//	        protected Canvas createRecordComponent(final ListGridRecord record, Integer colNum) {  
//	    		String fieldName = this.getFieldName(colNum);  
//	    		String txnStatus = record.getAttribute("b039");
//	    		listGrid.setOverflow(Overflow.AUTO);
//	    		
//	    		HLayout btnLayout = new HLayout(4);
//	    		btnLayout.setHeight(30);
//	    		btnLayout.setWidth100();
//	            if (fieldName.equals("buttonField")) {
//	                
//	            	/*查看交易详细信息按钮*/
//	            	IButton viewBtn = new IButton(constants.btnTitleViewTrans());
//	        		viewBtn.setWidth(60);
//	                viewBtn.addClickHandler(new ClickHandler() {
//						
//						@Override
//						public void onClick(com.smartgwt.client.widgets.events.ClickEvent event) {
//	                    	listGrid.selectSingleRecord(record);
//	                    	//显示所选择的交易详细信息
//	                    	fetchTransDetail(record.getAttributeAsInt(TmUnmatchO.P_LogKv));
//						}
//					});
//	                btnLayout.addMember(viewBtn);
//		        } 
//	            
//	            if(fieldName.equals("buttonField")&&(txnStatus.equals("00")||txnStatus.equals("11"))){
//	            	
//	            	//撤消交易按钮
//	            	IButton cancelBtn = new IButton(constants.cancel());
//	            	cancelBtn.setWidth(60);
//	            	cancelBtn.addClickHandler(new ClickHandler() {
//	            		
//	            		@Override
//	            		public void onClick(
//	            				com.smartgwt.client.widgets.events.ClickEvent event) {
//	            			listGrid.selectSingleRecord(record);
//	            			//撤消当前交易
//	            			cancelAuthTrans(record.getAttributeAsInt(TmUnmatchO.P_LogKv));
//	            		}  
//	            	});
//	            	
//	            	btnLayout.addMember(cancelBtn);
//	            	
//	            }
//	            return btnLayout;  
//			}
//		};
//		
//		listGrid.setShowRecordComponents(true);
//		listGrid.setShowRecordComponentsByCell(true);
//		listGrid.setHeight100();
//		listGrid.setWidth100();
//		listGrid.setCanHover(true);
//		listGrid.setShowHover(true);
//		listGrid.setHoverWidth(300);
//		listGrid.setRecordComponentPoolingMode(RecordComponentPoolingMode.DATA);
//		listGrid.setSelectionType(SelectionStyle.SINGLE);
//		listGrid.setOverflow(Overflow.AUTO);
//		
//		
//		ListGridField buttonField = new ListGridField("buttonField", constants.operate());
//		buttonField.setWidth(150);
//		ListGridField cardField=uCcsAuthmemoO.B002().width(100).createLGField();
//		cardField.setTitle(constants.cardNo());
//		
//		//交易传输日期格式转换 MMDDhhmmss -> MM-DD hh:mm:ss
//		ListGridField transmissionTimestampField = uCcsAuthmemoO.TransmissionTimestamp().width(100).createLGField();
//		{
//			transmissionTimestampField.setCellFormatter(new TransmissionTimestampCellFormatter());
//		}
//		
//		listGrid.setFields(
//				cardField,
//				uCcsAuthmemoO.AcctType().width(130).createLGField(),
//				transmissionTimestampField,
//				uCcsAuthmemoO.TxnCurrCd().width(90).createLGField(),
//				uCcsAuthmemoO.TxnAmt().width(90).createLGField(),
//				uCcsAuthmemoO.AuthCode().width(90).createLGField(),
//				uCcsAuthmemoO.TxnStatus().width(90).createLGField(),
//				uCcsAuthmemoO.TxnType().width(120).createLGField(),
//				uCcsAuthmemoO.Channel().width(90).createLGField(),
//				uCcsAuthmemoO.AcqNameAddr().width(120).createLGField(),
//				uCcsAuthmemoO.TxnDirection().width(90).createLGField(),
//				buttonField
//				);
//		
//		listGrid.setDataSource(this.createTmUnMatchListGrideDataSource());
		
		return listGrid;
	}
	
	/**
	 * 授权详细信息
	 */
	public VerticalPanel createUnmatchDetileForm(){

		VerticalPanel layout = new VerticalPanel();
//		layout.setWidth100();
//		layout.setHeight100();
//	   
//		//授权表单详细信息
//		unmatchDetileForm = new YakDynamicForm();
//		{
//			unmatchDetileForm.setNumCols(4);
//			unmatchDetileForm.setWidth100();
//			unmatchDetileForm.setHeight100();
//			unmatchDetileForm.setTitleWidth(130);
//			unmatchDetileForm.setOverflow(Overflow.AUTO);
//
//			
//			unmatchDetileForm.setFields(uCcsAuthmemoO.AcctType().asLabel().createFormItem(),
//					uCcsAuthmemoO.AcqRefNo().asLabel().createFormItem(),
//					uCcsAuthmemoO.TxnAmt().asLabel().createFormItem(),
//					uCcsAuthmemoO.TxnCurrCd().asLabel().createFormItem(),
//					uCcsAuthmemoO.AuthCode().asLabel().createFormItem(),
//					uCcsAuthmemoO.AcqNameAddr().asLabel().createFormItem(),
//					uCcsAuthmemoO.ChbTxnAmt().asLabel().createFormItem(),
//					uCcsAuthmemoO.ChbCurrCd().asLabel().createFormItem(),
//
//					uCcsAuthmemoO.Mcc().asLabel().createFormItem(),
//					uCcsAuthmemoO.AcqBranchId().asLabel().createFormItem(),
//					uCcsAuthmemoO.FwdInstId().asLabel().createFormItem(),
//					uCcsAuthmemoO.TransmissionTimestamp().asLabel().createFormItem(),
//					uCcsAuthmemoO.SettleDate().asLabel().createFormItem(),
//
//					uCcsAuthmemoO.TxnDirection().asLabel().createFormItem(),
//					uCcsAuthmemoO.TxnStatus().asLabel().createFormItem(),
//					uCcsAuthmemoO.TxnType().asLabel().createFormItem(),
//					uCcsAuthmemoO.Channel().asLabel().createFormItem(),
//					uCcsAuthmemoO.TxnTerminal().asLabel().createFormItem(),
//					uCcsAuthmemoO.LogOlTime().asLabel().createFormItem(),
//					uCcsAuthmemoO.LogBizDate().asLabel().createFormItem(),
//					
//					uCcsAuthmemoO.Mti().asLabel().createFormItem(),
//					uCcsAuthmemoO.OrigTxnType().asLabel().createFormItem(),
//					uCcsAuthmemoO.OrigFwdInstId().asLabel().createFormItem(),
//					uCcsAuthmemoO.OrigAcqInstId().asLabel().createFormItem(),
//					uCcsAuthmemoO.OrigMti().asLabel().createFormItem(),
//					uCcsAuthmemoO.OrigTransDate().asLabel().createFormItem(),
//					uCcsAuthmemoO.OrigTraceNo().asLabel().createFormItem(),
//					uCcsAuthmemoO.OrigTxnProc().asLabel().createFormItem(),
//					uCcsAuthmemoO.OrigTxnAmt().asLabel().createFormItem(),
//					uCcsAuthmemoO.OrigLogKv().asLabel().createFormItem(),
//					uCcsAuthmemoO.OrigTxnVal1().asLabel().createFormItem(),
//					uCcsAuthmemoO.OrigTxnVal2().asLabel().createFormItem(),
//					uCcsAuthmemoO.OrigChbTxnAmt().asLabel().createFormItem(),
//					
//					uCcsAuthmemoO.LastReversalDate().asLabel().createFormItem(),
//					uCcsAuthmemoO.VoidCount().asLabel().createFormItem(),
//					uCcsAuthmemoO.ManualAuthFlag().asLabel().createFormItem(),
//					uCcsAuthmemoO.OperaId().asLabel().createFormItem(),
//					uCcsAuthmemoO.Brand().asLabel().createFormItem(),
//					
//					uCcsAuthmemoO.ProductCd().asLabel().createFormItem(),
//					uCcsAuthmemoO.MccType().asLabel().createFormItem(),
//					uCcsAuthmemoO.FinalReason().asLabel().createFormItem(),
//					uCcsAuthmemoO.FinalAction().asLabel().createFormItem(),
//					uCcsAuthmemoO.CompAmt().asLabel().createFormItem(),
//
//					uCcsAuthmemoO.FinalUpdDirection().asLabel().createFormItem(),
//					uCcsAuthmemoO.FinalUpdAmt().asLabel().createFormItem(),
//					uCcsAuthmemoO.IcInd().asLabel().createFormItem(),
//					uCcsAuthmemoO.The3dsecureType().asLabel().createFormItem(),
//					uCcsAuthmemoO.VipStatus().asLabel().createFormItem(),
//					uCcsAuthmemoO.CurrBal().asLabel().createFormItem(),
//					uCcsAuthmemoO.CashAmt().asLabel().createFormItem(),
//					uCcsAuthmemoO.Otb().asLabel().createFormItem(),
//					
//					
//					uCcsAuthmemoO.CashOtb().asLabel().createFormItem(),
//					uCcsAuthmemoO.CustOtb().asLabel().createFormItem(),
//					uCcsAuthmemoO.CardBlackFlag().asLabel().createFormItem(),
//					uCcsAuthmemoO.MerchantBlackFlag().asLabel().createFormItem(),
//					uCcsAuthmemoO.ExpireDate().asLabel().createFormItem(),
//					uCcsAuthmemoO.TrackOneResult().asLabel().createFormItem(),
//					uCcsAuthmemoO.TrackTwoResult().asLabel().createFormItem(),
//					uCcsAuthmemoO.TrackThreeResult().asLabel().createFormItem(),
//					uCcsAuthmemoO.PwdType().asLabel().createFormItem(),
//					uCcsAuthmemoO.CheckPwdResult().asLabel().createFormItem(),
//					uCcsAuthmemoO.PayPwdErrNum().asLabel().createFormItem(),
//					
//					
//					uCcsAuthmemoO.CheckCvvResult().asLabel().createFormItem(),
//					uCcsAuthmemoO.CheckCvv2Result().asLabel().createFormItem(),
//					uCcsAuthmemoO.CheckIcvnResult().asLabel().createFormItem(),
//					uCcsAuthmemoO.CheckArqcResult().asLabel().createFormItem(),
//					uCcsAuthmemoO.CheckAtcResult().asLabel().createFormItem(),
//					uCcsAuthmemoO.CheckCvrResult().asLabel().createFormItem(),
//					uCcsAuthmemoO.CheckTvrResult().asLabel().createFormItem(),
//					uCcsAuthmemoO.RejReason().asLabel().createFormItem(),
//					
//					uCcsAuthmemoO.UnmatchCr().asLabel().createFormItem(),
//					uCcsAuthmemoO.UnmatchDb().asLabel().createFormItem(),
//					uCcsAuthmemoO.B002().asLabel().createFormItem(),
//					uCcsAuthmemoO.B003().asLabel().createFormItem(),
//					uCcsAuthmemoO.B007().asLabel().createFormItem(),
//					uCcsAuthmemoO.B011().asLabel().createFormItem(),
//					uCcsAuthmemoO.B022().asLabel().createFormItem(),
//					uCcsAuthmemoO.B025().asLabel().createFormItem(),
//					uCcsAuthmemoO.B032().asLabel().createFormItem(),
//					uCcsAuthmemoO.B033().asLabel().createFormItem(),
//					uCcsAuthmemoO.B039().asLabel().createFormItem(),
//					uCcsAuthmemoO.B042().asLabel().createFormItem(),
//					uCcsAuthmemoO.B060().startRow(true).colSpan(unmatchDetileForm.getNumCols()-1).asLabel().createFormItem(),
//					uCcsAuthmemoO.B061().startRow(true).colSpan(unmatchDetileForm.getNumCols()-1).asLabel().createFormItem(),
//					uCcsAuthmemoO.B090().asLabel().createFormItem()
//										);
//
//		}
//
//		layout.addMember(unmatchDetileForm);

		return layout;
	}
	/**
	 * 分期列表
	 */
//	private ListGrid createloanRegList(){
//		return null;
//	}
	
	/**
	 * 创建unmatch和分期信息的tabset
	 * 
	 * @return
	 */
	private Tab createTranTabSet() {
		TabSetting tabSetting = new TabSetting().dblClickToClose(true).dragToMove(true).showSwitch(true);
		tranTabSet = new Tab(tabSetting);
//		tranTabSet.setHeight100();
//		tranTabSet.setWidth100();
//		final Tab unmatchTab = new Tab(constants.unmatchList());
//		VLayout unmatchLayout = new VLayout();
//		unmatchLayout.setHeight100();
//		unmatchLayout.setWidth100();
//		unmatchLayout.addMember(createUnmatchList());
//		unmatchLayout.addMember(createUnmatchDetileForm());
//		{
//			unmatchTab.setPane(unmatchLayout);
//		}
//		tranTabSet.addTab(unmatchTab);	
		return tranTabSet;
	}
	
	/**
	 * 获取并显示所选择的交易详细信息
	 * @param logKey
	 */
	private void fetchTransDetail(final int logKey) {
//		RPCTemplate.call(new RPCExecutor<TmUnmatchO>() {
//			@Override
//			public void execute(AsyncCallback<TmUnmatchO> callback) {
//				unmatchDetileForm.clearValues();
//				server.getTmUnmatchO(logKey, callback);
//			}
//			
//			@Override
//			public void onSuccess(TmUnmatchO result) {
//				unmatchDetileForm.setValues(result.convertToMap());
//			}
//			
//		}, listGrid);
	}
	
	/**
	 * 点击撤消按钮时触发撤消授权交易的处理逻辑
	 * @param logKey
	 */
	private void cancelAuthTrans(final int logKey) {
//		SC.confirm(constants.msgConfirmCancel(), new BooleanCallback() {
//			
//			@Override
//			public void execute(Boolean chooseYes) {
//				if(null != chooseYes && chooseYes) {
//					RPCTemplate.call(new RPCExecutor<Void>() {
//						
//						@Override
//						public void execute(AsyncCallback<Void> callback) {
//							// 撤消授权交易
//							server.cancelLoanTrans(logKey, callback);
//						}
//						
//						@Override
//						public void onSuccess(Void result) {
//							clientUtils.showSuccess();
//						}
//							
//					}, listGrid);
//				}
//			}
//		});
	}
	
//	@Override
//	public void updateView(ParamMapToken token) {
//		//商户号、商户MCC取值方法
//		RPCTemplate.call(new RPCExecutor<Map<String, LoanMerchant>>() {
//			
//			@Override
//			public void execute(
//					AsyncCallback<Map<String, LoanMerchant>> callback) {
//				server.getLoanMerchant(callback);
//			}
//
//			@Override
//			public void onSuccess(Map<String, LoanMerchant> result) {
//				for( Map.Entry<String, LoanMerchant> entry:result.entrySet()){
//					valueMap.put(entry.getValue().merId, entry.getValue().merId+"-"+entry.getValue().merName);
//					valueMap2.put(entry.getValue().merId,entry.getValue().merType);
//				}
//				merChantNoItem.setValueMap(valueMap);
//			}
//		});
//		
//		//分期活动编号、分期期数取值方法
//		RPCTemplate.call(new RPCExecutor<Map<String,Program>>() {
//
//			@Override
//			public void execute(AsyncCallback<Map<String, Program>> callback) {
//				server.getProgram(callback);
//			}
//
//			@Override
//			public void onSuccess(Map<String, Program> result) {
//				for(Map.Entry<String, Program> entry:result.entrySet()){
//					valueMapProId.put(entry.getValue().programId, entry.getValue().programId);
//				}
//				programIdItem.setValueMap(valueMapProId);
//				map.putAll(result);
//			}
//		});
//	}
	
//	@Override
//	protected void createCanvas() {
//		final SectionStack transSectionStack = new SectionStack();
//		{
//			transSectionStack.setVisibilityMode(VisibilityMode.MULTIPLE);
//			transSectionStack.setWidth100();
//			transSectionStack.setHeight100();
//			transSectionStack.setOverflow(Overflow.AUTO);
//		}
//		//	 授权未入账交易列表
//		SectionStackSection gridSection = new SectionStackSection(constants.regApplyList());
//		{
//			gridSection.setExpanded(true);
//		    applyForm=applyForm();
//			gridSection.addItem(applyForm);
//		}
//				
//		transSectionStack.addSection(gridSection);
//		
//		
//		
//		SectionStackSection detailSection = new SectionStackSection(constants.loanRegDetile());   
//		detailSection.setExpanded(true);
//		unmatchSearchForm = new YakDynamicForm();
//		{
//			unmatchSearchForm.setHeight(40);
//			unmatchSearchForm.setWidth100();
//			unmatchSearchForm.setNumCols(3);
//			unmatchSearchForm.setColWidths(100,100,"*");
//			
//		}
//		cardNoItem=uiUtil.createCardNoItem();
//		cardNoItem.setRequired(true);
//		
//		queryItem = new ButtonItem("query",constants.query());
//		queryItem.setWidth(70);
//		queryItem.setHeight(25);
//		queryItem.setStartRow(false);
//		queryItem.setAlign(Alignment.LEFT);
//		queryItem.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler() {
//			
//			@Override
//			public void onClick(ClickEvent event) {
//				if(unmatchSearchForm.validate()){
//					cardNoQuery = UIUtil.getCardNo(cardNoItem);
//					unmatchDetileForm.clearValues();
//					listGrid.invalidateCache();
//					listGrid.fetchData();
//				}
//			}
//		});
//		unmatchSearchForm.setItems(cardNoItem,queryItem);
//		detailSection.setItems(unmatchSearchForm);
//		
//		listGrid=createUnmatchList();
//		tranTabSet = createTranTabSet();
//		detailSection.addItem(tranTabSet);
//		transSectionStack.addSection(detailSection);
//		
//		addMember(transSectionStack);
//	}
//	
//	/**
//	 * 申请表单
//	 */
//	private YakDynamicForm applyForm(){
//		applyForm = new YakDynamicForm();
//		{
//			applyForm.setNumCols(4);
//			applyForm.setWidth100();
//			
//			cardItem=uiUtil.createCardNoItem();
//			
//			merChantNoItem = new SelectItem();
//			merChantNoItem.setName("merchantNo");
//			merChantNoItem.setTitle(constants.merchantNo());
//			merChantNoItem.setRequired(true);
//			merChantNoItem.addChangedHandler(new ChangedHandler() {
//				
//				@Override
//				public void onChanged(ChangedEvent event) {
//					
//					if(event.getValue()!=null){
//						merchantMCCItem.clearValue();
//						merchantMCCItem.setDisabled(false);
//						merchantMCCItem.setValue(valueMap2.get(merChantNoItem.getValue()));
//						merchantMCCItem.setCanEdit(false);
//						
//					}else{
//						merchantMCCItem.setDisabled(true);
//                        merchantMCCItem.clearValue();
//					}
//					
//					
//				}
//			});
//			
//			amountItem = new DecimalColumnHelper("amount", constants.amount(), 15, BigDecimal.ZERO, BigDecimal.valueOf(9999999999999.99), 2).required().createFormItem();
//			
//			loanTermItem = new SelectItem();
//			loanTermItem.setName("loanTerm");
//			loanTermItem.setTitle(constants.loanTerm());
//			loanTermItem.setDisabled(true);
//			loanTermItem.setRequired(true);
//			
//			merchantMCCItem = new TextItem();
//			merchantMCCItem.setName("merchantMCC");
//			merchantMCCItem.setTitle(constants.merchantMCC());
//			merchantMCCItem.setDisabled(true);
//			merchantMCCItem.setRequired(true);
//			
//			commodityNameAddrInfoItem = new TextItem();
//			commodityNameAddrInfoItem.setName("commodityNameAddrInfo");
//			commodityNameAddrInfoItem.setTitle(constants.commodityNameAddrInfo());
//			commodityNameAddrInfoItem.setLength(20);
//			commodityNameAddrInfoItem.setWidth(300);
//			commodityNameAddrInfoItem.setRequired(true);
//			
//			feesChargedItem = uTmLoan.LoanFeeMethod().createSelectItem();
//			feesChargedItem.setRequired(true);
//			
//			programIdItem = new SelectItem();
//			programIdItem.setName("programId");
//			programIdItem.setTitle(constants.programId());
//			programIdItem.setRequired(true);
//			programIdItem.addChangedHandler(new ChangedHandler() {
//				
//				@Override
//				public void onChanged(ChangedEvent event) {
//					if(event.getValue()!=null){
//						LinkedHashMap<Integer, Integer> valueMap=new LinkedHashMap<Integer, Integer>();
//						for(Entry<String, Program>entry:map.entrySet()){
//							if(event.getValue().equals(entry.getValue().programId)){
//								for(Entry<Integer, ProgramFeeDef>enty:entry.getValue().programFeeDef.entrySet()){
//									valueMap.put(enty.getKey(), enty.getKey());
//								}
//							}
//						}
//						loanTermItem.clearValue();
//						loanTermItem.setDisabled(false);
//						loanTermItem.setValueMap(valueMap);
//						
//					}else{
//						loanTermItem.setDisabled(true);
//                        loanTermItem.clearValue();
//					}
//				}
//			});
//			
//			applyButton = new ButtonItem("apply",constants.apply());
//			applyButton.setStartRow(true);
//			applyButton.setAttribute("align", "center");
//			applyButton.setWidth(70);
//			applyButton.setHeight(25);	
//			applyButton.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler(){
//
//				@Override
//				public void onClick(ClickEvent event) {
//					if(applyForm.validate()){
//						cardNo = UIUtil.getCardNo(cardItem);
//						merId = merChantNoItem.getValueAsString();
//						mcc = merchantMCCItem.getValueAsString();
//						loanAmt =new BigDecimal(amountItem.getValue().toString()) ;
//						flag =LoanFeeMethod.valueOf(feesChargedItem.getValueAsString());
//						address = commodityNameAddrInfoItem.getValueAsString();
//						programId = programIdItem.getValueAsString();
//						term = Integer.parseInt(loanTermItem.getValue().toString());
//						
//						RPCTemplate.call(new RPCExecutor<Void>(){
//							
//							@Override
//							public void execute(
//									AsyncCallback<Void > callback) {
//								server.regPosLoan(cardNo, merId, mcc, loanAmt, flag, address, term,programId,callback);
//							}
//							
//							@Override
//							public void onSuccess(Void result) {
//								clientUtils.showSuccess();
//							}
//							
//						});
//					}
//				}
//			});
//			applyForm.setFields(cardItem,feesChargedItem,merChantNoItem,merchantMCCItem,
//					programIdItem,loanTermItem,amountItem,commodityNameAddrInfoItem,applyButton);
//			
//			
//
//		}
//		return applyForm;
//	}
//	/**
//	 * 授权信息数据源
//	 */
//	public YakDataSource createTmUnMatchListGrideDataSource(){
//		//先定义DataSource
//		YakDataSource fds = new YakDataSource() {
//			@Override
//			public void fetchData(FetchRequest fetchRequest,
//					AsyncCallback<FetchResponse> callback) {
//				server.getAuthUnTranList(fetchRequest, cardNoQuery, callback);
//			}
//		};
//		
//		return fds;
//	}
//	/**
//	 * 授权信息列表
//	 */
//	private ListGrid createUnmatchList(){
//		listGrid = new ListGrid(){
//
//			@Override  
//	        protected Canvas createRecordComponent(final ListGridRecord record, Integer colNum) {  
//	    		String fieldName = this.getFieldName(colNum);  
//	    		String txnStatus = record.getAttribute("b039");
//	    		listGrid.setOverflow(Overflow.AUTO);
//	    		
//	    		HLayout btnLayout = new HLayout(4);
//	    		btnLayout.setHeight(30);
//	    		btnLayout.setWidth100();
//	            if (fieldName.equals("buttonField")) {
//	                
//	            	/*查看交易详细信息按钮*/
//	            	IButton viewBtn = new IButton(constants.btnTitleViewTrans());
//	        		viewBtn.setWidth(60);
//	                viewBtn.addClickHandler(new ClickHandler() {
//						
//						@Override
//						public void onClick(com.smartgwt.client.widgets.events.ClickEvent event) {
//	                    	listGrid.selectSingleRecord(record);
//	                    	//显示所选择的交易详细信息
//	                    	fetchTransDetail(record.getAttributeAsInt(TmUnmatchO.P_LogKv));
//						}
//					});
//	                btnLayout.addMember(viewBtn);
//		        } 
//	            
//	            if(fieldName.equals("buttonField")&&(txnStatus.equals("00")||txnStatus.equals("11"))){
//	            	
//	            	//撤消交易按钮
//	            	IButton cancelBtn = new IButton(constants.cancel());
//	            	cancelBtn.setWidth(60);
//	            	cancelBtn.addClickHandler(new ClickHandler() {
//	            		
//	            		@Override
//	            		public void onClick(
//	            				com.smartgwt.client.widgets.events.ClickEvent event) {
//	            			listGrid.selectSingleRecord(record);
//	            			//撤消当前交易
//	            			cancelAuthTrans(record.getAttributeAsInt(TmUnmatchO.P_LogKv));
//	            		}  
//	            	});
//	            	
//	            	btnLayout.addMember(cancelBtn);
//	            	
//	            }
//	            return btnLayout;  
//			}
//		};
//		
//		listGrid.setShowRecordComponents(true);
//		listGrid.setShowRecordComponentsByCell(true);
//		listGrid.setHeight100();
//		listGrid.setWidth100();
//		listGrid.setCanHover(true);
//		listGrid.setShowHover(true);
//		listGrid.setHoverWidth(300);
//		listGrid.setRecordComponentPoolingMode(RecordComponentPoolingMode.DATA);
//		listGrid.setSelectionType(SelectionStyle.SINGLE);
//		listGrid.setOverflow(Overflow.AUTO);
//		
//		
//		ListGridField buttonField = new ListGridField("buttonField", constants.operate());
//		buttonField.setWidth(150);
//		ListGridField cardField=uTmUnmatch.B002().width(100).createLGField();
//		cardField.setTitle(constants.cardNo());
//		
//		//交易传输日期格式转换 MMDDhhmmss -> MM-DD hh:mm:ss
//		ListGridField transmissionTimestampField = uTmUnmatch.TransmissionTimestamp().width(100).createLGField();
//		{
//			transmissionTimestampField.setCellFormatter(new TransmissionTimestampCellFormatter());
//		}
//		
//		listGrid.setFields(
//				cardField,
//				uTmUnmatch.AcctType().width(130).createLGField(),
//				transmissionTimestampField,
//				uTmUnmatch.TxnCurrCd().width(90).createLGField(),
//				uTmUnmatch.TxnAmt().width(90).createLGField(),
//				uTmUnmatch.AuthCode().width(90).createLGField(),
//				uTmUnmatch.TxnStatus().width(90).createLGField(),
//				uTmUnmatch.TxnType().width(120).createLGField(),
//				uTmUnmatch.Channel().width(90).createLGField(),
//				uTmUnmatch.AcqNameAddr().width(120).createLGField(),
//				uTmUnmatch.TxnDirection().width(90).createLGField(),
//				buttonField
//				);
//		
//		listGrid.setDataSource(this.createTmUnMatchListGrideDataSource());
//		
//		return listGrid;
//	}
//	
//	/**
//	 * 授权详细信息
//	 */
//	public Layout createUnmatchDetileForm(){
//
//		VLayout layout = new VLayout();
//		layout.setWidth100();
//		layout.setHeight100();
//	   
//		//授权表单详细信息
//		unmatchDetileForm = new YakDynamicForm();
//		{
//			unmatchDetileForm.setNumCols(4);
//			unmatchDetileForm.setWidth100();
//			unmatchDetileForm.setHeight100();
//			unmatchDetileForm.setTitleWidth(130);
//			unmatchDetileForm.setOverflow(Overflow.AUTO);
//
//			
//			unmatchDetileForm.setFields(uTmUnmatch.AcctType().asLabel().createFormItem(),
//					uTmUnmatch.AcqRefNo().asLabel().createFormItem(),
//					uTmUnmatch.TxnAmt().asLabel().createFormItem(),
//					uTmUnmatch.TxnCurrCd().asLabel().createFormItem(),
//					uTmUnmatch.AuthCode().asLabel().createFormItem(),
//					uTmUnmatch.AcqNameAddr().asLabel().createFormItem(),
//					uTmUnmatch.ChbTxnAmt().asLabel().createFormItem(),
//					uTmUnmatch.ChbCurrCd().asLabel().createFormItem(),
//
//					uTmUnmatch.Mcc().asLabel().createFormItem(),
//					uTmUnmatch.AcqBranchId().asLabel().createFormItem(),
//					uTmUnmatch.FwdInstId().asLabel().createFormItem(),
//					uTmUnmatch.TransmissionTimestamp().asLabel().createFormItem(),
//					uTmUnmatch.SettleDate().asLabel().createFormItem(),
//
//					uTmUnmatch.TxnDirection().asLabel().createFormItem(),
//					uTmUnmatch.TxnStatus().asLabel().createFormItem(),
//					uTmUnmatch.TxnType().asLabel().createFormItem(),
//					uTmUnmatch.Channel().asLabel().createFormItem(),
//					uTmUnmatch.TxnTerminal().asLabel().createFormItem(),
//					uTmUnmatch.LogOlTime().asLabel().createFormItem(),
//					uTmUnmatch.LogBizDate().asLabel().createFormItem(),
//					
//					uTmUnmatch.Mti().asLabel().createFormItem(),
//					uTmUnmatch.OrigTxnType().asLabel().createFormItem(),
//					uTmUnmatch.OrigFwdInstId().asLabel().createFormItem(),
//					uTmUnmatch.OrigAcqInstId().asLabel().createFormItem(),
//					uTmUnmatch.OrigMti().asLabel().createFormItem(),
//					uTmUnmatch.OrigTransDate().asLabel().createFormItem(),
//					uTmUnmatch.OrigTraceNo().asLabel().createFormItem(),
//					uTmUnmatch.OrigTxnProc().asLabel().createFormItem(),
//					uTmUnmatch.OrigTxnAmt().asLabel().createFormItem(),
//					uTmUnmatch.OrigLogKv().asLabel().createFormItem(),
//					uTmUnmatch.OrigTxnVal1().asLabel().createFormItem(),
//					uTmUnmatch.OrigTxnVal2().asLabel().createFormItem(),
//					uTmUnmatch.OrigChbTxnAmt().asLabel().createFormItem(),
//					
//					uTmUnmatch.LastReversalDate().asLabel().createFormItem(),
//					uTmUnmatch.VoidCount().asLabel().createFormItem(),
//					uTmUnmatch.ManualAuthFlag().asLabel().createFormItem(),
//					uTmUnmatch.OperaId().asLabel().createFormItem(),
//					uTmUnmatch.Brand().asLabel().createFormItem(),
//					
//					uTmUnmatch.ProductCd().asLabel().createFormItem(),
//					uTmUnmatch.MccType().asLabel().createFormItem(),
//					uTmUnmatch.FinalReason().asLabel().createFormItem(),
//					uTmUnmatch.FinalAction().asLabel().createFormItem(),
//					uTmUnmatch.CompAmt().asLabel().createFormItem(),
//
//					uTmUnmatch.FinalUpdDirection().asLabel().createFormItem(),
//					uTmUnmatch.FinalUpdAmt().asLabel().createFormItem(),
//					uTmUnmatch.IcInd().asLabel().createFormItem(),
//					uTmUnmatch.The3dsecureType().asLabel().createFormItem(),
//					uTmUnmatch.VipStatus().asLabel().createFormItem(),
//					uTmUnmatch.CurrBal().asLabel().createFormItem(),
//					uTmUnmatch.CashAmt().asLabel().createFormItem(),
//					uTmUnmatch.Otb().asLabel().createFormItem(),
//					
//					
//					uTmUnmatch.CashOtb().asLabel().createFormItem(),
//					uTmUnmatch.CustOtb().asLabel().createFormItem(),
//					uTmUnmatch.CardBlackFlag().asLabel().createFormItem(),
//					uTmUnmatch.MerchantBlackFlag().asLabel().createFormItem(),
//					uTmUnmatch.ExpireDate().asLabel().createFormItem(),
//					uTmUnmatch.TrackOneResult().asLabel().createFormItem(),
//					uTmUnmatch.TrackTwoResult().asLabel().createFormItem(),
//					uTmUnmatch.TrackThreeResult().asLabel().createFormItem(),
//					uTmUnmatch.PwdType().asLabel().createFormItem(),
//					uTmUnmatch.CheckPwdResult().asLabel().createFormItem(),
//					uTmUnmatch.PayPwdErrNum().asLabel().createFormItem(),
//					
//					
//					uTmUnmatch.CheckCvvResult().asLabel().createFormItem(),
//					uTmUnmatch.CheckCvv2Result().asLabel().createFormItem(),
//					uTmUnmatch.CheckIcvnResult().asLabel().createFormItem(),
//					uTmUnmatch.CheckArqcResult().asLabel().createFormItem(),
//					uTmUnmatch.CheckAtcResult().asLabel().createFormItem(),
//					uTmUnmatch.CheckCvrResult().asLabel().createFormItem(),
//					uTmUnmatch.CheckTvrResult().asLabel().createFormItem(),
//					uTmUnmatch.RejReason().asLabel().createFormItem(),
//					
//					uTmUnmatch.UnmatchCr().asLabel().createFormItem(),
//					uTmUnmatch.UnmatchDb().asLabel().createFormItem(),
//					uTmUnmatch.B002().asLabel().createFormItem(),
//					uTmUnmatch.B003().asLabel().createFormItem(),
//					uTmUnmatch.B007().asLabel().createFormItem(),
//					uTmUnmatch.B011().asLabel().createFormItem(),
//					uTmUnmatch.B022().asLabel().createFormItem(),
//					uTmUnmatch.B025().asLabel().createFormItem(),
//					uTmUnmatch.B032().asLabel().createFormItem(),
//					uTmUnmatch.B033().asLabel().createFormItem(),
//					uTmUnmatch.B039().asLabel().createFormItem(),
//					uTmUnmatch.B042().asLabel().createFormItem(),
//					uTmUnmatch.B060().startRow(true).colSpan(unmatchDetileForm.getNumCols()-1).asLabel().createFormItem(),
//					uTmUnmatch.B061().startRow(true).colSpan(unmatchDetileForm.getNumCols()-1).asLabel().createFormItem(),
//					uTmUnmatch.B090().asLabel().createFormItem()
//										);
//
//		}
//
//		layout.addMember(unmatchDetileForm);
//
//		return layout;
//	}
//	/**
//	 * 分期列表
//	 */
////	private ListGrid createloanRegList(){
////		return null;
////	}
//	
//	/**
//	 * 创建unmatch和分期信息的tabset
//	 * 
//	 * @return
//	 */
//	private TabSet createTranTabSet() {
//		tranTabSet = new TabSet();
//		tranTabSet.setHeight100();
//		tranTabSet.setWidth100();
//		final Tab unmatchTab = new Tab(constants.unmatchList());
//		VLayout unmatchLayout = new VLayout();
//		unmatchLayout.setHeight100();
//		unmatchLayout.setWidth100();
//		unmatchLayout.addMember(createUnmatchList());
//		unmatchLayout.addMember(createUnmatchDetileForm());
//		{
//			unmatchTab.setPane(unmatchLayout);
//		}
//		tranTabSet.addTab(unmatchTab);	
//		return tranTabSet;
//	}
//	
//	/**
//	 * 获取并显示所选择的交易详细信息
//	 * @param logKey
//	 */
//	private void fetchTransDetail(final int logKey) {
//		RPCTemplate.call(new RPCExecutor<TmUnmatchO>() {
//			@Override
//			public void execute(AsyncCallback<TmUnmatchO> callback) {
//				unmatchDetileForm.clearValues();
//				server.getTmUnmatchO(logKey, callback);
//			}
//			
//			@Override
//			public void onSuccess(TmUnmatchO result) {
//				unmatchDetileForm.setValues(result.convertToMap());
//			}
//			
//		}, listGrid);
//	}
//	
//	/**
//	 * 点击撤消按钮时触发撤消授权交易的处理逻辑
//	 * @param logKey
//	 */
//	private void cancelAuthTrans(final int logKey) {
//		SC.confirm(constants.msgConfirmCancel(), new BooleanCallback() {
//			
//			@Override
//			public void execute(Boolean chooseYes) {
//				if(null != chooseYes && chooseYes) {
//					RPCTemplate.call(new RPCExecutor<Void>() {
//						
//						@Override
//						public void execute(AsyncCallback<Void> callback) {
//							// 撤消授权交易
//							server.cancelLoanTrans(logKey, callback);
//						}
//						
//						@Override
//						public void onSuccess(Void result) {
//							clientUtils.showSuccess();
//						}
//							
//					}, listGrid);
//				}
//			}
//		});
//		
//	}
//	@Override
//	public void updateView(ParamMapToken token) {
//		//商户号、商户MCC取值方法
//		RPCTemplate.call(new RPCExecutor<Map<String, LoanMerchant>>() {
//			
//			@Override
//			public void execute(
//					AsyncCallback<Map<String, LoanMerchant>> callback) {
//				server.getLoanMerchant(callback);
//			}
//
//			@Override
//			public void onSuccess(Map<String, LoanMerchant> result) {
//				for( Map.Entry<String, LoanMerchant> entry:result.entrySet()){
//					valueMap.put(entry.getValue().merId, entry.getValue().merId+"-"+entry.getValue().merName);
//					valueMap2.put(entry.getValue().merId,entry.getValue().merType);
//				}
//				merChantNoItem.setValueMap(valueMap);
//			}
//		});
//		
//		//分期活动编号、分期期数取值方法
//		RPCTemplate.call(new RPCExecutor<Map<String,Program>>() {
//
//			@Override
//			public void execute(AsyncCallback<Map<String, Program>> callback) {
//				server.getProgram(callback);
//			}
//
//			@Override
//			public void onSuccess(Map<String, Program> result) {
//				for(Map.Entry<String, Program> entry:result.entrySet()){
//					valueMapProId.put(entry.getValue().programId, entry.getValue().programId);
//				}
//				programIdItem.setValueMap(valueMapProId);
//				map.putAll(result);
//			}
//		});
//	}

}
