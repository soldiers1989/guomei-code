/**
 * 
 */
package com.sunline.ccs.batch.rpt.cca230;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;


/**  
 * @描述		:
 *  
 * @作者		: JiaoJian 
 * @创建时间	: 2015年11月26日  下午8:25:57   
 */
public class ContractCompareCcsLoanKey  implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	private long loanId;
	
	private Date createTime;
	
	private Date activeDate;
	
	private Date paidOutDate;
	
	private String contrNbr;
	
	private BigDecimal loanInitPrin;
	
	private long acctNbr;
	
	
	public Date getPaidOutDate() {
		return paidOutDate;
	}
	public void setPaidOutDate(Date paidOutDate) {
		this.paidOutDate = paidOutDate;
	}
	public Date getActiveDate() {
		return activeDate;
	}
	public void setActiveDate(Date activeDate) {
		this.activeDate = activeDate;
	}
	public long getLoanId() {
		return loanId;
	}
	public void setLoanId(long loanId) {
		this.loanId = loanId;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public String getContrNbr() {
		return contrNbr;
	}
	public void setContrNbr(String contrNbr) {
		this.contrNbr = contrNbr;
	}
	public BigDecimal getLoanInitPrin() {
		return loanInitPrin;
	}
	public void setLoanInitPrin(BigDecimal loanInitPrin) {
		this.loanInitPrin = loanInitPrin;
	}
	public long getAcctNbr() {
		return acctNbr;
	}
	public void setAcctNbr(long acctNbr) {
		this.acctNbr = acctNbr;
	}

}
