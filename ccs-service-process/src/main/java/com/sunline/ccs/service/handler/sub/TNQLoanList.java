package com.sunline.ccs.service.handler.sub;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S13080Loan;
import com.sunline.ccs.service.protocol.S13080Req;
import com.sunline.ccs.service.protocol.S13080Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.PageTools;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQLoanList
 * @see 描述：分期交易信息查询
 *
 * @see 创建日期： 2015年6月24日 下午3:31:51
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQLoanList {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@PersistenceContext
	private EntityManager em;

	QCcsCard qCcsCard = QCcsCard.ccsCard;
	QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;
	QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;

	@Transactional
	public S13080Resp handler(S13080Req req) throws ProcessException {
		LogTools.printLogger(logger, "S13080", "分期交易信息查询", req, true);

		// 校验上送域
		CheckUtil.checkCardNo(req.getCard_no());

		// 主查询
		JPAQuery query = new JPAQuery(em);
		BooleanExpression booleanExpression = qCcsLoan.acctNbr.eq(qCcsCard.acctNbr).and(qCcsCardLmMapping.logicCardNbr.eq(qCcsCard.logicCardNbr))
				.and(qCcsCardLmMapping.cardNbr.eq(req.getCard_no()));
		if (req.getStart_date() != null) {
			booleanExpression = booleanExpression.and(qCcsLoan.registerDate.goe(req.getStart_date()));
		}
		if (req.getEnd_date() != null) {
			booleanExpression = booleanExpression.and(qCcsLoan.registerDate.loe(req.getEnd_date()));
		}
		if (req.getStart_date() != null && req.getEnd_date() != null && DateUtils.truncatedCompareTo(req.getStart_date(), req.getEnd_date(), Calendar.DATE) > 0) {
			throw new ProcessException(Constants.ERRB004_CODE, Constants.ERRB004_MES);
		}

		List<CcsLoan> loanList = query.from(qCcsCardLmMapping, qCcsCard, qCcsLoan).where(booleanExpression).orderBy(qCcsLoan.registerDate.desc())
				.offset(req.getFirstrow()).limit(PageTools.calculateLmt(req.getFirstrow(), req.getLastrow())).list(qCcsLoan);

		ArrayList<S13080Loan> loans = new ArrayList<S13080Loan>();
		for (CcsLoan loan : loanList) {
			S13080Loan s13080Loan = new S13080Loan();
			s13080Loan.setCard_no(loan.getCardNbr());
			s13080Loan.setRegister_id(loan.getRegisterId());
			s13080Loan.setRegister_date(loan.getRegisterDate());
			s13080Loan.setLoan_type(loan.getLoanType());
			s13080Loan.setLoan_status(loan.getLoanStatus());
			s13080Loan.setTerminate_reason_cd(loan.getTerminalReasonCd());
			s13080Loan.setLoan_init_term(loan.getLoanInitTerm());
			s13080Loan.setCurr_term(loan.getCurrTerm());
			s13080Loan.setLoan_init_prin(loan.getLoanInitPrin());
			s13080Loan.setLoan_fixed_pmt_prin(loan.getLoanFixedPmtPrin());
			s13080Loan.setLoan_first_term_prin(loan.getLoanFirstTermPrin());
			s13080Loan.setLoan_final_term_prin(loan.getLoanFinalTermPrin());
			s13080Loan.setLoan_init_fee1(loan.getLoanInitFee());
			s13080Loan.setLoan_fixed_fee1(loan.getLoanFixedFee());
			s13080Loan.setLoan_first_term_fee1(loan.getLoanFirstTermFee());
			s13080Loan.setLoan_final_term_fee1(loan.getLoanFinalTermFee());
			s13080Loan.setActivate_date(loan.getActiveDate());
			s13080Loan.setPrin_paid(loan.getPaidPrincipal());
			s13080Loan.setInt_paid(loan.getPaidInterest());
			s13080Loan.setFee_paid(loan.getPaidFee());
			s13080Loan.setLoan_curr_bal(loan.getLoanCurrBal());
			s13080Loan.setLoan_bal_xfrout(loan.getLoanBalXfrout());
			s13080Loan.setLoan_bal_xfrin(loan.getLoanBalXfrin());
			s13080Loan.setLoan_prin_xfrout(loan.getLoanPrinXfrout());
			s13080Loan.setLoan_prin_xfrin(loan.getLoanPrinXfrin());
			s13080Loan.setLoan_fee1_xfrout(loan.getLoanFeeXfrout());
			s13080Loan.setLoan_fee1_xfrin(loan.getLoanFeeXfrin());
			s13080Loan.setLoan_code(loan.getLoanCode());
			s13080Loan.setLoan_fee_method(loan.getLoanFeeMethod());
			// 增加分期手续费率的显示
			s13080Loan.setInterest_rate(loan.getInterestRate());
			Date stmtDate = loan.getRegisterDate();
			if (loan.getLoanType() == LoanType.B) {
				CcsAcct acct = custAcctCardQueryFacility.getAcctByAcctNbr(loan.getAcctType(), loan.getAcctNbr());
				Integer billingCycle = Integer.parseInt(acct.getCycleDay());
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				Integer sd = Integer.parseInt(sdf.format(stmtDate).substring(6, 8));
				if (billingCycle > sd) {
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(stmtDate.getTime());
					calendar.add(Calendar.MONTH, -1);
					stmtDate = new Date(calendar.getTimeInMillis());
				}
			}
			s13080Loan.setStmt_date(stmtDate);
			loans.add(s13080Loan);
		}

		// 总记录数
		JPAQuery totalQuery = new JPAQuery(em);
		int totalRows = (int) totalQuery.from(qCcsCardLmMapping, qCcsCard, qCcsLoan).where(booleanExpression).count();

		// 构建响应报文对象
		S13080Resp resp = new S13080Resp();
		resp.setNextpage_flg(PageTools.hasNextPage(req.getLastrow(), totalRows));
		resp.setTotal_rows(totalRows);
		resp.setFirstrow(req.getFirstrow());
		resp.setLastrow(req.getLastrow());
		resp.setCard_no(req.getCard_no());
		resp.setStart_date(req.getStart_date());
		resp.setEnd_date(req.getEnd_date());
		resp.setLoans(loans);

		LogTools.printLogger(logger, "S13080", "分期交易信息查询", resp, false);
		return resp;
	}

}
