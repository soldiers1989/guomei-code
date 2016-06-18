package com.sunline.ccs.param.ui.client.pointplan;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.sunline.ccs.infrastructure.client.ui.i18n.TimePeriodConstants;
import com.sunline.kylin.web.ark.client.helper.DateColumnHelper;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.ui.core.client.common.Editor;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.ListData;
import com.sunline.ui.core.client.enums.EditorType;
import com.sunline.ui.core.client.util.DataUtil;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.layout.client.Layout;
//注意，这里不是singleton
@SuppressWarnings("serial")
public class TimePeriodLayout extends Layout {
	
	@Inject
	private TimePeriodConstants constants;
	
    private KylinGrid grid;
	
	protected IsWidget createCanvas() {
		VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        panel.setHeight("100%");
        
        grid = new KylinGrid();
        {
            grid.getSetting().enabledEdit(true);
            grid.setWidth(320);
            grid.getSetting().columnWidth(150);
            
            grid.setHeader(ClientUtils.createAddItem(new IClickEventListener()
            {
            	@Override
            	public void onClick()
                {
                    grid.getUi().addEditRow();
                }
            }),ClientUtils.createDeleteItem(new IClickEventListener()
            {
                @Override
            	public void onClick()
                {
                    grid.getUi().deleteSelectedRow();
                }
            }));
        
            grid.getSetting().usePager(false);
    		//标题
            DateColumnHelper startTimeText = new DateColumnHelper("startTime", constants.startTime(),true,false);
            DateColumnHelper endTimeText = new DateColumnHelper("endTime", constants.endTime(),true,false);
            grid.setColumns(startTimeText.format("yyyy-MM-dd").setColunmEditor(new Editor().type(EditorType.DATE)),
                            endTimeText.format("yyyy-MM-dd").setColunmEditor(new Editor().type(EditorType.DATE)));

        }
        panel.add(grid);
		return panel;
	}
	
	
	public void updateLayout(Data data){
		grid.loadData(data);
	}
	
	public void clearValues(){
		Data data = new Data();
		data.setJsData(DataUtil.convertDataType("[]"));
		grid.loadData(data);
	}
	
	public ListData getValue(){
		grid.getUi().endEdit();
		return grid.getUi().getData();
		
	}
}
