package com.sunline.ccs.batch.rpt.cca000;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.batch.rpt.common.RptParamFacility;
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

/**
 * 保费还款信息
 * @author wanghl
 *
 */
public class RA005YGInsuredAmtRpyInfo  extends KeyBasedStreamReader<Long,SA005YGInsuredAmtRpy>  {
	private QCcsRepayHst qRepayHst = QCcsRepayHst.ccsRepayHst;
	private QCcsLoan qLoan = QCcsLoan.ccsLoan;
	private QCcsOrderHst qOrderHst = QCcsOrderHst.ccsOrderHst;
	private QCcsRepaySchedule qSchedule = QCcsRepaySchedule.ccsRepaySchedule;
	private QCcsOrder qOrder = QCcsOrder.ccsOrder;
	private QCcsAcct qAcct = QCcsAcct.ccsAcct;
	@PersistenceContext
    private EntityManager em;
	@Autowired
	private RptParamFacility codeProv;
	
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	@Override
	protected List<Long> loadKeys() {
		
		Date batchDate = batchStatusFacility.getBatchDate();
		
		List<String> productCdList = codeProv.loadProductCdList(Ownership.P, LoanType.MCEI, codeProv.YG_ACQ_ACCEPTOR_ID);
		if(productCdList == null || productCdList.size() <= 0) return new ArrayList<Long>();
		
		BooleanExpression exp = 
				qRepayHst.acctNbr.eq(qAcct.acctNbr)
				.and(qRepayHst.acctType.eq(qAcct.acctType))
				.and(qAcct.productCd.in(productCdList ))
				.and(qRepayHst.batchDate.eq(batchDate ))
				.and(qRepayHst.bnpType.in(BucketObject.ctdIns, BucketObject.pastIns));//
				
		List<Long> repayInsurances = new JPAQuery(em).from(qRepayHst, qAcct).where(exp).distinct().list(qRepayHst.planId);
		
		return repayInsurances;
	}

	@Override
	protected SA005YGInsuredAmtRpy loadItemByKey(Long key) {
		Date batchDate = batchStatusFacility.getBatchDate();
		if(key == null){
			return null;
		}
		SA005YGInsuredAmtRpy info = new SA005YGInsuredAmtRpy();
		
		List<CcsRepayHst> repays = new JPAQuery(em).from(qRepayHst)
				.where(qRepayHst.planId.eq(key)
						.and(qRepayHst.batchDate.eq(batchDate))
						.and(qRepayHst.bnpType.in(BucketObject.ctdIns, BucketObject.pastIns)))
				.list(qRepayHst);
		
		CcsPlan plan = em.find(CcsPlan.class, key);
		
		Tuple loan = new JPAQuery(em).from(qLoan)
				.where(qLoan.acctNbr.eq(plan.getAcctNbr()).and(qLoan.acctType.eq(plan.getAcctType())))
				.singleResult(qLoan.dueBillNo, qLoan.loanId, qLoan.loanStatus, qLoan.terminalReasonCd);
		
		Tuple scheduleTuple = new JPAQuery(em).from(qSchedule)
				.where(qSchedule.loanId.eq(loan.get(qLoan.loanId)).and(qSchedule.currTerm.eq(plan.getTerm())))
				.singleResult(qSchedule.loanPmtDueDate, qSchedule.currTerm, qSchedule.cardNbr);
		
		List<Long> orderIds = new JPAQuery(em).from(qOrderHst)
				.where(qOrderHst.optDatetime.eq(batchDate)
						.and(qOrderHst.dueBillNo.eq(loan.get(qLoan.dueBillNo)))
						.and((qOrderHst.loanUsage.eq(LoanUsage.O)
									.and(qOrderHst.onlineFlag.eq(Indicator.Y).and(qOrderHst.orderStatus.eq(OrderStatus.S))
										.or(qOrderHst.onlineFlag.eq(Indicator.N).and(qOrderHst.orderStatus.eq(OrderStatus.D)).and(qOrderHst.oriOrderId.isNull()))
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
		info.setPlan(plan);
		info.setRepays(repays);
		info.setOrderIds(orderIds);
		if(loan.get(qLoan.loanStatus).equals(LoanStatus.T)){
			if(LoanTerminateReason.C.equals(loan.get(qLoan.terminalReasonCd))){
				return null;
			}else{
				info.setIsLoanTerminated(true);
				info.setTerminalReason(loan.get(qLoan.terminalReasonCd));
			}
		}
		if(scheduleTuple != null){
			info.setDdBankAcctNo(scheduleTuple.get(qSchedule.cardNbr));
			info.setTermPmtDueDate(scheduleTuple.get(qSchedule.loanPmtDueDate));
			info.setCurrTerm(scheduleTuple.get(qSchedule.currTerm));
		}
		info.setDueBillNo(loan.get(qLoan.dueBillNo));
		info.setBatchDate(batchDate);
		return info;
	}



}
