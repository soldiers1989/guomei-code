package com.sunline.ccs.param.def;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.sunline.ccs.param.def.enums.TierInd;
import com.sunline.ccs.param.def.enums.WaiveInd;
import com.sunline.ark.support.meta.PropertyInfo;

/**
 * 交易费参数
 */
public class TxnFee implements Serializable {

	private static final long serialVersionUID = -3485684742192831359L;
    
    /**
     * 费用交易代码
     * 指向参数TxnCd的主键
     */
    @PropertyInfo(name="费用交易代码", length=4)
    public String feeTxnCd;
    
    /**
     * 溢缴款收费标识
     */
    @PropertyInfo(name="溢缴款收费标识", length=1)
    public WaiveInd despositWaiveInd;
    
    /**
     * 溢缴款费用最大金额
     */
    @PropertyInfo(name="溢缴款费用最大金额", length=15, precision=2)
    public BigDecimal maxDespositWaiveFee;
    
    /**
     * 溢缴款费用最小金额
     */
    @PropertyInfo(name="溢缴款费用最小金额", length=15, precision=2)
    public BigDecimal minDespositWaiveFee;

    /**
     * 溢缴款手续费固定附加
     */
    @PropertyInfo(name="溢缴款手续费固定附加", length=15, precision=2)
    public BigDecimal despositBaseFee;
    
    /**
     * 溢缴款费率
     */
    @PropertyInfo(name="溢缴款费率", length=7, precision=4)
    public BigDecimal despositRate;
    
    /**
     * 透支收费标识
     */
    @PropertyInfo(name="透支收费标识", length=1)
    public WaiveInd chargeWaiveInd;
    
    /**
     * 透支费用最大金额
     */
    @PropertyInfo(name="透支费用最大金额", length=15, precision=2)
    public BigDecimal maxChargeWaiveFee;
    
    /**
     * 透支费用最小金额
     */
    @PropertyInfo(name="透支费用最小金额", length=15, precision=2)
    public BigDecimal minChargeWaiveFee;
    
    /**
     * 透支费用计算层级指示
     */
    @PropertyInfo(name="透支费用计算层级指示", length=1)
    public TierInd tierInd;
    
    /**
     * 透支费率
     */
    public List<RateDef> chargeRates;
    
}
