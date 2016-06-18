package com.sunline.ccs.param.def;

import java.io.Serializable;

import com.sunline.ark.support.meta.PropertyInfo;

/**
 * 非金融控制字段定义
* @author fanghj
 *
 */
public class NfControlField implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/*
	 * 字段编码
	 */
	@PropertyInfo(name="字段编码", length=30)
	public String fieldCode;
	/*
	 * 字段名称
	 */
	@PropertyInfo(name="字段名称", length=80)
	public String fieldName;
	/*
	 * 优先级
	 */
	@PropertyInfo(name="优先级", length=3)
	public Integer priority;
}
