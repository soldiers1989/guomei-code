package com.sunline.ccs.ui.client.commons;

import java.lang.reflect.Field;

import com.sunline.ark.support.meta.PropertyInfo;

/**
 * 
 * @说明 IOC实现类,用于重构期间的调试工作
 * 
 * @version 1.0 
 *
 * @Date Jun 17, 2015
 *
 * @作者 yeyu
 *
 * @修改记录 
 * [编号：20150617_01]，[修改人：yeyu ]，[修改说明：创建源文件]
 */
public final class CommonAutowired<T> {
	
	/**
	 * 
	 *
	 * @param type 被注入的javabean的class对象
	 * 
	 * @param fieldName 字段名称
	 * 
	 * @param value 自动注入的数据值
	 * 
	 * @return 注入目标对象实例
	 *
	 * @说明 实现控制反转
	 *
	 * @author yeyu
	 *
	 * @修改记录 
	 * [编号：20150617_01]，[修改人：yeyu ]，[修改说明：创建函数]
	 */
	public T simulator(Class<T> type, String fieldName, String value) {
		try {
			T instance = type.newInstance();
			Field[] fileds = type.getDeclaredFields();
			for (Field field : fileds) {
				String name = field.getName();
				if (name.equals(fieldName)) {
					PropertyInfo annotation = field
							.getAnnotation(PropertyInfo.class);
					if (annotation != null) {
						field.set(instance, value);
						return instance;
					} else {
						return null;
					}
				}
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}
	
}
