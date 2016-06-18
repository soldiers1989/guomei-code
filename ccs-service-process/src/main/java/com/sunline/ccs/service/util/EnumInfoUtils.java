package com.sunline.ccs.service.util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunline.ark.support.meta.EnumInfo;

public class EnumInfoUtils {
	private static Logger logger = LoggerFactory.getLogger(EnumInfoUtils.class);

	public static Map<String, String> getEnumInfoFromClass(Class<?> javaType){
		
		HashMap<String, String> valueMap = new HashMap<String, String>();
		
		EnumInfo enumInfo = javaType.getAnnotation(EnumInfo.class);
		if (enumInfo == null)
		{
			logger.info("No EnumInfo Annotation");
		}
		else
		{
			for (String value : enumInfo.value())
			{
				String kv[] = value.split("\\|");
				if (kv.length != 2){
					logger.warn("Unknown definition[{}]", javaType.getCanonicalName() + value);
					continue;
				}
				String key = kv[0];
//				key = StringUtils.replace(key, ".", "_");
				valueMap.put(key, kv[1]);
			}
		}
		
		return valueMap;
	}
}
