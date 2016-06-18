package com.sunline.ccs.param.ui.server;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.InterestTable;
import com.sunline.ccs.param.def.LoanMerchant;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.PaymentHierarchy;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ccs.param.def.TxnCd;
//import com.sunline.gls.param.def.BnpGroupMapping;
import com.sunline.kylin.web.ark.client.data.SelectOptionEntry;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.facility.ParameterFacility;
import com.sunline.pcm.param.def.Branch;
import com.sunline.pcm.param.def.CurrencyCd;
import com.sunline.pcm.param.def.Product;
import com.sunline.ppy.dictionary.enums.AccountType;

@Controller
@RequestMapping(value = "/ccsSelectOptionServer")
public class CcsSelectOptionServer
{
    
    @Autowired
    private ParameterFacility parameterFacility;
    
   	@ResponseBody
   	@RequestMapping(value = "/getTxnCd", method = {RequestMethod.POST})
   	public List<SelectOptionEntry> getTxnCd() {
   		List<TxnCd> txnCdList = parameterFacility.getParameterObject(TxnCd.class);
   		
   		List<SelectOptionEntry> txnSelect = new ArrayList<SelectOptionEntry>();
   		
   		if(txnCdList != null && !txnCdList.isEmpty()) {
   			for(TxnCd txnCd : txnCdList) {
   				SelectOptionEntry entry = new SelectOptionEntry();
   				entry.setId(txnCd.txnCd);
   				entry.setText(txnCd.txnCd + " - " + txnCd.description);
   				txnSelect.add(entry);
   			}
   		}			
   		return txnSelect;
   	}
   	
   	/**
   	* @Description 罚息利率表
   	* @author 鹏宇
   	* @date 2015-11-11 下午4:58:11
   	 */
   	@ResponseBody
   	@RequestMapping(value = "/getInterestTableList" , method= {RequestMethod.POST})
   	public List<SelectOptionEntry> getInterestTableList(){
   		List<InterestTable> list = parameterFacility.getParameterObject(InterestTable.class);
   		List<SelectOptionEntry> txnSelect = new ArrayList<SelectOptionEntry>();
   		
   		if(list != null && ! list.isEmpty()){
   			for(InterestTable interestTable : list){
   				SelectOptionEntry entry = new SelectOptionEntry();
   				entry.setId(interestTable.intTableId);
   				entry.setText(/*interestTable.intTableId +"-"+*/interestTable.description);
   				txnSelect.add(entry);
   			}
   		}
   		return txnSelect;
   	}
    
    public LinkedHashMap<String, String> getPlantemplateCd()
    {
        List<PlanTemplate> planTemplateList = parameterFacility.getParameterObject(PlanTemplate.class);
        LinkedHashMap<String, String> planTemplateMap = new LinkedHashMap<String, String>();
        if (planTemplateList != null && !planTemplateList.isEmpty())
        {
            for (PlanTemplate planTemplate : planTemplateList)
            {
                planTemplateMap.put(planTemplate.planNbr, planTemplate.planNbr + " - " + planTemplate.description);
            }
        }
        return planTemplateMap;
    }
    @ResponseBody
    @RequestMapping(value = "/getLoanPlan", method = {RequestMethod.POST})
    public List<SelectOptionEntry> getLoanPlan() {
	List<LoanPlan> loanPlanList = parameterFacility.getParameterObject(LoanPlan.class);
	List<SelectOptionEntry> uiShowData = new ArrayList<SelectOptionEntry>();
	if (loanPlanList != null && !loanPlanList.isEmpty()) {
	    for (LoanPlan loanPlan : loanPlanList) {
		SelectOptionEntry entry = new SelectOptionEntry(loanPlan.loanCode, loanPlan.description);
		uiShowData.add(entry);
	    }
	}
	return uiShowData;
    }
    
    @ResponseBody
    @RequestMapping(value = "/getAcctDescription", method = {RequestMethod.POST})
    public List<SelectOptionEntry> getAcctDescription() {
	List<AccountAttribute> acctAttrList = parameterFacility.getParameterObject(AccountAttribute.class);
	LinkedHashMap<Integer, AccountAttribute> accountAttributeMap = new LinkedHashMap<Integer, AccountAttribute>();
	if (acctAttrList != null && !acctAttrList.isEmpty()) {
	    for (AccountAttribute aal : acctAttrList) {
		accountAttributeMap.put(aal.accountAttributeId, aal);
	    }
	}
	List<SelectOptionEntry> uiShowData = new ArrayList<SelectOptionEntry>();
	if (accountAttributeMap != null && accountAttributeMap.size() > 0) {
	    for (Entry<Integer, AccountAttribute> enty : accountAttributeMap.entrySet()) {
		SelectOptionEntry entry = new SelectOptionEntry();
		entry.setId(enty.getValue().accountAttributeId);
		entry.setText(enty.getValue().acctDescription);
		uiShowData.add(entry);
	    }
	}
	return uiShowData;
    }
    
    @ResponseBody
    @RequestMapping(value = "/getAcctAtrrId", method = {RequestMethod.POST})
    public List<SelectOptionEntry> getAcctAtrrId() {
	List<AccountAttribute> acctAttrList = parameterFacility.getParameterObject(AccountAttribute.class);
	LinkedHashMap<Integer, AccountAttribute> accountAttributeMap = new LinkedHashMap<Integer, AccountAttribute>();
	if (acctAttrList != null && !acctAttrList.isEmpty()) {
	    for (AccountAttribute aal : acctAttrList) {
		accountAttributeMap.put(aal.accountAttributeId, aal);
	    }
	}
	List<SelectOptionEntry> uiShowData = new ArrayList<SelectOptionEntry>();
	if (accountAttributeMap != null && accountAttributeMap.size() > 0) {
	    for (Entry<Integer, AccountAttribute> enty : accountAttributeMap.entrySet()) {
		SelectOptionEntry entry = new SelectOptionEntry();
		entry.setId(enty.getValue().accountAttributeId);
		entry.setText(getAccountTypeDesc(enty.getValue().accountType.toString().charAt(0)));
		uiShowData.add(entry);
	    }
	}
	return uiShowData;
    }
    
    //用于获取账户类型相对应的中文描述
    public String getAccountTypeDesc(char acctType){
    	switch(acctType){
    	case 'A': return "人民币独立基本信用账户";
    	case 'B': return "美元独立基本信用账户";
    	case 'C': return "人民币共享基本信用账户";
    	case 'D': return "美元共享基本信用账户";
    	case 'E': return "人民币独立小额贷款账户";
    	case 'F': return "人民币活期借记账户";
    	default : return "其他";
    	}
    }
    
    @ResponseBody
    @RequestMapping(value = "/getCurrencyCdList", method = {RequestMethod.POST})
    public List<SelectOptionEntry> getCurrencyCdList()
    {
        List<CurrencyCd> currencyCdList = parameterFacility.getParameterObject(CurrencyCd.class);
        LinkedHashMap<String, CurrencyCd> currencyCdMap = new LinkedHashMap<String, CurrencyCd>();
        if (currencyCdList != null && !currencyCdList.isEmpty())
        {
            for (CurrencyCd cc : currencyCdList)
            {
                currencyCdMap.put(cc.currencyCd, cc);
            }
        }
        List<SelectOptionEntry> uiShowData = new ArrayList<SelectOptionEntry>();
        if (currencyCdMap != null && currencyCdMap.size() > 0)
        {
            for (Entry<String, CurrencyCd> enty : currencyCdMap.entrySet())
            {
                SelectOptionEntry entry = new SelectOptionEntry();
                entry.setId(enty.getValue().currencyCd);
                entry.setText(enty.getValue().description);
                uiShowData.add(entry);
            }
        }
        return uiShowData;
    }
    
    //
    @ResponseBody
    @RequestMapping(value = "/getPaymentHierarchy", method = {RequestMethod.POST})
    public List<SelectOptionEntry> getPaymentHierarchy(){
	List<PaymentHierarchy> paymentHierarchies = parameterFacility.getParameterObject(PaymentHierarchy.class);
	List<SelectOptionEntry> uiShowData = new ArrayList<SelectOptionEntry>();
	if (paymentHierarchies != null && !paymentHierarchies.isEmpty()) {
	    for (PaymentHierarchy paymentHierarchy : paymentHierarchies) {
		SelectOptionEntry entry = new SelectOptionEntry(paymentHierarchy.pmtHierId, paymentHierarchy.description);
		uiShowData.add(entry);
	    }
	}
	return uiShowData;
    }
    
    /**
     * <功能详细描述>
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    @ResponseBody
    @RequestMapping("/getInterestId")
    public LinkedHashMap<String, String> getInterestId()
    {
        List<InterestTable> interestList = parameterFacility.getParameterObject(InterestTable.class);
        LinkedHashMap<String, String> interestMap = new LinkedHashMap<String, String>();
        if (interestList != null && !interestList.isEmpty())
        {
            for (InterestTable interestTable : interestList)
            {
                interestMap.put(interestTable.intTableId.toString(), /*interestTable.intTableId.toString() + "-"
                    +*/ interestTable.description);
            }
        }
        return interestMap;
    }
    
    @ResponseBody
    @RequestMapping(value = "/getBranchList", method = {RequestMethod.POST})
    public List<SelectOptionEntry> getBranchList() {
	List<Branch> branchList = parameterFacility.getParameterObject(Branch.class);
	List<SelectOptionEntry> uiShowData = new ArrayList<SelectOptionEntry>();
	if (branchList != null && !branchList.isEmpty()) {
	    for (Branch branch : branchList) {
		SelectOptionEntry entry = new SelectOptionEntry(branch.branchId, branch.branchId + "-" + branch.name);
		uiShowData.add(entry);
	    }
	}
	return uiShowData;
    }

    @ResponseBody
    @RequestMapping(value = "/getProdcreditList", method = {RequestMethod.POST})
    public List<SelectOptionEntry> getProdcreditList() {
	List<Product> productList = parameterFacility.getParameterObject(Product.class);
	List<SelectOptionEntry> uiShowData = new ArrayList<SelectOptionEntry>();
	if (productList != null && !productList.isEmpty()) {
	    for (Product product : productList) {
		SelectOptionEntry entry =
			new SelectOptionEntry(product.productCode, product.productCode + "-" + product.description);
		uiShowData.add(entry);
	    }
	}
	return uiShowData;
    }
    
    @ResponseBody
    @RequestMapping(value = "/getLoanPlanForType", method = {RequestMethod.POST})
    public LoanPlan getLoanPlanForType(@RequestBody String key) throws FlatException
    {
        try
        {
            return parameterFacility.getParameterObject(key,LoanPlan.class);
        }
        catch (Exception e)
        {
            //logger.error(e.getMessage(), e);
            throw new FlatException("获取分期计划详细信息失败", e);
        }
    }
    
    @ResponseBody
    @RequestMapping(value = "/getMerList", method = {RequestMethod.POST})
    public List<SelectOptionEntry> getMerList() {
	List<LoanMerchant> loanmerchantList = parameterFacility.getParameterObject(LoanMerchant.class);
	List<SelectOptionEntry> uiShowData = new ArrayList<SelectOptionEntry>();
	if (loanmerchantList != null && !loanmerchantList.isEmpty()) {
	    for (LoanMerchant loanmerchant : loanmerchantList) {
		SelectOptionEntry entry =
			new SelectOptionEntry(loanmerchant.merId, loanmerchant.merId + "-" + loanmerchant.merName);
		uiShowData.add(entry);
	    }
	}
	return uiShowData;
    }
    
	@ResponseBody
	@RequestMapping(value ="/getLoanPlanForSelect")
	public LinkedHashMap<String, String> getLoanPlanForSelect() {

		List<LoanPlan> loanPlanList = parameterFacility.getParameterObject(LoanPlan.class);
		LinkedHashMap<String, String> loanPlanMap = new LinkedHashMap<String, String>();
		if (loanPlanList != null && !loanPlanList.isEmpty()) {
			for (LoanPlan loanPlan : loanPlanList) {
				
				if("C".equals(loanPlan.loanType.toString()) || "R".equals(loanPlan.loanType.toString()) || "P".equals(loanPlan.loanType.toString()) || "B".equals(loanPlan.loanType.toString())){
					loanPlanMap.put(loanPlan.loanCode, loanPlan.loanCode + " - " + loanPlan.description);
				}
			}
		}
		return loanPlanMap;
	}
}
