package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 查询/设定绑定借记卡号
* @author fanghj
 * @time 2014-3-26 上午11:18:48
 */
public class S20030Resp implements Serializable {

	private static final long serialVersionUID = 5568498570342300542L;

	/**
     * 贷款卡号
     */
    public String card_no;

    /**
     * 借记卡银行名称
     */
    public String dd_bank_name;

    /**
     * 借记卡开户行号
     */
    public String dd_bank_branch;

    /**
     * 借记账号
     */
    public String dd_bank_acct_no;

    /**
     * 借记卡账户姓名
     */
    public String dd_bank_acct_name;

    
	public String getCard_no() {
		return card_no;
	}


	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}


	public String getDd_bank_name() {
		return dd_bank_name;
	}


	public void setDd_bank_name(String dd_bank_name) {
		this.dd_bank_name = dd_bank_name;
	}


	public String getDd_bank_branch() {
		return dd_bank_branch;
	}


	public void setDd_bank_branch(String dd_bank_branch) {
		this.dd_bank_branch = dd_bank_branch;
	}


	public String getDd_bank_acct_no() {
		return dd_bank_acct_no;
	}


	public void setDd_bank_acct_no(String dd_bank_acct_no) {
		this.dd_bank_acct_no = dd_bank_acct_no;
	}


	public String getDd_bank_acct_name() {
		return dd_bank_acct_name;
	}


	public void setDd_bank_acct_name(String dd_bank_acct_name) {
		this.dd_bank_acct_name = dd_bank_acct_name;
	}


	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}

}

