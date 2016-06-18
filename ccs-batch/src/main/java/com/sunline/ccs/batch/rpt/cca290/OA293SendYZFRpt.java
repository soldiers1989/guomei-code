package com.sunline.ccs.batch.rpt.cca290;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import com.sunline.ccs.batch.rpt.common.FileTools;
import com.sunline.ccs.ftp.FtpUtil;

public class OA293SendYZFRpt implements Tasklet  {
	private Logger logger  = LoggerFactory.getLogger(this.getClass());
	
	private FtpUtil ftpOperations;

	@Value("#{env.batchWorkDir}")
	private String batchWorkDir;
	
	@Value("#{env.YZFCashTrade}")
	private String remotePath;
	
	private List<Resource> resource;
	
	private String extensionFileName;
	
	private String extensionFileNameCtrl;
	
	public void setExtensionFileNameCtrl(String extensionFileNameCtrl) {
		this.extensionFileNameCtrl = extensionFileNameCtrl;
	}
	public void setExtensionFileName(String extensionFileName) {
		this.extensionFileName = extensionFileName;
	}
	public void setResource(List<Resource> resource) {
 		this.resource = resource;
 	}
	
	
	public FtpUtil getFtpOperations() {
		return ftpOperations;
	}
	public void setFtpOperations(FtpUtil ftpOperations) {
		this.ftpOperations = ftpOperations;
	}

	@Autowired
	FileTools fileTools;
	
	
	@Override
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {
		for (int i = 0; i < resource.size(); i++) {
			//上传txt文件
			String uploadingAbsltPath = batchWorkDir + File.separator + fileTools.genBatchDatePrefix() + File.separator + resource.get(i).getFilename()+extensionFileName;
			//copy翼支付的提款还款文件到上传工作目录，加上时间前缀
			logger.info("本地{}文件路径:[{}]",extensionFileName, uploadingAbsltPath);
			String ftpPathstr = remotePath;
			ftpPathstr = ftpPathstr + resource.get(i).getFilename()+extensionFileName;
			logger.info("上传{}远程路径:[{}]",extensionFileName, ftpPathstr); 
			
			ftpOperations.uploadAndLogoutViaSFTP(ftpPathstr, new FileInputStream(new File(uploadingAbsltPath)));
			
			//上传ctrl文件
			String uploadingAbsltPathCtrl = batchWorkDir + File.separator + fileTools.genBatchDatePrefix() + File.separator + resource.get(i).getFilename()+extensionFileNameCtrl;
			//copy翼支付的提款还款文件到上传工作目录，加上时间前缀
			logger.info("本地{}文件路径:[{}]",extensionFileNameCtrl, uploadingAbsltPathCtrl);
			String ftpPathstrCtrl = remotePath;
			ftpPathstrCtrl = ftpPathstrCtrl + resource.get(i).getFilename()+extensionFileNameCtrl;
			logger.info("上传{}远程路径:[{}]",extensionFileNameCtrl, ftpPathstrCtrl); 
			
			ftpOperations.uploadAndLogoutViaSFTP(ftpPathstrCtrl, new FileInputStream(new File(uploadingAbsltPathCtrl)));
		}
		return RepeatStatus.FINISHED;
	}
	
	

}
