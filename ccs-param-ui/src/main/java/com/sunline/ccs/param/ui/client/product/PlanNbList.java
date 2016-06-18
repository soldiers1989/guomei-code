//package com.sunline.ccs.param.ui.client.product;
//
//import com.google.gwt.user.client.ui.Widget;
//import com.google.inject.Inject;
//import com.sunline.ccs.param.def.ProductCredit;
//import com.sunline.pcm.facility.client.common.IParameterWidget;
//import com.sunline.ui.core.client.data.Data;
//import com.sunline.ui.core.client.data.MapData;
//
//public class PlanNbList implements IParameterWidget
//{
//    
//    public static final String PRRAM_ID = "plannb_list";
//    
//    @Inject
//    private PlanNbListLayout planNbListLayout;
//    
//    private ProductCredit productCredit;
//    
//    @Inject
//    private ProductCreditConstants constants;
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
//        return constants.planNbrList();
//    }
//    
//    @Override
//    public int getTabIndex()
//    {
//        return 11;
//    }
//    
//    @Override
//    public int getBlockIndex()
//    {
//        return 0;
//    }
//    
//    @Override
//    public Widget getLayout()
//    {
//        return planNbListLayout.createCanvas();
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
//        planNbListLayout.updateView(data);
//    }
//    
//    @Override
//    public void editable(boolean flag)
//    {
//        planNbListLayout.getForm().setFormReadOnly(!flag);
//    }
//    
//    @Override
//    public boolean saveCheck()
//    {
//        if (!planNbListLayout.validForm())
//        {
//            return false;
//        }
//        return true;
//    }
//    
//    @Override
//    public MapData getSubmitData()
//    {
//        return planNbListLayout.getFormValues();
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
//        return planNbListLayout.initLayout();
//    }
//}
