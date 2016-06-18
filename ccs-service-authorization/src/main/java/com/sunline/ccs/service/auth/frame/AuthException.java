package com.sunline.ccs.service.auth.frame;

import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.ccs.param.def.enums.AuthReason;

/**
 * 
 * @see 类名：AuthException
 * @see 描述：正常授权流程中出现异常
 *
 * @see 创建日期：   2015年6月24日下午3:24:30
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@SuppressWarnings("serial")
public class AuthException extends RuntimeException {
	private AuthReason reason;

	private  AuthAction  action;
	
	private boolean fillDefaultAction = false;

	public AuthException(AuthReason reason,  boolean fillDefaultAction) {
		this.reason = reason;
		this.fillDefaultAction = fillDefaultAction;
	}
	
	public AuthException(AuthReason reason,  AuthAction  action) {
		this.reason = reason;
		this.action = action;
	}

	public AuthException(AuthReason reason,  AuthAction  action, String message) {
		super(message);
		this.reason = reason;
		this.action = action;
	}

	/**
	 * 发生原因为 reason，授权行为取默认值
	 */
	public AuthException(AuthReason reason) {
		this(reason, null);
	}

	public AuthReason getReason() {
		return reason;
	}

	public  AuthAction  getAction() {
		return action;
	}
	
	public boolean getFillDefaultAction() {
		return fillDefaultAction;
	}
}
