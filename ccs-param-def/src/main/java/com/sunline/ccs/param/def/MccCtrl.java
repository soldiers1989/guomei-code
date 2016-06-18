package com.sunline.ccs.param.def;

import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ark.support.meta.PropertyInfo;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * MCC控制参数
 */
public class MccCtrl implements Serializable {

	private static final long serialVersionUID = 3681714831265132289L;

	/**
     * 接入卡组织
     */
    @PropertyInfo(name="接入卡组织", length=8)
    public InputSource inputSource;
    
    /**
     * MCC
     */
    @PropertyInfo(name="MCC", length=4)
    public String mcc;

    /**
     * 本币账户单笔限额
     */
    @PropertyInfo(name="本币账户单笔限额", length=15, precision=2)
    public BigDecimal maxTxnAmtLcl;

    /**
     * 外币账户单笔限额
     */
    @PropertyInfo(name="外币账户单笔限额", length=15, precision=2)
    public BigDecimal maxTxnAmtFrg;

    /**
     * 是否累计积分
     */
    @PropertyInfo(name="累计积分", length=1)
    public Boolean bonusPntInd;

    /**
     * MCC交易允许标识
     * Y - 允许
     * N - 禁止交易
     */
    @PropertyInfo(name="允许交易", length=1)
    public Boolean validInd;
    
    /**
     * @param brand
     * @param mcc
     * @return 组装key
     */
    public static String assemblingMccCtrlKey(String mcc, InputSource input) {
    	return mcc + "|" + input;
    }
}
