package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 永久额度调整（带复核）请求接口
 * 
* @author fanghj
 * @date 2013-6-5  上午11:10:23
 * @version 1.0
 */
public class S15012Req implements Serializable {

	private static final long serialVersionUID = -8567730135720513031L;

	/**
     * 卡号
     */
    public String card_no ;

    /**
     * 币种
     */
    public String curr_cd ;

    /**
     * 信用额度
     */
    public BigDecimal credit_limit ;
    
    /**
     * 操作员id
     */
    public String operaId;

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

	public BigDecimal getCredit_limit() {
		return credit_limit;
	}

	public void setCredit_limit(BigDecimal credit_limit) {
		this.credit_limit = credit_limit;
	}

	public String getOperaId() {
		return operaId;
	}

	public void setOperaId(String operaId) {
		this.operaId = operaId;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

