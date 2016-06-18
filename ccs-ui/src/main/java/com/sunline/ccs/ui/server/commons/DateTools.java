package com.sunline.ccs.ui.server.commons;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;

import com.sunline.ppy.dictionary.exception.ProcessException;

public class DateTools {

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
		return dateCompare(DateFormatUtils.ISO_DATE_FORMAT.format(date1),
				DateFormatUtils.ISO_DATE_FORMAT.format(date2));
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
	public static boolean dateBetwen_IncludeEQ(Date startDate, Date middDate,
			Date endDate) {
		return dateCompare(middDate, startDate) >= 0
				&& dateCompare(endDate, middDate) >= 0;
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
	public static boolean dateBetwen_IncludeEQ(String startDate,
			String middDate, String endDate) {
		return dateCompare(middDate, startDate) >= 0
				&& dateCompare(endDate, middDate) >= 0;
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
	public static boolean dateBetwen(String startDate, String middDate,
			String endDate) {
		return dateCompare(middDate, startDate) > 0
				&& dateCompare(endDate, middDate) > 0;
	}

	/**
	 * 格式化开始日期 startDateStamp
	 * 
	 * @param date
	 * @return Date XXXX-XX-XX:00:00:00
	 * @exception
	 * @since 1.0.0
	 */
	public static  Date startDateStamp(Date date) {
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

	public static Date getDateValue(Object value)
	{
		if (value == null)
			return null;
		return (Date)value; 
	}
	
	/**
	 * 卡片有效期格式-年月
	 * @param str
	 * @return
	 * @throws ProcessException
	 */
	 public static String convertToYearAndMonth(Date str) throws ProcessException{
		 SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月");
		 String dateString = formatter.format(str);
		 return dateString;
	   }
	
}
