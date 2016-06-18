package com.sunline.ccs.service.auth.test.verifier;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.service.sdk.ParameterServiceMock;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.ccs.service.auth.frame.AuthServiceImpl;
import com.sunline.ccs.service.auth.frame.Handler;
import com.sunline.ark.support.service.YakMessage;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-service.xml")
@Transactional
public class StatusTest
{
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private AuthServiceImpl authDecisionServiceImpl;
	
	@Resource
	private Handler reasonCodeProcessor;
 
	
	@Autowired
	private ParameterServiceMock mock;

	@Before
	public void setupData()
	{
		//初始化参数
//		
//		CcsCustomer cust = new CcsCustomer();
//		cust.setBirthday(new Date());
//		em.persist(em);
		
		mock.putParameter("asdf", new Product());
	}


//	@Test
//	public void Test1()
//	{
//		YakMessage msg = new YakMessage();
//		msg.getHeadAttributes().put(11, "100");
//		msg.getBodyAttributes().put(3, "33");
//		
//		authDecisionServiceImpl.authorize(msg);
//		
//		msg.getHeadAttributes().put(11, "200");
//		msg.getBodyAttributes().put(3, "30");
//		
//		authDecisionServiceImpl.authorize(msg);
//		
//	}
	
	@Test
	public void Test2()
	{
		// ****************** YakMessage ******************
		YakMessage msg = new YakMessage();
		msg.getBodyAttributes().put(2, "234567890123456789");
		msg.getBodyAttributes().put(3, "00");
		msg.getBodyAttributes().put(4, "0");
		msg.getHeadAttributes().put(11, "100");
		msg.getBodyAttributes().put(14, "456");
		msg.getBodyAttributes().put(18, "6011");
		msg.getBodyAttributes().put(22, "07");
		msg.getBodyAttributes().put(23, "");
		msg.getBodyAttributes().put(35, "1234567890123456789012345678901234567890");
		msg.getBodyAttributes().put(48, "ASAA0101234567890");
		msg.getBodyAttributes().put(52, "0");
		msg.getBodyAttributes().put(60, "1234567555555901234567890");
		

		
		
		AuthContext context = new AuthContext();
		CcsAcctO account = new CcsAcctO();
		account.setBlockCode("1");
	//	context.setAccount(account);
 //		cupCrossVerifier.process(msg, context);
	}

}
