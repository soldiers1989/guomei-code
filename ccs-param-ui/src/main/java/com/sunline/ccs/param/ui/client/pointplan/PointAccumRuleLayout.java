package com.sunline.ccs.param.ui.client.pointplan;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.sunline.ccs.infrastructure.client.ui.UPointAccumRule;
import com.sunline.ccs.param.ui.client.util.RatesLayout;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.core.client.util.DataUtil;
import com.sunline.ui.layout.client.Layout;

@SuppressWarnings("serial")
public class PointAccumRuleLayout extends Layout{

	private KylinForm pointAccumRuleForm;
	
	@Inject
	private AddedRulesLayout addedRulesLayout;
	
	@Inject
	private RatesLayout pointRateLayout;
	
	@Inject
	private UPointAccumRule uPointAccumRule;
	

	public IsWidget createCanvas() {
		VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        panel.setHeight("100%");
		
		pointAccumRuleForm = new KylinForm();
		{
			pointAccumRuleForm.setCol(4);
			pointAccumRuleForm.setField(uPointAccumRule.MinValue().required(true),
					uPointAccumRule.MaxValue().required(true));
		}
		panel.add(pointAccumRuleForm);
		
		//手续费比率panel
		panel.add(pointRateLayout.createCanvas());
		
		//附加计算规则panel
		panel.add(addedRulesLayout.createCanvas());
		
		return panel;
	}
	public void clearValues() {
		pointAccumRuleForm.getUi().clear();
		pointRateLayout.clearValues();
		addedRulesLayout.clearValues();
	}
	public Data StringToData(String gridDataString){
		Data data = new Data();
		data.setJsData(DataUtil.convertDataType(gridDataString));
		return data;
	}
	
	public void updateView(Data data) {
		MapData value= data.asMapData();
		clearValues();
		pointAccumRuleForm.setFormData(data);
		pointRateLayout.setValue(value.getData("pointRate"));
		addedRulesLayout.setValue(value.getData("addedRules"));
		
	}
	
	
	public MapData getValue (){
		MapData value = new MapData();
		
		MapData.extend(value, getPointAccumRuleForm(), true);
		
		value.put("pointRate", getPointRate());
		value.put("addedRules", getAddedRules());
		
		return value;
	}
	private MapData getPointAccumRuleForm () {
		return pointAccumRuleForm.getSubmitData().asMapData();
	}
	
	private Data getPointRate(){
		return pointRateLayout.getValue();
	}
	
	private MapData getAddedRules (){
		return addedRulesLayout.getValue();
	}
	
	public boolean valid(){
	    return pointAccumRuleForm.valid();
	}
}
