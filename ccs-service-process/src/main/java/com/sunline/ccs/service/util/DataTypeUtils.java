package com.sunline.ccs.service.util;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import com.sunline.ppy.dictionary.exception.ProcessException;


/** 
 * @see 类名：DataTypeUtils
 * @see 描述：数据转换
 *
 * @see 创建日期：   2015年6月24日 下午2:51:54
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public abstract class DataTypeUtils {
	public static Integer getIntegerValue(Object value)
	{
		if (value == null)
			return null;
		if (value instanceof Integer)
			return (Integer)value;
		if (value instanceof Long)
			return ((Long)value).intValue();
		if (value instanceof BigDecimal)
			return ((BigDecimal)value).intValue();
		return Integer.valueOf(value.toString());
	}
	
	public static Long getLongValue(Object value)
	{
		if (value == null)
			return null;
		if (value instanceof Integer)
			return ((Integer)value).longValue();
		if (value instanceof Long)
			return (Long)value;
		if (value instanceof BigDecimal)
			return ((BigDecimal)value).longValue();
		return Long.valueOf(value.toString());
	}

	public static BigDecimal getBigDecimalValue(Object value)
	{
		if (value == null)
			return null;
		if (value instanceof Integer)
			return BigDecimal.valueOf((Integer)value);
		if (value instanceof Long)
			return BigDecimal.valueOf((Long)value);
		if (value instanceof BigDecimal)
			return (BigDecimal)value;
		return new BigDecimal(value.toString());
	}
	
	public static String getStringValue(Object value)
	{
		if (value == null)
			return null;
		return value.toString(); 
	}
	public static Date getDateValue(Object value)
	{
		if (value == null)
			return null;
		return (Date)value; 
	}
	public static Boolean getBooleanValue(Object value)
	{
		return (Boolean)value;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> T getEnumValue(Object value, Class<T> type)
	{
		if (value == null)
			return null;
		if (value instanceof Enum<?>)
			return (T)value;		//这里出于效率问题，不判具体的枚举类型了
		return Enum.valueOf(type, value.toString());
			
	}

	/**
	 *  用于系统中cps-service-process中空指针数据处理
	 * @param destClazz
	 * @param orig
	 * @return
	 */
	public static BigDecimal nullConvert(BigDecimal orig,int scale,int roundType){
		if(orig == null)
			return BigDecimal.ZERO;
		else
			return orig.setScale(scale,roundType);

	}
	
	/**
	 *  用于系统中cps-service-process中空指针数据处理
	 *  @param orig
	 *  @return
	 */
	public static BigDecimal nullConvert(BigDecimal orig){
		return nullConvert(orig,0,BigDecimal.ROUND_UNNECESSARY);
	}
	/**
	 * 用于字段转换,比如字段card_no默认与cardNbr、cardno是同一个参数
	 * FIXME 
	 * @param dest
	 * @param map
	 * @throws ProcessException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static  void MapConvertResp(Object dest ,Map<String,Serializable> map) {
        if(dest == null || map == null){
        	throw new IllegalArgumentException("Invalid Argument");
        } 
		Field[] fields = dest.getClass().getDeclaredFields();
         for(Field field : fields){
        	if(!field.isAccessible())
        		 field.setAccessible(true);
        	 //过滤序列化版本号这个字段
        	int modifier = field.getModifiers();
        	//排除 public static final字段
        	if(Modifier.isPublic(modifier) && Modifier.isStatic(modifier) && Modifier.isFinal(modifier))
        	 {
        		//TODO
        		 
        	 }else{
        		 for(String key : map.keySet()){
        			 if(field.getName().replace("_", "").toLowerCase().equals(key.toLowerCase())){
        				 Class typeClass = field.getType();
						try {
							if(typeClass.isEnum()) {
                               field.set(dest, DataTypeUtils.getEnumValue(map.get(key), typeClass));
							}else if(BigDecimal.class.equals(typeClass)){
								field.set(dest, DataTypeUtils.getBigDecimalValue(map.get(key)));
						    }else if(Date.class.equals(typeClass)){
							    field.set(dest, DataTypeUtils.getDateValue(map.get(key)));
							}else if(String.class.equals(typeClass)){
								 field.set(dest, DataTypeUtils.getStringValue(map.get(key)));
							}else if(Boolean.class.equals(typeClass)){
								 field.set(dest, DataTypeUtils.getBooleanValue(map.get(key)));
							}else if(Long.class.equals(typeClass)){
								 field.set(dest, DataTypeUtils.getLongValue(map.get(key)));
							}else if(Integer.class.equals(typeClass)){
								 field.set(dest, DataTypeUtils.getIntegerValue(map.get(key)));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
        			 }
        			 
        		 }
        	 }
        	 
         }
			
	}

}
