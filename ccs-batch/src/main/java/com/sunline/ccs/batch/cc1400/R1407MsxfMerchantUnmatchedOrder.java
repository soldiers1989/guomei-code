package com.sunline.ccs.batch.cc1400;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.batch.common.EnumUtils;
import com.sunline.ccs.facility.order.OrderFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsOrderHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.MatchErrorReason;
import com.sunline.ppy.dictionary.report.ccs.MsxfMatchErrRpt;
import com.sunline.ppy.dictionary.report.ccs.MsxfMerchantMatchErrRpt;

public class R1407MsxfMerchantUnmatchedOrder extends KeyBasedStreamReader<Long, MsxfMerchantMatchErrRpt> {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@PersistenceContext
    private EntityManager em;
	
	private QCcsOrder qOrder = QCcsOrder.ccsOrder;
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	@Autowired
	private OrderFacility orderFacility;
	@Override
	protected List<Long> loadKeys() {
		List<Long> orderIds = new JPAQuery(em).from(qOrder)
				//需要对账
				.where(qOrder.matchInd.eq(Indicator.Y)
						//渠道:商户平台
						.and(qOrder.contactChnl.eq(Constants.MERCHANT_ACQ_ID))
						//隔日匹配
						.and(qOrder.businessDate.before(batchStatusFacility.getBatchDate()))
						//未匹配
						.and(qOrder.comparedInd.isNull().or(qOrder.comparedInd.eq(Indicator.N))))
				.list(qOrder.orderId);
		return orderIds;
	}

	@Override
	protected MsxfMerchantMatchErrRpt loadItemByKey(Long key) {
		CcsOrder order = em.find(CcsOrder.class, key);
		logger.debug("未匹配到流水的订单id[{}]",order.getOrderId());
		MsxfMerchantMatchErrRpt errRpt = new MsxfMerchantMatchErrRpt();
		if(EnumUtils.in(order.getLoanUsage(),LoanUsage.V,LoanUsage.R)){
			CcsOrder oriOrder = em.find(CcsOrder.class, order.getOriOrderId());
			if(oriOrder==null){
				CcsOrderHst oriOrderHst = em.find(CcsOrderHst.class,order.getOriOrderId());
				if(oriOrderHst!=null){
					errRpt.outTraceNo = oriOrderHst.getMerchandiseOrder();
				}
			}else{
				errRpt.outTraceNo = oriOrder.getMerchandiseOrder();
			}
		}else{
			errRpt.outTraceNo = order.getMerchandiseOrder();
		}
		errRpt.cashFee = new Integer(order.getTxnAmt().toString().replace(".", ""));
		errRpt.deviceInfo =null;
		errRpt.feeType = "CNY";
		errRpt.mchId = Constants.MERCHANT_ACQ_ID;
		errRpt.orderStatus = orderFacility.getServiceIdMapping(order.getServiceId());
		errRpt.outRefundNo = null;
		errRpt.startTime = order.getOrderTime();
		errRpt.totalFee = null;
		errRpt.matchErrorReason = MatchErrorReason.R02;
		 
		return errRpt;
	}
}