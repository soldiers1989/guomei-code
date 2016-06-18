package com.sunline.ccs.batch.rpt.cca210;

import java.math.BigDecimal;
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
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.batch.rpt.cca210.item.MsLoanRptItem;
import com.sunline.ccs.batch.rpt.common.RptParamFacility;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.pcm.param.def.Product;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ppy.dictionary.enums.Ownership;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 马上贷放款结果查询送报表
 * @author wanghl
 *
 */
public class RA211MsLoanRpt extends KeyBasedStreamReader<Long, MsLoanRptItem>  {
	private static final Logger logger = LoggerFactory.getLogger(RA211MsLoanRpt.class);
	@PersistenceContext
    private EntityManager em;
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	@Autowired
	private RptParamFacility codeProv;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	
	private QCcsAcct qAcct = QCcsAcct.ccsAcct;
	private QCcsLoan qLoan = QCcsLoan.ccsLoan;
	@Override
	protected List<Long> loadKeys() {
		List<String> loanCdList = codeProv.loadLoanCdList(Ownership.O, LoanType.MCEI, null);
		if(loanCdList == null || loanCdList.size() <= 0) return new ArrayList<Long>();
		
		JPAQuery query = new JPAQuery(em);
		Date batchDate = batchStatusFacility.getBatchDate();
		BooleanExpression exp = 
				qLoan.activeDate.eq(batchDate)
					.and(qLoan.loanStatus.eq(LoanStatus.A))
					.and(qLoan.loanCode.in(loanCdList));
		
		return query.from(qLoan).where(exp).list(qLoan.loanId);
	}

	@Override
	protected MsLoanRptItem loadItemByKey(Long key) {
		MsLoanRptItem item = new MsLoanRptItem();
		logger.info("=====Loan_Id[{}]=====", key);
		
		CcsLoan loan = em.find(CcsLoan.class, key);
		Tuple acct = new JPAQuery(em).from( qAcct)
				.where( qAcct.acctNbr.eq(loan.getAcctNbr()) 
						.and(qAcct.acctType.eq(loan.getAcctType())))
				.singleResult(qAcct.name, qAcct.custId, qAcct.productCd);
		
		LoanPlan loanPlan = codeProv.loadParameter(loan.getLoanCode(),LoanPlan.class);
		Product product = codeProv.loadParameter(acct.get(qAcct.productCd), Product.class);
		
		logger.info("合同号："+loan.getContrNbr());
		item.productCd = product.productCode;
		item.productDesc = product.description;
		item.loanCode = loanPlan.loanCode;
		item.loanDesc = loanPlan.description;
		item.contrNbr = loan.getContrNbr();
		item.name = acct.get(qAcct.name);
		item.idNo = acct.get(qAcct.custId).toString();
		item.loanDate = loan.getActiveDate();
		item.orderStatus = OrderStatus.S; 
		item.loanAmt = loan.getLoanInitPrin().setScale(2, RoundingMode.HALF_UP);
		item.loanTerm = loan.getLoanInitTerm();
		
		if(loan.getInterestRate().compareTo(BigDecimal.ZERO) == 0){
			item.interestRate = BigDecimal.ZERO.setScale(8).toPlainString();
		}else{
			item.interestRate = loan.getInterestRate().setScale(8, RoundingMode.HALF_UP).toPlainString(); 
		}
		LoanFeeDef fee = null;
		try {
			fee = unifiedParamFacilityProvide.loanFeeDef(loan.getLoanCode(), loan.getLoanInitTerm(), loan.getLoanInitPrin());
		} catch (ProcessException e) {
			logger.info("LoanId["+loan.getLoanId()+"]LoanCode[" +loan.getLoanCode()+"]期数[" +loan.getLoanInitTerm()+"]无定价参数配置");
		}
		
		//服务费率
		BigDecimal svcFeeRate = null;
		if(loan.getInstallmentFeeRate() != null){
			svcFeeRate = loan.getInstallmentFeeRate();
		}else if(fee != null && fee.installmentFeeRate != null){
			svcFeeRate = fee.installmentFeeRate;
		}
		if(svcFeeRate != null && svcFeeRate.compareTo(BigDecimal.ZERO) >0 ){
				item.svcFeeRate = svcFeeRate.multiply(new BigDecimal(12))
						.divide(new BigDecimal(loan.getLoanInitTerm()), 8, RoundingMode.HALF_UP)
						.toPlainString();
		}else{
			item.svcFeeRate = BigDecimal.ZERO.setScale(8).toPlainString();
		}
	
		//寿险费率
		BigDecimal lifeInsuFeeRate = null;
		if(Indicator.Y.equals(loan.getJoinLifeInsuInd())){
			if(loan.getLifeInsuFeeRate() != null){
				lifeInsuFeeRate = loan.getLifeInsuFeeRate();
			}else if(fee != null && fee.lifeInsuFeeRate != null){
				lifeInsuFeeRate = fee.lifeInsuFeeRate;
			}
			if(lifeInsuFeeRate != null && lifeInsuFeeRate.compareTo(BigDecimal.ZERO) > 0){
				item.optionalSvcFeeRate = lifeInsuFeeRate.multiply(new BigDecimal(12))
						.divide(new BigDecimal(loan.getLoanInitTerm()), 8, RoundingMode.HALF_UP)
						.toPlainString();
			}else{
				item.optionalSvcFeeRate = BigDecimal.ZERO.setScale(8).toPlainString();
			}
		}else{
			item.optionalSvcFeeRate = BigDecimal.ZERO.setScale(8).toPlainString();
		}
		if(loan.getPremiumInd() == Indicator.Y && loan.getPremiumAmt() != null && loan.getPremiumAmt().compareTo(BigDecimal.ZERO)>0){
			item.premiumAmt = loan.getPremiumAmt().setScale(2, RoundingMode.HALF_UP);
		}else{
			item.premiumAmt = BigDecimal.ZERO;
		}
		return item;
	}
	

}
