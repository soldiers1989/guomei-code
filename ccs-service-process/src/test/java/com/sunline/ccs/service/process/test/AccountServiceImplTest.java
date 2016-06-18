package com.sunline.ccs.service.process.test; 

import static org.junit.Assert.fail;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.service.QueryRequest;
import com.sunline.ark.support.service.QueryResult;
import com.sunline.ccs.infrastructure.server.repos.RCcsStmtReprintReg;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAddress;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsStatement;
import com.sunline.ccs.infrastructure.shared.model.CcsStmtReprintReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.QCcsStmtReprintReg;
import com.sunline.ccs.param.def.enums.StatementFlag;
import com.sunline.ccs.service.process.AccountServiceImpl;
import com.sunline.ccs.service.process.test.util.CPSConstantsTest;
import com.sunline.ccs.service.process.test.util.DataFixtures;
import com.sunline.ccs.service.util.BlockCodeUtil;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.DdIndicator;
import com.sunline.ppy.dictionary.exception.ProcessException;
/** 
* @author fanghj
* @version 创建时间：2012-7-24 上午9:52:25 
* CPSAccountServiceImpl测试类
*/ 

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-service.xml")
@Transactional
@TransactionConfiguration(defaultRollback = true)

public class AccountServiceImplTest {
	
	
	@Autowired
	private AccountServiceImpl cpsAccountServiceImpl;
	@Autowired
	private RCcsStmtReprintReg rCcsReprintReg;

	
	private String cust_Id = (String) DataFixtures.getColunmValue(0,CcsCustomer.TABLE_NAME, 
			DataFixtures.getColumnName(CcsCustomer.class, CcsCustomer.P_CustId));
	private String org = (String)DataFixtures.getColunmValue(0,CcsCardLmMapping.TABLE_NAME, 
			DataFixtures.getColumnName(CcsCardLmMapping.class, CcsCardLmMapping.P_Org));
	private String card_No = (String)DataFixtures.getColunmValue(0,CcsCardLmMapping.TABLE_NAME, 
			DataFixtures.getColumnName(CcsCardLmMapping.class, CcsCardLmMapping.P_CardNbr));
	private AccountType acct_Type = AccountType.valueOf(DataFixtures.getColunmValue(0,CcsAcct.TABLE_NAME, 
			DataFixtures.getColumnName(CcsAcct.class, CcsAcct.P_AcctType)).toString());
	private String id_No = DataFixtures.getColunmValue(0,CcsCustomer.TABLE_NAME, 
			DataFixtures.getColumnName(CcsCustomer.class, CcsCustomer.P_IdNo)).toString();
	private String id_Type = DataFixtures.getColunmValue(0,CcsCustomer.TABLE_NAME, 
			DataFixtures.getColumnName(CcsCustomer.class, CcsCustomer.P_IdType)).toString();	
	
	private String currencyCd = "156";
	
	@PersistenceContext
	private EntityManager em;
	
	/**
	 * 
	 */
	@Before
	public void setupDatabase() {	
		
		OrganizationContextHolder.setCurrentOrg(org.trim());
		OrganizationContextHolder.setUsername("dfd");
	}

	/**
	 * 
	 */
	@Test
	public void testNF2101() {
		try {
			List<Map<String, Serializable>> list = cpsAccountServiceImpl.NF2101(card_No);
			Assert.assertNotNull(list);
			Assert.assertEquals(list.iterator().next().get(CcsCard.P_AcctNbr).toString(), 
					DataFixtures.getColunmValue(0,CcsCard.TABLE_NAME, DataFixtures.getColumnName(CcsCard.class, CcsCard.P_AcctNbr).toString()));
			
		} catch (ProcessException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * 
	 */
	@Test
	public void testNF2102() {
		try {
			List<Map<String,Serializable>>NF2012 = cpsAccountServiceImpl.NF2102(id_Type, id_No);
			Assert.assertNotNull(NF2012);
			Assert.assertEquals(NF2012.iterator().next().get(CcsCustomer.P_CustId).toString(), cust_Id);
			
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNF2103() {
		try {
			String stmtDate = DataFixtures.getColunmValue(0, CcsStatement.TABLE_NAME, 
					DataFixtures.getColumnName(CcsStatement.class, CcsStatement.P_StmtDate)).toString();//账单日
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date stmtMonth = sdf.parse(stmtDate);
			Map<String, Serializable> nf2103 = cpsAccountServiceImpl.NF2103(card_No, stmtMonth);
			Assert.assertNotNull(nf2103);
			Assert.assertEquals(nf2103.get(CcsStatement.P_AcctNbr).toString(), 
					DataFixtures.getColunmValue(0,CcsStatement.TABLE_NAME, DataFixtures.getColumnName(CcsStatement.class, CcsStatement.P_AcctNbr)));
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNF2104() {
		QueryRequest queryRequest = new QueryRequest();
		QueryResult<Map<String, Serializable>> queryrResult;
		queryRequest.setFirstRow(0);
		queryRequest.setLastRow(2);
		try {
			String stmtDate = DataFixtures.getColunmValue(0, CcsStatement.TABLE_NAME, 
					DataFixtures.getColumnName(CcsStatement.class, CcsStatement.P_StmtDate)).toString();//账单日
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date stmtMonth = sdf.parse(stmtDate);
			queryrResult = cpsAccountServiceImpl.NF2104(queryRequest, card_No, stmtMonth);
			Assert.assertEquals(queryrResult.getResult().iterator().hasNext(), true);
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNF2106() {
		//fail("Not yet implemented");
		try {
			List<Map<String,Serializable>> nf2106 = cpsAccountServiceImpl.NF2106(card_No);
			Assert.assertNotNull(nf2106);
			Assert.assertEquals(nf2106.iterator().next().get(CcsPlan.P_PlanType).toString(), 
					DataFixtures.getColunmValue(0,CcsPlan.TABLE_NAME, DataFixtures.getColumnName(CcsPlan.class, CcsPlan.P_PlanType)).toString());
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNF2202() {
		
		try {
			cpsAccountServiceImpl.NF2202(currencyCd, card_No,CPSConstantsTest.TEMPLIMIT, CPSConstantsTest.STARTDATE, null);
			CcsAcct CcsAcct = this.getCcsAcctToAcctTypeAndCardNo(acct_Type, card_No);
			Assert.assertEquals(CcsAcct.getTempLmt(),new BigDecimal(CPSConstantsTest.TEMPLIMIT));
		} catch (ProcessException e) {			
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNF2203() {
		try {
			cpsAccountServiceImpl.NF2203(currencyCd, card_No, CPSConstantsTest.CASHLIMITRATE);
			CcsAcct CcsAcct =this.getCcsAcctToAcctTypeAndCardNo(acct_Type,card_No);
			Assert.assertEquals(CcsAcct.getCashLmtRate(), CPSConstantsTest.CASHLIMITRATE);
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/**
	 * 
	 */
	@Test
	public void testNF2204() {
		try {
			cpsAccountServiceImpl.NF2204(currencyCd, card_No, CPSConstantsTest.LOANLIMITRATE);
			CcsAcct CcsAcct = this.getCcsAcctToAcctTypeAndCardNo(acct_Type,card_No);
			Assert.assertEquals(CcsAcct.getLoanLmtRate(), CPSConstantsTest.LOANLIMITRATE);
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNF2207() {
		try {
			cpsAccountServiceImpl.NF2207(card_No, null, DdIndicator.C, 
					CPSConstantsTest.NF2207_DDBANKNAME, CPSConstantsTest.NF2207_DDBANKBRANCH, CPSConstantsTest.NF2207_DDACCOUNTNO, CPSConstantsTest.NF2207_DDACCOUNTNAME);
			CcsAcct CcsAcct = this.getCcsAcctToAcctTypeAndCardNo(acct_Type, card_No);
			Assert.assertEquals(CcsAcct.getDdBankAcctName(), CPSConstantsTest.NF2207_DDACCOUNTNAME);
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNF2302() {
		try {
		List<Date> stmtDayList = new ArrayList<Date>();
		String stmtDate = DataFixtures.getColunmValue(0, CcsStatement.TABLE_NAME, 
				DataFixtures.getColumnName(CcsStatement.class, CcsStatement.P_StmtDate)).toString();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		stmtDayList.add(sdf.parse(stmtDate));
		cpsAccountServiceImpl.NF2302(card_No, "",stmtDayList);
		String stmtFlag = DataFixtures.getColunmValue(0, CcsStatement.TABLE_NAME, 
				DataFixtures.getColumnName(CcsStatement.class, CcsStatement.P_StmtFlag)).toString();
				if (!StatementFlag.valueOf(stmtFlag).equals(StatementFlag.O)) {
					QCcsStmtReprintReg qCcsStmtReprintReg = QCcsStmtReprintReg.ccsStmtReprintReg;
					CcsStmtReprintReg tmReprintReg = rCcsReprintReg
							.findOne(qCcsStmtReprintReg.org.eq(OrganizationContextHolder.getCurrentOrg()));
					Assert.assertEquals(tmReprintReg.getStmtDate().toString(), stmtDate);
				}
		
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNF2401() {
		
		try {
			cpsAccountServiceImpl.NF2401(card_No, currencyCd);
			CcsAcct CcsAcct = this.getCcsAcctToAcctTypeAndCardNo(acct_Type, card_No);
			CcsAcctO CcsAcctO = this.getCcsAcctOToAcctTypeAndCardNo(acct_Type, card_No);
			Assert.assertEquals(BlockCodeUtil.hasBlockCode(CcsAcct.getBlockCode(), CPSConstantsTest.BLOCKCODE_T), true);
			Assert.assertEquals(BlockCodeUtil.hasBlockCode(CcsAcctO.getBlockCode(), CPSConstantsTest.BLOCKCODE_T), true);
		} catch (ProcessException e) {
			
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNF2402() {
		try {
			cpsAccountServiceImpl.NF2402(card_No, currencyCd);
			CcsAcct CcsAcct = this.getCcsAcctToAcctTypeAndCardNo(acct_Type, card_No);
			CcsAcctO CcsAcctO = this.getCcsAcctOToAcctTypeAndCardNo(acct_Type, card_No);
			Assert.assertEquals(BlockCodeUtil.hasBlockCode(CcsAcct.getBlockCode(), CPSConstantsTest.BLOCKCODE_T), false);
			Assert.assertEquals(BlockCodeUtil.hasBlockCode(CcsAcctO.getBlockCode(), CPSConstantsTest.BLOCKCODE_T), false);
		} catch (ProcessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNF2404() {
		fail("Not yet implemented");
	}

	/**
	 * 
	 */
	@Test
	public void testNF2107() {
		try {
		QueryRequest queryRequest = new QueryRequest();
		queryRequest.setFirstRow(0);
		queryRequest.setLastRow(2);
		
		String stmtDate = DataFixtures.getColunmValue(0, CcsStatement.TABLE_NAME, 
				DataFixtures.getColumnName(CcsStatement.class, CcsStatement.P_StmtDate)).toString();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date startDate = dateFormat.parse(stmtDate);
		Date endDate = dateFormat.parse(stmtDate);
				
		QueryResult<Map<String,Serializable>> resultNF2107 =  
				cpsAccountServiceImpl.NF2107(queryRequest, card_No, startDate, endDate);
		
		Assert.assertNotNull(resultNF2107);
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNF2201() {
		try {
			cpsAccountServiceImpl.NF2201(card_No, DataFixtures.getColunmValue(0,CcsAddress.TABLE_NAME, 
					DataFixtures.getColumnName(CcsAddress.class,CcsAddress.P_AddrType)).toString());
			CcsAcct CcsAcct = this.getCcsAcctToAcctTypeAndCardNo(acct_Type, card_No);
			Assert.assertEquals(CcsAcct.getStmtMailAddrInd().name(),
				DataFixtures.getColunmValue(0,CcsAddress.TABLE_NAME, 
						DataFixtures.getColumnName(CcsAddress.class,CcsAddress.P_AddrType)).toString());		
			
		} catch (ProcessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNF2205() {
		try {
			cpsAccountServiceImpl.NF2205(currencyCd, card_No, CPSConstantsTest.CREDITLIMIT);
			CcsAcct CcsAcct = this.getCcsAcctToAcctTypeAndCardNo(acct_Type,card_No);
			Assert.assertEquals(CcsAcct.getCreditLmt(), new BigDecimal(CPSConstantsTest.CREDITLIMIT));
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 根据AcctType和CardNo查询CcsAcct表中信息
	 * @param acctType
	 * @param cardNbr
	 * @return CcsAcct
	 * @throws ProcessException 
	 */
	private CcsAcct getCcsAcctToAcctTypeAndCardNo(AccountType acctType,String cardNbr) throws ProcessException {
		QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;
		QCcsCard qCcsCard = QCcsCard.ccsCard;
		QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
		JPAQuery query = new JPAQuery(em);
		CcsAcct CcsAcct = query
				.from(qCcsCardLmMapping, qCcsCard, qCcsAcct)
				.where(qCcsCardLmMapping.logicCardNbr.eq(qCcsCard.logicCardNbr).and(
						qCcsCardLmMapping.org
								.eq(qCcsCard.org)
								.and(qCcsCardLmMapping.cardNbr.eq(cardNbr))
								.and(qCcsCard.acctNbr.eq(qCcsAcct.acctNbr).and(
										qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg()).and(
												qCcsAcct.acctType.eq(acctType)))))).singleResult(qCcsAcct);
		CheckUtil.rejectNull(CcsAcct, "卡号[" + cardNbr + "],账户类型[" + acctType + "]查询不到对应的账户信息");
		return CcsAcct;
	}
	
	/**
	 * 根据AcctType和CardNo查询CcsAcctO表中信息
	 * @param acctType
	 * @param cardNbr
	 * @return CcsAcctO
	 * @throws ProcessException 
	 */
	private CcsAcctO getCcsAcctOToAcctTypeAndCardNo(AccountType acctType,String cardNbr) throws ProcessException{
		QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;
		QCcsCard qCcsCard = QCcsCard.ccsCard;
		QCcsAcctO qCcsAcctO = QCcsAcctO.ccsAcctO;
		JPAQuery query = new JPAQuery(em);
		CcsAcctO CcsAcctO = query.from(qCcsCardLmMapping,qCcsCard,qCcsAcctO).where(
				qCcsCardLmMapping.logicCardNbr.eq(qCcsCard.logicCardNbr)
				.and(qCcsCard.acctNbr.eq(qCcsAcctO.acctNbr)
						.and(qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg())
								.and(qCcsAcctO.acctType.eq(acctType)
										.and(qCcsCardLmMapping.cardNbr.eq(cardNbr)))))).singleResult(qCcsAcctO);
		CheckUtil.rejectNull(CcsAcctO, "卡号[" + cardNbr + "],账户类型[" + acctType + "]查询不到对应的账户信息");
		return CcsAcctO;
	}
	
	/**
	 * 根据卡号获取账户列表，其中账户列表忽略本外币
	 * 
	 * @param cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public List<Long> getAcctNbrListDistinctToCardNo(String cardNbr) throws ProcessException {
		QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;
		QCcsCard qCcsCard = QCcsCard.ccsCard;
		QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
		JPAQuery query = new JPAQuery(em);
		List<Long> acctNbrList = query
				.from(qCcsCardLmMapping, qCcsCard, qCcsAcct)
				.where(qCcsCardLmMapping.logicCardNbr.eq(qCcsCard.logicCardNbr).and(
						qCcsCardLmMapping.org
								.eq(qCcsCard.org)
								.and(qCcsCardLmMapping.cardNbr.eq(cardNbr))
								.and(qCcsCard.acctNbr.eq(qCcsAcct.acctNbr).and(
										qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg())))))
				.distinct().list(qCcsAcct.acctNbr);
		if (acctNbrList.isEmpty()){
			throw new ProcessException("卡号[" + cardNbr + "]查询不到对应的账户信息");
		}
		return acctNbrList;
	}
	/*
	@Test
	public void testJoin(){
		QCcsCustomer qccsCustomer = QCcsCustomer.ccsCustomer;
		//QCcsCustomer qccsCustomer2 = new QCcsCustomer(cust_Id);
		QCcsAddress qCcsAddress = QCcsAddress.ccsAddress;
	//	QCcsAddress qCcsAddressKey= new QCcsAddress(cust_Id);
		QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
		JPAQuery query = new JPAQuery(em);
		HibernateSubQuery subQuery = new HibernateSubQuery();
		
		// subQuery.from(qCcsAddress).innerJoin(qCcsAddress.custId,qCcsAddress.custId);
		 ListSubQuery<Integer> sp = subQuery.from(qCcsAddress).list(qCcsAddress.custId);
		QCcsCustomer tmcustomer2 = new QCcsCustomer(cust_Id);
		query.from(qccsCustomer).leftJoin(qCcsAddress.address,qCcsAddress).list(qccsCustomer);
		
	}*/
	
}
 
