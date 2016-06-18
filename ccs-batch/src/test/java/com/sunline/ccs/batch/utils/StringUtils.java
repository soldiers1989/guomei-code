package com.sunline.ccs.batch.utils;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;

/**
* @author fanghj
 */
public class StringUtils {
	/**
	 * 格式化卡号,将16位卡号格式化为"#### #### #### ####"格式
	 * @param cardNo
	 * @return
	 */
	public static String formatCardNo(String cardNo){
		
		cardNo = getFixString(cardNo, 16, '0', true);
		StringBuilder sb = new StringBuilder(cardNo);
		sb.insert(4, ' ');
		sb.insert(9, ' ');
		sb.insert(14, ' ');
		return sb.toString();
	}
	/**
	 * 将日期格式化为MM/YY
	 * @param date YYYYMMDD或YYYYMM格式
	 * @return
	 */
	public static String formatValidDate(Date date){
		DateFormat df = new SimpleDateFormat("MM/yy");
		return df.format(date);
	}
	/**
	 * 将日期格式化为YYMM格式放入磁道信息
	 * @param date YYYYMM或YYYYMMDD
	 * @return
	 */
	public static String formatTrackDate(Date date){
		DateFormat df = new SimpleDateFormat("yyMM");
		return df.format(date);
	}
	/**
	 * 将给定字符串改为给定长度字符串，使用给定字符进行填充，支持左补和右补
	 * @param s 原字符串
	 * @param length 需要的长度
	 * @param padding 填充字符
	 * @param leftPadding 是否左补，否为右补
	 * @return
	 */
	public static String getFixString(String s,int length,char padding,boolean leftPadding){
		if(s == null){
			return getString(padding,length).toString();
		}
		StringBuilder sb;
		if(leftPadding){
			sb = getString(padding,length);
			sb.append(s);
			return sb.substring(sb.length()-length);
		}else{
			sb = new StringBuilder(s);
			sb.append(getString(padding, length));
			return sb.substring(0,length);
		}
	}
	/**
	 * 获得给定长度的字符组成的字符串
	 * @param c
	 * @param length
	 * @return
	 */
	public static StringBuilder getString(char c,int length){
		StringBuilder sb = new StringBuilder();
		for(int i = 0;i<length;i++){
			sb.append(c);
		}
		return sb;
	}
	/**
	 * 移除开始和结束的字符
	 * @param s
	 * @return
	 */
	public static String removeStartAndEnd(String s){
		if(s == null || s.trim().length() == 0){
			return null;
		}
		String mid = s.substring(1);
		return mid.substring(0,mid.length()-1);
	}
	/**
	 * 判断字符串是否为空
	 * @param s
	 * @return
	 */
	public static boolean isEmpty(String s){
		return s == null || s.trim().length() == 0;
	}
	/**
	 * 用于跟踪日志，打印对象所有字段值
	 * @param o
	 * @param msg 日志描述
	 */
	public static void debug(Object o,String msg,Logger logger){
		logger.info("------------------{}-----------------",msg);
		if(o instanceof String){
			logger.info(o.toString());
		}else{
			Field[]  fields = o.getClass().getDeclaredFields();
			for(Field f:fields){
				try {
					boolean changed = false;
					if(!f.isAccessible()){
						changed = true;
						f.setAccessible(true);
					}
					logger.info("[{}]\t=[{}]", f.getName(),f.get(o));
					if(changed){
						f.setAccessible(false);
					}
				} catch(Exception e){
					logger.error("反射错误",e);
				}
			}
		}
		logger.info("-----------------{}-结束--------------",msg);
	}
}
