package com.sunline.ccs.ui.client.commons;

import java.util.LinkedHashMap;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.KylinForm;

/**
 * 
 * @说明 下拉列表控件包装类
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
public final class CommonSelectItemWrapper {
	
	private static CommonSelectItemWrapper wrapper = new CommonSelectItemWrapper();
	
	private CommonSelectItemWrapper(){}
	
	public static CommonSelectItemWrapper getInstance(){
		return wrapper;
	}
	
	/**
	 * 
	 *
	 * @param _form 目标表单项
	 * 
	 * @param item ColumnHelper的子类即可
	 * 
	 * @param kvPair 下拉框控件键值对参数
	 * 
	 * @return
	 *
	 * @说明 包装KylinForm加入自定义下拉框
	 *
	 * @author yeyu
	 *
	 * @修改记录 
	 *
	 */
	@SuppressWarnings("all")
	public KylinForm wrapper(final KylinForm _form,final TextColumnHelper item,final LinkedHashMap<String,String> kvPair){
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
