package com.sunline.ccs.service.handler.sub;

import java.math.BigDecimal;
import java.util.Date;

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
import com.sunline.ccs.service.protocol.S20011Req;
import com.sunline.ccs.service.protocol.S20011Resp;
import com.sunline.ccs.service.provide.loan.MicroCreditProvide;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNRMcLoanAdvance
 * @see 描述： 提前还款
 *
 * @see 创建日期： 2015年06月26日上午 10:19:55
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNRMcLoanAdvance {

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
	 * @see 描述：提前还款handler
	 * @see 创建日期：2015年6月26日上午10:23:30
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
	public S20011Resp handler(S20011Req req) throws ProcessException {
		LogTools.printLogger(logger, "S20011", "提前全额还款", req, true);

		// 请求报文校验
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.checkLoanReceiptNbr(req.getLoan_receipt_nbr());
		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO) && !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)) {
			throw new ProcessException(Constants.ERRS004_CODE, Constants.ERRS004_MES);
		}

		// 得到联机日期
		Date businessDate = globalManagementService.getSystemStatus().getBusinessDate();

		CcsCard CcsCard = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		CcsAcct CcsAcct = custAcctCardQueryFacility.getAcctByCardNbrCurrency(CcsCard.getLogicCardNbr(), Constants.CURR_CD_156);
		CcsAcctO CcsAcctO = custAcctCardQueryFacility.getAcctOByCardNbr(req.getCard_no(), CcsAcct.getAcctType());
		CheckUtil.rejectNull(CcsAcct, Constants.ERRS001_CODE, Constants.ERRS001_MES);
		CheckUtil.rejectNull(CcsAcctO, Constants.ERRS001_CODE, Constants.ERRS001_MES);

		// 如果申请当天为账单日，则拒绝
		// int dayOfDate = DateTools.getDayOfDate(businessDate);
		// String day = String.format("%02d", dayOfDate);
		// String billingCycle = CcsAcct.getCycleDay();
		// if(day.equals(billingCycle)){
		// throw new ProcessException(Constants.ERRC020_CODE,
		// Constants.ERRC020_MES);
		// }

		// 已申请全额还款不行
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

		// 不是活动状态、展期、缩期不让还
		if (loan.getLoanStatus() != LoanStatus.A && loan.getLoanStatus() != LoanStatus.R && loan.getLoanStatus() != LoanStatus.S) {
			throw new ProcessException(Constants.ERRC022_CODE, Constants.ERRC022_MES);
		}
		// 不是等额本息、等额本金不让还
		if (!loan.getLoanType().equals(LoanType.MCEI) && !loan.getLoanType().equals(LoanType.MCEP)) {
			throw new ProcessException(Constants.ERRC028_CODE, Constants.ERRC028_MES);
		}
		// 0期不让还
		if (loan.getCurrTerm() < 1) {
			throw new ProcessException(Constants.ERRC021_CODE, Constants.ERRC021_MES);
		}
		// 最后1期不让还
		if (loan.getCurrTerm() >= loan.getLoanInitTerm() - 1) {
			throw new ProcessException(Constants.ERRC036_CODE, Constants.ERRC036_MES);
		}

		// 获取loanPlan
		LoanPlan loanPlan = unifiedParameterFacilityProvide.loanPlan(loan.getLoanCode());
		// 提前还款定价参数(区间查找)
		LoanFeeDef loanFeeDef = microCreditRescheduleUtils.getLoanFeeDef(loan.getLoanCode(), loan.getCurrTerm() + 1, 
				loan.getUnstmtPrin(),loan.getLoanFeeDefId());

		// 提前还款申请
		CcsLoanReg payInAdvLoanReg = MicroCreditProvide.genCcsLoanReg(loan, CcsAcctO, loanFeeDef, businessDate, loanPlan);

		// 获取提前还款后还款计划
		CcsRepaySchedule origNextSchedule = microCreditRescheduleUtils.getRepayScheduleByTerm(loan.getLoanId(), loan.getCurrTerm() + 1);
		CcsRepaySchedule prepaymentSchedule = microCreditRescheduleUtils.genPrepayment(payInAdvLoanReg, loan, CcsAcct.getNextStmtDate(), origNextSchedule);
		BigDecimal loanPmtDue = prepaymentSchedule.getLoanTermPrin().add(prepaymentSchedule.getLoanTermInt());

		// 当操作码为申请时
		if (req.getOpt().equals(Constants.OPT_ONE)) {

			if (req.getLoan_pmt_due() == null) {
				throw new ProcessException(Constants.ERRC045_CODE, Constants.ERRC045_MES);
			}

			if (req.getLoan_pmt_due().compareTo(loanPmtDue.setScale(2, BigDecimal.ROUND_HALF_UP)) != 0) {
				throw new ProcessException(Constants.ERRC046_CODE, Constants.ERRC046_MES);
			}

			// 提前还款手续费
			BigDecimal unearnedPrin = loan.getUnstmtPrin().subtract(origNextSchedule.getLoanTermPrin());
			BigDecimal loanServiceFee = microCreditRescheduleUtils.prepaymentFeeAmt(unearnedPrin, loanFeeDef,loan.getCurrTerm()+1);

			payInAdvLoanReg.setLoanAction(LoanAction.P);
			payInAdvLoanReg.setLoanRegStatus(LoanRegStatus.A);
			payInAdvLoanReg.setAdvPmtAmt(loanPmtDue);
			payInAdvLoanReg.setLoanSvcFee(loanServiceFee);
			rCcsLoanReg.save(payInAdvLoanReg);
			// TODO 短信
		}

		S20011Resp resp = new S20011Resp();
		resp.setCard_no(req.getCard_no());
		resp.setLoan_receipt_nbr(req.getLoan_receipt_nbr());
		resp.setLoan_init_prin(loan.getLoanInitPrin());
		resp.setLoan_init_term(loan.getLoanInitTerm());
		resp.setLoan_pmt_due(loanPmtDue);
		LogTools.printLogger(logger, "S20011", "提前全额还款", resp, false);
		return resp;
	}
}
