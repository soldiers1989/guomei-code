package com.sunline.ccs.batch.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.persistence.Column;

import com.sunline.ark.support.cstruct.CChar;
import com.sunline.ppy.dictionary.entity.Check;

/**
 *@author houxh
 *@version wanghl.1.4.0.1
 *用来生成测试类的默认数据
 */
public class MakeDataExt {
	
	/**
	 * 设置对象默认值
	 * @param obj
	 * @throws IllegalAccessException
	 */
	public static void setDefaultValue(Object obj)throws IllegalAccessException{
		Random r = new Random();
		Field[] fields = obj.getClass().getDeclaredFields();
		for(Field f : fields){
			if(Modifier.isFinal(f.getModifiers())){
				continue;
			}
			String type = f.getType().getName();
			f.setAccessible(true);
			Annotation[] annotations = f.getAnnotations();
			if(type.indexOf("String")!=-1){
				if(annotations!=null){
					for(Annotation a:annotations){
						if(a instanceof CChar){
							int value = ((CChar)a).value();
							if(value>10){
								value = 10;
							}
							f.set(obj, getRandomCharAndNum(value));
							continue;
						}else if(a instanceof Column){
							int length = ((Column)a).length();
							if(length>10){
								length = 10;
							}
							f.set(obj, getRandomCharAndNum(length));
							continue;
						}else if(a instanceof Check){
							f.set(obj, getRandomCharAndNum((int)((Check)a).lengths()));
							continue;
						}
					}
				}else{
					f.set(obj, getRandomCharAndNum(r.nextInt(5)));
				}
				
			}else if(type.indexOf("BigDecimal")!=-1){
				if(annotations!=null){
					for(Annotation a:annotations){
						if(a instanceof CChar){
							int value = ((CChar)a).value();
							if(value>10){
								value = 10;
							}
							f.set(obj, getRandomDecimal(value, 2));
							continue;
						}else if(a instanceof Column){
							int precision = ((Column)a).precision();
							int scale = ((Column)a).scale();
							if(precision>10){
								precision = 10;
							}
							f.set(obj, getRandomDecimal(precision, scale));
							continue;
						}else if(a instanceof Check){
							f.set(obj, getRandomDecimal((int)((Check)a).lengths(), 2));
							continue;
						}
					}
				}else{
					f.set(obj, new BigDecimal(r.nextInt(3)+"."+r.nextInt(2)));
				}
				
			}else if(type.indexOf("Date") != -1){
				f.set(obj, new Date());
			}else if(f.getType().isEnum()){
				Object[] t = f.getType().getEnumConstants();
				f.set(obj, t[r.nextInt(t.length)]);
			}else if(type.indexOf("Long") != -1){
				f.set(obj, Long.parseLong(getRandomNum(16)));
			}else{
				f.set(obj,r.nextInt(100));
			}
			f.setAccessible(false);
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
	 * 获取随机汉字
	 * 
	 * @param length
	 * @return
	 */
	public static String getRandomCH(int length){
		String val = "";
		Random r = new Random();
		for (int i = 0; i < length; i++) {
			val += (char)(0x4e00 + r.nextInt(0x9fa5 - 0x4e00 + 1));
		}
		return val;
	}
	
	
	/**
	 * 获取随机大小写数字
	 * 
	 * @param length
	 * @return
	 */
	public static String getRandomCharAndNum(int length) {
		String val = "";
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			// 输出字母还是数字
			String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
			// 字符串
			if ("char".equalsIgnoreCase(charOrNum)) {
				// 取得大写字母还是小写字母
				int choice = random.nextInt(2) % 2 == 0 ? 65 : 97;
				val += (char) (choice + random.nextInt(26));
			} else if ("num".equalsIgnoreCase(charOrNum)) { // 数字
				val += String.valueOf(random.nextInt(10));
			}
		}
		return val;
	}
	
	/**
	 * 获取随机小写字母
	 * 
	 * @param length
	 * @return
	 */
	public static String getRandomCharLower(int length) {
		String val = "";
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			val += (char) (random.nextInt(26) +97);
		}
		return val;
	}
	
	/**
	 * 获取指定位数随机数字
	 * 
	 * @param length
	 * @return
	 */
	public static String getRandomNum(int length) {
		String val = "";
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			val += String.valueOf(random.nextInt(10));
		}
		return val;
	}
	
	/**
	 * 获取随机小数
	 * @param length
	 * @param scale
	 * @return
	 */
	public static BigDecimal getRandomDecimal(int length, int scale){
		String num = getRandomNum(length);
		BigDecimal dec = new BigDecimal(num);
		dec = dec.movePointLeft(scale);
		return dec;
	}
	
	public static String getRandomValue(List<String> list){
		 Random rand = new Random();
		 return list.get(rand.nextInt(list.size()));
	}
	
	
	public static void main(String[] args) {
		String str = getRandomValue(Dictionary.idTypeList);
		System.out.println(str);
	}
	
}
