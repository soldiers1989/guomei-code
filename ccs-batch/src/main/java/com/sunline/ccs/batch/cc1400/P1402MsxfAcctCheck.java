package com.sunline.ccs.batch.cc1400;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.LineItem;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctOKey;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsSettleClaim;
import com.sunline.ccs.infrastructure.shared.model.CcsSettleLoanHst;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnPost;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsSettleClaim;
import com.sunline.ccs.infrastructure.shared.model.QCcsSettleLoanHst;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.TransAmtDirection;
import com.sunline.ccs.service.api.Constants;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.DecisionCode;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.ManualAuthFlag;
import com.sunline.ppy.dictionary.enums.MatchErrorReason;
import com.sunline.ppy.dictionary.enums.MsPayfrontError;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ppy.dictionary.exchange.MsxfTranFlow;
import com.sunline.ppy.dictionary.report.ccs.MsxfMatchErrRpt;
import com.sunline.ppy.dictionary.report.ccs.MsxfMatchSuccRpt;

public class P1402MsxfAcctCheck implements ItemProcessor<LineItem<MsxfTranFlow>, S1401RptInfo > {
	
	private static final Logger logger = LoggerFactory.getLogger(P1402MsxfAcctCheck.class);
	
	@PersistenceContext
    private EntityManager em;
	
	private QCcsOrder qOrder = QCcsOrder.ccsOrder;
	
	@Autowired
	private BatchStatusFacility batchFacility;
	@Autowired
	private MicroCreditRescheduleUtils microCreditRescheduleUtils;
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	
	@Override
	public S1401RptInfo process(LineItem<MsxfTranFlow> lineItem) throws Exception {
		S1401RptInfo info = new S1401RptInfo();
		MsxfTranFlow item = lineItem.getLineObject();
		
		if (logger.isDebugEnabled()) {
			logger.debug("开始对账,channelSerial:[" + item.channelSerial
					+ "]");
		}
		
		CcsOrder order = new JPAQuery(em).from(qOrder)
			.where(qOrder.orderId.eq(Long.parseLong(item.channelSerial))
					.and(qOrder.cardNo.eq(item.cardNo))
					.and(qOrder.txnAmt.eq(item.txnAmt==null?BigDecimal.ZERO.setScale(2):item.txnAmt))
					.and(qOrder.matchInd.eq(Indicator.Y))
					//渠道：马上支付
					//兼容数据
					.and(qOrder.contactChnl.isNull().or(qOrder.contactChnl.ne(Constants.MERCHANT_ACQ_ID)))
					.and(qOrder.comparedInd.isNull().or(qOrder.comparedInd.eq(Indicator.N))))
			.singleResult(qOrder);
		
		MsxfMatchErrRpt errRpt = null;
		MsxfMatchSuccRpt sucRpt = null;
		//匹配到订单
		if(order != null){
			logger.info("匹配到订单,orderId[{}]", order.getOrderId());
			
			OrganizationContextHolder.setCurrentOrg(order.getOrg());
			
			order.setComparedInd(Indicator.Y);
			//返回码状态相同，对账正常
			if(item.msDdReturnCode.equals(order.getCode())){
				logger.debug("对账成功");
				order.setErrInd(Indicator.N);
				sucRpt = getSuccRpt(order);
				info.setMsxfTransMatchSucRpt(sucRpt);
			}else{//返回码不同，对账结果不匹配
				
				// 处理中订单自动恢复
				if(OrderStatus.W == order.getOrderStatus()||OrderStatus.C == order.getOrderStatus()){
					errRpt = getErrRpt(item, order);
					errRpt.matchErrorReason = MatchErrorReason.R04;
					info.setMsxfTransMatchErrRpt(errRpt);
					autoRecovery(order, item);
					return info;
				}
				
				logger.debug("对账结果R3,返回码异常");
				order.setErrInd(Indicator.Y);
				errRpt = getErrRpt(item, order);
				errRpt.matchErrorReason = MatchErrorReason.R03;
				info.setMsxfTransMatchErrRpt(errRpt);
			}
			em.merge(order);
		}else{//未匹配到订单
			logger.info("未匹配到订单");
			errRpt = getErrRpt(item, order);
			errRpt.matchErrorReason = MatchErrorReason.R01;
			info.setMsxfTransMatchErrRpt(errRpt);
		}
		
		return info;
	}
	
	/**
	 * 处理中订单对账后自动恢复
	 * @param order
	 * @param item
	 */
	private void autoRecovery(CcsOrder order, MsxfTranFlow item) {
		logger.info("对账普通异常,自动恢复");
		
		// 根据返回码获取订单状态
		MsPayfrontError msPayfrontError = this.getErrorEnum(item.msDdReturnCode, MsPayfrontError.class);
		
		if(MsPayfrontError.S_0 == msPayfrontError){
			// 交易成功
			switch(order.getLoanUsage()){
			case S:
				subrogationProc(order, item);
				break;
			case M:
				// loanReg设置预约成功 也需要payCutPaymentProc
				prepaymentProc(order);
			case N:
			case O:
				payCutPaymentProc(order, item);
				break;
			case B:
				// 更新结算历史表
				updateSettleLoanHst(order, Indicator.Y);
				// 更新订单
				updateOrder(order, item, OrderStatus.S, order.getLogKv());
				break;
			case D:
				//溢缴款转出，不更新账户/loanReg信息
				depositProc(order, item);
				break;
			case A:
			case L:
				paySinPaymentProc(order, item);
				break;
			default: 
				// 其余的无对账流水
			}
		}else{
			// 因为处理中的状态算对账成功,不会进入到自动恢复逻辑
			//
			if(StringUtils.equals(msPayfrontError.getDecision(),DecisionCode.P.name())){
//				setErrorInfo(context,AuthTransStatus.P,OrderStatus.W,LoanRegStatus.C);
				updateOrder(order, item, OrderStatus.W, null);
			}else{
				// 交易失败
				if(LoanUsage.A==order.getLoanUsage() || LoanUsage.L==order.getLoanUsage()){
					// 代付需要额外设置
					setErrorInfo(order, item);
				}else if(LoanUsage.B == order.getLoanUsage()){
					// 更新结算历史表
					updateSettleLoanHst(order, Indicator.N);
				}
				// 更新订单
				updateOrder(order, item, OrderStatus.E, null);
			}
		}
	}
	
	/**
	 * 更新结算历史表结算状态 根据结算原订单信息
	 * @param order
	 * @param settleInd  结算成功 结算失败
	 */
	public void updateSettleLoanHst(CcsOrder order, Indicator settleInd){
		if(logger.isDebugEnabled())
			logger.debug("更新结算历史表结算状态");
		
		QCcsSettleLoanHst q = QCcsSettleLoanHst.ccsSettleLoanHst;
		CcsSettleLoanHst ccsSettleLoanHst = new JPAQuery(em).from(q).where(q.orderId.eq(order.getOrderId())).singleResult(q);
		if(null != ccsSettleLoanHst){
			// 若没取到 则表明为已重提的订单
			ccsSettleLoanHst.setIsSettle(settleInd);
		}
	}
	
	/**
	 * 代付失败
	 * @param order
	 * @param item
	 */
	private void setErrorInfo(CcsOrder order, MsxfTranFlow item) {
		// 更新loanReg
		CcsLoanReg loanReg = getSinLoanReg(order);
		loanReg.setValidDate(batchFacility.getBatchDate());// 生效日期为放款成功日期
		loanReg.setLoanRegStatus(LoanRegStatus.F);
		// 对未匹配金额进行反向处理
		CcsAcctO accto = em.find(CcsAcctO.class, new CcsAcctOKey(order.getAcctNbr(), order.getAcctType()));
		updateAcctoMemoAmt(accto, item.txnAmt, order.getTxnType(),false);
		// 更新accto
		updateAcctO(accto);
		// 更新authmemo
		CcsAuthmemoO um = em.find(CcsAuthmemoO.class, order.getLogKv());
		updateAuthMemoO(um, item, accto, AuthTransStatus.E);
	}

	private void paySinPaymentProc(CcsOrder order, MsxfTranFlow item) {
		if(logger.isDebugEnabled()){
			logger.debug("处理中代付对账成功");
		}
		
		CcsAcctO accto = em.find(CcsAcctO.class, new CcsAcctOKey(order.getAcctNbr(), order.getAcctType()));
		
		// 获取authmemo
		CcsAuthmemoO um = em.find(CcsAuthmemoO.class, order.getLogKv());
		// 获取loanReg
		CcsLoanReg loanReg = getSinLoanReg(order);
		// 更新订单状态
		updateOrder(order, item, OrderStatus.S, order.getLogKv());
		// 更新账户, 随借随还除外
		if(loanReg.getLoanType() != LoanType.MCAT)
			updateAcct(order, loanReg, accto);
		// 更新accto
		updateAcctO(accto);
		// 更新loanReg
		loanReg.setValidDate(batchFacility.getBatchDate());// 生效日期为放款成功日期
		loanReg.setLoanRegStatus(LoanRegStatus.S);
		loanReg.setRefNbr(order.getRefNbr());
		// 更新authmemo
		updateAuthMemoO(um, item, accto, AuthTransStatus.N);
		// 保存txnPost
		savePosting(order, item, um,accto);
	}

	//溢缴款转出处理
	private void depositProc(CcsOrder order, MsxfTranFlow item) {
		if(logger.isDebugEnabled()){
			logger.debug("处理中溢缴款转出对账成功");
		}
		CcsAcctO accto = em.find(CcsAcctO.class, new CcsAcctOKey(order.getAcctNbr(), order.getAcctType()));
		// 获取authmemo
		CcsAuthmemoO um = em.find(CcsAuthmemoO.class, order.getLogKv());
		// 更新订单状态
		updateOrder(order, item, OrderStatus.S, order.getLogKv());
		// 更新accto
		updateAcctO(accto);
		// 更新authmemo
		updateAuthMemoO(um, item, accto, AuthTransStatus.N);
		// 保存txnPost
		savePosting(order, item, um,accto);
	}
	
	private CcsLoanReg getSinLoanReg(CcsOrder order) {
		QCcsLoanReg q = QCcsLoanReg.ccsLoanReg;
		//阳光保险 用保单号查 马上贷用借据号查
		BooleanExpression expression = q.loanRegStatus.eq(LoanRegStatus.C).and(q.refNbr.eq(order.getRefNbr()));
		expression = expression.and(q.contrNbr.eq(order.getContrNbr()));
		expression = expression.and(q.dueBillNo.eq(order.getDueBillNo()));
		if(order.getGuarantyId() != null)
			expression = expression.and(q.guarantyId.eq(order.getGuarantyId()));
		CcsLoanReg loanReg = new JPAQuery(em).from(q).where(expression).singleResult(q);
		return loanReg;
	}

	/**
	 * 更新代付authmemo
	 * @param um
	 * @param item
	 * @param accto
	 * @param authTransStatus
	 */
	private void updateAuthMemoO(CcsAuthmemoO um, MsxfTranFlow item, CcsAcctO accto, AuthTransStatus authTransStatus) {
		MsPayfrontError msPayfrontError = this.getErrorEnum(item.msDdReturnCode, MsPayfrontError.class);
		um.setAuthTxnStatus(authTransStatus);
//		um.setRejReason(item.returnMessage);
		um.setB039RtnCode(msPayfrontError.getRespCode());
		
		if(authTransStatus == AuthTransStatus.N){
			//生成授权号
			um.setAuthCode(genAuthCode(um.getLogKv()));
			um.setFinalAction(AuthAction.A);//通过
			um.setFinalUpdDirection(TransAmtDirection.D.name());//借记
			um.setFinalUpdAmt(item.txnAmt);
		}else if(authTransStatus == AuthTransStatus.E){
			um.setFinalAction(AuthAction.D);//拒绝
		}
		
		um.setMemoCr(accto.getMemoCr());
		um.setMemoDb(accto.getMemoDb());
	}

	private void updateAcct(CcsOrder order, CcsLoanReg loanReg, CcsAcctO accto) {
		
		CcsAcct acct = em.find(CcsAcct.class, new CcsAcctKey(order.getAcctNbr(), order.getAcctType()));
		logger.debug("分期计划参数[{}],[{}],[{}]", loanReg.getLoanCode(), loanReg.getLoanInitTerm(), loanReg.getLoanInitPrin());
		ProductCredit productCredit = unifiedParameterFacility.loadParameter(acct.getProductCd(), ProductCredit.class);
		LoanPlan loanPlan = unifiedParameterFacility.loadParameter(loanReg.getLoanCode(), LoanPlan.class);
		LoanFeeDef loanFeeDef = microCreditRescheduleUtils.getLoanFeeDef(
				loanReg.getLoanCode(), loanReg.getLoanInitTerm(), loanReg.getLoanInitPrin(),loanReg.getLoanFeeDefId());
		AccountAttribute acctAttr = unifiedParameterFacility.loadParameter(
	    		productCredit.accountAttributeId, AccountAttribute.class);
		
		//首笔放款成功后，更新账单日，下一账单日期
		if(null == acct.getLtdLoanAmt() || acct.getLtdLoanAmt().compareTo(BigDecimal.ZERO)==0){
			acct = this.setNextStmtDate(acct, batchFacility.getBatchDate(), loanFeeDef, acctAttr, loanPlan);
			
		    Date stmtDate = acct.getNextStmtDate();
		    acct.setFirstStmtDate(stmtDate);// 建账时为首个账单日期
		    logger.debug("productCd[{}]",acct.getProductCd());
		    acct.setPmtDueDate(microCreditRescheduleUtils.getNextPaymentDay(acct.getProductCd(),  acct.getNextStmtDate()));// 计算下个还款日期
		    acct.setGraceDate(microCreditRescheduleUtils.getNextGraceDay(acct.getProductCd(),acct.getNextStmtDate()));// 计算下个还款日期
		    
		    if(null != accto){
		    	accto.setCycleDay(String.format("%02d", Integer.valueOf(acct.getCycleDay())));
		    }
		}
		
		if(logger.isDebugEnabled()){
			logger.debug("首个账单日期:[{}]",acct.getFirstStmtDate());
			logger.debug("下一账单日期:[{}]",acct.getNextStmtDate());
			logger.debug("到期还款日期:[{}]",acct.getPmtDueDate());
			logger.debug("宽限日期:[{}]",acct.getGraceDate());
		}
		
	}

	/**
	 * 设置账单日，下一账单日期
	 * 是否跳过月末
	 * @param acct
	 * @param txnInfo
	 * @param loanFeeDef
	 * @param acctAttr 
	 * @param loanPlan 
	 */
	public CcsAcct setNextStmtDate(CcsAcct acct,Date bizDate,
			LoanFeeDef loanFeeDef, AccountAttribute acctAttr, LoanPlan loanPlan) {
		//跳过月末
		Date fixDate = bizDate;
		
		//随借随还不处理账单日
		if(!loanPlan.loanType.equals(LoanType.MCAT)){
			fixDate = microCreditRescheduleUtils.skipEndOfMonth(acctAttr, bizDate);
		}
		
		Calendar fixCal = Calendar.getInstance();
		fixCal.setTime(fixDate);
		int cycleDay = fixCal.get(Calendar.DATE);// 账单日
		
		acct.setCycleDay(String.format("%02d", cycleDay));
		acct.setNextStmtDate(microCreditRescheduleUtils.getLoanPmtDueDate(
				fixDate, loanFeeDef, 2));// 计算下个账单日期
		
		return acct;
	}
	
	private void prepaymentProc(CcsOrder order) {
		if(logger.isDebugEnabled()){
			logger.debug("提前结清成功");
		}
		QCcsLoanReg q = QCcsLoanReg.ccsLoanReg;
		//阳光保险 用保单号查 马上贷用借据号查  
		BooleanExpression expression = q.loanRegStatus.eq(LoanRegStatus.A).and(q.loanAction.eq(LoanAction.O));
		expression = expression.and(q.contrNbr.eq(order.getContrNbr()));
		expression = expression.and(q.dueBillNo.eq(order.getDueBillNo()));
		if(StringUtils.isNotBlank(order.getGuarantyId()))
			expression = expression.and(q.guarantyId.eq(order.getGuarantyId()));
		
		CcsLoanReg loanReg = new JPAQuery(em).from(q).where(expression).singleResult(q);
		
		loanReg.setDdRspFlag(Indicator.Y);// 预约注册-扣款成功
		loanReg.setValidDate(batchFacility.getBatchDate());
	}

	private void payCutPaymentProc(CcsOrder order, MsxfTranFlow item) {
		if(logger.isDebugEnabled()){
			logger.debug("处理中代扣对账成功,更新相关数据");
		}
		
		CcsAcctO accto = em.find(CcsAcctO.class, new CcsAcctOKey(order.getAcctNbr(), order.getAcctType()));
		// 更新accto
		updateAcctO(accto);
		// 保存AuthMemo
		CcsAuthmemoO um = saveCcsAuthmemoO(order, item);
		// 生成授权码
		um.setAuthCode(genAuthCode(um.getLogKv()));
		// 更新订单
		updateOrder(order, item, OrderStatus.S, um.getLogKv());
		// 更新授权未匹配金额
		updateAcctoMemoAmt(accto, item.txnAmt, order.getTxnType(), true);
		// 入账流水
		savePosting(order, item, um,accto);
	}
	
	/**
	 * 生成授权码
	 * @param logKv
	 * @return
	 */
	private String genAuthCode(Long logKv) {
		String txnSeq = logKv.toString();
		String authCode = txnSeq.length() < 6 ? String.format("%06d", Integer.valueOf(txnSeq)) : StringUtils.right(txnSeq, 6);
		return authCode;
	}

	private void updateAcctO(CcsAcctO accto) {
		// 更新联机账户信息
		if(null==accto.getLastUpdateBizDate() || accto.getLastUpdateBizDate().before(batchFacility.getBatchDate()))
			accto.setLastUpdateBizDate(batchFacility.getBatchDate());
	}

	private void subrogationProc(CcsOrder order, MsxfTranFlow item) {
		if(logger.isDebugEnabled()){
			logger.debug("处理中代位追偿对账成功,累加追偿金额");
		}
		// 代位追偿只更新settleClaim ,不记录authmemo
		CcsSettleClaim claim = matchSettleClaim(order);
		claim.setCompensatoryAmt(claim.getCompensatoryAmt()==null? item.txnAmt:claim.getCompensatoryAmt().add(item.txnAmt));
		claim.setLastCompensatoryDate(batchFacility.getBatchDate());
		// 更新订单
		updateOrder(order, item, OrderStatus.S, null);
		// 更新授权未匹配金额
//		CcsAcctO accto = em.find(CcsAcctO.class, new CcsAcctOKey(order.getAcctNbr(), order.getAcctType()));
//		updateAcctoMemoAmt(accto, item.txnAmt, order.getTxnType(), true);
		// 入账流水
//		savePosting(order, item, null);
	}

	private CcsTxnPost savePosting(CcsOrder order, MsxfTranFlow item, CcsAuthmemoO um, CcsAcctO accto){
		if(logger.isDebugEnabled()){
			logger.debug("保存TxnPost");
		}
		MsPayfrontError msPayfrontError = this.getErrorEnum(item.msDdReturnCode, MsPayfrontError.class);
		CcsAcct acct = em.find(CcsAcct.class, new CcsAcctKey(order.getAcctNbr(), order.getAcctType()));
		CcsTxnPost txnPost = new CcsTxnPost();
		
		if(null != um){
			txnPost.setMti(um.getMti());
			txnPost.setAuthCode(um.getAuthCode());
			txnPost.setB003ProcCode(um.getB003ProcCode());
		}
		
		txnPost.setOrg(OrganizationContextHolder.getCurrentOrg());
		if(InputSource.SUNS == order.getChannelId()){
			txnPost.setTxnSource("YG");
			txnPost.setAcqAddress("阳光");		
			txnPost.setB033FwdIns("阳光");
		}else{
			txnPost.setTxnSource("MS");
			txnPost.setAcqAddress("马上");		
			txnPost.setB033FwdIns("马上");			
		}
		txnPost.setSrcChnl(order.getChannelId());
		//查询交易 重新赋值交易类型，其他用order中的交易类型
		if(order.getLoanUsage() == LoanUsage.E
		 ||order.getLoanUsage() == LoanUsage.G){
			txnPost.setTxnType(AuthTransType.AgentDebit);			
		}else if(order.getLoanUsage() == LoanUsage.F){
			txnPost.setTxnType(AuthTransType.AgentCredit);			
		}else {
			txnPost.setTxnType(order.getTxnType());			
		}
		txnPost.setTxnDirection(AuthTransDirection.Normal);
		txnPost.setCardNbr(acct.getDefaultLogicCardNbr());
		// 收取趸交费放款的交易，使用贷款本金入账
		// 否则使用交易金额入账
		txnPost.setTxnAmt(Indicator.Y==order.getPremiumInd()?order.getLoanAmt():order.getTxnAmt());
		txnPost.setTxnCurrenctCode(order.getCurrency());
		txnPost.setSettAmt(Indicator.Y==order.getPremiumInd()?order.getLoanAmt():order.getTxnAmt());
		txnPost.setSettCurrencyCode(order.getCurrency());
		txnPost.setTxnTime(order.getRequestTime().substring(4));
		txnPost.setRefNbr(order.getRefNbr());
		txnPost.setAcqTermId("");
		txnPost.setAcqAcceptorId(order.getAcqId());
		txnPost.setMerchCategoryCode("");
		// 应付手续费
		txnPost.setTxnFeeAmt(BigDecimal.ZERO);
		// 应收手续费
		txnPost.setFeeProfit(BigDecimal.ZERO);
//		txnPost.setB011Trace(order.getServicesn().substring(order.getServicesn().length()-6));//外部交易流水后6位随机数
		txnPost.setB011Trace(order.getRefNbr().substring(order.getRefNbr().length()-9,order.getRefNbr().length()-3));//内部交易流水后6位随机数
		txnPost.setB022Entrymode("");
		txnPost.setB025Entrycond("");
		txnPost.setB032AcqInst(order.getAcqId());
		txnPost.setB039RtnCode(msPayfrontError.getRespCode());
		txnPost.setB060("");
		txnPost.setB061("");
		txnPost.setBatchDate(batchFacility.getBatchDate());
		txnPost.setComparedInd(Indicator.N);
		txnPost.setIsRevoke(Indicator.N);
		txnPost.setExpInd(Indicator.N);
		txnPost.setErrInd(Indicator.N);
		//txnPost.setAuthTxnTerminal(order.getAuthTransTerminal());
		/** 第三方交易终端-改造增加40域*/
		txnPost.setB040TermId("");
		
		em.persist(txnPost);
		
		return txnPost;
	}
	
	/**
	 * 更新账户未入账金额
	 * @param accto
	 * @param txnAmt
	 * @param authTransType
	 * @param amtFlag compFlag 金额计算标识  true 加 false 减
	 */
	private void updateAcctoMemoAmt(CcsAcctO accto, BigDecimal txnAmt, AuthTransType authTransType, boolean amtFlag) {
		//代付
		if(AuthTransType.AgentDebit  == authTransType){
			if(amtFlag){
				accto.setMemoDb(accto.getMemoDb()!=null?accto.getMemoDb().add(txnAmt):txnAmt);
			}else{
				//金额为空或负数时，修正为0
				accto.setMemoDb(accto.getMemoDb()!=null && accto.getMemoDb().compareTo(txnAmt)>=0?
						accto.getMemoDb().subtract(txnAmt):BigDecimal.ZERO);
			}
		}else if(AuthTransType.AgentCredit == authTransType){
			//代收
			if(amtFlag){
				accto.setMemoCr(accto.getMemoCr()!=null?accto.getMemoCr().add(txnAmt):txnAmt);
			}else{
				//金额为空时，修正为0
				accto.setMemoCr(accto.getMemoCr()!=null && accto.getMemoCr().compareTo(txnAmt)>=0?
						accto.getMemoCr().subtract(txnAmt):BigDecimal.ZERO);
			}
		}
	}
	
	private void updateOrder(CcsOrder order, MsxfTranFlow item, OrderStatus orderStatus, Long logKv) {
		if(logger.isDebugEnabled()){
			logger.debug("更新订单状态");
		}
		order.setCode(item.msDdReturnCode);
		order.setMessage(item.returnMessage);
		order.setStatus(item.status);
		order.setOrderStatus(orderStatus);
		if(null != logKv && logKv.intValue()>0){
			order.setLogKv(logKv);
		}
		order.setOptDatetime(batchFacility.getBatchDate());
		order.setMemo(StringUtils.isBlank(order.getMemo())?"对账更新":order.getMemo()+"-对账更新");
		
		if(OrderStatus.S == orderStatus){
			order.setSuccessAmt(order.getTxnAmt());
		}else if(OrderStatus.E == orderStatus){
			order.setFailureAmt(order.getTxnAmt());
		}
	}

	private CcsAuthmemoO saveCcsAuthmemoO(CcsOrder order, MsxfTranFlow item) {
		if(logger.isDebugEnabled()){
			logger.debug("保存AuthMemo");
		}
		
		CcsAuthmemoO um = new CcsAuthmemoO();
		
		CcsAcctO accto = em.find(CcsAcctO.class, new CcsAcctOKey(order.getAcctNbr(), order.getAcctType()));
		CcsAcct acct = em.find(CcsAcct.class, new CcsAcctKey(order.getAcctNbr(), order.getAcctType()));
		MsPayfrontError msPayfrontError = this.getErrorEnum(item.msDdReturnCode, MsPayfrontError.class);
		
		um.setOrg(OrganizationContextHolder.getCurrentOrg());
		um.setAcctNbr(accto.getAcctNbr());
		um.setAcctType(accto.getAcctType());
		um.setCurrBal(accto.getCurrBal());
		um.setMemoCr(accto.getMemoCr());
		um.setMemoDb(accto.getMemoDb());
		um.setProductCd(accto.getProductCd());
		um.setAcctBlockcode(accto.getBlockCode());
		
		um.setB002CardNbr(acct.getDefaultLogicCardNbr());
		um.setProductCd(acct.getProductCd());
		
		um.setLogOlTime(new Date());
		um.setB039RtnCode(msPayfrontError.getRespCode());
	
		um.setManualAuthFlag(ManualAuthFlag.N);
		um.setChbTxnAmt(order.getTxnAmt());
		um.setChbCurrency(order.getCurrency());
		um.setTxnAmt(order.getTxnAmt());
		um.setTxnCurrency(order.getCurrency());
		um.setTxnType(order.getTxnType());
		
		um.setCustOtb(BigDecimal.ZERO);
		um.setCashOtb(BigDecimal.ZERO);
		um.setOtb(BigDecimal.ZERO);
		um.setLogBizDate(batchFacility.getBatchDate());
		um.setTxnDirection(AuthTransDirection.Normal);
		um.setRejReason("");
		um.setFinalAuthAmt(BigDecimal.ZERO);
		um.setFinalUpdDirection(TransAmtDirection.C.name());
		um.setFinalAction(AuthAction.A);//通过
		um.setFinalUpdAmt(item.txnAmt);
		um.setCashAmt(order.getTxnAmt());
		
		um.setAuthTxnStatus(AuthTransStatus.N);
		um.setSubTerminalType(order.getSubTerminalType());
		
		um.setB004Amt(this.getB004(order.getTxnAmt()));
		
		um.setB007TxnTime(order.getRequestTime().substring(4));//MMddHHmmss
//		um.setB011Trace(order.getServicesn().substring(order.getServicesn().length()-6));//外部交易流水后6位随机数
		um.setB011Trace(order.getRefNbr().substring(order.getRefNbr().length()-9,order.getRefNbr().length()-3));//内部交易流水后6位随机数
		um.setB037RefNbr(order.getRefNbr());//交易流水号
		um.setB032AcqInst(order.getAcqId());
		um.setB033FwdIns("");
		um.setGuarantyId(order.getGuarantyId());
		um.setRequestTime(order.getRequestTime());
		
		um.setMti("9900");
		um.setB003ProcCode("900001");
		um.setInputSource(order.getChannelId());
		um.setLogicCardNbr(acct.getDefaultLogicCardNbr());
		
		um.setVipStatus(null);
		
		em.persist(um);
		
		return um;
	}
	
	/**
	 * 设置b004
	 * @param txnAmt
	 * @return
	 */
	public BigDecimal getB004(BigDecimal txnAmt) {
		BigDecimal b004 = new BigDecimal(0);
		try{
			DecimalFormat df = new DecimalFormat("########.00");
	        String dff = df.format(txnAmt);
	        dff = dff.replace(".", "");
	        b004 = new BigDecimal(dff);
		}catch (Exception e){
			//不处理
			b004 = txnAmt;
		}
        return b004;
	}
	
	private CcsSettleClaim matchSettleClaim(CcsOrder order) {
		JPAQuery query = new JPAQuery(em);
		QCcsSettleClaim qCcsSettleClaim = QCcsSettleClaim.ccsSettleClaim;
		CcsSettleClaim settleClaim = query.from(qCcsSettleClaim)
				.where(qCcsSettleClaim.acctNbr.eq(order.getAcctNbr())
						.and(qCcsSettleClaim.guarantyId.eq(order.getGuarantyId()))
						.and(qCcsSettleClaim.dueBillNo.eq(order.getDueBillNo())))
				.singleResult(qCcsSettleClaim);
		return settleClaim;
	}
	
	/**
	 * 获取异常枚举
	 * @param value
	 * @param clazz
	 * @return
	 */
	public <T extends Enum<T>> T getErrorEnum(String value, Class<T> clazz) {
		try{
			String tip = "S_";
			if(!StringUtils.equals("0", value)){
				tip = "E_";
			}
			
			return Enum.valueOf(clazz, tip + value);
		}catch(Exception e){
			if (logger.isErrorEnabled())
				logger.error("无效响应码[{"+value+"}]", e);

			throw new ProcessException(MsPayfrontError.E_90000.getCode(),MsPayfrontError.E_90000.getDesc());
		}
	}
	
	private MsxfMatchErrRpt getErrRpt(MsxfTranFlow item, CcsOrder order) {
		
		MsxfMatchErrRpt errRpt = new MsxfMatchErrRpt();
		errRpt.channelSerial = item.channelSerial;
		errRpt.channelDate = item.channelDate;
		errRpt.bankId = item.bankId;
		errRpt.cardType = item.cardType;
		errRpt.cardNo = item.cardNo;
		errRpt.usrName = item.usrName;
		errRpt.idType = item.idType;
		errRpt.idNo = item.idNo;
		errRpt.prov = item.prov;
		errRpt.city = item.city;
		errRpt.txnAmt = item.txnAmt;
		errRpt.purpose = item.purpose;
		errRpt.status = item.status;
		if(order != null){
			errRpt.orderCode = order.getCode();
		}
		errRpt.msDdReturnCode = item.msDdReturnCode;
		errRpt.returnMessage = item.returnMessage;

		return errRpt;
	}
	
	private MsxfMatchSuccRpt getSuccRpt(CcsOrder order){
		MsxfMatchSuccRpt rpt = new MsxfMatchSuccRpt();
		rpt.channelSerial = order.getOrderId().toString();
		rpt.channelDate = order.getBusinessDate();
		rpt.bankId =  order.getOpenBankId();
		rpt.cardType = order.getCardType();
		rpt.cardNo = order.getCardNo();
		rpt.usrName = order.getUsrName();
		rpt.idType = order.getCertType();
		rpt.idNo = order.getCertId();
		rpt.prov = order.getState();
		rpt.city = order.getCity();
		rpt.txnAmt = order.getTxnAmt();
		rpt.purpose = order.getPurpose();
		rpt.status = order.getStatus();
		rpt.msDdReturnCode = order.getCode();
		rpt.returnMessage = order.getMessage();
		return rpt;
	}

}
