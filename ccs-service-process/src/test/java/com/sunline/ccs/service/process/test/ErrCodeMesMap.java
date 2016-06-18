package com.sunline.ccs.service.process.test;

import java.lang.reflect.Field;

import com.sunline.ccs.service.api.Constants;

/**
 * 描述：
* @author fanghj
 * @date 2013-5-2 上午10:43:21
 * @version 1.0
 */
public class ErrCodeMesMap {

	/**
	 * main(这里用一句话描述这个方法的作用)  
	 * (这里描述这个方法适用条件 – 可选)  
	 * @param args   
	 *void  
	 * @exception   
	 * @since  1.0.0  
	 */
	public static void main(String[] args) {
		Field[] fields = Constants.class.getFields();
		for(Field field:fields){
			try {
				String filename = field.getName();
				if(filename.startsWith("ERR") && filename.endsWith("CODE")){
					String s = filename.substring(0, 8);
					Field mesfield = Class.forName("com.sunline.ccs.service.api.Constants").getField(s+"MES");
					if(mesfield != null){
						String mes = mesfield.get(Class.forName("com.sunline.ccs.service.api.Constants").newInstance()).toString();	
						String code = field.get(Class.forName("com.sunline.ccs.service.api.Constants").newInstance()).toString();
						System.out.println(code+"="+mes);
						System.out.println("<entry key=\""+code+"\" value-ref=\""+mes+"\" />");
					}
					
				}
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
 
