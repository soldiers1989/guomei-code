package com.sunline.ccs.batch.cca400;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.batch.common.SettleRecordUtil;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsSettlePlatformRec;
import com.sunline.ccs.infrastructure.shared.model.QCcsRepayHst;
import com.sunline.ccs.param.def.enums.SettleTxnDirection;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.BucketType;

public class RA401SettleReplaceFee  extends KeyBasedStreamReader<CcsAcctKey, CcsAcctKey> {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	private QCcsRepayHst qRepay = QCcsRepayHst.ccsRepayHst;
	
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	@PersistenceContext
    private EntityManager em;
	@Autowired
	private BatchStatusFacility batchFacility;
	@Autowired
	private SettleRecordUtil settleUtil;
	
	@Override
	protected List<CcsAcctKey> loadKeys() {
		log.info("结算记录--代收费---开始");
		List<CcsAcctKey> keyList = new ArrayList<CcsAcctKey>();
		
		List<Tuple> repayAcctList = new JPAQuery(em).from(qRepay)
				.where(qRepay.batchDate.eq(batchFacility.getBatchDate())
						.and(qRepay.bnpType.in(settleUtil.SETTLE_BNP_TYPES))
						)
				.distinct()
				.list(qRepay.acctNbr, qRepay.acctType);
		for(Tuple a : repayAcctList){
			keyList.add(new CcsAcctKey(a.get(qRepay.acctNbr), a.get(qRepay.acctType)));
		}
		return keyList;
	}

	@Override
	protected CcsAcctKey loadItemByKey(CcsAcctKey acctKey) {
		log.info("Settlement Item: ReplaceFee,Acct[{}][{}]", acctKey.getAcctNbr(), acctKey.getAcctType());
		
		List<Tuple> repayList = new JPAQuery(em).from(qRepay)
				.where(qRepay.acctNbr.eq(acctKey.getAcctNbr())
						.and(qRepay.acctType.eq(acctKey.getAcctType()))
						.and(qRepay.batchDate.eq(batchFacility.getBatchDate()))
						.and(qRepay.bnpType.in(settleUtil.SETTLE_BNP_TYPES)
							)
						)
				.list(qRepay.bnpType, qRepay.repayAmt);
		
		log.info("repay List size[{}]", repayList==null?0:repayList.size());
		if(repayList == null || repayList.size()<=0){
			return null;
		}
		
		Map<BucketType, CcsSettlePlatformRec> recMap = new HashMap<BucketType, CcsSettlePlatformRec>();
		
		// 还款分配 TO 结算记录
		for(Tuple t : repayList){
			BucketType bucketType = t.get(qRepay.bnpType).getBucketType();
			BigDecimal repayAmt = t.get(qRepay.repayAmt);
			
			CcsSettlePlatformRec rec = recMap.get(bucketType);
			if(rec == null){
				
				rec = settleUtil.initSettleReplaceFeeRec(acctKey.getAcctNbr(), acctKey.getAcctType(), 
						SettleTxnDirection.ToCoop, bucketType, repayAmt);
				
				if(rec != null) recMap.put(bucketType, rec);
				
			}else{
				rec.setSettleAmt(rec.getSettleAmt().add(repayAmt));
			}
			
		}
		
		// 保存
		for(CcsSettlePlatformRec rec : recMap.values()){
			em.merge(rec);
		}
		return acctKey;
	}

}
