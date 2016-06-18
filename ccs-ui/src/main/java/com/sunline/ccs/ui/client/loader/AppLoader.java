//package com.sunline.ccs.ui.client.loader;
//
//import com.sunline.pcm.ui.common.client.ClientContext;
//import com.sunline.pcm.ui.common.client.InitializingModule;
//import com.sunline.pcm.ui.common.client.Loader;
//import com.sunline.pcm.ui.cp.client.panel.ControlPanelPlace;
//import com.sunline.pcm.ui.cp.client.panel.ControlPanelView;
//import com.sunline.pcm.ui.cp.client.settings.ControlPanelModule;
//import com.sunline.ccs.ui.client.security.setting.SecurityModule;
//import com.sunline.ccs.ui.client.settings.ManagementModule;
//import com.sunline.ccs.ui.client.settings.ManagementPlace;
//import com.sunline.ccs.ui.client.settings.ManagementView;
//import com.sunline.ark.gwt.client.mvp.MapperBuilder;
//import com.sunline.ark.gwt.client.util.RPCExecutor;
//import com.sunline.ark.gwt.client.util.RPCTemplate;
//import com.google.gwt.activity.shared.ActivityMapper;
//import com.google.gwt.place.shared.Place;
//import com.google.gwt.place.shared.PlaceHistoryMapper;
//import com.google.gwt.user.client.rpc.AsyncCallback;
//import com.google.inject.Inject;
//import com.google.inject.Singleton;
//
///**
// * 加载系统各个模块
//* @author fanghj
// *
// */
//@Singleton
//public class AppLoader extends Loader {
//	
//	@Inject
//	private AppPlaceHistoryMapper appPlaceHistoryMapper;
//
//	@Inject
//	private ControlPanelView controlPanelView;
//	
//	@Inject
//	private ManagementModule managementModule;
//	
//	@Inject
//	private SecurityModule securityModule;
//	
//	@Inject
//	private ControlPanelModule cpModule;
//	
//	@Inject
//	private ManagementView managementView;
//	
//	@Inject
//	private AppLoaderInterAsync server;
//
//	@Override
//	protected InitializingModule[] getModules() {
//		return new InitializingModule[]{managementModule, securityModule, cpModule};
//	}
//
//	@Override
//	protected Place getDefaultPlace() {
//		return new ManagementPlace();
//	}
//
//	@Override
//	protected PlaceHistoryMapper getPlaceHistoryMapper() {
//		return appPlaceHistoryMapper;
//	}
//
//	@Override
//	protected ActivityMapper createActivityMapper() {
//		return new MapperBuilder()
//			.addMapping(ControlPanelPlace.class, controlPanelView)
//			.addMapping(ManagementPlace.class, managementView)
//			.build();
//	}
//
//	@Override
//	protected void getClientContext() {
//		RPCTemplate.call(new RPCExecutor<ClientContext>() {
//
//			@Override
//			public void execute(AsyncCallback<ClientContext> callback) {
//				server.getClientContext(callback);
//			}
//			
//			@Override
//			public void onSuccess(ClientContext result) {
//				AppLoader.this.show(result);
//			}
//		});
//	}
//
//}
