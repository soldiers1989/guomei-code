package com.sunline.ccs.ui.client.images;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;

/**
 * 
 * @说明 
 * 
 * @version 1.0 
 *
 * @Date Jun 17, 2015
 *
 * @作者 yeyu
 *
 * @修改记录 
 * 
 */
public interface CPSImages extends ClientBundle {

	@Source("bell.png")
	@ImageOptions(repeatStyle=RepeatStyle.Both)
	ImageResource bell();
	
	@Source("safe.png")
	@ImageOptions(repeatStyle=RepeatStyle.Both)
	ImageResource safe();

	@Source("inbox.png")
	@ImageOptions(repeatStyle=RepeatStyle.Both)
	ImageResource inbox();

	@Source("nautical-vessel.png")
	@ImageOptions(repeatStyle=RepeatStyle.Both)
	ImageResource nauticalVessel();
}
