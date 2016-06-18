package com.sunline.ccs.param.ui.client.organization;

import com.google.gwt.inject.client.Ginjector;
import com.sunline.ccs.param.ui.client.authproduct.AuthProductParamLayout;
import com.sunline.ccs.param.ui.client.product.CcsProductMessageLayout;
import com.sunline.ccs.param.ui.client.product.CustomerServiceFeeLayout;
import com.sunline.ccs.param.ui.client.product.FeeLayout;
import com.sunline.ccs.param.ui.client.product.FinancialOrgLayout;
import com.sunline.ccs.param.ui.client.product.LatePaymentChargeLayout;
import com.sunline.ccs.param.ui.client.product.LoanParamLayout;
import com.sunline.ccs.param.ui.client.product.LoanPlanLayout;
import com.sunline.ccs.param.ui.client.product.OverlimitChargeLayout;
import com.sunline.ccs.param.ui.client.product.PlanNbListLayout;
import com.sunline.ccs.param.ui.client.product.ProductCreditLayout;
import com.sunline.ccs.param.ui.client.product.SetupLoanPosLayout;
import com.sunline.ccs.param.ui.client.product.TxnFeeLayout;

public interface AppInjector extends Ginjector{
    
    CcsOrgMessageLayout   getCcsOrgMessage();
    
    CcsOrgLayout getCcsLoanParamMng();
    
    LoanPlanLayout getCcsLoanPlanParam();
    
    OverlimitChargeLayout getOverlimitCharge();
    
    CcsProductMessageLayout getCcsProductMessage();
    
    CustomerServiceFeeLayout getCustomerServiceFee();
    
    FeeLayout getFeeParam();
    
    LatePaymentChargeLayout getCcsLatePaymentCharge();
    
    PlanNbListLayout getPlanNbList();
    
    LoanParamLayout getCcsLoanParam();
    
    ProductCreditLayout getProductCreditParam();
    
    SetupLoanPosLayout getSetupLoanPosParam();
    
    TxnFeeLayout getTxnFeeParam();
    
    AuthProductParamLayout getAuthProductParam();
    
    FinancialOrgLayout getFinancialOrgLayout();
    
}
