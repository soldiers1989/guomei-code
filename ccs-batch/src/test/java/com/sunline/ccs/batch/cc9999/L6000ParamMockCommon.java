package com.sunline.ccs.batch.cc9999;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.BucketDef;
import com.sunline.ccs.param.def.InterestTable;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.RateDef;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.enums.IntAccumFrom;
import com.sunline.ccs.param.def.enums.LogicMod;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.ccs.param.def.enums.TierInd;
import com.sunline.ccs.param.def.enums.TxnType;
import com.sunline.pcm.param.def.CpdDef;
import com.sunline.pcm.param.def.Mulct;
import com.sunline.pcm.param.def.MulctDef;
import com.sunline.pcm.param.def.enums.MulctCollectMethod;
import com.sunline.pcm.param.def.enums.MulctMethod;
import com.sunline.pcm.service.sdk.ParameterServiceMock;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.PenaltyAccuBase;
import com.sunline.ppy.dictionary.enums.PlanType;

public class L6000ParamMockCommon {
	public void putParams(ParameterServiceMock parameterMock){
		LoanPlan lp = new LoanPlan();
		lp.penaltyAccuBase = PenaltyAccuBase.U;
		lp.loanCode = "3001";
		lp.loanType = LoanType.MCEI;
		Map<Integer, LoanFeeDef> loanFeeDefMap = new HashMap<Integer, LoanFeeDef>();
		LoanFeeDef loanFeeDef1=new LoanFeeDef();
		loanFeeDef1.mulctTableId="1";
		loanFeeDef1.replaceMulctTableId="2";
		loanFeeDef1.loanFeeDefId=1;
		loanFeeDefMap.put(1, loanFeeDef1);
		lp.loanFeeDefMap = loanFeeDefMap;
		parameterMock.putParameter(lp.loanCode, lp);

		PlanTemplate pt = new PlanTemplate();
		pt.planNbr = "001";
		pt.planType = PlanType.Q;
		Map<BucketType, BucketDef> map = new HashMap<BucketType,BucketDef>();
		BucketDef prin = new BucketDef();
		prin.intTableId = 2;
		prin.intWaive = true;
		prin.intAccumFrom = IntAccumFrom.P;
		map.put(BucketType.Pricinpal,prin);
		BucketDef interest = new BucketDef();
		interest.intTableId = 2;
		interest.intWaive = true;
		interest.intAccumFrom = IntAccumFrom.P;
		map.put(BucketType.Interest, interest);
		pt.intTableId = 1;
		pt.intParameterBuckets = map;
		pt.isAccruPrinSum = Indicator.Y;
		pt.multSaleInd = true;
		
		PlanTemplate pt2 = new PlanTemplate();
		pt2.planNbr = "002";
		pt2.planType = PlanType.P;
		Map<BucketType, BucketDef> map2 = new HashMap<BucketType,BucketDef>();
		BucketDef prin2 = new BucketDef();
		prin2.intTableId = 2;
		prin2.intWaive = true;
		map2.put(BucketType.Pricinpal,prin2);
		BucketDef interest2 = new BucketDef();
		interest2.intTableId = 2;
		interest2.intWaive = true;
		map2.put(BucketType.Interest, interest2);
		pt2.intParameterBuckets = map2;
		pt2.intTableId = 2;
		pt2.isAccruPrinSum = Indicator.Y;
		pt2.multSaleInd = true;
		
		parameterMock.putParameter(pt.planNbr, pt);
		parameterMock.putParameter(pt2.planNbr, pt2);
		
		InterestTable zero = new InterestTable();
		zero.intTableId = 2;
		zero.tierInd = TierInd.F;
		zero.baseYear = 365;
		List<RateDef> chargeRates2 = new ArrayList<RateDef>();
		RateDef rd2 = new RateDef();
		rd2.rate = BigDecimal.ZERO;
		rd2.rateCeil = new BigDecimal("999999999");
		chargeRates2.add(rd2);
		zero.chargeRates = chargeRates2;

		InterestTable eigthTeen = new InterestTable();
		eigthTeen.intTableId = 1;
		eigthTeen.tierInd = TierInd.F;
		eigthTeen.baseYear = 365;
		List<RateDef> chargeRates = new ArrayList<RateDef>();
		RateDef rd = new RateDef();
		rd.rate = new BigDecimal("0.1825");
		rd.rateCeil = new BigDecimal("999999999");
		chargeRates.add(rd);
		eigthTeen.chargeRates = chargeRates;
		
		parameterMock.putParameter(eigthTeen.intTableId+"",eigthTeen);
		parameterMock.putParameter(zero.intTableId+"",zero);
		
		//罚金表
		Mulct mulct1=new Mulct();
		mulct1.mulctTableId = "1";
		mulct1.baseYear=365;
//		mulct1.dpdToleLmt=new BigDecimal(5);
		mulct1.isReviewMulct=Indicator.Y;
		mulct1.mulctCalMethod=MulctCollectMethod.D;
//		mulct1.cpdToleLmt=new BigDecimal(5);
		mulct1.mulctMethod = MulctMethod.CPD;
		List<CpdDef> cpdList=new ArrayList<CpdDef>();
		CpdDef cpd1=new CpdDef();
		cpd1.cpdOverAmt=new BigDecimal(10);
		cpd1.cpdOverDays=5;
		CpdDef cpd2=new CpdDef();
		cpd2.cpdOverAmt=new BigDecimal(20);
		cpd2.cpdOverDays=10;
		cpdList.add(cpd1);
		cpdList.add(cpd2);
		
		MulctDef dpd1=new MulctDef();
		dpd1.mulctOverAmt=new BigDecimal(10);
		dpd1.mulctOverDays=5;
		MulctDef dpd2=new MulctDef();
		dpd2.mulctOverAmt=new BigDecimal(12);
		dpd2.mulctOverDays=10;
		List<MulctDef> dpdList=new ArrayList<MulctDef>();
		dpdList.add(dpd1);
		dpdList.add(dpd2);
		mulct1.cpdDefs = cpdList;
		mulct1.mulctDefs = dpdList;
		
		//代收罚金表
		List<CpdDef> cpdList2 = new ArrayList<CpdDef>();
		CpdDef cpd3 = new CpdDef();
		CpdDef cpd4 = new CpdDef();
		cpd3.cpdOverAmt=new BigDecimal(11);
		cpd3.cpdOverDays = 3;
		cpd4.cpdOverAmt=new BigDecimal(12);
		cpd4.cpdOverDays = 7;
		Mulct mulct2=new Mulct();
		mulct2.mulctTableId="2";
		mulct2.baseYear=365;
//		mulct2.dpdToleLmt=new BigDecimal(5);
		mulct2.isReviewMulct=Indicator.Y;
		mulct2.mulctCalMethod=MulctCollectMethod.D;
//		mulct2.cpdToleLmt=new BigDecimal(5);
		mulct2.mulctDefs = dpdList;
		mulct2.mulctMethod = MulctMethod.CPD;
		
		parameterMock.putParameter(mulct1.mulctTableId,mulct1);
		parameterMock.putParameter(mulct2.mulctTableId,mulct2);
		
		//罚金入账交易码
		SysTxnCdMapping mapping = new SysTxnCdMapping();
		mapping.sysTxnCd = SysTxnCd.S73;
		mapping.txnCd = "T73";
		
		TxnCd txnCd = new TxnCd();
		txnCd.txnCd = "T73";
		txnCd.logicMod = LogicMod.L40;
		txnCd.planType = PlanType.Q;
		txnCd.blkcdCheckInd = false;
		txnCd.bonusPntInd = Indicator.N;
		txnCd.feeWaiveInd = false;
		txnCd.stmtInd = false;
		txnCd.txnType = TxnType.T01;
		parameterMock.putParameter(mapping.sysTxnCd.toString(),mapping);
		parameterMock.putParameter(txnCd.txnCd, txnCd);
		
		//代收罚金入账交易码
		SysTxnCdMapping mapping2 = new SysTxnCdMapping();
		mapping2.sysTxnCd = SysTxnCd.D12;
		mapping2.txnCd = "D12";
		
		TxnCd txnCd2 = new TxnCd();
		txnCd2.txnCd = "D12";
		txnCd2.logicMod = LogicMod.L55;
		txnCd2.planType = PlanType.Q;
		txnCd2.blkcdCheckInd = false;
		txnCd2.bonusPntInd = Indicator.N;
		txnCd2.feeWaiveInd = false;
		txnCd2.stmtInd = false;
		txnCd2.txnType = TxnType.T01;
		
		parameterMock.putParameter(mapping2.sysTxnCd.toString(),mapping2);
		parameterMock.putParameter(txnCd2.txnCd, txnCd2);
		
		//罚金回溯交易码
		SysTxnCdMapping mapping3 = new SysTxnCdMapping();
		mapping3.sysTxnCd = SysTxnCd.S74;
		mapping3.txnCd = "S74";
		
		TxnCd txnCd3 = new TxnCd();
		txnCd3.txnCd = "S74";
		txnCd3.logicMod = LogicMod.L41;
		txnCd3.planType = PlanType.Q;
		txnCd3.blkcdCheckInd = false;
		txnCd3.bonusPntInd = Indicator.N;
		txnCd3.feeWaiveInd = false;
		txnCd3.stmtInd = false;
		txnCd3.txnType = TxnType.T01;
		parameterMock.putParameter(mapping3.sysTxnCd.toString(),mapping3);
		parameterMock.putParameter(txnCd3.txnCd, txnCd3);
		
		ProductCredit pc = new ProductCredit();
		pc.productCd = "1201";
		pc.financeOrgNo = "0000";
		pc.accountAttributeId = 1;
		parameterMock.putParameter(pc.productCd, pc);
		
		//账户参数
		AccountAttribute aa = new AccountAttribute();
		aa.accountAttributeId = 1;
		
		//优惠券入账交易码
		SysTxnCdMapping mapping4 = new SysTxnCdMapping();
		mapping4.sysTxnCd = SysTxnCd.C18;
		mapping4.txnCd = "C18";
		
		TxnCd txnCd4 = new TxnCd();
		txnCd4.txnCd = "C18";
		txnCd4.logicMod = LogicMod.L67;
		txnCd4.planType = PlanType.Q;
		txnCd4.blkcdCheckInd = false;
		txnCd4.bonusPntInd = Indicator.N;
		txnCd4.feeWaiveInd = false;
		txnCd4.stmtInd = false;
		txnCd4.txnType = TxnType.T01;
		parameterMock.putParameter(mapping4.sysTxnCd.toString(),mapping4);
		parameterMock.putParameter(txnCd4.txnCd, txnCd4);
	}
}
