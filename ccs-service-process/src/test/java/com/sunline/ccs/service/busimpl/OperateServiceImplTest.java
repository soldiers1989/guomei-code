package com.sunline.ccs.service.busimpl;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.infrastructure.server.repos.RCcsCssfeeReg;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsCssfeeReg;
import com.sunline.ccs.infrastructure.shared.model.CcsCssfeeReg;
import com.sunline.ccs.service.handler.OperateServiceImpl;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-service.xml")
@Transactional
@TransactionConfiguration(defaultRollback = false)

public class OperateServiceImplTest {
	
	@Autowired
	OperateServiceImpl operateServiceImpl;
	
	@Autowired
	RCcsCustomer rCcsCustomer;
	
	@Autowired
	RCcsCssfeeReg rTmCssFeeReg;

	@Test
	public void testS11010() {
//		JPAQuery query = new JPAQuery(em);
		
//		query.
		
//		CcsCustomer ccsCustomer = new CcsCustomer();
		
		CcsCssfeeReg tmcssfeereg = new CcsCssfeeReg();
		
		tmcssfeereg.setCardNbr("61231231231");
//		tmcssfeereg.setCssfeeTxnSeq(1);
		tmcssfeereg.setOrg("00000001");
//		tmcssfeereg.setRegId(232323);
		tmcssfeereg.setRequestTime(new Date());
		tmcssfeereg.setServiceNbr("11040");
		tmcssfeereg.setTxnDate(new Date());
		rTmCssFeeReg.save(tmcssfeereg);

		
		CcsCssfeeReg tmcssfeereglist = rTmCssFeeReg.findOne(QCcsCssfeeReg.ccsCssfeeReg.cardNbr.eq("61231231231"));
		
		tmcssfeereglist.setOrg("00000002");
		
		
		
//		S11010Req req = new S11010Req();
//		operateServiceImpl.S11010(req);
	}
	
	@Test
	public void testS16040(){
		
	}
	

}
