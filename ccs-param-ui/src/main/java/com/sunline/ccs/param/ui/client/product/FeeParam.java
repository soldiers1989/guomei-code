//package com.sunline.ccs.param.ui.client.product;
//
//import java.util.List;
//
//import com.google.gwt.user.client.ui.Widget;
//import com.google.inject.Inject;
//import com.sunline.ccs.param.def.ProductCredit;
//import com.sunline.kylin.web.ark.client.ui.KylinForm;
//import com.sunline.pcm.facility.client.common.IParameterWidget;
//import com.sunline.ui.core.client.data.Data;
//import com.sunline.ui.core.client.data.MapData;
//
//public class FeeParam implements IParameterWidget
//{
//    
//    public static final String PRRAM_ID = "ccs_fee_param";
//    
//    @Inject
//    private FeeLayout feeLayout;
//    
//    @Inject
//    private ProductCreditConstants constants;
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
//        return constants.productFeeInfo();
//    }
//    
//    @Override
//    public int getTabIndex()
//    {
//        return 6;
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
//        List<KylinForm> forms = feeLayout.getForms();
//        for (KylinForm form : forms)
//        {
//            form.setFormReadOnly(!flag);
//        }
//    }
//    
//    @Override
//    public Widget getLayout()
//    {
//        return feeLayout.createCanvas();
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
//        feeLayout.updateView(data);
//    }
//    
//    @Override
//    public boolean saveCheck()
//    {
//        if (!feeLayout.updateModel(productCredit))
//        {
//            return false;
//        }
//        if (!feeLayout.isCreated())
//        {
//            return false;
//        }
//        return true;
//    }
//    
//    @Override
//    public MapData getSubmitData()
//    {
//        return feeLayout.getFormValues();
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
//        return feeLayout.initLayout();
//    }
//    
//}
