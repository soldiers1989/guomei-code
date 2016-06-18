package com.sunline.ccs.batch.cc1300;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.ark.batch.LineItem;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ccs.batch.common.TxnPrepare;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ppy.dictionary.enums.DdReturnCode;
import com.sunline.ppy.dictionary.enums.PostTxnType;
import com.sunline.ppy.dictionary.exchange.DdResponseInterfaceItem;
//import com.sunline.smsd.service.sdk.DdFailMessInterfaceItem;

/**
 * 
 * @see 类名：P1301LoadDdResp
 * @see 描述：约定还款回盘文件处理
 *
 * @see 创建日期：   2015-6-23下午7:24:15
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P1301LoadDdResp implements ItemProcessor<LineItem<DdResponseInterfaceItem>, Object> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private TxnPrepare txnPrepare;
	
	@Override
	public Object process(LineItem<DdResponseInterfaceItem> ddResponseFile) throws Exception {
		
		try
		{
			DdResponseInterfaceItem ddResponse = ddResponseFile.getLineObject();
			//取机构号并设置上下文
			OrganizationContextHolder.setCurrentOrg(ddResponse.org);
			
			CcsPostingTmp txnPost = new CcsPostingTmp();
			//失败短信接口
			Object item = null;
			
			//成功扣款记录生成金融交易
			if(ddResponse.ddReturnCode == DdReturnCode.R00)
			{
				txnPost.setOrg(ddResponse.org);
				txnPost.setAcctNbr(ddResponse.acctNo);
				txnPost.setAcctType(ddResponse.acctType);
				txnPost.setCardNbr(ddResponse.defaltCardNo);
				txnPost.setTxnDate(ddResponse.txnReturnDate);
				txnPost.setTxnTime(ddResponse.txnReturnDate);
				txnPost.setPostTxnType(PostTxnType.M);
				SysTxnCdMapping txnCdMapping = parameterFacility.loadParameter(SysTxnCd.S11.toString(), SysTxnCdMapping.class);
				txnPost.setTxnCode(txnCdMapping.txnCd);
				txnPost.setOrigTxnCode(txnCdMapping.txnCd);
				txnPost.setDbCrInd(DbCrInd.C);
				txnPost.setTxnAmt(ddResponse.txnAmt);
				txnPost.setOrigTxnAmt(ddResponse.txnAmt);
				txnPost.setOrigSettAmt(ddResponse.txnAmt);
				txnPost.setPostAmt(ddResponse.txnAmt);
				txnPost.setTxnCurrency(ddResponse.acctType.getCurrencyCode());
				txnPost.setPostCurrency(ddResponse.acctType.getCurrencyCode());
				txnPost.setOrigTransDate(ddResponse.txnReturnDate);
				txnPost.setRelPmtAmt(BigDecimal.ZERO);
				txnPost.setOrigPmtAmt(ddResponse.txnAmt);
				txnPost.setInterchangeFee(BigDecimal.ZERO);
				txnPost.setFeePayout(BigDecimal.ZERO);
				txnPost.setFeeProfit(BigDecimal.ZERO);
				txnPost.setLoanIssueProfit(BigDecimal.ZERO);
				
				txnPrepare.txnPrepare(txnPost, null);
			}
			//失败扣款记录生成失败短信接口
/*			else
			{
				//获取账户
				CcsAcct acct = queryFacility.getAcctByAcctNbr(ddResponse.acctType, ddResponse.acctNo);
				CcsCard card = queryFacility.getCardByCardNbr(ddResponse.defaltCardNo);
				
				item = new DdFailMessInterfaceItem();
				item.cardNo = ddResponse.defaltCardNo; // 不受卡号发送方式约束
				item.org = ddResponse.org;
				item.ddReturnCode = ddResponse.ddReturnCode;
				if(acct!=null && card!=null){
					item.msgCd = fetchMsgCdService.fetchMsgCd(card.getProductCd(), CPSMessageCategory.CPS041);
					item.custName = acct.getName();
					item.gender = acct.getGender();
					item.mobileNo = acct.getMobileNo();
				}else{
					return null;
				}
			}*/
			
			return item;
			
		}catch (Exception e) {
			logger.error("约定还款回盘文件处理异常, 卡号{}", CodeMarkUtils.subCreditCard(ddResponseFile.getLineObject().defaltCardNo));
			throw e;
		}
	}
}
