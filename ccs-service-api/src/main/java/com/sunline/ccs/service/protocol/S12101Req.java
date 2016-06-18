package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.SmsInd;

/**
 * 发送短信设置请求接口
 * 
* @author fanghj
 * @date 2013-6-5  上午10:21:43
 * @version 1.0
 */
public class S12101Req implements Serializable {

	private static final long serialVersionUID = 8715371086069147881L;

	/**
     *卡号
     */
    public String card_no ;

    /**
     * 币种
     */
    public String curr_cd ;

    /**
     * 操作类型
     */
    public String opt ;

    /**
     * 短信发送标志
     */
    public SmsInd sms_ind ;

    /**
     * 个性化短信发送阀值
     */
    public BigDecimal user_sms_amt ;

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	public String getCurr_cd() {
		return curr_cd;
	}

	public void setCurr_cd(String curr_cd) {
		this.curr_cd = curr_cd;
	}

	public String getOpt() {
		return opt;
	}

	public void setOpt(String opt) {
		this.opt = opt;
	}

	public SmsInd getSms_ind() {
		return sms_ind;
	}

	public void setSms_ind(SmsInd sms_ind) {
		this.sms_ind = sms_ind;
	}

	public BigDecimal getUser_sms_amt() {
		return user_sms_amt;
	}

	public void setUser_sms_amt(BigDecimal user_sms_amt) {
		this.user_sms_amt = user_sms_amt;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

