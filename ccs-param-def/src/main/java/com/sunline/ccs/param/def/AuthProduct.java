package com.sunline.ccs.param.def;

import java.io.Serializable;
import java.util.Map;

import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.ccs.param.def.enums.AuthFlagAction;
import com.sunline.ccs.param.def.enums.AuthReason;
import com.sunline.ccs.param.def.enums.AuthTransTerminal;
import com.sunline.ccs.param.def.enums.AuthVerifyAction;
import com.sunline.ccs.param.def.enums.CheckType;
import com.sunline.ccs.param.def.enums.VerifyEnum;
import com.sunline.ark.support.meta.PropertyInfo;
import com.sunline.ark.support.meta.ReferEnums;

/**
 * 授权系统层参数属性
 */
public class AuthProduct implements Serializable {

	private static final long serialVersionUID = 4728068321665630715L;

	/**
	 * 产品代码
	 */
	@PropertyInfo(name="产品代码", length = 6)
	public String productCode;
	
	/**
	 * 是否支持V卡签到签退交易
	 */
	@PropertyInfo(name="是否支持V卡签到签退交易", length = 1)
	public Boolean isSupportVCardSign;
	
	/**
	 * 授权原因码与行动码映射关系
	 * 此处的AuthReason仅包含AuthReasonGroups中COMMON_REASONS常量中的Reason
	 */
	public Map<AuthReason, AuthAction> reasonActions;
	
	/**
	 * CVV检查控制
	 */
	@ReferEnums({VerifyEnum.class, AuthVerifyAction.class})
 	public Map<VerifyEnum, AuthVerifyAction> verifyActions;

	/**
	 * 授权交易检查类型
	 */
	@ReferEnums({CheckType.class})
 	public Map<CheckType, Boolean> checkEnabled;
 	
 	/**
 	 * 授权产品参数中对交易终端的单独控制
 	 */
 	@ReferEnums({AuthTransTerminal.class, AuthFlagAction.class})
 	public Map<AuthTransTerminal, AuthFlagAction> terminalEnabled;
	
    /**
	 * 交易类型与交易渠道的允许矩阵
	 * AuthTransType： 交易类型(上述枚举)
	 * AuthTransTerminal: 交易渠道(上述枚举)
	 * Boolean: TRUE/FALSE(TRUE-渠道支持当前交易/FALSE-渠道不支持当前交易)
	 */
 	@ReferEnums({AuthTransType.class, AuthTransTerminal.class})
    public Map<AuthTransType, Map<AuthTransTerminal, Boolean>> transTypeTerminalEnabled;

 	/**
 	 * 授权交易是否支持孤立确认标识
 	 */
 	@PropertyInfo(name="支持孤立确认", length = 1)
 	public Indicator isolatedConfirm;
 	
	/**
	 * 授权码组成结构是否包含mcc类型
	 * 
	 */
	@PropertyInfo(name="授权码组成结构包含mcc类型")
    public Boolean authCodeIncMCCFlag;
 
    public boolean isTransTypeTerminalEnabled(AuthTransType type, AuthTransTerminal terminal)
    {
    	if (transTypeTerminalEnabled.containsKey(type))
    	{
    		Map<AuthTransTerminal, Boolean> channelMap = transTypeTerminalEnabled.get(type);
    		if (channelMap.containsKey(terminal))
    			return channelMap.get(terminal);
    	}
    	return false;
    }
}
