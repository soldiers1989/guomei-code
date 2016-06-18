package com.sunline.ccs.param.ui.client.product;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ark.support.def.DomainClientSupport;
import com.sunline.ccs.infrastructure.client.domain.TierIndDomainClient;
import com.sunline.ccs.infrastructure.client.ui.ULatePaymentCharge;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.TierInd;
import com.sunline.ccs.param.ui.client.util.RatesLayout;
import com.sunline.kylin.web.ark.client.helper.EnumColumnHelper;
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
 * 滞纳金信息
 * 
 * @author
 * @version [版本号, Jul 13, 2015]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
@Singleton
public class LatePaymentChargeLayout extends AbstractPcmProductParamLayout
{
    public static final String PRRAM_ID = "ccs_latepayment_charge";
    
    private static final String TIERIND_ITEM = "tierIndItem";
    
    @Inject
    private ULatePaymentCharge uLatePaymentCharge;
    
    @Inject
    private ProductCreditConstants constants;
    
    @Inject
    private RatesLayout ratesLayout;
    
    @Inject
    private RatesLayout dualRatesLayout;
    
    @Inject
    private RatesLayout replaceRatesLayout;
    
    private KylinForm editorForm;
    
    private KylinForm dualEditorForm;
    
    private KylinForm replaceEditorForm;//代收滞纳金表单
    
    private Tab tab;
    
    @Inject
    private TierIndDomainClient tierIndDomainClient;
    
    /**
     * 获取layout标题 <功能详细描述>
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    public String getLayoutTitle()
    {
        
        return constants.productLatePaymentChargeInfo();
    }
    
    public void createCanvas(VerticalPanel panel)
    {
        panel.setWidth("100%");
        panel.setHeight("100%");
        
        editorForm = createEditorForm();
        
        TabSetting tabSetting = new TabSetting().dblClickToClose(true).dragToMove(true).showSwitch(true);
        tab = new Tab(tabSetting);
        
        panel.add(tab);
        
        // 本币滞纳金panel
        VerticalPanel overPanel = new VerticalPanel();
        // 信息框
        overPanel.add(editorForm);
        // 费率框
        overPanel.add(ratesLayout.createCanvas());
        
        TabItemSetting overTabSetting = new TabItemSetting(null, constants.latePaymentChargeTitle());
        tab.addItem(overTabSetting, overPanel);
        
        // 外币滞纳金panel
        dualEditorForm = createEditorForm();
        
        VerticalPanel dualOverPanel = new VerticalPanel();
        // 信息框
        dualOverPanel.add(dualEditorForm);
        // 费率框
        dualOverPanel.add(dualRatesLayout.createCanvas());
        
        TabItemSetting dualOverSetting = new TabItemSetting(null, constants.dualLatePaymentChargeTitle());
        tab.addItem(dualOverSetting, dualOverPanel);
        
        //代收滞纳金panel
        replaceEditorForm = createEditorForm();
        
        VerticalPanel replaceOverPanel = new VerticalPanel();
        //信息框
        replaceOverPanel.add(replaceEditorForm);
        //费率框
        replaceOverPanel.add(replaceRatesLayout.createCanvas());
        
        TabItemSetting replaceOverSetting = new TabItemSetting(null, constants.replaceLatePaymentChargeTitle());
        tab.addItem(replaceOverSetting, replaceOverPanel);
    }
    
    private KylinForm createEditorForm()
    {
        KylinForm form = new KylinForm();
        form.setCol(3);
        form.setWidth("100%");
        
        EnumColumnHelper<TierInd> tierIndItem =
            new EnumColumnHelper<TierInd>(TIERIND_ITEM, uLatePaymentCharge.TierInd().getDisplay(), TierInd.class)
            {
                public DomainClientSupport<String> getDomain()
                {
                    return tierIndDomainClient;
                }
            };
        tierIndItem.asSelectItem(SelectType.KEY_LABLE);
        tierIndItem.required(true);
        
        form.setField(uLatePaymentCharge.AssessDays().required(true),
            uLatePaymentCharge.MinAgeCd().required(true),
            uLatePaymentCharge.Threshold().required(true),
            uLatePaymentCharge.MinCharge().required(true),
            uLatePaymentCharge.MaxCharge().required(true),
            uLatePaymentCharge.YearMaxCharge().required(true),
            uLatePaymentCharge.YearMaxCnt().required(true),
            uLatePaymentCharge.CalcBaseInd().required(true).asSelectItem(SelectType.KEY_LABLE),
            tierIndItem,
            uLatePaymentCharge.AssessInd().asCheckBoxItem(),
            uLatePaymentCharge.IsReviewLateFee().asSelectItem(SelectType.LABLE)
        		);
        
        form.getSetting().labelWidth(240).labelAlign("right");
        
        return form;
    }
    
    public void updateView(Data data)
    {
        // 在updateview之前清缓存
        editorForm.getUi().clear();
        dualEditorForm.getUi().clear();
        replaceEditorForm.getUi().clear();
        ratesLayout.getGrid().loadData(StringToData("[]"));
        dualRatesLayout.getGrid().loadData(StringToData("[]"));
        replaceRatesLayout.getGrid().loadData(StringToData("[]"));
        
        if (data.asMapData().getData("latePaymentCharge") != null)
        {
            Data lateData = data.asMapData().getData("latePaymentCharge");
            
            if (data.asMapData().getData("latePaymentCharge").asMapData().getData("chargeRates") != null)
            {
                ratesLayout.setValue(data.asMapData().getData("latePaymentCharge").asMapData().getData("chargeRates"));
            }
            
            editorForm.setFormData(lateData);
            editorForm.setFieldValue(TIERIND_ITEM,
                lateData.asMapData().getString(uLatePaymentCharge.TierInd().getName()));
        }
        
        if (data.asMapData().getData("dualLatePaymentCharge") != null)
        {
            Data dualData = data.asMapData().getData("dualLatePaymentCharge");
            
            if (data.asMapData().getData("dualLatePaymentCharge").asMapData().getData("chargeRates") != null)
            {
                dualRatesLayout.setValue(data.asMapData()
                    .getData("dualLatePaymentCharge")
                    .asMapData()
                    .getData("chargeRates"));
            }
            
            dualEditorForm.setFormData(dualData);
            dualEditorForm.setFieldValue(TIERIND_ITEM,
                dualData.asMapData().getString(uLatePaymentCharge.TierInd().getName()));
        }
        if (data.asMapData().getData("replaceLatePaymentCharge") != null)
        {
            Data replaceData = data.asMapData().getData("replaceLatePaymentCharge");
            
            if (data.asMapData().getData("replaceLatePaymentCharge").asMapData().getData("chargeRates") != null)
            {
            	replaceRatesLayout.setValue(data.asMapData().getData("replaceLatePaymentCharge").asMapData().getData("chargeRates"));
            }
            
            replaceEditorForm.setFormData(replaceData);
            replaceEditorForm.setFieldValue(TIERIND_ITEM,
            		replaceData.asMapData().getString(uLatePaymentCharge.TierInd().getName()));
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
        if (!replaceEditorForm.valid())
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
        formList.add(replaceEditorForm);
        
        return formList;
    }
    
    public MapData getFormValues()
    {
        MapData submitData = new MapData();
        
        ListData charge = ratesLayout.getValue();
        
        MapData overlimitMap = editorForm.getSubmitData().asMapData();
        overlimitMap.put("chargeRates", charge);
        overlimitMap.put(uLatePaymentCharge.TierInd().getName(), editorForm.getFieldValue(TIERIND_ITEM));
        
        submitData.put("latePaymentCharge", overlimitMap);
        
        ListData dualCharge = dualRatesLayout.getValue();
        MapData dualOverlimitMap = dualEditorForm.getSubmitData().asMapData();
        dualOverlimitMap.put(uLatePaymentCharge.TierInd().getName(), dualEditorForm.getFieldValue(TIERIND_ITEM));
        dualOverlimitMap.put("chargeRates", dualCharge);
        
        submitData.put("dualLatePaymentCharge", dualOverlimitMap);
        
        ListData replaceCharge = replaceRatesLayout.getValue();
        MapData replaceOverlimitMap = replaceEditorForm.getSubmitData().asMapData();
        replaceOverlimitMap.put(uLatePaymentCharge.TierInd().getName(), replaceEditorForm.getFieldValue(TIERIND_ITEM));
        replaceOverlimitMap.put("chargeRates", replaceCharge);
        
        submitData.put("replaceLatePaymentCharge", replaceOverlimitMap);
        
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
        if (!replaceEditorForm.valid())
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
        return constants.productLatePaymentChargeInfo();
    }
    
    /**
     * 重载方法
     * 
     * @return
     */
    @Override
    public int getTabIndex()
    {
        return 5;
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
