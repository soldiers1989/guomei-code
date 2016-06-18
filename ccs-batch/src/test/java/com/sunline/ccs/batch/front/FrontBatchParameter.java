package com.sunline.ccs.batch.front;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sunline.ccs.param.def.EarlyRepayDef;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.PrepaymentFeeMethod;
import com.sunline.pcm.param.def.FinancialOrg;
import com.sunline.pcm.param.def.Split;
import com.sunline.pcm.param.def.enums.SplitMethod;
import com.sunline.pcm.param.def.enums.SplitSort;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanType;

public class FrontBatchParameter {
	
	public static ProductCredit genProductCredit() {
		ProductCredit pc = new ProductCredit();
		
		pc.productCd = "123";
		pc.financeOrgNo = "123456";
		pc.claimsDays = 80;
		// p4000
		Map<LoanType, String> loanPlansMap = new HashMap<LoanType, String>();
		loanPlansMap.put(LoanType.MCEI, "123");
		pc.loanPlansMap = loanPlansMap;
		
		return pc;
	}
	
	public static FinancialOrg genFinancialOrg() {
		FinancialOrg financialOrg = new FinancialOrg();
		
		financialOrg.financialOrgNO = "123456";
		financialOrg.acqAcceptorId = "123456";
		financialOrg.splitTableId = "001";
		
		financialOrg.isSettle = Indicator.Y;
		Calendar c = Calendar.getInstance();
		financialOrg.monthSettleDay = c.get(Calendar.DAY_OF_MONTH);
		financialOrg.settleStartDay = financialOrg.monthSettleDay + 1;
		financialOrg.settleEndDay = financialOrg.monthSettleDay + 1;
		financialOrg.adFeeScale = BigDecimal.valueOf(0.5);
		return financialOrg;
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
	
	public static LoanFeeDef genLoanFeeDef(){
		LoanFeeDef loanFeeDef = new LoanFeeDef();
		
		loanFeeDef.prepaymentFeeMethod = PrepaymentFeeMethod.A;
		
		List<EarlyRepayDef> earlyRepayDefs = new ArrayList<EarlyRepayDef>();
		loanFeeDef.earlyRepayDefs = earlyRepayDefs;
		
		return loanFeeDef;
	}
	
	public static LoanPlan genLoanPlan(){
		LoanPlan loanPlan = new LoanPlan();
		
		loanPlan.loanCode = "123";
		Map<Integer,LoanFeeDef> m = new HashMap<Integer, LoanFeeDef>();
		m.put(1, genLoanFeeDef());
		loanPlan.loanFeeDefMap = m;
		
		return loanPlan;
	}
}
