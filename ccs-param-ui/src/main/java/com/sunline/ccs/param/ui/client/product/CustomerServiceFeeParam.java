///**
// * 
// */
//package com.sunline.ccs.param.ui.client.product;
//
//import com.google.gwt.user.client.ui.Widget;
//import com.google.inject.Inject;
//import com.sunline.ccs.param.def.ProductCredit;
//import com.sunline.pcm.facility.client.common.IParameterWidget;
//import com.sunline.ui.core.client.data.Data;
//import com.sunline.ui.core.client.data.MapData;
//
///**
// * @author Liming.Feng
// *
// */
//public class CustomerServiceFeeParam implements IParameterWidget
//{
//    public static final String PRRAM_ID = "customer_cervice_fee";
//    
//    @Inject
//    private ProductCreditConstants constants;
//    
//    @Inject
//    private CustomerServiceFeeLayout customerServiceFeeLayout;
//    
//    private ProductCredit productCredit;
//    
//    @Override
//    public String getParamID()
//    {
//        return PRRAM_ID;
//    }
//    
//    /*
//     * (non-Javadoc)
//     * 
//     * @see
//     * com.sunline.pcm.param.common.client.organization.IParameterWidget#getTabTitle
//     * ()
//     */
//    @Override
//    public String getTabTitle()
//    {
//        return constants.customerServiceFeeInfo();
//        
//    }
//    
//    /*
//     * (non-Javadoc)
//     * 
//     * @see
//     * com.sunline.pcm.param.common.client.organization.IParameterWidget#getTabIndex
//     * ()
//     */
//    @Override
//    public int getTabIndex()
//    {
//        return 8;
//    }
//    
//    /*
//     * (non-Javadoc)
//     * 
//     * @see com.sunline.pcm.param.common.client.organization.IParameterWidget#
//     * getBlockIndex()
//     */
//    @Override
//    public int getBlockIndex()
//    {
//        return 0;
//    }
//    
//    /*
//     * (non-Javadoc)
//     * 
//     * @see
//     * com.sunline.pcm.param.common.client.organization.IParameterWidget#editable
//     * (boolean)
//     */
//    @Override
//    public void editable(boolean flag)
//    {
//        /*
//         * List<KylinForm> forms = customerServiceFeeLayout.getForms(); for
//         * (KylinForm form : forms) { form.setFormReadOnly(!flag); }
//         */
//    }
//    
//    /*
//     * (non-Javadoc)
//     * 
//     * @see
//     * com.sunline.pcm.param.common.client.organization.IParameterWidget#getLayout
//     * ()
//     */
//    @Override
//    public Widget getLayout()
//    {
//        return customerServiceFeeLayout.createCanvas();
//    }
//    
//    /*
//     * (non-Javadoc)
//     * 
//     * @see
//     * com.sunline.pcm.param.common.client.organization.IParameterWidget#getDataType
//     * ()
//     */
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
//    /*
//     * (non-Javadoc)
//     * 
//     * @see
//     * com.sunline.pcm.param.common.client.organization.IParameterWidget#refresh
//     * (com.sunline.ui.core.client.data.Data)
//     */
//    
//    @Override
//    public void refresh(Data data)
//    {
//        customerServiceFeeLayout.updateView(data);
//    }
//    
//    /*
//     * (non-Javadoc)
//     * 
//     * @see
//     * com.sunline.pcm.param.common.client.organization.IParameterWidget#saveCheck
//     * ()
//     */
//    @Override
//    public boolean saveCheck()
//    {
//        if (!customerServiceFeeLayout.validForm())
//        {
//            return false;
//        }
//        return true;
//    }
//    
//    /*
//     * (non-Javadoc)
//     * 
//     * @see com.sunline.pcm.param.common.client.organization.IParameterWidget#
//     * getSubmitData()
//     */
//    @Override
//    public MapData getSubmitData()
//    {
//        return customerServiceFeeLayout.getFormValues();
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
//        return customerServiceFeeLayout.initLayout();
//    }
//}
