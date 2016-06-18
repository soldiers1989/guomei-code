package com.sunline.ccs.service.handler.sub;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils.ShortedResult;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S20090Req;
import com.sunline.ccs.service.protocol.S20090Resp;
import com.sunline.ccs.service.protocol.S20090Schedule;
import com.sunline.ccs.service.provide.loan.MicroCreditProvide;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNRMcLoanShorten
 * @see 描述： 贷款缩期
 *
 * @see 创建日期： 2015年06月26日上午 10:26:03
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNRMcLoanShorten {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;
	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private GlobalManagementService globalManagementService;
	@Autowired
	private MicroCreditRescheduleUtils microCreditRescheduleUtils;

	@Autowired
	private RCcsLoanReg rCcsLoanReg;

	@PersistenceContext
	private EntityManager em;
	private QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
	private QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;

	/**
	 * @see 方法名：handler
	 * @see 描述：贷款缩期handler
	 * @see 创建日期：2015年6月26日上午11:05:44
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
	public S20090Resp handler(S20090Req req) throws ProcessException {
		LogTools.printLogger(logger, "S20090", "贷款缩期", req, true);

		// 请求报文校验
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.checkLoanReceiptNbr(req.getLoan_receipt_nbr());
		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO) && !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)) {
			throw new ProcessException(Constants.ERRS004_CODE, Constants.ERRS004_MES);
		}
		if (req.getShorted_resc_type() == null) {
			throw new ProcessException(Constants.ERRC030_CODE, Constants.ERRC030_MES);
		}
		if (!req.getShorted_resc_type().equals(S20090Req.SHORTEDTYPE_A) && !req.getShorted_resc_type().equals(S20090Req.SHORTEDTYPE_T)
				&& !req.getShorted_resc_type().equals(S20090Req.SHORTEDTYPE_S)) {
			throw new ProcessException(Constants.ERRC032_CODE, Constants.ERRC032_MES);
		}

		// 得到联机日期
		Date businessDate = globalManagementService.getSystemStatus().getBusinessDate();

		CcsCard CcsCard = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		CcsAcct CcsAcct = custAcctCardQueryFacility.getAcctByCardNbrCurrency(CcsCard.getLogicCardNbr(), Constants.CURR_CD_156);
		CcsAcctO CcsAcctO = custAcctCardQueryFacility.getAcctOByCardNbr(req.getCard_no(), CcsAcct.getAcctType());
		CheckUtil.rejectNull(CcsAcct, Constants.ERRS001_CODE, Constants.ERRS001_MES);
		CheckUtil.rejectNull(CcsAcctO, Constants.ERRS001_CODE, Constants.ERRS001_MES);

		// 如果申请缩期当天为账单日，则拒绝
		// int dayOfDate = DateTools.getDayOfDate(businessDate);
		// String day = String.format("%02d", dayOfDate);
		// String billingCycle = CcsAcct.getCycleDay();
		// if(day.equals(billingCycle)){
		// throw new ProcessException(Constants.ERRC020_CODE,
		// Constants.ERRC020_MES);
		// }

		// 已申请缩期不行
		CcsLoanReg loanReg = rCcsLoanReg.findOne(qCcsLoanReg.dueBillNo.eq(req.getLoan_receipt_nbr())
				.and(qCcsLoanReg.logicCardNbr.eq(CcsCard.getLogicCardNbr())).and(qCcsLoanReg.org.eq(OrganizationContextHolder.getCurrentOrg()))
				.and(qCcsLoanReg.loanRegStatus.in(LoanRegStatus.N, LoanRegStatus.A)));
		CheckUtil.rejectNotNull(loanReg, Constants.ERRC033_COED, Constants.ERRC033_MES);

		// 查询贷款信息
		JPAQuery query = new JPAQuery(em);
		CcsLoan loan = query
				.from(qCcsLoan)
				.where(qCcsLoan.logicCardNbr.eq(CcsCard.getLogicCardNbr()).and(qCcsLoan.dueBillNo.eq(req.getLoan_receipt_nbr()))
						.and(qCcsLoan.org.eq(OrganizationContextHolder.getCurrentOrg()))).singleResult(qCcsLoan);
		CheckUtil.rejectNull(loan, Constants.ERRC016_CODE, Constants.ERRC016_MES);

		// 不是活动状态不让缩
		if (loan.getLoanStatus() != LoanStatus.A) {
			throw new ProcessException(Constants.ERRC022_CODE, Constants.ERRC022_MES);
		}
		// 不是等额本息、等额本金不让缩
		if (loan.getLoanType() != LoanType.MCEI && loan.getLoanType() != LoanType.MCEP) {
			throw new ProcessException(Constants.ERRC028_CODE, Constants.ERRC028_MES);
		}
		// 0期不让缩
		if (loan.getCurrTerm() < 1) {
			throw new ProcessException(Constants.ERRC021_CODE, Constants.ERRC021_MES);
		}
		// 只剩1期不让缩
		if (loan.getCurrTerm() >= loan.getLoanInitTerm() - 2) {
			throw new ProcessException(Constants.ERRC047_CODE, Constants.ERRC047_MES);
		}

		LoanPlan loanPlan = unifiedParameterFacilityProvide.loanPlan(loan.getLoanCode());
		// 缩期前定价参数(区间查找)
		LoanFeeDef loanFeeDef = microCreditRescheduleUtils.getLoanFeeDef(loan.getLoanCode(), loan.getLoanInitTerm(), 
				loan.getLoanInitPrin(),loan.getLoanFeeDefId());

		// 不还钱不行
		if (req.getLoan_pmt_due() == null || req.getLoan_pmt_due().compareTo(BigDecimal.ZERO) == 0) {
			throw new ProcessException(Constants.ERRC035_CODE, Constants.ERRC035_MES);
		}
		// 还少了不行
		if (req.getLoan_pmt_due().compareTo(loanFeeDef.shortedMinPmtDue) < 0) {
			throw new ProcessException(Constants.ERRC041_CODE, Constants.ERRC041_MES);
		}
		// 还多了也不行
		List<CcsRepaySchedule> schedules = new ArrayList<CcsRepaySchedule>();
		CcsRepaySchedule origNextSchedule = microCreditRescheduleUtils.getRepayScheduleByTerm(loan.getLoanId(), loan.getCurrTerm() + 1);
		if (req.getLoan_pmt_due().compareTo(loan.getUnstmtPrin().subtract(origNextSchedule.getLoanTermPrin())) >= 0) {
			throw new ProcessException(Constants.ERRC049_CODE, Constants.ERRC049_MES);
		}
		// 就不行
		if (!loanFeeDef.shortedRescInd) {
			throw new ProcessException(Constants.ERRC034_CODE, Constants.ERRC034_MES);
		}
		// 剩余本金太少不行
		if (loan.getUnstmtPrin().compareTo(loanFeeDef.shorteRescdMinAmount) < 0) {
			throw new ProcessException(Constants.ERRC042_CODE, Constants.ERRC042_MES);
		}

		// 要缩期到期数的贷款定价参数
		LoanFeeDef loanFeeDefNew = null;
		// 缩期后期数
		Integer shortedTerm = null;

		// A-期数不变，每月还款额降低
		if (S20090Req.SHORTEDTYPE_A.equals(req.getShorted_resc_type())) {
			shortedTerm = loan.getLoanInitTerm();
			loanFeeDefNew = loanFeeDef;
		}
		// T-每月还款额不变，期数减少
		else if (S20090Req.SHORTEDTYPE_T.equals(req.getShorted_resc_type())) {
			BigDecimal origPmt = origNextSchedule.getLoanTermPrin().add(origNextSchedule.getLoanTermInt());
			ShortedResult rt = microCreditRescheduleUtils.calcShortedResult(loan, req.getLoan_pmt_due(), origPmt, origNextSchedule.getLoanTermPrin(),
					new BigDecimal(999999999), loan.getLoanInitTerm(), origPmt);
			shortedTerm = loan.getCurrTerm() + rt.getTerm();
			loanFeeDefNew = microCreditRescheduleUtils.getLoanFeeDef(loan.getLoanCode(), shortedTerm, 
					loan.getUnstmtPrin().subtract(req.loan_pmt_due),loan.getLoanFeeDefId());
		}
		// S-指定缩期后期数，调整每月还款额
		else if (S20090Req.SHORTEDTYPE_S.equals(req.getShorted_resc_type())) {
			// 缩期后期数不能为空
			if (req.getShorted_resc_term() == null) {
				throw new ProcessException(Constants.ERRC029_CODE, Constants.ERRC029_MES);
			}
			// 缩期后期数应小于贷款原期数
			if (req.getShorted_resc_term().compareTo(loan.getLoanInitTerm()) >= 0) {
				throw new ProcessException(Constants.ERRC031_CODE, Constants.ERRC031_MES);
			}
			// 缩期后期数应大于贷款当前期数+1
			if (req.getShorted_resc_term().compareTo(loan.getCurrTerm() + 1) <= 0) {
				throw new ProcessException(Constants.ERRC048_CODE, Constants.ERRC048_MES);
			}
			// 缩期后期数不能小于贷款最短周期
			if (req.getShorted_resc_term().compareTo(loanPlan.minCycle) < 0) {
				throw new ProcessException(Constants.ERRC050_CODE, Constants.ERRC050_MES);
			}
			shortedTerm = req.getShorted_resc_term();
			loanFeeDefNew = microCreditRescheduleUtils.getLoanFeeDef(loan.getLoanCode(), shortedTerm, 
					loan.getUnstmtPrin().subtract(req.loan_pmt_due),loan.getLoanFeeDefId());
		}

		// 缩期手续费
		BigDecimal shortedServiceFee = microCreditRescheduleUtils.calcShortedFeeAmt(req.getLoan_pmt_due(), loanFeeDefNew);

		// 缩期申请
		CcsLoanReg shortenLoanReg = MicroCreditProvide.genCcsLoanReg(loan, CcsAcctO, loanFeeDefNew, businessDate, loanPlan);
		shortenLoanReg.setShortedPmtDue(req.getLoan_pmt_due());
		shortenLoanReg.setShortedTerm(shortedTerm);
		shortenLoanReg.setShortedType(req.getShorted_resc_type());
		shortenLoanReg.setRemark(req.getShorted_resc_reson());
		shortenLoanReg.setValidDate(req.getShorted_resc_vdate());
		shortenLoanReg.setLoanSvcFee(shortedServiceFee);
		shortenLoanReg.setLoanAction(LoanAction.S);

		if (req.getOpt().equals(Constants.OPT_ONE)) {
			rCcsLoanReg.save(shortenLoanReg);
			// TODO 短信
		}

		// 重新生成缩期后的还款明细
		if (loan.getLoanType() == LoanType.MCEI) {
			schedules = microCreditRescheduleUtils.genShortedForMCEI(shortenLoanReg, loan, CcsAcct.getNextStmtDate(), origNextSchedule, loanFeeDefNew);
		} else if (loan.getLoanType() == LoanType.MCEP) {
			schedules = microCreditRescheduleUtils.genShortedForMCEP(shortenLoanReg, loan, CcsAcct.getNextStmtDate(), origNextSchedule, loanFeeDefNew);
		}
		ArrayList<S20090Schedule> s20090Schedules = new ArrayList<S20090Schedule>();
		for (CcsRepaySchedule _schedule : schedules) {
			S20090Schedule schedule = new S20090Schedule();

			schedule.setCurr_term(_schedule.getCurrTerm());
			schedule.setLoan_grace_date(_schedule.getLoanGraceDate());
			schedule.setLoan_pmt_due_date(_schedule.getLoanPmtDueDate());
			schedule.setLoan_term_fee1(_schedule.getLoanTermFee());
			schedule.setLoan_term_interest(_schedule.getLoanTermInt().compareTo(BigDecimal.ZERO) > 0 ? _schedule.getLoanTermInt() : BigDecimal.ZERO);
			schedule.setLoan_term_prin(_schedule.getLoanTermPrin().compareTo(BigDecimal.ZERO) > 0 ? _schedule.getLoanTermPrin() : BigDecimal.ZERO);

			s20090Schedules.add(schedule);
		}

		S20090Resp resp = new S20090Resp();
		resp.setCard_no(req.getCard_no());
		resp.setLoan_init_prin(loan.getLoanInitPrin());
		resp.setLoan_init_term(loan.getLoanInitTerm());
		resp.setLoan_receipt_nbr(req.getLoan_receipt_nbr());
		resp.setShorted_resc_term(shortedTerm);
		resp.setShorted_resc_prin(loan.getUnstmtPrin().subtract(req.getLoan_pmt_due()));
		resp.setShorted_resc_vdate(req.getShorted_resc_vdate());
		resp.setSchedules(s20090Schedules);

		LogTools.printLogger(logger, "S20090", "贷款缩期", resp, false);
		return resp;
	}
}
