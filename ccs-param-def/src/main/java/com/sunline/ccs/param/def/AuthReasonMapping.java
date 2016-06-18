package com.sunline.ccs.param.def;

import java.io.Serializable;

import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.ccs.param.def.enums.AuthReason;
import com.sunline.ark.support.meta.PropertyInfo;

/**
 * 原因码定义映射表，主键为 {@link #inputSource} + {@link #reason}
 */
public class AuthReasonMapping implements Serializable {

	private static final long serialVersionUID = 885194298486921554L;

	/**
	 * 接入卡组织(主键)
	 */
    @PropertyInfo(name="接入卡组织", length = 4)
    public InputSource inputSource;
    
    /**
     * 业务错误原因码(主键)
     */
    @PropertyInfo(name="业务错误原因码", length = 4)
    public AuthReason reason;
    
    /**
     * 默认获取的Action
     */
    @PropertyInfo(name="默认获取的Action", length = 1)
    public AuthAction defaultAction;
    
    /**
     * 业务错误原因类型
     */
    @PropertyInfo(name="业务错误原因类型", length = 40)
    public String businessErrorReasonType;
    
    /**
     * 原因码级别
     */
    @PropertyInfo(name="原因码级别", length = 4)
    public Integer reasonCodeRank;
    
    /**
     * 原因描述
     */
    @PropertyInfo(name="原因描述", length = 255)
    public String reasonCodeDescribe;
    
    /**
     * 是否可参数配置
     * Y/N
     */
    @PropertyInfo(name="是否可参数配置", length = 1)
    public Boolean parameterIsConfigurable;
    
    /**
     * 是否可人工撤销
     * Y/N
     */
    @PropertyInfo(name="是否可人工撤销", length = 1)
    public Boolean manualIsRepeal;
    
    /**
     * 吞卡返回码 
     */
    @PropertyInfo(name="吞卡返回码", length = 2)
    public String pickupResponse;
    
    /**
     * 拒绝返回码 
     */
    @PropertyInfo(name="拒绝返回码", length = 2)
    public String declineResponse;
    
    /**
     * 联系返回码 
     */
    @PropertyInfo(name="联系返回码", length = 2)
    public String callResponse;
    
    /**
     * 通过返回码 
     */
    @PropertyInfo(name="通过返回码", length = 2)
    public String approveResponse;
    
}
