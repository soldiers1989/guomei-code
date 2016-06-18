package com.sunline.ccs.param.def;

import java.io.Serializable;
import java.util.Map;

import com.sunline.ark.support.meta.PropertyInfo;

/**
 * 非金融字段控制定义
 * 
* @author fanghj
 * 
 */

public class NfContrlServMapping implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 服务编码
	 */
	@PropertyInfo(name = "服务编码", length = 8)
	public String servCode;
	
	/**
	 * 服务控制的映射 key 为控制的字段 NfControlField的fieldCode ，false 为不能通过  true 为可以通过
	 */
	public Map<String, Boolean> fieldCodeMap;
	
	@PropertyInfo(name = "描述", length = 300)
	public String memo;
	
//	/**
//	 * 字段编码
//	 */
//	@PropertyInfo(name = "字段编码", length = 8)
//	public String fieldCode;
//	/**
//	 * 是否通过
//	 */
//	@PropertyInfo(name = "是否通过", length = 8)
//	public boolean isPass;

}
