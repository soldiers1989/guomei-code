package com.sunline.ccs.batch.cc7000;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnHst;

/**
 * @see 类名：R7000TxnProduce
 * @see 描述： CcsTxnHst对象读取, 条件是查询当天入账的交易
 *
 * @see 创建日期：   2015-6-24下午2:08:11
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class R7000TxnProduce extends KeyBasedStreamReader<Long, CcsTxnHst> {

	@PersistenceContext
	private EntityManager em;

	/**
	 * 批量日期
	 */
	@Autowired
	private BatchStatusFacility batchStatusFacility; 
	
	@Override
	protected List<Long> loadKeys() {
		QCcsTxnHst q = QCcsTxnHst.ccsTxnHst;
		return new JPAQuery(em).from(q)
			//过滤条件写在这里
			.where(q.postDate.eq(batchStatusFacility.getBatchDate()))
			.orderBy(q.txnSeq.asc())
			.list(q.txnSeq);
	}

	@Override
	protected CcsTxnHst loadItemByKey(Long key) {
		return em.find(CcsTxnHst.class, key);
	}
}
