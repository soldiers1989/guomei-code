package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.enums.ManualAuthFlag;
import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.AuthTransTerminal;

public class S13010Unmatch implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 交易卡号
	 */
	public String txn_card_no;
	/**
	 * 受理机构交易编码
	 */
	public Integer acq_ref_no;
	/**
	 * 交易金额
	 */
	public BigDecimal txn_amt;
	/**
	 * 交易币种代码
	 */
	public String txn_curr_cd;
	/**
	 * 授权码
	 */
	public String auth_code;
	/**
	 * 受理机构名称地址
	 */
	public String acq_name_addr;
	/**
	 * 持卡人账户币种金额
	 */
	public BigDecimal chb_txn_amt;
	/**
	 * 交易渠道
	 */
	public InputSource channel;
	
	/**
	 * 终端类型
	 */
	public AuthTransTerminal terminal_type;
	/**
	 * 商户类别代码
	 */
	public String mcc;
	/**
	 * 交易方向
	 */
	public AuthTransDirection  txn_direction;
	/**
	 * 交易状态
	 */
	public AuthTransStatus  txn_status;
	/**
	 * 交易类型
	 */
	public AuthTransType  txn_type;
	/**
	 * log联机时间
	 */
	public Date  log_ol_time;
	/**
	 * log联机业务日期
	 */
	public Date  log_biz_date;
	/**
	 * 交易类型标志
	 */
	public String  mti;

	/**
	 * 人工授权标志
	 */
	public ManualAuthFlag manual_auth_flag;
	/**
	 * 最终行动 
	 */
	public AuthAction final_action;
	/**
	 * 支付密码错误次数 
	 */
	public Integer pay_pwd_err_num;
	/**
	 * b039
	 */
	public String b039;
	
	public String getTxn_card_no() {
		return txn_card_no;
	}


	public void setTxn_card_no(String txn_card_no) {
		this.txn_card_no = txn_card_no;
	}


	public Integer getAcq_ref_no() {
		return acq_ref_no;
	}


	public void setAcq_ref_no(Integer acq_ref_no) {
		this.acq_ref_no = acq_ref_no;
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


	public String getAuth_code() {
		return auth_code;
	}


	public void setAuth_code(String auth_code) {
		this.auth_code = auth_code;
	}


	public String getAcq_name_addr() {
		return acq_name_addr;
	}


	public void setAcq_name_addr(String acq_name_addr) {
		this.acq_name_addr = acq_name_addr;
	}


	public BigDecimal getChb_txn_amt() {
		return chb_txn_amt;
	}


	public void setChb_txn_amt(BigDecimal chb_txn_amt) {
		this.chb_txn_amt = chb_txn_amt;
	}


	public InputSource getChannel() {
		return channel;
	}


	public void setChannel(InputSource channel) {
		this.channel = channel;
	}

	public AuthTransTerminal getTerminal_type() {
		return terminal_type;
	}


	public void setTerminal_type(AuthTransTerminal terminal_type) {
		this.terminal_type = terminal_type;
	}

	public String getMcc() {
		return mcc;
	}


	public void setMcc(String mcc) {
		this.mcc = mcc;
	}


	public AuthTransDirection getTxn_direction() {
		return txn_direction;
	}


	public void setTxn_direction(AuthTransDirection txn_direction) {
		this.txn_direction = txn_direction;
	}


	public AuthTransStatus getTxn_status() {
		return txn_status;
	}


	public void setTxn_status(AuthTransStatus txn_status) {
		this.txn_status = txn_status;
	}


	public AuthTransType getTxn_type() {
		return txn_type;
	}


	public void setTxn_type(AuthTransType txn_type) {
		this.txn_type = txn_type;
	}


	public Date getLog_ol_time() {
		return log_ol_time;
	}


	public void setLog_ol_time(Date log_ol_time) {
		this.log_ol_time = log_ol_time;
	}


	public Date getLog_biz_date() {
		return log_biz_date;
	}


	public void setLog_biz_date(Date log_biz_date) {
		this.log_biz_date = log_biz_date;
	}


	public String getMti() {
		return mti;
	}


	public void setMti(String mti) {
		this.mti = mti;
	}


	public ManualAuthFlag getManual_auth_flag() {
		return manual_auth_flag;
	}


	public void setManual_auth_flag(ManualAuthFlag manual_auth_flag) {
		this.manual_auth_flag = manual_auth_flag;
	}


	public AuthAction getFinal_action() {
		return final_action;
	}


	public void setFinal_action(AuthAction final_action) {
		this.final_action = final_action;
	}


	public Integer getPay_pwd_err_num() {
		return pay_pwd_err_num;
	}


	public void setPay_pwd_err_num(Integer pay_pwd_err_num) {
		this.pay_pwd_err_num = pay_pwd_err_num;
	}


	public String getB039() {
		return b039;
	}


	public void setB039(String b039) {
		this.b039 = b039;
	}


	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
