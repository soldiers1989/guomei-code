//package com.sunline.ccs.ui.client.settings;
//
//import com.sunline.pcm.ui.common.client.DispatcherPage;
//import com.sunline.pcm.ui.common.client.DispatcherPlace;
//import com.sunline.ark.gwt.client.mvp.ParamMapToken;
//import com.google.gwt.place.shared.PlaceTokenizer;
//
//public class ManagementPlace extends DispatcherPlace {
//	public static class Tokenizer implements PlaceTokenizer<ManagementPlace>
//	{
//
//		public ManagementPlace getPlace(String tokenString) {
//			return new ManagementPlace(new ParamMapToken(tokenString));
//		}
//
//		public String getToken(ManagementPlace place) {
//			return place.getToken().toString();
//		}
//	}
//	
//	public ManagementPlace()
//	{
//		super();
//	}
//
//	public ManagementPlace(String pageId)
//	{
//		super(pageId);
//	}
//	
//	public ManagementPlace(ParamMapToken token)
//	{
//		super(token);
//	}
//	
//	public ManagementPlace(DispatcherPage page)
//	{
//		this(page.getPageId());
//	}
//}
