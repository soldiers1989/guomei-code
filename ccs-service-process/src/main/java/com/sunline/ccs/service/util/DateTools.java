package com.sunline.ccs.service.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.service.api.Constants;

/** 
 * @see 类名：DateTools
 * @see 描述：日期工具类
 *
 * @see 创建日期：   2015年6月24日 下午2:52:15
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class DateTools {
	
	public static void main(String args[]){
		try {
			DateTools.parseStmtDate("20101a");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 得到最近的一个日期
	 * getSoonDate(这里用一句话描述这个方法的作用)  
	 * @param date
	 * @param cycle
	 * @return   
	 *Date  
	 * @exception   
	 * @since  1.0.0
	 */
	public static Date getSoonDate(Date date,int cycle){
		Date nextdate = DateUtils.setDays(date, cycle);
		if(dateCompare(nextdate, date)<1){
			return DateUtils.addMonths(nextdate, 1);
		}
		return nextdate;
	}

	/**
	 * 日期比较
	 * 
	 * @param date1
	 * @param date2
	 * @return int date1 > date2 =1；date1 = date2 =0；date1 <date2 =-1
	 * @exception
	 * @since 1.0.0
	 */
	public static int dateCompare(Date date1, Date date2) {
		return dateCompare(DateFormatUtils.ISO_DATE_FORMAT.format(date1), DateFormatUtils.ISO_DATE_FORMAT.format(date2));
	}

	/**
	 * 日期比较
	 * 
	 * @param date1
	 * @param date2
	 * @return int date1 > date2 =1；date1 = date2 =0；date1 <date2 =-1
	 * @exception
	 * @since 1.0.0
	 */
	public static int dateCompare(String date1, String date2) {
		int i = 0;
		SimpleDateFormat sm = new SimpleDateFormat("yyyy-MM-dd");
		try {
			i = sm.parse(date1).compareTo(sm.parse(date2));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return i;
	}

	/**
	 * 判断middDate是否在startDate,endDate区间，包含等于
	 * 
	 * @param startDate
	 * @param middDate
	 * @param endDate
	 * @return boolean
	 * @exception
	 * @since 1.0.0
	 */
	public static boolean dateBetwen_IncludeEQ(Date startDate, Date middDate, Date endDate) {
		return dateCompare(middDate, startDate) >= 0 && dateCompare(endDate, middDate) >= 0;
	}

	/**
	 * 判断middDate是否在startDate,endDate区间，包含等于
	 * 
	 * @param startDate
	 * @param middDate
	 * @param endDate
	 * @return boolean
	 * @exception
	 * @since 1.0.0
	 */
	public static boolean dateBetwen_IncludeEQ(String startDate, String middDate, String endDate) {
		return dateCompare(middDate, startDate) >= 0 && dateCompare(endDate, middDate) >= 0;
	}

	/**
	 * 判断middDate是否在startDate,endDate区间，不包含等于
	 * 
	 * @param startDate
	 * @param middDate
	 * @param endDate
	 * @return boolean
	 * @exception
	 * @since 1.0.0
	 */
	public static boolean dateBetwen(String startDate, String middDate, String endDate) {
		return dateCompare(middDate, startDate) > 0 && dateCompare(endDate, middDate) > 0;
	}

	/**
	 * 判断middDate是否在startDate,endDate区间，不包含等于
	 * 
	 * @param startDate
	 * @param middDate
	 * @param endDate
	 * @return boolean
	 * @exception
	 * @since 1.0.0
	 */
	public static boolean dateBetwen(Date startDate, Date middDate, Date endDate) {
		return dateCompare(middDate, startDate) > 0 && dateCompare(endDate, middDate) > 0;
	}

	/**
	 * 格式化开始日期 startDateStamp
	 * 
	 * @param date
	 * @return Date XXXX-XX-XX:00:00:00
	 * @exception
	 * @since 1.0.0
	 */
	public static Date startDateStamp(Date date) {
		return DateUtils.truncate(date, Calendar.DATE);
	}

	/**
	 * 格式化结束日期 endDateStamp
	 * 
	 * @param date
	 * @return Date XXXX-XX-XX:23:59:59
	 * @exception
	 * @since 1.0.0
	 */
	public static Date endDateStamp(Date date) {
		Calendar endday = Calendar.getInstance();
		endday.setTime(date);
		endday.set(Calendar.HOUR_OF_DAY, 23);
		endday.set(Calendar.MINUTE, 59);
		endday.set(Calendar.SECOND, 59);
		return endday.getTime();
	}

	/**
	 * Convert a Date into a Calendar object.
	 * 
	 * @param date
	 *            the date to convert to a Calendar
	 * @return the created Calendar
	 * @throws NullPointerException
	 *             if null is passed in
	 * @since 2.6
	 */
	public static Calendar toCalendar(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c;
	}

	/**
	 * 获取月份第一天的日期
	 */
	public static Date getMontdFirst(Date date) {
		if (date == null)
			return null;
		Calendar c = toCalendar(date);
		c.set(Calendar.DAY_OF_MONTH, 1);
		return date;
	}
	
	/**
	 * 将账单年月转为日期类型返回当月1号
	 * 
	 * @param stmtDate
	 * @return
	 * @throws ProcessException
	 */
	public static Date parseStmtDate(String stmtDate) throws ProcessException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
		sdf.setLenient(false);
		
		if(stmtDate.length() !=6 || !StringUtils.isNumeric(stmtDate)){
			throw new ProcessException(Constants.ERRB079_CODE, Constants.ERRB079_MES);
		}
		try {
			return sdf.parse(stmtDate);
		} catch (ParseException e) {
			throw new ProcessException(Constants.ERRB079_CODE, Constants.ERRB079_MES);
		}
	}
	
	/**
	 * 得到日期中天数,例如20120204返回4
	 * @param date
	 * @return day
	 */
	public static int getDayOfDate(Date date){
		int day = 0;
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		day = c.get(Calendar.DAY_OF_MONTH);
		return day;
	}
	
	/**
	 * 年月日和月份相加
	 * @param args
	 * @throws Exception
	 */
	public static Date getDateAddMount(Date date,Integer amount){
	     Calendar rightNow = Calendar.getInstance();
	     rightNow.setTime(date);
	     rightNow.add(Calendar.MONTH,amount);
	     return rightNow.getTime();
	}

	/**
	 * 日期大小比较
	 * @param expiryDate
	 * @param batchDate
	 * @return
	 */
	public static boolean isExpiry(Date rescheduleEndDateDate,Date expiryDate){
		DateFormat df = new SimpleDateFormat("yyyyMM");
		long expiry = Long.parseLong(df.format(expiryDate));
		long reschedule = Long.parseLong(df.format(rescheduleEndDateDate));
		return reschedule <= expiry;
	}
	
	/** 
     * 计算两个日期之间相差的天数 
     * @param date1 
     * @param date2 
     * @return 
     */  
    public static int daysBetween(Date date1,Date date2){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date1);
        long time1 = cal.getTimeInMillis();               
        cal.setTime(date2);
        long time2 = cal.getTimeInMillis();    
        long between_days=(time2-time1)/(1000*3600*24);
          
       return Integer.parseInt(String.valueOf(between_days));         
    }  
	
}
