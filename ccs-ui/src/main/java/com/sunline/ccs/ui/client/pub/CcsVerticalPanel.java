package com.sunline.ccs.ui.client.pub;

import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 页面主返回VerticalPanel
 * @author dch
 *
 */
public class CcsVerticalPanel extends VerticalPanel {
	
	/**
	 * 垂直布局默认构造函数
	 */
	public CcsVerticalPanel() {
		super.setWidth("100%");
		super.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
	}
	
	/**
	 * 垂直布局重载构造函数,可以设置布局宽度
	 * @param width 布局宽度
	 */
	public CcsVerticalPanel(String width) {
		super.setWidth(width);
		super.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
	}
}
