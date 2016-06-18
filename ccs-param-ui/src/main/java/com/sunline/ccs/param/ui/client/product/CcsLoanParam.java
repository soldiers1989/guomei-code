//package com.sunline.ccs.param.ui.client.product;
//
//import com.google.gwt.user.client.ui.Widget;
//import com.google.inject.Inject;
//import com.sunline.ccs.param.def.ProductCredit;
//import com.sunline.pcm.facility.client.common.IParameterWidget;
//import com.sunline.ui.core.client.data.Data;
//import com.sunline.ui.core.client.data.MapData;
//
//public class CcsLoanParam implements IParameterWidget
//{
//    public static final String PRRAM_ID = "ccs_loan_param";
//    
//    @Inject
//    private ProductCreditConstants constants;
//    
//    @Inject
//    private LoanParamLayout loanParamLayout;
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
//        return constants.loanParamInfo();
//    }
//    
//    @Override
//    public int getTabIndex()
//    {
//        return 10;
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
//        loanParamLayout.getForm().setFormReadOnly(!flag);
//    }
//    
//    @Override
//    public Widget getLayout()
//    {
//        return loanParamLayout.createCanvas();
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
//        loanParamLayout.updateView(data);
//    }
//    
//    @Override
//    public boolean saveCheck()
//    {
//        if (!loanParamLayout.updateModel(productCredit))
//        {
//            return false;
//        }
//        return true;
//    }
//    
//    @Override
//    public MapData getSubmitData()
//    {
//        return loanParamLayout.getFormValues();
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
//        return loanParamLayout.initLayout();
//    }
//}
