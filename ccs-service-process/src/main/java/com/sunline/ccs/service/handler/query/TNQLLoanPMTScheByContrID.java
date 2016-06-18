package com.sunline.ccs.service.handler.query;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ccs.facility.DateUtils;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.facility.contract.AcctOTBCal;
import com.sunline.ccs.facility.contract.ConstractStatusUtil;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.CcsRepayScheduleHst;
import com.sunline.ccs.infrastructure.shared.model.CcsStatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.loan.McLoanProvideImpl;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.PaymentIntervalUnit;
import com.sunline.ccs.param.def.enums.TermAmtPaidOutInd;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.handler.QueryCommService;
import com.sunline.ccs.service.msdentity.STNQLLoanPMTScheByContrIDReq;
import com.sunline.ccs.service.msdentity.STNQLLoanPMTScheByContrIDResp;
import com.sunline.ccs.service.msdentity.STNQLLoanPMTScheByContrIDRespSubLoan;
import com.sunline.ccs.service.msdentity.STNQLLoanPMTScheByContrIDRespSubPlan;
import com.sunline.ccs.service.msdentity.STNQLLoanPMTScheByContrIDRespSubStmt;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ccs.service.util.DateFormatUtil;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 合同详情查询
 * 
 * @author zqx
 * 
 */
@Service
public class TNQLLoanPMTScheByContrID {
	private Logger log = LoggerFactory.getLogger(this.getClass());
	@PersistenceContext
	protected EntityManager em;
	@Value("#{env.respDateFormat}")
	private String respDateFormat;
	@Autowired
	private RCcsCustomer rCustomer;
	@Autowired
	private QueryCommService queryCommService;
	@Autowired
	AppapiCommService appapiCommService;
	@Autowired
	ConstractStatusUtil constractStatusUtil;
	@Autowired
	AcctOTBCal acctOTBCal;
	@Autowired
	GlobalManagementService globalManagementService;
	@Autowired
	UnifiedParameterFacility unifiedParameterFacility;
	@Autowired
	UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	@Autowired
	MicroCreditRescheduleUtils rescheduleUtils;
	@Autowired
	private MicroCreditRescheduleUtils microCreditRescheduleUtils;
	@Autowired
	private McLoanProvideImpl mcLoanProvideImpl;

	public STNQLLoanPMTScheByContrIDResp handler(
			STNQLLoanPMTScheByContrIDReq req) throws ProcessException {
		LogTools.printLogger(log, "TNQLLoanPMTScheByContrID", "合同详情查询", req,
				true);
		LogTools.printObj(log, req, "请求参数TNQLLoanPMTScheByContrID");

		STNQLLoanPMTScheByContrIDResp resp = new STNQLLoanPMTScheByContrIDResp();
		TxnInfo txnInfo = new TxnInfo();
		try {
			getQueryInfo(req, resp);
		} catch (ProcessException pe) {
			if (log.isErrorEnabled())
				log.error(pe.getMessage(), pe);
			appapiCommService.preException(pe, pe, txnInfo);
		} catch (Exception e) {
			if (log.isErrorEnabled())
				log.error(e.getMessage(), e);
			appapiCommService.preException(e, null, txnInfo);
		} finally {
			LogTools.printLogger(log, "TNQLLoanPMTScheByContrID", "合同详情查询",
					resp, false);
		}
		setResponse(resp, txnInfo);

		return resp;
	}

	private void setResponse(MsResponseInfo resp, TxnInfo txnInfo) {

		resp.setErrorCode(txnInfo.getResponsCode());
		resp.setErrorMessage(txnInfo.getResponsDesc());
		if (StringUtils.equals(MsRespCode.E_0000.getCode(),
				txnInfo.getResponsCode())) {
			resp.setStatus("S");// 交易成功
		} else {
			resp.setStatus("F");// 交易失败
		}
	}

	private STNQLLoanPMTScheByContrIDResp getQueryInfo(
			STNQLLoanPMTScheByContrIDReq req,
			STNQLLoanPMTScheByContrIDResp respInfo) {
		// 取所有查询相关数据
		CcsAcct acct = getAcctByContrNbr(req.getContraNbr());
		ProductCredit productCredit = unifiedParameterFacility.loadParameter(
				acct.getProductCd(), ProductCredit.class);
		String loanCode = productCredit.loanPlansMap
				.get(productCredit.defaultLoanType);
		CcsAcctO accto = queryCommService.loadAcctO(acct.getAcctNbr(),
				acct.getAcctType(), true);
		CcsCustomer customer = queryCommService.loadCustomerByCustId(
				acct.getCustId(), false);
		List<CcsLoan> loanlist = queryCommService.getLoanlistByAcctInfo(
				acct.getAcctNbr(), acct.getAcctType());
		List<CcsLoanReg> loanReglist = queryCommService
				.getLoanReglistByAcctInfo(acct.getAcctNbr(), acct.getAcctType());
		List<CcsStatement> stmtList = queryCommService.loadStmtListByAcct(acct);
		LoanFeeDef loanFeeDef = unifiedParamFacilityProvide.loanFeeDefByKey(
				loanCode, Integer.valueOf(acct.getLoanFeeDefId()));
		// 批量时间检查
		queryCommService.batchProcessingCheck(acct, accto);

		// 赋值合同层信息
		setContractInfo(respInfo, req, loanCode, acct, accto, customer,
				loanlist, loanReglist, loanFeeDef);
		setLoanListInfo(respInfo, acct, accto, loanlist, loanReglist, stmtList);

		return respInfo;
	}

	/*
	 * 赋值合同层信息
	 * 
	 * @param STNQLLoanPMTScheByContrIDResp - 赋值的参数
	 */
	public void setContractInfo(STNQLLoanPMTScheByContrIDResp respInfo,
			STNQLLoanPMTScheByContrIDReq req, String loanCode, CcsAcct acct,
			CcsAcctO accto, CcsCustomer customer, List<CcsLoan> loanlist,
			List<CcsLoanReg> loanReglist, LoanFeeDef loanFeeDef) {
		respInfo.setContrNbr(acct.getContrNbr()); // 合同号
		respInfo.setCustUuid(customer.getInternalCustomerId());// 客户uuid
		respInfo.setDefaultLoanCode(loanCode);
		respInfo.setContrLmt(acct.getCreditLmt()); // 合同额度
		respInfo.setContraStatus(constractStatusUtil.getConstractStatus(acct)); // 合同状态
		respInfo.setContraExpireDate(DateFormatUtil.format(
				acct.getAcctExpireDate(), respDateFormat)); // 合同到期日期
		respInfo.setContrAgreeRate(acct.getInterestRate()); // 协议利率
		respInfo.setAgreeRateExpireDate(DateFormatUtil.format(
				acct.getAgreementRateExpireDate(), respDateFormat)); // 协议利率有效期
		respInfo.setContraBal(acct.getCurrBal()); // 欠款总额
		respInfo.setContraBalPrin(acct.getPrincipalBal()); // 欠款本金
		BigDecimal qualGraceBal = acct.getTotDueAmt();
		BigDecimal currDueAmt = acct.getCurrDueAmt() == null ? BigDecimal.ZERO
				: acct.getCurrDueAmt();
		BigDecimal pastDueAmt = BigDecimal.ZERO.add(
				acct.getTotDueAmt() == null ? BigDecimal.ZERO : acct
						.getTotDueAmt()).subtract(
				acct.getCurrDueAmt() == null ? BigDecimal.ZERO : acct
						.getCurrDueAmt());
		// 帮前段把 未入账还款金额计算进去
		qualGraceBal = qualGraceBal.subtract(accto.getMemoCr());
		qualGraceBal = qualGraceBal.compareTo(BigDecimal.ZERO) <= 0 ? BigDecimal.ZERO
				: qualGraceBal;

		if (pastDueAmt.compareTo(accto.getMemoCr()) > 0) {
			pastDueAmt = pastDueAmt.subtract(accto.getMemoCr());
		} else {
			pastDueAmt = BigDecimal.ZERO;
			BigDecimal remainCr = accto.getMemoCr().subtract(pastDueAmt);
			if (currDueAmt.compareTo(remainCr) > 0) {
				currDueAmt = currDueAmt.subtract(remainCr);
			} else {
				currDueAmt = BigDecimal.ZERO;
			}
		}
		respInfo.setQualGraceBal(qualGraceBal); // 全部应还款额
		respInfo.setCurrDueAmt(currDueAmt); // 当期应还款额
		respInfo.setPastDueAmt(pastDueAmt); // 往期应还款额

		respInfo.setContraMemoDb(accto.getMemoDb() == null ? BigDecimal.ZERO
				: accto.getMemoDb());
		respInfo.setContraMemoCr(accto.getMemoCr() == null ? BigDecimal.ZERO
				: accto.getMemoCr());
		// 计算可用额度
		CcsAcctO acctO = queryCommService.loadAcctO(acct.getAcctNbr(),
				acct.getAcctType(), true);
		BigDecimal acctOTB = acctOTBCal.getAcctOTB(null, acct, acctO,
				req.getBizDate());
		respInfo.setContraRemain(acctOTB.compareTo(BigDecimal.ZERO) <= 0 ? BigDecimal.ZERO
				: acctOTB); // 可用额度

		respInfo.setContraSetupDate(DateFormatUtil.format(acct.getSetupDate(),
				respDateFormat)); // 开户日期
		Date nextPmtDueDate = rescheduleUtils.getNextPaymentDay(
				acct.getProductCd(), acct.getNextStmtDate());
		respInfo.setNextPmtDueDate(DateFormatUtil.format(nextPmtDueDate,
				respDateFormat)); // 下一到期还款日期
		respInfo.setPmtDueDate(DateFormatUtil.format(acct.getPmtDueDate(),
				respDateFormat));
		respInfo.setBizDate(DateFormatUtil.format(req.getBizDate(),
				respDateFormat));
		respInfo.setLastStmtDate(DateFormatUtil.format(acct.getLastStmtDate(),
				respDateFormat));
		respInfo.setNextStmtDate(DateFormatUtil.format(acct.getNextStmtDate(),
				respDateFormat));
		respInfo.setDdDate(DateFormatUtil.format(acct.getDdDate(),
				respDateFormat)); // 下一约定扣款日
		respInfo.setDdBankAcctNbr(acct.getDdBankAcctNbr()); // 约定还款账号
		respInfo.setDdBankAcctName(acct.getDdBankAcctName()); // 约定还款账户姓名
		respInfo.setDdBankBranch(acct.getDdBankBranch()); // 约定还款开户行号
		respInfo.setDdBankName(acct.getDdBankName()); // 约定还款银行名称
		respInfo.setDdBankProvince(acct.getDdBankProvince()); // 约定还款开户行省
		respInfo.setDdBankProvCode(acct.getDdBankProvinceCode()); // 约定还款开户行省code
		respInfo.setDdBankCity(acct.getDdBankCity()); // 约定还款开户行市
		respInfo.setDdBankCityCode(acct.getDdBankCityCode()); // 约定还款开户行市code
		respInfo.setMobileNo(customer.getMobileNo());
		respInfo.setCooperatorId(acct.getAcqId());
		// 二期新增
		respInfo.setApplyDate(DateFormatUtil.format(acct.getApplyDate(),
				respDateFormat));// 申请日期
		// LOAN_DATE 赋值 -----------------------
		for (CcsLoanReg ccsLoanReg : loanReglist) {
			respInfo.setLoanDate(DateFormatUtil.format(ccsLoanReg.getValidDate(),respDateFormat));
		}
		respInfo.setPurpose(acct.getPurpose());// 贷款原因
		respInfo.setSubTerminalType(acct.getSubTerminalType());// 销售渠道
		// 合同业务场景
		respInfo.setContraBizSituation(constractStatusUtil
				.getContraBizSituation(acct, acctO, loanlist, loanReglist));

		// 申请单号
		respInfo.setApplicationNo(acct.getApplicationNo());
		// 还款频率
		PaymentIntervalUnit paymentIntervalUnit = loanFeeDef.paymentIntervalUnit;

		respInfo.setPaymentIntervalUnit(paymentIntervalUnit);
	}

	/*
	 * 赋值loan层信息
	 */
	private void setLoanListInfo(STNQLLoanPMTScheByContrIDResp respInfo,
			CcsAcct acct, CcsAcctO acctO, List<CcsLoan> loanlist,
			List<CcsLoanReg> loanReglist, List<CcsStatement> stmtList) {
		List<STNQLLoanPMTScheByContrIDRespSubLoan> subLoanList = new ArrayList<STNQLLoanPMTScheByContrIDRespSubLoan>();
		for (CcsLoan loan : loanlist) {
			// 马上贷 白名单一期 都是只有一个loan 所以先这么写后续 单合同对应多借据需修改
			STNQLLoanPMTScheByContrIDRespSubLoan contractsSubLoan = setSubLoan(
					respInfo, acct, acctO, loan, stmtList);
			subLoanList.add(contractsSubLoan);
		}

		for (CcsLoanReg loanReg : loanReglist) {
			// 跑批时loan 和 loanreg可能会重复 因此要去重
			Indicator isRepeatLoanReg = Indicator.N;
			for (CcsLoan loan : loanlist) {
				if (StringUtils.equals(loan.getLogicCardNbr(),
						loanReg.getLogicCardNbr())
						&& loan.getRegisterDate().equals(
								loanReg.getRegisterDate())
						&& StringUtils.equals(loan.getRefNbr(),
								loanReg.getRefNbr())
						&& loan.getLoanInitPrin().compareTo(
								loanReg.getLoanInitPrin()) == 0) {
					isRepeatLoanReg = Indicator.Y;
				}
			}

			if (!loanReg.getLoanAction().equals(LoanAction.O)
					&& !loanReg.getLoanAction().equals(LoanAction.U)
					&& !loanReg.getLoanAction().equals(LoanAction.D)
					&& !loanReg.getLoanAction().equals(LoanAction.T)
					&& isRepeatLoanReg == Indicator.N
					&& (loanReg.getLoanRegStatus().equals(LoanRegStatus.N)
							|| loanReg.getLoanRegStatus().equals(
									LoanRegStatus.A)
							|| loanReg.getLoanRegStatus().equals(
									LoanRegStatus.S) || loanReg
							.getLoanRegStatus().equals(LoanRegStatus.C))) {
				STNQLLoanPMTScheByContrIDRespSubLoan contractsSubLoan = setSubLoan(
						acct, loanReg);
				subLoanList.add(contractsSubLoan);
			}
		}
		respInfo.setSubLoanList(subLoanList);
		// 放款日期
		respInfo.setApplyDate(DateFormatUtil.format(getLoanDate(subLoanList),
				respDateFormat));
		//开户日期
		respInfo.setLoanDate(DateFormatUtil.format(getLoanDate(subLoanList),respDateFormat));
		// DPD逾期天数
		respInfo.setDpdDaysNumber(getloanDpd(loanlist));
		;
		// cpd日期
		respInfo.setCpdDaysNumber(getloanCpd(loanlist));
	}

	/*
	 * 获取每笔贷款信息
	 */
	public STNQLLoanPMTScheByContrIDRespSubLoan setSubLoan(
			STNQLLoanPMTScheByContrIDResp respInfo, CcsAcct acct,
			CcsAcctO acctO, CcsLoan loan, List<CcsStatement> stmtList) {
		STNQLLoanPMTScheByContrIDRespSubLoan subloan = new STNQLLoanPMTScheByContrIDRespSubLoan();
		// 获取loan下plan
		List<CcsPlan> planlist = queryCommService.getPlanlistByLoan(
				loan.getAcctNbr(), loan.getRefNbr());
		// 获取loan下schedule
		List<CcsRepaySchedule> scheduleList = queryCommService
				.getSchedulelistByLoan(loan.getLoanId());

		// 对于等额本息贷款退货情况 计算未入账息费
		BigDecimal intAcru = BigDecimal.ZERO;
		BigDecimal svcFee = BigDecimal.ZERO; // 服务费
		BigDecimal insuranceFee = BigDecimal.ZERO; // 保费
		BigDecimal lifeInsuFee = BigDecimal.ZERO; // 未入账首先费
		BigDecimal prepayFee = BigDecimal.ZERO; // 提前还款计划报费
		BigDecimal replaceSvcFee = BigDecimal.ZERO; // 代收服务费
		BigDecimal installFee = BigDecimal.ZERO; // 手续费
		if (loan.getLoanType() == LoanType.MCEI
				&& loan.getLoanStatus() == LoanStatus.T) {
			for (CcsRepaySchedule schedule : scheduleList) {
				if (loan.getCurrTerm().compareTo(schedule.getCurrTerm()) < 0) {
					intAcru = intAcru
							.add(schedule.getLoanTermInt() == null ? BigDecimal.ZERO
									: schedule.getLoanTermInt());
					svcFee = svcFee
							.add(schedule.getLoanTermFee() == null ? BigDecimal.ZERO
									: schedule.getLoanTermFee());
					insuranceFee = insuranceFee.add(schedule
							.getLoanInsuranceAmt() == null ? BigDecimal.ZERO
							: schedule.getLoanInsuranceAmt());
					lifeInsuFee = lifeInsuFee
							.add(schedule.getLoanLifeInsuAmt() == null ? BigDecimal.ZERO
									: schedule.getLoanLifeInsuAmt());
					prepayFee = prepayFee
							.add(schedule.getLoanPrepayPkgAmt() == null ? BigDecimal.ZERO
									: schedule.getLoanPrepayPkgAmt());
					replaceSvcFee = replaceSvcFee.add(schedule
							.getLoanReplaceSvcFee() == null ? BigDecimal.ZERO
							: schedule.getLoanReplaceSvcFee());
					installFee = installFee
							.add(schedule.getLoanSvcFee() == null ? BigDecimal.ZERO
									: schedule.getLoanSvcFee());
				}
			}
		}
		respInfo.setContraBal(respInfo.getContraBal().add(intAcru)
				.add(lifeInsuFee).add(svcFee).add(installFee).add(insuranceFee)
				.add(prepayFee).add(replaceSvcFee));

		subloan.setDueBillNo(loan.getDueBillNo()); // 借据号
		subloan.setLoanCode(loan.getLoanCode()); // 贷款产品代码
		subloan.setLoanType(loan.getLoanType());
		subloan.setLoanActiveDate(DateFormatUtil.format(loan.getActiveDate(),
				respDateFormat)); // 激活日期
		subloan.setLoanPaidOutDate(DateFormatUtil.format(loan.getPaidOutDate(),
				respDateFormat)); // 还清日期
		subloan.setLoanTerminalDate(DateFormatUtil.format(
				loan.getTerminalDate(), respDateFormat)); // 提前终止日期
		subloan.setTerminalReasonCd(loan.getTerminalReasonCd()); // 借据终止原因
		subloan.setLoanStatus(loan.getLoanStatus()); // 借据状态
		subloan.setLoanInitTerm(loan.getLoanInitTerm()); // 贷款总期数
		subloan.setLoanCurrTerm(loan.getCurrTerm()); // 当前期数
		subloan.setLoanRemainTerm(loan.getRemainTerm()); // 剩余期数
		subloan.setLoanInitPrin(loan.getLoanInitPrin()); // 贷款总本金
		subloan.setLoanInitFee(loan.getLoanInitFee()); // 贷款总服务费
		subloan.setLoanStampdutyAmt(loan.getLoanStampdutyAmt()); // 总印花税、
		subloan.setLoanLifeinsuAmt(loan.getTotLifeInsuAmt()); // 总寿险费用
		subloan.setLoanPrepayPkgAmt(loan.getTotPrepayPkgAmt());//总灵活还款服务包费
	
		// li
		subloan.setLoanAgentFee(loan.getTotReplaceSvcFee());// 总代收服务费
		subloan.setLoanPaidAgentFee(loan.getPaidReplaceSvcFee());// 已偿还代收服务费

		subloan.setLoanPaidPrin(loan.getPaidPrincipal()); // 已偿还本金
		subloan.setLoanPaidInt(loan.getPaidInterest()); // 已偿还利息
		subloan.setLoanPaidFee(loan.getPaidFee()); // 已偿还费用
		subloan.setLoanPaidStmp(loan.getPaidStampdutyAmt()); // 已偿还印花税
		subloan.setLoanPaidLifeinsuFee(loan.getPaidInsuranceAmt()); // 已偿还寿险费用
		subloan.setLoanCurrBal(loan.getLoanCurrBal()); // 当前总欠款
		subloan.setLoanRemainAmt(BigDecimal.ZERO);
		subloan.setLoanRemainPrin(BigDecimal.ZERO);
		subloan.setLoanUnsettlePrin(BigDecimal.ZERO);
		subloan.setLoanIntRate(loan.getInterestRate()); // 基础利率
		subloan.setLoanPenaltyRate(loan.getPenaltyRate()); // 罚息利率
		subloan.setLoanCompRate(loan.getCompoundRate()); // 复利利率
		subloan.setLoanExpireDate(DateFormatUtil.format(
				loan.getLoanExpireDate(), respDateFormat)); // 贷款到期日期
		subloan.setLoanFloatRate(loan.getFloatRate()); // 浮动比例
		subloan.setLoanOverdueDate(DateFormatUtil.format(loan.getOverdueDate(),
				respDateFormat)); // 逾期起始日期
		subloan.setLoanAgeCode(loan.getLoanAgeCode()); // 贷款逾期最大期数
		// subloan.setOverdueAmt(loan.getLoanBalXfrin()); //逾期欠款总额
		// 逾期欠款需要判断当前合同是否逾期 by lizz 20160315
		if (constractStatusUtil.isContractOverDue(acct, acctO) == Indicator.Y) {
			subloan.setOverdueAmt(loan.getLoanBalXfrin()); // 逾期欠款总额
		} else {
			subloan.setOverdueAmt(BigDecimal.ZERO);
		}
		subloan.setLoanMaxDpd(loan.getMaxDpd()); // DPD最大值
		subloan.setMaxDpdDate(DateFormatUtil.format(loan.getMaxDpdDate(),
				respDateFormat)); // 最大DPD日期
		subloan.setLoanMaxCpd(loan.getMaxCpd()); // CPD最大值
		subloan.setMaxCpdDate(DateFormatUtil.format(loan.getMaxCpdDate(),
				respDateFormat)); // 最大CPD日期

		// 把期数转化为BigDecimal,用于算月费率
		BigDecimal term = new BigDecimal(loan.getLoanInitTerm());

		// 新增协议费率
		subloan.setAgreementRateInd(loan.getAgreementRateInd());
		// 贷款服务费率
		subloan.setFeeRate(loan.getFeeRate() == null ? null : loan.getFeeRate()
				.divide(term, 8, RoundingMode.HALF_UP));
		// 贷款服务费固定金额
		subloan.setFeeAmt(loan.getFeeAmt());
		// 寿险费率
		subloan.setLifeInsuFeeRate(loan.getLifeInsuFeeRate() == null ? null
				: loan.getLifeInsuFeeRate().divide(term, 8,
						RoundingMode.HALF_UP));
		// 寿险固定金额
		subloan.setLifeInsuFeeAmt(loan.getLifeInsuFeeAmt());
		// 保费月费率
		subloan.setInsuranceRate(loan.getInsuranceRate() == null ? null : loan
				.getInsuranceRate().divide(term, 8, RoundingMode.HALF_UP));
		// 保费月固定金额
		subloan.setInsAmt(loan.getInsAmt());

		// 代收服务费

		// 代收服务费费率
		subloan.setAgentFeeRate(loan.getReplaceSvcFeeRate() == null ? null
				: loan.getReplaceSvcFeeRate().divide(term, 8,
						RoundingMode.HALF_UP));
		// 贷收服务费固定金额
		subloan.setAgentFeeAmount(loan.getReplaceSvcFeeAmt());

		// 分期手续费率
		subloan.setInstallmentFeeRate(loan.getInstallmentFeeRate() == null ? null
				: loan.getInstallmentFeeRate().divide(term, 8,
						RoundingMode.HALF_UP));
		// 分期手续费固定金额
		subloan.setInstallmentFeeAmt(loan.getInstallmentFeeAmt());
		// 提前还款包费率
		subloan.setPrepayPkgFeeRate(loan.getPrepayPkgFeeRate() == null ? null
				: loan.getPrepayPkgFeeRate().divide(term, 8,
						RoundingMode.HALF_UP));
		// 提前还款包固定金额
		subloan.setPrepayPkgFeeAmt(loan.getPrepayPkgFeeAmt());
		subloan.setPenaltyRate(loan.getPenaltyRate());
		subloan.setCompoundRate(loan.getCompoundRate());
		subloan.setInterestRate(loan.getInterestRate());
		subloan.setStampAmt(loan.getStampAmt());
		subloan.setStampdutyRate(loan.getStampdutyRate());
		// 趸交费金额
		subloan.setPremiumAmt(loan.getPremiumAmt());
		// 是否代收趸交费
		subloan.setPremiumInd(loan.getPremiumInd());
		//客户方代收罚息利率
		subloan.setCooperPenaltyRate(loan.getReplacePenaltyRate());

		// 赋值planlist
		setPlanOrScheduleListByLoan(respInfo, loan, planlist, scheduleList,
				subloan, acctO);

		// 赋值偿清计划
		if (loan.getLoanType().equals(LoanType.MCAT)) {
			// 随借随还
			setMCATStmtListInfo(stmtList, subloan);
		} else {
			// 非随借随还
			setNoMCATStmtListInfo(acct, acctO, scheduleList, planlist, loan, subloan);
		}

		return subloan;
	}

	/*
	 * 获取每笔贷款信息
	 */
	public STNQLLoanPMTScheByContrIDRespSubLoan setSubLoan(CcsAcct acct, CcsLoanReg loanReg) {
		STNQLLoanPMTScheByContrIDRespSubLoan subloan = new STNQLLoanPMTScheByContrIDRespSubLoan();

		subloan.setDueBillNo(loanReg.getDueBillNo()); // 借据号
		subloan.setLoanCode(loanReg.getLoanCode()); // 贷款产品代码
		subloan.setLoanType(loanReg.getLoanType());
		subloan.setLoanActiveDate(null); // 激活日期
		subloan.setLoanPaidOutDate(null); // 还清日期
		subloan.setLoanTerminalDate(null); // 提前终止日期
		subloan.setTerminalReasonCd(null); // 借据终止原因
		if (loanReg.getLoanRegStatus().equals(LoanRegStatus.N)
				|| loanReg.getLoanRegStatus().equals(LoanRegStatus.C)) {
			subloan.setLoanStatus(LoanStatus.P); // 借据状态
		} else {
			subloan.setLoanStatus(LoanStatus.A); // 借据状态
		}
		subloan.setLoanInitTerm(loanReg.getLoanInitTerm()); // 贷款总期数
		subloan.setLoanCurrTerm(0); // 当前期数
		subloan.setLoanRemainTerm(loanReg.getLoanInitTerm()); // 剩余期数
		subloan.setLoanInitPrin(loanReg.getLoanInitPrin()); // 贷款总本金
		subloan.setLoanInitFee(loanReg.getLoanInitFee()); // 贷款总服务费
		subloan.setLoanStampdutyAmt(loanReg.getStampdutyAmt()); // 总印花税、
		subloan.setLoanLifeinsuAmt(loanReg.getTotLifeInsuAmt()); // 总寿险费用
		subloan.setLoanPaidPrin(BigDecimal.ZERO); // 已偿还本金
		subloan.setLoanPaidInt(BigDecimal.ZERO); // 已偿还利息
		subloan.setLoanPaidFee(BigDecimal.ZERO); // 已偿还费用
		subloan.setLoanPaidStmp(BigDecimal.ZERO); // 已偿还印花税
		subloan.setLoanPaidLifeinsuFee(BigDecimal.ZERO); // 已偿还寿险费用
		subloan.setLoanCurrBal(BigDecimal.ZERO); // 当前总欠款
		subloan.setLoanRemainAmt(BigDecimal.ZERO); // 未到期总欠款 从schedule中计算
		subloan.setLoanRemainPrin(BigDecimal.ZERO);
		subloan.setLoanUnsettlePrin(BigDecimal.ZERO);
		subloan.setLoanIntRate(loanReg.getInterestRate()); // 基础利率
		subloan.setLoanPenaltyRate(loanReg.getPenaltyRate()); // 罚息利率
		subloan.setLoanCompRate(loanReg.getCompoundRate()); // 复利利率
		// subloan.setLoanExpireDate(null); //贷款到期日期

		LoanFeeDef loanFeeDef;
		try {
			if (StringUtils.isNotBlank(loanReg.getLoanFeeDefId())) {
				loanFeeDef = unifiedParamFacilityProvide.loanFeeDefByKey(
						// 20151127
						acct.getProductCd(), loanReg.getLoanType(),
						Integer.valueOf(loanReg.getLoanFeeDefId()));
			} else {
				loanFeeDef = unifiedParamFacilityProvide.loanFeeDef(
						loanReg.getLoanCode(), loanReg.getLoanInitTerm(),
						loanReg.getLoanInitPrin());
			}
		} catch (Exception e) {
			throw new ProcessException(MsRespCode.E_1052.getCode(),
					MsRespCode.E_1052.getMessage() + ",找不到贷款产品定价");
		}
		Date loanExpireDate = rescheduleUtils.getLoanPmtDueDate(
				acct.getNextStmtDate(), loanFeeDef, loanReg.getLoanInitTerm());
		subloan.setLoanExpireDate(DateFormatUtil.format(loanExpireDate,
				respDateFormat)); // 贷款到期日期

		subloan.setLoanFloatRate(loanReg.getFloatRate()); // 浮动比例
		subloan.setLoanOverdueDate(null); // 逾期起始日期
		subloan.setLoanAgeCode(null); // 贷款逾期最大期数
		subloan.setOverdueAmt(null); // 逾期欠款总额
		subloan.setLoanMaxDpd(null); // DPD最大值
		subloan.setMaxDpdDate(null); // 最大DPD日期
		subloan.setLoanMaxCpd(null); // CPD最大值
		subloan.setMaxCpdDate(null); // 最大CPD日期
		subloan.setSubPlanList(null);

		// 把期数转化为BigDecimal,用于算月费率
		BigDecimal term = new BigDecimal(loanReg.getLoanInitTerm());

		// 新增协议费率
		subloan.setAgreementRateInd(loanReg.getAgreementRateInd());
		// 贷款服务费率
		subloan.setFeeRate(loanReg.getFeeRate() == null ? null : loanReg.getFeeRate()
				.divide(term, 8, RoundingMode.HALF_UP));
		// 贷款服务费固定金额
		subloan.setFeeAmt(loanReg.getFeeAmt());
		// 寿险费率
		subloan.setLifeInsuFeeRate(loanReg.getLifeInsuFeeRate() == null ? null
				: loanReg.getLifeInsuFeeRate().divide(term, 8,
						RoundingMode.HALF_UP));
		// 寿险固定金额
		subloan.setLifeInsuFeeAmt(loanReg.getLifeInsuFeeAmt());
		// 保费月费率
		subloan.setInsuranceRate(loanReg.getInsuranceRate() == null ? null : loanReg
				.getInsuranceRate().divide(term, 8, RoundingMode.HALF_UP));
		// 保费月固定金额
		subloan.setInsAmt(loanReg.getInsAmt());
		// 分期手续费率
		subloan.setInstallmentFeeRate(loanReg.getInstallmentFeeRate() == null ? null
				: loanReg.getInstallmentFeeRate().divide(term, 8,
						RoundingMode.HALF_UP));
		// 分期手续费固定金额
		subloan.setInstallmentFeeAmt(loanReg.getInstallmentFeeAmt());
		// 提前还款包费率
		subloan.setPrepayPkgFeeRate(loanReg.getPrepayPkgFeeRate() == null ? null
				: loanReg.getPrepayPkgFeeRate().divide(term, 8,
						RoundingMode.HALF_UP));
		// 提前还款包固定金额
		subloan.setPrepayPkgFeeAmt(loanReg.getPrepayPkgFeeAmt());
		subloan.setPenaltyRate(loanReg.getPenaltyRate());
		subloan.setCompoundRate(loanReg.getCompoundRate());
		subloan.setInterestRate(loanReg.getInterestRate());
		subloan.setStampAmt(loanReg.getStampAmt());
		subloan.setStampdutyRate(loanReg.getStampdutyRate());
		subloan.setAgentFeeRate(loanReg.getReplaceSvcFeeRate());
		subloan.setAgentFeeAmount(loanReg.getReplaceSvcFeeAmt());
		// 趸交费金额
		subloan.setPremiumAmt(loanReg.getPremiumAmt());
		// 是否代收趸交费
		subloan.setPremiumInd(loanReg.getPremiumInd());
		return subloan;
	}

	/*
	 * 根据贷款loan获取对应 schedule 与 plan组合 已出的分期取plan 未出的分期取schedule
	 */
	private void setPlanOrScheduleListByLoan(
			STNQLLoanPMTScheByContrIDResp respInfo, CcsLoan loan,
			List<CcsPlan> planlist, List<CcsRepaySchedule> scheduleList,
			STNQLLoanPMTScheByContrIDRespSubLoan subloan, CcsAcctO acctO) {

		List<STNQLLoanPMTScheByContrIDRespSubPlan> subPlanList = new ArrayList<STNQLLoanPMTScheByContrIDRespSubPlan>();
		Map<Integer, CcsRepaySchedule> scheduleMap = transSchedulelistToMap(scheduleList);
		// 获取溢缴款计划余额，用于匹配每期欠款和未到期期款
		BigDecimal depositBal = mcLoanProvideImpl.genLoanDeposit(loan);
		
		for (CcsPlan plan : planlist) {
			// 只出转入计划
			if (plan.getPlanType() == PlanType.I
					|| plan.getPlanType() == PlanType.L
					|| plan.getPlanType() == PlanType.Q) {
				STNQLLoanPMTScheByContrIDRespSubPlan subPlan = setSubPlan(plan,
						scheduleMap);
				subPlanList.add(subPlan);
			}
			
//			// 溢缴款计划
//			if (plan.getPlanType() == PlanType.D) {
//				depositBal = depositBal.add(plan.getCurrBal().abs());
//			}

			// 随借随还 -- 未出账本金字段 ，计算逻辑=随借随还转出计划的已出账单本金+未出账单本金
			if (loan.getLoanType() == LoanType.MCAT
					&& plan.getPlanType() == PlanType.J) {
				subloan.setLoanUnsettlePrin(plan.getCtdPrincipal().add(
						plan.getPastPrincipal()));
			}
		}
		for (CcsRepaySchedule schedule : scheduleList) {
			if (loan.getCurrTerm().compareTo(schedule.getCurrTerm()) < 0) {
				STNQLLoanPMTScheByContrIDRespSubPlan subSchedule = setSubSchedule(
						schedule, subloan);
				subPlanList.add(subSchedule);
			}
			// 合同到期时，合同信息中当期应还款日期与下一应还款日期均为最后一个还款日
			if (loan.getCurrTerm() == loan.getLoanInitTerm()
					&& loan.getCurrTerm() == schedule.getCurrTerm()) {
				respInfo.setPmtDueDate(DateFormatUtil.format(
						schedule.getLoanPmtDueDate(), respDateFormat));
				respInfo.setNextPmtDueDate(DateFormatUtil.format(
						schedule.getLoanPmtDueDate(), respDateFormat));
			}
		}
		// 算出来当期总欠款
		if (scheduleList == null || scheduleList.size() == 0) {
			List<CcsRepayScheduleHst> scheduleHstList = queryCommService
					.getScheduleHstListByLoan(loan.getLoanId());

			tmpTotAmte(subPlanList,
					queryCommService.tmpTotAmteMapHst(scheduleHstList));
		} else {

			tmpTotAmte(subPlanList, tmpTotAmteMap(scheduleList));
		}
		
		// 2016-5-2商品贷主动还期款：计算当日溢缴款（溢缴款计划余额+MemoCr-MemoDb），
		// 用溢缴款按先后顺序匹配所有plan和schedule，每个plan或schedule欠款还清时，planStatus置为还清，未到期schedule只匹配最近一期
		// 待分配金额
		BigDecimal toAssignAmt = depositBal.add(acctO.getMemoCr()).subtract(acctO.getMemoDb());
		toAssignAmt = 
				toAssignAmt.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : toAssignAmt;
		// 转换planList为Map，用来校验还款计划是否已到期
		Map<Integer, CcsPlan> planMap = transPlanlistToMap(planlist);
		for(int i=1;i<=loan.getLoanInitTerm();i++){
			for (STNQLLoanPMTScheByContrIDRespSubPlan subPlan : subPlanList) {
				if(subPlan.getPlanCurrTerm() != null && subPlan.getPlanCurrTerm() == i){
					CcsPlan plan = planMap.get(i);
					BigDecimal planCurrBal = subPlan.getPlanCurrBal();
					BigDecimal scheduleCurrBal = subPlan.getScheduleCurrBal();
					// 如果plan不为null，说明已到期
					if(plan != null){
						if(toAssignAmt.compareTo(planCurrBal) >= 0){
							subPlan.setPlanStatus(TermAmtPaidOutInd.F);
							subPlan.setPlanCurrBal(BigDecimal.ZERO);
							toAssignAmt = toAssignAmt.subtract(planCurrBal);					
						}else{
							subPlan.setPlanStatus(TermAmtPaidOutInd.W);
							subPlan.setPlanCurrBal(planCurrBal.subtract(toAssignAmt));
							toAssignAmt = BigDecimal.ZERO;
						}
					}else{
						//未到期还款计划
						if(toAssignAmt.compareTo(scheduleCurrBal) >= 0){
							subPlan.setPlanStatus(TermAmtPaidOutInd.F);
							subPlan.setPlanCurrBal(BigDecimal.ZERO);
							toAssignAmt = toAssignAmt.subtract(planCurrBal);
						}else{
							subPlan.setPlanStatus(TermAmtPaidOutInd.O);
							toAssignAmt = BigDecimal.ZERO;
						}
					}
				}
			}
		}
		
		subloan.setSubPlanList(subPlanList);
	}

	/*
	 * 赋值偿清计划信息(非随借随还)//////
	 */
	private void setNoMCATStmtListInfo(CcsAcct acct, CcsAcctO accto,
			List<CcsRepaySchedule> scheduleList, List<CcsPlan> planlist, CcsLoan loan,
			STNQLLoanPMTScheByContrIDRespSubLoan subloan) {

		List<STNQLLoanPMTScheByContrIDRespSubStmt> subStmtList = new ArrayList<STNQLLoanPMTScheByContrIDRespSubStmt>();
		Map<Integer, CcsPlan> planmap = transPlanlistToMap(planlist);

		for (CcsRepaySchedule schedule : scheduleList) {
			Integer scheduleTerm = schedule.getCurrTerm();
			// 拿到对应term
			CcsPlan plan = planmap.get(scheduleTerm);
			STNQLLoanPMTScheByContrIDRespSubStmt subStmt = new STNQLLoanPMTScheByContrIDRespSubStmt();
			// 该期还款计划是否已出过plan
			Indicator planHaveGened = Indicator.Y;
			
			//如果plan为空，并当期期数大于loan上面的当期期数，则未出plan
			if (plan == null && loan.getCurrTerm() < scheduleTerm) {
				planHaveGened = Indicator.N;
			}
			// 代收服务费

			subStmt.setAgentFee(schedule.getLoanReplaceSvcFee() == null ? BigDecimal.ZERO
					: schedule.getLoanReplaceSvcFee());
			subStmt.setAgentFeeMatchInd(getStmtAmtTermAmtPaidOutInd(
					planHaveGened,
					plan == null ? null
							: (plan.getCtdReplaceSvcFee() == null ? BigDecimal.ZERO
									: plan.getCtdReplaceSvcFee()).add(plan
									.getPastReplaceSvcFee() == null ? BigDecimal.ZERO
									: plan.getPastReplaceSvcFee())));

			subStmt.setInterest(schedule.getLoanTermInt());
			subStmt.setInterestMatchInd(getStmtAmtTermAmtPaidOutInd(
					planHaveGened, plan == null ? null : plan.getCtdInterest()
							.add(plan.getPastInterest())));
			subStmt.setLifeInsuFee(schedule.getLoanLifeInsuAmt());
			subStmt.setLifeInsuFeeMatchInd(getStmtAmtTermAmtPaidOutInd(
					planHaveGened, plan == null ? null : plan
							.getCtdLifeInsuAmt().add(plan.getPastLifeInsuAmt())));
			if (plan != null) {
				subStmt.setMulctFee(plan.getCtdMulctAmt().add(
						plan.getPastMulctAmt()));
				subStmt.setMulctFeeMatchInd(getStmtAmtTermAmtPaidOutInd(
						planHaveGened, subStmt.getMulctFee()));
			} else {
				subStmt.setMulctFee(BigDecimal.ZERO);
				subStmt.setMulctFeeMatchInd(TermAmtPaidOutInd.O);
			}

			subStmt.setPmtDueDate(DateFormatUtil.format(
					schedule.getLoanPmtDueDate(), respDateFormat));
			subStmt.setPrepaymentFee(schedule.getLoanPrepayPkgAmt() == null ? BigDecimal.ZERO
					: schedule.getLoanPrepayPkgAmt());
			subStmt.setPrepaymentFeeMatchInd(getStmtAmtTermAmtPaidOutInd(
					planHaveGened,
					plan == null ? null : plan.getCtdPrepayPkgFee().add(
							plan.getPastPrepayPkgFee())));
			subStmt.setPrinciple(schedule.getLoanTermPrin());
			subStmt.setPrincipleMatchInd(getStmtAmtTermAmtPaidOutInd(
					planHaveGened, plan == null ? null : plan.getCtdPrincipal()
							.add(plan.getPastPrincipal())));
			subStmt.setStmtId(schedule.getScheduleId().toString());
			subStmt.setSvcFee(schedule.getLoanTermFee());
			subStmt.setSvcFeeMatchInd(getStmtAmtTermAmtPaidOutInd(
					planHaveGened, plan == null ? null : plan.getCtdSvcFee()
							.add(plan.getPastSvcFee())));

			// 期款金额
			subStmt.setTermAmt(schedule.getLoanInsuranceAmt() == null ? BigDecimal.ZERO
					: schedule.getLoanInsuranceAmt()
							.add(schedule.getLoanLifeInsuAmt() == null ? BigDecimal.ZERO
									: schedule.getLoanLifeInsuAmt())
							.add(schedule.getLoanPrepayPkgAmt() == null ? BigDecimal.ZERO
									: schedule.getLoanPrepayPkgAmt())
							.add(schedule.getLoanStampdutyAmt() == null ? BigDecimal.ZERO
									: schedule.getLoanStampdutyAmt())
							.add(schedule.getLoanTermFee() == null ? BigDecimal.ZERO
									: schedule.getLoanTermFee())
							.add(schedule.getLoanTermInt() == null ? BigDecimal.ZERO
									: schedule.getLoanTermInt())
							.add(schedule.getLoanTermPrin() == null ? BigDecimal.ZERO
									: schedule.getLoanTermPrin())
							.add(schedule.getLoanSvcFee() == null ? BigDecimal.ZERO
									: schedule.getLoanSvcFee())
							.add(schedule.getLoanReplaceSvcFee() == null ? BigDecimal.ZERO
									: schedule.getLoanReplaceSvcFee()));

			// 期款匹配状态
			if (plan == null && loan.getCurrTerm() > scheduleTerm) {
				subStmt.setTermAmtMatchInd("A");// 未到期
			} else if (constractStatusUtil.isContractOverDue(acct, accto) == Indicator.Y) {
				subStmt.setTermAmtMatchInd("C");// 已逾期
			} else if ((plan == null ? BigDecimal.ZERO : plan.getCurrBal()).compareTo(BigDecimal.ZERO) <= 0) {
				subStmt.setTermAmtMatchInd("B");// 已还清
			} else {
				subStmt.setTermAmtMatchInd("D");// 未还清
			}
			if (plan != null) {
				subStmt.setTermAmtPaidOutDate(DateFormatUtil.format(
						plan.getPaidOutDate(), respDateFormat));
			}

			subStmt.setCooperArMulctFee(plan == null ? BigDecimal.ZERO :(plan.getCtdReplaceMulct().add(
					plan.getPastReplaceMulct())));// 客户方应收罚金
			subStmt.setCooperArMulctFeeMatchInd(getStmtAmtTermAmtPaidOutInd(
					planHaveGened, subStmt.getCooperArMulctFee()));// 客户方应收罚金匹配状态
			
			subStmt.setCooperArPenaltyInt(plan == null ? BigDecimal.ZERO : (plan.getCtdReplacePenalty().add(
					plan.getPastReplacePenalty())));// 客户方应收罚息
			subStmt.setCooperArPenaltyIntMatchInd(getStmtAmtTermAmtPaidOutInd(
					planHaveGened, subStmt.getCooperArPenaltyInt()));// 客户方应收罚息匹配状态

			subStmt.setCooperArDuePenalty(plan == null ? BigDecimal.ZERO : (plan.getCtdReplaceLateFee().add(
					plan.getPastReplaceLateFee())));// 客户方应收违约金
			subStmt.setCooperArDuePenaltyMatchInd(getStmtAmtTermAmtPaidOutInd(
					planHaveGened, subStmt.getCooperArDuePenalty()));//客户方应收违约金匹配状态
			
			subStmt.setCooperPrepaymentFee(plan == null ? BigDecimal.ZERO : (plan.getCtdReplaceTxnFee().add(
					plan.getCtdReplaceTxnFee())));// 客户方提前还款手续费
			subStmt.setCooperPrepaymentFeeMatchInd(getStmtAmtTermAmtPaidOutInd(
					planHaveGened, subStmt.getCooperPrepaymentFee()));//客户方提前还款手续费匹配状态

			subStmtList.add(subStmt);
		}
		subloan.setSubStmtList(subStmtList);
	}

	/*
	 * 赋值偿清计划列表（随借随还情况）
	 */
	private void setMCATStmtListInfo(List<CcsStatement> stmtList,
			STNQLLoanPMTScheByContrIDRespSubLoan subloan) {

		List<STNQLLoanPMTScheByContrIDRespSubStmt> subStmtList = new ArrayList<STNQLLoanPMTScheByContrIDRespSubStmt>();
		for (CcsStatement stmt : stmtList) {
			STNQLLoanPMTScheByContrIDRespSubStmt subStmt = new STNQLLoanPMTScheByContrIDRespSubStmt();
			subStmt.setStmtId(stmt.getAcctNbr().toString()
					+ DateFormatUtil.format(stmt.getStmtDate(), respDateFormat));
			subStmt.setPmtDueDate(DateFormatUtil.format(stmt.getPmtDueDate()));
			subStmt.setTermAmt(stmt.getQualGraceBal());
			subStmt.setPrinciple(stmt.getCtdRetailAmt().add(
					stmt.getCtdCashAmt()));
			subStmt.setInterest(stmt.getCtdInterestAmt());
			subStmt.setSvcFee(stmt.getCtdFeeAmt());
			subStmtList.add(subStmt);
		}
		subloan.setSubStmtList(subStmtList);

	}

	/*
	 * 根据plan赋值 subplan
	 */
	public STNQLLoanPMTScheByContrIDRespSubPlan setSubPlan(CcsPlan plan,
			Map<Integer, CcsRepaySchedule> scheduleMap) {
		STNQLLoanPMTScheByContrIDRespSubPlan subPlan = new STNQLLoanPMTScheByContrIDRespSubPlan();
		subPlan.setPlanCurrTerm(plan.getTerm()); // 当前期数
		subPlan.setPlanCurrBal(plan.getCurrBal()); // 总欠款
		subPlan.setPlanPaidOutDate(DateFormatUtil.format(plan.getPaidOutDate(),
				respDateFormat)); // 还清日期
		subPlan.setLoanTermPrin(plan.getCtdPrincipal().add(
				plan.getPastPrincipal())); // 应收本金
		subPlan.setArInt(plan.getCtdInterest().add(plan.getPastInterest())); // 应收利息
		subPlan.setArAnnualFee(plan.getCtdCardFee().add(plan.getPastCardFee())); // 应收年费
		subPlan.setArSvcFee(plan.getCtdSvcFee().add(plan.getPastSvcFee())); // 应收服务费
		subPlan.setArDuePenalty(plan.getCtdLateFee().add(plan.getPastLateFee())); // 应收违约金
		subPlan.setArTxnFee(plan.getCtdTxnFee().add(plan.getPastTxnFee())); // 应收交易费
		subPlan.setArStamp(plan.getCtdStampdutyAmt().add(
				plan.getPastStampdutyAmt())); // 应收印花税
		subPlan.setArLifeInsuFee(plan.getCtdLifeInsuAmt().add(
				plan.getPastLifeInsuAmt())); // 应收寿险费
		subPlan.setArPrepayPkgFee(plan.getCtdPrepayPkgFee().add(
				plan.getPastPrepayPkgFee()));// 应收灵活还款服务包费
		
		// 1
		subPlan.setArAgentFee(plan.getCtdReplaceSvcFee() == null ? BigDecimal.ZERO
				: plan.getCtdReplaceSvcFee().add(
						plan.getPastReplaceSvcFee() == null ? BigDecimal.ZERO
								: plan.getPastReplaceSvcFee()));// 应代收服务费

		subPlan.setArMulctFee(plan.getCtdMulctAmt().add(plan.getPastMulctAmt())); // 应收罚金

		subPlan.setCooperArMulctFee(plan.getCtdReplaceMulct().add(
				plan.getPastReplaceMulct()));// 客户方应收罚金
		subPlan.setCooperArPenaltyInt(plan.getCtdReplacePenalty().add(
				plan.getPastReplacePenalty()));// 客户方应收罚息
		subPlan.setCooperArDuePenalty(plan.getCtdReplaceLateFee().add(
				plan.getPastReplaceLateFee()));// 客户方应收违约金
		subPlan.setCooperPrepaymentFee(plan.getCtdReplaceTxnFee().add(
				plan.getCtdReplaceTxnFee()));// 客户方提前还款手续费

		subPlan.setArPenaltyInt(plan.getCtdPenalty().add(plan.getPastPenalty())); // 应收罚息
		subPlan.setArCompoundInt(plan.getCtdCompound().add(
				plan.getPastCompound())); // 应收复利
		subPlan.setAcruInt(plan
				.getNodefbnpIntAcru()
				.setScale(2, RoundingMode.HALF_UP)
				.add(plan.getBegDefbnpIntAcru().setScale(2,
						RoundingMode.HALF_UP))
				.add(plan.getCtdDefbnpIntAcru().setScale(2,
						RoundingMode.HALF_UP))); // 应计利息
		subPlan.setAcruPenaltyInt(plan.getPenaltyAcru().setScale(2,
				RoundingMode.HALF_UP)); // 应计罚息
		subPlan.setCooperAcruPenaltyInt(plan.getReplacePenaltyAcru().setScale(2,
				RoundingMode.HALF_UP)); // 客户方应计罚息
		subPlan.setAcruCompoundInt(plan.getCompoundAcru().setScale(2,
				RoundingMode.HALF_UP)); // 应计复利
		// 为plan_list的plan表数据赋值到期还款日 by zqx lizz 20160304
		if (null != scheduleMap && scheduleMap.size() > 0
				&& null != plan.getTerm() && plan.getTerm() > 0) {
			CcsRepaySchedule schedule = scheduleMap.get(plan.getTerm());
			subPlan.setLoanPmtBueDate(DateFormatUtil.format(
					schedule.getLoanPmtDueDate(), respDateFormat));
		}
		return subPlan;
	}

	/*
	 * 根据schedule赋值 subplan
	 */
	public STNQLLoanPMTScheByContrIDRespSubPlan setSubSchedule(
			CcsRepaySchedule schedule,
			STNQLLoanPMTScheByContrIDRespSubLoan subloan) {
		STNQLLoanPMTScheByContrIDRespSubPlan subSchedule = new STNQLLoanPMTScheByContrIDRespSubPlan();
		BigDecimal currAmt = BigDecimal.ZERO
				.add(schedule.getLoanInsuranceAmt() == null ? BigDecimal.ZERO
						: schedule.getLoanInsuranceAmt())
				.add(schedule.getLoanLifeInsuAmt() == null ? BigDecimal.ZERO
						: schedule.getLoanLifeInsuAmt())
				.add(schedule.getLoanPrepayPkgAmt() == null ? BigDecimal.ZERO
						: schedule.getLoanPrepayPkgAmt())
				.add(schedule.getLoanStampdutyAmt() == null ? BigDecimal.ZERO
						: schedule.getLoanStampdutyAmt())
				.add(schedule.getLoanTermFee() == null ? BigDecimal.ZERO
						: schedule.getLoanTermFee())
				.add(schedule.getLoanTermInt() == null ? BigDecimal.ZERO
						: schedule.getLoanTermInt())
				.add(schedule.getLoanTermPrin() == null ? BigDecimal.ZERO
						: schedule
								.getLoanTermPrin()
								.add(schedule.getLoanSvcFee() == null ? BigDecimal.ZERO
										: schedule.getLoanSvcFee())
								.add(schedule.getLoanReplaceSvcFee() == null ? BigDecimal.ZERO
										: schedule.getLoanReplaceSvcFee()));

		subSchedule.setPlanCurrTerm(schedule.getCurrTerm()); // 当前期数
		subSchedule.setPlanCurrBal(currAmt); // 总欠款
		subSchedule.setPlanPaidOutDate(null); // 还清日期
		subSchedule.setLoanTermPrin(schedule.getLoanTermPrin()); // 应收本金
		subSchedule.setArInt(schedule.getLoanTermInt()); // 应收利息
		subSchedule.setArAnnualFee(null); // 应收年费
		subSchedule.setArSvcFee(schedule.getLoanTermFee()); // 应收服务费
		subSchedule
				.setArAgentFee(schedule.getLoanReplaceSvcFee() == null ? BigDecimal.ZERO
						: schedule.getLoanReplaceSvcFee());// 应收代收服务费
		subSchedule.setArDuePenalty(null); // 应收违约金
		subSchedule.setArTxnFee(null); // 应收交易费
		subSchedule.setArStamp(schedule.getLoanStampdutyAmt()); // 应收印花税
		subSchedule.setArLifeInsuFee(schedule.getLoanLifeInsuAmt()); // 应收寿险费
		subSchedule.setArMulctFee(null); // 应收罚金
		subSchedule.setArPenaltyInt(null); // 应收罚息
		subSchedule.setArCompoundInt(null); // 应收复利
		subSchedule.setAcruInt(null); // 应计利息
		subSchedule.setAcruPenaltyInt(null); // 应计罚息
		subSchedule.setCooperAcruPenaltyInt(null);// 客户方应计罚息
		subSchedule.setAcruCompoundInt(null); // 应计复利
		subSchedule.setCooperArDuePenalty(null);// 客户方应收罚金
		subSchedule.setCooperArMulctFee(null);// 客户方应收罚息
		subSchedule.setCooperArPenaltyInt(null);// 客户方应收违约金
		subSchedule.setCooperPrepaymentFee(null);// 客户方提前还款手续费
		subSchedule.setArPrepayPkgFee(schedule.getLoanPrepayPkgAmt());//应收灵活还款服务包费
		subSchedule.setLoanPmtBueDate(DateFormatUtil.format(
				schedule.getLoanPmtDueDate(), respDateFormat));
		// 获取loan下一期期款
		if (schedule.getCurrTerm() == subloan.getLoanCurrTerm() + 1) {
			subloan.setNextTermAmt(currAmt);
		}
		// 计算剩余未还款金额
		subloan.setLoanRemainAmt(subloan
				.getLoanRemainAmt()
				.add(schedule.getLoanInsuranceAmt() == null ? BigDecimal.ZERO
						: schedule.getLoanInsuranceAmt())
				.add(schedule.getLoanLifeInsuAmt() == null ? BigDecimal.ZERO
						: schedule.getLoanLifeInsuAmt())
				.add(schedule.getLoanPrepayPkgAmt() == null ? BigDecimal.ZERO
						: schedule.getLoanPrepayPkgAmt())
				.add(schedule.getLoanStampdutyAmt() == null ? BigDecimal.ZERO
						: schedule.getLoanStampdutyAmt())
				.add(schedule.getLoanTermFee() == null ? BigDecimal.ZERO
						: schedule.getLoanTermFee())
				.add(schedule.getLoanTermInt() == null ? BigDecimal.ZERO
						: schedule.getLoanTermInt())
				.add(schedule.getLoanTermPrin() == null ? BigDecimal.ZERO
						: schedule.getLoanTermPrin())
				.add(schedule.getLoanSvcFee() == null ? BigDecimal.ZERO
						: schedule.getLoanSvcFee())
				.add(schedule.getLoanReplaceSvcFee() == null ? BigDecimal.ZERO
						: schedule.getLoanReplaceSvcFee()));
		// 计算剩余未到期本金
		subloan.setLoanRemainPrin(subloan.getLoanRemainPrin().add(
				schedule.getLoanTermPrin() == null ? BigDecimal.ZERO : schedule
						.getLoanTermPrin()));

		return subSchedule;
	}

	/*
	 * 根据合同号取账户信息
	 */
	public CcsAcct getAcctByContrNbr(String contrNbr) {
		CcsAcct ccsAcct = new CcsAcct();

		try {
			QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;

			ccsAcct = new JPAQuery(em).from(qCcsAcct)
					.where(qCcsAcct.contrNbr.eq(contrNbr))
					.singleResult(qCcsAcct);
			if (null == ccsAcct) {
				throw new ProcessException(MsRespCode.E_1003.getCode(),
						MsRespCode.E_1003.getMessage());
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1003.getCode(),
					MsRespCode.E_1003.getMessage());
		}
		return ccsAcct;
	}

	/*
	 * 把planlist 转成 planmap key为plan期数
	 */
	public Map<Integer, CcsPlan> transPlanlistToMap(List<CcsPlan> planlist) {
		Map<Integer, CcsPlan> planmap = new HashMap<Integer, CcsPlan>();
		for (CcsPlan plan : planlist) {
			planmap.put(plan.getTerm(), plan);
		}
		return planmap;
	}

	/*
	 * 把schedulelist 转成 schedulemap key为schedule期数
	 */
	public Map<Integer, CcsRepaySchedule> transSchedulelistToMap(
			List<CcsRepaySchedule> schedulelist) {
		Map<Integer, CcsRepaySchedule> schedulemap = new HashMap<Integer, CcsRepaySchedule>();
		for (CcsRepaySchedule schedule : schedulelist) {
			schedulemap.put(schedule.getCurrTerm(), schedule);
		}
		return schedulemap;
	}

	/*
	 * 返回偿清计划 某个余额还清标志
	 */
	private TermAmtPaidOutInd getStmtAmtTermAmtPaidOutInd(
			Indicator planHaveGened, BigDecimal planAmt) {
		
		//如果金额为空，则默认为零
		planAmt = planAmt == null ? BigDecimal.ZERO : planAmt;
		
		if (planHaveGened == Indicator.N) {
			return TermAmtPaidOutInd.O;
		} else if (planAmt.compareTo(BigDecimal.ZERO) == 0) {
			return TermAmtPaidOutInd.F;
		} else {
			return TermAmtPaidOutInd.W;
		}
	}

	/**
	 * 获取最早激活日期
	 * 
	 * @param subLoanList
	 * @return
	 */
	public Date getLoanDate(
			List<STNQLLoanPMTScheByContrIDRespSubLoan> subLoanList) {

		Date activeDate = null;
		Date date = null;
		for (STNQLLoanPMTScheByContrIDRespSubLoan subLoan : subLoanList) {
			if (subLoan.getLoanActiveDate() == null) {
				continue;
			}
			try {
				date = new SimpleDateFormat("yyyyMMdd").parse(subLoan
						.getLoanActiveDate());
				if (activeDate == null) {
					activeDate = date;
				} else {
					if (activeDate.after(date)) {
						activeDate = date;
					}
				}
			} catch (ParseException e1) {
				e1.printStackTrace();
			}

		}

		return activeDate;
	}

	/**
	 * cpd天数
	 * 
	 * @param loanlist
	 * @return
	 */
	public int getloanCpd(List<CcsLoan> loanlist) {
		// 当前业务日期
		Date bizDate = globalManagementService.getSystemStatus()
				.getBusinessDate();
		Date cpdDate = null;
		Date date = null;
		int cpd = 0;
		for (CcsLoan loan : loanlist) {
			if (loan.getCpdBeginDate() == null) {
				continue;
			}
			date = loan.getCpdBeginDate();
			if (cpdDate == null) {
				cpdDate = date;
			} else {
				if (cpdDate.after(date)) {
					cpdDate = date;
				}
			}
		}
		if (cpdDate != null) {
			cpd = DateUtils.getIntervalDays(cpdDate, bizDate);
		}
		return cpd;
	}

	/**
	 * cpd天数
	 * 
	 * @param loanlist
	 * @return
	 */
	public int getloanDpd(List<CcsLoan> loanlist) {
		// 当前业务日期
		Date bizDate = globalManagementService.getSystemStatus()
				.getBusinessDate();
		Date cpdDate = null;
		Date date = null;
		int Dpd = 0;
		for (CcsLoan loan : loanlist) {

			if (loan.getOverdueDate() == null) {
				continue;
			}
			date = loan.getOverdueDate();
			if (cpdDate == null) {
				cpdDate = date;
			} else {
				if (cpdDate.after(date)) {
					cpdDate = date;
				}
			}
		}
		if (cpdDate != null) {
			Dpd = DateUtils.getIntervalDays(cpdDate, bizDate);
		}
		return Dpd;
	}

	/*
	 * 把schedule总欠款计算出来 key为schedule期数
	 */
	private Map<Integer, BigDecimal> tmpTotAmteMap(List<CcsRepaySchedule> scheduleList) {
		
		Map<Integer, BigDecimal> tmpTotAmteMap = new HashMap<Integer, BigDecimal>();
		for (CcsRepaySchedule schedule : scheduleList) {
			BigDecimal tmpTotAmt = BigDecimal.ZERO;
			// 当期总欠款
			tmpTotAmt = tmpTotAmt
					.add(schedule.getLoanTermPrin() == null ? BigDecimal.ZERO
							: schedule.getLoanTermPrin())
					.add(schedule.getLoanTermFee() == null ? BigDecimal.ZERO
							: schedule.getLoanTermFee())
					.add(schedule.getLoanTermInt() == null ? BigDecimal.ZERO
							: schedule.getLoanTermInt())
					.add(schedule.getLoanStampdutyAmt() == null ? BigDecimal.ZERO
							: schedule.getLoanStampdutyAmt())
					.add(schedule.getLoanLifeInsuAmt() == null ? BigDecimal.ZERO
							: schedule.getLoanLifeInsuAmt())
					.add(schedule.getLoanSvcFee() == null ? BigDecimal.ZERO
							: schedule.getLoanSvcFee())
					.add(schedule.getLoanReplaceSvcFee() == null ? BigDecimal.ZERO
							: schedule.getLoanReplaceSvcFee())
					.add(schedule.getLoanInsuranceAmt() == null ? BigDecimal.ZERO
							: schedule.getLoanInsuranceAmt())
					.add(schedule.getLoanPrepayPkgAmt() == null ? BigDecimal.ZERO
							: schedule.getLoanPrepayPkgAmt());
			tmpTotAmteMap.put(schedule.getCurrTerm(), tmpTotAmt);
		}
		return tmpTotAmteMap;
	}

	// 对plan层赋值
	private void tmpTotAmte(List<STNQLLoanPMTScheByContrIDRespSubPlan> subPlanList,Map<Integer, BigDecimal> tmpTotAmteMap) {
		
		for (STNQLLoanPMTScheByContrIDRespSubPlan subPlan : subPlanList) {
			subPlan.setScheduleCurrBal(tmpTotAmteMap.get(subPlan
					.getPlanCurrTerm()));
		}
	}
}
