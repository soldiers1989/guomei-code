package com.sunline.ccs.param.ui.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.shared.GWT;
import com.sunline.ccs.param.ui.client.organization.AppInjector;
import com.sunline.kylin.web.core.client.Flat;

public class ModuleLoad implements EntryPoint {
	
 // 嵌套到机构参数页面的所有类在stack中对应的key值
    private static final String ORG_IVK_CLASS = "orgIvkClass";
    private static final String PRODUCT_IVK_CLASS = "productIvkClass";

    private List<Object> orgIvkClassList = new ArrayList<Object>();
    private List<Object> productIvkClassList = new ArrayList<Object>();

    @Override
    public void onModuleLoad() {
	AppInjector appInjector = GWT.create(AppInjector.class);

	// 判断stack中是否已经存在机构参数嵌套list
	for (Entry entry : Flat.get().getStack().entrySet()) {
	    if (ORG_IVK_CLASS.equals((String)entry.getKey())) {
		orgIvkClassList = (List<Object>)entry.getValue();
		
		break;
	    }
	}

	// 判断stack中是否已经存在产品参数嵌套list
	for (Entry entry : Flat.get().getStack().entrySet()) {
	    if (PRODUCT_IVK_CLASS.equals((String)entry.getKey())) {
		productIvkClassList = (List<Object>)entry.getValue();

		break;
	    }
	}
	
	//机构队列
	add2OrgIvkList(appInjector.getCcsLoanParamMng());
	add2OrgIvkList(appInjector.getCcsOrgMessage());
	
	//产品队列
	add2productIvkList(appInjector.getCcsLoanPlanParam());
	add2productIvkList(appInjector.getOverlimitCharge());
	add2productIvkList(appInjector.getCcsProductMessage());
	add2productIvkList(appInjector.getCustomerServiceFee());
	add2productIvkList(appInjector.getFeeParam());
	add2productIvkList(appInjector.getCcsLatePaymentCharge());
	add2productIvkList(appInjector.getPlanNbList());
	add2productIvkList(appInjector.getCcsLoanParam());
	add2productIvkList(appInjector.getProductCreditParam());
	add2productIvkList(appInjector.getSetupLoanPosParam());
	add2productIvkList(appInjector.getTxnFeeParam());
	add2productIvkList(appInjector.getAuthProductParam());
	add2productIvkList(appInjector.getFinancialOrgLayout());
	
	Flat.get().getStack().put(ORG_IVK_CLASS, orgIvkClassList);
	Flat.get().getStack().put(PRODUCT_IVK_CLASS, productIvkClassList);
    }
    
    // 往机构页面list中添加对象
    private void add2OrgIvkList(Object obj) {
	if (orgIvkClassList == null || orgIvkClassList.size() == 0) {
	    orgIvkClassList.add(obj);
	}

	if (!orgIvkClassList.contains(obj)) {
	    orgIvkClassList.add(obj);
	}
    }

    // 往产品页面list中添加对象
    private void add2productIvkList(Object obj) {
	if (productIvkClassList == null || productIvkClassList.size() == 0) {
	    productIvkClassList.add(obj);
	}

	if (!productIvkClassList.contains(obj)) {
	    productIvkClassList.add(obj);
	}
    }
}
