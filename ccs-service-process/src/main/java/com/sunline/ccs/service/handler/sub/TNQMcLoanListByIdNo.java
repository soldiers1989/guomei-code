package com.sunline.ccs.service.handler.sub;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S20022Loan;
import com.sunline.ccs.service.protocol.S20022Req;
import com.sunline.ccs.service.protocol.S20022Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQMcLoanListByIdNo
 * @see 描述： 根据证件号码查询已用的贷款
 *
 * @see 创建日期： 2015年06月26日上午 10:26:03
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQMcLoanListByIdNo {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;

	@PersistenceContext
	private EntityManager em;
	private QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;

	/**
	 * @see 方法名：handler
	 * @see 描述：根据证件号码查询已用的贷款handler
	 * @see 创建日期：2015年6月26日上午10:36:20
	 * @author yanjingfeng
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@Transactional
	public S20022Resp handler(S20022Req req) throws ProcessException {
		LogTools.printLogger(logger, "S20022", "证件号码查询已用的贷款", req, true);
		S20022Resp resp = new S20022Resp();
		// 验证上送报文字段
		if (!CheckUtil.isIdNo(req.getId_type(), req.getId_no())) {
			throw new ProcessException(Constants.ERRB021_CODE, Constants.ERRB021_MES);
		}
		CcsCustomer customer = custAcctCardQueryFacility.getCustomerById(req.getId_no(), req.getId_type());
		CheckUtil.rejectNull(customer, Constants.ERRB015_CODE, Constants.ERRB015_MES);
		List<CcsAcct> CcsAcctList = custAcctCardQueryFacility.getAcctByCustId(customer.getCustId());
		CcsAcct CcsAcct = null;
		// 得到小额贷账户
		for (CcsAcct _CcsAcct : CcsAcctList) {
			if (_CcsAcct.getAcctType().equals(AccountType.E)) {
				CcsAcct = _CcsAcct;
			} else {
				continue;
			}
		}
		CheckUtil.rejectNull(CcsAcct, Constants.ERRC012_CODE, Constants.ERRC012_MES);
		BooleanExpression booleanExpression = qCcsLoan.acctNbr.eq(CcsAcct.getAcctNbr()).and(qCcsLoan.acctType.eq(CcsAcct.getAcctType()))
				.and(qCcsLoan.org.eq(OrganizationContextHolder.getCurrentOrg()));
		JPAQuery query = new JPAQuery(em);
		List<CcsLoan> loanList = query.from(qCcsLoan).where(booleanExpression).orderBy(qCcsLoan.registerDate.desc()).list(qCcsLoan);
		if (loanList.isEmpty()) {
			throw new ProcessException(Constants.ERRC013_CODE, Constants.ERRC013_MES);
		}

		ArrayList<S20022Loan> s20022LoanList = new ArrayList<S20022Loan>();
		for (CcsLoan loan : loanList) {
			S20022Loan s20022Loan = new S20022Loan();
			s20022Loan.setLoan_id(loan.getLoanId());
			s20022Loan.setRegister_date(loan.getRegisterDate());
			s20022Loan.setRequest_time(loan.getRequestTime());
			s20022Loan.setLoan_type(loan.getLoanType());
			s20022Loan.setLoan_status(loan.getLoanStatus());
			s20022Loan.setLast_loan_status(loan.getLastLoanStatus());
			s20022Loan.setLoan_init_term(loan.getLoanInitTerm());
			s20022Loan.setCurr_term(loan.getCurrTerm());
			s20022Loan.setRemain_term(loan.getRemainTerm());
			s20022Loan.setLoan_init_prin(loan.getLoanInitPrin());
			s20022Loan.setActivate_date(loan.getActiveDate());
			s20022Loan.setPaid_out_date(loan.getPaidOutDate());
			s20022Loan.setTerminate_date(loan.getTerminalDate());
			s20022Loan.setTerminate_reason_cd(loan.getTerminalReasonCd());
			s20022Loan.setPrin_paid(loan.getPaidPrincipal());
			s20022Loan.setInt_paid(loan.getPaidInterest());
			s20022Loan.setFee_paid(loan.getPaidFee());
			s20022Loan.setLoan_curr_bal(loan.getLoanCurrBal());
			s20022Loan.setLoan_prin_xfrin(loan.getLoanPrinXfrin());
			s20022Loan.setLoan_fee1_xfrout(loan.getLoanFeeXfrout());
			s20022Loan.setLoan_fee1_xfrin(loan.getLoanFeeXfrin());
			s20022Loan.setLoan_code(loan.getLoanCode());
			s20022Loan.setRegister_id(loan.getRegisterId());
			s20022Loan.setResch_init_prin(loan.getExtendInitPrin());
			s20022Loan.setResch_date(loan.getExtendDate());
			s20022Loan.setBef_resch_init_term(loan.getBefExtendInitTerm());
			s20022Loan.setBef_resch_init_fee1(loan.getBefExtendInitFee());
			s20022Loan.setLoan_fee_method(loan.getLoanFeeMethod());
			s20022Loan.setInterest_rate(loan.getInterestRate());
			s20022Loan.setPenalty_rate(loan.getPenaltyRate());
			s20022Loan.setCompound_rate(loan.getCompoundRate());
			s20022Loan.setFloat_rate(loan.getFloatRate());
			s20022Loan.setLoan_receipt_nbr(loan.getDueBillNo());
			s20022Loan.setLoan_expire_date(loan.getLoanExpireDate());
			s20022Loan.setLoan_cd(loan.getLoanCode());
			s20022Loan.setPayment_hist(loan.getPaymentHst());
			s20022Loan.setCtd_payment_amt(loan.getCtdRepayAmt());
			s20022Loan.setPast_resch_cnt(loan.getPastExtendCnt());
			// s20022Loan.setPast_systolic_cnt(loan.getPastSystolicCnt());
			s20022Loan.setAdv_pmt_amt(loan.getAdvPmtAmt());

			s20022LoanList.add(s20022Loan);
		}
		resp.setId_no(req.getId_no());
		resp.setId_type(req.getId_type());
		resp.setLoans(s20022LoanList);
		LogTools.printLogger(logger, "S20022", "贷款卡号查询已用的贷款", resp, false);
		return resp;
	}
}
