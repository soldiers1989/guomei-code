package com.sunline.ccs.param.ui.client.accountattr;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.domain.AgePmtHierIndDomainClient;
import com.sunline.ccs.infrastructure.client.ui.UAccountAttribute;
import com.sunline.ccs.param.def.enums.DirectDbIndicator;
import com.sunline.ccs.param.def.enums.DownpmtTolInd;
import com.sunline.ccs.param.def.enums.PaymentDueDay;
import com.sunline.kylin.web.ark.client.helper.BooleanColumnHelper;
import com.sunline.kylin.web.ark.client.helper.EnumColumnHelper;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.ark.client.utils.StringUtils;
import com.sunline.kylin.web.core.client.Flat;
import com.sunline.kylin.web.core.client.mvp.Token;
import com.sunline.kylin.web.core.client.res.SavePage;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ui.core.client.common.Editor;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.ListData;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.core.client.enums.EditorType;
import com.sunline.ui.core.client.function.IFunction;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.IClickEventListener;

/**
 * 账户参数添加页面
 * 
 * @author lisy
 * @version [版本号, Jun 29, 2015]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
@Singleton
public class AccountAttributeAddPage extends SavePage
{
    @Inject
    private UAccountAttribute uAccountAttribute;

    private KylinGrid grid;

    private KylinForm addForm;

    private EnumColumnHelper<DirectDbIndicator> directDbInd;

    private EnumColumnHelper<DownpmtTolInd> downpmtTolInd;

    private EnumColumnHelper<PaymentDueDay> paymentDueDay;
    private BooleanColumnHelper pmtDueDayFix;
    @Inject
    private AgePmtHierIndDomainClient agePmtHierIndDomainClient;

    // 账龄字符串
    private static final String AGE_CDS = "C0123456789";

    // 账龄-冲销表域
    private TextColumnHelper ageCdField;

    private TextColumnHelper agesPmtHierIndField;

    private TextColumnHelper agesPmtHierIdField;

    @Inject
    private AccountAtrributeConstants constants;

    private VerticalPanel panel;

    @Override
    public void refresh()
    {

	addForm.getUi().clear();

	RPC.ajax("rpc/ccsSelectOptionServer/getPaymentHierarchy", new RpcCallback<Data>()
	{

	    @Override
	    public void onSuccess(Data result)
	    {
		// 获取撤销冲正标识列
		agesPmtHierIdField.setColunmEditor(new Editor().type(EditorType.SELECT).data(result.asListData()
												     .toString()));
		grid.setColumns(ageCdField.setColumnWidth("20%"), agesPmtHierIndField.setColumnWidth("40%"),
				agesPmtHierIdField.setColumnWidth("40%"));
		panel.add(grid);

		// 初始化账龄列
		ListData listData = new ListData();
		for (int i = 0; i < AGE_CDS.length(); i++)
		{
		    MapData mapdata = new MapData();
		    grid.getUi().addEditRow();
		    mapdata.put("ageCd", "" + AGE_CDS.charAt(i));
		    listData.add(mapdata);
		}
		grid.loadData(listData);
	    }
	});

    }

    @Override
    public IsWidget createPage()
    {
	panel = new VerticalPanel();
	panel.setWidth("100%");
	grid = new KylinGrid();
	grid.setWidth("60%");
	grid.checkbox(false);

	ageCdField = new TextColumnHelper("ageCd", constants.ageCd(), 1).readonly(true);
	agesPmtHierIdField = new TextColumnHelper("agesPmtHierId", constants.agesPmtHierId(), 255);
	LinkedHashMap<String, String> linkHashMap = agePmtHierIndDomainClient.asLinkedHashMap();// =
												// new
												// LinkedHashMap<String,
												// String>();
	ListData listData = new ListData();
	for (Entry<String, String> entry : linkHashMap.entrySet())
	{
	    MapData mapData = new MapData();
	    mapData.put("id", entry.getKey());
	    mapData.put("text", entry.getValue());
	    listData.add(mapData);
	}
	agesPmtHierIndField =
		new TextColumnHelper("agesPmtHierInd", constants.agePmtHierInd(), 255).setColunmEditor(new Editor()
			.type(EditorType.SELECT).data(listData.toString()));

	grid.getSetting().enabledEdit(true);
	grid.getSetting().usePager(false);

	addForm = new KylinForm();
	addForm.setWidth("98%");
	directDbInd =
		uAccountAttribute.DirectDbInd().asSelectItem(SelectType.LABLE).bindEvent("change", new IFunction()
		{
		    @Override
		    public void function()
		    {
			if (StringUtils.isEmpty(addForm.getFieldValue(uAccountAttribute.DirectDbInd().getName())))
			{
			    return;
			}
			// 按照提前天数
			if ("P".equals(addForm.getFieldValue(uAccountAttribute.DirectDbInd().getName())))
			{
			    addForm.setFieldRequired(uAccountAttribute.DirectDbDays().getName(), true);
			    addForm.setFieldReadOnly(uAccountAttribute.DirectDbDays().getName(), true);
			    // 清除
			    addForm.setFieldValue(uAccountAttribute.DirectDbDate().getName(), "");
			    addForm.setFieldRequired(uAccountAttribute.DirectDbDate().getName(), false);
			    addForm.setFieldReadOnly(uAccountAttribute.DirectDbDate().getName(), false);
			} else if ("F".equals(addForm.getFieldValue(uAccountAttribute.DirectDbInd().getName())))
			{
			    // 清除
			    addForm.setFieldValue(uAccountAttribute.DirectDbDays().getName(), "");
			    addForm.setFieldRequired(uAccountAttribute.DirectDbDays().getName(), false);
			    addForm.setFieldReadOnly(uAccountAttribute.DirectDbDays().getName(), false);

			    addForm.setFieldRequired(uAccountAttribute.DirectDbDate().getName(), true);
			    addForm.setFieldReadOnly(uAccountAttribute.DirectDbDate().getName(), true);
			}
		    }

		});
	downpmtTolInd =
		uAccountAttribute.DownpmtTolInd().asSelectItem(SelectType.LABLE).bindEvent("change", new IFunction()
		{

		    @Override
		    public void function()
		    {
			if (StringUtils.isEmpty(addForm.getFieldValue(uAccountAttribute.DownpmtTolInd().getName())))
			{
			    return;
			}
			if ("R".equals(addForm.getFieldValue(uAccountAttribute.DownpmtTolInd().getName())))
			{
			    addForm.setFieldRequired(uAccountAttribute.DownpmtTolPerc().getName(), true);
			    addForm.setFieldReadOnly(uAccountAttribute.DownpmtTolPerc().getName(), true);

			    addForm.setFieldValue(uAccountAttribute.DownpmtTol().getName(), "");
			    addForm.setFieldRequired(uAccountAttribute.DownpmtTol().getName(), false);
			    addForm.setFieldReadOnly(uAccountAttribute.DownpmtTol().getName(), false);
			} else if ("A".equals(addForm.getFieldValue(uAccountAttribute.DownpmtTolInd().getName())))
			{
			    addForm.setFieldValue(uAccountAttribute.DownpmtTolPerc().getName(), "");
			    addForm.setFieldRequired(uAccountAttribute.DownpmtTolPerc().getName(), false);
			    addForm.setFieldReadOnly(uAccountAttribute.DownpmtTolPerc().getName(), false);

			    addForm.setFieldRequired(uAccountAttribute.DownpmtTol().getName(), true);
			    addForm.setFieldReadOnly(uAccountAttribute.DownpmtTol().getName(), true);

			} else if ("B".equals(addForm.getFieldValue(uAccountAttribute.DownpmtTolInd().getName())))
			{
			    addForm.setFieldRequired(uAccountAttribute.DownpmtTolPerc().getName(), true);
			    addForm.setFieldReadOnly(uAccountAttribute.DownpmtTolPerc().getName(), true);

			    addForm.setFieldRequired(uAccountAttribute.DownpmtTol().getName(), true);
			    addForm.setFieldReadOnly(uAccountAttribute.DownpmtTol().getName(), true);

			}
		    }
		});
	pmtDueDayFix = uAccountAttribute.PmtDueDayFix().asCheckBoxItem();
	paymentDueDay =
		uAccountAttribute.PaymentDueDay().asSelectItem(SelectType.LABLE).bindEvent("change", new IFunction()
		{
		    @Override
		    public void function()
		    {
			if (addForm.getFieldValue(uAccountAttribute.PaymentDueDay().getName()) != null
				&& !"".equals(addForm.getFieldValue(uAccountAttribute.PaymentDueDay().getName())))
			{
			    // 到期还款日=账单日
			    if ("C".equals(addForm.getFieldValue(uAccountAttribute.PaymentDueDay().getName())))
			    {
				// 清除
				addForm.setFieldValue(uAccountAttribute.PmtDueDate().getName(), "");
				addForm.setFieldReadOnly(uAccountAttribute.PmtDueDate().getName(), false);
				addForm.setFieldRequired(uAccountAttribute.PmtDueDate().getName(), false);
				// 清除
				addForm.setFieldValue(uAccountAttribute.PmtDueDays().getName(), "");
				addForm.setFieldReadOnly(uAccountAttribute.PmtDueDays().getName(), false);
				addForm.setFieldRequired(uAccountAttribute.PmtDueDays().getName(), false);
				// 清除
				addForm.setFieldValue(pmtDueDayFix.getName(), "");
				addForm.setFieldReadOnly(pmtDueDayFix.getName(), false);
				addForm.setFieldRequired(pmtDueDayFix.getName(), false);
				// 清除
				addForm.setFieldValue(uAccountAttribute.PmtDueDayFixUnit().getName(), "");
				addForm.setFieldRequired(uAccountAttribute.PmtDueDayFixUnit().getName(), false);
				addForm.setFieldReadOnly(uAccountAttribute.PmtDueDayFixUnit().getName(), false);
			    } else if ("D".equals(addForm.getFieldValue(uAccountAttribute.PaymentDueDay().getName())))
			    {
				addForm.setFieldRequired(uAccountAttribute.PmtDueDays().getName(), true);
				addForm.setFieldReadOnly(uAccountAttribute.PmtDueDays().getName(), true);
				// 清除
				addForm.setFieldValue(uAccountAttribute.PmtDueDate().getName(), "");
				addForm.setFieldRequired(uAccountAttribute.PmtDueDate().getName(), false);
				addForm.setFieldReadOnly(uAccountAttribute.PmtDueDate().getName(), false);
				addForm.setFieldReadOnly(pmtDueDayFix.getName(), true);
			    } else if ("F".equals(addForm.getFieldValue(uAccountAttribute.PaymentDueDay().getName())))
			    {
				addForm.setFieldValue(uAccountAttribute.PmtDueDays().getName(), "");
				addForm.setFieldRequired(uAccountAttribute.PmtDueDays().getName(), false);
				addForm.setFieldReadOnly(uAccountAttribute.PmtDueDays().getName(), false);

				addForm.setFieldValue(pmtDueDayFix.getName(), "");
				addForm.setFieldRequired(pmtDueDayFix.getName(), false);
				addForm.setFieldReadOnly(pmtDueDayFix.getName(), false);
				// 清除
				addForm.setFieldValue(uAccountAttribute.PmtDueDayFixUnit().getName(), "");
				addForm.setFieldRequired(uAccountAttribute.PmtDueDayFixUnit().getName(), false);
				addForm.setFieldReadOnly(uAccountAttribute.PmtDueDayFixUnit().getName(), false);

				addForm.setFieldRequired(uAccountAttribute.PmtDueDate().getName(), true);
				addForm.setFieldReadOnly(uAccountAttribute.PmtDueDate().getName(), true);
			    }
			}
		    }
		});
	pmtDueDayFix.bindEvent("change", new IFunction()
	{
	    @Override
	    public void function()
	    {
		if (addForm.getFieldValue(pmtDueDayFix.getName()) != null
			&& !"".equals(addForm.getFieldValue(pmtDueDayFix.getName())))
		{
		    if (addForm.getFieldValue(pmtDueDayFix.getName()).equals("true"))
		    {
			addForm.setFieldRequired(uAccountAttribute.PmtDueDayFixUnit().getName(), true);
			addForm.setFieldReadOnly(uAccountAttribute.PmtDueDayFixUnit().getName(), true);
			addForm.setFieldValue(uAccountAttribute.PmtDueDayFixUnit().getName(), "31");
		    }
		} else
		{
		    addForm.setFieldValue(uAccountAttribute.PmtDueDayFixUnit().getName(), "");
		    addForm.setFieldRequired(uAccountAttribute.PmtDueDayFixUnit().getName(), false);
		    addForm.setFieldReadOnly(uAccountAttribute.PmtDueDayFixUnit().getName(), false);
		}
	    }

	});
	addForm.setField(uAccountAttribute.AccountAttributeId().required(true), 
			uAccountAttribute.AccountType().required(true).asSelectItem(SelectType.LABLE), 
			uAccountAttribute.CycleBaseInd().required(true).asSelectItem(SelectType.LABLE), 
			uAccountAttribute.CycleBaseMult().required(true),
			uAccountAttribute.CashLimitRate().required(true),
			uAccountAttribute.LoanLimitRate().required(true),
			uAccountAttribute.OvrlmtRate().required(true),
			 uAccountAttribute.CollMinpmt().required(true), 
			 uAccountAttribute.CollOnAge().required(true),
			 uAccountAttribute.CollOnFsDlq().asCheckBoxItem(), 
			 uAccountAttribute.CollOnOvrlmt().asCheckBoxItem(),
			 uAccountAttribute.DelqDayInd().required(true).asSelectItem(SelectType.LABLE),
			 uAccountAttribute.DelqLtrPrd().required(true), 
			 uAccountAttribute.DelqTolInd().required(true).asSelectItem(SelectType.LABLE),
			 uAccountAttribute.DelqTol().required(true),
			 uAccountAttribute.DelqTolPerc().required(true), 
			 directDbInd.required(true), 
			 uAccountAttribute.DirectDbDate(), 
			 uAccountAttribute.DirectDbDays(),
			 downpmtTolInd.required(true),
			 uAccountAttribute.DownpmtTol(), 
			 uAccountAttribute.DownpmtTolPerc(),
			 paymentDueDay.required(true),
			 uAccountAttribute.PmtDueDate(),
			 uAccountAttribute.PmtDueDays(),
			 pmtDueDayFix,
			 // uAccountAttribute.PmtDueDayFix().asCheckBoxItem(),
			 uAccountAttribute.PmtDueDayFixUnit(), 
			 uAccountAttribute.PmtDueLtrPrd().required(true),//到期还款提醒提前天数
			 uAccountAttribute.PmtGracePrd().required(true), 
			 uAccountAttribute.StmtMinBal(),
			 uAccountAttribute.StmtOnBpt().asCheckBoxItem(), 
			 uAccountAttribute.CrMaxbalNoStmt(),
			 uAccountAttribute.TlExpPrmptPrd(), 
			 uAccountAttribute.LtrOnContDlq().asCheckBoxItem(),
			 uAccountAttribute.CreditLimitAdjustInterval(),
             uAccountAttribute.IsMergeBillDay(),
	         uAccountAttribute.AcctDescription(),        //新增账户描述   2015-11-11  9:43  chenpy
	         uAccountAttribute.AllowMinDueAmt(),              //新增允许最小还款额  2015.11.11 chenpy
	         uAccountAttribute.SkipEom().asSelectItem(SelectType.LABLE),
	         uAccountAttribute.PrepaymentSmsRemainDays()
			);    
			 
	addForm.getSetting().labelWidth(155);
	addForm.setCol(3);

	KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener()
	{
	    @Override
	    public void onClick()
	    {
		MapData data = addForm.getSubmitData().asMapData();
		MapData gridData = new MapData();
		gridData.put("agesPmtHierInd", getIndData());
		gridData.put("agesPmtHierId", getIdData());
		MapData.extend(data, gridData, true);
		if (!addForm.valid())
		{
		    return;
		}
		//校验到期还款提醒提前天数
		String pmtDueLtrPrd = addForm.getFieldValue(uAccountAttribute.PmtDueLtrPrd().getName());
		int length = pmtDueLtrPrd.length();
		String a = "1234567890,";
		
				for (int i = 0; i < length; i++) {

					char p = pmtDueLtrPrd.charAt(i);
					String b = String.valueOf(p);
						if (!a.contains(b)) {
							Dialog.alert("到期还款提醒提前天数输入错误！");
							return;
					}
				}
		
//		int prepayApplyEarlyDays = Integer.parseInt(addForm.getFieldValue(uAccountAttribute.PrepayApplyEarlyDays().getName()));
//		int prepayApplyCutDay = Integer.parseInt(addForm.getFieldValue(uAccountAttribute.PrepayApplyCutDay().getName()));
//		if(! (prepayApplyCutDay > prepayApplyEarlyDays)){
//			Dialog.alert("提前还款申请扣款提前日应大于提前还款申请提前天数");
//			return ;
//		}
		RPC.ajax("rpc/accountAttributeServer/addAccountAttr", new RpcCallback<Data>()
		{
		    @Override
		    public void onSuccess(Data result)
		    {
			Dialog.tipNotice("增加成功！");
			notice(false);
			Token token = Flat.get().getCurrentToken();
			token.directPage(AccountAttributePage.class);
			Flat.get().goTo(token);
		    }
		}, data);
	    }
	});
	KylinButton cBtn = ClientUtils.creCancelButton(new IClickEventListener()
	{
	    @Override
	    public void onClick()
	    {
		Token token = Flat.get().getCurrentToken();
		token.directPage(AccountAttributePage.class);
		Flat.get().goTo(token);
	    }
	});

	addButton(submitBtn);
	addButton(cBtn);
	panel.add(addForm);
	return panel;
    }

    public MapData getIndData()
    {
	MapData target = new MapData();
	ListData ld = new ListData();
	ld = grid.getData();

	for (int i = 0; i < AGE_CDS.length(); i++)
	{
	    if (ld.get(i).asMapData().getString("agesPmtHierInd") !=null&&
		    !ld.get(i).asMapData().getString("agesPmtHierInd").equals(""))
	    {
		char c = AGE_CDS.charAt(i);
		target.put("" + c, ld.get(i).asMapData().getString("agesPmtHierInd"));
	    }
	}
	return target;
    }

    public MapData getIdData()
    {
	MapData target = new MapData();
	ListData ld = new ListData();
	ld = grid.getData();

	for (int i = 0; i < AGE_CDS.length(); i++)
	{
	    if (ld.get(i).asMapData().getString("agesPmtHierId") != null&&
		    !ld.get(i).asMapData().getString("agesPmtHierId").equals(""))
	    {
		char c = AGE_CDS.charAt(i);
		target.put("" + c, ld.get(i).asMapData().getString("agesPmtHierId"));
	    }
	}
	return target;
    }
}
