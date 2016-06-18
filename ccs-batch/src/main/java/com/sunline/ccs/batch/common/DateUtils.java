package com.sunline.ccs.batch.common;

import java.util.Calendar;
import java.util.Date;

/**
 * @see 类名：DateUtils
 * @see 描述：扩展日期工具类
 *
 * @see 创建日期：   2015-6-24下午5:34:40
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class DateUtils extends org.apache.commons.lang.time.DateUtils {
	/**
	 * @see 方法名：getLastDayOfMonth 
	 * @see 描述：获得月底最后一天的日期
	 * @see 创建日期：2015-6-24下午5:34:55
	 * @author ChengChun
	 *  
	 * @param batchDate
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public static Date getLastDayOfMonth(Date batchDate){
		Calendar c = Calendar.getInstance();
		c.setTime(batchDate);
		c.add(Calendar.MONTH, 1);
		c.set(Calendar.DATE, 1);
		c.add(Calendar.DATE, -1);
		return c.getTime();
	}
	
	/**
	 * @see 方法名：getMonthInterval 
	 * @see 描述：获得给定两个日期之间相隔的月数，算到日
	 * @see 创建日期：2015-6-24下午5:35:07
	 * @author ChengChun
	 *  
	 * @param from
	 * @param to
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public static int getMonthInterval(Date from,Date to){
		Calendar f = Calendar.getInstance();
		f.setTime(from);
		Calendar t = Calendar.getInstance();
		t.setTime(to);
		int year =t.get(Calendar.YEAR) - f.get(Calendar.YEAR);
		int month = t.get(Calendar.MONTH) - f.get(Calendar.MONTH);
		return year*12+month;
	}
	/**
	 * @see 方法名：getIntervalDays 
	 * @see 描述：获得两个日期间隔天数
	 * @see 创建日期：2015-6-24下午5:35:21
	 * @author ChengChun
	 *  
	 * @param from
	 * @param to
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public static int getIntervalDays(Date from,Date to){
		long interval = truncate(to, Calendar.DATE).getTime()-truncate(from, Calendar.DATE).getTime();
		int result = (int)(interval/(1000*3600*24));
		return result;
	}
	
	/**
	 * @see 方法名：isMonthEnd 
	 * @see 描述：判断给定日期是否月底
	 * @see 创建日期：2015-6-24下午5:35:35
	 * @author ChengChun
	 *  
	 * @param date
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public static boolean isMonthEnd(Date date){
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, 1);
		return c.get(Calendar.DATE) == 1;
	}
}
