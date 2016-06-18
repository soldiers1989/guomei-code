package com.sunline.ccs.service.auth.test;


import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import antlr.collections.List;

import com.sunline.ppy.api.MediumInfo;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.BscSuppIndicator;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.enums.PasswordVerifyResult;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.param.def.AuthProduct;
import com.sunline.ccs.param.def.LoanMerchant;
import com.sunline.ccs.param.def.MerchantTxnCrtl;
import com.sunline.ccs.param.def.Program;
import com.sunline.ccs.param.def.ProgramFeeDef;
import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.ccs.param.def.enums.AuthReason;
import com.sunline.ccs.param.def.enums.AuthTransTerminal;
import com.sunline.ccs.param.def.enums.AuthVerifyAction;
import com.sunline.ccs.param.def.enums.AutoType;
import com.sunline.ccs.param.def.enums.CheckType;
import com.sunline.ccs.param.def.enums.CtrlListInd;
import com.sunline.ccs.param.def.enums.ProgramStatus;
import com.sunline.ccs.param.def.enums.VerifyEnum;
import com.sunline.ccs.service.auth.context.CupMsg;
import com.sunline.ccs.service.auth.context.LoanInfo;
import com.sunline.ccs.service.auth.context.TxnInfo;
import com.sunline.ark.support.service.YakMessage;

public class RuleTest {

	public static void main(String[] args) throws IOException, ParseException {

		RuleEngine pointRuleEngine = new RuleEngineImpl();
		System.out.println("初始化规则引擎...\n");
		pointRuleEngine.initEngine();

		final LoanInfo l = getLoan();
		final TxnInfo t = getTransInfo();
		final CcsAcctO a = getAccount();
		final CcsCardO c = getCard();
		final CupMsg cm = getCup();
		
		final Logger logger = LoggerFactory.getLogger(RuleTest.class);
		final MerchantTxnCrtl crtl = getMerchantTxnCrtl();
		final AuthProduct authPr = getAuthProduct();
		final MediumInfo mi = new MediumInfo();
		mi.setCvv2VerifyResult(PasswordVerifyResult.NoThisField);
		
		pointRuleEngine.executeRuleEngine(l, t, a, c, cm,logger,crtl,authPr,mi);
		System.out.println("\n结束");

	}

	private static AuthProduct getAuthProduct() {
		final AuthProduct authPr = new AuthProduct();
		HashMap<AuthReason, AuthAction> map = new HashMap<AuthReason, AuthAction>();
		map.put(AuthReason.TF03, AuthAction.D);
		map.put(AuthReason.TF02, AuthAction.C);
		map.put(AuthReason.B003, AuthAction.A);
		authPr.reasonActions = map;
		
		Map<CheckType, Boolean> checkEnabled = new HashMap<CheckType, Boolean>();
		checkEnabled.put(CheckType.MerchantRestrictFlag,true);
		authPr.checkEnabled = checkEnabled;
		
		Map<VerifyEnum, AuthVerifyAction> verifyActions = new HashMap<VerifyEnum, AuthVerifyAction>();
		verifyActions.put(VerifyEnum.CardNotMotoVerifyCvv2,AuthVerifyAction.Must);
		verifyActions.put(VerifyEnum.CardNotElectronVerifyCvv2,AuthVerifyAction.Must);
		authPr.verifyActions = verifyActions;
		
		return authPr;
	}

	public static LoanInfo getLoan() throws ParseException {
		BigDecimal max1 = new BigDecimal(1000);
		BigDecimal max2 = new BigDecimal(100);
		BigDecimal min1 = new BigDecimal(1);
		BigDecimal min2 = new BigDecimal(10);
		final LoanInfo l = new LoanInfo();
		LoanMerchant m = new LoanMerchant();
		m.merId = "1234567890";
		m.posLoanSupportInd = Indicator.N;
		m.eBankLoanSupportInd= Indicator.Y;
		m.motoLoanSupportInd =Indicator.N;
		Program p = new Program();
		p.programMaxAmount = max1;
		p.programMinAmount = min1;
		Map<Integer, ProgramFeeDef> map = new HashMap<Integer, ProgramFeeDef>();
		ProgramFeeDef pfd = new ProgramFeeDef();
		pfd.minAmount = min2;
		pfd.maxAmount = max2;
		map.put(3, pfd);
		p.programFeeDef = map;
		p.programStatus = ProgramStatus.I;
		p.programStartDate = SimpleDateFormat.getDateInstance().parse(
				"2000-01-01");
		p.programEndDate = SimpleDateFormat.getDateInstance().parse(
				"2010-01-01");
		p.ctrlBranchList = Arrays.asList("1234567890");
		p.ctrlBranchInd = CtrlListInd.A;
		p.ctrlProdCreditInd=CtrlListInd.A;
		p.ctrlProdCreditList = Arrays.asList("1234");
		p.ctrlMccInd = CtrlListInd.A;
		p.ctrlMccList = Arrays.asList("0156");
		p.programMerList = Arrays.asList("123456");
		l.setLoanInitTerm(3);
		l.setProgramId("2004");
		l.setMerchantId("12345");
		l.setProgram(p);
		l.setLoanMerchant(m);
		return l;
	}

	public static TxnInfo getTransInfo() throws ParseException {
		TxnInfo t = new TxnInfo();
		t.setChbTransAmt(new BigDecimal(0));
		t.setBizDate(SimpleDateFormat.getDateInstance().parse("2015-01-01"));
		t.setTransTerminal(AuthTransTerminal.PHE);
		t.setInputSource(InputSource.CUP);
		t.setAutoType(AutoType.NoCardSelfService);
		t.setAuthVerifyType("9");
		t.setTransType(AuthTransType.Auth);
		
		ArrayList<String> cl = new ArrayList<String>();
		cl.add("Ass_022");
		cl.add("Ass_023");
		cl.add("Ass_024");

		cl.add("Sec_015");
		cl.add("Sec_016");
		cl.add("Sec_017");
		
		t.setCheckList(cl);
		return t;
	}

	public static CcsAcctO getAccount() {
		CcsAcctO a = new CcsAcctO();
		a.setOwningBranch("1234567890");
		return a;
	}

	public static CcsCardO getCard() {
		CcsCardO	c = new CcsCardO();
		c.setProductCd("1234");
		c.setBscSuppInd(BscSuppIndicator.S);
		return c;
	}

	public static CupMsg getCup() {
		YakMessage message = new YakMessage();
		message.getBodyAttributes().put(18, "0156");
		message.getBodyAttributes().put(61, "00000000000000000000000000000000CUPAM1111111111111111003123003456000678003111");
//		message.getBodyAttributes().put(18, "0156");
		CupMsg c = new CupMsg(message);
		System.out.println("dyn_code"+c.getF061_6_AM_DYN_CODE());
		System.out.println("mobi_nbr"+c.getF061_6_AM_MOBI_NBR());
		return c;
	}
	
	public static MerchantTxnCrtl getMerchantTxnCrtl() throws ParseException {
		MerchantTxnCrtl ctl = new MerchantTxnCrtl();
		ctl.forceMotoRetailCvv2Ind = Indicator.Y;
		return ctl;
	}
}
