package com.sunline.ccs.facility;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sunline.ark.support.serializable.JsonSerializeUtil;
import com.sunline.ark.support.utils.DateUtils;
import com.sunline.ccs.service.payEntity.CommResp;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

@Service
public class ServJsonUtil {
	
	// 卡号请求字段
	public static final String FIELD_CARD_NO = "card_no";
	// 翻页起
	public static final String FIELD_FIRSTROW = "firstrow";
	// 翻页止
	public static final String FIELD_LASTROW = "lastrow";
	// 操作码
	public static final String FIELD_OPT = "opt";
	//密码解锁交易，错误类型
	public static final String FIELD_ERROR_TYPE = "error_type";
	public static final String FIELD_PAGE_SIZE = "PAGESIZE"; //页大小
	public static final String CHARSET_UTF8 = "UTF-8";

	private Logger log = LoggerFactory.getLogger(ServJsonUtil.class);
	
	/**
	 * 根据请求的对象 setBeanProperty
	 * 
	 * @param servRep
	 * @param obj 
	 *            void
	 * @throws ProcessException
	 * @exception
	 * @since 1.0.0
	 */
	public void setBeanProperty(CommResp resp, Object obj) throws ProcessException {
		if(log.isDebugEnabled()){
			log.debug("开始 - 将MainResp的data对象数据组装到[{}], @{}", obj.getClass().getCanonicalName(), System.currentTimeMillis());
		}
		// 获取request节点
		Object data = resp.getData();
		//obj 对应的实体
		populateObject(data, obj);
		if(log.isDebugEnabled()){
			log.debug("结束 - 将MainResp的data对象数据组装到[{}], @{}", obj.getClass().getCanonicalName(), System.currentTimeMillis());
		}
	}
	
	/**
	 * 支付前置返回报文处理
	 * @param resp 接口中的request
	 * @param obj 实体
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void populateObject(Object resp, Object obj) {
		if(null == resp) {
			return ;
		}
		
		Throwable exception = null;
		Map<String, Object> valueMap = new HashMap<String, Object>();
		if (resp instanceof Map) {
			valueMap = (Map)resp;
		} else {
			valueMap = JsonSerializeUtil.jsonReSerializerNoType(JsonSerializeUtil.jsonSerializerNoType(resp), Map.class);
		}
		
		try {
			Map<String, Field> fieldMap = getFields(obj.getClass());
						
			for (String key : valueMap.keySet()) {
				Object node = valueMap.get(key);
				if (node != null) {
					// 判断是否是枚举类型
					PropertyDescriptor propertyDescriptor = BeanUtilsBean.getInstance().
							getPropertyUtils().getPropertyDescriptor(obj, key);
					if (propertyDescriptor == null) {
						// propertyDescriptor 如果为空，意味这上送的报文接口在ccs的接口中不存在；抛非法的接口
						if(log.isWarnEnabled())
							log.warn("非法的字段，字段名称{} 字段数值{},接口对象类型{}",key, node, obj.getClass().getName()); 
						continue;
//						throw new ProcessException(MsRespCode.E_1043.getCode(), MsRespCode.E_1043 .getMessage()+ ",字段名称{" +key + "}");
					}
					
					Class type = propertyDescriptor.getPropertyType();
					log.debug("匹配报文字段，字段名称{} 字段数值{},数据类型{}", key, node, type.getName());
					if (type.isEnum()) {
						// 使用字符串格式化当前节点，获取节点value
						String nodeValue = node.toString();
						if (!StringUtils.isEmpty(nodeValue)) {
							BeanUtils.setProperty(obj, key, Enum.valueOf(type, nodeValue));
						}
					} else if(type.equals(BigDecimal.class)){
						String nodeValue = node.toString();
						
						DecimalFormat myformat = new DecimalFormat();
						myformat.applyPattern("#####.00");
						nodeValue = myformat.format(nodeValue);
						
						BeanUtils.setProperty(obj, key, nodeValue);				
						
					}else if (type.equals(Date.class)) {
						// 使用字符串格式化当前节点，获取节点value
						String nodeValue = node.toString();
						String[] pattern = new String[] {"yyyyMMdd", "yyyyMM", "yyMM", "yyyyMMddHHmmss"};
						if (!StringUtils.isEmpty(nodeValue)) {
							if (nodeValue.length() != 8 && nodeValue.length() != 6 
									&& nodeValue.length() != 4 && nodeValue.length() != 14) {
								if(log.isWarnEnabled())
									log.warn("非法的字段，字段名称{} 字段数值{},接口对象类型{}",key, node, obj.getClass().getName()); 
								throw new ProcessException(MsRespCode.E_1043.getCode(), MsRespCode.E_1043.getMessage()+ ",字段名称{" +key + "}");
							}
							Date value = DateUtils.parseDateStrictly(nodeValue, pattern);
							BeanUtils.setProperty(obj, key, value);
						}
					} else if (type.equals(Integer.class) || type.equals(Long.class) || type.equals(Short.class)) {
						// 使用字符串格式化当前节点，获取节点value
						String nodeValue = node.toString();
						if (StringUtils.isNotBlank(nodeValue)) {
							if (this.isNumeric(nodeValue)) {
								BeanUtils.setProperty(obj, key, nodeValue);
							} else {
								if(log.isWarnEnabled())
									log.warn("非法的字段，字段名称{} 字段数值{},接口对象类型{}",key, node, obj.getClass().getName()); 
								throw new ProcessException(MsRespCode.E_1043.getCode(), MsRespCode.E_1043 .getMessage()+ ",字段名称{" +key + "}");
							}
						}
					} else if (type.isAssignableFrom(List.class) || type.isAssignableFrom(ArrayList.class)) {
						List listNodes = null;
						Map map = null;
						
						if (node instanceof Map) {
							map = (Map) node;
							if (map.size() < 1) {
								continue;
							}
							listNodes = (List) map.values().iterator().next();
						} else {
							listNodes = (List) node;
						}
						
						Field field = fieldMap.get(key); //List field
						if(null == field || !(field.getGenericType() instanceof ParameterizedType)) {
							if(log.isWarnEnabled())
								log.warn("非法的字段，字段名称{} 字段数值{},接口对象类型{}",key, node, obj.getClass().getName()); 
							continue;
						}
						
						ParameterizedType pt = (ParameterizedType)field.getGenericType();
						if(pt.getActualTypeArguments() == null || pt.getActualTypeArguments().length < 1) {
							if(log.isWarnEnabled())
								log.warn("非法的字段，字段名称{} 字段数值{},接口对象类型{}",key, node, obj.getClass().getName()); 
							continue;
						}
						
						Class<?> itemClass = Class.forName(pt.getActualTypeArguments()[0].toString().substring(6));
						
						ArrayList<Object> list = new ArrayList<Object>();
						
						for(int k = 0; k < listNodes.size(); k++) {
							Object itemObj = itemClass.newInstance();
							populateObject((Serializable)listNodes.get(k), itemObj);
							list.add(itemObj);
						}
						
						BeanUtils.setProperty(obj, key, list);
					} else if(type.equals(String.class)) {
						// 使用字符串格式化当前节点，获取节点value
						String nodeValue = node.toString();
						if (StringUtils.isNotEmpty(nodeValue)) {
							BeanUtils.setProperty(obj, key, nodeValue);
						}
					} else {
						// 使用字符串格式化当前节点，获取节点value
						String nodeValue = node.toString();
						if (StringUtils.isNotEmpty(nodeValue)) {
							BeanUtils.setProperty(obj, key, nodeValue);
						}
					}
				}
			}
		} catch (ProcessException pe) {
			log.error(pe.getMessage(),pe);
			throw pe;
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			throw new ProcessException(MsRespCode.E_9998.getCode(), MsRespCode.E_9998.getMessage());
		} finally {
			if(null != exception) {
				log.warn("报文格式错误>> [" + exception.getClass().getSimpleName()+"][" 
						+ exception.getMessage()+"]", exception);
			}
		}
		
	}
	
	/**
	 * 
	 * @param str
	 * @return
	 */
	private boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		return pattern.matcher(str).matches();
	}
	
	/**
	 * 获取域
	 * @param cls
	 * @return
	 */
	private  Map<String, Field> getFields(Class<?> cls) {
		Map<String, Field> fieldMap = new HashMap<String, Field>();
		
		Field[] fields = cls.getDeclaredFields();
		for(Field f : fields) {
			fieldMap.put(f.getName(), f);
		}
		
		return fieldMap;
	}

}
