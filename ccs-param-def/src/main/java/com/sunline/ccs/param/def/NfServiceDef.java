package com.sunline.ccs.param.def;

import java.io.Serializable;

import com.sunline.ark.support.meta.PropertyInfo;


/**
 * 非金融服务定义
* @author fanghj
 *
 */
public class NfServiceDef implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1479985635656575600L;

	/**
	 * 服务编码
	 */
	@PropertyInfo(name="服务编码", length=8)
	public String servCode;
	
	/**
	 * 服务描述
	 */
	@PropertyInfo(name="服务描述", length=80)
	public String servDesc;

}
