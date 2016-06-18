package com.sunline.ccs.ui.client.pages.htrecord;

import java.util.Date;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsAuthmemoHst;
import com.sunline.ccs.infrastructure.client.ui.UCcsCardLmMapping;
import com.sunline.ccs.ui.client.commons.CommonCcsDateUtils;
//import com.sunline.ccs.ui.client.commons.CommonCcsDateUtils;
import com.sunline.ccs.ui.client.commons.CommonUiUtils;
import com.sunline.ccs.ui.client.commons.CommonVerticalPanel;
import com.sunline.ccs.ui.client.commons.CommonGlobalConstants;
import com.sunline.ccs.ui.client.commons.UIUtilConstants;
import com.sunline.kylin.web.ark.client.helper.DateColumnHelper;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.event.client.IClickEventListener;

/**
 * 
 * @see 类名：HistoryTradingRecord
 * @see 描述：授权交易历史查询页面配置
 *
 * @see 创建日期：   Jun 27, 20152:30:52 PM
 * @author dch
 * 
 * @see 修改记录：
 * @see [编号：20150627_调整日期增减功能实现方式]，[修改人：yeyu]，[方法名：createSearchForm]
 */
@Singleton
public class HistoryTradingRecord extends Page{
	
	@Inject
	private HistoryTradingRecordConstants constants;
	@Inject
	private CommonGlobalConstants publicConstants;
	@Inject
	private UIUtilConstants utilConstants;
	@Inject
	private UCcsCardLmMapping uCcsCardLmMapping;

	@Inject
	private UCcsAuthmemoHst uCcsAuthHst;
	private KylinForm searchForm;
	private KylinGrid searchGrid = new KylinGrid();
	
	@Override
	public void refresh() {
	    searchForm.getUi().clear();
	    searchGrid.clearData();
		searchGrid.loadDataFromUrl("rpc/t1312Server/getAuthHstData");
	}
	
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
	
	/**
	 * 创建列表list显示
	 * @return
	 */
	private KylinGrid createListGrid() {
		
		searchGrid = new KylinGrid();
		searchGrid.setWidth("98%");
		searchGrid.setHeight("98%");
		searchGrid.checkbox(false);
//		searchGrid.getSetting().delayLoad(true);
//		searchGrid.getSetting().delayLoad(true);
//		//交易传输日期格式转换 MMDDhhmmss -> MM-DD hh:mm:ss
//		ListGridField transmissionTimestampField = uCcsAuthHst.TransmissionTimestamp().width(100).createLGField();
//		{
//			transmissionTimestampField.setCellFormatter(new TransmissionTimestampCellFormatter());
//		}
		searchGrid.setColumns(
				uCcsCardLmMapping.LogicCardNbr(),
				uCcsAuthHst.AcctNbr(),
				uCcsAuthHst.AcctType().columnRender(),
				uCcsAuthHst.TransportTime(),
				uCcsAuthHst.TxnType().columnRender(),
				uCcsAuthHst.TxnDirection().columnRender(),
				uCcsAuthHst.AuthTxnStatus().columnRender(),
				uCcsAuthHst.TxnAmt(),
				uCcsAuthHst.InputSource(),
				uCcsAuthHst.AuthCode(),
				uCcsAuthHst.FinalAction().columnRender(),
				uCcsAuthHst.FinalReason().columnRender(),
				uCcsAuthHst.B039RtnCode()
				);
		
		return searchGrid;
	}
	
	/**
	 * 创建搜索表单
	 * @return
	 */
	private KylinForm createSearchForm() {
		searchForm = new KylinForm();
		{
			searchForm.setWidth("98%");
			searchForm.setCol(3);
			// 卡账标识
			TextColumnHelper cardAcct = new TextColumnHelper(UIUtilConstants.INDICATOR_NAME_CARD_ACCT, utilConstants.cardAccIndTitle(), 19);
			cardAcct.asSelectItem();
			// 渠道
			TextColumnHelper siChannal = new TextColumnHelper("channel", constants.selectItemChannel(), 19);
			siChannal.asSelectItem();
			// 起始日期
	        DateColumnHelper beginDateItem = new DateColumnHelper("beginDate", publicConstants.labelBeginDate(), true, false);
			Date bdate = new Date();
			//bdate.setYear(bdate.getYear() - 3);
			bdate = new CommonCcsDateUtils(bdate.getTime()).addYear(CommonCcsDateUtils.Unit.YEAR, -3).asDate();
			beginDateItem.startDate(bdate);
			//bdate.setYear(bdate.getYear() + 23);
			//bdate = CommonCcsDateUtils.dayAddOrMinus(bdate, 23, CommonCcsDateUtils.YEAR);
			bdate = new CommonCcsDateUtils(bdate.getTime()).addYear(CommonCcsDateUtils.Unit.YEAR, 23).asDate();
			beginDateItem.endDate(bdate);
	        // 截止日期
			DateColumnHelper endDateItem = new DateColumnHelper("endDate", publicConstants.labelEndDate(), true, false);
			Date edate = new Date();
			//edate.setYear(edate.getYear() - 3);
			//edate = CommonCcsDateUtils.dayAddOrMinus(edate, -3, CommonCcsDateUtils.YEAR);
			edate = new CommonCcsDateUtils(edate.getTime()).addYear(CommonCcsDateUtils.Unit.YEAR, -3).asDate();
			endDateItem.startDate(edate);
			//edate.setYear(edate.getYear() + 23);
			//edate = CommonCcsDateUtils.dayAddOrMinus(edate, 23, CommonCcsDateUtils.YEAR);
			edate = new CommonCcsDateUtils(edate.getTime()).addYear(CommonCcsDateUtils.Unit.YEAR, 23).asDate();
			endDateItem.endDate(edate);
			
			searchForm.setField(uCcsCardLmMapping.CardNbr().required(true), cardAcct.required(true), siChannal.required(true)
					, beginDateItem, endDateItem);
			
			// 页面加载后执行
			Scheduler.get().scheduleFinally(new ScheduledCommand() {
				@Override
				public void execute() {
					// 设置卡账标识下拉框值
					searchForm.setFieldSelectData(UIUtilConstants.INDICATOR_NAME_CARD_ACCT, CommonUiUtils.cardOrAcctSelectItem(utilConstants));
					// 设置渠道
			    	searchForm.setFieldSelectData("channel", CommonUiUtils.channalSelectItem(utilConstants));
				}
			});
			
	    	searchForm.addButton(ClientUtils.createSearchButton(new IClickEventListener(){
				@Override
				public void onClick() {
//					Data submitData = searchForm.getSubmitData();
//					if (submitData.asMapData().getString("beginDate") != null) {
//						searchGrid.getUi().setParm("bDate", submitData.asMapData().getString("beginDate"));
//					}
//					if (submitData.asMapData().getString("endDate") != null) {
//						searchGrid.getUi().setParm("eDate", submitData.asMapData().getString("endDate"));
//					}
//					searchGrid.loadData();
					searchGrid.getUi().setParm(uCcsCardLmMapping.CardNbr().getName(),searchForm.getFieldValue(uCcsCardLmMapping.CardNbr().getName()));
					searchGrid.getUi().setParm(UIUtilConstants.INDICATOR_NAME_CARD_ACCT,searchForm.getFieldValue(UIUtilConstants.INDICATOR_NAME_CARD_ACCT));
					searchGrid.getUi().setParm("channel",searchForm.getFieldValue("channel"));
					searchGrid.getUi().setParm("beginDate",searchForm.getFieldValue("beginDate"));
					searchGrid.getUi().setParm("endDate",searchForm.getFieldValue("endDate"));
					searchGrid.loadDataFromUrl("rpc/t1312Server/getAuthHstData");
				}
			}));
		}
		return searchForm;
	}
	

}
