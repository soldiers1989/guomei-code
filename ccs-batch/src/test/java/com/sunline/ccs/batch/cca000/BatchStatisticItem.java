package com.sunline.ccs.batch.cca000;

import java.util.Date;

import com.sunline.ark.support.cstruct.CChar;

public class BatchStatisticItem {
	
	@CChar ( value = 10, order = 50 )
	public Long TIME_CONSUME;
	
	@CChar ( value = 10, order = 100 )
	public Long JOB_INSTANCE_ID;
	
	@CChar ( value = 200, order = 200 )
	public String JOB_NAME;
	
	@CChar ( value = 10, order = 300 )
	public Long STEP_EXECUTION_ID;
	
	@CChar ( value = 200, order = 400 )
	public String STEP_NAME;
	
	@CChar ( value = 10, order = 500 )
	public Long JOB_EXECUTION_ID;
	
	@CChar( value = 32, datePattern = "M/dd/yyyy HH:mm:ss:SSS", order = 600 )
	public Date START_TIME;
	
	@CChar( value = 32, datePattern = "M/dd/yyyy HH:mm:ss:SSS", order = 700 )
	public Date END_TIME;
	
}
