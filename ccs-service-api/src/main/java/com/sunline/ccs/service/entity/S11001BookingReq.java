package com.sunline.ccs.service.entity;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 预约提前还款接口报文
 * @author jjb
 *
 */
public class S11001BookingReq extends SunshineRequestInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 保单号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="GUARANTYID")
	private String guarantyid;
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
	private Date caldate;
	
	public String getGuarantyid() {
		return guarantyid;
	}
	public void setGuarantyid(String guarantyid) {
		this.guarantyid = guarantyid;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Date getCaldate() {
		return caldate;
	}
	public void setCaldate(Date caldate) {
		this.caldate = caldate;
	}
	
}
