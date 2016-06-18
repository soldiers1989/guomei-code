package com.sunline.ccs.service.auth.frame;

import com.sunline.ark.support.service.YakMessage;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.ccs.service.auth.context.RespInfo;

/**
 * 
 * @see 类名：Handler
 * @see 描述：授权交易业务处理接口
 *
 * @see 创建日期：   2015年6月24日下午3:24:55
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public interface Handler
{
	RespInfo handle(YakMessage request, AuthContext context) throws AuthException;
}
