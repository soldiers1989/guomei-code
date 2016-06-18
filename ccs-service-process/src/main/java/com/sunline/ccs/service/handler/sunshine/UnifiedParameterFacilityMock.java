package com.sunline.ccs.service.handler.sunshine;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sunline.acm.service.api.ParameterRefreshRequest;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.Fee;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.CycleBaseInd;
import com.sunline.ccs.param.def.enums.LoanPlanStatus;
import com.sunline.ccs.param.def.enums.PaymentDueDay;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.service.sdk.BMPParameterService;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanMold;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.ProductType;

/**
 * 统一参数管理类
 * 
 * @author alen
 *
 */
@Service
public class UnifiedParameterFacilityMock implements BMPParameterService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void refreshParameter(ParameterRefreshRequest request) {
		
	}
	
//	/**
//	 * 取一定参数的便利函数，如果不存在则抛异常
//	 */
//	public <T extends Serializable> T loadParameter(Object key, Class<T> clazz) {
//		
//		return param;
//	}
	
	public Product loadProduct(){
		Product product = new Product();
		product.productCode = "480001";
		product.cardnoRangeFlr = "9000";
		product.bin = "820";
		product.productType = ProductType.M;
		product.cardnoRangeCeil = "9900";
		return product;
		
	}
	public ProductCredit loadProductCredit(){
		ProductCredit productCredit = new ProductCredit();
//		productCredit.dfltCycleDay = null;
		Fee fee = new Fee();
		productCredit.fee = fee;
		productCredit.fee.firstCardFeeInd = null;
//		productCredit.maxCashLoanCnt = 1;
		productCredit.multiCashLoanInd = Indicator.N;
		return productCredit;
		
	}

	public AccountAttribute loadAcctAttr() {
		AccountAttribute acctAttr = new AccountAttribute();
    	acctAttr.accountType = AccountType.A;
    	acctAttr.cycleBaseInd = CycleBaseInd.M;
    	acctAttr.paymentDueDay = PaymentDueDay.C;
    	acctAttr.cycleBaseMult = 2;
    	acctAttr.ovrlmtRate = BigDecimal.valueOf(0.1); 
		return acctAttr;
	}

	public LoanPlan loanPlan() {
		LoanPlan loanPlan = new LoanPlan();
		loanPlan.loanCode = "0001";
		loanPlan.loanType = LoanType.MCEI;
		loanPlan.loanStaus = LoanPlanStatus.A;
		loanPlan.loanMold = LoanMold.S;
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");  
		try {
			loanPlan.loanValidity = format.parse("2020-12-31");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return loanPlan;
	}

	public LoanFeeDef loanFeeDef() {
		LoanFeeDef loanFeeDef = new LoanFeeDef();
		loanFeeDef.feeRate = BigDecimal.valueOf(0.1);
		loanFeeDef.maxAmount = BigDecimal.valueOf(1000000);
		loanFeeDef.minAmount = BigDecimal.valueOf(0);
		
		return loanFeeDef;
	}
}
