package com.sunline.ccs.param.def;

import java.io.Serializable;
import java.util.Date;

import com.sunline.ccs.param.def.enums.PointPlanType;
import com.sunline.ark.support.meta.PropertyInfo;

/**
 * 积分计划参数
* @author fanghj
 *
 */
public class PointPlan implements Serializable {

	private static final long serialVersionUID = -4049953037538730522L;

	/**
	 * 积分计划编号
	 */
	@PropertyInfo(name="积分计划编号", length=6)
	public String planNbr;
	
	/**
	 * 描述
	 */
	@PropertyInfo(name="描述", length=200)
	public String description;
	
	/**
	 * 积分计划类型
	 */
	@PropertyInfo(name="积分计划类型", length=1)
	public PointPlanType planType;
	
	/**
	 * 积分计划起始日期
	 */
	@PropertyInfo(name="计划开始日期")
	public Date startDate;
	
	/**
	 * 积分计划结束日期
	 */
	@PropertyInfo(name="计划结束日期")
	public Date endDate;
	
	/**
	 * 单笔交易过滤规则
	 */
	//@PropertyInfo(name="交易过滤规则")
	public TxnFilterRule filterRule;
	
	/**
	 * 单笔交易积分规则
	 */
	//@PropertyInfo(name="积分规则")
	public PointAccumRule accumRule;
}
