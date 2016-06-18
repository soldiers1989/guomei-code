package com.sunline.ccs.ui.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;

import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ccs.ui.client.loader.AppLoaderInter;
import com.sunline.kylin.web.flat.client.data.ClientContext;


/**
 * 处理系统的登录与注销。
 * 
 * @see {@link LogoutFilter}, {@link SecurityContextLogoutHandler}
* @author fanghj
 * 
 */
@Controller
public class AppLoaderServer implements AppLoaderInter {

	private static final long serialVersionUID = 1L;
	
	@Autowired
	private GlobalManagementService globalManagementService;
	
	@Override
	public ClientContext getClientContext()
	{
		// TODO nova改造，暂时注释
		ClientContext cc = new ClientContext();
		
		//当前登录用户信息
//		BMPClientContextHelper.fillClientContext(cc);
		
		//当前业务日期
		cc.setBizDate(globalManagementService.getSystemStatus().getBusinessDate());

		//当前contextPath
//		cc.setServerContextPath(perThreadRequest.get().getContextPath());
		
		return cc;
	}
	
}
