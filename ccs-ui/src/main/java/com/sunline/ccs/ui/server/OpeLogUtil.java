/**
 * 
 */
package com.sunline.ccs.ui.server;


import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

//import com.sunline.ccs.infrastructure.server.repos.RCcsOpOperateLog;
import com.sunline.ccs.infrastructure.server.repos.RCcsOpOperateLog;
//import com.sunline.ccs.infrastructure.shared.model.CcsOpOperateLog;
import com.sunline.ccs.infrastructure.shared.model.CcsOpOperateLog;
import com.sunline.ark.support.OrganizationContextHolder;


/**
 * 记录操作日志
* @author fanghj
 *
 */
@Controller
public class OpeLogUtil {

	@Autowired
	private RCcsOpOperateLog rTmOpelog;
	
	/**
	 * 记录持卡人服务模块的操作日志
	 * @param inputSource	操作来源，如页面交易码
	 * @param custId	客户编号
	 * @param cardNo	介质卡号
	 * @param objectKey	操作对象的关键值
	 * @param opeDesc	操作内容描述
	 */
	@Transactional
	public void cardholderServiceLog(String inputSource, 
			Long custId, String cardNo, String objectKey, String opeDesc) {
		//记录操作日志
		Date now = new Date();
		
		
		CcsOpOperateLog tmOpelog = new CcsOpOperateLog();
		//操作人员分支行号暂时用机构号后四位
		tmOpelog.setBranchId(OrganizationContextHolder.getCurrentOrg().substring(OrganizationContextHolder.getCurrentOrg().length()-4,OrganizationContextHolder.getCurrentOrg().length()));
		tmOpelog.setCardNbr(cardNo);
		tmOpelog.setCustId(custId);
		tmOpelog.setOpId(OrganizationContextHolder.getUsername());
//		tmOpelog.setOperSeq(null);
		tmOpelog.setOpTime(now);
		tmOpelog.setOrg(OrganizationContextHolder.getCurrentOrg());
		tmOpelog.setRelatedDesc(opeDesc);
		tmOpelog.setRelatedKey(objectKey);
		tmOpelog.setServiceCode(inputSource);
		
		rTmOpelog.save(tmOpelog);
	}
	
	/**
	 * 记录持卡人服务模块的操作日志
	 * @param inputSource	操作来源，如页面交易码
	 * @param objectKey	操作对象的关键值
	 * @param opeDesc	操作内容描述
	 */
	public void safetyControlLog(String inputSource, String objectKey, String opeDesc) {

		Date now = new Date();
		
		//记录修改日志
		CcsOpOperateLog tmOpelog = new CcsOpOperateLog();
		tmOpelog.setBranchId("host");
		tmOpelog.setCardNbr("null");
		tmOpelog.setCustId(Long.parseLong("0"));
		tmOpelog.setOpId(OrganizationContextHolder.getUsername());
		tmOpelog.setOpSeq(null);
		tmOpelog.setOpTime(now);
		tmOpelog.setOrg(OrganizationContextHolder.getCurrentOrg());
		tmOpelog.setRelatedDesc(opeDesc);
		tmOpelog.setRelatedKey(objectKey);
		tmOpelog.setServiceCode(inputSource);
		
		rTmOpelog.save(tmOpelog);
	}
	
	/**
	 * 比较修改字段的新老数据：
	 * 如果两者不同则返回用于记录日志的字符串，格式为new=[$], old=[$]；
	 * 如果新老数据相同则返回null.
	 * @param newValue	修改后的新值
	 * @param oldValue	修改前的旧值
	 * @return
	 */
	public <T extends Comparable<T>> String makeLogStr(T newValue, T oldValue, String fieldName) {
		if(null == newValue && null == oldValue) {
			return null;
		}
		if(null != newValue && null != oldValue && newValue.compareTo(oldValue) == 0) {
			return null;
		}
		
		String logStr = null;
		
		if(null == newValue) {
			logStr = "new=[], ";
		} else {
			logStr = "new=["+newValue+"], ";
		}
		
		if(null == oldValue) {
			logStr += "old=[]";
		} else {
			logStr += "old=["+oldValue+"]";
		}
		
		return fieldName + ": " + logStr;
	}
	
}
