package com.sunline.ccs.batch.cc1000;

import java.io.FileNotFoundException;

import org.drools.KnowledgeBase;
import org.drools.runtime.StatelessKnowledgeSession;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.sunline.ccs.batch.cc1000.U1001RuleObject;
import com.sunline.ppy.dictionary.enums.AcctTypeGroup;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.exchange.TpsTranFlow;

public class DroolsTest {
	
	/**
	 * drools测试类
	 * 
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {
		
		ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext("/drools-context.xml");
		
		DroolsTest serviceDaemon = new DroolsTest();
		
		serviceDaemon.txncode(ctx); // 交易码获取规则
		
	}
	
	/**
	 * test for txncode.xls
	 * @param ctx
	 */
	public void txncode(ConfigurableApplicationContext ctx) {
		
		KnowledgeBase fileRules = (KnowledgeBase)ctx.getBean("fileRules");
		
		U1001RuleObject ro = new U1001RuleObject();
		
		TpsTranFlow transFlow = new TpsTranFlow();
		transFlow.mti = "9900";
		transFlow.processingCode="900000";
		transFlow.conditionCode="00";
		transFlow.merchCategoryCode="6010";
		transFlow.srcChannel="SUNS";
		
		transFlow.dbCrInd=null;
		transFlow.inputTxnCode=null;
		transFlow.feeProfit="1.1";
		transFlow.inputSource="SUNS";
		transFlow.orgId="123456789012";
		transFlow.settAmt="100";
		transFlow.settCurrencyCode="156";
		transFlow.tranAmt="100";
		transFlow.txnDateTime="131011000000";
		transFlow.txnFeeAmt="2.2";
		transFlow.txnCurrencyCode="156";
		transFlow.origTxnMess = null;
		
		
		ro.setTpsTranFlow(transFlow);
		ro.setLoanReturn(false);
		ro.setAcctTypeGroup(AcctTypeGroup.L);
		ro.setLoanType(LoanType.MCEP);
		StatelessKnowledgeSession sks = fileRules.newStatelessKnowledgeSession();
		//execute
		sks.setGlobal("ruleObject", ro);
		sks.execute(ro);
		
		//print
		System.out.println("****finish****");
		System.out.println("ro.getDcFlag() = "+ro.getDcFlag());
		System.out.println("ro.getTxnCode() = "+ ro.getTxnCode());
	}
	
	
}

