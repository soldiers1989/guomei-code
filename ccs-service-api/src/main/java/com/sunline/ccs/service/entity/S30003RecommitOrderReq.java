package com.sunline.ccs.service.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;

/**
 * 放款重提接口接受报文
 * @author jjb
 *
 */
public class S30003RecommitOrderReq extends SunshineRequestInfo implements Serializable{
	private static final long serialVersionUID = 1L;
	
	/**
	 * 订单号
	 */
	@JsonProperty(value="ORDERID")
	private Long orderId;

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	/**
	 * 保单号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="GUARANTYID")
	private String guarantyid;

	public String getGuarantyid() {
		return guarantyid;
	}

	public void setGuarantyid(String guarantyid) {
		this.guarantyid = guarantyid;
	}

	/**
	 * 借据号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="DUEBILLNO")
	private String duebillno;

	public String getDuebillno() {
		return duebillno;
	}

	public void setDuebillno(String duebillno) {
		this.duebillno = duebillno;
	}


}
