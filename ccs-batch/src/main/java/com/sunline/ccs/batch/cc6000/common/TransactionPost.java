package com.sunline.ccs.batch.cc6000.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.cc6000.S6000AcctInfo;
import com.sunline.ccs.batch.cc6000.interestaccrue.InterestReview;
import com.sunline.ccs.batch.cc6000.interestaccrue.LateFee;
import com.sunline.ccs.batch.cc6000.interestaccrue.MulctAccrue;
import com.sunline.ccs.batch.cc6000.interestaccrue.ReplaceMulctAccrue;
import com.sunline.ccs.batch.cc6000.interestaccrue.TxnDateAccrue;
import com.sunline.ccs.batch.common.EnumUtils;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.TxnFee;
import com.sunline.ccs.param.def.enums.BucketObject;
import com.sunline.ccs.param.def.enums.IntAccumFrom;
import com.sunline.ccs.param.def.enums.LogicMod;
import com.sunline.ccs.param.def.enums.ReturnFeeInd;
import com.sunline.ccs.param.def.enums.ReturnPointInd;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ppy.dictionary.enums.PostTxnType;
import com.sunline.ppy.dictionary.enums.PostingFlag;


/** 
 * @see 类名：TransactionPost
 * @see 描述：交易入账处理
 *
 * @see 创建日期：   2015年6月25日 下午3:06:58
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class TransactionPost {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private final String BLOCK_CODE_W = "W";
	private final String AGE_CD_W = "W";

	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private TransactionGenerator generatorTransaction;
	@Autowired
	private TransactionPostCheck postTxnCheck;
	@Autowired
	private PlanManager createPlan;
	@Autowired
	private LogicalModuleExecutor logicalModuleExecutor;
	@Autowired
	private BnpManager bnpManager;
	@Autowired
	private AuthMemoMatcher authorizeMatch;
	@Autowired
	private Calculator calculator;
	@Autowired
	private AgeController ageController;
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	@Autowired
	private MulctAccrue mulctAccrue;
	@Autowired
	private ReplaceMulctAccrue replaceMulctAccrue;
	@Autowired
	private LateFee lateFee;
	@Autowired
	private TxnDateAccrue txnDateAccrue;
	@Autowired
	private InterestReview interestReview;

	
	/**
	 * 交易入账处理
	 * @param account
	 * @param item
	 * @param plans
	 * @return
	 * @throws Exception
	 */
	public List<CcsPostingTmp> posting(S6000AcctInfo item, CcsPostingTmp txnPost, Date batchDate) {
		if (logger.isDebugEnabled()) {
			logger.debug("交易入账:TxnSeq["+txnPost.getTxnSeq()
					+"],CardBlockCode["+txnPost.getCardBlockCode()
					+"],TxnTime["+txnPost.getTxnTime()
					+"],TxnCode["+txnPost.getTxnCode()
					+"],LogicalCardNo["+txnPost.getLogicCardNbr()
					+"],PlanNbr["+txnPost.getPlanNbr()
					+"],RefNbr["+txnPost.getRefNbr()
					+"],PostAmt["+txnPost.getPostAmt()
					+"]");
		}
		
		List<CcsPostingTmp> newTxnPosts = new ArrayList<CcsPostingTmp>();
		// 保存入账前账户的账户余额
		BigDecimal postPreAcctCurrBal = item.getAccount().getCurrBal();

		// 入账交易检查：未通过入账交易检查，暂存该笔交易，处理下笔
		PostingFlag postingFlag = postTxnCheck.postTransactionCheck(item.getAccount(), txnPost, batchDate);
		if (postingFlag != null) {
			if(postingFlag.equals(PostingFlag.F04) || postingFlag.equals(PostingFlag.F08)){
				//某些问题应该断批量
				throw new IllegalArgumentException("交易挂账原因太严重啦 :TxnSeq["+txnPost.getTxnSeq()
						+"],TxnCode["+txnPost.getTxnCode()
						+"],LogicalCardNo["+txnPost.getLogicCardNbr()
						+"],RefNbr["+txnPost.getRefNbr()
						+"],PostingFlag["+postingFlag+"]");
			}else{
				txnPost.setPostingFlag(postingFlag);
				return newTxnPosts;
			}
			
		}
		// 计算交易的账单日期
		txnPost.setStmtDate(item.getAccount().getNextStmtDate());
		// MEMO交易不执行入账逻辑
		if (DbCrInd.M == txnPost.getDbCrInd()) return newTxnPosts;

		// 更新入账日期
		txnPost.setPostDate(batchDate);
		
		// 00：成功入账
		txnPost.setPostingFlag(PostingFlag.F00);
		
		
		// 根据生成的内部交易类型，确定入账的逻辑模块
		TxnCd txnCd = parameterFacility.loadParameter(txnPost.getTxnCode(), TxnCd.class);
		// 判断对应交易，交易类型是否为积分交易：P|积分交易
		if (PostTxnType.P.equals(txnPost.getPostTxnType())) {
			if (logger.isDebugEnabled()) {
				logger.debug("积分:PostAmt["+txnPost.getPostAmt()
						+"],PointBal["+item.getAccount().getPointsBal()
						+"]");
			}
			// 积分交易入账：61：积分增加；62：积分减少；63：积分兑换
			logicalModuleExecutor.executeLogicalModule(txnCd.logicMod, item, txnPost, null);
			if (logger.isDebugEnabled()) {
				logger.debug("积分:PostAmt["+txnPost.getPostAmt()
						+"],PointBal["+item.getAccount().getPointsBal()
						+"]");
			}
			return newTxnPosts;
		}

		// 根据交易的信用计划号，查找信用计划模板，如果未找到，将抛出异常中断批量
		PlanTemplate planTemplate = parameterFacility.loadParameter(txnPost.getPlanNbr(), PlanTemplate.class);
		// 查找交易对应信用计划
		String logicCardNbr;
		if (LogicMod.L30.equals(txnCd.logicMod) || LogicMod.L32.equals(txnCd.logicMod)){
			logicCardNbr = item.getAccount().getDefaultLogicCardNbr();
		}else {
			logicCardNbr = txnPost.getCardBasicNbr();
		}
		
		List<CcsPlan> findPlans = new ArrayList<CcsPlan>();
		CcsLoan origLoan = null;
		if(planTemplate.multSaleInd){
			findPlans = createPlan.findTxnPlans(logicCardNbr, txnPost.getPlanNbr(), planTemplate.planType, txnPost.getRefNbr(), item.getPlans(),txnPost.getTerm());
		}else{
			if(txnCd.logicMod == LogicMod.L01){
				if(txnPost.getRefNbr() != null){
					// 直接新建计划，不在查找原plan
				}else{
					throw new IllegalArgumentException("新建计划时，REFNbr不允许为空！");
				}
			}
			//若退货，使用原交易的REF_NBR
			else if(LogicMod.L02.equals(txnCd.logicMod) && planTemplate.planType == PlanType.O){
				for(CcsLoan loan : item.getLoans()){
					if(txnPost.getAuthCode().equals(loan.getOrigAuthCode())
//							&& txnPost.getOrigTransDate().compareTo(loan.getOrigTransDate()) ==0
							&& txnPost.getLogicCardNbr().equals(loan.getLogicCardNbr())
							&& txnPost.getTxnAmt().compareTo(loan.getOrigTxnAmt()) ==0){
						origLoan = loan;
					}
				}
				findPlans = createPlan.findTxnPlans(logicCardNbr, txnPost.getPlanNbr(), PlanType.O, origLoan.getRefNbr(), item.getPlans(),txnPost.getTerm());
			}else{
				if(txnPost.getRefNbr() != null){
					findPlans = createPlan.findTxnPlans(logicCardNbr, txnPost.getPlanNbr(), planTemplate.planType, txnPost.getRefNbr(), item.getPlans(),txnPost.getTerm());
				}else{
					throw new IllegalArgumentException("新建计划时，REFNbr不允许为空！");
				}
			}
		}
		
		CcsPlan plan = null;
		// 信用计划不存在
		if (findPlans.size() < 1) {
			// 建立信用计划
			try {
				plan = createPlan.generateTxnPlan(item.getAccount(), item.getLoans(),	logicCardNbr, txnPost.getProductCd(), txnPost.getRefNbr(), planTemplate, batchDate,txnPost.getTerm());
			} catch (Exception e) {
				txnPost.setPostingFlag(PostingFlag.F11);
				return newTxnPosts;
			}
			item.getPlans().add(plan);
		}
		// F13|找到多个信用计划
		else if (findPlans.size() > 1 && planTemplate.planType != PlanType.D) {
			txnPost.setPostingFlag(PostingFlag.F13);
			return newTxnPosts;
		} else plan = findPlans.get(findPlans.size() - 1);
		
		if (logger.isDebugEnabled()) {
			logger.debug("查找信用计划-确定计划:bscLogiccardNo["+plan.getLogicCardNbr()
					+"],planNbr["+plan.getPlanNbr()
					+"],planType["+plan.getPlanType()
					+"],refNbr["+plan.getRefNbr()
					+"]");
		}

		if (logger.isDebugEnabled()) {
			logger.debug("金融交易:PostAmt["+txnPost.getPostAmt()
					+"],CurrBal["+item.getAccount().getCurrBal()
					+"]");
		}
		//设置交易的loancode
		if(txnPost.getLoanCode() == null && item.getAccount().getAcctType() == AccountType.E
				&& plan.getRefNbr()!=null){
			for(CcsLoan loan:item.getLoans()){
				if(loan.getRefNbr().equals(plan.getRefNbr())){
					txnPost.setLoanCode(loan.getLoanCode());
					break;
				}
			}
		}
		
		if (LogicMod.L30.equals(txnCd.logicMod) || LogicMod.L32.equals(txnCd.logicMod)) {
			// 如果入账逻辑模块还款，判断是否回溯罚金
			//如果交易时间小于入账时间执行罚金回溯
			if(txnPost.getPostDate().compareTo(txnPost.getTxnDate())>0){
				if (logger.isDebugEnabled()) {
					logger.debug("还款交易，执行罚金回溯:PostAmt["+txnPost.getPostAmt()
							+"],CurrBal["+item.getAccount().getCurrBal()
							+"]");
				}
				//记录原逾期起始日期、CPD起始日期
				//避免由于优先级导致
				Date overDueDate = item.getLoans().get(0).getOverdueDate();
				Date cpdBeginDate = item.getLoans().get(0).getCpdBeginDate();
				mulctAccrue.accumulateMulct(item,txnPost.getPostAmt(),txnPost.getTxnDate(),newTxnPosts);
				if (logger.isDebugEnabled()) {
					logger.debug("还款交易，执行滞纳金回溯:PostAmt["+txnPost.getPostAmt()
							+"],CurrBal["+item.getAccount().getCurrBal()
							+"]");
				}
				lateFee.accumulateLateFee(item, txnPost.getPostAmt(),txnPost.getTxnDate(), newTxnPosts, BucketType.LatePaymentCharge);
				if (logger.isDebugEnabled()) {
					logger.debug("还款交易，执行代收罚金回溯:PostAmt["+txnPost.getPostAmt()
							+"],CurrBal["+item.getAccount().getCurrBal()
							+"]");
				}
				replaceMulctAccrue.accumulateMulct(item,txnPost.getPostAmt(),txnPost.getTxnDate(),newTxnPosts,overDueDate,cpdBeginDate);
				if (logger.isDebugEnabled()) {
					logger.debug("还款交易，执行代收滞纳金回溯:PostAmt["+txnPost.getPostAmt()
							+"],CurrBal["+item.getAccount().getCurrBal()
							+"]");
				}
				lateFee.accumulateLateFee(item, txnPost.getPostAmt(),txnPost.getTxnDate(), newTxnPosts, BucketType.ReplaceLatePaymentCharge);
			}
		}
		// 判断对应交易，入账逻辑模块
		BucketType bucketType = logicalModuleExecutor.executeLogicalModule(txnCd.logicMod, item, txnPost, plan);
		if(txnPost.getPostingFlag() != PostingFlag.F00){//挂账交易，直接返回不做处理
			return newTxnPosts;
		}
		// 更新账户最后交易入账日期
		item.getAccount().setLastPostDate(batchDate);
		
		// 判断信用计划余额不等于零，且还清日期不等于NULL，则更新还清日期为NULL
		if (plan.getCurrBal().compareTo(BigDecimal.ZERO) != 0 
				&& plan.getPaidOutDate() != null){
			plan.setPaidOutDate(null);
		}
		
		// 获取交易码对应，要生成的内部交易费集合，判断是否需伴随生成交易费
		ProductCredit productCredit = parameterFacility.loadParameter(txnPost.getProductCd(), ProductCredit.class);
				
		//按交易日计息,只考虑借记交易，在入账完成后，进行计息，计息只补记交易本身产生的利息
		if( txnPost.getPostDate().compareTo(txnPost.getTxnDate())>0){
			if(DbCrInd.D == txnCd.logicMod.getDbCrInd()){
				// 获取计息参数
				//获取计息开始日期类型（暂只支持入账日起息和账单日起息）
				IntAccumFrom f = planTemplate.intParameterBuckets.get(bucketType).intAccumFrom;
				logger.debug("金融交易:借贷方向=["+txnCd.logicMod.getDbCrInd()+"]," +
						"交易日=["+txnPost.getTxnDate()+"],入账日=["+txnPost.getPostDate()+"],起息类型=["+f.toString()+"]");
				if(f.equals(IntAccumFrom.T)){
					//补记利息
					txnDateAccrue.txnAccure(item, plan, planTemplate, bucketType, txnPost);
				}
			}else if(DbCrInd.C == txnCd.logicMod.getDbCrInd()){
				if( productCredit.isReviewInt == Indicator.Y ){
					logger.debug("金融交易:借贷方向=["+txnCd.logicMod.getDbCrInd()+"]," +
							"交易日=["+txnPost.getTxnDate()+"],入账日=["+txnPost.getPostDate()+"],回溯利息");
					//利息回溯
					interestReview.accumulateInterReview(item, txnPost.getPostAmt(), txnPost.getTxnDate(), newTxnPosts);
				}
			}
			
		}
		
		
		if (logger.isDebugEnabled()) {
			logger.debug("金融交易:PostAmt["+txnPost.getPostAmt()
					+"],CurrBal["+item.getAccount().getCurrBal()
					+"]");
		}

		
				
		// 判断对应交易，借贷标记字段是否为“C|贷记交易”
		if (DbCrInd.C == txnCd.logicMod.getDbCrInd() ) {
			/**
			 * 账单分期贷调,不对往期最小还款额冲抵
			 */
			if(EnumUtils.in(txnCd.logicMod,LogicMod.A26,LogicMod.L26)){
				ageController.updateMinDueByCrAdj(item.getAccount(), txnPost, batchDate);
			}else{
				// 计算最小还款额(DUE)
				ageController.offsetMinDue(item.getAccount(), txnPost.getPostAmt());
			}
			// 计算账龄
			ageController.setAgeCode(item.getAccount(), batchDate,false);
			//重算逾期起始天数
			//更新loan的逾期起始日期，由于会用到账龄，必须放到更新账龄后
			ageController.updateOverdueDate(item.getLoans(), item.getPlans(),false,item.getAccount());
			
		}

		// 判断对应交易，入账逻辑模块字段是否为“L30|还款”
		if (LogicMod.L30.equals(txnCd.logicMod) || LogicMod.L32.equals(txnCd.logicMod)) {
			// 增加总账的交易流水
			this.addGlTxn(item, txnPost);
		} else {
			// 增加总账的交易流水
			calculator.makeSingleGlTxn(item, txnPost, txnPost.getPlanNbr(), item.getPreAccount().getAgeCodeGl(), txnPost.getPostAmt(), bucketType);
		}

		//授权匹配处理
		authorizeMatch.authorizeMatch(txnPost, item.getUnmatchs(), item.getUnmatchStatuses());
		
		if (productCredit.txnFeeList != null
				// 检查账户锁定码免除交易费标志
				&& !blockCodeUtils.getMergedTxnFeeWaiveInd(item.getAccount().getBlockCode())
				// 检查卡片锁定码免除交易费标志
				&& !blockCodeUtils.getMergedTxnFeeWaiveInd(txnPost.getCardBlockCode())) {

			List<TxnFee> txnFees = productCredit.txnFeeList.get(txnCd.txnCd);
			for (int i = 0; txnFees != null && i < txnFees.size(); i++) {
				TxnFee txnFee = txnFees.get(i);
				// 内部交易生成
				CcsPostingTmp newTxn = generatorTransaction.generateTransactionFee(item, postPreAcctCurrBal, txnPost, txnFee, batchDate, plan.getProductCd(),newTxnPosts);
				
				if (logger.isDebugEnabled()) {
					logger.debug("内部交易生成:TxnCode["+txnPost.getTxnCode()
							+"],feeTxnCd["+txnFees.get(i).feeTxnCd
							+"],txnFee["+txnFee
							+"],ProductCd["+plan.getProductCd()
							+"]");
				}
				
				if (newTxn == null) continue;
			}
		}
		
		// 分期退货生成手续费贷调交易和积分调整交易
		if(LogicMod.L02.equals(txnCd.logicMod) && planTemplate.planType == PlanType.O){
			LoanPlan loanPlan = parameterFacility.loadParameter(origLoan.getLoanCode(), LoanPlan.class);
			// 积分调整
			if(loanPlan.returnPointInd == ReturnPointInd.Y){
				//则以交易金额向上取整作为积分点数，并为该笔积分点数生成"S14-积分调整"交易
				BigDecimal returnPoint = txnPost.getTxnAmt().setScale(0, RoundingMode.HALF_UP);
				SysTxnCdMapping returnPointTxnCdMapping = parameterFacility.loadParameter(SysTxnCd.S14.toString(), SysTxnCdMapping.class);
				TxnCd returnPointTxnCd = parameterFacility.loadParameter(returnPointTxnCdMapping.txnCd, TxnCd.class);
				generatorTransaction.generatePoint(item, txnPost, returnPointTxnCd, returnPoint, batchStatusFacility.getBatchDate(),newTxnPosts);
			}
			
			// 手续费贷调
			if(loanPlan.returnFeeInd == ReturnFeeInd.B && origLoan.getCurrTerm() >0){
				BigDecimal returnFeeAmt = origLoan.getLoanFeeXfrin();//分期已出账单手续费
				SysTxnCdMapping returnPointTxnCdMapping = parameterFacility.loadParameter(SysTxnCd.S35.toString(), SysTxnCdMapping.class);
				TxnCd returnFeeTxnCd = parameterFacility.loadParameter(returnPointTxnCdMapping.txnCd, TxnCd.class);
				generatorTransaction.generateLoanFee(item, txnPost, origLoan, plan, returnFeeTxnCd, returnFeeAmt, batchStatusFacility.getBatchDate(),newTxnPosts);
			}else if(loanPlan.returnFeeInd == ReturnFeeInd.P && origLoan.getLoanFeeMethod() == LoanFeeMethod.F && origLoan.getLoanFeeXfrin().compareTo(BigDecimal.ZERO) >0){
				//如果是分期收取不做操作,如果是一次性收取，已出账单手续费*剩余期数/总期数
				BigDecimal returnFeeAmt = new BigDecimal(origLoan.getLoanFeeXfrin().floatValue() 
						*(origLoan.getRemainTerm().floatValue() / origLoan.getLoanInitTerm().floatValue()));
				SysTxnCdMapping returnPointTxnCdMapping = parameterFacility.loadParameter(SysTxnCd.S35.toString(), SysTxnCdMapping.class);
				TxnCd returnFeeTxnCd = parameterFacility.loadParameter(returnPointTxnCdMapping.txnCd, TxnCd.class);
				generatorTransaction.generateLoanFee(item, txnPost, origLoan, plan, returnFeeTxnCd, returnFeeAmt, batchStatusFacility.getBatchDate(),newTxnPosts);
			}
			// 分期退货终止
			origLoan.setLoanStatus(LoanStatus.T);// 暂时T
		}
		
		return newTxnPosts;
	}

	/**
	 * 增加总账的交易流水
	 * @param item
	 * @param prePlans 入账前计划列表
	 * @param txnPost 入账交易
	 */
	private void addGlTxn(S6000AcctInfo item, CcsPostingTmp txnPost) {
		List<CcsPlan> prePlans = item.getPaymentPrePlans();
		
		for (int i = 0; i < item.getPlans().size(); i++) {
			// 是否为溢缴款计划
			if (PlanType.D.equals(item.getPlans().get(i).getPlanType())) {
				// 获取Plan的余额成份
				BigDecimal currBnpAmt = bnpManager.getBnpAmt(item.getPlans().get(i), BucketObject.ctdPrincipal);
				BigDecimal perBnpAmt = bnpManager.getBnpAmt(prePlans.get(i), BucketObject.ctdPrincipal);
				BigDecimal glPostAmt = perBnpAmt.subtract(currBnpAmt).abs();
				if (glPostAmt.compareTo(BigDecimal.ZERO) > 0) {
					// 增加总账的交易流水
					calculator.makeSingleGlTxn(item, txnPost, item.getPlans().get(i).getPlanNbr(), 
							"C", glPostAmt, BucketObject.ctdPrincipal.getBucketType());
				}
			}
			else {
				// 根据信用计划号，查找信用计划模板
				PlanTemplate planT = parameterFacility.loadParameter(item.getPlans().get(i).getPlanNbr(), PlanTemplate.class);
				// 是否参与还款分配标志
				if (!planT.pmtAssignInd) continue;
				
				String glAgeCd;
				if(blockCodeUtils.isExists(item.getAccount().getBlockCode(), BLOCK_CODE_W)){
					glAgeCd = AGE_CD_W;
				}else{
					glAgeCd = item.getPreAccount().getAgeCodeGl();
				}
				
				for (BucketObject bnp : BucketObject.values()) {
					// 获取Plan的余额成份
					BigDecimal currBnpAmt = bnpManager.getBnpAmt(item.getPlans().get(i), bnp);
					BigDecimal perBnpAmt = bnpManager.getBnpAmt(prePlans.get(i), bnp);
					BigDecimal glPostAmt = perBnpAmt.subtract(currBnpAmt).abs();
					if (glPostAmt.compareTo(BigDecimal.ZERO) > 0) {
						// 增加总账的交易流水
						calculator.makeSingleGlTxn(item, txnPost, item.getPlans().get(i).getPlanNbr(), 
								glAgeCd, glPostAmt, bnp.getBucketType());
					}
				}
			}
		}
	}
}
