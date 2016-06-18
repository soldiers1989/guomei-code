package com.sunline.ccs.batch.cc9200;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsAddress;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsLinkman;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnHst;

/**
 * @see 类名：R9201MasterData
 * @see 描述：卸数数据源
 *
 * @see 创建日期：   2015-6-24下午5:31:15
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class R9201MasterData extends KeyBasedStreamReader<CcsAcctKey, S9201MasterData> {
	@PersistenceContext
	protected EntityManager em;
	
	@Autowired
	private BatchStatusFacility batchFacility;

	@Override
	protected List<CcsAcctKey> loadKeys() {
		QCcsAcct q = QCcsAcct.ccsAcct;
		List<CcsAcctKey> keys = new ArrayList<CcsAcctKey>();

		JPAQuery query = new JPAQuery(em).from(q);
		for (Tuple objs : query.list(q.acctNbr, q.acctType))
			keys.add(new CcsAcctKey(objs.get(q.acctNbr), objs.get(q.acctType)));
		
		return keys;
	}

	@Override
	protected S9201MasterData loadItemByKey(CcsAcctKey key) {
		S9201MasterData info = new S9201MasterData();
		
		//账户主表
		CcsAcct account = em.find(CcsAcct.class, key);
		info.setAccount(account);

		//客户表
		CcsCustomer customer = em.find(CcsCustomer.class, account.getCustId());
		info.setCustomer(customer);
		
		//客户额度表
		CcsCustomerCrlmt custLimitO = em.find(CcsCustomerCrlmt.class, account.getCustLmtId());
		info.setCustLimitO(custLimitO);
		
		//联系信息表(CcsLinkman)
		QCcsLinkman qTmContact = QCcsLinkman.ccsLinkman;
		info.setListContact(new JPAQuery(em).from(qTmContact)
				.where(qTmContact.custId.eq(account.getCustId()))
				.list(qTmContact));
		
		//地址信息表(CcsAddress)
		QCcsAddress qTmAddress = QCcsAddress.ccsAddress;
		info.setListAddress(new JPAQuery(em).from(qTmAddress)
				.where(qTmAddress.custId.eq(account.getCustId()))
				.list(qTmAddress));
		
		//卡片主表(CcsCard)
		QCcsCard qTmCard = QCcsCard.ccsCard;
		info.setListCard(new JPAQuery(em).from(qTmCard)
				.where(qTmCard.acctNbr.eq(account.getAcctNbr()))
				.list(qTmCard));
		
		//入账交易历史表(CcsTxnHst)
		QCcsTxnHst qTmTxnHst = QCcsTxnHst.ccsTxnHst;
		info.setListTxnHst(new JPAQuery(em).from(qTmTxnHst)
				.where(qTmTxnHst.acctNbr.eq(account.getAcctNbr()).and(qTmTxnHst.postDate.after(batchFacility.getLastBatchDate())))
				.list(qTmTxnHst));
		
		//待入账交易表(CcsPostingTmp)
		QCcsPostingTmp qTtTxnPost = QCcsPostingTmp.ccsPostingTmp;
		info.setListTxnPost(new JPAQuery(em).from(qTtTxnPost)
				.where(qTtTxnPost.acctNbr.eq(account.getAcctNbr()).and(qTtTxnPost.postDate.after(batchFacility.getLastBatchDate())))
				.list(qTtTxnPost));

		//信用计划表(CcsPlan)
		QCcsPlan qTmPlan = QCcsPlan.ccsPlan;
		info.setListPlan(new JPAQuery(em).from(qTmPlan)
				.where(qTmPlan.acctNbr.eq(account.getAcctNbr()).and(qTmPlan.acctType.eq(account.getAcctType())))
				.list(qTmPlan));
		return info;
	}

}
