package com.sunline.ccs.batch.cca200;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.sunline.ccs.batch.cca000.SA000ParamDataUtil;
import com.sunline.ccs.batch.sdk.BatchDateUtil;
import com.sunline.ccs.batch.utils.JUnit4ClassRunner;

@RunWith(JUnit4ClassRunner.class)
@ContextConfiguration("/cca000/test-context-cca.xml")
public class SA2Test {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired 
	private JobLauncherTestUtils jobLauncherTestUtils;
	@Autowired
	private BatchDateUtil batchDateUtil;
	@Autowired
	private SA000ParamDataUtil paramUtil;
	
	private final static String BATCH_DATE_STR = "20181217";
	private final static String STEP_NAME = "cc6001";
	
	@Before
	public void init() throws Exception{
		batchDateUtil.setBatchDate(BATCH_DATE_STR);
		paramUtil.loadParamFromDir();
//		paramUtil.prepareData(STEP_NAME);
	}
	/**
	 * cca408SettleFile
	 * cc1351
	 * ccaOverdueRpt
	 * ccaOverdueRptFileMerge
	 * 
	 * cca401SettleReplaceFee
	 * cca402SettlePremiumAmt
	 * cca408SettleFile
	 * 
	 * cca252MsOrderCutExceptionRpt
	 * cca253MsOrderPayExceptionRpt
	 * 
	 * cca211MsLoanRpt
	 * cca212MsLoanRepayRpt
	 * cca213MsLoanBalanceRpt
	 * cca214MsLoanAppWithYgInsRpt
	 * cca215CooperationLoanBal
	 * cca221MCATLoanRpt
	 * cca222MCATLoanRepayRpt
	 * cca223MCATLoanBalanceRpt
	 * 
	 * cca001YGLoanDetail
	 * cca002YGRpyPlan
	 * cca003YGRpyDetail
	 * cca004YGInsuredAmtStatus
	 * cca005YGInsuredAmtRpyInfo
	 * cca006YGClaimInfo
	 * cca007YGConfirmClaimInfo
	 * cca008YGPenalInfo
	 * cca009YGStatInfo
	 * 
	 * cca201LoanRpt
	 * cca202LoanBalanceRpt
	 * cca203LoanRecoveryRpt
	 * cca204LoanSettlePayRpt
	 * cca205LoanSettleConfirmRpt
	 * cca206LoanRepayRpt
	 * cca207LoanRecoveryPayRpt
	 * 
	 * cca021SendYGFile
	 * cca022SendMSRpt
	 */
	
	@Test
	public void saTest(){
		
		logger.info(">>>>>>>>>>>>Batch Date [{}]<<<<<<<<<<<<", batchDateUtil.getBatchDate());
		JobExecution jobExecution = jobLauncherTestUtils.launchStep(STEP_NAME);
		ExitStatus exitStatus = jobExecution.getExitStatus();
		Assert.assertEquals(exitStatus,ExitStatus.COMPLETED);
	}
	
	

}
