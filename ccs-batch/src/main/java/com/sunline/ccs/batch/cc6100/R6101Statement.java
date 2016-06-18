package com.sunline.ccs.batch.cc6100;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsStatement;
import com.sunline.ccs.infrastructure.shared.model.CcsStatementKey;
import com.sunline.ccs.infrastructure.shared.model.QCcsStatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnUnstatement;

/**
 * @see 类名：R6101Statement
 * @see 描述：未出账单信息reader
 *          前提：账单汇总信息表记录已经更新
 *          读取所有账单日等于当前批量日期的账单汇总信息记录(按账户、账户类型排序)，同时从未出账单交易表中取出账单日期为当前批量日期的记录
 *
 * @see 创建日期：   2015-6-24上午10:41:23
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class R6101Statement extends KeyBasedStreamReader<CcsStatementKey, U6101Statement> {
	@PersistenceContext
	private EntityManager em;

	@Autowired
	private BatchStatusFacility batchStatusFacility;

	@Override
	protected List<CcsStatementKey> loadKeys() {
		//只要取两个字段，stmtDate是过滤条件
		QCcsStatement q = QCcsStatement.ccsStatement;
		Date batchDate = batchStatusFacility.getBatchDate();
		List<Tuple> result = new JPAQuery(em).from(q)
			//TODO确定跳批问题
			.where(q.stmtDate.eq(batchDate))
			.orderBy(q.acctNbr.asc(), q.acctType.asc())
			.list(q.acctNbr, q.acctType);

		List<CcsStatementKey> keys = new ArrayList<CcsStatementKey>();
		for (Tuple objs : result)
			keys.add(new CcsStatementKey(objs.get(q.acctNbr), objs.get(q.acctType), batchDate));
		return keys;
	}

	@Override
	protected U6101Statement loadItemByKey(CcsStatementKey key) {
		U6101Statement info = new U6101Statement();
		CcsStatement hst = em.find(CcsStatement.class, key);
		info.setStmtHst(hst);
		
		QCcsTxnUnstatement q = QCcsTxnUnstatement.ccsTxnUnstatement;
		info.setTxnUnstmts(new JPAQuery(em)
			.from(q)
			.where(
				q.acctNbr.eq(hst.getAcctNbr())
				.and(q.acctType.eq(hst.getAcctType()))
				.and(q.stmtDate.loe(hst.getStmtDate()))
				)
			.orderBy(q.txnSeq.asc())
			.list(q));
		return info;
	}

}
