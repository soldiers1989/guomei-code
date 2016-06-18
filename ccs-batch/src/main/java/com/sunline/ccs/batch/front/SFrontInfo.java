package com.sunline.ccs.batch.front;

import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;

/**
 * 小批量数据收集
 * @author zhangqiang
 *
 */
public class SFrontInfo {
	
	/**
	 * 账户
	 */
	private CcsAcct acct;
	/**
	 * 客户
	 */
	private CcsCustomer cust;
	/**
	 * 贷款
	 */
	private CcsLoan loan;
	/**
	 * 期供(正常还款用)
	 */
	private CcsRepaySchedule schedule;
	/**
	 * 贷款注册信息(提前还款/理赔用)
	 */
	private CcsLoanReg loanReg;
	/**
	 * 联机追偿订单(追偿扣款用)
	 */
	private CcsOrder rOrder;
	
	public CcsAcct getAcct() {
		return acct;
	}
	public void setAcct(CcsAcct acct) {
		this.acct = acct;
	}
	public CcsCustomer getCust() {
		return cust;
	}
	public void setCust(CcsCustomer cust) {
		this.cust = cust;
	}
	public CcsLoan getLoan() {
		return loan;
	}
	public void setLoan(CcsLoan loan) {
		this.loan = loan;
	}
	public CcsRepaySchedule getSchedule() {
		return schedule;
	}
	public void setSchedule(CcsRepaySchedule schedule) {
		this.schedule = schedule;
	}
	public CcsLoanReg getLoanReg() {
		return loanReg;
	}
	public void setLoanReg(CcsLoanReg loanReg) {
		this.loanReg = loanReg;
	}
	public CcsOrder getrOrder() {
		return rOrder;
	}
	public void setrOrder(CcsOrder rOrder) {
		this.rOrder = rOrder;
	}
	
}
