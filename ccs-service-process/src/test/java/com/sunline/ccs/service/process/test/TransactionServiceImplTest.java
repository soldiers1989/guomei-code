package com.sunline.ccs.service.process.test;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoHst;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ccs.service.process.TransactionServiceImpl;
import com.sunline.ccs.service.process.test.util.CPSConstantsTest;
import com.sunline.ccs.service.process.test.util.DataFixtures;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ark.support.service.QueryRequest;
import com.sunline.ark.support.service.QueryResult;

/**
* @author fanghj
 * @version 创建时间：2012-8-4 下午6:01:26 类说明
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-service.xml")
@Transactional
@TransactionConfiguration(defaultRollback = true)
public class TransactionServiceImplTest {

	@Autowired
	TransactionServiceImpl cpsTransactionServiceImpl;

	String org = DataFixtures.getColunmValue(
			0,
			CcsCardLmMapping.TABLE_NAME,
			DataFixtures.getColumnName(CcsCardLmMapping.class,
					CcsCardLmMapping.P_Org)).toString();
	String card_No = DataFixtures.getColunmValue(
			0,
			CcsCardLmMapping.TABLE_NAME,
			DataFixtures.getColumnName(CcsCardLmMapping.class,
					CcsCardLmMapping.P_CardNbr)).toString();

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
	public void testNF4101() {
		try {
			QueryRequest queryRequest = new QueryRequest();
			queryRequest.setFirstRow(0);
			queryRequest.setLastRow(1);
			// 得到开始日期和结束日期
			String txnTime = DataFixtures.getColunmValue(
					0,
					CcsAuthmemoO.TABLE_NAME,
					DataFixtures.getColumnName(CcsAuthmemoO.class,
							CcsAuthmemoO.P_OrigTransDate)).toString();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date startDate = dateFormat.parse(txnTime);
			Date endDate = dateFormat.parse(txnTime);

			QueryResult<Map<String, Serializable>> queryResult_A = cpsTransactionServiceImpl
					.NF4101(queryRequest, CPSConstantsTest.NF4101_TRANSTYPE_A,
							card_No, startDate, endDate);
			QueryResult<Map<String, Serializable>> queryResult_C = cpsTransactionServiceImpl
					.NF4101(queryRequest, CPSConstantsTest.NF4101_TRANSTYPE_C,
							card_No, startDate, endDate);

			Assert.assertEquals(queryResult_A.getResult().iterator().hasNext(),
					true);
			Assert.assertEquals(queryResult_C.getResult().iterator().hasNext(),
					true);
			Assert.assertNotNull(queryResult_A);
			Assert.assertNotNull(queryResult_C);
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testNF4102() {
		try {
			QueryRequest queryRequest = new QueryRequest();
			queryRequest.setFirstRow(0);
			queryRequest.setLastRow(1);
			QueryResult<Map<String, Serializable>> queryResult_A = cpsTransactionServiceImpl
					.NF4102(queryRequest, CPSConstantsTest.NF4102_TRANSTYPE_A,
							card_No);
			QueryResult<Map<String, Serializable>> queryResult_C = cpsTransactionServiceImpl
					.NF4102(queryRequest, CPSConstantsTest.NF4102_TRANSTYPE_C,
							card_No);

			Assert.assertEquals(queryResult_A.getResult().iterator().hasNext(),
					true);
			Assert.assertEquals(queryResult_C.getResult().iterator().hasNext(),
					true);

			Assert.assertNotNull(queryResult_A);
			Assert.assertNotNull(queryResult_C);
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNF4103() {
		try {
			QueryRequest queryRequest = new QueryRequest();
			queryRequest.setFirstRow(0);
			queryRequest.setLastRow(2);

			// 得到开始日期和结束日期
			String txnDate = DataFixtures.getColunmValue(
					0,
					CcsAuthmemoHst.TABLE_NAME,
					DataFixtures.getColumnName(CcsAuthmemoHst.class,
							CcsAuthmemoHst.P_OrigTransDate)).toString();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date startDate = dateFormat.parse(txnDate);
			Date endDate = dateFormat.parse(txnDate);

			QueryResult<Map<String, Serializable>> QueryResult_A = cpsTransactionServiceImpl
					.NF4103(queryRequest, CPSConstantsTest.NF4103_TRANSTYPE_A,
							card_No, startDate, endDate);
			QueryResult<Map<String, Serializable>> QueryResult_C = cpsTransactionServiceImpl
					.NF4103(queryRequest, CPSConstantsTest.NF4103_TRANSTYPE_C,
							card_No, startDate, endDate);

			Assert.assertNotNull(QueryResult_A);
			Assert.assertNotNull(QueryResult_C);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNF4104() {
		try {
			QueryRequest queryRequest = new QueryRequest();
			queryRequest.setFirstRow(0);
			queryRequest.setLastRow(2);

			QueryResult<Map<String, Serializable>> queryResult_A = cpsTransactionServiceImpl
					.NF4104(queryRequest, card_No,
							CPSConstantsTest.NF4104_TRANSTYPE_A, null);
			QueryResult<Map<String, Serializable>> queryResult_C = cpsTransactionServiceImpl
					.NF4104(queryRequest, card_No,
							CPSConstantsTest.NF4104_TRANSTYPE_C, null);

			Assert.assertEquals(queryResult_A.getResult().iterator().hasNext(),
					true);
			Assert.assertEquals(queryResult_C.getResult().iterator().hasNext(),
					true);
			Assert.assertNotNull(queryResult_A);
			Assert.assertNotNull(queryResult_C);
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
