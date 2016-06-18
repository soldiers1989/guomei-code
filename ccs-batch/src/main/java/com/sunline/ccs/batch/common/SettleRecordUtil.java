package com.sunline.ccs.batch.common;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsSettlePlatformRec;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.param.def.enums.BucketObject;
import com.sunline.ccs.param.def.enums.SettleFeeType;
import com.sunline.ccs.param.def.enums.SettleTxnDirection;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.enums.Indicator;

/**
 * 生成结算平台文件记录工具
 * @author lin
 */
@Service
public class SettleRecordUtil {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	private QCcsAcct qAcct = QCcsAcct.ccsAcct;
	private QCcsCustomer qCust = QCcsCustomer.ccsCustomer;
	private QCcsLoan qLoan = QCcsLoan.ccsLoan;
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private BatchStatusFacility batchFacility;
	
	/**
	 * ctdReplaceSvcFee, pastReplaceSvcFee	<P>
	 * ctdReplacePenalty, pastReplacePenalty	<P>
	 * ctdReplaceLpc, pastReplaceLpc	<P>
	 * ctdReplaceMulct, pastReplaceMulct <P>
	 * ctdReplaceTxnFee, pastReplaceTxnFee <P>
	 */
	public final List<BucketObject> SETTLE_BNP_TYPES = Arrays.asList(
			BucketObject.ctdReplaceSvcFee, BucketObject.pastReplaceSvcFee,
			BucketObject.ctdReplacePenalty,BucketObject.pastReplacePenalty, 
			BucketObject.ctdReplaceLpc,BucketObject.pastReplaceLpc,
			BucketObject.ctdReplaceMulct,BucketObject.pastReplaceMulct,
			BucketObject.ctdReplaceTxnFee, BucketObject.pastReplaceTxnFee);
	
	/**
	 * 初始化结算代收费记录
	 */
	public CcsSettlePlatformRec initSettleReplaceFeeRec(Long acctNbr, AccountType accountType, 
			SettleTxnDirection txnDirection, BucketType bucketType, BigDecimal amt) {
		log.info("init replace fee settle record[{}][{}]", bucketType, txnDirection);
		
		Tuple loan = new JPAQuery(em).from(qLoan)
				.where(qLoan.acctNbr.eq(acctNbr).and(qLoan.acctType.eq(accountType)))
				.singleResult(qLoan.currTerm, qLoan.loanCode);
		
		SettleFeeType settleFeeType = null;
		switch (bucketType) {
		case ReplaceSvcFee: settleFeeType = SettleFeeType.CollectionSvcFee; break;//代收服务费
		case ReplacePenalty: settleFeeType = SettleFeeType.CollectionPenaltyInt; break;//代收罚息
		case ReplaceLatePaymentCharge: settleFeeType = SettleFeeType.CollectionLateFee; break;//代收滞纳金
		case ReplaceMulct: settleFeeType = SettleFeeType.CollectionMulctAmt; break;//代收罚金
		case ReplaceTxnFee: settleFeeType = SettleFeeType.CollePrepayTxnFee; break;//代收提前结清手续费（违约金）
		default:
			log.error("unexpected bucket type[{}], skip!!!", bucketType.name());
			return null;
		}
		
		return initSettleCommonRec(acctNbr, accountType, txnDirection, settleFeeType , amt, loan.get(qLoan.currTerm), loan.get(qLoan.loanCode));
	}
	/**
	 * 保存犹豫期内提前结清记录
	 */
	public CcsSettlePlatformRec savePrepayRec(CcsLoan loan, CcsLoanReg reg){
		if(reg == null) {
			log.info("没有Reg参数，跳过");
			return null;
		}
		
		log.info("保存犹豫期内提前结清记录，注册编号[{}]", reg.getRegisterId());
		Long acctNbr = loan.getAcctNbr();
		AccountType accountType = loan.getAcctType();
		
		CcsSettlePlatformRec rec = initSettleCommonRec(acctNbr, accountType, 
				null, SettleFeeType.PrepayHesitationAmt, reg.getPreAdAmt(), loan.getCurrTerm(), loan.getLoanCode());
		
		rec.setContrTerminalDate(batchFacility.getBatchDate());
		em.persist(rec);
		return rec;
	}
	
	/**
	 *  保存趸交费结算记录
	 */
	public void savePremiumItem(Long acctNbr,AccountType accountType, 
			SettleTxnDirection txnDirection, SettleFeeType settleFeeType, BigDecimal amt, Integer currTerm) {
		log.info("保存趸交费结算记录，账户[{}][{}]结算方向[{}][{}]", acctNbr, accountType, txnDirection, settleFeeType);
		
		String loanCd = new JPAQuery(em).from(qLoan)
				.where(qLoan.acctNbr.eq(acctNbr).and(qLoan.acctType.eq(accountType)))
				.singleResult(qLoan.loanCode);
		
		CcsSettlePlatformRec rec = initSettleCommonRec(acctNbr, accountType,
				txnDirection, settleFeeType, amt, currTerm, loanCd);
		
		rec.setContrTerminalDate(null);
		em.persist(rec); 
	}
	
	/**
	 * 保存代偿、代偿退还结算记录
	 * @param account
	 * @param loan
	 * @param term
	 * @param compensateAmt
	 * @param tocoop
	 */
	public void saveCompstSettleRecord(CcsAcct account, CcsLoan loan,
			CcsPlan plan, BigDecimal compstAmt, SettleTxnDirection settleTxnDirection) {
		log.info("保存代偿交易记录[{}][{}]planId[{}],方向[{}],term[{}]", 
				account.getAcctNbr(), account.getAcctType(), plan.getPlanId(), settleTxnDirection, plan.getTerm());
		
		CcsSettlePlatformRec rec = initSettleCommonRec(account, loan,
				settleTxnDirection, SettleFeeType.CompensateAmt, compstAmt, plan.getTerm());
		
		rec.setContrTerminalDate(null);
		em.persist(rec); 
		
	}

	/**
	 * 初始化结算平台记录
	 * @param account
	 * @param loan
	 * @param settleTxnDirection
	 * @param settleFeeType
	 * @param compensateAmt
	 * @param term
	 * @return
	 */
	private CcsSettlePlatformRec initSettleCommonRec(CcsAcct account,
			CcsLoan loan, SettleTxnDirection settleTxnDirection,
			SettleFeeType settleFeeType, BigDecimal compensateAmt, Integer term) {
		
		Tuple cust = new JPAQuery(em).from(qCust)
				.where(qCust.custId.eq(account.getCustId()))
				.singleResult(qCust.idNo, qCust.idType, qCust.mobileNo);
		
		CcsSettlePlatformRec rec =  new CcsSettlePlatformRec();
		rec.setAcctNbr(account.getAcctNbr());
		rec.setAcctType(account.getAcctType());
		rec.setIdNo(cust.get(qCust.idNo));
		rec.setIdType(cust.get(qCust.idType));
		rec.setMobileNumber(cust.get(qCust.mobileNo));
		rec.setApplicationNo(account.getApplicationNo());
		rec.setContrNbr(account.getContrNbr());
		rec.setProductCd(loan.getLoanCode());
		rec.setTerm(term);
		rec.setSettleFeeType(settleFeeType);
		rec.setSettleAmt(compensateAmt);
		rec.setTxnDirection(settleTxnDirection);
		rec.setCooperationId(account.getAcqId());
		rec.setName(account.getName());
//		rec.setContrTerminalDate("");//不在这里赋值
		rec.setPostDate(batchFacility.getBatchDate());
		rec.setFileRecorded(Indicator.N);
		return rec;
	}
	/**
	 * 初始化结算平台记录
	 * @param acctNbr
	 * @param accountType
	 * @param txnDirection
	 * @param settleFeeType
	 * @param amt
	 * @param currTerm
	 * @return
	 */
	private CcsSettlePlatformRec initSettleCommonRec(Long acctNbr, AccountType accountType, 
			SettleTxnDirection txnDirection, SettleFeeType settleFeeType,BigDecimal amt, Integer currTerm, String loanCd) {
		
		Tuple acct = new JPAQuery(em).from(qAcct)
				.where(qAcct.acctNbr.eq(acctNbr).and(qAcct.acctType.eq(accountType)))
				.singleResult(qAcct.contrNbr, qAcct.name, qAcct.productCd, qAcct.custId, qAcct.org, 
						qAcct.acqId, qAcct.mobileNo, qAcct.applicationNo
						);
		Tuple cust = new JPAQuery(em).from(qCust)
				.where(qCust.custId.eq(acct.get(qAcct.custId)))
				.singleResult(qCust.idNo, qCust.idType, qCust.mobileNo);
		
		CcsSettlePlatformRec rec =  new CcsSettlePlatformRec();
		rec.setAcctNbr(acctNbr);
		rec.setAcctType(accountType);
		rec.setIdNo(cust.get(qCust.idNo));
		rec.setIdType(cust.get(qCust.idType));
		rec.setMobileNumber(cust.get(qCust.mobileNo));
		rec.setApplicationNo(acct.get(qAcct.applicationNo));
		rec.setContrNbr(acct.get(qAcct.contrNbr));
		rec.setProductCd(loanCd);
		rec.setTerm(currTerm);
		rec.setSettleFeeType(settleFeeType);
		rec.setSettleAmt(amt);
		rec.setTxnDirection(txnDirection);
		rec.setCooperationId(acct.get(qAcct.acqId));
		rec.setName(acct.get(qAcct.name));
//		rec.setContrTerminalDate("");//不在这里赋值
		rec.setPostDate(batchFacility.getBatchDate());
		rec.setFileRecorded(Indicator.N);
		return rec;
	}
	

}
