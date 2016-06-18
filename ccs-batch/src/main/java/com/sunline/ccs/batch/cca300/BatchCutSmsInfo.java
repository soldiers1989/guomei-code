package com.sunline.ccs.batch.cca300;

import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsOrderHst;

/**
 * 批量扣款成功短信-数据收集
 * @author lizz
 *
 */
public class BatchCutSmsInfo {
	
	/**
	 * 账户
	 */
	private CcsAcct acct;
	/**
	 * 贷款
	 */
	private CcsLoan loan;
	/**
	 * 批量代扣订单
	 */
	private CcsOrder order;
	/**
	 * 批量代扣订单
	 */
	private CcsOrderHst orderHst;
	
	public CcsAcct getAcct() {
		return acct;
	}
	public void setAcct(CcsAcct acct) {
		this.acct = acct;
	}
	public CcsLoan getLoan() {
		return loan;
	}
	public void setLoan(CcsLoan loan) {
		this.loan = loan;
	}
	public CcsOrderHst getOrderHst() {
		return orderHst;
	}
	public void setOrderHst(CcsOrderHst orderHst) {
		this.orderHst = orderHst;
	}
	public CcsOrder getOrder() {
		return order;
	}
	public void setOrder(CcsOrder order) {
		this.order = order;
	}
	
}
