package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.SmsInd;

/**
 * 发送短信设置响应接口
 * 
* @author fanghj
 * @date 2013-6-5  上午10:26:07
 * @version 1.0
 */
public class S12101Resp implements Serializable {

	private static final long serialVersionUID = -4702924990782782515L;

	/**
     * 卡号
     */
    public String card_no ;

    /**
     * 币种
     */
    public String curr_cd ;

    /**
     *短信发送标志
     */
    public SmsInd smsind ;

    /**
     * 个性化短信设置阀值
     */
    public BigDecimal user_sms_amt ;

    /**
     * 是否存在外币标志
     */
    public Indicator dual_curr_ind ;

    /**
     *外币币种
     */
    public String dual_curr_cd ;
    
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

	public SmsInd getSmsind() {
		return smsind;
	}

	public void setSmsind(SmsInd smsind) {
		this.smsind = smsind;
	}

	public BigDecimal getUser_sms_amt() {
		return user_sms_amt;
	}

	public void setUser_sms_amt(BigDecimal user_sms_amt) {
		this.user_sms_amt = user_sms_amt;
	}

	public Indicator getDual_curr_ind() {
		return dual_curr_ind;
	}

	public void setDual_curr_ind(Indicator dual_curr_ind) {
		this.dual_curr_ind = dual_curr_ind;
	}

	public String getDual_curr_cd() {
		return dual_curr_cd;
	}

	public void setDual_curr_cd(String dual_curr_cd) {
		this.dual_curr_cd = dual_curr_cd;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

