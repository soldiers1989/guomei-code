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
import com.sunline.ccs.infrastructure.client.domain.AuthFlagActionDomainClient;
import com.sunline.ccs.infrastructure.client.domain.AuthTransTerminalDomainClient;
import com.sunline.ccs.param.def.enums.AuthTransTerminal;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.utils.StringUtils;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;

@Singleton
public class AuthProductTerminalEnabledLayout
{
    @Inject
    private AuthProductConstants authProductConstants;
    
    @Inject
    private AuthTransTerminalDomainClient authTransTerminalDomainClient;
    
    @Inject
    private AuthFlagActionDomainClient authFlagActionDomainClient;
    
    private KylinForm terminalEnabledForm;
    
    private Map<AuthTransTerminal, TextColumnHelper> items;
    
    private List<TextColumnHelper> formItems;
    
    public String getLayoutTitle()
    {
        
        return authProductConstants.authProductTerminalEnabled();
    }
    
    public Widget createCanvas()
    {
        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        panel.setHeight("100%");
        
        formItems = new ArrayList<TextColumnHelper>();
        items = new HashMap<AuthTransTerminal, TextColumnHelper>();
            
        LinkedHashMap<String, String> transTmlMap = authTransTerminalDomainClient.asLinkedHashMap(false);
        
        for (AuthTransTerminal transTml : AuthTransTerminal.values())
        {
            TextColumnHelper item =
                new TextColumnHelper(transTml.toString(), transTmlMap.get(transTml.toString()), 120).asSelectItem();
            
            items.put(transTml, item);
            formItems.add(item);
        }
        terminalEnabledForm = new KylinForm();
        terminalEnabledForm.setCol(3);
        terminalEnabledForm.setWidth("100%");
        
        terminalEnabledForm.setField(formItems.toArray(new TextColumnHelper[formItems.size()]));
        
        terminalEnabledForm.getSetting().labelWidth(120);
        
        panel.add(terminalEnabledForm);
        
        return panel;
    }
    
    @SuppressWarnings("unchecked")
    public void updateView(Data data)
    {
        terminalEnabledForm.getUi().clear();
        
        LinkedHashMap<String, String> flagActionMap = authFlagActionDomainClient.asLinkedHashMap();
        for (TextColumnHelper item : formItems)
        {
            terminalEnabledForm.setFieldSelectData(item.getName(),
                new SelectItem<String>(SelectType.LABLE).setValue(flagActionMap));
        }
        
        Map<String, String> terminalEnabled = new HashMap<String, String>();
        if (data.asMapData().getData("terminalEnabled") != null)
        {
            terminalEnabled = (Map<String, String>)data.asMapData().getData("terminalEnabled").asMapData().toMap();
        }
        
        if (terminalEnabled != null && terminalEnabled.size() > 0)
        {
            for (AuthTransTerminal transTml : AuthTransTerminal.values())
            {
                TextColumnHelper item = items.get(transTml);
                if (terminalEnabled.containsKey(transTml.name()))
                {
                    String value = terminalEnabled.get(transTml.name());
                    if (StringUtils.isNotEmpty(value))
                    {
                        terminalEnabledForm.setFieldValue(item.getName(), value);
                    }
                }
            }
        }
    }
    
    public boolean validForm()
    {
        return terminalEnabledForm.valid();
    }
    
    public MapData getFormValues()
    {
        MapData submitData = new MapData();
        submitData.put("terminalEnabled", terminalEnabledForm.getSubmitData().asListData());
        
        return submitData;
    }
    
    public KylinForm getForms()
    {
        
        return terminalEnabledForm;
    }
}
