package com.sunline.ccs.ui.client.pub;

import com.sunline.kylin.web.ark.client.ui.KylinGrid;

/**
 * ccs统一用KylinGrid格式
 * @author dch
 *
 */
public class CcsKylinGrid extends KylinGrid {

	public CcsKylinGrid() {
		super.setWidth("98%");
		super.setHeight("98%");
	}
	
	public CcsKylinGrid(String height,String width) {
		super.setWidth(width);
		super.setHeight(height);
	}
	
}
