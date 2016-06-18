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
import com.sunline.ark.support.def.DomainClientSupport;
import com.sunline.ccs.infrastructure.client.domain.LoanFeeMethodDomainClient;
import com.sunline.ccs.infrastructure.client.domain.LoanTypeDomainClient;
import com.sunline.ccs.infrastructure.client.ui.UProductCredit;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.kylin.web.ark.client.helper.BooleanColumnHelper;
import com.sunline.kylin.web.ark.client.helper.DecimalColumnHelper;
import com.sunline.kylin.web.ark.client.helper.EnumColumnHelper;
import com.sunline.kylin.web.ark.client.helper.IntegerColumnHelper;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.pcm.facility.client.common.AbstractPcmProductParamLayout;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ui.core.client.common.Field;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.ListData;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.core.client.function.IFunction;

/**
 * 分期产品
 * 
 * @author
 * @version [版本号, Jul 13, 2015]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
@Singleton
public class LoanPlanLayout extends AbstractPcmProductParamLayout
{
    public static final String PRRAM_ID = "ccs_loan_plan";
    
    @Inject
    private ProductCreditConstants constants;
    
    @Inject
    private LoanTypeDomainClient loanTypeDomainClient;
    
    @Inject
    private LoanFeeMethodDomainClient loanFeeMethodDomainClient;
    
    @Inject
    private UProductCredit uProductCredit;
    
    private KylinForm form;
    
    private KylinForm autoLoanForm;
    
    private TextColumnHelper rItem; // 消费转分期
    
    private TextColumnHelper cItem; // 现金分期
    
    private TextColumnHelper bItem; // 账单分期
    
    private TextColumnHelper mcatItem; // 随借随还
    
    private TextColumnHelper mcepItem; // 等额本金
    
    private TextColumnHelper mceiItem; // 等额本息
    
    private BooleanColumnHelper autoLoanItem; // 消费自动转分期
    
    private TextColumnHelper loanRItem;// 消费转分期产品
    
    private DecimalColumnHelper minLoanRAmtItem;// 最低转分期消费金额
    
    private IntegerColumnHelper autoLoanRInitTermItem;// 分期总期数
    
    private EnumColumnHelper<LoanFeeMethod> autoLoanRFeeMethodItem;// 分期手续费收取方式
    
    private EnumColumnHelper<LoanType> defaultTypeItem;//默认产品类型
    private LinkedHashMap<String, String> rLoanTypeMap;
    
    private LinkedHashMap<String, String> loanMethodMap;
    
    private Map<String, Map<String, LoanFeeDef>> loanFeeDefMap;
    
    private Map<String, LoanFeeDef> data;
    
    private LinkedHashMap<String, String> loanNumMap;
    
    /**
     * 获取layout标题 <功能详细描述>
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    public String getLayoutTitle()
    {
        
        return constants.loanCodeInfo();
    }
    
    public void createCanvas(VerticalPanel panel)
    {
        panel.setWidth("100%");
        panel.setHeight("100%");
        
        LinkedHashMap<String, String> planTypeMap = loanTypeDomainClient.asLinkedHashMap(false);
        
        loanMethodMap = loanFeeMethodDomainClient.asLinkedHashMap(true);
        
        form = new KylinForm();
        {
            form.setWidth("85%");
            // 消费转分期
            rItem =
                new TextColumnHelper(LoanType.R.name(), planTypeMap.get("R"), 2000).asSelectItem().bindEvent("change",
                    new IFunction()
                    {
                        
                        @Override
                        public void function()
                        {
                            // 消费转分期类型改变，则分期总期数和分期手续费收取方式下拉框中的内容会产生改变
                            String loanCode = form.getFieldValue(rItem.getName());
                            changeAutoLoanInitData(loanCode);
                        }
                    });
            // 现金分期
            cItem = new TextColumnHelper(LoanType.C.name(), planTypeMap.get("C"), 200).asSelectItem();
            // 账单分期
            bItem = new TextColumnHelper(LoanType.B.name(), planTypeMap.get("B"), 200).asSelectItem();
            //随借随还
            mcatItem = new TextColumnHelper(LoanType.MCAT.name(), planTypeMap.get("MCAT"), 200).asSelectItem();
            //等额本金
            mcepItem = new TextColumnHelper(LoanType.MCEP.name(), planTypeMap.get("MCEP"), 200).asSelectItem();
            //等额本息
            mceiItem = new TextColumnHelper(LoanType.MCEI.name(), planTypeMap.get("MCEI"), 200).asSelectItem();
            //默认产品类型
            defaultTypeItem = uProductCredit.DefaultLoanType();
            form.setField(rItem.setNewline(true), cItem.setNewline(true), bItem.setNewline(true),
                          mcatItem.setNewline(true),mcepItem.setNewline(true),mceiItem.setNewline(true)
                          ,defaultTypeItem.asSelectItem(SelectType.KEY_LABLE).setNewline(true));
            
        }
        
        autoLoanForm = new KylinForm();
        {
            autoLoanForm.setWidth("85%");
            
            // 如果“自动消费转分期”勾选了，则最低转分期消费金额、分期总期数必须填写
            autoLoanItem = uProductCredit.AutoLoanR().asCheckBoxItem().bindEvent("change", new IFunction()
            {
                @Override
                public void function()
                {
                    vaidateAutoLoan();
                }
            });
            
            // 消费转分期产品描述
            loanRItem = uProductCredit.LoanRDesc().readonly(true).setNewline(true);
            // 最低转分期消费金额
            minLoanRAmtItem = uProductCredit.MinAutoLoanRAmt().setNewline(true);
            // 分期总期数
            autoLoanRInitTermItem = uProductCredit.AutoLoanRInitTerm().asSelectItem().setNewline(true);
            // 分期手续费收取方式
            autoLoanRFeeMethodItem =
                uProductCredit.AutoLoanRFeeMethod().asSelectItem(SelectType.LABLE).setNewline(true);
            
            autoLoanForm.setField(autoLoanItem.setGroup(constants.autoLoanTtitle())
                .setGroupicon("skins/icons/communication.gif"),
                loanRItem,
                minLoanRAmtItem,
                autoLoanRInitTermItem,
                autoLoanRFeeMethodItem);
            
            autoLoanForm.getSetting().labelWidth(150).labelAlign("right");
        }
        
        panel.add(form);
        panel.add(autoLoanForm);
    }
    
    private void changeAutoLoanInitData(String loanCode)
    {
        if (loanCode != null && !"".equals(loanCode))
        {
            if (loanFeeDefMap != null && loanFeeDefMap.size() > 0)
            {
                data = loanFeeDefMap.get(loanCode);
                loanNumMap = new LinkedHashMap<String, String>();
                if (data != null)
                {
                    for (Entry<String, LoanFeeDef> entry : data.entrySet())
                    {
                        // Integer loanNum = Integer.valueOf();
                        loanNumMap.put(String.valueOf((entry.getKey().toString())),
                            String.valueOf(entry.getKey().toString()));
                    }
                    
                    autoLoanForm.setFieldValue(loanRItem.getName(), rLoanTypeMap.get(loanCode));
                    autoLoanForm.setFieldSelectData(autoLoanRFeeMethodItem.getName(), new SelectItem<String>(
                        SelectType.LABLE).setValue(loanMethodMap));
                    autoLoanForm.setFieldSelectData(autoLoanRInitTermItem.getName(), new SelectItem<String>(
                        SelectType.LABLE).setValue(loanNumMap));
                }
            }
        }
    }
    
    private void vaidateAutoLoan()
    {
        
        Boolean autoLoanBool = autoLoanForm.getFieldBoolValue(autoLoanItem.getName());
        
        // 如果“自动消费转分期”勾选了，则最低转分期消费金额、分期总期数必须填写
        // 且最低转分期消费金额、分期总期数、分期手续费收取方式为可编辑
        
        if (autoLoanBool)
        {
            autoLoanForm.setFieldReadOnly(minLoanRAmtItem.getName(), true);
            autoLoanForm.setFieldReadOnly(autoLoanRInitTermItem.getName(), true);
            autoLoanForm.setFieldReadOnly(autoLoanRFeeMethodItem.getName(), true);
            autoLoanForm.setFieldRequired(autoLoanRFeeMethodItem.getName(), true);
            autoLoanForm.setFieldRequired(minLoanRAmtItem.getName(), true);
            autoLoanForm.setFieldRequired(autoLoanRInitTermItem.getName(), true);
        }
        else
        {
            autoLoanForm.setFieldReadOnly(minLoanRAmtItem.getName(), false);
            autoLoanForm.setFieldReadOnly(autoLoanRFeeMethodItem.getName(), false);
            autoLoanForm.setFieldReadOnly(autoLoanRInitTermItem.getName(), false);
            autoLoanForm.setFieldRequired(autoLoanRFeeMethodItem.getName(), false);
            autoLoanForm.setFieldRequired(minLoanRAmtItem.getName(), false);
            autoLoanForm.setFieldRequired(autoLoanRInitTermItem.getName(), false);
            autoLoanForm.setFieldValue(minLoanRAmtItem.getName(), "");
            autoLoanForm.setFieldValue(autoLoanRInitTermItem.getName(), "");
            autoLoanForm.setFieldValue(autoLoanRFeeMethodItem.getName(), "");
        }
        
    }
    
    @SuppressWarnings("unchecked")
    public void updateView(final Data srcData)
    {
        
        // 清空表格数据
        form.getUi().clear();
        autoLoanForm.getUi().clear();
        
        if (loanFeeDefMap == null)
        {
            loanFeeDefMap = new HashMap<String, Map<String, LoanFeeDef>>();
        }
        
        //        Map<String, Serializable> srcDataMap = (Map<String, Serializable>)srcData.asMapData().toMap();
        
        Map<String, String> loanPlansMap = new HashMap<String, String>();
        
        if (srcData.asMapData().getData("loanPlansMap") != null)
        {
            loanPlansMap = (Map<String, String>)srcData.asMapData().getData("loanPlansMap").asMapData().toMap();
        }
		if (srcData.asMapData().getString("defaultLoanType") != null)
	    {
			form.setFieldValue(uProductCredit.DefaultLoanType().getName(),srcData.asMapData().getString("defaultLoanType"));
	    }
        final Map<String, String> loanPlans = loanPlansMap;
 
        RPC.ajax("rpc/productCreditServer/getLoanPlans", new RpcCallback<Data>()
        {
            @Override
            public void onSuccess(Data result)
            {
                if (rLoanTypeMap == null)
                {
                    rLoanTypeMap = new LinkedHashMap<String, String>();
                }
                LinkedHashMap<String, String> cLoanTypeMap = new LinkedHashMap<String, String>();
                LinkedHashMap<String, String> bLoanTypeMap = new LinkedHashMap<String, String>();
                LinkedHashMap<String, String> mcatLoanTypeMap = new LinkedHashMap<String, String>();
                LinkedHashMap<String, String> mcepLoanTypeMap = new LinkedHashMap<String, String>();
                LinkedHashMap<String, String> mceiLoanTypeMap = new LinkedHashMap<String, String>();
//                LinkedHashMap<String, String> defaultTypeMap = new LinkedHashMap<String, String>();
                
                ListData rLoanType = new ListData();
                ListData cLoanType = new ListData();
                ListData bLoanType = new ListData();
                ListData mcatLoanType = new ListData();
                ListData mcepLoanType = new ListData();
                ListData mceiLoanType = new ListData();
                
                //如果不存在响应的数据，则转换为ListData的过程中可能异常
                if(result.asMapData().getData(LoanType.R.name())!=null)
                	rLoanType=result.asMapData().getData(LoanType.R.name()).asListData();
                if(result.asMapData().getData(LoanType.C.name())!=null)
                	cLoanType = result.asMapData().getData(LoanType.C.name()).asListData();
                if(result.asMapData().getData(LoanType.B.name())!=null)
                	bLoanType = result.asMapData().getData(LoanType.B.name()).asListData();
                if(result.asMapData().getData(LoanType.MCAT.name())!=null)
                	mcatLoanType = result.asMapData().getData(LoanType.MCAT.name()).asListData();
                if(result.asMapData().getData(LoanType.MCEP.name())!=null)
                	mcepLoanType = result.asMapData().getData(LoanType.MCEP.name()).asListData();
                if(result.asMapData().getData(LoanType.MCEI.name())!=null)
                	mceiLoanType = result.asMapData().getData(LoanType.MCEI.name()).asListData();
                
                if (rLoanType != null)
                {
                    for (int i = 0; i < rLoanType.size(); i++)
                    {
                        loanFeeDefMap.put(rLoanType.get(i).asMapData().getString("loanCode"),
                            (Map<String, LoanFeeDef>)rLoanType.get(i)
                                .asMapData()
                                .getData("loanFeeDefMap")
                                .asMapData()
                                .toMap());
                        rLoanTypeMap.put(rLoanType.get(i).asMapData().getString("loanCode"), rLoanType.get(i)
                            .asMapData()
                            .getString("description"));
                    }
                }
                
                if (bLoanType != null)
                {
                    for (int i = 0; i < bLoanType.size(); i++)
                    {
                        bLoanTypeMap.put(bLoanType.get(i).asMapData().getString("loanCode"), bLoanType.get(i)
                            .asMapData()
                            .getString("description"));
                    }
                }
                
                if (cLoanType != null)
                {
                    for (int i = 0; i < cLoanType.size(); i++)
                    {
                        cLoanTypeMap.put(cLoanType.get(i).asMapData().getString("loanCode"), cLoanType.get(i)
                            .asMapData()
                            .getString("description"));
                    }
                }
                
                if (mcatLoanType != null)
                {
                    for (int i = 0; i < mcatLoanType.size(); i++)
                    {
                	mcatLoanTypeMap.put(mcatLoanType.get(i).asMapData().getString("loanCode"), mcatLoanType.get(i)
                	                 .asMapData()
                	                 .getString("description"));
                    }
                }
                //如果未配置该类型的产品
                if (mcepLoanType != null)
                {
                    for (int i = 0; i < mcepLoanType.size(); i++)
                    {
                	mcepLoanTypeMap.put(mcepLoanType.get(i).asMapData().getString("loanCode"), mcepLoanType.get(i)
                	                    .asMapData()
                	                    .getString("description"));
                    }
                }
                if (mceiLoanType != null)
                {
                    for (int i = 0; i < mceiLoanType.size(); i++)
                    {
                	mceiLoanTypeMap.put(mceiLoanType.get(i).asMapData().getString("loanCode"), mceiLoanType.get(i)
                	                    .asMapData()
                	                    .getString("description"));
                    }
                }
//                if (defaultTypeData != null)
//                {
//                    for (int i = 0; i < defaultTypeData.size(); i++)
//                    {
//                    	defaultTypeMap.put(defaultTypeData.get(i).asMapData().getString("loanCode"), defaultTypeData.get(i)
//                	                    .asMapData()
//                	                    .getString("description"));
//                    }
//                }
                
                form.setFieldSelectData(rItem.getName(), new SelectItem<String>().setValue(rLoanTypeMap));
                form.setFieldSelectData(bItem.getName(), new SelectItem<String>().setValue(bLoanTypeMap));
                form.setFieldSelectData(cItem.getName(), new SelectItem<String>().setValue(cLoanTypeMap));
                form.setFieldSelectData(mcatItem.getName(), new SelectItem<String>().setValue(mcatLoanTypeMap));
                form.setFieldSelectData(mcepItem.getName(), new SelectItem<String>().setValue(mcepLoanTypeMap));
                form.setFieldSelectData(mceiItem.getName(), new SelectItem<String>().setValue(mceiLoanTypeMap));
                if (loanPlans != null)
                {
                    if (loanPlans.get(LoanType.R.name()) != null)
                    {
                        String loanCode = loanPlans.get(LoanType.R.name());
                        changeAutoLoanInitData(loanCode);
                        form.setFieldValue(rItem.getName(), loanPlans.get(LoanType.R.name()));
                    }
                    
                    if (loanPlans.get(LoanType.B.name()) != null)
                    {
                        form.setFieldValue(bItem.getName(), loanPlans.get(LoanType.B.name()));
                    }
                    
                    if (loanPlans.get(LoanType.C.name()) != null)
                    {
                        form.setFieldValue(cItem.getName(), loanPlans.get(LoanType.C.name()));
                    }
                    if (loanPlans.get(LoanType.MCEI.name()) != null)
                    {
                        String loanCode = loanPlans.get(LoanType.MCEI.name());
                        changeAutoLoanInitData(loanCode);
                        form.setFieldValue(mceiItem.getName(), loanPlans.get(LoanType.MCEI.name()));
                    }
                    
                    if (loanPlans.get(LoanType.MCAT.name()) != null)
                    {
                        form.setFieldValue(mcatItem.getName(), loanPlans.get(LoanType.MCAT.name()));
                    }
                    
                    if (loanPlans.get(LoanType.MCEP.name()) != null)
                    {
                        form.setFieldValue(mcepItem.getName(), loanPlans.get(LoanType.MCEP.name()));
                    }
                }
                
                // 给autoLoanForm表单赋初始值
                autoLoanForm.setFormData(srcData);
                
                // 初始化表格
                vaidateAutoLoan();
            }
        });
    }
    
    public boolean updateModel(ProductCredit productCredit)
    {
        if (!autoLoanForm.valid())
        {
            return false;
        }
        return true;
    }
    
    @SuppressWarnings("unused")
    private Map<String, Serializable> getFormValues(KylinForm form)
    {
        Map<String, Serializable> map = new HashMap<String, Serializable>();
        
        for (Field field : form.getFields())
        {
        	if(!"defaultLoanType".equals(field.getName()))
            map.put(field.getName(), form.getFieldValue(field.getName()));
        }
        
        return map;
    }
    
    public MapData getFormValues()
    {
        MapData submitData = autoLoanForm.getSubmitData().asMapData();
        MapData data=form.getSubmitData().asMapData();
        data.remove("defaultLoanType");
        submitData.put("loanPlansMap", data.asListData());
        //默认产品类型不放在loanPlanMap中
        submitData.put("defaultLoanType",form.getFieldValue(uProductCredit.DefaultLoanType().getName()));
        return submitData;
    }
    
    public List<KylinForm> getForms()
    {
        List<KylinForm> formList = new ArrayList<KylinForm>();
        formList.add(form);
        formList.add(autoLoanForm);
        
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
        if (!autoLoanForm.valid())
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
        return constants.loanCodeInfo();
    }
    
    /**
     * 重载方法
     * 
     * @return
     */
    @Override
    public int getTabIndex()
    {
        return 8;
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
