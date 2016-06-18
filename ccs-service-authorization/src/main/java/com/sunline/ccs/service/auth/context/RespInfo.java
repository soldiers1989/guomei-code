package com.sunline.ccs.service.auth.context;

import java.math.BigDecimal;
import java.util.Date;

import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.Indicator;

/**
 * 
 * @see 类名：RespInfo
 * @see 描述：响应信息
 *
 * @see 创建日期：   2015年6月24日下午3:17:57
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class RespInfo {

	private String origMti;
	// 原始受理机构号
	private String origAcqInstId;
	// 原始授权金额
	private BigDecimal origChbTxnAmt;
	// 原始转发机构号
	private String origFwdInstId;
	// 原交易LOG键值
	private Long origLogKv;
	// 原始系统跟踪号
	private Integer origTraceNo;
	// 原始交易日期
	private Date origTransDate;
	// 原始交易金额
	private BigDecimal origTxnAmt;
	// 原交易处理码
	private String origTxnProc;
	// 原交易类型
	private AuthTransType origTxnType;
	// 原交易键值1
	private String origTxnVal1;
	// 原交易键值2
	private String origTxnVal2;
	// 原交易键值3
	private String origTxnVal3;
	// 原贷记卡系统的处理日期
	private Date origBizDate;

	// 以下属性是交易业务处理产生的中间数据
	// 原交易授权码
	private String origAuthCode;
	// 授权码
	private String b038_AuthCode;
	// 实际金额-Additional Amounts
	private String b054Resp_AddAmt;
	// 持卡人信息
	private String b061CustInfo_Cardholer;
	// 最后金额
	private BigDecimal finalUpdAmt;
	// 借贷记标志
	private String finalUpdDirection;

	// 附加响应数据-Additional Response Data(二期)
	private String b044_AddRespData;
	// 附加交易信息(二期)
	private String b057_AddPriv;
	// 发卡方保留-Issuer Institution Reserved(二期)
	private String b123_IssInsReserved;
	// 孤立确认标识
	private boolean isolatedConfirm = false;
	// 无卡自助开通标识
	private Indicator openNoCardSelf = Indicator.N; 

	public String getOrigAuthCode() {
		return origAuthCode;
	}

	public void setOrigAuthCode(String origAuthCode) {
		this.origAuthCode = origAuthCode;
	}

	public String getOrigMti() {
		return origMti;
	}

	public void setOrigMti(String origMti) {
		this.origMti = origMti;
	}

	public boolean isIsolatedConfirm() {
		return isolatedConfirm;
	}

	public void setIsolatedConfirm(boolean isolatedConfirm) {
		this.isolatedConfirm = isolatedConfirm;
	}

	 

	public Date getOrigBizDate() {
		return origBizDate;
	}

	public void setOrigBizDate(Date origBizDate) {
		this.origBizDate = origBizDate;
	}

	public BigDecimal getFinalUpdAmt() {
		return finalUpdAmt;
	}

	public void setFinalUpdAmt(BigDecimal finalUpdAmt) {
		this.finalUpdAmt = finalUpdAmt;
	}

	public String getFinalUpdDirection() {
		return finalUpdDirection;
	}

	public void setFinalUpdDirection(String finalUpdDirection) {
		this.finalUpdDirection = finalUpdDirection;
	}

	public String getB061CustInfo_Cardholer() {
		return b061CustInfo_Cardholer;
	}

	public void setB061CustInfo_Cardholer(String b061CustInfo_Cardholer) {
		this.b061CustInfo_Cardholer = b061CustInfo_Cardholer;
	}

	public String getOrigAcqInstId() {
		return origAcqInstId;
	}

	public void setOrigAcqInstId(String origAcqInstId) {
		this.origAcqInstId = origAcqInstId;
	}

	public BigDecimal getOrigChbTxnAmt() {
		return origChbTxnAmt;
	}

	public void setOrigChbTxnAmt(BigDecimal origChbTxnAmt) {
		this.origChbTxnAmt = origChbTxnAmt;
	}

	public String getOrigFwdInstId() {
		return origFwdInstId;
	}

	public void setOrigFwdInstId(String origFwdInstId) {
		this.origFwdInstId = origFwdInstId;
	}

	public Long getOrigLogKv() {
		return origLogKv;
	}

	public void setOrigLogKv(Long origLogKv) {
		this.origLogKv = origLogKv;
	}

	public Integer getOrigTraceNo() {
		return origTraceNo;
	}

	public void setOrigTraceNo(Integer origTraceNo) {
		this.origTraceNo = origTraceNo;
	}

	public Date getOrigTransDate() {
		return origTransDate;
	}

	public void setOrigTransDate(Date origTransDate) {
		this.origTransDate = origTransDate;
	}

	public BigDecimal getOrigTxnAmt() {
		return origTxnAmt;
	}

	public void setOrigTxnAmt(BigDecimal origTxnAmt) {
		this.origTxnAmt = origTxnAmt;
	}

	public String getOrigTxnProc() {
		return origTxnProc;
	}

	public void setOrigTxnProc(String origTxnProc) {
		this.origTxnProc = origTxnProc;
	}

	public AuthTransType getOrigTxnType() {
		return origTxnType;
	}

	public void setOrigTxnType(AuthTransType origTxnType) {
		this.origTxnType = origTxnType;
	}

	public String getOrigTxnVal1() {
		return origTxnVal1;
	}

	public void setOrigTxnVal1(String origTxnVal1) {
		this.origTxnVal1 = origTxnVal1;
	}

	public String getOrigTxnVal2() {
		return origTxnVal2;
	}

	public void setOrigTxnVal2(String origTxnVal2) {
		this.origTxnVal2 = origTxnVal2;
	}

	public String getOrigTxnVal3() {
		return origTxnVal3;
	}

	public void setOrigTxnVal3(String origTxnVal3) {
		this.origTxnVal3 = origTxnVal3;
	}

	public String getB038_AuthCode() {
		return b038_AuthCode;
	}

	public void setB038_AuthCode(String b038_AuthCode) {
		this.b038_AuthCode = b038_AuthCode;
	}

	public String getB044_AddRespData() {
		return b044_AddRespData;
	}

	public void setB044_AddRespData(String b044_AddRespData) {
		this.b044_AddRespData = b044_AddRespData;
	}

	public String getB054Resp_AddAmt() {
		return b054Resp_AddAmt;
	}

	public void setB054Resp_AddAmt(String b054Resp_AddAmt) {
		this.b054Resp_AddAmt = b054Resp_AddAmt;
	}

	public String getB057_AddPriv() {
		return b057_AddPriv;
	}

	public void setB057_AddPriv(String b057_AddPriv) {
		this.b057_AddPriv = b057_AddPriv;
	}

	public String getB123_IssInsReserved() {
		return b123_IssInsReserved;
	}

	public void setB123_IssInsReserved(String b123_IssInsReserved) {
		this.b123_IssInsReserved = b123_IssInsReserved;
	}

	public Indicator getOpenNoCardSelf() {
		return openNoCardSelf;
	}

	public void setOpenNoCardSelf(Indicator openNoCardSelf) {
		this.openNoCardSelf = openNoCardSelf;
	}
	
}
