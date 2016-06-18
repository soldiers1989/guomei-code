package com.sunline.ccs.batch.rpt.cca000;

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
import com.sunline.ccs.batch.rpt.common.RptParamFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsOrderHst;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepayHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrderHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsRepayHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsRepaySchedule;
import com.sunline.ccs.param.def.enums.BucketObject;
import com.sunline.ccs.param.def.enums.LoanTerminateReason;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ppy.dictionary.enums.Ownership;
import com.sunline.ppy.dictionary.enums.PlanType;

/**
 * 贷款还款明细
 * @author wanghl
 *
 */
public class RA003YGRpyDetail  extends KeyBasedStreamReader<RA003Key,SA003YGRpyDetailInfo> {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private RptParamFacility codeProv;
	private QCcsRepayHst qRepayHst = QCcsRepayHst.ccsRepayHst;
	private QCcsLoan qLoan = QCcsLoan.ccsLoan;
	private QCcsOrderHst qOrderHst = QCcsOrderHst.ccsOrderHst;
	private QCcsOrder qOrder = QCcsOrder.ccsOrder;
	private QCcsRepaySchedule qSchedule = QCcsRepaySchedule.ccsRepaySchedule;
	private QCcsAcct qAcct = QCcsAcct.ccsAcct;
	@PersistenceContext
    private EntityManager em;
	
	@Autowired
	BatchStatusFacility batchStatusFacility;
	@Override
	protected List<RA003Key> loadKeys() {  
		List<RA003Key> keys = new ArrayList<RA003Key>();

		Date batchDate = batchStatusFacility.getBatchDate();
		List<String> productCdList = codeProv.loadProductCdList(Ownership.P, LoanType.MCEI, codeProv.YG_ACQ_ACCEPTOR_ID);
		if(productCdList == null || productCdList.size() <= 0) return keys;
		
		List<Long> planIds = new JPAQuery(em).from(qRepayHst, qAcct)
				.where(qAcct.acctNbr.eq(qRepayHst.acctNbr)
						.and(qAcct.acctType.eq(qRepayHst.acctType))
						.and(qAcct.productCd.in(productCdList))
						.and(qRepayHst.batchDate.eq(batchDate ))
						.and(qRepayHst.bnpType.in(
								BucketObject.ctdPrincipal, BucketObject.ctdInterest, BucketObject.ctdMulct,
								BucketObject.pastPrincipal, BucketObject.pastInterest, BucketObject.pastMulct
								)))
				.distinct().list(qRepayHst.planId);
		
		List<Long> recoverOrderHstIds = new JPAQuery(em).from(qOrderHst, qAcct)
				.where(qOrderHst.acctNbr.eq(qAcct.acctNbr).and(qOrderHst.acctType.eq(qAcct.acctType))
						.and(qOrderHst.optDatetime.eq(batchDate))
						.and(qAcct.productCd.in(productCdList))
						.and(qOrderHst.loanUsage.eq(LoanUsage.S))
						.and(qOrderHst.onlineFlag.eq(Indicator.Y).and(qOrderHst.orderStatus.eq(OrderStatus.S))
								.or(qOrderHst.onlineFlag.eq(Indicator.N).and(qOrderHst.orderStatus.eq(OrderStatus.D)))
								))
				.list(qOrderHst.orderId);
		List<Long> recoverOrderIds = new JPAQuery(em).from(qOrder, qAcct)
				.where(qOrder.acctNbr.eq(qAcct.acctNbr).and(qOrder.acctType.eq(qAcct.acctType))
						.and(qOrder.optDatetime.eq(batchDate))
						.and(qAcct.productCd.in(productCdList))
						.and(qOrder.loanUsage.eq(LoanUsage.S))
						.and(qOrder.onlineFlag.eq(Indicator.Y).and(qOrder.orderStatus.eq(OrderStatus.S))
								.or(qOrder.onlineFlag.eq(Indicator.N).and(qOrder.orderStatus.eq(OrderStatus.D)))
								))
				.list(qOrder.orderId);
		
		for(Long id : planIds){
			keys.add(new RA003Key(id, false, false));
		}
		for(Long id : recoverOrderIds){
			keys.add(new RA003Key(id, true, false));
		}
		for(Long id : recoverOrderHstIds){
			keys.add(new RA003Key(id, true, true));
		}
		return keys;
	}

	@Override
	protected SA003YGRpyDetailInfo loadItemByKey(RA003Key key) {
		Long id = key.getId();
		if(key.getIsRecovery()){
			if(key.getIsOrderHst()){
				return getInfoFromOrderHst(id);
			}else{
				return getInfoFromOrder(id);
			}
		}else{
			return getAcctRepayInfo(id);
		}
	}
	
	/**
	 * 理赔后代位追偿还款
	 * @param orderId 
	 */ 
	private SA003YGRpyDetailInfo getInfoFromOrder(Long orderId) {
		SA003YGRpyDetailInfo info = new SA003YGRpyDetailInfo();
		info.setOrder(em.find(CcsOrder.class, orderId));
		info.setIsRecovery(true);
		info.setIsLoanTerminated(true);
		return info;
	}

	/**
	 * 理赔后代位追偿还款
	 * @param orderId 
	 */ 
	private SA003YGRpyDetailInfo getInfoFromOrderHst(Long orderHstId) {
		SA003YGRpyDetailInfo info = new SA003YGRpyDetailInfo();
		info.setOrderHst(em.find(CcsOrderHst.class, orderHstId));
		info.setIsRecovery(true);
		info.setIsLoanTerminated(true);
		return info;
	}

	/**
	 * 账户还款
	 * @param planId
	 */
	private SA003YGRpyDetailInfo getAcctRepayInfo(Long planId) {
		Date batchDate = batchStatusFacility.getBatchDate();
		SA003YGRpyDetailInfo info = new SA003YGRpyDetailInfo();
		info.setIsRecovery(false);
		logger.info("planId[{}]",planId);
		
		if(planId == null){
			return null;
		}
		//
		CcsPlan plan = em.find(CcsPlan.class, planId);
		if(!(plan.getPlanType().equals(PlanType.I) || plan.getPlanType().equals(PlanType.Q)) || plan.getTerm() == null){
			return null;
		}
		logger.info("plan[{}][{}]Term[{}]", plan.getPlanId(), plan.getPlanType(), plan.getTerm());
		Tuple loan = new JPAQuery(em).from(qLoan)
				.where(qLoan.acctNbr.eq(plan.getAcctNbr()))
				.singleResult(qLoan.loanStatus, qLoan.terminalReasonCd, qLoan.terminalDate, 
						qLoan.dueBillNo, qLoan.loanId, qLoan.acctNbr, qLoan.acctType, qLoan.loanType);
		if(!LoanType.MCEI.equals(loan.get(qLoan.loanType))){
			return null;
		}
		if(LoanStatus.T == loan.get(qLoan.loanStatus)){
			if(LoanTerminateReason.C.equals(loan.get(qLoan.terminalReasonCd))){
				return null;
			}else{
				info.setIsLoanTerminated(true);
				info.setTerminalReason(loan.get(qLoan.terminalReasonCd));
			}
		}else{//Loan非T
			Date termPmtDueDate = new JPAQuery(em).from(qSchedule)
					.where(qSchedule.acctNbr.eq(plan.getAcctNbr()).and(qSchedule.currTerm.eq(plan.getTerm())))
					.singleResult(qSchedule.loanPmtDueDate);
			info.setTermPmtDueDate(termPmtDueDate);
			
			
		}
		
		//还款分配记录
		List<CcsRepayHst> repays = new JPAQuery(em).from(qRepayHst)
				.where(qRepayHst.planId.eq(planId).and(qRepayHst.batchDate.eq(batchDate)))
				.list(qRepayHst);
		
		//找原始订单
		List<Long> orderIds = new JPAQuery(em).from(qOrderHst)
				.where(qOrderHst.optDatetime.eq(batchDate)
						.and(qOrderHst.dueBillNo.eq(loan.get(qLoan.dueBillNo)))
						.and((qOrderHst.loanUsage.eq(LoanUsage.O)
									.and(qOrderHst.onlineFlag.eq(Indicator.Y)
											.and(qOrderHst.orderStatus.eq(OrderStatus.S))
											.or(qOrderHst.onlineFlag.eq(Indicator.N)
												.and(qOrderHst.orderStatus.eq(OrderStatus.D))
												.and(qOrderHst.oriOrderId.isNull()))
									)
								)
								.or(qOrderHst.loanUsage.in(LoanUsage.M, LoanUsage.N).and(qOrderHst.orderStatus.eq(OrderStatus.S)))
							)
					)
				.list(qOrderHst.orderId);
		
		if(orderIds.size() == 0){
			orderIds = new JPAQuery(em).from(qOrder)
			.where(qOrder.optDatetime.eq(batchDate)
					.and(qOrder.dueBillNo.eq(loan.get(qLoan.dueBillNo)))
					.and((qOrder.loanUsage.eq(LoanUsage.O)
								.and(qOrder.onlineFlag.eq(Indicator.Y).and(qOrder.orderStatus.eq(OrderStatus.S))
									.or(qOrder.onlineFlag.eq(Indicator.N).and(qOrder.orderStatus.eq(OrderStatus.D)).and(qOrder.oriOrderId.isNull()))
								)
							)
							.or(qOrder.loanUsage.in(LoanUsage.M, LoanUsage.N).and(qOrder.orderStatus.eq(OrderStatus.S)))
						)
					)
			.list(qOrder.orderId);
		}
		
		//客户的约定还款卡号
		String acctCardNo = new JPAQuery(em).from(qAcct)
				.where(qAcct.acctNbr.eq(loan.get(qLoan.acctNbr)).and(qAcct.acctType.eq(loan.get(qLoan.acctType))))
				.singleResult(qAcct.ddBankAcctNbr);
		
		info.setPlan(plan);
		info.setRepays(repays);
		info.setOrderIds(orderIds);
		info.setDdBankAcctNo(acctCardNo);
		
		info.setCurrTerm(plan.getTerm());
		info.setBatchDate(batchDate);
		info.setDueBillNo(loan.get(qLoan.dueBillNo));
		return info;
	}

}
