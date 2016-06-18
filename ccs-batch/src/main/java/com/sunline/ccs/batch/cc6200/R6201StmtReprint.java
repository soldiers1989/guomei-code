package com.sunline.ccs.batch.cc6200;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsStmtReprintReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsStmtReprintReg;
/**
 * 
 * @see 类名：R6201StmtReprint
 * @see 描述：CcsStmtReprintReg对象读取
 *
 * @see 创建日期：   2015-6-24下午2:06:35
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class R6201StmtReprint extends KeyBasedStreamReader<Long, CcsStmtReprintReg> {
	
	@Autowired
	private BatchStatusFacility batchFacility;
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	protected List<Long> loadKeys() {
		QCcsStmtReprintReg q = QCcsStmtReprintReg.ccsStmtReprintReg;
		return new JPAQuery(em).from(q)
			//上次跑批日期(不含) 至 跑批日期(含)
			.where(q.txnDate.after(batchFacility.getLastBatchDate()).and(q.txnDate.loe(batchFacility.getBatchDate())))
			.orderBy(q.reprintSeq.asc())
			.list(q.reprintSeq);
		
	}

	@Override
	protected CcsStmtReprintReg loadItemByKey(Long key) {
		return em.find(CcsStmtReprintReg.class, key);
	}

}
