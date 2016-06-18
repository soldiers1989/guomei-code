package com.sunline.ccs.batch.utils;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunline.ark.support.cstruct.CStruct;

/**
 * 
 * 用于将文件中的字段逐个解析输出到控制台
* @author fanghj
 *
 * @param <T>
 */
public class CheckFile<T> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	public void check(String s,Class<T> c)throws Exception{
		CStruct<T> t = new CStruct<T>(c);
		Object o = t.parseByteBuffer(ByteBuffer.wrap(s.getBytes("utf-8")));
		StringUtils.debug(o, "debug", logger);
	}
}
