package com.sunline.ccs.batch.cc6000;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
import org.springframework.util.Log4jConfigurer;

import com.sunline.acm.service.sdk.GlobalManagementServiceMock;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.DateUtils;
import com.sunline.ccs.batch.cc3000.loan.LoanMCEI;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.service.sdk.ParameterServiceMock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/test-context-front.xml"})
@Transactional
public class CreateLoanTest {
	
	private static final Logger logger = LoggerFactory.getLogger(CreateLoanTest.class);
	
	
	@Autowired
	private ParameterServiceMock parameterMock;
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private LoanMCEI loanMCEI;
	@Autowired
	private GlobalManagementServiceMock managementMock;

	
	@Before
	public void setup() throws ParseException{
		
		try {  
            Log4jConfigurer.initLogging("classpath:log4j.properties");  
        } catch (FileNotFoundException ex) {  
            System.err.println("Cannot Initialize log4j");  
        }  
		
		// 参数
//		LoanFeeDef df  = BatchParameter.genLoanFeeDef();
		
		ProductCredit pd = BatchParameter.genProductCredit();
		parameterMock.putParameter(pd.productCd, pd);
		
		PlanTemplate pt = BatchParameter.genPlanTemplate();
		parameterMock.putParameter(pt.planNbr, pt);
		
		LoanPlan lp = BatchParameter.genLoanPlan();
		parameterMock.putParameter(lp.loanCode, lp);
		OrganizationContextHolder.setCurrentOrg("000000000001");
		
		AccountAttribute aat = BatchParameter.genAccountAttribute();
		parameterMock.putParameter(aat.accountAttributeId.toString(), aat);
		
		Product pdt = BatchParameter.genProduct();
		parameterMock.putParameter(pdt.productCode, pdt);

		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = sf.parse("2015-05-01");
		
		managementMock.setupBatchDate(date, DateUtils.addDays(new Date(), -1));
		
	}
	
	@Test
	public void testCase() throws Exception{
		logger.info("开始测试----------------------------------");
		
		
		CcsLoanReg loanReg = BatchData.genLoanReg();
		CcsAcct acct = BatchData.genAcct();
		em.persist(acct);
		
		// 批量日期
//		managementMock.setupBusinessDate(new Date());
		
		logger.info("数据准备完毕-------------------------------");
		
		loanMCEI.add(loanReg);
		
		logger.info("数据校验----------------------------------");
		
//		QCcsLoan q = QCcsLoan.ccsLoan;
//		List<CcsLoan> loans = new JPAQuery(em).from(q).list(q);
		
		
//		QCcsRepaySchedule rsl = QCcsRepaySchedule.ccsRepaySchedule;
//		
//		List<CcsRepaySchedule> rss = new JPAQuery(em).from(rsl).orderBy(rsl.scheduleId.asc()).list(rsl);
//		
//		for(CcsRepaySchedule rs :rss){
//			logger.info("schdule :"+rs.getCurrTerm()+"本金  "+rs.getLoanInitPrin()+"保费  "+rs.getLoanInsuranceAmt()+"到期还款日  "+rs.getLoanPmtDueDate()
//					+"利息  "+rs.getLoanTermInt()+"印花税  "+rs.getLoanStampdutyAmt()+"增值服务费  "+rs.getLoanAddedvaluetaxAmt());
//		}

		
	}
	
}
