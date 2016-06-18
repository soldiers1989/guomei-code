/**
 * 
 */
package com.sunline.ccs.batch.cc3000;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaQuery;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gwt.core.client.JsonUtils;
import com.mysema.query.jpa.impl.JPAQuery;
import com.rabbitmq.tools.json.JSONUtil;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.QCcsPostingTmp;

/**  
 * @描述		: 自动豁免测试
 *  
 * @作者		: JiaoJian 
 * @创建时间	: 2015年11月13日  下午6:54:09   
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
		"/cc3000/test-context-cc3000.xml"})
public class AutoExemptTest {
	
	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;
	
	@PersistenceContext
	private EntityManager em;
	
	@Test
	public void testStep3011() {
		JobExecution jobExecution = jobLauncherTestUtils.launchStep("cc3011AutoExempt");
		System.out.println("===>step 3011执行结果："+jobExecution.getExitStatus().getExitCode());
		//查看生成的交易
		QCcsPostingTmp q = QCcsPostingTmp.ccsPostingTmp;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			List<CcsPostingTmp> list = new JPAQuery(em).from(q)
			   .where(q.createTime.after(dateFormat.parse("2015-11-13 19:30:00")))
			   .list(q);
			
			System.out.println("===>查询到的交易："+list.size());
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

}
