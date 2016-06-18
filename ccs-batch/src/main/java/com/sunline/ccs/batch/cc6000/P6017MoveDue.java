package com.sunline.ccs.batch.cc6000;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.ccs.batch.cc6000.common.AgeController;
import com.sunline.acm.service.sdk.BatchStatusFacility;


/** 
 * @see 类名：P6017MoveDue
 * @see 描述：最小还款额账单日移位
 *
 * @see 创建日期：   2015年6月25日 上午10:52:03
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P6017MoveDue implements ItemProcessor<S6000AcctInfo, S6000AcctInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private AgeController ageController;
	@Autowired
	private BatchStatusFacility batchStatusFacility;

	@Override
	public S6000AcctInfo process(S6000AcctInfo item) throws Exception {
		Date batchDate = batchStatusFacility.getBatchDate();
		logger.info("最小还款额账单日移位处理Process started! AccountNo:[" + item.getAccount().getAcctNbr() + "] AccountType:[" + item.getAccount().getAcctType()
				+ "] ");

		Date nextStmtDate = item.getAccount().getNextStmtDate();
		// 批量日期
		logger.info("当前批量 日期:[" + batchDate.toString() + "]");
		logger.info("下一账单 日期:[" + nextStmtDate.toString() + "]");

		// 判断是否账单日
		if (batchStatusFacility.shouldProcess(nextStmtDate)) {
			ageController.moveDueAtStmtDay(item.getAccount());
		}
		return item;
	}

}
