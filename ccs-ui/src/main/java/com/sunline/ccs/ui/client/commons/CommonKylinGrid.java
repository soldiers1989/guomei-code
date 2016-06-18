package com.sunline.ccs.ui.client.commons;

import com.sunline.kylin.web.ark.client.ui.KylinGrid;

/**
 * 
 * @说明 ccs统一用KylinGrid格式
 * 
 * @version 1.0 
 *
 * @Date Jun 17, 2015
 *
 * @作者 dch
 *
 * @修改记录 
 * 
 */
public class CommonKylinGrid extends KylinGrid {
	
	/**
	 * @说明 初始化固定长宽的KylinGrid
	 */
	public CommonKylinGrid() {
		super.setWidth("98%");
		super.setHeight("98%");
	}
	
	/**
	 * 
	 * @param height 宽度
	 * @param width 高度
	 * @说明 初始化可自定义长宽的KylinGrid
	 */
	public CommonKylinGrid(String height,String width) {
		super.setWidth(width);
		super.setHeight(height);
	}
	
}
