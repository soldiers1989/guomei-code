package com.sunline.ccs.ui.client.commons;

import java.util.Map;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsCardLmMapping;
import com.sunline.ccs.infrastructure.client.ui.UTxnCd;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.kylin.web.ark.client.helper.ColumnHelper;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.pcm.infrastructure.client.ui.UCurrencyCd;

/**
 * 客户端工具
* @author fanghj
 *
 */
@Singleton
public class UIUtil {
	
	public static final String TXNCD_KEY_CODE = "txnCd";
	
	public static final String TXNCD_KEY_DESC = "description";
	
	public static final String CURRENCYCD_KEY_CODE = "currencyCd";
	
	public static final String CURRENCYCD_KEY_DESC = "description";
	
	@Inject
	private UTxnCd uTxnCd;
	
	@Inject
	private UCurrencyCd uCurrencyCd;
	
	@Inject
	private UCcsCardLmMapping uCcsCardLmMapping;;
	
	@Inject
	private UIUtilConstants constants;
	
//	//交易码下拉框中的卡片数据
//	private HashMap<PickList, ArrayList<Record>> txnCdListMap = new HashMap<PickList, ArrayList<Record>>();
//	
//	//货币代码下拉框中的卡片数据
//	private HashMap<PickList, ArrayList<Record>> currencyListMap = new HashMap<PickList, ArrayList<Record>>();
	
	/**
	 * 创建介质卡号输入框组件
	 * @return
	 */
	public TextColumnHelper createCardNoItem() {
		// TODO 暂时去除校验器
//		CardNoValidator validator = new CardNoValidator();
		TextColumnHelper cardNoItem = uCcsCardLmMapping.CardNbr()
//				.validators(validator)
				.required(true);
//				.createFormItem()
		
		return cardNoItem;
	}
	
	/**
	 * 获取输入的卡号，会自动将其中的空格自动删除保留有效字符。
	 * @param cardNoItem
	 * @return
	 */
	public static String getCardNo(KylinForm form) {
//		getCardNo(FormItem cardNoItem)
//		if(null == cardNoItem || null == cardNoItem.getValue()) {
//			return "";
//		}
//		return CardNoValidator.removeSpaces(cardNoItem.getValue().toString());
		// TODO 临时写法
		return form.getFieldValue(CcsCardLmMapping.P_CardNbr);
	}
	
	/**
	 * 创建卡账指示下拉框
	 *  A-账户
	 *  C-卡片
	 * @return
	 */
//	public SelectItem createCardAcctIndSelectItem() {
//		SelectItem si= new SelectItem(UIUtilConstants.INDICATOR_NAME_CARD_ACCT, 
//				constants.cardAccIndTitle());// 新建卡帐指示下拉框
//		si.setRequired(true);
//		
//		LinkedHashMap<String, String> valueMap = new LinkedHashMap<String, String>();
//		valueMap.put(UIUtilConstants.INDICATOR_VAL_ACCT, constants.acctIndTitle());
//		valueMap.put(UIUtilConstants.INDICATOR_VAL_CARD, constants.cardIndTitle());
//		
//		si.setValueMap(valueMap);
//		si.setDefaultValue(UIUtilConstants.INDICATOR_VAL_ACCT);
//		
//		return si;
//	}
	

	/**
	 * 创建交易码下拉框组件所需的数据源对象
	 * @return
	 */
//	private DataSource makeTxnCdDataSource() {
//		DataSource dataSource = new DataSource();
//		DataSourceTextField txnCdField = new DataSourceTextField(TXNCD_KEY_CODE);
//		txnCdField.setPrimaryKey(true);
//        
//        DataSourceTextField descField = new DataSourceTextField(TXNCD_KEY_DESC);   
//        dataSource.setFields(txnCdField, descField);
//        dataSource.setClientOnly(true);
//		return dataSource;
//	}
	
	/**
	 * 创建交易码下拉框组件
	 * @param txnCdItem 预先创建好的交易码下拉框组件，类型为SelectItem或者ComboBoxItem
	 */
//	public void constructTxnCdPickListUI(PickList txnCdItem) {
//		DataSource dataSource = makeTxnCdDataSource();
//		
//		ArrayList<Record> cardRecordList = new ArrayList<Record>();
//		txnCdListMap.put(txnCdItem, cardRecordList);
//		
//        txnCdItem.setOptionDataSource(dataSource);
//        txnCdItem.setValueField(TXNCD_KEY_CODE); 
//        txnCdItem.setDisplayField(TXNCD_KEY_CODE);
//        txnCdItem.setPickListWidth(250);
//        
//        ListGridField txnCdField = uTxnCd.TxnCd().createLGField();
//        txnCdField.setWidth(60);
//        ListGridField descField = uTxnCd.Description().createLGField();
//        descField.setWidth(180);
//        
//        if(txnCdItem instanceof ComboBoxItem) {
//        	ComboBoxItem item = (ComboBoxItem)txnCdItem;
//	        item.setPickListFields(
//	        		txnCdField,
//	        		descField
//	        		);   
//        } else if(txnCdItem instanceof SelectItem) {
//        	SelectItem item = (SelectItem)txnCdItem;
//        	item.setPickListFields(
//        			txnCdField,
//	        		descField
//	        		);   
//        } else {
//        	throw new IllegalArgumentException("txnCdItem must be SelectItem or ComboBoxItem.");
//        }
//	}
	
	/**
	 * 根据交易码列表数据填充交易码下拉框组件中的交易码数据
	 * @param txnCdItem	UIUtil创建的交易码下拉框组件
	 * @param txnCdList	交易码列表数据
	 */
//	public void updateTxnCdPickListData(PickList txnCdItem, Collection<TxnCd> txnCdList) {
//		DataSource dataSource = txnCdItem.getOptionDataSource();
//		
//		//首先清除下拉框中原有的卡片列表
//		ArrayList<Record> txnCdRecordList = txnCdListMap.get(txnCdItem);
//		if(null != txnCdRecordList) {
//			for(Record record : txnCdRecordList) {
//				dataSource.removeData(record);
//			}
//			txnCdRecordList.clear();
//		} else {
//			txnCdRecordList = new ArrayList<Record>();
//		}
//		
//		//将新的交易码列表添加到下拉框中
//		if(null != txnCdList) {
//			for(TxnCd code : txnCdList) {
//				ListGridRecord record = createTxnCdLGRecord(code);
//				dataSource.addData(record);
//				txnCdRecordList.add(record); //保存交易码列表以便下次清除下拉框中原有的交易码列表
//			}
//		}
//		txnCdListMap.put(txnCdItem, txnCdRecordList);
//		txnCdItem.setOptionDataSource(dataSource);
//	}
	
	/**
	 * 根据交易码对象组建ListGridRecord对象
	 * @param txnCd
	 * @return
	 */
//	private ListGridRecord createTxnCdLGRecord(TxnCd txnCd) {
//		ListGridRecord record = new ListGridRecord();
//		record.setAttribute(TXNCD_KEY_CODE, txnCd.txnCd);
//		record.setAttribute(TXNCD_KEY_DESC, txnCd.description);
//		return record;
//	}
	

	/**
	 * 填充交易码下拉框的数据
	 * 
	 * @param txnCdItem	交易码下拉框
	 * @param txnCdList	交易码列表
	 */
//	public void updateTxnCdSelectItemData(SelectItem txnCdItem, Collection<TxnCd> txnCdList) {
//		LinkedHashMap<String, String> valueMap = new LinkedHashMap<String, String>();
//		if(null != txnCdList) {
//			for(TxnCd txnCd : txnCdList) {
//				valueMap.put(txnCd.txnCd, txnCd.txnCd + " - " + txnCd.description);
//			}
//		}
//		// 获取交易码列表并更新交易码下拉框
//		txnCdItem.setValueMap(valueMap);
//	}
	
	/**
	 * 填充货币代码下拉框的数据
	 * 
	 * @param currencyCd
	 */
//	public void updateCurrencyCdSelectItemData(SelectItem currencyCdItem, Collection<CurrencyCd> currencyCdList, CurrencyCd defaultValue) {
//		
//		LinkedHashMap<String, String> valueMap = new LinkedHashMap<String, String>();
//		if(null != currencyCdList) {
//			for(CurrencyCd currencyCd : currencyCdList) {
//				valueMap.put(currencyCd.currencyCd, currencyCd.currencyCd + " - " + currencyCd.description);
//			}
//		}
//		
//		// 获取交易码列表并更新交易码下拉框
//		currencyCdItem.setValueMap(valueMap);
//		currencyCdItem.setDefaultValue(null != defaultValue ? defaultValue.currencyCd : "");
//	}

	/**
	 * 创建货币代码下拉框组件所需的数据源对象
	 * @return
	 */
//	private DataSource makeCurrencyDataSource() {
//		DataSource dataSource = new DataSource();
//		DataSourceTextField currencyCdField = new DataSourceTextField(CURRENCYCD_KEY_CODE);
//		currencyCdField.setPrimaryKey(true);
//        
//        DataSourceTextField descField = new DataSourceTextField(CURRENCYCD_KEY_DESC);   
//        
//        dataSource.setFields(currencyCdField, descField);
//  
//        dataSource.setClientOnly(true);
//        
//		return dataSource;
//	}
	
	/**
	 * 创建货币代码下拉框组件
	 * @param currencyItem 预先创建好的货币代码下拉框组件，类型为SelectItem或者ComboBoxItem
	 */
//	public void constructCurrencyPickListUI(PickList currencyItem) {
//		DataSource dataSource = makeCurrencyDataSource();
//		
//		ArrayList<Record> currencyRecordList = new ArrayList<Record>();
//		txnCdListMap.put(currencyItem, currencyRecordList);
//		
//        currencyItem.setOptionDataSource(dataSource);
//        currencyItem.setValueField(CURRENCYCD_KEY_CODE); 
//        currencyItem.setDisplayField(CURRENCYCD_KEY_CODE);
//        currencyItem.setPickListWidth(210);
//        
//        ListGridField codeField = uCurrencyCd.CurrencyCd().createLGField();
//        codeField.setWidth(60);
//        ListGridField descField = uCurrencyCd.Description().createLGField();
//        descField.setWidth(150);
//        
//        if(currencyItem instanceof ComboBoxItem) {
//        	ComboBoxItem item = (ComboBoxItem)currencyItem;
//	        item.setPickListFields(
//	        		codeField,
//	        		descField
//	        		);   
//        } else if(currencyItem instanceof SelectItem) {
//        	SelectItem item = (SelectItem)currencyItem;
//        	item.setPickListFields(
//        			codeField,
//	        		descField
//	        		);   
//        } else {
//        	throw new IllegalArgumentException("currencyItem must be SelectItem or ComboBoxItem.");
//        }
//		
//	}
	
	/**
	 * 根据货币代码列表数据填充货币代码下拉框组件中的货币代码数据
	 * @param currencyItem	UIUtil创建的货币代码下拉框组件
	 * @param currencyCdList	货币代码列表数据
	 */
//	public void updateCurrencyPickListData(PickList currencyItem, Collection<CurrencyCd> currencyCdList, CurrencyCd defaultValue) {
//		DataSource dataSource = currencyItem.getOptionDataSource();
//		
//		//首先清除下拉框中原有的卡片列表
//		ArrayList<Record> currencyRecordList = currencyListMap.get(currencyItem);
//		if(null != currencyRecordList) {
//			for(Record record : currencyRecordList) {
//				dataSource.removeData(record);
//			}
//			currencyRecordList.clear();
//		} else {
//			currencyRecordList = new ArrayList<Record>();
//		}
//		
//		//将新的货币代码列表添加到下拉框中
//		if(null != currencyCdList) {
//			for(CurrencyCd code : currencyCdList) {
//				ListGridRecord record = createCurrencyLGRecord(code);
//				dataSource.addData(record);
//				currencyRecordList.add(record); //保存货币代码列表以便下次清除下拉框中原有的货币代码列表
//			}
//		}
//		
//		currencyListMap.put(currencyItem, currencyRecordList);
//		currencyItem.setOptionDataSource(dataSource);
//		
//		if(defaultValue != null) {
//			if(currencyItem instanceof ComboBoxItem) {
//	        	ComboBoxItem item = (ComboBoxItem)currencyItem;
//	        	item.setDefaultValue(defaultValue.currencyCd);
//	        } else if(currencyItem instanceof SelectItem) {
//	        	SelectItem item = (SelectItem)currencyItem;
//	        	item.setDefaultValue(defaultValue.currencyCd);
//	        } else {
//	        	throw new IllegalArgumentException("currencyItem must be SelectItem or ComboBoxItem.");
//	        }
//		}
//	}
	
	/**
	 * 根据货币代码对象组建ListGridRecord对象
	 * @param currencyCd
	 * @return
	 */
//	private ListGridRecord createCurrencyLGRecord(CurrencyCd currencyCd) {
//		ListGridRecord record = new ListGridRecord();
//		record.setAttribute(CURRENCYCD_KEY_CODE, currencyCd.currencyCd);
//		record.setAttribute(CURRENCYCD_KEY_DESC, currencyCd.description);
//		
//		return record;
//	}
	
	public HorizontalPanel createSubmitClearLayout(final KylinForm form, final ColumnHelper ... clearValueFormItems)
	{
		HorizontalPanel layout = new HorizontalPanel();
		layout.setHeight("10px");
//		{
//			layout.setWidth("100%");
//			layout.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
//			IButton submitButton = clientUtils.createSubmitButton();
//			{
//				submitButton.addClickHandler(new ClickHandler() {
//					
//					@Override
//					public void onClick(ClickEvent event) {
//						form.submit();
//					}
//				});
//			}
//			layout.addMember(submitButton);
//			
//			IButton clearButton = clientUtils.createResetButton();
//			{
//				clearButton.setTitle(constants.btnTitleClear());
//				clearButton.addClickHandler(new ClickHandler() {
//					
//					@Override
//					public void onClick(ClickEvent event) {
//						form.clearValues();
//						for(FormItem item: clearValueFormItems) {
//							item.clearValue();
//						}
//					}
//				});
//			}
//			layout.addMember(clearButton);
//		}
		return layout;
	}
	
//	public SelectItem createCurrencySelectItem() {
//		SelectItem currencyItem = new SelectItem(PublicConst.NAME_CURRENCY_CODE, constants.labelCurrency());
//		currencyItem.setRequired(true);
//		LinkedHashMap<String, String> valueMap = new LinkedHashMap<String, String>();
//		valueMap.put("156", constants.labelCurrencyRMB());
//		valueMap.put("840", constants.labelCurrencyUS());
//		currencyItem.setValueMap(valueMap);
//		
//		currencyItem.setDefaultValue("156");
//		
//		return currencyItem;
//	}
	
	@SuppressWarnings("rawtypes")
	public void setFormValus (KylinForm form, Map map) {
		for (Object keyObj : map.keySet()) {
			String key = (String) keyObj;
			Object value = map.get(key);
			if (value instanceof String) {
				form.setFieldValue(key, (String) value);
			} else if (value instanceof Boolean) {
				form.setFieldValue(key, (Boolean) value);
			}
		}
	}
}
