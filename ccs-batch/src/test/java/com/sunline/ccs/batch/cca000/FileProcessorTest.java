package com.sunline.ccs.batch.cca000;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.batch.item.ItemProcessor;

import com.sunline.ark.batch.LineItem;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;

public class FileProcessorTest implements ItemProcessor<LineItem<FileItem>, CcsRepaySchedule> {
	private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SSS");

	@Override
	public CcsRepaySchedule process(LineItem<FileItem> itemLine ) throws Exception {
		FileItem item = itemLine.getLineObject();
		
		System.out.println("开始时间" + sdf.format(item.beginTime) + "结束时间："+ sdf.format(item.endTime) + "间隔:\t" + (item.endTime.getTime() - item.beginTime.getTime()));
		return null;
	}
	
	public static void main(String[] args) throws ParseException {
		System.out.println(sdf.format(new Date()));
		sdf.parse("2/16/2016 00:38:26:394000");
	}


}
