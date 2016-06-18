package com.sunline.ccs.param.ui.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sunline.ark.support.meta.MapUtils;
import com.sunline.ccs.infrastructure.shared.map.LoanMerchantMapHelper;
import com.sunline.ccs.param.def.CustomerServiceFee;
import com.sunline.ccs.param.def.Fee;
import com.sunline.ccs.param.def.LatePaymentCharge;
import com.sunline.ccs.param.def.LoanMerchant;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.OverlimitCharge;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.Program;
import com.sunline.pcm.facility.ParameterFacility;
import com.sunline.pcm.param.def.BMPMessageTemplate;
import com.sunline.pcm.param.def.FinancialOrg;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ppy.dictionary.exception.ProcessException;

@Controller
@RequestMapping(value = "/productCreditServer")
@Transactional
public class ProductCreditServer
{
    @Autowired
    private ParameterFacility parameterFacility;
    
    @ResponseBody
    @RequestMapping("/saveOrUpdateFee")
    public void saveOrUpdateFee(@RequestBody Fee fee, @RequestBody String productCd,
        @RequestBody boolean isDual) throws ProcessException
    {
        ProductCredit productCredit = parameterFacility.getParameterObject(productCd, ProductCredit.class);
//        Fee fee = new Fee();
//        MapUtils.updateFieldFromMap(fee, feeMap);
        if (productCredit == null)
        {
            productCredit = new ProductCredit();
            if (isDual)
            {
                productCredit.dualFee = fee;
            }
            else
            {
                productCredit.fee = fee;
            }
            parameterFacility.addNewParameter(productCd, productCredit);
        }
        else
        {
            if (isDual)
            {
                productCredit.dualFee = fee;
            }
            else
            {
                productCredit.fee = fee;
            }
            parameterFacility.updateParameterObject(productCd, productCredit);
        }
    }
    
    @ResponseBody
    @RequestMapping("/getProductCredit")
    public ProductCredit getProductCredit(@RequestBody String key)
    {
        return parameterFacility.getParameterObject(key, ProductCredit.class);
    }
    
    public void saveOrUpdateProductCredit(@RequestBody Map<String, Serializable> prdtCreditMap,
        @RequestBody String productCd) throws ProcessException
    {
        ProductCredit productCredit = parameterFacility.getParameterObject(productCd, ProductCredit.class);
        MapUtils.updateFieldFromMap(productCredit, prdtCreditMap);
        if (productCredit == null)
        {
            parameterFacility.addNewParameter(productCd, productCredit);
        }
        else
        {
            parameterFacility.updateParameterObject(productCd, productCredit);
        }
    }
    
    @ResponseBody
    @RequestMapping("/saveOrUpdateOverlimitCharge")
    public void saveOrUpdateOverlimitCharge(@RequestBody OverlimitCharge overlimitCharge,
        @RequestBody String productCd, @RequestBody boolean isDual) throws ProcessException
    {
        // 校验利率最大金额
        // ServerUtil.validateRateCeil(overlimitCharge.rateCeils);
        ProductCredit productCredit = parameterFacility.getParameterObject(productCd, ProductCredit.class);
        if (productCredit == null)
        {
            productCredit = new ProductCredit();
            if (isDual)
            {
                productCredit.dualOverlimitCharge = overlimitCharge;
            }
            else
            {
                productCredit.overlimitCharge = overlimitCharge;
            }
            parameterFacility.addNewParameter(productCd, productCredit);
        }
        else
        {
            if (isDual)
            {
                productCredit.dualOverlimitCharge = overlimitCharge;
            }
            else
            {
                productCredit.overlimitCharge = overlimitCharge;
            }
            parameterFacility.updateParameterObject(productCd, productCredit);
        }
    }
    
    @ResponseBody
    @RequestMapping("/saveOrUpdateLatePaymentCharge")
    public void saveOrUpdateLatePaymentCharge(@RequestBody LatePaymentCharge latePaymentCharge,
        @RequestBody String productCd, @RequestBody boolean isDual) throws ProcessException
    {
        // 校验利率最大金额
        // ServerUtil.validateRateCeil(latePaymentCharge.rateCeils);
        ProductCredit productCredit = parameterFacility.getParameterObject(productCd, ProductCredit.class);
        if (productCredit == null)
        {
            productCredit = new ProductCredit();
            if (isDual)
            {
                productCredit.dualLatePaymentCharge = latePaymentCharge;
            }
            else
            {
                productCredit.latePaymentCharge = latePaymentCharge;
            }
            parameterFacility.addNewParameter(productCd, productCredit);
        }
        else
        {
            if (isDual)
            {
                productCredit.dualLatePaymentCharge = latePaymentCharge;
            }
            else
            {
                productCredit.latePaymentCharge = latePaymentCharge;
            }
            parameterFacility.updateParameterObject(productCd, productCredit);
        }
    }
    
    @ResponseBody
    @RequestMapping("/saveOrUpdateProductCredit")
    public void saveOrUpdateProductCredit(@RequestBody Map<String, Serializable> productCreditMap,
        @RequestBody String productCd, @RequestBody boolean isDual) throws ProcessException
    {
        ProductCredit productCredit = parameterFacility.getParameterObject(productCd, ProductCredit.class);
        ProductCredit pdtCredit = new ProductCredit();
        MapUtils.updateFieldFromMap(pdtCredit, productCreditMap);
        if (productCredit == null)
        {
            productCredit = new ProductCredit();
            if (isDual)
            {
                productCredit.dualAccountAttributeId = pdtCredit.dualAccountAttributeId;
            }
            else
            {
                productCredit.accountAttributeId = pdtCredit.accountAttributeId;
            }
            parameterFacility.addNewParameter(productCd, productCredit);
        }
        else
        {
            if (isDual)
            {
                productCredit.dualAccountAttributeId = pdtCredit.dualAccountAttributeId;
            }
            else
            {
                productCredit.accountAttributeId = pdtCredit.accountAttributeId;
            }
            parameterFacility.updateParameterObject(productCd, productCredit);
        }
    }
    
    @ResponseBody
    @RequestMapping("/saveOrUpdateCustomerServiceFee")
    public void saveOrUpdateCustomerServiceFee(@RequestBody Map<String, CustomerServiceFee> cstServiceFee,
        @RequestBody String productCd) throws ProcessException
    {
        ProductCredit productCredit = parameterFacility.getParameterObject(productCd, ProductCredit.class);
        if (productCredit == null)
        {
            productCredit = new ProductCredit();
            productCredit.customerServiceFee = cstServiceFee;
            parameterFacility.addNewParameter(productCd, productCredit);
        }
        else
        {
            productCredit.customerServiceFee = cstServiceFee;
            parameterFacility.updateParameterObject(productCd, productCredit);
        }
    }
    
    @ResponseBody
    @RequestMapping("/saveOrUpdateLoanPlans")
    public void saveOrUpdateLoanPlans(@RequestBody Map<LoanType, String> loanPlans, @RequestBody String productCd)
        throws ProcessException
    {
        ProductCredit productCredit = parameterFacility.getParameterObject(productCd, ProductCredit.class);
        if (productCredit == null)
        {
            productCredit = new ProductCredit();
            productCredit.loanPlansMap = loanPlans;
            parameterFacility.addNewParameter(productCd, productCredit);
        }
        else
        {
            productCredit.loanPlansMap = loanPlans;
            parameterFacility.updateParameterObject(productCd, productCredit);
        }
    }
    
    // @Override
    // public void saveOrUpdateTxnFeeList(Map<String, TxnFee[]> txnFeeList,
    // String productCd) throws ProcessException{
    //
    // Set<Entry<String, TxnFee[]>> set = txnFeeList.entrySet();
    //
    // for(Iterator<Entry<String, TxnFee[]>> it = set.iterator() ; it.hasNext()
    // ; ){
    //
    // Entry<String, TxnFee[]> entry = it.next();
    // TxnFee[] txnFees = entry.getValue();
    // for(TxnFee txnFee : txnFees){
    //
    // // ServerUtil.validateRateCeil(txnFee.rateCeils);
    // }
    // }
    // ProductCredit productCredit =
    // parameterFacility.getParameterObject(ProductCredit.class, productCd);
    // if (productCredit == null) {
    // try {
    // productCredit = new ProductCredit();
    // productCredit.txnFeeList = txnFeeList;
    // parameterFacility.addNewParameter(productCd, productCredit);
    // } catch (ProcessException e) {
    // e.printStackTrace();
    // }
    // } else {
    //
    // productCredit.txnFeeList = txnFeeList;
    // parameterFacility.updateParameterObject(ProductCredit.class, productCd,
    // productCredit);
    // }
    //
    // }
    @ResponseBody
    @RequestMapping("/getPlanTmps")
    public List<PlanTemplate> getPlanTmps()
    {
        return parameterFacility.getParameterObject(PlanTemplate.class);
    }
    
    @ResponseBody
    @RequestMapping("/saveOrUpdatePlanNbrList")
    public void saveOrUpdatePlanNbrList(Map<PlanType, String> planNbList, String productCd) throws ProcessException
    {
        ProductCredit productCredit = parameterFacility.getParameterObject(productCd, ProductCredit.class);
        if (productCredit == null)
        {
            productCredit = new ProductCredit();
            productCredit.planNbrList = planNbList;
            parameterFacility.addNewParameter(productCd, productCredit);
        }
        else
        {
            productCredit.planNbrList = planNbList;
            parameterFacility.updateParameterObject(productCd, productCredit);
        }
    }
    
    @ResponseBody
    @RequestMapping(value = "/getLoanPlans", method = {RequestMethod.POST})
    public Map<String, List<LoanPlan>> getLoanPlans()
    {
        Map<String, List<LoanPlan>> map = new LinkedHashMap<String, List<LoanPlan>>();
        List<LoanPlan> loanPlanList = parameterFacility.getParameterObject(LoanPlan.class);
        for (LoanPlan plan : loanPlanList)
        {
            LoanType type = plan.loanType;
            List<LoanPlan> list = map.get(type.name());
            if (list == null)
            {
                list = new ArrayList<LoanPlan>();
                list.add(plan);
            }
            else
            {
                list.add(plan);
            }
            map.put(type.name(), list);
        }
//        Map<String, List<LoanPlan>> a = new HashMap<String, List<LoanPlan>>();
        return map;
    }
    
    
    @ResponseBody
    @RequestMapping(value = "/getFinanceOrgList", method = {RequestMethod.POST})
    public List<FinancialOrg> getFinanceOrgMap()
    {
	List<FinancialOrg> financialOrgList = parameterFacility.getParameterObject(FinancialOrg.class);
	return financialOrgList;
    }
    
    @ResponseBody
    @RequestMapping(value = "/getMessageValueMaps", method = {RequestMethod.POST})
    public Map<String, LinkedHashMap<String, String>> getMessageValueMaps()
    {
        Map<String, LinkedHashMap<String, String>> result = new HashMap<String, LinkedHashMap<String, String>>();
        
        List<BMPMessageTemplate> list = parameterFacility.getParameterObject(BMPMessageTemplate.class);
        
        for (BMPMessageTemplate mt : list)
        {
            if (!result.containsKey(mt.msgCategory))
            {
                result.put(mt.msgCategory, new LinkedHashMap<String, String>());
            }
            result.get(mt.msgCategory).put(mt.code, mt.desc);
        }
        
        return result;
    }
    
    @ResponseBody
    @RequestMapping("/getLoanMerchant")
    public List<Map<String, Serializable>> getLoanMerchant()
    {
    	List<Map<String, Serializable>> result = new ArrayList<Map<String, Serializable>>();
        List<LoanMerchant> merchantList = parameterFacility.getParameterObject(LoanMerchant.class);
        
        if(merchantList != null && merchantList.size() > 0)
        {
        	for(int i = 0; i < merchantList.size(); i++)
        	{
        		LoanMerchant merchant = merchantList.get(i);
        		
        		Map<String, Serializable> obj = LoanMerchantMapHelper.convertToMap(merchant);
        		
        		result.add(obj);
        	}
        }
        
        return result;
    }
    
    @ResponseBody
    @RequestMapping("/getProgram")
    public List<Program> getProgram()
    {
        return parameterFacility.getParameterObject(Program.class);
    }
}
