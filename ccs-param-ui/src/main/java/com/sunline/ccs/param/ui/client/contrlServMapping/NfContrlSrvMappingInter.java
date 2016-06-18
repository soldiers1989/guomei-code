package com.sunline.ccs.param.ui.client.contrlServMapping;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.param.def.NfContrlServMapping;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * 非金融服务控制约束服务器接口
* @author fanghj
 *
 */
@RemoteServiceRelativePath("rpc/nfContrlSvrMappingServer")
public interface NfContrlSrvMappingInter extends RemoteService{

	
	/**
	 * 增加非金融控制约束
	 * @param contrlServMapping
	 */
	void saveContrlSerrMapping(NfContrlServMapping contrlServMapping) throws ProcessException;
	
	/**
	 * 更新非金融控制约束
	 * @param contrlServMapping
	 */
	void updateContrlSerrMapping(NfContrlServMapping contrlServMapping) throws ProcessException;
	
	/**
	 * 获取非金融控制页面要素集合
	 * @return
	 */
	List getNfContrlSvrMapping(String srvCode) throws ProcessException;
	
	
//	FetchResponse getNfContrSrvMappingList(FetchRequest fetchRequest);
	
	void deleteNfContrlSrvMapping(List<String> keys) throws ProcessException;
	
	/**
	 * 获取机构下的非金融服务控制约束
	 * @param fetchRequest
	 * @return
	 */
	Map<String, Serializable> getNfContrSrvMappingList(String servCode) throws ProcessException;
	

}
