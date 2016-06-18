package com.sunline.ccs.batch.tools;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;

import javax.persistence.Column;

import com.sunline.ark.support.cstruct.CChar;
import com.sunline.ppy.dictionary.entity.Check;

/**
* @author fanghj
 *用来生成测试类的默认数据
 */
public class MakeData {
	private static StringBuffer sb = new StringBuffer();
	static{
		for(int i = 'a';i<='z';i++){
			sb.append((char)i);
		}
		for(int i ='A';i<='Z';i++){
			sb.append((char)i);
		}
		for(int i = 0;i<=9;i++){
			sb.append(i);
		}
	}
	/**
	 * 设置对象默认值
	 * @param obj
	 * @throws IllegalAccessException
	 */
	public static void setDefaultValue(Object obj)throws IllegalAccessException{
		Random r = new Random();
		Field[] fields = obj.getClass().getDeclaredFields();
		for(Field f : fields){
			System.out.print("属性："+f.getName());
			if(Modifier.isFinal(f.getModifiers())){
				continue;
			}
			String type = f.getType().getName();
			f.setAccessible(true);
			if(type.indexOf("String")!=-1){
				Annotation[] annotations = f.getAnnotations();
				if(annotations!=null){
					for(Annotation a:annotations){
						if(a instanceof CChar){
							f.set(obj, getRandomENString(((CChar)a).value()));
							continue;
						}else if(a instanceof Column){
							f.set(obj, getRandomENString(((Column)a).length()));
							continue;
						}else if(a instanceof Check){
							f.set(obj, getRandomENString((int)((Check)a).lengths()));
							continue;
						}
					}
				}else{
					f.set(obj, getRandomENString(r.nextInt(5)));
				}
				
			}else if(type.indexOf("Date") != -1){
				f.set(obj, new Date());
			}else if(type.indexOf("BigDecimal")!=-1){
				f.set(obj, new BigDecimal(r.nextInt(100)));
			}else if(f.getType().isEnum()){
				Object[] t = f.getType().getEnumConstants();
				f.set(obj, t[r.nextInt(t.length)]);
			}else if(type.indexOf("Long") != -1){
				f.set(obj, r.nextLong());
			}else{
				f.set(obj,r.nextInt(100));
			}
			f.setAccessible(false);
			System.out.println("属性值："+f.get(obj));
			
		}
	}
	/**
	 * 生成给定长度的随机中文字符串
	 * @param length
	 * @return
	 */
	public static String getRandomCHString(int length){
		int start = '\u4E00';
		int end = '\u9FA5';
		StringBuffer result = new StringBuffer();
		Random r = new Random();
		for(int i = 0;i<length;i++){
			result.append((char)(start+r.nextInt(end-start)));
		}
		return result.toString();
	}
	/**
	 * 生成给定长度的随机英文数字字符串
	 * @param length
	 * @return
	 */
	public static String getRandomENString(int length){
		Random r = new Random();
		StringBuffer result = new StringBuffer();
		for(int i = 0;i<length;i++){
			result.append(sb.charAt(r.nextInt(sb.length())));
		}
		return result.toString();
	}
}
