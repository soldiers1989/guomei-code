package com.sunline.ccs.batch.cc6000.transfer;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.cc6000.S6000AcctInfo;
import com.sunline.ccs.batch.cc6000.common.Calculator;
import com.sunline.ccs.batch.cc6000.common.PlanManager;
import com.sunline.ccs.batch.cc6000.common.TransactionGenerator;
import com.sunline.ccs.batch.cc6000.common.TransactionPost;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnSeq;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.enums.LoanTerminateReason;
import com.sunline.ccs.param.def.enums.PaymentCalcMethod;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.pcm.param.def.FinancialOrg;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ppy.dictionary.enums.ExceptionType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.PostGlIndicator;
import com.sunline.ppy.dictionary.enums.PostTxnType;
import com.sunline.ppy.dictionary.enums.PostingFlag;
import com.sunline.ppy.dictionary.exception.ProcessException;


/**
 * @see 类名：TransferPlanP
 * @see 描述： 等额本息的转移
                                     每个账单日按计划表转移本金、利息
 *
 * @see 创建日期：   2015-6-24下午6:28:51
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class TransferPlanP extends Transfer {
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private TransactionGenerator generatorTransaction;
	@Autowired
	private PlanManager createPlan;
	@Autowired
	private Calculator calculator;
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	@Autowired
	private BatchStatusFacility batchFacility;
	@PersistenceContext
	protected EntityManager em;
	@Autowired
	private PlanManager planManager;
	@Autowired
	private TransactionPost transactionPost;
	@Autowired
	private CustAcctCardFacility queryFacility;
	
	
	@Override
	public CcsPlan getXfrInPlan(S6000AcctInfo item, PlanTemplate xfrOut,
			PlanTemplate xfrIn, CcsPlan xfrOutPlan, CcsLoan loan) {
		List<CcsPlan> findPlans = createPlan.findTxnPlans(xfrOutPlan.getLogicCardNbr(), xfrIn.planNbr, xfrIn.planType, xfrOutPlan.getRefNbr(), item.getPlans(),loan.getCurrTerm()+1);
		CcsPlan xfrInPlan = null;
		// 信用计划不存在
		if (findPlans.size() < 1) {
			// 建立信用计划
			xfrInPlan = createPlan.generateXfrInPlan(xfrOutPlan, loan, xfrIn, batchFacility.getBatchDate(),loan.getCurrTerm()+1);
			// 增加新建信用计划
			item.getPlans().add(xfrInPlan);
		}
		// 找到多个转入信用计划
		else if (findPlans.size() > 1) {
			// 增加异常账户报表记录
			calculator.makeExceptionAccount(item, xfrIn.planNbr, xfrOutPlan.getRefNbr(), ExceptionType.E10);
			return null;
		} else{ 
			xfrInPlan = findPlans.get(0);
		}
		return xfrInPlan;
	}

	@Override
	public void xfr(S6000AcctInfo item, CcsPlan xfrOutPlan, CcsPlan xfrInPlan,
			CcsLoan loan) throws Exception {
		// 根据分期类型代码，查找分期控制参数
		LoanPlan loanPlan = parameterFacility.loadParameter(loan.getLoanCode(), LoanPlan.class);
		CcsAcct account = item.getAccount();
		BigDecimal bal = BigDecimal.ZERO;
		CcsRepaySchedule schedule = null;
		CcsAcct acct = item.getAccount();
		// 人工终止转移
		if (loan.getLoanStatus() == LoanStatus.T
				&& (loan.getTerminalReasonCd()==LoanTerminateReason.V || loan.getTerminalReasonCd()==LoanTerminateReason.M )){
			loan.setTerminalDate(batchFacility.getBatchDate());
			loan.setRemainTerm(1);
			loan.setLoanExpireDate(batchFacility.getBatchDate());
			schedule =  nextScheduletRetry(loan, item, batchFacility.getBatchDate());
			acct.setBlockCode(blockCodeUtils.addBlockCode(acct.getBlockCode(), I_CODE));
		}
		// 逾期终止转移
		else if (!account.getAgeCode().equals("C") && loan.getLoanStatus() != LoanStatus.T
				&& Integer.valueOf(account.getAgeCode()) >= Integer.valueOf(loanPlan.terminateAgeCd)) {
			loan.setLastLoanStatus(loan.getLoanStatus());
			loan.setLoanStatus(LoanStatus.T);
			loan.setTerminalReasonCd(LoanTerminateReason.D);
			loan.setTerminalDate(batchFacility.getBatchDate());
			loan.setRemainTerm(1);
			loan.setLoanExpireDate(batchFacility.getBatchDate());
			schedule =  nextScheduletRetry(loan, item, batchFacility.getBatchDate());
			acct.setBlockCode(blockCodeUtils.addBlockCode(acct.getBlockCode(), I_CODE));
		}
		// 锁定码终止转移：B|要求全部余额进行还款
		else if (PaymentCalcMethod.B == blockCodeUtils.getMergedPaymentInd(account.getBlockCode()) && loan.getLoanStatus() != LoanStatus.T) {
			loan.setLastLoanStatus(loan.getLoanStatus());
			loan.setLoanStatus(LoanStatus.T);
			loan.setTerminalReasonCd(LoanTerminateReason.R);
			loan.setTerminalDate(batchFacility.getBatchDate());
			loan.setRemainTerm(1);
			loan.setLoanExpireDate(batchFacility.getBatchDate());
			schedule =  nextScheduletRetry(loan, item, batchFacility.getBatchDate());
			acct.setBlockCode(blockCodeUtils.addBlockCode(acct.getBlockCode(), I_CODE));
		}
		// 正常转移
		else {
			// 如果剩余期数<=1，设置完成
			if (loan.getRemainTerm() <= 1) {
				loan.setLastLoanStatus(loan.getLoanStatus());
				if(loan.getLoanStatus() != LoanStatus.T){
					loan.setLoanStatus(LoanStatus.F);
				}
			}
			//正常取下一期（包括理赔和提前结清）
			for(CcsRepaySchedule t : item.getSchedules()){
				if(t.getLoanId().intValue() == loan.getLoanId().intValue()){
					if(t.getCurrTerm().intValue() == (loan.getCurrTerm()+1)){
						schedule = t;
						break;
					}
				}
			}
		}
		
		if(schedule == null){
			throw new ProcessException("未找到分期还款计划表，严重异常,loanId="+loan.getLoanId());
		}else{
			// 当前计划余额 > 分期每期应还本金, 正常转移1期金额
			bal = bal.add(schedule.getLoanTermPrin());
			if((xfrOutPlan.getCtdPrincipal().add(xfrOutPlan.getPastPrincipal())).compareTo(bal)<0){
				bal = xfrOutPlan.getCtdPrincipal().add(xfrOutPlan.getPastPrincipal());
			}
		}
		generateLoanAndPlan(item, xfrOutPlan, xfrInPlan, loan, bal,SysTxnCd.S51,SysTxnCd.S52);
		
		BigDecimal interest = schedule.getLoanTermInt();
		//生成一笔利息入账到转入计划
		generatorTransaction.generateInterestTransaction(item, xfrInPlan, interest, batchFacility.getBatchDate());

		//保费是分期还款计划的
		BigDecimal insurance = schedule.getLoanInsuranceAmt();
		if(insurance.compareTo(BigDecimal.ZERO) > 0){
			//生成一笔保费入账到转入计划
			generatorTransaction.generateTxnTransaction(item, xfrInPlan, insurance, batchFacility.getBatchDate(),BucketType.InsuranceFee);
			// 更新分期信息表(TM_LOAN)的未出账单分期印花税
			loan.setUnstmtInsuranceAmt(loan.getUnstmtInsuranceAmt().subtract(insurance));		
			//更新分期信息表(TM_LOAN)的分期已出账单印花税
			loan.setLoanInsuranceAmt(loan.getLoanInsuranceAmt().add(insurance));
		}
		//印花税是分期还款计划的
		BigDecimal stampDuty = schedule.getLoanStampdutyAmt();
		if(stampDuty.compareTo(BigDecimal.ZERO) > 0){
			//生成一笔印花税入账到转入计划
			generatorTransaction.generateTxnTransaction(item, xfrInPlan, stampDuty, batchFacility.getBatchDate(),BucketType.StampDuty);
			// 更新分期信息表(TM_LOAN)的未出账单分期印花税
			loan.setUnstmtStampdutyAmt(loan.getUnstmtStampdutyAmt().subtract(stampDuty));		
			//更新分期信息表(TM_LOAN)的分期已出账单印花税
			loan.setLoanStampdutyAmt(loan.getLoanStampdutyAmt().add(stampDuty));
		}
		if(loan.getStampCustomInd().equals(Indicator.N) && loan.getUnstmtStampdutyAmt().compareTo(BigDecimal.ZERO) > 0){
			//如果贷款印花税不计入客户账，并且未出账单的印花税大于0,生成转入交易
			CcsPostingTmp stampdutyTxn = generatePostingTmp(loan, SysTxnCd.S97, xfrInPlan, batchFacility.getBatchDate(), loan.getUnstmtStampdutyAmt(), loan.getCurrTerm());
			em.persist(stampdutyTxn);
			item.getTxnPosts().add(stampdutyTxn);
			// 调用内部交易入账处理
			transactionPost.posting(item, stampdutyTxn, batchFacility.getBatchDate());
			if(loan.getIsOffsetRate().equals(Indicator.Y)){
				//冲减利息
				CcsPostingTmp stampdutyTxnXfrInt = generatePostingTmp(loan,  SysTxnCd.S98, xfrInPlan, batchFacility.getBatchDate(), loan.getUnstmtStampdutyAmt(), loan.getCurrTerm());
				
				em.persist(stampdutyTxnXfrInt);
				item.getTxnPosts().add(stampdutyTxnXfrInt);
				// 调用内部交易入账处理
				transactionPost.posting(item, stampdutyTxnXfrInt, batchFacility.getBatchDate());
			}
			loan.setUnstmtStampdutyAmt(BigDecimal.ZERO);	
		}
		//寿险计划包费是分期还款计划的
		BigDecimal lifeInsuFee = schedule.getLoanLifeInsuAmt();
		if(lifeInsuFee.compareTo(BigDecimal.ZERO) > 0){
			//生成一笔寿险计划包费入账到转入计划
			generatorTransaction.generateTxnTransaction(item, xfrInPlan, lifeInsuFee, batchFacility.getBatchDate(),BucketType.LifeInsuFee);
			// 更新分期信息表(TM_LOAN)的未出账单分期寿险计划包费
			loan.setUnstmtLifeInsuAmt(loan.getUnstmtLifeInsuAmt().subtract(lifeInsuFee));		
			//更新分期信息表(TM_LOAN)的分期已出账单寿险计划包费
			loan.setPastLifeInsuAmt(loan.getPastLifeInsuAmt().add(lifeInsuFee));
		}
		//贷款服务费是分期还款计划的
		BigDecimal svcFee = schedule.getLoanSvcFee();
		if(svcFee.compareTo(BigDecimal.ZERO) > 0){
			//生成一笔贷款服务费入账到转入计划
			generatorTransaction.generateTxnTransaction(item, xfrInPlan, svcFee, batchFacility.getBatchDate(),BucketType.SVCFee);
			// 更新分期信息表(TM_LOAN)的未出账单贷款服务费
			loan.setUnstmtSvcFee(loan.getUnstmtSvcFee().subtract(svcFee));		
			//更新分期信息表(TM_LOAN)的分期已出账单贷款服务费
			loan.setPastSvcFee(loan.getPastSvcFee().add(svcFee));
		}
		
		//贷款手续费是分期还款计划的
		BigDecimal installmenFee = schedule.getLoanTermFee();
		if(installmenFee.compareTo(BigDecimal.ZERO) > 0){
			//生成一笔贷款手续费入账到转入计划
			generatorTransaction.generateTxnTransaction(item, xfrInPlan, installmenFee, batchFacility.getBatchDate(),BucketType.SVCFee);
			// 更新分期信息表(TM_LOAN)的未出账单贷款服务费
			loan.setUnstmtFee(loan.getUnstmtFee().subtract(installmenFee));		
			//更新分期信息表(TM_LOAN)的分期已出账单贷款服务费
			loan.setLoanFeeXfrin(loan.getLoanFeeXfrin().add(installmenFee));
			//更新分期信息表(TM_LOAN)的分期未到期贷款服务费
			loan.setLoanFeeXfrout(loan.getLoanFeeXfrout().subtract(installmenFee));
		}
		
		// 代收服务费是分期还款计划的
		BigDecimal replaceSvcFee = schedule.getLoanReplaceSvcFee();
		if(replaceSvcFee.compareTo(BigDecimal.ZERO) > 0){
			//生成一笔代收服务费入账到转入计划
			generatorTransaction.generateTxnTransaction(item, xfrInPlan, replaceSvcFee, batchFacility.getBatchDate(),BucketType.ReplaceSvcFee);
			// 更新分期信息表(TM_LOAN)的未出账单代收服务费
			loan.setUnstmtReplaceSvcFee(loan.getUnstmtReplaceSvcFee().subtract(replaceSvcFee));		
			//更新分期信息表(TM_LOAN)的分期已出账单代收服务费
			loan.setPastReplaceSvcFee(loan.getPastReplaceSvcFee().add(replaceSvcFee));
		}
		
		// 灵活还款计划包
		BigDecimal prepayPkg = schedule.getLoanPrepayPkgAmt();
		if(prepayPkg.compareTo(BigDecimal.ZERO)>0){
			//生成一笔灵活还款计划包入账到转入计划
			generatorTransaction.generateTxnTransaction(item, xfrInPlan, prepayPkg, batchFacility.getBatchDate(),BucketType.PrepayPkg);
			//更新未出已出账单灵活还款计划包费
			loan.setUnstmtPrepayPkgAmt(loan.getUnstmtPrepayPkgAmt().subtract(prepayPkg));
			loan.setPastPrepayPkgAmt(loan.getPastPrepayPkgAmt().add(prepayPkg));
		}
	}

	/**
	 * @see 方法名：generateLoanAndPlan 
	 * @see 描述：更新loan、plan，生成入账交易
	 * @see 创建日期：2015-6-24下午6:29:38
	 * @author ChengChun
	 *  
	 * @param item
	 * @param xfrOutPlan
	 * @param xfrInPlan
	 * @param loan
	 * @param xfrAmt
	 * @param from
	 * @param to
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private void generateLoanAndPlan(S6000AcctInfo item, CcsPlan xfrOutPlan,CcsPlan xfrInPlan, CcsLoan loan, BigDecimal xfrAmt,SysTxnCd from,SysTxnCd to) {
		if (loan.getRemainTerm() >= 1) {
        	loan.setCurrTerm(loan.getCurrTerm() + 1);
        	loan.setRemainTerm(loan.getRemainTerm() - 1);
        }
		if(xfrAmt.equals(BigDecimal.ZERO)){//0金额不做xfr
			return;
		}
		List<CcsPostingTmp> l = generatorTransaction.generateLoanTransferTransaction(item, xfrOutPlan, xfrInPlan, loan, xfrAmt, batchFacility.getBatchDate(),from,to);
		String txnSeq = UUID.randomUUID().toString();
		for(CcsPostingTmp t : l){
			//将分期转出和转入送入glp
			item.getGlTxnItemList().add(calculator.makeGlTxn(
					item.getAccount().getOrg(), item.getAccount().getDefaultLogicCardNbr(), item.getAccount().getAcctNbr(), item.getAccount().getAcctType(), 
					txnSeq, item.getAccount().getCurrency(), item.getAccount().getAgeCodeGl(), t.getTxnCode(), t.getTxnDesc(), DbCrInd.M, 
					batchFacility.getBatchDate(), t.getTxnAmt(), PostGlIndicator.N, item.getAccount().getOwningBranch(), 
					t.getAcqBranchIq(), t.getPlanNbr(), BucketType.Pricinpal));
		}
		
		// 信用计划余额
		xfrOutPlan.setCurrBal(xfrOutPlan.getCurrBal().subtract(xfrAmt));
		xfrInPlan.setCurrBal(xfrInPlan.getCurrBal().add(xfrAmt));
		// 信用计划本金
		BigDecimal outPrincipal = xfrOutPlan.getCtdPrincipal().subtract(xfrAmt);
		if (outPrincipal.compareTo(BigDecimal.ZERO) >= 0) {
			xfrOutPlan.setCtdPrincipal(outPrincipal);
		} else {
			xfrOutPlan.setCtdPrincipal(BigDecimal.ZERO);
			xfrOutPlan.setPastPrincipal(xfrOutPlan.getPastPrincipal().subtract(outPrincipal.abs()));
		}
		xfrInPlan.setCtdPrincipal(xfrInPlan.getCtdPrincipal().add(xfrAmt));
		
		// 更新分期信息表(TM_LOAN)的未出账单分期本金
		loan.setUnstmtPrin(loan.getUnstmtPrin().subtract(xfrAmt));		
		//更新分期信息表(TM_LOAN)的分期已出账单本金
		loan.setLoanPrinXfrin(loan.getLoanPrinXfrin().add(xfrAmt));
		//更新分期信息表(TM_LOAN)的分期未到期本金
		loan.setLoanPrinXfrout(loan.getLoanPrinXfrout().subtract(xfrAmt));
	}
	
	
	/**
	 * 保费贷调，并入账
	 * @return
	 */
	public CcsPostingTmp generateReInsurance(S6000AcctInfo item, BigDecimal txnAmt, Date batchDate) {

		// 查找系统内部交易类型对照表
		SysTxnCdMapping sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S75), SysTxnCdMapping.class);
		// 根据交易码，查找交易码对象
		TxnCd txnCode = parameterFacility.loadParameter(sysTxnCd.txnCd, TxnCd.class);
		// 根据产品代码，查找超限参数对象
		ProductCredit productCr = parameterFacility.loadParameter(item.getAccount().getProductCd(), ProductCredit.class);
				
		// 交易主键生成
		CcsTxnSeq txnSeq = new CcsTxnSeq();
		txnSeq.setOrg(item.getAccount().getOrg());
		em.persist(txnSeq);
				
		String acqAcceptorId=productCr.financeOrgNo;
		
		// 生成内部交易
		String planNbr = planManager.getPlanNbrByTxnCd(txnCode, item.getAccount().getProductCd());
		CcsPostingTmp newTxn = generateTrans(txnSeq, item, item.getAccount().getDefaultLogicCardNbr(), item.getAccount().getDefaultLogicCardNbr(), productCr.productCd, txnCode, txnAmt, batchDate, planNbr, "",null,acqAcceptorId);
		
		// 数据持久化
		em.persist(newTxn);
		// 保存超限费交易
		item.getTxnPosts().add(newTxn);
		// 调用内部交易入账处理
		transactionPost.posting(item, newTxn, batchDate);
		
		return newTxn;
	}
	
	/**
	 * 
	 * @param txnSeq
	 * @param item
	 * @param cardNo
	 * @param logicCardNo
	 * @param productCd
	 * @param txnCd
	 * @param amt
	 * @param origTransDate
	 * @param planNbr
	 * @param refNbr
	 * @return
	 */
	private CcsPostingTmp generateTrans(CcsTxnSeq txnSeq,S6000AcctInfo item,String cardNo,String logicCardNo,String productCd,TxnCd txnCd,BigDecimal amt,Date origTransDate,String planNbr,String refNbr,Integer term,String acqAcceptorId){
		Date batchDate = batchFacility.getBatchDate();
		CcsCard card = queryFacility.getCardByCardNbr(cardNo);
		// 获取卡产品信息
		ProductCredit productCredit = parameterFacility.loadParameter(card.getProductCd(), ProductCredit.class);
		FinancialOrg financialOrg = parameterFacility.loadParameter(productCredit.financeOrgNo, FinancialOrg.class);
		
		CcsPostingTmp newTxn = new CcsPostingTmp();
		newTxn.setOrg(item.getAccount().getOrg()); // 机构号
		newTxn.setTxnSeq(txnSeq.getTxnSeq()); // 交易流水号
		newTxn.setAcctNbr(item.getAccount().getAcctNbr()); // 账户编号
		newTxn.setAcctType(item.getAccount().getAcctType()); // 账户类型
		newTxn.setCardNbr(cardNo); // 介质卡号
		newTxn.setLogicCardNbr(logicCardNo); // 逻辑卡号
		newTxn.setCardBasicNbr(logicCardNo); // 逻辑卡主卡卡号
		newTxn.setProductCd(productCd); // 产品代码
		newTxn.setTxnDate(batchDate); // 交易日期
		newTxn.setTxnTime(new Date()); // 交易时间
		newTxn.setPostTxnType(PostTxnType.M); // 入账交易类型
		newTxn.setTxnCode(txnCd.txnCd); // 交易码
		newTxn.setDbCrInd(txnCd.logicMod.getDbCrInd()); // 借贷标志
		newTxn.setTxnAmt(amt); // 交易金额
		newTxn.setPostAmt(amt); // 入账币种金额
		newTxn.setPostDate(batchDate); // 入账日期
		newTxn.setAuthCode(""); // 授权码
		newTxn.setCardBlockCode(""); // 卡片锁定码
		newTxn.setTxnCurrency(item.getAccount().getCurrency()); // 交易币种代码
		newTxn.setPostCurrency(item.getAccount().getCurrency()); // 入账币种代码
		newTxn.setOrigTransDate(origTransDate); // 原始交易日期
		// 如何确定分期交易原先使用的卡产品？是使用帐户上的默认卡产品，还是要根据原交易来确定？
		newTxn.setPlanNbr(planNbr); // 信用计划号
		newTxn.setRefNbr(refNbr); // 交易参考号
		newTxn.setTxnDesc(txnCd.description); // 交易描述
		newTxn.setTxnShortDesc(txnCd.shortDesc);
		newTxn.setPoints(BigDecimal.ZERO); // 积分数值
		newTxn.setPostingFlag(PostingFlag.F00); // 入账结果标示码
		newTxn.setPrePostingFlag(PostingFlag.F00);
		newTxn.setRelPmtAmt(BigDecimal.ZERO); // 公司卡还款金额
		newTxn.setOrigPmtAmt(BigDecimal.ZERO); // 还款交易原始金额
		newTxn.setAcqBranchIq(""); // 受理分行代码
		newTxn.setAcqTerminalId(""); // 受理机构终端标识码
		newTxn.setAcqAcceptorId(financialOrg.acqAcceptorId); // 受卡方标识码
		newTxn.setAcqAddress(""); // 受理机构名称地址
		newTxn.setMcc(""); // 商户类别代码
		newTxn.setOrigTxnCode(""); // 原交易交易码
		newTxn.setOrigTxnAmt(amt); // 原交易交易金额
		newTxn.setOrigSettAmt(amt); // 原交易清算金额
		newTxn.setInterchangeFee(BigDecimal.ZERO); // 原交易货币转换费
		newTxn.setFeePayout(BigDecimal.ZERO); // 原交易交易手续费
		newTxn.setFeeProfit(BigDecimal.ZERO); // 发卡方应得手续费收入
		newTxn.setLoanIssueProfit(BigDecimal.ZERO); // 分期交易发卡行收益
		newTxn.setStmtDate(item.getAccount().getNextStmtDate()); // 账单日期
		newTxn.setVoucherNo(""); // 销售单凭证号
		newTxn.setTerm(term);
		return newTxn;
	}
	/**
	 * 
	 * @see 方法名：generatePostingTmp 
	 * @see 描述：创建CcsPostingTmp
	 * @see 创建日期：2015-6-23下午7:36:48
	 * @param txnAmt
	 * @param term
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public CcsPostingTmp generatePostingTmp(CcsLoan loan, SysTxnCd sysTxnCd, CcsPlan xfrInPlan,
			Date batDate, BigDecimal txnAmt, Integer term) {
		
		SysTxnCdMapping sysTxnCdMapping = parameterFacility.loadParameter(sysTxnCd.toString(), SysTxnCdMapping.class);
		TxnCd txnCd = parameterFacility.loadParameter(sysTxnCdMapping.txnCd, TxnCd.class);
		CcsTxnSeq txnSeq = new CcsTxnSeq();
		txnSeq.setOrg(loan.getOrg());
		em.persist(txnSeq);
		
		CcsPostingTmp ttTxnPost = new CcsPostingTmp();
		ttTxnPost.setOrg(loan.getOrg());
		ttTxnPost.setTxnSeq(txnSeq.getTxnSeq());
		ttTxnPost.setAcctNbr(loan.getAcctNbr());
		ttTxnPost.setAcctType(loan.getAcctType());
		ttTxnPost.setCardNbr(loan.getCardNbr());
		ttTxnPost.setLogicCardNbr(loan.getLogicCardNbr());
		ttTxnPost.setCardBasicNbr(loan.getLogicCardNbr());
		ttTxnPost.setProductCd(xfrInPlan.getProductCd());
		ttTxnPost.setTxnTime(batDate);
		ttTxnPost.setTxnCurrency(loan.getAcctType().getCurrencyCode());
		ttTxnPost.setTxnAmt(txnAmt);
		ttTxnPost.setPostDate(batDate);
		ttTxnPost.setPostCurrency(loan.getAcctType().getCurrencyCode());
		ttTxnPost.setPostAmt(txnAmt);
		ttTxnPost.setPostTxnType(PostTxnType.M);
		ttTxnPost.setTxnCode(txnCd.txnCd);
		ttTxnPost.setOrigTxnCode(txnCd.txnCd);
		ttTxnPost.setDbCrInd(DbCrInd.M);
		ttTxnPost.setTxnDesc(txnCd.description);
		ttTxnPost.setTxnShortDesc(txnCd.shortDesc);
		ttTxnPost.setOrigTxnAmt(BigDecimal.ZERO);
		ttTxnPost.setOrigSettAmt(BigDecimal.ZERO);
		ttTxnPost.setMcc(null);
		ttTxnPost.setAuthCode(null);
		ttTxnPost.setTxnDate(batDate);
		String planNbr = planManager.getPlanNbrByTxnCd(txnCd, xfrInPlan.getProductCd());
		ttTxnPost.setPlanNbr(planNbr);
		ttTxnPost.setRefNbr(loan.getRefNbr());
		ttTxnPost.setVoucherNo(null);
		ttTxnPost.setCardBlockCode(null);
		ttTxnPost.setStmtDate(batDate);
		ttTxnPost.setOrigTransDate(null);
		ttTxnPost.setOrigPmtAmt(BigDecimal.ZERO);
		ttTxnPost.setPoints(BigDecimal.ZERO);
		ttTxnPost.setPrePostingFlag(PostingFlag.F00);
		ttTxnPost.setPostingFlag(PostingFlag.F00);
		ttTxnPost.setAcqBranchIq(null);
		ttTxnPost.setAcqTerminalId(null);
		ttTxnPost.setAcqAcceptorId(null);
		ttTxnPost.setAcqAddress(null);
		ttTxnPost.setRelPmtAmt(BigDecimal.ZERO);
		ttTxnPost.setInterchangeFee(BigDecimal.ZERO);
		ttTxnPost.setFeePayout(BigDecimal.ZERO);
		ttTxnPost.setFeeProfit(BigDecimal.ZERO);
		ttTxnPost.setLoanIssueProfit(BigDecimal.ZERO);
		ttTxnPost.setTerm(term);
		
		return ttTxnPost;
	}	

}
