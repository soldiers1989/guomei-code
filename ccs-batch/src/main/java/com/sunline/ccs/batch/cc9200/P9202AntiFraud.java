package com.sunline.ccs.batch.cc9200;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsLinkman;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ccs.otb.CommProvide;
import com.sunline.ppy.dictionary.exchange.CcExportAccountItem;
import com.sunline.ppy.dictionary.exchange.CcExportCardItem;
import com.sunline.ppy.dictionary.exchange.CcExportContactItem;
import com.sunline.ppy.dictionary.exchange.CcExportCustomerItem;
import com.sunline.ppy.dictionary.exchange.CcExportTxnItem;

/**
 * @see 类名：P9202AntiFraud
 * @see 描述：送反欺诈文件
 *
 * @see 创建日期：   2015-6-24下午4:13:35
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P9202AntiFraud implements ItemProcessor<S9201MasterData, S9201MasterData> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	CommProvide commonProvide;
	
	@Override
	public S9201MasterData process(S9201MasterData item) throws Exception {

		try
		{
			//取机构号并设置上下文
			OrganizationContextHolder.setCurrentOrg(item.getAccount().getOrg());
			
			this.makeRdsAccount(item);
			this.makeRdsCustomer(item);
			this.makeRdsLinkman(item);
			this.makeRdsCard(item);
			this.makeRdsTxn(item);
			
		}catch (Exception e) {
			logger.error("送反欺诈文件生成异常, 账号{}, 账户类型{}", item.getAccount().getAcctNbr(), item.getAccount().getAcctType());
			throw e;
		}
		
		return item;
	}
	
	/**
	 *  
	 * @see 方法名：makeRdsAccount 
	 * @see 描述：催收账户信息
	 * @see 创建日期：2015-6-24下午4:14:10
	 * @author ChengChun
	 *  
	 * @param item
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void makeRdsAccount(S9201MasterData item) {
		CcExportAccountItem rdsAccount = new CcExportAccountItem();
		
		rdsAccount.org = item.getAccount().getOrg();
		rdsAccount.acctNo = String.valueOf( item.getAccount().getAcctNbr() );
		rdsAccount.acctType = item.getAccount().getAcctType();
		rdsAccount.custId = String.valueOf( item.getAccount().getCustId() );
		rdsAccount.productCd = item.getAccount().getProductCd();
		rdsAccount.defaultLogicalCardNo = item.getAccount().getDefaultLogicCardNbr();
		rdsAccount.currCd = item.getAccount().getCurrency();
		rdsAccount.creditLimit = item.getAccount().getCreditLmt();
		rdsAccount.tempLimit = item.getAccount().getTempLmt();
		rdsAccount.tempLimitBeginDate = item.getAccount().getTempLmtBegDate();
		rdsAccount.tempLimitEndDate = item.getAccount().getTempLmtEndDate();
		rdsAccount.cashLimitRt = commonProvide.getCashLmtRate(item.getAccount());
		rdsAccount.ovrlmtRate = commonProvide.getOvrlmtRate(item.getAccount());
		rdsAccount.loanLimitRt = commonProvide.getLoanLmtRate(item.getAccount());
		rdsAccount.currBal = item.getAccount().getCurrBal();
		rdsAccount.cashBal = item.getAccount().getCashBal();
		rdsAccount.principalBal = item.getAccount().getPrincipalBal();
		rdsAccount.loanBal = item.getAccount().getLoanBal();
		rdsAccount.disputeAmt = item.getAccount().getDisputeAmt();
		rdsAccount.beginBal = item.getAccount().getBegBal();
		rdsAccount.pmtDueDayBal = item.getAccount().getPmtDueDayBal();
		rdsAccount.qualGraceBal = item.getAccount().getQualGraceBal();
		rdsAccount.graceDaysFullInd = item.getAccount().getGraceDaysFullInd();
		rdsAccount.pointBeginBal = item.getAccount().getPointsBegBal();
		rdsAccount.ctdEarnedPoints = item.getAccount().getCtdPoints();
		rdsAccount.ctdDisbPoints = item.getAccount().getCtdSpendPoints();
		rdsAccount.ctdAdjPoints = item.getAccount().getCtdAdjPoints();
		rdsAccount.pointBal = item.getAccount().getPointsBal();
		rdsAccount.setupDate = item.getAccount().getSetupDate();
		rdsAccount.dormentDate = item.getAccount().getDormentDate();
//		rdsAccount.reinstateDate = item.getAccount().getReinstateDate(); //FIXME
		rdsAccount.ovrlmtDate = item.getAccount().getOvrlmtDate();
		rdsAccount.ovrlmtNbrOfCyc = item.getAccount().getOvrlmtNbrOfCyc();
		rdsAccount.name = item.getAccount().getName();
		rdsAccount.gender = item.getAccount().getGender();
		rdsAccount.owningBranch = item.getAccount().getOwningBranch();
		rdsAccount.mobileNo = item.getAccount().getMobileNo();
		rdsAccount.corpName = item.getAccount().getCorpName();
		rdsAccount.billingCycle = item.getAccount().getCycleDay();
		rdsAccount.stmtFlag = item.getAccount().getStmtFlag();
		rdsAccount.stmtMailAddrInd = item.getAccount().getStmtMailAddrInd();
		rdsAccount.stmtMediaType = item.getAccount().getStmtMediaType();
		rdsAccount.stmtCountryCd = item.getAccount().getStmtCountryCode();
		rdsAccount.stmtState = item.getAccount().getStmtState();
		rdsAccount.stmtCity = item.getAccount().getStmtCity();
		rdsAccount.stmtDistrict = item.getAccount().getStmtDistrict();
		rdsAccount.stmtAddress = item.getAccount().getStmtAddress();
		rdsAccount.stmtZip = item.getAccount().getStmtPostcode();
		rdsAccount.email = item.getAccount().getEmail();
		rdsAccount.blockCode = item.getAccount().getBlockCode();
		rdsAccount.ageCd = item.getAccount().getAgeCode();
		rdsAccount.glAgeCd = item.getAccount().getAgeCodeGl();
		rdsAccount.unmatchDb = item.getAccount().getMemoDb();
		rdsAccount.unmatchCash = item.getAccount().getMemoCash();
		rdsAccount.unmatchCr = item.getAccount().getMemoCr();
		rdsAccount.ddInd = item.getAccount().getDdInd();
		rdsAccount.ddBankName = item.getAccount().getDdBankName();
		rdsAccount.ddBankBranch = item.getAccount().getDdBankBranch();
		rdsAccount.ddBankAcctNo = item.getAccount().getDdBankAcctNbr();
		rdsAccount.ddBankAcctName = item.getAccount().getDdBankAcctName();
		rdsAccount.lastDdAmt = item.getAccount().getLastDdAmt();
		rdsAccount.lastDdDate = item.getAccount().getLastDdDate();
		rdsAccount.dualBillingFlag = item.getAccount().getDualBillingFlag();
		rdsAccount.lastPmtAmt = item.getAccount().getLastPmtAmt();
		rdsAccount.lastPmtDate = item.getAccount().getLastPmtDate();
		rdsAccount.lastStmtDate = item.getAccount().getLastStmtDate();
		rdsAccount.lastPmtDueDate = item.getAccount().getLastPmtDueDate();
		rdsAccount.lastAgingDate = item.getAccount().getLastAgingDate();
		rdsAccount.collectDate = item.getAccount().getCollectInDate();
		rdsAccount.collectOutDate = item.getAccount().getCollectOutDate();
		rdsAccount.nextStmtDate = item.getAccount().getNextStmtDate();
		rdsAccount.pmtDueDate = item.getAccount().getPmtDueDate();
		rdsAccount.ddDate = item.getAccount().getDdDate();
		rdsAccount.graceDate = item.getAccount().getGraceDate();
		rdsAccount.dlblDate = item.getAccount().getDualBillingDate();
		rdsAccount.closedDate = item.getAccount().getClosedDate();
		rdsAccount.firstStmtDate = item.getAccount().getFirstStmtDate();
		rdsAccount.cancelDate = item.getAccount().getCloseDate();
		rdsAccount.chargeOffDate = item.getAccount().getChargeOffDate();
		rdsAccount.firstPurchaseDate = item.getAccount().getFirstRetlDate();
		rdsAccount.firstPurchaseAmt = item.getAccount().getFirstRetlAmt();
		rdsAccount.totDueAmt = item.getAccount().getTotDueAmt();
		rdsAccount.currDueAmt = item.getAccount().getCurrDueAmt();
		rdsAccount.pastDueAmt1 = item.getAccount().getPastDueAmt1();
		rdsAccount.pastDueAmt2 = item.getAccount().getPastDueAmt2();
		rdsAccount.pastDueAmt3 = item.getAccount().getPastDueAmt3();
		rdsAccount.pastDueAmt4 = item.getAccount().getPastDueAmt4();
		rdsAccount.pastDueAmt5 = item.getAccount().getPastDueAmt5();
		rdsAccount.pastDueAmt6 = item.getAccount().getPastDueAmt6();
		rdsAccount.pastDueAmt7 = item.getAccount().getPastDueAmt7();
		rdsAccount.pastDueAmt8 = item.getAccount().getPastDueAmt8();
		rdsAccount.ctdCashAmt = item.getAccount().getCtdCashAmt();
		rdsAccount.ctdCashCnt = item.getAccount().getCtdCashCnt();
		rdsAccount.ctdRetailAmt = item.getAccount().getCtdRetailAmt();
		rdsAccount.ctdRetailCnt = item.getAccount().getCtdRetailCnt();
		rdsAccount.ctdPaymentAmt = item.getAccount().getCtdRepayAmt();
		rdsAccount.ctdPaymentCnt = item.getAccount().getCtdRepayCnt();
		rdsAccount.ctdDbAdjAmt = item.getAccount().getCtdDbAdjAmt();
		rdsAccount.ctdDbAdjCnt = item.getAccount().getCtdDbAdjCnt();
		rdsAccount.ctdCrAdjAmt = item.getAccount().getCtdCrAdjAmt();
		rdsAccount.ctdCrAdjCnt = item.getAccount().getCtdCrAdjCnt();
		rdsAccount.ctdFeeAmt = item.getAccount().getCtdFeeAmt();
		rdsAccount.ctdFeeCnt = item.getAccount().getCtdFeeCnt();
		rdsAccount.ctdInterestAmt = item.getAccount().getCtdInterestAmt();
		rdsAccount.ctdInterestCnt = item.getAccount().getCtdInterestCnt();
		rdsAccount.ctdRefundAmt = item.getAccount().getCtdRefundAmt();
		rdsAccount.ctdRefundCnt = item.getAccount().getCtdRefundCnt();
		rdsAccount.ctdHiOvrlmtAmt = item.getAccount().getCtdMaxOvrlmtAmt();
		rdsAccount.mtdRetailAmt = item.getAccount().getMtdRetailAmt();
		rdsAccount.mtdRetailCnt = item.getAccount().getMtdRetailCnt();
		rdsAccount.mtdCashAmt = item.getAccount().getMtdCashAmt();
		rdsAccount.mtdCashCnt = item.getAccount().getMtdCashCnt();
		rdsAccount.mtdRefundAmt = item.getAccount().getMtdRefundAmt();
		rdsAccount.mtdRefundCnt = item.getAccount().getMtdRefundCnt();
		rdsAccount.ytdRetailAmt = item.getAccount().getYtdRetailAmt();
		rdsAccount.ytdRetailCnt = item.getAccount().getYtdRetailCnt();
		rdsAccount.ytdCashAmt = item.getAccount().getYtdCashAmt();
		rdsAccount.ytdCashCnt = item.getAccount().getYtdCashCnt();
		rdsAccount.ytdRefundAmt = item.getAccount().getYtdRefundAmt();
		rdsAccount.ytdRefundCnt = item.getAccount().getYtdRefundCnt();
		rdsAccount.ytdOvrlmtFeeAmt = item.getAccount().getYtdOvrlmtFeeAmt();
		rdsAccount.ytdOvrlmtFeeCnt = item.getAccount().getYtdOvrlmtFeeCnt();
		rdsAccount.ytdLpcAmt = item.getAccount().getYtdLateFeeAmt();
		rdsAccount.ytdLpcCnt = item.getAccount().getYtdLateFeeCnt();
		rdsAccount.ltdRetailAmt = item.getAccount().getLtdRetailAmt();
		rdsAccount.ltdRetailCnt = item.getAccount().getLtdRetailCnt();
		rdsAccount.ltdCashAmt = item.getAccount().getLtdCashAmt();
		rdsAccount.ltdCashCnt = item.getAccount().getLtdCashCnt();
		rdsAccount.ltdRefundAmt = item.getAccount().getLtdRefundAmt();
		rdsAccount.ltdRefundCnt = item.getAccount().getLtdRefundCnt();
		rdsAccount.ltdHighestPrincipal = item.getAccount().getLtdHighestPrin();
		rdsAccount.ltdHighestCrBal = item.getAccount().getLtdHighestCrBal();
		rdsAccount.ltdHighestBal = item.getAccount().getLtdHighestBal();
		rdsAccount.collectTimes = item.getAccount().getCollectCnt();
		rdsAccount.collectColr = item.getAccount().getCollector();
		rdsAccount.collectReason = item.getAccount().getCollectReason();
		rdsAccount.ageHist = item.getAccount().getAgeHst();
		rdsAccount.paymentHist = item.getAccount().getPaymentHst();
		rdsAccount.waiveOvlfeeInd = item.getAccount().getWaiveOvlfeeInd();
		rdsAccount.waiveCardfeeInd = item.getAccount().getWaiveCardfeeInd();
		rdsAccount.waiveLatefeeInd = item.getAccount().getWaiveLatefeeInd();
		rdsAccount.waiveSvcfeeInd = item.getAccount().getWaiveSvcfeeInd();
		rdsAccount.mtdPaymentAmt = item.getAccount().getMtdPaymentAmt();
		rdsAccount.mtdPaymentCnt = item.getAccount().getMtdPaymentCnt();
		rdsAccount.ytdPaymentAmt = item.getAccount().getYtdRepayAmt();
		rdsAccount.ytdPaymentCnt = item.getAccount().getYtdRepayCnt();
		rdsAccount.ltdPaymentAmt = item.getAccount().getLtdRepayAmt();
		rdsAccount.ltdPaymentCnt = item.getAccount().getLtdRepayCnt();
		rdsAccount.smsInd = item.getAccount().getSmsInd();
		rdsAccount.userSmsAmt = item.getAccount().getUserSmsAmt();
		rdsAccount.ytdCycleChagCnt = item.getAccount().getYtdCycleChagCnt();
		
		item.setRdsAccount(rdsAccount);
	}
	
	/**
	 * @see 方法名：makeRdsCustomer 
	 * @see 描述：催收客户信息
	 * @see 创建日期：2015-6-24下午4:36:56
	 * @author ChengChun
	 *  
	 * @param item
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void makeRdsCustomer(S9201MasterData item) {
		CcExportCustomerItem rdsCustomer = new CcExportCustomerItem();
		
		rdsCustomer.org = item.getCustomer().getOrg();
		rdsCustomer.custId = String.valueOf( item.getCustomer().getCustId() );
		rdsCustomer.idNo = item.getCustomer().getIdNo();
		rdsCustomer.idType = item.getCustomer().getIdType();
		rdsCustomer.title = item.getCustomer().getTitle();
		rdsCustomer.name = item.getCustomer().getName();
		rdsCustomer.gender = item.getCustomer().getGender();
		rdsCustomer.birthday = item.getCustomer().getBirthday();
		rdsCustomer.occupation = item.getCustomer().getOccupation();
		rdsCustomer.bankMemberNo = item.getCustomer().getInternalStaffId();
		rdsCustomer.nationality = item.getCustomer().getNationality();
		rdsCustomer.prOfCountry = item.getCustomer().getPrOfCountry();
		rdsCustomer.residencyCountryCd = item.getCustomer().getResidencyCountryCd();
		rdsCustomer.maritalStatus = item.getCustomer().getMaritalStatus();
		rdsCustomer.qualification = item.getCustomer().getEducation();
		rdsCustomer.socialStatus = item.getCustomer().getSocialStatus();
		rdsCustomer.idIssuerAddress = item.getCustomer().getIdIssAddr();
		rdsCustomer.homePhone = item.getCustomer().getHomePhone();
		rdsCustomer.houseOwnership = item.getCustomer().getHouseOwnership();
		rdsCustomer.houseType = item.getCustomer().getHouseType();
		rdsCustomer.homeStandFrom = item.getCustomer().getHomeStandFrom();
		rdsCustomer.liquidAsset = item.getCustomer().getLiquidAsset();
		rdsCustomer.mobileNo = item.getCustomer().getMobileNo();
		rdsCustomer.email = item.getCustomer().getEmail();
		rdsCustomer.empStatus = item.getCustomer().getEmpStatus();
		rdsCustomer.nbrOfDependents = item.getCustomer().getNbrOfDependents();
		rdsCustomer.languageInd = item.getCustomer().getLanguageInd();
		rdsCustomer.setupDate = item.getCustomer().getSetupDate();
		rdsCustomer.socialInsAmt = item.getCustomer().getSocialInsAmt();
		rdsCustomer.driveLicenseId = item.getCustomer().getDriveLicenseId();
		rdsCustomer.driveLicRegDate = item.getCustomer().getDriveLicRegDate();
		rdsCustomer.obligateQuestion = item.getCustomer().getSecureQuestion();
		rdsCustomer.obligateAnswer = item.getCustomer().getSecureAnswer();
		rdsCustomer.empStability = item.getCustomer().getEmpQuitFreq();
		rdsCustomer.corpName = item.getCustomer().getCorpName();
		rdsCustomer.bankCustomerId = item.getCustomer().getInternalCustomerId();
		rdsCustomer.embName = item.getCustomer().getOncardName();
		rdsCustomer.creditLimit = item.getCustLimitO().getCreditLmt();
		
		item.setRdsCustomer(rdsCustomer);
	}
	
	/**
	 * @see 方法名：makeRdsLinkman 
	 * @see 描述：客户联系信息
	 * @see 创建日期：2015-6-24下午4:37:23
	 * @author ChengChun
	 *  
	 * @param item
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void makeRdsLinkman(S9201MasterData item) {
		List<CcsLinkman> listContact = item.getListContact();
		for (CcsLinkman tmContact : listContact) {
			CcExportContactItem rdsContact = new CcExportContactItem();
			
			rdsContact.org = tmContact.getOrg();
			rdsContact.contactId = tmContact.getLinkmanId();
			rdsContact.custId = String.valueOf( tmContact.getCustId() );
			rdsContact.relationship = tmContact.getRelationship();
			rdsContact.name = tmContact.getName();
			rdsContact.gender = tmContact.getGender();
			rdsContact.mobileNo = tmContact.getMobileNo();
			rdsContact.birthday = tmContact.getBirthday();
			rdsContact.corpName = tmContact.getCorpName();
			rdsContact.idType = tmContact.getIdType();
			rdsContact.idNo = tmContact.getIdNo();
			rdsContact.corpPhone = tmContact.getCorpTelephNbr();
			rdsContact.corpFax = tmContact.getCorpFax();
			rdsContact.corpPost = tmContact.getCorpPosition();
			
			item.getRdsContact().add(rdsContact);
		}
	}
	
	/**
	 * @see 方法名：makeRdsCard 
	 * @see 描述：卡片信息
	 * @see 创建日期：2015-6-24下午4:37:46
	 * @author ChengChun
	 *  
	 * @param item
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void makeRdsCard(S9201MasterData item) {
		List<CcsCard> listCard = item.getListCard();
		for (CcsCard tmCard : listCard) {
			CcExportCardItem rdsCard = new CcExportCardItem();
			
			rdsCard.org = tmCard.getOrg();
			rdsCard.logicalCardNo = tmCard.getLogicCardNbr();
			rdsCard.acctNo = String.valueOf( tmCard.getAcctNbr() );
			rdsCard.custId = String.valueOf( tmCard.getCustId() );
//			rdsCard.corpId = tmCard.getCorpId(); //FIXME
			rdsCard.productCd = tmCard.getProductCd();
			rdsCard.appNo = tmCard.getApplNbr();
			rdsCard.barcode = tmCard.getBarcode();
			rdsCard.bscSuppInd = tmCard.getBscSuppInd();
			rdsCard.bscLogiccardNo = tmCard.getCardBasicNbr();
			rdsCard.owningBranch = tmCard.getOwningBranch();
			rdsCard.appPromotionCd = tmCard.getApplPromoCode();
			rdsCard.recomName = tmCard.getRecomName();
			rdsCard.recomCardNo = tmCard.getRecomCardNo();
			rdsCard.setupDate = tmCard.getSetupDate();
			rdsCard.blockCode = tmCard.getBlockCode();
			rdsCard.activateInd = tmCard.getActiveInd();
			rdsCard.activateDate = tmCard.getActiveDate();
			rdsCard.cancelDate = tmCard.getCloseDate();
			rdsCard.latestCardNo = tmCard.getLastestMediumCardNbr();
			rdsCard.salesInd = tmCard.getSalesInd();
			rdsCard.appSource = tmCard.getApplSrc();
			rdsCard.representName = tmCard.getRepresentName();
			rdsCard.posPinVerifyInd = tmCard.getPosPinVerifyInd();
			rdsCard.relationshipToBsc = tmCard.getRelationshipToBsc();
			rdsCard.cardExpireDate = tmCard.getCardExpireDate();
			rdsCard.cardFeeRate = tmCard.getCardFeeRate();
			rdsCard.renewInd = tmCard.getRenewInd();
			rdsCard.renewRejectCd = tmCard.getRenewRejectCd();
			rdsCard.firstCardFeeDate = tmCard.getFirstCardFeeDate();
			rdsCard.lastRenewalDate = tmCard.getLastRenewcardDate();
			rdsCard.nextCardFeeDate = tmCard.getNextCardFeeDate();
			rdsCard.waiveCardfeeInd = tmCard.getWaiveCardfeeInd();
			rdsCard.cardFetchMethod = tmCard.getCardDeliverMethod();
			rdsCard.cardMailerInd = tmCard.getCardDeliverAddrFlag();
			
			item.getRdsCard().add(rdsCard);
		}
	}
	
	/**
	 * @see 方法名：makeRdsTxn 
	 * @see 描述：交易信息
	 * @see 创建日期：2015-6-24下午4:38:23
	 * @author ChengChun
	 *  
	 * @param item
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void makeRdsTxn(S9201MasterData item) {
		List<CcsTxnHst> listTxnHst = item.getListTxnHst();
		for (CcsTxnHst tmTxnHst : listTxnHst) {
			CcExportTxnItem rdsTxn = new CcExportTxnItem();
			
			rdsTxn.org = tmTxnHst.getOrg();
			rdsTxn.txnSeq = tmTxnHst.getTxnSeq();
			rdsTxn.custId = String.valueOf( item.getCustomer().getCustId() );
			rdsTxn.acctNo = String.valueOf( tmTxnHst.getAcctNbr() );
			rdsTxn.acctType = tmTxnHst.getAcctType();
			rdsTxn.cardNo = tmTxnHst.getCardNbr();
			rdsTxn.logicalCardNo = tmTxnHst.getLogicCardNbr();
			rdsTxn.bscLogicCardNo = tmTxnHst.getCardBasicNbr();
			rdsTxn.productCd = tmTxnHst.getProductCd();
			rdsTxn.txnCode = tmTxnHst.getTxnCode();
			rdsTxn.txnDate = tmTxnHst.getTxnDate();
			rdsTxn.txnTime = tmTxnHst.getTxnTime();
			rdsTxn.txnCurrCd = tmTxnHst.getTxnCurrency();
			rdsTxn.txnAmt = tmTxnHst.getTxnAmt();
			rdsTxn.postDate = tmTxnHst.getPostDate();
			rdsTxn.postCurrCd = tmTxnHst.getPostCurrency();
			rdsTxn.postAmt = tmTxnHst.getPostAmt();
			rdsTxn.postTxnType = tmTxnHst.getPostTxnType();
			rdsTxn.dbCrInd = tmTxnHst.getDbCrInd();
			rdsTxn.point = tmTxnHst.getPoints();
			rdsTxn.cardBlockCode = tmTxnHst.getCardBlockCode();
			rdsTxn.refNbr = tmTxnHst.getRefNbr();
			rdsTxn.txnDesc = tmTxnHst.getTxnDesc();
			rdsTxn.txnShortDesc = tmTxnHst.getTxnShortDesc();
			rdsTxn.mcc = tmTxnHst.getMcc();
			rdsTxn.stmtDate = tmTxnHst.getStmtDate();
			
			item.getRdsTxn().add(rdsTxn);
		}
	}
}
