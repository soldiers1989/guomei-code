package com.sunline.ccs.batch.end;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.Resource;

public class RptJobStartTasklet implements Tasklet {
	
	private Resource resource;
	
	@Override
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {
		
		if(!resource.exists()){
			resource.getFile().createNewFile();
		}
		
		return RepeatStatus.FINISHED;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

}
