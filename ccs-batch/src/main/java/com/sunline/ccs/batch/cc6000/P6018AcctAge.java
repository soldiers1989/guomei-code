package com.sunline.ccs.batch.cc6000;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ccs.batch.cc6000.common.AgeController;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.DelqDayInd;
import com.sunline.acm.service.sdk.BatchStatusFacility;


/** 
 * @see 类名：P6018AcctAge
 * @see 描述：更新账龄处理
 *
 * @see 创建日期：   2015年6月25日 上午10:52:15
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P6018AcctAge implements ItemProcessor<S6000AcctInfo, S6000AcctInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private AgeController ageController;
	@Autowired
    private BatchStatusFacility batchFacility;

	
	@Override
	public S6000AcctInfo process(S6000AcctInfo item) {
		if (logger.isDebugEnabled()) {
			logger.debug("更新账龄处理：Org["+item.getAccount().getOrg()
					+"],AcctType["+item.getAccount().getAcctType()
					+"],AcctNo["+item.getAccount().getAcctNbr()
					+"],BatchDate["+batchFacility.getBatchDate()
					+"]");
		}
		Date batchDate = batchFacility.getBatchDate();

		// 账户属性参数
		ProductCredit productCredit = parameterFacility.loadParameter(item.getAccount().getProductCd(), ProductCredit.class);
		AccountAttribute acctAttr = parameterFacility.loadParameter(productCredit.accountAttributeId, AccountAttribute.class);
		logger.info("账龄提升参数:[" + acctAttr.delqDayInd + "]"+"到期还款日:["+item.getAccount().getPmtDueDate()+
				"],账单日:["+item.getAccount().getNextStmtDate()+"],宽限日:["+item.getAccount().getGraceDate()+"]");
		
		// 根据账户属性参数设定的账龄提升日期进行计算
		if (
				// 溢缴款每日检查账龄
				item.getAccount().getAgeCode().equals("C")
				// 到期还款日提升
				|| (item.getAccount().getPmtDueDate() != null && acctAttr.delqDayInd == DelqDayInd.P && batchFacility.shouldProcess(item.getAccount().getPmtDueDate()))
				// 宽限日提升
				|| (item.getAccount().getGraceDate() !=null && acctAttr.delqDayInd == DelqDayInd.G && batchFacility.shouldProcess(item.getAccount().getGraceDate()))
				// 账单日提升
				|| (item.getAccount().getNextStmtDate() != null && acctAttr.delqDayInd == DelqDayInd.C && batchFacility.shouldProcess(item.getAccount().getNextStmtDate()))
				)
		{
			//	更新账龄
			logger.info("开始处理更新账龄");
			ageController.setAgeCode(item.getAccount(), batchDate,true);					
		}
		
		if (acctAttr.delqDayInd != DelqDayInd.P 
				&& acctAttr.delqDayInd != DelqDayInd.G
				&& acctAttr.delqDayInd != DelqDayInd.C)
		{
			throw new IllegalArgumentException("账龄提升日期类型不正确");
		}
				
		logger.info("更新账龄处理Process finished! AccountNo:[" + item.getAccount().getAcctNbr() + "] AccountType:[" 
				+ item.getAccount().getAcctType() + "] ");

		return item;
	}

}
