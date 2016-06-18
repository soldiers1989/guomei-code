package com.sunline.ccs.batch.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.Resource;

public class SimpleFileMergeTasklet implements Tasklet {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private List<Resource> sources ;
	private Resource target;
	private Boolean isEndOfNewLine = true;
	/**
	 * 1M per read
	 */
	private static final int COPY_BUFFER = 1 * 1024 * 1024;
	@Override
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {
		logger.info("合并文件开始" );
		
		File targetFile = target.getFile();
		File targetTemp = new File(targetFile.getParentFile().getAbsolutePath() + File.separator + target.getFilename() + ".temp" );
		if(!targetTemp.getParentFile().exists()){
			targetTemp.getParentFile().mkdir();
		}
		FileOutputStream fos = new FileOutputStream(targetTemp);
		for(Resource resource : sources){
			logger.info("合并文件[{}][{}]" , resource.getFile(), resource.getFile().length() );
			if(resource.getFile().length() <= 0) continue;
			
			FileInputStream fis = new FileInputStream(resource.getFile());
			byte[] buffer = new byte[COPY_BUFFER];
			IOUtils.copyLarge(fis, fos, buffer );
			if(!isEndOfNewLine){
				fos.write('\n');
			}
			IOUtils.closeQuietly(fis);
		}
		IOUtils.closeQuietly(fos);
		
		if (targetFile.exists())
		{
			logger.warn("输出文件[{}]已存在，将被删除。", targetFile);
			targetFile.delete();
		}
		boolean renameSuccess = targetTemp.renameTo(targetFile);
		if(renameSuccess) {
			logger.info("rename方法改名成功");
		} else {
			logger.info("rename方法改名失败，尝试文件复制");
			try {
				FileUtils.copyFile(targetTemp, targetFile);
			} catch (IOException e) {
				logger.error("文件复制失败，source：{}，target：{}。", targetTemp, targetFile);
				throw e;
			}
		}
		logger.info("改名成功,合并后文件[{}]" , targetFile.getAbsolutePath());
		return RepeatStatus.FINISHED;
	}
	public List<Resource> getSources() {
		return sources;
	}
	public void setSources(List<Resource> sources) {
		this.sources = sources;
	}
	public Resource getTarget() {
		return target;
	}
	public void setTarget(Resource target) {
		this.target = target;
	}
	public void setIsEndOfNewLine(Boolean isEndOfNewLine) {
		this.isEndOfNewLine = isEndOfNewLine;
	}
	
}
