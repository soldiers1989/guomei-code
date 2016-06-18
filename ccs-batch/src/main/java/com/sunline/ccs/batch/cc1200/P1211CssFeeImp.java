package com.sunline.ccs.batch.cc1200;

import java.math.BigDecimal;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.PostTxnType;
import com.sunline.ppy.dictionary.enums.PostingFlag;
import com.sunline.ppy.dictionary.report.ccs.CssFeeRptItem;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ccs.batch.common.TxnPrepare;
import com.sunline.ccs.batch.common.TxnPrepareSet;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.infrastructure.server.repos.RCcsCssfeeReg;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCssfeeReg;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.CustomerServiceFee;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.OrganizationContextHolder;

/**
 * 
 * @see 类名：P1211CssFeeImp
 * @see 描述：CMD040-客服类费用处理<p>
 *          生成各类客服费用，包括密码函重打费、补打对账单费、挂失换卡费等<p>
 *          输入：各客服交易送入核心系统的操作接口<p>
 *          输出：客服类交易金融流水（金融交易预处理临时表）<p>
 *          相关主表：帐户主表，逻辑卡主表等
 *
 * @see 创建日期：   2015-6-23下午7:22:23
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P1211CssFeeImp implements ItemProcessor<CcsCssfeeReg, CssFeeRptItem>{
	
	@Autowired
	private CustAcctCardFacility queryfacility;
	
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	
	@Autowired
	private RCcsCssfeeReg rTmCssfeeReg;
	
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	@Autowired
	private TxnPrepare txnPostPreEdit;
	
	@Override
	public CssFeeRptItem process(CcsCssfeeReg cssfeeReg) throws Exception {
		//	TODO 目前接口中只保存物理卡号，需要批量重新抓取逻辑卡号，对于性能影响较大。未来考虑接口中包含逻辑卡号
		
		OrganizationContextHolder.setCurrentOrg(cssfeeReg.getOrg());
		
		CcsCard card = queryfacility.getCardByCardNbr(cssfeeReg.getCardNbr());
		
		// 获取卡产品信息
		ProductCredit productCredit = parameterFacility.loadParameter(card.getProductCd(), ProductCredit.class);
		AccountAttribute acctAttr = parameterFacility.loadParameter(productCredit.accountAttributeId, AccountAttribute.class);
		// 获取本币账户
		CcsAcct acct = queryfacility.getAcctByAcctNbr(acctAttr.accountType, card.getAcctNbr());
		
		// 获取该卡产品对应的本币账户属性对象
		CustomerServiceFee customerService = productCredit.customerServiceFee.get(cssfeeReg.getServiceNbr());
		TxnCd txnCd = parameterFacility.loadParameter(customerService.txnCd, TxnCd.class);
		
		//不免除服务费则生成交易
		if(acct.getWaiveSvcfeeInd() == Indicator.N){
			generatePosting(cssfeeReg, acct, card, customerService, txnCd);
		}
			
		// 生成客户费报表接口
		CssFeeRptItem rptItem = new CssFeeRptItem();
		rptItem.procDate = batchStatusFacility.getBatchDate();
		rptItem.org = cssfeeReg.getOrg();
		rptItem.cardNo = cssfeeReg.getCardNbr();
		rptItem.serviceNbr = cssfeeReg.getServiceNbr();
		rptItem.acctNo = card.getAcctNbr();
		rptItem.cssFeeAmt = customerService.fee;
		rptItem.txnCd = txnCd.txnCd;
		rptItem.waiveSvcfeeInd = acct.getWaiveSvcfeeInd(); 
		rptItem.remark = null; //预留字段
		
		// 清空当日客服费用注册表
		rTmCssfeeReg.delete(cssfeeReg);
		
		return rptItem;
	}
      /**
       * 
       * @see 方法名：generatePosting 
       * @see 描述：TODO 方法描述
       * @see 创建日期：2015-6-23下午7:23:19
       * @author ChengChun
       *  
       * @param cssfeeReg
       * @param acct
       * @param card
       * @param customerService
       * @param txnCd
       * @return
       * @throws Exception
       * 
       * @see 修改记录： 
       * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
       */
	private TxnPrepareSet generatePosting(CcsCssfeeReg cssfeeReg, CcsAcct acct, CcsCard card, CustomerServiceFee customerService, TxnCd txnCd) throws Exception {
		CcsPostingTmp ttTxnPost = new CcsPostingTmp();
		
		ttTxnPost.setOrg(cssfeeReg.getOrg());
		ttTxnPost.setAcctNbr(acct.getAcctNbr());
		ttTxnPost.setAcctType(acct.getAcctType());
		ttTxnPost.setCardNbr(cssfeeReg.getCardNbr());
		ttTxnPost.setLogicCardNbr(card.getLogicCardNbr());
		ttTxnPost.setCardBasicNbr(null);
		ttTxnPost.setProductCd(card.getProductCd());
		ttTxnPost.setTxnDate(cssfeeReg.getTxnDate());
		ttTxnPost.setTxnTime(cssfeeReg.getRequestTime());
		ttTxnPost.setPostTxnType(PostTxnType.M);
		ttTxnPost.setTxnCode(txnCd.txnCd);
		ttTxnPost.setOrigTxnCode(txnCd.txnCd);
		ttTxnPost.setDbCrInd(txnCd.logicMod.getDbCrInd());
		ttTxnPost.setTxnAmt(customerService.fee);
		ttTxnPost.setOrigSettAmt(customerService.fee);
		ttTxnPost.setOrigTxnAmt(customerService.fee);
		ttTxnPost.setPostAmt(customerService.fee);
		ttTxnPost.setPostDate(null);
		ttTxnPost.setAuthCode(null);
		ttTxnPost.setCardBlockCode(card.getBlockCode());
		ttTxnPost.setTxnCurrency(acct.getAcctType().getCurrencyCode());
		ttTxnPost.setPostCurrency(acct.getAcctType().getCurrencyCode());
		ttTxnPost.setOrigTransDate(batchStatusFacility.getBatchDate());
		ttTxnPost.setRefNbr(null);
		ttTxnPost.setTxnDesc(txnCd.description);
		ttTxnPost.setTxnShortDesc(txnCd.shortDesc);
		ttTxnPost.setPoints(BigDecimal.ZERO);
		ttTxnPost.setPostingFlag(PostingFlag.F00);
		ttTxnPost.setPrePostingFlag(PostingFlag.F00);
		ttTxnPost.setRelPmtAmt(BigDecimal.ZERO);
		ttTxnPost.setOrigPmtAmt(BigDecimal.ZERO);
		ttTxnPost.setAcqBranchIq(null);
		ttTxnPost.setAcqTerminalId(null);
		ttTxnPost.setAcqAddress(null);
		ttTxnPost.setAcqAcceptorId(null);
		ttTxnPost.setMcc(null);
		ttTxnPost.setFeePayout(BigDecimal.ZERO);
		ttTxnPost.setInterchangeFee(BigDecimal.ZERO);
		ttTxnPost.setFeeProfit(BigDecimal.ZERO);
		ttTxnPost.setLoanIssueProfit(BigDecimal.ZERO);
		ttTxnPost.setStmtDate(null);
		ttTxnPost.setVoucherNo(null);
		ttTxnPost.setOrigTxnCode(ttTxnPost.getTxnCode());
		
		return txnPostPreEdit.txnPrepare(ttTxnPost, null);
	}
}
