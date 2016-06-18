package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ccs.param.def.enums.TxnType;

public class S17011TxnPoint implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7892279940163497544L;

	/**
     * 币种
     */
    public String curr_cd ;

    /**
     * 交易日期
     */
    public Date txn_date ;

    /**
     * 交易时间
     */
    public Date txn_time ;

    /**
     * 交易码
     */
    public String txn_code ;

    /**
     * 交易金额
     */
    public BigDecimal txn_amt ;

    /**
     * 交易币种代码	
     */
    public String txn_curr_cd ;

    /**
     * 交易参考号
     */
    public String ref_nbr ;

    /**
     * 账单交易描述
     */
    public String txn_short_desc ;

	/**
     * 积分数值
     */
    public BigDecimal point ;

    /**
     * 受卡方标识码
     */
    public String acq_acceptor_id ;

    /**
     * 受理机构名称地址
     */
    public String acq_name_addr ;
    
    /**
     * 
     * 交易类型
     */
    public TxnType sett_txn_type;
    
	public String getCurr_cd() {
		return curr_cd;
	}

	public void setCurr_cd(String curr_cd) {
		this.curr_cd = curr_cd;
	}

	public Date getTxn_date() {
		return txn_date;
	}

	public void setTxn_date(Date txn_date) {
		this.txn_date = txn_date;
	}

	public Date getTxn_time() {
		return txn_time;
	}

	public void setTxn_time(Date txn_time) {
		this.txn_time = txn_time;
	}

	public BigDecimal getTxn_amt() {
		return txn_amt;
	}

	public void setTxn_amt(BigDecimal txn_amt) {
		this.txn_amt = txn_amt;
	}

	public String getTxn_curr_cd() {
		return txn_curr_cd;
	}

	public void setTxn_curr_cd(String txn_curr_cd) {
		this.txn_curr_cd = txn_curr_cd;
	}

	public String getRef_nbr() {
		return ref_nbr;
	}

	public void setRef_nbr(String ref_nbr) {
		this.ref_nbr = ref_nbr;
	}

	public String getTxn_short_desc() {
		return txn_short_desc;
	}

	public void setTxn_short_desc(String txn_short_desc) {
		this.txn_short_desc = txn_short_desc;
	}

	public BigDecimal getPoint() {
		return point;
	}

	public void setPoint(BigDecimal point) {
		this.point = point;
	}

	public String getAcq_acceptor_id() {
		return acq_acceptor_id;
	}

	public void setAcq_acceptor_id(String acq_acceptor_id) {
		this.acq_acceptor_id = acq_acceptor_id;
	}

	public String getAcq_name_addr() {
		return acq_name_addr;
	}

	public void setAcq_name_addr(String acq_name_addr) {
		this.acq_name_addr = acq_name_addr;
	}
	
	public String getTxn_code() {
		return txn_code;
	}

	public void setTxn_code(String txn_code) {
		this.txn_code = txn_code;
	}

	public TxnType getSett_txn_type() {
		return sett_txn_type;
	}

	public void setSett_txn_type(TxnType sett_txn_type) {
		this.sett_txn_type = sett_txn_type;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
