package com.sunline.ccs.service.msdentity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ppy.dictionary.entity.Check;
import com.sunline.ppy.dictionary.enums.OrderStatus;
/**
 * 交易结果查询
 * @author zhengjf
 */
public class STFNTxnOrderInqResp extends MsResponseInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 合同号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="CONTR_NBR")
	public String  contrNbr;
	/**
	 * 借据号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="DUE_BILL_NO")
	public String  bueBillNo;
	/**
	 * 交易状态
	 */
	@Check(lengths=1,notEmpty=true)
	@JsonProperty(value="TXN_STATUS")
	public OrderStatus  txnStatus;
	/**
	 * 原交易返回码
	 */
	@JsonProperty(value="TXN_CODE")
	public String  txnCode;
	/**
	 * 失败原因
	 */
	@JsonProperty(value="FAILURE_MESSAGE")
	public String  failureMassage;
	
	public String getContrNbr() {
		return contrNbr;
	}
	public void setContrNbr(String contrNbr) {
		this.contrNbr = contrNbr;
	}
	public String getBueBillNo() {
		return bueBillNo;
	}
	public void setBueBillNo(String bueBillNo) {
		this.bueBillNo = bueBillNo;
	}
	public OrderStatus getTxnStatus() {
		return txnStatus;
	}
	public void setTxnStatus(OrderStatus txnStatus) {
		this.txnStatus = txnStatus;
	}
	public String getTxnCode() {
		return txnCode;
	}
	public void setTxnCode(String txnCode) {
		this.txnCode = txnCode;
	}
	public String getFailureMassage() {
		return failureMassage;
	}
	public void setFailureMassage(String failureMassage) {
		this.failureMassage = failureMassage;
	}
	
	
}
