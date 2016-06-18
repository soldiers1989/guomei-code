package com.sunline.ccs.batch.cc1400;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.MatchErrorReason;
import com.sunline.ppy.dictionary.report.ccs.MsxfMatchErrRpt;

/**
 * 马上对账文件-未匹配到马上流水的订单
 * @author wanghl
 *
 */
public class R1403MsxfUnmatchedOrder extends KeyBasedStreamReader<Long, MsxfMatchErrRpt> {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@PersistenceContext
    private EntityManager em;
	
	private QCcsOrder qOrder = QCcsOrder.ccsOrder;
	@Override
	protected List<Long> loadKeys() {
		List<Long> orderIds = new JPAQuery(em).from(qOrder)
				.where(qOrder.matchInd.eq(Indicator.Y)
						.and(qOrder.contactChnl.isNull().or(qOrder.contactChnl.ne(Constants.MERCHANT_ACQ_ID)))
						.and(qOrder.comparedInd.isNull().or(qOrder.comparedInd.eq(Indicator.N))))
				.list(qOrder.orderId);
		return orderIds;
	}

	@Override
	protected MsxfMatchErrRpt loadItemByKey(Long key) {
		CcsOrder order = em.find(CcsOrder.class, key);
		logger.debug("未匹配到流水的订单id[{}]",order.getOrderId());
		MsxfMatchErrRpt errRpt = new MsxfMatchErrRpt();
		errRpt.channelSerial = order.getOrderId().toString();
		errRpt.channelDate = order.getBusinessDate(); //FIXME 渠道日期是哪个？？
		errRpt.bankId =  order.getOpenBankId();
		errRpt.cardType = order.getCardType();
		errRpt.cardNo = order.getCardNo();
		errRpt.usrName = order.getUsrName();
		errRpt.idType = order.getCertType();
		errRpt.idNo = order.getCertId();
		errRpt.prov = order.getState();
		errRpt.city = order.getCity();
		errRpt.txnAmt = order.getTxnAmt();
		errRpt.purpose = order.getPurpose();
		errRpt.status = order.getStatus();
		errRpt.msDdReturnCode = order.getCode();
		errRpt.returnMessage = order.getMessage();
		errRpt.matchErrorReason = MatchErrorReason.R02;
		 
		return errRpt;
	}

}
