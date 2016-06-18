package com.sunline.ccs.param.ui.client.pointplan;
/*
import com.sunline.ccs.infrastructure.client.ui.UPointPlan;
import com.sunline.ccs.infrastructure.shared.map.PointPlanMapHelper;
import com.sunline.ccs.param.def.PointPlan;
import com.sunline.ark.gwt.client.support.LazyLayout;
import com.sunline.ark.gwt.client.ui.LayoutControl;
import com.sunline.ark.gwt.client.ui.YakDynamicForm;
import com.google.inject.Inject;
import com.smartgwt.client.widgets.form.fields.FormItem;
*/
public class PointPlanBasicLayout /*extends LazyLayout implements LayoutControl<PointPlan>*/{
/*
	@Inject
	private UPointPlan uPointPlan;
	
	@Inject
	private PointPlanConstants constants;
	
	private YakDynamicForm pointPlanBasicForm;
	
	private FormItem planNbr;
	
	private boolean isAdd = true;
	
	public void setAdd(boolean isAdd) {
		this.isAdd = isAdd;
	}

	@Override
	public String getLayoutTitle() {
		
		return constants.pointPlan();
	}

	@Override
	public void createCanvas() {
		
		setMembersMargin(10);
		
		pointPlanBasicForm = new YakDynamicForm();
		{
			pointPlanBasicForm.setWidth100();
			pointPlanBasicForm.setAutoHeight();
			pointPlanBasicForm.setNumCols(4);
			pointPlanBasicForm.setColWidths(150, 150, 200, "*");
			
			if(isAdd)
				planNbr = uPointPlan.PlanNbr().required().createFormItem();
			else
				planNbr = uPointPlan.PlanNbr().asLabel().createFormItem();
			
			pointPlanBasicForm.setFields(planNbr,
					uPointPlan.PlanType().required().createSelectItem(),
					uPointPlan.StartDate().required().createFormItem(),
					uPointPlan.EndDate().required().endRow(true).createFormItem(),
					uPointPlan.Description().asTextArea().colSpan(3).width(500).createFormItem());
		}
		addMember(pointPlanBasicForm);
	}

	@Override
	public void updateView(PointPlan model) {
		clearValues();
		
		pointPlanBasicForm.setValues(PointPlanMapHelper.convertToMap(model));
	}

	@Override
	public boolean updateModel(PointPlan model) {
		if(!pointPlanBasicForm.validate())
			return false;
		
		PointPlanMapHelper.updateFromMap(model, pointPlanBasicForm.getValues());
		return true;
	}

	@Override
	public void clearValues() {
		
		pointPlanBasicForm.clearValues();
		pointPlanBasicForm.reset();
	}

	@Override
	public boolean isChanged() {
		
		return pointPlanBasicForm.valuesHaveChanged();
	}
*/
	
}
