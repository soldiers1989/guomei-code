package com.sunline.ccs.batch.cca400;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.Predicate;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.batch.cca400.item.SettleReqRptItem;
import com.sunline.ccs.infrastructure.shared.model.CcsSettlePlatformRec;
import com.sunline.ccs.infrastructure.shared.model.QCcsSettlePlatformRec;
import com.sunline.ppy.dictionary.enums.Indicator;

/**
 * 生成结算文件
 * @author lin
 *
 */
public class RA408SettleFile extends KeyBasedStreamReader<Long, SettleReqRptItem>{

	@PersistenceContext
	private EntityManager em;
	private QCcsSettlePlatformRec qrec = QCcsSettlePlatformRec.ccsSettlePlatformRec;
	
	@Override
	protected List<Long> loadKeys() {
		
		Predicate exp = qrec.fileRecorded.eq(Indicator.N);
		return new JPAQuery(em).from(qrec).where(exp ).list(qrec.settleRecId);
	}

	@Override
	protected SettleReqRptItem loadItemByKey(Long key) {
		CcsSettlePlatformRec rec = em.find(CcsSettlePlatformRec.class, key);
		
		SettleReqRptItem item = new SettleReqRptItem();
		item.cooperationId = rec.getCooperationId();
		item.customerName = rec.getName();
		item.mobileNo = rec.getMobileNumber();
		item.idNo = rec.getIdNo();
		item.applyNo = rec.getApplicationNo();
		item.contraNbr = rec.getContrNbr();
		item.settleFeeType = rec.getSettleFeeType();
		item.amt = rec.getSettleAmt();
		item.postDate = rec.getPostDate();
		item.productCd = rec.getProductCd();
		item.term = rec.getTerm();
		item.terminalDate = rec.getContrTerminalDate();
		item.txnDirection = rec.getTxnDirection()==null?null:rec.getTxnDirection().getSettleFileSign() ;
		
		rec.setFileRecorded(Indicator.Y);
		rec.setIsSend(Indicator.Y);
		em.merge(rec);
		
		return item;
	}

}
