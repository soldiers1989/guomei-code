package com.sunline.ccs.param.ui.client.pointplan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.sunline.ccs.infrastructure.client.domain.AuthTransTerminalDomainClient;
import com.sunline.ccs.infrastructure.client.domain.AuthTransTypeDomainClient;
import com.sunline.ccs.infrastructure.client.domain.BlackWhiteCodeDomainClient;
import com.sunline.ccs.infrastructure.client.domain.DayDomainClient;
import com.sunline.ccs.infrastructure.client.ui.UTxnFilterRule;
import com.sunline.ccs.param.def.enums.AuthTransTerminal;
import com.sunline.kylin.web.ark.client.helper.BooleanColumnHelper;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.core.client.util.SelectItemUtil;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.BlackWhiteCode;
import com.sunline.ppy.dictionary.enums.Day;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.ListData;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.layout.client.Layout;
import com.sunline.ui.tab.client.Tab;
import com.sunline.ui.tab.client.TabSetting;

@SuppressWarnings("serial")
public class TxnFilterRuleLayout extends Layout {

	@Inject
	private UTxnFilterRule uTxnFilterRule;

	@Inject
	private DayDomainClient dayDomainClient;

	@Inject
	private AuthTransTypeDomainClient authTransTypeDomainClient;

	@Inject
	private AuthTransTerminalDomainClient authTransTerminalDomainClient;

	@Inject
	private BlackWhiteCodeDomainClient listTypeDomainClient;

	private KylinForm currencyForm;

	private KylinForm daysForm;

	private KylinForm supportTransTypesForm;

	private KylinForm supportTerminalsForm;

	private KylinForm typeForm;

	@Inject
	private MccTerminalsLayout mccTerminalsLayout;

	@Inject
	private TimePeriodLayout timePeriodLayout;

	@Inject
	private PointPlanConstants constants;

	private Map<Day, BooleanColumnHelper> dayitems;

	private Map<AuthTransType, BooleanColumnHelper> transtypeitems;

	private Map<AuthTransTerminal, BooleanColumnHelper> transterminalitems;

	private Map<BlackWhiteCode, BooleanColumnHelper> codeitems;

	/**
	 * 获取layout标题 <功能详细描述>
	 *
	 * @return
	 * @see [类、类#方法、类#成员]
	 */
	public String getLayoutTitle() {

		return constants.pointTxnFilterRule();
	}

	public IsWidget createCanvas() {
		VerticalPanel panel = new VerticalPanel();

		// 计划编号面板
		VerticalPanel formPanel = new VerticalPanel();
		formPanel.setHeight("70%");

		// 时间周期tab面板
		VerticalPanel timePeriodPanel = new VerticalPanel();
		// 商户mcc面板
		VerticalPanel mccTerminalsPanel = new VerticalPanel();
		// tabPanel.setHeight("30%");

		{
			panel.setWidth("100%");

			TabSetting tabSetting = new TabSetting().dblClickToClose(true)
					.dragToMove(true).showSwitch(true);
			Tab tab1 = new Tab(tabSetting);
			Tab tab2 = new Tab(tabSetting);
			tab1.setWidth("98%");
			tab2.setWidth("98%");

			// 交易币种Form
			currencyForm = new KylinForm();
			{
				currencyForm.setWidth(450);
				currencyForm.setCol(2);
				currencyForm.setField(uTxnFilterRule.CurrCd().required(true)
						.asSelectItem());
			}

			panel.add(currencyForm);

			SelectItemUtil.initSelectItem(currencyForm, uTxnFilterRule.CurrCd()
					.getName(), SelectType.LABLE,
					"rpc/pcmSelectOptionServer/getCurrencyCd");

			// 活动周期Form
			daysForm = new KylinForm();
			{
				daysForm.setWidth("98%");
				daysForm.setTitle(constants.pointActivityCycle());
				daysForm.getSetting().labelWidth(130).labelAlign("right")
						.inputWidth(16).space(20);

				LinkedHashMap<String, String> checkMap = dayDomainClient
						.asLinkedHashMap(false);
				dayitems = new HashMap<Day, BooleanColumnHelper>();

				List<BooleanColumnHelper> formItems = new ArrayList<BooleanColumnHelper>();
				for (Day day : Day.values()) {
					BooleanColumnHelper item = new BooleanColumnHelper(
							day.toString(), day.toString()
									+ checkMap.get(day.toString()))
							.asCheckBoxItem().setGroup(
									constants.pointActivityCycle());
					dayitems.put(day, item);
					formItems.add(item);
				}
				daysForm.setField(formItems
						.toArray(new BooleanColumnHelper[formItems.size()]));

			}
			panel.add(daysForm);

			// 时间周期layout
			timePeriodPanel.add(timePeriodLayout.createCanvas());
			panel.add(timePeriodPanel);

			// 交易类型Form
			supportTransTypesForm = new KylinForm();
			{
				supportTransTypesForm.setWidth("98%");
				supportTransTypesForm.setCol(4);
				supportTransTypesForm.getSetting().labelWidth(180)
						.labelAlign("right").inputWidth(16).space(20);
				supportTransTypesForm.setTitle(constants.supportTransTypes());

				LinkedHashMap<String, String> checkMap = authTransTypeDomainClient
						.asLinkedHashMap(false);
				transtypeitems = new HashMap<AuthTransType, BooleanColumnHelper>();

				List<BooleanColumnHelper> formItems = new ArrayList<BooleanColumnHelper>();
				for (AuthTransType authTransType : AuthTransType.values()) {
					BooleanColumnHelper item = new BooleanColumnHelper(
							authTransType.toString(), authTransType.toString()
									+ checkMap.get(authTransType.toString()))
							.asCheckBoxItem().setGroup(
									constants.supportTransTypes());
					transtypeitems.put(authTransType, item);
					formItems.add(item);

				}
				supportTransTypesForm.setField(formItems
						.toArray(new BooleanColumnHelper[formItems.size()]));
			}
			panel.add(supportTransTypesForm);

			// 交易渠道Form
			supportTerminalsForm = new KylinForm();
			{
				supportTerminalsForm.setWidth("98%");
				supportTerminalsForm.setCol(4);
				supportTerminalsForm.getSetting().labelWidth(130)
						.labelAlign("right").inputWidth(16).space(20);
				supportTerminalsForm.setTitle(constants.supportTerminals());

				LinkedHashMap<String, String> checkMap = authTransTerminalDomainClient
						.asLinkedHashMap(false);
				transterminalitems = new HashMap<AuthTransTerminal, BooleanColumnHelper>();

				List<BooleanColumnHelper> formItems = new ArrayList<BooleanColumnHelper>();
				for (AuthTransTerminal authTransTerminal : AuthTransTerminal
						.values()) {
					BooleanColumnHelper item = new BooleanColumnHelper(
							authTransTerminal.toString(),
							authTransTerminal.toString()
									+ checkMap.get(authTransTerminal.toString()))
							.asCheckBoxItem().setGroup(
									constants.supportTerminals());
					transterminalitems.put(authTransTerminal, item);
					formItems.add(item);
				}
				supportTerminalsForm.setField(formItems
						.toArray(new BooleanColumnHelper[formItems.size()]));
			}
			panel.add(supportTerminalsForm);

			// 黑白名单Form
			typeForm = new KylinForm();
			{
				typeForm.setWidth("98%");
				typeForm.setCol(3);
				typeForm.getSetting().labelWidth(60).labelAlign("right")
						.inputWidth(16).space(20);

				LinkedHashMap<String, String> checkMap = listTypeDomainClient
						.asLinkedHashMap(false);
				codeitems = new HashMap<BlackWhiteCode, BooleanColumnHelper>();

				List<BooleanColumnHelper> formItems = new ArrayList<BooleanColumnHelper>();
				for (BlackWhiteCode blackWhiteCode : BlackWhiteCode.values()) {
					BooleanColumnHelper item = new BooleanColumnHelper(
							blackWhiteCode.toString(),
							blackWhiteCode.toString()
									+ checkMap.get(blackWhiteCode.toString()))
							.asCheckBoxItem().setGroup(constants.type());
					codeitems.put(blackWhiteCode, item);
					formItems.add(item);
				}
				typeForm.setField(formItems
						.toArray(new BooleanColumnHelper[formItems.size()]));
			}
			panel.add(typeForm);

			// 商户代码Layout
			mccTerminalsPanel.add(mccTerminalsLayout.createCanvas());
			panel.add(mccTerminalsPanel);

		}

		// panel.add(formPanel);
		// panel.add(tabPanel);

		return panel;
	}

	/**
	 * 更新数据 <功能详细描述>
	 *
	 * @param model
	 * @see [类、类#方法、类#成员]
	 */

	public void updateView(Data data) {
		MapData value= data.asMapData();
		clearValues();
		currencyForm.setFormData(data);
		setCheckBox(value.getData("days"),daysForm);
		setCheckBox(value.getData("supportTransTypes"),supportTransTypesForm);
		setCheckBox(value.getData("supportTerminals"),supportTerminalsForm);
		// 黑白名单应该是单选，当期没有该组件，用checkBox
		setTypeCheckBox(value.getString("listType"),typeForm);
		
		timePeriodLayout.updateLayout(value.getData("timePeriods"));
		mccTerminalsLayout.updateLayout(value.getData("mccTerminals"));
	}

	private void setTypeCheckBox(String data, KylinForm typeForm) {
		Data result = new Data();
		result.asMapData().put(data, true);
		typeForm.setFormData(result);
	}

	// 交换的数据和组件直接获得和设置的数据格式不同，需要转换
	private void setCheckBox(Data data,KylinForm form) {
		ListData list = data.asListData();
		Data result = new Data();
		for(int i =0;i<list.size();i++){
			result.asMapData().put(list.getString(i), true);
		}
		form.setFormData(result);
		
	}

	public void clearValues() {

		currencyForm.getUi().clear();
		daysForm.getUi().clear();
		supportTransTypesForm.getUi().clear();
		supportTerminalsForm.getUi().clear();
		typeForm.getUi().clear();
		timePeriodLayout.clearValues();
		mccTerminalsLayout.clearValues();

	}

	public MapData getValue() {
		MapData value = new MapData();

		MapData.extend(value, getCurrCd(), true);

		value.put("days", getDays());
		value.put("supportTransTypes", getSupportTransTypes());
		value.put("supportTerminals", getSupportTerminals());
		value.put("listType", getType());
		value.put("timePeriods", getTimePeriods());
		value.put("mccTerminals", getMccTerminals());

		return value;
	}

	private MapData getCurrCd() {
		return currencyForm.getSubmitData().asMapData();
	}

	private ListData getDays() {
		return getList(daysForm.getSubmitData().asMapData());
	}

	private Data getSupportTransTypes() {
		return getList(supportTransTypesForm.getSubmitData().asMapData());
	}

	private Data getSupportTerminals() {
		return getList(supportTerminalsForm.getSubmitData().asMapData());
	}

	@SuppressWarnings("rawtypes")
	private String getType() {
		Data type = typeForm.getSubmitData();
		String result = null;
		for (Entry e : type.asMapData().toMap().entrySet()) {
			result = (String) e.getKey();
		}
		return result;
	}

	private Data getTimePeriods() {
		return timePeriodLayout.getValue();
	}

	private MapData getMccTerminals() {
		return mccTerminalsLayout.getValue();
	}

	@SuppressWarnings("rawtypes")
	private ListData getList(MapData md) {
		ListData list = new ListData();
		for (Entry entry : md.toMap().entrySet()) {
		    if(entry.getValue() != null && entry.getValue() instanceof Boolean){
		        Boolean value = (Boolean)entry.getValue();
		        if(value){
		            list.addString((String) entry.getKey());
		        }
		    }
		}
		return list;
	}

    public boolean valid(){
        return currencyForm.valid()
            &&daysForm.valid()
            &&supportTerminalsForm.valid()
            &&supportTransTypesForm.valid()
            &&typeForm.valid();
    }	
}
