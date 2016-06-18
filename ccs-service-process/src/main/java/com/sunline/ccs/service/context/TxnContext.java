package com.sunline.ccs.service.context;

import java.util.List;

import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanRegHst;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsOrderHst;
import com.sunline.ccs.infrastructure.shared.model.CcsSettleClaim;
import com.sunline.ccs.infrastructure.shared.model.CcsSettleLoanHst;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnPost;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.AuthProduct;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.service.entity.SunshineRequestInfo;
import com.sunline.ccs.service.msentity.MsRequestInfo;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ccs.service.payEntity.CommResp;
import com.sunline.pcm.param.def.Product;
import com.sunline.ppy.dictionary.enums.MsPayfrontError;

/**
 * 
 * @see 类名：TxnContext
 * @see 描述：授权上下文，用于缓存信息
 *
 * @author wangz
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class TxnContext {
	
	// 中间业务信息
	private TxnInfo txnInfo;
	
	//分期信息
	private LoanInfo loanInfo;
	
	private CcsLoan loan;

	private CcsLoan origLoan;
	
	private CcsLoanReg loanReg;
	
	private CcsLoanRegHst loanRegHst;
	
	private CcsLoanReg origLoanReg;

	private CcsAcct account;

	// 数据库加载数据
	private CcsAcctO accounto;

	private CcsCustomer customer;

	// 参数
	private AuthProduct authProduct;

	private ProductCredit productCredit;

	private Product product;
	
	private CcsCustomerCrlmt custLimitO;
	
	private SunshineRequestInfo sunshineRequestInfo;
	
	private CcsOrder order;
	
	private CcsOrder origOrder;
	
	private CcsOrderHst origOrderHst;

	private CcsAuthmemoO authmemo;
	
	private CcsAuthmemoO origAuthmemo;

	//理赔结清
	private CcsSettleClaim claim;
	
	private CcsCard card;
	private CcsCardO cardo;
	
	private AccountAttribute accAttr;
	
	private LoanPlan loanPlan;
	
	private CcsSettleLoanHst ccsSettleLoanHst;
	
	private List<CcsLoanReg> regList; 
	
	private CcsTxnPost txnPost;
	
	public CcsSettleLoanHst getCcsSettleLoanHst() {
		return ccsSettleLoanHst;
	}

	public void setCcsSettleLoanHst(CcsSettleLoanHst ccsSettleLoanHst) {
		this.ccsSettleLoanHst = ccsSettleLoanHst;
	}

	private LoanFeeDef loanFeeDef;
	
	private CommResp mainResp;
	
	private MsPayfrontError msPayfrontError;
	
	private String decisionCode;

	private MsRequestInfo msRequestInfo;
	
	private MsResponseInfo msResponseInfo;
	
	public CcsLoanReg getOrigLoanReg() {
		return origLoanReg;
	}

	public void setOrigLoanReg(CcsLoanReg origLoanReg) {
		this.origLoanReg = origLoanReg;
	}

	public String getDecisionCode() {
		return decisionCode;
	}

	public void setDecisionCode(String decisionCode) {
		this.decisionCode = decisionCode;
	}

	public void setOrigAuthmemo(CcsAuthmemoO origAuthmemo) {
		this.origAuthmemo = origAuthmemo;
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

	public TxnInfo getTxnInfo() {
		return txnInfo;
	}

	public void setTxnInfo(TxnInfo txnInfo) {
		this.txnInfo = txnInfo;
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

	public ProductCredit getProductCredit() {
		return productCredit;
	}

	public void setProductCredit(ProductCredit productCredit) {
		this.productCredit = productCredit;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public CcsLoan getLoan() {
		return loan;
	}

	public void setLoan(CcsLoan loan) {
		this.loan = loan;
	}

	public CcsLoan getOrigLoan() {
		return origLoan;
	}

	public void setOrigLoan(CcsLoan loan) {
		this.origLoan = loan;
	}
	
	public CcsLoanReg getLoanReg() {
		return loanReg;
	}

	public void setLoanReg(CcsLoanReg loanReg) {
		this.loanReg = loanReg;
	}

	public CcsLoanReg getOirgLoanReg() {
		return origLoanReg;
	}

	public void setOirgLoanReg(CcsLoanReg loanReg) {
		this.origLoanReg = loanReg;
	}

	public CcsAcctO getAccounto() {
		return accounto;
	}

	public void setAccounto(CcsAcctO accounto) {
		this.accounto = accounto;
	}

	public CcsAcct getAccount() {
		return account;
	}

	public void setAccount(CcsAcct account) {
		this.account = account;
	}

	public SunshineRequestInfo getSunshineRequestInfo() {
		return sunshineRequestInfo;
	}

	public void setSunshineRequestInfo(SunshineRequestInfo sunshineRequestInfo) {
		this.sunshineRequestInfo = sunshineRequestInfo;
	}

	public CcsOrder getOrder() {
		return order;
	}

	public void setOrder(CcsOrder order) {
		this.order = order;
	}

	public CcsOrder getOrigOrder() {
		return origOrder;
	}

	public void setOrigOrder(CcsOrder order) {
		this.origOrder = order;
	}
	
	public CcsAuthmemoO getAuthmemo() {
		return authmemo;
	}

	public void setAuthmemo(CcsAuthmemoO authmemo) {
		this.authmemo = authmemo;
	}

	public CcsAuthmemoO getOrigAuthmemo() {
		return origAuthmemo;
	}

	public void setOirgAuthmemo(CcsAuthmemoO authmemo) {
		this.origAuthmemo = authmemo;
	}

	public CcsSettleClaim getClaim() {
		return claim;
	}

	public void setClaim(CcsSettleClaim claim) {
		this.claim = claim;
	}

	public CcsCard getCard() {
		return card;
	}

	public void setCard(CcsCard card) {
		this.card = card;
	}

	public AccountAttribute getAccAttr() {
		return accAttr;
	}

	public void setAccAttr(AccountAttribute accAttr) {
		this.accAttr = accAttr;
	}

	public LoanPlan getLoanPlan() {
		return loanPlan;
	}

	public void setLoanPlan(LoanPlan loanPlan) {
		this.loanPlan = loanPlan;
	}

	public LoanFeeDef getLoanFeeDef() {
		return loanFeeDef;
	}

	public void setLoanFeeDef(LoanFeeDef loanFeeDef) {
		this.loanFeeDef = loanFeeDef;
	}
	public CommResp getMainResp(){
		return mainResp;
	}
	
	public void setMainResp(CommResp resp){
		mainResp = resp;
	}
	
	public MsPayfrontError getMsPayfrontError(){
		return msPayfrontError;
	}
	
	public void setMsPayfrontError(MsPayfrontError msPayfrontError){
		this.msPayfrontError = msPayfrontError;
	}

	public MsRequestInfo getMsRequestInfo() {
		return msRequestInfo;
	}

	public void setMsRequestInfo(MsRequestInfo msRequestInfo) {
		this.msRequestInfo = msRequestInfo;
	}

	public MsResponseInfo getMsResponseInfo() {
		return msResponseInfo;
	}

	public void setMsResponseInfo(MsResponseInfo msResponseInfo) {
		this.msResponseInfo = msResponseInfo;
	}

	public CcsCardO getCardo() {
		return cardo;
	}

	public void setCardo(CcsCardO cardo) {
		this.cardo = cardo;
	}

	public List<CcsLoanReg> getRegList() {
		return regList;
	}

	public void setRegList(List<CcsLoanReg> regList) {
		this.regList = regList;
	}

	public CcsOrderHst getOrigOrderHst() {
		return origOrderHst;
	}

	public void setOrigOrderHst(CcsOrderHst origOrderHst) {
		this.origOrderHst = origOrderHst;
	}

	public CcsTxnPost getTxnPost() {
		return txnPost;
	}

	public void setTxnPost(CcsTxnPost txnPost) {
		this.txnPost = txnPost;
	}

	public CcsLoanRegHst getLoanRegHst() {
		return loanRegHst;
	}

	public void setLoanRegHst(CcsLoanRegHst loanRegHst) {
		this.loanRegHst = loanRegHst;
	}
	
}
