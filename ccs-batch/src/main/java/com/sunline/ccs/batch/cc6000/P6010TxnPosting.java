package com.sunline.ccs.batch.cc6000;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.ccs.batch.cc6000.common.TransactionPost;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.acm.service.sdk.BatchStatusFacility;


/**
 * @see 类名：P6010TxnPosting
 * @see 描述：入账处理本账户内所有外送交易，及有交易费用的交易进行交易费用收取
 *
 * @see 创建日期：   2015-6-24上午10:05:17
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class P6010TxnPosting implements ItemProcessor<S6000AcctInfo, S6000AcctInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private TransactionPost transactionPost;
	@Autowired
    private BatchStatusFacility batchFacility;
	
	
	@Override
	public S6000AcctInfo process(S6000AcctInfo item) {
		if (logger.isDebugEnabled()) {
			logger.debug("交易入账处理：Org["+item.getAccount().getOrg()
					+"],AcctType["+item.getAccount().getAcctType()
					+"],AcctNo["+item.getAccount().getAcctNbr()
					+"],TxnPost.size["+item.getTxnPosts().size()
					+"]");
		}

		// 循环处理所有当日入账交易数据(TT_TXN_POST)
		List<CcsPostingTmp> addTxnPosts = new ArrayList<CcsPostingTmp>();
		for (CcsPostingTmp txnPost : item.getTxnPosts()) {
			addTxnPosts.addAll(transactionPost.posting(item, txnPost, batchFacility.getBatchDate()));
		}
		item.getTxnPosts().addAll(addTxnPosts);
		return item;
	}
}
