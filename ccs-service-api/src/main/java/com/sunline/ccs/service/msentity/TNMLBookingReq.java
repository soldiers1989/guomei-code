package com.sunline.ccs.service.msentity;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 预约提前还款接口报文
 * @author jjb
 *
 */
public class TNMLBookingReq extends MsRequestInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 借据号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="DUE_BILL_NO")
	public String dueBillNo;
	
	/**
	 * 合同号
	 */
	@Check(lengths=18)
	@JsonProperty(value="CONTRNBR")
	public String contrNbr;
	
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
	@Check(lengths=8,notEmpty=true,isNumber=true,fixed=true)
	@JsonProperty(value="CALDATE")
	private Date caldate;
	
	public String getContrNbr() {
		return contrNbr;
	}
	public void setContrNbr(String contrNbr) {
		this.contrNbr = contrNbr;
	}
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
	public Date getCaldate() {
		return caldate;
	}
	public void setCaldate(Date caldate) {
		this.caldate = caldate;
	}
	
}
