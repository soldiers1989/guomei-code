package com.sunline.ccs.param.ui.client.product;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.i18n.CustomerServiceFeeConstants;
import com.sunline.ccs.infrastructure.shared.map.CustomerServiceFeeMapHelper;
import com.sunline.ccs.infrastructure.shared.map.ProductCreditMapHelper;
import com.sunline.ccs.param.def.CustomerServiceFee;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.ark.client.utils.StringUtils;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.pcm.facility.client.common.AbstractPcmProductParamLayout;
import com.sunline.ui.core.client.common.Editor;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.ListData;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.core.client.enums.EditorType;
import com.sunline.ui.core.client.util.DataUtil;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.GridEditParamEntity;
import com.sunline.ui.grid.client.listener.IBeforeEditListener;

@Singleton
public class CustomerServiceFeeLayout extends AbstractPcmProductParamLayout
{
    public static final String PRRAM_ID = "customer_cervice_fee";
    
    @Inject
    private CustomerServiceFeeConstants customerServiceFeeConstants;
    
    @Inject
    private ProductCreditConstants constants;
    
    private KylinGrid grid;
    
    // 客户代码框
    private TextColumnHelper customerCdText;
    
    // 费用框
    private TextColumnHelper feeText;
    
    // 交易代码下拉框
    private TextColumnHelper txnItem;
    
    private HashMap<String, String> enumMap;
    
    private ListData enumListData;
    
    public String getLayoutTitle()
    {
        return constants.messageTitle();
    }
    
    public void createCanvas(VerticalPanel panel)
    {
        panel.setWidth("100%");
        grid = new KylinGrid();
        grid.setWidth("99%");
        grid.setHeight("100%");
        grid.getSetting().enabledEdit(true);
        grid.getSetting().usePager(false);
        grid.setHeader(ClientUtils.createAddItem(new IClickEventListener()
        {
            @Override
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
        customerCdText = new TextColumnHelper("customerCd", constants.customerCd(), 150);
        feeText = new TextColumnHelper("fee", customerServiceFeeConstants.fee(), 150);
        txnItem = new TextColumnHelper("txnCd", customerServiceFeeConstants.txnCd(), 150);
        
        enumMap = new HashMap<String, String>();
        grid.setColumns(/*customerCdText.setColumnWidth(150).setLength(5)
                        .setColunmEditor(new Editor().type(EditorType.TEXT)),
                        txnItem.setColumnWidth(150).setColunmEditor(new Editor().type(EditorType.SELECT)
                        .data(result.asListData().toString())),
                        feeText.setColumnWidth(150).setColunmEditor(new Editor().type(EditorType.NUMBER))*/
        customerCdText.setColumnWidth(150).setLength(5).setColunmEditor(new Editor().type(EditorType.TEXT)),
            txnItem.setColumnWidth(150).setColunmEditor(new Editor().type(EditorType.SELECT)).columnRender(enumMap),
            feeText.setColumnWidth(150).setColunmEditor(new Editor().type(EditorType.NUMBER)));
        // 编辑前事件，根据类型初始化值域下拉框的值
        grid.getSetting().onBeforeEdit(new IBeforeEditListener()
        {
            
            @Override
            public boolean onBeforeEdit(GridEditParamEntity editParm)
            {
                MapData columnData = editParm.getColumn();
                if ("txnCd".equals(editParm.getColumnName()))
                {
                    MapData editor = columnData.getData("editor").asMapData();
                    if (enumListData == null)
                    {
                        enumListData = new ListData();
                    }
                    editor.put("data", enumListData);
                }
                return true;
            }
        });
        
        panel.add(grid);
    }
    
    @SuppressWarnings("unchecked")
    public void updateView(Data data)
    {
        grid.loadData(string2Data("[]"));
        
        RPC.ajax("rpc/ccsSelectOptionServer/getTxnCd", new RpcCallback<Data>()
        {
            
            @Override
            public void onSuccess(Data result)
            {
                MapData map = new MapData();
                ListData data = result.asListData();
                if (enumListData == null)
                {
                    enumListData = new ListData();
                }
                for (int i = 0; i < data.size(); i++)
                {
                    map.put(data.get(i).asMapData().getString("id"), data.get(i).asMapData().getString("text"));
                    enumListData.add(data.get(i));
                }
                enumMap.putAll((HashMap<String, String>)map.toMap());
                
            }
        });
        
        ProductCredit model = new ProductCredit();
        ProductCreditMapHelper.updateFromMap(model, (Map<String, Serializable>)data.asMapData().toMap());
        
        if (data.asMapData().getData("customerServiceFee") != null)
        {
            model.customerServiceFee =
                (Map<String, CustomerServiceFee>)data.asMapData().getData("customerServiceFee").asMapData().toMap();
        }
        
        if (model.customerServiceFee != null)
        {
            Map<String, CustomerServiceFee> customerServiceFeeMap = model.customerServiceFee;
            
            StringBuilder strBuf = new StringBuilder("[");
            
            Iterator<Entry<String, CustomerServiceFee>> iter = customerServiceFeeMap.entrySet().iterator();
            
            while (iter.hasNext())
            {
                Entry<String, CustomerServiceFee> entry = (Entry<String, CustomerServiceFee>)iter.next();
                
                CustomerServiceFee customer = new CustomerServiceFee();
                CustomerServiceFeeMapHelper.updateFromMap(customer, (Map<String, Serializable>)entry.getValue());
                
                strBuf.append("{\"customerCd\":\"" + String.valueOf(entry.getKey()) + "\",");
                strBuf.append("\"txnCd\":\"" + customer.txnCd + "\",");
                
                if (iter.hasNext())
                {
                    strBuf.append("\"fee\":\"" + customer.fee + "\"},");
                }
                else
                {
                    strBuf.append("\"fee\":\"" + customer.fee + "\"}");
                }
            }
            
            strBuf.append("]");
            
            grid.loadData(string2Data(strBuf.toString()));
        }
    }
    
    public MapData getFormValues()
    {
        MapData submitData = new MapData();
        MapData gridData = new MapData();
        grid.getUi().endEdit();
        ListData gridList = grid.getData().asListData();
        
        if (gridList != null && gridList.toList().size() > 0)
        {
            for (int i = 0; i < gridList.toList().size(); i++)
            {
                MapData custServiceFee = new MapData();
                
                gridList.get(i).asMapData().getJsData();
                
                custServiceFee.put("txnCd", gridList.get(i).asMapData().getString("txnCd"));
                String fee = gridList.get(i).asMapData().getString("fee");
                if (StringUtils.isEmpty(fee) || "null".equals(fee))
                {
                    fee = "0";
                }
                custServiceFee.put("fee", fee);
                
                gridData.put(String.valueOf(gridList.get(i).asMapData().toMap().get("customerCd")), custServiceFee);
            }
        }
        
        submitData.put("customerServiceFee", gridData);
        
        return submitData;
    }
    
    private Data string2Data(String str)
    {
        Data data = new Data();
        data.setJsData(DataUtil.convertDataType(str));
        
        return data;
    }
    
    /**
     * 重载方法
     * 
     * @return
     */
    @Override
    public boolean validForm()
    {
        return true;
    }
    
    /**
     * 重载方法
     * 
     * @return
     */
    @Override
    public String getParamID()
    {
        return PRRAM_ID;
    }
    
    /**
     * 重载方法
     * 
     * @return
     */
    @Override
    public String getTabTitle()
    {
        return constants.customerServiceFeeInfo();
    }
    
    /**
     * 重载方法
     * 
     * @return
     */
    @Override
    public int getTabIndex()
    {
        return 8;
    }
    
    /**
     * 重载方法
     * 
     * @return
     */
    @Override
    public int getBlockIndex()
    {
        return 0;
    }
    
    /**
     * 重载方法
     * 
     * @param flag
     */
    @Override
    public void editable(boolean flag)
    {
        
    }
    
    /**
     * 重载方法
     * @return
     */
    @Override
    public Class<?> getDataTypeClass()
    {
        return ProductCredit.class;
    }
}
