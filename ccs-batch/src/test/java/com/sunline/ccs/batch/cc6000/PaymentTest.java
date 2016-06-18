package com.sunline.ccs.batch.cc6000;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.batch.cc6000.common.PaymentHier;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.AgePmtHierInd;
import com.sunline.ccs.param.def.enums.BucketObject;
import com.sunline.pcm.service.sdk.ParameterServiceMock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-context-front.xml")
@Transactional
public class PaymentTest {
	
	private static final Logger logger = LoggerFactory.getLogger(PaymentTest.class);
	
	
	@Autowired
	private ParameterServiceMock parameterMock;
	@Autowired
	private PaymentHier paymentHier;
	@PersistenceContext
	private EntityManager em;
	static SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
	
	@Before
	public void setup(){
		// 参数
//		LoanFeeDef df  = BatchParameter.genLoanFeeDef();
		LoanPlan lp = BatchParameter.genLoanPlan();
		parameterMock.putParameter(lp.loanCode, lp);
		
		ProductCredit pd = BatchParameter.genProductCredit();
		parameterMock.putParameter(pd.productCd, pd);
		
		PlanTemplate pt = BatchParameter.genPlanTemplate();
		parameterMock.putParameter(pt.planNbr, pt);
		
	}
	
	@Test
	public void testCase() throws Exception{
		logger.info("开始测试----------------------------------");
		
		List<CcsPlan>  plans = BatchData.genPlans();
		List<BucketObject> bnps= BatchData.genBucketObjectList();
		BigDecimal assignBal = new BigDecimal(2135);
		AgePmtHierInd agePmtInd = AgePmtHierInd.I;
		logger.info("数据准备完毕-------------------------------");
		
//		BigDecimal bal = paymentHier.payAssign(plans, bnps, assignBal,sf.parse("2015-06-10"), agePmtInd, null, "");
		
		logger.info("数据校验----------------------------------");
		
//		logger.info("金额"+bal);
//		for (CcsPlan p : plans){
//			logger.info("信用计划："+p.getPlanId());
//			logger.info("当期保费："+p.getCtdInsurance());
//			logger.info("往期保费："+p.getPastInsurance());
//			logger.info("当期罚金："+p.getCtdMulctAmt());
//			logger.info("往期罚金："+p.getPastMulctAmt());
//			logger.info("当期利息："+p.getCtdInterest());
//			logger.info("往期利息："+p.getPastInterest());
//			logger.info("当期本金："+p.getCtdPrincipal());
//			logger.info("往期本金："+p.getPastPrincipal());
//			logger.info("当期印花税："+p.getCtdStampdutyAmt());
//			logger.info("往期印花税："+p.getPastStampdutyAmt());
//			logger.info("当期增值费："+p.getCtdAddedvaluetaxAmt());
//			logger.info("往期增值费："+p.getPastAddedvaluetaxAmt());
//		}

		
	}
	
}
