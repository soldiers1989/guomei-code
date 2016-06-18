package com.sunline.ccs.param.def;

import java.io.Serializable;
import java.math.BigDecimal;

import com.sunline.ccs.param.def.enums.Arithmetic;
import com.sunline.ark.support.meta.PropertyInfo;

public class CalcOperation implements Serializable {

	private static final long serialVersionUID = 8512128337410417987L;

	/**
	 * 计算方式
	 * 加、减、乘、除
	 */
	@PropertyInfo(name="计算方式", length=9)
	public Arithmetic arithmetic;
	
	/**
	 * 计算数值
	 */
	@PropertyInfo(name="计算数值", length=13, precision=4)
	public BigDecimal value;

}
