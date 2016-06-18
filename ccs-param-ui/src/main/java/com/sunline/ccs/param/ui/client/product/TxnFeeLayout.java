package com.sunline.ccs.param.ui.client.product;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UTxnCd;
import com.sunline.ccs.infrastructure.client.ui.UTxnFee;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.pcm.facility.client.common.AbstractPcmProductParamLayout;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.ListData;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.EventObjectHandler;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.IColumnRenderFunctionListener;
import com.sunline.ui.grid.client.listener.IDblClickRowEventListener;

@Singleton
public class TxnFeeLayout extends AbstractPcmProductParamLayout
{
    public static final String PRRAM_ID = "txn_fee_param";
    
    @Inject
    private ProductCreditConstants constants;
    
    @Inject
    private UTxnFee uTxnFee;
    
    @Inject
    private KylinGrid listGrid;
    
    @Inject
    private UTxnCd uTxnCd;
    
    private MapData txnFeeMapData;
    
    @Inject
    private TxnFeeInfoDialog txnFeeInfoDialog;
    
    @Inject
    private ProductCreditConstants creditConstants;
    
    private LinkedHashMap<String, String> txnCdMap;
    
    public void createCanvas(VerticalPanel panel)
    {
        panel.setWidth("100%");
        panel.setHeight("100%");
        
        listGrid = new KylinGrid();
        {
            listGrid.setWidth("100%");
            listGrid.setHeight("100%");
            listGrid.getSetting().usePager(false);
            listGrid.getSetting().columnWidth(150);
            listGrid.getSetting().groupColumnDisplay("");
            listGrid.getSetting().groupColumnName(uTxnCd.TxnCd().getName());
            listGrid.setColumns(uTxnCd.TxnCd().columnRender(new IColumnRenderFunctionListener()
            {
                @Override
                public String render(MapData rowdata, int rowindex, String value, EventObjectHandler column)
                {
                    if (txnCdMap != null)
                    {
                        return (String)txnCdMap.get(value);
                    }
                    return value;
                }
            }), uTxnFee.DespositRate(), uTxnFee.FeeTxnCd().columnRender(new IColumnRenderFunctionListener()
            {
                @Override
                public String render(MapData rowdata, int rowindex, String value, EventObjectHandler column)
                {
                    if (txnCdMap != null)
                    {
                        return (String)txnCdMap.get(value);
                    }
                    return value;
                }
            }), uTxnFee.TierInd().columnRender());
            
            listGrid.addDblClickListener(new IDblClickRowEventListener()
            {
                
                @Override
                public void onDblClickRow(MapData data, String rowid, EventObjectHandler row)
                {
                    String txnCd = data.getString(uTxnCd.TxnCd().getName());
                    String feeTxnCd = data.getString(uTxnFee.FeeTxnCd().getName());
                    txnFeeInfoDialog.setAdd(false);
                    txnFeeInfoDialog.setGrid(listGrid);
                    txnFeeInfoDialog.setTxnFeeMapData(txnFeeMapData);
                    txnFeeInfoDialog.setTxnCd(txnCd);
                    txnFeeInfoDialog.setFeeTxnCd(feeTxnCd);
                    txnFeeInfoDialog.show();
                }
            });
            
            // 刷新按钮
            listGrid.addHeader(ClientUtils.createRefreshItem(new IClickEventListener()
            {
                @Override
                public void onClick()
                {
                    listGrid.clearData();
                    listGrid.loadData(txnFeeInfoDialog.StringToData(txnFeeInfoDialog.generateGridData(txnFeeInfoDialog.mapDataToGridMapData(txnFeeMapData))));
                }
            }));
            
            // 增加按钮
            listGrid.setHeader(ClientUtils.createAddItem(new IClickEventListener()
            {
                @Override
                public void onClick()
                {
                    txnFeeInfoDialog.setAdd(true);
                    txnFeeInfoDialog.setGrid(listGrid);
                    txnFeeInfoDialog.setTxnFeeMapData(txnFeeMapData);
                    txnFeeInfoDialog.show();
                }
            }));
            
            // 删除按钮
            listGrid.setHeader(ClientUtils.createDeleteItem(new IClickEventListener()
            {
                @Override
                public void onClick()
                {
                    if (listGrid.getGrid().getSelectedRows().size() < 1)
                    {
                        Dialog.alert(creditConstants.pleaseChoose());
                    }
                    else
                    {
                        ListData listData = listGrid.getGrid().getSelectedRows();
                        for (int i = 0; i < listData.size(); i++)
                        {
                            // 删除子
                            // 父map是否为空，是则删除
                            String selectTxncd = listData.get(i).asMapData().getString(uTxnCd.TxnCd().getName());
                            
                            String feeTxnCd = listData.get(i).asMapData().getString(uTxnFee.FeeTxnCd().getName());
                            
                            txnFeeMapData.getData(selectTxncd).asMapData().remove(feeTxnCd);
                            if (txnFeeMapData.getData(selectTxncd).asMapData().toMap().size() == 0)
                            {
                                txnFeeMapData.remove(selectTxncd);
                            }
                        }
                        listGrid.loadData(txnFeeInfoDialog.StringToData(txnFeeInfoDialog.generateGridData(txnFeeInfoDialog.mapDataToGridMapData(txnFeeMapData))));
                    }
                }
            }));
            
        }
        panel.add(listGrid);
    }
    
    public void updateView(Data data)
    {
        if (txnFeeMapData == null)
        {
            txnFeeMapData = new MapData();
        }
        
        if (data.asMapData().getData("txnFeeList") != null)
        {
            initTxnFeeMapData(data.asMapData().getData("txnFeeList").asMapData());
            
            listGrid.clearData();
            listGrid.loadData(txnFeeInfoDialog.StringToData(txnFeeInfoDialog.generateGridData(data.asMapData()
                .getData("txnFeeList")
                .asMapData())));
        }
        
        if (txnCdMap == null)
        {
            txnCdMap = new LinkedHashMap<String, String>();
            
            RPC.ajax("rpc/ccsSelectOptionServer/getTxnCd", new RpcCallback<Data>()
            {
                @SuppressWarnings("unchecked")
                @Override
                public void onSuccess(Data result)
                {
                    List<Map<String, String>> itemList = (List<Map<String, String>>)result.asListData().toList();
                    
                    if (itemList.size() > 0)
                    {
                        for (int i = 0; i < itemList.size(); i++)
                        {
                            Map<String, String> itemMap = itemList.get(i);
                            
                            txnCdMap.put(itemMap.get("id"), itemMap.get("text"));
                        }
                    }
                }
            });
        }
    }
    
    @SuppressWarnings("unchecked")
    private void initTxnFeeMapData(MapData mapData)
    {
        Map<String, Serializable> dataMap = (Map<String, Serializable>)mapData.toMap();
        txnFeeMapData = new MapData();
        for (Entry<String, Serializable> entry : dataMap.entrySet())
        {
            String txnCd = entry.getKey();
            ListData feeListData = mapData.getData(txnCd).asListData();
            MapData subTxnFeeMapData = new MapData();
            for (int i = 0; i < feeListData.size(); i++)
            {
                Data txnFee = feeListData.get(i);
                subTxnFeeMapData.put(txnFee.asMapData().getString(uTxnFee.FeeTxnCd().getName()), txnFee);
            }
            txnFeeMapData.put(txnCd, subTxnFeeMapData);
        }
    }
    
    public MapData mapDataToGridMapData()
    {
        return txnFeeInfoDialog.mapDataToGridMapData(txnFeeMapData);
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
    public MapData getFormValues()
    {
        MapData submitMapData = new MapData();
        submitMapData.put("txnFeeList", mapDataToGridMapData());
        return submitMapData;
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
        return constants.txnFee();
    }
    
    /**
     * 重载方法
     * 
     * @return
     */
    @Override
    public int getTabIndex()
    {
        return 12;
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
     * 
     * @return
     */
    @Override
    public Class<?> getDataTypeClass()
    {
        return ProductCredit.class;
    }
    
}
