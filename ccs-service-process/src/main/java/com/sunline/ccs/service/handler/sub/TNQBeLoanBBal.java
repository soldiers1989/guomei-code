package com.sunline.ccs.service.handler.sub;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.InstallmentWithoutTempLimitProvider;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsStatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.otb.CommProvide;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S13110Req;
import com.sunline.ccs.service.protocol.S13110Resp;
import com.sunline.ccs.service.protocol.S13110Term;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQBeLoanBBal
 * @see 描述：账单可分期金额查询
 *
 * @see 创建日期： 2015年6月24日 下午11:27:29
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQBeLoanBBal {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParamFacilityProvide;
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

	QCcsPlan qCcsPlan = QCcsPlan.ccsPlan;
	QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
	QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;

	@Transactional
	public S13110Resp handler(S13110Req req) throws ProcessException {
		LogTools.printLogger(logger, "S13110", "账单可分期金额查询", req, true);

		// 校验上送报文域
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.checkCurrCd(req.getCurr_cd(), false);

		// 获取卡片信息
		CcsCard card = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(card, Constants.ERRB001_CODE, Constants.ERRB001_CODE);
		// 获取账户信息
		CcsAcct acct;
		acct = custAcctCardQueryFacility.getAcctByCardNbrCurrency(req.getCard_no(), req.getCurr_cd());
		CheckUtil.rejectNull(acct, Constants.ERRB001_CODE, Constants.ERRB001_CODE);

		// 获取最近一期账单
		CcsStatement ccsStatement = common.getLastStmtHst(acct);
		CheckUtil.rejectNull(ccsStatement, Constants.ERRB068_CODE, Constants.ERRB068_MES);
		// 获取账户层参数
		AccountAttribute accountAttribute = unifiedParamFacilityProvide.acct_attribute(acct.getProductCd());
		// 获取分期计划参数
		LoanPlan loanPlan;
		loanPlan = unifiedParamFacilityProvide.loanPlan(acct.getProductCd(), LoanType.B);

		// ref_nbr = 账号+账户类型+上次账单日期
		String refNbr = acct.getAcctNbr() + acct.getAcctType().toString() + acct.getLastStmtDate();

		// 使用ref_nbr判断是否已经做过分期
		CcsLoan loan = rCcsLoan.findOne(qCcsLoan.refNbr.eq(refNbr));
		CheckUtil.rejectNotNull(loan, Constants.ERRL015_CODE, Constants.ERRL015_MES);
		CcsLoanReg loanReg = rCcsLoanReg.findOne(qCcsLoanReg.refNbr.eq(refNbr));
		CheckUtil.rejectNotNull(loanReg, Constants.ERRL015_CODE, Constants.ERRL015_MES);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
		S13110Resp resp = new S13110Resp();
		resp.setCard_no(req.getCard_no());
		resp.setCurr_cd(req.getCurr_cd());
		resp.setStmt_date(sdf.format(ccsStatement.getStmtDate()));

		// // 根据卡号找到主卡对应的消费计划
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
		// 基于武汉的需求，账单分期允许的最大金额改不与往期消费累加；
		// 当期最大允许的分期金额=min(当期消费金额-当期已经分期的消费金额，当期剩余全部应还款额 ,消费计划已出账单本金之和)
		// TODO 以后会考虑加参数

		// 获取信用计划
		List<CcsPlan> plans = commonProvide.getCcsPlanByCcsAcct(acct);
		BigDecimal ddAmt = BigDecimal.ZERO;
		ddAmt = commonProvide.getRemainGraceBal(plans).setScale(2, BigDecimal.ROUND_HALF_UP);
		if (ddAmt.compareTo(BigDecimal.ZERO) < 0) {
			ddAmt = BigDecimal.ZERO;
		}

		// 上个账单日期和要分期的账单日期作为查询条件，由于边界值的问题，需要如下处理
		// TODO 由于批量bug账单中lastStmtDate为null，暂时用账单日期减一个月，等批量修复之后，再修正
		Date lastStmtDate = DateUtils.addDays(DateUtils.addMonths(ccsStatement.getStmtDate(), -1), 1);
		Date stmtDate = DateUtils.addDays(ccsStatement.getStmtDate(), 1);

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

		// 根据卡号找到主卡对应的消费计划
		JPAQuery query1 = new JPAQuery(em);
		List<BigDecimal> pastPrincipalList = query1.from(qCcsPlan)
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

		// 当账户上的额度内分期比例不为null时，允许分期的最大额度=信用额度（如果临时额度有效使用之）*额度内分期比例 - loanbal
		BigDecimal loanLmt = BigDecimal.ZERO;
		// 临时额度是否有效
		Date businessDate = unifiedParamFacilityProvide.BusinessDate();
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
			loanLmt = creditLmt.multiply(acct.getLoanLmtRate()).subtract(isLoanBal).setScale(2, BigDecimal.ROUND_HALF_UP);
		} else {
			loanLmt = creditLmt.multiply(accountAttribute.loanLimitRate).subtract(isLoanBal).setScale(2, BigDecimal.ROUND_HALF_UP);
		}

		ArrayList<S13110Term> terms = new ArrayList<S13110Term>();
		Map<Integer, LoanFeeDef> loanFeeDefs = loanPlan.loanFeeDefMap;
		BigDecimal loanAmt = BigDecimal.ZERO;
		BigDecimal loanMaxAmt = BigDecimal.ZERO;
		BigDecimal ctdNotLoanTxnAmt = BigDecimal.ZERO;
		for (Integer key : loanFeeDefs.keySet()) {
			S13110Term term = new S13110Term();

			term.setLoan_init_term(key);
			LoanFeeDef loanFeeDef = unifiedParamFacilityProvide.loanFeeDef(acct.getProductCd(), LoanType.B, key);
			// 当期没有分期的消费金额 = 当期新增消费 - 当期已经分期的消费,用此金额*总本金可转分期比例，再和ddAmt取小
			ctdNotLoanTxnAmt = ccsStatement.getCtdRetailAmt().subtract(isLoanAmtAdd).multiply(loanFeeDef.maxAmountRate).setScale(2, BigDecimal.ROUND_HALF_UP);
			// 三个值取小
			loanMaxAmt = ddAmt.compareTo(ctdNotLoanTxnAmt) > 0 ? ctdNotLoanTxnAmt : ddAmt;
			loanMaxAmt = loanMaxAmt.compareTo(pastPrincipal) > 0 ? pastPrincipal : loanMaxAmt;
			loanAmt = loanMaxAmt.compareTo(loanLmt) > 0 ? loanLmt : loanMaxAmt;
			// 临额不参与分期
			if (!loanPlan.useTemplimit) {
				// 临额不参与分期时，账单分期最大允许金额，该金额与ddAmt取小返回
				BigDecimal stmtLoanMaxAmt = tempLmtNotConductLoanProvider.StmtLoanMaxAmt(ccsStatement, acct, card, accountAttribute, loanFeeDef, businessDate,
						isLoanAmtAdd);
				logger.debug("临额不参与分期时，账单分期最大允许金额:[" + stmtLoanMaxAmt + "]");
				loanAmt = loanAmt.compareTo(stmtLoanMaxAmt) > 0 ? stmtLoanMaxAmt : loanAmt;
			}
			logger.debug("账单分期最大允许金额:[" + loanAmt + "]");
			term.setLoan_amt(loanAmt);
			terms.add(term);
		}

		resp.setTerms(terms);

		LogTools.printLogger(logger, "S13110", "账单可分期金额查询", resp, false);
		return resp;
	}

}
