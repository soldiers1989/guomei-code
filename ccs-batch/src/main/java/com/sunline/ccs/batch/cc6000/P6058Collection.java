package com.sunline.ccs.batch.cc6000;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.cc6000.common.Calculator;
import com.sunline.ccs.batch.cc6000.common.AgeController;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.otb.CommProvide;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.CollectionReason;
import com.sunline.ppy.dictionary.enums.Direction;
import com.sunline.ppy.dictionary.exchange.CollectionItem;


/** 
 * @see 类名：P6058Collection
 * @see 描述：催收接口文件处理
 *
 * @see 创建日期：   2015年6月25日 下午2:33:51
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P6058Collection implements ItemProcessor<S6000AcctInfo, S6000AcctInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());
		
	private static final String AGE_CD_LIST = "C0123456789";

	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private AgeController computeDue;
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	@Autowired
	private CommProvide commonProvide;
	@Autowired
	private Calculator calculator;
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	@Override
	public S6000AcctInfo process(S6000AcctInfo item) {
		if (logger.isDebugEnabled()) {
			logger.debug("催收接口文件处理：Org["+item.getAccount().getOrg()
					+"],AcctNo["+item.getAccount().getAcctNbr()
					+"],AcctType["+item.getAccount().getAcctType()
					+"],BatchDate["+batchStatusFacility.getBatchDate()
					+"],CollectReason["+item.getAccount().getCollectReason()
					+"]");
		}
		
		Date batchDate = batchStatusFacility.getBatchDate();

		//	原锁定码
		CollectionReason preReason = item.getAccount().getCollectReason();

		//	新锁定码
		CollectionReason newReason = null;
			
		//	获取锁定码入催原因码
		CollectionReason blockCodeReason = checkBlockCode(item.getAccount());
		logger.info("锁定码入催原因码检查结果:[" + blockCodeReason + "]");
		
		//	获取首次还款拖欠入催原因码
		CollectionReason firstPaymentReason = checkFirstPayment(item.getAccount(), item.getPlans());
		logger.info("首次还款拖欠原因码检查结果:[" + firstPaymentReason + "]");
		
		//	获取账龄拖欠入催原因码
		CollectionReason ageCdReason = checkAgeCd(item.getAccount());
		logger.info("账龄拖欠入催原因码检查结果:[" + ageCdReason + "]");	
		
		//	获取超限入催原因码
		CollectionReason overLimitReason = checkOverLimit(item.getAccount(), item.getPlans());
		logger.info("超限入催原因码检查结果:[" + overLimitReason + "]");	
		
		//	设置新原因码为等级最高的原因码
		if (overLimitReason != null){
			newReason = overLimitReason;
		}
		
		if (ageCdReason != null){
			newReason = ageCdReason;
		}
		
		if (firstPaymentReason != null){
			newReason = firstPaymentReason;
		}
		
		if (blockCodeReason != null){
			newReason = blockCodeReason;
		}
		
		logger.info("最新入催原因码:[" + newReason + "]");	
		
		//	无入催原因码，直接返回
		if (preReason == null && newReason == null){
			return item;
		}
		
		//	判断是否是出催
		if (preReason != null && newReason == null){
			//	出催处理
			logger.info("生成出催记录");	
			createCollectionFile(item, null);
			item.getAccount().setCollectInDate(null);
			item.getAccount().setCollectOutDate(batchDate);
			item.getAccount().setCollectReason(null);
			item.getAccount().setCollector(null);
			return item;
		}	
		
		//	判断是否入催
		if (preReason == null && newReason != null){
			logger.info("生成入催升级记录");	
			createCollectionFile(item, newReason);
			item.getAccount().setCollectInDate(batchDate);
			item.getAccount().setCollectOutDate(null);
			item.getAccount().setCollectReason(newReason);
			item.getAccount().setCollectCnt(item.getAccount().getCollectCnt() + 1);
			return item;
		}
		
		//	催收原因码升降级
		logger.info("生成入催升级记录");	
		createCollectionFile(item, newReason);
		item.getAccount().setCollectReason(newReason);
		return item;
	}
	
	/**
	 * 锁定码入催检查
	 * @param item
	 * @return 锁定码催收原因码
	 */
	private CollectionReason checkBlockCode(CcsAcct acct){
		//	判断锁定码非空且锁定码催收指示需要入催时
		//	返回锁定码入催原因码
		if(acct.getBegBal() != null && blockCodeUtils.getMergedCollectionInd(acct.getBlockCode())){
			return CollectionReason.B0;
		}else{
			return null;
		}
	}
	
	/**
	 * 首次还款拖欠入催检查
	 * 此方法仅在宽限日至账单日之间进行入催原因码计算
	 * 主要是因为在账单日至最后还款日之间无法获得第一个有余额的账单日
	 * 造成无法得知是否已到第一个需要还款的宽限日
	 * @param item
	 * @return
	 */
	private CollectionReason checkFirstPayment(CcsAcct account, List<CcsPlan> plans){
		// 计算往期最小还款额累计
		BigDecimal minDues = BigDecimal.ZERO;
		// 账户属性参数
		ProductCredit productCredit = parameterFacility.loadParameter(account.getProductCd(), ProductCredit.class);
		AccountAttribute acctAttr = parameterFacility.loadParameter(productCredit.accountAttributeId, AccountAttribute.class);
		
		for (char c : "987654321".toCharArray()){
			minDues = minDues.add(computeDue.getMinDueByAgeCd(account, c));
		}
		
		//	判断当前日期是否在宽限日和下一账单日之间
		BigDecimal collPmt = commonProvide.getRemainGraceBal(plans);
		if (account.getGraceDate() != null
				// 判断账户是否有期初余额
				&& account.getBegBal().compareTo(BigDecimal.ZERO) >= 0
				// 判断批量日期在宽限日当天或以后,或者账户存在往期欠款
				&& batchStatusFacility.getBatchDate().compareTo(account.getGraceDate()) >= 0
				&& minDues.compareTo(BigDecimal.ZERO) > 0
				// 判断未还金额大于入催最小金额
				&& collPmt.compareTo(acctAttr.collMinpmt) > 0
				// 判断上次还款金额为空，或者为0
//				&& (account.getLastPmtAmt() == null || account.getLastPmtAmt().compareTo(BigDecimal.ZERO) == 0)
				// 判断上次还款日期为空
//				&& account.getLastPmtDate() == null
				// 账户参数上的首次还款入催标志
				&& acctAttr.collOnFsDlq){
			return CollectionReason.A0;
		}else{
			return null;
		}
	}

	/**
	 * 账龄入催检查
	 * @param accountInfo
	 * @return
	 */
	private CollectionReason checkAgeCd(CcsAcct account){
		// 账户属性参数
		ProductCredit productCredit = parameterFacility.loadParameter(account.getProductCd(), ProductCredit.class);
		AccountAttribute acctAttr = parameterFacility.loadParameter(productCredit.accountAttributeId, AccountAttribute.class);
				
		//	判断账龄不为空，且账龄大于等于开始催收账龄，当前余额大于账户参数中免催最大金额
		if(account.getAgeCode() != null
				&& AGE_CD_LIST.indexOf(account.getAgeCode()) >= AGE_CD_LIST.indexOf(acctAttr.collOnAge)
				&& account.getCurrBal().compareTo(acctAttr.collMinpmt) >0){
			return CollectionReason.A1;
		}else{
			return null;
		}
	}
	
	
	/**
	 * 超限入催检查
	 * @param accountInfo
	 * @return
	 */
	private CollectionReason checkOverLimit(CcsAcct account, List<CcsPlan> plans){
		// 账户属性参数
		ProductCredit productCredit = parameterFacility.loadParameter(account.getProductCd(), ProductCredit.class);
		AccountAttribute acctAttr = parameterFacility.loadParameter(productCredit.accountAttributeId, AccountAttribute.class);
		// 计算超限金额
		BigDecimal overAmt = calculator.getOverLimitAmt(account, plans, batchStatusFacility.getBatchDate());
		//	当前余额大于催收最小金额 and 超限金额 > 0
		//	以上条件成立时，返回超限催收原因码
		if(account.getCurrBal().compareTo(acctAttr.collMinpmt) >= 0
				&& overAmt.compareTo(BigDecimal.ZERO) > 0
				&& acctAttr.collOnOvrlmt){
			return CollectionReason.O0;
		}else{
			return null;
		}
	}
	
	/**
	 * 生成催收文件
	 * @param item
	 * @param reason 原因码
	 */
	private void createCollectionFile(S6000AcctInfo item,	CollectionReason reason){
		CcsAcct account = item.getAccount();
		
		CollectionItem fileItem = new CollectionItem();
		
		fileItem.acctNo = account.getAcctNbr();
		fileItem.blockCode = account.getBlockCode();
		fileItem.collectInd = (reason == null) ? Direction.O : Direction.I;
		fileItem.collectionReason = reason;
		fileItem.creditLimit = account.getCreditLmt();
		fileItem.currBalance = account.getCurrBal();
		fileItem.currCd = account.getCurrency();
		fileItem.defaultLogicCard = account.getDefaultLogicCardNbr();
		fileItem.mobileNo = account.getMobileNo();
		fileItem.name = account.getName();
		fileItem.org = account.getOrg();
		fileItem.procDate = batchStatusFacility.getBatchDate();
		fileItem.stmtAddress = account.getStmtAddress();
		fileItem.stmtCity = account.getStmtCity();
		fileItem.stmtCountryCd = account.getStmtCountryCode();
		fileItem.stmtDistrict = account.getStmtDistrict();
		fileItem.stmtState = account.getStmtState();
		fileItem.stmtZip = account.getStmtPostcode();
		fileItem.totDueAmt = account.getTotDueAmt();
		
		//	添加输出文件行
		item.getCollectionItems().add(fileItem);
	}

}
