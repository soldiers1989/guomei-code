package com.sunline.ccs.param.ui.client.ServerDef;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sunline.ccs.param.def.NfServiceDef;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.ppy.dictionary.exception.ProcessException;
/*
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.param.def.NfServiceDef;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
*/
@RemoteServiceRelativePath("rpc/nfServiceDefServer")
public interface NfServiceDefInter extends RemoteService{
	
	/**
	 * 获取非金融服务定义参数列表
	 * @param request
	 * @return
	 */
	FetchResponse getNfServiceDefList(FetchRequest request);
	
	/**
	 * 增加非金融服务定义
	 * @param nfServiceDef
	 * @param callback
	 */
	void addNfServiceDef(NfServiceDef nfServiceDef) throws ProcessException;
	
	/**
	 * 获取非金融服务定义明细
	 * @param svrCode
	 * @throws ProcessException
	 */
	NfServiceDef getNfServiceDef(String svrCode) throws ProcessException;

	/**
	 * 根据服务编号修改非金融服务定义
	 * @param nfServiceDef
	 * @throws ProcessException
	 */
	void updateNfServiceDef(NfServiceDef nfServiceDef) throws ProcessException;
	
	/**
	 * 删除非金融服务定义
	 * @param keys
	 * @throws ProcessException
	 */
	void deleteNfServiceDef(List<String> keys) throws ProcessException;

}
