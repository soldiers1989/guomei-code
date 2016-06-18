package com.sunline.ccs.batch.cc6000.common;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.cc6000.S6000AcctInfo;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.RateDef;
import com.sunline.ccs.param.def.enums.BnpPeriod;
import com.sunline.ccs.param.def.enums.TierInd;
import com.sunline.ccs.param.def.enums.WaiveInd;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ppy.dictionary.enums.ExceptionType;
import com.sunline.ppy.dictionary.enums.PostGlIndicator;
import com.sunline.ppy.dictionary.enums.PostingFlag;
import com.sunline.ppy.dictionary.exchange.GlTxnItem;
import com.sunline.ppy.dictionary.report.ccs.ExceptionAccountRptItem;


/**
 * @see 类名：CommonComputeClass
 * @see 描述：入账公共计算类
 *
 * @see 创建日期：   2015-6-24下午6:58:35
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class Calculator {
	private Logger logger = LoggerFactory.getLogger(getClass());
		
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private BnpManager bnpManager;
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	
	/**
	 * @see 方法名：getOvrlmtNbrOfCyc 
	 * @see 描述：计算账户的连续超限账期
	 * @see 创建日期：2015-6-24下午6:59:07
	 * @author ChengChun
	 *  
	 * @param productCd
	 * @param ovrlmtDate
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public int getOvrlmtNbrOfCyc(String productCd, Date ovrlmtDate){
		
		// 账户属性参数
		ProductCredit productCredit = parameterFacility.loadParameter(productCd, ProductCredit.class);
		AccountAttribute acctAttr = parameterFacility.loadParameter(productCredit.accountAttributeId, AccountAttribute.class);
		
		Calendar batchDate = DateUtils.truncate(DateUtils.toCalendar(batchStatusFacility.getBatchDate()), Calendar.DATE);
		Calendar OLDate = DateUtils.truncate(DateUtils.toCalendar(ovrlmtDate), Calendar.DATE);

		switch (acctAttr.cycleBaseInd){
		case W: return Math.round((batchDate.getTimeInMillis() - OLDate.getTimeInMillis())/(1000*60*60*24)/7) + 1;
		case M:
			// 超限当月账单日
			Date stmtDay = DateUtils.setDays(ovrlmtDate, productCredit.dfltCycleDay);
			OLDate = DateUtils.truncate(DateUtils.toCalendar(stmtDay), Calendar.DATE);
			
			if (DateUtils.truncatedCompareTo(ovrlmtDate, stmtDay, Calendar.DATE) >= 0) {
				//OLDate = DateUtils.truncate(DateUtils.toCalendar(DateUtils.addMonths(stmtDay, -1)), Calendar.DATE);
				return (batchDate.get(Calendar.YEAR) - OLDate.get(Calendar.YEAR))*12 + 
					batchDate.get(Calendar.MONTH) - OLDate.get(Calendar.MONTH) + 2;
			} else {
				//OLDate = DateUtils.truncate(DateUtils.toCalendar(stmtDay), Calendar.DATE);
				return (batchDate.get(Calendar.YEAR) - OLDate.get(Calendar.YEAR))*12 + 
						batchDate.get(Calendar.MONTH) - OLDate.get(Calendar.MONTH) + 1;
			}
		default: throw new IllegalArgumentException("账户属性中账单周期类型["+acctAttr.cycleBaseInd+"]不正确");
		}
	}

	
	/**
	 * @see 方法名：getLCardNoByProductCr 
	 * @see 描述：根据逻辑卡号，获取产品参数
	 * @see 创建日期：2015-6-24下午6:59:47
	 * @author ChengChun
	 *  
	 * @param logicCardNbr
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public ProductCredit getLCardNoByProductCr(String logicCardNbr){
		// 获取卡BIN
		String lCardNoBin = logicCardNbr.substring(0,6);
		// 去掉卡BIN和最后一位校验位，获取卡号值
		BigDecimal lCardNoValue = new BigDecimal(logicCardNbr.substring(6, logicCardNbr.length()-1));
		
		List<Product> productList = new ArrayList<Product>();
		Map<String, Product> productMap = parameterFacility.retrieveParameterObject(Product.class);
		for (String productCode : productMap.keySet()) {
			Product productObj = productMap.get(productCode);
			
			if (!lCardNoBin.equals(productObj.bin)) continue;

			// 卡号段下限
			BigDecimal cardNoRangeFlr = new BigDecimal(productObj.cardnoRangeFlr);
			// 卡号段上限
			BigDecimal cardNoRangeCeil = new BigDecimal(productObj.cardnoRangeCeil);
			
			if (logger.isDebugEnabled()) {
				logger.debug("productCode["+productCode
						+"],bin["+productObj.bin
						+"],cardNoRangeFlr["+cardNoRangeFlr
						+"],cardNoRangeCeil["+cardNoRangeCeil
						+"]");
			}
			if (lCardNoValue.compareTo(cardNoRangeFlr) >= 0 && lCardNoValue.compareTo(cardNoRangeCeil) <= 0){
				if (logger.isDebugEnabled()) {
					logger.debug("productCode["+productCode
							+"]");
				}
				productList.add(productObj);
			}
		}
		if (productList.size() < 1) throw new IllegalArgumentException("根据逻辑卡号["+logicCardNbr+"]，未找到对应产品代码！");
		if (productList.size() > 1) throw new IllegalArgumentException("根据逻辑卡号["+logicCardNbr+"]，找到多个产品代码！");
		
		return parameterFacility.loadParameter(productList.get(0).productCode, ProductCredit.class);
	}
	

	/**
	 * @see 方法名：getCurrCreditLimit 
	 * @see 描述：获取账户层当前信用额度
	 * @see 创建日期：2015-6-24下午7:00:10
	 * @author ChengChun
	 *  
	 * @param account
	 * @param batchDate
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BigDecimal getCurrCreditLimit(CcsAcct account, Date batchDate) {
		// 当前信用额度
		BigDecimal currLimit = account.getCreditLmt();
		
		// 临时额度开始结束日期
		Date beginDate = account.getTempLmtBegDate();
		Date endDate = account.getTempLmtEndDate();
		
		// 判断临时额度是否在有效期内
		if (beginDate != null && endDate != null 
				&& batchDate.compareTo(beginDate) >= 0
				&& batchDate.compareTo(endDate) <= 0) {
			// 临时额度有效，当前信用额度 = 临时信用额度
			currLimit = account.getTempLmt(); 
		}
		return currLimit;
	}
	

	/**
	 * @see 方法名：getOverLimitAmt 
	 * @see 描述：计算超限金额
	 * @see 创建日期：2015-6-24下午7:02:55
	 * @author ChengChun
	 *  
	 * @param account
	 * @param plans
	 * @param batchDate
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BigDecimal getOverLimitAmt(CcsAcct account, List<CcsPlan> plans, Date batchDate) {
		// 账户总余额
		BigDecimal totalBal = BigDecimal.ZERO;
		
		// 计算账户参与超限计算的总余额
		for (CcsPlan plan : plans) {
			PlanTemplate planTemplate = parameterFacility.loadParameter(plan.getPlanNbr(), PlanTemplate.class);
			// 循环处理所有参与超限计算的字段
			for (BucketType bucketType : BucketType.values()) {
				if(planTemplate.intParameterBuckets.get(bucketType) == null) continue;
				// Bnp是否参与超限计算
				Boolean b = planTemplate.intParameterBuckets.get(bucketType).overlimitQualify;
				if (b==null?true:!b) continue;
				
				totalBal = totalBal.add(bnpManager.getBucketAmount(plan, bucketType, BnpPeriod.CTD))
								   .add(bnpManager.getBucketAmount(plan, bucketType, BnpPeriod.PAST));
			}
		}
		
		// 获得授权超限比例
		BigDecimal ovrlmtRate = this.getOvrlmtRate(account.getProductCd(), account.getOvrlmtRate());
		// 超限金额 = 账户总余额 - 当前信用额度 × 允许超限比例
		BigDecimal overAmount = totalBal.subtract(
				this.getCurrCreditLimit(account, batchDate).multiply(BigDecimal.ONE.add(ovrlmtRate)));
		// 判断超限金额是否大于0
		if (overAmount.compareTo(BigDecimal.ZERO) > 0){
			return overAmount;
		}

		return BigDecimal.ZERO;
	}
	
	/**
	 * @see 方法名：getOvrlmtRate 
	 * @see 描述： 获得授权超限比例
	 * @see 创建日期：2015-6-24下午7:03:10
	 * @author ChengChun
	 *  
	 * @param productCd
	 * @param ovrlmtRate
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BigDecimal getOvrlmtRate(String productCd, BigDecimal ovrlmtRate) {
		// 账户属性参数
		ProductCredit productCredit = parameterFacility.loadParameter(productCd, ProductCredit.class);
		AccountAttribute acctAttr = parameterFacility.loadParameter(productCredit.accountAttributeId, AccountAttribute.class);
		return ovrlmtRate == null ? acctAttr.ovrlmtRate : ovrlmtRate;
	}

	/**
	 * @see 方法名：makeExceptionAccount 
	 * @see 描述：增加异常账户报表记录
	 * @see 创建日期：2015-6-24下午7:03:36
	 * @author ChengChun
	 *  
	 * @param item
	 * @param planNbr
	 * @param refNbr
	 * @param exceptionType
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void makeExceptionAccount(S6000AcctInfo item, String planNbr, String refNbr, ExceptionType exceptionType) {
		
		ExceptionAccountRptItem exceptionAccount = new ExceptionAccountRptItem();
		exceptionAccount.org = item.getAccount().getOrg();
		exceptionAccount.acctNo = item.getAccount().getAcctNbr();
		exceptionAccount.acctType = item.getAccount().getAcctType();
		exceptionAccount.defaultLogicalCardNo = item.getAccount().getDefaultLogicCardNbr();
		exceptionAccount.planNbr = planNbr;
		exceptionAccount.refNbr = refNbr;
		exceptionAccount.exceptionType = exceptionType;
		
		item.getExceptionAccounts().add(exceptionAccount);
	}

	/**
	 * @see 方法名：makeSingleGlTxn 
	 * @see 描述：增加单笔总账的金融交易流水
	 * @see 创建日期：2015-6-24下午7:04:04
	 * @author ChengChun
	 *  
	 * @param item
	 * @param txnPost
	 * @param planNbr
	 * @param ageCd
	 * @param glPostAmt
	 * @param bucketType
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void makeSingleGlTxn(S6000AcctInfo item, CcsPostingTmp txnPost, String planNbr, String ageCd, BigDecimal glPostAmt, BucketType bucketType) {
		// 根据交易的信用计划号，查找信用计划模板
		//PlanTemplate planT = parameterFacility.retrieveParameterObject(planNbr, PlanTemplate.class);
		
		//零金额交易不记账
		if(txnPost.getTxnAmt().compareTo(BigDecimal.ZERO) !=0){
			// TODO zhengpy 将当日挂账的处理与当日成功的处理逻辑改为两个方法实现 当日挂账
			if (txnPost.getPostingFlag() != PostingFlag.F00) {
				// 当日挂账，往日初始值，送总账入账交易-挂账
				if (txnPost.getPrePostingFlag() == PostingFlag.F00) {
					// 增加总账的交易流水
					item.getGlTxnItemList().add(
							this.makeGlTxn(item.getAccount().getOrg(), txnPost.getCardNbr(), item.getAccount().getAcctNbr(), item.getAccount().getAcctType(), 
							String.valueOf(txnPost.getTxnSeq()), item.getAccount().getCurrency(), ageCd, txnPost.getTxnCode(), txnPost.getTxnDesc(), txnPost.getDbCrInd(), 
							txnPost.getPostDate(), glPostAmt.abs(), PostGlIndicator.S, item.getAccount().getOwningBranch(), 
							txnPost.getAcqBranchIq(), planNbr, bucketType));
				}
			}
			// 当日成功
			else {
				// 当日成功，往日挂账，送总账入账交易-核销
				if (txnPost.getPrePostingFlag() != PostingFlag.F00) {
					// 增加总账的交易流水
					item.getGlTxnItemList().add(
							this.makeGlTxn(item.getAccount().getOrg(), txnPost.getCardNbr(), item.getAccount().getAcctNbr(), item.getAccount().getAcctType(), 
							String.valueOf(txnPost.getTxnSeq()), item.getAccount().getCurrency(), ageCd, txnPost.getTxnCode(), txnPost.getTxnDesc(), txnPost.getDbCrInd(), 
							txnPost.getPostDate(), glPostAmt.abs(), PostGlIndicator.W, item.getAccount().getOwningBranch(), 
							txnPost.getAcqBranchIq(), planNbr, bucketType));
				}
				// 当日成功，往日初始值，送总账入账交易-正常
				else {
					// 增加总账的交易流水
					item.getGlTxnItemList().add(
							this.makeGlTxn(item.getAccount().getOrg(), txnPost.getCardNbr(), item.getAccount().getAcctNbr(), item.getAccount().getAcctType(), 
							String.valueOf(txnPost.getTxnSeq()), item.getAccount().getCurrency(), ageCd, txnPost.getTxnCode(), txnPost.getTxnDesc(), txnPost.getDbCrInd(), 
							txnPost.getPostDate(), glPostAmt.abs(), PostGlIndicator.N, item.getAccount().getOwningBranch(), 
							txnPost.getAcqBranchIq(), planNbr, bucketType));
				}
			}
		}
	}

	/**
	 * 
	 * @see 方法名：makeGlTxn 
	 * @see 描述：生成总账的交易流水
	 * @see 创建日期：2015-6-24下午7:08:41
	 * @author ChengChun
	 *  
	 * @param org
	 * @param cardNo
	 * @param acctNo
	 * @param acctType
	 * @param txnSeq
	 * @param currCd
	 * @param ageCd
	 * @param txnCode
	 * @param txnDesc
	 * @param dbCrInd
	 * @param postDate
	 * @param glPostAmt
	 * @param postGlInd
	 * @param owningBranch
	 * @param acqBranchId
	 * @param planNbr
	 * @param bucketType
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public GlTxnItem makeGlTxn(String org, String cardNo, Long acctNo, AccountType acctType, String txnSeq, String currCd,
			String ageCd, String txnCode, String txnDesc, DbCrInd dbCrInd, Date postDate,
			BigDecimal glPostAmt, PostGlIndicator postGlInd, String owningBranch, String acqBranchId, 
			String planNbr, BucketType bucketType) {
		
		GlTxnItem glTxn = new GlTxnItem();
		glTxn.cardNo = cardNo;
		glTxn.txnDesc = txnDesc;
		glTxn.org = org;
		glTxn.acctNo = acctNo;
		glTxn.acctType = acctType;
		glTxn.txnSeq = txnSeq;
		glTxn.currCd = currCd;
		glTxn.ageCd = ageCd;
		glTxn.txnCode = txnCode;
		glTxn.dbCrInd = dbCrInd;
		glTxn.postDate = postDate;
		glTxn.postAmount = glPostAmt.abs();
		glTxn.postGlInd = postGlInd;
		glTxn.owningBranch = owningBranch;
		glTxn.acqBranchId = acqBranchId;
		glTxn.planNbr = planNbr;
		glTxn.bucketType = bucketType;
		
		return glTxn;
	}

	/**
	 * @see 方法名：clonePlan 
	 * @see 描述：克隆账户对应所有计划
	 * @see 创建日期：2015-6-24下午7:08:56
	 * @author ChengChun
	 *  
	 * @param plans
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public List<CcsPlan> clonePlan(List<CcsPlan> plans){
		
		List<CcsPlan> prePlans = new ArrayList<CcsPlan>();
		for (CcsPlan plan : plans){
			CcsPlan prePlan = new CcsPlan();
			prePlan.updateFromMap(plan.convertToMap());
			prePlans.add(prePlan);
		}
		return prePlans;
	}
	
	/**
	 * @see 方法名：calcQualGraceBal 
	 * @see 描述：计算账单所有应还款额
	 * @see 创建日期：2015-6-24下午7:09:09
	 * @author ChengChun
	 *  
	 * @param plans
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BigDecimal calcQualGraceBal(List<CcsPlan> plans) {
		// 应还款额
		BigDecimal graceBal = BigDecimal.ZERO;

		// 循环所有的Plan
		for (CcsPlan plan : plans) {
			// 获取Plan参数
			PlanTemplate planTemplate = parameterFacility.retrieveParameterObject(plan.getPlanNbr(), PlanTemplate.class);

			// 循环所有余额成分
			// 判断余额成分对应的是否计入全额应还款金额参数为true的余额成分计入全额还款金额
			for (BucketType bucketType : BucketType.values()) {
				if(planTemplate.intParameterBuckets.get(bucketType) == null) continue;
				Boolean b = planTemplate.intParameterBuckets.get(bucketType).graceQualify;
				if (b==null?false:b) {
					graceBal = graceBal.add(bnpManager.getBucketAmount(plan, bucketType, BnpPeriod.PAST))
										.add(bnpManager.getBucketAmount(plan, bucketType, BnpPeriod.CTD));
				}
			}
		}

		if (graceBal.compareTo(BigDecimal.ZERO) < 0){
			graceBal = BigDecimal.ZERO;
		}
		return graceBal;
	}
	
	
	/**
	 * @see 方法名：computeFeeAmount 
	 * @see 描述：分段比例计算金额公用方法
	 * @see 创建日期：2015-6-24下午7:13:07
	 * @author ChengChun
	 *  
	 * @param tierInd
	 * @param chargeRates
	 * @param calcAmount
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BigDecimal getFeeAmount(TierInd tierInd, List<RateDef> chargeRates, BigDecimal calcAmount){

		// 检测输入参数是否为空
		if (tierInd ==null || chargeRates == null || calcAmount == null){
			throw new IllegalArgumentException("输入的参数为null，无法处理");
		}

		// 检测多级费率的最大金额是否按照从小到大排列
		BigDecimal preRateCeil = BigDecimal.ZERO;
		for (RateDef rateDef : chargeRates)
		{
			if (rateDef.rateCeil.compareTo(preRateCeil) < 0)
			{
				throw new IllegalArgumentException("参数中最大值列表并未按照从小到大排序");
			}
			preRateCeil = rateDef.rateCeil;	
		}
		
		// 开始计算
		BigDecimal feeAmount = BigDecimal.ZERO;
		
		// 根据分段计费类型进行不同的计算
		switch(tierInd){
		case F: 
			// 使用全部金额作为计算金额
			for (int i = 0 ; i < chargeRates.size(); i ++){
				if (logger.isDebugEnabled()) {
					logger.debug("多级费率:rateCeils-"+i+"["+chargeRates.get(i).rateCeil
							+"],rates-"+i+"["+chargeRates.get(i).rate
							+"]");
				}
				if (calcAmount.compareTo(chargeRates.get(i).rateCeil) <= 0){
					if (logger.isDebugEnabled()) {
						logger.debug("多级费率:rateCeils-"+i+"["+chargeRates.get(i).rateCeil
								+"],rates-"+i+"["+chargeRates.get(i).rate
								+"]");
					}
					feeAmount = calcAmount.multiply(chargeRates.get(i).rate);
					// 如果存在基准金额，则附加基准金额
					if (chargeRates.get(i).rateBase != null){
						feeAmount = feeAmount.add(chargeRates.get(i).rateBase);
					}
					return feeAmount;
				}
			}
			// 如果计算基础金额大于参数中配置的最大交易金额，则抛出异常
			throw new IllegalArgumentException("实际交易金额大于参数配置的最大交易金额");
		case T: 
			// 采用分段金额作为计算金额
			// 尚未计算金额
			BigDecimal current = calcAmount;

			for (int i = 0 ; (i < chargeRates.size()) && (current.signum() == 1) ; i ++)
			{
				BigDecimal minus = chargeRates.get(i).rateCeil.compareTo(current) > 0 ? current : chargeRates.get(i).rateCeil;
				feeAmount = feeAmount.add(minus.multiply(chargeRates.get(i).rate));
				current = current.subtract(minus);
				if (chargeRates.get(i).rateBase != null){
					feeAmount = feeAmount.add(chargeRates.get(i).rateBase);
				}
			}
			if (current.compareTo(BigDecimal.ZERO) > 0){
				throw new IllegalArgumentException("实际交易金额大于参数配置的最大交易金额");
			}
			return feeAmount;
		default: throw new IllegalArgumentException("无法处理的分段计费类型 tierInd:[" + tierInd.toString() + "]");
		}
	}
	
	/**
	 * @see 方法名：isWaive 
	 * @see 描述：是否免除费用
	 * @see 创建日期：2015-6-24下午7:13:31
	 * @author ChengChun
	 *  
	 * @param waiveInd
	 * @param acqBranchId
	 * @param owningBranch
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public boolean isWaive(WaiveInd waiveInd, String acqBranchId, String owningBranch) {
		switch (waiveInd) {
		case A:
			return true;
		case N:
			return false;
		case O:
			if(acqBranchId.equals(owningBranch)){
				return true;
			}else{
				return false;
			}
		default:
			throw new IllegalArgumentException("无效的收费标识："+ waiveInd);
		}
	}
	

	/**
	 * @see 方法名：fixWaiveFee 
	 * @see 描述：费用修正
	 * @see 创建日期：2015-6-24下午7:13:57
	 * @author ChengChun
	 *  
	 * @param waiveFee
	 * @param minWaiveFee
	 * @param maxWaiveFee
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BigDecimal fixWaiveFee(BigDecimal waiveFee, BigDecimal minWaiveFee, BigDecimal maxWaiveFee) {
		if(waiveFee.compareTo(maxWaiveFee) > 0){
			return maxWaiveFee;
		}else if(waiveFee.compareTo(minWaiveFee) < 0){
			return minWaiveFee;
		}else{
			return waiveFee.setScale(2, BigDecimal.ROUND_HALF_UP);
		}
	}
	
}
