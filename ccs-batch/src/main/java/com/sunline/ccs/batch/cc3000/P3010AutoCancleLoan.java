package com.sunline.ccs.batch.cc3000;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.batch.cc3000.cancle.AutoCancle;
import com.sunline.ccs.batch.cc3000.cancle.AutoCancleLoanJ;
import com.sunline.ccs.batch.cc3000.cancle.AutoCancleLoanP;
import com.sunline.ccs.batch.common.DateUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ppy.dictionary.enums.Ownership;

public class P3010AutoCancleLoan implements ItemProcessor<CcsLoan, CcsLoan> {
	
	private static final Logger logger = LoggerFactory.getLogger(P3010AutoCancleLoan.class);
	
	@Autowired
	private UnifiedParameterFacility unifiedFacility;
	
	@Autowired
	private BatchStatusFacility batchFacility;
	
	@Autowired
	private AutoCancleLoanP cancleP;
	
	@Autowired
	private AutoCancleLoanJ cancleJ;
	
	@PersistenceContext
	private EntityManager em;

	QCcsOrder o = QCcsOrder.ccsOrder;
	
	@Override
	public CcsLoan process(CcsLoan loan) throws Exception {
		
		OrganizationContextHolder.setCurrentOrg(loan.getOrg());
		// 自有产品才强制取消
		LoanPlan loanPlan = unifiedFacility.loadParameter(loan.getLoanCode(), LoanPlan.class);
		if(loanPlan.cpdOverdueEndDays == null){
			return null;
		}
		// cpd90天以内不取消
		if(DateUtils.getIntervalDays(loan.getCpdBeginDate(), batchFacility.getBatchDate())<= loanPlan.cpdOverdueEndDays)
			return null;
		
		if(loanPlan.ownership != Ownership.O)
			return null;
		
		int wOrderCount = new JPAQuery(em).from(o)
				.where(o.acctNbr.eq(loan.getAcctNbr()).and(o.acctType.eq(loan.getAcctType()))
					.and(o.orderStatus.eq(OrderStatus.W)))
				.list(o.orderId).size();
		// 若有在途订单不强制取消
		if(wOrderCount > 0)
			return null;
		
		//贷款未终止或者未完成才做终止操作
		if(!loan.getLoanStatus().equals(LoanStatus.F) && !loan.getLoanStatus().equals(LoanStatus.T)){
			if(logger.isDebugEnabled()){
				logger.debug("强制取消分期,org:[" + loan.getOrg() + 
							"],loanId:[" + loan.getLoanId() + 
							"]");
			}
			// 强制取消
			AutoCancle cancle = defineCancle(loan);
			cancle.cancle(loan);
		}
		
		return loan;
	}

	private AutoCancle defineCancle(CcsLoan loan) {
		if(LoanType.MCEI == loan.getLoanType()){
			return cancleP;
		}else if(LoanType.MCAT == loan.getLoanType()){
			return cancleJ;
		}else{
			throw new RuntimeException("不支持的强制取消分期贷款类型,loanType:" + loan.getLoanType());
		}
	}
	
}
