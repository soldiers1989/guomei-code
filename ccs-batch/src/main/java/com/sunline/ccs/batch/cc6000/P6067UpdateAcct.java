package com.sunline.ccs.batch.cc6000;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.utils.DateUtils;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AuthTransType;


/** 
 * @see 类名：P6067UpdateAcct
 * @see 描述：维护账户和信用计划数据
 *
 * @see 创建日期：   2015年6月25日 下午2:38:14
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class P6067UpdateAcct implements ItemProcessor<S6000AcctInfo, S6000AcctInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private BatchStatusFacility batchFacility;
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	@PersistenceContext
	private EntityManager em;

	
	public S6000AcctInfo process(S6000AcctInfo item) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("维护账户和信用计划数据：Org["+item.getAccount().getOrg()
					+"],AcctNo["+item.getAccount().getAcctNbr()
					+"],AcctType["+item.getAccount().getAcctType()
					+"],Plan.size["+item.getPlans().size()
					+"]");
		}
		// 超过有效期未提现冻结账户
		frozeAccount(item.getAccount());
		
		List<CcsPlan> delPlans = new ArrayList<CcsPlan>();
		for (CcsPlan plan : item.getPlans()) {
			// 判断信用计划余额等于零，且还清日期为NULL，则更新还清日期为当前日期
			if (plan.getCurrBal().compareTo(BigDecimal.ZERO) == 0 
					&& plan.getPaidOutDate() == null){
				plan.setPaidOutDate(batchFacility.getBatchDate());
			} 
			// 判断信用计划余额不等于零，且还清日期不等于NULL，则更新还清日期为NULL
			else if (plan.getCurrBal().compareTo(BigDecimal.ZERO) != 0 
					&& plan.getPaidOutDate() != null){
				plan.setPaidOutDate(null);
			}
			// 计划是否过期
			PlanTemplate planTemplate = parameterFacility.loadParameter(plan.getPlanNbr(), PlanTemplate.class);
			// 当天批量处理日期-计划保存天数>=计划还清日期
			Date planDate = DateUtils.addDays(batchFacility.getBatchDate(), -planTemplate.planPurgeDays);
//FXQ		if (plan.getPaidOutDate() != null && DateUtils.truncatedCompareTo(planDate, plan.getPaidOutDate(), Calendar.DATE) >= 0) {
			if (plan.getPaidOutDate() != null && DateUtils.truncatedCompareTo(planDate, plan.getPaidOutDate(), Calendar.DATE) >= 0
					&&plan.getNodefbnpIntAcru().compareTo(BigDecimal.ZERO) == 0&&plan.getBegDefbnpIntAcru().compareTo(BigDecimal.ZERO) == 0
					&&plan.getCtdDefbnpIntAcru().compareTo(BigDecimal.ZERO) == 0) {	
			// 删除计划
				em.remove(plan);
				delPlans.add(plan);
			}
			

			//今天新建的计划
			if (plan.getPlanId() == null) {
				if (logger.isDebugEnabled()) {
					logger.debug("维护账户和信用计划数据：PlanId["+plan.getPlanId()
							+"],PlanNbr["+plan.getPlanNbr()
							+"],Org["+plan.getOrg()
							+"],PlanType["+plan.getPlanType()
							+"],InterestRate["+plan.getInterestRate()
							+"]");
				}
				em.persist(plan);
			}
		}
		
		//删除计划
		item.getPlans().remove(delPlans);
		
		return item;
	}


	/**
	 * 循环贷超提款有效期未提款冻结账户
	 * @param acct
	 */
	private void frozeAccount(CcsAcct acct) {
		// 若已经冻结则不继续判断
		if(blockCodeUtils.isExists(acct.getBlockCode(), "T"))
			return;
		ProductCredit pc = parameterFacility.loadParameter(acct.getProductCd(), ProductCredit.class);
		// 是否在提款有效期内
		if(pc.dormentDays==null || pc.dormentDays<=0)
			return;
		Date dormentDate = DateUtils.addDays(acct.getSetupDate(), pc.dormentDays);
		if(DateUtils.truncatedCompareTo(batchFacility.getBatchDate(), dormentDate, Calendar.DATE)<=0)
			return;
		// 是否提过款
		if(acct.getLtdLoanAmt()!=null && acct.getLtdLoanAmt().compareTo(BigDecimal.ZERO)>0)
			return;
		// 是否有订单
		QCcsOrder qCcsOrder = QCcsOrder.ccsOrder;
		int orderCount = new JPAQuery(em).from(qCcsOrder)
				.where(qCcsOrder.acctNbr.eq(acct.getAcctNbr())
						.and(qCcsOrder.acctType.eq(acct.getAcctType()))
						.and(qCcsOrder.txnType.eq(AuthTransType.AgentDebit)))
				.list(qCcsOrder.orderId).size();
		if(orderCount > 0)
			return;
		
		// 冻结账户
		acct.setBlockCode(blockCodeUtils.addBlockCode(acct.getBlockCode(), "T"));
	}
}
