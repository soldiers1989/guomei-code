package com.sunline.ccs.service.util;

import org.apache.commons.lang.StringUtils;

/** 
 * @see 类名：EnumUtils
 * @see 描述：枚举的工具类
 *
 * @see 创建日期：   2015年6月24日 下午2:52:35
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class EnumUtils {
	
	/**
	 * 判断枚举e是否存在于枚举enums中
	 * @param e
	 * @param enums
	 * @return
	 */
	public static boolean in(Enum<?> e,Enum<?>...enums){
		for(Enum<?> i : enums){
			if(e == i){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 判断字符串e是否存在于枚举enums中
	 * @param e
	 * @param enums
	 * @return
	 */
	public static boolean in(String e,Enum<?>...enums){
		for(Enum<?> i : enums){
			if(i.name().equals(e)){
				return true;
			}
		}
		return false;
	}

}
