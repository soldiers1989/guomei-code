package com.sunline.ccs.batch.cca200;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.sunline.ccs.batch.cca400.item.SettleReqRptItem;

public class EVRGenTestTool {
	private static String targetName;
	private static String sourceName;
	
	public static void main(String[] args) {
		methodString(SettleReqRptItem.class, "item", null);
	}

	/**
	 * @param clazz 属性来源
	 * @param target 目标对象名
	 * @param source 来源对象名
	 */
	public static void methodString(Class<?> clazz,String target,String source){
		targetName = target;
		sourceName = source;
		
		process(clazz, clazz.getDeclaredFields());
		
		Class<?> supClazz = clazz.getSuperclass();
		process(supClazz, supClazz.getDeclaredFields());
	}

	/**
	 * @param clazz
	 * @param fields
	 */
	private static void process(Class<?> clazz, Field[] fields) {
		List<String> listStr = new ArrayList<String>();
		System.out.println("属性数量："+fields.length);
		for(int i=0;i<fields.length;i++){
			Field f = fields[i];
			f.setAccessible(true);
			String fieldName = f.getName();
			String setMethodStr = null;
			String getMethodStr = null;
			
			try {
				if(Modifier.isPrivate(f.getModifiers())){
					
					PropertyDescriptor srcPd = new PropertyDescriptor(fieldName, clazz);
					setMethodStr = srcPd.getWriteMethod().getName();
					getMethodStr = srcPd.getReadMethod().getName();
					
					if(StringUtils.isBlank(sourceName)){
						System.out.println(targetName+"."+setMethodStr+"(\"\");");
					}else{
//						System.out.println(fieldName);
						System.out.println(targetName+"."+setMethodStr+"("+sourceName+"."+getMethodStr+"());");
//						System.out.println(targetName+"."+setMethodStr+"("+sourceName+".getString(\""+fieldName+"\"));");
					}
				}else if(Modifier.isPublic(f.getModifiers())){
					
//					System.out.println(targetName + "." + fieldName + " = " + sourceName + "." + fieldName +  ";");
					System.out.println(targetName + "." + fieldName + " = ;");
					
				}
			} catch (Exception e) {
				listStr.add("\n"+fieldName+" : Failed to access getter method");
				continue;
			}
		}
	}

}
