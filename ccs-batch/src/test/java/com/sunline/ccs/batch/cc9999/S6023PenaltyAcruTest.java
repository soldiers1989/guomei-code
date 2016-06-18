package com.sunline.ccs.batch.cc9999;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ccs.batch.cc6000.P6008PenaltyAcruFillUp;
import com.sunline.ccs.batch.cc6000.P6009MCATCoupon;
import com.sunline.ccs.batch.cc6000.P6022InterestAcruToday;
import com.sunline.ccs.batch.cc6000.P6023PenaltyAccuPerDay;
import com.sunline.ccs.batch.cc6000.P6026MCEICoupon;
import com.sunline.ccs.batch.cc6000.S6000AcctInfo;
import com.sunline.ccs.facility.tools.ObjectCompareCommon;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.pcm.service.sdk.ParameterServiceMock;
import com.sunline.ppy.dictionary.enums.LoanUsage;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/cc9999/test-context-9999.xml")
@Transactional
public class S6023PenaltyAcruTest {

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;
	@PersistenceContext
	private EntityManager em;
	static SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
	@Autowired
	private P6023PenaltyAccuPerDay p6023;
	@Autowired
	private P6008PenaltyAcruFillUp p6008;
	@Autowired
	private P6022InterestAcruToday p6022;
	@Autowired
	private ParameterServiceMock paramMock;
	@Autowired
	private P6026MCEICoupon p6026;
	@Autowired
	private P6009MCATCoupon p6009;
    @Autowired
    private GlobalManagementService globalManagementService;
	@Before
	public void setup() throws ParseException {
		globalManagementService.getSystemStatus().setProcessDate(new Date());
		globalManagementService.getSystemStatus().setLastProcessDate(DateUtils.addDays(new Date(), -1));
		L6000ParamMockCommon mock = new L6000ParamMockCommon();
		mock.putParams(paramMock);
	}

	@Test
	public void testCase() throws Exception {
		L6000AcctInfoCommonItem s = new L6000AcctInfoCommonItem();
//		S6000AcctInfo item = s.getItemMCEI();
		S6000AcctInfo item = s.getItemMCAT();
		CcsOrder order = new CcsOrder();
		order.setLoanUsage(LoanUsage.Q);
		order.setTxnAmt(new BigDecimal(110));
//		order.setTerm(2);
		order.setAcctNbr(item.getAccount().getAcctNbr());
		order.setAcctType(item.getAccount().getAcctType());
		em.persist(order);
		S6000AcctInfo preItem = item;
//		p6022.process(item);
//		p6023.process(item);
		p6009.process(item);
//		p6008.process(item);
		
		ObjectCompareCommon.compare(preItem.getPlans().get(0),item.getPlans().get(0));
	}
	
	public static void p(Object o){
		System.out.println(o);
	}
}
