package com.sunline.ccs.param.ui.client.organization;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UOrganization;
import com.sunline.ccs.param.def.Organization;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.pcm.facility.client.common.AbstractPcmProductParamLayout;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;

@Singleton
public class CcsOrgLayout extends AbstractPcmProductParamLayout
{
    public static final String PRRAM_ID = "ccs_loan_param";
    
    @Inject
    protected OrgConstants orgConstants;
    
    @Inject
    private CcsOrgConstants constants;
    
    @Inject
    private UOrganization uOrgParam;
    
    //贷记参数管理Form
    private KylinForm form;
    
    //贷款参数Form
    private KylinForm loanForm;
    
    /**
     * 获取layout标题
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    public String getLayoutTitle()
    {
        
        return constants.orgCcs();
    }
    
    public void createCanvas(VerticalPanel panel)
    {
        panel.setWidth("100%");
        panel.setHeight("100%");
        
        form = new KylinForm();
        
        form.setWidth("100%");
        
        form.setField(uOrgParam.BaseCurrCd()
            .required(true)
            .setGroup(constants.orgCcs())
            .setGroupicon("skins/icons/communication.gif"),
            uOrgParam.BaseCurrexp().required(true),
            uOrgParam.DaysBeforeClose().required(true),
            uOrgParam.DefaultBranchId(),
            uOrgParam.DepositPlanNbr().required(true),
            uOrgParam.DualEnableInd().asCheckBoxItem(),
            uOrgParam.GlLevel().required(true),
            uOrgParam.LimitControlType().required(true).asSelectItem(SelectType.KEY_LABLE),
            uOrgParam.MaxCreditLimit().required(true),
            uOrgParam.MaxCycChange().required(true),
            uOrgParam.NofullpayDeferInd().asSelectItem(SelectType.KEY_LABLE).required(true),
            uOrgParam.OverlimitDeferInd().asSelectItem(SelectType.KEY_LABLE).required(true),
            uOrgParam.RenewFrequency().required(true),
            uOrgParam.RnwlRvwMth().required(true),
            uOrgParam.StmtRtnPrd().required(true),
            uOrgParam.TempLimitMaxMths().required(true),
            uOrgParam.TlMaxRt().required(true),
            uOrgParam.CrRevFloorLimit().required(true),
            uOrgParam.NoCardSelfDiscern().required(true).asSelectItem(SelectType.KEY_LABLE),
            uOrgParam.CashLoanSendMode().asSelectItem(SelectType.KEY_LABLE).required(true),
            uOrgParam.IntPostOnPlan().asCheckBoxItem().setNewline(true),
            uOrgParam.CreditVoidOtbCtrlInd().asCheckBoxItem(),
            uOrgParam.CreditReverseOtbCtrlInd().asCheckBoxItem(),
            uOrgParam.NeedRds().asCheckBoxItem(),
            uOrgParam.GlAgeCdConsistentWithAgeCd().asCheckBoxItem(),
            uOrgParam.CashLoanNeedAudit().asCheckBoxItem().setNewline(true));
        
        form.setCol(3);
        form.getSetting().labelWidth(170);
        //	form.getSetting().labelAlign("right");
        panel.add(form);
        
        loanForm = new KylinForm();
        loanForm.setCol(2);
        loanForm.getSetting().labelWidth(140);
        
        loanForm.setField(uOrgParam.MicroLoanSupport()
            .required(true)
            .setColumnWidth(240)
            .asSelectItem(SelectType.KEY_LABLE)
            .setGroup(constants.loanCcs())
            .setGroupicon("skins/icons/communication.gif"));
        panel.add(loanForm);
    }
    
    public void updateView(Data model)
    {
        form.getUi().clear();
        loanForm.getUi().clear();
        
        if (model != null)
        {
            form.setFormData(model);
            loanForm.setFormData(model);
        }
    }
    
    public List<KylinForm> getForms()
    {
        List<KylinForm> formList = new ArrayList<KylinForm>();
        formList.add(form);
        formList.add(loanForm);
        
        return formList;
    }
    
    public MapData getFormValues()
    {
        MapData submitData = new MapData();
        
        MapData.extend(submitData, form.getSubmitData().asMapData(), false);
        
        MapData.extend(submitData, loanForm.getSubmitData().asMapData(), false);
        
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
        if (!form.valid())
            return false;
        if (!loanForm.valid())
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
        return orgConstants.orgDetail();
    }
    
    /**
     * 重载方法
     * 
     * @return
     */
    @Override
    public int getTabIndex()
    {
        return 0;
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
        return Organization.class;
    }
}
