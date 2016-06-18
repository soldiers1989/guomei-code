package com.sunline.ccs.param.ui.client.loanMerchant;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sunline.ccs.param.def.LoanMerchant;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.ppy.dictionary.exception.ProcessException;
/*
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.param.def.LoanMerchant;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
*/
@RemoteServiceRelativePath("rpc/loanMerchantServer")
public interface LoanMerchantInter extends RemoteService{
	
	FetchResponse fetchLoanMerchantList(FetchRequest request);
	
	LoanMerchant getLoanMerchant(String merId);
	
	void addLoanMerchant(LoanMerchant loanMerchant) throws ProcessException;
	
	void updateLoanMerchant(LoanMerchant loanMerchant) throws ProcessException;
	
	void deleteLoanMerchant(List<String> keys) throws ProcessException;
	
	TreeMap<String, String> loadAddressData();
	
	LinkedHashMap<String, String> getMerGroup(boolean showLabel);

}
