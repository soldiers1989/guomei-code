package com.sunline.ccs.batch.cc1400;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.batch.common.DateUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsOutsideDdTxn;
import com.sunline.ccs.infrastructure.shared.model.CcsOutsideDdTxnHst;
import com.sunline.ppy.dictionary.enums.Indicator;

/**
 * 清理转移外部扣款表
 * @author liuqi
 *
 */
public class P1405OutsideDDTxnClean implements ItemProcessor<CcsOutsideDdTxn, Object> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@PersistenceContext
    private EntityManager em;
    @Autowired
    private BatchStatusFacility batchFacility;
	
	@Override
	public Object process(CcsOutsideDdTxn item) throws Exception {
		OrganizationContextHolder.setCurrentOrg(item.getOrg());
		CcsOutsideDdTxnHst hst = new CcsOutsideDdTxnHst();
		boolean isMove = false;
		if(item.getErrInd() == Indicator.Y){
			isMove = true;
		}else{
			if(item.getDdRspFlag() != null ){
				isMove = true;
			}else{
				Date processStatusExpired = DateUtils.addDays(item.getBusinessDate(), P1404MsxfOrderClean.processExpired) ;	
				if(batchFacility.getBatchDate().compareTo(processStatusExpired)>=0){
					isMove = true;
				}
			}
		}
		if(isMove){
			logger.debug("PTP扣款：txnid:["+item.getTxnId()+"],转移历史！");
			
			hst.updateFromMap(item.convertToMap());
			em.persist(hst);
			em.remove(item);
		}
		return null;
	}

}
