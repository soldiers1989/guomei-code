package com.sunline.ccs.service.auth.test;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ccs.param.def.Program;
import com.sunline.ccs.param.def.ProgramFeeDef;
import com.sunline.ccs.service.auth.common.AuthCommService;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.ccs.service.auth.context.LoanInfo;
import com.sunline.ccs.service.auth.context.TxnInfo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-service.xml")
@Transactional
public class SeviceTest {
	
	@Before
	public void setup() throws ParseException{
	}
	
	@Test
	public void TestAuth(){
	}
	
	public static void main(String[] args) {
//		BigDecimal a = new BigDecimal("11");
//		
//		System.out.println(new BigDecimal(100).subtract(new BigDecimal(10).multiply(new BigDecimal(4-1))).setScale(2));
//		System.out.println(BigDecimal.ZERO.setScale(2));
//		System.out.println(a.movePointRight(2));
//		System.out.println(StringUtils.leftPad(a.toString(), 12, "0"));
//		System.out.println(StringUtils.leftPad(a.divide(new BigDecimal(3),0,BigDecimal.ROUND_HALF_UP).setScale(2).movePointRight(2).toString(),12,"0"));
		AuthCommService authCommonService = new AuthCommService();
		LoanInfo l = new LoanInfo();
		Program p = new Program();
//		p.loanFeeMethod = LoanFeeMethod.F;
		ProgramFeeDef pfd = new ProgramFeeDef();
		pfd.merFeeRate = new BigDecimal(0.5000);
		pfd.feeRate = new BigDecimal(0.5);
		Map<Integer, ProgramFeeDef> programFeeDef = new HashMap<Integer, ProgramFeeDef>();
		programFeeDef.put(3, pfd);
		p.programFeeDef = programFeeDef;
		l.setLoanFeeMethod(LoanFeeMethod.E);
		l.setProgram(p);
		l.setLoanInitTerm(3);
		
//		l.setFee(new BigDecimal("99.09"));
//		l.setFirstFee(new BigDecimal("99"));
//		l.setFinalFee(new BigDecimal("0"));
//		l.setFixedFee(new BigDecimal("0"));
//		
//		System.out.println(getB057Loan(l));
		
		AuthContext context = new AuthContext();
		TxnInfo t = new TxnInfo();
		t.setChbCurr("156");
		t.setChbTransAmt(new BigDecimal(1000));
		context.setTxnInfo(t);
		
	//	System.out.println(authCommonService.getB057Loan(l,t));
	}
}
