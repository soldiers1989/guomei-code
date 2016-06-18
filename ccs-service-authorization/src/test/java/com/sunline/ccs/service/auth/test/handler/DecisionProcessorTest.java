package com.sunline.ccs.service.auth.test.handler;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.Brand;
import com.sunline.ppy.dictionary.enums.CardClass;
import com.sunline.ppy.dictionary.enums.ExpiryDateFlag;
import com.sunline.ppy.dictionary.enums.IdType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.PasswordVerifyResult;
import com.sunline.ppy.dictionary.enums.ProductType;
import com.sunline.pcm.param.def.CurrencyCd;
import com.sunline.pcm.param.def.Mcc;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.service.sdk.ParameterServiceMock;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardThresholdCtrl;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.AuthMccStateCurrXVerify;
import com.sunline.ccs.param.def.AuthProduct;
import com.sunline.ccs.param.def.BlockCode;
import com.sunline.ccs.param.def.CountryCtrl;
import com.sunline.ccs.param.def.CurrencyCtrl;
import com.sunline.ccs.param.def.MccCtrl;
import com.sunline.ccs.param.def.MerchantTxnCrtl;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.ccs.param.def.enums.AuthFlagAction;
import com.sunline.ccs.param.def.enums.AuthReason;
import com.sunline.ccs.param.def.enums.AuthTransTerminal;
import com.sunline.ccs.param.def.enums.CheckType;
import com.sunline.ccs.param.def.enums.CycleBaseInd;
import com.sunline.ccs.param.def.enums.DelqDayInd;
import com.sunline.ccs.param.def.enums.DelqTolInd;
import com.sunline.ccs.param.def.enums.DirectDbIndicator;
import com.sunline.ccs.param.def.enums.DownpmtTolInd;
import com.sunline.ccs.param.def.enums.LimitType;
import com.sunline.ccs.param.def.enums.PaymentCalcMethod;
import com.sunline.ccs.param.def.enums.PaymentDueDay;
import com.sunline.ccs.param.def.enums.PostAvailiableInd;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.ccs.service.auth.context.TxnInfo;
import com.sunline.ccs.service.auth.frame.AuthServiceImpl;
import com.sunline.ccs.service.auth.handler.AuthCashHandler;
import com.sunline.ccs.service.auth.handler.AuthCashVoidHandler;
import com.sunline.ccs.service.auth.handler.AuthRetailHandler;
import com.sunline.ark.support.service.YakMessage;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-service.xml")
@Transactional
public class DecisionProcessorTest {

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private AuthServiceImpl authDecisionServiceImpl;

	@Autowired
	private AuthRetailHandler authRetailProcessor;

	@Autowired
	private AuthCashHandler authCashProcessor;

	@Autowired
	private AuthCashVoidHandler authCashVoidProcessor;

	@Autowired
	private ParameterServiceMock mock;

	private AuthContext context;

	private YakMessage msg;

	static DateFormat format = new SimpleDateFormat("yyyyMMdd");

	@Before
	public void setupData() {
		// 初始化参数

		msg = new YakMessage();
		msg.getBodyAttributes().put(2, "6200480204984198");
		msg.getBodyAttributes().put(3, "00");
		msg.getBodyAttributes().put(4, "5000");
		msg.getHeadAttributes().put(11, "0100");
		msg.getBodyAttributes().put(14, "456");
		msg.getBodyAttributes().put(18, "1001");
		msg.getBodyAttributes().put(19, "111");
		msg.getBodyAttributes().put(22, "07");
		msg.getBodyAttributes().put(23, "");
		msg.getBodyAttributes().put(35,"1234567890123456789012345678901234567890");
		msg.getBodyAttributes().put(38, "600001");
		msg.getBodyAttributes().put(42, "6011");
		msg.getBodyAttributes().put(48, "ASAA0101234567890");
		msg.getBodyAttributes().put(49, "156");
		msg.getBodyAttributes().put(52, "0");
		msg.getBodyAttributes().put(60, "1234567555555901234567890");

		msg.getCustomAttributes().put("cardActiveFlag", Indicator.Y);
		msg.getCustomAttributes().put("blockCodes", "1234ABCD");
		msg.getCustomAttributes().put("expiryDateFlag", ExpiryDateFlag.Space);
		msg.getCustomAttributes().put("logicCardNbr", "6200480204984198");
		msg.getCustomAttributes().put("passwordVerifyResult",
				PasswordVerifyResult.Approve);
		msg.getCustomAttributes().put("com.sunline.fms.business.date",
				new Date());

		context = new AuthContext();

		ProductCredit product = new ProductCredit();

		product.accountAttributeId = 1;
		product.athMatchTolRt = BigDecimal.valueOf(0.05);
		product.cotbInclCrath = true;
		product.cotbInclCrbal = true;
		product.cvvTry = 3;
		product.dfltCycleDay = 10;
		product.dualAccountAttributeId = 2;
		// product.dualFee = genFee();
		// product.dualLatePaymentCharge = genLatePaymentCharge();
		// product.dualOverlimitCharge = genOverlimitCharge();
		// product.fee = genFee();
		// product.latePaymentCharge = genLatePaymentCharge();
		product.otbInclCrath = true;
		product.otbInclCrbal = true;
		product.otbInclDspt = false;
		product.otbInclFrzn = false;
		// product.overlimitCharge = genOverlimitCharge();
		product.pinTry = 3;
		product.preathCompTolRt = BigDecimal.valueOf(0.05);
		product.preathRtnPrd = 30;
		product.productCd = "1001";
		product.purchasePinInd = true;
		product.unmatchCrRtnPrd = 30;
		product.unmatchDbRtnPrd = 30;

		//	FIXME 以下属性从ProductCredit移至Product 
		Product prod = new Product();
		prod.productCode = "1001";
		//	FIXME 补充其它参数
		prod.description = "默认产品0000";
		prod.brand = Brand.C;
		prod.cardClass = CardClass.N;
		prod.productType = ProductType.C;
		
		mock.putParameter("1001", product);

		AccountAttribute attr1 = new AccountAttribute();
		attr1.accountAttributeId = 1;
		attr1.accountType = AccountType.C;
		attr1.cashLimitRate = BigDecimal.valueOf(0.5);
		attr1.collMinpmt = BigDecimal.TEN;
		attr1.collOnAge = "1";
		attr1.collOnFsDlq = true;
		attr1.collOnOvrlmt = true;
		attr1.cycleBaseInd = CycleBaseInd.M;
		attr1.cycleBaseMult = 1;
		attr1.delqDayInd = DelqDayInd.C;
		attr1.delqLtrPrd = 5;
		attr1.delqTol = BigDecimal.valueOf(0.02);
		attr1.delqTolInd = DelqTolInd.A;
		attr1.delqTolPerc = BigDecimal.ZERO;
		attr1.directDbDate = null;
		attr1.directDbDays = 1;
		attr1.directDbInd = DirectDbIndicator.P;
		attr1.downpmtTol = BigDecimal.valueOf(10);
		attr1.downpmtTolInd = DownpmtTolInd.R;
		attr1.downpmtTolPerc = BigDecimal.valueOf(0.01);
		attr1.loanLimitRate = BigDecimal.valueOf(0.5);
		attr1.ltrOnContDlq = true;
		attr1.ovrlmtRate = BigDecimal.valueOf(0.05);
		attr1.paymentDueDay = PaymentDueDay.D;
		attr1.pmtDueDate = null;
		attr1.pmtDueDays = 10;
		attr1.pmtDueLtrPrd = "2";
		attr1.pmtGracePrd = 2;
		attr1.stmtMinBal = BigDecimal.ZERO;
		attr1.stmtOnBpt = true;
		attr1.crMaxbalNoStmt = BigDecimal.valueOf(10);
		attr1.tlExpPrmptPrd = 1;
		mock.putParameter("1", attr1);

		// AccountAttributeId = 2
		AccountAttribute attr2 = new AccountAttribute();
		attr2.accountAttributeId = 2;
		attr2.accountType = AccountType.D;
		attr2.cashLimitRate = BigDecimal.valueOf(0.5);
		attr2.collMinpmt = BigDecimal.TEN;
		attr2.collOnAge = "1";
		attr2.collOnFsDlq = true;
		attr2.collOnOvrlmt = true;
		attr2.cycleBaseInd = CycleBaseInd.M;
		attr2.cycleBaseMult = 1;
		attr2.delqDayInd = DelqDayInd.C;
		attr2.delqLtrPrd = 5;
		attr2.delqTol = BigDecimal.valueOf(0.02);
		attr2.delqTolInd = DelqTolInd.A;
		attr2.delqTolPerc = BigDecimal.ZERO;
		attr2.directDbDate = null;
		attr2.directDbDays = 1;
		attr2.directDbInd = DirectDbIndicator.P;
		attr2.downpmtTol = BigDecimal.valueOf(10);
		attr2.downpmtTolInd = DownpmtTolInd.R;
		attr2.downpmtTolPerc = BigDecimal.valueOf(0.01);
		attr2.loanLimitRate = BigDecimal.valueOf(0.5);
		attr2.ltrOnContDlq = true;
		attr2.ovrlmtRate = BigDecimal.valueOf(0.05);
		attr2.paymentDueDay = PaymentDueDay.D;
		attr2.pmtDueDate = null;
		attr2.pmtDueDays = 10;
		attr2.pmtDueLtrPrd = "2";
		attr2.pmtGracePrd = 2;
		attr2.stmtMinBal = BigDecimal.ZERO;
		attr2.stmtOnBpt = true;
		attr2.crMaxbalNoStmt = BigDecimal.valueOf(10);
		attr2.tlExpPrmptPrd = 1;
		mock.putParameter("2", attr2);

		CurrencyCd currency1 = new CurrencyCd();
		currency1.currencyCd = "156";
		currency1.conversionRt = BigDecimal.ZERO;
		currency1.exponent = 2;
		mock.putParameter("156", currency1);

	 

		AuthProduct authProduct = new AuthProduct();
		authProduct.productCode = "1001";
		Map<AuthReason, AuthAction> reasonA = new HashMap<AuthReason, AuthAction>();
		reasonA.put(AuthReason.S001, AuthAction.D);
		reasonA.put(AuthReason.S008, AuthAction.C);
		reasonA.put(AuthReason.S009, AuthAction.D);
		reasonA.put(AuthReason.S007, AuthAction.D);
		reasonA.put(AuthReason.S006, AuthAction.D);
		authProduct.reasonActions = reasonA;

		Map<AuthTransTerminal, AuthFlagAction> terminals = new HashMap<AuthTransTerminal, AuthFlagAction>();
		terminals.put(AuthTransTerminal.ATM, AuthFlagAction.Yes);
		terminals.put(AuthTransTerminal.CS, AuthFlagAction.Yes);
		terminals.put(AuthTransTerminal.IVR, AuthFlagAction.Yes);
		authProduct.terminalEnabled = terminals;

		Map<AuthTransTerminal, Boolean> ters = new HashMap<AuthTransTerminal, Boolean>();
		ters.put(AuthTransTerminal.ATM, true);
		ters.put(AuthTransTerminal.CS, false);
		ters.put(AuthTransTerminal.EB, true);
		ters.put(AuthTransTerminal.HOST, false);
		ters.put(AuthTransTerminal.IVR, true);

		Map<AuthTransType, Map<AuthTransTerminal, Boolean>> tyTs = new HashMap<AuthTransType, Map<AuthTransTerminal, Boolean>>();
		tyTs.put(AuthTransType.Inq, ters);
		tyTs.put(AuthTransType.Cash, ters);
		tyTs.put(AuthTransType.Auth, ters);
		tyTs.put(AuthTransType.PreAuth, ters);
		authProduct.transTypeTerminalEnabled = tyTs;

		Map<CheckType, Boolean> cks = new HashMap<CheckType, Boolean>();
		//cks.put(CheckType.AtmVerify, true);
		//cks.put(CheckType.CashVerify, true);
		cks.put(CheckType.CheckCycleLimit, true);
		cks.put(CheckType.CountryCodeCheckFlag, true);
		authProduct.checkEnabled = cks;
		mock.putParameter("1001", authProduct);

		BlockCode code2 = new BlockCode();
		code2.blockCode = "2";
		code2.priority = 15;
		code2.postInd = PostAvailiableInd.A;
		code2.renewInd = true;
		code2.intAccuralInd = true;
		code2.intWaiveInd = false;
		code2.txnFeeWaiveInd = false;
		code2.cardFeeWaiveInd = false;
		code2.ovrlmtFeeWaiveInd = false;
		code2.lateFeeWaiveInd = false;
		code2.stmtInd = true;
		code2.paymentInd = PaymentCalcMethod.N;
		code2.pointEarnInd = true;
		code2.loanInd = true;
		code2.collectionInd = false;
		code2.authReason = AuthReason.S007;
		code2.letterCd = "000001";
		code2.cashAction = AuthAction.D;
		code2.nonMotoRetailAction = AuthAction.D;
		code2.motoElecAction = AuthAction.D;
		code2.motoRetailAction = AuthAction.D;
		code2.inquireAction = AuthAction.D;
		code2.debitAdjustAction = AuthAction.D;
		mock.putParameter("2", code2);

		// code = 3
		BlockCode code3 = new BlockCode();
		code3.blockCode = "3";
		code3.priority = 26;
		code3.postInd = PostAvailiableInd.A;
		code3.renewInd = true;
		code3.intAccuralInd = true;
		code3.intWaiveInd = false;
		code3.txnFeeWaiveInd = false;
		code3.cardFeeWaiveInd = false;
		code3.ovrlmtFeeWaiveInd = false;
		code3.lateFeeWaiveInd = false;
		code3.stmtInd = true;
		code3.paymentInd = PaymentCalcMethod.N;
		code3.pointEarnInd = true;
		code3.loanInd = true;
		code3.collectionInd = false;
		code3.authReason = AuthReason.S008;
		code3.letterCd = "000001";
		code3.cashAction = AuthAction.C;
		code3.nonMotoRetailAction = AuthAction.C;
		code3.motoElecAction = AuthAction.C;
		code3.motoRetailAction = AuthAction.C;
		code3.inquireAction = AuthAction.C;
		code3.debitAdjustAction = AuthAction.C;
		mock.putParameter("3", code3);

		// code = 4
		BlockCode code4 = new BlockCode();
		code4.blockCode = "4";
		code4.priority = 26;
		code4.postInd = PostAvailiableInd.A;
		code4.renewInd = true;
		code4.intAccuralInd = true;
		code4.intWaiveInd = false;
		code4.txnFeeWaiveInd = false;
		code4.cardFeeWaiveInd = false;
		code4.ovrlmtFeeWaiveInd = false;
		code4.lateFeeWaiveInd = false;
		code4.stmtInd = true;
		code4.paymentInd = PaymentCalcMethod.N;
		code4.pointEarnInd = true;
		code4.loanInd = true;
		code4.collectionInd = false;
		code4.authReason = AuthReason.S009;
		code4.letterCd = "000001";
		code4.cashAction = AuthAction.D;
		code4.nonMotoRetailAction = AuthAction.D;
		code4.motoElecAction = AuthAction.D;
		code4.motoRetailAction = AuthAction.D;
		code4.inquireAction = AuthAction.D;
		code4.debitAdjustAction = AuthAction.D;
		mock.putParameter("4", code4);

		// code = 5
		BlockCode code5 = new BlockCode();
		code5.blockCode = "5";
		code5.priority = 26;
		code5.postInd = PostAvailiableInd.A;
		code5.renewInd = true;
		code5.intAccuralInd = true;
		code5.intWaiveInd = false;
		code5.txnFeeWaiveInd = false;
		code5.cardFeeWaiveInd = false;
		code5.ovrlmtFeeWaiveInd = false;
		code5.lateFeeWaiveInd = false;
		code5.stmtInd = true;
		code5.paymentInd = PaymentCalcMethod.N;
		code5.pointEarnInd = true;
		code5.loanInd = true;
		code5.collectionInd = false;
		code5.authReason = AuthReason.S006;
		code5.letterCd = "000001";
		code5.cashAction = AuthAction.P;
		code5.nonMotoRetailAction = AuthAction.P;
		code5.motoElecAction = AuthAction.P;
		code5.motoRetailAction = AuthAction.P;
		code5.inquireAction = AuthAction.P;
		code5.debitAdjustAction = AuthAction.P;
		mock.putParameter("5", code5);

		CountryCtrl countryCtrl = new CountryCtrl();
		countryCtrl.countryCode = "111";
		mock.putParameter("111", countryCtrl);

		MccCtrl mccCtrl = new MccCtrl();
		mccCtrl.mcc = "1001";
		mock.putParameter("1001", mccCtrl);

		Mcc mcc = new Mcc();
		mcc.mcc = "1001";
		mcc.mccType = "A";
		mock.putParameter("1001", mcc);

		CurrencyCtrl currencyCtrl = new CurrencyCtrl();
		currencyCtrl.currencyCd = "156";
		mock.putParameter("156", currencyCtrl);

		AuthMccStateCurrXVerify authMccStateCurrXVerity = new AuthMccStateCurrXVerify();
		//authMccStateCurrXVerity.brand = "CUP";
		authMccStateCurrXVerity.mccCode = "1001";
		authMccStateCurrXVerity.countryCode = "111";
		authMccStateCurrXVerity.transCurrencyCode = "156";
		mock.putParameter("CUP|1001|111|156", authMccStateCurrXVerity);
		
		MerchantTxnCrtl merchantTxnCrtl = new MerchantTxnCrtl();
		merchantTxnCrtl.merchantId = "6011";
		mock.putParameter("6011", merchantTxnCrtl);

		TxnInfo txnInfo = new TxnInfo();
		txnInfo.setChbCurr("156");
		txnInfo.setChbTransAmt(BigDecimal.valueOf(100));
		txnInfo.setTransType(AuthTransType.Cash);
		txnInfo.setTransTerminal(AuthTransTerminal.OTC);
		txnInfo.setCustomerOTB(BigDecimal.valueOf(50000));
		txnInfo.setAccountOTB(BigDecimal.valueOf(40000));
		txnInfo.setTransType(AuthTransType.Cash);

		CcsCustomer customer = new CcsCustomer();
		customer.setIdType(IdType.I);
		customer.setIdNo("310322198807222821");
		customer.setName("张三三");
		em.persist(customer);
		msg.getCustomAttributes().put("custId", customer.getCustId());

		CcsCustomerCrlmt limto = new CcsCustomerCrlmt();
		limto.setCustLmtId(customer.getCustId());
//		limto.setBalloonLimit(BigDecimal.valueOf(5000));
		limto.setCreditLmt(BigDecimal.valueOf(5000));
//		limto.setCreditLmtRmb(BigDecimal.valueOf(5000));
//		limto.setCreditLmtUsd(BigDecimal.valueOf(5000));
		limto.setLmtCalcMethod(LimitType.H);
		em.persist(limto);
		
		
		CcsAcctO account = new CcsAcctO();
//		account.setAcctNbr(1000000001);
		account.setAcctType(AccountType.C);
		account.setMemoCash(BigDecimal.valueOf(100));
		account.setMemoCr(BigDecimal.valueOf(100));
		account.setMemoDb(BigDecimal.valueOf(100));
		account.setCurrBal(BigDecimal.valueOf(20000));
		account.setCustId(customer.getCustId());
		account.setCycleDay("2");
		account.setBlockCode("245");

		em.persist(account);

		CcsAcctO account2 = new CcsAcctO();
//		account2.setAcctNbr(1000000002);
		account2.setAcctType(AccountType.D);
		account2.setMemoCash(BigDecimal.valueOf(100));
		account2.setMemoCr(BigDecimal.valueOf(100));
		account2.setMemoDb(BigDecimal.valueOf(100));
		account2.setCurrBal(BigDecimal.valueOf(20000));
		account2.setCustId(customer.getCustId());
		em.persist(account2);

		CcsCardO card = new CcsCardO();
		card.setLogicCardNbr("6200480204984198");
//		card.setCustId(111111111);
//		card.setCustLmtId(222222);
		card.setProductCd("1001");
		card.setDayUsedAtmAmt(BigDecimal.valueOf(100));
		card.setDayUsedAtmNbr(10);
		card.setDayUsedCashAmt(BigDecimal.valueOf(100));
		card.setDayUsedCashNbr(10);
		card.setDayUsedRetailAmt(BigDecimal.valueOf(100));
		card.setDayUsedRetailNbr(10);
		card.setCtdCashAmt(BigDecimal.valueOf(100));
		card.setCtdUsedAmt(BigDecimal.valueOf(100));
//		card.setAcctNbr(1000000001);
		card.setLastUpdateBizDate(new Date());
		card.setBlockCode("3");

		em.persist(card);

		CcsCardThresholdCtrl cardOvd = new CcsCardThresholdCtrl();
		cardOvd.setDayAtmOvriInd(Indicator.N);
		cardOvd.setDayCashOvriInd(Indicator.N);
		cardOvd.setDayRetlOvriInd(Indicator.N);
		cardOvd.setDayXfroutOvriInd(Indicator.N);
		cardOvd.setLogicCardNbr("6200480204984198");
		em.persist(cardOvd);

		CcsAuthmemoO unmatchO = new CcsAuthmemoO();
		unmatchO.setMti("0100");
		unmatchO.setB002CardNbr("6200480204984198");// request.getBody(2)主账号
		unmatchO.setAuthCode("600001");// request.getBody(38)授权标识应答码
		unmatchO.setB042MerId("123456789012345");// request.getBody(42)受卡方标识码
		unmatchO.setLogBizDate(new Date());
		unmatchO.setTxnAmt(BigDecimal.valueOf(100));// getChbTransAmt
		unmatchO.setAuthTxnStatus(AuthTransStatus.N);
		unmatchO.setFinalAction(AuthAction.A);
//		unmatchO.setLogKv(Integer.valueOf(100001));
		unmatchO.setOrigTxnType(AuthTransType.Cash);
		em.persist(unmatchO);

	}

	@Test
	public void TestDecisionProcess() {

		// OrganizationContextHolder.setCurrentOrg("1234");
		authDecisionServiceImpl.authorize(msg);

	}

}
