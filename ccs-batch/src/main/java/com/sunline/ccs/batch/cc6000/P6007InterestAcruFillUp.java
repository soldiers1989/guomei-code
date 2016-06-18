package com.sunline.ccs.batch.cc6000;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.cc6000.common.InterestAcru;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.Indicator;


/**
 * @see 类名：P6007InterestAcruFillUp
 * @see 描述： 利息补计处理
 *
 * @see 创建日期：   2015-6-24上午10:04:39
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P6007InterestAcruFillUp implements ItemProcessor<S6000AcctInfo, S6000AcctInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private InterestAcru interestAccrual;
	@Autowired
    private BatchStatusFacility batchFacility;
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	
	@Override
	public S6000AcctInfo process(S6000AcctInfo item) {
		if (logger.isDebugEnabled()) {
			logger.debug("利息补计处理：Org["+item.getAccount().getOrg()
					+"],AcctType["+item.getAccount().getAcctType()
					+"],AcctNo["+item.getAccount().getAcctNbr()
					+"],LastBatchDate["+batchFacility.getLastBatchDate()
					+"],BatchDate["+batchFacility.getBatchDate()
					+"]");
		}
		Date lastBatchDate = batchFacility.getLastBatchDate();
		Date batchDate = batchFacility.getBatchDate();

		// 待执行连续补计利息天数
		int i = 0;
		
		Date patchDate = DateUtils.addDays(lastBatchDate, 1);
		
		// 单次补计计息起始日
		Date currDate = DateUtils.addDays(lastBatchDate, 1);
		
		// 还款日
		Date paymentDay = item.getAccount().getPmtDueDate();
		
		// 宽限日
		Date graceDay = item.getAccount().getGraceDate();
		int j = 0;
		// TODO 二期处理补给周期中包含账单日的处理
		
		// TODO 逻辑整理
		// 如果补计日期小于当前批量日期，则进行利息补计
		while (patchDate.before(batchDate))	{
			j++;
			// 判断补计日期是否等于还款日、宽限日和下一账单日
			if (paymentDay == null || graceDay == null ||
					(patchDate.compareTo(paymentDay) != 0 
					&& patchDate.compareTo(graceDay) != 0 
					&& patchDate.compareTo(item.getAccount().getNextStmtDate()) != 0)){
				// 补计日期不等于还款日、宽限日和下一账单日时，待执行连续补计利息天数+1
				i ++ ;
			}	else{
				// 补计日期为还款日、宽限日和下一账单日，可能为起息日，单独执行记息
				
				// 处理已累积连续补计天数
				if (i > 0){
					if (logger.isDebugEnabled()) {
						logger.debug("补计平日利息 起始日期["+currDate.toString()
								+"],补计天数["+i
								+"]");
					}
					interestAccrual.accumulateInterest(item, i, currDate);
					currDate = DateUtils.addDays(currDate, i);
				}
				
				// 正常记息
				if (logger.isDebugEnabled()) {
					logger.debug("补计平日利息 起始日期["+currDate.toString()
							+"]");
				}
				interestAccrual.accumulateInterest(item, 1, currDate);
				currDate = DateUtils.addDays(currDate, 1);
				
				// 重置待执行连续补计利息天数
				i = 0;
			}
			
			patchDate = DateUtils.addDays(patchDate, 1);
		}
		
		// 如果连续记息天数不为0，则进行利息补计
		if (i > 0){
			interestAccrual.accumulateInterest(item, i, currDate);
		}
		if(j > 0){
			accumulatePrin(item, j);
		}
		
		return item;
	}
	/**
	 * 根据天数累计本金基数
	 * @param item
	 */
	private void accumulatePrin(S6000AcctInfo item,int j){
		if (blockCodeUtils.getMergedIntAccuralInd(item.getAccount().getBlockCode())){
			for(CcsPlan plan:item.getPlans()){
				// 计划是否过期
				PlanTemplate planTemplate = parameterFacility.loadParameter(plan.getPlanNbr(), PlanTemplate.class);
				//累计本金基数
				if(planTemplate.isAccruPrinSum.equals(Indicator.Y)){
					BigDecimal prin = plan.getCtdPrincipal().add(plan.getPastPrincipal());
					plan.setAccruPrinSum(plan.getAccruPrinSum().add(prin.multiply(new BigDecimal(j))));
				}
			}
		}
		
	}

}

