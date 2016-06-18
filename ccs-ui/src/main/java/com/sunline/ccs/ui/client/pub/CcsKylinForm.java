package com.sunline.ccs.ui.client.pub;

import com.sunline.kylin.web.ark.client.helper.ColumnHelper;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;

/**
 * ccs统一用KylinForm格式
 * @author dch
 *
 */
public class CcsKylinForm extends KylinForm{

	public CcsKylinForm() {
		super.setWidth("98%");
		super.setCol(3);
	}
	
	/**
	 * 
	 * @param components
	 * @return
	 */
	public CcsKylinForm addFileds(Object... components) {
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
