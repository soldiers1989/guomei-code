package com.sunline.ccs.service.util;

import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.service.api.Constants;

/** 
 * @see 类名：PageTools
 * @see 描述：翻页工具类
 *
 * @see 创建日期：   2015年6月24日 下午2:53:32
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class PageTools {

	/**
	 * 返回是否有下一页
	 * 
	 * @param lastRow
	 * @param total
	 * @return
	 */
	public static Indicator hasNextPage(int lastRow, int total) {
		if(lastRow+1 < total){
			return Indicator.Y;
		}else{
			return Indicator.N;
		}
	}
	
	/**
	 * 计算翻页数据
	 * calculateLmt(这里用一句话描述这个方法的作用)  
	 * (这里描述这个方法适用条件 – 可选)  
	 * @param firstRow
	 * @param lastRow
	 * @return
	 * @throws ProcessException   
	 *int  
	 * @exception   
	 * @since  1.0.0
	 */
	public static int calculateLmt(int firstRow,int lastRow) throws ProcessException{
		if (firstRow > lastRow)
			throw new ProcessException(Constants.ERRS009_CODE, Constants.ERRS009_MES);
		return lastRow - firstRow+1;
	}
	
}
