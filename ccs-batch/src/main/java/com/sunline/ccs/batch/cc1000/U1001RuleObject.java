package com.sunline.ccs.batch.cc1000;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunline.ppy.dictionary.enums.AcctTypeGroup;
import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.exchange.TpsTranFlow;


/**
 * @see 类名：U1001RuleObject
 * @see 描述：规则结构体
 *
 * @see 创建日期：   2015-6-23下午7:29:55
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class U1001RuleObject {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private DbCrInd dcFlag;
	private String txnCode;
	private TpsTranFlow tpsTranFlow;
	private boolean isLoanReturn;
	private AcctTypeGroup acctTypeGroup;
	private LoanType loanType;
	

	public boolean isLoanReturn() {
		return isLoanReturn;
	}

	public void setLoanReturn(boolean isLoanReturn) {
		this.isLoanReturn = isLoanReturn;
	}

	public DbCrInd getDcFlag() {
		return dcFlag;
	}

	public void setDcFlag(DbCrInd dcFlag) {
		this.dcFlag = dcFlag;
	}

	public String getTxnCode() {
		return txnCode;
	}

	public void setTxnCode(String txnCode) {
		this.txnCode = txnCode;
	}

	public TpsTranFlow getTpsTranFlow() {
		return tpsTranFlow;
	}

	public void setTpsTranFlow(TpsTranFlow tpsTranFlow) {
		this.tpsTranFlow = tpsTranFlow;
	}
	
	public AcctTypeGroup getAcctTypeGroup() {
		return acctTypeGroup;
	}

	public void setAcctTypeGroup(AcctTypeGroup acctTypeGroup) {
		this.acctTypeGroup = acctTypeGroup;
	}
	
	//33域判断是否银联境外
	//与联机规则相同
	public boolean isXborder() {
		logger.debug("b033=[{}]",tpsTranFlow.b033);
		//判断是否是固定值00010344
		if ("00010344".equals(tpsTranFlow.b033)) 
		{
			logger.debug("isXborder()=true");
			return true;
		}else{
			logger.debug("isXborder()=false");
			return false; 
		}
	}

	public LoanType getLoanType() {
		return loanType;
	}

	public void setLoanType(LoanType loanType) {
		this.loanType = loanType;
	}
	
}
