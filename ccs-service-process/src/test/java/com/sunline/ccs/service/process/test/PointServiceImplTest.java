package com.sunline.ccs.service.process.test;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.QCcsPointsReg;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.CcsPointsReg;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnUnstatement;
import com.sunline.ccs.service.process.PointServiceImpl;
import com.sunline.ccs.service.process.test.util.CPSConstantsTest;
import com.sunline.ccs.service.process.test.util.DataFixtures;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.service.QueryRequest;
import com.sunline.ark.support.service.QueryResult;
import com.mysema.query.jpa.impl.JPAQuery;

/** 
* @author fanghj
* @version 创建时间：2012-8-15 上午10:55
* 测试PointServiceImpl类
*/ 

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-service.xml")
@Transactional
@TransactionConfiguration(defaultRollback = true)
public class PointServiceImplTest {
	
	@Autowired
	private PointServiceImpl cpsPointServiceImpl;
	
	@PersistenceContext
	private EntityManager em ;
	
	private String org = (String)DataFixtures.getColunmValue(0,CcsCardLmMapping.TABLE_NAME, 
			DataFixtures.getColumnName(CcsCardLmMapping.class, CcsCardLmMapping.P_Org));
	private String card_No = (String)DataFixtures.getColunmValue(0,CcsCardLmMapping.TABLE_NAME, 
			DataFixtures.getColumnName(CcsCardLmMapping.class, CcsCardLmMapping.P_CardNbr));
	
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
	public void testNF5102() {
		try {
		QueryRequest queryRequest = new QueryRequest();
		queryRequest.setFirstRow(0);
		queryRequest.setLastRow(2);
		
		String stmtDate = DataFixtures.getColunmValue(0, CcsTxnUnstatement.TABLE_NAME, 
				DataFixtures.getColumnName(CcsTxnUnstatement.class, CcsTxnUnstatement.P_StmtDate)).toString();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date startDate = dateFormat.parse(stmtDate);
		Date endDate = dateFormat.parse(stmtDate);
		
		QueryResult<Map<String,Serializable>>queryResult = cpsPointServiceImpl.NF5102(queryRequest, card_No, startDate, endDate);
		
		Assert.assertEquals(queryResult.getResult().iterator().hasNext(), true);
		Assert.assertNotNull(queryResult);
		} catch (ParseException e) {			
			e.printStackTrace();
		} catch (ProcessException e) {
			
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNF5201() {
		try {
			BigDecimal points = new BigDecimal(DataFixtures.getColunmValue
					(0, CcsAcct.TABLE_NAME, DataFixtures.getColumnName(CcsAcct.class, CcsAcct.P_PointsBal)).toString());
			//先把积分调整方向设为增加，测试第二次把积分调整方向设为减少,由于accttype被写死，测试时数据库中把accttype值设为"C1"
			cpsPointServiceImpl.NF5201(card_No, CPSConstantsTest.NF5201_ADJUSTTYPE_A, points);
			CcsAcct CcsAcct = this.getCcsAcctToCardNo(card_No, AccountType.A.name());
			CcsPointsReg tmPointReg = this.getCcsPointsRegToAcctNo(CcsAcct);
			Assert.assertEquals(tmPointReg.getPoints(), points.add(points));
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNF5202() {
		try {
			BigDecimal points = new BigDecimal(DataFixtures.getColunmValue(0, CcsAcct.TABLE_NAME, 
					DataFixtures.getColumnName(CcsAcct.class, CcsAcct.P_PointsBal)).toString());
			cpsPointServiceImpl.NF5202(card_No, points);
			CcsAcct CcsAcct = this.getCcsAcctToCardNo(card_No, AccountType.A.name());
			CcsPointsReg tmPointReg = this.getCcsPointsRegToAcctNo(CcsAcct);
			Assert.assertEquals(tmPointReg.getPoints(), points.subtract(points));
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 根据卡号，账户类型获取账户信息
	 * 
	 * @param cardNbr
	 * @param acctType
	 * @return CcsAcct
	 * @throws ProcessException
	 */
	private CcsAcct getCcsAcctToCardNo(String cardNbr, String acctType) throws ProcessException {
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
												qCcsAcct.acctType.eq(AccountType.valueOf(acctType))))))).singleResult(qCcsAcct);
		CheckUtil.rejectNull(CcsAcct, "卡号[" + cardNbr + "],账户类型[" + acctType + "]查询不到对应的账户信息");
		return CcsAcct;
	}
	
	/**
	 * 根据CcsAcct的acctno得到积分调整通知表数据CcsPointsReg
	 * @param CcsAcct
	 * @return CcsPointsReg
	 * @throws ProcessException
	 */
	private CcsPointsReg getCcsPointsRegToAcctNo(CcsAcct CcsAcct) throws ProcessException{
		QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;
		QCcsPointsReg qCcsPointsReg = QCcsPointsReg.ccsPointsReg;
		JPAQuery query = new JPAQuery(em);
		CcsPointsReg tmPointReg = query.from(qCcsCardLmMapping,qCcsPointsReg)
				.where(qCcsCardLmMapping.org.eq(org)
						.and(qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg())
								.and(qCcsPointsReg.acctNbr.eq(CcsAcct.getAcctNbr())))).singleResult(qCcsPointsReg);
		CheckUtil.rejectNull(tmPointReg, "CcsAcct.getAcctNbr["+CcsAcct.getAcctNbr()+"]查询不到对应的注册信息");
		return tmPointReg;
	}

}
