package com.sunline.ccs.batch.common;

public class EnumUtils {
	/**
	 * @see 方法名：in 
	 * @see 描述：判断给定的枚举e是否在enums列表中出现
	 * @see 创建日期：2015-6-24下午5:35:52
	 * @author ChengChun
	 *  
	 * @param e
	 * @param enums
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public static boolean in(Enum<?> e,Enum<?>...enums){
		for(Enum<?> i : enums){
			if(e == i){
				return true;
			}
		}
		return false;
	}
}
