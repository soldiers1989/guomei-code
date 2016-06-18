package com.sunline.ccs.param.ui.client.util;

import java.util.LinkedHashMap;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("rpc/ccsSelectOptionServer")
public interface CcsSelectOptionInter extends RemoteService {

	public LinkedHashMap<String, String> getTxnCd();

	public LinkedHashMap<String, String> getPlantemplateCd();

	public LinkedHashMap<String, String> getLoanPLan();

	public LinkedHashMap<String, String> getAcctAtrrId();

	public LinkedHashMap<String, String> getPaymentHierarchy();

	public LinkedHashMap<String, String> getInterestId();
	
	public LinkedHashMap<String, String> getBranchList();
	
	public LinkedHashMap<String, String> getProdcreditList();
	
	public LinkedHashMap<String, String> getLoanPLanForType();
	
	public LinkedHashMap<String, String> getMerList();
	
//	public LinkedHashMap<String, String> getMccList();
}
