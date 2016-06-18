package com.sunline.ccs.param.ui.client.organization;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.domain.CPSMessageCategoryDomainClient;
import com.sunline.ccs.param.def.Organization;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.pcm.facility.client.common.AbstractPcmProductParamLayout;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;

@Singleton
public class CcsOrgMessageLayout extends AbstractPcmProductParamLayout
{
    private static final String DATA_KEY = "messageTemplates";
    
    public static final String PRRAM_ID = "ccs_org_message";
    
    @Inject
    private CcsOrgConstants constants;
    
    @Inject
    protected OrgConstants orgConstants;
    
    // 贷记参数管理Form
    private KylinForm form;
    
    private Data orgData;
    
    @Inject
    private CPSMessageCategoryDomainClient dcMessageCategory;
    
    private Map<CPSMessageCategory, TextColumnHelper> messageItems;
    
    private Map<CPSMessageCategory, String> messageRslt;
    
    public void createCanvas(VerticalPanel panel)
    {
        panel.setWidth("100%");
        panel.setHeight("100%");
        
        form = new KylinForm();
        
        form.setWidth("100%");
        
        messageItems = new HashMap<CPSMessageCategory, TextColumnHelper>();
        Map<String, String> titleMap = dcMessageCategory.asLinkedHashMap();
        
        TextColumnHelper items[] = new TextColumnHelper[CPSMessageCategory.values().length];
        
        int i = 0;
        for (CPSMessageCategory mc : CPSMessageCategory.values())
        {
            TextColumnHelper item =
                new TextColumnHelper(mc.name(), titleMap.get(mc.name()), 20).asSelectItem()
                    .setGroup(constants.orgCcs())
                    .setGroupicon("skins/icons/communication.gif");
            
            messageItems.put(mc, item);
            items[i++] = item;
        }
        
        form.setField(items);
        form.getSetting().labelWidth(140);
        form.setCol(3);
        
        panel.add(form);
    }
    
    /**
     * 把参数对象里的数据更新到控件上
     * 
     * @param cps
     */
    public void updateView(Data data)
    {
        form.getUi().clear();
        orgData = data;
        
        RPC.ajax("rpc/ccsOrgServer/getMessageValueMaps", new RpcCallback<Data>()
        {
            @SuppressWarnings("unchecked")
            @Override
            public void onSuccess(Data result)
            {
                Map<String, LinkedHashMap<String, String>> resultMap =
                    (Map<String, LinkedHashMap<String, String>>)result.asMapData().toMap();
                SelectItem<String> columnitem = new SelectItem<String>(SelectType.LABLE);
                
                Map<String, String> valueMap = new LinkedHashMap<String, String>();
                
                if (result != null)
                {
                    for (Entry<CPSMessageCategory, TextColumnHelper> entry : messageItems.entrySet())
                    {
                        valueMap = resultMap.get(entry.getKey().toString());
                        if (valueMap != null)
                        {
                            LinkedHashMap<String, String> itemMap = new LinkedHashMap<String, String>();
                            for (Entry<String, String> etry : valueMap.entrySet())
                            {
                                itemMap.put(etry.getKey(), etry.getValue());
                            }
                            
                            columnitem.setValue(itemMap);
                            form.setFieldSelectData(entry.getValue().getName(), columnitem);
                        }
                    }
                }
                
                form.setFormData(orgData.asMapData().getData(DATA_KEY));
            }
        });
    }
    
    //	/**
    //	 * 把控件上的数据更新到参数对象上
    //	 * 
    //	 * @param cps
    //	 */
    //	public boolean updateModel(Organization org)
    //	{
    //		messageRslt = new HashMap<CPSMessageCategory, String>();
    //		for (Entry<CPSMessageCategory, TextColumnHelper> entry : messageItems.entrySet())
    //		{
    //			String code = form.getFieldValue(entry.getValue().getName());
    //
    //			if (code != null)
    //			{
    //				messageRslt.put(entry.getKey(), code);
    //			}
    //		}
    //
    //		org.messageTemplates = messageRslt;
    //		return true;
    //	}
    
    public KylinForm getForm()
    {
        return form;
    }
    
    public MapData getFormValues()
    {
        messageRslt = new HashMap<CPSMessageCategory, String>();
        for (Entry<CPSMessageCategory, TextColumnHelper> entry : messageItems.entrySet())
        {
            String code = form.getFieldValue(entry.getValue().getName());
            
            if (code != null)
            {
                messageRslt.put(entry.getKey(), code);
            }
        }
        
        MapData submitData = new MapData();
        
        MapData messageMapData = new MapData();
        
        for (Entry<CPSMessageCategory, String> entry : messageRslt.entrySet())
        {
            messageMapData.put(entry.getKey().toString(), entry.getValue());
        }
        
        submitData.put("messageTemplates", messageMapData);
        // // 获取基本信息
        // for (Field field : form.getFields()) {
        // submitData.put(field.getName(), form.getFieldValue(field.getName()));
        // }
        
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
        return true;
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
        return orgConstants.messageInfo();
    }
    
    /**
     * 重载方法
     * 
     * @return
     */
    @Override
    public int getTabIndex()
    {
        return 1;
    }
    
    /**
     * 重载方法
     * 
     * @return
     */
    @Override
    public int getBlockIndex()
    {
        return 1;
    }
    
    /**
     * 重载方法
     * 
     * @param flag
     */
    @Override
    public void editable(boolean flag)
    {
        getForm().setFormReadOnly(!flag);
    }
    
    /**
     * 重载方法
     * @return
     */
    @Override
    public Class<?> getDataTypeClass()
    {
        return Organization.class;
    }
}
