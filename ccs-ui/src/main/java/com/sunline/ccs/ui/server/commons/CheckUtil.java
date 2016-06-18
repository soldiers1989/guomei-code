package com.sunline.ccs.ui.server.commons;

import org.apache.commons.lang.StringUtils;

import com.sunline.ppy.dictionary.enums.IdType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 
* @author fanghj
 *
 */

public class CheckUtil {
	
	
	/**
	 * 判断对象是否为空
	 * @param o
	 * @throws ProcessException
	 */
	public static void rejectNull(Object o)throws ProcessException{
		CheckUtil.rejectNull(o,null);
	}

	/**
	 * 判断对象是否为空，并给出错误信息
	 * @param o
	 * @param errMsg
	 * @throws ProcessException
	 */
	public static void rejectNull(Object o,String errMsg)throws ProcessException{
		if(o == null){
			if(errMsg == null){
				throw new ProcessException(o+"不允许为空");
			}else{
				throw new ProcessException(errMsg);
			}
		}
	}
	
	/**
	 * 判断对用不为空
	 * @param o
	 * @param errMsg
	 * @throws ProcessException
	 */
	public static void rejectNotNull(Object o,String errMsg)throws ProcessException{
		if(o != null){
			if(errMsg == null){
				throw new ProcessException(o+"不为空");
			}else{
				throw new ProcessException(errMsg);
			}
		}
	}
	
	/**
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isEmpty(Object obj){
		return obj == null || obj.equals("");
	}
	
	/**
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isEmpty(String s){
		return s == null || s.trim().length() == 0;
	}
	
	/**
	 * 检查输入的卡号是否合法
	 * @param CardNo
	 * @throws ProcessException 
	 */
	public static void checkCardNo(String cardNo) throws ProcessException{
		if(StringUtils.isEmpty(cardNo)){
			throw new ProcessException("卡号不能为空");
		}
		if(!StringUtils.isNumeric(cardNo)){
			throw new ProcessException("卡号只能为数字类型");
		}
		
	}
	
	/**
	 * 检查输入的客户号是否合法
	 * @param custID
	 * @throws ProcessException 
	 */
	public static void checkCustomer(Integer custID) throws ProcessException{
		if(custID == null){
			throw new ProcessException("客户号不能为空");
		}
	}
	
	/**
	 * 检查证件类型是否合法
	 * @param idType
	 * @param idNo
	 * @throws ProcessException 
	 */
	public static void checkId(IdType idType, String idNo) throws ProcessException{
		if(null == idType){
			throw new ProcessException("证件类型不能为空 ");
		}
		if(StringUtils.isEmpty(idNo)){
			throw new ProcessException("证件号码不能为空 ");
		}
	}
	
	/**
	 * 检查证件类型是否合法
	 * @param idType
	 * @param idNo
	 * @throws ProcessException 
	 */
	public static void checkId(String idType, String idNo) throws ProcessException{
		if(StringUtils.isEmpty(idType)){
			throw new ProcessException("证件类型不能为空 ");
		}
		if(StringUtils.isEmpty(idNo)){
			throw new ProcessException("证件号码不能为空 ");
		}
	}
	
	/**
	 * 检查输入的账户号是否合法
	 * @param accountid
	 * @throws ProcessException
	 */
	public static void checkAccount(Long acctNo) throws ProcessException{
		if(acctNo == null){
			throw new ProcessException("客户号不能为空");
		}
	}
	/**
	 * 检查输入的币种是否为空
	 * @param currCd
	 * @throws ProcessException
	 */
	public static void checkCurrCd(String currCd) throws ProcessException{
		if(StringUtils.isBlank(currCd)){
			throw new ProcessException("币种不能为空");
		}
	}


}
