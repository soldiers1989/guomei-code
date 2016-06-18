package com.sunline.ccs.batch.rpt.cca290;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.sunline.ark.batch.FTPClientOperations;
import com.sunline.ccs.batch.rpt.common.FileTools;

public class OA291SendYGFile implements Tasklet {
	private Logger logger  = LoggerFactory.getLogger(this.getClass());
	private FTPClientOperations ftpOperations;
	
	@Value("#{env.batchWorkDir}")
	private String batchWorkDir;
	
	@Value("#{env.ygFtpRemoteFilePath}")
	private String remotePath;
//	
	
	@Autowired
	FileTools fileTools;
	@Override
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {
		
		String uploadFileAbsltPath = batchWorkDir + File.separator + fileTools.genBatchDatePrefix() + File.separator + "ygUpload" + File.separator;
		String uploadingAbsltPath = batchWorkDir + File.separator + fileTools.genBatchDatePrefix() + File.separator + "ygUploading" + File.separator;
		
		//copy阳光文件到上传工作目录，加上时间前缀
		logger.info("目录[{}]文件拷贝到目录[{}]，文件名加时间前缀", uploadFileAbsltPath, uploadingAbsltPath);
		fileTools.copyDirFiles(
				uploadFileAbsltPath, 
				uploadingAbsltPath,
				fileTools.genBatchDatePrefix(),  true);
//		上传文件
		logger.info("上传远程路径:[{}]", remotePath); 
		ftpOperations.uploadFiles(uploadingAbsltPath, remotePath + File.separator);
		
		fileTools.copyDirFiles(uploadFileAbsltPath, batchWorkDir + File.separator + fileTools.genBatchDatePrefix() + File.separator, null, true);
		fileTools.copyDirFiles(uploadingAbsltPath, batchWorkDir + File.separator + fileTools.genBatchDatePrefix() + File.separator, null, true);
		
		return RepeatStatus.FINISHED;
	}

	public FTPClientOperations getFtpOperations() {
		return ftpOperations;
	}

	public void setFtpOperations(FTPClientOperations ftpOperations) {
		this.ftpOperations = ftpOperations;
	}

}
