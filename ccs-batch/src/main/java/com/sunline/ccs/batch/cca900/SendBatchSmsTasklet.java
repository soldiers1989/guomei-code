package com.sunline.ccs.batch.cca900;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.serializable.JsonSerializeUtil;
import com.sunline.ccs.batch.cca300.MsFileRespInfo;
import com.sunline.ccs.batch.cca300.MsSmsNoticeInfo;
import com.sunline.ccs.batch.cca300.MsSmsRespInfo;
import com.sunline.ccs.batch.common.MsBatchSmsNoticeSend;
import com.sunline.ccs.infrastructure.shared.model.CcsSmsFileCtl;
import com.sunline.ppy.dictionary.enums.Indicator;

public class SendBatchSmsTasklet implements Tasklet {
	
	private static final Logger logger = LoggerFactory.getLogger(SendBatchSmsTasklet.class);
	
	// 业务类型
	private String sourceBizType = "";
	
	private String fileName;

	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	@Autowired
	private MsBatchSmsNoticeSend msBatchSmsNoticeSend;
	
	@PersistenceContext
	private EntityManager em;
	
	@Value("#{env.batchWorkDir}")
	private String batchWorkDir;
	
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		if(logger.isDebugEnabled())
			logger.debug("发送短信文件到通知平台,fileName:" + fileName);
		
		Map<String, String> params = new HashMap<String, String>();
		// 流水号 = 批量日期 + 文件名
		String serialNo = new SimpleDateFormat("yyyyMMdd").format(batchStatusFacility.getBatchDate())+ new SimpleDateFormat("hhmmss").format(new Date())+ fileName;
		params.put("serialNo", serialNo);
		params.put("bizCode", "112002");
		
		// 新建短信文件控制表,本应在生成文件时新建,但writer做不到
		CcsSmsFileCtl smsCtl = new CcsSmsFileCtl();
		smsCtl.setSourceBatchNumber(serialNo); // 使用文件流水号作为批次号
		smsCtl.setSourceBizSystem("accounting");
		smsCtl.setSourceBizType(sourceBizType);
		smsCtl.setOrignFileName(fileName);
		smsCtl.setUploadFlag(Indicator.N);
		
		try {
			logger.info("开始上传文件,fileName:" + fileName);
			// 上传文件通知平台返回的fileId
			String filePath = batchWorkDir + File.separator + new SimpleDateFormat("yyyyMMdd").format(batchStatusFacility.getBatchDate()) + File.separator + fileName;
			
			File file = new File(filePath);
			if(file.length() == 0){
				// 若文件大小为0 影像平台将返回失败 故直接跳过 也不保存短信文件控制表
				return RepeatStatus.FINISHED;
			}
			
			String fileRespJson = msBatchSmsNoticeSend.uploadSmsFile("fileData", file, params);
			logger.info("影像平台返回报文:" + fileRespJson);
			MsFileRespInfo fileRespInfo = JsonSerializeUtil.jsonReSerializerNoType(fileRespJson, MsFileRespInfo.class);
			
			if(!"0".equals(fileRespInfo.getCode())){
				logger.error("上传文件失败,message:" + fileRespInfo.getMessage());
				throw new Exception("上传文件失败," + fileRespInfo.getMessage());
			}
			
			String fileId = fileRespInfo.getFileId();
			logger.info("文件上传成功,fileId:" + fileId);
			smsCtl.setSendFileId(fileId);
			smsCtl.setUploadFlag(Indicator.Y);
			smsCtl.setUploadTime(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
			
			// 上传成功通知
			MsSmsNoticeInfo smsNoticeInfo = new MsSmsNoticeInfo();
			smsNoticeInfo.setSourceBatchNumber(serialNo);
			smsNoticeInfo.setSendFileId(fileId);
			String reqJson = JsonSerializeUtil.jsonSerializerNoType(smsNoticeInfo);
			logger.info("上传文件成功通知,请求json:" + reqJson);
			// 通知返回报文
			String respJson = msBatchSmsNoticeSend.sendSmsNotice(reqJson, Indicator.N);
			logger.info("上传文件通知成功,响应json:" + respJson);
			MsSmsRespInfo respInfo = JsonSerializeUtil.jsonReSerializerNoType(respJson, MsSmsRespInfo.class);
			// 设置信息
			smsCtl.setRespCode(respInfo.getCode());
			smsCtl.setMessage(respInfo.getMessage());
			
		} catch (Exception e) {
			//什么也不做,上传失败不断批
			logger.error("上传短信文件失败", e);
		}
		
		em.persist(smsCtl);
		
		return RepeatStatus.FINISHED;
	}

	public void setSourceBizType(String sourceBizType) {
		this.sourceBizType = sourceBizType;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
}
