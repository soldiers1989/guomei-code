package com.sunline.ccs.ui.client.pages.optloggingconf;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RenderableStamper;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsOpOperateLog;
import com.sunline.ccs.infrastructure.shared.model.CcsOpOperateLog;
import com.sunline.ccs.ui.client.commons.CommonKylinForm;
import com.sunline.ccs.ui.client.commons.CommonKylinGrid;
import com.sunline.ccs.ui.client.commons.CommonUiUtils;
import com.sunline.ccs.ui.client.commons.CommonVerticalPanel;
import com.sunline.kylin.web.ark.client.helper.DateColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.ui.core.client.common.Field;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.Detail;
import com.sunline.ui.grid.client.listener.IShowDetailEventListener;

@Singleton
public class OperationLoggingConfig extends Page {

    @Inject
    private OperationLoggingConfigConstants constants;
    @Inject
    private UCcsOpOperateLog uCcsOpOperateLog;

    private VerticalPanel mainWindow;
    private VerticalPanel dividePanel;
    private KylinForm serachForm;
    private CommonKylinGrid operateLogGrid;
    private CommonKylinGrid detailGrid;
    DateColumnHelper labelBeginDate = new DateColumnHelper("beginDate", "开始日期", true, false);
    DateColumnHelper labelEndDate = new DateColumnHelper("endDate", "结束日期", true, false);

    /*
     * (non-Javadoc)
     * 
     * @see com.sunline.kylin.web.core.client.res.Page#createPage()
     */
    @Override
    public IsWidget createPage() {
	// 主操作界面整体布局(垂直布局)
	mainWindow = new VerticalPanel();
	mainWindow.setHeight("100%");
	mainWindow.setWidth("98%");
	// 布局
	dividePanel = new VerticalPanel();
	dividePanel.setWidth("100%");
	// 实例化提交查询按钮
	// 实例化kylinweb表单
	serachForm = new KylinForm();
	serachForm.setField(uCcsOpOperateLog.CardNbr(), labelBeginDate.required(true), labelEndDate);
	KylinButton btnSearch = new KylinButton("查询",null);
	btnSearch.addClickEventListener(new OperateLogHandler(serachForm));
	HorizontalPanel hPanel = CommonUiUtils.lineLayoutForm(serachForm, btnSearch, null, null);
	// 初始化Grid
	operateLogGrid = new CommonKylinGrid();
	operateLogGrid.checkbox(false);
	operateLogGrid.setColumns(uCcsOpOperateLog.OpTime(), uCcsOpOperateLog.OpId(), uCcsOpOperateLog.CardNbr(),
				  uCcsOpOperateLog.CustId(), uCcsOpOperateLog.ServiceCode(),
				  uCcsOpOperateLog.RelatedDesc());
	// 增加明细表
	Detail detail = new Detail();
	detail.setHeight("100%").onShowDetail(new IShowDetailEventListener(){
	    @Override
	    public Widget onShowDetail(MapData data) {
		// return detailGrid;
		return addDetail(data);
	    }
	});
	operateLogGrid.getSetting().detail(detail);
	dividePanel.add(hPanel);
	dividePanel.add(operateLogGrid);
	mainWindow.add(dividePanel);
	return mainWindow;
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

    @Override
    public void refresh() {
	serachForm.getUi().clear();
	operateLogGrid.clearData();
    }

    class OperateLogHandler implements IClickEventListener {

	KylinForm _form;

	public OperateLogHandler(KylinForm form) {
	    this._form = form;
	}

	@Override
	public void onClick() {
	    if (_form.valid()) {
		    MapData submitMapData = _form.getSubmitData().asMapData();
		    for (Field field : _form.getFields()) {
			operateLogGrid.getUi().removeParm(field.getName());
		    }
		    if(submitMapData.getString("beginDate") != null){
			operateLogGrid.getUi().setParm("beginDate",submitMapData.getString("beginDate"));
		    }
		    if (submitMapData.getString("endDate") != null) {
			operateLogGrid.getUi().setParm("endDate", submitMapData.getString("endDate"));
		    }
		    if (submitMapData.getString(CcsOpOperateLog.P_CardNbr) != null) {
			operateLogGrid.getUi().setParm(CcsOpOperateLog.P_CardNbr, submitMapData.getString(CcsOpOperateLog.P_CardNbr));
		    }
		    operateLogGrid.loadDataFromUrl("rpc/queryOperationLogServer/getAllUserLogList");
	    }
	}
    }
}