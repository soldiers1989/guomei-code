/**
 * 
 */
package com.sunline.ccs.ui.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



//import com.sunline.ccs.infrastructure.server.repos.RCcsOpPrivilege;
import com.sunline.ccs.infrastructure.server.repos.RCcsOpPrivilege;
//import com.sunline.ccs.infrastructure.shared.model.CcsOpPrivilege;
import com.sunline.ccs.infrastructure.shared.model.CcsOpPrivilege;
//import com.sunline.ccs.infrastructure.shared.model.CcsOpPrivilegeKey;
//找不到对应关系
import com.sunline.ccs.infrastructure.shared.model.CcsOpPrivilegeKey;
import com.sunline.ccs.infrastructure.shared.model.QCcsOpPrivilege;
import com.sunline.ark.support.OrganizationContextHolder;

/**
 * 操作员权限公共类
* @author fanghj
 *
 */
@Component
public class OperatorAuthUtil {
	@Autowired
	private RCcsOpPrivilege rTmOperAuth;
	private QCcsOpPrivilege qCcsOpPrivilege = QCcsOpPrivilege.ccsOpPrivilege;
	
	public CcsOpPrivilege getTmOperAuth(String operatorId) {
		CcsOpPrivilegeKey key = new CcsOpPrivilegeKey();
		key.setOrg(OrganizationContextHolder.getCurrentOrg());
		key.setOpId(operatorId);
		
		return rTmOperAuth.findOne(key);
	}
	
	/**
	 * 获取当前操作员的操作权限
	 * @return
	 */
	public CcsOpPrivilege getCurrentOperatorAuth() {
		CcsOpPrivilegeKey key = new CcsOpPrivilegeKey();
		key.setOrg(OrganizationContextHolder.getCurrentOrg());
		key.setOpId(OrganizationContextHolder.getUsername());
		
		return rTmOperAuth.findOne(qCcsOpPrivilege.org.eq(OrganizationContextHolder.getCurrentOrg())
				.and(qCcsOpPrivilege.opId.eq(OrganizationContextHolder.getUsername())));
	}
	
}
