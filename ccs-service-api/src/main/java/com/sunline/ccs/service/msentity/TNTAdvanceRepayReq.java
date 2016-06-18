package com.sunline.ccs.service.msentity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;
/**
 * 
 * 兜底 提前还款试算/申请
 * @author zhengjf
 *
 */
public class TNTAdvanceRepayReq extends MsRequestInfo{

private static final long serialVersionUID = 1L;
	
	/**
	 * 借据号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="DUE_BILL_NO")
	private String dueBillNo;
	/**
	 * 请求类型
	 */
	@Check(lengths=1,notEmpty=true)
	@JsonProperty(value="TYPE")
	private String type;
	/**
	 * 还款日期
	 * YYYYMMDD
	 */
	@Check(lengths=8,notEmpty=true)
	@JsonProperty(value="CALDATE")
	private Date calDate;
	
	public String getDueBillNo() {
		return dueBillNo;
	}
	public void setDueBillNo(String dueBillNo) {
		this.dueBillNo = dueBillNo;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Date getCalDate() {
		return calDate;
	}
	public void setCalDate(Date calDate) {
		this.calDate = calDate;
	}
	
}
