package com.sunline.ccs.service.handler.sub;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S20021Req;
import com.sunline.ccs.service.protocol.S20021Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQMcLoan
 * @see 描述： 根据贷款借据号查询贷款信息
 *
 * @see 创建日期： 2015年06月26日上午 10:26:03
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQMcLoan {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@PersistenceContext
	private EntityManager em;
	private QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;

	/**
	 * @see 方法名：handler
	 * @see 描述：根据贷款借据号查询贷款信息handler
	 * @see 创建日期：2015年6月26日上午10:33:42
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
	public S20021Resp handler(S20021Req req) throws ProcessException {
		LogTools.printLogger(logger, "S20021", "根据贷款借据号查询贷款信息", req, true);
		S20021Resp resp = new S20021Resp();

		// 校验上送报文域
		CheckUtil.checkLoanReceiptNbr(req.getLoan_receipt_nbr());

		BooleanExpression booleanExpression = qCcsLoan.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsLoan.dueBillNo.eq(req.getLoan_receipt_nbr()));
		JPAQuery query = new JPAQuery(em);
		CcsLoan loan = query.from(qCcsLoan).where(booleanExpression).singleResult(qCcsLoan);
		CheckUtil.rejectNull(loan, Constants.ERRC016_CODE, Constants.ERRC016_MES);

		// 构建响应报文对象
		resp.setRequest_time(loan.getRequestTime());
		resp.setLoan_receipt_nbr(loan.getDueBillNo());
		resp.setLoan_receipt_nbr(req.getLoan_receipt_nbr());
		resp.setLoan_id(loan.getLoanId());
		resp.setRegister_date(loan.getRegisterDate());
		resp.setRemain_term(loan.getRemainTerm());
		resp.setLoan_type(loan.getLoanType());
		resp.setLoan_status(loan.getLoanStatus());
		resp.setLast_loan_status(loan.getLastLoanStatus());
		resp.setLoan_init_term(loan.getLoanInitTerm());
		resp.setCurr_term(loan.getCurrTerm());
		resp.setRemain_term(loan.getRemainTerm());
		resp.setLoan_init_prin(loan.getLoanInitPrin());
		resp.setActivate_date(loan.getActiveDate());
		resp.setPaid_out_date(loan.getPaidOutDate());
		resp.setTerminate_date(loan.getTerminalDate());
		resp.setTerminate_reason_cd(loan.getTerminalReasonCd());
		resp.setPrin_paid(loan.getPaidPrincipal());
		resp.setInt_paid(loan.getPaidInterest());
		resp.setFee_paid(loan.getPaidFee());
		resp.setLoan_curr_bal(loan.getLoanCurrBal());
		resp.setLoan_prin_xfrin(loan.getLoanPrinXfrin());
		resp.setLoan_fee1_xfrout(loan.getLoanFeeXfrout());
		resp.setLoan_fee1_xfrin(loan.getLoanFeeXfrin());
		resp.setLoan_code(loan.getLoanCode());
		resp.setRegister_id(loan.getRegisterId());
		resp.setLoan_fee_method(loan.getLoanFeeMethod());
		resp.setInterest_rate(loan.getInterestRate());
		resp.setPenalty_rate(loan.getPenaltyRate());
		resp.setCompound_rate(loan.getCompoundRate());
		resp.setFloat_rate(loan.getFloatRate());
		resp.setLoan_receipt_nbr(loan.getDueBillNo());
		resp.setLoan_expire_date(loan.getLoanExpireDate());
		resp.setLoan_cd(loan.getLoanCode());
		resp.setPayment_hist(loan.getPaymentHst());
		resp.setCtd_payment_amt(loan.getCtdRepayAmt());
		resp.setPast_resch_cnt(loan.getPastExtendCnt());
		resp.setPast_systolic_cnt(loan.getPastShortenCnt());
		resp.setAdv_pmt_amt(loan.getAdvPmtAmt());
		LogTools.printLogger(logger, "S20021", "根据贷款借据号查询贷款信息", resp, false);
		return resp;
	}
}
