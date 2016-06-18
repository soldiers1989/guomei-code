package com.sunline.ccs.batch.cca303;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.sunline.acm.service.sdk.GlobalManagementServiceMock;
import com.sunline.ccs.batch.cc6000.BatchParameter;
import com.sunline.ccs.batch.utils.JUnit4ClassRunner;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctCrlmtAdjLog;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctO;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.pcm.service.sdk.ParameterServiceMock;

@RunWith(JUnit4ClassRunner.class)
@ContextConfiguration("/test-a303-context.xml")
public class SA303PrepayMsgTest {
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
	@Autowired
	private ParameterServiceMock parameterMock;
	QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;
	@Before
	public void init() throws ParseException{
		AccountAttribute aa=BatchParameter.genAccountAttribute();
		parameterMock.putParameter(aa.accountAttributeId+"", aa);
		ProductCredit pc=BatchParameter.genProductCredit();
		ProductCredit pc2=BatchParameter.genProductCredit();
		parameterMock.putParameter(pc.productCd, pc);
		parameterMock.putParameter(pc2.productCd,pc2);
		parameterMock.putParameter("001101",pc2);
		managementMock.setupBatchDate(new SimpleDateFormat("yyyyMMdd").parse("20160223"),new SimpleDateFormat("yyyyMMdd").parse("20160212"));
	}
	@Test
	public void run() throws Exception{
		 
	}
}
