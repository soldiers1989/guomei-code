package com.sunline.ccs.batch.cc5000;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsCardCloseReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardCloseReg;


/**
 * @see 类名：R5001AcctClose
 * @see 描述：销卡销户及关闭账户，要求按顺序执行，不能并发
 *
 * @see 创建日期：   2015-6-23下午7:53:57
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class R5001AcctClose extends KeyBasedStreamReader<Long, CcsCardCloseReg> {

	@PersistenceContext
	protected EntityManager em;

	@Override
	protected List<Long> loadKeys() {
		QCcsCardCloseReg q = QCcsCardCloseReg.ccsCardCloseReg;
		
		return new JPAQuery(em)
				.from(q)
				.orderBy(q.requestSeq.asc())		//要求按顺序执行，不能并发
				.list(q.requestSeq);
	}

	@Override
	protected CcsCardCloseReg loadItemByKey(Long key) {
		return em.find(CcsCardCloseReg.class, key);
	}
}
