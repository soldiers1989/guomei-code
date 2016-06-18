/**
 * 
 */
package com.sunline.ccs.ui.client.validator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

/**
* @author fanghj
 *
 */
public interface CustomValidatorMessages extends Messages {
	public static final CustomValidatorMessages INSTANCE = GWT.create(CustomValidatorMessages.class);
	
	String integerInvalid();
	String integerInvalidWidthMinLength(int minLength, String others);
	String notAllowMinus();
	String and();
	
	String cardNoInvalid();
	
}
