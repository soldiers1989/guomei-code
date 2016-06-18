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
import com.sunline.ark.support.def.DomainClientSupport;
import com.sunline.ccs.infrastructure.client.domain.AuthActionDomainClient;
import com.sunline.ccs.infrastructure.client.domain.AuthReasonDomainClient;
import com.sunline.ccs.param.def.consts.AuthReasonGroups;
import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.ccs.param.def.enums.AuthReason;
import com.sunline.kylin.web.ark.client.helper.EnumColumnHelper;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;

/**
 * 原因码对应行动码控制
 * 
 * @author  lindh
 * @version  [版本号, Jul 13, 2015]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Singleton
public class AuthProductAthrsnLayout
{
    
    @Inject
    private AuthProductConstants authProductConstants;
    
    @Inject
    private AuthReasonDomainClient authReasonDomainClient;
    
    private KylinForm reasonActionsForm;
    
    @Inject
    private AuthActionDomainClient authActionDomainClient;
    
    private Map<AuthReason, EnumColumnHelper<AuthAction>> items;
    
    public String getLayoutTitle()
    {
        
        return authProductConstants.authProductReasonAction();
    }
    
    public Widget createCanvas()
    {
        
        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        panel.setHeight("100%");
        
        LinkedHashMap<String, String> reasonMap = authReasonDomainClient.asLinkedHashMap(false);
        LinkedHashMap<String, String> actionMap = authActionDomainClient.asLinkedHashMap();
        
        items = new HashMap<AuthReason, EnumColumnHelper<AuthAction>>();
        
        List<EnumColumnHelper<AuthAction>> formItems = new ArrayList<EnumColumnHelper<AuthAction>>();
        
        for (AuthReason reason : AuthReasonGroups.COMMON_REASONS)
        {
            EnumColumnHelper<AuthAction> item =
                new EnumColumnHelper<AuthAction>(reason.toString(), reasonMap.get(reason.toString()), AuthAction.class)
                {
                    public DomainClientSupport<String> getDomain()
                    {
                        return authActionDomainClient;
                    }
                };
            item.asSelectItem(SelectType.KEY_LABLE);
            items.put(reason, item);
            formItems.add(item);
        }
        reasonActionsForm = new KylinForm();
        reasonActionsForm.setCol(3);
        reasonActionsForm.setWidth("100%");
        
        reasonActionsForm.setField(formItems.toArray(new EnumColumnHelper[formItems.size()]));
        
        for (EnumColumnHelper<AuthAction> item : formItems)
        {
            reasonActionsForm.setFieldSelectData(item.getName(), new SelectItem<String>().setValue(actionMap));
        }
        
        panel.add(reasonActionsForm);
        
        return panel;
    }
    
    @SuppressWarnings("unchecked")
    public void updateView(Data data)
    {
        reasonActionsForm.getUi().clear();
//        
//        AuthProduct model = new AuthProduct();
//        AuthProductMapHelper.updateFromMap(model, (Map<String, Serializable>)data.asMapData().toMap());
        
        Map<String, String> reasonActions = (Map<String, String>)data.asMapData().toMap().get("reasonActions");
        
        if (reasonActions != null && !reasonActions.toString().equals("{}"))
        {
            for (AuthReason reason : AuthReasonGroups.COMMON_REASONS)
            {
                EnumColumnHelper<AuthAction> item = items.get(reason);
                if (reasonActions.containsKey(reason.name()))
                {
                    AuthAction value = AuthAction.valueOf(reasonActions.get(reason.name()));
                    
                    if (value != null)
                    {
                        reasonActionsForm.setFieldValue(item.getName(), value.toString());
                    }
                }
            }
        }
    }
    
    public boolean validForm()
    {
        if (!reasonActionsForm.valid())
            return false;
        
        return true;
    }
    
    public MapData getFormValues()
    {
        MapData submitData = new MapData();
        submitData.put("reasonActions", reasonActionsForm.getSubmitData().asMapData());
        return submitData;
    }
    
    public KylinForm getForms()
    {
        return reasonActionsForm;
    }
    
}
