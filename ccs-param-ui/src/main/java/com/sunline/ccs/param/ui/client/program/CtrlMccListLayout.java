package com.sunline.ccs.param.ui.client.program;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.res.SavePage;
import com.sunline.ui.core.client.common.Editor;
import com.sunline.ui.core.client.data.ListData;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.core.client.enums.EditorType;
import com.sunline.ui.event.client.IClickEventListener;
/**
 * 分期活动:MCC列表页面
 * 
 * @author lisy
 * @version [版本号, Jun 26, 2015]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
@Singleton
public class CtrlMccListLayout extends SavePage {

    @Inject
    private ProgramConstants constants;

    private TextColumnHelper mccSelect;
    private KylinGrid grid;

    @Override
    public IsWidget createPage() {
	final VerticalPanel panel = new VerticalPanel();

	panel.setWidth("100%");
	grid = new KylinGrid();
	grid.getSetting().enabledEdit(true);

	grid.setHeader(ClientUtils.createAddItem(new IClickEventListener(){
	    @Override
	    public void onClick() {
		grid.getUi().addEditRow();
	    }
	}), ClientUtils.createDeleteItem(new IClickEventListener(){
	    @Override
	    public void onClick() {
		grid.getUi().deleteSelectedRow();
	    }
	}));

	// 记录显示网格
	grid.setWidth("100%");
	grid.setHeight("100%");
	// 设置禁止分页
	grid.getSetting().usePager(false);

	mccSelect = new TextColumnHelper("MccId", constants.ctrlMcc(), 400);
	grid.setColumns(mccSelect.setColumnWidth(400).setColunmEditor(new Editor().type(EditorType.TEXT)));
	panel.add(grid);
	return panel;
    }

    @Override
    public void refresh() {
	// form.getUi().clear();

    }

    public void updataView(ListData listData) {
	grid.clearData();
	ListData gridData = new ListData();
	
	for(int i = 0; i < listData.toList().size(); i++)
	{
	    MapData dataMap = new MapData();
	    dataMap.put("MccId", listData.getString(i));
	    gridData.add(dataMap);
	}
	
	grid.loadData(gridData);
    }
    public MapData sumbitData() {
    	grid.getUi().endEdit();
	ListData target=new ListData();
	ListData ld = grid.getData();
	for (int i=0; i < ld.size(); i++) {
	    if(ld.get(i).asMapData().getString("MccId") !=null&&
		    !ld.get(i).asMapData().getString("MccId").equals("")){
	    target.addString(ld.get(i).asMapData().getString("MccId"));
	    }
	}
	MapData md=new MapData();
	md.put("ctrlMccList",target.asMapData());
	return md;
    }
}