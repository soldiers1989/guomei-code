package com.sunline.ccs.batch.cca000;

import java.util.Date;

import com.sunline.ark.support.cstruct.CChar;

public class FileItem {
	/**
	 * 渠道流水
	 */
	@CChar(value = 32, datePattern = "M/dd/yyyy HH:mm:ss:SSS", order = 100)
	public Date beginTime;
	/**
	 * 渠道日期
	 */
	@CChar( value = 32, datePattern = "M/dd/yyyy HH:mm:ss:SSS", order = 200 )
	public Date endTime;
}
