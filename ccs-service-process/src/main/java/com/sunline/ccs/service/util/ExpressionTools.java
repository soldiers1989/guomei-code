package com.sunline.ccs.service.util;

import java.util.Date;

import com.mysema.query.support.Expressions;
import com.mysema.query.types.Ops;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.expr.BooleanOperation;
import com.mysema.query.types.path.DatePath;
import com.mysema.query.types.path.DateTimePath;


/** 
 * @see 类名：ExpressionTools
 * @see 描述：dslquery查询表达
 *
 * @see 创建日期：   2015年6月24日 下午2:53:04
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class ExpressionTools {

	/**
	 * 日期戳大于表达式
	 * 
	 * @param path
	 * @param dateStr
	 * @return BooleanExpression tmAuthHst.logOlTime > 2012-12-12
	 * @exception
	 * @since 1.0.0
	 */
	public static BooleanExpression getDateGTExpression(
			DateTimePath<java.util.Date> path, Date date) {
		return path.gt(date);
	}

	/**
	 * 日期戳大于等于表达式
	 * 
	 * @param path
	 * @param dateStr
	 * @return BooleanExpression tmAuthHst.logOlTime >= 2012-12-12
	 * @exception
	 * @since 1.0.0
	 */
	public static BooleanExpression getDateGOEExpression(
			DateTimePath<java.util.Date> path, Date date) {
		return path.goe(date);
	}

	/**
	 * 日期戳小于表达式
	 * 
	 * @param path
	 * @param dateStr
	 * @return BooleanExpression tmAuthHst.logOlTime < 2012-12-12
	 * @exception
	 * @since 1.0.0
	 */
	public static BooleanExpression getDateLTExpression(
			DateTimePath<java.util.Date> path, Date date) {
		return path.lt(date);
	}

	/**
	 * 日期戳小于表达式
	 * 
	 * @param path
	 * @param dateStr
	 * @return BooleanExpression tmAuthHst.logOlTime <= 2012-12-12
	 * @exception
	 * @since 1.0.0
	 */
	public static BooleanExpression getDateLOEExpression(
			DateTimePath<java.util.Date> path, Date date) {
		return path.loe(date);
	}

	/**
	 * 日期大于表达式
	 * 
	 * @param path
	 * @param dateStr
	 * @return BooleanExpression tmAuthHst.logOlTime > 2012-12-12
	 * @exception
	 * @since 1.0.0
	 */
	public static BooleanExpression getDateGTExpression(
			DateTimePath<java.util.Date> path, String dateStr) {
		DatePath<java.sql.Date> datePath = Expressions.datePath(
				java.sql.Date.class, dateStr);
		return BooleanOperation.create(Ops.GT, path, datePath);
	}

	/**
	 * 日期大于等于表达式
	 * 
	 * @param path
	 * @param dateStr
	 * @return BooleanExpression tmAuthHst.logOlTime >= 2012-12-12
	 * @exception
	 * @since 1.0.0
	 */
	public static BooleanExpression getDateGOEExpression(
			DateTimePath<java.util.Date> path, String dateStr) {
		DatePath<java.sql.Date> datePath = Expressions.datePath(
				java.sql.Date.class, dateStr);
		return BooleanOperation.create(Ops.GOE, path, datePath);
	}

	/**
	 * 日期小于表达式
	 * 
	 * @param path
	 * @param dateStr
	 * @return BooleanExpression tmAuthHst.logOlTime < 2012-12-12
	 * @exception
	 * @since 1.0.0
	 */
	public static BooleanExpression getDateLTExpression(
			DateTimePath<java.util.Date> path, String dateStr) {
		DatePath<java.sql.Date> datePath = Expressions.datePath(
				java.sql.Date.class, dateStr);
		return BooleanOperation.create(Ops.LT, path, datePath);
	}

	/**
	 * 日期小于等于表达式
	 * 
	 * @param path
	 * @param dateStr
	 * @return BooleanExpression tmAuthHst.logOlTime <= 2012-12-12
	 * @exception
	 * @since 1.0.0
	 */
	public static BooleanExpression getDateLOEExpression(
			DateTimePath<java.util.Date> path, String dateStr) {
		DatePath<java.sql.Date> datePath = Expressions.datePath(
				java.sql.Date.class, dateStr);
		return BooleanOperation.create(Ops.LOE, path, datePath);
	}

//	public static void main(String[] args) {
//		QCcsAuthmemoHst qCcsAuthmemoHst = QCcsAuthmemoHst.tmAuthHst;
//		// System.out.println(Test.getDateGTExpression(qCcsAuthmemoHst.logOlTime,
//		// d));
//		// System.out.println(Test.getDateGOEExpression(qCcsAuthmemoHst.logOlTime,
//		// d));
//		// System.out.println(Test.getDateLTExpression(qCcsAuthmemoHst.logOlTime,
//		// d));
//		// System.out.println(Test.getDateLOEExpression(qCcsAuthmemoHst.logOlTime,
//		// d));
//		// Pair<Date, Date> pair = new Pair<Date, Date>(new Date(), new Date());
//		Date d = new Date();
//
//		System.out.println(CpsExpressionTools.getDateGTExpression(qCcsAuthmemoHst.logOlTime,
//				d));
//		System.out.println(CpsExpressionTools.getDateGOEExpression(qCcsAuthmemoHst.logOlTime,
//				d));
//		System.out.println(CpsExpressionTools.getDateLTExpression(qCcsAuthmemoHst.logOlTime,
//				d));
//		System.out.println(CpsExpressionTools.getDateLOEExpression(qCcsAuthmemoHst.logOlTime,
//				d));
//		 System.out.println(inDatePeriod(qCcsAuthmemoHst.logOlTime, pair));
//	}

}
