package com.sunline.ccs.facility.tools;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
/**
 * 
 * 对象比较公共方法
 * <p>基于JavaBean规范</p>
 * @author Lisy
 * 
 * 
 */

public class ObjectCompareCommon {
	private static SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@SuppressWarnings({ "rawtypes", "unused" })
	public static void compare(Object o1, Object o2) throws Exception {
		try {
			Class class0 = o1.getClass();
			if (class0 != o2.getClass()) {
				print("Different Type[" + class0.getName() + "],[" + o2.getClass().getName() + "]");
				return;
			}
			print("类型：" + class0.getName());

			Method m = null;
			for (int i = 0; i < class0.getMethods().length; i++) {
				try {
					m = class0.getMethods()[i];
				} catch (Exception e) {
					// 不存在对应方法，不处理
				} finally {
					if (m == null) {
						continue;
					}
					if (m.getParameterTypes().length > 0) {
						// 需要传参的方法，不处理
						continue;
					}
					// 依赖于JavaBean规范，同时屏蔽其他方法
					if (!m.getName().contains("get")) {
						continue;
					}
					Class fieldClass = m.getReturnType();
					
					Object v1 = m.invoke(o1, new Object[0]);
					Object v2 = m.invoke(o2, new Object[0]);

					if (fieldClass == Boolean.class) {
						// 不处理
					} else if (String.class == fieldClass || fieldClass.isEnum()) {
						// 不处理
					} else if (Date.class == fieldClass) {
						v1 = v1 == null ? null : sf.format((Date) v1);
						v2 = v2 == null ? null : sf.format((Date) v2);
					} else if (BigDecimal.class == fieldClass || Long.class == fieldClass
							|| Integer.class == fieldClass) {
						v1 = v1 == null ? null : new BigDecimal(v1.toString()).toString();
						v2 = v2 == null ? null : new BigDecimal(v2.toString()).toString();
					} else if (List.class == fieldClass) {
						//
						Class c1 = v1.getClass();
						if (c1 != v2.getClass()) {
							// 可能存在重载方法
							print("Different Type in List!");
							continue;
						}
						print("比较列表：" + m.getName());
						List l1 = (List) v1;
						List l2 = (List) v2;
						// 以第一个列表的长度为基准
						// 如果是乱序的就没法比较啦...
						for (int j = 0; j < l1.size(); j++) {
							Object lo1 = null;
							Object lo2 = null;
							// 可能不等长
							lo1 = l1.get(j);
							if (j < l2.size()) {
								lo2 = l2.get(j);
							} else {
								print("不等长列表，停止对比");
								continue;
							}
							ObjectCompareCommon.compare(lo1, lo2);
						}
						continue;
					}else if (Map.class == fieldClass) {
						// TODO
						Map m1=(Map)v1;
						Map m2=(Map)v2;
						continue;
					} else {
						// 非关键域不处理
						continue;
					}

					String s1 = v1 == null ? "null" : v1.toString();
					String s2 = v2 == null ? "null" : v2.toString();
					
					if (!s1.equals(s2)) {
						// 变量名
						String f = m.getName().replace("get", "");
						System.out.print(f+"：");
						print("[" + s1 + "]不同于[" + s2 + "]");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void print(Object... obj) {
		for (int i = 0; i < obj.length; i++) {
			System.out.println(obj[i].toString());
		}
	}

	public static void main(String args[]) {
		
	}
}
