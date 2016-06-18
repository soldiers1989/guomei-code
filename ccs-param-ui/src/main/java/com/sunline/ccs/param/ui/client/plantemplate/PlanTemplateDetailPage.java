package com.sunline.ccs.param.ui.client.plantemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UInterestTable;
import com.sunline.ccs.infrastructure.client.ui.UPlanTemplate;
import com.sunline.kylin.web.ark.client.helper.IntegerColumnHelper;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.utils.StringUtils;
import com.sunline.kylin.web.core.client.Flat;
import com.sunline.kylin.web.core.client.mvp.Token;
import com.sunline.kylin.web.core.client.res.SavePage;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.IClickEventListener;

@Singleton
public class PlanTemplateDetailPage extends SavePage
{
    
    @Inject
    private UPlanTemplate uPlanTemplate;
    
    private KylinForm editForm;
    
    private String planNbr;
    
    private FlexTable tableHead;
    
    private FlexTable tableRow;
    
    @Inject
    private UInterestTable uInterestTable;           //添加罚息利率表   2015.11.11  chenpy
    
    private IntegerColumnHelper integerColumnHelper;       //添加罚息利率表   2015.11.11  chenpy
    
    @Inject
    private IntParameterBucketLayout intParameterBucketLayout;
    
    private Map<String, List<Object>> tableMap = new HashMap<String, List<Object>>();
    
    @Override
    public IsWidget createPage()
    {
        VerticalPanel panel = new VerticalPanel();
        editForm = new KylinForm();
        editForm.setWidth("100%");
        
        integerColumnHelper = uInterestTable.IntTableId().asSelectItem();         //添加罚息利率表   2015.11.11  chenpy
        
        editForm.setField(uPlanTemplate.PlanNbr().required(true).readonly(true),
            uPlanTemplate.PlanType().required(true).readonly(true),
            uPlanTemplate.Description(),
            uPlanTemplate.PmtPriority().setNewline(true),
            uPlanTemplate.DualXfrPlanNbr(),
            uPlanTemplate.PlanPurgeDays().required(true),
            uPlanTemplate.MultSaleInd().setNewline(true).asCheckBoxItem(),
            uPlanTemplate.PmtAssignInd().asCheckBoxItem(),
            uPlanTemplate.IsAccruPrinSum().asSelectItem(SelectType.LABLE),             //是否累计本金基数  2015.11.18 chenpy
            integerColumnHelper.required(true),                   //添加罚息利率表   2015.11.11  chenpy
            uPlanTemplate.RepPenaltyIntId().asSelectItem().required(true)		//添加代收罚息利率表
        	);
        editForm.getSetting().labelWidth(120);
        editForm.setCol(3);
        KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener()
        {
            @SuppressWarnings({"rawtypes", "unchecked"})
            @Override
            public void onClick()
            {
                if(!editForm.valid())return;
                
                // 获取信用计划基本表单数据
                MapData data = editForm.getSubmitData().asMapData();
                
                // 需要把IntegerColumnHelper的值转为Integer类型
                String pmtPriority = data.getString("pmtPriority");
                String planPurgeDays = data.getString("planPurgeDays");
                if (StringUtils.isNotEmpty(pmtPriority))
                {
                    data.put("pmtPriority", Integer.parseInt(pmtPriority));
                }
                if (StringUtils.isNotEmpty(planPurgeDays))
                {
                    data.put("planPurgeDays", Integer.parseInt(planPurgeDays));
                }
                
                // 余额成分提交数据
                Map<BucketType, Map<String, Object>> bucketTypeMap = new HashMap<BucketType, Map<String, Object>>();
                
                // 遍历余额成分表格数据，转化为Map
                for (Entry entry : tableMap.entrySet())
                {
                    List<Object> list = (List<Object>)entry.getValue();
                    intParameterBucketLayout.covertBucketTypeValuesToMap(bucketTypeMap, list);
                }
                
                RPC.ajax("rpc/planTemplateServer/updatePlantemplate", new RpcCallback<Data>()
                {
                    @Override
                    public void onSuccess(Data result)
                    {
                        notice(false);
                        Dialog.tipNotice("修改成功！");
                        Token token = Flat.get().getCurrentToken();
                        token.directPage(PlanTemplatePage.class);
                        Flat.get().goTo(token);
                    }
                }, data, bucketTypeMap);
            }
        });
        KylinButton cBtn = ClientUtils.creCancelButton(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
                Token token = Flat.get().getCurrentToken();
                token.directPage(PlanTemplatePage.class);
                Flat.get().goTo(token);
            }
        });
        addButton(submitBtn);
        addButton(cBtn);
        
        tableHead = new FlexTable();
        tableHead.setBorderWidth(0);
        tableHead.setCellSpacing(55);
        tableHead.setCellPadding(5);
        
        tableRow = new FlexTable();
        tableRow.setBorderWidth(0);
        tableRow.setCellSpacing(55);
        tableRow.setCellPadding(5);
        
        panel.add(editForm);
        panel.add(tableHead);
        panel.add(tableRow);
        
        return panel;
    }
    
    @Override
    public void refresh()
    {
        notice(true);
        editForm.getUi().clear();
        tableRow.clear();
        
        //添加罚息利率表   2015.11.11  chenpy
        RPC.ajax("rpc/ccsSelectOptionServer/getInterestTableList", new RpcCallback<Data>() {

			@Override
			public void onSuccess(Data result) {
				SelectItem<String> si1 = new SelectItem<String>();
				si1.setValue(result.asListData());
				editForm.setFieldSelectData(uInterestTable.IntTableId().getName(), si1);
				editForm.setFieldSelectData(uPlanTemplate.RepPenaltyIntId().getName(), si1);
			}
		});
        
        Token token = Flat.get().getCurrentToken();
        planNbr = token.getParam(uPlanTemplate.PlanNbr().getName());
        RPC.ajax("rpc/ccsSelectOptionServer/getInterestId", new RpcCallback<Data>()
        {
            @Override
            public void onSuccess(Data result)
            {
                // 解析利率表编号下拉列表
                String intTableIdData = intParameterBucketLayout.parseIntTableIdSelectData(result);
                
                // 创建余额成分表格
                intParameterBucketLayout.createBucketTypeTable(tableHead, tableRow, tableMap, intTableIdData);
                
                RPC.ajax("rpc/planTemplateServer/getPlantemplate", new RpcCallback<Data>()
                {
                    @Override
                    public void onSuccess(Data arg0)
                    {
                        MapData editFormData = arg0.asMapData();
                        MapData intParameterBuckets = editFormData.getData("intParameterBuckets").asMapData();
                        intParameterBucketLayout.updateTableRow(tableMap, intParameterBuckets);
                        editForm.setFormData(editFormData);
                        
                      //添加罚息利率表   2015.11.11  chenpy
                        editForm.setFieldValue(uInterestTable.IntTableId().getName(), arg0.asMapData().getString(uInterestTable.IntTableId().getName()));
                        editForm.setFieldValue(uPlanTemplate.RepPenaltyIntId().getName(), arg0.asMapData().getString(uPlanTemplate.RepPenaltyIntId().getName()));
                    }
                }, planNbr);
            }
        });
    }
}
