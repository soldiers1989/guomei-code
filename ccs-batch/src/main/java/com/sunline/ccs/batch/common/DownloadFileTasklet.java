package com.sunline.ccs.batch.common;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.FTPClientOperations;

/**
 * ftp下载代扣回盘文件
 * @author zhangqiang
 *
 */
public class DownloadFileTasklet implements Tasklet {
	
	private static final Logger logger = LoggerFactory.getLogger(DownloadFileTasklet.class);
	
	//配置注入ftp操作实现类
	private FTPClientOperations ftpClientOperations;
	
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	private String fileName;
	private String disburseFileName;
	private String msxfTrans;
	private String msxfPayTranFlow;
	private String msxfWithholdTranFlow;
	private String acctBatchLimitAdj;
	private String msxfMerchantTranFlow;
	
	@Value("#{env.batchWorkDir}")
	private String batchWorkDir;
	
	@Value("#{env.payAccountFileRespRmPath}")
	private String payAccountFileRespRmPath;
	
	@Value("#{env.cutAccountFileRespRmPath}")
	private String cutAccountFileRespRmPath;
	
	@Value("#{env.respRemotePath}")
	private String respRemotePath;
	
	@Value("#{env.disburseRespRemotePath}")
	private String disburseRespRemotePath;
	
	@Value("#{env.merchantFileRespRmPath}")
	private String merchantFileRespRmPath;
	
	@Value("#{env.extensionFileName}")
	private String extensionFileName;
	
	@Value("#{env.validateFileName}")
	private String validateFileName;
	

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("开始ftp下载回盘文件");
		}
		
		String batchDateStr = new SimpleDateFormat("yyyyMMdd").format(batchStatusFacility.getBatchDate());
		
		if(fileName!=null){
			ftpClientOperations.download(batchWorkDir + File.separator + batchDateStr + File.separator + fileName, 
					respRemotePath + "/" + "10-" + batchDateStr + extensionFileName); 
			ftpClientOperations.download(batchWorkDir + File.separator + batchDateStr + File.separator + fileName + validateFileName, 
					respRemotePath + "/" + "10-" + batchDateStr + validateFileName);
		}
		
		// 批量代付回盘
		if(disburseFileName != null){
			ftpClientOperations.download(batchWorkDir + File.separator + batchDateStr + File.separator + disburseFileName, 
					disburseRespRemotePath + "/" + "10-" + batchDateStr + extensionFileName); 
			ftpClientOperations.download(batchWorkDir + File.separator + batchDateStr + File.separator + disburseFileName + validateFileName, 
					disburseRespRemotePath + "/" + "10-" + batchDateStr + validateFileName);
		}
		
		if(msxfPayTranFlow!=null){
			ftpClientOperations.download(batchWorkDir + File.separator + batchDateStr + File.separator + msxfPayTranFlow, 
					payAccountFileRespRmPath + "/" + "11-" + batchDateStr + extensionFileName); 
			ftpClientOperations.download(batchWorkDir + File.separator + batchDateStr + File.separator + msxfPayTranFlow + validateFileName, 
					payAccountFileRespRmPath + "/" + "11-" + batchDateStr + validateFileName);
		}
		
		if(msxfWithholdTranFlow!=null){
			ftpClientOperations.download(batchWorkDir + File.separator + batchDateStr + File.separator + msxfWithholdTranFlow, 
					cutAccountFileRespRmPath + "/" + "11-" + batchDateStr + extensionFileName); 
			ftpClientOperations.download(batchWorkDir + File.separator + batchDateStr + File.separator + msxfWithholdTranFlow + validateFileName, 
					cutAccountFileRespRmPath + "/" + "11-" + batchDateStr + validateFileName);
		}
		
		if(acctBatchLimitAdj!=null){
			List<String> existFileNames = new ArrayList<String>();
			existFileNames.addAll(ftpClientOperations.listFiles(respRemotePath+"/"));
			String limitAdjDataFile = respRemotePath + "/" + "limitadjust-" + batchDateStr + extensionFileName;
			String limitAdjValidFile = respRemotePath + "/" + "limitadjust-" + batchDateStr + validateFileName;
			if (!existFileNames.contains(limitAdjDataFile)){
				logger.debug("文件[{}]还不存在。",limitAdjDataFile);
			}else{
				ftpClientOperations.download(batchWorkDir + File.separator + batchDateStr + File.separator + acctBatchLimitAdj,limitAdjDataFile); 
			}
			if (!existFileNames.contains(limitAdjValidFile)){
				logger.debug("文件[{}]还不存在。",limitAdjValidFile);
			}else{
				ftpClientOperations.download(batchWorkDir + File.separator + batchDateStr + File.separator + acctBatchLimitAdj + validateFileName,limitAdjValidFile); 
			}
		}
		
		if(msxfMerchantTranFlow!=null){
			ftpClientOperations.download(batchWorkDir + File.separator + batchDateStr + File.separator + msxfMerchantTranFlow, 
					merchantFileRespRmPath + "/" + "transflow-" + batchDateStr + extensionFileName); 
			ftpClientOperations.download(batchWorkDir + File.separator + batchDateStr + File.separator + msxfMerchantTranFlow + validateFileName, 
					merchantFileRespRmPath + "/" + "transflow-" + batchDateStr + validateFileName);
		}
		
//		ftpClientOperations.download(batchWorkDir + File.separator + batchDateStr + File.separator + msxfTranFlow, respRemotePath + File.separator + batchDateStr + msxfTrans + extensionFileName); 
//		ftpClientOperations.download(batchWorkDir + File.separator + batchDateStr + File.separator + msxfTranFlow + validateFileName, respRemotePath + File.separator + batchDateStr + msxfTrans + validateFileName);
		
		/*File tranFlowFile = new File(batchWorkDir + File.separator + batchDateStr + File.separator + msxfPayTranFlow);
		File tranFlowCtrlFile = new File(batchWorkDir + File.separator + batchDateStr + File.separator + msxfPayTranFlow + validateFileName);
		if(!tranFlowFile.exists())
			tranFlowFile.createNewFile();
		if(!tranFlowCtrlFile.exists())
			tranFlowCtrlFile.createNewFile();*/
		
		return RepeatStatus.FINISHED;
	}
	
	public void setFtpClientOperations(FTPClientOperations ftpClientOperations) {
		this.ftpClientOperations = ftpClientOperations;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public String getDisburseFileName() {
		return disburseFileName;
	}

	public void setDisburseFileName(String disburseFileName) {
		this.disburseFileName = disburseFileName;
	}

	public void setMsxfTrans(String msxfTrans) {
		this.msxfTrans = msxfTrans;
	}

	public void setMsxfPayTranFlow(String msxfPayTranFlow) {
		this.msxfPayTranFlow = msxfPayTranFlow;
	}

	public void setMsxfWithholdTranFlow(String msxfWithholdTranFlow) {
		this.msxfWithholdTranFlow = msxfWithholdTranFlow;
	}

	public void setAcctBatchLimitAdj(String acctBatchLimitAdj) {
		this.acctBatchLimitAdj = acctBatchLimitAdj;
	}

	public void setMsxfMerchantTranFlow(String msxfMerchantTranFlow) {
		this.msxfMerchantTranFlow = msxfMerchantTranFlow;
	}
	
	
	
}