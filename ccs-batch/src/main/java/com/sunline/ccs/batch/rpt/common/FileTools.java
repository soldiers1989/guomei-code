package com.sunline.ccs.batch.rpt.common;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.acm.service.sdk.BatchStatusFacility;


@Component
public class FileTools {
	
	@Autowired
	BatchStatusFacility batchStatusFacility;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public void copyDirFiles(String fromPath, String toPath, Boolean delOriginalFile) throws IOException{
		copyDirFiles(fromPath, toPath, null, delOriginalFile);
	}
	
	/**
	 * 批量拷贝一个目录下所有文件，文件名加上前缀
	 * @param fromPath
	 * @param toPath 
	 * @param addPrefix 前缀
	 * @param delOriginalFile 是否删除原文件
	 * @throws IOException
	 */
	public void copyDirFiles(String fromPath, String toPath, String addPrefix, boolean delOriginalFile) throws IOException{
		File fromPathDir = new File(fromPath);
		File toPathDir = new File(toPath);
		//防止fromPath空目录，toPath目的目录不创建
		if(!toPathDir.exists()){
			toPathDir.mkdir();
		}
		Collection<File> files = FileUtils.listFiles(fromPathDir, null, false);
		Iterator<File> it = files.iterator();
		while(it.hasNext()){
			String name = it.next().getName();
			String newName = null;
			if(StringUtils.isNotBlank(addPrefix)){
				newName = addPrefix + name;
			}
			copyFile(fromPath, toPath, name, newName, delOriginalFile);
		}
	}
	
	public void copyFile(String fromPath, String toPath, 
			String fileName,String newFileName, boolean delOriginalFile) throws IOException{
		File newFile = new File(toPath + File.separator + (StringUtils.isBlank(newFileName)?fileName:newFileName));
		File file = new File(fromPath + File.separator + fileName);
		try {
			FileUtils.copyFile(file, newFile);
			if(file.exists() && delOriginalFile){
				file.delete();  
			}
		} catch (IOException e) {
			logger.error("拷贝文件异常,文件名:" + fileName);
        	throw e;
		} 
	}
	
	public String genBatchDatePrefix(){
		return genBatchDatePrefix(null);
	}
	
	public String genBatchDatePrefix(String pattern){
		Date batchDate = batchStatusFacility.getBatchDate();
		String ptn = StringUtils.isBlank(pattern)?"yyyyMMdd":pattern;
		String prefix = new SimpleDateFormat(ptn).format(batchDate);
		return prefix;
	}
}
