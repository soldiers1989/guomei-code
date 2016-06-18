package com.sunline.ccs.service.handler.sub;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
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
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnUnstatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnUnstatement;
import com.sunline.ccs.loan.TxnLoanProvideImpl;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S13082Req;
import com.sunline.ccs.service.protocol.S13082Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNRLoanR
 * @see 描述：消费转分期申请
 *
 * @see 创建日期： 2015年6月24日 下午7:02:46
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNRLoanR {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	@Autowired
	private Common common;

	@Autowired
	private RCcsLoanReg rCcsLoanReg;
	@Autowired
	private RCcsLoan rCcsLoan;
	@PersistenceContext
	private EntityManager em;
	QCcsPlan qCcsPlan = QCcsPlan.ccsPlan;
	QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
	QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;
	QCcsTxnUnstatement qCcsTxnUnstatement = QCcsTxnUnstatement.ccsTxnUnstatement;

	@Transactional
	public S13082Resp handler(S13082Req req) throws ProcessException {
		LogTools.printLogger(logger, "S13082", "消费转分期申请", req, true);

		// 校验上送报文域
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.checkCurrCd(req.getCurr_cd(), false);
		CheckUtil.rejectNull(req.getTxn_date(), Constants.ERRL001_CODE, Constants.ERRL001_MES);
		CheckUtil.rejectNull(req.getTxn_amt(), Constants.ERRL002_CODE, Constants.ERRL002_MES);
		CheckUtil.rejectNull(req.getLoan_init_term(), Constants.ERRL003_CODE, Constants.ERRL003_MES);

		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO) && !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)) {
			throw new ProcessException(Constants.ERRS004_CODE, Constants.ERRS004_MES);
		}

		// 查询卡片信息
		CcsCard card = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(card, Constants.ERRS001_CODE, Constants.ERRS001_CODE);

		// 判断转分期交易对应的卡片是否过有效期
		if (unifiedParamFacilityProvide.BusinessDate().compareTo(card.getCardExpireDate()) > 0) {
			throw new ProcessException(Constants.ERRL011_CODE, Constants.ERRL011_MES);
		}

		// 获取账户信息
		CcsAcct acct = custAcctCardQueryFacility.getAcctByCardNbrCurrency(req.getCard_no(), req.getCurr_cd());
		CheckUtil.rejectNull(acct, Constants.ERRS001_CODE, Constants.ERRS001_CODE);

		// 获取账户层参数
		AccountAttribute accountAttribute = unifiedParamFacilityProvide.acct_attribute(acct.getProductCd());

		// 获取分期计划参数
		LoanPlan loanPlan = unifiedParamFacilityProvide.loanPlan(card.getProductCd(), LoanType.R);

		// 判断账户逾期状态
		// TODO 3（总期数内逾期多少次）、2（总期数内连续逾期多少次）、6（总期数）应为参数部分，这里先固定写
		if (common.isOverdue(acct.getPaymentHst(), 3, 2, 6)) {
			throw new ProcessException(Constants.ERRL048_CODE, Constants.ERRL048_MES);
		}

		// 查询原交易信息，无则拒绝
		JPAQuery queryTxn = new JPAQuery(em);
		List<CcsTxnUnstatement> txnUnstmtList = queryTxn
				.from(qCcsTxnUnstatement)
				.where(qCcsTxnUnstatement.cardNbr.eq(req.getCard_no()).and(qCcsTxnUnstatement.acctType.eq(acct.getAcctType()))
						.and(qCcsTxnUnstatement.txnDate.eq(req.getTxn_date())).and(qCcsTxnUnstatement.txnAmt.eq(req.getTxn_amt()))).list(qCcsTxnUnstatement);
		if (txnUnstmtList == null || txnUnstmtList.size() == 0) {
			throw new ProcessException(Constants.ERRL005_CODE, Constants.ERRL005_MES);
		}

		CcsTxnUnstatement txnUnstatement = null;
		boolean isLoan = true;
		for (CcsTxnUnstatement _tmTxnUnstmt : txnUnstmtList) {
			// 判断交易码是否可以做分期
			if (!loanPlan.txnCdList.contains(_tmTxnUnstmt.getTxnCode())) {
				continue;
			}

			// 判断当天是否已经做分期申请
			CcsLoanReg loanReg = rCcsLoanReg.findOne(qCcsLoanReg.cardNbr.eq(_tmTxnUnstmt.getCardNbr()).and(qCcsLoanReg.acctType.eq(_tmTxnUnstmt.getAcctType()))
					.and(qCcsLoanReg.origTransDate.eq(_tmTxnUnstmt.getTxnDate())).and(qCcsLoanReg.origTxnAmt.eq(_tmTxnUnstmt.getTxnAmt()))
					.and(qCcsLoanReg.origAuthCode.eq(_tmTxnUnstmt.getAuthCode())).and(qCcsLoanReg.refNbr.eq(_tmTxnUnstmt.getRefNbr()))
					.and(qCcsLoanReg.org.eq(OrganizationContextHolder.getCurrentOrg())));

			// 判断该交易是否已经做分期
			CcsLoan loan = rCcsLoan.findOne(qCcsLoan.cardNbr.eq(_tmTxnUnstmt.getCardNbr()).and(qCcsLoan.origTransDate.eq(_tmTxnUnstmt.getTxnDate()))
					.and(qCcsLoan.origTxnAmt.eq(_tmTxnUnstmt.getTxnAmt())).and(qCcsLoan.origAuthCode.eq(_tmTxnUnstmt.getAuthCode()))
					.and(qCcsLoan.refNbr.eq(_tmTxnUnstmt.getRefNbr())).and(qCcsLoan.org.eq(OrganizationContextHolder.getCurrentOrg())));

			if (loanReg == null && loan == null) {
				isLoan = false;
				txnUnstatement = _tmTxnUnstmt;
				break;
			}
		}
		// 判断交易码是否可以做分期,如果tmTxnUnstmt为null必为非可转分期类型
		if (txnUnstatement == null) {
			throw new ProcessException(Constants.ERRL006_CODE, Constants.ERRL006_MES);
		}

		if (isLoan) {
			throw new ProcessException(Constants.ERRL007_CODE, Constants.ERRL007_MES);
		}

		// 判断账户上是否有锁定码存在，存在则无法分期
		if (!blockCodeUtils.getMergedLoanInd(acct.getBlockCode())) {
			throw new ProcessException(Constants.ERRL008_CODE, Constants.ERRL008_MES);
		}

		// 判断卡片上是否有锁定码存在，存在则无法分期
		if (!blockCodeUtils.getMergedLoanInd(card.getBlockCode())) {
			throw new ProcessException(Constants.ERRL008_CODE, Constants.ERRL008_MES);
		}

		// 消费转分期最终计算公式----2014-3-11(王伟民、高寒确认版)
		// appAmt 当前申请消费转分期金额
		// origRetailAmt 原交易金额
		// appedAmt 已申请消费转分期金额(待审/待转,不含拒绝或已转)
		// loanbal 分期余额
		//
		// retailPrin 当前消费计划总本金
		// crlimit 永久额度
		// loanLmtRate 永久额度可分期比例
		// tempLmte 临时额度
		// currbal 总余额
		//
		// r0 = origRetailAmt
		// r1 = retailPrin - appedAmt
		//
		// r2 = crlimit * crlimit2loanRate -(appedamt + loanbal)
		// r4 = currbal - ( appedamt + loanbal )
		//
		// appAmt = min(r0,r1,r2,r4)

		// 根据卡号找到主卡对应的消费计划
		JPAQuery query = new JPAQuery(em);
		List<BigDecimal> currBalList = query.from(qCcsPlan)
				.where(qCcsPlan.acctNbr.eq(acct.getAcctNbr()).and(qCcsPlan.acctType.eq(acct.getAcctType())).and(qCcsPlan.planType.eq(PlanType.R)))
				.list(qCcsPlan.currBal);
		if (currBalList.isEmpty()) {
			throw new ProcessException(Constants.ERRL009_CODE, Constants.ERRL009_MES);
		}

		// 当前消费计划总本金
		BigDecimal retailPrin = BigDecimal.ZERO;
		for (BigDecimal bigDecimail : currBalList) {
			retailPrin = retailPrin.add(bigDecimail);
		}

		// 原交易金额
		BigDecimal origRetailAmt = txnUnstatement.getPostAmt();

		// 总余额
		BigDecimal currBal = acct.getCurrBal();

		BigDecimal creditLmt = BigDecimal.ZERO;
		// 当临额参与分期时并且临额在有效期内,信用额度 = 临额额度
		if (loanPlan.useTemplimit) {
			// 临时额度是否有效
			Date businessDate = unifiedParamFacilityProvide.BusinessDate();
			if (acct.getTempLmtBegDate() != null && acct.getTempLmtEndDate() != null && businessDate.compareTo(acct.getTempLmtBegDate()) >= 0
					&& businessDate.compareTo(acct.getTempLmtEndDate()) <= 0) {
				creditLmt = acct.getTempLmt();
			} else {
				creditLmt = acct.getCreditLmt();
			}
		} else {
			creditLmt = acct.getCreditLmt();
		}

		// 已分期余额
		BigDecimal appedAmt = BigDecimal.ZERO;
		// 查询账户下当天申请没有入账的消费分期金额
		JPAQuery queryLoanReg = new JPAQuery(em);
		List<CcsLoanReg> loanRegList = queryLoanReg
				.from(qCcsLoanReg)
				.where(qCcsLoanReg.acctNbr.eq(acct.getAcctNbr()).and(qCcsLoanReg.acctType.eq(acct.getAcctType()))
						.and(qCcsLoanReg.loanType.eq(LoanType.R).and(qCcsLoanReg.org.eq(acct.getOrg())))).list(qCcsLoanReg);
		if (!loanRegList.isEmpty()) {
			for (CcsLoanReg isloanReg : loanRegList) {
				appedAmt = appedAmt.add(isloanReg.getLoanInitPrin());
			}
		}
		// 分期余额
		BigDecimal loanBal = acct.getLoanBal();
		// 额度内可分期比例
		BigDecimal loanLmtRate = BigDecimal.ZERO;
		if (acct.getLoanLmtRate() != null) {
			loanLmtRate = acct.getLoanLmtRate();

		} else {
			loanLmtRate = accountAttribute.loanLimitRate;
		}

		BigDecimal r0 = origRetailAmt;

		BigDecimal r1 = retailPrin.subtract(appedAmt);
		r1 = r1.compareTo(BigDecimal.ZERO) > 0 ? r1 : BigDecimal.ZERO;

		BigDecimal r2 = creditLmt.multiply(loanLmtRate).subtract(appedAmt).subtract(loanBal);
		r2 = r2.compareTo(BigDecimal.ZERO) > 0 ? r2 : BigDecimal.ZERO;

		BigDecimal r4 = currBal.subtract(appedAmt).subtract(loanBal);
		r4 = r4.compareTo(BigDecimal.ZERO) > 0 ? r4 : BigDecimal.ZERO;

		// 排序取最小值
		ArrayList<BigDecimal> appAmtList = new ArrayList<BigDecimal>();
		appAmtList.add(r0);
		appAmtList.add(r1);
		appAmtList.add(r2);
		appAmtList.add(r4);
		Collections.sort(appAmtList);

		// 最终申请分期金额
		BigDecimal appAmt = appAmtList.get(0).setScale(2, BigDecimal.ROUND_HALF_UP);

		// 获取分期参数
		LoanFeeDef loanFeeDef;
		loanFeeDef = unifiedParamFacilityProvide.loanFeeDef(card.getProductCd(), LoanType.R, req.getLoan_init_term());

		// 判断申请金额是否小于分期最小允许金额LOAN_MIN_AMT
		if (appAmt.compareTo(loanFeeDef.minAmount) < 0) {
			throw new ProcessException(Constants.ERRL010_CODE, Constants.ERRL010_MES);
		}
		// 判断申请金额是否大于分期最大允许金额
		if (appAmt.compareTo(loanFeeDef.maxAmount) > 0) {
			throw new ProcessException(Constants.ERRL018_CODE, Constants.ERRL018_MES);
		}

		if (loanFeeDef.distributeMethod == null) {
			throw new ProcessException(Constants.ERRS001_CODE, Constants.ERRS001_MES);
		}
		TxnLoanProvideImpl txnLoanProvideImpl = new TxnLoanProvideImpl(LoanType.R, loanFeeDef, txnUnstatement);
		CcsLoanReg loanReg;
		loanReg = txnLoanProvideImpl.genLoanReg(req.getLoan_init_term(), appAmt, txnUnstatement.getRefNbr(), txnUnstatement.getLogicCardNbr(),
				txnUnstatement.getCardNbr(), req.getLoan_fee_method(), txnUnstatement.getAcctNbr(), txnUnstatement.getAcctType(), loanPlan.loanCode,
				unifiedParamFacilityProvide.BusinessDate());

		// 若申请，保存分期数据
		if (StringUtils.equals(req.getOpt(), Constants.OPT_ONE)) {
			rCcsLoanReg.save(loanReg);

			// 分期付款申请成功
			common.genMessageApplySuccess(req.getCard_no(), acct, loanReg);
		}

		S13082Resp resp = new S13082Resp();
		resp.setCard_no(loanReg.getCardNbr());
		resp.setCurr_cd(loanReg.getAcctType().getCurrencyCode());
		resp.setRegister_id(loanReg.getRegisterId());
		resp.setLoan_init_term(loanReg.getLoanInitTerm());
		resp.setLoan_fee_method(loanReg.getLoanFeeMethod());
		resp.setLoan_init_prin(loanReg.getLoanInitPrin());
		resp.setLoan_fixed_pmt_prin(loanReg.getLoanFixedPmtPrin());
		resp.setLoan_first_term_prin(loanReg.getLoanFirstTermPrin());
		resp.setLoan_final_term_prin(loanReg.getLoanFinalTermPrin());
		resp.setLoan_init_fee1(loanReg.getLoanInitFee());
		resp.setLoan_fixed_fee1(loanReg.getLoanFixedFee());
		resp.setLoan_first_term_fee1(loanReg.getLoanFirstTermFee());
		resp.setLoan_final_term_fee1(loanReg.getLoanFinalTermFee());

		LogTools.printLogger(logger, "S13082", "消费转分期申请", resp, false);
		return resp;
	}

}
