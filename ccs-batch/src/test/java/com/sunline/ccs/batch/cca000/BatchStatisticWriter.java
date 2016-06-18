package com.sunline.ccs.batch.cca000;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import com.sunline.ark.batch.LineItem;


public class BatchStatisticWriter implements ItemWriter<LineItem<BatchStatisticItem>>{
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@PersistenceContext
	private EntityManager em;

	@Override
	public void write(List<? extends LineItem<BatchStatisticItem>> items)
			throws Exception {
		for(LineItem<BatchStatisticItem> lineItem : items) {
			BatchStatisticItem item = lineItem.getLineObject();
			
//			CcsBatchStatistic c = new CcsBatchStatistic();
//			c.setJobExecutionId(item.JOB_EXECUTION_ID);
//			c.setJobInstanceId(item.JOB_INSTANCE_ID);
//			c.setJobName(item.JOB_NAME);
//			c.setStartTime(item.START_TIME);
//			c.setEndTime(item.END_TIME);
//			c.setStepExecutionId(item.STEP_EXECUTION_ID);
//			c.setStepName(item.STEP_NAME);
//			c.setTimeConsume(item.TIME_CONSUME);
//			em.merge(c);
//			logger.info(ReflectionToStringBuilder.toString(c, ToStringStyle.MULTI_LINE_STYLE));
		}
		
	}

}
