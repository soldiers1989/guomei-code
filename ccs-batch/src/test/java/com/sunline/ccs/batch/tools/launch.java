package com.sunline.ccs.batch.tools;

import java.util.Date;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class launch {
	
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ApplicationContext context = new ClassPathXmlApplicationContext("batch-context.xml");
		JobLauncher launcher = (JobLauncher) context.getBean("jobLauncher");
		Job job = (Job) context.getBean("ccsJob");

		try {
			// JOB 实行
			@SuppressWarnings("deprecation")
			JobExecution result = launcher.run(job, new JobParametersBuilder().addString("test", "1")
					.addDate("batch.date", new Date(112,11,4)).toJobParameters());
			// 运行结果输出
			System.out.println(result.toString());
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
