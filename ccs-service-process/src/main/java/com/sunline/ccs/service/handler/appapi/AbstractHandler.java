package com.sunline.ccs.service.handler.appapi;

import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.service.msentity.MsRequestInfo;
import com.sunline.ccs.service.msentity.MsResponseInfo;

/**
 * 交易处理器
 * @author wangz
 *
 */
public abstract class AbstractHandler {

	/**
	 * 交易处理
	 * @param msRequestInfo
	 * @return
	 */
	public MsResponseInfo handler(MsRequestInfo msRequestInfo){
		this.setEnv(msRequestInfo);
		MsResponseInfo msResponseInfo = execute(msRequestInfo);
		return msResponseInfo;
	}
	
	/**
	 * 业务处理
	 * @param msRequestInfo 请求报文
	 * @return
	 */
	public abstract MsResponseInfo execute(MsRequestInfo msRequestInfo);
	
	
	
	/**
	 * 设置机构上下文
	 * @param req
	 */
	private void setEnv(MsRequestInfo req){
		// 获取机构ID
		String org = req.getOrg();
		// 获取操作员
		String opID = req.getOpId();

		// 放置机构上下文
		OrganizationContextHolder.setCurrentOrg(org);
		OrganizationContextHolder.setUsername(opID);
	}
}
