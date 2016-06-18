package com.sunline.ccs.param.ui.client.util;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.sunline.ccs.infrastructure.client.ui.URateDef;
import com.sunline.ccs.infrastructure.client.ui.i18n.RateDefConstants;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.ui.core.client.common.Editor;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.ListData;
import com.sunline.ui.core.client.enums.EditorType;
import com.sunline.ui.core.client.util.DataUtil;
import com.sunline.ui.event.client.IClickEventListener;

/**
 * 利率列表布局，在超限费、滞纳金页面使用
 * 
 * @author
 * @version [版本号, Jul 13, 2015]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class RatesLayout
{
    @Inject
    private RateDefConstants constants;
    
    @Inject
    private URateDef uRateDef;
    
    private TextColumnHelper rate;
    
    private TextColumnHelper rateCeil;
    
    private TextColumnHelper rateBase;
    
    private KylinGrid grid;
    
    public Widget createCanvas()
    {
        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        panel.setHeight("100%");
        
        grid = new KylinGrid();
        grid.setWidth(430);
        grid.setHeight("80%");
        grid.setHeader(ClientUtils.createAddItem(new IClickEventListener()
        {
            public void onClick()
            {
                grid.getUi().addEditRow();
            }
        }), ClientUtils.createDeleteItem(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
                grid.getUi().deleteSelectedRow();
            }
        }));
        
        rate = new TextColumnHelper(uRateDef.Rate().getName(), constants.rate(), 120);
        rateCeil = new TextColumnHelper(uRateDef.RateCeil().getName(), constants.rateCeil(), 120);
        rateBase = new TextColumnHelper(uRateDef.RateBase().getName(), constants.rateBase(), 120);
        
        grid.setColumns(rate.setColumnWidth(130).setColunmEditor(new Editor().type(EditorType.NUMBER)),
        
        rateCeil.setColumnWidth(130).setColunmEditor(new Editor().type(EditorType.NUMBER)),
        
        rateBase.setColumnWidth(138).setColunmEditor(new Editor().type(EditorType.NUMBER)));
        
        grid.getSetting().usePager(false);
        grid.getSetting().enabledEdit(true);
        
        panel.add(grid);
        
        return panel;
    }
    
    public void setValue(Data data)
    {
        grid.loadData(data);
    }
    
    public ListData getValue()
    {
    	grid.getUi().endEdit();
        return grid.getGrid().getData();
    }
    
    public KylinGrid getGrid()
    {
        return grid;
    }
    
    public void clearValues()
    {
        Data data = new Data();
        data.setJsData(DataUtil.convertDataType("[]"));
        grid.loadData(data);
    }
    
    public Data StringToData(String gridDataString)
    {
        Data data = new Data();
        data.setJsData(DataUtil.convertDataType(gridDataString));
        return data;
    }
}
