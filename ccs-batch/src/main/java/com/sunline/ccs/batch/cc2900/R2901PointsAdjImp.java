package com.sunline.ccs.batch.cc2900;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsPointsReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsPointsReg;

/**
 * @see 类名：R2901PointsAdjImp
 * @see 描述：CcsPointsReg
 *
 * @see 创建日期：   2015-6-23下午7:31:42
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class R2901PointsAdjImp extends KeyBasedStreamReader<Long, CcsPointsReg> {
	
	@Autowired
	private BatchStatusFacility batchFacility;
	
	@PersistenceContext
	private EntityManager em;

	@Override
	protected List<Long> loadKeys() {
		QCcsPointsReg q = QCcsPointsReg.ccsPointsReg;
		return new JPAQuery(em).from(q)
				//上次跑批日期(不含) 至 跑批日期(含)
				.where(q.txnDate.after(batchFacility.getLastBatchDate()).and(q.txnDate.loe(batchFacility.getBatchDate())))
				.orderBy(q.reprintSeq.asc())
				.list(q.reprintSeq);
	}

	@Override
	protected CcsPointsReg loadItemByKey(Long key) {
		return em.find(CcsPointsReg.class, key);
	}

}
