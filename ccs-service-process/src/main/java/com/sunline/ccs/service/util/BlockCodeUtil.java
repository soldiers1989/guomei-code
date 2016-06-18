package com.sunline.ccs.service.util;

import org.apache.commons.lang.StringUtils;

import com.sunline.ppy.api.CcServProConstants;
import com.sunline.ppy.dictionary.exception.ProcessException;


/** 
 * @see 类名：BlockCodeUtil
 * @see 描述：锁定码工具
 *
 * @see 创建日期：   2015年6月24日 下午2:49:47
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class BlockCodeUtil {

	private static String eMPTY = "";
	
	/**
	 * 增加锁定码
	 * 
	 * @param blockCode
	 * @param newCode
	 * @return
	 */
	public static String addBlockCode(String blockCode, String newCode) {
		if (CheckUtil.isEmpty(blockCode)) {
			return newCode;
		}
		
		if (hasBlockCode(blockCode, newCode)) {
			return blockCode;
		}
		return new StringBuffer(blockCode).append(newCode).toString();
	}

	/**
	 * 移除锁定码
	 * 
	 * @param blockCode
	 * @param removeCodes
	 * @return
	 */
	public static String removeBlockCode(String blockCode,
			String... removeCodes) {
		if (CheckUtil.isEmpty(blockCode)) {
			return eMPTY;
		}
		for (String s : removeCodes) {
			blockCode = blockCode.replaceAll(s, eMPTY);
		}
		return blockCode;
	}

	/**
	 * 判断对应的锁定码存在
	 * 
	 * @param blockCode
	 * @param searchCodes
	 * @return
	 */
	public static boolean hasBlockCode(String blockCode, String searchCodes) {
		return StringUtils.containsAny(blockCode, searchCodes);
	}
	
	/**
	 * 判断对应的锁定码不存在
	 * @param blockCode
	 * @param searchCodes
	 * @return
	 */
	public static boolean hasNoBlockCode(String blockCode, String searchCodes){
		return !StringUtils.containsAny(blockCode, searchCodes);
	}
	
	/**
	 * 判断是否可能增加锁定码 
	 * @param blockCode
	 * @throws ProcessException
	 */
	public static void isCanAddBlockCode_C(String blockCode) throws ProcessException{
		if (BlockCodeUtil.hasBlockCode(blockCode,
				CcServProConstants.BLOCKCODE_C)) {
			throw new ProcessException("已为销卡状态，无需再设");
		}		
	}
	
	
	/**
	 * 判断是否可能增加到期不换卡锁定码
	 * @param blockCode
	 * @throws ProcessException
	 */
	public static void isCanAddBlockCode_Q(String blockCode) throws ProcessException{
		if (BlockCodeUtil.hasBlockCode(blockCode,
				CcServProConstants.BLOCKCODE_Q)) {
			throw new ProcessException("已为到期不换卡，无需再设");
		}		
	}
	
	/**
	 * 是否能撤销到期不换卡锁定码
	 * @param blockCode
	 * @throws ProcessException
	 */
	public static void isCanCancel_Q(String blockCode) throws ProcessException{
		if (BlockCodeUtil.hasNoBlockCode(blockCode,CcServProConstants.BLOCKCODE_Q)) {
			throw new ProcessException("卡片非到期不换卡状态，无法进行撤销");
		}
	}
	
	/**
	 * 是否能销卡撤销
	 * @param blockCode
	 * @throws ProcessException
	 */
	public static void isCanCancel(String blockCode) throws ProcessException{
		if (BlockCodeUtil.hasNoBlockCode(blockCode,CcServProConstants.BLOCKCODE_C)) {
			throw new ProcessException("卡片非销卡状态，无法进行销卡撤销");
		}
	}
	
	/**
	 * 
	 * isCanCancel 是否能分期 
	 * @param blockCode
	 * @throws ProcessException   
	 * @exception   
	 * @since  1.0.0
	 */
	public static void isCanLoan(String blockCode) throws ProcessException{
		if (BlockCodeUtil.hasNoBlockCode(blockCode,CcServProConstants.BLOCKCODE_N)) {
			throw new ProcessException("存在锁定码N,卡片无法分期");
		}
	}

}
