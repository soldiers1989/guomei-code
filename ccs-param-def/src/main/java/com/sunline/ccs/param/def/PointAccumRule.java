package com.sunline.ccs.param.def;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.sunline.ccs.param.def.enums.Event;
import com.sunline.ark.support.meta.PropertyInfo;
import com.sunline.ark.support.meta.ReferEnums;

/**
 * 积分计算规则
* @author fanghj
 *
 */
public class PointAccumRule implements Serializable {

	private static final long serialVersionUID = 4231709692218170737L;

	/**
	 * 单笔最小积分
	 */
	@PropertyInfo(name="单笔最小积分", length=9)
	public Integer minValue;
	
	/**
	 * 单笔最大积分
	 */
	@PropertyInfo(name="单笔最大积分", length=9)
	public Integer maxValue;
	
	/**
	 * 积分计算列表
	 */
//	@PropertyInfo(name="积分计算列表")
	public List<RateDef> pointRate;
	
	/**
	 * 附加计算规则
	 */
	@ReferEnums({Event.class})
	public Map<Event, CalcOperation> addedRules;
}
