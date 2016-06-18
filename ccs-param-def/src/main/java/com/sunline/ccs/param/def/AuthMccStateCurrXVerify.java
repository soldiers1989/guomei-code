package com.sunline.ccs.param.def;

import java.io.Serializable;
import java.math.BigDecimal;

import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ark.support.meta.PropertyInfo;

/**
 * 授权系统mcc/countrycode/currencecode交叉控制表
 * 主键为 {@link #brand} + {@link #mccCode} + {@link #countryCode} + {@link #transCurrencyCode}
 * 取参数时，只允许获取所有列表，然后遍历
 */
public class AuthMccStateCurrXVerify implements Serializable {

	private static final long serialVersionUID = 8381842244583858453L;

	/**
	 * 接入卡组织(主键)
	 */
    @PropertyInfo(name="接入卡组织", length=2)
    public  InputSource inputSource;
    
   /**
    * mcc码(主键)
    */
    @PropertyInfo(name="MCC", length=4)
    public String mccCode;
    
    /**
     * 国家代码(主键)
     */
    @PropertyInfo(name="国家代码", length=3)
    public String countryCode;
    
    /**
     * 交易币种代码(主键)
     */
    @PropertyInfo(name="交易币种代码", length=3)
    public String transCurrencyCode;
    
    /**
     * 当前是否生效标志
     */
    @PropertyInfo(name="当前生效", length=1)
    public Boolean currentActiveFlag;
    
    /**
     * 是否禁止此组合交易
     */
    @PropertyInfo(name="禁止此组合交易", length=1)
    public Boolean forbiddenFlag;
    
    /**
     * 本币账户单笔限额
     */
    @PropertyInfo(name="本币账户单笔限额", length=17, precision=2)
    public BigDecimal maxAmtLcl;

    /**
     * 外币账户单笔限额
     */
    @PropertyInfo(name="外币账户单笔限额", length=17, precision=2)
    public BigDecimal maxAmtFrg;
    
    /**
     * @param brand
     * @param mccCode
     * @param countryCode
     * @param transCurrencyCode
     * @return 组装完的key
     */
    public static String assemblingKey(InputSource inputSource, String mccCode, String countryCode, String transCurrencyCode) {
    	return inputSource + "|" + mccCode + "|" + countryCode + "|" + transCurrencyCode;
    }
    
}
