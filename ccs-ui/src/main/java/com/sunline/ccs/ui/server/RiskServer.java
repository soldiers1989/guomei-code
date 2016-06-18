/**
 * 
 */
package com.sunline.ccs.ui.server;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsOpPrivilege;
import com.sunline.ccs.infrastructure.shared.model.CcsOpPrivilege;
import com.sunline.ccs.infrastructure.shared.model.CcsOpPrivilegeKey;
import com.sunline.ccs.param.def.Organization;
import com.sunline.kylin.web.core.client.exception.FlatException;


/**
 * 操作员权限控制
* @author fanghj
 *
 */
@Controller
@RequestMapping(value = "/riskServer")
public class RiskServer{
	private static final long serialVersionUID = 1L;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private RCcsOpPrivilege rTmOperAuth;
	
	//TODO nova改造 暂时注释
//	@Autowired
//	private SecurityService securityService;

	@Autowired
	private OpeLogUtil opeLogUtil;

	@Autowired
	private UnifiedParamFacilityProvide unifiedparameterfacilityprovide;
	
	@ResponseBody()
	@RequestMapping(value = "/getOperatorInfo", method = {RequestMethod.POST})
	public CcsOpPrivilege getOperatorInfo(@RequestBody String operatorId) throws FlatException {
		if(StringUtils.isEmpty(operatorId)) {
			throw new FlatException("操作员号不能为空");
		}
		
		CcsOpPrivilegeKey key = new CcsOpPrivilegeKey();
		key.setOrg(OrganizationContextHolder.getCurrentOrg());
		key.setOpId(operatorId);
		
		//判断操作员号是否合法
//		checkInputOperator(operatorId);
		
		//查找操作员权限记录
		CcsOpPrivilege tmOperAuth = rTmOperAuth.findOne(key);
		
		if(null == tmOperAuth) {
			//未找到操作员，则新增并赋初值
			tmOperAuth = new CcsOpPrivilege();
			tmOperAuth.setOrg(key.getOrg());
			tmOperAuth.setOpId(key.getOpId());
			tmOperAuth.setMaxLmtAdj(BigDecimal.ZERO);
			tmOperAuth.setMaxPointsAdj(BigDecimal.ZERO);
			tmOperAuth.setMaxAcctTxnAdj(BigDecimal.ZERO);
			tmOperAuth.setMaxCashloanAdj(BigDecimal.ZERO);
			tmOperAuth.setMaxLoanApproveAdj(BigDecimal.ZERO);
			tmOperAuth.setMaxPosloanAdj(BigDecimal.ZERO);
		}
		
		return tmOperAuth;
	}

	@Transactional
	@ResponseBody()
	@RequestMapping(value = "/saveOperatorAuth", method = {RequestMethod.POST})
	public void saveOperatorAuth(@RequestBody CcsOpPrivilege authMap) throws FlatException {
		String operatorId = authMap.getOpId();
		String org = OrganizationContextHolder.getCurrentOrg();
		if(OrganizationContextHolder.getUsername().equals(authMap.getOpId()))
			throw new FlatException("不允许操作本人权限!");
		logger.info("saveOperatorAuth: org["+org+"], operatorId ["+operatorId+"]");
		
//		if(authMap.containsKey(CcsOpPrivilege.P_Org)) {
//			authMap.remove(CcsOpPrivilege.P_Org);
//		}
		
		if(StringUtils.isEmpty(operatorId)) {
			throw new FlatException("操作员号不能为空");
		}
		
		CcsOpPrivilegeKey key = new CcsOpPrivilegeKey();
		key.setOrg(org);
		key.setOpId(operatorId);

		//判断操作员号是否合法
		checkInputOperator(operatorId);
		
		//查找操作员权限记录
		CcsOpPrivilege tmOperAuth = rTmOperAuth.findOne(key);
		if(null == tmOperAuth) { //未找到操作员权限记录则新增
			tmOperAuth = new CcsOpPrivilege();
			tmOperAuth.setOpUpdateId(OrganizationContextHolder.getUsername());
			tmOperAuth.setOrg(org);
			tmOperAuth.setOpId(authMap.getOpId());
			tmOperAuth.setOpTime(new Date());
			tmOperAuth.fillDefaultValues();
			rTmOperAuth.save(tmOperAuth);
		}
			//记录修改日志
			opeLogUtil.safetyControlLog("risk", operatorId, "操作员权限设置[新增]");
			
			//记录修改日志
			opeLogUtil.safetyControlLog("risk", operatorId, "操作员权限设置[修改]");
			//信用额度调整上限
			BigDecimal adjCreditLimitMaxNew = authMap.getMaxLmtAdj();
			BigDecimal adjPointMaxNew = authMap.getMaxPointsAdj();
			//账务调整上限
			BigDecimal adjTxnAmtMaxNew = authMap.getMaxAcctTxnAdj();
			BigDecimal adjCashLoanMaxNew = authMap.getMaxCashloanAdj();
			BigDecimal adjLoanParamMaxNew = authMap.getMaxLoanApproveAdj();
			//校验被调操作员额度调整上限是否超过登录用户的可调信用额度上限
			CcsOpPrivilegeKey key2 = new CcsOpPrivilegeKey();
			key2.setOrg(OrganizationContextHolder.getCurrentOrg());
			key2.setOpId(OrganizationContextHolder.getUsername());
			
			CcsOpPrivilege tmOperAuth2 = rTmOperAuth.findOne(key2);
			if(tmOperAuth2 == null){
				throw new FlatException("当前登录操作员无权限设置");
			}
			if(tmOperAuth2.getMaxLmtAdj() == null){
				throw new FlatException("登录操作员的信用额度调整上限值不能为空");
			}
			if(tmOperAuth2.getMaxLmtAdj().compareTo(adjCreditLimitMaxNew) < 0){
				throw new FlatException("信用额度调整上限应小于登录操作员最大信用额度调整上限");
			}
			//校验机构信用额度调整上限
			Organization organization = unifiedparameterfacilityprovide.organization();
		    if(adjCreditLimitMaxNew.compareTo(organization.maxCreditLimit == null ? BigDecimal.ZERO : organization.maxCreditLimit) > 0){
				throw new FlatException("信用额度调整上限应小于机构最大信用额度调整上限");	
			}
		    
		    if(adjCashLoanMaxNew.compareTo(organization.maxCreditLimit == null ? BigDecimal.ZERO : organization.maxCreditLimit) > 0){
		    	throw new FlatException("分期调整金额上限应小于机构最大信用额度调整上限");
		    }
		  
		    String logStr = null;
			
			logStr = opeLogUtil.makeLogStr(adjCreditLimitMaxNew, tmOperAuth.getMaxLmtAdj(), CcsOpPrivilege.P_MaxLmtAdj);
			if(logStr != null) {
				opeLogUtil.safetyControlLog("risk", operatorId, logStr);
			}
			
			logStr = opeLogUtil.makeLogStr(adjPointMaxNew, tmOperAuth.getMaxPointsAdj(), CcsOpPrivilege.P_MaxPointsAdj);
			if(logStr != null) {
				opeLogUtil.safetyControlLog("risk", operatorId, logStr);
			}
			
			logStr = opeLogUtil.makeLogStr(adjTxnAmtMaxNew, tmOperAuth.getMaxAcctTxnAdj(), CcsOpPrivilege.P_MaxAcctTxnAdj);
			if(logStr != null) {
				opeLogUtil.safetyControlLog("risk", operatorId, logStr);
			}
			
			logStr = opeLogUtil.makeLogStr(adjCashLoanMaxNew, tmOperAuth.getMaxCashloanAdj(), CcsOpPrivilege.P_MaxCashloanAdj);
			if(logStr != null) {
				opeLogUtil.safetyControlLog("risk", operatorId, logStr);
			}
			
			logStr = opeLogUtil.makeLogStr(adjLoanParamMaxNew, tmOperAuth.getMaxLoanApproveAdj(), CcsOpPrivilege.P_MaxLoanApproveAdj);
			if(logStr != null){
				opeLogUtil.safetyControlLog("risk", operatorId, logStr);
			}
			
			tmOperAuth.setMaxAcctTxnAdj(adjTxnAmtMaxNew);
			tmOperAuth.setMaxLmtAdj(adjCreditLimitMaxNew);
			tmOperAuth.setMaxPointsAdj(adjPointMaxNew);
			tmOperAuth.setMaxCashloanAdj(adjCashLoanMaxNew);
			tmOperAuth.setMaxLoanApproveAdj(adjLoanParamMaxNew);
			tmOperAuth.setOpUpdateId(OrganizationContextHolder.getUsername());
			tmOperAuth.setOpId(authMap.getOpId());
			tmOperAuth.setOpTime(new Date());
			rTmOperAuth.saveAndFlush(tmOperAuth);
	}
	
	
	
	/**
	 * 调用远程服务接口判断操作员号是否合法
	 * @param operatorId	操作员号
	 * @throws FlatException
	 */
	private void checkInputOperator(@RequestBody String operatorId) throws FlatException {
		//调用远程服务接口判断操作员号是否合法
//		try {
//			boolean existed = securityService.checkUser(OrganizationContextHolder.getCurrentOrg(), operatorId);
//			if(!existed) {
//				throw new FlatException("该操作员号["+operatorId+"]不存在");
//			}
//		} catch(Exception caught) {
//			throw new FlatException(caught.getLocalizedMessage());
//		}
	}

}
