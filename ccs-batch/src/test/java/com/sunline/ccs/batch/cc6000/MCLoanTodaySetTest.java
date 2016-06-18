package com.sunline.ccs.batch.cc6000;

import java.text.SimpleDateFormat;
import java.util.Date;
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
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.loan.McLoanProvideImpl;
import com.sunline.ccs.loan.TrialResp;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.pcm.service.sdk.ParameterServiceMock;
import com.sunline.ppy.dictionary.enums.LoanUsage;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-context-front.xml")
@Transactional
public class MCLoanTodaySetTest {
	
	private static final Logger logger = LoggerFactory.getLogger(MCLoanTodaySetTest.class);
	
	
	@Autowired
	private ParameterServiceMock parameterMock;
	@Autowired
	private McLoanProvideImpl mcLoanProvideImpl;
	@PersistenceContext
	private EntityManager em;
    private final String systemType = "CCS";

    @Value("#{env.instanceName}")
    private String instanceName;
    @Autowired
    private GlobalManagementService globalManagementService;
	
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
		
		List<String> orgs = globalManagementService.getServeOrg(systemType, instanceName);
		TrialResp trialResp = new TrialResp();
		
		CcsLoan loan = BatchData.genLoan();
		List<CcsPlan>  plans = BatchData.genPlans();
		
		List<CcsRepaySchedule> rss = BatchData.genSchedule();
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		Date batDate = sf.parse("2015-07-04");
		for(CcsRepaySchedule rs :rss){
			logger.info("schdule :"+rs.getCurrTerm()+"  "+sf.format(rs.getLoanPmtDueDate()));
			em.persist(rs);
		}
		logger.info("数据准备完毕-------------------------------");
		trialResp = mcLoanProvideImpl.mCLoanTodaySettlement(loan,null, batDate, LoanUsage.M, trialResp, plans,null);
		
		logger.info("数据校验----------------------------------");
		
		logger.info("金额"+trialResp.getTotalAMT());
		logger.info("本金"+trialResp.getCtdPricinpalAMT());
		logger.info("本金金额"+trialResp.getCtdPricinpalAMT().add(trialResp.getPastPricinpalAMT()));
		logger.info("利息金额"+trialResp.getCtdInterestAMT().add(trialResp.getPastInterestAMT()));
		logger.info("保费金额"+trialResp.getCtdInsuranceAMT().add(trialResp.getPastInsuranceAMT()));
		logger.info("罚金金额"+trialResp.getCtdMulctAMT().add(trialResp.getPastMulctAMT()));
		logger.info("印花税金额"+trialResp.getCtdStampdutyAMT().add(trialResp.getPastStampdutyAMT()));
		logger.info("寿险计划包金额"+trialResp.getCtdLifeInsuFeeAMT().add(trialResp.getPastLifeInsuFeeAMT()));
		logger.info("手续费金额"+trialResp.getCtdInitFee().add(trialResp.getPastInitFee()));
		

		
	}
	
}
