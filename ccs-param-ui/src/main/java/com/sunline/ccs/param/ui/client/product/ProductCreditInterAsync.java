package com.sunline.ccs.param.ui.client.product;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ccs.param.def.CustomerServiceFee;
import com.sunline.ccs.param.def.LatePaymentCharge;
import com.sunline.ccs.param.def.LoanMerchant;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.OverlimitCharge;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.Program;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ProductCreditInterAsync {

	void saveOrUpdateFee(Map<String, Serializable> fee, String productCd,
			boolean isDual, AsyncCallback<Void> callback);

	void getProductCredit(String key, AsyncCallback<ProductCredit> callback);

	void saveOrUpdateProductCredit(Map<String, Serializable> productCredit,
			String productCd, AsyncCallback<Void> callback);

	void saveOrUpdateOverlimitCharge(OverlimitCharge overlimitCharge,
			String productCd, boolean isDual, AsyncCallback<Void> callback);

	void saveOrUpdateLatePaymentCharge(LatePaymentCharge latePaymentCharge,
			String productCd, boolean isDual, AsyncCallback<Void> callback);

	void saveOrUpdateProductCredit(Map<String, Serializable> productCredit,
			String productCd, boolean isDual, AsyncCallback<Void> callback);

	void saveOrUpdateCustomerServiceFee(
			Map<String, CustomerServiceFee> cstServiceFee, String productCd,
			AsyncCallback<Void> callback);

	void saveOrUpdateLoanPlans(Map<LoanType, Map<String, String>> loanPlans,
			String productCd, AsyncCallback<Void> callback);

	// void saveOrUpdateTxnFeeList(Map<String, TxnFee[]> txnFeeList, String
	// productCd, AsyncCallback<Void> callback);

	void getPlanTmps(AsyncCallback<List<PlanTemplate>> callback);

	void saveOrUpdatePlanNbrList(Map<PlanType, String> planNbList,
			String productCd, AsyncCallback<Void> callback);

	void getLoanPlans(AsyncCallback<Map<LoanType, List<LoanPlan>>> callback);

	void getMessageValueMaps(
			AsyncCallback<Map<String, LinkedHashMap<String, String>>> callback);

	void getLoanMerchant(AsyncCallback<List<LoanMerchant>> callback);

	void getProgram(AsyncCallback<List<Program>> callback);
}
