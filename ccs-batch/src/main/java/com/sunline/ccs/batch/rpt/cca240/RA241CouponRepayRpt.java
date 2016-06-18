package com.sunline.ccs.batch.rpt.cca240;

import java.math.RoundingMode;
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
import com.sunline.ccs.batch.rpt.cca240.item.CouponRepayRptItem;
import com.sunline.ccs.batch.rpt.common.RptBatchUtil;
import com.sunline.ccs.batch.rpt.common.RptParamFacility;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrderHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnHst;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.LoanUsage;

/**
 * 优惠券入账流水
 * @author wanghl
 *
 */
public class RA241CouponRepayRpt extends KeyBasedStreamReader<Long, CouponRepayRptItem> {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private QCcsTxnHst qTxn = QCcsTxnHst.ccsTxnHst;
	private QCcsOrder qOrder = QCcsOrder.ccsOrder;
	private QCcsOrderHst qOrderHst = QCcsOrderHst.ccsOrderHst;
	
	@Autowired
	private BatchStatusFacility batchFacility;
	@Autowired
	private RptBatchUtil rptBatchUtil;
	@Autowired
	private RptParamFacility codeProv;
	
	private TxnCd txnCdMCATInt;// 随借随还优惠券入账（抵扣利息）
	private TxnCd txnCdMCEIInt;// 等额本息优惠券入账（抵扣利息）
	
	@PersistenceContext
    private EntityManager em;
	@Override
	protected List<Long> loadKeys() {
		logger.info("CouponRepay post records, load keys");
		loadParam();
		
		List<String> txnCdList = new ArrayList<String>();
		if(txnCdMCATInt != null)
			txnCdList.add(txnCdMCATInt.txnCd);
		if(txnCdMCEIInt != null)
			txnCdList.add(txnCdMCEIInt.txnCd);
		
		if(txnCdList.size() <= 0 )
			return new ArrayList<Long>();
		
		List<Long> txnSeqs = new JPAQuery(em).from(qTxn)
				.where(qTxn.postDate.eq(batchFacility.getBatchDate())
						.and(qTxn.txnCode.in(txnCdList))
						)
				.list(qTxn.txnSeq);
		
		
		return txnSeqs;
	}

	@Override
	protected CouponRepayRptItem loadItemByKey(Long key) {
		logger.info("CouponRepay post record generate item:{}", key);
		CouponRepayRptItem item = new CouponRepayRptItem();
		loadParam();

		Tuple txnHst = new JPAQuery(em).from(qTxn).where(qTxn.txnSeq.eq(key))
			.singleResult(qTxn.acctNbr, qTxn.acctType, qTxn.txnCode, qTxn.postAmt, qTxn.txnAmt, qTxn.postDate,qTxn.term,qTxn.txnSeq);
		item.postAmt = txnHst.get(qTxn.postAmt).setScale(2, RoundingMode.HALF_UP);
		item.postDate = txnHst.get(qTxn.postDate);
		
		if(txnCdMCATInt.txnCd.equals(txnHst.get(qTxn.txnCode))){
			if(!loadInfoFromOrder(item,txnHst.get(qTxn.acctNbr),txnHst.get(qTxn.acctType))){
				logger.error("No Order find -{}-", txnHst.get(qTxn.txnSeq));
				return null;
			}
		}else if(txnCdMCEIInt.txnCd.equals(txnHst.get(qTxn.txnCode))){
			if(!loadInfoFromOrderHst(item,txnHst.get(qTxn.term),txnHst.get(qTxn.acctNbr),txnHst.get(qTxn.acctType))){
				logger.error("No Order find -{}-", txnHst.get(qTxn.txnSeq));
				return null;
			}
		}else
			return null;
		
		return item;
	}

	/**
	 * 等额本息查找OrderHst，赋值Item
	 * @param refNbr
	 * @param item
	 * @return
	 */
	private Boolean loadInfoFromOrderHst(CouponRepayRptItem item,int term,long acctNbr,AccountType acctType) {
		Tuple orderHst = new JPAQuery(em).from(qOrderHst)
				//期数匹配交易
				.where(qOrderHst.term.eq(term)
				.and(qOrderHst.acctNbr.eq(acctNbr))
				.and(qOrderHst.acctType.eq(acctType))
				.and(qOrderHst.loanUsage.eq(LoanUsage.Q)))
				.singleResult(qOrderHst.contrNbr, qOrderHst.term, qOrderHst.offsetType, qOrderHst.couponId, qOrderHst.businessDate,
						qOrderHst.txnAmt);
		if(orderHst == null) return false;	
		
		item.contrNbr = orderHst.get(qOrderHst.contrNbr);
		item.offsetTerm = orderHst.get(qOrderHst.term);
		item.amount = orderHst.get(qOrderHst.txnAmt).setScale(2, RoundingMode.HALF_UP);
		item.offsetType = orderHst.get(qOrderHst.offsetType);
		item.offsetNo = orderHst.get(qOrderHst.couponId);
		item.txnDate = orderHst.get(qOrderHst.businessDate);
		return true;
		
	}
	
	/**
	 * 随借随还查找Order，赋值Item
	 * @param refNbr
	 * @param item
	 * @return
	 */
	private Boolean loadInfoFromOrder(CouponRepayRptItem item,Long acctNbr,AccountType acctType) {
		Tuple order = new JPAQuery(em).from(qOrder).where(qOrder.loanUsage.eq(LoanUsage.Q)
				.and(qOrder.acctNbr.eq(acctNbr))
				.and(qOrder.acctType.eq(acctType))
				//当日入账，当日出报表
				.and(qOrder.businessDate.eq(batchFacility.getBatchDate())))
				.singleResult(qOrder.contrNbr, qOrder.term, qOrder.offsetType, qOrder.couponId, qOrder.businessDate,
						qOrder.txnAmt);
		if(order == null) return false;
		
		item.contrNbr = order.get(qOrder.contrNbr);
		item.offsetTerm = order.get(qOrder.term);
		item.amount = order.get(qOrder.txnAmt).setScale(2, RoundingMode.HALF_UP);
		item.offsetType = order.get(qOrder.offsetType);
		item.offsetNo = order.get(qOrder.couponId);
		item.txnDate = order.get(qOrder.businessDate);
		return true;
	}

	private void loadParam() {
		rptBatchUtil.setCurrOrgNoToContext();
		// 随借随还优惠券入账（抵扣利息）
		if(txnCdMCATInt == null){
			SysTxnCdMapping couponTxncdMCATIntMp = codeProv.retrieveParam(SysTxnCd.C18.name(), SysTxnCdMapping.class);
			if(couponTxncdMCATIntMp != null)
				txnCdMCATInt = codeProv.retrieveParam(couponTxncdMCATIntMp.txnCd, TxnCd.class);
		}
		
		// 等额本息优惠券入账（抵扣利息）
		if (txnCdMCEIInt == null) {
			SysTxnCdMapping couponTxncdMCEIIntMp = codeProv.retrieveParam(SysTxnCd.C20.name(), SysTxnCdMapping.class);
			if(couponTxncdMCEIIntMp != null)
				txnCdMCEIInt = codeProv.retrieveParam(couponTxncdMCEIIntMp.txnCd, TxnCd.class);
		}
	}
}
