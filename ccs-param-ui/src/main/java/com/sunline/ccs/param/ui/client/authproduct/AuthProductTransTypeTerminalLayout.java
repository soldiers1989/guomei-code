package com.sunline.ccs.param.ui.client.authproduct;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.domain.AuthTransTerminalDomainClient;
import com.sunline.ccs.infrastructure.client.domain.AuthTransTypeDomainClient;
import com.sunline.ccs.param.def.enums.AuthTransTerminal;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;

/**
 * 
 * 交易类型与终端的支持控制
 * 
 * @author lindh
 * @version [版本号, Jun 24, 2015]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */

@Singleton
public class AuthProductTransTypeTerminalLayout
{
    @Inject
    private AuthProductConstants authProductConstants;
    
    @Inject
    private AuthTransTypeDomainClient dcTransType;
    
    @Inject
    private AuthTransTerminalDomainClient dcTransTerm;
    
    private FlexTable tableRow;
    
    private Map<AuthTransType, Map<AuthTransTerminal, CheckBox>> checkItems =
        new HashMap<AuthTransType, Map<AuthTransTerminal, CheckBox>>();
    
    public String getLayoutTitle()
    {
        return authProductConstants.authProductTransTypeTerminal();
    }
    
    public Widget createCanvas()
    {
        VerticalPanel vPanel = new VerticalPanel();
        vPanel.setWidth("95%");
        vPanel.setHeight("95%");
        
        tableRow = new FlexTable();
        tableRow.setBorderWidth(0);
        tableRow.setCellSpacing(55);
        tableRow.setCellPadding(5);
        
        Map<String, String> transTermLabels = dcTransTerm.asLinkedHashMap(false);
        Map<String, String> transTypeLabels = dcTransType.asLinkedHashMap(false);
        
        String panelWidth = "87px";
        String panelHeight = "25px";
        for (int i = -1; i < AuthTransType.values().length; i++)
        {
            Map<AuthTransTerminal, CheckBox> authTransTerminalCheckBox = new HashMap<AuthTransTerminal, CheckBox>();
            for (int j = -1; j < AuthTransTerminal.values().length; j++)
            {
                HorizontalPanel panel = new HorizontalPanel();
                panel.setWidth(panelWidth);
                panel.setHeight(panelHeight);
                
                // 表头第一个元素
                if (i == -1 && j == -1)
                {
                    panel.setWidth("100px");
                    Label label = new Label(authProductConstants.txnType());
                    label.setHorizontalAlignment(Label.ALIGN_CENTER);
                    panel.add(label);
                    tableRow.setWidget(0, 0, panel);
                }
                
                // 第一行，表头终端信息
                else if (i == -1)
                {
                    Label label = new Label(transTermLabels.get(AuthTransTerminal.values()[j].name()));
                    panel.add(label);
                    tableRow.setWidget(0, j + 1, panel);
                }
                
                // 第一列，交易类型
                else if (j == -1)
                {
                    Label label = new Label(transTypeLabels.get(AuthTransType.values()[i].name()));
                    label.setHorizontalAlignment(Label.ALIGN_RIGHT);
                    panel.add(label);
                    tableRow.setWidget(i + 1, 0, panel);
                }
                else
                {
                    CheckBox checkBox = new CheckBox();
                    checkBox.getElement().getFirstChildElement().addClassName("gwt-l-checkbox");
                    panel.add(checkBox);
                    tableRow.setWidget(i + 1, j + 1, panel);
                    authTransTerminalCheckBox.put(AuthTransTerminal.values()[j], checkBox);
                }
            }
            if (i != -1)
            {
                checkItems.put(AuthTransType.values()[i], authTransTerminalCheckBox);
            }
        }
        vPanel.add(tableRow);
        return vPanel;
    }
    
    @SuppressWarnings({"unchecked"})
    public void updateView(Data data)
    {
        for (AuthTransType authTransType : AuthTransType.values())
            for (AuthTransTerminal transTerm : AuthTransTerminal.values())
                checkItems.get(authTransType).get(transTerm).setValue(false);
        
        if (data.asMapData().getData("transTypeTerminalEnabled") != null)
        {
            
            MapData model = data.asMapData().getData("transTypeTerminalEnabled").asMapData();
            if (model != null)
            {
                for (AuthTransType authTransType : AuthTransType.values())
                {
                    for (AuthTransTerminal transTerm : AuthTransTerminal.values())
                    {
                        CheckBox item = checkItems.get(authTransType).get(transTerm);
                        Map<AuthTransTerminal, Boolean> authTransMap =
                            (Map<AuthTransTerminal, Boolean>)model.getData(authTransType.name()).asMapData().toMap();
                        if (authTransMap != null && authTransMap.containsKey(transTerm.name()))
                            item.setValue(authTransMap.get(transTerm.name()));
                    }
                }
            }
        }
    }
    
    public boolean validForm()
    {
        return true;
    }
    
    public MapData getFormValues()
    {
        MapData authTransTypeMap = new MapData();
        for (AuthTransType authTransType : AuthTransType.values())
        {
            MapData transTermMap = new MapData();
            for (AuthTransTerminal transTerm : AuthTransTerminal.values())
            {
                CheckBox item = checkItems.get(authTransType).get(transTerm);
                transTermMap.put(transTerm.name(), item.getValue());
            }
            authTransTypeMap.put(authTransType.name(), transTermMap);
        }
        
        MapData transTypeTerminalEnabled = new MapData();
        transTypeTerminalEnabled.put("transTypeTerminalEnabled", authTransTypeMap);
        return transTypeTerminalEnabled;
    }
}
