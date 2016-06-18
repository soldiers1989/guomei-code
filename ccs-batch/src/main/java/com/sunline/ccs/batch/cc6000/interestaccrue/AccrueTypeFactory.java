package com.sunline.ccs.batch.cc6000.interestaccrue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.ccs.param.def.enums.IntAccumFrom;


/**
 * @see 类名：AccrueTypeFactory
 * @see 描述：根据起息类型获得不同计息对象，因天数已经提前算好，无需区分交易日计息和入账日计息
 *
 * @see 创建日期：   2015-6-24下午6:55:39
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class AccrueTypeFactory {
	@Autowired
	private StmtAccrue stmtAccrue;
	@Autowired
	private PostDateAccrue postDateAccrue;
	
	
	/**
	 * @see 方法名：getAccrue 
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：2015-6-24下午6:56:14
	 * @author ChengChun
	 *  
	 * @param intAccumFrom
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public Accrue getAccrue(IntAccumFrom intAccumFrom){
		switch(intAccumFrom){
		case C:return stmtAccrue;
		case P:;
		case T:return postDateAccrue;
		default:
			throw new IllegalArgumentException("还款计划中的起息日类型不正确");
		}
		
	}
}
