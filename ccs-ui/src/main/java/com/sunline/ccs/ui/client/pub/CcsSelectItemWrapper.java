package com.sunline.ccs.ui.client.pub;

import java.util.LinkedHashMap;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.sunline.kylin.web.ark.client.helper.ColumnHelper;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
import com.sunline.kylin.web.ark.client.ui.KylinForm;

/**
 * 
 * @author yeyu
 *
 */
public final class CcsSelectItemWrapper {
	
	private static CcsSelectItemWrapper wrapper = new CcsSelectItemWrapper();
	
	private CcsSelectItemWrapper(){}
	
	public static CcsSelectItemWrapper getInstance(){
		return wrapper;
	}
	
	/**
	 * 
	 * @param _form 目标表单项
	 * @param item ColumnHelper的子类即可
	 * @param kvPair 下拉框控件键值对参数
	 * @return
	 */
	@SuppressWarnings("all")
	public CcsKylinForm wrapper(final CcsKylinForm _form,final ColumnHelper item,final LinkedHashMap<String,String> kvPair){
		Scheduler.get().scheduleFinally(new ScheduledCommand(){

			@Override
			public void execute() {
				SelectItem selectItem = new SelectItem();
				selectItem.setValue(kvPair);
				_form.setField(item);
				_form.setFieldSelectData(item.getName(), selectItem);
			}
		
		});
		return _form;
	}
	
}
