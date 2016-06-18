package com.sunline.ccs.param.ui.client.product;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.sunline.ccs.infrastructure.client.ui.URateDef;
import com.sunline.ccs.infrastructure.client.ui.UTxnCd;
import com.sunline.ccs.infrastructure.client.ui.UTxnFee;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinDialog;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
//import com.sunline.kylin.web.code.client.common.CodeSelectOptionUtils;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ui.core.client.common.Editor;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.ListData;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.core.client.enums.EditorType;
import com.sunline.ui.core.client.util.DataUtil;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.IClickEventListener;

/**
 * 交易费用对话框
 * 
 * @author
 * @version [版本号, Jul 13, 2015]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class TxnFeeInfoDialog extends KylinDialog
{
    
    /**
     * 判断是否在添加模式
     */
    private boolean add = false;
    
    private KylinForm detailForm;
    
    private KylinGrid listGrid;
    
    private MapData txnFeeMapData;
    
    private String txnCd;
    
    private String feeTxnCd;
    
    @Inject
    private ProductCreditConstants constants;
    
    @Inject
    private UTxnFee uTxnFee;
    
    @Inject
    private UTxnCd uTxnCd;
    
    private KylinGrid ratesGrid;
    
    @Inject
    private URateDef uRateDef;
    
    @Override
    protected Widget createContent()
    {
        VerticalPanel layout = new VerticalPanel();
        setWidth(800);
        setHeight(400);
        setTitle(constants.addTxnFee());
        detailForm = new KylinForm();
        
//        CodeSelectOptionUtils.initCodeSelectItem(detailForm,
//            uTxnCd.TxnCd().getName(),
//            uTxnFee.FeeTxnCd().getName(),
//            SelectType.KEY_LABLE);
        
        {
            detailForm.setCol(4);
            detailForm.setField(uTxnCd.TxnCd().asSelectItem(),
                uTxnFee.FeeTxnCd().asSelectItem(),
                uTxnFee.DespositWaiveInd().asSelectItem(SelectType.KEY_LABLE).setNewline(true),
                uTxnFee.DespositRate(),
                uTxnFee.MinDespositWaiveFee().setNewline(true),
                uTxnFee.MaxDespositWaiveFee(),
                uTxnFee.DespositBaseFee().setNewline(true).setNewline(true),
                uTxnFee.TierInd().asSelectItem(SelectType.KEY_LABLE).setNewline(true),
                uTxnFee.ChargeWaiveInd().asSelectItem(SelectType.KEY_LABLE),
                uTxnFee.MinChargeWaiveFee().setNewline(true),
                uTxnFee.MaxChargeWaiveFee());
            
            detailForm.getSetting().labelWidth(150).labelAlign("right");
        }
        
        ratesGrid = new KylinGrid();
        ratesGrid.getSetting().usePager(false);
        
        ratesGrid.getSetting().enabledEdit(true);
        
        ratesGrid.setColumns(uRateDef.Rate().setColumnWidth(120).setColunmEditor(new Editor().type(EditorType.TEXT)),
            uRateDef.RateBase().setColumnWidth(120).setColunmEditor(new Editor().type(EditorType.TEXT)),
            uRateDef.RateCeil().setColumnWidth(120).setColunmEditor(new Editor().type(EditorType.TEXT)));
        ratesGrid.setHeader(ClientUtils.createAddItem(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
                ratesGrid.getUi().addEditRow();
            }
        }), ClientUtils.createDeleteItem(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
                ratesGrid.getUi().deleteSelectedRow();
            }
        }));
        
        layout.add(detailForm);
        layout.add(ratesGrid);
        
        addConfirmButton(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
                // 1. 获取所有表单的值，保存到LoanFeeDef对象里边
                // 2. 并放到一个Map<int, LoanFeeDef>对象里边 key为LoanNum
                // 3. 重新刷新列表数据（需要转格式（对象中的值拼装，再加上一个map的key）
                
                MapData detailFormData = new MapData();
                MapData txnFeeDefData = new MapData();
                if (detailForm.valid())
                {
                    detailFormData = detailForm.getSubmitData().asMapData();
                    txnFeeDefData = detailFormData;
                }
                ratesGrid.getUi().endEdit();
                ListData rateListData = ratesGrid.getGrid().getData();
                
                txnFeeDefData.put("chargeRates", rateListData);
                
                if (isAdd())
                {
                    Data subData = txnFeeMapData.getData(txnFeeDefData.getString(uTxnCd.TxnCd().getName()));
                    MapData subMapData;
                    if (subData != null && !"".equals(subData))
                    {
                        Data txnFeeData =
                            subData.asMapData().getData(txnFeeDefData.getString(uTxnFee.FeeTxnCd().getName()));
                        if (txnFeeData != null && !"".equals(txnFeeData))
                        {
                            Dialog.alert("该费用信息已定义");
                            return;
                        }
                        else
                        {
                            subMapData = subData.asMapData();
                        }
                    }
                    else
                    {
                        subMapData = new MapData();
                    }
                    subMapData.put(txnFeeDefData.getString(uTxnFee.FeeTxnCd().getName()), txnFeeDefData);
                    txnFeeMapData.put(txnFeeDefData.getString(uTxnCd.TxnCd().getName()), subMapData);
                }
                else
                {
                    MapData subMapData =
                        txnFeeMapData.getData(txnFeeDefData.getString(uTxnCd.TxnCd().getName())).asMapData();
                    subMapData.put(txnFeeDefData.getString(uTxnFee.FeeTxnCd().getName()), txnFeeDefData);
                    txnFeeMapData.put(txnFeeDefData.getString(uTxnCd.TxnCd().getName()), subMapData);
                }
                
                // 重新拼装数据，刷新列表
                // 列表双击事件在列表进行父页面进行定义
                // 事件双击存储的LoanFeeDef信息存放在loanFeeDefMap，父页面通过getLoanFeeDef获取
                listGrid.loadData(StringToData(generateGridData(mapDataToGridMapData(txnFeeMapData))));
                detailForm.getUi().clear();
                hide();
            }
        });
        addCancelButton(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
                updateView();
                hide();
            }
        });
        return layout;
    }
    
    @Override
    protected void updateView()
    {
        detailForm.getUi().clear();
        detailForm.setFieldReadOnly(uTxnCd.TxnCd().getName(), true);
        detailForm.setFieldReadOnly(uTxnFee.FeeTxnCd().getName(), true);
        ratesGrid.loadData(StringToData("[]"));
        if (!isAdd())
        {
            Data formData = txnFeeMapData.getData(txnCd);
            detailForm.setFieldValue(uTxnCd.TxnCd().getName(), txnCd);
            detailForm.setFieldReadOnly(uTxnFee.FeeTxnCd().getName(), false);
            detailForm.setFieldReadOnly(uTxnCd.TxnCd().getName(), false);
            detailForm.setFormData(formData.asMapData().getData(feeTxnCd));
            Data rateListData = formData.asMapData().getData(feeTxnCd).asMapData().getData("chargeRates");
            ratesGrid.loadData(rateListData);
        }
        RPC.ajax("rpc/ccsSelectOptionServer/getTxnCd", new RpcCallback<Data>()
        {
            @Override
            public void onSuccess(Data result)
            {
                if (result != null)
                {
                    SelectItem<String> columnitem = new SelectItem<String>();
                    columnitem.setValue(result.asListData());
                    columnitem.setSelectType(SelectType.LABLE);
                    detailForm.setFieldSelectData(uTxnCd.TxnCd().getName(), columnitem);
                    detailForm.setFieldSelectData(uTxnFee.FeeTxnCd().getName(), columnitem);
                }
                
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    public String generateGridData(MapData mapData)
    {
        Map<String, Serializable> dataMap = (Map<String, Serializable>)mapData.toMap();
        StringBuffer sb = new StringBuffer("[");
        int index = 0;
        for (Entry<String, Serializable> entry : dataMap.entrySet())
        {
            String txnCd = entry.getKey();
            ListData feeListData = mapData.getData(txnCd).asListData();
            index = index + 1;
            int subIndex = 0;
            for (int i = 0; i < feeListData.size(); i++)
            {
                Data txnFee = feeListData.get(i);
                subIndex = subIndex + 1;
                sb.append("{");
                sb.append("\"" + uTxnCd.TxnCd().getName() + "\":\"" + txnCd + "\",");
                sb.append("\"" + uTxnFee.DespositRate().getName() + "\":\""
                    + txnFee.asMapData().getString(uTxnFee.DespositRate().getName()) + "\",");
                sb.append("\"" + uTxnFee.FeeTxnCd().getName() + "\":\""
                    + txnFee.asMapData().getString(uTxnFee.FeeTxnCd().getName()) + "\",");
                sb.append("\"" + uTxnFee.TierInd().getName() + "\":\""
                    + txnFee.asMapData().getString(uTxnFee.TierInd().getName()) + "\"");
                if (subIndex <= feeListData.size() - 1)
                {
                    sb.append("},");
                }
                else
                {
                    sb.append("}");
                }
            }
            if (index <= dataMap.size() - 1)
            {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
    
    @SuppressWarnings("unchecked")
    public MapData mapDataToGridMapData(MapData mapData)
    {
        MapData gridMapData = new MapData();
        
        Map<String, Serializable> dataMap = (Map<String, Serializable>)mapData.toMap();
        for (Entry<String, Serializable> entry : dataMap.entrySet())
        {
            String txnCdKey = entry.getKey();
            MapData feeMapData = mapData.getData(txnCdKey).asMapData();
            Map<String, Serializable> feeMap = (Map<String, Serializable>)feeMapData.toMap();
            ListData feeListData = new ListData();
            for (Entry<String, Serializable> entryFee : feeMap.entrySet())
            {
                String feeTxnCdKey = entryFee.getKey();
                feeListData.add(feeMapData.getData(feeTxnCdKey));
            }
            gridMapData.put(txnCdKey, feeListData);
        }
        return gridMapData;
    }
    
    public Data StringToData(String gridDataString)
    {
        Data data = new Data();
        data.setJsData(DataUtil.convertDataType(gridDataString));
        return data;
    }
    
    public boolean isAdd()
    {
        return add;
    }
    
    public void setAdd(boolean add)
    {
        this.add = add;
    }
    
    public void setGrid(KylinGrid listGrid)
    {
        this.listGrid = listGrid;
    }
    
    public void setTxnFeeMapData(MapData txnFeeMapData)
    {
        this.txnFeeMapData = txnFeeMapData;
    }
    
    public void setTxnCd(String key)
    {
        this.txnCd = key;
    }
    
    public void setFeeTxnCd(String feeTxnCd)
    {
        this.feeTxnCd = feeTxnCd;
    }
}
