package com.sunline.ccs.service.handler.sub;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.InstallmentWithoutTempLimitProvider;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsStatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.loan.StmtLoanProvideImpl;
import com.sunline.ccs.otb.CommProvide;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S12012Req;
import com.sunline.ccs.service.protocol.S12012Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.DateTools;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNRLoanB
 * @see 描述：账单分期申请
 *
 * @see 创建日期： 2015年6月24日 下午6:18:43
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNRLoanB {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	@Autowired
	private CommProvide commonProvide;
	@Autowired
	private InstallmentWithoutTempLimitProvider tempLmtNotConductLoanProvider;
	@Autowired
	private Common common;

	@Autowired
	private RCcsLoan rCcsLoan;
	@Autowired
	private RCcsLoanReg rCcsLoanReg;
	@PersistenceContext
	private EntityManager em;

	QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
	QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;
	QCcsPlan qCcsPlan = QCcsPlan.ccsPlan;

	@Transactional
	public S12012Resp handler(S12012Req req) throws ProcessException {
		LogTools.printLogger(logger, "S12012", "账单分期申请", req, true);

		// 校验上送报文域
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.checkCurrCd(req.getCurr_cd(), false);
		CheckUtil.rejectNull(req.getLoan_init_term(), Constants.ERRL003_CODE, Constants.ERRL003_MES);
		CheckUtil.rejectNull(req.getLoan_init_prin(), Constants.ERRL013_CODE, Constants.ERRL013_MES);
		CheckUtil.rejectNull(req.getLoan_fee_method(), Constants.ERRL004_CODE, Constants.ERRL004_MES);
		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO) && !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)) {
			throw new ProcessException(Constants.ERRS004_CODE, Constants.ERRS004_MES);
		}

		// 获取卡片信息
		CcsCard card = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(card, Constants.ERRB001_CODE, Constants.ERRB001_CODE);
		// 获取账户信息
		CcsAcct acct;
		acct = custAcctCardQueryFacility.getAcctByCardNbrCurrency(req.getCard_no(), req.getCurr_cd());

		CheckUtil.rejectNull(acct, Constants.ERRS001_CODE, Constants.ERRS001_MES);

		// 获取最近一个账单
		CcsStatement statement = common.getLastStmtHst(acct);
		CheckUtil.rejectNull(statement, Constants.ERRL014_CODE, Constants.ERRL014_MES);

		// ref_nbr = 账号+账户类型+上次账单日期
		String refNbr = acct.getAcctNbr() + acct.getAcctType().toString() + acct.getLastStmtDate();

		// 使用ref_nbr判断是否已经做过分期
		CcsLoan loan = rCcsLoan.findOne(qCcsLoan.refNbr.eq(refNbr));
		CheckUtil.rejectNotNull(loan, Constants.ERRL015_CODE, Constants.ERRL015_MES);
		CcsLoanReg loanReg = rCcsLoanReg.findOne(qCcsLoanReg.refNbr.eq(refNbr));
		CheckUtil.rejectNotNull(loanReg, Constants.ERRL015_CODE, Constants.ERRL015_MES);

		// 判断是否有锁定码N存在，存在则无法分期
		if (!blockCodeUtils.getMergedLoanInd(acct.getBlockCode())) {
			throw new ProcessException(Constants.ERRL008_CODE, Constants.ERRL008_MES);
		}

		// 日期区间：判断申请当日业务日期是否处于“最近一次账单日期LAST_STMT_DATE次日”至“到期还款日前N天（N为DIRECT_DB_DAYS参数值）且保证是最后一期账单
		Date begDate = DateUtils.addDays(acct.getLastStmtDate(), 1);
		Date endDate = DateUtils.addDays(acct.getPmtDueDate(), -unifiedParameterFacilityProvide.direct_Db_Days(acct.getProductCd()));
		if (!DateTools.dateBetwen_IncludeEQ(begDate, unifiedParameterFacilityProvide.BusinessDate(), endDate)
				|| acct.getLastStmtDate().compareTo(statement.getStmtDate()) != 0) {
			throw new ProcessException(Constants.ERRL016_CODE, Constants.ERRL016_MES);
		}

		// 根据卡号找到主卡对应的消费计划
		// JPAQuery query = new JPAQuery(em);
		// List<BigDecimal> pastPrincipalList =
		// query.from(qCcsPlan).where(qCcsPlan.logicCardNbr.eq(CcsCard.getCardBasicNbr()).and(qCcsPlan.planType.eq(PlanType.R))).list(qCcsPlan.pastPrincipal);
		// if (pastPrincipalList.isEmpty()) {
		// throw new ProcessException(Constants.ERRL014_CODE,
		// Constants.ERRL014_MES);
		// }
		// //当期全部消费计划本金余额之和
		// BigDecimal pastPrincipalAdd = BigDecimal.ZERO;
		// for(BigDecimal pastPrincipal : pastPrincipalList){
		// pastPrincipalAdd = pastPrincipalAdd.add(pastPrincipal);
		// }
		// 获取账户层参数
		AccountAttribute accountAttribute = unifiedParameterFacilityProvide.acct_attribute(acct.getProductCd());

		// 获取账单分期参数
		LoanFeeDef loanFeeDef;
		loanFeeDef = unifiedParameterFacilityProvide.loanFeeDef(acct.getProductCd(), LoanType.B, req.getLoan_init_term());

		if (loanFeeDef.distributeMethod == null) {
			throw new ProcessException(Constants.ERRS001_CODE, Constants.ERRS001_MES);
		}

		// 判断申请金额是否大于账户的“该账户下所有消费信用计划当前PAST_PRIN之和,也就是当期账单余额
		// if
		// (req.getLoan_init_prin().compareTo(ccsStatement.getStmtCurrBal().multiply(loanFeeDef.maxAmountRate))
		// > 0) {
		// 基于武汉的需求，账单分期允许的最大金额改不与往期消费累加；
		// 当期最大允许的分期金额=min(当期消费金额-当期已经分期的消费金额，当期剩余全部应还款额,消费计划已出账单本金之和 )
		// TODO 以后会考虑加参数
		// 获取信用计划
		List<CcsPlan> plans = commonProvide.getCcsPlanByCcsAcct(acct);
		BigDecimal ddAmt = BigDecimal.ZERO;
		BigDecimal remainGraceBal = commonProvide.getRemainGraceBal(plans).setScale(2, BigDecimal.ROUND_HALF_UP);
		ddAmt = remainGraceBal;
		if (ddAmt.compareTo(BigDecimal.ZERO) < 0) {
			ddAmt = BigDecimal.ZERO;
		}

		// 上个账单日期和要分期的账单日期作为查询条件，由于边界值的问题，需要如下处理
		// TODO 由于批量bug账单中lastStmtDate为null，暂时用账单日期减一个月，等批量修复之后，再修正
		Date lastStmtDate = DateUtils.addDays(DateUtils.addMonths(statement.getStmtDate(), -1), 1);
		Date stmtDate = DateUtils.addDays(statement.getStmtDate(), 1);

		BigDecimal isLoanAmtAdd = BigDecimal.ZERO;
		// 查询当期已经消费分期的金额
		JPAQuery loanQuery = new JPAQuery(em);
		List<BigDecimal> isLoanTxnList = loanQuery
				.from(qCcsLoan)
				.where(qCcsLoan.org.eq(acct.getOrg()).and(qCcsLoan.acctNbr.eq(acct.getAcctNbr())).and(qCcsLoan.acctType.eq(acct.getAcctType()))
						.and(qCcsLoan.loanType.in(LoanType.R, LoanType.C, LoanType.P)).and(qCcsLoan.registerDate.goe(lastStmtDate))
						.and(qCcsLoan.registerDate.before(stmtDate))).list(qCcsLoan.loanInitPrin);
		// 不为null，则加总
		if (!isLoanTxnList.isEmpty()) {
			for (BigDecimal isLoanAmt : isLoanTxnList) {
				isLoanAmtAdd = isLoanAmtAdd.add(isLoanAmt).setScale(2, BigDecimal.ROUND_HALF_UP);
			}
		}

		BigDecimal ctdNotLoanAmt = statement.getCtdRetailAmt().subtract(isLoanAmtAdd).multiply(loanFeeDef.maxAmountRate).setScale(2, BigDecimal.ROUND_HALF_UP);

		// 根据卡号找到主卡对应的消费计划
		JPAQuery query = new JPAQuery(em);
		List<BigDecimal> pastPrincipalList = query.from(qCcsPlan)
				.where(qCcsPlan.acctNbr.eq(acct.getAcctNbr()).and(qCcsPlan.acctType.eq(acct.getAcctType())).and(qCcsPlan.planType.eq(PlanType.R)))
				.list(qCcsPlan.pastPrincipal);
		if (pastPrincipalList.isEmpty()) {
			throw new ProcessException(Constants.ERRL009_CODE, Constants.ERRL009_MES);
		}

		// 当前消费计划总本金
		BigDecimal pastPrincipal = BigDecimal.ZERO;
		for (BigDecimal bigDecimail : pastPrincipalList) {
			pastPrincipal = pastPrincipal.add(bigDecimail);
		}

		// 三值取小
		if (ddAmt.compareTo(ctdNotLoanAmt) > 0) {
			ddAmt = ctdNotLoanAmt;
		}
		if (ddAmt.compareTo(pastPrincipal) > 0) {
			ddAmt = pastPrincipal;
		}
		if (req.getLoan_init_prin().compareTo(ddAmt) > 0) {
			throw new ProcessException(Constants.ERRL017_CODE, Constants.ERRL017_MES);
		}

		// 当账户上的额度内分期比例不为null时，允许分期的最大额度=信用额度（如果临时额度有效使用之）*额度内分期比例 - loanbal
		BigDecimal allowLoanAmt = BigDecimal.ZERO;
		// 临时额度是否有效
		Date businessDate = unifiedParameterFacilityProvide.BusinessDate();
		BigDecimal creditLmt = BigDecimal.ZERO;
		if (acct.getTempLmtBegDate() != null && acct.getTempLmtEndDate() != null && businessDate.compareTo(acct.getTempLmtBegDate()) >= 0
				&& businessDate.compareTo(acct.getTempLmtEndDate()) <= 0) {
			creditLmt = acct.getTempLmt();
		} else {
			creditLmt = acct.getCreditLmt();
		}

		// 已分期余额
		BigDecimal isLoanBal = BigDecimal.ZERO;
		// 查询当天申请没有入账的分期金额
		JPAQuery queryLoanReg = new JPAQuery(em);
		List<CcsLoanReg> loanRegList = queryLoanReg
				.from(qCcsLoanReg)
				.where(qCcsLoanReg.acctNbr.eq(acct.getAcctNbr()).and(qCcsLoanReg.acctType.eq(acct.getAcctType()))
						.and(qCcsLoanReg.cardNbr.eq(card.getLogicCardNbr()).and(qCcsLoanReg.org.eq(acct.getOrg())))).list(qCcsLoanReg);
		if (!loanRegList.isEmpty()) {
			for (CcsLoanReg isloanReg : loanRegList) {
				isLoanBal = isLoanBal.add(isloanReg.getLoanInitPrin());
			}
		}
		isLoanBal = isLoanBal.add(acct.getLoanBal()).setScale(2, BigDecimal.ROUND_HALF_UP);
		if (acct.getLoanLmtRate() != null) {
			allowLoanAmt = creditLmt.multiply(acct.getLoanLmtRate()).subtract(isLoanBal).setScale(2, BigDecimal.ROUND_HALF_UP);
			if (req.getLoan_init_prin().compareTo(allowLoanAmt) > 0) {
				req.setLoan_init_prin(allowLoanAmt);
			}
		} else {
			allowLoanAmt = creditLmt.multiply(accountAttribute.loanLimitRate).subtract(isLoanBal).setScale(2, BigDecimal.ROUND_HALF_UP);
			if (req.getLoan_init_prin().compareTo(allowLoanAmt) > 0) {
				req.setLoan_init_prin(allowLoanAmt);
			}
		}

		// 获取分期计划参数
		LoanPlan loanPlan;
		loanPlan = unifiedParameterFacilityProvide.loanPlan(acct.getProductCd(), LoanType.B);
		// 临额是否参与分期
		Boolean isUseTempLmt = loanPlan.useTemplimit;
		// 不参与
		if (!isUseTempLmt) {
			BigDecimal stmtloanMaxAmt = tempLmtNotConductLoanProvider.StmtLoanMaxAmt(statement, acct, card, accountAttribute, loanFeeDef, businessDate,
					isLoanAmtAdd);
			if (req.getLoan_init_prin().compareTo(stmtloanMaxAmt) > 0) {
				req.setLoan_init_prin(stmtloanMaxAmt);
			}
		}
		// 判断申请金额是否大于参数中设置的最大允许分期金额
		if (req.getLoan_init_prin().compareTo(loanFeeDef.maxAmount) > 0) {
			throw new ProcessException(Constants.ERRL018_CODE, Constants.ERRL018_MES);
		}

		// 判断申请金额是否小于分期最小允许金额LOAN_MIN_AMT
		if (req.getLoan_init_prin().compareTo(loanFeeDef.minAmount) < 0) {
			throw new ProcessException(Constants.ERRL010_CODE, Constants.ERRL010_MES);
		}

		// 判断账户逾期状态
		// TODO 3（总期数内逾期多少次）、2（总期数内连续逾期多少次）、6（总期数）应为参数部分，这里先固定写
		if (common.isOverdue(acct.getPaymentHst(), 3, 2, 6)) {
			throw new ProcessException(Constants.ERRL048_CODE, Constants.ERRL048_MES);
		}

		StmtLoanProvideImpl stmtProvide = new StmtLoanProvideImpl(LoanType.B, loanFeeDef);
		CcsLoanReg applyLoanReg;
		applyLoanReg = stmtProvide.genLoanReg(req.getLoan_init_term(), req.getLoan_init_prin(), refNbr, card.getLogicCardNbr(), req.getCard_no(),
				req.getLoan_fee_method(), acct.getAcctNbr(), acct.getAcctType(), loanPlan.loanCode, unifiedParameterFacilityProvide.BusinessDate());

		// 若申请，保存分期数据
		if (StringUtils.equals(req.getOpt(), Constants.OPT_ONE)) {
			rCcsLoanReg.save(applyLoanReg);

			// 分期付款申请成功
			common.genMessageApplySuccess(req.getCard_no(), acct, applyLoanReg);
		}

		S12012Resp resp = new S12012Resp();
		resp.setCard_no(applyLoanReg.getCardNbr());
		resp.setCurr_cd(applyLoanReg.getAcctType().getCurrencyCode());
		resp.setRegister_id(applyLoanReg.getRegisterId());
		resp.setLoan_init_term(applyLoanReg.getLoanInitTerm());
		resp.setLoan_fee_method(applyLoanReg.getLoanFeeMethod());
		resp.setLoan_init_prin(applyLoanReg.getLoanInitPrin());
		resp.setLoan_fixed_pmt_prin(applyLoanReg.getLoanFixedPmtPrin());
		resp.setLoan_first_term_prin(applyLoanReg.getLoanFirstTermPrin());
		resp.setLoan_final_term_prin(applyLoanReg.getLoanFinalTermPrin());
		resp.setLoan_init_fee1(applyLoanReg.getLoanInitFee());
		resp.setLoan_fixed_fee1(applyLoanReg.getLoanFixedFee());
		resp.setLoan_first_term_fee1(applyLoanReg.getLoanFirstTermFee());
		resp.setLoan_final_term_fee1(applyLoanReg.getLoanFinalTermFee());
		CcsAcctO acctO = custAcctCardQueryFacility.getAcctOByAcctNbr(acct.getAcctType(), acct.getAcctNbr());
		BigDecimal ctdPmtNotAmt = remainGraceBal.subtract(acctO.getMemoCr()).subtract(req.getLoan_init_prin()).setScale(2, BigDecimal.ROUND_HALF_UP);
		resp.setCtd_pmt_not_amt(ctdPmtNotAmt.compareTo(BigDecimal.ZERO) > 0 ? ctdPmtNotAmt : BigDecimal.ZERO);

		LogTools.printLogger(logger, "S12012", "账单分期申请", resp, false);
		return resp;
	}

}
