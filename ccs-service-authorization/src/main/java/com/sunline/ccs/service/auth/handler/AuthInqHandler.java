package com.sunline.ccs.service.auth.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ccs.service.auth.common.AuthCommService;
import com.sunline.ccs.service.auth.context.AuthContext;
 
/**
 * 
 * @see 类名：AuthInqHandler
 * @see 描述：借记[查询]处理
 *
 * @see 创建日期：   2015年6月24日下午3:28:54
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class AuthInqHandler extends AuthHandler {
	
	@Autowired
	AuthCommService authCommonService ;

	@Override
	protected void updateUnmatchAmt(AuthContext context) {
		
	}
	
	@Override
	protected void updateStatistics(AuthContext context)
	{
		 		 
	}


}
