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
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsRepaySchedule;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S20023Req;
import com.sunline.ccs.service.protocol.S20023Resp;
import com.sunline.ccs.service.protocol.S20023Schedule;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQMcRepaySchedule
 * @see 描述： 根据借据号查询还款计划表
 *
 * @see 创建日期： 2015年06月26日上午 10:26:03
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQMcRepaySchedule {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;

	@PersistenceContext
	private EntityManager em;
	private QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
	private QCcsRepaySchedule qCcsRepaySchedule = QCcsRepaySchedule.ccsRepaySchedule;

	/**
	 * @see 方法名：handler
	 * @see 描述：根据借据号查询还款计划表handler
	 * @see 创建日期：2015年6月26日上午10:42:32
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
	public S20023Resp handler(S20023Req req) throws ProcessException {
		LogTools.printLogger(logger, "S20023", "根据借据号查询还款计划表", req, true);
		S20023Resp resp = new S20023Resp();

		// 检查上送请求报文字段
		CheckUtil.checkLoanReceiptNbr(req.getLoan_receipt_nbr());
		CheckUtil.checkCardNo(req.getCard_no());

		CcsCard CcsCard = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		BooleanExpression booleanExpression = qCcsLoan.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsLoan.dueBillNo.eq(req.getLoan_receipt_nbr()))
				.and(qCcsLoan.logicCardNbr.eq(CcsCard.getLogicCardNbr()));
		JPAQuery query = new JPAQuery(em);
		CcsLoan loan = query.from(qCcsLoan).where(booleanExpression).singleResult(qCcsLoan);
		CheckUtil.rejectNull(loan, Constants.ERRC016_CODE, Constants.ERRC016_MES);

		JPAQuery queryList = new JPAQuery(em);

		List<CcsRepaySchedule> tmScheduleList = queryList.from(qCcsRepaySchedule).where(qCcsRepaySchedule.loanId.eq(loan.getLoanId())).list(qCcsRepaySchedule);
		if (tmScheduleList.isEmpty()) {
			throw new ProcessException(Constants.ERRC015_CODE, Constants.ERRC015_MES);
		}

		ArrayList<S20023Schedule> tmSchedules = new ArrayList<S20023Schedule>();

		for (CcsRepaySchedule tmSchedule : tmScheduleList) {
			S20023Schedule schedule = new S20023Schedule();
			schedule.setLoan_grace_date(tmSchedule.getLoanGraceDate());
			schedule.setLoan_pmt_due_date(tmSchedule.getLoanPmtDueDate());
			schedule.setLoan_term_fee1(tmSchedule.getLoanTermFee());
			schedule.setLoan_term_interest(tmSchedule.getLoanTermInt());
			schedule.setLoan_term_prin(tmSchedule.getLoanTermPrin());
			schedule.setTerm_nbr(tmSchedule.getCurrTerm());// 当前期数
			tmSchedules.add(schedule);
		}
		resp.setSchedules(tmSchedules);
		resp.setCard_no(req.getCard_no());

		LogTools.printLogger(logger, "20023", "根据借据号查询还款计划表", resp, false);
		return resp;
	}
}
