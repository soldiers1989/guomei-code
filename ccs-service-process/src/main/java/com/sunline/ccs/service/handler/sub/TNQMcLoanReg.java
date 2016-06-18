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
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S20026LoanReg;
import com.sunline.ccs.service.protocol.S20026Req;
import com.sunline.ccs.service.protocol.S20026Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQMcLoanReg
 * @see 描述： 当日贷款变更申请
 *
 * @see 创建日期： 2015年06月26日上午 10:26:03
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQMcLoanReg {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;

	@PersistenceContext
	private EntityManager em;
	private QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
	private QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;

	/**
	 * @see 方法名：handler
	 * @see 描述：当日贷款变更申请handler
	 * @see 创建日期：2015年6月26日上午10:50:55
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
	public S20026Resp handler(S20026Req req) throws ProcessException {
		LogTools.printLogger(logger, "S20026", "当日贷款申请查询", req, true);
		S20026Resp resp = new S20026Resp();

		// 检查上送请求报文字段
		CheckUtil.checkLoanReceiptNbr(req.getLoan_receipt_nbr());
		CheckUtil.checkCardNo(req.getCard_no());

		CcsCard CcsCard = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);
		CcsAcct CcsAcct = custAcctCardQueryFacility.getAcctByCardNbr(CcsCard.getLogicCardNbr(), AccountType.E);
		CheckUtil.rejectNull(CcsAcct, Constants.ERRC012_CODE, Constants.ERRC012_MES);

		// BooleanExpression booleanExpression =
		// qCcsLoanReg.logicCardNbr.eq(CcsCard.getLogicCardNbr()).and(qCcsLoanReg.acctNbr.eq(CcsAcct.getAcctNbr())
		// .and(qCcsLoanReg.org.eq(OrganizationContextHolder.getCurrentOrg())));

		BooleanExpression booleanExpression = qCcsLoanReg.logicCardNbr.eq(CcsCard.getLogicCardNbr())
				.and(qCcsLoanReg.org.eq(OrganizationContextHolder.getCurrentOrg())).and(qCcsLoanReg.dueBillNo.eq(req.getLoan_receipt_nbr()));

		JPAQuery query = new JPAQuery(em);
		List<CcsLoanReg> loanRegList = query.from(qCcsLoanReg).where(booleanExpression).list(qCcsLoanReg);
		if (loanRegList.isEmpty()) {
			throw new ProcessException(Constants.ERRC025_CODE, Constants.ERRC025_MES);
		}

		ArrayList<S20026LoanReg> loanRegs = new ArrayList<S20026LoanReg>();

		for (CcsLoanReg loanReg : loanRegList) {
			S20026LoanReg s20026LoanReg = new S20026LoanReg();

			BooleanExpression booleanExp = qCcsLoan.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsLoan.dueBillNo.eq(req.getLoan_receipt_nbr()));
			JPAQuery q = new JPAQuery(em);
			CcsLoan loan = q.from(qCcsLoan).where(booleanExp).singleResult(qCcsLoan);
			CheckUtil.rejectNull(loan, Constants.ERRC016_CODE, Constants.ERRC016_MES);

			if (loanReg.getLoanAction().equals(LoanAction.R)) {
				s20026LoanReg.setBef_resch_init_term(loan.getLoanInitTerm());// 展期前总期数
				s20026LoanReg.setBef_resch_init_prin(loan.getLoanInitPrin());// 展期前总本金=原贷款本金

			}
			if (loanReg.getLoanAction().equals(LoanAction.S)) {
				s20026LoanReg.setBef_shorted_init_term(loan.getLoanInitTerm());// 缩期前总期数
				s20026LoanReg.setBef_shorted_init_prin(loan.getLoanInitPrin());// 缩期前总本金=原贷款本金
			}
			s20026LoanReg.setRegister_id(loanReg.getRegisterId());
			s20026LoanReg.setRegister_date(loanReg.getRegisterDate());
			s20026LoanReg.setRequest_time(loanReg.getRequestTime());
			s20026LoanReg.setLoan_type(loanReg.getLoanType());
			s20026LoanReg.setLoan_reg_status(loanReg.getLoanRegStatus());
			s20026LoanReg.setLoan_init_term(loanReg.getLoanInitTerm());
			s20026LoanReg.setLoan_init_prin(loanReg.getLoanInitPrin());
			s20026LoanReg.setLoan_init_fee1(loanReg.getLoanFixedFee());
			s20026LoanReg.setLoan_fee_method(loanReg.getLoanFeeMethod());
			s20026LoanReg.setLoan_code(loanReg.getLoanCode());
			s20026LoanReg.setLoan_action(loanReg.getLoanAction());
			s20026LoanReg.setInterest_rate(loanReg.getInterestRate());
			s20026LoanReg.setPenalty_rate(loanReg.getPenaltyRate());
			s20026LoanReg.setCompound_rate(loanReg.getCompoundRate());
			s20026LoanReg.setFloat_rate(loanReg.getFloatRate());
			s20026LoanReg.setAdv_pmt_amt(loanReg.getAdvPmtAmt());
			s20026LoanReg.setValid_date(loanReg.getValidDate());
			s20026LoanReg.setReschedule_term(loanReg.getExtendTerm());
			s20026LoanReg.setShorted_term(loanReg.getShortedTerm());
			s20026LoanReg.setShorted_resc_type(loanReg.getShortedType());
			s20026LoanReg.setShorted_pmt_due(loanReg.getShortedPmtDue());
			s20026LoanReg.setRemark(loanReg.getRemark());

			loanRegs.add(s20026LoanReg);
		}
		resp.setLoanRegs(loanRegs);
		resp.setCard_no(req.getCard_no());
		resp.setLoan_receipt_nbr(req.getLoan_receipt_nbr());

		LogTools.printLogger(logger, "20026", "当日贷款申请查询", resp, false);
		return resp;
	}
}
