package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ccs.param.def.enums.TxnType;

/**
 *
* @author fanghj
 *@time 2014-3-26 下午2:16:34
 */
public class S20050Txn implements Serializable{

	private static final long serialVersionUID = -5708891919922930192L;
	
	/**
     * 交易卡号
     */
    public String txn_card_no;

    /**
     * 交易日期
     */
    public Date txn_date;

    /**
     * 交易时间
     */
    public Date txn_time;

    /**
     * 交易码
     */
    public String txn_code;

    /**
     * 交易类型
     */
    public TxnType sett_txn_type;

    /**
     * 交易金额
     */
    public BigDecimal txn_amt;

    /**
     * 入账币种金额
     */
    public BigDecimal post_amt;

    /**
     * 入账币种
     */
    public String post_curr_cd;

    /**
     * 入账日期
     */
    public Date post_date;

    /**
     * 授权码
     */
    public String auth_code;

    /**
     * 交易币种代码
     */
    public String txn_curr_cd;

    /**
     * 交易参考号
     */
    public String ref_nbr;

    /**
     * 账单交易描述
     */
    public String txn_short_desc;

    /**
     * 受卡方标识码
     */
    public String acq_acceptor_id;

    /**
     * 受理机构名称地址
     */
    public String acq_name_addr;
    
	public String getTxn_card_no() {
		return txn_card_no;
	}

	public void setTxn_card_no(String txn_card_no) {
		this.txn_card_no = txn_card_no;
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

	public BigDecimal getTxn_amt() {
		return txn_amt;
	}

	public void setTxn_amt(BigDecimal txn_amt) {
		this.txn_amt = txn_amt;
	}

	public BigDecimal getPost_amt() {
		return post_amt;
	}

	public void setPost_amt(BigDecimal post_amt) {
		this.post_amt = post_amt;
	}

	public String getPost_curr_cd() {
		return post_curr_cd;
	}

	public void setPost_curr_cd(String post_curr_cd) {
		this.post_curr_cd = post_curr_cd;
	}

	public Date getPost_date() {
		return post_date;
	}

	public void setPost_date(Date post_date) {
		this.post_date = post_date;
	}

	public String getAuth_code() {
		return auth_code;
	}

	public void setAuth_code(String auth_code) {
		this.auth_code = auth_code;
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

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
