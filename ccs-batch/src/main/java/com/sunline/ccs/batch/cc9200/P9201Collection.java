package com.sunline.ccs.batch.cc9200;


import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.ppy.dictionary.exchange.CcExportAccountItem;
import com.sunline.ppy.dictionary.exchange.CcExportAddressItem;
import com.sunline.ppy.dictionary.exchange.CcExportCardItem;
import com.sunline.ppy.dictionary.exchange.CcExportContactItem;
import com.sunline.ppy.dictionary.exchange.CcExportCustomerItem;
import com.sunline.ppy.dictionary.exchange.CcExportTxnItem;
import com.sunline.ccs.infrastructure.shared.model.CcsAddress;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsLinkman;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ccs.otb.CommProvide;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.utils.CodeMarkUtils;

/**
 * @see 类名：P9201Collection
 * @see 描述：送催收文件
 *
 * @see 创建日期：   2015-6-24下午3:34:23
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P9201Collection implements ItemProcessor<S9201MasterData, S9201MasterData> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * 当前批量日期获取
	 */
	@Autowired
    private BatchStatusFacility batchFacility;
	@Autowired
	private CommProvide commonProvide;

	
	@Override
	public S9201MasterData process(S9201MasterData item) throws Exception {

		if (logger.isDebugEnabled()) {
			logger.debug("催收账户对应文件生成：Org["+item.getAccount().getOrg()
					+"],AcctType["+item.getAccount().getAcctType()
					+"],AcctNo["+item.getAccount().getAcctNbr()
					+"],IdNo后四位["+CodeMarkUtils.markIDCard(item.getCustomer().getIdNo())
					+"],Contact.size["+item.getListContact().size()
					+"],Address.size["+item.getListAddress().size()
					+"],Card.size["+item.getListCard().size()
					+"],TxnHst.size["+item.getListTxnHst().size()
					+"]");
		}

		// 入催原因等于null，且出催日期等于批量日期；或入催原因不等于null；
		if ((item.getAccount().getCollectReason() == null 
				&& item.getAccount().getCollectOutDate() != null
				&& DateUtils.truncatedCompareTo(batchFacility.getBatchDate(), 
						item.getAccount().getCollectOutDate(), Calendar.DATE) == 0)
				|| item.getAccount().getCollectReason() != null ) {
			
			this.makeCtsAccount(item);
			this.makeCtsCustomer(item);
			this.makeCtsLinkman(item);
			this.makeCtsAddress(item);
			this.makeCtsCard(item);
			this.makeCtsTxn(item);
		}
		
		return item;
	}
	
	/**
	 * @see 方法名：makeCtsAccount 
	 * @see 描述：催收账户信息
	 * @see 创建日期：2015-6-24下午3:54:17
	 * @author ChengChun
	 *  
	 * @param item
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void makeCtsAccount(S9201MasterData item) {
		item.setCtsAccount(new CcExportAccountItem());
		item.getCtsAccount().org = item.getAccount().getOrg();
		item.getCtsAccount().acctNo = String.valueOf( item.getAccount().getAcctNbr() );
		item.getCtsAccount().acctType = item.getAccount().getAcctType();
		item.getCtsAccount().custId = String.valueOf( item.getAccount().getCustId() );
		item.getCtsAccount().productCd = item.getAccount().getProductCd();
		item.getCtsAccount().defaultLogicalCardNo = item.getAccount().getDefaultLogicCardNbr();
		item.getCtsAccount().currCd = item.getAccount().getCurrency();
		item.getCtsAccount().creditLimit = item.getAccount().getCreditLmt();
		item.getCtsAccount().tempLimit = item.getAccount().getTempLmt();
		item.getCtsAccount().tempLimitBeginDate = item.getAccount().getTempLmtBegDate();
		item.getCtsAccount().tempLimitEndDate = item.getAccount().getTempLmtEndDate();
		item.getCtsAccount().cashLimitRt = commonProvide.getCashLmtRate(item.getAccount());
		item.getCtsAccount().ovrlmtRate = commonProvide.getOvrlmtRate(item.getAccount());
		item.getCtsAccount().loanLimitRt = commonProvide.getLoanLmtRate(item.getAccount());
		item.getCtsAccount().currBal = item.getAccount().getCurrBal();
		item.getCtsAccount().cashBal = item.getAccount().getCashBal();
		item.getCtsAccount().principalBal = item.getAccount().getPrincipalBal();
		item.getCtsAccount().loanBal = item.getAccount().getLoanBal();
		item.getCtsAccount().disputeAmt = item.getAccount().getDisputeAmt();
		item.getCtsAccount().beginBal = item.getAccount().getBegBal();
		item.getCtsAccount().pmtDueDayBal = item.getAccount().getPmtDueDayBal();
		item.getCtsAccount().qualGraceBal = item.getAccount().getQualGraceBal();
		item.getCtsAccount().graceDaysFullInd = item.getAccount().getGraceDaysFullInd();
		item.getCtsAccount().pointBeginBal = item.getAccount().getPointsBegBal();
		item.getCtsAccount().ctdEarnedPoints = item.getAccount().getCtdPoints();
		item.getCtsAccount().ctdDisbPoints = item.getAccount().getCtdSpendPoints();
		item.getCtsAccount().ctdAdjPoints = item.getAccount().getCtdAdjPoints();
		item.getCtsAccount().pointBal = item.getAccount().getPointsBal();
		item.getCtsAccount().setupDate = item.getAccount().getSetupDate();
		item.getCtsAccount().dormentDate = item.getAccount().getDormentDate();
//		item.getCtsAccount().reinstateDate = item.getAccount().getReinstateDate(); //TODO
		item.getCtsAccount().ovrlmtDate = item.getAccount().getOvrlmtDate();
		item.getCtsAccount().ovrlmtNbrOfCyc = item.getAccount().getOvrlmtNbrOfCyc();
		item.getCtsAccount().name = item.getAccount().getName();
		item.getCtsAccount().gender = item.getAccount().getGender();
		item.getCtsAccount().owningBranch = item.getAccount().getOwningBranch();
		item.getCtsAccount().mobileNo = item.getAccount().getMobileNo();
		item.getCtsAccount().corpName = item.getAccount().getCorpName();
		item.getCtsAccount().billingCycle = item.getAccount().getCycleDay();
		item.getCtsAccount().stmtFlag = item.getAccount().getStmtFlag();
		item.getCtsAccount().stmtMailAddrInd = item.getAccount().getStmtMailAddrInd();
		item.getCtsAccount().stmtMediaType = item.getAccount().getStmtMediaType();
		item.getCtsAccount().stmtCountryCd = item.getAccount().getStmtCountryCode();
		item.getCtsAccount().stmtState = item.getAccount().getStmtState();
		item.getCtsAccount().stmtCity = item.getAccount().getStmtCity();
		item.getCtsAccount().stmtDistrict = item.getAccount().getStmtDistrict();
		item.getCtsAccount().stmtAddress = item.getAccount().getStmtAddress();
		item.getCtsAccount().stmtZip = item.getAccount().getStmtPostcode();
		item.getCtsAccount().email = item.getAccount().getEmail();
		item.getCtsAccount().blockCode = item.getAccount().getBlockCode();
		item.getCtsAccount().ageCd = item.getAccount().getAgeCode();
		item.getCtsAccount().glAgeCd = item.getAccount().getAgeCodeGl();
		item.getCtsAccount().unmatchDb = item.getAccount().getMemoDb();
		item.getCtsAccount().unmatchCash = item.getAccount().getMemoCash();
		item.getCtsAccount().unmatchCr = item.getAccount().getMemoCr();
		item.getCtsAccount().ddInd = item.getAccount().getDdInd();
		item.getCtsAccount().ddBankName = item.getAccount().getDdBankName();
		item.getCtsAccount().ddBankBranch = item.getAccount().getDdBankBranch();
		item.getCtsAccount().ddBankAcctNo = item.getAccount().getDdBankAcctNbr();
		item.getCtsAccount().ddBankAcctName = item.getAccount().getDdBankAcctName();
		item.getCtsAccount().lastDdAmt = item.getAccount().getLastDdAmt();
		item.getCtsAccount().lastDdDate = item.getAccount().getLastDdDate();
		item.getCtsAccount().dualBillingFlag = item.getAccount().getDualBillingFlag();
		item.getCtsAccount().lastPmtAmt = item.getAccount().getLastPmtAmt();
		item.getCtsAccount().lastPmtDate = item.getAccount().getLastPmtDate();
		item.getCtsAccount().lastStmtDate = item.getAccount().getLastStmtDate();
		item.getCtsAccount().lastPmtDueDate = item.getAccount().getLastPmtDueDate();
		item.getCtsAccount().lastAgingDate = item.getAccount().getLastAgingDate();
		item.getCtsAccount().collectDate = item.getAccount().getCollectInDate();
		item.getCtsAccount().collectOutDate = item.getAccount().getCollectOutDate();
		item.getCtsAccount().nextStmtDate = item.getAccount().getNextStmtDate();
		item.getCtsAccount().pmtDueDate = item.getAccount().getPmtDueDate();
		item.getCtsAccount().ddDate = item.getAccount().getDdDate();
		item.getCtsAccount().graceDate = item.getAccount().getGraceDate();
		item.getCtsAccount().dlblDate = item.getAccount().getDualBillingDate();
		item.getCtsAccount().closedDate = item.getAccount().getClosedDate();
		item.getCtsAccount().firstStmtDate = item.getAccount().getFirstStmtDate();
		item.getCtsAccount().cancelDate = item.getAccount().getCloseDate();
		item.getCtsAccount().chargeOffDate = item.getAccount().getChargeOffDate();
		item.getCtsAccount().firstPurchaseDate = item.getAccount().getFirstRetlDate();
		item.getCtsAccount().firstPurchaseAmt = item.getAccount().getFirstRetlAmt();
		item.getCtsAccount().totDueAmt = item.getAccount().getTotDueAmt();
		item.getCtsAccount().currDueAmt = item.getAccount().getCurrDueAmt();
		item.getCtsAccount().pastDueAmt1 = item.getAccount().getPastDueAmt1();
		item.getCtsAccount().pastDueAmt2 = item.getAccount().getPastDueAmt2();
		item.getCtsAccount().pastDueAmt3 = item.getAccount().getPastDueAmt3();
		item.getCtsAccount().pastDueAmt4 = item.getAccount().getPastDueAmt4();
		item.getCtsAccount().pastDueAmt5 = item.getAccount().getPastDueAmt5();
		item.getCtsAccount().pastDueAmt6 = item.getAccount().getPastDueAmt6();
		item.getCtsAccount().pastDueAmt7 = item.getAccount().getPastDueAmt7();
		item.getCtsAccount().pastDueAmt8 = item.getAccount().getPastDueAmt8();
		item.getCtsAccount().ctdCashAmt = item.getAccount().getCtdCashAmt();
		item.getCtsAccount().ctdCashCnt = item.getAccount().getCtdCashCnt();
		item.getCtsAccount().ctdRetailAmt = item.getAccount().getCtdRetailAmt();
		item.getCtsAccount().ctdRetailCnt = item.getAccount().getCtdRetailCnt();
		item.getCtsAccount().ctdPaymentAmt = item.getAccount().getCtdRepayAmt();
		item.getCtsAccount().ctdPaymentCnt = item.getAccount().getCtdRepayCnt();
		item.getCtsAccount().ctdDbAdjAmt = item.getAccount().getCtdDbAdjAmt();
		item.getCtsAccount().ctdDbAdjCnt = item.getAccount().getCtdDbAdjCnt();
		item.getCtsAccount().ctdCrAdjAmt = item.getAccount().getCtdCrAdjAmt();
		item.getCtsAccount().ctdCrAdjCnt = item.getAccount().getCtdCrAdjCnt();
		item.getCtsAccount().ctdFeeAmt = item.getAccount().getCtdFeeAmt();
		item.getCtsAccount().ctdFeeCnt = item.getAccount().getCtdFeeCnt();
		item.getCtsAccount().ctdInterestAmt = item.getAccount().getCtdInterestAmt();
		item.getCtsAccount().ctdInterestCnt = item.getAccount().getCtdInterestCnt();
		item.getCtsAccount().ctdRefundAmt = item.getAccount().getCtdRefundAmt();
		item.getCtsAccount().ctdRefundCnt = item.getAccount().getCtdRefundCnt();
		item.getCtsAccount().ctdHiOvrlmtAmt = item.getAccount().getCtdMaxOvrlmtAmt();
		item.getCtsAccount().mtdRetailAmt = item.getAccount().getMtdRetailAmt();
		item.getCtsAccount().mtdRetailCnt = item.getAccount().getMtdRetailCnt();
		item.getCtsAccount().mtdCashAmt = item.getAccount().getMtdCashAmt();
		item.getCtsAccount().mtdCashCnt = item.getAccount().getMtdCashCnt();
		item.getCtsAccount().mtdRefundAmt = item.getAccount().getMtdRefundAmt();
		item.getCtsAccount().mtdRefundCnt = item.getAccount().getMtdRefundCnt();
		item.getCtsAccount().ytdRetailAmt = item.getAccount().getYtdRetailAmt();
		item.getCtsAccount().ytdRetailCnt = item.getAccount().getYtdRetailCnt();
		item.getCtsAccount().ytdCashAmt = item.getAccount().getYtdCashAmt();
		item.getCtsAccount().ytdCashCnt = item.getAccount().getYtdCashCnt();
		item.getCtsAccount().ytdRefundAmt = item.getAccount().getYtdRefundAmt();
		item.getCtsAccount().ytdRefundCnt = item.getAccount().getYtdRefundCnt();
		item.getCtsAccount().ytdOvrlmtFeeAmt = item.getAccount().getYtdOvrlmtFeeAmt();
		item.getCtsAccount().ytdOvrlmtFeeCnt = item.getAccount().getYtdOvrlmtFeeCnt();
		item.getCtsAccount().ytdLpcAmt = item.getAccount().getYtdLateFeeAmt();
		item.getCtsAccount().ytdLpcCnt = item.getAccount().getYtdLateFeeCnt();
		item.getCtsAccount().ltdRetailAmt = item.getAccount().getLtdRetailAmt();
		item.getCtsAccount().ltdRetailCnt = item.getAccount().getLtdRetailCnt();
		item.getCtsAccount().ltdCashAmt = item.getAccount().getLtdCashAmt();
		item.getCtsAccount().ltdCashCnt = item.getAccount().getLtdCashCnt();
		item.getCtsAccount().ltdRefundAmt = item.getAccount().getLtdRefundAmt();
		item.getCtsAccount().ltdRefundCnt = item.getAccount().getLtdRefundCnt();
		item.getCtsAccount().ltdHighestPrincipal = item.getAccount().getLtdHighestPrin();
		item.getCtsAccount().ltdHighestCrBal = item.getAccount().getLtdHighestCrBal();
		item.getCtsAccount().ltdHighestBal = item.getAccount().getLtdHighestBal();
		item.getCtsAccount().collectTimes = item.getAccount().getCollectCnt();
		item.getCtsAccount().collectColr = item.getAccount().getCollector();
		item.getCtsAccount().collectReason = item.getAccount().getCollectReason();
		item.getCtsAccount().ageHist = item.getAccount().getAgeHst();
		item.getCtsAccount().paymentHist = item.getAccount().getPaymentHst();
		item.getCtsAccount().waiveOvlfeeInd = item.getAccount().getWaiveOvlfeeInd();
		item.getCtsAccount().waiveCardfeeInd = item.getAccount().getWaiveCardfeeInd();
		item.getCtsAccount().waiveLatefeeInd = item.getAccount().getWaiveLatefeeInd();
		item.getCtsAccount().waiveSvcfeeInd = item.getAccount().getWaiveSvcfeeInd();
		item.getCtsAccount().mtdPaymentAmt = item.getAccount().getMtdPaymentAmt();
		item.getCtsAccount().mtdPaymentCnt = item.getAccount().getMtdPaymentCnt();
		item.getCtsAccount().ytdPaymentAmt = item.getAccount().getYtdRepayAmt();
		item.getCtsAccount().ytdPaymentCnt = item.getAccount().getYtdRepayCnt();
		item.getCtsAccount().ltdPaymentAmt = item.getAccount().getLtdRepayAmt();
		item.getCtsAccount().ltdPaymentCnt = item.getAccount().getLtdRepayCnt();
		item.getCtsAccount().smsInd = item.getAccount().getSmsInd();
		item.getCtsAccount().userSmsAmt = item.getAccount().getUserSmsAmt();
		item.getCtsAccount().ytdCycleChagCnt = item.getAccount().getYtdCycleChagCnt();
		item.getCtsAccount().passDueAmt = getUnPayMents(item);
	}
	
	/**
	 * @see 方法名：makeCtsCustomer 
	 * @see 描述：催收客户信息
	 * @see 创建日期：2015-6-24下午3:56:05
	 * @author ChengChun
	 *  
	 * @param item
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void makeCtsCustomer(S9201MasterData item) {
		item.setCtsCustomer(new CcExportCustomerItem());
		item.getCtsCustomer().org = item.getCustomer().getOrg();
		item.getCtsCustomer().custId = String.valueOf( item.getCustomer().getCustId() );
		item.getCtsCustomer().idNo = item.getCustomer().getIdNo();
		item.getCtsCustomer().idType = item.getCustomer().getIdType();
		item.getCtsCustomer().title = item.getCustomer().getTitle();
		item.getCtsCustomer().name = item.getCustomer().getName();
		item.getCtsCustomer().gender = item.getCustomer().getGender();
		item.getCtsCustomer().birthday = item.getCustomer().getBirthday();
		item.getCtsCustomer().occupation = item.getCustomer().getOccupation();
		item.getCtsCustomer().bankMemberNo = item.getCustomer().getInternalStaffId();
		item.getCtsCustomer().nationality = item.getCustomer().getNationality();
		item.getCtsCustomer().prOfCountry = item.getCustomer().getPrOfCountry();
		item.getCtsCustomer().residencyCountryCd = item.getCustomer().getResidencyCountryCd();
		item.getCtsCustomer().maritalStatus = item.getCustomer().getMaritalStatus();
		item.getCtsCustomer().qualification = item.getCustomer().getEducation();
		item.getCtsCustomer().socialStatus = item.getCustomer().getSocialStatus();
		item.getCtsCustomer().idIssuerAddress = item.getCustomer().getIdIssAddr();
		item.getCtsCustomer().homePhone = item.getCustomer().getHomePhone();
		item.getCtsCustomer().houseOwnership = item.getCustomer().getHouseOwnership();
		item.getCtsCustomer().houseType = item.getCustomer().getHouseType();
		item.getCtsCustomer().homeStandFrom = item.getCustomer().getHomeStandFrom();
		item.getCtsCustomer().liquidAsset = item.getCustomer().getLiquidAsset();
		item.getCtsCustomer().mobileNo = item.getCustomer().getMobileNo();
		item.getCtsCustomer().email = item.getCustomer().getEmail();
		item.getCtsCustomer().empStatus = item.getCustomer().getEmpStatus();
		item.getCtsCustomer().nbrOfDependents = item.getCustomer().getNbrOfDependents();
		item.getCtsCustomer().languageInd = item.getCustomer().getLanguageInd();
		item.getCtsCustomer().setupDate = item.getCustomer().getSetupDate();
		item.getCtsCustomer().socialInsAmt = item.getCustomer().getSocialInsAmt();
		item.getCtsCustomer().driveLicenseId = item.getCustomer().getDriveLicenseId();
		item.getCtsCustomer().driveLicRegDate = item.getCustomer().getDriveLicRegDate();
		item.getCtsCustomer().obligateQuestion = item.getCustomer().getSecureQuestion();
		item.getCtsCustomer().obligateAnswer = item.getCustomer().getSecureAnswer();
		item.getCtsCustomer().empStability = item.getCustomer().getEmpQuitFreq();
		item.getCtsCustomer().corpName = item.getCustomer().getCorpName();
		item.getCtsCustomer().bankCustomerId = item.getCustomer().getInternalCustomerId();
		item.getCtsCustomer().embName = item.getCustomer().getOncardName();
		item.getCtsCustomer().creditLimit = item.getCustLimitO().getCreditLmt();
	}
	
	/**
	 * @see 方法名：makeCtsLinkman 
	 * @see 描述：客户联系信息
	 * @see 创建日期：2015-6-24下午3:57:12
	 * @author ChengChun
	 *  
	 * @param item
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void makeCtsLinkman(S9201MasterData item) {
		List<CcsLinkman> listContact = item.getListContact();
		for (CcsLinkman tmContact : listContact) {
			CcExportContactItem ctsContact = new CcExportContactItem();
			
			ctsContact.org = tmContact.getOrg();
			ctsContact.contactId = tmContact.getLinkmanId();
			ctsContact.custId = String.valueOf( tmContact.getCustId() );
			ctsContact.relationship = tmContact.getRelationship();
			ctsContact.name = tmContact.getName();
			ctsContact.gender = tmContact.getGender();
			ctsContact.mobileNo = tmContact.getMobileNo();
			ctsContact.birthday = tmContact.getBirthday();
			ctsContact.corpName = tmContact.getCorpName();
			ctsContact.idType = tmContact.getIdType();
			ctsContact.idNo = tmContact.getIdNo();
			ctsContact.corpPhone = tmContact.getCorpTelephNbr();
			ctsContact.corpFax = tmContact.getCorpFax();
			ctsContact.corpPost = tmContact.getCorpPosition();
			
			item.getCtsContact().add(ctsContact);
		}
	}
	
	/**
	 * @see 方法名：makeCtsAddress 
	 * @see 描述：客户联系地址
	 * @see 创建日期：2015-6-24下午4:02:16
	 * @author ChengChun
	 *  
	 * @param item
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void makeCtsAddress(S9201MasterData item) {
		List<CcsAddress> listAddress = item.getListAddress();
		for (CcsAddress tmAddress : listAddress) {
			CcExportAddressItem ctsAddress = new CcExportAddressItem();
			
			ctsAddress.org = tmAddress.getOrg();
			ctsAddress.addrId = tmAddress.getAddrId();
			ctsAddress.custId = String.valueOf( tmAddress.getCustId() );
			ctsAddress.addressType = tmAddress.getAddrType();
			ctsAddress.countryCd = tmAddress.getCountryCode();
			ctsAddress.state = tmAddress.getState();
			ctsAddress.city = tmAddress.getCity();
			ctsAddress.district = tmAddress.getDistrict();
			ctsAddress.zip = tmAddress.getPostcode();
			ctsAddress.phone = tmAddress.getPhone();
			ctsAddress.address = tmAddress.getAddress();
			
			item.getCtsAddress().add(ctsAddress);
		}
	}
	
	/**
	 * @see 方法名：makeCtsCard 
	 * @see 描述：卡片信息
	 * @see 创建日期：2015-6-24下午4:02:43
	 * @author ChengChun
	 *  
	 * @param item
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void makeCtsCard(S9201MasterData item) {
		List<CcsCard> listCard = item.getListCard();
		for (CcsCard tmCard : listCard) {
			CcExportCardItem ctsCard = new CcExportCardItem();
			
			ctsCard.org = tmCard.getOrg();
			ctsCard.logicalCardNo = tmCard.getLogicCardNbr();
			ctsCard.acctNo = String.valueOf( tmCard.getAcctNbr() );
			ctsCard.custId = String.valueOf( tmCard.getCustId() );
//			ctsCard.corpId = tmCard.getCorpId(); // FIXME
			ctsCard.productCd = tmCard.getProductCd();
			ctsCard.appNo = tmCard.getApplNbr();
			ctsCard.barcode = tmCard.getBarcode();
			ctsCard.bscSuppInd = tmCard.getBscSuppInd();
			ctsCard.bscLogiccardNo = tmCard.getCardBasicNbr();
			ctsCard.owningBranch = tmCard.getOwningBranch();
			ctsCard.appPromotionCd = tmCard.getApplPromoCode();
			ctsCard.recomName = tmCard.getRecomName();
			ctsCard.recomCardNo = tmCard.getRecomCardNo();
			ctsCard.setupDate = tmCard.getSetupDate();
			ctsCard.blockCode = tmCard.getBlockCode();
			ctsCard.activateInd = tmCard.getActiveInd();
			ctsCard.activateDate = tmCard.getActiveDate();
			ctsCard.cancelDate = tmCard.getCloseDate();
			ctsCard.latestCardNo = tmCard.getLastestMediumCardNbr();
			ctsCard.salesInd = tmCard.getSalesInd();
			ctsCard.appSource = tmCard.getApplSrc();
			ctsCard.representName = tmCard.getRepresentName();
			ctsCard.posPinVerifyInd = tmCard.getPosPinVerifyInd();
			ctsCard.relationshipToBsc = tmCard.getRelationshipToBsc();
			ctsCard.cardExpireDate = tmCard.getCardExpireDate();
			ctsCard.cardFeeRate = tmCard.getCardFeeRate();
			ctsCard.renewInd = tmCard.getRenewInd();
			ctsCard.renewRejectCd = tmCard.getRenewRejectCd();
			ctsCard.firstCardFeeDate = tmCard.getFirstCardFeeDate();
			ctsCard.lastRenewalDate = tmCard.getLastRenewcardDate();
			ctsCard.nextCardFeeDate = tmCard.getNextCardFeeDate();
			ctsCard.waiveCardfeeInd = tmCard.getWaiveCardfeeInd();
			ctsCard.cardFetchMethod = tmCard.getCardDeliverMethod();
			ctsCard.cardMailerInd = tmCard.getCardDeliverAddrFlag();
			
			item.getCtsCard().add(ctsCard);
		}
	}
	
	/**
	 * @see 方法名：makeCtsTxn 
	 * @see 描述：交易信息
	 * @see 创建日期：2015-6-24下午4:09:30
	 * @author ChengChun
	 *  
	 * @param item
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void makeCtsTxn(S9201MasterData item) {
		List<CcsTxnHst> listTxnHst = item.getListTxnHst();
		for (CcsTxnHst tmTxnHst : listTxnHst) {
			CcExportTxnItem ctsTxn = new CcExportTxnItem();
			
			ctsTxn.org = tmTxnHst.getOrg();
			ctsTxn.txnSeq = tmTxnHst.getTxnSeq();
			ctsTxn.custId = String.valueOf( item.getCustomer().getCustId() );
			ctsTxn.acctNo = String.valueOf( tmTxnHst.getAcctNbr() );
			ctsTxn.acctType = tmTxnHst.getAcctType();
			ctsTxn.cardNo = tmTxnHst.getCardNbr();
			ctsTxn.logicalCardNo = tmTxnHst.getLogicCardNbr();
			ctsTxn.bscLogicCardNo = tmTxnHst.getCardBasicNbr();
			ctsTxn.productCd = tmTxnHst.getProductCd();
			ctsTxn.txnCode = tmTxnHst.getTxnCode();
			ctsTxn.txnDate = tmTxnHst.getTxnDate();
			ctsTxn.txnTime = tmTxnHst.getTxnTime();
			ctsTxn.txnCurrCd = tmTxnHst.getTxnCurrency();
			ctsTxn.txnAmt = tmTxnHst.getTxnAmt();
			ctsTxn.postDate = tmTxnHst.getPostDate();
			ctsTxn.postCurrCd = tmTxnHst.getPostCurrency();
			ctsTxn.postAmt = tmTxnHst.getPostAmt();
			ctsTxn.postTxnType = tmTxnHst.getPostTxnType();
			ctsTxn.dbCrInd = tmTxnHst.getDbCrInd();
			ctsTxn.point = tmTxnHst.getPoints();
			ctsTxn.cardBlockCode = tmTxnHst.getCardBlockCode();
			ctsTxn.refNbr = tmTxnHst.getRefNbr();
			ctsTxn.txnDesc = tmTxnHst.getTxnDesc();
			ctsTxn.txnShortDesc = tmTxnHst.getTxnShortDesc();
			ctsTxn.mcc = tmTxnHst.getMcc();
			ctsTxn.stmtDate = tmTxnHst.getStmtDate();
			
			item.getCtsTxn().add(ctsTxn);
		}
	}
	
	/**
	 * @see 方法名：getUnPayMents 
	 * @see 描述：计算信用计划的总金额
	 * @see 创建日期：2015-6-24下午4:12:33
	 * @author ChengChun
	 *  
	 * @param item
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private BigDecimal getUnPayMents(S9201MasterData item){
		BigDecimal amt = BigDecimal.ZERO;
		List<CcsPlan> planList = item.getListPlan();
		if(planList != null && planList.size() > 0){
			
			for(CcsPlan plan : planList){
				
				switch(plan.getPlanType()){
				case C: 
				case R:
				case I:
					amt = amt.add(plan.getPastPrincipal())
						   .add(plan.getPastInterest())
						   .add(plan.getPastCardFee())
						   .add(plan.getPastOvrlmtFee())
						   .add(plan.getPastLateFee())
						   .add(plan.getPastNsfundFee())
						   .add(plan.getPastTxnFee())
						   .add(plan.getPastSvcFee())
						   .add(plan.getPastInsurance())
						   .add(plan.getPastUserFee1())
						   .add(plan.getPastUserFee2())
						   .add(plan.getPastUserFee3())
						   .add(plan.getPastUserFee4())
						   .add(plan.getPastUserFee5())
						   .add(plan.getPastUserFee6());
					break;
				default :
					break;
				}
			}
		}
		return amt;
	}
}
