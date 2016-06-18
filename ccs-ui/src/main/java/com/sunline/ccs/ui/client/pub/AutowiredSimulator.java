package com.sunline.ccs.ui.client.pub;

import java.lang.reflect.Field;

import com.sunline.ark.support.meta.PropertyInfo;

/**
 * 
 * @author yeyu
 *
 */
public final class AutowiredSimulator<T> {
	
	/**
	 * 
	 * @param type 被注入的javabean的class对象
	 * @param fieldName 字段名称
	 * @param value 自动注入的数据值
	 * @return 
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
						System.out.println("@PropertyInfo(name=\""
								+ annotation.name() + "\" length=\""
								+ annotation.length() + "\")");
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
