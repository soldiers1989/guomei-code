package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.Brand;
import com.sunline.ppy.dictionary.enums.CardClass;
import com.sunline.ccs.param.def.enums.FirstCardFeeInd;

public class S14021Resp implements Serializable {

	private static final long serialVersionUID = 4909599475729647164L;

	/**
     * 产品名称
     */
    public String product_name ;

    /**
     * 卡等级
     */
    public CardClass cardclass ;

    /**
     * 卡品牌
     */
    public Brand brand ;

    /**
     * 密码重置费
     */
    public BigDecimal passwd_reset_fee ;

    /**
     * 卡片挂失费
     */
    public BigDecimal card_loss_fee ;

    /**
     * 补打账单费
     */
    public BigDecimal repint_stmt_fee ;

    /**
     * 首次年费收取方式
     */
    public FirstCardFeeInd first_card_fee_ind ;

    /**
     * 主卡年费
     */
    public BigDecimal b_card_fee ;

    /**
     * 附卡年费
     */
    public BigDecimal s_card_fee ;

    /**
     * 加急费
     */
    public BigDecimal urgent_fee ;

    /**
     * 短信费
     */
    public BigDecimal sms_fee ;

    /**
     * 卡片工本费
     */
    public BigDecimal card_issue_fee ;

	public String getProduct_name() {
		return product_name;
	}

	public void setProduct_name(String product_name) {
		this.product_name = product_name;
	}

	public CardClass getCardclass() {
		return cardclass;
	}

	public void setCardclass(CardClass cardclass) {
		this.cardclass = cardclass;
	}

	public Brand getBrand() {
		return brand;
	}

	public void setBrand(Brand brand) {
		this.brand = brand;
	}

	public BigDecimal getPasswd_reset_fee() {
		return passwd_reset_fee;
	}

	public void setPasswd_reset_fee(BigDecimal passwd_reset_fee) {
		this.passwd_reset_fee = passwd_reset_fee;
	}

	public BigDecimal getCard_loss_fee() {
		return card_loss_fee;
	}

	public void setCard_loss_fee(BigDecimal card_loss_fee) {
		this.card_loss_fee = card_loss_fee;
	}

	public BigDecimal getRepint_stmt_fee() {
		return repint_stmt_fee;
	}

	public void setRepint_stmt_fee(BigDecimal repint_stmt_fee) {
		this.repint_stmt_fee = repint_stmt_fee;
	}

	public FirstCardFeeInd getFirst_card_fee_ind() {
		return first_card_fee_ind;
	}

	public void setFirst_card_fee_ind(FirstCardFeeInd first_card_fee_ind) {
		this.first_card_fee_ind = first_card_fee_ind;
	}

	public BigDecimal getB_card_fee() {
		return b_card_fee;
	}

	public void setB_card_fee(BigDecimal b_card_fee) {
		this.b_card_fee = b_card_fee;
	}

	public BigDecimal getS_card_fee() {
		return s_card_fee;
	}

	public void setS_card_fee(BigDecimal s_card_fee) {
		this.s_card_fee = s_card_fee;
	}

	public BigDecimal getUrgent_fee() {
		return urgent_fee;
	}

	public void setUrgent_fee(BigDecimal urgent_fee) {
		this.urgent_fee = urgent_fee;
	}

	public BigDecimal getSms_fee() {
		return sms_fee;
	}

	public void setSms_fee(BigDecimal sms_fee) {
		this.sms_fee = sms_fee;
	}

	public BigDecimal getCard_issue_fee() {
		return card_issue_fee;
	}

	public void setCard_issue_fee(BigDecimal card_issue_fee) {
		this.card_issue_fee = card_issue_fee;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

