//package com.sunline.ccs.ui.client.security.setting;
//
//import com.sunline.pcm.ui.common.client.InitializingModule;
//import com.sunline.pcm.ui.cp.client.settings.ControlPanelConfig;
//import com.sunline.pcm.ui.cp.client.settings.ControlPanelGroup;
//import com.sunline.ccs.ui.client.security.risk.RiskPage;
//import com.google.gwt.place.shared.Place;
//import com.google.inject.Inject;
//
///**
// * 
//* @author fanghj
// *
// */
//public class SecurityModule implements InitializingModule {
//	@Inject
//	private SecurityConstants constants;
//	
//	@Inject
//	private ControlPanelConfig config;
//	
//	@Inject
//	private RiskPage riskPage;
//	
//	@Override
//	public void init() {
//		ControlPanelGroup cpg = new ControlPanelGroup("sec", constants.security());
//		cpg.addPage(riskPage);
//		config.addPanelGroup(cpg);
//	}
//
//	@Override
//	public String getModuleTitle() {
//		return null;
//	}
//
//	@Override
//	public Place getEntryPlace() {
//		return null;
//	}
//
//}
