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
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S20020Loan;
import com.sunline.ccs.service.protocol.S20020Req;
import com.sunline.ccs.service.protocol.S20020Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQMcLoanListByCardNbr
 * @see 描述： 根据贷款卡号查询已用的贷款
 *
 * @see 创建日期： 2015年06月26日上午 10:26:03
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQMcLoanListByCardNbr {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;

	@PersistenceContext
	private EntityManager em;
	private QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;

	/**
	 * @see 方法名：handler
	 * @see 描述：根据贷款卡号查询已用的贷款handler
	 * @see 创建日期：2015年6月26日上午10:31:40
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
	public S20020Resp handler(S20020Req req) throws ProcessException {
		LogTools.printLogger(logger, "S20020", "根据贷款卡号查询已用的贷款", req, true);
		S20020Resp resp = new S20020Resp();

		// 验证上送报文字段
		CheckUtil.checkCardNo(req.getCard_no());
		CcsCard CcsCard = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		BooleanExpression booleanExpression = qCcsLoan.logicCardNbr.eq(CcsCard.getLogicCardNbr()).and(
				qCcsLoan.org.eq(OrganizationContextHolder.getCurrentOrg()));
		JPAQuery query = new JPAQuery(em);
		List<CcsLoan> loanList = query.from(qCcsLoan).where(booleanExpression).orderBy(qCcsLoan.registerDate.desc()).list(qCcsLoan);
		if (loanList.isEmpty()) {
			throw new ProcessException(Constants.ERRC013_CODE, Constants.ERRC013_MES);
		}

		ArrayList<S20020Loan> s20020LoanList = new ArrayList<S20020Loan>();
		for (CcsLoan loan : loanList) {
			S20020Loan s20020Loan = new S20020Loan();
			s20020Loan.setLoan_id(loan.getLoanId());
			s20020Loan.setRegister_date(loan.getRegisterDate());
			s20020Loan.setRequest_time(loan.getRequestTime());
			s20020Loan.setLoan_type(loan.getLoanType());
			s20020Loan.setLoan_status(loan.getLoanStatus());
			s20020Loan.setLast_loan_status(loan.getLastLoanStatus());
			s20020Loan.setLoan_init_term(loan.getLoanInitTerm());
			s20020Loan.setCurr_term(loan.getCurrTerm());
			s20020Loan.setRemain_term(loan.getRemainTerm());
			s20020Loan.setLoan_init_prin(loan.getLoanInitPrin());
			s20020Loan.setActivate_date(loan.getActiveDate());
			s20020Loan.setPaid_out_date(loan.getPaidOutDate());
			s20020Loan.setTerminate_date(loan.getTerminalDate());
			s20020Loan.setTerminate_reason_cd(loan.getTerminalReasonCd());
			s20020Loan.setPrin_paid(loan.getPaidPrincipal());
			s20020Loan.setInt_paid(loan.getPaidInterest());
			loan.getPaidFee();
			s20020Loan.setFee_paid(loan.getPaidFee());
			s20020Loan.setLoan_curr_bal(loan.getLoanCurrBal());
			s20020Loan.setLoan_prin_xfrin(loan.getLoanPrinXfrin());
			s20020Loan.setLoan_fee1_xfrout(loan.getLoanFeeXfrout());
			s20020Loan.setLoan_fee1_xfrin(loan.getLoanFeeXfrin());
			s20020Loan.setLoan_code(loan.getLoanCode());
			s20020Loan.setRegister_id(loan.getRegisterId());
			s20020Loan.setResch_init_prin(loan.getExtendInitPrin());
			s20020Loan.setResch_date(loan.getExtendDate());
			s20020Loan.setBef_resch_init_term(loan.getBefExtendInitTerm());
			s20020Loan.setBef_resch_init_fee1(loan.getBefExtendInitFee());
			s20020Loan.setLoan_fee_method(loan.getLoanFeeMethod());
			s20020Loan.setInterest_rate(loan.getInterestRate());
			s20020Loan.setPenalty_rate(loan.getPenaltyRate());
			s20020Loan.setCompound_rate(loan.getCompoundRate());
			s20020Loan.setFloat_rate(loan.getFloatRate());
			s20020Loan.setLoan_receipt_nbr(loan.getDueBillNo());
			s20020Loan.setLoan_expire_date(loan.getLoanExpireDate());
			s20020Loan.setLoan_cd(loan.getLoanCode());
			s20020Loan.setPayment_hist(loan.getPaymentHst());
			loan.getPastExtendCnt();
			s20020Loan.setCtd_payment_amt(loan.getCtdRepayAmt());
			s20020Loan.setPast_resch_cnt(loan.getPastExtendCnt());
			// loan.setPast_systolic_cnt(loan.getPastSystolicCnt());
			s20020Loan.setAdv_pmt_amt(loan.getAdvPmtAmt());

			s20020LoanList.add(s20020Loan);
		}
		resp.setCard_no(req.getCard_no());
		resp.setLoans(s20020LoanList);
		LogTools.printLogger(logger, "S20020", "贷款卡号查询已用的贷款", resp, false);
		return resp;
	}
}
