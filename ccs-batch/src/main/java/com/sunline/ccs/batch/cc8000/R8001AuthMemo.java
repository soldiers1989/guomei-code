package com.sunline.ccs.batch.cc8000;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoO;
import com.sunline.ppy.dictionary.enums.AuthTransStatus;

/**
 * @see 类名：R8001AuthMemo
 * @see 描述： 备份前一天到当天的数据到AuthHst
 *
 * @see 创建日期：   2015-6-24下午2:26:28
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class R8001AuthMemo extends KeyBasedStreamReader<Long, CcsAuthmemoO> {
	@Autowired
	private BatchStatusFacility batchFacility;

	@PersistenceContext
	private EntityManager em;

	@Override
	protected List<Long> loadKeys() {
		QCcsAuthmemoO q = QCcsAuthmemoO.ccsAuthmemoO;
		return new JPAQuery(em).from(q)
				.where(q.logBizDate.gt(batchFacility.getLastBatchDate())
						.and(q.logBizDate.loe(batchFacility.getBatchDate()))
						.and(q.authTxnStatus.ne(AuthTransStatus.P)))
				.list(q.logKv);
	}

	@Override
	protected CcsAuthmemoO loadItemByKey(Long key) {
		return em.find(CcsAuthmemoO.class, key);
	}
}
