package com.sunline.ccs.batch.cc1000;

import java.math.BigDecimal;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.drools.KnowledgeBase;
import org.drools.runtime.StatelessKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.LineItem;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ccs.batch.common.BatchUtils;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanRegHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanRegHst;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.pcm.param.def.CurrencyCd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.exchange.FmInterfaceItem;
import com.sunline.ppy.dictionary.exchange.TpsTranFlow;
import com.sunline.ppy.dictionary.report.ccs.UnmatchedLoanReturnRptItem;

/**
 * @see 类名：P1001LoadTxn
 * @see 描述：外部交易流水装载
 * 
 * @see 创建日期： 2015-6-23下午7:20:11
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */

public class P1001LoadTxn implements ItemProcessor<LineItem<TpsTranFlow>, S1001LoadTxn> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private KnowledgeBase fileRules;
    @Autowired
    private CustAcctCardFacility queryFacility;
    @Autowired
    private UnifiedParameterFacility parameterFacility;
    @Autowired
    private BatchStatusFacility batchFacility;
    @Autowired
    private BatchUtils batchUtils;
    @PersistenceContext
    private EntityManager em;

    @Override
	public S1001LoadTxn process(LineItem<TpsTranFlow> item) throws Exception {
		TpsTranFlow trans = item.getLineObject();
		
		try
		{
			//取机构号并设置上下文
			OrganizationContextHolder.setCurrentOrg(trans.orgId);
			S1001LoadTxn output = new S1001LoadTxn();
			
			U1001RuleObject ro = new U1001RuleObject();
			ro.setTpsTranFlow(trans);
			
			CcsCard card = null;
			AccountType acctType = null;
			if(StringUtils.isNotBlank(trans.cardNo)){
				card = queryFacility.getCardByCardNbr(trans.cardNo);
				
				if(card != null){
					ProductCredit productCredit = parameterFacility.loadParameter(card.getProductCd(), ProductCredit.class);
					
					//补充赋值交易所属的机构组织
					//设置获取卡产品所属机构
					if(trans.acqAcceptorId == null || "".equals(trans.acqAcceptorId)){
						trans.acqAcceptorId=productCredit.financeOrgNo;
					}
					
					
					acctType = getAcctTypeByProduct(productCredit.accountAttributeId, trans.settCurrencyCode);
					if(acctType == null){
						acctType = getAcctTypeByProduct(productCredit.dualAccountAttributeId, trans.settCurrencyCode);
					}
				}
			}
			
			if(acctType != null){
				ro.setAcctTypeGroup(acctType.getAcctTypeGroup());
			}else{
				logger.info("单转双规则未命中: 交易币种["+trans.settCurrencyCode+"]非产品可入账币种, 卡号["+trans.cardNo+"]");
			}
			
			//若商户分期交易判断是否退货交易
			LoanReturn loanReturn = getLoanReturn(trans);
			if(trans.mti.equals("0220") && trans.processingCode.substring(0,2).equals("20")){
				ro.setLoanReturn(loanReturn.isLoanReturn);
			}
			output.setUnmatchedLoanReturnRpt(loanReturn.getUnmatchedRpt());
			
//			String refNbr = txnUtils.getRefnbr(trans.txnDateTime, trans.authCode, trans.refNbr);
			String refNbr = trans.refNbr;
			//若小额贷款区分随借随还及等额本金本息
			if(trans.mti.equals("0208") && trans.processingCode.substring(0,2).equals("48")){
				QCcsLoanReg qTmLoanReg = QCcsLoanReg.ccsLoanReg;
				CcsLoanReg loanReg = new JPAQuery(em).from(qTmLoanReg)
						.where(qTmLoanReg.logicCardNbr.eq(trans.cardNo).and(qTmLoanReg.refNbr.eq(refNbr)))
						.singleResult(qTmLoanReg);
				if(loanReg != null){
					ro.setLoanType(loanReg.getLoanType());
				}else{
					//这里增加一个从交易历史表里查询，是为了处理如果交易挂账，loan已经生成的情况
					QCcsLoanRegHst qTmLoanRegHst = QCcsLoanRegHst.ccsLoanRegHst;
					CcsLoanRegHst loanRegHst = new JPAQuery(em).from(qTmLoanRegHst)
							.where(qTmLoanRegHst.logicCardNbr.eq(trans.cardNo).and(qTmLoanRegHst.refNbr.eq(refNbr)))
							.singleResult(qTmLoanRegHst);
					if(loanRegHst != null){
						ro.setLoanType(loanRegHst.getLoanType());
					}else{
						logger.error("小额贷款交易未匹配到LoanReg");
					}
				}
			}
			
			// 将消息放入规则引擎
			StatelessKnowledgeSession sks = fileRules.newStatelessKnowledgeSession();
			sks.setGlobal("ruleObject", ro);
			sks.execute(ro);
			
			if(ro.getDcFlag()==null || ro.getTxnCode() == null){
				logger.info("--------- 单转双规则未命中 TRANSACTION ---------");
				logger.info("卡号[cardNo]后四位 ------------ " + CodeMarkUtils.subCreditCard(trans.cardNo));
				logger.info("交易参考号[refNbr] ------------ " + refNbr);
				logger.info("原交易参考号[refNbr] ---------- " + trans.refNbr);
				logger.info("--------- 单转双规则未命中 CONDITION -----------");
				logger.info("消息类型[mti] ----------------- " + trans.mti);
				logger.info("3域 [processingCode] ---------- " + trans.processingCode);
				logger.info("25域[conditionCode] ----------- " + trans.conditionCode);
				logger.info("18域[merchCategoryCode] ------- " + trans.merchCategoryCode);
				logger.info("40域[transTerminal] ----------- " + trans.transTerminal);
				logger.info("来源渠道[srcChannel] ---------- " + trans.srcChannel);
				logger.info("是否银联境外[isXborder()] ----- " + ro.isXborder());
				logger.info("是否分期退货[isLoanReturn()] -- " + ro.isLoanReturn());
				logger.info("处理账户[acctTypeGroup] ------- " + ro.getAcctTypeGroup());
				logger.info("分期贷款类型[loanType] -------- " + ro.getLoanType());
				logger.info("--------- 单转双规则未命中 ACTION --------------");
				logger.info("借贷标识[dbCrInd] ------------- " + ro.getDcFlag());
				logger.info("交易码[txnCode] --------------- " + ro.getTxnCode());
			}
			
			
			FmInterfaceItem result = new FmInterfaceItem();
			result.dbCrInd = ro.getDcFlag();
			result.inputTxnCode = ro.getTxnCode();
			
			// 根据币种控制表中本币的小数精度，设置交易金额的小数位
			CurrencyCd txnCurrencyCd = parameterFacility.loadParameter(trans.txnCurrencyCode, CurrencyCd.class);
			CurrencyCd postCurrencyCd = parameterFacility.loadParameter(trans.settCurrencyCode, CurrencyCd.class);

			// 属性赋值
			result.authCode = trans.authCode;
			result.acqAcceptorId = trans.acqAcceptorId;
			//result.acqBranchId = 
			result.acqNameAddr = trans.acqNameAddr;
			result.acqTerminalId = trans.acqTerminalId;
			result.cardNo = trans.cardNo;
			result.feePayout = formatAmt(trans.txnFeeAmt, postCurrencyCd.exponent);
			result.feeProfit = formatAmt(trans.feeProfit, postCurrencyCd.exponent);
			result.inputSource = InputSource.valueOf(trans.srcChannel);
			result.loanIssueProfit = BigDecimal.ZERO;//暂定
			result.mcc = trans.merchCategoryCode;
			result.org = trans.orgId;
			result.postAmt = formatAmt(trans.settAmt, postCurrencyCd.exponent);
			result.postCurrCd = trans.settCurrencyCode;
			result.refNbr = refNbr;
			result.txnAmt = formatAmt(trans.tranAmt, txnCurrencyCd.exponent);
			result.txnCurrCd = trans.txnCurrencyCode;
			result.voucherNo = trans.voucherNo;
			result.txnDateTime = batchUtils.fixYear(trans.txnDateTime, batchFacility.getBatchDate());
			result.loanCode = trans.loanCode;
			output.setTpsInterface(result);
			
			return output;
		}catch (Exception e) {
			logger.error("交易转换异常, 卡号后四位{}, 检索参考号{}", CodeMarkUtils.subCreditCard(trans.cardNo), trans.refNbr);
			throw e;
		}
	}

    /**
     * @see 方法名：getLoanReturn
     * @see 描述：判断是否退货
     * @see 创建日期：2015-6-23下午7:20:43
     * @author ChengChun
     * 
     * @param tranFlow
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public LoanReturn getLoanReturn(TpsTranFlow tranFlow) {
		LoanReturn loanReturn = new LoanReturn();
	
		CurrencyCd currencyCd = parameterFacility.loadParameter(tranFlow.txnCurrencyCode, CurrencyCd.class);
		BigDecimal tranAmt = formatAmt(tranFlow.tranAmt, currencyCd.exponent);
	
		Integer month = 2;
		Integer dayOfMonth = 31;
		if (StringUtils.isNotBlank(tranFlow.origTxnMess)) {
		    // 联机写入的origTxnMess
		    if (tranFlow.origTxnMess.length() == 42) {
			month = Integer.valueOf(tranFlow.origTxnMess.substring(10, 12));
			dayOfMonth = Integer.valueOf(tranFlow.origTxnMess.substring(12, 14));
		    }
		    // 批量写入的origTxnMess
		    else if (tranFlow.origTxnMess.length() == 16) {
			month = Integer.valueOf(tranFlow.origTxnMess.substring(0, 2));
			dayOfMonth = Integer.valueOf(tranFlow.origTxnMess.substring(2, 4));
		    }
		}
	
		JPAQuery query = new JPAQuery(em);
		QCcsLoan qTmLoan = QCcsLoan.ccsLoan;
		CcsLoan loan =
			query.from(qTmLoan)
				.where(qTmLoan.cardNbr.eq(tranFlow.cardNo).and(qTmLoan.origAuthCode.eq(tranFlow.authCode))
					       .and(qTmLoan.origTxnAmt.eq(tranAmt))
					       .and(qTmLoan.origTransDate.month().eq(month))
					       .and(qTmLoan.origTransDate.dayOfMonth().eq(dayOfMonth))).singleResult(qTmLoan);
		loanReturn.setLoanReturn(loan != null ? true : false);
	
		// 退货未匹配到Loan的异常交易报表
		if ("64".equals(tranFlow.conditionCode) && "0220".equals(tranFlow.mti)
			&& "20".equals(tranFlow.processingCode.substring(0, 2)) && loan == null) {
		    UnmatchedLoanReturnRptItem unmatchedRpt = new UnmatchedLoanReturnRptItem();
	
		    unmatchedRpt.org = tranFlow.orgId;
		    unmatchedRpt.cardNo = tranFlow.cardNo;
		    unmatchedRpt.refNbr = tranFlow.refNbr;
		    unmatchedRpt.authCode = tranFlow.authCode;
		    unmatchedRpt.tranAmt = formatAmt(tranFlow.tranAmt, currencyCd.exponent);
		    unmatchedRpt.txnCurrencyCode = tranFlow.txnCurrencyCode;
		    unmatchedRpt.txnDateTime = batchUtils.fixYear(tranFlow.txnDateTime, batchFacility.getBatchDate());
		    unmatchedRpt.b032 = tranFlow.b032;
		    unmatchedRpt.acqTerminalId = tranFlow.acqTerminalId;
		    unmatchedRpt.srcChannel = tranFlow.srcChannel;
	
		    loanReturn.setUnmatchedRpt(unmatchedRpt);
		}
	
		return loanReturn;
    }

    /**
     * @see 方法名：formatAmt
     * @see 描述：范型金额字段
     * @see 创建日期：2015-6-23下午7:20:58
     * @author ChengChun
     * 
     * @param amt
     * @param exponent
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private BigDecimal formatAmt(String amt, Integer exponent) {
    	return StringUtils.isNotBlank(amt) ? new BigDecimal(amt).movePointLeft(exponent) : BigDecimal.ZERO;
    }

    /**
     * 
     * @see 方法名：getAcctTypeByProduct
     * @see 描述：根据交易币种获取该产品匹配的账户类型
     * @see 创建日期：2015-6-23下午7:21:12
     * @author ChengChun
     * 
     * @param acctAttrId
     * @param tpsCurrencyCode
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private AccountType getAcctTypeByProduct(Integer acctAttrId, String tpsCurrencyCode) {
    	AccountAttribute acctAttr = parameterFacility.loadParameter(acctAttrId, AccountAttribute.class);
    	return tpsCurrencyCode.equals(acctAttr.accountType.getCurrencyCode()) ? acctAttr.accountType : null;
    }

    private class LoanReturn {

		boolean isLoanReturn;
		UnmatchedLoanReturnRptItem unmatchedRpt;
	
		public UnmatchedLoanReturnRptItem getUnmatchedRpt() {
		    return unmatchedRpt;
		}
	
		public void setLoanReturn(boolean isLoanReturn) {
		    this.isLoanReturn = isLoanReturn;
		}
	
		public void setUnmatchedRpt(UnmatchedLoanReturnRptItem unmatchedRpt) {
		    this.unmatchedRpt = unmatchedRpt;
		}
    }

}
