package com.sunline.ccs.batch.common;

import java.io.File;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sunline.ccs.http.HttpClient;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.exception.ProcessException;

@Service
public class MsBatchSmsNoticeSend{

	private static final Logger logger = LoggerFactory.getLogger(MsBatchSmsNoticeSend.class);
	
	@Autowired
	HttpClient httpClient;
	
	/**
	 * 上传短信文件到通知平台url
	 */
	@Value("#{env['msBatSmsPlatformUrl']}")
	private String msBatSmsPlatformUrl;
	
	/**
	 * 上传完短信文件后发消息到通知平台url
	 */
	@Value("#{env['msNoticeSmsPlatformUrl']}")
	private String msNoticeSmsPlatformUrl;

	/**
	 * 批后发送通知到数据平台url
	 */
	@Value("#{env['msBdpUrl']}")
	private String msBdpUrl;
	
	/**
	 * 报文字符集
	 */
	@Value("#{env['payCharset']?:'utf-8'}")
	private String payCharset;

	/**
	 * 批后发送通知到数据平台
	 * @param json
	 * @throws ProcessException
	 */
	public String sendNoticeToBdp(String json, Indicator isHttpGet) throws ProcessException {
		
		if(logger.isDebugEnabled())
    		logger.debug("批后发送通知到数据平台");
		
		if(isHttpGet == Indicator.Y){
			
			return httpClient.sendMsHttpGet(json,msBdpUrl,payCharset);

		}else{
			
			return httpClient.sendMsHttpPost(json,msBdpUrl,payCharset);
		}
	}
	
	/**
	 * 上传短信文件到通知平台
	 * @param fileBodyName 
	 * @param file
	 * @param params
	 * @return
	 */
	public String uploadSmsFile(String fileBodyName, File file, Map<String, String> params){
		
		if(logger.isDebugEnabled())
			logger.debug("发送批量短信文件到通知平台");
		
		return httpClient.uploadWithPost(msBatSmsPlatformUrl, fileBodyName, file, params, payCharset);
	}

	/**
	 * 通知短信文件已上传
	 * @param json
	 * @param isHttpGet
	 * @return
	 * @throws ProcessException
	 */
	public String sendSmsNotice(String json, Indicator isHttpGet) throws ProcessException {
		if(logger.isDebugEnabled())
    		logger.debug("发送短信文件后发送通知到通知平台");
		
		if(isHttpGet == Indicator.Y){
			
			return httpClient.sendMsHttpGet(json,msNoticeSmsPlatformUrl,payCharset);

		}else{
			
			return httpClient.sendMsHttpPost(json,msNoticeSmsPlatformUrl,payCharset);
		}
	}
	
}
