package com.sunline.ccs.service.process.test; 

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.time.DateUtils;
import org.hsqldb.util.DatabaseManagerSwing;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.hibernate.HibernateSubQuery;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.infrastructure.server.repos.RCcsCard;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardO;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.service.process.CardServiceImpl;
import com.sunline.ccs.service.process.test.util.CPSConstantsTest;
import com.sunline.ccs.service.process.test.util.DataFixtures;
import com.sunline.ccs.service.util.BlockCodeUtil;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.enums.AddressType;
import com.sunline.ppy.dictionary.enums.BscSuppIndicator;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.exception.ProcessException;
/** 
* @author fanghj
* @version 创建时间：2012-7-26 下午5:28:49 
* 卡片类测试类
*/ 


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-service.xml")
@Transactional
@TransactionConfiguration(defaultRollback = true)
public class CardServiceImplTest {
	
	@Autowired
	private CardServiceImpl cpsCardServiceImpl;
	
	@Autowired
	private RCcsCard rCcsCard;
	
	@Autowired
	private RCcsCardO rCcsCardO;
	
	@Autowired
	CustAcctCardFacility cpsBusProvide;
	
	private String card_No = DataFixtures.getColunmValue(0,CcsCardLmMapping.TABLE_NAME, 
			DataFixtures.getColumnName(CcsCardLmMapping.class, CcsCardLmMapping.P_CardNbr)).toString();
	private String org = DataFixtures.getColunmValue(0,CcsCardLmMapping.TABLE_NAME, 
			DataFixtures.getColumnName(CcsCardLmMapping.class, CcsCardLmMapping.P_Org)).toString();
	
	
	
	
	
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
	
	@Test
	public void testGetCard(){
		JPAQuery query = new JPAQuery(em);
		QCcsPlan qCcsPlan = QCcsPlan.ccsPlan;
		QCcsCard qCcsCard = QCcsCard.ccsCard;
		QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;
		BooleanExpression ex = qCcsCard.logicCardNbr.eq(qCcsCardLmMapping.logicCardNbr).and(qCcsCardLmMapping.cardNbr.eq(""));
		List<CcsPlan> plan = query.from(qCcsPlan).where(qCcsPlan.logicCardNbr.eq(new HibernateSubQuery().from(qCcsCard,qCcsCardLmMapping).where(ex).unique(qCcsCard.cardBasicNbr))).list(qCcsPlan);
		System.out.println("232323233");
	}

	/**
	 * 
	 */
	@Test
	public void testNF3101() {
		try {
			List<Map<String,Serializable>> nf3101= cpsCardServiceImpl.NF3101(DataFixtures.getColunmValue(0,CcsCustomer.TABLE_NAME, 
					DataFixtures.getColumnName(CcsCustomer.class, CcsCustomer.P_IdType)).toString(), 
					DataFixtures.getColunmValue(0,CcsCustomer.TABLE_NAME, 
							DataFixtures.getColumnName(CcsCustomer.class, CcsCustomer.P_IdNo)).toString());
			Assert.assertNotNull(nf3101);
			Assert.assertEquals(nf3101.iterator().next().get(CcsCardLmMapping.P_LogicCardNbr).toString(), 
					DataFixtures.getColunmValue(0,CcsCardLmMapping.TABLE_NAME, 
							DataFixtures.getColumnName(CcsCardLmMapping.class, CcsCardLmMapping.P_LogicCardNbr)).toString());
		} catch (ProcessException e) {
			
			e.printStackTrace();
		}
	}


	/**
	 * 
	 */
	@Test
	public void testNF3104() {
		try {
			DatabaseManagerSwing ds = new DatabaseManagerSwing();
			ds.main();
			Map<String,Serializable> nf3104 = cpsCardServiceImpl.NF3104(card_No);
			Assert.assertNotNull(nf3104);
			Assert.assertEquals(nf3104.get(CcsCard.P_CustId).toString(), 
					DataFixtures.getColunmValue(0,CcsCard.TABLE_NAME, DataFixtures.getColumnName(CcsCard.class, CcsCard.P_CustId)).toString());
		} catch (ProcessException e) {
			e.printStackTrace();
		}
	}


	/**
	 * 
	 */
	@Test
	public void testNF3203() {
		Map<String,Serializable> transLmt = new HashMap<String,Serializable>();
		transLmt.put(CcsCardO.P_TxnLmt, 2000);
		transLmt.put(CcsCardO.P_TxnCashLmt, 1000);
		transLmt.put(CcsCardO.P_CycleRetailLmt, 1000);
		try {
			cpsCardServiceImpl.NF3203(card_No, transLmt);
			
			CcsCardO CcsCardO = this.getCcsCardOToCardNo(card_No);
			Assert.assertEquals(CcsCardO.getTxnLmt().toString(), transLmt.get(CcsCardO.P_TxnLmt).toString());
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 
	 */
	@Test
	public void testNF3204() {
		try {

			cpsCardServiceImpl.NF3204(card_No, Indicator.N);

			cpsCardServiceImpl.NF3204(card_No, Indicator.Y);

			
			CcsCard CcsCard = this.getCcsCardToCardNo(card_No);
			Assert.assertEquals(CcsCard.getPosPinVerifyInd(), Indicator.N);
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNF3403() {
		try {
			cpsCardServiceImpl.NF3403(card_No);
			Assert.assertEquals(this.getCcsCardToCardNo(card_No).getActiveInd(),Indicator.Y);
			Assert.assertEquals(this.getCcsCardOToCardNo(card_No).getActiveInd(), Indicator.Y);
		} catch (ProcessException e) {			
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNF3404() {
		try {
			cpsCardServiceImpl.NF3404(card_No);
			
			CcsCard CcsCard = this.getCcsCardToCardNo(card_No);
			CcsCardO CcsCardO = this.getCcsCardOToCardNo(card_No);
			
			
			//判断该卡是否为主卡
			if(CcsCard.getBscSuppInd().equals(BscSuppIndicator.B)){
				List<CcsCard> listCcsCard = this.getSUPPCcsCardToCardNo(card_No);
				List<CcsCardO> listCcsCardO = this.getSUPPCcsCardOToCardNo(card_No);
				for(CcsCard suppCcsCard:listCcsCard){
					Assert.assertEquals(BlockCodeUtil.hasBlockCode(suppCcsCard.getBlockCode(), CPSConstantsTest.BLOCKCODE_C), true);
				}
				for(CcsCardO suppCcsCardO:listCcsCardO){
					Assert.assertEquals(BlockCodeUtil.hasBlockCode(suppCcsCardO.getBlockCode(), CPSConstantsTest.BLOCKCODE_C), true);
				}
			}
			
			List<CcsAcct> listCcsAcct = this.getCcsAcctTOAcctNo(CcsCard.getAcctNbr());
			for(CcsAcct CcsAcct:listCcsAcct){
				Assert.assertEquals(BlockCodeUtil.hasBlockCode(CcsAcct.getBlockCode(), CPSConstantsTest.BLOCKCODE_C), true);
			}
			List<CcsAcctO>listCcsAcctO = this.getCcsAcctOToAcctNo(CcsCardO.getAcctNbr());			
			for(CcsAcctO CcsAcctO:listCcsAcctO){
				Assert.assertEquals(BlockCodeUtil.hasBlockCode(CcsAcctO.getBlockCode(), CPSConstantsTest.BLOCKCODE_C), true);
			}
			Assert.assertEquals(BlockCodeUtil.hasBlockCode(CcsCard.getBlockCode(), CPSConstantsTest.BLOCKCODE_C), true);
			Assert.assertEquals(BlockCodeUtil.hasBlockCode(CcsCardO.getBlockCode(), CPSConstantsTest.BLOCKCODE_C), true);
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	/**
	 * 
	 */
	@Test
	public void testNF3205() {
		try {
			cpsCardServiceImpl.NF3205(card_No, AddressType.valueOf(CPSConstantsTest.ADDRETYPE));
			
			CcsCard CcsCard = this.getCcsCardToCardNo(card_No);
			
			Assert.assertEquals(CcsCard.getCardDeliverAddrFlag().name(), CPSConstantsTest.ADDRETYPE);
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * 
	 */
	@Test
	public void testNF3406() {
		try {
			cpsCardServiceImpl.NF3406(card_No);
			CcsCard CcsCard = this.getCcsCardToCardNo(card_No);
			CcsCardO CcsCardO = this.getCcsCardOToCardNo(card_No);
			if(CcsCard.getBscSuppInd().equals(BscSuppIndicator.B)){
				Assert.assertEquals(CcsCard.getBlockCode().indexOf(CPSConstantsTest.BLOCKCODE_C), -1);
				Assert.assertEquals(CcsCardO.getBlockCode().indexOf(CPSConstantsTest.BLOCKCODE_C), -1);
				List<CcsAcct> listAccount = this.getCcsAcctTOAcctNo(CcsCard.getAcctNbr());
				List<CcsAcctO> listAccountO = this.getCcsAcctOToAcctNo(CcsCardO.getAcctNbr());
				for(CcsAcct CcsAcct:listAccount){
					Assert.assertEquals(CcsAcct.getBlockCode().indexOf(CPSConstantsTest.BLOCKCODE_C), -1);
				}
				for(CcsAcctO CcsAcctO:listAccountO){
					Assert.assertEquals(CcsAcctO.getBlockCode().indexOf(CPSConstantsTest.BLOCKCODE_C), -1);
				}
			}else if(CcsCard.getBscSuppInd().equals(BscSuppIndicator.S)){
				CcsCard bscCcsCard = this.getCcsCardToLogicCardNo(CcsCard.getCardBasicNbr());
				CcsCardO bscCcsCardO = this.getCcsCardOToLogicCardNo(CcsCard.getCardBasicNbr());
				Assert.assertEquals(bscCcsCard.getBlockCode().indexOf(CPSConstantsTest.BLOCKCODE_C), -1);
				Assert.assertEquals(bscCcsCardO.getBlockCode().indexOf(CPSConstantsTest.BLOCKCODE_C), -1);
			}
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
	/**
	 * 
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testNF3207() {
		try {
			cpsCardServiceImpl.NF3207(card_No);
			CcsCard CcsCard = this.getCcsCardToCardNo(card_No);
			Assert.assertEquals(CcsCard.getNextCardFeeDate().getYear(),DateUtils.addYears(new Date(), 1).getYear());
			
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testNF3407(){
		try {
			cpsCardServiceImpl.NF3407(card_No);
			CcsCard CcsCard = this.getCcsCardToCardNo(card_No);
			CcsCardO CcsCardO = this.getCcsCardOToCardNo(card_No);
			Assert.assertEquals(CcsCard.getBlockCode().contains(CPSConstantsTest.BLOCKCODE_T), true);
			Assert.assertEquals(CcsCardO.getBlockCode().contains(CPSConstantsTest.BLOCKCODE_T), true);
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 */
	@Test
	public void testNF3408(){
		try {
			cpsCardServiceImpl.NF3408(card_No);
			CcsCard CcsCard = this.getCcsCardToCardNo(card_No);
			CcsCardO CcsCardO = this.getCcsCardOToCardNo(card_No);
			Assert.assertEquals(CcsCard.getBlockCode().contains(CPSConstantsTest.BLOCKCODE_T), false);
			Assert.assertEquals(CcsCardO.getBlockCode().contains(CPSConstantsTest.BLOCKCODE_T), false);
			
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 根据卡号获取CcsCard数据
	 * 
	 * @param cardNbr
	 * @return
	 * @throws ProcessException 
	 */
	private CcsCard getCcsCardToCardNo(String cardNbr) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		QCcsCard qCcsCard = QCcsCard.ccsCard;
		QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;
		CcsCard CcsCard = query.from(qCcsCardLmMapping, qCcsCard).where(
				qCcsCardLmMapping.logicCardNbr.eq(qCcsCard.logicCardNbr).and(
						qCcsCardLmMapping.org.eq(qCcsCard.org).and(
								qCcsCardLmMapping.cardNbr.eq(cardNbr).and(
										qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg()))))).singleResult(qCcsCard);
		CheckUtil.rejectNull(CcsCard, "卡号:["+cardNbr+"]查询不到对应的CcsCard数据");
		return CcsCard;
	}

	/**
	 * 根据卡号获取CcsCardO数据
	 * 
	 * @param cardNbr
	 * @return
	 * @throws ProcessException 
	 */
	private CcsCardO getCcsCardOToCardNo(String cardNbr) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		QCcsCardO qCcsCardO = QCcsCardO.ccsCardO;
		QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;
		CcsCardO CcsCardO = query.from(qCcsCardLmMapping, qCcsCardO).where(
				qCcsCardLmMapping.logicCardNbr.eq(qCcsCardO.logicCardNbr).and(
						qCcsCardLmMapping.org.eq(qCcsCardO.org).and(
								qCcsCardLmMapping.cardNbr.eq(cardNbr).and(
										qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg()))))).singleResult(qCcsCardO);
		CheckUtil.rejectNull(CcsCardO, "卡号:["+cardNbr+"]查询不到对应的CcsCardO数据");
		return CcsCardO;
	}
	
	/**
	 * 根据卡号获取所有的附卡信息
	 * 
	 * @param cardNbr
	 * @return
	 */
	private List<CcsCard> getSUPPCcsCardToCardNo(String cardNbr){
		QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;
		QCcsCard qCcsCard = QCcsCard.ccsCard;
		JPAQuery query =  new JPAQuery(em);
		List<CcsCard> list=query.from(qCcsCard,qCcsCardLmMapping).where(
				qCcsCardLmMapping.logicCardNbr.eq(qCcsCard.cardBasicNbr)
				.and(qCcsCardLmMapping.org.eq(qCcsCard.org)
						.and(qCcsCardLmMapping.cardNbr.eq(cardNbr)
								.and(qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg()))))).list(qCcsCard);
		return list;
	}
	
	/**
	 * 根据卡号获取所有附卡信息
	 * @param cardNbr
	 * @return
	 */
	private List<CcsCardO> getSUPPCcsCardOToCardNo(String cardNbr){
		QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;
		QCcsCardO qCcsCardO = QCcsCardO.ccsCardO;
		JPAQuery query =  new JPAQuery(em);
		List<CcsCardO> list=query.from(qCcsCardO,qCcsCardLmMapping).where(
				qCcsCardLmMapping.logicCardNbr.eq(qCcsCardO.cardBasicNbr)
				.and(qCcsCardLmMapping.org.eq(qCcsCardO.org)
						.and(qCcsCardLmMapping.cardNbr.eq(cardNbr)
								.and(qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg()))))).list(qCcsCardO);
		return list;
	}
	
	/**
	 * 根据账户号获取账户列表
	 * @param long1
	 * @return
	 * @throws ProcessException 
	 */
	private List<CcsAcct> getCcsAcctTOAcctNo(Long long1) throws ProcessException{
		JPAQuery query = new JPAQuery(em);
		QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
		List<CcsAcct> list = query.from(qCcsAcct)
				.where(qCcsAcct.acctNbr.eq(long1).and(qCcsAcct.org.eq(OrganizationContextHolder.getCurrentOrg())))
				.list(qCcsAcct);
		if (list.isEmpty()){
			throw new ProcessException("账户号[" + long1 + "]查询不到对应的账户信息");
		}
			return list;
	}
	
	/**
	 * 根据账户号获取账户列表
	 * @param long1
	 * @return
	 * @throws ProcessException 
	 */
	private List<CcsAcctO> getCcsAcctOToAcctNo(Long long1) throws ProcessException{
		JPAQuery query = new JPAQuery(em);
		QCcsAcctO qCcsAcctO = QCcsAcctO.ccsAcctO;
		List<CcsAcctO> list = query.from(qCcsAcctO)
				.where(qCcsAcctO.acctNbr.eq(long1).and(qCcsAcctO.org.eq(OrganizationContextHolder.getCurrentOrg())))
				.list(qCcsAcctO);
		if (list.isEmpty()){
			throw new ProcessException("账户号[" + long1 + "]查询不到对应的账户信息");
		}
			return list;
	}
	
	/**
	 * 根据logicCardNo获取卡片信息
	 * @param logicCardNo
	 * @return
	 * @throws ProcessException
	 */
	private CcsCard getCcsCardToLogicCardNo(String bscLogiccardNbr) throws ProcessException{
		QCcsCard qCcsCard = QCcsCard.ccsCard;
		CcsCard CcsCard = rCcsCard.findOne(qCcsCard.org.eq(OrganizationContextHolder.getCurrentOrg()).and(
				qCcsCard.logicCardNbr.eq(bscLogiccardNbr)));
		CheckUtil.rejectNull(CcsCard, "逻辑卡号[" + bscLogiccardNbr + "]查询不到对应的卡片信息");
		return CcsCard;
	}
	
	/**
	 * 根据logicCardNo获取卡片信息
	 * @param logicCardNo
	 * @return
	 * @throws ProcessException
	 */
	public CcsCardO getCcsCardOToLogicCardNo(String bsclogiccardNbr) throws ProcessException {
		QCcsCardO qCcsCardO = QCcsCardO.ccsCardO;
		CcsCardO CcsCardO = rCcsCardO.findOne(qCcsCardO.org.eq(OrganizationContextHolder.getCurrentOrg()).and(
				qCcsCardO.logicCardNbr.eq(bsclogiccardNbr)));
		CheckUtil.rejectNull(CcsCardO, "逻辑卡号[" + bsclogiccardNbr + "]查询不到对应的卡片信息");
		return CcsCardO;

	}

}
 
