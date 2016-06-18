package com.sunline.ccs.batch.cc6000;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.EarlyRepayDef;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.enums.CalcMethod;
import com.sunline.ccs.param.def.enums.CycleBaseInd;
import com.sunline.ccs.param.def.enums.PaymentDueDay;
import com.sunline.ccs.param.def.enums.PaymentIntervalUnit;
import com.sunline.ccs.param.def.enums.PrepaymentFeeMethod;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.pcm.param.def.FinancialOrg;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.param.def.Split;
import com.sunline.pcm.param.def.enums.SplitMethod;
import com.sunline.pcm.param.def.enums.SplitSort;
import com.sunline.pcm.service.sdk.ParameterServiceMock;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.Ownership;
@Service
public class BatchParameter {
	@Autowired
	private ParameterServiceMock parameterMock;
	
	public static ProductCredit genProductCredit() {
		ProductCredit pc = new ProductCredit();
		
		pc.productCd = "004101";
		pc.financeOrgNo = "123456";
		pc.claimsDays = 80;
		pc.accountAttributeId = 1;
		pc.postCurrCd="156";
		
		return pc;
	}
	public ProductCredit genMulProductCredit() {
		ProductCredit pc = new ProductCredit();
		pc.productCd = "000301";
		pc.financeOrgNo = "123456";
		pc.claimsDays = 80;
		pc.accountAttributeId = 1;
		pc.postCurrCd="156";
		parameterMock.putParameter(pc.productCd, pc);
		AccountAttribute aa=BatchParameter.genAccountAttribute();
		parameterMock.putParameter(pc.accountAttributeId+"", aa);
		pc.productCd = "000401";
		pc.financeOrgNo = "123456";
		pc.claimsDays = 80;
		pc.accountAttributeId = 1;
		pc.postCurrCd="156";
		parameterMock.putParameter(pc.productCd, pc);
		pc.productCd = "000421";
		pc.financeOrgNo = "123456";
		pc.claimsDays = 80;
		pc.accountAttributeId = 1;
		pc.postCurrCd="156";
		parameterMock.putParameter(pc.productCd, pc);
		pc.productCd = "001101";
		pc.financeOrgNo = "123456";
		pc.claimsDays = 80;
		pc.accountAttributeId = 1;
		pc.postCurrCd="156";
		parameterMock.putParameter(pc.productCd, pc);
		pc.productCd = "001102";
		pc.financeOrgNo = "123456";
		pc.claimsDays = 80;
		pc.accountAttributeId = 1;
		pc.postCurrCd="156";
		parameterMock.putParameter(pc.productCd, pc);
		pc.productCd = "003101";
		pc.financeOrgNo = "123456";
		pc.claimsDays = 80;
		pc.accountAttributeId = 1;
		pc.postCurrCd="156";
		parameterMock.putParameter(pc.productCd, pc);
		pc.productCd = "004101";
		pc.financeOrgNo = "123456";
		pc.claimsDays = 80;
		pc.accountAttributeId = 1;
		pc.postCurrCd="156";
		parameterMock.putParameter(pc.productCd, pc);
		pc.productCd = "004102";
		pc.financeOrgNo = "123456";
		pc.claimsDays = 80;
		pc.accountAttributeId = 1;
		pc.postCurrCd="156";
		parameterMock.putParameter(pc.productCd, pc);
		pc.productCd = "004103";
		pc.financeOrgNo = "123456";
		pc.claimsDays = 80;
		pc.accountAttributeId = 1;
		pc.postCurrCd="156";
		parameterMock.putParameter(pc.productCd, pc);
		return pc;
	}
	
	public LoanPlan genMulLoanPlan(){
		LoanPlan lp = new LoanPlan();
		Map<Integer, LoanFeeDef> lm= new HashMap<Integer, LoanFeeDef>();
		
		lp.loanCode="1101";
		lp.loanType = LoanType.MCEI;
		lp.ownership=Ownership.O;
		lm= new HashMap<Integer, LoanFeeDef>();
		lm.put(6, genLoanFeeDef());
		lp.loanFeeDefMap=lm;
		parameterMock.putParameter(lp.loanCode, lp);
		
		lp.loanCode="2001";
		lp.loanType = LoanType.MCEI;
		lp.ownership=Ownership.O;
		lm= new HashMap<Integer, LoanFeeDef>();
		lm.put(6, genLoanFeeDef());
		lp.loanFeeDefMap=lm;
		parameterMock.putParameter(lp.loanCode, lp);
		
		lp.loanCode="3101";
		lp.loanType = LoanType.MCEI;
		lp.ownership=Ownership.O;
		lm= new HashMap<Integer, LoanFeeDef>();
		lm.put(6, genLoanFeeDef());
		lp.loanFeeDefMap=lm;
		parameterMock.putParameter(lp.loanCode, lp);
		
		lp.loanCode="3001";
		lp.loanType = LoanType.MCAT;
		lp.ownership=Ownership.O;
		lm= new HashMap<Integer, LoanFeeDef>();
		lm.put(6, genLoanFeeDef());
		lp.loanFeeDefMap=lm;
		parameterMock.putParameter(lp.loanCode, lp);
		
		lp.loanCode="4101";
		lp.loanType = LoanType.MCAT;
		lp.ownership=Ownership.O;
		lm= new HashMap<Integer, LoanFeeDef>();
		lm.put(6, genLoanFeeDef());
		lp.loanFeeDefMap=lm;
		parameterMock.putParameter(lp.loanCode, lp);
		
		lp.loanCode="4102";
		lp.loanType = LoanType.MCAT;
		lp.ownership=Ownership.O;
		lm= new HashMap<Integer, LoanFeeDef>();
		lm.put(6, genLoanFeeDef());
		lp.loanFeeDefMap=lm;
		parameterMock.putParameter(lp.loanCode, lp);
		
		lp.loanCode="5001";
		lp.loanType = LoanType.MCEI;
		lp.ownership=Ownership.P;
		lm= new HashMap<Integer, LoanFeeDef>();
		lm.put(6, genLoanFeeDef());
		lp.loanFeeDefMap=lm;
		parameterMock.putParameter(lp.loanCode, lp);
		
		return lp;
	}
	
	public static PlanTemplate genPlanTemplate() {
		PlanTemplate pt = new PlanTemplate();
		pt.planNbr="111111";
		pt.pmtAssignInd=true;
		
		return pt;
	}
	
	public static FinancialOrg genFinancialOrg() {
		FinancialOrg finanicalOrg = new FinancialOrg();
		
		finanicalOrg.financialOrgNO = "123456";
		finanicalOrg.splitTableId = "001";
		
		return finanicalOrg;
	}
	
	public static LoanFeeDef genLoanFeeDef(){
		LoanFeeDef lf = new LoanFeeDef();
		lf.insCollMethod = LoanFeeMethod.E;
		lf.insCalcMethod = PrepaymentFeeMethod.R;
		lf.insRate = new BigDecimal(0.01);
		
		lf.lifeInsuFeeMethod = LoanFeeMethod.E;
		lf.lifeInsuFeeCalMethod = PrepaymentFeeMethod.A;
		lf.lifeInsuFeeAmt = new BigDecimal(50);
		
		lf.stampMethod = LoanFeeMethod.F;
		lf.stampCalcMethod = PrepaymentFeeMethod.R;
		lf.stampRate  = new BigDecimal(0.02);
		
//		lf.adCollectmethod = PrepaymentFeeMethod.A;
		lf.prepaymentFeeMethod = PrepaymentFeeMethod.A;
		List<EarlyRepayDef> ers = new ArrayList<EarlyRepayDef>();
		for(int i =1;i<4;i++){
			EarlyRepayDef er = new EarlyRepayDef();
			er.adCurPeriod = i;
			er.adFeeAmt =  new BigDecimal(i*10);
			ers.add(er);
		}
		
		lf.paymentIntervalUnit = PaymentIntervalUnit.M;
		lf.paymentIntervalPeriod = 1;
		lf.loanFeeCalcMethod = CalcMethod.R;
		lf.isOffsetRate=Indicator.Y;
		
		lf.earlyRepayDefs = ers;
		
		return lf;
	}
	
	public static SysTxnCdMapping genSysTxnCdMapping(){
		SysTxnCdMapping st = new SysTxnCdMapping();
		st.sysTxnCd=SysTxnCd.S73;
		st.txnCd = "T904";
		return st;
	}
	public static TxnCd genTxnCd(){
		TxnCd tc = new TxnCd();
		tc.txnCd="T904";
		return tc;
	}
	
	public static LoanPlan genLoanPlan(){
		LoanPlan lp = new LoanPlan();
		lp.loanCode="3001";
		lp.loanType = LoanType.MCEI;
		Map<Integer, LoanFeeDef> lm= new HashMap<Integer, LoanFeeDef>();
		lm.put(6, genLoanFeeDef());
		lp.loanFeeDefMap=lm;
		
		return lp;
	}
	
	public static AccountAttribute genAccountAttribute(){
		AccountAttribute aat = new AccountAttribute();
		aat.accountAttributeId =1;
		aat.pmtGracePrd = 0;
		aat.ovrlmtRate = BigDecimal.valueOf(0.1);
		aat.creditLimitAdjustInterval = 0;
		aat.paymentDueDay = PaymentDueDay.C;
		aat.cycleBaseInd = CycleBaseInd.M;
		aat.cycleBaseMult = 1;
		aat.pmtDueLtrPrd = "23";
		return aat;
	}
	
	public static Product genProduct(){
		Product pd = new Product();
		pd.productCode="004101";
		pd.cardnoRangeFlr = "000000000";
		pd.cardnoRangeCeil ="999999999";
		pd.bin="625156";
		
		return pd;
	}
	/**
	 * 按比例0.3拆分,最小金额50,升序
	 * @return
	 */
	public static Split genSplit(){
		Split split = new Split();
		
		split.splitTableId = "001";
		split.splitMethod = SplitMethod.A;
		split.splitRate = BigDecimal.valueOf(0.3);
		split.splitMinAMT = BigDecimal.valueOf(50);
		split.splitAmtSort = SplitSort.A;
		
//		split.splitMethod = SplitMethod.B; // 按固定金额
		split.splitAMT = BigDecimal.valueOf(333.33);
		
		return split;
	}
}
