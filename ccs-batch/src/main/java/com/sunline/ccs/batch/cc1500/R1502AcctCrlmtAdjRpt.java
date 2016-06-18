package com.sunline.ccs.batch.cc1500;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.ccs.infrastructure.shared.model.CcsAcctCrlmtAdjLog;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcctCrlmtAdjLog;
import com.sunline.ccs.service.api.Constants;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.mysema.query.jpa.impl.JPAQuery;

/**
 * @see 类名：R8502AcctCrlmtAdjRpt
 * @see 描述： 额度调整记录
 *
 * @see 创建日期： 2015-11-09
 * @author mengxiang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class R1502AcctCrlmtAdjRpt extends KeyBasedStreamReader<Long, CcsAcctCrlmtAdjLog> {

	@PersistenceContext
	protected EntityManager em;

	@Autowired
	private BatchStatusFacility batchStatusFacility;

	@Override
	protected List<Long> loadKeys() {
		QCcsAcctCrlmtAdjLog q = QCcsAcctCrlmtAdjLog.ccsAcctCrlmtAdjLog;
		// 获取所有调整记录
		return new JPAQuery(em).from(q)
				.where(q.procDate.loe(batchStatusFacility.getBatchDate())).list(q.opSeq);
	}

	@Override
	protected CcsAcctCrlmtAdjLog loadItemByKey(Long key) {
		return em.find(CcsAcctCrlmtAdjLog.class, key);
	}
}
