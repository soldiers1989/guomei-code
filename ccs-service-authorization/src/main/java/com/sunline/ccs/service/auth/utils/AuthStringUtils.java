package com.sunline.ccs.service.auth.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @see 类名：AuthStringUtils
 * @see 描述：授权字符串处理工具类
 *
 * @see 创建日期：   2015年6月24日下午3:14:59
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class AuthStringUtils {
	/**
	 * 按字节截取字符串
	 * 
	 * @param orignal
	 *            原始字符串
	 * @param subcount
	 *            截取位数
	 */
	public static String subStringByByte(String orignal, int subcount) {
		if(orignal.getBytes().length < subcount){
			throw new IllegalArgumentException("数据长度错误");
		}
		int cnt = 0;
		StringBuilder sb = new StringBuilder();
		char[] tempChar = orignal.toCharArray();
		for (int i = 0; (i < tempChar.length && subcount > cnt); i++) {
			byte[] b = String.valueOf(tempChar[i]).getBytes();
			if (subcount - cnt < b.length) {
				throw new IllegalArgumentException("报文长度错误");
			} else {
				cnt += b.length;
				sb.append(tempChar[i]);
			}
		}
		return sb.toString();
	}
	
	/**
	 * 根据长度截取数据
	 * @param str
	 * @param ints 
	 * @return
	 */
	public static List<String> splitByArray(String str, Integer[] array) {
		List<String> l = new ArrayList<String>();
		for (Integer i : array) {
			String s = subStringByByte(str, i);
			if(s.getBytes().length != i ){
				throw new IllegalArgumentException("数据长度错误");
			}
			str = str.substring(s.length());
			l.add(s);
		}
		return l;
	}
	
	/**
	 * 在给定内容右边用给定填充字节根据指定长度填充字节数组
	 * 
	 * @param bytes
	 *            数组内容
	 * @param length
	 *            数组总长度
	 * @param paddingByte
	 *            填充字节
	 * 
	 * @return 按给定参数填充后的字节数组
	 */
	public static byte[] rightPad(final byte[] bytes, final int length, final byte paddingByte) {
		if (length < bytes.length) {
			throw new IllegalArgumentException("length必须大于字节数组本身的长度");
		}
		byte[] result = new byte[length];
		// 获取填充字节
		byte[] paddingBytes = new byte[length - bytes.length];
		Arrays.fill(paddingBytes, paddingByte);
		// 组装内容
		System.arraycopy(bytes, 0, result, 0, bytes.length);
		// 组装填充
		System.arraycopy(paddingBytes, 0, result, bytes.length, paddingBytes.length);
		return result;
	}
	/**
	 * 在给定内容左边用给定填充字节根据指定长度填充字节数组
	 * 
	 * @param bytes
	 *            数组内容
	 * @param length
	 *            数组总长度
	 * @param paddingByte
	 *            填充字节
	 * 
	 * @return 按给定参数填充后的字节数组
	 */
	public static byte[] leftPad(byte[] bytes, int length, byte paddingByte) {
		if (length < bytes.length) {
			throw new IllegalArgumentException("length必须大于字节数组本身的长度");
		}
		byte[] result = new byte[length];
		// 获取填充字节
		byte[] paddingBytes = new byte[length - bytes.length];
		Arrays.fill(paddingBytes, paddingByte);
		// 组装填充
		System.arraycopy(paddingBytes, 0, result, 0, paddingBytes.length);
		// 组装内容
		System.arraycopy(bytes, 0, result, paddingBytes.length, bytes.length);
		return result;
	}
	
}
