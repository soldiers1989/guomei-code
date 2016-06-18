//package com.sunline.ccs.param.ui.client.authproduct;
//
//import java.util.List;
//
//import com.google.gwt.user.client.ui.Widget;
//import com.google.inject.Inject;
//import com.sunline.ccs.param.def.AuthProduct;
//import com.sunline.kylin.web.ark.client.ui.KylinForm;
//import com.sunline.pcm.facility.client.common.IParameterWidget;
//import com.sunline.ui.core.client.data.Data;
//import com.sunline.ui.core.client.data.MapData;
//
//public class AuthProductParam implements IParameterWidget
//{
//    
//    public static final String PRRAM_ID = "auth_product_param";
//    
//    @Inject
//    private AuthProductConstants authProductConstants;
//    
//    @Inject
//    private AuthProductParamLayout authProductParamLayout;
//    
//    private AuthProduct authProduct;
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
//        return authProductConstants.authProductInfo();
//    }
//    
//    @Override
//    public int getTabIndex()
//    {
//        return 13;
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
//        List<KylinForm> forms = authProductParamLayout.getForms();
//        for (KylinForm form : forms)
//        {
//            form.setFormReadOnly(!flag);
//        }
//    }
//    
//    @Override
//    public Widget getLayout()
//    {
//        return authProductParamLayout.createCanvas();
//    }
//    
//    @Override
//    public String getDataType()
//    {
//        if (authProduct == null)
//        {
//            authProduct = new AuthProduct();
//        }
//        return authProduct.getClass().getName().toString();
//    }
//    
//    @Override
//    public void refresh(Data data)
//    {
//        authProductParamLayout.updateView(data);
//    }
//    
//    @Override
//    public boolean saveCheck()
//    {
//        if (!authProductParamLayout.updateModel(authProduct))
//        {
//            return false;
//        }
//        
//        if (!authProductParamLayout.isCreated())
//        {
//            return false;
//        }
//        return true;
//    }
//    
//    @Override
//    public MapData getSubmitData()
//    {
//        return authProductParamLayout.getFormValues();
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
//        return authProductParamLayout.initLayout();
//    }
//    
//}
