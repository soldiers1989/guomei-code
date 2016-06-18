package com.sunline.ccs.param.ui.client.pointplan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.sunline.ccs.infrastructure.client.domain.EventDomainClient;
import com.sunline.ccs.infrastructure.client.ui.UCalcOperation;
import com.sunline.ccs.param.def.enums.Event;
import com.sunline.kylin.web.ark.client.helper.BooleanColumnHelper;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.layout.client.Layout;

@SuppressWarnings("serial")
public class AddedRulesLayout extends Layout {
	
	private static final String ARITHMETIC = "arithmetic";
	
	private static final String VALUE = "value";

	@Inject
	private EventDomainClient eventDomainClient;

	@Inject
	private UCalcOperation uCalcOperation;

	@Inject
	private PointPlanConstants constants;

	private Map<Event, BooleanColumnHelper> eventtypemap;

	private List<KylinForm> forms;
	
	private List<String> fieldNames;

	public String getLayoutTitle() {

		return constants.addedRules();
	}

	public IsWidget createCanvas() {

		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");
		panel.setHeight("100%");

		forms = new ArrayList<KylinForm>();

		forms.add(new KylinForm());
		forms.add(new KylinForm());
		
		LinkedHashMap<String, String> eventMap = eventDomainClient
				.asLinkedHashMap();
		eventtypemap = new HashMap<Event, BooleanColumnHelper>();

		List<BooleanColumnHelper> booleanItem = new ArrayList<BooleanColumnHelper>();
		fieldNames = new ArrayList<String>();
		for (Event evt : Event.values()) {
			BooleanColumnHelper item = new BooleanColumnHelper(
					evt.toString(), evt.toString()
							+ eventMap.get(evt.toString()))
					.asCheckBoxItem();
			fieldNames.add(evt.toString());
			eventtypemap.put(evt, item);
			booleanItem.add(item);
		}

		for (int index = 0; index < forms.size(); index++) {
			{
				forms.get(index).setWidth("98%");
				forms.get(index).setCol(3);
				forms.get(index).setTitle(constants.addedRules());
				forms.get(index).getSetting().labelWidth(120)
						.labelAlign("right").space(20);

				
				forms.get(index).setField(
						booleanItem.get(index).setFieldWidth(120));
				forms.get(index).setField(
						uCalcOperation.Arithmetic().asSelectItem(
								SelectType.KEY_LABLE));
				forms.get(index).setField(uCalcOperation.Value());

			}

			panel.add(forms.get(index));
		}

		

		
		return panel;
		
	}
	public void clearValues() {
		for (int index = 0; index < forms.size(); index++){
			forms.get(index).getUi().clear();
		}
		
	}
	
	public void setValue(Data data ) {
		Data temp = new Data();
		String arithmetic;
		Integer value;
		for (int index = 0; index < fieldNames.size(); index++)
		{
			temp = data.asMapData().getData(fieldNames.get(index));
			if(temp!=null){
				Data result = new Data();
				arithmetic = temp.asMapData().getString(ARITHMETIC);
				value = temp.asMapData().getInteger(VALUE);
				result.asMapData().put(fieldNames.get(index), true);
				result.asMapData().put(ARITHMETIC, arithmetic);
				result.asMapData().put(VALUE, value);
				forms.get(index).setFormData(result);
			}
		}
	}
	
	// 数据结构需要转换
	public MapData getValue() {
		MapData addedRulesFormData = new MapData();
		Data temp = new Data();
		Boolean tempValue;
		for (int index = 0; index < forms.size(); index++)
		{
			temp = forms.get(index).getSubmitData();
			for(int i=0 ;i<fieldNames.size();i++){
				tempValue = temp.asMapData().getBoolean(fieldNames.get(i));
				if(tempValue!=null && tempValue){
					MapData md = new MapData();
					md.put(ARITHMETIC, temp.asMapData().getString(ARITHMETIC));
					md.put(VALUE, temp.asMapData().getInteger(VALUE));
					addedRulesFormData.put(fieldNames.get(i), md);
				}
			}
		}

		return addedRulesFormData;
	}

}
