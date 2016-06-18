package com.sunline.ccs.batch.cc6000;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.ppy.dictionary.enums.SmsInd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ccs.batch.cc6000.common.TransactionGenerator;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.acm.service.sdk.BatchStatusFacility;


/** 
 * @see 类名：P6032GenerateMessageFee
 * @see 描述：短信费/加急费支持
 *
 * @see 创建日期：   2015年6月25日 下午2:15:58
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class P6032GenerateMessageFee implements ItemProcessor<S6000AcctInfo, S6000AcctInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private TransactionGenerator generatorTransaction;
	@Autowired
    private BatchStatusFacility batchFacility;
	
	@Override
	public S6000AcctInfo process(S6000AcctInfo item) throws Exception {
		
		try {
			
			ProductCredit productCr = parameterFacility.loadParameter(item.getAccount().getProductCd(), ProductCredit.class);
			
			//账单日 + 个性化动账短信发送开启 + 个性化动账短信发送阈值<默认阀值
			if(batchFacility.shouldProcess(item.getAccount().getNextStmtDate())
					&& item.getAccount().getSmsInd()==SmsInd.C
					&& item.getAccount().getUserSmsAmt().compareTo(productCr.defaultSmsAmt) < 0){
				
				generatorTransaction.generateSmsFee(item, productCr, batchFacility.getBatchDate());
			}
			return item;
			
		} catch (Exception e) {
			logger.error("短信费批量作业异常,账号{}", item.getAccount().getAcctNbr());
			throw e;
		}
	}
	
	
}
