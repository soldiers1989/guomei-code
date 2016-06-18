package com.sunline.ccs.facility.SMSService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.serializable.JsonSerializeUtil;
import com.sunline.ccs.facility.contract.AcctOTBCal;
import com.sunline.ccs.infrastructure.server.repos.RCcsSmsSendCtl;
import com.sunline.ccs.infrastructure.shared.model.CcsSmsSendCtl;
import com.sunline.ccs.service.msdentity.SMSentitySendResp;
import com.sunline.ccs.service.msdentity.SMSentitySendReq;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.api.MsSmsNoticeSendService;
import com.sunline.ppy.dictionary.enums.Indicator;
/**
 * 短信服务
 * @author zhengjf
 *
 */
@Service
public class MsgFacility {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private MsSmsNoticeSendService msSmsNoticeSendService;
    @Autowired
    private AcctOTBCal acctOTBCal;
    @Autowired
    private RCcsSmsSendCtl rCcsSmsSendCtl;
    @Autowired
    private UnifiedParameterFacility unifiedParameterFacility;
    @Autowired
    private GlobalManagementService globalManagementService;
    @PersistenceContext
	private EntityManager em;
	/**
	 * 发送短信
	 * @param org smsInfo
	 */
	public void sendSingleSms(String org,SMSentitySendReq req) {
		
		//保存发送报文
		CcsSmsSendCtl ctl=saveData(org, req);
		
		//转化报文
		String smsJson = JsonSerializeUtil.jsonSerializerNoType(req);
		
		logger.info("短信发送请求报文" + smsJson);
		String respJson = msSmsNoticeSendService.sendSingleSms(smsJson, Indicator.N);
		logger.info("短信返回应答报文"+ respJson);
		updateSMSSend(ctl,respJson);
	}
	/**
	 * 保存数据库
	 * @param org
	 * @param smsInfo
	 * @param respJson
	 */
	@Transactional
	private CcsSmsSendCtl saveData(String org,SMSentitySendReq req) {
		CcsSmsSendCtl ctl = new CcsSmsSendCtl();
		
		ctl.setMobileNumber(req.getMobileNumber());
		ctl.setOptUser(org);
		ctl.setSendTime(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
		ctl.setSourceBizSystem(req.getSourceBizSystem());
		ctl.setSourceSerialNumber(req.getSourceSerialNumber());
		ctl.setSourceBizType(req.getSourceBizType());
		ctl.setMsgParam(req.getMsgParam());
		
		
		return rCcsSmsSendCtl.save(ctl);
	}
	/**
	 * 更新短信发送数据库
	 * @param respJson
	 */
	@Transactional
	public void updateSMSSend(CcsSmsSendCtl ctl,String respJson) {
		if (respJson==null) {
			ctl.setRespCode("999999999");
			ctl.setMessage("没有返回数据");
		}else{
			SMSentitySendResp resp=JsonSerializeUtil.jsonReSerializerNoType(respJson,SMSentitySendResp.class);
			ctl.setRespCode(resp.getCode()==null?null:resp.getCode());
			ctl.setMessage(resp.getMessage()==null?null:resp.getMessage());
			ctl.setData(resp.getData()==null?null:resp.getData());
		}
		rCcsSmsSendCtl.save(ctl);
	}
		
	/**
	 * 组装业务类型 及短信模版
	 * @param smsInfo
	 * @param params
	 * @return
	 */
	public SMSentitySendReq getSMSInfo(SMSentitySendReq req,Map<Integer, Object> params) {
		String msgContent = null;

		for (int i = 1 ;i <= params.size()-1 ; i++ ){
			if (params.get(i) == null)
				logger.warn("短信内容存在null");
			
			if(msgContent == null){
				msgContent = params.get(i).toString();
			}else{
				msgContent = msgContent + "|" + params.get(i);
			}
		}
		if (params.get(0)!=null) {
			//业务类型
			req.setSourceBizType(params.get(0).toString());
		}
		req.setMsgParam(msgContent.toString());
		return req;
	}
}
