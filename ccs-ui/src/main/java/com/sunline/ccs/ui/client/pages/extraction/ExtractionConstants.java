/**
 * 
 */
package com.sunline.ccs.ui.client.pages.extraction;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.i18n.client.Constants;

/**
 * 计提申请页面字符串常量
* @author fanghj
 *
 */
public interface ExtractionConstants extends Constants {
	
	public static final String PROVISION = "provision";
	public static final String REMIT = "remit";
	
	public static final String ADJID = "adjId";
	public static final String PROVISIONORREMIT = "provisionOrRemit";
	public static final String CURRENCY = "currency";
	public static final String POSTAMT = "postAmt";
	public static final String THECARDNETWORK = "theCardNetwork";
	public static final String TXNCODE = "txnCode";
	public static final String AGEGROUP = "ageGroup";
	public static final String BUCKETTYPE = "bucketType";
	public static final String PLANNBR = "planNbr";
	public static final String POSTGLIND = "postGlInd";
	public static final String AGEGROUPANDDESCRIPTION = "ageGroupAndDescription";
	public static final String PERCENTAGE = "percentage";
	public static final String COUNTRESULT = "countResult";
	public static final String MODIFYRESULT = "modifyResult";
	public static final String VERIFICATION = "verification";
	
	String pageTitle();
	
	String adjustFlagTitle();
	String adjustAmtTitle();
	String adjustDateTitle();
	String remarkTitle();
	
	String btnTitleSearch();
	String labelLogSeq();

	String titleAdjLogList();
	String titleAdjForm();
	
//	String tabTitleLogForm();
	String tabTitleTranForm();
	

	String btnTitleCancel();
	
	String msgSelectNoRecord();
	String msgCancelConfirm();
	
	String msgSearchFirst();
	String aTitle();
	String bTitle();
	String cTitle();
	String dTitle();
	String msgSearchConditionChanged();
	
	String custName();
	
	String selectItemProvisionOrRemit();
	String provision();
	String remit();


	String theCardNetwork();

	String adjId();

	String provisionOrRemit();

	String currency();

	String postAmt();

	String txnCode();

	String ageGroup();

	String bucketType();

	String planNbr();

	String postGlInd();

	String titleOperation();

	String trialButton();
	
	String btnTitleAdjust();

	String msgRemitConfirm();

	String ageGroupAndDescription();

	String percentage();

	String countResult();

	String modifyResult();

	String total();

	String tabTitleRemitForm();

	String provisionButton();

	String provisionApply();

	String remitButton();

	String remitApply();

	String deleteButton();

	String provisionOrRemitApply();

	String queryList();

	String trialMoney();

	String verification();

	String warningForTotal();
}
