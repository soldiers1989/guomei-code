package com.sunline.ccs.batch.cca400;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.FTPClientOperations;

public class OA409SendSettleRpt implements Tasklet {
	private Logger logger  = LoggerFactory.getLogger(this.getClass());
	private FTPClientOperations ftpOperations;

	@Value("#{env.batchWorkDir}")
	private String batchWorkDir;
	@Autowired
	private BatchStatusFacility batchStatus;
	
	private Map<String, List<Resource>> filePaths;
	private String extensionFileName;
	private String validateFileName;
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	
	@Override
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {
		
		for(String remotePath : filePaths.keySet()){
			List<Resource> resources = filePaths.get(remotePath);
			for(Resource localFile : resources){
				//上传文件
				logger.info("上传文件[{}]到FTP路径[{}]", localFile.getFile().getAbsoluteFile() + extensionFileName, remotePath); 
				ftpOperations.upload(localFile.getFile().getAbsolutePath() + extensionFileName , 
						remotePath + File.separator + sdf.format(batchStatus.getBatchDate()) + localFile.getFilename() + extensionFileName);
				logger.info("上传文件[{}]到FTP路径[{}]", localFile.getFile().getAbsoluteFile() + validateFileName, remotePath); 
				ftpOperations.upload(localFile.getFile().getAbsolutePath() + validateFileName , 
						remotePath + File.separator + sdf.format(batchStatus.getBatchDate()) + localFile.getFilename() + validateFileName);
			}
		}
		
		return RepeatStatus.FINISHED;
	}

	public void setFtpOperations(FTPClientOperations ftpOperations) {
		this.ftpOperations = ftpOperations;
	}

	public void setFilePaths(Map<String, List<Resource>> filePaths) {
		this.filePaths = filePaths;
	}

	public void setExtensionFileName(String extensionFileName) {
		this.extensionFileName = extensionFileName;
	}
	public void setValidateFileName(String validateFileName) {
		this.validateFileName = validateFileName;
	}

}
