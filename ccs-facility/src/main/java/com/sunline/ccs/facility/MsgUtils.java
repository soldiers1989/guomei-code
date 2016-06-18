/**
 * 
 */
package com.sunline.ccs.facility;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.apache.commons.codec.binary.Hex;

/**
 * 报文处理工具类
* @author fanghj
 *
 */
public class MsgUtils {

	private static final String CHARSET_NAME = "GBK";
	
	/**
	 * 获取字符串的前N个字节，长度不够则右补指定字符
	 * @param value  字符串值
	 * @param length 获取字节长度
	 * @return
	 */
	public static byte[] rightPad(String value, int length, char filledChar) {
		if(length < 1) {
			return null;
		}
		
		byte[] retBytes = new byte[length];
		
		if(null == value) {
			value = "";
		}
		
		byte[] oriBytes = value.getBytes();
		int minLen = length <= oriBytes.length ? length : oriBytes.length;
		int index = 0;
		for( ; index < minLen; index++) {
			retBytes[index] = oriBytes[index];
		}
		
		if(length > oriBytes.length) {
			for( ; index < length; index++) {
				retBytes[index] = (byte)filledChar;
			}
		}
		
		return retBytes;
	}
	
	/**
	 * 给指定字符串向右补空格，如果指定字符串大于length则不进行操作
	 * @param value  字符串值
	 * @param length 获取字节长度
	 * @return
	 */
	public static String rightPadToString(String value, int length) {
		if(length < 1) {
			return null;
		}
		
		if(null == value) {
			value = "";
		}
		
		if (value.length() >= length) {
			return value;
		}
		
		StringBuffer returnString = new StringBuffer(value);
		for (int i = value.getBytes().length; i < length; i++) {
			returnString.append(' ');
		}
		return returnString.toString();
	}
	
	/**
	 * 获取字符串的前N个字节，长度不够则右补空格
	 * @param value
	 * @param length
	 * @return
	 */
	public static byte[] rightPad(String value, int length) {
		return rightPad(value, length, ' ');
	}
	
	/**
	 * 获取字符串的前N个字节，长度不够则左补指定字符
	 * @param value  字符串值
	 * @param length 获取字节长度
	 * @return
	 */
	public static byte[] leftPad(String value, int length, char filledChar) {
		if(length < 1) {
			return null;
		}
		
		byte[] retBytes = new byte[length];
		
		if(null == value) {
			value = "";
		}
		
		byte[] oriBytes = value.getBytes();
		int index = 0;
		
		int filledLength = length - oriBytes.length;
		if(filledLength > 0) {
			for( ; index < filledLength; index++) {
				retBytes[index] = (byte)filledChar;
			}
		} else {
			filledLength = 0;
		}
		
		for( ; index < length; index++) {
			retBytes[index] = oriBytes[index - filledLength];
		}
		
		return retBytes;
	}
	
	/**
	 * 获取字符串的前N个字节，长度不够则左补零
	 * @param value  字符串值
	 * @param length 获取字节长度
	 * @return
	 */
	public static byte[] leftPad(String value, int length) {
		return leftPad(value, length, '0');
	}

	/**
	 * 按字节截取字符串
	 * 
	 * @param orignal 原始字符串
	 * @param byteCount 截取位数
	 * @return 截取后的字符串
	 * @throws UnsupportedEncodingException 使用了JAVA不支持的编码格式
	 */
	public static String substring(String orignal, int byteCount) {
		// 原始字符不为null，也不是空字符串
		if (orignal != null && orignal.length() > 0) {
			// 将原始字符串转换为UTF-8编码格式
			try {
				// 要截取的字节数大于0，且小于原始字符串的字节数
				if (byteCount > 0 && byteCount < orignal.getBytes(CHARSET_NAME).length) {
					StringBuffer buff = new StringBuffer();
					char c;
					int byteNum = 0;
					for (int i = 0; i < orignal.length(); i++) {
						c = orignal.charAt(i);

						byteNum += getCharLength(c);
						if (byteNum > byteCount) {
							break;
						} else {
							buff.append(c);
						}

					}
					return buff.toString();
				}
			} catch (UnsupportedEncodingException e) {
				return null;
			}
		}

		return orignal;
	}

	/**
	 * 获取一个字符（包括汉字、英文字母、数字、其他符号等）的指定字符集的字节长度。
	 * 
	 * @param c 字符
	 * @return
	 * @throws UnsupportedEncodingException 使用了JAVA不支持的编码格式
	 */
	public static int getCharLength(char c) throws UnsupportedEncodingException {
		// 如果字节数大于1，是汉字
		return String.valueOf(c).getBytes(CHARSET_NAME).length;
	}

	public static void main(String[] args) {
		System.out.println(new String(leftPad("1030155030", 6)));
		System.out.println(new String(leftPad("12345", 6)));
		System.out.println(new String(rightPad("1030155030", 6)));
		System.out.println(new String(rightPad("12345", 6)));
		
		
		String l = "lihr";
		String b = Hex.encodeHexString(Hex.encodeHexString(Arrays.copyOf(l.getBytes(), 4)).toUpperCase().getBytes());
		System.out.println(b);
		
		System.out.println(Arrays.copyOf(l.getBytes(), 4));
		System.out.println(String.format("%04d", l.length()).getBytes());
		
		
	}
	
}
