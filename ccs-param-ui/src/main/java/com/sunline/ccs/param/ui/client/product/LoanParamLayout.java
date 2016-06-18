package com.sunline.ccs.param.ui.client.product;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UProductCredit;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.utils.StringUtils;
import com.sunline.pcm.facility.client.common.AbstractPcmProductParamLayout;
import com.sunline.ui.core.client.common.Field;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;

@Singleton
public class LoanParamLayout extends AbstractPcmProductParamLayout
{
    public static final String PRRAM_ID = "ccs_loan_param";
    
    @Inject
    private ProductCreditConstants constants;
    
    @Inject
    private UProductCredit uProductCredit;
    
    private KylinForm loanParamForm;
    
    public String getLayoutTitle()
    {
        return constants.loanParamInfo();
    }
    
    public void createCanvas(VerticalPanel panel)
    {
        panel.setWidth("100%");
        panel.setHeight("100%");
        
        loanParamForm = new KylinForm();
        loanParamForm.setWidth("100%");
        
        loanParamForm.setCol(2);
        loanParamForm.getSetting().labelWidth(160);
        loanParamForm.setField(uProductCredit.CreditExpiryDateInd().asSelectItem(SelectType.KEY_LABLE),
            uProductCredit.PrvPeriodOfCrExpDate(),
            uProductCredit.PrvReportOfCrExpDateInd().asSelectItem(SelectType.KEY_LABLE),
            uProductCredit.CrExpiredReportInd().asSelectItem(SelectType.KEY_LABLE),
            uProductCredit.CrReApproveReportInd().asSelectItem(SelectType.KEY_LABLE),
            uProductCredit.ExpiryAutoCheck().asSelectItem(SelectType.KEY_LABLE),
            uProductCredit.KeepingDDDays().required(true),
            uProductCredit.DdAfterDelin().asSelectItem(SelectType.KEY_LABLE),
            uProductCredit.DdAfterLastTerm().asSelectItem(SelectType.KEY_LABLE));
        
        panel.add(loanParamForm);
    }
    
    public void updateView(Data data)
    {
        loanParamForm.getUi().clear();
        MapData mapData = data.asMapData();
        List<Field> fields = loanParamForm.getFields();
        for (Field field : fields)
        {
            String value = mapData.getString(field.getName());
            if (StringUtils.isNotEmpty(value))
            {
                loanParamForm.setFieldValue(field.getName(), value);
            }
        }
    }
    
    public boolean updateModel(ProductCredit model)
    {
        
        if (loanParamForm.valid())
        {
            return true;
        }
        
        return false;
    }
    
    @SuppressWarnings("unused")
    private Map<String, Serializable> getFormValues(KylinForm form)
    {
        Map<String, Serializable> map = new HashMap<String, Serializable>();
        
        for (Field field : form.getFields())
        {
            map.put(field.getName(), form.getFieldValue(field.getName()));
        }
        
        return map;
    }
    
    public MapData getFormValues()
    {
        // 获取控件信息
        return loanParamForm.getSubmitData().asMapData();
    }
    
    /**
     * 重载方法
     * 
     * @return
     */
    @Override
    public boolean validForm()
    {
        if (loanParamForm.valid())
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
    public KylinForm getForm()
    {
        return loanParamForm;
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
        return constants.loanParamInfo();
    }
    
    /**
     * 重载方法
     * 
     * @return
     */
    @Override
    public int getTabIndex()
    {
        return 10;
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
        getForm().setFormReadOnly(!flag);
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
