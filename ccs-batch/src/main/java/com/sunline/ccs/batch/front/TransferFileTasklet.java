package com.sunline.ccs.batch.front;

import java.io.File;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.FTPClientOperations;

/**
 * ftp上传代扣文件
 * @author zhangqiang
 *
 */
public class TransferFileTasklet implements Tasklet {
	
	private static final Logger logger = LoggerFactory.getLogger(TransferFileTasklet.class);
	
	// 配置注入ftp操作实现类
	private FTPClientOperations ftpClientOperations;
	
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	@Value("#{env.batchWorkDir}")
	private String batchWorkDir;
	
	@Value("#{env.frontRemotePath}")
	private String frontRemotePath;

	@Value("#{env.paymentFileName}")
	private String paymentFileName;
	
	// 代付文件远程目录
	@Value("#{env.disburseRemotePath}")
	private String disburseRemotePath;
	
	// 代付文件名
	@Value("#{env.disburseFileName}")
	private String disburseFileName;
	
	@Value("#{env.extensionFileName}")
	private String extensionFileName;
	
	@Value("#{env.validateFileName}")
	private String validateFileName;
	
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("开始ftp转移代扣文件");
		}
		
		String busiDateStr = new SimpleDateFormat("yyyyMMdd").format(batchStatusFacility.getSystemStatus().getBusinessDate());
		
		// 处理代扣文件
		File origPaymentDataFile = new File(batchWorkDir + File.separator + paymentFileName + extensionFileName);
		File origPaymentCtrlFile = new File(batchWorkDir + File.separator + paymentFileName + validateFileName);
		
		// 修改批量上传的代扣文件名
		String localPaymentDataFilePath = batchWorkDir + File.separator + "10-" + busiDateStr + "-" + paymentFileName + extensionFileName;
		String localPaymentCtrlFilePath = batchWorkDir + File.separator + "10-" + busiDateStr + "-" + paymentFileName + validateFileName;
		if(origPaymentDataFile.exists())
			origPaymentDataFile.renameTo(new File(localPaymentDataFilePath));
		if(origPaymentCtrlFile.exists())
			origPaymentCtrlFile.renameTo(new File(localPaymentCtrlFilePath));
		
		// 将本地代扣文件上传到服务器上
		String removePaymentDataFilePath = frontRemotePath + File.separator + "10-" + busiDateStr + extensionFileName;
		String removePaymentCtrlFilePath = frontRemotePath + File.separator + "10-" + busiDateStr + validateFileName;
		ftpClientOperations.upload(localPaymentDataFilePath, removePaymentDataFilePath);
		ftpClientOperations.upload(localPaymentCtrlFilePath, removePaymentCtrlFilePath);
		
		
		// 处理代付文件
		File origDisburseDataFile = new File(batchWorkDir + File.separator + disburseFileName + extensionFileName);
		File origDisburseCtrlFile = new File(batchWorkDir + File.separator + disburseFileName + validateFileName);
		
		// 修改批量上传的代付文件名
		String localDisburseDataFilePath = batchWorkDir + File.separator + "10-" + busiDateStr + "-" + disburseFileName + extensionFileName;
		String localDisburseCtrlFilePath = batchWorkDir + File.separator + "10-" + busiDateStr + "-" + disburseFileName + validateFileName;
		if(origDisburseDataFile.exists())
			origDisburseDataFile.renameTo(new File(localDisburseDataFilePath));
		if(origDisburseCtrlFile.exists())
			origDisburseCtrlFile.renameTo(new File(localDisburseCtrlFilePath));
		
		
		// 将本地代付文件上传到服务器上
		String removeDisburseDataFilePath = disburseRemotePath + File.separator + "10-" + busiDateStr + extensionFileName;
		String removeDisburseCtrlFilePath = disburseRemotePath + File.separator + "10-" + busiDateStr + validateFileName;
		ftpClientOperations.upload(localDisburseDataFilePath, removeDisburseDataFilePath);
		ftpClientOperations.upload(localDisburseCtrlFilePath, removeDisburseCtrlFilePath);
		
		return RepeatStatus.FINISHED;
	}

	public void setFtpClientOperations(FTPClientOperations ftpClientOperations) {
		this.ftpClientOperations = ftpClientOperations;
	}
	
}
