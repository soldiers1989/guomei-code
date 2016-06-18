package com.sunline.ccs.service.auth.context;

import com.sunline.ppy.api.MediumInfo;
import com.sunline.pcm.param.def.Mcc;
import com.sunline.pcm.param.def.Product;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.param.def.AuthProduct;
import com.sunline.ccs.param.def.CountryCtrl;
import com.sunline.ccs.param.def.CurrencyCtrl;
import com.sunline.ccs.param.def.MccCtrl;
import com.sunline.ccs.param.def.ProductCredit;

/**
 * 
 * @see 类名：AuthContext
 * @see 描述：授权上下文，用于缓存信息
 *
 * @see 创建日期：   2015年6月22日上午10:56:05
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class AuthContext {
	
	// 卡管信息
	private MediumInfo mediumInfo;

	// 中间业务信息
	private TxnInfo txnInfo;
	
	//分期信息
	private LoanInfo loanInfo;

	// 数据库加载数据
	private CcsAcctO account;

	private CcsCustomer customer;

	private CcsCardO card;

	// 参数
	private AuthProduct authProduct;

	private ProductCredit productCredit;

	private Product product;
	
	private CountryCtrl authCountry;

	private MccCtrl mccCtrl;

	private CurrencyCtrl currencyCtrl;
	
	private Mcc mcc;
	
	private CcsCustomerCrlmt custLimitO;
	
	private CupMsg message;
	
	public CupMsg getMessage() {
		return message;
	}

	public void setMessage(CupMsg message) {
		this.message = message;
	}

	public CcsCustomerCrlmt getCustLimitO() {
		return custLimitO;
	}

	public void setCustLimitO(CcsCustomerCrlmt custLimitO) {
		this.custLimitO = custLimitO;
	}

	public LoanInfo getLoanInfo() {
		return loanInfo;
	}

	public void setLoanInfo(LoanInfo loanInfo) {
		this.loanInfo = loanInfo;
	}

	public Mcc getMcc() {
		return mcc;
	}

	public void setMcc(Mcc mcc) {
		this.mcc = mcc;
	}

	public CcsCardO getCard() {
		return card;
	}

	public void setCard(CcsCardO card) {
		this.card = card;
	}

	public CountryCtrl getAuthCountry() {
		return authCountry;
	}

	public void setAuthCountry(CountryCtrl authCountry) {
		this.authCountry = authCountry;
	}

	public MccCtrl getMccCtrl() {
		return mccCtrl;
	}

	public void setMccCtrl(MccCtrl mccCtrl) {
		this.mccCtrl = mccCtrl;
	}

	public CurrencyCtrl getCurrencyCtrl() {
		return currencyCtrl;
	}

	public void setCurrencyCtrl(CurrencyCtrl currencyCtrl) {
		this.currencyCtrl = currencyCtrl;
	}

	public CcsAcctO getAccount() {
		return account;
	}

	public void setAccount(CcsAcctO account) {
		this.account = account;
	}

	 

	public MediumInfo getMediumInfo() {
		return mediumInfo;
	}

	public void setMediumInfo(MediumInfo mediumInfo) {
		this.mediumInfo = mediumInfo;
	}

	public ProductCredit getProductCredit() {
		return productCredit;
	}

	public void setProductCredit(ProductCredit productCredit) {
		this.productCredit = productCredit;
	}

	public CcsCustomer getCustomer() {
		return customer;
	}

	public void setCustomer(CcsCustomer customer) {
		this.customer = customer;
	}

	public AuthProduct getAuthProduct() {
		return authProduct;
	}

	public void setAuthProduct(AuthProduct authProduct) {
		this.authProduct = authProduct;
	}

	public TxnInfo getTxnInfo() {
		return txnInfo;
	}

	public void setTxnInfo(TxnInfo txnInfo) {
		this.txnInfo = txnInfo;
	}
	
	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

}
