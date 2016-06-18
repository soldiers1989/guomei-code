package com.sunline.ccs.param.ui.client.program;

import java.io.Serializable;
import java.util.Map;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UProgram;
import com.sunline.ccs.infrastructure.client.ui.UProgramFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.kylin.web.ark.client.helper.EnumColumnHelper;
import com.sunline.kylin.web.ark.client.helper.EventEnum;
import com.sunline.kylin.web.ark.client.helper.IntegerColumnHelper;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.ui.KylinDialog;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.ui.core.client.data.ListData;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.core.client.function.IFunction;
import com.sunline.ui.event.client.IClickEventListener;

@Singleton
public class ProgramFeeInfoDialog extends KylinDialog
{
    @Inject
    private ProgramConstants constants;

    // 保存分期计划模板
    private MapData loanNumData;

    private static final String LoanNum = "loanNum";

    @Inject
    private UProgramFeeDef uProgramFeeDef;

    @Inject
    UProgram uProgram;

    private KylinForm form;

    private EnumColumnHelper loanFeeCalcMethod;// 分期手续费计算方式

    private KylinGrid layoutGrid;

    private MapData layoutData;

    private IntegerColumnHelper loanNumColumn;

    private MapData dialogData;

    private boolean add;
    private LoanPlan loanPlan; // 所选分期产品参数值

    public void setLoanPlan(LoanPlan loanPlan)
    {
	this.loanPlan = loanPlan;
    }

    public LoanPlan getLoanPlan()
    {
	return loanPlan;
    }

    @Override
    protected Widget createContent()
    {
	VerticalPanel panel = new VerticalPanel();
	panel.setWidth("100%");
	setWidth(800);
	setWidth(800);
	setHeight(400);

	setTitle(constants.programTermDetail());

	form = new KylinForm();
	form.getSetting().labelWidth(120);
	loanNumColumn = new IntegerColumnHelper(LoanNum, constants.loanNum(), 8, 0, 999).required(true);
	loanNumColumn.bindEvent(EventEnum.onchange, new IFunction()
	{
	    @SuppressWarnings("unchecked")
	    @Override
	    public void function()
	    {
		String loanNum = form.getFieldValue(LoanNum);

		if (loanNumData != null)
		{
		    Map<String, Serializable> map = (Map<String, Serializable>)loanNumData.toMap();
		    for (String key : map.keySet())
		    {
			if (loanNum == null)
			{
			    break;
			}
			if (loanNum.equals(key))
			{
			    form.setFormData(loanNumData.getData(key).asMapData());
			}
		    }
		}
	    }
	});

	loanFeeCalcMethod =
		uProgramFeeDef.LoanFeeCalcMethod().asSelectItem(SelectType.KEY_LABLE).required(true)
			.bindEvent("change", new IFunction()
			{
			    @Override
			    public void function()
			    {
				String loanFeeCalc = form.getFieldValue(uProgramFeeDef.LoanFeeCalcMethod().getName());
				if (loanFeeCalc != null)
				{
				    if ("A".equals(loanFeeCalc))
				    {
					form.setReadOnly(false, uProgramFeeDef.FeeRate().getName());
					form.setReadOnly(true, uProgramFeeDef.FeeAmount().getName());
					form.setFieldRequired(uProgramFeeDef.FeeRate().getName(), false);
					form.setFieldRequired(uProgramFeeDef.FeeAmount().getName(), true);

				    } else if ("R".equals(loanFeeCalc))
				    {
					form.setReadOnly(true, uProgramFeeDef.FeeRate().getName());
					form.setReadOnly(false, uProgramFeeDef.FeeAmount().getName());
					form.setFieldRequired(uProgramFeeDef.FeeRate().getName(), true);
					form.setFieldRequired(uProgramFeeDef.FeeAmount().getName(), false);
				    }
				}

			    }
			});

	form.setField(isAdd() ? loanNumColumn.asSelectItem() : loanNumColumn.readonly(true), 
		      uProgramFeeDef.MaxAmount().required(true), 
	              uProgramFeeDef.MinAmount().required(true), 
		      uProgramFeeDef.MerFeeRate().required(true),
		      loanFeeCalcMethod, 
		      uProgramFeeDef.FeeRate(), 
		      uProgramFeeDef.FeeAmount(), 
		      uProgramFeeDef.LoanFeeMethod().required(true)
		     );

	form.setCol(2);
	panel.add(form);

	addConfirmButton(new IClickEventListener()
	{
	    @SuppressWarnings("unchecked")
	    @Override
	    public void onClick()
	    {
		if (!form.valid())
		{
		    return;
		}
		boolean flag = false;
		// 得到当前选择的分期数值
		String loan = form.getFieldValue(LoanNum);

		Map<String, Serializable> map = (Map<String, Serializable>)layoutData.toMap();

		// 編輯狀態下的
		for (String key : map.keySet())
		{
		    if (loan.equals(key))
		    {
			layoutData.remove(loan);
			MapData submitData = form.getUi().getSubmitData().asMapData();
			submitData.remove(LoanNum);
			layoutData.put(loan, submitData);
			layoutGrid.loadData(mapDataToGridMapData(layoutData));
			flag = true;
			break;
		    }
		}

		// map中未找到相应数据，直接添加
		if (!flag)
		{
		    MapData submitData = form.getUi().getSubmitData().asMapData();
		    String addLoan = form.getFieldValue(LoanNum);
		    submitData.remove(LoanNum);
		    layoutData.put(addLoan, submitData);
		    layoutGrid.loadData(mapDataToGridMapData(layoutData));
		}
		close();
		form.getUi().clear();

		dialogData = new MapData();
		loanNumData = new MapData();
	    }
	});

	addCancelButton(new IClickEventListener()
	{
	    @Override
	    public void onClick()
	    {
		close();
	    }
	});

	return panel;
    }

    @Override
    protected void updateView()
    {
	form.getUi().clear();
	
	if (isAdd())
	{
	    // 设置分期期数下拉框值
	    setLoanNumItemVal();
	} else
	{
	    if (dialogData == null)
	    {
		dialogData = new MapData();
	    }

	    form.setFormData(dialogData);
	    form.setFieldValue(uProgramFeeDef.MerFeeRate().getName(),
			       dialogData.getString(uProgramFeeDef.MerFeeRate().getName()));
	}
    }

    public void updata(MapData data)
    {
	dialogData = data;
    }

    public boolean isAdd()
    {
	return add;
    }

    public KylinGrid getLayoutGrid()
    {
	return layoutGrid;
    }

    public void setLayoutGrid(KylinGrid layoutGrid)
    {
	this.layoutGrid = layoutGrid;
    }

    public ListData mapDataToGridMapData(MapData toData)
    {
	ListData ShowLoan = new ListData();
	Map<String, Serializable> map = (Map<String, Serializable>)toData.toMap();
	for (String listKey : map.keySet())
	{
	    MapData myMap = toData.getData(listKey).asMapData();
	    myMap.put("loanNum", listKey);
	    ShowLoan.add(myMap);
	}
	return ShowLoan;
    }

    /**
     * 
     * 说明：给分期期数下拉框设置初始值
     * 
     * @return
     * 
     * @author chenshaop
     * 
     *         修改记录： [编号：日期]，[修改人：*** ]，[修改说明：***]
     *
     */
    @SuppressWarnings("unchecked")
    private void setLoanNumItemVal()
    {
	ListData loanNumSelectData = new ListData();
	Map<String, Serializable> map = (Map<String, Serializable>)loanNumData.toMap();

	for (String key : map.keySet())
	{
	    MapData myMap = new MapData();
	    myMap.put("id", Integer.parseInt(key));
	    myMap.put("text", Integer.parseInt(key));
	    loanNumSelectData.add(myMap);
	}

	SelectItem<String> loanNumSelect = new SelectItem<String>(SelectType.KEY_LABLE);
	loanNumSelect.setValue(loanNumSelectData);

	form.setFieldSelectData(loanNumColumn.getName(), loanNumSelect);
    }

    public MapData getLoanNumData()
    {
	return loanNumData;
    }

    public void setLoanNumData(MapData loanNumData)
    {
	this.loanNumData = loanNumData;
    }

    public MapData getLayoutData()
    {
	return layoutData;
    }

    public void setLayoutData(MapData layoutData)
    {
	this.layoutData = layoutData;
    }

    public void setAdd(boolean add)
    {
	this.add = add;
    }
}