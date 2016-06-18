package com.sunline.ccs.batch.cc6000;

import org.springframework.batch.item.ItemProcessor;

import com.sunline.ark.support.OrganizationContextHolder;


/**
 * @see 类名：P6000SetOrganization
 * @see 描述：为下面的所有Processor设置Organization上下文
 *
 * @see 创建日期：   2015-6-24上午9:57:08
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P6000SetOrganization implements ItemProcessor<S6000AcctInfo, S6000AcctInfo> {

	@Override
	public S6000AcctInfo process(S6000AcctInfo item) throws Exception {
		OrganizationContextHolder.setCurrentOrg(item.getAccount().getOrg());
		return item;
	}
	
}
