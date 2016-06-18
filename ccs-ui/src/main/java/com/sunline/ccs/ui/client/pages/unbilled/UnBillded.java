/**
 * 
 */
package com.sunline.ccs.ui.client.pages.unbilled;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsAcct;
import com.sunline.ccs.infrastructure.client.ui.UCcsTxnUnstatement;
import com.sunline.ccs.ui.client.commons.CommonUiUtils;
import com.sunline.ccs.ui.client.commons.CommonVerticalPanel;
import com.sunline.ccs.ui.client.commons.UIUtilConstants;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.ui.event.client.IClickEventListener;

/**
 * 未出账单交易查询
* @author dch
 *
 */
@Singleton
public class UnBillded extends Page {
	
	private KylinForm searchForm;
	private KylinGrid searchGrid = new KylinGrid();
	@Inject
	private UCcsTxnUnstatement uCcsTxnUnstatement;
	@Inject
	private UCcsAcct uCcsAcct;
	@Inject
	private UIUtilConstants utilConstants;
	@Inject
	private UnBilledConstants constants;
	
//	@Inject
//	private T1306InterAsync server;
//	
//	@Inject
//	private ClientUtils clientUtils;
//	
//	@Inject
//	private UIUtil uiUtil;
//	
//	@Inject
//	private UTmTxnUnstmt uCcsTxnUnstatement;

//
//	private GridHeader gridHeader;
//	private ListGrid listGrid;
//	private FormItem cardItem;
//	private FormItem txnCodeItem;
//	private SelectItem cardAcctIndItem;
//	private SelectItem currencyItem;
//	private String submitCardNo;
	
	@Override
	public IsWidget createPage() {
		
		// 返回组件
		CommonVerticalPanel panel = new CommonVerticalPanel();
		
		// 搜索
		panel.add(createSearchForm());
		
		// 列表
		panel.add(createListGrid());
		
		return panel;
	}
	
	@Override
	public void refresh() {
		searchForm.getUi().clear();
		searchGrid.clearData();
//		searchGrid.loadDataFromUrl("rpc/t1306Server/getUnstmtTranList");
	}
	
	/**
	 * 创建列表list显示
	 * @return
	 */
	private KylinGrid createListGrid() {
		
		searchGrid = new KylinGrid();
		searchGrid.setWidth("98%");
		searchGrid.setHeight("400px");
		searchGrid.checkbox(false);
		searchGrid.getSetting().delayLoad(true);
		searchGrid.loadDataFromUrl("rpc/t1306Server/getUnstmtTranList");

		searchGrid.setColumns(
				uCcsTxnUnstatement.AcctNbr(),
				uCcsTxnUnstatement.TxnDesc(),
			    uCcsTxnUnstatement.TxnShortDesc(),
			    uCcsTxnUnstatement.TxnCode(),
				uCcsTxnUnstatement.TxnDate(),
				uCcsTxnUnstatement.TxnTime(),
				uCcsTxnUnstatement.RefNbr(),
				uCcsTxnUnstatement.AcqAddress(),
				uCcsTxnUnstatement.TxnCurrency(),
				uCcsTxnUnstatement.DbCrInd().columnRender(),
				uCcsTxnUnstatement.TxnAmt(),
				uCcsTxnUnstatement.AuthCode(),
				uCcsTxnUnstatement.PostCurrency(),
				uCcsTxnUnstatement.PostAmt(),
				uCcsTxnUnstatement.PostDate()
				);
		
		return searchGrid;
	}
	
	/**
	 * 创建搜索表单
	 * @return
	 */
	private KylinForm createSearchForm() {
		//搜索框
		searchForm = new KylinForm();
		{
			searchForm.setWidth("98%");
			searchForm.setCol(3);
			// 卡账标识
//			TextColumnHelper cardAcct = new TextColumnHelper(UIUtilConstants.INDICATOR_NAME_CARD_ACCT, utilConstants.cardAccIndTitle(), 19);
//			cardAcct.asSelectItem();
			// 币种
//			TextColumnHelper currCd = new TextColumnHelper("currCd", constants.labelCurrency(), 19);
//			currCd.asSelectItem();
			searchForm.setField(uCcsAcct.ContrNbr());
			
			// 页面加载后执行
//			Scheduler.get().scheduleFinally(new ScheduledCommand() {
//				@Override
//				public void execute() {
//					// 设置卡账标识下拉框值
//					searchForm.setFieldSelectData(UIUtilConstants.INDICATOR_NAME_CARD_ACCT, CommonUiUtils.cardOrAcctSelectItem(utilConstants));
//					// 设置币种
//			    	searchForm.setFieldSelectData("currCd", CommonUiUtils.currencySelectItem(utilConstants));
//				}
//			});
			
	    	searchForm.addButton(ClientUtils.createSearchButton(new IClickEventListener(){
				@Override
				public void onClick() {
				    if(searchForm.valid()){
//					searchGrid.getUi().setParm(uCcsAcct.ContrNbr().getName(), searchForm.getFieldValue(uCcsAcct.ContrNbr().getName()));
//					searchGrid.loadData();
					searchGrid.loadData(searchForm);
				    }
				}
			}));
			
		}
		return searchForm;
	}
	
//	/**
//	 * 创建币种下拉框
//	 * @return
//	 */
//	public SelectItem createCurrencySelectItem() {
//		SelectItem currencyItem = new SelectItem("currCd", constants.labelCurrency());
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
	
}
