/**
 * 
 */
package com.sunline.ccs.ui.client.commons;

import com.google.gwt.i18n.client.Constants;

/**
 * 
 * @说明 全局常量定义接口
 * 
 * @version 1.0 
 *
 * @Date Jun 17, 2015
 *
 * @作者 yeyu
 *
 * @修改记录 
 * [编号：20150617_01]，[修改人：yeyu ]，[修改说明：加入注释]
 */
public interface CommonGlobalConstants extends Constants {
	
	/**
	 * 
	 *
	 * @return 开始日期标签
	 *
	 * @说明 获取开始日期标签
	 *
	 * @author yeyu
	 *
	 * @修改记录 
	 * 
	 */
	String labelBeginDate();
	
	/**
	 * 
	 *
	 * @return 结束日期标签
	 *
	 * @说明 获取结束日期标签
	 *
	 * @author yeyu
	 *
	 * @修改记录 
	 *
	 */
	String labelEndDate();
	
	
	String msgSearchConditionInvalid();
	
	/**
	 * 
	 *
	 * @return 提交按钮显示内容
	 *
	 * @说明 获取提交按钮显示内容
	 *
	 * @author yeyu
	 *
	 * @修改记录 
	 *
	 */
	String msgSubmitConfirm();
	
}
