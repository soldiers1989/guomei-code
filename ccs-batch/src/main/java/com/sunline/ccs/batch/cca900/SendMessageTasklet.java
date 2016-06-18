package com.sunline.ccs.batch.cca900;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.ark.support.serializable.JsonSerializeUtil;
import com.sunline.ccs.batch.common.MsBatchSmsNoticeSend;
import com.sunline.ppy.dictionary.enums.Indicator;

public class SendMessageTasklet implements Tasklet {
	
	private static final Logger logger = LoggerFactory.getLogger(SendMessageTasklet.class);
	
	@Autowired
	private MsBatchSmsNoticeSend msSmsNoticeSendService;
	
	@Override
	public RepeatStatus execute(StepContribution arg0, ChunkContext arg1) throws Exception {
		
		if(logger.isDebugEnabled()){
			logger.debug("批量完成，通知数据平台");
		}
		
		SendMessageRequest request = new SendMessageRequest();
		String reqJson = JsonSerializeUtil.jsonSerializerNoType(request);
		
		try{
			logger.info("发送通知到数据平台请求报文" + reqJson);
			String resJson = msSmsNoticeSendService.sendNoticeToBdp(reqJson, Indicator.N);
			logger.info("数据平台应答报文" + resJson);
			
			SendMessageResponse response = JsonSerializeUtil.jsonReSerializerNoType(resJson, SendMessageResponse.class);
			
			if("200".equals(response.getCode())){
				logger.info("发送成功 " + response.getMessage());
			}else{
				logger.info("发送失败" + response.getMessage());
			}
		}catch(Exception e){
			//只打印日志，再往前抛异常，保证交易正常结束
			logger.error("数据平台通知发送异常",e);
		}
        
		return RepeatStatus.FINISHED;
	}
	
}
