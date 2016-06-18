package com.sunline.ccs.param.ui.client.loanPlan;

import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.sunline.ccs.infrastructure.client.ui.ULoanFeeDef;
import com.sunline.kylin.web.ark.client.helper.BooleanColumnHelper;
import com.sunline.kylin.web.ark.client.helper.EnumColumnHelper;
import com.sunline.kylin.web.ark.client.helper.IntegerColumnHelper;
import com.sunline.kylin.web.ark.client.ui.KylinDialog;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.core.client.function.IFunction;
import com.sunline.ui.core.client.util.DataUtil;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.IClickEventListener;

public class LoandFeeInfoDialog extends KylinDialog {

	@Inject
	private ULoanFeeDef uLoanFeeDef;

	private KylinForm loanPlanParamForm;// 分期信息页面表格

	private EnumColumnHelper loanFeeCalcMethod;// 分期手续费计算方式

	private EnumColumnHelper prepayPkgFeeCalMethod;// 提前还款手续费计算方式

	private static String LOAN_NUM = "loanNum";

	private KylinGrid layoutGrid;

	private Map<String, MapData> loanFeeInfoMap;

	private BooleanColumnHelper rescheduleInd;// 是否允许展期

	private EnumColumnHelper rescheduleCalcMethod;// 展期手续费计算方式
	
	private String key;

	/**
	 * 判断是否在添加模式
	 * 
	 */
	private boolean add = false;

	@Inject
	private LoanPlanConstants loanPlanConstants;

	@Override
	protected Widget createContent() {
		VerticalPanel panel = new VerticalPanel();
		setWidth(800);
		setHeight(400);
		setTitle(loanPlanConstants.loanTermDetail());
		loanPlanParamForm = new KylinForm();
		{
			loanFeeCalcMethod = uLoanFeeDef.LoanFeeCalcMethod().required(true).bindEvent("change", new IFunction() {

				@Override
				public void function() {
					String loanFeeCalc = loanPlanParamForm.getFieldValue(uLoanFeeDef.LoanFeeCalcMethod().getName());
					if(loanFeeCalc != null) {
						if("A".equals(loanFeeCalc)) {
							loanPlanParamForm.setReadOnly(false, uLoanFeeDef.FeeRate().getName());
							loanPlanParamForm.setFieldRequired(uLoanFeeDef.FeeRate().getName(),false);
							loanPlanParamForm.setReadOnly(true, uLoanFeeDef.FeeAmount().getName());
							loanPlanParamForm.setFieldRequired( uLoanFeeDef.FeeAmount().getName(),true);
						} else if("R".equals(loanFeeCalc)) {
							loanPlanParamForm.setReadOnly(true, uLoanFeeDef.FeeRate().getName());
							loanPlanParamForm.setFieldRequired(uLoanFeeDef.FeeRate().getName(),true);
							loanPlanParamForm.setReadOnly(false, uLoanFeeDef.FeeAmount().getName());
							loanPlanParamForm.setFieldRequired( uLoanFeeDef.FeeAmount().getName(),false);
						}
					}else{
					    loanPlanParamForm.setFieldRequired(uLoanFeeDef.FeeRate().getName(),false);
					    loanPlanParamForm.setFieldRequired( uLoanFeeDef.FeeAmount().getName(),false);
					}
				}
			});
			prepayPkgFeeCalMethod = uLoanFeeDef.PrepayPkgFeeCalMethod().required(true).bindEvent("change", new IFunction() {
				@Override
				public void function() {
					String prepayPkgFeeCalMethodValue = loanPlanParamForm.getFieldValue(uLoanFeeDef.PrepayPkgFeeCalMethod().getName());
					if(prepayPkgFeeCalMethodValue != null) {
						if("A".equals(prepayPkgFeeCalMethodValue)) {
							loanPlanParamForm.setReadOnly(false, uLoanFeeDef.PrepayPkgFeeAmountRate().getName());
							loanPlanParamForm.setFieldRequired( uLoanFeeDef.PrepayPkgFeeAmountRate().getName(),false);
							loanPlanParamForm.setReadOnly(true, uLoanFeeDef.PrepayPkgFeeAmount().getName());
							loanPlanParamForm.setFieldRequired( uLoanFeeDef.PrepayPkgFeeAmount().getName(),true);
						} else if("R".equals(prepayPkgFeeCalMethodValue)) {
							loanPlanParamForm.setReadOnly(true, uLoanFeeDef.PrepayPkgFeeAmountRate().getName());
							loanPlanParamForm.setReadOnly(false, uLoanFeeDef.PrepayPkgFeeAmount().getName());
							loanPlanParamForm.setFieldRequired( uLoanFeeDef.PrepayPkgFeeAmountRate().getName(),true);
							loanPlanParamForm.setFieldRequired( uLoanFeeDef.PrepayPkgFeeAmount().getName(),false);
						}
					}else{
					    loanPlanParamForm.setFieldRequired( uLoanFeeDef.PrepayPkgFeeAmountRate().getName(),false);
					    loanPlanParamForm.setFieldRequired( uLoanFeeDef.PrepayPkgFeeAmount().getName(),false);
					}
				}
			});
			rescheduleInd = uLoanFeeDef.RescheduleInd().asCheckBoxItem().bindEvent("change", new IFunction() {

				@Override
				public void function() {
					validateRescheDule();
				}
			});
			
			rescheduleCalcMethod = uLoanFeeDef.RescheduleCalcMethod().bindEvent("change", new IFunction() {
				@Override
				public void function() {
					String rescheduleCalc = loanPlanParamForm.getFieldValue(uLoanFeeDef.RescheduleCalcMethod().getName());
					if(rescheduleCalc == null){
					    loanPlanParamForm.setFieldRequired( uLoanFeeDef.RescheduleFeeRate().getName(),false);
					    loanPlanParamForm.setFieldRequired( uLoanFeeDef.RescheduleFeeAmount().getName(),false);
					}
					if("A".equals(rescheduleCalc)) {
						loanPlanParamForm.setReadOnly(true, uLoanFeeDef.RescheduleFeeAmount().getName());
						loanPlanParamForm.setFieldRequired( uLoanFeeDef.RescheduleFeeAmount().getName(),true);
						loanPlanParamForm.setReadOnly(false, uLoanFeeDef.RescheduleFeeRate().getName());
						loanPlanParamForm.setFieldRequired( uLoanFeeDef.RescheduleFeeRate().getName(),false);
					} else if("R".equals(rescheduleCalc)) {
						loanPlanParamForm.setReadOnly(false, uLoanFeeDef.RescheduleFeeAmount().getName());
						loanPlanParamForm.setFieldRequired( uLoanFeeDef.RescheduleFeeAmount().getName(),false);
						loanPlanParamForm.setReadOnly(true, uLoanFeeDef.RescheduleFeeRate().getName());
						loanPlanParamForm.setFieldRequired( uLoanFeeDef.RescheduleFeeRate().getName(),true);
					}
				}
			});
			
			IntegerColumnHelper loanNumColumn = new IntegerColumnHelper(LOAN_NUM, loanPlanConstants.loanNum(), 8, 0, 999);
			loanPlanParamForm.getSetting().labelWidth(140).labelAlign("right");
			loanPlanParamForm.setField(loanNumColumn.required(true), 
									uLoanFeeDef.DistributeMethod().required(true), 
									loanFeeCalcMethod,  
									uLoanFeeDef.FeeAmount(),
									uLoanFeeDef.FeeRate().setNewline(true), 
									uLoanFeeDef.LoanFeeMethod().required(true), 
									uLoanFeeDef.MaxAmount().required(true).setNewline(true), 
									uLoanFeeDef.MaxAmountRate(), 
									uLoanFeeDef.MinAmount().required(true).setNewline(true),
									prepayPkgFeeCalMethod,
									uLoanFeeDef.PrepayPkgFeeAmount(), 
									uLoanFeeDef.PrepayPkgFeeAmountRate(),
									rescheduleInd.setNewline(true), 
									(EnumColumnHelper)rescheduleCalcMethod.setNewline(true), 
									uLoanFeeDef.RescheduleFeeAmount(), 
									uLoanFeeDef.RescheduleFeeRate().setNewline(true), 
									uLoanFeeDef.RescheduleFeeMethod().required(true), 
									uLoanFeeDef.PrinScheduleMethod().setNewline(true), 
									uLoanFeeDef.IntTableOverrideId());
			loanPlanParamForm.setFieldRequired(loanFeeCalcMethod.getName(), true);
			loanPlanParamForm.setFieldRequired(prepayPkgFeeCalMethod.getName(), true);
			loanPlanParamForm.setFieldRequired(rescheduleCalcMethod.getName(), true);
			

		}
		panel.add(loanPlanParamForm);
		addConfirmButton(new IClickEventListener() {
			@Override
			public void onClick() {
			    if(!loanPlanParamForm.valid()){
				return;
			    }
				MapData loanPlanParamFormData = loanPlanParamForm.getUi().getSubmitData().asMapData();
				String loanNum = loanPlanParamFormData.getString(LOAN_NUM);
				if(loanNum != null) {
					if(isAdd()){
						if(loanFeeInfoMap.containsKey(loanPlanParamForm.getFieldValue("loanNum"))){
							Dialog.alert("该分期期数已定义");
							return;
						}else{
							loanFeeInfoMap.put(loanPlanParamForm.getFieldValue("loanNum"), loanPlanParamFormData);
						}
					}else{
						loanFeeInfoMap.put(loanPlanParamForm.getFieldValue("loanNum"), loanPlanParamFormData);
					}
				}
				layoutGrid.loadData(StringToData(getGridData(loanFeeInfoMap)));
				clearValues();
				hide();
			}
		});
		addCancelButton(new IClickEventListener() {
			@Override
			public void onClick() {
				updateView();
				hide();
			}
		});
		return panel;
	}
	
	public String getGridData(Map<String, MapData> loanFeeDefMap){
		StringBuffer gridData = new StringBuffer("[");
		int appendIndex = 0;
		for (Entry<String, MapData> entry : loanFeeDefMap.entrySet()){
			String loanNum = entry.getKey();
			MapData loanFeeInfoMapData = entry.getValue();
			gridData.append("{");
			gridData.append("\"loanNum\":\"" + loanNum +"\",");
			gridData.append("\"" + uLoanFeeDef.DistributeMethod().getName() + "\":\"" + loanFeeInfoMapData.getString(uLoanFeeDef.DistributeMethod().getName())+"\",");
			gridData.append("\"" + uLoanFeeDef.LoanFeeCalcMethod().getName() + "\":\"" + loanFeeInfoMapData.getString(uLoanFeeDef.LoanFeeCalcMethod().getName())+"\",");
			gridData.append("\"" + uLoanFeeDef.FeeAmount().getName() + "\":\"" + loanFeeInfoMapData.getString(uLoanFeeDef.FeeAmount().getName())+"\",");
			gridData.append("\"" + uLoanFeeDef.FeeRate().getName() + "\":\"" + loanFeeInfoMapData.getString(uLoanFeeDef.FeeRate().getName())+"\",");
			gridData.append("\"" + uLoanFeeDef.LoanFeeCalcMethod().getName() + "\":\"" + loanFeeInfoMapData.getString(uLoanFeeDef.LoanFeeCalcMethod().getName())+"\"");
			gridData.append("}");
			appendIndex ++;
			if(appendIndex<=loanFeeDefMap.size()-1){
				gridData.append(",");
			}
		}
		gridData.append("]");
		return gridData.toString();
	}

	@Override
	protected void updateView() {
		clearValues();
		loanPlanParamForm.setFieldReadOnly("loanNum", true);
		if(!isAdd()) {
			//如果是编辑，则需要进行赋值
			//周期为key的值
			loanPlanParamForm.setFieldReadOnly(LOAN_NUM, false);
			for (MapData loanFeeDefData : loanFeeInfoMap.values()) {
				if (key != null && key.equals(loanFeeDefData.getString("initTerm"))) {
					loanPlanParamForm.setFormData(loanFeeDefData);
					break;
				}
			}
		}
		validateRescheDule();
	}
	
	public boolean isAdd() {
		return add;
	}

	public void setLoanNum(String key) {
		this.key = key;
	}

	public void setAdd(boolean add) {
		this.add = add;
	}

	public KylinForm getForm() {
		return loanPlanParamForm;
	}

	public void setForm(KylinForm form) {
		this.loanPlanParamForm = form;
	}

	public KylinGrid getLayoutGrid() {
		return layoutGrid;
	}

	public void setLayoutGrid(KylinGrid layoutGrid) {
		this.layoutGrid = layoutGrid;
	}
	
	public void setLoanFeeInfoMap(Map<String, MapData> loanFeeInfoMap) {
		
		this.loanFeeInfoMap = loanFeeInfoMap;
	}

	public Data StringToData(String gridDataString) {
		Data data = new Data();
		data.setJsData(DataUtil.convertDataType(gridDataString));
		return data;
	}
	
	private void clearValues() {
		if(loanPlanParamForm != null) {
			loanPlanParamForm.getUi().clear();
		}
	}

	private void validateRescheDule(){
		Boolean isReschedule = loanPlanParamForm.getFieldBoolValue(uLoanFeeDef.RescheduleInd().getName());
		if(isReschedule != null) {
			if(isReschedule) {
				loanPlanParamForm.setReadOnly(true, rescheduleCalcMethod.getName());
				loanPlanParamForm.setReadOnly(true, uLoanFeeDef.RescheduleFeeAmount().getName());
				loanPlanParamForm.setReadOnly(true, uLoanFeeDef.RescheduleFeeRate().getName());
				loanPlanParamForm.setReadOnly(true, uLoanFeeDef.RescheduleFeeMethod().getName());
				loanPlanParamForm.setReadOnly(true, uLoanFeeDef.PrinScheduleMethod().getName());
				loanPlanParamForm.setReadOnly(true, uLoanFeeDef.IntTableOverrideId().getName());
				
				loanPlanParamForm.setFieldRequired(rescheduleCalcMethod.getName(), true);
				loanPlanParamForm.setFieldRequired(uLoanFeeDef.RescheduleFeeMethod().getName(), true);
			} else {
				loanPlanParamForm.setReadOnly(false, rescheduleCalcMethod.getName());
				loanPlanParamForm.setReadOnly(false, uLoanFeeDef.RescheduleFeeAmount().getName());
				loanPlanParamForm.setReadOnly(false, uLoanFeeDef.RescheduleFeeRate().getName());
				loanPlanParamForm.setReadOnly(false, uLoanFeeDef.RescheduleFeeMethod().getName());
				loanPlanParamForm.setReadOnly(false, uLoanFeeDef.PrinScheduleMethod().getName());
				loanPlanParamForm.setReadOnly(false, uLoanFeeDef.IntTableOverrideId().getName());
				
				loanPlanParamForm.setFieldRequired(rescheduleCalcMethod.getName(), false);
				loanPlanParamForm.setFieldRequired(uLoanFeeDef.RescheduleFeeMethod().getName(), false);
			}
		}
	}
}
