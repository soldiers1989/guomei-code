package com.sunline.ccs.batch.cc2100;


import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ccs.batch.common.BatchUtils;
import com.sunline.ccs.batch.common.TxnPrepare;
import com.sunline.ccs.batch.common.TxnPrepareSet;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.infrastructure.server.repos.RCcsTxnAdjLog;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnAdjLog;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.report.ccs.TranAdjLogRptItem;

/**
 * 
 * @see 类名：P2101TxnAdjImp
 * @see 描述：账务调整成功交易重新待入账预处理
 *
 * @see 创建日期：   2015-6-23下午7:29:01
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P2101TxnAdjImp implements ItemProcessor<CcsTxnAdjLog, S2101TxnAdjImp> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	CustAcctCardFacility custAcctCardQueryFacility;
	
	@Autowired
	private RCcsTxnAdjLog rTxnAdjLog;
	
	@Autowired
	private BatchStatusFacility batchFacility;
	
	@Autowired
	private BatchUtils batchUtils;
	
	@Autowired
	private TxnPrepare txnPrepare;
	
	@Override
	public S2101TxnAdjImp process(CcsTxnAdjLog txn) throws Exception {
		
		OrganizationContextHolder.setCurrentOrg(txn.getOrg());
		
		try {
			//日志表记录全量出报表
			TranAdjLogRptItem tranAdjLogRptItem = new TranAdjLogRptItem();
			tranAdjLogRptItem.org = txn.getOrg();
			tranAdjLogRptItem.operSeq = txn.getOpSeq();
			tranAdjLogRptItem.operTime = txn.getOpTime();
			tranAdjLogRptItem.operaId = txn.getOpId();
			tranAdjLogRptItem.acctNo = txn.getAcctNbr();
			tranAdjLogRptItem.cardNo = txn.getCardNbr();
			tranAdjLogRptItem.txnCode = txn.getTxnCode();
			tranAdjLogRptItem.dbCrInd = txn.getDbCrInd();
			tranAdjLogRptItem.txnAmt = txn.getTxnAmt();
			tranAdjLogRptItem.txnDate = txn.getTxnDate();
			tranAdjLogRptItem.currCd = txn.getCurrency();
			tranAdjLogRptItem.refNbr = txn.getRefNbr();
			tranAdjLogRptItem.remark = txn.getRemark();
			tranAdjLogRptItem.authCode = txn.getAuthCode();
			tranAdjLogRptItem.mcc = txn.getMcc();
			tranAdjLogRptItem.b003 = txn.getB003ProcCode();
			tranAdjLogRptItem.b004 = txn.getB004Amt();
			tranAdjLogRptItem.b007 = txn.getB007TxnTime();
			tranAdjLogRptItem.b011 = txn.getB011Trace();
			tranAdjLogRptItem.b032 = txn.getB032AcqInst();
			tranAdjLogRptItem.b033 = txn.getB033FwdIns();
			tranAdjLogRptItem.b049 = txn.getB049CurrCode();
			tranAdjLogRptItem.mti = txn.getMti();
			tranAdjLogRptItem.voidInd = txn.getVoidInd();
			tranAdjLogRptItem.voidTime = txn.getVoidTime();
			tranAdjLogRptItem.voidReason = txn.getVoidReason();
			tranAdjLogRptItem.voidOperator = txn.getVoidOpId();
			tranAdjLogRptItem.b042 = txn.getB042MerId();
			tranAdjLogRptItem.b039 = txn.getB039RtnCode();
			
			//取姓名证件信息
			CcsCustomer cust = custAcctCardQueryFacility.getCustomerByCardNbr(txn.getCardNbr());
			tranAdjLogRptItem.name = cust.getName();
			tranAdjLogRptItem.idType = cust.getIdType();
			tranAdjLogRptItem.idNo = cust.getIdNo();
			
			//待入账的调账交易:VoidInd=N && (b039=00||b039=11)
			TxnPrepareSet output5 = new TxnPrepareSet();
			if(txn.getVoidInd() == Indicator.N 
					&& ("00".equals(txn.getB039RtnCode()) || "11".equals(txn.getB039RtnCode())))
			{
				CcsPostingTmp post = new CcsPostingTmp();
				post.updateFromMap(txn.convertToMap());
				post.setPostCurrency(txn.getCurrency());
				post.setFeeProfit(BigDecimal.ZERO);
				post.setFeePayout(BigDecimal.ZERO);
				post.setLoanIssueProfit(BigDecimal.ZERO);
				post.setPostAmt(txn.getTxnAmt());
				post.setTxnCurrency(txn.getCurrency());
				post.setPostCurrency(txn.getCurrency());
				post.setOrigTxnAmt(txn.getTxnAmt());
				post.setOrigSettAmt(txn.getTxnAmt());
				post.setOrigTxnCode(txn.getTxnCode());
				
				//交易日期时间优先取post.TxnDate,若null则取b007, b007也null则new Date()
				if(txn.getTxnDate() != null){
					post.setTxnDate(txn.getTxnDate());
				}else{
					if(StringUtils.isNotBlank(txn.getB007TxnTime())){
						Date txnDateTime = batchUtils.fixYear(txn.getB007TxnTime(), batchFacility.getBatchDate());
						post.setTxnDate(txnDateTime);
					}else{
						post.setTxnDate(new Date());
					}
				}
				if(txn.getOpTime() != null){
					post.setTxnTime(txn.getOpTime());
				}else{
					if(StringUtils.isNotBlank(txn.getB007TxnTime())){
						Date txnDateTime = batchUtils.fixYear(txn.getB007TxnTime(), batchFacility.getBatchDate());
						post.setTxnTime(txnDateTime);
					}else{
						post.setTxnTime(new Date());
					}
				}
				
				//交易预处理
				output5 = txnPrepare.txnPrepare(post, InputSource.BANK);
			}
			
			S2101TxnAdjImp output4 = new S2101TxnAdjImp();
			output4.setGlTxnItemList(output5.getGlTxnItemList());
			output4.setRptTxnItem(output5.getRptTxnItem());
			output4.setTranAdjLogRptItem(tranAdjLogRptItem);
			
			rTxnAdjLog.delete(txn);
			
			return output4;
			
		} catch (Exception e) {
			logger.error("账务调整批量作业异常, 卡号后四位{}", CodeMarkUtils.subCreditCard(txn.getCardNbr()));
			throw e;
		}
	}
}
