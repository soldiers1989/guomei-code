package com.sunline.ccs.batch.cca400;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import com.sunline.ccs.batch.rpt.common.RptBatchUtil;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnHst;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.enums.SettleFeeType;
import com.sunline.ccs.param.def.enums.SettleTxnDirection;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;

public class RA402SettlePremiumAmt extends KeyBasedStreamReader<Long, Long> {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	private QCcsTxnHst qTxn = QCcsTxnHst.ccsTxnHst;
	
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	@PersistenceContext
    private EntityManager em;
	@Autowired
	private BatchStatusFacility batchFacility;
	@Autowired
	private RptBatchUtil rptBatchUtil;
	@Autowired
	private SettleRecordUtil settleUtil;
	

	@Override
	protected List<Long> loadKeys() {
		log.info("结算记录--趸交费---开始");
		Date batchDate = batchFacility.getBatchDate();
		rptBatchUtil.setCurrOrgNoToContext();
		
		List<String> txnCodeList = mappingTxnCd(SysTxnCd.S82, SysTxnCd.D11);
		if(txnCodeList == null || txnCodeList.size() <= 0){
			return new ArrayList<Long>();
		}
		List<Long> txnSeqList = new JPAQuery(em).from(qTxn)
				.where(qTxn.postDate.eq(batchDate).and(qTxn.txnCode.in(txnCodeList )))
				.list(qTxn.txnSeq);
		return txnSeqList;
	}

	private List<String> mappingTxnCd(SysTxnCd... stcs) {
		SysTxnCd[] sysTxnCdList = stcs.clone();
		List<String> txnCdList = new ArrayList<String>();
		
		for(int i=0; i<sysTxnCdList.length; i++){
			SysTxnCdMapping mp = unifiedParameterFacility.retrieveParameterObject(sysTxnCdList[i].name(), SysTxnCdMapping.class);
			if(mp != null){
				log.info("交易码[{}]", mp.txnCd);
				txnCdList.add(mp.txnCd);
			}else{
				log.info("SysTxnCdMapping参数[{}]不存在", sysTxnCdList[i].name());
			}
		}
		return txnCdList;
	}

	@Override
	protected Long loadItemByKey(Long key) {

		log.info("Settlement Item: PremiumFee,txnSeq[{}]", key);
		rptBatchUtil.setCurrOrgNoToContext();
		
		SysTxnCdMapping premiumDeductTxnCdMp = unifiedParameterFacility.retrieveParameterObject(SysTxnCd.S82.name(), SysTxnCdMapping.class);
		SysTxnCdMapping premiumRefundTxnCdMp = unifiedParameterFacility.retrieveParameterObject(SysTxnCd.D11.name(), SysTxnCdMapping.class);
		
		Tuple txn = new JPAQuery(em).from(qTxn)
				.where(qTxn.txnSeq.eq(key))
				.singleResult(qTxn.acctNbr, qTxn.acctType, qTxn.txnCode, qTxn.postAmt, qTxn.postDate);
		
		SettleTxnDirection txnDirection = null;
		if(premiumDeductTxnCdMp != null && premiumDeductTxnCdMp.txnCd.equals(txn.get(qTxn.txnCode))){
			
			txnDirection = SettleTxnDirection.ToCoop;
		}else if(premiumRefundTxnCdMp != null && premiumRefundTxnCdMp.txnCd.equals(txn.get(qTxn.txnCode))){
			
			txnDirection = SettleTxnDirection.FromCoop;
		}else{
			log.error("unexpected txnCd[{}], skip", txn.get(qTxn.txnCode));
			return null;
		}
		
		settleUtil.savePremiumItem(txn.get(qTxn.acctNbr), txn.get(qTxn.acctType), 
				txnDirection, SettleFeeType.CollectionPremiumFee, txn.get(qTxn.postAmt), null);
		
	
		return key;
	}
	

}
