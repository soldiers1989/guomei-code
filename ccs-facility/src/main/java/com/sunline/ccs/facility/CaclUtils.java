package com.sunline.ccs.facility;

import java.math.BigDecimal;

/**
 * 
 * @see 类名：CaclUtils
 * @see 描述：运算相关工具类
 *
 * @see 创建日期：   2015年6月23日下午2:25:38
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class CaclUtils {
	
	/**
	 * 
	 * @see 方法名：checkPositiveNum 
	 * @see 描述：和零修正，检查数据，
	 * 			如果小于0返回0,
	 * 			如果为空返回0
	 * @see 创建日期：2015年6月23日下午2:33:24
	 * @author liruilin
	 *  
	 * @param num 待检查数据
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public static BigDecimal checkPositive(BigDecimal num){
		BigDecimal res = BigDecimal.ZERO;
		if (num != null){
			res = num.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : num;
		}
		return res;
	}
	
	/**
	 * 
	 * @see 方法名：checkPositive 
	 * @see 描述：和零修正，检查数据，
	 * 			如果小于0返回0,
	 * 			如果为空返回0
	 * @see 创建日期：2015年6月23日下午3:17:33
	 * @author liruilin
	 *  
	 * @param num 待检查数据
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public static Integer checkPositive(Integer num){
		Integer res = Integer.valueOf(0);
		if (num != null){
			res = num < 0 ? res : num;
		}
		return res;
	}
	
	/**
	 * 
	 * @see 方法名：setScale2HalfUp 
	 * @see 描述：设定精度为2，4舍5入
	 * 			如果为空返回0.00
	 * @see 创建日期：2015年6月23日下午2:52:51
	 * @author liruilin
	 *  
	 * @param num 待处理数据
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public static BigDecimal setScale2HalfUp(BigDecimal num){
		if (num == null){
			num = BigDecimal.ZERO;
		}
		return num.setScale(2, BigDecimal.ROUND_HALF_UP);
	}
	
}
