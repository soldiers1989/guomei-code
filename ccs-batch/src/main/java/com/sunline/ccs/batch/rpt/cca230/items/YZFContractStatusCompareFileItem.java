/**
 * 
 */
package com.sunline.ccs.batch.rpt.cca230.items;

import java.math.BigDecimal;
import java.util.Date;

import com.sunline.ark.support.cstruct.CChar;

/**  
 * @描述		: 合同状态对账文件详情
 *  
 * @作者		: JiaoJian 
 * @创建时间	: 2015年11月26日  下午3:57:02   
 */
public class YZFContractStatusCompareFileItem {
	
	/**
	 * 合作方编码
	 */
	@CChar (value = 8, order = 1)
	private String acqId;
	
	/**
	 * 马上客户编码
	 */
	@CChar (value = 32, order = 2)
	private String custUniqueId;
	
	/**
	 * 合同生效日期
	 */
	@CChar (value = 10, order = 3, datePattern="yyyy-MM-dd")
	private Date activeDate;
	
	/**
	 * 合同状态
	 */
	@CChar (value = 30, order = 4)
	private String contractStatus;
	
	/**
	 * 合同号
	 */
	@CChar (value = 30, order = 5)
	private String contractNo;
	
	/**
	 * 合同额度
	 */
	@CChar (value = 10, order = 6, precision=2, pointSupported=true)
	private BigDecimal contractLimit;
	
	/**
	 * 失败原因
	 */
	@CChar (value = 20, order = 7)
	private String failReason = "";
	
	
	
	

	public String getAcqId() {
		return acqId;
	}

	public void setAcqId(String acqId) {
		this.acqId = acqId;
	}

	public String getCustUniqueId() {
		return custUniqueId;
	}

	public void setCustUniqueId(String custUniqueId) {
		this.custUniqueId = custUniqueId;
	}

	public Date getActiveDate() {
		return activeDate;
	}

	public void setActiveDate(Date activeDate) {
		this.activeDate = activeDate;
	}

	public String getContractStatus() {
		return contractStatus;
	}

	public void setContractStatus(String contractStatus) {
		this.contractStatus = contractStatus;
	}

	public String getContractNo() {
		return contractNo;
	}

	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}

	public BigDecimal getContractLimit() {
		return contractLimit;
	}

	public void setContractLimit(BigDecimal contractLimit) {
		this.contractLimit = contractLimit;
	}

	public String getFailReason() {
		return failReason;
	}

	public void setFailReason(String failReason) {
		this.failReason = failReason;
	}
	
}
