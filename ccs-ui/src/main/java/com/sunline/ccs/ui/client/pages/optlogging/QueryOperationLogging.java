package com.sunline.ccs.ui.client.pages.optlogging;

import java.util.Date;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsOpOperateLog;
import com.sunline.ccs.infrastructure.shared.model.CcsOpOperateLog;
import com.sunline.ccs.ui.client.commons.CommonGlobalConstants;
import com.sunline.ccs.ui.client.commons.CommonKylinForm;
import com.sunline.ccs.ui.client.commons.CommonUiUtils;
import com.sunline.ccs.ui.client.commons.CommonVerticalPanel;
import com.sunline.ccs.ui.client.commons.UIUtilConstants;
import com.sunline.kylin.web.ark.client.helper.DateColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.Detail;
import com.sunline.ui.grid.client.listener.IDetailExtendEventListener;
import com.sunline.ui.grid.client.listener.IShowDetailEventListener;

/**
 * @see 类名：QueryOperationLogging
 * @see 描述：操作日志记录查询
 *
 * @see 创建日期： Jun 23, 20158:41:45 PM
 * @author songyanchao
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Singleton
public class QueryOperationLogging extends Page {
    @Inject
    private QueryOperationLoggingConstants constants;
    @Inject
    private CommonGlobalConstants publicConstants;
    @Inject
    private UIUtilConstants utilConstants;
    @Inject
    private UCcsOpOperateLog uCcsOpOperateLog;
    private KylinForm searchForm;
    private KylinGrid searchGrid;

    @Override
    public IsWidget createPage() {

	// 返回组件
	VerticalPanel panel = new VerticalPanel();
	panel.setWidth("98%");
	panel.setHeight("100%");
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
    }

    /**
     * 创建搜索表单
     * 
     * @return
     */
    private HorizontalPanel createSearchForm() {
	searchForm = new KylinForm();
	    searchForm.setCol(4);
	    // 起始日期
	    DateColumnHelper beginDateItem =
		    new DateColumnHelper("beginDate", publicConstants.labelBeginDate(), true, true);
	    Date bdate = new Date();
	    beginDateItem.startDate(bdate);
	    beginDateItem.endDate(bdate);
	    // 截止日期
	    DateColumnHelper endDateItem = new DateColumnHelper("endDate", publicConstants.labelEndDate(), true, true);
	    Date edate = new Date();
	    endDateItem.startDate(edate);
	    endDateItem.endDate(edate);
	    searchForm.setField(uCcsOpOperateLog.CardNbr().required(true), beginDateItem, endDateItem);
	    // 页面加载后执行
	    Scheduler.get().scheduleFinally(new ScheduledCommand(){
		@Override
		public void execute() {
		    searchForm.setFieldSelectData(UIUtilConstants.INDICATOR_NAME_CARD_ACCT,
						  CommonUiUtils.cardOrAcctSelectItem(utilConstants));
		}
	    });

	    KylinButton searchButton = ClientUtils.createSearchButton(new IClickEventListener(){
		@Override
		public void onClick() {
		    if(searchForm.valid()){
			Data submitData = searchForm.getSubmitData();
			    if (submitData.asMapData().getString("beginDate") != null) {
			    	searchGrid.getUi().setParm("bDate",submitData.asMapData().getString("beginDate"));
			    }
			    if (submitData.asMapData().getString("endDate") != null) {
			    	searchGrid.getUi().setParm("eDate", submitData.asMapData().getString("endDate"));
			    }
			    searchGrid.getUi().setParm("cardNbr", submitData.asMapData().getString("cardNbr"));
			    searchGrid.loadData();
			    searchGrid.loadData(searchForm);
		    }
		}
	    });
	return CommonUiUtils.lineLayoutForm(searchForm, searchButton, null, null);
    }

    /**
     * 创建列表list显示
     * 
     * @return
     */
    private KylinGrid createListGrid() {

	searchGrid = new KylinGrid();
	searchGrid.setWidth("98%");
	searchGrid.setHeight("98%");
	searchGrid.checkbox(false);
	searchGrid.getSetting().delayLoad(true);
	searchGrid.loadDataFromUrl("rpc/t9001Server/getOperateLogList");
	searchGrid
		.setColumns(uCcsOpOperateLog.OpTime(), uCcsOpOperateLog.OpId(), uCcsOpOperateLog.CardNbr(),
			    uCcsOpOperateLog.CustId(), uCcsOpOperateLog.ServiceCode(), uCcsOpOperateLog.RelatedDesc());

	// 增加明细表
	Detail d = new Detail();
	d.setHeight("100%").onShowDetail(new IShowDetailEventListener(){

	    @Override
	    public Widget onShowDetail(MapData data) {
		return addDetail(data);
	    }
	}).onExtend(new IDetailExtendEventListener(){

	    @Override
	    public void onExtend(MapData record, Element container) {

		container.getElementsByTagName("input").getItem(0)
			.setAttribute("value", record.getString(CcsOpOperateLog.P_RelatedKey));
		container.getElementsByTagName("input").getItem(1)
			.setAttribute("value", record.getString(CcsOpOperateLog.P_Org));
	    }

	});
	searchGrid.getSetting().detail(d);
	return searchGrid;
    }

    private CommonKylinForm addDetail(final MapData data) {
	final CommonKylinForm detailForm = new CommonKylinForm();
	detailForm.setField(uCcsOpOperateLog.RelatedKey().readonly(true), uCcsOpOperateLog.Org().readonly(true));
	Scheduler.get().scheduleFinally(new ScheduledCommand(){
	    @Override
	    public void execute() {
		detailForm.setFormData(data);
	    }
	});
		return detailForm;
    }
}