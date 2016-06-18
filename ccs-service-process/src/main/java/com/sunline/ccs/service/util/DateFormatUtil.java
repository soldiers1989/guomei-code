package com.sunline.ccs.service.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatUtil {
	
	public static final String PATTERN_DFT = "yyyyMMdd"; 
	
	public static String format(Date date ){
		return format(date, PATTERN_DFT);
	}
	public static String format(Date date, String pattern){
		if(date == null) return null;
		if(pattern == null){
			throw new IllegalArgumentException("请指定日期格式参数pattern ");
		}
		SimpleDateFormat sdf = null;
		try {
			sdf = new SimpleDateFormat(pattern);
		} catch (Exception e) {
			throw new IllegalArgumentException("无效的日期格式参数:["+pattern+"]");
		}
		
		return sdf.format(date);
	}

}
