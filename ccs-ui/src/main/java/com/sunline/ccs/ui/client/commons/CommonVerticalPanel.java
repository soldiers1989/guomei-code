package com.sunline.ccs.ui.client.commons;

import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 
 * @说明 页面主返回VerticalPanel
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
public class CommonVerticalPanel extends VerticalPanel {
	
	/**
	 * @说明 垂直布局默认构造函数
	 */
	public CommonVerticalPanel() {
		super.setWidth("100%");
		super.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
	}
	
	/**
	 * @param width 布局宽度
	 * @说明 垂直布局重载构造函数,可以设置布局宽度
	 */
	public CommonVerticalPanel(String width) {
		super.setWidth(width);
		super.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
	}
}
