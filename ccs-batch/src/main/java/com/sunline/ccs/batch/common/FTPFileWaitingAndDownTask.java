package com.sunline.ccs.batch.common;

import java.io.File;
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
 * 等待ftp ptp文件
 * @author liqui
 *
 */
public class FTPFileWaitingAndDownTask implements Tasklet {
	
	private static final Logger logger = LoggerFactory.getLogger(FTPFileWaitingAndDownTask.class);

	@Autowired
	private BatchStatusFacility batchStatusFacility;
	// 注入
	private FTPClientOperations ftpClientOperations;
	
	private String ptpFileName;
	
	@Value("#{env.batchWorkDir}")
	private String batchWorkDir;
	
	@Value("#{env.extensionFileName}")
	private String extensionFileName;
	
	@Value("#{env.validateFileName}")
	private String validateFileName;
	
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		
		logger.info("开始检查输入文件状态");
		List<String> waitFileNames = setWaitFileNames();
		
		List<String> existFileNames = new ArrayList<String>();
		existFileNames.addAll(ftpClientOperations.listFiles("/"));
		
		List<String> notReady = new ArrayList<String>();
		
		for (String fileName : waitFileNames){
			if (!existFileNames.contains(fileName)){
				if (logger.isDebugEnabled())
					logger.debug("文件[{}]还不存在。", fileName);
				notReady.add(fileName);
			}
		}

		if (notReady.isEmpty()){
			logger.debug("文件都已存在，开始下载");
			String batchDateStr = new SimpleDateFormat("yyyyMMdd").format(batchStatusFacility.getSystemStatus().getBusinessDate());
			
			ftpClientOperations.download(batchWorkDir + File.separator + batchDateStr + File.separator + ptpFileName, 
					"/" + "ptp-" + batchDateStr + extensionFileName); 
			ftpClientOperations.download(batchWorkDir + File.separator + batchDateStr + File.separator + ptpFileName + validateFileName, 
					"/" + "ptp-" + batchDateStr + validateFileName); 

		}else{
			logger.debug("文件不存在。跳过处理");
		}
		return RepeatStatus.FINISHED;
	}

	protected List<String> setWaitFileNames() {
		List<String> waitFileNames = new ArrayList<String>();
		
		String batchDateStr = new SimpleDateFormat("yyyyMMdd").format(batchStatusFacility.getSystemStatus().getBusinessDate());
		//ptp文件
		waitFileNames.add("/" + "ptp-" + batchDateStr + extensionFileName);
		waitFileNames.add("/" + "ptp-" + batchDateStr + validateFileName);
		return waitFileNames;
	}

	public void setFtpClientOperations(FTPClientOperations ftpClientOperations) {
		this.ftpClientOperations = ftpClientOperations;
	}
	public String getPtpFileName() {
		return ptpFileName;
	}

	public void setPtpFileName(String ptpFileName) {
		this.ptpFileName = ptpFileName;
	}
	
}
