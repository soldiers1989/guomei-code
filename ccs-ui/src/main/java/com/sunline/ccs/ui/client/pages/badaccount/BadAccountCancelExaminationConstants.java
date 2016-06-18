/**
 * 
 */
package com.sunline.ccs.ui.client.pages.badaccount;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.i18n.client.Constants;

/**
 * 呆账核销页面的字符常量
* @author fanghj
 *
 */
public interface BadAccountCancelExaminationConstants extends Constants {
	public static final String ADJID = "adjId";
	public static final String PROVISIONORREMIT = "dbCrInd";
	public static final String CURRENCY = "postCurrCd";
	public static final String POSTAMT = "postAmt";
	public static final String THECARDNETWORK = "owningBranch";
	public static final String TXNCODE = "txnCode";
	public static final String AGEGROUP = "ageGroup";
	public static final String BUCKETTYPE = "bucketType";
	public static final String PLANNBR = "planNbr";
	public static final String POSTGLIND = "postGlInd";
	
	public static final String TOWRITEOFFBADDEBTS = "toWriteOffBadDebts";
	
	public static final String PRINCIPALBALANCE = "principalBalance";
	public static final String COSTBALANCE = "costBalance";
	public static final String BJANDFY = "bjAndFy";
	
	String titleWarning();
	
	String alertWarning();
	
	String pageTitle();
	String searchFormTitle();
	
	String sectionTitleAcctList();
	String sectionTitleAcctDetail();
	
	String btnTitleFetchPlanList();
	
	String acctListTitle();
	String acctBatchInfoTitle();
	String acctAuthInfoTitle();
	String planListTitle();
	String planInfoTitle();
	
	String msgSelectNoAcct();
	String blockcodeTitle();
	String descTitle();
	String blockCode();
	String accountAlterTitle();
	String msgOKConfirm();
	
	String msgBlockCodeError();
	String toWriteOffBadDebts();
	String toWriteOffBadDebtsApply();
	
	String adjId();
	String dbCrInd();
	String postCurrCd();
	String postAmt();
	String owningBranch();
	String txnCode();
	String ageGroup();
	String bucketType();
	String planNbr();
	String postGlInd();
	String principalBalance();
	String writeOffAmout();
	String costBalance();
	String msgWriteOffApply();
	String judgePrincipalBalanceOrCostBalance();
	String btnTitleClear();
	
	
	String titleAcctDetail();
	String titleAuthDetail();
	String titleCreditPlanDetail();
	String titleAcctLockCodeDetail();
	String titleBadAcctCheckRequire();
}
