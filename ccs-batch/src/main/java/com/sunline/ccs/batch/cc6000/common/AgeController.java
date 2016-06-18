package com.sunline.ccs.batch.cc6000.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.cc6000.S6000AcctInfo;
import com.sunline.ccs.batch.common.EnumUtils;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.facility.Card2ProdctAcctFacility;
import com.sunline.ccs.facility.DateUtils;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.Organization;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.BnpPeriod;
import com.sunline.ccs.param.def.enums.PaymentCalcMethod;
import com.sunline.pcm.param.def.Mulct;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.PlanType;


/**
 * @see 类名：ComputeDueAndAgeCode
 * @see 描述： 拖欠处理：最小还款额(DUE)、计算账龄
 *
 * @see 创建日期：   2015-6-24下午7:23:12
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class AgeController {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private static final String AGE_CD = "0123456789";
	private static final String AGE_CD_ERROR = "账龄参数不正确";
	private static final String AGE_CD_OVERPMT = "C";
	private static final String AGE_CD_0 = "0";
	private static final String AGE_CD_5 = "5";

	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private BnpManager bnpManager;
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	@Autowired
	private MicroCreditRescheduleUtils rescheduleUtils;
	@Autowired
	private Card2ProdctAcctFacility productFacility;
	@Autowired
    private BatchStatusFacility batchFacility;
	
	
	/**
	 * @see 方法名：moveDueAtStmtDay 
	 * @see 描述：最小还款额移位
	 * @see 创建日期：2015-6-24下午7:24:35
	 * @author ChengChun
	 *  
	 * @param account
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void moveDueAtStmtDay(CcsAcct account){
		
		//	账龄9的当前最小还款额
		BigDecimal minDueAtAge = getMinDueByAgeCd(account, '9');
			
		//	上期ageCd
		char preAgeCd = '9';
		
		//	处理账龄提升1
		for (char c : "87654321".toCharArray()){
			minDueAtAge = minDueAtAge.add(getMinDueByAgeCd(account, c));
			setMinDueByAgeCd(account, preAgeCd, minDueAtAge);
			minDueAtAge = BigDecimal.ZERO;
			preAgeCd = c;
		}
		
		// 设置当期最小还款额为0
		setMinDueByAgeCd(account, '1', BigDecimal.ZERO);
	}
	
	/**
	 * @see 方法名：getMinDue
	 * @see 描述：计算当期最小还款额,账单日处理
	 * @see 创建日期：2015-6-24下午7:24:48
	 * @author ChengChun
	 *  
	 * @param accountInfo
	 * @param batchDate
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void getMinDue(S6000AcctInfo accountInfo, Date batchDate) {
		logger.info("Starting computing Min Due for account [{}], account type is [{}], batch date is [{}]"
				, accountInfo.getAccount().getAcctNbr()
				, accountInfo.getAccount().getAcctType()
				, batchDate.toString());

		//	账户信息
		CcsAcct account = accountInfo.getAccount();
		
		// 最小还款额
		BigDecimal minDue;
		
		//	根据锁定码上的要求全额还款指示进行最小还款额计算
		PaymentCalcMethod pcm = blockCodeUtils.getMergedPaymentInd(account.getBlockCode()); 
		switch(pcm){
		case N :
			// 计算常规最小还款额, 超限部分在账户层面不计入最小还款额,但在出账单时会计入账单上的最小还款额
			minDue = getCommonMinDue(accountInfo.getPlans()); 
			break;
		case B :
			//	最小还款额 = 欠款余额
			minDue = account.getCurrBal();
			break;
		default :
			throw new IllegalArgumentException("无法处理锁定码给出的还款指示！ PaymentCalcMethod:[" + pcm + "]");
		}

		//	设置最小还款额的小数位数
		minDue = minDue.setScale(2, RoundingMode.HALF_UP);
		
		//  若，最小还款额+所有拖欠金额+转出计划余额>账户余额，则，最小还款额 = 账户余额-转出计划余额-所有拖欠金额
		BigDecimal planOBal = BigDecimal.ZERO;
		for(CcsPlan p : accountInfo.getPlans()){
			if(p.getPlanType() == PlanType.O){
				planOBal = planOBal.add(p.getCurrBal());
			}
		}
		if (minDue.add(account.getTotDueAmt()).add(planOBal).compareTo(account.getCurrBal()) > 0){
			minDue = account.getCurrBal().subtract(planOBal).subtract(account.getTotDueAmt());
		}
		
		//	如果最小还款额小于0，则最小还款额 =0
		if (minDue.compareTo(BigDecimal.ZERO) < 0){
			minDue = BigDecimal.ZERO;
		}
		
		// 更新最小还款额
		setMinDueByAgeCd(account, '1', minDue.setScale(2, RoundingMode.HALF_UP));
		
		//	最小还款额合计
		BigDecimal totDueAmt = BigDecimal.ZERO;
						
		//	循环累加所有账龄对应的最小还款额
		for (char c : "987654321".toCharArray()){
			totDueAmt = totDueAmt.add(getMinDueByAgeCd(account, c));
		}
		
		BigDecimal preTotDueAmt = totDueAmt;
		
		// 账户属性参数
		ProductCredit productCredit = parameterFacility.loadParameter(account.getProductCd(), ProductCredit.class);
		AccountAttribute acctAttr = parameterFacility.loadParameter(productCredit.accountAttributeId, AccountAttribute.class);
		//全部最小还款额小于允许的最小还款额
		if(acctAttr.allowMinDueAmt !=null && totDueAmt.compareTo(acctAttr.allowMinDueAmt) <0 ){
			//账户欠款小于等于允许的最小还款额
			if(account.getCurrBal().compareTo(acctAttr.allowMinDueAmt) < 0){
				totDueAmt = account.getCurrBal().setScale(2, RoundingMode.HALF_UP);
			}else{
				totDueAmt = acctAttr.allowMinDueAmt.setScale(2, RoundingMode.HALF_UP);
			}
		}
		if(preTotDueAmt.compareTo(totDueAmt) < 0){
			//把新增的最小还款加到当期的最小还款上
			// 更新最小还款额
			setMinDueByAgeCd(account, '1', minDue.add(totDueAmt.subtract(preTotDueAmt)).setScale(2, RoundingMode.HALF_UP));
		}
		//	更新账户上的最小还款额合计
		account.setTotDueAmt(totDueAmt);
		
	}
	
	/**
	 * @see 方法名：getCommonMinDue 
	 * @see 描述：计算常规最小还款额
	 * @see 创建日期：2015-6-24下午7:25:36
	 * @author ChengChun
	 *  
	 * @param plans
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BigDecimal getCommonMinDue(List<CcsPlan> plans){
		// 最小还款额
		BigDecimal minDue = BigDecimal.ZERO;
		
		for (CcsPlan plan : plans){
			if(EnumUtils.in(plan.getPlanType(), PlanType.P,PlanType.J)){
				continue;
			}
			PlanTemplate planTemplate = parameterFacility.loadParameter(plan.getPlanNbr(),PlanTemplate.class);
			for (BucketType bucketType : BucketType.values()){
				for (BnpPeriod bnpPeriod : BnpPeriod.values()){
					// 累加往期余额最小还款额
					BigDecimal bal = bnpManager.getBucketAmount(plan, bucketType, bnpPeriod);
					if(planTemplate.intParameterBuckets.get(bucketType)!=null){
						Map<BnpPeriod, BigDecimal> m = planTemplate.intParameterBuckets.get(bucketType).minPaymentRates;
						if(m!=null && m.get(bnpPeriod)!=null){
							minDue = minDue.add(bal.multiply(m.get(bnpPeriod)));
						}
					}
				}
			}
		}
		
		return minDue;
	}
	/**
	 * 获取计划的最小还款额
	 * @param plan
	 * @return
	 */
	public BigDecimal getCommonMinDue(CcsPlan plan){
		// 最小还款额
		BigDecimal minDue = BigDecimal.ZERO;
		PlanTemplate planTemplate = parameterFacility.loadParameter(plan.getPlanNbr(),PlanTemplate.class);
		for (BucketType bucketType : BucketType.values()){
			for (BnpPeriod bnpPeriod : BnpPeriod.values()){
				// 累加往期余额最小还款额
				BigDecimal bal = bnpManager.getBucketAmount(plan, bucketType, bnpPeriod);
				if(planTemplate.intParameterBuckets.get(bucketType)!=null){
					Map<BnpPeriod, BigDecimal> m = planTemplate.intParameterBuckets.get(bucketType).minPaymentRates;
					if(m!=null && m.get(bnpPeriod)!=null){
						minDue = minDue.add(bal.multiply(m.get(bnpPeriod)));
					}
				}
			}
		}
		
		return minDue;
	}

	/**
	 * @see 方法名：setAgeCode
	 * @see 描述：账龄计算
                账龄增加（账单日处理）
                账龄降低（还款交易、贷记调整、退货）
	 * @see 创建日期：2015-6-24下午7:28:11
	 * @author ChengChun
	 *  
	 * @param account
	 * @param currDate
	 * @param allowPromote 是否允许提升账龄
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void setAgeCode(CcsAcct account, Date currDate,boolean allowPromote) {
		if (logger.isDebugEnabled()) {
			logger.debug("账龄计算-前:BlockCode["+account.getBlockCode()
					+"],AgeCd["+account.getAgeCode()
					+"],GlAgeCd["+account.getAgeCodeGl()
					+"]");
		}
		
		// TODO 目前只实现账龄拖欠金额阈值控制，拖欠百分比控制暂不实现
		// 获取账户参数，取得账户参数中账龄提升最小阈值
		
		// 从最高账期累加最小还款额
		BigDecimal totDue = BigDecimal.ZERO;
		
		// 获得组织机构参数
		Organization org = parameterFacility.loadParameter(null, Organization.class);
		
		// 账户属性参数
		ProductCredit productCredit = parameterFacility.loadParameter(account.getProductCd(), ProductCredit.class);
		AccountAttribute acctAttr = parameterFacility.loadParameter(productCredit.accountAttributeId, AccountAttribute.class);
		
		// 原账龄
		String preAgeCd = account.getAgeCode();
		
		// 新账龄
		String newAgeCd = null;
		
		// 账龄列表索引号
		for (char c : "987654321".toCharArray()){
			// 累加该账期未还款最小还款额
			totDue = totDue.add(getMinDueByAgeCd(account, c));
			// 判断已累加未还款最小还款额是否大于账龄阈值
			if (totDue.compareTo(acctAttr.delqTol) > 0 ){
				newAgeCd = String.valueOf(c);
				break;
			}
		}
	
		// 如果新账龄仍为null,则根据当前余额的数值判断账龄代码
		if (newAgeCd == null){
			switch (account.getCurrBal().compareTo(BigDecimal.ZERO)){
			case -1 : 
				// 有溢缴款
				newAgeCd = AGE_CD_OVERPMT; break;
			case 0 :
				// 当前未欠款 newAgeCd为age0
			case 1 :
				// 当前有欠款
				newAgeCd = AGE_CD_0; break;
			}
		}
		
		// 判断新旧账龄是否相同
		if (!preAgeCd.equals(newAgeCd)){
			if(!allowPromote && isPromote(newAgeCd,preAgeCd)){
				//新账龄对于旧账龄是提升，并且不允许提升，则不更新账龄
			}else{
				// 设置新账龄
				account.setAgeCode(newAgeCd);
				
				//	设置最后账龄提升日期
				if (isPromote(newAgeCd,preAgeCd)){
					account.setLastAgingDate(currDate);	
				}
				
				String newBlockCode = account.getBlockCode();
				//	判断原账龄是否是拖欠，如果是则remove掉原先的锁定码
				if (StringUtils.contains("123456789", preAgeCd))
					newBlockCode = blockCodeUtils.removeBlockCode(newBlockCode, preAgeCd);
				
				//	判断新账龄是否需要上锁定码，如果需要上，则增加锁定码
				if (StringUtils.contains("123456789", newAgeCd))
					newBlockCode = blockCodeUtils.addBlockCode(newBlockCode, newAgeCd);
				
				account.setBlockCode(newBlockCode);
			}
		}
		
		// 更新送入总账的账龄
		// 送总账账龄与账龄一致标志
		Boolean glAgeCdFlag = org.glAgeCdConsistentWithAgeCd == null ? false : org.glAgeCdConsistentWithAgeCd;
		if (AGE_CD_OVERPMT.equals(account.getAgeCode()) 
				|| account.getAgeCodeGl() == null 
				|| AGE_CD_OVERPMT.equals(account.getAgeCodeGl())) {
			account.setAgeCodeGl(account.getAgeCode());
		} 
		// 账龄减少
		else if (account.getAgeCode().compareTo(account.getAgeCodeGl()) < 0) {
			// 送总账的账龄大于等于“5”，且账户账龄小于等于“5”，且当前余额大于“0”
			if (AGE_CD_5.compareTo(account.getAgeCodeGl()) <= 0 && AGE_CD_5.compareTo(account.getAgeCode()) > 0 
					&& account.getCurrBal().compareTo(BigDecimal.ZERO) > 0 && !glAgeCdFlag) {
				account.setAgeCodeGl(AGE_CD_5);
			} else {
				account.setAgeCodeGl(account.getAgeCode());
			}
		} 
		// 账龄增加
		else {
			account.setAgeCodeGl(account.getAgeCode());
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("账龄计算-后:BlockCode["+account.getBlockCode()
					+"],AgeCd["+account.getAgeCode()
					+"],GlAgeCd["+account.getAgeCodeGl()
					+"]");
		}
	}
	/**
	 * 判断账龄是否提升
	 * @param newAgeCd
	 * @param preAgeCd
	 * @return
	 */
	public boolean isPromote(String newAgeCd,String preAgeCd){
		return !newAgeCd.equals(AGE_CD_OVERPMT) && !newAgeCd.equals(AGE_CD_0) && AGE_CD.indexOf(newAgeCd) > AGE_CD.indexOf(preAgeCd);
	}
	
	/**
	 * @see 方法名：setAgeCdHst 
	 * @see 描述：循环记录账龄历史信息
	 * @see 创建日期：2015-6-24下午7:28:47
	 * @author ChengChun
	 *  
	 * @param account
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void setAgeCdHst(CcsAcct account){
		// 判断ageHst当前状态，是null/小于24个月/等于24个月
		if (account.getAgeHst() == null){
			account.setAgeHst(account.getAgeCode());
		}	else if (account.getAgeHst().length() < 24){
			account.setAgeHst(account.getAgeCode() + account.getAgeHst());
		}	else{
			account.setAgeHst(account.getAgeCode() + account.getAgeHst().substring(0, 23));
		}
	}
	
	/**
	 * @see 方法名：offsetMinDue 
	 * @see 描述：还款冲销最小还款额
	 * @see 创建日期：2015-6-24下午7:30:30
	 * @author ChengChun
	 *  
	 * @param account
	 * @param amount
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void offsetMinDue(CcsAcct account, BigDecimal amount){
		if (logger.isDebugEnabled()) {
			logger.debug("最小还款额计算-前:TotDueAmt["+account.getTotDueAmt()
					+"],CurrDueAmt["+account.getCurrDueAmt()
					+"],PastDueAmt1["+account.getPastDueAmt1()
					+"],PastDueAmt2["+account.getPastDueAmt2()
					+"],PastDueAmt3["+account.getPastDueAmt3()
					+"],PastDueAmt4["+account.getPastDueAmt4()
					+"],PastDueAmt5["+account.getPastDueAmt5()
					+"],PastDueAmt6["+account.getPastDueAmt6()
					+"],PastDueAmt7["+account.getPastDueAmt7()
					+"],PastDueAmt8["+account.getPastDueAmt8()
					+"],AgeCd["+account.getAgeCode()
					+"]");
		}
		// 账龄列表索引号
		int i = AGE_CD.length() - 1;
		
		// 未冲销还款金额
		BigDecimal leftAmount = amount;
		
		// 未冲销还款金额>0 and 尚有账龄未进行冲销时循环
		while (i >= 0 && leftAmount.compareTo(BigDecimal.ZERO) > 0){
			// 判断该账龄对应的未还款额是否>0
			if (getMinDueByAgeCd(account, AGE_CD.charAt(i)).compareTo(BigDecimal.ZERO) > 0 )
			{
				// 判断未还款额是否大于还款金额
				if (getMinDueByAgeCd(account, AGE_CD.charAt(i)).compareTo(leftAmount) > 0)
				{
					// 未还款额大于还款金额
					// 未还款金额 = 未还款金额 - 还款金额
					setMinDueByAgeCd(account, AGE_CD.charAt(i), 
							getMinDueByAgeCd(account, AGE_CD.charAt(i))
								.subtract(leftAmount));
					
					// 还款金额 = 0
					leftAmount = BigDecimal.ZERO;
				}	else{
					// 未还款额小于等于还款金额
					// 还款金额 = 还款金额 - 未还款金额
					leftAmount = leftAmount.subtract(getMinDueByAgeCd(account, AGE_CD.charAt(i)));
					
					// 未还款金额 = 0 
					setMinDueByAgeCd(account, AGE_CD.charAt(i), BigDecimal.ZERO);
				}
			}
			i--;
		}
		
		// 更新账户上的最小还款额合计
		if (leftAmount.compareTo(BigDecimal.ZERO) > 0){
			account.setTotDueAmt(BigDecimal.ZERO);
		}	else {
			account.setTotDueAmt(account.getTotDueAmt().subtract(amount));
		}
		if (logger.isDebugEnabled()) {
			logger.debug("最小还款额计算-后:TotDueAmt["+account.getTotDueAmt()
					+"],CurrDueAmt["+account.getCurrDueAmt()
					+"],PastDueAmt1["+account.getPastDueAmt1()
					+"],PastDueAmt2["+account.getPastDueAmt2()
					+"],PastDueAmt3["+account.getPastDueAmt3()
					+"],PastDueAmt4["+account.getPastDueAmt4()
					+"],PastDueAmt5["+account.getPastDueAmt5()
					+"],PastDueAmt6["+account.getPastDueAmt6()
					+"],PastDueAmt7["+account.getPastDueAmt7()
					+"],PastDueAmt8["+account.getPastDueAmt8()
					+"],AgeCd["+account.getAgeCode()
					+"]");
		}
	}
	
	/**
	 * @see 方法名：getMinDueByAgeCd 
	 * @see 描述：根据账龄获取对应的最小未还款额
	 * @see 创建日期：2015-6-24下午7:30:42
	 * @author ChengChun
	 *  
	 * @param account
	 * @param ageCd
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BigDecimal getMinDueByAgeCd(CcsAcct account, Character ageCd){
		switch (ageCd){
		case 'C':
			return BigDecimal.ZERO;
		case '0':
			return BigDecimal.ZERO;
		case '1':
			return account.getCurrDueAmt();
		case '2':
			return account.getPastDueAmt1();
		case '3':
			return account.getPastDueAmt2();
		case '4':
			return account.getPastDueAmt3();
		case '5':
			return account.getPastDueAmt4();
		case '6':
			return account.getPastDueAmt5();
		case '7':
			return account.getPastDueAmt6();
		case '8':
			return account.getPastDueAmt7();
		case '9':
			return account.getPastDueAmt8();
		default: throw new IllegalArgumentException(AGE_CD_ERROR);
		}
	}
	
	/**
	 * @see 方法名：setMinDueByAgeCd 
	 * @see 描述：根据账龄更新对应的最小未还款额
	 * @see 创建日期：2015-6-24下午7:30:58
	 * @author ChengChun
	 *  
	 * @param account
	 * @param ageCd
	 * @param minDue
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private void setMinDueByAgeCd(CcsAcct account, Character ageCd, BigDecimal minDue)
	{
		switch (ageCd){
		case 'C':
			break;
		case '0':
			break; 
		case '1':
			account.setCurrDueAmt(minDue); break;
		case '2':
			account.setPastDueAmt1(minDue); break;
		case '3':
			account.setPastDueAmt2(minDue); break;
		case '4':
			account.setPastDueAmt3(minDue); break;
		case '5':
			account.setPastDueAmt4(minDue); break;
		case '6':
			account.setPastDueAmt5(minDue); break;
		case '7':
			account.setPastDueAmt6(minDue); break;
		case '8':
			account.setPastDueAmt7(minDue); break;
		case '9':
			account.setPastDueAmt8(minDue); break;
		default: throw new IllegalArgumentException("账龄参数不正确");
		}
	}

	/**
	 * @see 方法名：updateMinDueByCrAdj 
	 * @see 描述：账单分期贷调时，只对账户当期最小还款额造成修改，不对往期做修改
	 *          如果分期后余额小于当期最小还款额，当期最小还款额=余额，全部最小还款额随之更新，否则，最小还款额不做调整
	 * @see 创建日期：2015-6-24下午7:31:17
	 * @author ChengChun
	 *  
	 * @param account
	 * @param txnPost
	 * @param batchDate
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void updateMinDueByCrAdj(CcsAcct account, CcsPostingTmp txnPost,Date batchDate) {
		BigDecimal bal = account.getQualGraceBal().subtract(txnPost.getPostAmt());
		BigDecimal x = bal.subtract(account.getCurrDueAmt());
		if(x.compareTo(BigDecimal.ZERO)<0){//分期后余额小于当期最小还款额
			account.setCurrDueAmt(bal);
			account.setTotDueAmt(account.getTotDueAmt().subtract(x.abs()));
		}
	}
	/**
	 * 根据loan和plan 更新loan中的逾期起始日期
	 * @param loan
	 * @param plans
	 * @param allowPromote 是否允许提升dpd和cpd
	 */
	public void updateOverdueDate(List<CcsLoan> loans, List<CcsPlan> plans,boolean allowPromote,CcsAcct acct){
		
		for(CcsLoan loan : loans){
			logger.debug("更新CPD和DPD开始,允许提升标志：["+allowPromote+"],loanid:["+loan.getLoanId()+"],loantype:["+loan.getLoanType()+"],原DPD:["+
					loan.getOverdueDate()+"],原CPD:["+loan.getCpdBeginDate()+"]");
			// DPD/CPD容差
			LoanFeeDef loanFeeDef = rescheduleUtils.getLoanFeeDef(loan.getLoanCode(), loan.getLoanInitTerm(), 
					loan.getLoanInitPrin(),loan.getLoanFeeDefId());
//			Mulct mulct = parameterFacility.loadParameter(loanFeeDef.mulctTableId, Mulct.class);
			BigDecimal cpdToleLmt = loanFeeDef.cpdToleLmt==null?BigDecimal.ZERO:loanFeeDef.cpdToleLmt;
			BigDecimal dpdToleLmt = loanFeeDef.dpdToleLmt==null?BigDecimal.ZERO:loanFeeDef.dpdToleLmt;
			AccountAttribute acctAttr = productFacility.CardNoTOAccountAttribute(loan.getCardNbr(), loan.getAcctType().getCurrencyCode());
			//DPD更新
			if(!allowPromote && loan.getOverdueDate() == null){
				//贷款不逾期，有不允许提升DPD，则不更新DPD
			}else{
				//DPD的日期不会向前滚
				Date earlest = this.updateLoanDPD(loan, plans, dpdToleLmt,acctAttr,acct);
				loan.setOverdueDate(earlest);
			}
			//CPD更新
			if(!allowPromote && loan.getCpdBeginDate() == null){
				//贷款不逾期，有不允许提升CPD，则不更新CPD
			}else{
				Date cpdEarlest = this.updateLoanCPD(loan, plans, cpdToleLmt,acctAttr,acct);
				//新的日期早于当期的cpd，新的日期为空，直接设置
				if(cpdEarlest !=null ){
					//如果原始的日期为空，或者新的日期早于原有日期
					if(loan.getCpdBeginDate()==null || (loan.getCpdBeginDate() != null && cpdEarlest.compareTo(loan.getCpdBeginDate()) <= 0)){
						//允许提升才修改
						if(allowPromote){
							loan.setCpdBeginDate(cpdEarlest);
						}
					}
					
				}else{
					loan.setCpdBeginDate(cpdEarlest);
				}
				
			}
			logger.debug("更新CPD和DPD,允许提升标志：["+allowPromote+"],loanid:["+loan.getLoanId()+"],loantype:["+loan.getLoanType()+"],新DPD:["+
					loan.getOverdueDate()+"],新CPD:["+loan.getCpdBeginDate()+"]");
		}
	}
	
	private Date updateLoanDPD(CcsLoan loan,List<CcsPlan> plans,BigDecimal dpdToleLmt,AccountAttribute acctAttr,CcsAcct acct){
		//如果是随借随还
		if(loan.getLoanType() == LoanType.MCAT){
			logger.debug("更新DPD,loantype:["+loan.getLoanType()+"],agecode:["+acct.getAgeCode()+"]");
			if(acct.getAgeCode().equals("C") || acct.getAgeCode().equals("0")){
				return null;
			}else{
				return rescheduleUtils.getNextPaymentDay(acct.getProductCd(), this.getCycleDate(acct.getNextStmtDate(), acct.getCycleDay(), acct.getAgeCode()));
			}
		}
		
		Date earlest = null;//拖欠最早期数
		for(CcsPlan plan : plans){
			//如果信用计划的新建日期小于等于贷款的激活日期，跳过此plan
			if(plan.getPlanAddDate().compareTo(loan.getActiveDate())<=0){
				continue;
			}
			//建立日期是账单日才做更新
//			if(!this.isCycleDate(plan.getPlanAddDate(),acct.getCycleDay())){
//				continue;
//			}
			if(bnpManager.getPastDueSum(plan).compareTo(BigDecimal.ZERO) <= 0){
				continue;
			}
			int term = plan.getTerm()==null?0:plan.getTerm();
			if(loan.getRefNbr().equals(plan.getRefNbr()) && loan.getCurrTerm()>=term){
				//转入计划的期数在贷款当前期数内，并且是转入计划
				if(plan.getPlanType().isXfrIn()){
					BigDecimal mulctAmt = BigDecimal.ZERO;
					//最新的dpd规则是不算罚金
					mulctAmt=plan.getCtdMulctAmt().add(plan.getPastMulctAmt());
					BigDecimal penaltyAmt = BigDecimal.ZERO;
					//最新的dpd规则是不算罚xi
					penaltyAmt=plan.getCtdPenalty().add(plan.getPastPenalty());
					//最新的dpd规则是不算复利
					penaltyAmt=penaltyAmt.add(plan.getCtdCompound().add(plan.getPastCompound()));
					//最新的dpd规则是不算滞纳金
					penaltyAmt=penaltyAmt.add(plan.getCtdLateFee().add(plan.getPastLateFee()));
					//不计算代收罚金、代收滞纳金、代收罚息（DPD规则只关注期款是否偿还）
					penaltyAmt=penaltyAmt.add(plan.getCtdReplaceMulct().add(plan.getPastReplaceMulct())
							.add(plan.getCtdReplacePenalty().add(plan.getPastReplacePenalty()))
							.add(plan.getCtdReplaceLateFee().add(plan.getPastReplaceLateFee())));
					if((plan.getCurrBal().subtract(penaltyAmt.add(mulctAmt))).compareTo(dpdToleLmt)>0){
						Date pmtDueDate = rescheduleUtils.getNextPaymentDay(acct.getProductCd(), plan.getPlanAddDate());
						if(earlest == null){
							earlest = pmtDueDate;
						}else{
							if(pmtDueDate.before(earlest)){
								earlest = pmtDueDate;
							}
						}
					}
				}
			}
		}
		return earlest;
	}
	private Date updateLoanCPD(CcsLoan loan,List<CcsPlan> plans,BigDecimal cpdToleLmt,AccountAttribute acctAttr,CcsAcct acct){
		//如果是随借随还
		if(loan.getLoanType() == LoanType.MCAT){
			logger.debug("更新CPD,loantype:["+loan.getLoanType()+"],agecode:["+acct.getAgeCode()+"]");
			if(acct.getAgeCode().equals("C") || acct.getAgeCode().equals("0")){
				return null;
			}else{
				Date newCPDDate = rescheduleUtils.getNextPaymentDay(acct.getProductCd(), this.getCycleDate(acct.getNextStmtDate(), acct.getCycleDay(), acct.getAgeCode()));
				if(newCPDDate.compareTo(batchFacility.getBatchDate())>0){
					return null;
				}
				return newCPDDate;
			}
		}
				
		Date cpdEarlest = loan.getCpdBeginDate(); //cpd起始日期
		boolean overDue = false;//是否有plan满足cpd条件
		for(CcsPlan plan : plans){
			//如果信用计划的新建日期小于等于贷款的激活日期，跳过此plan
			if(plan.getPlanAddDate().compareTo(loan.getActiveDate())<=0){
				continue;
			}
			//建立日期是账单日才做更新
			//这么考虑并不严谨，如果是提前结清未还款，逾期日期就会统计不到，现在不会出现这种状况，先这么处理
//			if(!this.isCycleDate(plan.getPlanAddDate(),acct.getCycleDay())){
//				continue;
//			}
			
			// 最新规则，CPD计算时需要考虑当期违约金（罚金、罚息、滞纳金）
			BigDecimal fine = BigDecimal.ZERO;
			fine = fine.add(plan.getCtdMulctAmt()).add(plan.getCtdPenalty()).add(plan.getCtdLateFee());
			fine = fine.add(plan.getCtdReplaceMulct()).add(plan.getCtdReplacePenalty()).add(plan.getCtdReplaceLateFee());
			BigDecimal cpdCalSum = BigDecimal.ZERO;
			cpdCalSum = bnpManager.getPastDueSum(plan).add(fine);
			if(cpdCalSum.compareTo(BigDecimal.ZERO) <= 0){
				continue;
			}
			int term = plan.getTerm()==null?0:plan.getTerm();
			// 判断是否符合cpd条件
			if(!loan.getRefNbr().equals(plan.getRefNbr()) // refnbr不符
					|| term > loan.getCurrTerm() // 未到期
					|| !plan.getPlanType().isXfrIn() // 非转入计划
					|| plan.getCurrBal().compareTo(cpdToleLmt)<=0) // 当前余额<=容差
				continue;
			overDue = true;//cpd逾期
			Date pmtDueDate = rescheduleUtils.getNextPaymentDay(acct.getProductCd(), plan.getPlanAddDate());
			
			if(cpdEarlest==null){
				cpdEarlest = pmtDueDate;
			}else{
				cpdEarlest = pmtDueDate.before(cpdEarlest) ? pmtDueDate : cpdEarlest;
			}
		}
		return overDue?cpdEarlest:null;
	}
	/**
	 * 判断是否是账单日
	 * @param date
	 * @param cycleDate
	 * @return
	 */
	public boolean isCycleDate(Date date,String cycleDate){
		Calendar calendar = Calendar.getInstance(); 
		calendar.setTime(date); 
		int cycleInt = Integer.parseInt(cycleDate);
		int dateDays = calendar.get(Calendar.DAY_OF_MONTH);
		if(cycleInt > dateDays){
			///一月的最后一天
			int days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH); 
			if(days==dateDays){
				return true;
			}else{
				return false;
			}
		}else if(cycleInt == dateDays){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * 根据下一账单日和账龄计算，对应的账单日
	 * @param date
	 * @param cycleDate
	 * @param ageCode
	 * @return
	 */
	public Date getCycleDate(Date date,String cycleDate,String ageCode){
		int cycleint = Integer.parseInt(cycleDate);
		int age = Integer.parseInt(ageCode);
		Calendar calendar = Calendar.getInstance(); 
		calendar.setTime(date); 
		// 下个账账单日直接XFRIN
		calendar.add(Calendar.MONTH, -age); 
		if(cycleint > calendar.get(Calendar.DAY_OF_MONTH)){
			///如果下一个账单日的日期在date范围之内,下一个还款日特殊处理未下一月的最后一天
			int days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH); 
			if(cycleint <=days){
				calendar.set(Calendar.DAY_OF_MONTH,cycleint);
			}
			if(cycleint > days){
				calendar.set(Calendar.DAY_OF_MONTH,days); 
			}
		}
		return calendar.getTime();
	}
	/**
	 * 判断并更新CPD和DPD的最大值和最大值日期
	 * @param loan
	 * @param newDate
	 * @return
	 */
	public CcsLoan updateMaxOverdueDate(CcsLoan loan){
		
		if(loan.getOverdueDate() == null){
			//不更新
		}else{
			int days = 0;
			int maxDPD = loan.getMaxDpd()==null?0:loan.getMaxDpd();
			days = DateUtils.getIntervalDays(loan.getOverdueDate(),batchFacility.getBatchDate());
			logger.debug("更新DPD最大值,原值=["+loan.getMaxDpd()+"],新值=["+days+"],批量日期=["+batchFacility.getBatchDate()+"]");
			
			if(days > maxDPD){
				loan.setMaxDpd(days);
				loan.setMaxDpdDate(batchFacility.getBatchDate());
			}
		}
		
		if(loan.getCpdBeginDate() == null){
			//不更新
		}else{
			int days = 0;
			int maxCPD = loan.getMaxCpd()==null?0:loan.getMaxCpd();
			days = DateUtils.getIntervalDays(loan.getCpdBeginDate(),batchFacility.getBatchDate());
			logger.debug("更新CPD最大值,原值=["+loan.getMaxCpd()+"],新值=["+days+"],批量日期=["+batchFacility.getBatchDate()+"]");
			
			if(days > maxCPD){
				loan.setMaxCpd(days);
				loan.setMaxCpdDate(batchFacility.getBatchDate());
			}
		}
		
		return loan;
	}

//	public static void main(String[] args) throws ParseException{
//		AgeController age = new AgeController();
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
//		String cycleDate= "30";
//		Date date = sdf.parse("2015-09-30");
//		String ageCode ="2";
//		System.out.println(age.isCycleDate(date, cycleDate));
//		System.out.println(sdf.format(age.getCycleDate(date, cycleDate, ageCode)));
//	}
}
