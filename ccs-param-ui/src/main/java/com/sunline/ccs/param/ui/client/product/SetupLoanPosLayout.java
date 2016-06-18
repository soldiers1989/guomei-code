package com.sunline.ccs.param.ui.client.product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UProductCredit;
import com.sunline.ccs.infrastructure.shared.map.ProductCreditMapHelper;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.kylin.web.ark.client.helper.BooleanColumnHelper;
import com.sunline.kylin.web.ark.client.helper.EnumColumnHelper;
import com.sunline.kylin.web.ark.client.helper.EventEnum;
import com.sunline.kylin.web.ark.client.helper.IntegerColumnHelper;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.pcm.facility.client.common.AbstractPcmProductParamLayout;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ui.core.client.common.Field;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.core.client.function.IFunction;

/**
 * 随交易开卡-指定pos分期开卡
 * 
 * @author
 *
 */
@Singleton
public class SetupLoanPosLayout extends AbstractPcmProductParamLayout
{
    public static final String PRRAM_ID = "setup_loan_pos_param";
    
    @Inject
    private ProductCreditConstants constants;
    
    @Inject
    private UProductCredit uProductCredit;
    
    private KylinForm inactiveForm;
    
    private KylinForm loanPostForm;
    
    // 未激活可交易
    private BooleanColumnHelper inactiveItem;
    
    // 商户号
    private TextColumnHelper merChantNoItem;
    
    // 商户类型
    private TextColumnHelper merchantMCCItem;
    
    // 分期活动编号
    private TextColumnHelper programIdItem;
    
    // 分期期数
    private IntegerColumnHelper loanTermItem;
    
    // 指定POS分期开卡
    private BooleanColumnHelper setupLoanPItem;
    
    // 商品地址信息
    private TextColumnHelper commodityNameAddrInfoItem;
    
    // 分期手续费收取方式
    private EnumColumnHelper<LoanFeeMethod> setupLoanPFeeMethodItem;
    
    private LinkedHashMap<String, String> merChantMap = new LinkedHashMap<String, String>(); // 存放分期商户
    
    private LinkedHashMap<String, String> merchantMccMap = new LinkedHashMap<String, String>(); // 存放分期商户MCC
    
    private LinkedHashMap<String, String> programMap = new LinkedHashMap<String, String>(); // 存放分期活动
    
    private List<Map<String, Serializable>> map = new ArrayList<Map<String, Serializable>>();
    
    private Map<String, Serializable> resultMap;
    
    public String getLayoutTitle()
    {
        return constants.setupLoanPInfo();
    }
    
    public void createCanvas(VerticalPanel panel)
    {
        panel.setWidth("100%");
        panel.setHeight("100%");
        
        inactiveForm = new KylinForm();
        inactiveForm.setWidth("100%");
        inactiveForm.setCol(2);
        
        // 未激活可交易form
        inactiveItem = uProductCredit.InactiveTrade().asCheckBoxItem(); // 未激活可交易
        inactiveItem.bindEvent("change", new IFunction()
        {
            @Override
            public void function()
            {
                validateInactive();
            }
        });
        
        inactiveForm.setField(inactiveItem);
        
        panel.add(inactiveForm);
        
        // 指定POS分期开卡form
        loanPostForm = new KylinForm();
        loanPostForm.setWidth("100%");
        loanPostForm.setCol(2);
        
        setupLoanPItem =
            uProductCredit.SetupLoanP()
                .asCheckBoxItem()
                .setGroup(constants.setupLoanPostTitle())
                .setGroupicon("skins/icons/communication.gif");
        setupLoanPItem.bindEvent("change", new IFunction()
        {
            @Override
            public void function()
            {
                validateSetupLoan();
            }
        });
        
        merChantNoItem = uProductCredit.SetupLoanPMerchantNo().setNewline(true).asSelectItem(); // 商户号
        
        // 根据商户号获取MCC
        merChantNoItem.bindEvent("change", new IFunction()
        {
            @Override
            public void function()
            {
                String itemVal = loanPostForm.getFieldValue(merChantNoItem.getName());
                
                if (itemVal != null && !"".equals(itemVal))
                {
                    loanPostForm.setFieldValue(merchantMCCItem.getName(), "");
                    loanPostForm.setFieldReadOnly(merchantMCCItem.getName(), false);
                    loanPostForm.setFieldValue(merchantMCCItem.getName(), merchantMccMap.get(itemVal));
                }
                else
                {
                    loanPostForm.setFieldReadOnly(merchantMCCItem.getName(), true);
                    loanPostForm.setFieldValue(merchantMCCItem.getName(), "");
                }
            }
        });
        
        merchantMCCItem = uProductCredit.SetupLoanPMerchantMCC().readonly(true); // 商户MCC
        programIdItem = uProductCredit.SetupLoanPProgramId().asSelectItem(); // 分期活动编号
        
        programIdItem.bindEvent(EventEnum.onchange, new IFunction()
        {
            @SuppressWarnings("unchecked")
            @Override
            public void function()
            {
                Boolean inactiveBool = inactiveForm.getFieldBoolValue(inactiveItem.getName());
                Boolean setupLoanPBool = loanPostForm.getFieldBoolValue(setupLoanPItem.getName());
                if (!inactiveBool || !setupLoanPBool)
                {
                    return;
                }
                
                String itemVal = loanPostForm.getFieldValue(programIdItem.getName());
                
                if (itemVal != null && !"".equals(itemVal))
                {
                    LinkedHashMap<String, String> valueMap = new LinkedHashMap<String, String>();
                    
                    for (Map<String, Serializable> program : map)
                    {
                        if (itemVal.equals(program.get("programId")) && program.get("programFeeDef") != null)
                        {
                            Map<String, Object> programMap = (Map<String, Object>)program.get("programFeeDef");
                            
                            for (Entry<String, Object> enty : programMap.entrySet())
                            {
                                valueMap.put(enty.getKey(), enty.getKey());
                            }
                        }
                    }
                    
                    loanPostForm.setFieldReadOnly(loanTermItem.getName(), true);
                    loanPostForm.setFieldValue(loanTermItem.getName(), "");
                    //                    loanPostForm.setFieldRequired(loanTermItem.getName(), true);
                    
                    if (!valueMap.isEmpty())
                    {
                        loanPostForm.setFieldSelectData(loanTermItem.getName(),
                            new SelectItem<String>(SelectType.LABLE).setValue(valueMap));
                    }
                    else
                    {
                        return;
                    }
                }
                else
                {
                    loanPostForm.setFieldValue(loanTermItem.getName(), "");
                    loanPostForm.setFieldRequired(loanTermItem.getName(), false);
                    loanPostForm.setFieldReadOnly(loanTermItem.getName(), false);
                }
            }
        });
        
        loanTermItem = uProductCredit.SetupLoanPInitTerm().asSelectItem(); // 分期期数
        setupLoanPFeeMethodItem =
            uProductCredit.SetupLoanPFeeMethod().columnRender().asSelectItem(SelectType.KEY_LABLE);
        commodityNameAddrInfoItem = uProductCredit.SetupLoanPCommodityNameAddr().setColumnWidth(500);// 商品地址信息
        
        loanPostForm.setField(setupLoanPItem,
            merChantNoItem,
            merchantMCCItem,
            programIdItem,
            loanTermItem,
            setupLoanPFeeMethodItem,
            commodityNameAddrInfoItem);
        loanPostForm.getSetting().labelWidth(130);
        
        panel.add(loanPostForm);
    }
    
    @SuppressWarnings("unchecked")
    public void updateView(Data data)
    {
        ProductCredit model = new ProductCredit();
        ProductCreditMapHelper.updateFromMap(model, (Map<String, Serializable>)data.asMapData().toMap());
        
        // 分期活动编号、分期期数取值方法
        RPC.ajax("rpc/productCreditServer/getProgram", new RpcCallback<Data>()
        {
            @Override
            public void onSuccess(Data result)
            {
                List<Map<String, Serializable>> resList = (List<Map<String, Serializable>>)result.asListData().toList();
                
                if (resList != null && resList.size() > 0)
                {
                    for (int i = 0; i < resList.size(); i++)
                    {
                        Map<String, Serializable> program = resList.get(i);
                        programMap.put((String)program.get("programId"), (String)program.get("programId"));
                    }
                    
                    loanPostForm.setFieldSelectData(programIdItem.getName(),
                        new SelectItem<String>(SelectType.LABLE).setValue(programMap));
                    
                    map.addAll(resList);
                    
                    if (resultMap.get(programIdItem.getName()) != null
                        && !"null".equals(resultMap.get(programIdItem.getName())))
                    {
                        loanPostForm.setFieldValue(programIdItem.getName(),
                            String.valueOf(resultMap.get(programIdItem.getName())));
                    }
                    
                    String itemVal = loanPostForm.getFieldValue(programIdItem.getName());
                    
                    if (itemVal != null && !"".equals(itemVal))
                    {
                        LinkedHashMap<String, String> valueMap = new LinkedHashMap<String, String>();
                        
                        for (Map<String, Serializable> program : map)
                        {
                            if (itemVal.equals(program.get("programId")))
                            {
                                Map<String, Object> programMap = (Map<String, Object>)program.get("programFeeDef");
                                
                                for (Entry<String, Object> enty : programMap.entrySet())
                                {
                                    valueMap.put(enty.getKey(), enty.getKey());
                                }
                            }
                        }
                        
                        loanPostForm.setFieldReadOnly(loanTermItem.getName(), true);
                        loanPostForm.setFieldValue(loanTermItem.getName(), "");
                        //                    loanPostForm.setFieldRequired(loanTermItem.getName(), true);
                        
                        if (!valueMap.isEmpty())
                        {
                            loanPostForm.setFieldSelectData(loanTermItem.getName(), new SelectItem<String>(
                                SelectType.LABLE).setValue(valueMap));
                        }
                        else
                        {
                            return;
                        }
                    }
                    else
                    {
                        loanPostForm.setFieldValue(loanTermItem.getName(), "");
                        loanPostForm.setFieldRequired(loanTermItem.getName(), false);
                        loanPostForm.setFieldReadOnly(loanTermItem.getName(), false);
                    }
                    
                    if (resultMap.get(loanTermItem.getName()) != null
                        && !"null".equals(resultMap.get(loanTermItem.getName())))
                        loanPostForm.setFieldValue(loanTermItem.getName(),
                            String.valueOf(resultMap.get(loanTermItem.getName())));
                }
            }
        });
        
        // 商户号、商户MCC取值方法
        RPC.ajax("rpc/productCreditServer/getLoanMerchant", new RpcCallback<Data>()
        {
            @Override
            public void onSuccess(Data result)
            {
                List<Map<String, Serializable>> resList = (List<Map<String, Serializable>>)result.asListData().toList();
                
                if (resList != null && resList.size() > 0)
                {
                    for (int i = 0; i < resList.size(); i++)
                    {
                        Map<String, Serializable> loanMerchant = resList.get(i);
                        
                        merChantMap.put((String)loanMerchant.get("merId"), loanMerchant.get("merId") + "-"
                            + loanMerchant.get("merName"));
                        merchantMccMap.put((String)loanMerchant.get("merId"), (String)loanMerchant.get("merType"));
                    }
                    
                    loanPostForm.setFieldSelectData(merChantNoItem.getName(),
                        new SelectItem<String>(SelectType.LABLE).setValue(merChantMap));
                    
                }
            }
        });
        
        resultMap = ProductCreditMapHelper.convertToMap(model);
        
        if (resultMap.get(inactiveItem.getName()) == null)
        {
            inactiveForm.setFieldValue(inactiveItem.getName(), Boolean.FALSE);
        }
        else
        {
            inactiveForm.setFieldValue(inactiveItem.getName(), (Boolean)resultMap.get(inactiveItem.getName()));
        }
        
        for (Field field : loanPostForm.getFields())
        {
            if (resultMap.get(field.getName()) != null)
            {
                if (field.getName().equals(setupLoanPItem.getName()))
                {
                    loanPostForm.setFieldValue(field.getName(), (Boolean)resultMap.get(field.getName()));
                }
                else
                {
                    loanPostForm.setFieldValue(field.getName(), String.valueOf(resultMap.get(field.getName())));
                }
            }
        }
        
        validateInactive();
    }
    
    protected void validateInactive()
    {
        Boolean itemBool = inactiveForm.getFieldBoolValue(inactiveItem.getName());
        
        if (itemBool)
        {
            loanPostForm.setFieldReadOnly(setupLoanPItem.getName(), true);
            //            loanPostForm.setFieldRequired(setupLoanPItem.getName(), true);
            
            validateSetupLoan();
        }
        else
        {
            loanPostForm.setFieldValue(setupLoanPItem.getName(), Boolean.FALSE);
            loanPostForm.setFieldReadOnly(setupLoanPItem.getName(), false);
            
            validateSetupLoan();
        }
    }
    
    private void validateSetupLoan()
    {
        Boolean itemBool = loanPostForm.getFieldBoolValue(setupLoanPItem.getName());
        
        if (itemBool)
        {
            loanPostForm.setFieldRequired(merChantNoItem.getName(), true);
            loanPostForm.setFieldReadOnly(merChantNoItem.getName(), true);
            
            loanPostForm.setFieldRequired(programIdItem.getName(), true);
            loanPostForm.setFieldReadOnly(programIdItem.getName(), true);
            
            loanPostForm.setFieldRequired(commodityNameAddrInfoItem.getName(), true);
            loanPostForm.setFieldReadOnly(commodityNameAddrInfoItem.getName(), true);
            
            loanPostForm.setFieldReadOnly(setupLoanPFeeMethodItem.getName(), true);
        }
        else
        {
            loanPostForm.setFieldValue(merChantNoItem.getName(), "");
            loanPostForm.setFieldRequired(merChantNoItem.getName(), false);
            loanPostForm.setFieldReadOnly(merChantNoItem.getName(), false);
            
            loanPostForm.setFieldReadOnly(merchantMCCItem.getName(), false);
            loanPostForm.setFieldValue(merchantMCCItem.getName(), "");
            
            loanPostForm.setFieldValue(programIdItem.getName(), "");
            loanPostForm.setFieldRequired(programIdItem.getName(), false);
            loanPostForm.setFieldReadOnly(programIdItem.getName(), false);
            
            loanPostForm.setFieldValue(loanTermItem.getName(), "");
            loanPostForm.setFieldRequired(loanTermItem.getName(), false);
            loanPostForm.setFieldReadOnly(loanTermItem.getName(), false);
            
            loanPostForm.setFieldValue(setupLoanPFeeMethodItem.getName(), "");
            loanPostForm.setFieldReadOnly(setupLoanPFeeMethodItem.getName(), false);
            
            loanPostForm.setFieldValue(commodityNameAddrInfoItem.getName(), "");
            loanPostForm.setFieldRequired(commodityNameAddrInfoItem.getName(), false);
            loanPostForm.setFieldReadOnly(commodityNameAddrInfoItem.getName(), false);
        }
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
        MapData submitData = loanPostForm.getSubmitData().asMapData();
        
        MapData.extend(submitData, inactiveForm.getSubmitData().asMapData(), false);
        
        return submitData;
    }
    
    public List<KylinForm> getForms()
    {
        List<KylinForm> formList = new ArrayList<KylinForm>();
        formList.add(loanPostForm);
        formList.add(inactiveForm);
        
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
        if (!inactiveForm.valid())
        {
            return false;
        }
        
        if (!loanPostForm.valid())
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
        return constants.setupLoanPInfo();
    }
    
    /**
     * 重载方法
     * 
     * @return
     */
    @Override
    public int getTabIndex()
    {
        return 9;
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
