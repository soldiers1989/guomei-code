package com.sunline.ccs.batch.front;

import java.math.RoundingMode;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ppy.dictionary.enums.LoanUsage;

/**
 * 将order表数据转换为批量代扣接口
 * @author zhangqiang
 *
 */
public class P9000GeneratePaymentFile implements ItemProcessor<CcsOrder, S9000PaymentItem> {
	
	private static final Logger logger = LoggerFactory.getLogger(P9000GeneratePaymentFile.class);
	
	@Override
	public S9000PaymentItem process(CcsOrder order) throws Exception {
		// 理赔订单汇总,所有子订单不进行扣款
		if(order.getLoanUsage() == LoanUsage.C && order.getOriOrderId() != null)
			return null;
		
		// 溢缴款转出，会出代付文件
		if (LoanUsage.D.equals(order.getLoanUsage())) {
			return null;
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("生产代扣文件：Org["+order.getOrg()
					+"],orderId["+order.getOrderId()
					+"]");
		}
		
		S9000PaymentItem item = new S9000PaymentItem();
		item.channelSeq = order.getOrderId();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		item.channelDateStr = format.format(order.getBusinessDate());
		item.bankId = order.getOpenBankId();
		item.cardType = order.getCardType();
		item.cardNo = order.getCardNo();
		item.name = order.getUsrName();
		item.idType = order.getCertType();
		item.idNo = order.getCertId();
		item.txnAmt = order.getTxnAmt().setScale(2, RoundingMode.HALF_UP);
		item.purpose = order.getPurpose();
		item.privateField = order.getPriv1();
		item.province = order.getState();
		item.city = order.getCity();
		return item;
	}

}
