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
//public class CcsProductMessage implements IParameterWidget {
//
//    public static final String PRRAM_ID = "ccs_product_message";
//    
//    @Inject
//    private ProductCreditConstants constants;
//    
//    @Inject
//    private CcsProductMessageLayout ccsProductMessageLayout;
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
//	return constants.messageTitle();
//    }
//
//    @Override
//    public int getTabIndex() {
//	return 14;
//    }
//
//    @Override
//    public int getBlockIndex() {
//	return 0;
//    }
//
//    @Override
//    public void editable(boolean flag) {
//	List<KylinForm> forms = ccsProductMessageLayout.getForms();
//	for (KylinForm form : forms) {
//	    form.setFormReadOnly(!flag);
//	}
//    }
//
//    @Override
//    public Widget getLayout() {
//	return ccsProductMessageLayout.createCanvas();
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
//	ccsProductMessageLayout.updateView(data);
//    }
//
//    @Override
//    public boolean saveCheck() {
//	if (!ccsProductMessageLayout.updateModel(productCredit)) {
//	    return false;
//	}
//	return true;
//    }
//
//    @Override
//    public MapData getSubmitData() {
//	return ccsProductMessageLayout.getFormValues();
//    }
//
//    /**
//     * 重载方法
//     * @return
//     */
//    @Override
//    public Widget initTabItem()
//    {
//        return ccsProductMessageLayout.initLayout();
//    }
//
//}
