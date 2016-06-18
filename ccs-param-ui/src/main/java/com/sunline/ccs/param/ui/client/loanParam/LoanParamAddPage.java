package com.sunline.ccs.param.ui.client.loanParam;

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

@Singleton
public class LoanParamAddPage extends SavePage
{
    
    public static final String PAGE_ID = "auth-loanParam-add";
    
    private KylinForm addForm;
    
    @Inject
    private ULoanPlan uLoanPlan;
    
    private Tab tab;
    
    @Inject
    private LoanParamConstants constants;
    
    @Inject
    private LoanFeeDefLayout loanFeeDefLayout;
    
    private Map<String, MapData> loanFeeDefDataMap;
    
    @Override
    public IsWidget createPage()
    {
        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        addForm = new KylinForm();
        {
            addForm.setCol(4);
            addForm.setWidth("98%");
            addForm.getSetting().labelWidth(120).labelAlign("right");
            TextColumnHelper loanType = new TextColumnHelper(uLoanPlan.LoanType().getName(), constants.loanType(), 10);
            loanType.asSelectItem().setColumnWidth(150);
            
            loanType.bindEvent("change", new IFunction()
            {
                @Override
                public void function()
                {
                    String loanTypeValue = addForm.getFieldValue(uLoanPlan.LoanType().getName());
                    if (loanTypeValue != null)
                    {
                        if ("MCAT".equals(loanTypeValue))
                        {
                            addForm.setFieldValue(uLoanPlan.MinCycle().getName(), "", "");
                            addForm.setFieldValue(uLoanPlan.MaxCycle().getName(), "", "");
                            addForm.setFieldVisible(uLoanPlan.MinCycle().getName(), false);
                            addForm.setFieldVisible(uLoanPlan.MaxCycle().getName(), false);
                            addForm.setFieldRequired(uLoanPlan.MinCycle().getName(), false);
                            addForm.setFieldRequired(uLoanPlan.MaxCycle().getName(), false);
                            
                        }
                        else
                        {
                            addForm.setFieldVisible(uLoanPlan.MinCycle().getName(), true);
                            addForm.setFieldVisible(uLoanPlan.MaxCycle().getName(), true);
                            addForm.setFieldRequired(uLoanPlan.MinCycle().getName(), true);
                            addForm.setFieldRequired(uLoanPlan.MaxCycle().getName(), true);
                        }
                    }
                }
            });
            
            addForm.setField(uLoanPlan.LoanCode().setDisplay(constants.loanCode()).required(true).setColumnWidth(150),
                loanType.setNewline(true).required(true),
                uLoanPlan.Description().setColumnWidth(150),
                uLoanPlan.TerminateAgeCd().required(true).setColumnWidth(150),
                uLoanPlan.LoanValidity()
                    .showTime(false)
                    .setDisplay(constants.loanValidity())
                    .required(true)
                    .setColumnWidth(150),
                uLoanPlan.LoanStaus()
                    .setDisplay(constants.loanStaus())
                    .required(true)
                    .setColumnWidth(150)
                    .asSelectItem(SelectType.KEY_LABLE),
                  //放款类型
                    uLoanPlan.LoanMold().required(true),
                uLoanPlan.MinCycle().required(true).setColumnWidth(150),
                uLoanPlan.MaxCycle().required(true).setColumnWidth(150),
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
        
        panel.add(addForm);
        
        TabSetting tabSetting = new TabSetting().dblClickToClose(true).dragToMove(true).showSwitch(true);
        tab = new Tab(tabSetting);
        tab.setWidth("98%");
        tabSetting.contextmenu(false);
        panel.add(tab);
        
        TabItemSetting loanParamItem = new TabItemSetting(null, constants.merchantParam());
        
        tab.addItem(loanParamItem, loanFeeDefLayout.createCanvas());
        
        KylinButton saveButton = ClientUtils.creConfirmButton(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
                if(!addForm.valid())return;
                
                MapData loanPlanData = addForm.getSubmitData().asMapData();
                
                MapData loanFeeDefGridData = new MapData();
                for (Entry<String, MapData> entry : loanFeeDefDataMap.entrySet())
                {
                    loanFeeDefGridData.put(entry.getKey(), entry.getValue());
                }
                
                loanPlanData.put("loanFeeDefMap", loanFeeDefGridData);
                DateTimeFormat format = DateTimeFormat.getFormat("yyyy-MM-dd");
                loanPlanData.put(uLoanPlan.LoanValidity().getName(),
                    format.format(new Date(Long.parseLong(loanPlanData.getString(uLoanPlan.LoanValidity().getName())))));
                if (addForm.valid())
                {
                    RPC.ajax("rpc/loanPlanServer/addLoanPlan", new RpcCallback<Data>()
                    {
                        @Override
                        public void onSuccess(Data result)
                        {
                            Dialog.tipNotice("添加成功");
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
        return panel;
    }
    
    @Override
    public void refresh()
    {
        addForm.getUi().clear();
        loanFeeDefLayout.clearValues();
        
        RPC.ajax("rpc/loanPlanServer/getProductCode", new RpcCallback<Data>(){

			@Override
			public void onSuccess(Data result) {
				SelectItem<String> si=new SelectItem<String>();
				si.setValue(result.asListData());
				addForm.setFieldSelectData(uLoanPlan.ProductCode().getName(), si);
			}
        	
        });
        RPC.ajax("rpc/loanPlanServer/getGroupCtrlId", new RpcCallback<Data>(){

			@Override
			public void onSuccess(Data result) {
				SelectItem<String> si=new SelectItem<String>();
				si.setValue(result.asListData());
				addForm.setFieldSelectData(uLoanPlan.GroupId().getName(), si);
			}
        	
        });
        LinkedHashMap<String, String> resultMap = new LinkedHashMap<String, String>();
        resultMap.put("MCAT", constants.loanTypeS());
        resultMap.put("MCEP", constants.loanTypeE());
        resultMap.put("MCEI", constants.loanTypeL());
        SelectItem<String> item = new SelectItem<String>(SelectType.LABLE);
        item.setValue(resultMap);
        
        addForm.setFieldSelectData(uLoanPlan.LoanType().getName(), item);
        loanFeeDefDataMap = new HashMap<String, MapData>();
        loanFeeDefLayout.updateView(loanFeeDefDataMap);
    }
}
