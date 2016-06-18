package com.sunline.ccs.param.ui.client.loanMerchant;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.ULoanMerchant;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.utils.StringUtils;
import com.sunline.kylin.web.core.client.Flat;
import com.sunline.kylin.web.core.client.mvp.Token;
import com.sunline.kylin.web.core.client.res.SavePage;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.kylin.web.core.client.util.SelectItemUtil;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.core.client.function.IFunction;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.IClickEventListener;

/**
 * 分组商户维护管理更新页面
 * 
 * @author lisy
 * @version [版本号, Jun 23, 2015]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
@Singleton
public class LoanMerchantDetailPage extends SavePage
{
    
    /**
     * 编辑表单
     */
    private KylinForm editForm;
    
    /**
     * 商户ID
     */
    private String merId;
    
    @Inject
    private ULoanMerchant uLoanMerchant;
    
    /**
     * 省份，只有国家码为中国才有数据
     */
    private TextColumnHelper provinceColumn;
    
    private MapData loanMerchantData;
    
    @Override
    public IsWidget createPage()
    {
        VerticalPanel panel = new VerticalPanel();
        editForm = new KylinForm();
        provinceColumn = uLoanMerchant.MerState().asSelectItem().bindEvent("change", new IFunction()
        {
            
            @Override
            public void function()
            {
                String province = editForm.getFieldValue(uLoanMerchant.MerState().getName());
                if (StringUtils.isNotEmpty(province))
                {
                    SelectItemUtil.initSelectItem(editForm,
                        uLoanMerchant.MerCity().getName(),
                        SelectType.LABLE,
                        "rpc/loanMerchantServer/getCities",
                        province);
                }
                if (loanMerchantData != null)
                {
                    editForm.setFieldValue(uLoanMerchant.MerCity().getName(),
                        loanMerchantData.getString(uLoanMerchant.MerCity().getName()));
                }
            }
        });
        
        editForm.setWidth("100%");
        editForm.setField(uLoanMerchant.MerId().readonly(true).required(true),
            uLoanMerchant.MerName().required(true),
            uLoanMerchant.RecStatus().required(true),
            uLoanMerchant.MerType().required(true),
            uLoanMerchant.MerGroup().required(true).asSelectItem(),
            provinceColumn.required(true),
            uLoanMerchant.MerCity().required(true).asSelectItem(),
            uLoanMerchant.MerAddr().required(true).asTextArea().setNewline(true),
            uLoanMerchant.MerPstlCd().required(true).setNewline(true),
            uLoanMerchant.MerLinkMan().required(true),
            // 此处涉及电话号码验证
            uLoanMerchant.MerPhone().required(true),//.setValidator(this.getPhone)
            uLoanMerchant.PosLoanSupportInd().required(true).asSelectItem(SelectType.LABLE),
            uLoanMerchant.MotoLoanSupportInd().required(true).asSelectItem(SelectType.LABLE),
            uLoanMerchant.EBankLoanSupportInd().required(true).asSelectItem(SelectType.LABLE),
            uLoanMerchant.MacroLoanSupportInd().required(true).asSelectItem(SelectType.LABLE),
            uLoanMerchant.PosLoanSingleAmtMin().required(true).setNewline(true),
            uLoanMerchant.PosLoanSingleAmtMax().required(true),
            uLoanMerchant.SpecLoanSingleAmtMin().required(true),
            uLoanMerchant.SpecLoanSingleAmtMax().required(true),
            uLoanMerchant.PosFeeIssPerc(),
            uLoanMerchant.PosFeeAcqPerc(),
            uLoanMerchant.MacroFeeIssPerc(),
            uLoanMerchant.MacroFeeAcqPerc(),
            uLoanMerchant.MerBranche().asSelectItem(),
            uLoanMerchant.MerBrand().required(true),
            uLoanMerchant.Memo().asTextArea().setColumnWidth("100%"));
        editForm.setCol(2);
	editForm.getSetting().labelWidth(155);

        KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener()
        {
            
            @Override
            public void onClick()
            {
                if (!editForm.valid())
                {
                    return;
                }
                MapData data = editForm.getSubmitData().asMapData();
                RPC.ajax("rpc/loanMerchantServer/updateLoanMerchant", new RpcCallback<Data>()
                {
                    
                    @Override
                    public void onSuccess(Data result)
                    {
                        notice(false);
                        Dialog.tipNotice("修改成功！");
                        Token token = Flat.get().getCurrentToken();
                        token.directPage(LoanMerchantPage.class);
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
                token.directPage(LoanMerchantPage.class);
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
        notice(true);
        editForm.getUi().clear();
        Token token = Flat.get().getCurrentToken();
        merId = token.getParam(uLoanMerchant.MerId().getName());
        RPC.ajax("rpc/loanMerchantServer/getLoanMerchant", new RpcCallback<Data>()
        {
            
            @Override
            public void onSuccess(Data arg0)
            {
                loanMerchantData = arg0.asMapData();
                editForm.setFormData(arg0);
            }
        }, merId);
        // 获取商户分组更新下拉框
        RPC.ajax("rpc/loanMerchantServer/getMerGroup", new RpcCallback<Data>()
        {
            
            @Override
            public void onSuccess(Data result)
            {
                SelectItem<String> si1 = new SelectItem<String>();
                si1.setValue(result.asListData());
                editForm.setFieldSelectData(uLoanMerchant.MerGroup().getName(), si1);
            }
        });
        //获取省份更新下拉框
        RPC.ajax("rpc/loanMerchantServer/getProvince", new RpcCallback<Data>()
        {
            
            @Override
            public void onSuccess(Data result)
            {
                SelectItem<String> si1 = new SelectItem<String>();
                si1.setValue(result.asListData());
                editForm.setFieldSelectData(uLoanMerchant.MerState().getName(), si1);
            }
        });
        //获取所属分行更新下拉框
        RPC.ajax("rpc/ccsSelectOptionServer/getBranchList", new RpcCallback<Data>()
        {
            
            @Override
            public void onSuccess(Data result)
            {
                SelectItem<String> si1 = new SelectItem<String>();
                si1.setValue(result.asListData());
                editForm.setFieldSelectData(uLoanMerchant.MerBranche().getName(), si1);
            }
        });
    }
    /*
     * 国内电话校验
     * 
     * @return
     */
    /*
     public RegExpValidator getPhone() { 
    	 RegExpValidator regExpABC = new
     RegExpValidator(); regExpABC.setExpression(
     "(^[0]\\d{2,3}\\-\\d{7,8}$|^[0]\\d{2,3}\\-\\d{7,8}-\\d{1,4}$)|(^[1-9]\\d{6,7}$)|(^[1-9]\\d{6,7}-\\d{1,4}$)"
     ); regExpABC.setAttribute("errorMessage", constants.phoneMessage()); return
    regExpABC; }
    */
}