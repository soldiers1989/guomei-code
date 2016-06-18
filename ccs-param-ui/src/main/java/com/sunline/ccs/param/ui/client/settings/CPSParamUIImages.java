package com.sunline.ccs.param.ui.client.settings;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;

public interface CPSParamUIImages extends ClientBundle {

	@Source("cps-param.png")
	@ImageOptions(repeatStyle=RepeatStyle.Both)
	ImageResource cpsParam();
	
}
