package com.sunline.ccs.param.ui.client.validate;
/*
import com.smartgwt.client.widgets.form.validator.RegExpValidator;
*/
public class CpsExpValidator {

	/**
	 * 邮编校验(6位置数字)
	 * @return
	 */
/*	public RegExpValidator getPostcode(CpsValidatorConstants constants) {
		RegExpValidator regExpABC = new RegExpValidator();
		regExpABC.setExpression("^[0-9]\\d{5}");
		regExpABC.setAttribute("errorMessage", constants.validatePostcode());
		return regExpABC;
	}
	
	*//**
	 * 国内电话校验
	 * @return
	 *//*
	public RegExpValidator getPhone(CpsValidatorConstants constants) {
		RegExpValidator regExpABC = new RegExpValidator();
		regExpABC.setExpression("(^[0]\\d{2,3}\\-\\d{7,8}$|^[0]\\d{2,3}\\-\\d{7,8}-\\d{1,4}$)|(^[1-9]\\d{6,7}$)|(^[1-9]\\d{6,7}-\\d{1,4}$)");
		regExpABC.setAttribute("errorMessage", constants.validatePhone());
		return regExpABC;
	}
	
	
	*//**
	 * 国内电话校验
	 * @return
	 *//*
	public RegExpValidator getPmtDueDayFixUnit(CpsValidatorConstants constants) {
		RegExpValidator regExpABC = new RegExpValidator();
		regExpABC.setExpression("^(30|31*)$");
		regExpABC.setAttribute("errorMessage", constants.validatePmtDueDayFixUnit());
		return regExpABC;
	}
	*/
}
