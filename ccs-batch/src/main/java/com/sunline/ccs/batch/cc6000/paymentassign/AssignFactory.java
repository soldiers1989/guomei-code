package com.sunline.ccs.batch.cc6000.paymentassign;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.ppy.dictionary.enums.PlanType;


/**
 * @see 类名：AssignFactory
 * @see 描述：根据信用计划类型获得对应的还款处理对象
 *
 * @see 创建日期：   2015-6-24下午6:41:54
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class AssignFactory {
	@Autowired
	private AssignPlanP p;
	@Autowired
	private AssignPlanQ q;
	@Autowired
	private AssignPlanJ j;
	@Autowired
	private AssignPlanL l;
	@Autowired
	private AssignDefault d;
	
	/**
	 * @see 方法名：getPaymentAssign 
	 * @see 描述：根据信用计划类型获得对应的还款分配对象
	 * @see 创建日期：2015-6-24下午6:42:07
	 * @author ChengChun
	 *  
	 * @param planType
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public Assign getPaymentAssign(PlanType planType){
		switch(planType){
		case P:return p;
		case Q:return q;
		case J:return j;
		case L:return l;
		default: return d;
		}
	}
}
