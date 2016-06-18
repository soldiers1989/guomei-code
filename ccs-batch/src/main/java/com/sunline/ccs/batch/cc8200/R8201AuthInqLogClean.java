package com.sunline.ccs.batch.cc8200;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoInqLog;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoInqLog;

/**
 * @see 类名：R8201AuthInqLogClean
 * @see 描述：根据条件删除CcsAuthmemoInqLog中的交易
 *
 * @see 创建日期：   2015-6-24下午2:27:54
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class R8201AuthInqLogClean extends KeyBasedStreamReader<Long, CcsAuthmemoInqLog> {
	@PersistenceContext
	private EntityManager em;

	@Autowired
	private BatchStatusFacility batchFacility;

	@Override
	protected List<Long> loadKeys() {
		QCcsAuthmemoInqLog q = QCcsAuthmemoInqLog.ccsAuthmemoInqLog;
		return new JPAQuery(em)
				.from(q)
				.where(q.logBizDate.lt(batchFacility.getBatchDate()))
				.list(q.logKv);
	}

	@Override
	protected CcsAuthmemoInqLog loadItemByKey(Long key) {
		return em.find(CcsAuthmemoInqLog.class, key);
	}
}
