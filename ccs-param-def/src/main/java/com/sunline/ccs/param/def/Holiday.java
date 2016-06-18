package com.sunline.ccs.param.def;

import com.sunline.ark.support.meta.PropertyInfo;
import java.io.Serializable;
import java.util.Date;

/**
 * 节假日参数
 */
public class Holiday implements Serializable {

	private static final long serialVersionUID = -9046053810645275138L;

	/**
     * 日期
     */
    @PropertyInfo(name="CALENDAR_DATE")
    public Date calendarDate;

    /**
     * 是否节假日
     */
    @PropertyInfo(name="是否节假日", length=1)
    public Boolean holidayInd;

    /**
     * 非处理日标识
     * Y - 非处理日
     * N - 处理日
     */
    @PropertyInfo(name="非处理日标识", length=1)
    public Boolean noprocInd;
}
