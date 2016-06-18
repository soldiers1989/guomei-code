package com.sunline.ccs.batch.cc6000;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.cc6000.common.TransactionPost;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsOrderHst;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnSeq;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.PostTxnType;
import com.sunline.ppy.dictionary.enums.PostingFlag;

/**
 * 
 * @see	优惠券抵扣利息（等额本息）
 * @author Lisy
 *
 */
public class P6026MCEICoupon implements ItemProcessor<S6000AcctInfo, S6000AcctInfo>{
	@PersistenceContext
	protected EntityManager em;
	@Autowired
	private TransactionPost transactionPost;
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private BatchStatusFacility batchFacility;
	
	@Override
	public S6000AcctInfo process(S6000AcctInfo item) throws Exception {
		//指处理随借随还账户
		for(CcsLoan loan:item.getLoans()){
			if(loan.getLoanType()!=LoanType.MCEI){
				return item;
			}
		}
		QCcsOrder q = QCcsOrder.ccsOrder;
		//优惠券订单只存在一条
		CcsOrder order = new JPAQuery(em).from(q).where(q.loanUsage.eq(LoanUsage.Q)
				.and(q.acctNbr.eq(item.getAccount().getAcctNbr())
						.and(q.acctType.eq(item.getAccount().getAcctType()))))
						.singleResult(q);
		if(order==null){
			//不存在优惠券订单
			return item;
		}
		logger.debug("优惠券（等额本息）处理：账户号["+item.getAccount().getAcctNbr()+"]，订单号["+order.getOrderId()+"]");
		logger.debug("优惠券编号：["+order.getCouponId()+"]，抵扣类型["+order.getOffsetType()+"]");
		//未到账单日，不做处理
		if(!batchFacility.shouldProcess(item.getAccount().getNextStmtDate())){
			logger.debug("未到账单日，不做处理");
			return item;
		}
		// 查找转入计划
		CcsPlan xfrInPlan = null;
		for(CcsPlan plan:item.getPlans()){
			if(plan.getPlanType().isXfrIn()&&plan.getTerm()==order.getTerm()){
				xfrInPlan = plan;
			}
		}
		//找不到转入计划，严重错误
		if(xfrInPlan==null){
			throw new Exception("找不到信用计划，账户号["+item.getAccount().getAcctNbr()+"]，订单号："+order.getOrderId()+"]，期数["+order.getTerm()+"]");
		}
		// 交易主键生成
		CcsTxnSeq txnSeq = new CcsTxnSeq();
		txnSeq.setOrg(item.getAccount().getOrg());
		em.persist(txnSeq);
		// 实际抵扣金额
		BigDecimal creditAmt = xfrInPlan.getCtdInterest().add(xfrInPlan.getPastInterest()).subtract(order.getTxnAmt());
		if(creditAmt.compareTo(BigDecimal.ZERO)<0){
			creditAmt = xfrInPlan.getCtdInterest().add(xfrInPlan.getPastInterest());
		}else{
			creditAmt = order.getTxnAmt();
		}
		CcsPostingTmp post = generateTrans(txnSeq,item,creditAmt,xfrInPlan);
		// 数据持久化
		em.persist(post);
		// 保存交易
		item.getTxnPosts().add(post);
		// 已处理的订单转移历史表
		CcsOrderHst hst = new CcsOrderHst();
		logger.debug("订单转移历史表：订单号:["+order.getOrderId()+"]");
		hst.updateFromMap(order.convertToMap());
		em.persist(hst);
		em.remove(order);
		// 调用内部交易入账处理
		transactionPost.posting(item, post, batchFacility.getBatchDate());
		return item;
	}
	
	
	/**
	 * @see 生成内部交易
	 * @return
	 */
	private CcsPostingTmp generateTrans(CcsTxnSeq txnSeq,S6000AcctInfo item,BigDecimal amt,CcsPlan xfrInPlan){
		SysTxnCdMapping sysTxnCdMapping = parameterFacility.loadParameter(SysTxnCd.C20, SysTxnCdMapping.class);
		TxnCd txnCd = parameterFacility.loadParameter(sysTxnCdMapping.txnCd, TxnCd.class);
		Date batchDate = batchFacility.getBatchDate();
		
		CcsPostingTmp newTxn = new CcsPostingTmp();
		newTxn.setOrg(item.getAccount().getOrg()); // 机构号
		newTxn.setTxnSeq(txnSeq.getTxnSeq()); // 交易流水号
		newTxn.setAcctNbr(item.getAccount().getAcctNbr()); // 账户编号
		newTxn.setAcctType(item.getAccount().getAcctType()); // 账户类型
		newTxn.setCardNbr(item.getAccount().getDefaultLogicCardNbr()); // 介质卡号
		newTxn.setLogicCardNbr(item.getAccount().getDefaultLogicCardNbr()); // 逻辑卡号
		newTxn.setCardBasicNbr(item.getAccount().getDefaultLogicCardNbr()); // 逻辑卡主卡卡号
		newTxn.setProductCd(item.getAccount().getProductCd()); // 产品代码
		newTxn.setTxnDate(batchDate); // 交易日期
		newTxn.setTxnTime(new Date()); // 交易时间
		newTxn.setPostTxnType(PostTxnType.M); // 入账交易类型
		newTxn.setTxnCode(txnCd.txnCd); // 交易码
		newTxn.setDbCrInd(txnCd.logicMod.getDbCrInd()); // 借贷标志
		newTxn.setTxnAmt(amt); // 交易金额
		newTxn.setPostAmt(amt); // 入账币种金额
		newTxn.setPostDate(batchDate); // 入账日期
		newTxn.setAuthCode(""); // 授权码
		newTxn.setCardBlockCode(""); // 卡片锁定码
		newTxn.setTxnCurrency(item.getAccount().getCurrency()); // 交易币种代码
		newTxn.setPostCurrency(item.getAccount().getCurrency()); // 入账币种代码
		newTxn.setOrigTransDate(batchDate); // 原始交易日期
		// 如何确定分期交易原先使用的卡产品？是使用帐户上的默认卡产品，还是要根据原交易来确定？
		newTxn.setPlanNbr(xfrInPlan.getPlanNbr()); // 信用计划号
		newTxn.setRefNbr(xfrInPlan.getRefNbr()); // 交易参考号
		newTxn.setTxnDesc(txnCd.description); // 交易描述
		newTxn.setTxnShortDesc(txnCd.shortDesc);
		newTxn.setPoints(BigDecimal.ZERO); // 积分数值
		newTxn.setPostingFlag(PostingFlag.F00); // 入账结果标示码
		newTxn.setPrePostingFlag(PostingFlag.F00);
		newTxn.setRelPmtAmt(BigDecimal.ZERO); // 公司卡还款金额
		newTxn.setOrigPmtAmt(BigDecimal.ZERO); // 还款交易原始金额
		newTxn.setAcqBranchIq(""); // 受理分行代码
		newTxn.setAcqTerminalId(""); // 受理机构终端标识码
		newTxn.setAcqAcceptorId(""); // 受卡方标识码
		newTxn.setAcqAddress(""); // 受理机构名称地址
		newTxn.setMcc(""); // 商户类别代码
		newTxn.setOrigTxnCode(""); // 原交易交易码
		newTxn.setOrigTxnAmt(amt); // 原交易交易金额
		newTxn.setOrigSettAmt(amt); // 原交易清算金额
		newTxn.setInterchangeFee(BigDecimal.ZERO); // 原交易货币转换费
		newTxn.setFeePayout(BigDecimal.ZERO); // 原交易交易手续费
		newTxn.setFeeProfit(BigDecimal.ZERO); // 发卡方应得手续费收入
		newTxn.setLoanIssueProfit(BigDecimal.ZERO); // 分期交易发卡行收益
		newTxn.setStmtDate(item.getAccount().getNextStmtDate()); // 账单日期
		newTxn.setVoucherNo(""); // 销售单凭证号
		newTxn.setTerm(xfrInPlan.getTerm());
		return newTxn;
	}
}