package com.sunline.ccs.param.ui.client.authproduct;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UAuthProduct;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;

/**
 * 授权控制基本信息
 * 
 * @author  
 * @version  [版本号, Jul 13, 2015]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Singleton
public class AuthProductBasicInfoLayout
{
    @Inject
    private UAuthProduct uAuthProduct;
    
    @Inject
    private AuthProductConstants authProductConstants;
    
    private KylinForm authProductBasicForm;
    
    public String getLayoutTitle()
    {
        return authProductConstants.authProductBasicInfo();
    }
    
    public Widget createCanvas()
    {
        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        panel.setHeight("100%");
        
        authProductBasicForm = new KylinForm();
        
        authProductBasicForm.setCol(2);
        authProductBasicForm.setWidth("100%");
        
        authProductBasicForm.setField(uAuthProduct.IsolatedConfirm()
            .required(true)
            .setNewline(true)
            .asSelectItem(SelectType.KEY_LABLE),
            uAuthProduct.AuthCodeIncMCCFlag().setNewline(true).asCheckBoxItem(),
            uAuthProduct.IsSupportVCardSign().setNewline(true).asCheckBoxItem());
        authProductBasicForm.getSetting().labelWidth(180).labelAlign("right");
        
        panel.add(authProductBasicForm);
        
        return panel;
    }
    
    public void updateView(Data data)
    {
        authProductBasicForm.getUi().clear();
        
        authProductBasicForm.setFormData(data);
    }
    
    public boolean validForm()
    {
        
        if (!authProductBasicForm.valid())
            return false;
        
        return true;
    }
    
    public MapData getFormValues()
    {
        
        return authProductBasicForm.getSubmitData().asMapData();
    }
    
    public KylinForm getForms()
    {
        
        return authProductBasicForm;
    }
}
