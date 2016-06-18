package com.sunline.ccs.param.ui.client.loanParam;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.ULoanPlan;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.utils.StringUtils;
import com.sunline.kylin.web.core.client.Flat;
import com.sunline.kylin.web.core.client.mvp.Token;
import com.sunline.kylin.web.core.client.res.SavePage;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.core.client.function.IFunction;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.tab.client.Tab;
import com.sunline.ui.tab.client.TabItemSetting;
import com.sunline.ui.tab.client.TabSetting;

/**
 * 贷款参数定义明细
 * 
 * @author fanghj
 *
 */
@Singleton
public class LoanParamDetailPage extends SavePage
{
    public static final String KEY = "loanCode";
    
    private String loanCode;
    
    private KylinForm detailForm;
    
    @Inject
    private ULoanPlan uLoanPlan;
    
    @Inject
    private LoanParamConstants constants;
    
    private Tab tab;
    
    @Inject
    private LoanFeeDefLayout loanFeeDefLayout;
    
    private Map<String, MapData> loanFeeDefDataMap;
    
    @Override
    public IsWidget createPage()
    {
        VerticalPanel layout = new VerticalPanel();
        layout.setWidth("100%");
        detailForm = new KylinForm();
        {
            detailForm.setCol(4);
            detailForm.setWidth("98%");
            detailForm.getSetting().labelWidth(120).labelAlign("right");
            TextColumnHelper loanType = new TextColumnHelper(uLoanPlan.LoanType().getName(), constants.loanType(), 10);
            loanType.asSelectItem().setColumnWidth(150);
            loanType.bindEvent("change", new IFunction()
            {
                @Override
                public void function()
                {
                    validateCycle();
                }
            });
            
            detailForm.setField(uLoanPlan.LoanCode().readonly(true).setDisplay(constants.loanCode()),
                loanType.setNewline(true).required(true),
                uLoanPlan.Description(),
                uLoanPlan.TerminateAgeCd().required(true),
                uLoanPlan.LoanValidity()
                    .format("yyyy-MM-dd")
                    .showTime(false)
                    .setDisplay(constants.loanValidity())
                    .required(true),
                uLoanPlan.LoanStaus()
                    .setDisplay(constants.loanStaus())
                    .required(true)
                    .asSelectItem(SelectType.KEY_LABLE),
                  //放款类型
                    uLoanPlan.LoanMold().required(true),
                uLoanPlan.MinCycle().required(true),
                uLoanPlan.MaxCycle().required(true),
                uLoanPlan.ProductCode().asSelectItem().required(true),
                uLoanPlan.Ownership().required(true),
                uLoanPlan.ContractTemplateId().required(true),
                uLoanPlan.GroupId().asSelectItem(),
                uLoanPlan.CpdOverdueEndDays().required(true),                                        //增加时间 2015.11.18 chenpy
                uLoanPlan.IsAutoWaive().asSelectItem(SelectType.LABLE).required(true),
                uLoanPlan.AutoWaiveCpdDays().required(true),
                uLoanPlan.AutoWaiveAMT().required(true),
                uLoanPlan.IsOverAccruIns().asSelectItem(SelectType.LABLE),              //是否逾期计息  2015.12.12  chenpy
                uLoanPlan.PenaltyAccuBase().asSelectItem(SelectType.LABLE),         //罚息累计基数
                uLoanPlan.DepositEarlyDays()									//溢缴款转出距离账单日提前天数
                );
        }
        
        layout.add(detailForm);
        
        TabSetting tabSetting = new TabSetting().dblClickToClose(true).dragToMove(true).showSwitch(true);
        tab = new Tab(tabSetting);
        tab.setWidth("98%");
        tabSetting.contextmenu(false);
        layout.add(tab);
        
        TabItemSetting loanParamItem = new TabItemSetting(null, constants.merchantParam());
        tab.addItem(loanParamItem, loanFeeDefLayout.createCanvas());
        
        KylinButton saveButton = ClientUtils.creConfirmButton(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
                MapData loanPlanData = detailForm.getSubmitData().asMapData();
                
                MapData loanFeeDefGridData = new MapData();
                for (Entry<String, MapData> entry : loanFeeDefDataMap.entrySet())
                {
                    loanFeeDefGridData.put(entry.getKey(), entry.getValue());
                }
                loanPlanData.put("loanFeeDefMap", loanFeeDefGridData);
                if (detailForm.valid())
                {
                    RPC.ajax("rpc/loanPlanServer/updateLoanPlan", new RpcCallback<Data>()
                    {
                        @Override
                        public void onSuccess(Data result)
                        {
                            Dialog.tipNotice("修改成功");
                            notice(false);
                            Token token = Flat.get().getCurrentToken();
                            token.directPage(LoanParamPage.class);
                            Flat.get().goTo(token);
                        }
                    }, loanPlanData);
                }
            }
        });
        
        KylinButton cancelButton = ClientUtils.creCancelButton(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
                Token token = Flat.get().getCurrentToken();
                token.directPage(LoanParamPage.class);
                Flat.get().goTo(token);
            }
        });
        addButton(saveButton);
        addButton(cancelButton);
        
        return layout;
    }
    
    @Override
    public void refresh()
    {
        // 进入页面清空数据
        detailForm.getUi().clear();
        detailForm.setFieldValue(uLoanPlan.LoanType().getName(), "");
        loanFeeDefLayout.clearValues();
        
        RPC.ajax("rpc/loanPlanServer/getProductCode", new RpcCallback<Data>(){

			@Override
			public void onSuccess(Data result) {
				SelectItem<String> si=new SelectItem<String>();
				si.setValue(result.asListData());
				detailForm.setFieldSelectData(uLoanPlan.ProductCode().getName(), si);
			}
        	
        });
        RPC.ajax("rpc/loanPlanServer/getGroupCtrlId", new RpcCallback<Data>(){

			@Override
			public void onSuccess(Data result) {
				SelectItem<String> si=new SelectItem<String>();
				si.setValue(result.asListData());
				detailForm.setFieldSelectData(uLoanPlan.GroupId().getName(), si);
			}
        	
        });
        LinkedHashMap<String, String> resultMap = new LinkedHashMap<String, String>();
        resultMap.put("MCAT", constants.loanTypeS());
        resultMap.put("MCEP", constants.loanTypeE());
        resultMap.put("MCEI", constants.loanTypeL());
        SelectItem<String> item = new SelectItem<String>(SelectType.LABLE);
        item.setValue(resultMap);
        detailForm.setFieldSelectData(uLoanPlan.LoanType().getName(), item);
        
        loanCode = Flat.get().getCurrentToken().getParam(KEY);
        RPC.ajax("rpc/loanPlanServer/getLoanPlan", new RpcCallback<Data>()
        {
            @SuppressWarnings("unchecked")
            @Override
            public void onSuccess(Data result)
            {
            	loanFeeDefDataMap = new HashMap<String,MapData>();
                if (result == null)
                {
                    loanFeeDefDataMap = new HashMap<String, MapData>();
                }
                else
                {
                    String loanValidityTime = result.asMapData().getString(uLoanPlan.LoanValidity().getName());
                    DateTimeFormat format = DateTimeFormat.getFormat("yyyy-MM-dd");
                    MapData mapdata = result.asMapData();
                    if (StringUtils.isNotEmpty(loanValidityTime))
                    {
                        mapdata.put(uLoanPlan.LoanValidity().getName(),
                            format.format(new Date(Long.parseLong(loanValidityTime))));
                    }
                    detailForm.setFormData(mapdata);
                    
                    Map<String, Serializable> currentLoanPlanData =
                        (Map<String, Serializable>)result.asMapData().toMap();
                    validateCycle();
                    if (currentLoanPlanData.get("loanFeeDefMap") != null)
                    {
                        Map<String, Map<String, Serializable>> loanFeeDefMapData =
                            (Map<String, Map<String, Serializable>>)result.asMapData()
                                .getData("loanFeeDefMap")
                                .asMapData()
                                .toMap();
                        
                        if (loanFeeDefDataMap == null)
                        {
                            loanFeeDefDataMap = new HashMap<String, MapData>();
                        }
                        
                        for (Entry<String, Map<String, Serializable>> entry : loanFeeDefMapData.entrySet())
                        {
                            String loanFeeDefId = entry.getKey();
                            MapData loanFeeDefData =
                                result.asMapData().getData("loanFeeDefMap").asMapData().getData(loanFeeDefId).asMapData();
                            loanFeeDefData.put("loanFeeDefId", loanFeeDefId);
                            loanFeeDefDataMap.put(loanFeeDefId, loanFeeDefData);
                        }
                    }
                    loanFeeDefLayout.updateView(loanFeeDefDataMap);
                }
                
            }
        },
            loanCode);
    }
    
    private void validateCycle()
    {
        String loanTypeValue = detailForm.getFieldValue(uLoanPlan.LoanType().getName());
        if (loanTypeValue != null)
        {
            if ("MCAT".equals(loanTypeValue))
            {
                detailForm.setFieldValue(uLoanPlan.MinCycle().getName(), "");
                detailForm.setFieldValue(uLoanPlan.MaxCycle().getName(), "");
                detailForm.setFieldVisible(uLoanPlan.MinCycle().getName(), false);
                detailForm.setFieldVisible(uLoanPlan.MaxCycle().getName(), false);
                detailForm.setFieldRequired(uLoanPlan.MinCycle().getName(), false);
                detailForm.setFieldRequired(uLoanPlan.MaxCycle().getName(), false);
            }
            else
            {
                detailForm.setFieldVisible(uLoanPlan.MinCycle().getName(), true);
                detailForm.setFieldVisible(uLoanPlan.MaxCycle().getName(), true);
                detailForm.setFieldRequired(uLoanPlan.MinCycle().getName(), true);
                detailForm.setFieldRequired(uLoanPlan.MaxCycle().getName(), true);
            }
        }
    }
}
