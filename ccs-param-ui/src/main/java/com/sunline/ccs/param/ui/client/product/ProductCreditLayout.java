package com.sunline.ccs.param.ui.client.product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.sunline.ark.support.meta.PropertyInfo;
import com.sunline.ccs.infrastructure.client.ui.UAccountAttribute;
import com.sunline.ccs.infrastructure.client.ui.UProductCredit;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.pcm.facility.client.common.AbstractPcmProductParamLayout;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;

/**
 * 贷记卡产品参数页面
 * 账务参数信息
 * @author lisy
 * @version [版本号, Jun 22, 2015]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class ProductCreditLayout extends AbstractPcmProductParamLayout
{
    public static final String PRRAM_ID = "ccs_credit_param";
    
    @Inject
    private ProductCreditConstants constants;
    
    private KylinForm form;
    
    @Inject
    private UProductCredit uProductCredit;
    
    @Inject
    private UAccountAttribute uAccountAttribute;         //最小还款额
    
    public String getLayoutTitle()
    {
        return constants.productCreditInfo();
    }
    
    public void createCanvas(VerticalPanel panel)
    {
        panel.setWidth("100%");
        panel.setHeight("100%");
        form = new KylinForm();
        form.setWidth("100%");
        form.setCol(3);
        form.setField(uProductCredit.ProductCd().readonly(true),
            uProductCredit.AccountAttributeId().required(true).asSelectItem().setNewline(true),
            uProductCredit.DualAccountAttributeId().asSelectItem(),
            uProductCredit.CardMailMethodInd().required(true).asSelectItem(SelectType.KEY_LABLE),
            uProductCredit.PostCurrCd().required(true).asSelectItem(),
            uProductCredit.DualCurrCd().asSelectItem(),
            uProductCredit.DfltCycleDay().required(true),
            uProductCredit.AthMatchTolRt().required(true),
            uProductCredit.DayAtmLimit().required(true),
            uProductCredit.DayAtmNbr().required(true),
            uProductCredit.DayCashAmtLimit().required(true),
            uProductCredit.DayCashNbrLimit().required(true),
            uProductCredit.DayRetailAmtLimit().required(true),
            uProductCredit.DayRetailNbrLimit().required(true),
            uProductCredit.DayXfroutAmtLimit().required(true),
            uProductCredit.DayXfroutNbrLimit().required(true),
            uProductCredit.DayCupxbAtmLimit().required(true),
            uProductCredit.CvvBlockResetInd().required(true).asSelectItem(SelectType.KEY_LABLE),
            uProductCredit.CvvTry().required(true),
            uProductCredit.Cvv2Try().required(true),
            uProductCredit.IcvvTry().required(true),
            uProductCredit.Cvv2BlockResetInd().asSelectItem(SelectType.KEY_LABLE).required(true),
            uProductCredit.IcvvBlockResetInd().asSelectItem(SelectType.KEY_LABLE).required(true),
            uProductCredit.InqPinBlockResetInd().required(true).asSelectItem(SelectType.KEY_LABLE),
            uProductCredit.MaxInqPinTry().required(true),
            uProductCredit.PinBlockResetInd().required(true).asSelectItem(SelectType.KEY_LABLE),
            uProductCredit.PinTry().required(true),
            uProductCredit.PreathCompTolRt().required(true),
            uProductCredit.DefaultSmsAmt().required(true),
            uProductCredit.PreathRtnPrd().required(true),
            uProductCredit.OldPreAuthVoidSuppInd().asSelectItem(SelectType.KEY_LABLE),
            uProductCredit.UnmatchCrRtnPrd().required(true),
            uProductCredit.UnmatchDbRtnPrd().required(true),
            uProductCredit.MultiCashLoanInd().asSelectItem(SelectType.KEY_LABLE).required(true),
            uProductCredit.MaxCashLoanCnt().required(true),
            uProductCredit.CashLoanLimitType().asSelectItem(SelectType.KEY_LABLE).required(true),
            uProductCredit.MultiSpecCashLoanInd().asSelectItem(SelectType.KEY_LABLE).required(true),
            uProductCredit.MaxSpecCashLoanCnt().required(true),
            uProductCredit.LostToSendMessageDays(),
            uProductCredit.LostToChangeCardDays(),
            uProductCredit.PurchasePinInd().asCheckBoxItem().setNewline(true),
            uProductCredit.CotbInclCrath().asCheckBoxItem(),
            uProductCredit.CotbInclCrbal().asCheckBoxItem(),
            uProductCredit.OtbInclCrath().asCheckBoxItem(),
            uProductCredit.OtbInclCrbal().asCheckBoxItem(),
            uProductCredit.OtbInclDspt().asCheckBoxItem(),
            uProductCredit.OtbInclFrzn().asCheckBoxItem(),
            uProductCredit.LoanSuppInd().asCheckBoxItem(),
            uProductCredit.SpecLoanSuppInd().asCheckBoxItem(),
            uProductCredit.TransferDeditOverdrawValid().asCheckBoxItem(),
            uProductCredit.AutoDCAmtLimit().required(true),
            uProductCredit.BatchDCInd(),
            uProductCredit.DormentDays(),
            uProductCredit.IsReviewInt().asSelectItem(SelectType.KEY_LABLE).required(true),
            uProductCredit.WithdrawLowlimit().required(true),             
            uProductCredit.RepayLowlimit().required(true),
            uProductCredit.IsGracedayIntWaive().asSelectItem(SelectType.KEY_LABLE).required(true)
            );
        
        form.getSetting().labelWidth(200).labelAlign("right");
        
        panel.add(form);
    }
    
    public void updateView(final Data data)
    {
        // 在updateview之前清缓存
        form.getUi().clear();
        
        // 更新本币外币账户参数标识下拉框
        RPC.ajax("rpc/ccsSelectOptionServer/getAcctDescription", new RpcCallback<Data>()
        {
            
            @Override
            public void onSuccess(Data result)
            {
                SelectItem<String> accountVal = new SelectItem<String>(SelectType.KEY_LABLE);
                accountVal.setValue(result.asListData());
                
                SelectItem<String> dualAccountVal = new SelectItem<String>(SelectType.KEY_LABLE);
                dualAccountVal.setValue(result.asListData());
                form.setFieldSelectData(uProductCredit.AccountAttributeId().getName(), accountVal);
                form.setFieldSelectData(uProductCredit.DualAccountAttributeId().getName(), dualAccountVal);
                
                // 更新本币外币入账币种参数标识下拉框
                RPC.ajax("rpc/ccsSelectOptionServer/getCurrencyCdList", new RpcCallback<Data>()
                {
                    
                    @Override
                    public void onSuccess(Data result)
                    {
                        SelectItem<String> dualCurrCdVal = new SelectItem<String>(SelectType.KEY_LABLE);
                        dualCurrCdVal.setValue(result.asListData());
                        
                        SelectItem<String> postCurrCdVal = new SelectItem<String>(SelectType.KEY_LABLE);
                        postCurrCdVal.setValue(result.asListData());
                        form.setFieldSelectData(uProductCredit.DualCurrCd().getName(), dualCurrCdVal);
                        form.setFieldSelectData(uProductCredit.PostCurrCd().getName(), postCurrCdVal);
                        
                        form.setFormData(data);
                    }
                });
            }
        });
    }
    
    public MapData getFormValues()
    {
        MapData submitData = form.getSubmitData().asMapData();
        return submitData;
    }
    
    public List<KylinForm> getForms()
    {
        List<KylinForm> formList = new ArrayList<KylinForm>();
        formList.add(form);
        
        return formList;
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
        return constants.productCreditInfo();
    }
    
    /**
     * 重载方法
     * 
     * @return
     */
    @Override
    public int getTabIndex()
    {
        return 3;
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
