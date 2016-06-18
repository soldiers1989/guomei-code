package com.sunline.ccs.param.ui.client.authproduct;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.param.def.AuthProduct;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.pcm.facility.client.common.AbstractPcmProductParamLayout;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.tab.client.Tab;
import com.sunline.ui.tab.client.TabItemSetting;
import com.sunline.ui.tab.client.TabSetting;

@Singleton
public class AuthProductParamLayout extends AbstractPcmProductParamLayout
{
    public static final String PRRAM_ID = "auth_product_param";
    
    @Inject
    private AuthProductConstants authProductConstants;
    
    //原因码对应行动码控制
    @Inject
    private AuthProductAthrsnLayout authAthrsnLayout;
    
    //授权控制基本信息
    @Inject
    private AuthProductBasicInfoLayout authBasicInfoLayout;
    
    //授权验证点控制
    @Inject
    private AuthProductCheckEnabledLayout authCheckEnabledLayout;
    
    //终端整体支持控制
    @Inject
    private AuthProductTerminalEnabledLayout authTerminalEnabledLayout;
    
    //交易类型与终端的支持控制
    @Inject
    private AuthProductTransTypeTerminalLayout authTypeTerminalLayout;
    
    //安全要素验证控制
    @Inject
    private AuthProductVerifyActionsLayout authVerifyActionsLayout;
    
    public void createCanvas(VerticalPanel panel)
    {
        panel.setWidth("100%");
        panel.setHeight("100%");
        
        TabItemSetting authBasicInfoSetting =
            new TabItemSetting(authBasicInfoLayout.getLayoutTitle(), authBasicInfoLayout.getLayoutTitle());
        TabItemSetting authAthrsnSetting =
            new TabItemSetting(authAthrsnLayout.getLayoutTitle(), authAthrsnLayout.getLayoutTitle());
        TabItemSetting authTypeTerminalSetting =
            new TabItemSetting(authTypeTerminalLayout.getLayoutTitle(), authTypeTerminalLayout.getLayoutTitle());
        TabItemSetting authVerifyActionsSetting =
            new TabItemSetting(authVerifyActionsLayout.getLayoutTitle(), authVerifyActionsLayout.getLayoutTitle());
        TabItemSetting authCheckEnabledSetting =
            new TabItemSetting(authCheckEnabledLayout.getLayoutTitle(), authCheckEnabledLayout.getLayoutTitle());
        TabItemSetting authTerminalEnabledSetting =
            new TabItemSetting(authTerminalEnabledLayout.getLayoutTitle(), authTerminalEnabledLayout.getLayoutTitle());
        
        TabSetting setting = new TabSetting();
        Tab tab = new Tab(setting);
        
        tab.addItem(authBasicInfoSetting, authBasicInfoLayout.createCanvas());
        tab.addItem(authAthrsnSetting, authAthrsnLayout.createCanvas());
        tab.addItem(authTypeTerminalSetting, authTypeTerminalLayout.createCanvas());
        tab.addItem(authVerifyActionsSetting, authVerifyActionsLayout.createCanvas());
        tab.addItem(authCheckEnabledSetting, authCheckEnabledLayout.createCanvas());
        tab.addItem(authTerminalEnabledSetting, authTerminalEnabledLayout.createCanvas());
        
        panel.add(tab);
    }
    
    public List<KylinForm> getForms()
    {
        List<KylinForm> formList = new ArrayList<KylinForm>();
        formList.add(authBasicInfoLayout.getForms());
        formList.add(authAthrsnLayout.getForms());
        formList.add(authVerifyActionsLayout.getForms());
        formList.add(authCheckEnabledLayout.getForms());
        formList.add(authTerminalEnabledLayout.getForms());
        
        return formList;
    }
    
    public void updateView(Data data)
    {
        authBasicInfoLayout.updateView(data);
        authAthrsnLayout.updateView(data);
        authTypeTerminalLayout.updateView(data);
        authVerifyActionsLayout.updateView(data);
        authCheckEnabledLayout.updateView(data);
        authTerminalEnabledLayout.updateView(data);
    }
    
    @SuppressWarnings("static-access")
    public MapData getFormValues()
    {
        MapData submitData = new MapData();
        
        submitData.extend(submitData, authBasicInfoLayout.getFormValues(), false);
        submitData.extend(submitData, authAthrsnLayout.getFormValues(), false);
        submitData.extend(submitData, authTypeTerminalLayout.getFormValues(), false);
        submitData.extend(submitData, authVerifyActionsLayout.getFormValues(), false);
        submitData.extend(submitData, authCheckEnabledLayout.getFormValues(), false);
        submitData.extend(submitData, authTerminalEnabledLayout.getFormValues(), false);
        
        return submitData;
    }
    
    /**
     * 重载方法
     * 
     * @return
     */
    @Override
    public boolean validForm()
    {
        if (authBasicInfoLayout.validForm() && authAthrsnLayout.validForm() && authTypeTerminalLayout.validForm()
            && authVerifyActionsLayout.validForm() && authCheckEnabledLayout.validForm()
            && authTerminalEnabledLayout.validForm())
        {
            return true;
        }
        else
        {
            return false;
        }
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
        return authProductConstants.authProductInfo();
    }
    
    /**
     * 重载方法
     * 
     * @return
     */
    @Override
    public int getTabIndex()
    {
        return 13;
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
        List<KylinForm> forms = getForms();
        for (KylinForm form : forms)
        {
            form.setFormReadOnly(!flag);
        }
    }
    
    /**
     * 重载方法
     * 
     * @return
     */
    @Override
    public Class<?> getDataTypeClass()
    {
        return AuthProduct.class;
    }
}
