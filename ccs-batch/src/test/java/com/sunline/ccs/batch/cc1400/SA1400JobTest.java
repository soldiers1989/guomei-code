package com.sunline.ccs.batch.cc1400;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.sunline.ccs.batch.cca000.SA000ParamDataUtil;
import com.sunline.ccs.batch.sdk.BatchDateUtil;
import com.sunline.ccs.batch.utils.JUnit4ClassRunner;
import com.sunline.ccs.infrastructure.server.repos.RCcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrderHst;
import com.sunline.ppy.dictionary.enums.Indicator;
@RunWith(JUnit4ClassRunner.class)
@ContextConfiguration("/test-context-cc1400.xml")
public class SA1400JobTest {
	private Logger log = LoggerFactory.getLogger(getClass());
	@PersistenceContext
	private EntityManager em;
	@Autowired 
	private JobLauncherTestUtils jobLauncherTestUtils;
	@Autowired
	private BatchDateUtil batchDateUtil;
	@Autowired
	private RCcsOrder r;
	@Autowired
	private SA000ParamDataUtil param;
	
	private QCcsOrderHst qos = QCcsOrderHst.ccsOrderHst;
	private QCcsOrder qo = QCcsOrder.ccsOrder;
	
	@Before
	public void init(){
		batchDateUtil.setBatchDate("20170708");
//		param.loadParamFromDir();
//		prepareData();
	}
	@Test
	public void doTest() throws Exception{
		
		JobParametersBuilder jobParamBuilder = new JobParametersBuilder();
		jobParamBuilder.addString("testId", "1000028");
		jobParamBuilder.addDate("batchDate", batchDateUtil.getBatchDate());
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParamBuilder.toJobParameters());
//		JobExecution jobExecution = jobLauncherTestUtils.launchStep("cc1401");
		
//		List<CcsOrderHst> orderHsts = new JPAQuery(em).from(qos).list(qos);
//		log.debug("orderHst num[{}]",orderHsts.size());
//		for(CcsOrderHst oh : orderHsts){
////			DebugTools.printObj(log, oh, null);
//		}
//		List<CcsOrder> orders = new JPAQuery(em).from(qo).list(qo);
//		log.debug("order num[{}]",orders.size());
//		for(CcsOrder o : orders){
////			DebugTools.printObj(log, o, null);
//		}
		
		Assert.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
	}
	
	/**
	 * 
	 */
	private void prepareData() {
		CcsOrder o1 = new CcsOrder();
		o1.setTxnAmt(new BigDecimal(100).setScale(2));
		o1.setCardNo("111111");
		o1.setMatchInd(Indicator.Y);
		o1.setComparedInd(Indicator.N);
		o1.setErrInd(Indicator.N);
		o1.setCode("00");
		o1.setPriv1("订单001");
		Map<String, Serializable> map = o1.convertToMap();
		o1 = r.save(o1);
//		DebugTools.printObj(log, o1, "order1");
		
		CcsOrder o2 = new CcsOrder();
		o2.updateFromMap(map);
		o2.setTxnAmt(new BigDecimal(200).setScale(2));
		o2.setCardNo("222222");
		o2.setCode("00");
		o2.setPriv1("订单002");
		o2 = r.save(o2);
//		DebugTools.printObj(log, o2, "order2");
		
		CcsOrder o3 = new CcsOrder();
		o3.updateFromMap(map);
		o3.setTxnAmt(new BigDecimal(200).setScale(2));
		o3.setCardNo("333333111");
		o3.setCode("00");
		o3.setPriv1("订单003");
//		DebugTools.printObj(log, o3, "order3");
		o3 = r.save(o3);
		
		CcsOrder o4 = new CcsOrder();
		o4.updateFromMap(map);
		o4.setTxnAmt(new BigDecimal(200).setScale(2));
		o4.setCardNo("444444");
		o4.setCode("00");
		o4.setPriv1("订单004");
		o4 = r.save(o4);
//		DebugTools.printObj(log, o4, "order4");
	}
	
	

}
