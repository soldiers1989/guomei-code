package com.sunline.ccs.batch.common;

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

/**
 * 备份文件
 * @author zhangqiang
 *
 */
public class BackupFileTasklet implements Tasklet {
	
	private static final Logger logger = LoggerFactory.getLogger(BackupFileTasklet.class);
	
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	@Value("#{env.batchWorkDir}")
	private String batchWorkDir;
	
	@Value("#{env.backupPath}")
	private String backupPath;
	
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("开始备份文件");
		}
		
		String batchDateStr = new SimpleDateFormat("yyyyMMdd").format(batchStatusFacility.getBatchDate());
		
		File dir = new File(batchWorkDir);
		File[] files = dir.listFiles();
		
		for(File file : files){
			if(file.isFile()){
				File newFile = new File(backupPath + File.separator + batchDateStr + File.separator + file.getName());
				newFile.getParentFile().mkdirs();
				file.renameTo(newFile);
			}
		}
		
		return RepeatStatus.FINISHED;
	}

}