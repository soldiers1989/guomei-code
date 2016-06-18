package com.sunline.ccs.param.ui.client.product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UFee;
import com.sunline.ccs.infrastructure.shared.map.FeeMapHelper;
import com.sunline.ccs.infrastructure.shared.map.ProductCreditMapHelper;
import com.sunline.ccs.param.def.Fee;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.pcm.facility.client.common.AbstractPcmProductParamLayout;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.tab.client.Tab;
import com.sunline.ui.tab.client.TabItemSetting;
import com.sunline.ui.tab.client.TabSetting;

/**
 * 杂项费用信息
 * 
 * @author
 * @version [版本号, Jul 13, 2015]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
@Singleton
public class FeeLayout extends AbstractPcmProductParamLayout
{
    public static final String PRRAM_ID = "ccs_fee_param";
    
    @Inject
    private UFee uFee;
    
    private KylinForm editorForm;
    
    private KylinForm dualEditorForm;
    
    @Inject
    private ProductCreditConstants constants;
    
    private Tab tab;
    
    /**
     * 获取layout标题
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    public String getLayoutTitle()
    {
        
        return constants.productFeeInfo();
    }
    
    public void createCanvas(VerticalPanel panel)
    {
        panel.setWidth("100%");
        panel.setHeight("100%");
        
        editorForm = createEditorForm();
        
        TabSetting tabSetting = new TabSetting().dblClickToClose(true).dragToMove(true).showSwitch(true);
        tab = new Tab(tabSetting);
        
        panel.add(tab);
        
        // 本币杂项费用
        VerticalPanel feepanel = new VerticalPanel();
        // 信息框
        feepanel.add(editorForm);
        
        TabItemSetting feeTabSetting = new TabItemSetting(null, constants.feeTitle());
        tab.addItem(feeTabSetting, feepanel);
        
        // 外币杂项费用
        dualEditorForm = createEditorForm();
        VerticalPanel dualFeepanel = new VerticalPanel();
        // 信息框
        dualFeepanel.add(dualEditorForm);
        
        TabItemSetting dualFeeTabSetting = new TabItemSetting(null, constants.dualFeeTitle());
        tab.addItem(dualFeeTabSetting, dualFeepanel);
    }
    
    private KylinForm createEditorForm()
    {
        KylinForm form = new KylinForm();
        form.setCol(3);
        form.setWidth("100%");
        
        form.setField(uFee.Description(),
            uFee.FirstCardFeeInd().required(true).asSelectItem(SelectType.KEY_LABLE),
            uFee.FirstCardFeeWaiveInd().asCheckBoxItem(),
            uFee.PrimCardFee().required(true),
            uFee.SuppCardFee().required(true),
            uFee.JoiningFee().required(true),
            uFee.LetterFee().required(true),
            uFee.NsfFee().required(true),
            uFee.SmsFee().required(true),
            uFee.UrgentFee().required(true),
            uFee.WaiveMonthReprintStmt().required(true),
            uFee.UserFee1(),
            uFee.UserFee2(),
            uFee.UserFee3(),
            uFee.UserFee4(),
            uFee.UserFee5(),
            uFee.UserFee6());
        
        form.getSetting().labelWidth(130).labelAlign("right");
        
        return form;
    }
    
    @SuppressWarnings("unchecked")
    public void updateView(Data data)
    {
        // 在updateview之前清缓存
        editorForm.getUi().clear();
        dualEditorForm.getUi().clear();
        
        ProductCredit model = new ProductCredit();
        
        ProductCreditMapHelper.updateFromMap(model, (Map<String, Serializable>)data.asMapData().toMap());
        
        if (model.fee == null)
        {
            model.fee = new Fee();
        }
        
        if (data.asMapData().getData("fee") != null)
        {
            FeeMapHelper.updateFromMap(model.fee, (Map<String, Serializable>)data.asMapData()
                .getData("fee")
                .asMapData()
                .toMap());
            
            editorForm.setFormData(data.asMapData().getData("fee"));
        }
        
        if (model.dualFee == null)
            model.dualFee = new Fee();
        
        if (data.asMapData().getData("dualFee") != null)
        {
            FeeMapHelper.updateFromMap(model.dualFee, (Map<String, Serializable>)data.asMapData()
                .getData("dualFee")
                .asMapData()
                .toMap());
            
            dualEditorForm.setFormData(data.asMapData().getData("dualFee"));
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
        
        submitData.put("fee", editorForm.getSubmitData().asMapData());
        submitData.put("dualFee", dualEditorForm.getSubmitData().asMapData());
        
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
        return editorForm.valid()&&dualEditorForm.valid();
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
        return constants.productFeeInfo();
    }
    
    /**
     * 重载方法
     * 
     * @return
     */
    @Override
    public int getTabIndex()
    {
        return 6;
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
