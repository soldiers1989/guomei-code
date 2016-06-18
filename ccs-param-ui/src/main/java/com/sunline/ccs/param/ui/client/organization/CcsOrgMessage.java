//package com.sunline.ccs.param.ui.client.organization;
//
//import com.google.gwt.user.client.ui.Widget;
//import com.google.inject.Inject;
//import com.sunline.ccs.infrastructure.client.domain.CPSMessageCategoryDomainClient;
//import com.sunline.ccs.param.def.Organization;
//import com.sunline.pcm.facility.client.common.IParameterWidget;
//import com.sunline.ui.core.client.data.Data;
//import com.sunline.ui.core.client.data.MapData;
//
//public class CcsOrgMessage implements IParameterWidget
//{
//    
//    public static final String PRRAM_ID = "ccs_org_message";
//    
//    @Inject
//    protected OrgConstants constants;
//    
//    protected Organization ccsOrg;
//    
//    protected CPSMessageCategoryDomainClient dcMessageCategory;
//    
//    @Inject
//    protected CcsOrgMessageLayout ccsOrgMessageLayout;
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
//        return constants.messageInfo();
//    }
//    
//    @Override
//    public int getTabIndex()
//    {
//        return 1;
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
//        ccsOrgMessageLayout.getForm().setFormReadOnly(!flag);
//    }
//    
//    @Override
//    public Widget getLayout()
//    {
//        return ccsOrgMessageLayout.createCanvas();
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
//        ccsOrgMessageLayout.updateView(data);
//    }
//    
//    @Override
//    public boolean saveCheck()
//    {
//        if (!ccsOrgMessageLayout.validForm())
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
//        //	map.put("messageTemplates", (Serializable)ccsOrg.messageTemplates);
//        //	
//        //	return map;
//        return ccsOrgMessageLayout.getFormValues();
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
//        return ccsOrgMessageLayout.initLayout();
//    }
//    
//}
