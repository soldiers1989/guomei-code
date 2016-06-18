package com.sunline.ccs.service.auth.frame;

import java.io.Serializable;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ark.support.service.YakMessage;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.ccs.service.auth.context.RespInfo;

/**
 * 
 * @see 类名：HandlerDispatcher
 * @see 描述：用于在main-route.xls里分发服务请求
 *
 * @see 创建日期：   2015年6月24日下午3:26:21
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@SuppressWarnings("serial")
@Service
public class HandlerDispatcher implements Serializable
{

	@Autowired
	private Map<String, Handler> processors;

	public RespInfo dispatch(String name, YakMessage request, AuthContext context) throws AuthException
	{
	 
		return 	processors.get(name).handle(request, context);
	}

}
