package com.sunline.ccs.param.ui.client.product;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UOverlimitCharge;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.ui.client.util.RatesLayout;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.pcm.facility.client.common.AbstractPcmProductParamLayout;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.ListData;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.core.client.util.DataUtil;
import com.sunline.ui.tab.client.Tab;
import com.sunline.ui.tab.client.TabItemSetting;
import com.sunline.ui.tab.client.TabSetting;

/**
 * 超限费信息
 * 
 * @author
 * @version [版本号, Jul 13, 2015]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
@Singleton
public class OverlimitChargeLayout extends AbstractPcmProductParamLayout
{
    public static final String PRRAM_ID = "ccs_overlimit_charge";
    
    @Inject
    private ProductCreditConstants constants;
    
    @Inject
    private UOverlimitCharge uOverlimitCharge;
    
    @Inject
    private RatesLayout ratesLayout;
    
    @Inject
    private RatesLayout dualRatesLayout;
    
    private KylinForm editorForm;
    
    private KylinForm dualEditorForm;
    
    private Tab tab;
    
    /**
     * 获取layout标题 <功能详细描述>
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    public String getLayoutTitle()
    {
        
        return constants.productOverlimitChargeInfo();
    }
    
    public void createCanvas(VerticalPanel panel)
    {
        panel.setWidth("100%");
        panel.setHeight("100%");
        
        editorForm = createEditorForm();
        
        TabSetting tabSetting = new TabSetting().dblClickToClose(true).dragToMove(true).showSwitch(true);
        tab = new Tab(tabSetting);
        
        panel.add(tab);
        
        // 本币超限费panel
        VerticalPanel overPanel = new VerticalPanel();
        // 信息框
        overPanel.add(editorForm);
        // 费率框
        overPanel.add(ratesLayout.createCanvas());
        
        TabItemSetting overTabSetting = new TabItemSetting(null, constants.overlimitChargeTitle());
        tab.addItem(overTabSetting, overPanel);
        
        // 外币超限费panel
        dualEditorForm = createEditorForm();
        
        VerticalPanel dualOverPanel = new VerticalPanel();
        // 信息框
        dualOverPanel.add(dualEditorForm);
        // 费率框
        dualOverPanel.add(dualRatesLayout.createCanvas());
        
        TabItemSetting dualOverSetting = new TabItemSetting(null, constants.dualOverlimitChargeTitle());
        tab.addItem(dualOverSetting, dualOverPanel);
        
    }
    
    private KylinForm createEditorForm()
    {
        KylinForm form = new KylinForm();
        form.setCol(3);
        form.setWidth("100%");
        
        form.setField(uOverlimitCharge.ChargeDateInd().required(true).asSelectItem(SelectType.KEY_LABLE),
            uOverlimitCharge.MinCharge().required(true),
            uOverlimitCharge.MaxCharge().required(true),
            uOverlimitCharge.YearMaxCharge().required(true),
            uOverlimitCharge.YearMaxCnt().required(true),
            uOverlimitCharge.CalcInd().required(true).asSelectItem(SelectType.KEY_LABLE),
            uOverlimitCharge.TierInd().required(true).asSelectItem(SelectType.KEY_LABLE));
        
        form.getSetting().labelWidth(240).labelAlign("right");
        return form;
    }
    
    public void updateView(Data data)
    {
        editorForm.getUi().clear();
        dualEditorForm.getUi().clear();
        ratesLayout.getGrid().loadData(StringToData("[]"));
        dualRatesLayout.getGrid().loadData(StringToData("[]"));
        
        if (data.asMapData().getData("overlimitCharge") != null)
        {
            Data overData = data.asMapData().getData("overlimitCharge");
            
            if (data.asMapData().getData("overlimitCharge").asMapData().getData("chargeRates") != null)
            {
                ratesLayout.setValue(data.asMapData().getData("overlimitCharge").asMapData().getData("chargeRates"));
            }
            
            editorForm.setFormData(overData);
        }
        
        if (data.asMapData().getData("dualOverlimitCharge") != null)
        {
            Data dualData = data.asMapData().getData("dualOverlimitCharge");
            
            if (data.asMapData().getData("dualOverlimitCharge").asMapData().getData("chargeRates") != null)
            {
                dualRatesLayout.setValue(data.asMapData()
                    .getData("dualOverlimitCharge")
                    .asMapData()
                    .getData("chargeRates"));
            }
            
            dualEditorForm.setFormData(dualData);
        }
    }
    
    public boolean updateModel(ProductCredit model)
    {
        if (!editorForm.valid())
        {
            return false;
        }
        
        if (!dualEditorForm.valid())
        {
            return false;
        }
        
        return true;
    }
    
    public List<KylinForm> getForms()
    {
        List<KylinForm> formList = new ArrayList<KylinForm>();
        formList.add(editorForm);
        formList.add(dualEditorForm);
        
        return formList;
    }
    
    public MapData getFormValues()
    {
        MapData submitData = new MapData();
        
        ListData charge = ratesLayout.getValue();
        
        MapData overlimitMap = editorForm.getSubmitData().asMapData();
        overlimitMap.put("chargeRates", charge);
        
        submitData.put("overlimitCharge", overlimitMap);
        
        ListData dualCharge = dualRatesLayout.getValue();
        MapData dualOverlimitMap = dualEditorForm.getSubmitData().asMapData();
        dualOverlimitMap.put("chargeRates", dualCharge);
        
        submitData.put("dualOverlimitCharge", dualOverlimitMap);
        
        return submitData;
    }
    
    public Data StringToData(String gridDataString)
    {
        Data data = new Data();
        data.setJsData(DataUtil.convertDataType(gridDataString));
        return data;
    }
    
    /**
     * 重载方法
     * 
     * @return
     */
    @Override
    public boolean validForm()
    {
        if (!editorForm.valid())
        {
            return false;
        }
        
        if (!dualEditorForm.valid())
        {
            return false;
        }
        
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
        return constants.productOverlimitChargeInfo();
    }
    
    /**
     * 重载方法
     * 
     * @return
     */
    @Override
    public int getTabIndex()
    {
        return 4;
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
        return ProductCredit.class;
    }
}
