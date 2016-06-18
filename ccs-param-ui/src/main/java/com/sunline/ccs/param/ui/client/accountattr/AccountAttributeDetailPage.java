package com.sunline.ccs.param.ui.client.accountattr;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
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
 * 账户参数更新页面
 * 
 * @author lisy
 * @version [版本号, Jun 19, 2015]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
@Singleton
public class AccountAttributeDetailPage extends SavePage
{
    /**
     * 编辑表单
     */
    @Inject
    private AgePmtHierIndDomainClient agePmtHierIndDomainClient;
    
    private VerticalPanel panel;
    
    @Inject
    private AccountAtrributeConstants constants;
    
    private KylinForm editForm;
    
    private EnumColumnHelper<DirectDbIndicator> directDbInd;
    
    private EnumColumnHelper<DownpmtTolInd> downpmtTolInd;
    
    private EnumColumnHelper<PaymentDueDay> paymentDueDay;
    
    private KylinGrid grid;
    
    @Inject
    private UAccountAttribute uAccountAttribute;
    
    //账龄字符串
    private static final String AGE_CDS = "C0123456789";
    
    //账龄-冲销表域
    private TextColumnHelper ageCdField;
    
    private TextColumnHelper agesPmtHierIndField;
    
    private TextColumnHelper agesPmtHierIdField;
    private BooleanColumnHelper pmtDueDayFix;

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
        LinkedHashMap<String, String> linkHashMap = agePmtHierIndDomainClient.asLinkedHashMap();
        ListData listData = new ListData();
        for (Entry<String, String> entry : linkHashMap.entrySet())
        {
            MapData mapData = new MapData();
            mapData.put("id", entry.getKey());
            mapData.put("text", entry.getValue());
            listData.add(mapData);
        }
        agesPmtHierIndField =
            new TextColumnHelper("agesPmtHierInd", constants.agePmtHierInd(), 255).setColunmEditor(new Editor().type(EditorType.SELECT)
                .data(listData.toString()));
        grid.getSetting().enabledEdit(true);
        grid.getSetting().usePager(false);
        editForm = new KylinForm();
        editForm.setWidth("98%");
        directDbInd =
                uAccountAttribute.DirectDbInd().asSelectItem(SelectType.LABLE).bindEvent("change", new IFunction()
                {
                    @Override
                    public void function()
                    {
                        if (StringUtils.isEmpty(editForm.getFieldValue(uAccountAttribute.DirectDbInd().getName())))
                        {
                            return;
                        }
                        //按照提前天数
                        if ("P".equals(editForm.getFieldValue(uAccountAttribute.DirectDbInd().getName())))
                        {
                            editForm.setFieldRequired(uAccountAttribute.DirectDbDays().getName(), true);
                            editForm.setFieldReadOnly(uAccountAttribute.DirectDbDays().getName(), true);
                            // 清除
                            editForm.setFieldValue(uAccountAttribute.DirectDbDate().getName(),"");
                            editForm.setFieldRequired(uAccountAttribute.DirectDbDate().getName(), false);
                            editForm.setFieldReadOnly(uAccountAttribute.DirectDbDate().getName(), false);
                        }
                        else if ("F".equals(editForm.getFieldValue(uAccountAttribute.DirectDbInd().getName())))
                        {
                            // 清除
                            editForm.setFieldValue(uAccountAttribute.DirectDbDays().getName(),"");
                            editForm.setFieldRequired(uAccountAttribute.DirectDbDays().getName(), false);
                            editForm.setFieldReadOnly(uAccountAttribute.DirectDbDays().getName(), false);
                            
                            editForm.setFieldRequired(uAccountAttribute.DirectDbDate().getName(), true);
                            editForm.setFieldReadOnly(uAccountAttribute.DirectDbDate().getName(), true);
                        }
                    }
                    
                });
            downpmtTolInd =
                uAccountAttribute.DownpmtTolInd().asSelectItem(SelectType.LABLE).bindEvent("change", new IFunction()
                {
                    
                    @Override
                    public void function()
                    {
                        if (StringUtils.isEmpty(editForm.getFieldValue(uAccountAttribute.DownpmtTolInd().getName())))
                        {
                            return;
                        }
                        if ("R".equals(editForm.getFieldValue(uAccountAttribute.DownpmtTolInd().getName())))
                        {
                            editForm.setFieldRequired(uAccountAttribute.DownpmtTolPerc().getName(), true);
                            editForm.setFieldReadOnly(uAccountAttribute.DownpmtTolPerc().getName(), true);
                            
                            editForm.setFieldValue(uAccountAttribute.DownpmtTol().getName(),"");
                            editForm.setFieldRequired(uAccountAttribute.DownpmtTol().getName(), false);
                            editForm.setFieldReadOnly(uAccountAttribute.DownpmtTol().getName(), false);
                        }
                        else if ("A".equals(editForm.getFieldValue(uAccountAttribute.DownpmtTolInd().getName())))
                        {
                    	editForm.setFieldValue(uAccountAttribute.DownpmtTolPerc().getName(),"");
                            editForm.setFieldRequired(uAccountAttribute.DownpmtTolPerc().getName(), false);
                            editForm.setFieldReadOnly(uAccountAttribute.DownpmtTolPerc().getName(), false);
                            
                            editForm.setFieldRequired(uAccountAttribute.DownpmtTol().getName(), true);
                            editForm.setFieldReadOnly(uAccountAttribute.DownpmtTol().getName(), true);
                            
                        }
                        else if ("B".equals(editForm.getFieldValue(uAccountAttribute.DownpmtTolInd().getName())))
                        {
                            editForm.setFieldRequired(uAccountAttribute.DownpmtTolPerc().getName(), true);
                            editForm.setFieldReadOnly(uAccountAttribute.DownpmtTolPerc().getName(), true);
                            
                            editForm.setFieldRequired(uAccountAttribute.DownpmtTol().getName(), true);
                            editForm.setFieldReadOnly(uAccountAttribute.DownpmtTol().getName(), true);
                            
                        }
                    }
                });
            pmtDueDayFix=uAccountAttribute.PmtDueDayFix().asCheckBoxItem();
            paymentDueDay =
                uAccountAttribute.PaymentDueDay().asSelectItem(SelectType.LABLE).bindEvent("change", new IFunction()
                {
                    @Override
                    public void function()
                    {
                        if (editForm.getFieldValue(uAccountAttribute.PaymentDueDay().getName()) != null
                            && !"".equals(editForm.getFieldValue(uAccountAttribute.PaymentDueDay().getName())))
                        {
                    	//到期还款日=账单日
                            if ("C".equals(editForm.getFieldValue(uAccountAttribute.PaymentDueDay().getName())))
                            {
                                // 清除
                                editForm.setFieldValue(uAccountAttribute.PmtDueDate().getName(),"");
                                editForm.setFieldReadOnly(uAccountAttribute.PmtDueDate().getName(), false);
                                editForm.setFieldRequired(uAccountAttribute.PmtDueDate().getName(), false);
                                // 清除
                                editForm.setFieldValue(uAccountAttribute.PmtDueDays().getName(),"");
                                editForm.setFieldReadOnly(uAccountAttribute.PmtDueDays().getName(), false);
                                editForm.setFieldRequired(uAccountAttribute.PmtDueDays().getName(), false);
                                // 清除
                                editForm.setFieldValue(pmtDueDayFix.getName(),"");
                                editForm.setFieldReadOnly(pmtDueDayFix.getName(), false);
                                editForm.setFieldRequired(pmtDueDayFix.getName(), false);
                                // 清除
                                editForm.setFieldValue(uAccountAttribute.PmtDueDayFixUnit().getName(),"");
                                editForm.setFieldRequired(uAccountAttribute.PmtDueDayFixUnit().getName(), false);
                                editForm.setFieldReadOnly(uAccountAttribute.PmtDueDayFixUnit().getName(), false);
                            }
                            else if ("D".equals(editForm.getFieldValue(uAccountAttribute.PaymentDueDay().getName())))
                            {
                                editForm.setFieldRequired(uAccountAttribute.PmtDueDays().getName(), true);
                                editForm.setFieldReadOnly(uAccountAttribute.PmtDueDays().getName(), true);
                                // 清除
                                editForm.setFieldValue(uAccountAttribute.PmtDueDate().getName(),"");
                                editForm.setFieldRequired(uAccountAttribute.PmtDueDate().getName(), false);
                                editForm.setFieldReadOnly(uAccountAttribute.PmtDueDate().getName(), false);
                                editForm.setFieldReadOnly(pmtDueDayFix.getName(), true);
                            }
                            else if ("F".equals(editForm.getFieldValue(uAccountAttribute.PaymentDueDay().getName())))
                            {
                                editForm.setFieldValue(uAccountAttribute.PmtDueDays().getName(),"");
                                editForm.setFieldRequired(uAccountAttribute.PmtDueDays().getName(), false);
                                editForm.setFieldReadOnly(uAccountAttribute.PmtDueDays().getName(), false);

                                editForm.setFieldValue(pmtDueDayFix.getName(),"");
                                editForm.setFieldRequired(pmtDueDayFix.getName(), false);
                                editForm.setFieldReadOnly(pmtDueDayFix.getName(), false);
                                // 清除
                                editForm.setFieldValue(uAccountAttribute.PmtDueDayFixUnit().getName(),"");
                                editForm.setFieldRequired(uAccountAttribute.PmtDueDayFixUnit().getName(), false);
                                editForm.setFieldReadOnly(uAccountAttribute.PmtDueDayFixUnit().getName(), false);
                                
                                editForm.setFieldRequired(uAccountAttribute.PmtDueDate().getName(), true);
                                editForm.setFieldReadOnly(uAccountAttribute.PmtDueDate().getName(), true);
                            }
                        } 
                    }
                });
            pmtDueDayFix.bindEvent("change", new IFunction()
            {
                @Override
                public void function()
                {
                    if (editForm.getFieldValue(pmtDueDayFix.getName()) != null
                        && !"".equals(editForm.getFieldValue(pmtDueDayFix.getName())))
                    {
                        if (editForm.getFieldValue(pmtDueDayFix.getName())
                            .equals("true")){
                            editForm.setFieldRequired(uAccountAttribute.PmtDueDayFixUnit().getName(),
                                true);
                        editForm.setFieldReadOnly(uAccountAttribute.PmtDueDayFixUnit().getName(), true);
                        editForm.setFieldValue(uAccountAttribute.PmtDueDayFixUnit().getName(), "31");
                        }
                    }
                    else
                    {
                        editForm.setFieldValue(uAccountAttribute.PmtDueDayFixUnit().getName(),"");
                        editForm.setFieldRequired(uAccountAttribute.PmtDueDayFixUnit().getName(), false);
                        editForm.setFieldReadOnly(uAccountAttribute.PmtDueDayFixUnit().getName(), false);
                    }
                }
                
            });
        editForm.setCol(3);
        
        editForm.setField(uAccountAttribute.AccountAttributeId().required(true).readonly(true),
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
            uAccountAttribute.AllowMinDueAmt(),            //新增允许最小还款额  2015.11.11 chenpy
            uAccountAttribute.SkipEom().asSelectItem(SelectType.LABLE),
            uAccountAttribute.PrepaymentSmsRemainDays()
        		);     
            
        editForm.getSetting().labelWidth(155);
        //,uAccountAttribute.AgesPmtHierId().asSelectItem());
        
        KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
                MapData data = editForm.getSubmitData().asMapData();
                MapData gridData = new MapData();
                gridData.put("agesPmtHierInd", getIndData());
                gridData.put("agesPmtHierId", getIdData());
                MapData.extend(data, gridData, true);
        		if (!editForm.valid())
        		{
        		    return;
        		}
        		
        		//校验到期还款提醒提前天数
        		String pmtDueLtrPrd = editForm.getFieldValue(uAccountAttribute.PmtDueLtrPrd().getName());
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
        		
        		
//        		int prepayApplyEarlyDays = Integer.parseInt(editForm.getFieldValue(uAccountAttribute.PrepayApplyEarlyDays().getName()));
//        		int prepayApplyCutDay = Integer.parseInt(editForm.getFieldValue(uAccountAttribute.PrepayApplyCutDay().getName()));
//        		if(! (prepayApplyCutDay > prepayApplyEarlyDays)){
//        			Dialog.alert("提前还款申请扣款提前日应大于提前还款申请提前天数");
//        			return ;
//        		}
                RPC.ajax("rpc/accountAttributeServer/updateAccountAttr", new RpcCallback<Data>()
                {
                    @Override
                    public void onSuccess(Data result)
                    {
                        notice(false);
                        Dialog.tipNotice("修改成功！");
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
        panel.add(editForm);
        return panel;
    }
    
    @Override
    public void refresh()
    {
        editForm.getUi().clear();
        RPC.ajax("rpc/ccsSelectOptionServer/getPaymentHierarchy", new RpcCallback<Data>()
        {
            
            @Override
            public void onSuccess(Data result)
            {
                //获取撤销冲正标识列ID
                agesPmtHierIdField.setColunmEditor(new Editor().type(EditorType.SELECT).data(result.asListData()
                    .toString()));
                grid.setColumns(ageCdField.setColumnWidth("20%"),
                    agesPmtHierIndField.setColumnWidth("40%"),
                    agesPmtHierIdField.setColumnWidth("40%"));
                panel.add(grid);
                //初始化账龄列
                ListData listData = new ListData();
                for (int i = 0; i < AGE_CDS.length(); i++)
                {
                    MapData mapdata = new MapData();
                    grid.getUi().addEditRow();
                    mapdata.put("ageCd", "" + AGE_CDS.charAt(i));
                    listData.add(mapdata);
                }
                grid.loadData(listData);
                
                Token token = Flat.get().getCurrentToken();
                String accountAttrId = token.getParam(uAccountAttribute.AccountAttributeId().getName());
                RPC.ajax("rpc/accountAttributeServer/getAccountAttr", new RpcCallback<Data>()
                {
                    @Override
                    public void onSuccess(Data arg0)
                    {
                        if(arg0.asMapData()!=null && arg0.asMapData().getData("agesPmtHierInd")!=null){
                            setGridData(arg0.asMapData().getData("agesPmtHierInd").asMapData(),
                            arg0.asMapData().getData("agesPmtHierId").asMapData());
                        }
                        editForm.setFormData(arg0);
                    }
                },
                    accountAttrId);
            }
        });
        
        notice(true);
    }
    
    //设置账龄-撤销冲正表单数据
    public void setGridData(MapData indData, MapData idData)
    {
        ListData ld = new ListData();
        for (int i = 0; i < AGE_CDS.length(); i++)
        {
            char c = AGE_CDS.charAt(i);
            MapData target = new MapData();
            target.put("ageCd", "" + c);
            target.put("agesPmtHierInd", indData.getString("" + c));
            target.put("agesPmtHierId", idData.getString("" + c));
            ld.add(target);
        }
        grid.loadData(ld);
    }
    
    // 拆分账龄-撤销冲正表单数据:账龄标识与账龄ID
    public MapData getIndData()
    {
        MapData target = new MapData();
        ListData ld = new ListData();
        ld = grid.getData();
        
        for (int i = 0; i < AGE_CDS.length(); i++)
        {
            if(ld.get(i).asMapData().getString("agesPmtHierInd")!= null&&
        	    !ld.get(i).asMapData().getString("agesPmtHierInd").equals("")){
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
            if(ld.get(i).asMapData().getString("agesPmtHierId")!= null&&
        	    !ld.get(i).asMapData().getString("agesPmtHierId").equals("")){
            char c = AGE_CDS.charAt(i);
            target.put("" + c, ld.get(i).asMapData().getString("agesPmtHierId"));
        }
        }
        return target;
    }
}