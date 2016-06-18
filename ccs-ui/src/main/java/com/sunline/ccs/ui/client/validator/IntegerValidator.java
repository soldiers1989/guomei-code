///**
// * 
// */
//package com.sunline.ccs.ui.client.validator;
//
//import com.smartgwt.client.widgets.form.validator.CustomValidator;
//
///**
// * 判断输入的数据是否为整数，允许不输入数据
//* @author fanghj
// *
// */
//public class IntegerValidator extends CustomValidator {
//	private CustomValidatorMessages messages = CustomValidatorMessages.INSTANCE;
//	
//	private boolean allowMinus = false;
//	private int minLength;
//	
//	/* (non-Javadoc)
//	 * @see com.smartgwt.client.widgets.form.validator.CustomValidator#condition(java.lang.Object)
//	 */
//	@Override
//	protected boolean condition(Object value) {
//		if(null == value) {
//			return true;
//		}
//		
//		String errorMessage = messages.integerInvalid();
//		
//		String valueStr = value.toString();
//		String regex = "[0-9]";
//		if(allowMinus) {
//			regex = "^-?" + regex;
//		}
//		
//		if(minLength > 0) {
//			regex += "{"+minLength+",}";
//			
//			if(allowMinus) {
//				errorMessage = messages.integerInvalidWidthMinLength(minLength, "");
//			} else {
//				errorMessage = messages.integerInvalidWidthMinLength(minLength, messages.and()+messages.notAllowMinus());
//			}
//		} else {
//			regex += "*";
//		}
//		
//		if(valueStr.matches(regex)) {
//			return true;
//		} else {
//			setErrorMessage(errorMessage);
//			return false;
//		}
//	}
//
//	public IntegerValidator setAllowMinus(boolean allowMinus) {
//		this.allowMinus = allowMinus;
//		
//		return this;
//	}
//
//	public int getMinLength() {
//		return minLength;
//	}
//
//	public IntegerValidator setMinLength(int minLength) {
//		this.minLength = minLength;
//		return this;
//	}
//
//	public boolean isAllowMinus() {
//		return allowMinus;
//	}
//}
