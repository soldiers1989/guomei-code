package com.sunline.ccs.service.handler.collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ark.support.serializable.JsonSerializeUtil;
import com.sunline.ppy.api.MsCSNoticeSendService;
import com.sunline.ppy.dictionary.enums.Indicator;
/**
 * 催收扣款通知发送
 * @author zhengjf
 *
 */
@Service
public class MsCSNoticeFacility {


	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private MsCSNoticeSendService msCSNoticeSendService;
	
	public void sendCSNotice(CollectionReq req) {
		

		//转化报文
		String smsJson = JsonSerializeUtil.jsonSerializerNoType(req);
		try {
			logger.info("催收发送请求报文" + smsJson);
			String respJson= msCSNoticeSendService.sendCollectionNotice(smsJson, Indicator.Y);
			logger.info("催收返回应答报文"+ respJson);
		} catch (Exception e) {
			logger.error("发送催收平台异常",e);
		}
			
	}
}
