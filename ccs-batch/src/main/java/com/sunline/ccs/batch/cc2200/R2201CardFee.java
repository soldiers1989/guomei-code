package com.sunline.ccs.batch.cc2200;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;

/**
 * 
 * @see 类名：R2201CardFee
 * @see 描述： 符合条件的年费卡对象
 *
 * @see 创建日期：   2015-6-23下午7:31:12
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class R2201CardFee extends KeyBasedStreamReader<String, CcsCard> {
	/**
	 * 批量日期
	 */
	@Autowired
	private BatchStatusFacility batchStatusFacility; 
	
	@PersistenceContext
	protected EntityManager em;

	@Override
	protected List<String> loadKeys() {
		QCcsCard q = QCcsCard.ccsCard;
		return new JPAQuery(em).from(q)
			//过滤条件写在这里
			.where(q.nextCardFeeDate.loe(batchStatusFacility.getBatchDate()))
			.list(q.logicCardNbr);
	}

	@Override
	protected CcsCard loadItemByKey(String key) {
		return em.find(CcsCard.class, key);
	}

}
