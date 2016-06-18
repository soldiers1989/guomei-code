package com.sunline.ccs.service.auth.test;

import org.slf4j.Logger;

import com.sunline.ppy.api.MediumInfo;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.param.def.AuthProduct;
import com.sunline.ccs.param.def.MerchantTxnCrtl;
import com.sunline.ccs.service.auth.context.CupMsg;
import com.sunline.ccs.service.auth.context.LoanInfo;
import com.sunline.ccs.service.auth.context.TxnInfo;

/** 
 * 规则接口 
* @author fanghj
 */  
public interface RuleEngine {  
      
    /** 
     * 初始化规则引擎 
     */  
    public void initEngine();  
      
    /** 
     * 刷新规则引擎中的规则 
     */  
    public void refreshEnginRule();  
      
    /** 
     * 执行规则引擎 
     * @param authPr 
     * @param crtl 
     * @param logger 
     * @param mi 
     * @param pointDomain 积分Fact 
     */  
    public void executeRuleEngine(final LoanInfo loanInfo,
    		final TxnInfo txnInfo,
    		final CcsAcctO ccsAcctO,
    		final CcsCardO tmCardO,
    		final CupMsg cupMessage, Logger logger, MerchantTxnCrtl crtl, AuthProduct authPr, MediumInfo mi);  
}  
