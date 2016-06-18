package com.sunline.ccs.service.msdentity;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.service.msentity.MsRequestInfo;
import com.sunline.ppy.dictionary.entity.Check;

@SuppressWarnings("serial")
public class STNQOrderInfoReq extends MsRequestInfo implements Serializable{
	/**
	 * 合同号
	 */
	@Check(lengths=22,notEmpty=true)
	@JsonProperty(value="CONTR_NBR")
	private String contractNo;
	/**
	 * 起始时间
	 */
	@Check(lengths=8)
	@JsonProperty(value="START_TIME")
	private Date starttime;
	/**
	 * 截止日期
	 */
	@Check(lengths=8)
	@JsonProperty(value="END_TIME")
	private Date endtime;
	/**
	 * 页大小
	 */
	@Check(lengths=10,notEmpty=true,isNumber=true)
	@JsonProperty(value="PAGE_SIZE")
	private int pagesize;
	
	/**
	 * 显示页数
	 */
	@Check(lengths=10,notEmpty=true,isNumber=true)
	@JsonProperty(value="PAGE_POSITION")
	private int pageposition;

	public void setPageposition(int pageposition) {
		this.pageposition = pageposition;
	}

	
	public String getContractNo() {
		return contractNo;
	}

	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}

	public Date getStarttime() {
		return starttime;
	}

	public void setStarttime(Date starttime) {
		this.starttime = starttime;
	}

	public Date getEndtime() {
		return endtime;
	}

	public void setEndtime(Date endtime) {
		this.endtime = endtime;
	}

	public int getPagesize() {
		return pagesize;
	}

	public void setPagesize(int pagesize) {
		this.pagesize = pagesize;
	}

	public int getPageposition() {
		return pageposition;
	}
}
