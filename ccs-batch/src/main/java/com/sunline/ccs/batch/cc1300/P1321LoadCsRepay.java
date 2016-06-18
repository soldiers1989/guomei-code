package com.sunline.ccs.batch.cc1300;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ppy.dictionary.enums.DdReturnCode;
import com.sunline.ppy.dictionary.enums.PostTxnType;
import com.sunline.ppy.dictionary.enums.PostingFlag;
import com.sunline.ppy.dictionary.exchange.DdResponseInterfaceItem;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ccs.batch.common.TxnPrepare;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.batch.LineItem;
import com.sunline.ark.support.utils.CodeMarkUtils;

/**
 * 
 * @see 类名：P1321LoadCsRepay
 * @see 描述：公务卡还款文件处理
 *
 * @see 创建日期：   2015-6-23下午7:24:59
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P1321LoadCsRepay implements ItemProcessor<LineItem<DdResponseInterfaceItem>, Object> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private TxnPrepare txnPrepare;
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	@Override
	public Object process(LineItem<DdResponseInterfaceItem> csRepayment) throws Exception {
		
		try
		{
			DdResponseInterfaceItem cs = csRepayment.getLineObject();
			//取机构号并设置上下文
			OrganizationContextHolder.setCurrentOrg(cs.org);
			
			//成功扣款记录生成金融交易
			if(cs.ddReturnCode == DdReturnCode.R00)
			{
				PostingFlag postingFlag = checkName(cs);
				
				CcsPostingTmp txnPost = generatePosting(cs, postingFlag);
				
				txnPrepare.txnPrepare(txnPost, null);
			}
			
			return null;
			
		}catch (Exception e) {
			logger.error("公务卡还款文件处理异常, 卡号{}", CodeMarkUtils.subCreditCard(csRepayment.getLineObject().defaltCardNo));
			throw e;
		}
	}


	/**
	 * 
	 * @see 方法名：checkName 
	 * @see 描述：校验还款文件中姓名与持卡人姓名是否一致，否则挂账
	 * @see 创建日期：2015-6-23下午7:26:02
	 * @author ChengChun
	 *  
	 * @param cs
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private PostingFlag checkName(DdResponseInterfaceItem cs) {
		CcsCustomer cust = queryFacility.getCustomerByCardNbr(cs.defaltCardNo);
		if(cust!=null && StringUtils.isNotBlank(cs.custName) && cs.custName.trim().equals(cust.getName().trim())){
			return PostingFlag.F00;
		}else{
			return PostingFlag.F12;
		}
	}

	
	/**
	 * 
	 * @see 方法名：generatePosting 
	 * @see 描述： 生成待入账记录
	 * @see 创建日期：2015-6-23下午7:25:37
	 * @author ChengChun
	 *  
	 * @param cs
	 * @param postingFlag
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private CcsPostingTmp generatePosting(DdResponseInterfaceItem cs, PostingFlag postingFlag) {
		CcsPostingTmp txnPost = new CcsPostingTmp();
		
		txnPost.setOrg(cs.org);
		txnPost.setAcctNbr(null);
		txnPost.setAcctType(null);
		txnPost.setCardNbr(cs.defaltCardNo);
		txnPost.setTxnDate(batchStatusFacility.getBatchDate());// 统一批量日期
		txnPost.setTxnTime(batchStatusFacility.getBatchDate());
		txnPost.setPostTxnType(PostTxnType.M);
		SysTxnCdMapping txnCdMapping = parameterFacility.loadParameter(SysTxnCd.S66.toString(), SysTxnCdMapping.class);
		txnPost.setTxnCode(txnCdMapping.txnCd);
		txnPost.setOrigTxnCode(txnCdMapping.txnCd);
		txnPost.setDbCrInd(DbCrInd.C);
		txnPost.setTxnAmt(cs.txnAmt);
		txnPost.setOrigTxnAmt(cs.txnAmt);
		txnPost.setOrigSettAmt(cs.txnAmt);
		txnPost.setPostAmt(cs.txnAmt);
		txnPost.setTxnCurrency("156");//默认156
		txnPost.setPostCurrency("156");
		txnPost.setOrigTransDate(cs.txnReturnDate);
		txnPost.setRelPmtAmt(BigDecimal.ZERO);
		txnPost.setOrigPmtAmt(cs.txnAmt);
		txnPost.setInterchangeFee(BigDecimal.ZERO);
		txnPost.setFeePayout(BigDecimal.ZERO);
		txnPost.setFeeProfit(BigDecimal.ZERO);
		txnPost.setLoanIssueProfit(BigDecimal.ZERO);
		txnPost.setPostingFlag(postingFlag);
		
		return txnPost;
	}
}
