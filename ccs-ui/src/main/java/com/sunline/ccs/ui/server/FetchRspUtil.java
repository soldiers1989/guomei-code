/**
 * 
 */
package com.sunline.ccs.ui.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;

/**
 * FetchResponse工具类
* @author fanghj
 *
 */
public class FetchRspUtil {
	/**
	 * 将List转换为FetchResponse
	 * @param millisecondsBeginTime
	 * 			查询的其实时间，单位为毫秒
	 * @param result
	 * 			查询的结果
	 * @return
	 */
	public static FetchResponse toFetchResponse(FetchRequest fetchRequest, 
			long millisecondsBeginTime, List<Map<String, Serializable>> result) {
		
		final int totalRows = result.size();
		
		//组装FetchResponse并返回
		FetchResponse response = new FetchResponse();
		response.setTotal(totalRows);
		
		//组装FetchResponse的fields
//		String[] fields = fetchRequest.getFields();
//		response.setFields(fields);
		
		ArrayList<Serializable> data = new ArrayList<Serializable>();
		
		if(totalRows > 0) {
			//组装FetchResponse的fields
			Map<String, Serializable> planMap = result.get(0);
			Set<String> keySet = planMap.keySet();
			String fields[] = new String[keySet.size()];
			keySet.toArray(fields);
			
			//组装FetchResponse的data
			for(Map<String, Serializable> map : result) {
				HashMap<String,Serializable> row = new HashMap<String,Serializable>();
				for(int j = 0; j < fields.length; j++) {
					if (map.get(fields[j]) != null) {
						row.put(fields[j], (Serializable)map.get(fields[j]));
					} else {
						row.put(fields[j], "");
					}
				}
				data.add(row);
			}
		}
		response.setRows(data);
		response.setMilliseconds(millisecondsBeginTime);
		
		return response;
	}
	
}
