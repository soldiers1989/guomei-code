package com.sunline.ccs.service.util;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.sunline.ark.support.meta.EnumInfo;
import com.sunline.ark.support.service.QueryRequest;
import com.sunline.ark.support.service.QueryResult;


/** 
 * @see 类名：CPSServProBusUtil
 * @see 描述：TODO 中文描述
 *
 * @see 创建日期：   2015年6月24日 下午2:50:36
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class CPSServProBusUtil {

	/**
	 * 生成翻页接口对象
	 * 
	 * @param queryRequest
	 * @param totalRows
	 * @param result
	 * @return
	 */
	public static QueryResult<Map<String, Serializable>> genQueryResultTOListMap(
			QueryRequest queryRequest, int totalRows,
			List<Map<String, Serializable>> result) {
		QueryResult<Map<String, Serializable>> qrs = new QueryResult<Map<String, Serializable>>();
		qrs.setFirstRow(queryRequest.getFirstRow());
		if (queryRequest.getLastRow() <= totalRows) {
			qrs.setLastRow(queryRequest.getLastRow());
		} else {
			qrs.setLastRow(totalRows);
		}
		qrs.setTotalRows(totalRows);
		qrs.setResult(result);
		return qrs;
	}
	
	public static String getEnumInfo(Enum<?> e) {
		String textValue = "";
		EnumInfo info = e.getClass().getAnnotation(EnumInfo.class);
		for (String strs : info.value()) {
			String[] strtemp = strs.split("\\|");
			if (strtemp[0].equals(e.name())) {
				textValue = strtemp[1];
				break;
			}
		}
		return textValue;
	}
	
//	public static void main(String[] args){
//		System.out.println(CPSServProBusUtil.getEnumInfo(AddressType.N));
//	}



}
