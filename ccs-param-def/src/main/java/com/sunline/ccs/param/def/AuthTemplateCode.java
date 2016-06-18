package com.sunline.ccs.param.def;

import java.io.Serializable;
import java.util.Map;

import com.sunline.ccs.param.def.enums.AuthCheckPoint;
import com.sunline.ark.support.meta.PropertyInfo;
import com.sunline.ark.support.meta.ReferEnums;

/**
 * 交易码
 */
public class AuthTemplateCode implements Serializable{

	private static final long serialVersionUID = 6828401848956690268L;
	
	/**
     * 模板代码
     */
    @PropertyInfo( name = "模板代码", length = 5 )
    public String tmplateCode;
   
    /**
     * 交易代码描述
     */
    @PropertyInfo( name = "描述", length = 40 )
    public String description;


	/**
	 * 授权交易检查项
	 */
	@ReferEnums({AuthCheckPoint.class})
 	public Map<AuthCheckPoint, Boolean> authCheckPointEnabled;
}
