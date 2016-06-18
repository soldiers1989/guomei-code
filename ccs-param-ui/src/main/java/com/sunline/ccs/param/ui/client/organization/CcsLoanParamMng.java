//package com.sunline.ccs.param.ui.client.organization;
//
//import java.util.List;
//
//import com.google.gwt.user.client.ui.Widget;
//import com.google.inject.Inject;
//import com.sunline.ccs.param.def.Organization;
//import com.sunline.kylin.web.ark.client.ui.KylinForm;
//import com.sunline.pcm.facility.client.common.IParameterWidget;
//import com.sunline.ui.core.client.data.Data;
//import com.sunline.ui.core.client.data.MapData;
//
//public class CcsLoanParamMng implements IParameterWidget
//{
//    
//    public static final String PRRAM_ID = "ccs_loan_param";
//    
//    @Inject
//    protected OrgConstants constants;
//    
//    @Inject
//    protected CcsOrgLayout ccsOrgLayout;
//    
//    protected Organization ccsOrg;
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
//        return constants.orgDetail();
//    }
//    
//    @Override
//    public int getTabIndex()
//    {
//        return 0;
//    }
//    
//    @Override
//    public int getBlockIndex()
//    {
//        return 1;
//    }
//    
//    @Override
//    public void editable(boolean flag)
//    {
//        List<KylinForm> forms = ccsOrgLayout.getForms();
//        for (KylinForm form : forms)
//        {
//            form.setFormReadOnly(!flag);
//        }
//    }
//    
//    @Override
//    public Widget getLayout()
//    {
//        return ccsOrgLayout.createCanvas();
//    }
//    
//    @Override
//    public String getDataType()
//    {
//        if (ccsOrg == null)
//            ccsOrg = new Organization();
//        
//        return ccsOrg.getClass().getName().toString();
//    }
//    
//    @Override
//    public void refresh(Data data)
//    {
//        
//        ccsOrgLayout.updateView(data);
//    }
//    
//    @Override
//    public boolean saveCheck()
//    {
//        if (!ccsOrgLayout.validForm())
//        {
//            return false;
//        }
//        return true;
//    }
//    
//    @Override
//    public MapData getSubmitData()
//    {
//        //	Map<String, Serializable> map = OrganizationMapHelper.convertToMap(ccsOrg);
//        //	
//        //	return map;
//        return ccsOrgLayout.getFormValues();
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
//        return ccsOrgLayout.initLayout();
//    }
//    
//}
