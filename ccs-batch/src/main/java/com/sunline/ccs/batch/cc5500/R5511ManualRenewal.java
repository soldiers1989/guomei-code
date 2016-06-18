package com.sunline.ccs.batch.cc5500;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsCardExpList;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardExpList;
/**
 * @see 类名：R5511ManualRenewal
 * @see 描述：TODO 中文描述
 *
 * @see 创建日期：   2015-6-23下午7:57:47
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class R5511ManualRenewal extends KeyBasedStreamReader<Long, CcsCardExpList> {
	@PersistenceContext
	protected EntityManager em;

	@Autowired
	private BatchStatusFacility facility;
	@Override
	protected List<Long> loadKeys() {
		QCcsCardExpList q = QCcsCardExpList.ccsCardExpList;
		return new JPAQuery(em).from(q).where(q.renewRejectCd.isNotNull()
				.and(q.procDate.gt(facility.getLastBatchDate()))
				.and(q.procDate.loe(facility.getBatchDate()))).list(q.listId);
	}

	@Override
	protected CcsCardExpList loadItemByKey(Long key) {
		return em.find(CcsCardExpList.class, key);
	}
	
}
