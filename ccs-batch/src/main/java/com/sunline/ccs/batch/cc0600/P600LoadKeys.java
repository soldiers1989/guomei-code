package com.sunline.ccs.batch.cc0600;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnPost;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnPost;

/**
 * @see 类名：P600LoadKeys
 * @see 描述：TODO 中文描述
 *
 * @see 创建日期： 2015年6月18日下午4:25:13
 * @author songyanchao
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P600LoadKeys extends KeyBasedStreamReader<Long, CcsTxnPost> {

    @Autowired
    private BatchStatusFacility batchStatusFacility;

    @PersistenceContext
    private EntityManager em;

    @Override
    protected List<Long> loadKeys() {
		QCcsTxnPost qCcsTxnPost = QCcsTxnPost.ccsTxnPost;
		// 返回交易日期上个批量日结束到这个批量日结束，所有金融交易
		JPAQuery query = new JPAQuery(em);
		BooleanExpression exp = qCcsTxnPost.batchDate.gt(batchStatusFacility.getLastBatchDate())
				.and(qCcsTxnPost.batchDate.loe(batchStatusFacility.getBatchDate()));
		List<Long> listTranId = query.from(qCcsTxnPost).where(exp).list(qCcsTxnPost.txnId);
	
		return listTranId;
    }

    @Override
    protected CcsTxnPost loadItemByKey(Long key) {
    	return em.find(CcsTxnPost.class, key);
    }
}
