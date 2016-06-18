package com.sunline.ccs.param.ui.client.plantemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.domain.i18n.BucketTypeConstants;
import com.sunline.ccs.infrastructure.client.ui.UBucketDef;
import com.sunline.ccs.infrastructure.client.ui.i18n.BucketDefConstants;
import com.sunline.ccs.param.def.enums.BnpPeriod;
import com.sunline.ccs.param.def.enums.IntAccumFrom;
import com.sunline.kylin.web.ark.client.utils.StringUtils;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ui.combobox.client.Combobox;
import com.sunline.ui.combobox.client.ComboboxSetting;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.textBox.client.TextBox;
import com.sunline.ui.textBox.client.TextBoxSetting;

/**
 * 余额成分参数配置
 * 
 * @author lindh
 * @version [版本号, Jun 24, 2015]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
@Singleton
public class IntParameterBucketLayout
{
    @Inject
    private PlanTemplateConstants constants;
    
    @Inject
    private BucketDefConstants bucketDefConstants;
    
    @Inject
    private BucketTypeConstants bucketTypeConstants;
    
    @Inject
    private UBucketDef uBucketDef;
    
    /**
     * 更新表格内容
     *
     * @param intParameterBuckets
     * @see [类、类#方法、类#成员]
     */
    public void updateTableRow(Map<String, List<Object>> tableMap, MapData intParameterBuckets)
    {
        for (BucketType bucketType : BucketType.values())
        {
            List<Object> rowList = tableMap.get(bucketType.name());
            if (intParameterBuckets.getData(bucketType.name()) == null)
                continue;
            MapData bucketTypeMapData = intParameterBuckets.getData(bucketType.name()).asMapData();
            
            Integer intTableId = bucketTypeMapData.getInteger("intTableId");
            Combobox intTableIdCBB = (Combobox)rowList.get(1);
            intTableIdCBB.setValue(String.valueOf(intTableId));
            
            String intAccumFrom = bucketTypeMapData.getString("intAccumFrom");
            Combobox intAccumFromCBB = (Combobox)rowList.get(2);
            intAccumFromCBB.setValue(intAccumFrom);
            
            Boolean intWaive = bucketTypeMapData.getBoolean("intWaive");
            CheckBox intWaiveBox = (CheckBox)rowList.get(3);
            intWaiveBox.setValue(intWaive);
            
            Boolean graceQualify = bucketTypeMapData.getBoolean("graceQualify");
            CheckBox graceQualifyBox = (CheckBox)rowList.get(4);
            graceQualifyBox.setValue(graceQualify);
            
            Boolean overlimitQualify = bucketTypeMapData.getBoolean("overlimitQualify");
            CheckBox overlimitQualifyBox = (CheckBox)rowList.get(5);
            overlimitQualifyBox.setValue(overlimitQualify);
            
            MapData minPaymentRates = bucketTypeMapData.getData("minPaymentRates").asMapData();
            if (minPaymentRates.toString().equals("{}"))
                continue;
            TextBox ctdMinPaymentRateTB = (TextBox)rowList.get(6);
            ctdMinPaymentRateTB.setValue(minPaymentRates.getBigDecimal(BnpPeriod.CTD.name()).toString());
            TextBox pastMinPaymentRateTB = (TextBox)rowList.get(7);
            pastMinPaymentRateTB.setValue(minPaymentRates.getBigDecimal(BnpPeriod.PAST.name()).toString());
        }
    }
    
    /**
     * 创建余额成分表格
     *
     * @param tableHead
     * @param tableRow
     * @param tableMap
     * @param intTableIdData
     * @see [类、类#方法、类#成员]
     */
    public void createBucketTypeTable(FlexTable tableHead, FlexTable tableRow, Map<String, List<Object>> tableMap,
        String intTableIdData)
    {
        this.setTableHead(tableHead);
        this.setTableRows(tableRow, tableMap, intTableIdData);
    }
    
    /**
     * 设置表头
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    private void setTableHead(FlexTable tableHead)
    {
        Label bucketTypeLabel = new Label(constants.bucketType());
        bucketTypeLabel.setWidth("120px");
        tableHead.setWidget(0, 0, bucketTypeLabel);
        
        Label intTableIdLabel = new Label(bucketDefConstants.intTableId());
        intTableIdLabel.setWidth("130px");
        tableHead.setWidget(0, 1, intTableIdLabel);
        
        Label intAccumFromLabel = new Label(bucketDefConstants.intAccumFrom());
        intAccumFromLabel.setWidth("130px");
        tableHead.setWidget(0, 2, intAccumFromLabel);
        
        Label intWaiveLabel = new Label(bucketDefConstants.intWaive());
        intWaiveLabel.setWidth("120px");
        tableHead.setWidget(0, 3, intWaiveLabel);
        
        Label graceQualifyLabel = new Label(bucketDefConstants.graceQualify());
        graceQualifyLabel.setWidth("150px");
        tableHead.setWidget(0, 4, graceQualifyLabel);
        
        Label overlimitQualifyLabel = new Label(bucketDefConstants.overlimitQualify());
        overlimitQualifyLabel.setWidth("120px");
        tableHead.setWidget(0, 5, overlimitQualifyLabel);
        
        Label ctdMinPaymentRateLabel = new Label(constants.ctdMinPaymentRate());
        ctdMinPaymentRateLabel.setWidth("120px");
        tableHead.setWidget(0, 6, ctdMinPaymentRateLabel);
        
        Label pastMinPaymentRateLabel = new Label(constants.pastMinPaymentRate());
        pastMinPaymentRateLabel.setWidth("120px");
        tableHead.setWidget(0, 7, pastMinPaymentRateLabel);
    }
    
    /**
     * 设置表行
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    private void setTableRows(FlexTable tableRow, Map<String, List<Object>> tableMap, String intTableIdData)
    {
        // 起息日类型下拉列表
        String intAccumFromJson = parseIntAccumFromSelectData();
        
        for (int i = 0; i < BucketType.values().length; i++)
        {
            List<Object> rowList = new ArrayList<Object>();
            BucketType bucketType = BucketType.values()[i];
            
            // 余额成份类型
            HorizontalPanel bucketTypePanel = new HorizontalPanel();
            bucketTypePanel.setWidth("120px");
            Label bucketTypeLabel =
                new Label(bucketTypeConstants.getString(bucketType.toString().toUpperCase()) + " : ");
            bucketTypeLabel.setWidth("100px");
            bucketTypeLabel.setHorizontalAlignment(Label.ALIGN_RIGHT);
            bucketTypePanel.add(bucketTypeLabel);
            tableRow.setWidget(i, 0, bucketTypePanel);
            rowList.add(bucketType);
            
            // 利率表编号
            HorizontalPanel intTableIdPanel = new HorizontalPanel();
            intTableIdPanel.setWidth("130px");
            ComboboxSetting intTableIdCS = new ComboboxSetting();
            intTableIdCS.cancelable(false);
            intTableIdCS.data(intTableIdData).width(120);
            Combobox intTableIdCB = new Combobox(intTableIdCS);
            intTableIdCB.setWidth("120px");
            intTableIdPanel.add(intTableIdCB);
            tableRow.setWidget(i, 1, intTableIdPanel);
            rowList.add(intTableIdCB);
            
            // 起息日类型
            HorizontalPanel intAccumFromPanel = new HorizontalPanel();
            intAccumFromPanel.setWidth("130px");
            ComboboxSetting intAccumFromCS = new ComboboxSetting();
            intAccumFromCS.cancelable(false);
            intAccumFromCS.data(intAccumFromJson).width(120);
            Combobox intAccumFromCB = new Combobox(intAccumFromCS);
            intAccumFromCB.setWidth("120px");
            intAccumFromPanel.add(intAccumFromCB);
            tableRow.setWidget(i, 2, intAccumFromPanel);
            rowList.add(intAccumFromCB);
            
            // 享受免息期
            HorizontalPanel intWaivePanel = new HorizontalPanel();
            intWaivePanel.setWidth("120px");
            CheckBox intWaiveBox = new CheckBox();
            intWaivePanel.add(intWaiveBox);
            Label intWaiveLabel = new Label(uBucketDef.IntWaive().getDisplay());
            intWaivePanel.add(intWaiveLabel);
            tableRow.setWidget(i, 3, intWaivePanel);
            rowList.add(intWaiveBox);
            
            // 计入全部还款额
            HorizontalPanel graceQualifyPanel = new HorizontalPanel();
            graceQualifyPanel.setWidth("150px");
            CheckBox graceQualifyBox = new CheckBox();
            graceQualifyPanel.add(graceQualifyBox);
            Label graceQualifyLable = new Label(uBucketDef.GraceQualify().getDisplay());
            graceQualifyPanel.add(graceQualifyLable);
            tableRow.setWidget(i, 4, graceQualifyPanel);
            rowList.add(graceQualifyBox);
            
            // 参与超限计算
            HorizontalPanel overlimitQualifyPanel = new HorizontalPanel();
            overlimitQualifyPanel.setWidth("120px");
            CheckBox overlimitQualifyBox = new CheckBox();
            overlimitQualifyPanel.add(overlimitQualifyBox);
            Label overlimitQualifyLabel = new Label(uBucketDef.OverlimitQualify().getDisplay());
            overlimitQualifyPanel.add(overlimitQualifyLabel);
            tableRow.setWidget(i, 5, overlimitQualifyPanel);
            rowList.add(overlimitQualifyBox);
            
            // 当期最小还款额计算比例
            HorizontalPanel ctdMinPaymentRatePanel = new HorizontalPanel();
            ctdMinPaymentRatePanel.setWidth("130px");
            TextBoxSetting ctdMinPaymentRateTBS = new TextBoxSetting();
            ctdMinPaymentRateTBS.width(120);
            TextBox ctdMinPaymentRateTB = new TextBox(ctdMinPaymentRateTBS);
            ctdMinPaymentRatePanel.add(ctdMinPaymentRateTB);
            tableRow.setWidget(i, 6, ctdMinPaymentRatePanel);
            rowList.add(ctdMinPaymentRateTB);
            
            // 往期最小还款额计算比例
            HorizontalPanel pastMinPaymentRatePanel = new HorizontalPanel();
            pastMinPaymentRatePanel.setWidth("120px");
            TextBoxSetting pastMinPaymentRateTBS = new TextBoxSetting();
            pastMinPaymentRateTBS.width(120);
            TextBox pastMinPaymentRateTB = new TextBox(pastMinPaymentRateTBS);
            pastMinPaymentRatePanel.add(pastMinPaymentRateTB);
            tableRow.setWidget(i, 7, pastMinPaymentRatePanel);
            rowList.add(pastMinPaymentRateTB);
            
            tableMap.put(bucketType.name(), rowList);
        }
    }
    
    /**
     * 转换余额成分表格值为Map
     *
     * @param list
     * @see [类、类#方法、类#成员]
     */
    public void covertBucketTypeValuesToMap(Map<BucketType, Map<String, Object>> bucketTypeMap, List<Object> list)
    {
        Map<String, Object> rowMap = new HashMap<String, Object>();
        
        // 余额成分类型
        BucketType bucketType = (BucketType)list.get(0);
        
        // 利率表编号
        Combobox intTableIdCB = (Combobox)list.get(1);
        if (StringUtils.isEmpty(intTableIdCB.getValue()))
            return;
        rowMap.put("intTableId", intTableIdCB.getValue());
        
        // 起息日类型
        Combobox intAccumFromCB = (Combobox)list.get(2);
        if (StringUtils.isEmpty(intAccumFromCB.getValue()))
            return;
        rowMap.put("intAccumFrom", intAccumFromCB.getValue());
        
        // 享受免息期
        CheckBox intWaiveBox = (CheckBox)list.get(3);
        rowMap.put("intWaive", intWaiveBox.getValue());
        
        // 计入全部应还款额
        CheckBox graceQualifyBox = (CheckBox)list.get(4);
        rowMap.put("graceQualify", graceQualifyBox.getValue());
        
        // 参与超限计算
        CheckBox overlimitQualifyBox = (CheckBox)list.get(5);
        rowMap.put("overlimitQualify", overlimitQualifyBox.getValue());
        
        Map<BnpPeriod, BigDecimal> minPaymentRates = new HashMap<BnpPeriod, BigDecimal>();
        
        // 当期/往期最小还款额计算比例
        TextBox ctdMinPaymentRateTB = (TextBox)list.get(6);
        TextBox pastMinPaymentRateTB = (TextBox)list.get(7);
        minPaymentRates.put(BnpPeriod.CTD, new BigDecimal(StringUtils.isEmpty(ctdMinPaymentRateTB.getValue()) ? "0"
            : ctdMinPaymentRateTB.getValue()));
        minPaymentRates.put(BnpPeriod.PAST, new BigDecimal(StringUtils.isEmpty(pastMinPaymentRateTB.getValue()) ? "0"
            : pastMinPaymentRateTB.getValue()));
        rowMap.put("minPaymentRates", minPaymentRates);
        
        bucketTypeMap.put(bucketType, rowMap);
    }
    
    /**
     * 解析利率表编号下拉列表成Json格式
     *
     * @param result
     * @see [类、类#方法、类#成员]
     */
    @SuppressWarnings("unchecked")
    public String parseIntTableIdSelectData(Data result)
    {
        Map<String, String> mapData = (Map<String, String>)result.asMapData().toMap();
        StringBuffer mapDataStr = new StringBuffer();
        mapDataStr.append("[");
        Iterator<Map.Entry<String, String>> it = mapData.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry<String, String> entry = it.next();
            mapDataStr.append("{\"id\":\"");
            mapDataStr.append(entry.getKey());
            mapDataStr.append("\",\"text\":\"");
            mapDataStr.append(entry.getValue());
            mapDataStr.append("\"}");
            if (it.hasNext())
            {
                mapDataStr.append(",");
            }
        }
        mapDataStr.append("]");
        
        return mapDataStr.toString();
    }
    
    /**
     * 解析起息日类型下拉列表成Json格式
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    private String parseIntAccumFromSelectData()
    {
        StringBuffer intAccumFromJson = new StringBuffer();
        intAccumFromJson.append("[");
        for (int i = 0; i < IntAccumFrom.values().length; i++)
        {
            IntAccumFrom intAccumFrom = IntAccumFrom.values()[i];
            intAccumFromJson.append("{\"id\":\"");
            intAccumFromJson.append(intAccumFrom.name());
            intAccumFromJson.append("\",\"text\":\"");
            intAccumFromJson.append(intAccumFrom.getKeyLabelDesc());
            intAccumFromJson.append("\"}");
            if (i < IntAccumFrom.values().length - 1)
            {
                intAccumFromJson.append(",");
            }
        }
        intAccumFromJson.append("]");
        return intAccumFromJson.toString();
    }
}
