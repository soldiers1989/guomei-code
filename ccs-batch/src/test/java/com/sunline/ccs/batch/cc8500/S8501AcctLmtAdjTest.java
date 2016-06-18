package com.sunline.ccs.batch.cc8500;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import junit.framework.Assert;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.sunline.acm.service.sdk.GlobalManagementServiceMock;
import com.sunline.ccs.batch.cc6000.BatchData;
import com.sunline.ccs.batch.cc6000.BatchParameter;
import com.sunline.ccs.batch.front.FrontBatchData;
import com.sunline.ccs.batch.utils.JUnit4ClassRunner;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctCrlmtAdjLog;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctO;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctCrlmtAdjLog;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.pcm.service.sdk.ParameterServiceMock;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.LoanType;

@RunWith(JUnit4ClassRunner.class)
@ContextConfiguration("/cc8500/test-context-cc8500.xml")
public class S8501AcctLmtAdjTest {
	
	@Autowired 
	private JobLauncherTestUtils jobLauncherTestUtils;
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private RCcsAcct rCcsAcct;
	@Autowired
	private RCcsAcctO rCcsAcctO;
	@Autowired
	private RCcsLoan rCcsLoan;
	@Autowired
	private RCcsCustomerCrlmt rTmCustLimitO;
	@Autowired
	private RCcsAcctCrlmtAdjLog rCcsAcctCrlmtAdjLog;
	
	@Autowired
	private GlobalManagementServiceMock managementMock;
	@Resource(name="parameterServiceMock")
	private ParameterServiceMock parameterMock;
	QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;

	@Before
	public void init() throws Exception{
		AccountAttribute aa=BatchParameter.genAccountAttribute();
		parameterMock.putParameter(aa.accountAttributeId+"", aa);
		ProductCredit pc=BatchParameter.genProductCredit();
		pc.productCd = "000001";
		parameterMock.putParameter(pc.productCd, pc);
		managementMock.setupBatchDate(DateUtils.addDays(new Date(), -1),new Date());
	}
	
	@Test
	public void doTest() throws Exception{
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
		CcsAcct acct=BatchData.genAcct();
		CcsAcctO acctO=BatchData.genAcctO();
		CcsCustomerCrlmt custLmt=BatchData.genCcsCustomerCrlmt();
		CcsLoan loan=BatchData.genLoan();
		loan.setLoanExpireDate(sf.parse("20180228"));
		loan.setRemainTerm(8);
		loan.setLoanType(LoanType.MCAT);
		CcsAcct acct2=new CcsAcct();
		FrontBatchData.setNotNullField(CcsAcct.class, acct2);
		acct.setOrg("000000000001");
		acct.setProductCd("000001");
		acct.setCurrBal(BigDecimal.valueOf(2000));
		acct2.setAcctNbr(1101501L);
		acct2.setAcctType(AccountType.E);
		acct2.setContrNbr("300116110101101500");
		acct2.setCtdAdjPoints(BigDecimal.valueOf(0));
		acct2.setName("list");
		acct2.setCreditLmt(BigDecimal.valueOf(13000));
		acct2.setCustLmtId(1L);
		acct.setAcctExpireDate(new Date());
		acct2.setAcctExpireDate(new Date());
		acct2.setAgeCode("0");
		acct.setAgeCode("0");

		CcsAcctO acctO2=new CcsAcctO();
		FrontBatchData.setNotNullField(CcsAcctO.class, acctO2);
		acctO2.setAcctNbr(1101501L);
		acctO2.setAcctType(AccountType.E);
		acctO2.setContrNbr("300116110101101500");
		acctO2.setCreditLmt(BigDecimal.valueOf(13000));
		acctO2.setCustLmtId(1L);
		rCcsAcct.save(acct2);
		rCcsAcct.flush();
		rCcsAcctO.save(acctO2);
		rCcsAcctO.flush();
		
		rCcsAcct.save(acct);
		rCcsAcct.flush();
		rCcsAcct.save(acct2);
		rCcsAcct.flush();
		rCcsAcctO.save(acctO2);
		rCcsAcctO.flush();
		rCcsAcctO.save(acctO);
		rCcsAcctO.flush();
		rCcsLoan.save(loan);
		rCcsLoan.flush();
		rTmCustLimitO.save(custLmt);
		rTmCustLimitO.flush();
		
		CcsLoan ccsLoan = rCcsLoan.findOne(qCcsLoan.contrNbr.eq("300116110101101500").and(qCcsLoan.org.eq("000000000001")));
		System.out.println(ccsLoan.getLoanExpireDate());
		
		JobExecution jobExecution = jobLauncherTestUtils.launchStep("cc8501");
		
		List<CcsAcct> acctList = rCcsAcct.findAll();
		List<CcsLoan> loanList = rCcsLoan.findAll();
		List<CcsAcctCrlmtAdjLog> logList = rCcsAcctCrlmtAdjLog.findAll();
		
		for(int i=0;i<acctList.size();i++){
			System.out.println("------------------------");
			System.out.println(acctList.get(i).getAcctExpireDate());
			System.out.println(acctList.get(i).getCreditLmt());
			System.out.println(loanList.get(i).getLoanExpireDate());
			System.out.println(loanList.get(i).getRemainTerm());
			System.out.println(logList.get(i).getCreditLmtNew());
		}
		Assert.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
	}
	
	public static void main(String args[]){
		new BigDecimal("1999.88");
	}

}
