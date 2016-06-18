package com.sunline.ccs.batch.cc6100;

import java.util.ArrayList;
import java.util.List;

import com.sunline.ccs.infrastructure.shared.model.CcsStatement;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnUnstatement;

/**
 * @see 类名：U6101Statement
 * @see 描述：账单信息,包括账单汇总信息，和未出账单交易信息
 *
 * @see 创建日期：   2015-6-24下午2:05:21
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class U6101Statement {
	private CcsStatement stmtHst;
	
	private List<CcsTxnUnstatement> txnUnstmts = new ArrayList<CcsTxnUnstatement>();

	public CcsStatement getStmtHst() {
		return stmtHst;
	}

	public void setStmtHst(CcsStatement stmtHst) {
		this.stmtHst = stmtHst;
	}

	public List<CcsTxnUnstatement> getTxnUnstmts() {
		return txnUnstmts;
	}

	public void setTxnUnstmts(List<CcsTxnUnstatement> txnUnstmts) {
		this.txnUnstmts = txnUnstmts;
	}
	
}
