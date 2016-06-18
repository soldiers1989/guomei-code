package com.sunline.ccs.ui.client.commons;

import java.util.Date;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * 
 * @see 类名：CommonCcsDateUtils
 * @see 描述：日期处理函数集
 *
 * @see 创建日期：   Jun 27, 20151:45:28 PM
 * @author yeyu
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public final class CommonCcsDateUtils {
	
	/**
	 * 
	 * @see 类名：Unit
	 * @see 描述：日期时间类型枚举;注意当前只实现了对天,月,年的增减功能。秒,分钟,小时的调整尚未实现
	 *
	 * @see 创建日期：   Jun 30, 20152:22:08 PM
	 * @author yeyu
	 * 
	 * @see 修改记录：
	 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
	 */
	public enum Unit {  
	    SECOND("s"), MINUTE("n"), HOUR("h"), DAY("d"), MONTH("m"), YEAR("y");  
	    private final String jsCode;
	    private Unit(String jsCode) {  
	      this.jsCode = jsCode;
	    }  
	}
	
	/**
	 * @see GWT Javascript脚本对象 [{@link com.google.gwt.core.client.JavaScriptObject}]
	 */
	final JavaScriptObject jsDate;
	
	/**
	 * 
	 * Creates a new instance of CommonCcsDateUtils. 
	 * 
	 * @param mills {@linkplain 当前日期毫秒数}
	 */
	public CommonCcsDateUtils(double mills){
		this(createDate(mills));
	}
	
	/**
	 * 
	 * Creates a new instance of CommonCcsDateUtils. 
	 * 
	 * @param jso {@link com.google.gwt.core.client.JavaScriptObject}
	 */
	private CommonCcsDateUtils(JavaScriptObject jso) {
	    jsDate = jso;
	}
	
	/**
	 * 
	 * @see 方法名：createDate 
	 * @see 描述：JSNI方法,用于创建Javascript Date对象
	 * @see 创建日期：Jun 30, 20152:29:49 PM
	 * @author yeyu
	 *  
	 * @param mills {@linkplain 外部传入的Java.util.Date对象所对应的当前时间毫秒数}
	 * @return {@link com.google.gwt.core.client.JavaScriptObject}
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private static native JavaScriptObject createDate(double mills)/*-{
		return new Date(mills);
	}-*/;
	
	/**
	 * 
	 * @see 方法名：addInterval 
	 * @see 描述：增减当前日期年,月,日位置的具体javascript函数实现
	 * @see 创建日期：Jun 30, 20152:31:58 PM
	 * @author yeyu
	 *  
	 * @param interval {@link com.sunline.ccs.ui.client.commons.CommonCcsDateUtils.Unit}
	 * @param quantity {@linkplain 需要调整的具体数值}
	 * @return {@link com.google.gwt.core.client.JavaScriptObject}
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private native JavaScriptObject addInterval(String interval,double quantity)/*-{
		var d = this.@com.sunline.ccs.ui.client.commons.CommonCcsDateUtils::jsDate;
		switch(interval){
			case 'y' : 
				var fullYear = d.getUTCFullYear();
				fullYear = fullYear + quantity;
				d.setUTCFullYear(fullYear);
				break;
			case 'm' :
				var utcMonth = d.getUTCMonth();
				utcMonth = utcMonth + quantity;
				d.setUTCMonth(utcMonth);
				break;
			case 'd' :
				var utcDate = d.getUTCDate();
				utcDate = utcDate + quantity;
				d.setUTCDate(utcDate);
				break;
			default : 
				break;
		}
		return d;
	}-*/;
	
	/**
	 * 
	 * @see 方法名：addYear 
	 * @see 描述：将日期对象中的年字段增加或减少指定数值
	 * @see 创建日期：Jun 30, 20152:34:23 PM
	 * @author yeyu
	 *  
	 * @param interval {@link com.sunline.ccs.ui.client.commons.CommonCcsDateUtils.Unit}
	 * @param quantity {@linkplain 需要调整的具体数值}
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public CommonCcsDateUtils addYear(Unit interval,int quantity){
		return new CommonCcsDateUtils(addInterval(interval.jsCode,quantity));
	}
	
	/**
	 * 
	 * @see 方法名：asDate 
	 * @see 描述：获取经过javascript处理后的java.util.Date对象
	 * @see 创建日期：Jun 30, 20152:35:30 PM
	 * @author yeyu
	 *  
	 * @return 增加或减少指定数值后的日期对象 {@link java.util.Date}
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public Date asDate(){
		return new Date((long)callMethod("getTime"));
	}
	
	/**
	 * 
	 * @see 方法名：callMethod 
	 * @see 描述：从java代码中调用原生javascript方法
	 * @see 创建日期：Jun 30, 20152:36:56 PM
	 * @author yeyu
	 *  
	 * @param method {@linkplain javascript方法名称}
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private native double callMethod(String method)/*-{
		var d = this.@com.sunline.ccs.ui.client.commons.CommonCcsDateUtils::jsDate;
		return d[method]();
	}-*/;
}
