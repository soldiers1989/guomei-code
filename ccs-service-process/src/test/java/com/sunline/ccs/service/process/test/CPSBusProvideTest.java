package com.sunline.ccs.service.process.test;


import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.IdType;
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardUsage;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.service.process.test.util.DataFixtures;
import com.sunline.ark.support.OrganizationContextHolder;

/** 
* @author fanghj
* @version 创建时间：2012-8-25 下午15:15
* CPSBusProvide测试类
*/ 

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-service.xml")
@Transactional
@TransactionConfiguration(defaultRollback = true)
public class CPSBusProvideTest {

	@Autowired
	private CustAcctCardFacility cpsBusProvide;
	
	private String org = (String)DataFixtures.getColunmValue(
			0,CcsCardLmMapping.TABLE_NAME, DataFixtures.getColumnName(CcsCardLmMapping.class, CcsCardLmMapping.P_Org));
	private String card_No = (String)DataFixtures.getColunmValue(
			0,CcsCardLmMapping.TABLE_NAME, DataFixtures.getColumnName(CcsCardLmMapping.class, CcsCardLmMapping.P_CardNbr));
	private AccountType acct_Type = AccountType.valueOf(DataFixtures.getColunmValue(
			0,CcsAcct.TABLE_NAME, DataFixtures.getColumnName(CcsAcct.class, CcsAcct.P_AcctType)).toString());
	private Long acct_No = Long.parseLong(DataFixtures.getColunmValue(
			0, CcsAcct.TABLE_NAME, DataFixtures.getColumnName(CcsAcct.class, CcsAcct.P_AcctNbr)).toString());
	private IdType id_Type = IdType.valueOf(DataFixtures.getColunmValue(
			0, CcsCustomer.TABLE_NAME, DataFixtures.getColumnName(CcsCustomer.class, CcsCustomer.P_IdType)).toString()); 
	private String id_No = (String)DataFixtures.getColunmValue(
			0, CcsCustomer.TABLE_NAME, DataFixtures.getColumnName(CcsCustomer.class, CcsCustomer.P_IdNo));
	
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
	public void testGetCcsCustomerToCard() {
		CcsCustomer ccsCustomer = cpsBusProvide.getCustomerByCardNbr(card_No);
		Assert.assertNotNull(ccsCustomer);
				
	}

	/**
	 * 
	 */
	@Test
	public void testGetTmCustLmtOToCustLmtId() {
		Long custLmtId = Long.parseLong(DataFixtures.getColunmValue(
				0, CcsCustomerCrlmt.TABLE_NAME, DataFixtures.getColumnName(CcsCustomerCrlmt.class, CcsCustomerCrlmt.P_CustLmtId)).toString());
		CcsCustomerCrlmt tmCustLmtO = cpsBusProvide.getCustomerCrLmtByCustLmtId(custLmtId);
		Assert.assertNotNull(tmCustLmtO);
	}

	/**
	 * 
	 */
	@Test
	public void testGetCcsAcctToCustId() {
		try {
			Long custId = (Long)DataFixtures.getColunmValue(
					0, CcsAcct.TABLE_NAME, DataFixtures.getColumnName(CcsAcct.class, CcsAcct.P_CustId));
			List<CcsAcct> CcsAcct = cpsBusProvide.getAcctByCustId(custId);
			Assert.assertEquals(CcsAcct.iterator().hasNext(),true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * 
	 */
	@Test
	public void testGetCcsAcctToCardNoString() {
		try {
			List<CcsAcct>CcsAcct = cpsBusProvide.getAcctByCardNbr(card_No);
			Assert.assertEquals(CcsAcct.iterator().hasNext(), true);
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testGetAcctNoListDistinctToCardNo() {
		try {
			List<Long> distinctList = cpsBusProvide.getDistinctAcctNbrListByCardNbr(card_No);
			Assert.assertNotNull(distinctList);
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testGetCcsAcctToCardNo() {
			CcsAcct CcsAcct;
			try {
				CcsAcct = cpsBusProvide.getAcctByCardNbr(card_No, acct_Type);
				Assert.assertEquals(CcsAcct.getAcctType(), acct_Type);
			} catch (ProcessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}

	/**
	 * 
	 */
	@Test
	public void testGetCcsAcctOToCardNo() {
		try {
			CcsAcctO CcsAcctO = cpsBusProvide.getAcctOByCardNbr(card_No, acct_Type);
			Assert.assertEquals(CcsAcctO.getAcctType(), acct_Type);
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testGetCcsAcctOToCardNoString() {
		try {
			List<CcsAcctO> CcsAcctOList = cpsBusProvide.getAcctOByCardNbr(card_No);
			Assert.assertEquals(CcsAcctOList.iterator().hasNext(),true);
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	



	/**
	 * 
	 */
	@Test
	public void testGetCcsCardOToCardNo() {
		CcsCardO CcsCardO = cpsBusProvide.getCardOByCardNbr(card_No);
		Assert.assertNotNull(CcsCardO);
	}
	/**
	 * 
	 */
	@Test
	public void testGetCcsCardToCardNo() {
		CcsCard CcsCard = cpsBusProvide.getCardByCardNbr(card_No);
		Assert.assertNotNull(CcsCard);
	}

	/**
	 * 
	 */
	@Test
	public void testGetCcsCardUsageToCardNo() {
		try {
			CcsCardUsage CcsCardUsage = cpsBusProvide.getCardUsageByCardNbr
					(card_No);
			Assert.assertNotNull(CcsCardUsage);
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testGetSUPPCcsCardToCardNo() {
		List<CcsCard> CcsCardList = cpsBusProvide.getSuppCardByCardNbr(card_No);
		Assert.assertNotNull(CcsCardList);
	}

	/**
	 * 
	 */
	@Test
	public void testGetSUPPCcsCardOToCardNo() {
		List<CcsCardO> CcsCardOList = cpsBusProvide.getSuppCardOByCardNbr(card_No);
		Assert.assertNotNull(CcsCardOList);
	}

	/**
	 * 
	 */
	@Test
	public void testGetCcsAcctOTOAcctNo() {
		try {
			List<CcsAcctO>CcsAcctOList = cpsBusProvide.getAcctOByAcctNbr(acct_No);
			Assert.assertEquals(CcsAcctOList.get(0).getAcctNbr(), acct_No);
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testGetCcsAcctTOAcctNoInteger() {
		try {
			List<CcsAcct>CcsAcctList = cpsBusProvide.getAcctByAcctNbr(acct_No);
			Assert.assertEquals(CcsAcctList.get(0).getAcctNbr(), acct_No);
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testGetCcsAcctOToAcctNo() {
		CcsAcctO CcsAcctO = cpsBusProvide.getAcctOByAcctNbr(acct_Type, acct_No);
		Assert.assertEquals(CcsAcctO.getAcctNbr(), acct_Type);
	}

	/**
	 * 
	 */
	@Test
	public void testGetCcsAcctTOAcctNoStringInteger() {
		try {
			CcsAcct CcsAcct = cpsBusProvide.getAcctByAcctNbr(acct_Type, acct_No);
			Assert.assertEquals(CcsAcct.getAcctNbr(), acct_No);
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testGetCcsCardListTOAccount() {
		List<CcsCard> CcsCardList = cpsBusProvide.getCardListByAcctNbr(acct_No);
		Assert.assertEquals(CcsCardList.get(0).getAcctNbr(), acct_No);
	}

	/**
	 * 
	 */
	@Test
	public void testGetCcsCardToLogicCardNo() {
		String logicCardNo = DataFixtures.getColunmValue(
				0, CcsCard.TABLE_NAME, DataFixtures.getColumnName(CcsCard.class, CcsCard.P_LogicCardNbr)).toString();
		CcsCard CcsCard = cpsBusProvide.getCardByLogicCardNbr(logicCardNo);
		Assert.assertNotNull(CcsCard);
	}

	/**
	 * 
	 */
	@Test
	public void testGetCcsCardOToLogicCardNo() {
		String logicCardNo = DataFixtures.getColunmValue(0
				, CcsCardO.TABLE_NAME, DataFixtures.getColumnName(CcsCardO.class, CcsCardO.P_LogicCardNbr)).toString();
		CcsCardO CcsCardO = cpsBusProvide.getCardOByLogicCardNbr(logicCardNo);
		Assert.assertNotNull(CcsCardO);
		
		}

	/**
	 * 
	 */
	@Test
	public void testGetCcsAcctTOIDtypeIdNo() {
		try {
			List<CcsAcct> CcsAcct = cpsBusProvide.getAcctByIdTypeIdNo(id_Type.name(), id_No);
			Assert.assertEquals(CcsAcct.iterator().hasNext(), true);
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testGetCcsAcctOTOIDtypeIdNo() {
		try {
			List<CcsAcctO> CcsAcctO = cpsBusProvide.getAcctOByIdTypeIdNo(id_Type.name(), id_No);
			Assert.assertEquals(CcsAcctO.iterator().hasNext(), true);
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
