package com.sunline.ccs.batch.cc8100;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoO;
import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ppy.dictionary.enums.AuthTransType;

/**
 * @see 类名：R8101AuthMemoClean
 * @see 描述：根据条件删除unmatchO中的反向交易
 *
 * @see 创建日期：   2015-6-24下午2:27:05
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class R8101AuthMemoClean extends KeyBasedStreamReader<Long, CcsAuthmemoO> {

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private BatchStatusFacility batchFacility;

	@Override
	protected List<Long> loadKeys() {
		QCcsAuthmemoO q = QCcsAuthmemoO.ccsAuthmemoO;
		return new JPAQuery(em).from(q).where(
								((q.authTxnStatus.notIn(AuthTransStatus.N, AuthTransStatus.O, AuthTransStatus.P)
									.or(q.txnDirection.in(AuthTransDirection.Reversal, AuthTransDirection.Revocation , AuthTransDirection.RevocationReversal)))
									.and(q.lastReversalDate.loe(batchFacility.getBatchDate()).or(q.lastReversalDate.isNull()))
									.and(q.logBizDate.loe(batchFacility.getBatchDate()))
								)
								.or(
									q.finalAction.ne(AuthAction.A)
									.and(q.b039RtnCode.notIn("00","0000", "11")).and(q.authTxnStatus.ne( AuthTransStatus.P))
								)
								.or(q.txnType.in(AuthTransType.ContractBuildUp,AuthTransType.ContractTermination,AuthTransType.AcctVerfication))
							)
						.list(q.logKv);
	}

	@Override
	protected CcsAuthmemoO loadItemByKey(Long key) {
		return em.find(CcsAuthmemoO.class, key);
	}
}
