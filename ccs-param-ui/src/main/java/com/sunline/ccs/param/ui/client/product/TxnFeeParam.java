//package com.sunline.ccs.param.ui.client.product;
//
//import com.google.gwt.user.client.ui.Widget;
//import com.google.inject.Inject;
//import com.sunline.ccs.param.def.ProductCredit;
//import com.sunline.pcm.facility.client.common.IParameterWidget;
//import com.sunline.ui.core.client.data.Data;
//import com.sunline.ui.core.client.data.MapData;
//
//public class TxnFeeParam implements IParameterWidget
//{
//    
//    public static final String PRRAM_ID = "txn_fee_param";
//    
//    @Inject
//    private ProductCreditConstants constants;
//    
//    @Inject
//    private TxnFeeLayout txnFeeLayout;
//    
//    private ProductCredit productCredit;
//    
//    @Override
//    public String getParamID()
//    {
//        return PRRAM_ID;
//    }
//    
//    @Override
//    public String getTabTitle()
//    {
//        return constants.txnFee();
//    }
//    
//    @Override
//    public int getTabIndex()
//    {
//        return 12;
//    }
//    
//    @Override
//    public int getBlockIndex()
//    {
//        return 0;
//    }
//    
//    @Override
//    public void editable(boolean flag)
//    {
//        /*List<KylinForm> forms = productCreditLayout.getForms();
//        for(KylinForm form : forms) {
//        	form.setFormReadOnly(!flag);
//        }*/
//    }
//    
//    @Override
//    public Widget getLayout()
//    {
//        return txnFeeLayout.createCanvas();
//    }
//    
//    @Override
//    public String getDataType()
//    {
//        if (productCredit == null)
//        {
//            productCredit = new ProductCredit();
//        }
//        return productCredit.getClass().getName().toString();
//    }
//    
//    @Override
//    public void refresh(Data data)
//    {
//        txnFeeLayout.updateView(data);
//    }
//    
//    @Override
//    public boolean saveCheck()
//    {
//        /*if(!productCreditLayout.updateModel(productCredit)) {
//        	return false;
//        }*/
//        return true;
//    }
//    
//    @Override
//    public MapData getSubmitData()
//    {
//        MapData submitMapData = new MapData();
//        submitMapData.put("txnFeeList", txnFeeLayout.mapDataToGridMapData());
//        return submitMapData;
//    }
//    
//    /**
//     * 重载方法
//     * 
//     * @return
//     */
//    @Override
//    public Widget initTabItem()
//    {
//        return txnFeeLayout.initLayout();
//    }
//}
