package com.sunline.ccs.batch.cc6000;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ccs.infrastructure.server.repos.RCcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.loan.McLoanProvideImpl;
import com.sunline.ccs.loan.TrialResp;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.pcm.service.sdk.ParameterServiceMock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-context-front.xml")
@Transactional
public class OracleSaveTest {
	
	private static final Logger logger = LoggerFactory.getLogger(OracleSaveTest.class);
	
	
	@Autowired
	private ParameterServiceMock parameterMock;
	@Autowired
	private McLoanProvideImpl mcLoanProvideImpl;
	@PersistenceContext
	private EntityManager em;
    private final String systemType = "CCS";
	@Autowired
	private RCcsPlan rCcsPlan;

	
	@Before
	public void setup(){
		// 参数
//		LoanFeeDef df  = BatchParameter.genLoanFeeDef();
		LoanPlan lp = BatchParameter.genLoanPlan();
		parameterMock.putParameter(lp.loanCode, lp);
	}
	
	@Test
	public void testCase() throws Exception{
		logger.info("开始测试----------------------------------");
		
		
		List<CcsPlan>  plans = BatchData.genPlans();
		CcsPlan plan1 = rCcsPlan.findOne(150l);
		System.out.println("PlanId :  "+plan1.getPlanId());
		
		for(CcsPlan plan : plans){
			rCcsPlan.save(plan);
			System.out.println("PlanId :  "+plan.getPlanId());
		}
		for(CcsPlan plan : plans){
			em.persist(plan);
			System.out.println("PlanId :  "+plan.getPlanId());
		}
		

		
	}
	
}
