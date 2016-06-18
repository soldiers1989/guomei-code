package com.sunline.ccs.service.process.test;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
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

import com.sunline.ppy.api.CcServProConstants;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.server.repos.RCcsTxnUnstatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnUnstatement;
import com.sunline.ccs.service.process.LoanServiceImpl;
import com.sunline.ccs.service.process.test.util.CPSConstantsTest;
import com.sunline.ccs.service.process.test.util.DataFixtures;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.service.QueryRequest;
import com.sunline.ark.support.service.QueryResult;
import com.mysema.query.jpa.impl.JPAQuery;

/**
* @author fanghj
 * @version 创建时间：2012-8-11 下午15:28 测试LoanServiceImpl类
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-service.xml")
@Transactional
@TransactionConfiguration(defaultRollback = true)
public class LoanServiceImplTest {

	@Autowired
	private LoanServiceImpl cpsLoanServiceImpl;
	@Autowired
	private RCcsTxnUnstatement rCcsTxnUnstatement;
	@Autowired
	private RCcsLoanReg rCcsLoanReg;
	@Autowired
	private RCcsLoan rCcsLoan;

	private String org = (String) DataFixtures.getColunmValue(0,
			CcsCardLmMapping.TABLE_NAME, DataFixtures.getColumnName(
					CcsCardLmMapping.class, CcsCardLmMapping.P_Org));
	private String card_No = (String) DataFixtures.getColunmValue(0,
			CcsCardLmMapping.TABLE_NAME, DataFixtures.getColumnName(
					CcsCardLmMapping.class, CcsCardLmMapping.P_CardNbr));

	@PersistenceContext
	private EntityManager em;

	/**
	 * 
	 */
	@Before
	public void setupDatabase() {
		OrganizationContextHolder.setCurrentOrg(org);
		OrganizationContextHolder.setUsername("dfd");
	}

	/**
	 * 
	 */
	@Test
	public void testNF6101() {
		try {
			CheckUtil.checkCardNo(card_No);
			QueryRequest queryRequest = new QueryRequest();
			queryRequest.setFirstRow(0);
			queryRequest.setLastRow(1);
			QueryResult<Map<String, Serializable>> NF6101_A = cpsLoanServiceImpl
					.NF6101(queryRequest, card_No,
							CPSConstantsTest.NF6101_ACCOUNTINDICATOR_A);
			QueryResult<Map<String, Serializable>> NF6101_C = cpsLoanServiceImpl
					.NF6101(queryRequest, card_No,
							CPSConstantsTest.NF6101_CARDINDICATOR_C);

			Assert.assertNotNull(NF6101_A);
			Assert.assertNotNull(NF6101_C);
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 
	 */
	@Test
	public void testNF6301() {
		Map<String, Serializable> transaction = new HashMap<String, Serializable>();
		transaction.put(
				CcsTxnUnstatement.P_TxnSeq,
				DataFixtures.getColunmValue(
						0,
						CcsTxnUnstatement.TABLE_NAME,
						DataFixtures.getColumnName(CcsTxnUnstatement.class,
								CcsTxnUnstatement.P_TxnSeq)).toString());
		BigDecimal getTxnAmt = new BigDecimal(DataFixtures.getColunmValue(
				0,
				CcsTxnUnstatement.TABLE_NAME,
				DataFixtures.getColumnName(CcsTxnUnstatement.class,
						CcsTxnUnstatement.P_TxnAmt)).toString());// 得到交易金额
		try {
			Map<String, Serializable> NF6301 = cpsLoanServiceImpl.NF6301(
					CPSConstantsTest.NF6301_TERM, transaction, true, null);
			Assert.assertNotNull(NF6301);
			CcsTxnUnstatement tmTxnUnstmt = rCcsTxnUnstatement
					.findOne(Long.parseLong(transaction.get(
							CcsTxnUnstatement.P_TxnSeq).toString()));
			BigDecimal planCurrBal = this.getPlanCurrbalToAccttypeAndAcctNo(
					tmTxnUnstmt.getAcctType().name(), tmTxnUnstmt.getAcctNbr());
			if (planCurrBal.compareTo(getTxnAmt) > 0) {
				Assert.assertEquals(NF6301.get(CcsLoanReg.P_LoanFirstTermFee),
						getTxnAmt.multiply(CcServProConstants.SCALE));
				Assert.assertEquals(NF6301.get(CcsLoanReg.P_LoanFirstTermPrin),
						getTxnAmt.divideToIntegralValue(new BigDecimal(
								CPSConstantsTest.NF6301_TERM)));
				Assert.assertEquals(
						NF6301.get(CcsLoanReg.P_LoanInitFee),
						getTxnAmt.multiply(CcServProConstants.SCALE).multiply(
								new BigDecimal(CPSConstantsTest.NF6301_TERM)));
				Assert.assertEquals(
						NF6301.get(CcsLoanReg.P_LoanFinalTermPrin),
						getTxnAmt
								.subtract(getTxnAmt
										.divideToIntegralValue(
												new BigDecimal(
														CPSConstantsTest.NF6301_TERM))
										.multiply(
												new BigDecimal(
														CPSConstantsTest.NF6301_TERM - 1))));
			} else {
				Assert.assertEquals(NF6301.get(CcsLoanReg.P_LoanFirstTermFee),
						planCurrBal.multiply(CcServProConstants.SCALE));
				Assert.assertEquals(NF6301.get(CcsLoanReg.P_LoanFirstTermPrin),
						planCurrBal.divideToIntegralValue(new BigDecimal(
								CPSConstantsTest.NF6301_TERM)));
				Assert.assertEquals(
						NF6301.get(CcsLoanReg.P_LoanInitFee),
						planCurrBal.multiply(CcServProConstants.SCALE).multiply(
								new BigDecimal(CPSConstantsTest.NF6301_TERM)));
				Assert.assertEquals(
						NF6301.get(CcsLoanReg.P_LoanFinalTermPrin),
						planCurrBal
								.subtract(planCurrBal
										.divideToIntegralValue(
												new BigDecimal(
														CPSConstantsTest.NF6301_TERM))
										.multiply(
												new BigDecimal(
														CPSConstantsTest.NF6301_TERM - 1))));
			}

		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNF6302() {
		CcsTxnUnstatement tmTxnUnstmt = new CcsTxnUnstatement();
		Map<String, Serializable> transaction = new HashMap<String, Serializable>();
		transaction.put(
				CcsTxnUnstatement.P_CardNbr,
				DataFixtures.getColunmValue(
						0,
						CcsTxnUnstatement.TABLE_NAME,
						DataFixtures.getColumnName(CcsTxnUnstatement.class,
								CcsTxnUnstatement.P_CardNbr)).toString());
		transaction.put(
				CcsTxnUnstatement.P_RefNbr,
				DataFixtures.getColunmValue(
						0,
						CcsTxnUnstatement.TABLE_NAME,
						DataFixtures.getColumnName(CcsTxnUnstatement.class,
								CcsTxnUnstatement.P_RefNbr)).toString());
		try {
			cpsLoanServiceImpl.NF6302(transaction);
			tmTxnUnstmt.updateFromMap(transaction);
			QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;
			CcsLoanReg loanReg = rCcsLoanReg.findOne(qCcsLoanReg.refNbr
					.eq(tmTxnUnstmt.getRefNbr()));
			Assert.assertEquals(loanReg, null);
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNF6303() {
		Map<String, Serializable> transaction = new HashMap<String, Serializable>();
		transaction.put(
				CcsLoan.P_LoanId,
				DataFixtures.getColunmValue(
						0,
						CcsLoan.TABLE_NAME,
						DataFixtures.getColumnName(CcsLoan.class,
								CcsLoan.P_LoanId)).toString());
		try {
			cpsLoanServiceImpl.NF6303(transaction);
			QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
			CcsLoan loan = rCcsLoan.findOne(qCcsLoan.loanId.eq(Long
					.parseLong(transaction.get(CcsLoan.P_LoanId).toString())));
			Assert.assertEquals(loan.getLoanType().name(),
					CPSConstantsTest.NF6303_LOANSTATUS_M);
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNF6304() {
		Map<String, Serializable> transaction = new HashMap<String, Serializable>();
		transaction.put(
				CcsLoan.P_LoanId,
				DataFixtures.getColunmValue(
						0,
						CcsLoan.TABLE_NAME,
						DataFixtures.getColumnName(CcsLoan.class,
								CcsLoan.P_LoanId)).toString());
		try {
			cpsLoanServiceImpl.NF6304(transaction);
			QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
			CcsLoan loan = rCcsLoan.findOne(qCcsLoan.loanId.eq(
					Long.parseLong(transaction.get(CcsLoan.P_LoanId)
							.toString())).and(
					qCcsLoan.org.eq(OrganizationContextHolder.getCurrentOrg())));
			CheckUtil.rejectNull(loan, "分期交易不存在!!");
			Assert.assertEquals(
					loan.getLoanStatus().name(),
					DataFixtures.getColunmValue(
							0,
							CcsLoan.TABLE_NAME,
							DataFixtures.getColumnName(CcsLoan.class,
									CcsLoan.P_LastLoanStatus)).toString());
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNF6102() {
		try {
			QueryRequest queryRequest = new QueryRequest();
			queryRequest.setFirstRow(0);
			queryRequest.setLastRow(1);
			QueryResult<Map<String, Serializable>> loanListMap_A = cpsLoanServiceImpl
					.NF6102(queryRequest, card_No,
							CPSConstantsTest.NF6101_ACCOUNTINDICATOR_A);
			QueryResult<Map<String, Serializable>> loanListMap_C = cpsLoanServiceImpl
					.NF6102(queryRequest, card_No,
							CPSConstantsTest.NF6101_CARDINDICATOR_C);
			Assert.assertNotNull(loanListMap_A);
			Assert.assertNotNull(loanListMap_C);

		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 根据TmTxnUnstmt表中的accpType和accpNo得到CcsPlan表中的加总金额loanInitPrin
	 * 
	 * @param acctType
	 * @param long1
	 * @return loanInitPrin加总金额
	 * @throws ProcessException
	 */
	private BigDecimal getPlanCurrbalToAccttypeAndAcctNo(String acctType,
			Long long1) throws ProcessException {
		QCcsPlan qTmplan = QCcsPlan.ccsPlan;
		JPAQuery query = new JPAQuery(em);
		Iterator<BigDecimal> currBalanceIter = query
				.from(qTmplan)
				.where(qTmplan.acctType.eq(AccountType.valueOf(acctType)).and(
						qTmplan.acctNbr.eq(long1).and(
								qTmplan.planType.eq(PlanType.R))))
				.list(qTmplan.currBal).iterator();
		if (!currBalanceIter.hasNext()) {
			throw new ProcessException("查询不到对应的消费计划");
		}
		BigDecimal loanInitPrin = new BigDecimal(0);
		while (currBalanceIter.hasNext()) {
			loanInitPrin = loanInitPrin.add(currBalanceIter.next());
		}
		return loanInitPrin;
	}

}
