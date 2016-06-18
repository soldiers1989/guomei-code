package com.sunline.ccs.batch.cc6000;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.ccs.batch.cc6000.common.PenaltyAcru;
/**
 * 特殊规则罚息每日计息
 * @author Lisy
 *
 */
public class P6023PenaltyAccuPerDay implements ItemProcessor<S6000AcctInfo,S6000AcctInfo>{
	private Logger logger = LoggerFactory.getLogger(getClass());	

	@Autowired
	private PenaltyAcru penaltyAcru;
	
 	public S6000AcctInfo process(S6000AcctInfo item) throws Exception {
 		if (logger.isDebugEnabled()) {
			logger.debug("当期罚息累计-按全部未还本金：Org["+item.getAccount().getOrg()
					+"],AcctType["+item.getAccount().getAcctType()
					+"],AcctNo["+item.getAccount().getAcctNbr()
					+"]");
		}
 		penaltyAcru.processUnpaidPrin(item, 1);
		return item;
 	}
}
