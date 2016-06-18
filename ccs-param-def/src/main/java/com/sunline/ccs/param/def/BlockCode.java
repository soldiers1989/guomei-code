package com.sunline.ccs.param.def;

import java.io.Serializable;

import com.sunline.ppy.dictionary.enums.SetupIndicator;
import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.ccs.param.def.enums.AuthReason;
import com.sunline.ccs.param.def.enums.BlockLevel;
import com.sunline.ccs.param.def.enums.PaymentCalcMethod;
import com.sunline.ccs.param.def.enums.PostAvailiableInd;
import com.sunline.ark.support.meta.PropertyInfo;

/**
 * BLOCK CODE，键为 {@link #blockCode}
 */
public class BlockCode implements Serializable, Comparable<BlockCode> {

	private static final long serialVersionUID = -6443949660391506845L;

	/**
     * 0-9 : age
     * A-Z
     */
    @PropertyInfo(name="锁定码", length=1)
    public String blockCode;

    /**
     * 描述
     */
    @PropertyInfo(name="描述", length=40)
    public String description;
    
    /**
     * 优先级
     */
    @PropertyInfo(name="优先级", length=3)
    public Integer priority;

    /**
     * 入账许可指示
     */
    @PropertyInfo(name="入账许可指示", length=1)
    public PostAvailiableInd postInd;

    /**
     * 到期续卡标识
     */
    @PropertyInfo(name="到期续卡", length=1)
    public Boolean renewInd;

    /**
     * 是否进行日常利息累积
     */
    @PropertyInfo(name="进行日常利息累积", length=1)
    public Boolean intAccuralInd;

    /**
     * 是否免除利息
     */
    @PropertyInfo(name="免除利息", length=1)
    public Boolean intWaiveInd;

    /**
     * 是否免除交易费
     */
    @PropertyInfo(name="免除交易费", length=1)
    public Boolean txnFeeWaiveInd; 

    /**
     * 是否免除年费
     */
    @PropertyInfo(name="免除年费", length=1)
    public Boolean cardFeeWaiveInd; 

    /**
     * 是否免除超限费
     */
    @PropertyInfo(name="免除超限费", length=1)
    public Boolean ovrlmtFeeWaiveInd;
    
    /**
     * 是否免除滞纳金
     */
    @PropertyInfo(name="免除滞纳金", length=1)
    public Boolean lateFeeWaiveInd; 

    /**
     * 是否输出账单
     */
    @PropertyInfo(name="输出账单", length=1)
    public Boolean stmtInd;

    /**
     * 最小还款额计算方式
     */
    @PropertyInfo(name="最小还款额计算方式", length=1)
    public PaymentCalcMethod paymentInd;

    /**
     * 是否累积积分
     */
    @PropertyInfo(name="累积积分", length=1)
    public Boolean pointEarnInd; 

    /**
     * 分期支持标识
     */
    @PropertyInfo(name="分期支持", length=1)
    public Boolean loanInd; 

    /**
     * 是否入催
     */
    @PropertyInfo(name="进行催收", length=1)
    public Boolean collectionInd; 

    /**
     * 授权决定原因代码
     */
    @PropertyInfo(name="授权决定原因代码",length=4)
    public AuthReason authReason;
 
    /**
     * 取现决定
     */
    @PropertyInfo(name="取现决定", length=1)
    public AuthAction cashAction;
    
    /**
     * 非moto普通消费决定
     */
    @PropertyInfo(name="非moto普通消费决定", length=1)
    public AuthAction nonMotoRetailAction;

    /**
     * moto消费决定
     */
    @PropertyInfo(name="moto消费决定", length=1)
    public AuthAction motoRetailAction;

    /**
     * moto电子子消费决定
     */
    @PropertyInfo(name="moto电子子消费决定", length=1)
    public AuthAction motoElecAction;

    /**
     * 大额分期决定
     */
    @PropertyInfo(name="大额分期决定", length=1)
    public AuthAction specAction;

    /**
     * 代收决定
     */
    @PropertyInfo(name="代收决定", length=1)
    public AuthAction agentAction;

    /**
     * 查询决定
     */
    @PropertyInfo(name="查询决定", length=1)
    public AuthAction inquireAction;

    /**
     * 借记调整决定
     */
    @PropertyInfo(name="借记调整决定", length=1)
    public AuthAction debitAdjustAction;

    /**
     * 系统自动添加标志
     */
    @PropertyInfo(name="系统自动添加标志", length=1)
    public SetupIndicator sysInd;

    /**
     * 信函代码
     */
    @PropertyInfo(name="信函代码", length=8)
    public String letterCd;
    
    /**
     * 锁定码层级
     */
    @PropertyInfo(name="锁定码层级",length=4)
    public BlockLevel blockLevel;
    
    
    /**
     * 是否免除罚金
     */
    @PropertyInfo(name="免除罚金", length=1)
    public Boolean mulctWaiveIND;
    /**
     * 是否免除服务费
     */
    @PropertyInfo(name="免除服务费", length=1)
    public Boolean svcfeeWaiveIND;
    /**
     * 是否免除其他费用（寿险计划包费、保险费、提前计划还款包费）（Y-是/N-否）
     */
    @PropertyInfo(name="免除其他费用(增值服务费)", length=1)
    public Boolean otherfeeWaiveIND;

	@Override
	public int compareTo(BlockCode o) {
		Integer leftPriority = null;
		Integer rightPriority = null;
		
		if (priority == null){
			leftPriority = 0;
		}	else{ 
			leftPriority = priority;
		}
		
		if (o.priority == null){
			rightPriority = 0;
		}	else{
			rightPriority = o.priority;
		}
		
		return leftPriority.compareTo(rightPriority);
	}
}
