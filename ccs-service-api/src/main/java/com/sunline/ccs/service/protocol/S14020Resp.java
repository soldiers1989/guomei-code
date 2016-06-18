package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.BscSuppIndicator;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.Relationship;
import com.sunline.ppy.dictionary.enums.RenewInd;
import com.sunline.ppy.dictionary.enums.RenewResult;

public class S14020Resp implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String P_Cardno = "cardNbr";
    public static final String P_FirstCardno = "firstCardNbr";
    public static final String P_LastExpiryDate = "oldExpiryDate";
    public static final String P_BlockCd = "blockCode";
    public static final String P_ActivateInd = "activeInd";
    public static final String P_ActivateDate = "activateDate";

    /**
     * 卡号
     */
    public String card_no;
    /**
     * 持卡人姓名
     */
    public String cardholder_name;
    /**
     * 卡产品名称
     */
    public String product_name;
    /**
     * 主附卡指示
     */
    public BscSuppIndicator bsc_supp_ind;
    /**
     * 主卡卡号
     */
    public String bsc_card_no;
    /**
     * 发卡网点
     */
    public String owning_branch;
    /**
     * 创建日期
     */
    public Date setup_date;
    /**
     * 锁定码
     */
    public String block_code;
    /**
     * 是否已激活
     */
    public Indicator activate_ind;
    /**
     * 激活日期
     */
    public Date activate_date;
    /**
     * 销卡销户日期
     */
    public Date cancel_date;
    /**
     * 是否消费凭密
     */
    public Indicator pos_pin_verify_ind;
    /**
     * 与主卡持卡人关系
     */
    public Relationship relationship;
    /**
     * 卡片有效日期
     */
    public Date card_expire_date;
    /**
     * 下个年费收取日期
     */
    public Date next_card_fee_date;
    /**
     * 续卡标识
     */
    public RenewInd renew_ind;
    /**
     * 续卡拒绝原因码
     */
    public RenewResult renew_reject_cd;
    /**
     * 是否新发卡
     */
    public Indicator new_card_issue_ind;
    /**
     * 是否存在查询密码
     */
    public Indicator q_pin_exist_ind;
    /**
     * 是否存在交易密码
     */
    public Indicator p_pin_exist_ind;
    /**
     * 交易密码错误次数
     */
    public Integer pin_tries;
    /**
     * 查询密码错误次数
     */
    public Integer inq_pin_tries;
    /**
     * 上次密码错时间
     */
    public Date last_pin_tries_time;
    /**
     * 上次查询密码错误时间
     */
    public Date last_inq_pin_tries_time;
    /**
     * 是否超过查询密码次数
     */
    public Indicator inq_pin_limiterr_ind;
    /**
     * 是否超过交易密码次数
     */
    public Indicator pin_limiterr_ind;

    /**
     * CVV2错误次数
     */
    public Integer cvv2_tries;

    /**
     * CVV2是否锁定
     */
    public Indicator cvv2_limiterr_ind;

    /**
     * CVV错误次数
     */
    public Integer cvv_tries;
    /**
     * CVV是否锁定
     */
    public Indicator cvv_limiterr_ind;
    /**
     * ICVV错误次数
     */
    public Integer icvv_tries;
    /**
     * ICVV是否锁定
     */
    public Indicator icvv_limiterr_ind;

    public String getCardholder_name() {
	return cardholder_name;
    }

    public void setCardholder_name(String cardholder_name) {
	this.cardholder_name = cardholder_name;
    }

    public String getCard_no() {
	return card_no;
    }

    public String getProduct_name() {
	return product_name;
    }

    public BscSuppIndicator getBsc_supp_ind() {
	return bsc_supp_ind;
    }

    public String getBsc_card_no() {
	return bsc_card_no;
    }

    public String getOwning_branch() {
	return owning_branch;
    }

    public Date getSetup_date() {
	return setup_date;
    }

    public String getBlock_code() {
	return block_code;
    }

    public Indicator getActivate_ind() {
	return activate_ind;
    }

    public Date getActivate_date() {
	return activate_date;
    }

    public Date getCancel_date() {
	return cancel_date;
    }

    public Indicator getPos_pin_verify_ind() {
	return pos_pin_verify_ind;
    }

    public Relationship getRelationship() {
	return relationship;
    }

    public Date getCard_expire_date() {
	return card_expire_date;
    }

    public Date getNext_card_fee_date() {
	return next_card_fee_date;
    }

    public RenewInd getRenew_ind() {
	return renew_ind;
    }

    public RenewResult getRenew_reject_cd() {
	return renew_reject_cd;
    }

    public Indicator getNew_card_issue_ind() {
	return new_card_issue_ind;
    }

    public Indicator getQ_pin_exist_ind() {
	return q_pin_exist_ind;
    }

    public Indicator getP_pin_exist_ind() {
	return p_pin_exist_ind;
    }

    public Integer getPin_tries() {
	return pin_tries;
    }

    public Integer getInq_pin_tries() {
	return inq_pin_tries;
    }

    public Date getLast_pin_tries_time() {
	return last_pin_tries_time;
    }

    public Date getLast_inq_pin_tries_time() {
	return last_inq_pin_tries_time;
    }

    public void setCard_no(String card_no) {
	this.card_no = card_no;
    }

    public void setProduct_name(String product_name) {
	this.product_name = product_name;
    }

    public void setBsc_supp_ind(BscSuppIndicator bsc_supp_ind) {
	this.bsc_supp_ind = bsc_supp_ind;
    }

    public void setBsc_card_no(String bsc_card_no) {
	this.bsc_card_no = bsc_card_no;
    }

    public void setOwning_branch(String owning_branch) {
	this.owning_branch = owning_branch;
    }

    public void setSetup_date(Date setup_date) {
	this.setup_date = setup_date;
    }

    public void setBlock_code(String block_code) {
	this.block_code = block_code;
    }

    public void setActivate_ind(Indicator activate_ind) {
	this.activate_ind = activate_ind;
    }

    public void setActivate_date(Date activate_date) {
	this.activate_date = activate_date;
    }

    public void setCancel_date(Date cancel_date) {
	this.cancel_date = cancel_date;
    }

    public void setPos_pin_verify_ind(Indicator pos_pin_verify_ind) {
	this.pos_pin_verify_ind = pos_pin_verify_ind;
    }

    public void setRelationship(Relationship relationship) {
	this.relationship = relationship;
    }

    public void setCard_expire_date(Date card_expire_date) {
	this.card_expire_date = card_expire_date;
    }

    public void setNext_card_fee_date(Date next_card_fee_date) {
	this.next_card_fee_date = next_card_fee_date;
    }

    public void setRenew_ind(RenewInd renew_ind) {
	this.renew_ind = renew_ind;
    }

    public void setRenew_reject_cd(RenewResult renew_reject_cd) {
	this.renew_reject_cd = renew_reject_cd;
    }

    public void setNew_card_issue_ind(Indicator new_card_issue_ind) {
	this.new_card_issue_ind = new_card_issue_ind;
    }

    public void setQ_pin_exist_ind(Indicator q_pin_exist_ind) {
	this.q_pin_exist_ind = q_pin_exist_ind;
    }

    public void setP_pin_exist_ind(Indicator p_pin_exist_ind) {
	this.p_pin_exist_ind = p_pin_exist_ind;
    }

    public void setPin_tries(Integer pin_tries) {
	this.pin_tries = pin_tries;
    }

    public void setInq_pin_tries(Integer inq_pin_tries) {
	this.inq_pin_tries = inq_pin_tries;
    }

    public void setLast_pin_tries_time(Date last_pin_tries_time) {
	this.last_pin_tries_time = last_pin_tries_time;
    }

    public void setLast_inq_pin_tries_time(Date last_inq_pin_tries_time) {
	this.last_inq_pin_tries_time = last_inq_pin_tries_time;
    }

    public Indicator getInq_pin_limiterr_ind() {
	return inq_pin_limiterr_ind;
    }

    public void setInq_pin_limiterr_ind(Indicator inq_pin_limiterr_ind) {
	this.inq_pin_limiterr_ind = inq_pin_limiterr_ind;
    }

    public Indicator getPin_limiterr_ind() {
	return pin_limiterr_ind;
    }

    public void setPin_limiterr_ind(Indicator pin_limiterr_ind) {
	this.pin_limiterr_ind = pin_limiterr_ind;
    }

    public Integer getCvv2_tries() {
	return cvv2_tries;
    }

    public void setCvv2_tries(Integer cvv2_tries) {
	this.cvv2_tries = cvv2_tries;
    }

    public Indicator getCvv2_limiterr_ind() {
	return cvv2_limiterr_ind;
    }

    public void setCvv2_limiterr_ind(Indicator cvv2_limiterr_ind) {
	this.cvv2_limiterr_ind = cvv2_limiterr_ind;
    }

    public Integer getCvv_tries() {
	return cvv_tries;
    }

    public void setCvv_tries(Integer cvv_tries) {
	this.cvv_tries = cvv_tries;
    }

    public Indicator getCvv_limiterr_ind() {
	return cvv_limiterr_ind;
    }

    public void setCvv_limiterr_ind(Indicator cvv_limiterr_ind) {
	this.cvv_limiterr_ind = cvv_limiterr_ind;
    }

    public Integer getIcvv_tries() {
	return icvv_tries;
    }

    public void setIcvv_tries(Integer icvv_tries) {
	this.icvv_tries = icvv_tries;
    }

    public Indicator getIcvv_limiterr_ind() {
	return icvv_limiterr_ind;
    }

    public void setIcvv_limiterr_ind(Indicator icvv_limiterr_ind) {
	this.icvv_limiterr_ind = icvv_limiterr_ind;
    }

    public String toString() {
	return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
