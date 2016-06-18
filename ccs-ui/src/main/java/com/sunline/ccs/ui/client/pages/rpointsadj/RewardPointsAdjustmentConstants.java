/**
 * 
 */
package com.sunline.ccs.ui.client.pages.rpointsadj;

import com.google.gwt.i18n.client.Constants;

/**
 * 积分调整相应常量
* @author fanghj
 *
 */
public interface RewardPointsAdjustmentConstants extends Constants {
	//积分调整方向  A 增加，  S 减少
	public static final String POINT_AJUST_DIRECTION="pointAjustDriection";
	public static final String INCREASE="A";
	public static final String DISCREASE="S";
	
	String pageTitle();
	String msgUpdateCustInfoFail();
	String modifyValue();
	String msgPointAdjuDirect();
	String pointIncrease();
	String pointDiscrease();
	String searchFormTitle();
	String searchBtnTitle();
	String labelLogSeq();

	String pointWarning();
	String msgOKConfirm();
	
	String msgSearchFirst();
	String msgSearchConditionChanged();
	
	String custName();
}
