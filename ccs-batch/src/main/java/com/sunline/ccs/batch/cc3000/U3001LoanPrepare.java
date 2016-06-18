package com.sunline.ccs.batch.cc3000;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.common.BatchUtils;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanRegHst;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.CcsRepayScheduleHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsPostingTmp;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.pcm.param.def.FinancialOrg;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.LoanAction;

import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.PostTxnType;
import com.sunline.ppy.dictionary.enums.PostingFlag;
import com.sunline.ppy.dictionary.report.ccs.LoanSuccessRptItem;
//import com.sunline.smsd.service.sdk.LoanRescheduleMsgItem;

@Component
public class U3001LoanPrepare {
	
	private static final String ERR_NO_MATCHES = "未匹配到原分期交易";
	
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private BatchUtils batchUtils;
	@Autowired
    private BatchStatusFacility batchFacility;
	@PersistenceContext
	private EntityManager em;
	
	/**
	 * @see 方法名：matchCcsLoan 
	 * @see 描述：通过CcsLoanReg匹配CcsLoan
	 * @see 创建日期：2015-6-23下午7:33:30
	 * @author ChengChun
	 *  
	 * @param loanReg
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public CcsLoan matchCcsLoan(CcsLoanReg loanReg) {
		QCcsLoan qTmLoan = QCcsLoan.ccsLoan;
		CcsLoan origLoan = new JPAQuery(em).from(qTmLoan)
				.where(qTmLoan.logicCardNbr.eq(loanReg.getLogicCardNbr()).and(qTmLoan.refNbr.eq(loanReg.getRefNbr())))
				.singleResult(qTmLoan);
		if(origLoan == null){
			loanReg.setRemark(ERR_NO_MATCHES);
		}
		return origLoan;
	}
	
	
	/**
	 * @see 方法名：matchPostingTmp 
	 * @see 描述：根据CcsLoanReg匹配待入账交易CcsPostingTmp
	 * @see 创建日期：2015-6-23下午7:35:24
	 * @author ChengChun
	 *  
	 * @param loanReg
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public CcsPostingTmp matchPostingTmp(CcsLoanReg loanReg) {
		// 未超期匹配ttTxnPost，有则出loan，无则跳过；
		QCcsPostingTmp qTtTxnPost = QCcsPostingTmp.ccsPostingTmp;
		SimpleDateFormat sdf = new SimpleDateFormat("MMdd");
		Integer month = Integer.valueOf(sdf.format(loanReg.getOrigTransDate()).substring(0, 2));
		Integer date = Integer.valueOf(sdf.format(loanReg.getOrigTransDate()).substring(2, 4));
		JPAQuery query = new JPAQuery(em);
		CcsPostingTmp txnPost = query.from(qTtTxnPost)
				.where(qTtTxnPost.cardNbr.eq(loanReg.getCardNbr())
						.and(qTtTxnPost.txnAmt.eq(loanReg.getOrigTxnAmt()))
						.and(qTtTxnPost.authCode.eq(loanReg.getOrigAuthCode()))
						.and(qTtTxnPost.txnDate.month().eq(month))
						.and(qTtTxnPost.txnDate.dayOfMonth().eq(date))
						.and(qTtTxnPost.refNbr.eq(loanReg.getRefNbr()))
						.and(qTtTxnPost.txnAmt.ne(BigDecimal.ZERO)))
				.singleResult(qTtTxnPost);
		return txnPost;
	}

	
	/**
	 * 
	 * @see 方法名：generatePostingTmp 
	 * @see 描述：创建CcsPostingTmp
	 * @see 创建日期：2015-6-23下午7:36:48
	 * @author ChengChun
	 *  
	 * @param loanReg
	 * @param sysTxnCd
	 * @param cardNo
	 * @param logicCardNbr
	 * @param bscLogiccardNo
	 * @param productCd
	 * @param txnAmt
	 * @param term
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public CcsPostingTmp generatePostingTmp(CcsLoanReg loanReg, SysTxnCd sysTxnCd, String cardNo, 
			String logicCardNbr, String bscLogiccardNo, String productCd, BigDecimal txnAmt, Integer term) {
		
		SysTxnCdMapping sysTxnCdMapping = parameterFacility.loadParameter(sysTxnCd.toString(), SysTxnCdMapping.class);
		TxnCd txnCd = parameterFacility.loadParameter(sysTxnCdMapping.txnCd, TxnCd.class);
		
		CcsCard card = queryFacility.getCardByCardNbr(loanReg.getCardNbr());
		// 获取卡产品信息
		ProductCredit productCredit = parameterFacility.loadParameter(card.getProductCd(), ProductCredit.class);
		FinancialOrg financialOrg = parameterFacility.loadParameter(productCredit.financeOrgNo, FinancialOrg.class);

		
		CcsPostingTmp ttTxnPost = new CcsPostingTmp();
		
		ttTxnPost.setOrg(loanReg.getOrg());
		ttTxnPost.setAcctNbr(loanReg.getAcctNbr());
		ttTxnPost.setAcctType(loanReg.getAcctType());
		ttTxnPost.setCardNbr(cardNo);
		ttTxnPost.setLogicCardNbr(logicCardNbr);
		ttTxnPost.setCardBasicNbr(bscLogiccardNo);
		ttTxnPost.setProductCd(productCd);
		ttTxnPost.setTxnTime(loanReg.getRequestTime());
		ttTxnPost.setTxnCurrency(loanReg.getAcctType().getCurrencyCode());
		ttTxnPost.setTxnAmt(txnAmt);
		ttTxnPost.setPostDate(batchFacility.getBatchDate());
		ttTxnPost.setPostCurrency(loanReg.getAcctType().getCurrencyCode());
		ttTxnPost.setPostAmt(txnAmt);
		ttTxnPost.setPostTxnType(PostTxnType.M);
		ttTxnPost.setTxnCode(txnCd.txnCd);
		ttTxnPost.setOrigTxnCode(txnCd.txnCd);
		ttTxnPost.setDbCrInd(txnCd.logicMod.getDbCrInd());
		ttTxnPost.setTxnDesc(txnCd.description);
		ttTxnPost.setTxnShortDesc(txnCd.shortDesc);
		ttTxnPost.setOrigTxnAmt(txnAmt);
		ttTxnPost.setOrigSettAmt(txnAmt);
		ttTxnPost.setMcc(null);
		if(sysTxnCd == SysTxnCd.S36){
			ttTxnPost.setAuthCode(loanReg.getOrigAuthCode());
			ttTxnPost.setTxnDate(batchUtils.fixYear(loanReg.getB007TxnTime(), batchFacility.getBatchDate()));
		} else if(sysTxnCd == SysTxnCd.S67){
			ttTxnPost.setAuthCode(null);
			ttTxnPost.setTxnDate(batchFacility.getBatchDate()); // 提前结清的交易日期和时间取当前批量日期
			ttTxnPost.setTxnTime(batchFacility.getBatchDate());
		}else{
			ttTxnPost.setAuthCode(null);
			ttTxnPost.setTxnDate(loanReg.getRegisterDate());
		}
		ttTxnPost.setRefNbr(loanReg.getRefNbr());
		ttTxnPost.setVoucherNo(null);
		ttTxnPost.setCardBlockCode(null);
		ttTxnPost.setStmtDate(null);
		ttTxnPost.setOrigTransDate(loanReg.getRegisterDate());
		ttTxnPost.setOrigPmtAmt(BigDecimal.ZERO);
		ttTxnPost.setPoints(BigDecimal.ZERO);
		ttTxnPost.setPrePostingFlag(PostingFlag.F00);
		ttTxnPost.setPostingFlag(PostingFlag.F00);
		ttTxnPost.setAcqBranchIq(null);
		ttTxnPost.setAcqTerminalId(null);
		ttTxnPost.setAcqAcceptorId(financialOrg.acqAcceptorId);
		ttTxnPost.setAcqAddress(null);
		ttTxnPost.setRelPmtAmt(BigDecimal.ZERO);
		ttTxnPost.setInterchangeFee(BigDecimal.ZERO);
		ttTxnPost.setFeePayout(BigDecimal.ZERO);
		ttTxnPost.setFeeProfit(BigDecimal.ZERO);
		ttTxnPost.setLoanIssueProfit(BigDecimal.ZERO);
		ttTxnPost.setTerm(term);
		ttTxnPost.setLoanCode(loanReg.getLoanCode());
		return ttTxnPost;
	}
	
	
	/**
	 * @see 方法名：generateLoan 
	 * @see 描述：创建分期主信息
	 * @see 创建日期：2015-6-23下午7:38:02
	 * @author ChengChun
	 *  
	 * @param loanReg
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public CcsLoan generateLoan(CcsLoanReg loanReg) {
		CcsLoan loan = new CcsLoan();
		
		loan.setOrg(loanReg.getOrg());
		loan.setAcctNbr(loanReg.getAcctNbr());
		loan.setAcctType(loanReg.getAcctType());
		loan.setRefNbr(loanReg.getRefNbr());
		loan.setLogicCardNbr(loanReg.getLogicCardNbr());
		loan.setCardNbr(loanReg.getCardNbr());
		loan.setRegisterDate(loanReg.getRegisterDate());
		loan.setLoanType(loanReg.getLoanType());
		loan.setLoanStatus(LoanStatus.A);
		loan.setLastLoanStatus(null);
		loan.setLoanInitTerm(loanReg.getLoanInitTerm());
		loan.setCurrTerm(0);
		loan.setRemainTerm(loanReg.getLoanInitTerm());
		loan.setLoanCode(loanReg.getLoanCode());
		loan.setLoanInitPrin(loanReg.getLoanInitPrin()); // 分期总本金
		loan.setLoanFixedPmtPrin(loanReg.getLoanFixedPmtPrin());
		loan.setLoanFirstTermPrin(loanReg.getLoanFirstTermPrin());
		loan.setLoanFinalTermPrin(loanReg.getLoanFinalTermPrin());
		loan.setLoanInitFee(loanReg.getLoanInitFee());
		loan.setLoanFixedFee(loanReg.getLoanFixedFee());
		loan.setLoanFirstTermFee(loanReg.getLoanFirstTermFee());
		loan.setLoanFinalTermFee(loanReg.getLoanFinalTermFee());
		loan.setUnstmtPrin(loanReg.getLoanInitPrin());
		loan.setUnstmtFee(loanReg.getLoanInitFee());
		loan.setActiveDate(loanReg.getValidDate());
		loan.setPaidOutDate(null);
		loan.setTerminalDate(null);
		loan.setTerminalReasonCd(null);
		loan.setPaidPrincipal(BigDecimal.ZERO);
		loan.setPaidInterest(BigDecimal.ZERO);
		loan.setPaidFee(BigDecimal.ZERO);
		loan.setLoanCurrBal(BigDecimal.ZERO);
		loan.setLoanBalXfrout(loanReg.getLoanInitPrin().add(loanReg.getLoanInitFee()));
		loan.setLoanBalXfrin(BigDecimal.ZERO);
		loan.setLoanPrinXfrout(loanReg.getLoanInitPrin());
		loan.setLoanPrinXfrin(BigDecimal.ZERO);
		loan.setLoanFeeXfrout(loanReg.getLoanInitFee());
		loan.setLoanFeeXfrin(BigDecimal.ZERO);
		loan.setOrigTxnAmt(loanReg.getOrigTxnAmt());
		loan.setOrigTransDate(loanReg.getOrigTransDate());
		loan.setOrigAuthCode(loanReg.getOrigAuthCode());
		loan.setRegisterId(loanReg.getRegisterId());
		loan.setRegisterDate(loanReg.getRegisterDate());
		loan.setRequestTime(loanReg.getRequestTime());
		loan.setExtendInitPrin(BigDecimal.ZERO);
		loan.setExtendDate(null);
		loan.setBefExtendFixedPmtPrin(BigDecimal.ZERO);
		loan.setBefExtendInitTerm(0);
		loan.setBefExtendFirstTermPrin(BigDecimal.ZERO);
		loan.setBefExtendFinalTermPrin(BigDecimal.ZERO);
		loan.setBefExtendInitFee(BigDecimal.ZERO);
		loan.setBefExtendFixedFee(BigDecimal.ZERO);
		loan.setBefExtendFirstTermFee(BigDecimal.ZERO);
		loan.setBefExtendFinalTermFee(BigDecimal.ZERO);
		loan.setExtendFirstTermFee(BigDecimal.ZERO);
		loan.setLoanFeeMethod(loanReg.getLoanFeeMethod());
		
		loan.setFloatRate(loanReg.getFloatRate());
		loan.setDueBillNo(loanReg.getDueBillNo());
		loan.setLoanExpireDate(null);
		//loan.setLoanCode("0");
		loan.setPaymentHst(null);
		loan.setCtdRepayAmt(BigDecimal.ZERO);
		loan.setPastExtendCnt(0);
		loan.setPastShortenCnt(0);
		loan.setAdvPmtAmt(BigDecimal.ZERO);
		loan.setLastActionType(null);
		loan.setLastActionDate(null);
		
		//新增
		loan.setContrNbr(loanReg.getContrNbr());
		loan.setGuarantyId(loanReg.getGuarantyId());
		
		//设置分期总金额
		loan.setInsuranceAmt(loanReg.getInsuranceAmt());
		loan.setStampdutyAmt(loanReg.getStampdutyAmt());
		loan.setTotLifeInsuAmt(loanReg.getTotLifeInsuAmt());
		loan.setTolSvcFee(loanReg.getLoanSvcFee());
		loan.setTotReplaceSvcFee(loanReg.getTotReplaceSvcFee());
		loan.setTotPrepayPkgAmt(loanReg.getTotPrepayPkgAmt());
		
		//设置分期未出账单金额
		loan.setUnstmtInsuranceAmt(loanReg.getInsuranceAmt());
		loan.setUnstmtStampdutyAmt(loanReg.getStampdutyAmt());
		loan.setUnstmtLifeInsuAmt(loanReg.getTotLifeInsuAmt());
		loan.setUnstmtSvcFee(loanReg.getLoanSvcFee());
		loan.setUnstmtReplaceSvcFee(loanReg.getTotReplaceSvcFee());
		loan.setUnstmtPrepayPkgAmt(loanReg.getTotPrepayPkgAmt());
		
		//设置分期已出账单金额
		loan.setLoanInsuranceAmt(BigDecimal.ZERO);
		loan.setLoanStampdutyAmt(BigDecimal.ZERO);
		loan.setPastLifeInsuAmt(BigDecimal.ZERO);
		loan.setPastSvcFee(BigDecimal.ZERO);
		loan.setPastReplaceSvcFee(BigDecimal.ZERO);
		loan.setPastPrepayPkgAmt(BigDecimal.ZERO);
		//设置方式
		loan.setLoanInsFeeMethod(loanReg.getLoanInsFeeMethod());
		loan.setStampdutyMethod(loanReg.getStampdutyMethod());
		loan.setLifeInsuFeeMethod(loanReg.getLifeInsuFeeMethod());
		loan.setSvcfeeMethod(loanReg.getSvcfeeMethod());
		loan.setReplaceSvcFeeMethod(loanReg.getReplaceSvcFeeMethod());
		loan.setPrepayPkgFeeMethod(loanReg.getPrepayPkgFeeMethod());
		
		// 设置已偿还金额
		loan.setPaidSvcFee(BigDecimal.ZERO);
		loan.setPaidReplaceSvcFee(BigDecimal.ZERO);
		loan.setPaidPrepayPkgAmt(BigDecimal.ZERO);
		
		loan.setAgreementRateInd(loanReg.getAgreementRateInd());
		loan.setJoinLifeInsuInd(loanReg.getJoinLifeInsuInd());
		//设置子产品编号
		loan.setLoanFeeDefId(loanReg.getLoanFeeDefId());
		
		//趸交费
		loan.setPremiumAmt(loanReg.getPremiumAmt());
		loan.setPremiumInd(loanReg.getPremiumInd());
		
		//代收
		loan.setReplacePenaltyRate(loanReg.getReplacePenaltyRate());
		
		// 代偿统计
		loan.setCompensateAmtSum(BigDecimal.ZERO);
		loan.setCompensateCount(0);
		loan.setCompensateRefundAmtSum(BigDecimal.ZERO);
		loan.setCompensateRefundCount(0);
		
		//5月新增
		loan.setPrepayPkgInd(loanReg.getPrepayPkgInd());
		return loan;
	}
	
	
	/**
	 * @see 方法名：generateRepayScheduleHst 
	 * @see 描述：创建贷款分配计划schedule历史
	 * @see 创建日期：2015-6-23下午7:38:41
	 * @author ChengChun
	 *  
	 * @param schedule
	 * @param registerId
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public CcsRepayScheduleHst generateRepayScheduleHst(CcsRepaySchedule schedule, Long registerId) {
		CcsRepayScheduleHst scheduleHst = new CcsRepayScheduleHst();
		
		scheduleHst.updateFromMap(schedule.convertToMap());
		scheduleHst.setRegisterId(registerId);
		
/*		scheduleHst.setRegisterId(registerId);
		scheduleHst.setOrg(schedule.getOrg());
		scheduleHst.setLoanId(schedule.getLoanId());
		scheduleHst.setAcctNbr(schedule.getAcctNbr());
		scheduleHst.setAcctType(schedule.getAcctType());
		scheduleHst.setLogicCardNbr(schedule.getLogicCardNbr());
		scheduleHst.setCardNbr(schedule.getCardNbr());
		scheduleHst.setLoanInitPrin(schedule.getLoanInitPrin());
		scheduleHst.setLoanInitTerm(schedule.getLoanInitTerm());
		scheduleHst.setCurrTerm(schedule.getCurrTerm());
		scheduleHst.setLoanTermPrin(schedule.getLoanTermPrin());
		scheduleHst.setLoanTermFee(schedule.getLoanTermFee());
		scheduleHst.setLoanTermInt(schedule.getLoanTermInt());
		scheduleHst.setLoanPmtDueDate(schedule.getLoanPmtDueDate());
		scheduleHst.setLoanGraceDate(schedule.getLoanGraceDate());
		scheduleHst.setLoanSvcFee(schedule.getLoanSvcFee());
		scheduleHst.setLoanReplaceSvcFee(schedule.getLoanSvcFee());*/
		return scheduleHst;
	}
	
	
	/**
	 * @see 方法名：generateLoanRegHst 
	 * @see 描述：生成分期注册信息历史
	 * @see 创建日期：2015-6-23下午7:40:33
	 * @author ChengChun
	 *  
	 * @param loanReg
	 * @param befInitTerm
	 * @param befInitPrin
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public CcsLoanRegHst generateLoanRegHst(CcsLoanReg loanReg, Integer befInitTerm, BigDecimal befInitPrin) {
		CcsLoanRegHst loanRegHst = new CcsLoanRegHst();
		loanRegHst.updateFromMap((loanReg.convertToMap()));
		if(loanReg.getLoanAction() == LoanAction.R){
			loanRegHst.setBefExtendInitTerm(befInitTerm);
			loanRegHst.setBefExtendInitPrin(befInitPrin);
			loanRegHst.setBefShortenInitTerm(0);
			loanRegHst.setBefShortenInitPrin(BigDecimal.ZERO);
		}else if(loanReg.getLoanAction() == LoanAction.S){
			loanRegHst.setBefExtendInitTerm(0);
			loanRegHst.setBefExtendInitPrin(BigDecimal.ZERO);
			loanRegHst.setBefShortenInitTerm(befInitTerm);
			loanRegHst.setBefShortenInitPrin(befInitPrin);
		}else{
			loanRegHst.setBefExtendInitTerm(0);
			loanRegHst.setBefExtendInitPrin(BigDecimal.ZERO);
			loanRegHst.setBefShortenInitTerm(0);
			loanRegHst.setBefShortenInitPrin(BigDecimal.ZERO);
		}
		
		return loanRegHst;
	}
	

	/**
	 * @see 方法名：makeLoanSuccessItem 
	 * @see 描述： 创建分期注册成功报表文件
	 * @see 创建日期：2015-6-23下午7:41:10
	 * @author ChengChun
	 *  
	 * @param loanReg
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public LoanSuccessRptItem makeLoanSuccessItem(CcsLoanReg loanReg) {
		LoanSuccessRptItem item = new LoanSuccessRptItem();
		
		item.org = loanReg.getOrg();
		item.registerId = loanReg.getRegisterId();
		item.acctNo = loanReg.getAcctNbr();
		item.acctType = loanReg.getAcctType();
		item.registerDate = loanReg.getRegisterDate();
		item.requestTime = loanReg.getRequestTime();
		item.logicalCardNo = loanReg.getLogicCardNbr();
		item.cardNo = loanReg.getCardNbr();
		item.refNbr = loanReg.getRefNbr();
		item.loanType = loanReg.getLoanType();
		item.loanRegStatus = loanReg.getLoanRegStatus();
		item.loanFeeMethod = loanReg.getLoanFeeMethod();
		item.loanFinalTermFee1 = loanReg.getLoanFinalTermFee();
		item.loanFinalTermPrin = loanReg.getLoanFinalTermPrin();
		item.loanFirstTermFee1 = loanReg.getLoanFirstTermFee();
		item.loanFirstTermPrin = loanReg.getLoanFirstTermPrin();
		item.loanFixedFee1 = loanReg.getLoanFixedFee();
		item.loanFixedPmtPrin = loanReg.getLoanFixedPmtPrin();
		item.loanInitFee1 = loanReg.getLoanInitFee();
		item.loanInitPrin = loanReg.getLoanInitPrin();
		item.loanInitTerm = loanReg.getLoanInitTerm();
		item.origAuthCode = loanReg.getOrigAuthCode();
		item.origTransDate = loanReg.getOrigTransDate();
		item.origTxnAmt = loanReg.getOrigTxnAmt();
		item.loanCode = loanReg.getLoanCode();
		item.b007 = loanReg.getB007TxnTime();
		item.b011 = loanReg.getB011Trace();
		item.b032 = loanReg.getB032AcqInst();
		item.b033 = loanReg.getB033FwdIns();
		item.loanAction = loanReg.getLoanAction();
		item.loanserviceReturnFee = loanReg.getLoanSvcFeeReturn();
		item.loanserviceFee = loanReg.getLoanSvcFee();
		item.matched = loanReg.getMatched();
		item.remark = loanReg.getRemark();

		return item;
	}
	
	
	/**
	 * @see 方法名：makeRescheduleMsgItem 
	 * @see 描述：创建展期缩期批量短信
	 * @see 创建日期：2015-6-23下午7:41:45
	 * @author ChengChun
	 *  
	 * @param loanReg
	 * @param term
	 * @param prin
	 * @param fee
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
/*	public LoanRescheduleMsgItem makeRescheduleMsgItem(CcsLoanReg loanReg, Integer term, BigDecimal prin, BigDecimal fee) {
		LoanRescheduleMsgItem msg = new LoanRescheduleMsgItem();
		CcsCustomer cust = queryFacility.getCustomerByCardNbr(loanReg.getCardNbr());
		CcsCard card = queryFacility.getCardByCardNbr(loanReg.getCardNbr());
		CcsAcct acct = queryFacility.getAcctByAcctNbr(loanReg.getAcctType(), loanReg.getAcctNbr());
		
		msg.org = loanReg.getOrg();
		msg.custName = cust.getName();
		msg.gender = cust.getGender();
		msg.cardNo = batchUtils.getCardNoBySendMsgCardType(acct, card);
		msg.mobileNo = cust.getMobileNo();
		msg.msgCd = fetchMsgCdService.fetchMsgCd(card.getProductCd(), CPSMessageCategory.CPS052);
		msg.loanReceiptNbr = loanReg.getDueBillNo();
		msg.loanAction = loanReg.getLoanAction();
		msg.loanActionDate = batchFacility.getBatchDate();
		msg.term = term;
		msg.prin = prin;
		msg.fee = fee;

		return msg;
	}*/
}
