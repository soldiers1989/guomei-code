package com.sunline.ccs.batch.front;

import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;

/**
 * 将溢缴款转出订单转换为批量代付接口
 * @author MengXiang
 *
 */
public class P9100GenerateDisburseFile implements ItemProcessor<CcsOrder, S9100DisburseItem> {
	
	private static final Logger logger = LoggerFactory.getLogger(P9100GenerateDisburseFile.class);
	
	@Override
	public S9100DisburseItem process(CcsOrder order) throws Exception {
		
		if (logger.isDebugEnabled()) {
			logger.debug("生产代付文件：Org["+order.getOrg()
					+"],orderId["+order.getOrderId()
					+"]");
		}
		
		S9100DisburseItem item = new S9100DisburseItem();
		
		item.channelSeq = order.getOrderId();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		item.channelDateStr = format.format(order.getBusinessDate());
		item.cardNo = order.getCardNo();
		item.name = order.getUsrName();
		item.openBank = order.getOpenBank();
		item.province = order.getState();
		item.city = order.getCity();
		
		// 开户支行如果为空，则取开户行
		item.subBank = StringUtils.isNotBlank(order.getSubBank()) ? order.getSubBank() : order.getOpenBank();
		item.txnAmt = order.getTxnAmt().setScale(2, RoundingMode.HALF_UP);
		item.purpose = order.getPurpose();
		item.flag = order.getFlag();
		
		return item;
	}

}
