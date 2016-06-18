//package com.sunline.ccs.param.ui.client.product;
//
//import java.util.List;
//
//import com.google.gwt.user.client.ui.Widget;
//import com.google.inject.Inject;
//import com.sunline.ccs.param.def.ProductCredit;
//import com.sunline.kylin.web.ark.client.ui.KylinForm;
//import com.sunline.pcm.facility.client.common.IParameterWidget;
//import com.sunline.ui.core.client.data.Data;
//import com.sunline.ui.core.client.data.MapData;
//
//public class CcsLatePaymentCharge implements IParameterWidget {
//
//    public static final String PRRAM_ID = "ccs_latepayment_charge";
//
//    @Inject
//    private ProductCreditConstants constants;
//    
//    @Inject
//    private LatePaymentChargeLayout latePaymentChargeLayout;
//    
//    private ProductCredit productCredit;
//    
//    @Override
//    public String getParamID() {
//	return PRRAM_ID;
//    }
//
//    @Override
//    public String getTabTitle() {
//	return constants.productLatePaymentChargeInfo();
//    }
//
//    @Override
//    public int getTabIndex() {
//	return 5;
//    }
//
//    @Override
//    public int getBlockIndex() {
//	return 0;
//    }
//
//    @Override
//    public void editable(boolean flag) {
//	List<KylinForm> forms = latePaymentChargeLayout.getForms();
//	for (KylinForm form : forms) {
//	    form.setFormReadOnly(!flag);
//	}
//    }
//
//    @Override
//    public Widget getLayout() {
//	return latePaymentChargeLayout.createCanvas();
//    }
//
//    @Override
//    public String getDataType() {
//	if (productCredit == null) {
//	    productCredit = new ProductCredit();
//	}
//	return productCredit.getClass().getName().toString();
//    }
//
//    @Override
//    public void refresh(Data data) {
//	latePaymentChargeLayout.updateView(data);
//    }
//
//    @Override
//    public boolean saveCheck() {
//	if (!latePaymentChargeLayout.updateModel(productCredit)) {
//	    return false;
//	}
//	return true;
//    }
//
//    @Override
//    public MapData getSubmitData() {
//	return latePaymentChargeLayout.getFormValues();
//    }
//
//    /**
//     * 重载方法
//     * @return
//     */
//    @Override
//    public Widget initTabItem()
//    {
//        return latePaymentChargeLayout.initLayout();
//    }
//
//}
