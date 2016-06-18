package com.sunline.ccs.param.ui.client.product;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.param.def.CustomerServiceFee;
import com.sunline.ccs.param.def.LatePaymentCharge;
import com.sunline.ccs.param.def.LoanMerchant;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.OverlimitCharge;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.Program;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("rpc/productCreditServer")
public interface ProductCreditInter extends RemoteService {

	void saveOrUpdateProductCredit(Map<String, Serializable> productCredit, String productCd) throws ProcessException;

	void saveOrUpdateProductCredit(Map<String, Serializable> productCredit, String productCd, boolean isDual) throws ProcessException;

	void saveOrUpdateFee(Map<String, Serializable> fee, String productCd, boolean isDual) throws ProcessException;

	void saveOrUpdateOverlimitCharge(OverlimitCharge overlimitCharge, String productCd, boolean isDual) throws ProcessException;

	void saveOrUpdateLatePaymentCharge(LatePaymentCharge latePaymentCharge, String productCd, boolean isDual) throws ProcessException;

	void saveOrUpdateCustomerServiceFee(Map<String, CustomerServiceFee> cstServiceFee, String productCd) throws ProcessException;

	void saveOrUpdateLoanPlans(Map<LoanType, String> loanPlans, String productCd) throws ProcessException;

//	void saveOrUpdateTxnFeeList(Map<String, TxnFee[]> txnFeeList, String productCd) throws ProcessException;

	void saveOrUpdatePlanNbrList(Map<PlanType, String> planNbList, String productCd) throws ProcessException;

	ProductCredit getProductCredit(String key);

	List<PlanTemplate> getPlanTmps();
	
	/**
	 * 获取所有的分期计划，并按照计划类型分类存放
	 * @return
	 */
	Map<LoanType, List<LoanPlan>> getLoanPlans();
	
	Map<String, LinkedHashMap<String, String>> getMessageValueMaps();
	
	List<LoanMerchant> getLoanMerchant();

	List<Program> getProgram();
	
}
