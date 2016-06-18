package com.sunline.ccs.batch.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.springframework.stereotype.Service;

@Service
public class MessageUtils {
	
//	public static final String seperator = "{}";
	
	/**
	 * 获取短信流水号(32位)
	 * 批量日期(8位) + 账号(16位) + 随机数(8位)
	 * @param acctNbr
	 * @param batchDate
	 * @return
	 */
	public String getMsgSerialNo(Long acctNbr, Date batchDate){
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
		String batDateStr = dateFormatter.format(batchDate);
		String acctNbrStr = String.format("%016d", acctNbr);
//		String timeStr = new SimpleDateFormat("HHmmssSS").format(new Date()).substring(0, 8);
		String randomStr = String.format("%08d", new Random().nextInt(99999999));
		return batDateStr + acctNbrStr + randomStr;
	}
	
}