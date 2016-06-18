package com.sunline.ccs.ui.server.commons;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/**
* @author fanghj
 * @version 创建时间：2012-8-8 下午4:09:03 类说明
 */

public class BlockCodeUtil {
	
	private static String eMPTY ="";

	/**
	 * 增加锁定码
	 * @param blockCode
	 * @param newCode
	 * @return
	 */
	public static String addBlockCode(String blockCode, String newCode) {
		if(CheckUtil.isEmpty(blockCode)){
			return newCode;
		}
		if(hasBlockCode(blockCode, newCode)){
			return blockCode;
		}
		return new StringBuffer(blockCode).append(newCode).toString();
	}
	
	/**
	 * 移除锁定码
	 * @param blockCode
	 * @param removeCodes
	 * @return
	 */
	public static String removeBlockCode(String blockCode, String...removeCodes){
		if(CheckUtil.isEmpty(blockCode)){
			return eMPTY;
		}
		for(String s:removeCodes){
			blockCode = blockCode.replaceAll(s, eMPTY);
		}
		return blockCode;
	}

	/**
	 * 判断对应的锁定码是否存在
	 * 
	 * @param blockCode
	 * @param searchCodes
	 * @return
	 */
	public static boolean hasBlockCode(String blockCode, String... searchCodes) {
		if (CheckUtil.isEmpty(blockCode)) {
			return false;
		}
		for (int i = 0; i < searchCodes.length; i++) {
			if (blockCode.indexOf(searchCodes[i]) != -1) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 合并BlockCode，取2个blockCode的并集
	 * 
	 * @param blockCode1
	 * @param blockCode2
	 * @return
	 */
	public static String unionBlockCodes(String blockCodes1,String blockCodes2){
		if(blockCodes1 == null) blockCodes1 = "";
		if(blockCodes2 == null) blockCodes2 = "";
		
		char[] a = blockCodes1.toCharArray();
		char[] b = blockCodes2.toCharArray();
		Set<Character> set = new LinkedHashSet<Character>();
		for(char _a:a){
			set.add(_a);
		}
		for(char _b:b){
			set.add(_b);
		}
		StringBuilder result = new StringBuilder();
		Iterator<Character> iterator = set.iterator();
		while(iterator.hasNext()){
			result.append(iterator.next());
		}
		
		if("".equals(result)){
			return null;
		}else{
			return result.toString();
		}
	}
	
	/**
	 * 删除拼接锁定码中的重复锁定码
	 * 
	 * @param str
	 * @return
	 */
	public static String removeRepeatedBlockCode(String str){
		TreeSet<String> noRepeated = new TreeSet<String>();
		for (int i = 0; i < str.length(); i++) {
			noRepeated.add(""+str.charAt(i));
		}
		str = "";
		for(String index : noRepeated){
			str += index;
		}
		return str;
	}
	
	/**
	 * 把a中包含b的字符串用“”替换
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static String replace(String a, String b){
		if(a == null) a = "";
		if(b == null) b = "";
		char[] c = b.toCharArray();
		
		for(char d : c){
			a = a.replaceAll(String.valueOf(d), "");
		}
		return a;
	}
	
	/**
	 * a、b字符串是否相互包含
	 * @param a
	 * @param b
	 * @return 包含true，反之false
	 */
	public static boolean isContainEachother(String a, String b ){
		boolean isContain = true;
		if (a.trim().length() == b.trim().length()) {
			for(Character c : b.toCharArray()){
				if(a.contains(c.toString())){
					isContain = true;
				}else {
					isContain = false;
					break;
				}
			}
		}else {
			isContain = false;
		}
		return isContain;
	}

}
