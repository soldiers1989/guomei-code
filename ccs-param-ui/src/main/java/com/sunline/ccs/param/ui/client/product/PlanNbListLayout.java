package com.sunline.ccs.param.ui.client.product;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.domain.i18n.PlanTypeConstants;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.pcm.facility.client.common.AbstractPcmProductParamLayout;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.ListData;
import com.sunline.ui.core.client.data.MapData;

@Singleton
public class PlanNbListLayout extends AbstractPcmProductParamLayout
{
    public static final String PRRAM_ID = "plannb_list";
    
    private KylinForm planNbListForm;
    
    @Inject
    private ProductCreditConstants constants;
    
    @Inject
    private PlanTypeConstants planTypeConstants;
    
    private ListData planTmps;
    
    public String getLayoutTitle()
    {
        return constants.planNbrList();
    }
    
    private void fillPlanTmps(ListData list, PlanType item)
    {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        if (list.size() > 0){
            for (int i = 0; i < list.size(); i++){
                MapData planTmp = list.get(i).asMapData();
                if (planTmp.getString("planType").equals(item.toString())){
                    map.put(planTmp.getString("planNbr"), planTmp.getString("description"));
                }
            planNbListForm.setFieldSelectData(item.toString(),new SelectItem<String>(SelectType.KEY_LABLE).setValue(map));
            }
        }
    }
    
    public void createCanvas(VerticalPanel panel)
    {
        panel.setWidth("100%");
        panel.setHeight("100%");
        
        planNbListForm = new KylinForm();
        
        planNbListForm.setWidth("100%");
        planNbListForm.setCol(2);
        
        List<TextColumnHelper> formList = new ArrayList<TextColumnHelper>();
        
        for (PlanType planType : PlanType.values())
        {
            TextColumnHelper column =
                new TextColumnHelper(planType.toString(),
                    planTypeConstants.getString(planType.toString().toUpperCase()), 100).setNewline(true)
                    .asSelectItem();
            
            formList.add(column);
        }
        
        planNbListForm.setField(formList.toArray(new TextColumnHelper[formList.size()]));
        planNbListForm.getSetting().labelWidth(120);
        
        panel.add(planNbListForm);
    }
    
    @SuppressWarnings("unchecked")
    public void updateView(final Data data)
    {
        planNbListForm.getUi().clear();
        
        //下拉框赋值 
        RPC.ajax("rpc/productCreditServer/getPlanTmps", new RpcCallback<Data>()
        {
            @Override
            public void onSuccess(Data result)
            {
                if (result != null)
                {
                    planTmps = result.asListData();
                    
                    for (PlanType planType : PlanType.values())
                    {
                        fillPlanTmps(planTmps, planType);
                    }
                }
                else
                {
                    planTmps = new ListData();
                }
                
                Map<String, String> planNbrList = (Map<String, String>)data.asMapData().toMap().get("planNbrList");
                
                if (planNbrList != null && !planNbrList.isEmpty())
                {
                    for (PlanType planType : PlanType.values())
                    {
                        //                        PlanType key = PlanType.valueOf(item.getName());
                        planNbListForm.setFieldValue(planType.toString(), planNbrList.get(planType.toString()));
                    }
                }
            }
        });
    }
    
    public KylinForm getForm()
    {
        return planNbListForm;
    }
    
    public MapData getFormValues()
    {
        
        MapData submitData = new MapData();
        submitData.put("planNbrList", planNbListForm.getSubmitData().asListData());
        
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
        return planNbListForm.valid();
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
        return constants.planNbrList();
    }
    
    /**
     * 重载方法
     * 
     * @return
     */
    @Override
    public int getTabIndex()
    {
        return 11;
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
