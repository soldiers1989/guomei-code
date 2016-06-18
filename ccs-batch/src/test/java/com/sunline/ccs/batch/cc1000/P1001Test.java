package com.sunline.ccs.batch.cc1000;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.sunline.ppy.dictionary.exchange.FmInterfaceItem;
import com.sunline.ppy.dictionary.exchange.TpsTranFlow;
import com.sunline.acm.service.sdk.GlobalManagementServiceMock;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.batch.FileHeader;
import com.sunline.ark.batch.YakFileResourceMock;
import com.sunline.ark.support.utils.DateUtils;
import com.sunline.ccs.batch.tools.MakeData;

/**
 * step-s0200单元测试
 * 
* @author fanghj
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-context.xml")
public class P1001Test {
	
	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;
	
	@Autowired
	private GlobalManagementServiceMock managementMock;
	
	/**
	 * 文件资源resS0200TpsTranFlow
	 */
	@Resource
	private YakFileResourceMock<FileHeader, TpsTranFlow> resS0200TpsTranFlow;
	
	/**
	 * 文件资源resS020001TpsTrans
	 */
	@Resource
	private YakFileResourceMock<FileHeader, FmInterfaceItem> resS020001TpsTrans;
	
	@Before
	public void setUp() {
		OrganizationContextHolder.setCurrentOrg("123456789012");
		
		//设置批量时间
		Date today = new Date();
		managementMock.setupBatchDate(today, DateUtils.addDays(today, -1));
	}

	@Test
	public void jobTest() throws Exception
	{
		//准备数据
		int rows = 1;
		TpsTranFlow[] items = new TpsTranFlow[rows];
		for (int i = 0; i < rows ; i ++){
			TpsTranFlow trans = new TpsTranFlow();
			MakeData.setDefaultValue(trans);
			
			trans.mti = "0200";
			trans.processingCode="01";
			trans.conditionCode="";
			trans.merchCategoryCode="6011";
			trans.srcChannel="BANK";
			
			trans.dbCrInd=null;
			trans.inputTxnCode=null;
			trans.feeProfit="1.1";
			trans.inputSource="BANK";
			trans.orgId="123456789012";
			trans.settAmt="100";
			trans.settCurrencyCode="156";
			trans.tranAmt="100";
			trans.txnDateTime="20121212";
			trans.txnFeeAmt="2.2";
			trans.recType="DT";
			
			items[i] = trans;
		}
		//创建文件
		resS0200TpsTranFlow.prepare(new FileHeader(), items);
		
		//开始跑批
		System.out.println("----BEGIN----");
		JobExecution jobExecution = jobLauncherTestUtils.launchStep("s0200-fileimp");
		System.out.println("----END----");
		
		//检查结果
		Assert.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
		
		//检查输出数据的值
		List<FmInterfaceItem> fmInterfaceItem = resS020001TpsTrans.parseDetails();
		System.out.println("123123123213123213213");
		Assert.assertNotNull(fmInterfaceItem);
		
		
	}
}
