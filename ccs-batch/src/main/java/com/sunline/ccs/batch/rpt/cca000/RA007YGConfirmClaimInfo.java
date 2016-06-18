package com.sunline.ccs.batch.rpt.cca000;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.batch.rpt.cca000.items.YGConfirmClaimInfoItem;
import com.sunline.ccs.infrastructure.shared.model.CcsSettleClaim;
import com.sunline.ccs.infrastructure.shared.model.QCcsSettleClaim;
import com.sunline.ppy.dictionary.enums.Indicator;

public class RA007YGConfirmClaimInfo extends KeyBasedStreamReader<Long,YGConfirmClaimInfoItem> {
	@PersistenceContext
    private EntityManager em;
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	private QCcsSettleClaim qClaim = QCcsSettleClaim.ccsSettleClaim;
	
	@Override
	protected List<Long> loadKeys() {
		Date batchDate = batchStatusFacility.getBatchDate();
		BooleanExpression exp = 
				qClaim.settleSucDate.eq(batchDate ).and(qClaim.settleFlag.eq(Indicator.Y));
		return new JPAQuery(em).from(qClaim).where(exp).list(qClaim.loanId);
	}

	@Override
	protected YGConfirmClaimInfoItem loadItemByKey(Long key) {
		
		CcsSettleClaim info = em.find(CcsSettleClaim.class, key);
		
		YGConfirmClaimInfoItem item = new YGConfirmClaimInfoItem();
		
		item.putOutNo = info.getDueBillNo();
		item.lpamt = info.getSettleAmt().setScale(2, RoundingMode.HALF_UP);
		item.actlpamt = info.getSettleAmt().setScale(2, RoundingMode.HALF_UP);
		item.balance = new BigDecimal("0.00");
		item.occurDate = info.getSettleDate();
		
		return item;
	}

}
