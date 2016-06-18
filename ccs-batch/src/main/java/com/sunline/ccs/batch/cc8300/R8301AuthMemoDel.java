package com.sunline.ccs.batch.cc8300;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoDelTmp;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoDelTmp;
/**
 * @see 类名：R8301AuthMemoDel
 * @see 描述：TODO 中文描述
 *
 * @see 创建日期：   2015-6-24下午2:28:42
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class R8301AuthMemoDel extends KeyBasedStreamReader<Long, CcsAuthmemoDelTmp> {
	@PersistenceContext
	private EntityManager em;

	@Override
	protected List<Long> loadKeys() {
		QCcsAuthmemoDelTmp q = QCcsAuthmemoDelTmp.ccsAuthmemoDelTmp;
		return new JPAQuery(em).from(q).list(q.logKv);
	}

	@Override
	protected CcsAuthmemoDelTmp loadItemByKey(Long key) {
		return em.find(CcsAuthmemoDelTmp.class, key);
	}

}
