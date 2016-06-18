package com.sunline.ccs.batch.cc6000;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.ccs.batch.cc6000.common.AgeController;
import com.sunline.acm.service.sdk.BatchStatusFacility;


/** 
 * @see 类名：P6043ComputeDue
 * @see 描述：账单日最小还款额处理
 *
 * @see 创建日期：   2015年6月25日 下午2:22:51
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P6043ComputeDue implements ItemProcessor<S6000AcctInfo, S6000AcctInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private AgeController ageController;
	@Autowired
    private BatchStatusFacility batchFacility;
	
	@Override
	public S6000AcctInfo process(S6000AcctInfo item) {
		if (logger.isDebugEnabled()) {
			logger.debug("账单日最小还款额处理：Org["+item.getAccount().getOrg()
					+"],AcctNo["+item.getAccount().getAcctNbr()
					+"],AcctType["+item.getAccount().getAcctType()
					+"],BatchDate["+batchFacility.getBatchDate()
					+"],NextStmtDate["+item.getAccount().getNextStmtDate()
					+"]");
		}
		Date batchDate = batchFacility.getBatchDate();
		
		// 判断当天是否账单日
		if (batchFacility.shouldProcess(item.getAccount().getNextStmtDate())){
			logger.info("批量日期为账单日期,开始处理最小还款额");
			ageController.getMinDue(item, batchDate);
		}
		
		logger.info("账单日最小还款额处理Process finished! AccountNo:[" + item.getAccount().getAcctNbr() + "] AccountType:[" 
				+ item.getAccount().getAcctType() + "] ");

		return item;
	}

}
