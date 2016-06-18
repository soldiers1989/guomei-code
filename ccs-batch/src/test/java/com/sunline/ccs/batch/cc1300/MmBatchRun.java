package com.sunline.ccs.batch.cc1300;

import java.util.Date;

import javax.annotation.Resource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @see 类名：MmBatchRun
 * @see 描述：test用-批量调用
 * 
 * @see 创建日期： 2015年6月23日下午8:32:33
 * @author dch
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class MmBatchRun {
    @Autowired
    private JobLauncher jobLauncher;
    @Resource(name = "ccs1331")
    private Job job1331;


    /**
     * @throws Exception
     */
    public void exCcs1331() throws Exception {
	jobLauncher.run(job1331, new JobParametersBuilder().addDate("now", new Date()).toJobParameters());
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
	ApplicationContext context = new ClassPathXmlApplicationContext("test-context.xml");
	MmBatchRun bt = (MmBatchRun)context.getBean("batchTest");
	 bt.exCcs1331();
//	bt.exCcs0100();
	// bt.checkApplyData();
	// bt.checkEmbData();
	// bt.makeEmbData();
	// bt.makeDDResponse();
//	bt.makeIcItem();
    }

    public JobLauncher getJobLauncher() {
	return jobLauncher;
    }

    public void setJobLauncher(JobLauncher jobLauncher) {
	this.jobLauncher = jobLauncher;
    }

	public Job getJob1331() {
		return job1331;
	}

	public void setJob1331(Job job1331) {
		this.job1331 = job1331;
	}


}
