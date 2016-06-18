package com.sunline.ccs.batch.common;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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
 * 轮询等待ftp扣款回盘文件
 * @author zhangqiang
 *
 */
public class FTPFileWaitingTask implements Tasklet {
	
	private static final Logger logger = LoggerFactory.getLogger(FTPFileWaitingTask.class);

	@Value("#{env['batchFilePollInterval']?:5000}")
	private int pollInterval;
	
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	// 注入
	private FTPClientOperations ftpClientOperations;
	
	private String respRemotePath;
	
	// 批量代付回盘文件目录
	private String disburseRespRemotePath;
	
	private String payAccountFileRespRmPath;
	
	private String cutAccountFileRespRmPath;
	
	private String merchantFileRespRmPath;
	
	@Value("#{env.extensionFileName}")
	private String extensionFileName;
	
	@Value("#{env.validateFileName}")
	private String validateFileName;
	
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		
		logger.info("开始检查输入文件状态");
		List<String> waitFileNames = setWaitFileNames();
		
		List<String> existFileNames = new ArrayList<String>();
		if(respRemotePath!=null)
			existFileNames.addAll(ftpClientOperations.listFiles(respRemotePath));
		if(disburseRespRemotePath!=null)
			existFileNames.addAll(ftpClientOperations.listFiles(disburseRespRemotePath));
		if(payAccountFileRespRmPath!=null)
			existFileNames.addAll(ftpClientOperations.listFiles(payAccountFileRespRmPath));
		if(cutAccountFileRespRmPath!=null)
			existFileNames.addAll(ftpClientOperations.listFiles(cutAccountFileRespRmPath));
		if(merchantFileRespRmPath!=null)
			existFileNames.addAll(ftpClientOperations.listFiles(merchantFileRespRmPath));
		
		List<String> notReady = new ArrayList<String>();
		
		for (String fileName : waitFileNames){
			if (!existFileNames.contains(fileName)){
				if (logger.isDebugEnabled())
					logger.debug("文件[{}]还不存在。", fileName);
				notReady.add(fileName);
			}
		}

		if (notReady.isEmpty()){
			logger.debug("文件都已存在，开始运行批量步骤");
			return RepeatStatus.FINISHED;
		}

		//暂停一会儿
		try{
			logger.info("轮询等待[{}]秒", pollInterval/1000);
			Thread.sleep(pollInterval);
		}catch (InterruptedException e){
			//被中断了也无所谓
		}
		return RepeatStatus.CONTINUABLE;
	}

	protected List<String> setWaitFileNames() {
		List<String> waitFileNames = new ArrayList<String>();
		
		String batchDateStr = new SimpleDateFormat("yyyyMMdd").format(batchStatusFacility.getBatchDate());
		//回盘文件
		if(respRemotePath!=null){
			waitFileNames.add(respRemotePath + "/" + "10-" + batchDateStr + extensionFileName);
			waitFileNames.add(respRemotePath + "/" + "10-" + batchDateStr + validateFileName);
		//调额文件--不等待
//			waitFileNames.add(respRemotePath + "/" + "limitajust-" + batchDateStr + extensionFileName);
//			waitFileNames.add(respRemotePath + "/" + "limitajust-" + batchDateStr + validateFileName);
		}
		// 批量代付回盘文件
		if(disburseRespRemotePath!=null){
			waitFileNames.add(disburseRespRemotePath + "/" + "10-" + batchDateStr + extensionFileName);
			waitFileNames.add(disburseRespRemotePath + "/" + "10-" + batchDateStr + validateFileName);
		}
		//对账文件
		if(payAccountFileRespRmPath!=null){
			waitFileNames.add(payAccountFileRespRmPath + "/" + "11-" + batchDateStr + extensionFileName);
			waitFileNames.add(payAccountFileRespRmPath + "/" + "11-" + batchDateStr + validateFileName);
		}
		if(cutAccountFileRespRmPath!=null){
			waitFileNames.add(cutAccountFileRespRmPath + "/" + "11-" + batchDateStr + extensionFileName);
			waitFileNames.add(cutAccountFileRespRmPath + "/" + "11-" + batchDateStr + validateFileName);
		}
		//商户对账文件
		if(merchantFileRespRmPath!=null){
			waitFileNames.add(merchantFileRespRmPath + "/" + "transflow-" + batchDateStr + extensionFileName);
			waitFileNames.add(merchantFileRespRmPath + "/" + "transflow-" + batchDateStr + validateFileName);
		}
		return waitFileNames;
	}

	public void setFtpClientOperations(FTPClientOperations ftpClientOperations) {
		this.ftpClientOperations = ftpClientOperations;
	}
	
	public void setRespRemotePath(String respRemotePath) {
		this.respRemotePath = respRemotePath;
	}

	public void setPayAccountFileRespRmPath(String payAccountFileRespRmPath) {
		this.payAccountFileRespRmPath = payAccountFileRespRmPath;
	}

	public void setCutAccountFileRespRmPath(String cutAccountFileRespRmPath) {
		this.cutAccountFileRespRmPath = cutAccountFileRespRmPath;
	}

	public void setMerchantFileRespRmPath(String merchantFileRespRmPath) {
		this.merchantFileRespRmPath = merchantFileRespRmPath;
	}

	public void setDisburseRespRemotePath(String disburseRespRemotePath) {
		this.disburseRespRemotePath = disburseRespRemotePath;
	}
	
}
