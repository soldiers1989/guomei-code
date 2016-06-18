package com.sunline.ccs.batch.cc6000;

import java.math.BigDecimal;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.cc6000.common.BnpManager;
import com.sunline.ccs.batch.cc6000.common.Calculator;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.enums.BucketObject;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ppy.dictionary.enums.PostGlIndicator;
import com.sunline.ppy.dictionary.enums.PostTxnType;
import com.sunline.ppy.dictionary.enums.PostingFlag;


/** 
 * @see 类名：P6053GenerateTxnGl
 * @see 描述：总账交易接口
 *
 * @see 创建日期：   2015年6月25日 下午2:29:15
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class P6053GenerateTxnGl implements ItemProcessor<S6000AcctInfo, S6000AcctInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private Calculator calculator;
	@Autowired
	private BnpManager bnpManager;
	@Autowired
    private BatchStatusFacility batchFacility;
	
	@Override
	public S6000AcctInfo process(S6000AcctInfo item) {
		if (logger.isDebugEnabled()) {
			logger.debug("总账交易接口：Org["+item.getAccount().getOrg()
					+"],AcctNo["+item.getAccount().getAcctNbr()
					+"],AcctType["+item.getAccount().getAcctType()
					+"],Acct.AgeCd["+item.getAccount().getAgeCode()
					+"],Acct.GlAgeCd["+item.getAccount().getAgeCodeGl()
					+"],PreAcct.AgeCd["+item.getPreAccount().getAgeCode()
					+"],PreAcct.GlAgeCd["+item.getPreAccount().getAgeCodeGl()
					+"]");
		}
//		generateCreditCommitment(item);
		
		//合并还款产生的结息交易到ttTxnPost
		item.getTxnPosts().addAll(item.getGenerateTxns());
		
		// 账龄增加或贷记交易入账时账龄减少（账龄未减少到"C"），产生结转交易
		if (!"C".equals(item.getPreAccount().getAgeCode()) 
				&& !"C".equals(item.getAccount().getAgeCode())
				&& !item.getPreAccount().getAgeCode().equals(item.getAccount().getAgeCode())) {
			for (int i = 0; i < item.getPlans().size(); i++) {
				// 溢缴款计划不做结转
				if (item.getPlans().get(i).getPlanType() == PlanType.D) continue;
				// 处理所有bnp
				for (BucketObject bnp : BucketObject.values()) {
					// 获取Plan的对应余额成份
					BigDecimal planBnpBal = bnpManager.getBnpAmt(item.getPlans().get(i), bnp);
					// 当前为零则不做结转
					if (planBnpBal.compareTo(BigDecimal.ZERO) == 0) continue;
					// 查找系统内部交易类型对照表-转出
					SysTxnCdMapping sysTxnCdOut = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S18), SysTxnCdMapping.class);
					TxnCd txnCdOut = parameterFacility.loadParameter(sysTxnCdOut.txnCd, TxnCd.class);
					// 总账交易分组唯一标识
					String glTxnUUID = UUID.randomUUID().toString();
					// 增加总账的交易流水-转出
					item.getGlTxnItemList().add(calculator.makeGlTxn(
							item.getAccount().getOrg(), item.getAccount().getDefaultLogicCardNbr(), item.getAccount().getAcctNbr(), item.getAccount().getAcctType(), 
							glTxnUUID, item.getAccount().getCurrency(), item.getPreAccount().getAgeCodeGl(), sysTxnCdOut.txnCd, txnCdOut.description, DbCrInd.M, 
							batchFacility.getBatchDate(), planBnpBal.abs(), PostGlIndicator.N, item.getAccount().getOwningBranch(), 
							null, item.getPlans().get(i).getPlanNbr(), bnp.getBucketType()));
					// 查找系统内部交易类型对照表-转入
					SysTxnCdMapping sysTxnCdIn = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S19), SysTxnCdMapping.class);
					TxnCd txnCdIn = parameterFacility.loadParameter(sysTxnCdIn.txnCd, TxnCd.class);
					// 增加总账的交易流水-转入
					item.getGlTxnItemList().add(calculator.makeGlTxn(
							item.getAccount().getOrg(), item.getAccount().getDefaultLogicCardNbr(), item.getAccount().getAcctNbr(), item.getAccount().getAcctType(), 
							glTxnUUID, item.getAccount().getCurrency(), item.getAccount().getAgeCodeGl(), sysTxnCdIn.txnCd, txnCdIn.description, DbCrInd.M, 
							batchFacility.getBatchDate(), planBnpBal.abs(), PostGlIndicator.N, item.getAccount().getOwningBranch(), 
							null, item.getPlans().get(i).getPlanNbr(), bnp.getBucketType()));
				}
			}
		}
		// 处理所有挂账类金融交易
		for (CcsPostingTmp txnPost : item.getTxnPosts()) {
			logger.info("挂账交易码："+txnPost.getTxnSeq());
			if (txnPost.getPostingFlag() != PostingFlag.F00
					&& txnPost.getPostTxnType() == PostTxnType.M) {
				// 根据生成的内部交易类型，确定入账的逻辑模块
				TxnCd txnCd = parameterFacility.loadParameter(txnPost.getTxnCode(), TxnCd.class);
				// 增加单笔总账的金融交易流水
				calculator.makeSingleGlTxn(item, txnPost, txnPost.getPlanNbr(), "0", txnPost.getPostAmt(), txnCd.logicMod.getBucketType());
			}
		}

		return item;
	}
//
//	/**
//	 * 生成贷记卡承诺科目虚拟交易记录
//	 * @param item
//	 */
//	private void generateCreditCommitment(S6000AcctInfo item) {
//		if(isFirstRun(item)){
//			firstRun(item);
//			return;
//		}
//		CcsAcct acct = item.getAccount();
//		if(batchFacility.shouldProcess(acct.getSetupDate())){//新制卡，使用全部额度
//			BigDecimal bal = correctCurrBal(acct);
//			addCommitmentGlTxn(item, acct, bal, SysTxnCd.S41, DbCrInd.D);
//			acct.setUserAmt6(bal);
//		}else if(acct.getBlockCode()!=null && acct.getBlockCode().indexOf("P")!=-1){//销户释放额度
//			if(acct.getClosedDate() != null && batchFacility.shouldProcess(acct.getClosedDate()))
//			{
//				addCommitmentGlTxn(item, acct, acct.getCreditLmt(), SysTxnCd.S42, DbCrInd.C);
//				acct.setUserAmt6(BigDecimal.ZERO);
//			}
//		}else{//正常账户，比较今日承诺余额与昨日承诺余额差值
//			BigDecimal lastBal = acct.getUserAmt6();
//			BigDecimal bal = correctCurrBal(acct);
//			if(lastBal == null){
//				lastBal = correctCurrBal(item.getPreAccount());
//			} 
//			BigDecimal b = bal.subtract(lastBal);
//			if(b.compareTo(BigDecimal.ZERO)>0){
//				addCommitmentGlTxn(item, acct, b, SysTxnCd.S41, DbCrInd.D);
//			}else if(b.compareTo(BigDecimal.ZERO)<0){
//				addCommitmentGlTxn(item, acct, b.abs(), SysTxnCd.S42, DbCrInd.C);
//			}
//			acct.setUserAmt6(bal);
//		}
//	}
//
//	/**
//	 * 首次运行，使用授信额度-余额作为交易金额
//	 * @param item
//	 */
//	private void firstRun(S6000AcctInfo item) {
//		//承诺增加
//		BigDecimal b = correctCurrBal(item.getAccount());
//		addCommitmentGlTxn(item, item.getAccount(), b, SysTxnCd.S41, DbCrInd.D);
//		item.getAccount().setUserAmt6(b);
//	}
//
//	/**
//	 * 根据机构首次运行时间判断是否初次运行
//	 * @param item
//	 * @return
//	 */
//	private boolean isFirstRun(S6000AcctInfo item) {
//		CcsCommitRunCtl t = rTmCommitRunCtl.findOne(item.getAccount().getOrg());
//		if(t == null){
//			t = new CcsCommitRunCtl();
//			t.setFirstBatchDate(batchFacility.getBatchDate());
//			t.setOrg(item.getAccount().getOrg());
//			rTmCommitRunCtl.save(t);
//			return true;
//		}else{
//			if(DateUtils.truncatedEquals(t.getFirstBatchDate(), batchFacility.getBatchDate(), Calendar.DATE)){
//				return true;
//			}else{
//				return false;
//			}
//		}
//	}
//	
//	/**
//	 * 修正余额
//	 * @param acct
//	 * @return
//	 */
//	private BigDecimal correctCurrBal(CcsAcct acct){
//		BigDecimal b = acct.getCurrBal();
//		if(b.compareTo(BigDecimal.ZERO)<0){ //如果余额小于0，说明是溢缴款，金额修正为0
//			b = BigDecimal.ZERO;
//		}
//		BigDecimal bal = acct.getCreditLmt().subtract(b);//如果已超限，修正为0
//		if(bal.compareTo(BigDecimal.ZERO)<0){
//			return BigDecimal.ZERO;
//		}
//		return bal;
//	}
//	/**
//	 * 生成贷记卡承诺科目交易记录
//	 * @param item
//	 * @param acct
//	 * @param amt
//	 * @param sysTxnCd
//	 * @param dbCrInd
//	 */
//	private void addCommitmentGlTxn(S6000AcctInfo item, CcsAcct acct,BigDecimal amt,SysTxnCd sysTxnCd,DbCrInd dbCrInd){
//		GlTxnItem glTxn = new GlTxnItem();
//		glTxn.cardNo = acct.getDefaultLogicCardNbr();
//		glTxn.txnDesc = "";
//		glTxn.org = acct.getOrg();
//		glTxn.acctNo = acct.getAcctNbr();
//		glTxn.acctType = acct.getAcctType();
//		glTxn.txnSeq = UUID.randomUUID().toString();
//		glTxn.currCd = acct.getCurrency();
//		glTxn.ageCd = "0";//写死
//		SysTxnCdMapping sysTxnCdOut = parameterFacility.loadParameter(String.valueOf(sysTxnCd), SysTxnCdMapping.class);
//		// 根据交易码，查找交易码对象
//		TxnCd txnCode = parameterFacility.loadParameter(sysTxnCdOut.txnCd, TxnCd.class);
//		
//		glTxn.txnCode = txnCode.txnCd;
//		glTxn.dbCrInd = dbCrInd;
//		glTxn.postDate = batchFacility.getBatchDate();
//		glTxn.postAmount = amt;
//		glTxn.postGlInd = PostGlIndicator.N;
//		glTxn.owningBranch = acct.getOwningBranch();
//		glTxn.acqBranchId = acct.getOwningBranch();
//		glTxn.planNbr = planManager.getPlanNbrByTxnCd(txnCode, acct.getProductCd());;
//		glTxn.bucketType = txnCode.logicMod.getBucketType();
//		
//		item.getGlTxnItemList().add(glTxn);
//	}
}
