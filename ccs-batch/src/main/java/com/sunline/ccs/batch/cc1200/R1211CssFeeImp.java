package com.sunline.ccs.batch.cc1200;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.ccs.infrastructure.shared.model.QCcsCssfeeReg;
import com.sunline.ccs.infrastructure.shared.model.CcsCssfeeReg;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.mysema.query.jpa.impl.JPAQuery;


/**
 * @see 类名：R1211CssFeeImp
 * @see 描述：CssFeeImp对象读取
 *
 * @see 创建日期：   2015-6-23下午7:21:38
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class R1211CssFeeImp extends KeyBasedStreamReader<Long, CcsCssfeeReg> {
	@Autowired
	private BatchStatusFacility batchFacility;

	@PersistenceContext
	private EntityManager em;

	@Override
	protected List<Long> loadKeys() {
		QCcsCssfeeReg q = QCcsCssfeeReg.ccsCssfeeReg;
		// 处理上次批量时间之后，到本次批量时间之间产生的客服费注册记录（含本天）
		return new JPAQuery(em)
				.from(q)
				.where(q.txnDate.after(batchFacility.getLastBatchDate()).and(q.txnDate.loe(batchFacility.getBatchDate())))
				.orderBy(q.cssfeeTxnSeq.asc())
				.list(q.cssfeeTxnSeq);
	}

	@Override
	protected CcsCssfeeReg loadItemByKey(Long key) {
		return em.find(CcsCssfeeReg.class, key);
	}
}
