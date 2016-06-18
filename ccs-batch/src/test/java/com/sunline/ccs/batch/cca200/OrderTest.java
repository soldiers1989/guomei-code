package com.sunline.ccs.batch.cca200;

import java.math.BigDecimal;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.DebugTools;
import com.sunline.ccs.batch.cca000.SA000ParamDataUtil;
import com.sunline.ccs.batch.front.FrontBatchUtil;
import com.sunline.ccs.batch.sdk.BatchDateUtil;
import com.sunline.ccs.batch.tools.MakeData;
import com.sunline.ccs.batch.utils.JUnit4ClassRunner;
import com.sunline.ccs.batch.utils.MakeDataExt;
import com.sunline.ccs.infrastructure.server.repos.RCcsCard;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardLmMapping;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.pcm.param.def.FinancialOrg;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.LoanUsage;

/**
 * @author lin
 */
@RunWith(JUnit4ClassRunner.class)
@ContextConfiguration("/cca000/test-context-cca.xml")
@TransactionConfiguration(defaultRollback=false)
public class OrderTest {
	Logger logger = LoggerFactory.getLogger(this.getClass());
//	@Autowired 
//	private JobLauncherTestUtils jobLauncherTestUtils;
	@Autowired
	private BatchDateUtil batchDateUtil;
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	@Autowired
	private FrontBatchUtil frontUtil;
	@PersistenceContext
	private EntityManager em;
	@Autowired
	SA000ParamDataUtil paramUtil;
	private QCcsAcct qacct = QCcsAcct.ccsAcct;
	private QCcsLoan qloan = QCcsLoan.ccsLoan;
	private QCcsCustomer qcust = QCcsCustomer.ccsCustomer;
	@Autowired
	private RCcsLoanReg rreg ;
	@Autowired
	private RCcsCard rcard;
	@Autowired
	private RCcsCardLmMapping rcm;
	
	@Before
	public void init(){
		batchDateUtil.setBatchDate("20160802");
		paramUtil.loadParamFromDir();
	}
	
//	@Test
	public void orderSet() throws IllegalAccessException{
		CcsOrder o = new CcsOrder();
		MakeData.setDefaultValue(o);
		
	}
	
	
//	@Test
	public void saTest(){
		
		CcsAcct a = new JPAQuery(em).from(qacct).where(qacct.acctNbr.eq(1112766L)).singleResult(qacct);
		CcsLoan l = new JPAQuery(em).from(qloan).where(qloan.acctNbr.eq(a.getAcctNbr())).singleResult(qloan);
		CcsCustomer c = new JPAQuery(em).from(qcust).where(qcust.custId.eq(a.getCustId())).singleResult(qcust);
		FinancialOrg finanicalOrg = unifiedParameterFacility.loadParameter("001", FinancialOrg.class);
		CcsOrder o = frontUtil.initOrder(a, c, l, LoanUsage.C, new BigDecimal("1000.0"), null );
		DebugTools.printObj(logger, o, "order");
	}
//	@Test
	public void genReg() throws Exception{
		CcsLoanReg r = new CcsLoanReg();
		MakeDataExt.setDefaultValue(r);
		rreg.save(r);
	}
	@Test
	@Rollback(value=false)
	public void genCard() throws Exception{
		CcsAcct a = new JPAQuery(em).from(qacct).where(qacct.acctNbr.eq(1120205L)).singleResult(qacct);
		CcsCustomer cust = new JPAQuery(em).from(qcust).where(qcust.custId.eq(a.getCustId())).singleResult(qcust);
		/*CcsCard c = new CcsCard();
		MakeDataExt.setDefaultValue(c);
		c.setAcctNbr(a.getAcctNbr());
		c.setLogicCardNbr(a.getDefaultLogicCardNbr());
		c.setCustId(cust.getCustId());
		c.setCardBasicNbr(a.getDefaultLogicCardNbr());
		c.setProductCd(a.getProductCd());
		rcard.save(c);*/
		CcsCardLmMapping cm = new CcsCardLmMapping();
		cm.setLogicCardNbr(a.getDefaultLogicCardNbr());
		cm.setCardNbr(a.getDefaultLogicCardNbr());
		cm.setOrg("000000000001");
		rcm.save(cm);
	}
	
}
