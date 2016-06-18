package com.sunline.ccs.service.handler.sub;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.enums.LoanTerminateReason;
import com.sunline.ccs.param.def.enums.PrepaymentFeeInd;
import com.sunline.ccs.param.def.enums.PrepaymentFeeMethod;
import com.sunline.ccs.param.def.enums.PrepaymentInd;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S13120Req;
import com.sunline.ccs.service.protocol.S13120Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNRLoanAdvance
 * @see 描述：分期提前还款
 *
 * @see 创建日期： 2015年6月25日 上午12:03:14
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNRLoanAdvance {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	@Autowired
	private RCcsLoan rCcsLoan;
	@Autowired
	private RCcsLoanReg rCcsLoanReg;

	QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
	QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;

	@Transactional
	public S13120Resp handler(S13120Req req) throws ProcessException {
		LogTools.printLogger(logger, "S13120", "分期提前还款", req, true);

		// 校验上送域
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.rejectNull(req.getRegister_id(), Constants.ERRL020_CODE, Constants.ERRL020_MES);
		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO) && !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)) {
			throw new ProcessException(Constants.ERRS004_CODE, Constants.ERRS004_MES);
		}

		// 创建返回对像
		S13120Resp resp = new S13120Resp();

		// 根据分期顺序号查询分期信息
		CcsLoan loan = rCcsLoan.findOne(qCcsLoan.registerId.eq(Long.valueOf(req.getRegister_id())));
		CheckUtil.rejectNull(loan, Constants.ERRL021_CODE, Constants.ERRL021_MES);

		// 判断该分期状态是否为活动
		if (loan.getLoanStatus() != LoanStatus.A && loan.getLoanStatus() != LoanStatus.R) {
			throw new ProcessException(Constants.ERRL026_CODE, Constants.ERRL026_MES);
		}

		// 获取账户信息
		CcsAcct acct;
		acct = custAcctCardQueryFacility.getAcctByAcctNbr(loan.getAcctType(), loan.getAcctNbr());
		CheckUtil.rejectNull(acct, Constants.ERRS001_CODE, Constants.ERRS001_MES);

		// 获取分期计划参数
		LoanPlan loanPlan;
		loanPlan = unifiedParamFacilityProvide.loanPlan(loan.getLoanCode());

		if (loanPlan.prepaymentInd.equals(PrepaymentInd.N)) {
			throw new ProcessException(Constants.ERRL039_CODE, Constants.ERRL039_MES);
		}

		// 计算手续费
		BigDecimal prepaymentFee = BigDecimal.ZERO;// 提前还款手续费
//		// 当提前还款手续费收取方式为按比例计算时 提前还款手续费 = 未出账单本金 * 提前还款手续费比例
//		if (loanPlan.loanFeeDefMap.get(loan.getLoanInitTerm()).prepaymentFeeMethod.equals(PrepaymentFeeMethod.R)) {
//			prepaymentFee = loan.getUnstmtPrin().multiply(loanPlan.loanFeeDefMap.get(loan.getLoanInitTerm()).prepaymentFeeAmountRate);
//		}
//		// 当提前还款手续费收取方式为固定金额时 提前还款手续费 = 提前还款手续费金额
//		if (loanPlan.loanFeeDefMap.get(loan.getLoanInitTerm()).prepaymentFeeMethod.equals(PrepaymentFeeMethod.A)) {
//			prepaymentFee = loanPlan.loanFeeDefMap.get(loan.getLoanInitTerm()).prepaymentFeeAmount;
//		}

		// 提前还款退手续费
		BigDecimal prepaymentReturnFee = BigDecimal.ZERO;
		// 当提前还款不退手续
		if (loanPlan.prepaymentFeeInd.equals(PrepaymentFeeInd.N)) {
			prepaymentReturnFee = BigDecimal.ZERO;
		}
		// 当退手续费为全退时，金额为已出账单手续费
		if (loanPlan.prepaymentFeeInd.equals(PrepaymentFeeInd.B)) {
			prepaymentReturnFee = loan.getLoanFeeXfrin();
		}
		// 当退手续费按比例并
		if (loanPlan.prepaymentFeeInd.equals(PrepaymentFeeInd.P)) {
			// 手续费收取方式为一次性收取时
//			if (loanPlan.loanFeeDefMap.get(loan.getLoanInitTerm()).loanFeeMethod.equals(LoanFeeMethod.F)) {
			//更改取原分期计划中的手续费收取方式
			if (loan.getLoanFeeMethod().equals(LoanFeeMethod.F)) {
				//优化退分期手续费计算方法
				prepaymentReturnFee = loan.getLoanFeeXfrin().divide(BigDecimal.valueOf(loan.getLoanInitTerm()), 2, BigDecimal.ROUND_HALF_UP)
						.multiply(BigDecimal.valueOf(loan.getRemainTerm()))
						.setScale(2, BigDecimal.ROUND_HALF_UP);
			}
			// 手续费收取方式为分期收取时
//			if (loanPlan.loanFeeDefMap.get(loan.getLoanInitTerm()).loanFeeMethod.equals(LoanFeeMethod.E)) {
			//更改取原分期计划中的手续费收取方式
			if (loan.getLoanFeeMethod().equals(LoanFeeMethod.E)) {
				prepaymentReturnFee = BigDecimal.ZERO;
			}
			// 手续费收取方式为自行指定时
			// TODO 没实现
//			if (loanPlan.loanFeeDefMap.get(loan.getLoanInitTerm()).loanFeeMethod.equals(LoanFeeMethod.C)) {
			//更改取原分期计划中的手续费收取方式
			if (loan.getLoanFeeMethod().equals(LoanFeeMethod.C)) {
				throw new ProcessException("手续费收取方式为自行指定暂没有实现");
			}

		}

		// 如果操作为提前还款
		if (StringUtils.equals(req.getOpt(), Constants.OPT_ONE)) {
			// 修改分期主表状态
			loan.setLastLoanStatus(loan.getLoanStatus());
			loan.setLoanStatus(LoanStatus.T);
			loan.setTerminalDate(unifiedParamFacilityProvide.BusinessDate());
			loan.setTerminalReasonCd(LoanTerminateReason.P);

			// 保存到分期注册表
			CcsLoanReg loanReg = new CcsLoanReg();
			loanReg.setOrg(OrganizationContextHolder.getCurrentOrg());
			loanReg.setAcctNbr(loan.getAcctNbr());
			loanReg.setAcctType(loan.getAcctType());
			loanReg.setLogicCardNbr(loan.getLogicCardNbr());
			loanReg.setCardNbr(loan.getCardNbr());
			loanReg.setLoanCode(loan.getLoanCode());

			loanReg.setLoanFeeMethod(LoanFeeMethod.F);// 对于提前还款需要自定手续费收取方式为一次性收取
			loanReg.setLoanType(loan.getLoanType());
			loanReg.setRefNbr(loan.getRefNbr());
			loanReg.setLoanAction(LoanAction.P);
			loanReg.setLoanRegStatus(LoanRegStatus.A);
			loanReg.setLoanSvcFee(prepaymentFee);
			loanReg.setLoanSvcFeeReturn(prepaymentReturnFee);
			loanReg.setOrigAuthCode(loan.getOrigAuthCode());
			loanReg.setOrigTransDate(loan.getOrigTransDate());
			loanReg.setOrigTxnAmt(loan.getOrigTxnAmt());
			loanReg.setRegisterDate(unifiedParamFacilityProvide.BusinessDate());
			loanReg.setRequestTime(new Date());
			loanReg.setLoanFinalTermFee(loan.getLoanFinalTermFee());
			loanReg.setLoanFinalTermPrin(loan.getLoanFinalTermPrin());
			loanReg.setLoanFirstTermFee(loan.getLoanFirstTermFee());
			loanReg.setLoanFirstTermPrin(loan.getLoanFirstTermPrin());
			loanReg.setLoanFixedFee(loan.getLoanFixedFee());
			loanReg.setLoanFixedPmtPrin(loan.getLoanFixedPmtPrin());
			loanReg.setLoanInitFee(loan.getLoanInitFee());
			loanReg.setLoanInitPrin(loan.getLoanInitPrin());
			loanReg.setLoanInitTerm(loan.getLoanInitTerm());
			rCcsLoanReg.save(loanReg);

		}

		// 查找刚注册成功的分期提前还款，用于返回Register_id
		CcsLoanReg loanReg_S = rCcsLoanReg.findOne(qCcsLoanReg.refNbr.eq(loan.getRefNbr()).and(qCcsLoanReg.loanAction.eq(LoanAction.P))
				.and(qCcsLoanReg.loanRegStatus.eq(LoanRegStatus.A)));

		resp.setCard_no(req.getCard_no());
		resp.setRegister_id(req.getRegister_id());
		resp.setRegister_id(loanReg_S == null ? req.getRegister_id() : loanReg_S.getRegisterId());
		resp.setLoan_init_term(loan.getLoanInitTerm());
		resp.setCurr_term(loan.getCurrTerm());
		resp.setUnearned_prin(loan.getUnstmtPrin());
		resp.setPrepayment_fee(prepaymentFee);
		resp.setTotal_amt(loan.getUnstmtPrin().add(prepaymentFee).subtract(prepaymentReturnFee));// 提前还款总金额
																									// =
																									// 分期未出账单本金
																									// +
																									// 提前还款手续费
																									// -
																									// 提前还款应退手续费

		LogTools.printLogger(logger, "S13120", "分期提前还款", resp, false);

		return resp;
	}

}
