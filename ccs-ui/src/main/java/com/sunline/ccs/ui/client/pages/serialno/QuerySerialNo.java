/**
 * 
 */
package com.sunline.ccs.ui.client.pages.serialno;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
//import com.sunline.ccs.infrastructure.client.ui.UTmAuthInqLog;
import com.sunline.ccs.infrastructure.client.ui.UCcsAuthmemoInqLog;
//import com.sunline.ccs.infrastructure.client.ui.UTmCardMediaMap;
import com.sunline.ccs.infrastructure.client.ui.UCcsCardLmMapping;
import com.sunline.ccs.ui.client.commons.CommonGlobalConstants;
import com.sunline.ccs.ui.client.commons.CommonUiUtils;
import com.sunline.ccs.ui.client.commons.UIUtilConstants;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.ui.event.client.IClickEventListener;

/**
 * 当天授权查询类流水查询
 * @author dch
 *
 */
@Singleton
public class QuerySerialNo extends Page {
	@Inject
	private QuerySerialNoConstants constants;
	@Inject
	private CommonGlobalConstants publicConstants;
	@Inject
	private UIUtilConstants utilConstants;
	@Inject
	private UCcsAuthmemoInqLog uCcsAuthInqLog;
	@Inject
	private UCcsCardLmMapping uCcsCardLmMapping;
	
	private KylinForm searchForm;
	
	private KylinGrid searchGrid;
	
	@Override
	public IsWidget createPage() {
		
		VerticalPanel panel = new VerticalPanel();
		
		panel.setWidth("98%");
		
		panel.add(createSearchForm());
		
		panel.add(createListGrid());
		
		return panel;
	}
	
	@Override
	public void refresh() {
		searchForm.getUi().clear();
		searchGrid.clearData();
	}
	
	/**
	 * 创建搜索表单
	 * @return
	 */
	private HorizontalPanel createSearchForm() {
		HorizontalPanel hPanel;
		searchForm = new KylinForm();
		{
			searchForm.setWidth("100%");
			searchForm.setHeight("60px");
			searchForm.setCol(3);
			// 卡账标识
			TextColumnHelper cardAcct = new TextColumnHelper(UIUtilConstants.INDICATOR_NAME_CARD_ACCT, utilConstants.cardAccIndTitle(), 19);
			cardAcct.asSelectItem();
			searchForm.setField(uCcsCardLmMapping.CardNbr().required(true), cardAcct.required(true));
			// 页面加载后执行
			Scheduler.get().scheduleFinally(new ScheduledCommand() {
				@Override
				public void execute() {
					// 设置卡账标识下拉框值
					searchForm.setFieldSelectData(UIUtilConstants.INDICATOR_NAME_CARD_ACCT, CommonUiUtils.cardOrAcctSelectItem(utilConstants));
				}
			});
	    	KylinButton btnSearch = ClientUtils.createSearchButton(new IClickEventListener(){
				@Override
				public void onClick() {
				    	if (searchForm.valid()) {
						searchGrid.loadData(searchForm);
				    	}
				}
			});
	    	hPanel = CommonUiUtils.lineLayoutForm(searchForm, btnSearch, null, null);
		}
		return hPanel;
	}
	
	/**
	 * 创建列表list显示
	 * @return
	 */
	private KylinGrid createListGrid() {
		searchGrid = new KylinGrid();
		searchGrid.setWidth("100%");
		searchGrid.setHeight("450px");
		searchGrid.checkbox(false);
		searchGrid.getSetting().delayLoad(true);
		searchGrid.loadDataFromUrl("rpc/t1311Server/getCurrentDayData");
		searchGrid.setColumns(
				uCcsAuthInqLog.B002CardNbr(),
				uCcsAuthInqLog.AcctNbr(),
				uCcsAuthInqLog.AcctType().columnRender(),
				uCcsAuthInqLog.TransportTime(),
				uCcsAuthInqLog.TxnType().columnRender(),
				uCcsAuthInqLog.TxnDirection(),
				uCcsAuthInqLog.AuthTxnStatus().columnRender(),
				uCcsAuthInqLog.AuthCode(),
				uCcsAuthInqLog.FinalAction().columnRender(),
				uCcsAuthInqLog.FinalReason().columnRender()
		);
		return searchGrid;
	}
}
