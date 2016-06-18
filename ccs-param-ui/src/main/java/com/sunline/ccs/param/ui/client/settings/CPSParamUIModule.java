package com.sunline.ccs.param.ui.client.settings;

import com.google.inject.Singleton;

/*
import com.sunline.pcm.facility.client.security.AuthorityConfig;
import com.sunline.pcm.facility.client.security.SimpleAuthorityGroup;
import com.sunline.pcm.ui.common.client.InitializingModule;
import com.sunline.pcm.ui.cp.client.settings.ControlPanelConfig;
import com.sunline.pcm.ui.cp.client.settings.ControlPanelGroup;
import com.sunline.ccs.param.def.enums.CPSAppAuthority;
import com.sunline.ccs.param.def.enums.CPSAuthority;
import com.sunline.ccs.param.ui.client.ServerDef.AddNfServiceDefPage;
import com.sunline.ccs.param.ui.client.ServerDef.NfServiceDefDetailPage;
import com.sunline.ccs.param.ui.client.ServerDef.NfServiceDefPage;
import com.sunline.ccs.param.ui.client.accountattr.AccountAttributeAddPage;
import com.sunline.ccs.param.ui.client.accountattr.AccountAttributeDetailPage;
import com.sunline.ccs.param.ui.client.accountattr.AccountAttributePage;
import com.sunline.ccs.param.ui.client.authMccStateCurrXVerify.AuthMccStateCurrXVerifyAddPage;
import com.sunline.ccs.param.ui.client.authMccStateCurrXVerify.AuthMccStateCurrXVerifyDetailPage;
import com.sunline.ccs.param.ui.client.authMccStateCurrXVerify.AuthMccStateCurrXVerifyPage;
import com.sunline.ccs.param.ui.client.authReasonMapping.AuthReasonMappingAddPage;
import com.sunline.ccs.param.ui.client.authReasonMapping.AuthReasonMappingDetailPage;
import com.sunline.ccs.param.ui.client.authReasonMapping.AuthReasonMappingPage;
import com.sunline.ccs.param.ui.client.blockcode.BlockCodeAddPage;
import com.sunline.ccs.param.ui.client.blockcode.BlockCodeDetailPage;
import com.sunline.ccs.param.ui.client.blockcode.BlockCodePage;
import com.sunline.ccs.param.ui.client.contrlServMapping.NfContrlSrvMappingAddPage;
import com.sunline.ccs.param.ui.client.contrlServMapping.NfContrlSrvMappingDetail;
import com.sunline.ccs.param.ui.client.contrlServMapping.NfContrlSrvMappingPage;
import com.sunline.ccs.param.ui.client.controlField.NfControlFieldAdd;
import com.sunline.ccs.param.ui.client.controlField.NfControlFieldDetail;
import com.sunline.ccs.param.ui.client.controlField.NfControlFieldPage;
import com.sunline.ccs.param.ui.client.countryctrl.AuthCountryAddPage;
import com.sunline.ccs.param.ui.client.countryctrl.AuthCountryDetailPage;
import com.sunline.ccs.param.ui.client.countryctrl.AuthCountryListPage;
import com.sunline.ccs.param.ui.client.currencyCtrl.CurrencyCtrlAddPage;
import com.sunline.ccs.param.ui.client.currencyCtrl.CurrencyCtrlDetailPage;
import com.sunline.ccs.param.ui.client.currencyCtrl.CurrencyCtrlPage;
import com.sunline.ccs.param.ui.client.interestTable.InterestTableAddPage;
import com.sunline.ccs.param.ui.client.interestTable.InterestTableDetailPage;
import com.sunline.ccs.param.ui.client.interestTable.InterestTablePage;
import com.sunline.ccs.param.ui.client.loanMerchant.LoanMerchantAddPage;
import com.sunline.ccs.param.ui.client.loanMerchant.LoanMerchantDetailPage;
import com.sunline.ccs.param.ui.client.loanMerchant.LoanMerchantPage;
import com.sunline.ccs.param.ui.client.loanParam.LoanParamAddPage;
import com.sunline.ccs.param.ui.client.loanParam.LoanParamDetailPage;
import com.sunline.ccs.param.ui.client.loanParam.LoanParamPage;
import com.sunline.ccs.param.ui.client.loanPlan.LoanPlanAddPage;
import com.sunline.ccs.param.ui.client.loanPlan.LoanPlanDetailPage;
import com.sunline.ccs.param.ui.client.loanPlan.LoanPlanPage;
import com.sunline.ccs.param.ui.client.mccctrl.MccCtrlAddPage;
import com.sunline.ccs.param.ui.client.mccctrl.MccCtrlDetailPage;
import com.sunline.ccs.param.ui.client.mccctrl.MccCtrlPage;
import com.sunline.ccs.param.ui.client.merchantGroup.MerchantGroupAddPage;
import com.sunline.ccs.param.ui.client.merchantGroup.MerchantGroupDetailPage;
import com.sunline.ccs.param.ui.client.merchantGroup.MerchantGroupPage;
import com.sunline.ccs.param.ui.client.merchantTxnCrtl.MerchantTxnCrtlAddPage;
import com.sunline.ccs.param.ui.client.merchantTxnCrtl.MerchantTxnCrtlDetailPage;
import com.sunline.ccs.param.ui.client.merchantTxnCrtl.MerchantTxnCrtlPage;
import com.sunline.ccs.param.ui.client.paymentHierarchy.PaymentHierarchyAddPage;
import com.sunline.ccs.param.ui.client.paymentHierarchy.PaymentHierarchyDetailPage;
import com.sunline.ccs.param.ui.client.paymentHierarchy.PaymentHierarchyPage;
import com.sunline.ccs.param.ui.client.plantemplate.PlanTemplateAddPage;
import com.sunline.ccs.param.ui.client.plantemplate.PlanTemplateDetailPage;
import com.sunline.ccs.param.ui.client.plantemplate.PlanTemplatePage;
import com.sunline.ccs.param.ui.client.pointplan.PointPlanAddPage;
import com.sunline.ccs.param.ui.client.pointplan.PointPlanDetailPage;
import com.sunline.ccs.param.ui.client.pointplan.PointPlanPage;
import com.sunline.ccs.param.ui.client.program.ProgramAddPage;
import com.sunline.ccs.param.ui.client.program.ProgramDetailPage;
import com.sunline.ccs.param.ui.client.program.ProgramPage;
import com.sunline.ccs.param.ui.client.sysTxnCdMapping.SysTxnCdMappingAddPage;
import com.sunline.ccs.param.ui.client.sysTxnCdMapping.SysTxnCdMappingDetailPage;
import com.sunline.ccs.param.ui.client.sysTxnCdMapping.SysTxnCdMappingPage;
import com.sunline.ccs.param.ui.client.txnCd.TxnCdAddPage;
import com.sunline.ccs.param.ui.client.txnCd.TxnCdDetailPage;
import com.sunline.ccs.param.ui.client.txnCd.TxnCdPage;
import com.google.gwt.place.shared.Place;
import com.google.inject.Inject;
import com.google.inject.Singleton;
*/
@Singleton
public class CPSParamUIModule /*implements InitializingModule*/ {
/*	@Inject
	private ControlPanelConfig cpConfig;

	@Inject
	private AuthCountryListPage authCountryListPage;

	@Inject
	private AuthCountryAddPage authCountryAddPage;

	@Inject
	private AuthCountryDetailPage authCountryDetailPage;

	@Inject
	private AuthReasonMappingPage authReasonMappingPage;

	@Inject
	private AuthReasonMappingAddPage authReasonMappingAddPage;

	@Inject
	private AuthReasonMappingDetailPage authReasonMappingDetailPage;

	@Inject
	private BlockCodePage blockCodePage;

	@Inject
	private BlockCodeAddPage blockCodeAddPage;

	@Inject
	private BlockCodeDetailPage blockCodeDetailPage;

	@Inject
	private CurrencyCtrlPage currencyCtrlPage;

	@Inject
	private CurrencyCtrlDetailPage currencyCtrlDetailPage;

	@Inject
	private CurrencyCtrlAddPage currencyCtrlAddPage;

	@Inject
	private AuthMccStateCurrXVerifyPage authMccStateCurrXVerifyPage;

	@Inject
	private AuthMccStateCurrXVerifyDetailPage authMccStateCurrXVerifyDetailPage;

	@Inject
	private AuthMccStateCurrXVerifyAddPage authMccStateCurrXVerifyAddPage;

	@Inject
	private SysTxnCdMappingPage sysTxnCdMappingPage;

	@Inject
	private SysTxnCdMappingAddPage sysTxnCdMappingAddPage;

	@Inject
	private SysTxnCdMappingDetailPage sysTxnCdMappingDetailPage;

	@Inject
	private TxnCdPage txnCdPage;

	@Inject
	private TxnCdAddPage txnCdAddPage;

	@Inject
	private TxnCdDetailPage txnCdDetailPage;

	@Inject
	private MerchantTxnCrtlPage merchantTxnCrtlPage;

	@Inject
	private MerchantTxnCrtlAddPage merchantTxnCrtlAddPage;

	@Inject
	private MerchantTxnCrtlDetailPage merchantTxnCrtlDetailPage;

	@Inject
	private PaymentHierarchyPage paymentHierarchyPage;

	@Inject
	private PaymentHierarchyAddPage paymentHierarchyAddPage;

	@Inject
	private PaymentHierarchyDetailPage paymentHierarchyDetailPage;

	@Inject
	private PlanTemplatePage planTemplatePage;

	@Inject
	private PlanTemplateAddPage planTemplateAddPage;

	@Inject
	private PlanTemplateDetailPage planTemplateDetailPage;

	@Inject
	private MccCtrlPage mccCtrlPage;

	@Inject
	private MccCtrlAddPage mccCtrlAddPage;

	@Inject
	private MccCtrlDetailPage mccCtrlDetailpage;

	@Inject
	private LoanPlanPage loanPlanPage;

	@Inject
	private LoanPlanAddPage loanPlanAddPage;

	@Inject
	private LoanPlanDetailPage loanPlanDetailPage;
	
	@Inject
	private LoanParamPage loanParamPage;
	
	@Inject
	private LoanParamAddPage loanParamAddPage;
	
	@Inject
	private LoanParamDetailPage loanParamDetailPage;
	
	@Inject
	private InterestTablePage interestTablePage;
	
	@Inject
	private InterestTableAddPage interestTableAddPage;
	
	@Inject
	private InterestTableDetailPage interestTableDetailPage;
	
	@Inject
	private AccountAttributePage accountAttributePage;

	@Inject
	private AccountAttributeAddPage accountAttributeAddPage;
	
	@Inject
	private AccountAttributeDetailPage accountAttributeDetailPage;
	
	@Inject
	private PointPlanPage pointPlanPage;
	
	@Inject
	private PointPlanAddPage pointPlanAddPage;
	
	@Inject
	private PointPlanDetailPage pointPlanDetailPage;
	
	@Inject
	private MerchantGroupPage merchantGroupPage;
	
	@Inject
	private MerchantGroupAddPage merchantGroupAddPage;
	
	@Inject
	private MerchantGroupDetailPage merchantGroupDetailPage;
	
	@Inject
	private LoanMerchantPage loanMerchantPage;
	
	@Inject
	private LoanMerchantAddPage loanMerchantAddPage;
	
	@Inject
	private LoanMerchantDetailPage loanMerchantDetailPage;
	
	@Inject
	private ProgramPage programPage;
	
	@Inject
	private ProgramAddPage programAddPage;
	
	@Inject
	private ProgramDetailPage programDetailPage;
	
	@Inject
	private NfServiceDefPage nfServiceDefPage;
	
	@Inject
	private AddNfServiceDefPage nfServiceDefAddPage;
	
	@Inject
	private NfServiceDefDetailPage nfServiceDefDetailPage;
	
	@Inject
	private NfControlFieldPage nfControlFieldPage;
	
	@Inject
	private NfControlFieldAdd nfControlFieldAdd;
	
	@Inject
	private NfControlFieldDetail nfControlFieldDetail;
	
	@Inject
	private NfContrlSrvMappingPage contrlSrvMappingPage;
	
	@Inject
	private NfContrlSrvMappingDetail contrlSrvMappingDetail;
	
	@Inject
	private NfContrlSrvMappingAddPage contrlSrvMappingAdd;
	
	@Inject
	private CPSParamUIConstants constants;

	@Inject
	private AuthorityConfig authorityConfig;
	
	@Inject
	private CPSParamUIImages images;

	@Override
	public void init() {

		ControlPanelGroup cpg = new ControlPanelGroup("auth", constants.groupTitle(), images.cpsParam());
		cpg.addPage(authCountryListPage);
		cpg.addPage(authCountryAddPage);
		cpg.addPage(authCountryDetailPage);
		cpg.addPage(authReasonMappingPage);
		cpg.addPage(authReasonMappingAddPage);
		cpg.addPage(authReasonMappingDetailPage);
		cpg.addPage(blockCodePage);
		cpg.addPage(blockCodeAddPage);
		cpg.addPage(blockCodeDetailPage);
		cpg.addPage(currencyCtrlPage);
		cpg.addPage(currencyCtrlDetailPage);
		cpg.addPage(currencyCtrlAddPage);
		cpg.addPage(authMccStateCurrXVerifyPage);
		cpg.addPage(authMccStateCurrXVerifyDetailPage);
		cpg.addPage(authMccStateCurrXVerifyAddPage);
		cpg.addPage(sysTxnCdMappingPage);
		cpg.addPage(sysTxnCdMappingAddPage);
		cpg.addPage(sysTxnCdMappingDetailPage);
		cpg.addPage(txnCdPage);
		cpg.addPage(txnCdAddPage);
		cpg.addPage(txnCdDetailPage);
		cpg.addPage(merchantTxnCrtlPage);
		cpg.addPage(merchantTxnCrtlAddPage);
		cpg.addPage(merchantTxnCrtlDetailPage);
		cpg.addPage(paymentHierarchyPage);
		cpg.addPage(paymentHierarchyAddPage);
		cpg.addPage(paymentHierarchyDetailPage);
		cpg.addPage(planTemplatePage);
		cpg.addPage(planTemplateAddPage);
		cpg.addPage(planTemplateDetailPage);
		cpg.addPage(mccCtrlPage);
		cpg.addPage(mccCtrlAddPage);
		cpg.addPage(mccCtrlDetailpage);
		cpg.addPage(loanPlanPage);
		cpg.addPage(loanPlanAddPage);
		cpg.addPage(loanPlanDetailPage);
		cpg.addPage(loanParamPage);
		cpg.addPage(loanParamAddPage);
		cpg.addPage(loanParamDetailPage);
		cpg.addPage(interestTablePage);
		cpg.addPage(interestTableAddPage);
		cpg.addPage(interestTableDetailPage);
		cpg.addPage(accountAttributePage);
		cpg.addPage(accountAttributeAddPage);
		cpg.addPage(accountAttributeDetailPage);
		cpg.addPage(pointPlanPage);
		cpg.addPage(pointPlanAddPage);
		cpg.addPage(pointPlanDetailPage);
		cpg.addPage(merchantGroupPage);
		cpg.addPage(merchantGroupAddPage);
		cpg.addPage(merchantGroupDetailPage);
		cpg.addPage(loanMerchantPage);
		cpg.addPage(loanMerchantAddPage);
		cpg.addPage(loanMerchantDetailPage);
		cpg.addPage(programPage);
		cpg.addPage(programAddPage);
		cpg.addPage(programDetailPage);
		cpg.addPage(nfServiceDefPage);
		cpg.addPage(nfServiceDefAddPage);
		cpg.addPage(nfServiceDefDetailPage);
		cpg.addPage(nfControlFieldPage);
		cpg.addPage(nfControlFieldAdd);
		cpg.addPage(nfControlFieldDetail);
		cpg.addPage(contrlSrvMappingPage);
		cpg.addPage(contrlSrvMappingAdd);
		cpg.addPage(contrlSrvMappingDetail);
		cpConfig.addPanelGroup(cpg);

		SimpleAuthorityGroup authorityGroup = new SimpleAuthorityGroup(constants.groupTitle());
		for(CPSAuthority authority : CPSAuthority.values()){
			
			authorityGroup.addAuthority(authority.toString(), constants);
		}
		authorityConfig.addAuthorityGroup(authorityGroup);
		
		SimpleAuthorityGroup cpsAppGroup = new SimpleAuthorityGroup(constants.cpsAppGroupTitle());
		for(CPSAppAuthority authority : CPSAppAuthority.values()){
			
			cpsAppGroup.addAuthority(authority.toString(), constants);
		}
		authorityConfig.addAuthorityGroup(cpsAppGroup);
	}

	@Override
	public String getModuleTitle() {
		return null;
	}

	@Override
	public Place getEntryPlace() {

		return null;
	}
*/
}
