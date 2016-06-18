package com.sunline.ccs.ui.client.commons;

import com.sunline.kylin.web.ark.client.helper.ColumnHelper;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;


/**
 * 
 * @说明 ccs统一用KylinForm格式
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
public class CommonKylinForm extends KylinForm{

	public CommonKylinForm() {
		super.setWidth("98%");
		super.setCol(3);
	}
	
	/**
	 * 
	 *
	 * @param components ColumnHelper子类
	 * 
	 * @return this
	 *
	 * @说明 批量向KylinForm中加入控件
	 *
	 * @author yeyu
	 *
	 * @修改记录 
	 *
	 */
	public CommonKylinForm addFileds(Object... components) {
		for(Object component : components){
			if(component instanceof ColumnHelper){
				this.setField((ColumnHelper)component);
			} else if(component instanceof KylinButton) {
				this.addButton((KylinButton)component);
			} 
		}
		return this;
	}
	
	
	
}
