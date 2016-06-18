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

/**
 * 信用计划模板添加页面
 * 
 * @author lindh
 * @version [版本号, Jun 24, 2015]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
@Singleton
public class PlanTemplateAddPage extends SavePage
{
    @Inject
    private UPlanTemplate uPlanTemplate;
    
    /**
     * 添加表单
     */
    private KylinForm form;
    
    /**
     * 余额成分表头
     */
    private FlexTable tableHead;
    
    /**
     * 余额成分表格行
     */
    private FlexTable tableRow;
    
    @Inject
    private UInterestTable uInterestTable;           //添加罚息利率表   2015.11.11  chenpy
    
    private IntegerColumnHelper integerColumnHelper;       //添加罚息利率表   2015.11.11  chenpy
    
    @Inject
    private IntParameterBucketLayout intParameterBucketLayout;
    
    /**
     * 用于存放余额成分数据
     */
    private Map<String, List<Object>> tableMap = new HashMap<String, List<Object>>();
    
    @Override
    public IsWidget createPage()
    {
        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        
        form = new KylinForm();
        form.setWidth("98%");
        
        integerColumnHelper = uInterestTable.IntTableId().asSelectItem();         //添加罚息利率表   2015.11.11  chenpy
        
        form.setField(uPlanTemplate.PlanNbr().required(true),
            uPlanTemplate.PlanType().required(true).asSelectItem(SelectType.KEY_LABLE),
            uPlanTemplate.Description(),
            uPlanTemplate.PmtPriority().setNewline(true),
            uPlanTemplate.DualXfrPlanNbr(),
            uPlanTemplate.PlanPurgeDays().required(true),
            uPlanTemplate.MultSaleInd().setNewline(true).asCheckBoxItem(),
            uPlanTemplate.PmtAssignInd().asCheckBoxItem(),
            uPlanTemplate.IsAccruPrinSum().asSelectItem(SelectType.LABLE),             //是否累计本金基数  2015.11.18 chenpy
            integerColumnHelper.required(true),					 //添加罚息利率表   2015.11.11  chenpy
            uPlanTemplate.RepPenaltyIntId().asSelectItem().required(true)		//添加代收罚息利率表
            );                  
        
        form.getSetting().labelWidth(120);
        form.setCol(3);
        
        // 提交按钮
        KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener()
        {
            @SuppressWarnings({"rawtypes", "unchecked"})
            @Override
            public void onClick()
            {
                if(!form.valid())return;
                
                // 获取信用计划基本表单数据
                MapData data = form.getSubmitData().asMapData();
                
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
                
                RPC.ajax("rpc/planTemplateServer/addPlantemplate", new RpcCallback<Data>()
                {
                    @Override
                    public void onSuccess(Data result)
                    {
                        notice(false);
                        Dialog.tipNotice("添加成功！");
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
        panel.add(form);
        
        tableHead = new FlexTable();
        tableHead.setBorderWidth(0);
        tableHead.setCellSpacing(55);
        tableHead.setCellPadding(5);
        
        tableRow = new FlexTable();
        tableRow.setBorderWidth(0);
        tableRow.setCellSpacing(55);
        tableRow.setCellPadding(5);
        
        panel.add(tableHead);
        panel.add(tableRow);
        return panel;
    }
    
    @Override
    public void refresh()
    {
        form.getUi().clear();
        tableRow.clear();
        
      //添加罚息利率表   2015.11.11  chenpy
        RPC.ajax("rpc/ccsSelectOptionServer/getInterestTableList", new RpcCallback<Data>() {

			@Override
			public void onSuccess(Data result) {
				SelectItem<String> si1 = new SelectItem<String>();
				si1.setValue(result.asListData());
				form.setFieldSelectData(uInterestTable.IntTableId().getName(), si1);
				form.setFieldSelectData(uPlanTemplate.RepPenaltyIntId().getName(), si1);
			}
		});
        
        RPC.ajax("rpc/ccsSelectOptionServer/getInterestId", new RpcCallback<Data>()
        {
            @Override
            public void onSuccess(Data result)
            {
                // 解析利率表编号下拉列表
                String intTableIdData = intParameterBucketLayout.parseIntTableIdSelectData(result);
                
                // 创建余额成分表格
                intParameterBucketLayout.createBucketTypeTable(tableHead, tableRow, tableMap, intTableIdData);
            }
        });
    }
}

