package com.sunline.ccs.service.handler.sub;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.service.protocol.S13083Loan;
import com.sunline.ccs.service.protocol.S13083Req;
import com.sunline.ccs.service.protocol.S13083Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.PageTools;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQLoanRegList
 * @see 描述：当日分期交易信息查询
 *
 * @see 创建日期： 2015年6月24日 下午6:49:50
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQLoanRegList {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@PersistenceContext
	private EntityManager em;
	QCcsCard qCcsCard = QCcsCard.ccsCard;
	QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;
	QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;

	@Transactional
	public S13083Resp handler(S13083Req req) throws ProcessException {
		LogTools.printLogger(logger, "S13083", "当日分期交易信息查询", req, true);

		// 校验上送域
		CheckUtil.checkCardNo(req.getCard_no());

		// 主查询
		JPAQuery query = new JPAQuery(em);
		BooleanExpression booleanExpression = qCcsLoanReg.acctNbr.eq(qCcsCard.acctNbr).and(qCcsCardLmMapping.logicCardNbr.eq(qCcsCard.logicCardNbr))
				.and(qCcsCardLmMapping.cardNbr.eq(req.getCard_no()));

		List<CcsLoanReg> loanRegList = query.from(qCcsCardLmMapping, qCcsCard, qCcsLoanReg).where(booleanExpression).orderBy(qCcsLoanReg.registerDate.desc())
				.offset(req.getFirstrow()).limit(PageTools.calculateLmt(req.getFirstrow(), req.getLastrow())).list(qCcsLoanReg);

		ArrayList<S13083Loan> loans = new ArrayList<S13083Loan>();
		for (CcsLoanReg loanReg : loanRegList) {
			S13083Loan s13083Loan = new S13083Loan();
			SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
			s13083Loan.setCard_no(loanReg.getCardNbr());
			s13083Loan.setRegister_id(loanReg.getRegisterId());
			s13083Loan.setRegister_date(loanReg.getRegisterDate());
			s13083Loan.setRequest_time(sdf.format(loanReg.getRequestTime()));
			s13083Loan.setLoan_type(loanReg.getLoanType());
			s13083Loan.setLoan_reg_status(loanReg.getLoanRegStatus());
			s13083Loan.setLoan_action(loanReg.getLoanAction());
			s13083Loan.setLoan_init_term(loanReg.getLoanInitTerm());
			s13083Loan.setLoan_init_prin(loanReg.getLoanInitPrin());
			s13083Loan.setLoan_fixed_pmt_prin(loanReg.getLoanFixedPmtPrin());
			s13083Loan.setLoan_first_term_prin(loanReg.getLoanFirstTermPrin());
			s13083Loan.setLoan_final_term_prin(loanReg.getLoanFinalTermPrin());
			s13083Loan.setLoan_init_fee1(loanReg.getLoanInitFee());
			s13083Loan.setLoan_fixed_fee1(loanReg.getLoanFixedFee());
			s13083Loan.setLoan_first_term_fee1(loanReg.getLoanFirstTermFee());
			s13083Loan.setLoan_final_term_fee1(loanReg.getLoanFinalTermFee());
			s13083Loan.setLoan_fee_method(loanReg.getLoanFeeMethod());
			// 增加分期手续费率的显示
			s13083Loan.setInterest_rate(loanReg.getInterestRate());

			loans.add(s13083Loan);
		}

		// 总记录数
		JPAQuery totalQuery = new JPAQuery(em);
		int totalRows = (int) totalQuery.from(qCcsCardLmMapping, qCcsCard, qCcsLoanReg).where(booleanExpression).count();

		// 构建响应报文对象
		S13083Resp resp = new S13083Resp();
		resp.setNextpage_flg(PageTools.hasNextPage(req.getLastrow(), totalRows));
		resp.setTotal_rows(totalRows);
		resp.setFirstrow(req.getFirstrow());
		resp.setLastrow(req.getLastrow());
		resp.setCard_no(req.getCard_no());
		resp.setLoans(loans);

		LogTools.printLogger(logger, "S13083", "当日分期交易信息查询", resp, false);
		return resp;
	}

}
