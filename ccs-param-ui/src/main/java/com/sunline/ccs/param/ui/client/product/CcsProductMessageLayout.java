package com.sunline.ccs.param.ui.client.product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.domain.CPSMessageCategoryDomainClient;
import com.sunline.ccs.infrastructure.client.ui.UProductCredit;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.param.def.enums.SendMessageCardType;
import com.sunline.kylin.web.ark.client.helper.BooleanColumnHelper;
import com.sunline.kylin.web.ark.client.helper.EnumColumnHelper;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.utils.StringUtils;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.pcm.facility.client.common.AbstractPcmProductParamLayout;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.radioList.client.RadioListSetting;

/**
 * 短信配置模板
 * 
 * @author lindh
 * @version [版本号, Jun 25, 2015]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
@Singleton
public class CcsProductMessageLayout extends AbstractPcmProductParamLayout
{
    public static final String PRRAM_ID = "ccs_product_message";
    
    @Inject
    private ProductCreditConstants constants;
    
    @Inject
    private UProductCredit uProductCredit;
    
    @Inject
    private CPSMessageCategoryDomainClient dcMessageCategory;
    
    private Map<CPSMessageCategory, TextColumnHelper> messageItems;
    
    private KylinForm proForm;
    
    private KylinForm form;
    
    private BooleanColumnHelper useOrgMsgItem;
    
    private EnumColumnHelper<SendMessageCardType> sendMessageItem;
    
    /**
     * 获取layout标题 <功能详细描述>
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    public String getLayoutTitle()
    {
        
        return constants.messageTitle();
    }
    
    public void createCanvas(VerticalPanel panel)
    {
        panel.setWidth("100%");
        panel.setHeight("100%");
        
        proForm = new KylinForm();
        proForm.setWidth("100%");
        
        proForm.setCol(2);
        
        RadioListSetting radioSetting = new RadioListSetting();
        radioSetting.rowSize(1);
        radioSetting.name("useOrgMessageTemplate");
        radioSetting.textFiled(constants.useOrgMessageTemplate());
        
        useOrgMsgItem =
            new BooleanColumnHelper("useOrgMessageTemplate", constants.useOrgMessageTemplate()).asCheckBoxItem();
        sendMessageItem = uProductCredit.SendMessageCardType();
        
        Map<String, String> titleMap = dcMessageCategory.asLinkedHashMap();
        
        proForm.setField(useOrgMsgItem, sendMessageItem.asSelectItem(SelectType.KEY_LABLE));
        
        panel.add(proForm);
        
        form = new KylinForm();
        form.setWidth("100%");
        form.getSetting().labelWidth(120);
        
        messageItems = new HashMap<CPSMessageCategory, TextColumnHelper>();
        TextColumnHelper items[] = new TextColumnHelper[CPSMessageCategory.values().length];
        
        int i = 0;
        for (CPSMessageCategory mc : CPSMessageCategory.values())
        {
            TextColumnHelper item =
                new TextColumnHelper(mc.toString(), titleMap.get(mc.toString()), 200).asSelectItem();
            
            messageItems.put(mc, item);
            
            items[i++] = item;
        }
        
        form.setField(items);
        
        form.setCol(3);
        
        panel.add(form);
    }
    
    @SuppressWarnings("unchecked")
    public void updateView(Data data)
    {
        proForm.getUi().clear();
        form.getUi().clear();
        
        final MapData mapData = data.asMapData();
        String useOrgMessageTemplate = mapData.getString("useOrgMessageTemplate");
        String sendMessageCardType = mapData.getString("sendMessageCardType");
        
        if (StringUtils.isNotEmpty(useOrgMessageTemplate) && Indicator.Y.name().equals(useOrgMessageTemplate))
        {
            proForm.setFieldValue(useOrgMsgItem.getName(), Boolean.TRUE);
        }
        else
        {
            proForm.setFieldValue(useOrgMsgItem.getName(), Boolean.FALSE);
        }
        
        if (StringUtils.isNotEmpty(sendMessageCardType))
        {
            proForm.setFieldValue(sendMessageItem.getName(), sendMessageCardType);
        }
        
        RPC.ajax("rpc/productCreditServer/getMessageValueMaps", new RpcCallback<Data>()
        {
            @Override
            public void onSuccess(Data result)
            {
                Map<String, HashMap<String, String>> resultlist =
                    (Map<String, HashMap<String, String>>)result.asMapData().toMap();
                
                SelectItem<String> columnitem = new SelectItem<String>();
                
                if (result != null)
                {
                    for (Entry<CPSMessageCategory, TextColumnHelper> entry : messageItems.entrySet())
                    {
                        LinkedHashMap<String, String> valueMap = new LinkedHashMap<String, String>();
                        Map<String, String> map = resultlist.get(entry.getKey().toString());
                        if (map == null)
                        {
                            continue;
                        }
                        valueMap.putAll(map);
                        if (valueMap != null)
                        {
                            columnitem.setValue(valueMap);
                            form.setFieldSelectData(entry.getValue().getName(), columnitem);
                        }
                    }
                }
                
                if (mapData.getData("messageTemplates") == null)
                {
                    return;
                }
                Map<CPSMessageCategory, String> map =
                    (Map<CPSMessageCategory, String>)mapData.getData("messageTemplates").asMapData().toMap();
                
                if (map != null && map.size() > 0)
                {
                    for (Entry<CPSMessageCategory, TextColumnHelper> entry : messageItems.entrySet())
                    {
                        String templateCode = map.get(entry.getKey().name());
                        
                        if (templateCode != null)
                            form.setFieldValue(entry.getValue().getName(), templateCode);
                        else
                            form.setFieldValue(entry.getValue().getName(), "");
                    }
                }
            }
        });
    }
    
    /**
     * 把控件上的数据更新到参数对象上
     * 
     * @param cps
     */
    public boolean updateModel(ProductCredit productCredit)
    {
        if (form.valid() && proForm.valid())
        {
            return true;
        }
        return false;
    }
    
    public List<KylinForm> getForms()
    {
        List<KylinForm> formList = new ArrayList<KylinForm>();
        formList.add(form);
        formList.add(proForm);
        
        return formList;
    }
    
    public MapData getFormValues()
    {
        MapData submitData = new MapData();
        
        submitData.put("messageTemplates", form.getSubmitData().asMapData());
        
        String useOrgMessageTemplateValue = proForm.getFieldValue("useOrgMessageTemplate");
        if (StringUtils.isNotEmpty(useOrgMessageTemplateValue)
            && useOrgMessageTemplateValue.equals(Boolean.TRUE.toString()))
        {
            submitData.put("useOrgMessageTemplate", Indicator.Y.name());
        }
        else
        {
            submitData.put("useOrgMessageTemplate", Indicator.N.name());
        }
        
        submitData.put(sendMessageItem.getName(), proForm.getFieldValue(sendMessageItem.getName()));
        
        //	    
        //	    // 获取基本信息
        //	    for (Field field : form.getFields()) {
        //		submitData.put(field.getName(), form.getFieldValue(field.getName()));
        //	    }
        //	    // 获取支持卡片类型数据
        //	    for (Field field : proForm.getFields()) {
        //		submitData.put(field.getName(), proForm.getFieldValue(field.getName()));
        //	    }
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
        if (form.valid() && proForm.valid())
        {
            return true;
        }
        return false;
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
        return constants.messageTitle();
    }
    
    /**
     * 重载方法
     * 
     * @return
     */
    @Override
    public int getTabIndex()
    {
        return 14;
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
     * @return
     */
    @Override
    public Class<?> getDataTypeClass()
    {
        return ProductCredit.class;
    }
}
