package com.sunline.ccs.batch.cc6000;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsStatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsStatement;
import com.sunline.ccs.otb.CommProvide;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.Indicator;


/** 
 * @see 类名：P6027InterestGraceWaive
 * @see 描述：全额还款检验
 *
 * @see 创建日期：   2015年6月25日 下午2:00:42
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P6027InterestGraceWaive implements ItemProcessor<S6000AcctInfo, S6000AcctInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
    private BatchStatusFacility batchFacility;
	@Autowired
	private CommProvide commonProvide;
	@PersistenceContext
	protected EntityManager em;
	
	@Override
	public S6000AcctInfo process(S6000AcctInfo item) {
		if (logger.isDebugEnabled()) {
			logger.debug("全额还款检验：Org["+item.getAccount().getOrg()
					+"],AcctType["+item.getAccount().getAcctType()
					+"],AcctNo["+item.getAccount().getAcctNbr()
					+"],BatchDate["+batchFacility.getBatchDate()
					+"]");
		}
		Date batchDate = batchFacility.getBatchDate();
		Date lastBatchDate = batchFacility.getLastBatchDate();

		// 未经过首次账单日的账户，更新全额还款标志为已还款，并返回
		if (item.getAccount().getFirstStmtDate().after(batchDate) ){
			if (item.getAccount().getGraceDaysFullInd() == Indicator.N){
				item.getAccount().setGraceDaysFullInd(Indicator.Y);
			}
			return item;
		}
		
		// 下一宽限日
		Date graceDay = item.getAccount().getGraceDate();
		
		// 判断批量日期在账单日与宽限日之间，则进行全额还款标志计算
		if (graceDay != null && (batchDate.before(DateUtils.addDays(graceDay, 1)) || lastBatchDate.before(graceDay)))
		{
			logger.info("日期时间段符合 批量日期:[" + batchDate.toString() + "] 上一账单日:[" 
											+ item.getAccount().getLastStmtDate() + "] 下一还款日:[" 
											+ graceDay.toString() + "]");
			if (item.getAccount().getGraceDaysFullInd() == Indicator.N)
			{
				verifyFullPayment(item);
			}
		}
		
		logger.info("全额还款检验Process finished! AccountNo:[" + item.getAccount().getAcctNbr() + "] AccountType:["
														+ item.getAccount().getAcctType() + "] ");
		
		return item;
	}

	/**
	 * 判断是否全额还款，更新全额还款标志
	 * @param item 账户信息
	 */
	private void verifyFullPayment(S6000AcctInfo item)	{
		
		CcsAcct account = item.getAccount();
		logger.info("开始判断全额还款 accountNo:[" + account.getAcctNbr() + "] accountType:[" + account.getAcctType() + "]");
		
		//获取参数
		ProductCredit productCredit = parameterFacility.loadParameter(account.getProductCd(), ProductCredit.class);
		AccountAttribute acctAttr = parameterFacility.loadParameter(productCredit.accountAttributeId, AccountAttribute.class);
		logger.info("容忍度计算方式 downpmtTolInd:[" + acctAttr.downpmtTolInd + "]");
		
		//应还款额
		BigDecimal qualGraceBal = item.getAccount().getQualGraceBal();
		
		//宽限额度
		BigDecimal stmtBalance = BigDecimal.ZERO;
		
		//当期剩余应还款额
		BigDecimal remainGraceBal = commonProvide.getRemainGraceBal(item.getPlans());
		
		//当期贷方交易金额大于账单总金额时，更新全额还款指示
		switch(acctAttr.downpmtTolInd){
		//按比例计算容忍度
		case R : stmtBalance = qualGraceBal.multiply(acctAttr.downpmtTolPerc);break;
		//按金额计算容忍度
		case A : stmtBalance = acctAttr.downpmtTol; break;
		//比例和金额同时考虑,取高的应还款额
		case B : 
			BigDecimal rateBalance = qualGraceBal.multiply(acctAttr.downpmtTolPerc);
			BigDecimal amountBalance = acctAttr.downpmtTol;
			stmtBalance = rateBalance.compareTo(amountBalance) > 0 ? rateBalance : amountBalance;
			break;
		default : 
			throw new IllegalArgumentException("账户属性中全额还款容忍度标志无法处理" );
		}
		if (remainGraceBal.compareTo(stmtBalance) <= 0) {
			// 已全额还款
			
			// 设置全额还款标志
			item.getAccount().setGraceDaysFullInd(Indicator.Y);
			
			// 免除往期累积延时利息
			waiveInterest(item.getPlans());
			
			//更新上一期账单的全额还款标示
			updateStatement(account);
		}
	}
	
	/**
	 * 免除往期余额累积利息
	 * @param plans 信用计划列表
	 */
	private void waiveInterest(List<CcsPlan> plans){
		for (CcsPlan plan : plans){
			plan.setBegDefbnpIntAcru(BigDecimal.ZERO);
		}
	}
	/**
	 * 更新最新一期账单的全额还款标志位Y
	 * @param acct
	 */
	private void updateStatement(CcsAcct acct){
		QCcsStatement qCcsStatement = QCcsStatement.ccsStatement;
		CcsStatement statement = new JPAQuery(em).from(qCcsStatement)
			.where(qCcsStatement.acctNbr.eq(acct.getAcctNbr())
					.and(qCcsStatement.acctType.eq(acct.getAcctType()))
					.and(qCcsStatement.stmtDate.goe(acct.getLastStmtDate())))
					.singleResult(qCcsStatement);
		statement.setGraceDaysFullInd(Indicator.Y);
		em.persist(statement);
	}
}
