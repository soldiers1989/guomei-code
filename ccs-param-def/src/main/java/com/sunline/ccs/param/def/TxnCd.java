package com.sunline.ccs.param.def;

import java.io.Serializable;

import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ccs.param.def.enums.AdjustIndicator;
import com.sunline.ccs.param.def.enums.LogicMod;
import com.sunline.ccs.param.def.enums.TxnType;
import com.sunline.ark.support.meta.PropertyInfo;

/**
 * 交易代码
 */
public class TxnCd implements Serializable {

	private static final long serialVersionUID = -7694875491782901840L;

	/**
     * 交易代码
     */
    @PropertyInfo(name="交易代码", length=4)
    public String txnCd;
    
    /**
     * 交易类型
     */
    @PropertyInfo(name="交易类型", length=3)
    public TxnType txnType;

    /**
     * 描述
     */
    @PropertyInfo(name="描述", length=80)
    public String description;

    /**
     * 简要描述
     * 出现在对账单上面的描述
     */
    @PropertyInfo(name="简要描述", length=20)
    public String shortDesc;

    /**
     * 信用计划类型
     */
    @PropertyInfo(name="信用计划类型", length=1)
    public PlanType planType;

    /**
     * 入账逻辑模块
     */   
    @PropertyInfo(name="入账逻辑模块", length=3)
    public LogicMod logicMod;

    /**
     * 入账是否要检查BlockCode
     * Y/N
     */
    @PropertyInfo(name="入账前检查锁定码", length=1)
    public Boolean blkcdCheckInd;

    /**
     * 是否累计积分标识
     * Y/N
     */
    @PropertyInfo(name="累计积分", length=1)
    public Indicator bonusPntInd;

    /**
     * 是否年费豁免标识
     * Y/N
     */
    @PropertyInfo(name="年费减免", length=1)
    public Boolean feeWaiveInd;

    /**
     * 交易出具对账单标识
     * Y/N
     */
    @PropertyInfo(name="需要出具对账单", length=1)
    public Boolean stmtInd;
    
    /**
     * 调整标识
     * 客服或者内管对该交易的调整标识
     * N-非调整交易码
     * D-可借记调整交易码
     * C-可贷记调整交易码
     */
    @PropertyInfo(name="调整标识", length=1)
    public AdjustIndicator adjustInd;
}
