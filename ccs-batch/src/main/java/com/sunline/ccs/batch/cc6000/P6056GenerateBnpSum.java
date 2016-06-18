package com.sunline.ccs.batch.cc6000;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.cc6000.common.BnpManager;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.enums.BnpPeriod;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.exchange.PlanBnpSumItem;


/** 
 * @see 类名：P6056GenerateBnpSum
 * @see 描述：生成分户账汇总信息
 *
 * @see 创建日期：   2015年6月25日 下午2:29:36
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class P6056GenerateBnpSum implements ItemProcessor<S6000AcctInfo, S6000AcctInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private BnpManager bnpManager;
	@Autowired
    private BatchStatusFacility batchFacility;

	@Override
	public S6000AcctInfo process(S6000AcctInfo item) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("生成分户账汇总信息：Org["+item.getAccount().getOrg()
					+"],AcctNo["+item.getAccount().getAcctNbr()
					+"],AcctType["+item.getAccount().getAcctType()
					+"],Plan.size["+item.getPlans().size()
					+"]");
		}
		// 查找系统内部交易类型对照表
		SysTxnCdMapping sysTxnCdOut = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S30), SysTxnCdMapping.class);

		for (CcsPlan plan : item.getPlans()) {
			for (BucketType bucketType : BucketType.values()) {
				// 获取Plan的对应余额成份
				BigDecimal bucketBal = this.sumPlanBalance(plan, bucketType);
				// 当前为零则不生成
				if (bucketBal.compareTo(BigDecimal.ZERO) == 0) continue;

				PlanBnpSumItem planBnpSum = new PlanBnpSumItem();
				planBnpSum.org = item.getAccount().getOrg();
				planBnpSum.acctNo = item.getAccount().getAcctNbr();
				planBnpSum.acctType = item.getAccount().getAcctType();
				planBnpSum.currCd = item.getAccount().getCurrency();
				planBnpSum.ageCd = item.getAccount().getAgeCodeGl();
				planBnpSum.txnCode = sysTxnCdOut.txnCd;
				planBnpSum.postDate = batchFacility.getBatchDate();
				planBnpSum.owningBranch = item.getAccount().getOwningBranch();

				planBnpSum.postAmount = bucketBal.abs();
				planBnpSum.planNbr = plan.getPlanNbr();
				planBnpSum.bucketType = bucketType;
				
				item.getPlanBnpSums().add(planBnpSum);
			}
		}

		return item;
	}
	
	/**
	 * 汇总当期和往期BNP金额
	 * @param plan
	 * @param bucketType
	 * @return
	 */
	private BigDecimal sumPlanBalance(CcsPlan plan, BucketType bucketType){
		BigDecimal balance = BigDecimal.ZERO;
		balance = balance
				.add(bnpManager.getBucketAmount(plan, bucketType, BnpPeriod.PAST))
				.add(bnpManager.getBucketAmount(plan, bucketType, BnpPeriod.CTD));
		return balance;
	}
}
