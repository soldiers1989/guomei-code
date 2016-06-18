package com.sunline.ccs.param.ui.client.authproduct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.domain.AuthVerifyActionDomainClient;
import com.sunline.ccs.infrastructure.client.domain.VerifyEnumDomainClient;
import com.sunline.ccs.param.def.enums.VerifyEnum;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.utils.StringUtils;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;

/**
 * 安全要素验证控制
 * 
 * @author
 * @version [版本号, Jul 13, 2015]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
@Singleton
public class AuthProductVerifyActionsLayout
{
    @Inject
    private AuthProductConstants authProductConstants;
    
    @Inject
    private VerifyEnumDomainClient verifyEnumDomainClient;
    
    @Inject
    private AuthVerifyActionDomainClient authVerifyActionDomainClient;
    
    private Map<VerifyEnum, TextColumnHelper> items;
    
    private KylinForm verifyActionsForm;
    
    private List<TextColumnHelper> formItems;
    
    public String getLayoutTitle()
    {
        
        return authProductConstants.authProductVerifyActions();
    }
    
    public Widget createCanvas()
    {
        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        panel.setHeight("100%");
        
        LinkedHashMap<String, String> verifyMap = verifyEnumDomainClient.asLinkedHashMap(false);
        formItems = new ArrayList<TextColumnHelper>();
            
        items = new HashMap<VerifyEnum, TextColumnHelper>();
        
        for (VerifyEnum verify : VerifyEnum.values())
        {
            TextColumnHelper item =
                new TextColumnHelper(verify.toString(), verifyMap.get(verify.toString()), 120).setNewline(true)
                    .asSelectItem();
            items.put(verify, item);
            formItems.add(item);
        }
        
        verifyActionsForm = new KylinForm();
        verifyActionsForm.setWidth("100%");
        verifyActionsForm.setField(formItems.toArray(new TextColumnHelper[formItems.size()]));
        
        verifyActionsForm.getSetting().labelWidth(250).labelAlign("right");
        
        panel.add(verifyActionsForm);
        
        return panel;
    }
    
    @SuppressWarnings("unchecked")
    public void updateView(Data data)
    {
        verifyActionsForm.getUi().clear();
        
        LinkedHashMap<String, String> actionMap = authVerifyActionDomainClient.asLinkedHashMap();
        for (TextColumnHelper item : formItems)
        {
            verifyActionsForm.setFieldSelectData(item.getName(),
                new SelectItem<String>(SelectType.LABLE).setValue(actionMap));
        }
        
        Data model = data.asMapData().getData("verifyActions");
        if (model == null || model.toString().equals("{}"))
        {
            return;
        }
        Map<String, String> verifyActions = (Map<String, String>)model.asMapData().toMap();
        
        if (verifyActions != null && verifyActions.size() > 0)
        {
            for (VerifyEnum verify : VerifyEnum.values())
            {
                TextColumnHelper item = items.get(verify);
                if (verifyActions.containsKey(verify.name()))
                {
                    if (StringUtils.isNotEmpty(verifyActions.get(verify.name())))
                    {
                        verifyActionsForm.setFieldValue(item.getName(), verifyActions.get(verify.name()));
                    }
                }
            }
        }
    }
    
    public boolean validForm()
    {
        
        if (!verifyActionsForm.valid())
            return false;
        
        return true;
    }
    
    public MapData getFormValues()
    {
        
        MapData submitData = new MapData();
        submitData.put("verifyActions", verifyActionsForm.getSubmitData().asListData());
        
        return submitData;
    }
    
    public KylinForm getForms()
    {
        
        return verifyActionsForm;
    }
}
