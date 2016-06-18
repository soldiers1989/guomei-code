///**
// * 
// */
//package com.sunline.ccs.ui.client.validator;
//
//import com.smartgwt.client.widgets.form.validator.CustomValidator;
//
///**
// * 银行卡号校验，允许不输入任何职。
// * 默认忽略输入的卡号中的空白字符，有效字符须为16~19位数字。
// * 只有调用setUseLuhnValidate(true)才会进行Luhn算法校验。
//* @author fanghj
// *
// */
//public class CardNoValidator extends CustomValidator {
//
//	public static final int MIN_LENGTH = 13;
//	public static final int MAX_LENGTH = 19;
//	
//	private boolean useLuhnValidate = false; //是否使用Luhn算法校验卡号
//	private boolean ignoreSpaces = true; //是否忽略输入的卡号中的空白字符
//	
//	private CustomValidatorMessages messages = CustomValidatorMessages.INSTANCE;
//	
//	@Override
//	protected boolean condition(Object value) {
//		if(null == value) {
//			return true;
//		}
//		
//		String valueStr = value.toString();
//		if(valueStr.length() == 0) {
//			return true;
//		}
//		
//		if(ignoreSpaces) {
//			valueStr = removeSpaces(valueStr);
//		}
//		
//		//长度校验
//		if(valueStr.length() < MIN_LENGTH || valueStr.length() > MAX_LENGTH) {
//			setErrorMessage(messages.cardNoInvalid());
//			return false;
//		}
//		
//		//luhn校验
//		if(useLuhnValidate) {
//			if(luhnValidate(valueStr)) {
//				return true;
//			}
//		} else {
//			if(isNumeric(valueStr)) {
//				return true;
//			}
//		}
//		
//		setErrorMessage(messages.cardNoInvalid());
//		return false;
//	}
//	
//	/**
//	 * 判断输入值是否全为数字
//	 * @param value
//	 * @return
//	 */
//	public static boolean isNumeric(String value) {
//		return null == value || value.matches("[0-9]*");
//	}
//	
//	/**
//	 * 取出输入值中的空白字符
//	 * @param value
//	 * @return
//	 */
//	public static String removeSpaces(String value) {
//		if(null == value) {
//			return "";
//		}
//		
//		StringBuffer buffer = new StringBuffer();
//		for(char c : value.toCharArray()) {
//			if(c != ' ') {
//				buffer.append(c);
//			}
//		}
//		
//		return buffer.toString();
//	}
//	
//	/**
//	 * 银行卡号的Luhn算法校验
//	 * @param cardNo 卡号仅允许包含数字
//	 * @return
//	 */
//	public static boolean luhnValidate(String cardNo) {
//		if(null == cardNo || cardNo.length() == 0) {
//			return true;
//		}
//		
//		if(!isNumeric(cardNo)) {
//			return false;
//		}
//		
//		int result = 0;
//		int digit = 0;
//		int counter = 0;
//		for(int i = cardNo.length()-2; i >= 0; i--) {
//			digit = Character.digit(cardNo.charAt(i), 10);
//			if((counter % 2) == 0) {
//				digit *= 2;
//				result += (digit/10 + digit%10);
//			} else {
//				result += digit;
//			}
//			counter++;
//		}
//		
//		return (10 - result%10) == Character.digit(cardNo.charAt(cardNo.length()-1), 10);
//	}
//
//	/*public static void main(String[] args) {
//		System.out.println(luhnValidate("49927398716"));
//	}*/
//
//	public boolean isUseLuhnValidate() {
//		return useLuhnValidate;
//	}
//
//	public CardNoValidator setUseLuhnValidate(boolean useLuhnValidate) {
//		this.useLuhnValidate = useLuhnValidate;
//		return this;
//	}
//
//	public boolean isIgnoreSpaces() {
//		return ignoreSpaces;
//	}
//
//	public CardNoValidator setIgnoreSpaces(boolean ignoreSpaces) {
//		this.ignoreSpaces = ignoreSpaces;
//		return this;
//	}
//}
