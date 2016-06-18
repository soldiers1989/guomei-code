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
import com.sunline.ccs.infrastructure.shared.model.CcsRepayScheduleHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsRepayScheduleHst;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S20025Req;
import com.sunline.ccs.service.protocol.S20025Resp;
import com.sunline.ccs.service.protocol.S20025ScheduleHst;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQMcRepayScheduleHst
 * @see 描述： 根据借据号查询贷款还款分配历史
 *
 * @see 创建日期： 2015年06月26日上午 10:26:03
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQMcRepayScheduleHst {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;

	@PersistenceContext
	private EntityManager em;
	private QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
	private QCcsRepayScheduleHst qCcsRepayScheduleHst = QCcsRepayScheduleHst.ccsRepayScheduleHst;

	/**
	 * @see 方法名：handler
	 * @see 描述：根据借据号查询贷款还款分配历史handler
	 * @see 创建日期：2015年6月26日上午10:47:44
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
	public S20025Resp handler(S20025Req req) throws ProcessException {
		LogTools.printLogger(logger, "S20025", "根据借据号查询贷款还款分配历史", req, true);
		S20025Resp resp = new S20025Resp();

		// 检查上送请求报文字段
		CheckUtil.checkLoanReceiptNbr(req.getLoan_receipt_nbr());
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.rejectNull(req.getRegister_hst_id(), Constants.ERRL050_CODE, Constants.ERRL050_MES);
		CcsCard CcsCard = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		BooleanExpression booleanExpression = qCcsLoan.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsLoan.dueBillNo.eq(req.getLoan_receipt_nbr()))
				.and(qCcsLoan.logicCardNbr.eq(CcsCard.getLogicCardNbr()));
		JPAQuery query = new JPAQuery(em);
		CcsLoan loan = query.from(qCcsLoan).where(booleanExpression).singleResult(qCcsLoan);
		CheckUtil.rejectNull(loan, Constants.ERRC016_CODE, Constants.ERRC016_MES);

		JPAQuery queryList = new JPAQuery(em);

		List<CcsRepayScheduleHst> tmScheduleList = queryList.from(qCcsRepayScheduleHst)
				.where(qCcsRepayScheduleHst.loanId.eq(loan.getLoanId()).and(qCcsRepayScheduleHst.registerId.eq(Long.valueOf(req.register_hst_id))))
				.list(qCcsRepayScheduleHst);
		if (tmScheduleList.isEmpty()) {
			throw new ProcessException(Constants.ERRC018_CODE, Constants.ERRC018_MES);
		}

		ArrayList<S20025ScheduleHst> tmSchedulesHsts = new ArrayList<S20025ScheduleHst>();

		for (CcsRepayScheduleHst tmScheduleHst : tmScheduleList) {
			S20025ScheduleHst scheduleHst = new S20025ScheduleHst();
			scheduleHst.setLoan_grace_date(tmScheduleHst.getLoanGraceDate());
			scheduleHst.setLoan_pmt_due_date(tmScheduleHst.getLoanPmtDueDate());
			scheduleHst.setLoan_term_fee1(tmScheduleHst.getLoanTermFee());
			scheduleHst.setLoan_term_interest(tmScheduleHst.getLoanTermInt());
			scheduleHst.setLoan_term_prin(tmScheduleHst.getLoanTermPrin());
			scheduleHst.setTerm_nbr(tmScheduleHst.getCurrTerm());// 当前期数
			tmSchedulesHsts.add(scheduleHst);
		}
		resp.setScheduleHsts(tmSchedulesHsts);
		resp.setCard_no(req.getCard_no());
		resp.setRegister_hst_id(req.getRegister_hst_id());

		LogTools.printLogger(logger, "20025", "根据借据号查询贷款还款分配历史", resp, false);
		return resp;
	}
}
