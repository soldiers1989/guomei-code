package com.sunline.ccs.param.ui.client.program;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UProgramFeeDef;
import com.sunline.kylin.web.ark.client.helper.IntegerColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ui.core.client.common.Field;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.ListData;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.dialog.client.listener.ConfirmCallbackFunction;
import com.sunline.ui.event.client.EventObjectHandler;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.IDblClickRowEventListener;

@Singleton
public class ProgramFeeLayout
{

    @Inject
    private ProgramFeeInfoDialog programFeeInfoDialog = GWT.create(ProgramFeeInfoDialog.class);
    @Inject
    private UProgramFeeDef uProgramFeeDef;

    @Inject
    private KylinGrid grid;

    private String loanPlanId;

    private KylinForm form;

    private static final String LoanNum = "loanNum";

    @Inject
    private ProgramConstants constants;

    private MapData pageData;

    public String getLayoutTitle()
    {
	return constants.programParam();
    }

    public Widget createCanvas()
    {
	VerticalPanel panel = new VerticalPanel();
	panel.setWidth("100%");
	panel.setHeight("100%");
	
	grid.setWidth("98%");
	grid.setHeight("98%");
	grid.getSetting().usePager(false);

	// 列表刷新按钮
	grid.setHeader(ClientUtils.createRefreshItem(new IClickEventListener()
	{
	    @Override
	    public void onClick()
	    {
		grid.loadData(buildProgramFeeGridData());
	    }
	}));

	// 列表编辑按钮
	grid.addDblClickListener(new IDblClickRowEventListener()
	{
	    @Override
	    public void onDblClickRow(MapData data, String rowid, EventObjectHandler row)
	    {
		programFeeInfoDialog.setAdd(false);
		programFeeInfoDialog.setLoanNumData(new MapData());
		programFeeInfoDialog.setLayoutGrid(grid);
		programFeeInfoDialog.setLayoutData(pageData);
		programFeeInfoDialog.updata(data);
		programFeeInfoDialog.show();
	    }
	});

	// 列表添加按钮
	grid.setHeader(ClientUtils.createAddItem(new IClickEventListener()
	{
	    @Override
	    public void onClick()
	    {
		loanPlanId = form.getFieldValue("loanPlanId");
		programFeeInfoDialog.setAdd(true);
		programFeeInfoDialog.setLayoutGrid(grid);
		programFeeInfoDialog.setLayoutData(pageData);

		// 将分期数据导入
		if (loanPlanId != null)
		{
		    RPC.ajax("rpc/loanPlanServer/getLoanPlan", new RpcCallback<Data>()
		    {
			@Override
			public void onSuccess(Data result)
			{
			    if (result != null
				    && result.asMapData().getData("loanFeeDefMap").asMapData().toMap().size() > 0)
			    {
				programFeeInfoDialog.setLoanNumData(result.asMapData().getData("loanFeeDefMap")
					.asMapData());
				programFeeInfoDialog.show();
			    } else
			    {
				Dialog.alert(constants.planNoExist());
			    }
			}
		    }, loanPlanId);
		} else
		{
		    Dialog.alert("请先选择分期产品ID号码");
		}

		// programFeeInfoDialog.setLoanPlan(loanPlan);
	    }
	}));
	// 列表删除按钮
	grid.setHeader(ClientUtils.createDeleteItem(new IClickEventListener()
	{
	    @Override
	    public void onClick()
	    {
		delete();
	    }
	}));

	grid.setColumns(new IntegerColumnHelper(LoanNum, constants.loanNum(), 10, 0, 999), 
	                uProgramFeeDef.FeeAmount(),
			uProgramFeeDef.MinAmount(), 
			uProgramFeeDef.MaxAmount(), 
			uProgramFeeDef.MerFeeRate(),
			uProgramFeeDef.FeeRate());

	panel.add(grid);
	return panel;

    }

    public String getLoanPlanId()
    {
	return loanPlanId;
    }

    public void setLoanPlanId(String loanPlanId)
    {
	this.loanPlanId = loanPlanId;
    }

    private Map<String, Serializable> getFormValues(KylinForm form)
    {
	Map<String, Serializable> map = new HashMap<String, Serializable>();
	for (Field field : form.getFields())
	{
	    map.put(field.getName(), form.getFieldValue(field.getName()));
	}
	return map;
    }

    public void updateView()
    {
	grid.clearData();
	grid.loadData(buildProgramFeeGridData());
    }

    /**
     * 
     * 说明：将分期期数收费计划转换成listData格式，
     * 	        用来填充活动参数列表
     *  
     * @return
     * 
     * @author chenshaop
     * 
     * 修改记录： 
     * [编号：日期]，[修改人：*** ]，[修改说明：***]
     *
     */
    @SuppressWarnings("unchecked")
    public ListData buildProgramFeeGridData()
    {
	ListData ShowLoan = new ListData();
	Map<String, Serializable> map = (Map<String, Serializable>)pageData.toMap();
	for (String listKey : map.keySet())
	{
	    MapData myMap = pageData.getData(listKey).asMapData();
	    myMap.put("loanNum", listKey);
	    ShowLoan.add(myMap);
	}
	
	return ShowLoan;
    }

    public MapData getPageData()
    {
	return pageData;
    }

    public void setPageData(MapData pageData)
    {
	this.pageData = pageData;
    }

    public KylinForm getForm()
    {
	return form;
    }

    public void setForm(KylinForm form)
    {
	this.form = form;
    }

    public void delete()
    {
	if (grid.getGrid().getSelectedRows().size() <= 0)
	{
	    Dialog.alert("请选择您需要删除的记录");
	} else
	{
	    Dialog.confirm("是否确认要删除？", "提示", new ConfirmCallbackFunction()
	    {
		@Override
		public void corfimCallback(boolean value)
		{
		    if (value)
		    {
			ListData listData = grid.getGrid().getSelectedRows();
			for (int i = 0; i < listData.size(); i++)
			{
			    String key = listData.get(i).asMapData().getString(LoanNum);
			    pageData.remove(key);
			}
			grid.loadData(buildProgramFeeGridData());

		    }
		}
	    });
	}

    }

    public MapData sumbitData()
    {

	MapData md = new MapData();
	md.put("programFeeDef", pageData);
	return md;
    }

}
