package com.sunline.ccs.service.handler.sub;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.MapBuilder;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.FetchSmsNbrFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.loan.StmtLoanProvideImpl;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.param.def.enums.CalcMethod;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S13130Req;
import com.sunline.ccs.service.protocol.S13130Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.exception.ProcessException;
//import com.sunline.smsd.service.sdk.DownMsgFacility;

/**
 * @see 类名：TNRLoanExtend
 * @see 描述：分期展期
 *
 * @see 创建日期： 2015年6月25日 上午12:15:43
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNRLoanExtend {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParamFacilityProvide;
/*	@Autowired
	private DownMsgFacility downMsgFacility;
*/	@Autowired
	private FetchSmsNbrFacility fetchMsgCdService;

	@Autowired
	private RCcsCustomer rCcsCustomer;
	@Autowired
	private RCcsLoan rCcsLoan;
	@Autowired
	private RCcsLoanReg rCcsLoanReg;

	QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
	QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;

	@Transactional
	public S13130Resp handler(S13130Req req) throws ProcessException {
		LogTools.printLogger(logger, "S13130", "分期展期", req, true);

		// 校验上送域
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.rejectNull(req.getRegister_id(), Constants.ERRL020_CODE, Constants.ERRL020_MES);
		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO) && !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)) {
			throw new ProcessException(Constants.ERRS004_CODE, Constants.ERRS004_MES);
		}

		// 创建返回对像
		S13130Resp resp = new S13130Resp();

		CcsCard card = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(card, Constants.ERRB001_CODE, Constants.ERRB001_MES);
		// 根据分期顺序号查询分期信息
		CcsLoan loan = rCcsLoan.findOne(qCcsLoan.registerId.eq(req.getRegister_id()).and(qCcsLoan.logicCardNbr.eq(card.getLogicCardNbr()))
				.and(qCcsLoan.org.eq(OrganizationContextHolder.getCurrentOrg())));
		CheckUtil.rejectNull(loan, Constants.ERRL021_CODE, Constants.ERRL021_MES);
		// 判断该分期状态是否为活动
		if (!loan.getLoanStatus().equals(LoanStatus.A)) {
			throw new ProcessException(Constants.ERRL030_CODE, Constants.ERRL030_MES);
		}
		// 判断展期期数是否大于原分期期数
		if (req.getReschedule_term().compareTo(loan.getLoanInitTerm()) <= 0) {
			throw new ProcessException(Constants.ERRL032_CODE, Constants.ERRL032_MES);
		}
		if (loan.getCurrTerm() == 0) {
			throw new ProcessException(Constants.ERRC021_CODE, Constants.ERRC021_MES);
		}

		// 根据分期顺序号查询当天是否做过展期
		CcsLoanReg loanReg = rCcsLoanReg.findOne(qCcsLoanReg.refNbr.eq(loan.getRefNbr()).and(qCcsLoanReg.loanAction.eq(LoanAction.R))
				.and(qCcsLoanReg.loanRegStatus.eq(LoanRegStatus.A)).and(qCcsLoanReg.org.eq(OrganizationContextHolder.getCurrentOrg()))
				.and(qCcsLoanReg.logicCardNbr.eq(loan.getLogicCardNbr())));
		CheckUtil.rejectNotNull(loanReg, Constants.ERRL027_COED, Constants.ERRL027_MES);

		// 获取账户信息
		CcsAcct acct;
		acct = custAcctCardQueryFacility.getAcctByAcctNbr(loan.getAcctType(), loan.getAcctNbr());
		CheckUtil.rejectNull(acct, Constants.ERRS001_CODE, Constants.ERRS001_MES);

		// 获取分期计划参数
		LoanPlan loanPlan;
		loanPlan = unifiedParamFacilityProvide.loanPlan(loan.getLoanCode());

		Map<Integer, LoanFeeDef> feeDefMap = loanPlan.loanFeeDefMap;
		// 如果申请展期的期数不支持
		if (!feeDefMap.keySet().contains(req.getReschedule_term())) {
			throw new ProcessException(Constants.ERRC027_CODE, Constants.ERRC027_MES);
		}

		// 获取展期分期定价参数
		LoanFeeDef loanFeeDefReschedule;
		loanFeeDefReschedule = loanPlan.loanFeeDefMap.get(req.getReschedule_term());
		// 获取原分期定价参数
		LoanFeeDef loanFeeDef = loanPlan.loanFeeDefMap.get(loan.getLoanInitTerm());

		if (loanFeeDefReschedule.distributeMethod == null && loanFeeDef.distributeMethod == null) {
			throw new ProcessException(Constants.ERRS001_CODE, Constants.ERRS001_MES);
		}
		// 根据分期参数，该分期是否允许做展期 根据要展到的期数的参数判断是否能展到该期数
		if (!loanFeeDef.rescheduleInd || !loanFeeDefReschedule.rescheduleInd) {
			throw new ProcessException(Constants.ERRL040_CODE, Constants.ERRL040_MES);
		}

		// 展期手续费计算
		StmtLoanProvideImpl stmtProvide = new StmtLoanProvideImpl(loan.getLoanType(), loanFeeDefReschedule);
		CcsLoanReg loanRegRes;

		loanRegRes = stmtProvide.genRescheduleLoanReg(req.getReschedule_term(), loan, acct, loan.getLoanFeeMethod(), loanPlan.loanCode,
				unifiedParamFacilityProvide.BusinessDate());

		// 当手续费收取方式为一次性收取时，修改展期收取的总手续费和首期手续费
		if (loan.getLoanFeeMethod().equals(LoanFeeMethod.F)) {
			BigDecimal loanInitFee = BigDecimal.ZERO;// 展期后应收手续费
			// 展期后应收手续费 = 展期后的总手续费 - 已出账单的手续费
			// 展期后总手续费 = 未出账单本金*展期后分期手续费比例*（展期后期数 - 分期当前期数）----2014-5-15 高寒确认
			loanInitFee = loanRegRes.getLoanInitFee().subtract(loan.getLoanFeeXfrin()).setScale(2, BigDecimal.ROUND_HALF_UP);
			loanRegRes.setLoanInitFee(loanInitFee);
			loanRegRes.setLoanFirstTermFee(loanInitFee);
		}

		// 展期动作手续费
		/* loan.getLoanInitPrin() */
		BigDecimal rescheduleProcedureFee = rescheduleProcedureFee(loan.getUnstmtPrin(), loanFeeDefReschedule);
		// 若申请，保存分期数据
		if (StringUtils.equals(req.getOpt(), Constants.OPT_ONE)) {
			loanRegRes.setLoanSvcFee(rescheduleProcedureFee);
			rCcsLoanReg.save(loanRegRes);

			// 分期展期成功发送短信
			CcsCustomer customer = rCcsCustomer.findOne(acct.getCustId());
			// ProductCredit product =
			// unifiedParameterFacility.loadParameter(CcsAcct.getProductCd(),
			// ProductCredit.class);
			// messageService.sendMessage(
			// MessageCategory.P02,
			// CcsAcct.getProductCd(),
			// req.getCard_no(),
			// customer.getName(),
			// customer.getGender(),
			// customer.getMobileNo(),
			// customer.getEmail(),
			// new Date(),
			// new MapBuilder<String, Object>().add("loanType",
			// loanReg.getLoanType()).add("amt",
			// loanReg.getLoanInitPrin()).add("term", loanReg.getLoanInitTerm())
			// .add("loanFee", loanReg.getLoanInitFee().setScale(2,
			// BigDecimal.ROUND_HALF_UP))
			// .add("nextPayment",
			// loanReg.getLoanFirstTermPrin().add(loanReg.getLoanFirstTermFee()).setScale(2,
			// BigDecimal.ROUND_HALF_UP))
			// .add("loanFixedFee", loanReg.getLoanFixedFee().setScale(2,
			// BigDecimal.ROUND_HALF_UP)).build());
			String msgCd = fetchMsgCdService.fetchMsgCd(acct.getProductCd(), CPSMessageCategory.CPS033);
/*			downMsgFacility.sendMessage(
					msgCd,
					req.getCard_no(),
					customer.getName(),
					customer.getGender(),
					customer.getMobileNo(),
					new Date(),
					new MapBuilder<String, Object>().add("loanType", loanRegRes.getLoanType()).add("amt", loanRegRes.getLoanInitPrin())
							.add("term", loanRegRes.getLoanInitTerm()).add("loanFee", loanRegRes.getLoanInitFee().setScale(2, BigDecimal.ROUND_HALF_UP))
							.add("nextPayment", loanRegRes.getLoanFirstTermPrin().add(loanRegRes.getLoanFirstTermFee()).setScale(2, BigDecimal.ROUND_HALF_UP))
							.add("loanFixedFee", loanRegRes.getLoanFixedFee().setScale(2, BigDecimal.ROUND_HALF_UP)).build());
*/
		}
		// 查找刚注册成功的分期展期，用于返回Register_id
		/*
		 * CcsLoanReg loanReg_S =
		 * rCcsLoanReg.findOne(qCcsLoanReg.refNbr.eq(loan.
		 * getRefNbr()).and(qCcsLoanReg.loanAction.eq(LoanAction.R))
		 * .and(qCcsLoanReg.loanRegStatus.eq(LoanRegStatus.A)));
		 */
		CcsLoanReg loanReg_S = rCcsLoanReg.findOne(qCcsLoanReg.refNbr.eq(loan.getRefNbr()).and(qCcsLoanReg.loanAction.eq(LoanAction.R))
				.and(qCcsLoanReg.loanRegStatus.eq(LoanRegStatus.A)).and(qCcsLoanReg.logicCardNbr.eq(loan.getLogicCardNbr())));

		resp.setCard_no(req.getCard_no());
		resp.setRegister_id(loanReg_S == null ? req.getRegister_id() : loanReg_S.getRegisterId());
		resp.setLoan_init_term(loan.getLoanInitTerm());
		resp.setCurr_term(loan.getCurrTerm());
		resp.setUnearned_prin(loan.getUnstmtPrin());
		resp.setReschedule_term(req.getReschedule_term());
		resp.setReschedule_fee(rescheduleProcedureFee);
		resp.setReschedule_fixed_pmt_prin(loanRegRes.getLoanFixedPmtPrin());
		resp.setReschedule_first_term_prin(loanRegRes.getLoanFirstTermPrin());
		resp.setReschedule_final_term_prin(loanRegRes.getLoanFinalTermPrin());
		resp.setReschedule_init_fee1(loanRegRes.getLoanInitFee());
		resp.setReschedule_fixed_fee1(loanRegRes.getLoanFixedFee());
		resp.setReschedule_first_term_fee1(loanRegRes.getLoanFirstTermFee());
		resp.setReschedule_final_term_fee1(loanRegRes.getLoanFinalTermFee());

		// DebugTools.printLogger(logger, "S13130", "分期展期", resp, false);

		return resp;
	}

	/**
	 * 展期动作的手续费,在此计算时没有考虑展期手续费收取方式参数
	 * 
	 * @param loanInitPrin
	 * @param loanFeeDef
	 * @return
	 */
	private BigDecimal rescheduleProcedureFee(BigDecimal loanInitPrin, LoanFeeDef loanFeeDef) {
		BigDecimal rescheduleProcedureFee = BigDecimal.ZERO;

		if (loanFeeDef.rescheduleCalcMethod.equals(CalcMethod.R)) {
			rescheduleProcedureFee = loanInitPrin.multiply(loanFeeDef.rescheduleFeeRate).setScale(2, BigDecimal.ROUND_HALF_UP);
		}

		if (loanFeeDef.rescheduleCalcMethod.equals(CalcMethod.A)) {
			rescheduleProcedureFee = loanFeeDef.rescheduleFeeAmount;
		}

		return rescheduleProcedureFee;
	}
}
