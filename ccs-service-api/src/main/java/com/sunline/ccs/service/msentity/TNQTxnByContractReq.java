package com.sunline.ccs.service.msentity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * @author wanghl
 *
 */
public class TNQTxnByContractReq  extends MsRequestInfo  {
	private static final long serialVersionUID = -5795690158090428131L;
	
	/**
	 *  合同号	VARCHAR2(32)	Y
	 */
	@Check(lengths=32, notEmpty=true)
	@JsonProperty(value="CONTR_NBR")
	public String contraNbr;
	/**
	 * 起始日期	VARCHAR2(8)	N
	 */
	@Check(lengths=8, fixed=true, notEmpty=false)
	@JsonProperty(value="START_TIME")
	public Date startTime;
	/**
	 * 截止日期	VARCHAR2(8)	N
	 */
	@Check(lengths=8, fixed=true, notEmpty=false)
	@JsonProperty(value="END_TIME")
	public Date endTime;
	/**
	 * 页大小	VARCHAR2(10)	Y
	 */
	@Check(lengths=10, fixed=false, isNumber=true, notEmpty=true)
	@JsonProperty(value="PAGE_SIZE")
	public Integer pageSize;
	/**
	 * 显示页数	VARCHAR2(10)	Y
	 */
	@Check(lengths=10, isNumber=true, notEmpty=true)
	@JsonProperty(value="PAGE_POSITION")
	public Integer pagePosition;

	public String getContraNbr() {
		return contraNbr;
	}

	public void setContraNbr(String contraNbr) {
		this.contraNbr = contraNbr;
	}
	
	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Integer getPagePosition() {
		return pagePosition;
	}

	public void setPagePosition(Integer pagePosition) {
		this.pagePosition = pagePosition;
	}

	
	
}
