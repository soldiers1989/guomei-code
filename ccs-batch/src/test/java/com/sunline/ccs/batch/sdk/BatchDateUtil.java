package com.sunline.ccs.batch.sdk;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.acm.service.sdk.GlobalManagementServiceMock;
import com.sunline.ark.support.utils.DateUtils;

@Component
public class BatchDateUtil {
	@Autowired
	private GlobalManagementServiceMock managerService;
	
	private final static String[] pattern = {"yyyyMMdd", "yyyyMM", "yyMM", "yy-MM", "yyyy-MM", "yyyy-MM-dd", "yyyyMMddHHmmss"};
	
	public void setBatchDate(String datestr) {
		try {
			Date processDate = DateUtils.parseDateStrictly(datestr, pattern);
			
			Calendar lastProcessDateCal = Calendar.getInstance();
			Calendar businessDateCal = Calendar.getInstance();
			
			businessDateCal.setTime(processDate);
			lastProcessDateCal.setTime(processDate);
			
			lastProcessDateCal.add(Calendar.DAY_OF_MONTH, -1);
			businessDateCal.add(Calendar.DAY_OF_MONTH, 1);
			
			managerService.setupBatchDate(processDate, lastProcessDateCal.getTime() );
			managerService.setupBusinessDate(businessDateCal.getTime());
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
	}
	
	public void setBatchDate(String lastProcessDateStr, String processDateStr){
		try {
			Date processDate = DateUtils.parseDateStrictly(processDateStr, pattern);
			Date lastProcessDate = DateUtils.parseDateStrictly(lastProcessDateStr, pattern);
			
			Calendar businessDateCal = Calendar.getInstance();
			businessDateCal.setTime(processDate);
			businessDateCal.add(Calendar.DAY_OF_MONTH, 1);
			
			managerService.setupBatchDate(processDate, lastProcessDate );
			managerService.setupBusinessDate(businessDateCal.getTime());
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public Date getBatchDate() {
		return  managerService.getSystemStatus().getProcessDate();
	}

}
