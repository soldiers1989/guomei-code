package com.sunline.ccs.param.def;

import java.io.Serializable;
import java.util.Date;

import com.sunline.ark.support.meta.PropertyInfo;

public class TimePeriod implements Serializable {

	private static final long serialVersionUID = 4705135891259830909L;

	/**
	 * 开始时间
	 * 仅保存小时和分，不包括日期，判断时不能直接使用Date类型比大小
	 */
	@PropertyInfo(name="开始时间")
	public Date startTime;
	
	/**
	 * 结束时间
	 * 仅保存小时和分，不包括日期，判断时不能直接使用Date类型比大小
	 */
	@PropertyInfo(name="结束时间")
	public Date endTime;
}
