package com.sunline.ccs.service.handler.sub;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
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
import com.sunline.ccs.service.protocol.S20080Req;
import com.sunline.ccs.service.protocol.S20080Resp;
import com.sunline.ccs.service.protocol.S20080Schedule;
import com.sunline.ccs.service.provide.loan.MicroCreditProvide;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.DataTypeUtils;
import com.sunline.ccs.service.util.DateTools;
import com.sunline.ppy.api.MmCardService;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNRMcLoanExtend
 * @see 描述： 贷款展期
 *
 * @see 创建日期： 2015年06月26日上午 10:26:03
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNRMcLoanExtend {

	public static final String EXPIRY_DATE = "origExpiryDate";

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;
	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private GlobalManagementService globalManagementService;
	@Autowired
	private MicroCreditRescheduleUtils microCreditRescheduleUtils;
	@Resource(name = "mmCardService")
	private MmCardService mmCardService;

	@Autowired
	private RCcsLoanReg rCcsLoanReg;

	@PersistenceContext
	private EntityManager em;
	private QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
	private QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;

	/**
	 * @see 方法名：handler
	 * @see 描述：贷款展期handler
	 * @see 创建日期：2015年6月26日上午11:03:25
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
	public S20080Resp handler(S20080Req req) throws ProcessException {
		LogTools.printLogger(logger, "S20080", "贷款展期", req, true);

		// 请求报文校验
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.checkLoanReceiptNbr(req.getLoan_receipt_nbr());
		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO) && !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)) {
			throw new ProcessException(Constants.ERRS004_CODE, Constants.ERRS004_MES);
		}
		if (req.getReschedule_term() == null) {
			throw new ProcessException(Constants.ERRC019_CODE, Constants.ERRC019_MES);
		}

		// 得到联机日期
		Date businessDate = globalManagementService.getSystemStatus().getBusinessDate();

		CcsCard CcsCard = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		CcsAcct CcsAcct = custAcctCardQueryFacility.getAcctByCardNbrCurrency(CcsCard.getLogicCardNbr(), Constants.CURR_CD_156);
		CcsAcctO CcsAcctO = custAcctCardQueryFacility.getAcctOByCardNbr(req.getCard_no(), CcsAcct.getAcctType());
		CheckUtil.rejectNull(CcsAcct, Constants.ERRS001_CODE, Constants.ERRS001_MES);
		CheckUtil.rejectNull(CcsAcctO, Constants.ERRS001_CODE, Constants.ERRS001_MES);

		// 如果申请展期当天为账单日，则拒绝
		// int dayOfDate = DateTools.getDayOfDate(businessDate);
		// String day = String.format("%02d", dayOfDate);
		// String billingCycle = CcsAcct.getCycleDay();
		// if(day.equals(billingCycle)){
		// throw new ProcessException(Constants.ERRC020_CODE,
		// Constants.ERRC020_MES);
		// }

		// 已申请展期不行
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

		// 不是活动状态不让展
		if (loan.getLoanStatus() != LoanStatus.A) {
			throw new ProcessException(Constants.ERRC022_CODE, Constants.ERRC022_MES);
		}
		// 不是等额本息、等额本金不让展
		if (!loan.getLoanType().equals(LoanType.MCEI) && !loan.getLoanType().equals(LoanType.MCEP)) {
			throw new ProcessException(Constants.ERRC028_CODE, Constants.ERRC028_MES);
		}
		// 0期不让展
		if (loan.getCurrTerm() < 1) {
			throw new ProcessException(Constants.ERRC021_CODE, Constants.ERRC021_MES);
		}
		// 展后期数不能少于展期前
		if (req.getReschedule_term().compareTo(loan.getLoanInitTerm()) <= 0) {
			throw new ProcessException(Constants.ERRC023_CODE, Constants.ERRC023_MES);
		}

		LoanPlan loanPlan = unifiedParameterFacilityProvide.loanPlan(loan.getLoanCode());

		// 调用MPS服务获取介质卡片信息
		Map<String, Serializable> map = mmCardService.MS3102(req.getCard_no());
		// 卡片有效期
		Date expiryDate = DataTypeUtils.getDateValue(map.get(EXPIRY_DATE));
		// 展期后到期日
		Date rescheduleEndDateDate = DateTools.getDateAddMount(businessDate, req.getReschedule_term() - loan.getCurrTerm());
		// 展期后到期日 大于 卡片有效期，不让展
		if (!DateTools.isExpiry(rescheduleEndDateDate, expiryDate)) {
			throw new ProcessException(Constants.ERRC039_CODE, Constants.ERRC039_MES);
		}
		// 展期后到期日 大于 贷款产品有效期，不让展
		if (!DateTools.isExpiry(rescheduleEndDateDate, loanPlan.loanValidity)) {
			throw new ProcessException(Constants.ERRC040_CODE, Constants.ERRC040_MES);
		}
		// 展期后期数不能大于贷款最长周期
		if (req.getReschedule_term().compareTo(loanPlan.maxCycle) > 0) {
			throw new ProcessException(Constants.ERRC051_CODE, Constants.ERRC051_MES);
		}

		// 展期前定价参数(区间查找)
		LoanFeeDef loanFeeDef = microCreditRescheduleUtils.getLoanFeeDef(loan.getLoanCode(), loan.getLoanInitTerm(), 
				loan.getLoanInitPrin(),loan.getLoanFeeDefId());
		// 展期后定价参数(区间查找)
		LoanFeeDef loanFeeDefNew = microCreditRescheduleUtils.getLoanFeeDef(loan.getLoanCode(), req.getReschedule_term(), 
				loan.getUnstmtPrin(),loan.getLoanFeeDefId());

		// 剩余本金太多，不让展（这个参数意义不大）
		if (loan.getUnstmtPrin().compareTo(loanFeeDef.rescheduleMaxAmount) > 0) {
			throw new ProcessException(Constants.ERRC043_CODE, Constants.ERRC043_MES);
		}
		// 剩余本金太少，不让展
		if (loan.getUnstmtPrin().compareTo(loanFeeDef.rescheduleMinAmount) < 0) {
			throw new ProcessException(Constants.ERRC044_CODE, Constants.ERRC044_MES);
		}
		// 就不让展
		if (!loanFeeDef.rescheduleInd) {
			throw new ProcessException(Constants.ERRC024_CODE, Constants.ERRC024_MES);
		}

		// 展期手续费
		BigDecimal rescheduleServiceFee = microCreditRescheduleUtils.calcRescheduleFeeAmt(loan.getUnstmtPrin(), loanFeeDefNew);

		// 展期申请
		CcsLoanReg extendLoanReg = MicroCreditProvide.genCcsLoanReg(loan, CcsAcctO, loanFeeDefNew, businessDate, loanPlan);
		extendLoanReg.setExtendTerm(req.getReschedule_term());
		extendLoanReg.setRemark(req.getReschedule_reson());
		extendLoanReg.setValidDate(req.getReschedule_vdate());
		extendLoanReg.setLoanSvcFee(rescheduleServiceFee);
		extendLoanReg.setLoanAction(LoanAction.R);

		// 操作类型为展期申请
		if (req.getOpt().equals(Constants.OPT_ONE)) {
			rCcsLoanReg.save(extendLoanReg);
			// TODO 短信
		}

		// TODO 查询到原贷款的还款明细,为了计算展期前后全部应还款额差
		// JPAQuery querySchedule = new JPAQuery(em);
		// List<CcsRepaySchedule> scheduleList =
		// querySchedule.from(qCcsRepaySchedule).where(qCcsRepaySchedule.loanId.eq(loan.getLoanId())
		// .and(qCcsRepaySchedule.acctNbr.eq(loan.getAcctNbr())).and(qCcsRepaySchedule.acctType.eq(loan.getAcctType()))
		// .and(qCcsRepaySchedule.org.eq(OrganizationContextHolder.getCurrentOrg()))).orderBy(
		// qCcsRepaySchedule.currTerm.asc()).list(qCcsRepaySchedule);

		// 重新生成展期后的还款明细
		List<CcsRepaySchedule> schedules = new ArrayList<CcsRepaySchedule>();
		CcsRepaySchedule origNextSchedule = microCreditRescheduleUtils.getRepayScheduleByTerm(loan.getLoanId(), loan.getCurrTerm() + 1);
		if (loan.getLoanType().equals(LoanType.MCEI)) {
			schedules = microCreditRescheduleUtils.genRescheduleForMCEI(extendLoanReg, loan, CcsAcct.getNextStmtDate(), origNextSchedule, loanFeeDefNew);
		} else if (loan.getLoanType().equals(LoanType.MCEP)) {
			schedules = microCreditRescheduleUtils.genRescheduleForMCEP(extendLoanReg, loan, CcsAcct.getNextStmtDate(), origNextSchedule, loanFeeDefNew);
		}
		ArrayList<S20080Schedule> s20080Schedules = new ArrayList<S20080Schedule>();
		for (CcsRepaySchedule _schedule : schedules) {
			S20080Schedule schedule = new S20080Schedule();

			schedule.setCurr_term(_schedule.getCurrTerm());
			schedule.setLoan_grace_date(_schedule.getLoanGraceDate());
			schedule.setLoan_pmt_due_date(_schedule.getLoanPmtDueDate());
			schedule.setLoan_term_fee1(_schedule.getLoanTermFee());
			schedule.setLoan_term_interest(_schedule.getLoanTermInt());
			schedule.setLoan_term_prin(_schedule.getLoanTermPrin());

			s20080Schedules.add(schedule);
		}

		S20080Resp resp = new S20080Resp();
		resp.setCard_no(req.getCard_no());
		resp.setLoan_init_prin(loan.getLoanInitPrin());
		resp.setLoan_init_term(loan.getLoanInitTerm());
		resp.setLoan_receipt_nbr(loan.getDueBillNo());
		resp.setReschedule_prin(loan.getUnstmtPrin());
		resp.setReschedule_term(req.getReschedule_term());
		resp.setReschedule_vdate(req.getReschedule_vdate());
		resp.setSchedules(s20080Schedules);

		LogTools.printLogger(logger, "S20080", "贷款展期", resp, false);
		return resp;
	}
}
