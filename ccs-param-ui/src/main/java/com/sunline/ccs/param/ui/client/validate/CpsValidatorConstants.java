package com.sunline.ccs.param.ui.client.validate;

import com.google.gwt.i18n.client.Constants;

public interface CpsValidatorConstants extends Constants{
	 /**
     * 请正确输入6位邮编
     */
    String validatePostcode();
    
    /**
     * 请输入0XX-XXXXXXXX-XXX;XXXXXXXX;0XXXXXXXXXX
     */
    String validatePhone();

    /**
     * 请输入30或者31
     * @return
     */
	String validatePmtDueDayFixUnit();

}
