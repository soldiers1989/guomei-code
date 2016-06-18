package com.sunline.ccs.batch.cc6000.interestaccrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.cc6000.S6000AcctInfo;
import com.sunline.ccs.batch.cc6000.common.PlanManager;
import com.sunline.ccs.batch.cc6000.common.TransactionPost;
import com.sunline.ccs.batch.common.EnumUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.CcsStatement;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnSeq;
import com.sunline.ccs.infrastructure.shared.model.QCcsStatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnHst;
import com.sunline.ccs.param.def.LatePaymentCharge;
import com.sunline.ccs.param.def.Organization;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.PostTxnType;
import com.sunline.ppy.dictionary.enums.PostingFlag;
import com.sunline.ppy.dictionary.exception.ProcessException;


/**
 * @see 类名：LateFee
 * @see 描述： 滞纳金的回溯
 *
 * @see 创建日期：   2015-10-15 
 * @author liuqi
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class LateFee{
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private TransactionPost transactionPost;
	@Autowired
    private BatchStatusFacility batchFacility;
	@PersistenceContext
	protected EntityManager em;
	@Autowired
	private PlanManager planManager;
	
	/**
	 * 滞纳金回溯
	 * @param item
	 * @param postAmt 还款的金额
	 */
	public BigDecimal accumulateLateFee(S6000AcctInfo item,BigDecimal postAmt,Date txnDate, 
			List<CcsPostingTmp> newTxnPosts, BucketType bucketType)
	{
		CcsAcct acct = item.getAccount();

		logger.debug("滞纳金回溯:账户编号：["+acct.getAcctNbr()+"],账户类型:["+acct.getAcctType()+"]," +
					"回溯交易金额:["+postAmt+"],回溯交易日期:["+txnDate+"],账户最小还款额:["+acct.getTotDueAmt()+"]");
		// 参数不允许是非滞纳金余额成分
		if(!EnumUtils.in(bucketType, BucketType.ReplaceLatePaymentCharge, BucketType.LatePaymentCharge)){
			throw new ProcessException("不支持的滞纳金回溯余额成分类型[" + bucketType==null?"Null":bucketType.name() + "]");
		}
		
		// 获取滞纳金参数
		ProductCredit productCr = parameterFacility.loadParameter(item.getAccount().getProductCd(), ProductCredit.class);
		Organization org = parameterFacility.loadParameter(null, Organization.class);
		
		// 滞纳金参数
		LatePaymentCharge latePayCharge = null;
		if (org.baseCurrCd.equals(item.getAccount().getCurrency())){
			if(bucketType == BucketType.ReplaceLatePaymentCharge){
				latePayCharge = productCr.replaceLatePaymentCharge;
			}else{
				latePayCharge = productCr.latePaymentCharge;
			}
		}else{ // 外币
			latePayCharge = productCr.dualLatePaymentCharge;
		}
		if(latePayCharge == null){
			logger.debug("没有配置[{}]滞纳金参数,跳过", bucketType);
			return null;
		}
		if(latePayCharge.isReviewLateFee.equals(Indicator.N)){
			logger.debug("滞纳金回溯:账户编号：["+acct.getAcctNbr()+"],账户类型:["+acct.getAcctType()+"],isReviewLateFee=["+latePayCharge.isReviewLateFee+"],无需不回溯滞纳金");
			return null;
		}else{
			logger.debug("滞纳金回溯:账户编号：["+acct.getAcctNbr()+"],账户类型:["+acct.getAcctType()+"],isReviewLateFee=["+latePayCharge.isReviewLateFee+"],回溯滞纳金");
		}
		List<CcsTxnHst> txnHstList = this.getLateFeeTxn(item, txnDate, bucketType);
		List<CcsStatement> statementList = getStatement(acct, txnDate);
		if(txnHstList.size() <= 0 || statementList.size() <= 0){
			logger.debug("滞纳金回溯信息收集:账户编号：["+acct.getAcctNbr()+"],账户类型:["+acct.getAcctType()+"]," +
					"交易日之后无入账滞纳金,或者无账单");
			return null;
		}
			
		int i =0;
		for(CcsStatement statement:statementList){
			if(statement.getStmtDate().compareTo(txnDate)<0){
				i++;
			}else{
				break;
			}
		}
		//所属的账单日
		CcsStatement ctdStatement = null;
		if(i == 0){
			ctdStatement = statementList.get(0);
		}else{
			ctdStatement = statementList.get(i-1);
		}
		
		logger.debug("滞纳金回溯信息收集:账户编号：["+acct.getAcctNbr()+"],账户类型:["+acct.getAcctType()+"]," +
				"交易日=["+postAmt+"],所属账单日=["+ctdStatement.getStmtDate()+"]");
		//当期
		List<CcsTxnHst> txnCList = this.getCTxn(item.getAccount(), txnDate,ctdStatement);
		BigDecimal cTxnSum = BigDecimal.ZERO;
		
		if(txnCList.size()>0){
			for(CcsTxnHst txnHst : txnCList){
				cTxnSum = cTxnSum.add(txnHst.getPostAmt());
			}
		}
		logger.debug("滞纳金回溯信息收集:账户编号：["+acct.getAcctNbr()+"],账户类型:["+acct.getAcctType()+"]," +
				"账单日之后-交易日之前的入账贷记金额:["+cTxnSum+"]");
		
		BigDecimal leteFeeAmt = BigDecimal.ZERO;
		for(int j=0;j < statementList.size();j++){
			CcsStatement statement = statementList.get(j);
			BigDecimal bal = BigDecimal.ZERO;
			//当期账单以后的才回溯
			if(statement.getStmtDate().compareTo(ctdStatement.getStmtDate())>=0 ){
				logger.debug("滞纳金回溯执行:账户编号：["+acct.getAcctNbr()+"],账户类型:["+acct.getAcctType()+"]," +
						"处理账单，账单日=["+statement.getStmtDate()+"]，该账单的最小还款额=["+statement.getTotDueAmt()+"]");
				if(statement.getStmtDate().compareTo(ctdStatement.getStmtDate())==0 ){
					bal = statement.getTotDueAmt().subtract(cTxnSum).subtract(leteFeeAmt);
				}else if(statement.getStmtDate().compareTo(ctdStatement.getStmtDate())>0){
					bal = statement.getTotDueAmt().subtract(leteFeeAmt);
				}
				logger.debug("滞纳金回溯执行:账户编号：["+acct.getAcctNbr()+"],账户类型:["+acct.getAcctType()+"]," +
						"交易金额:["+postAmt+"]"+"当期最小还款额除去还款金额和回溯罚金后的金额:["+bal+"]");
				if(postAmt.compareTo(bal)>=0){
					for(CcsTxnHst txnHst : txnHstList  ){
						//大于账单日回溯
						if(txnHst.getPostDate().compareTo(statement.getStmtDate()) > 0){
							//存在下一个账单日
							if(j < statementList.size()-1){
								logger.debug("滞纳金交易:交易:seq:["+txnHst.getTxnSeq()+"],交易码:["+txnHst.getTxnCode()+
										"]交易原始入账时间:["+txnHst.getPostDate()+"],下一账单的账单日:["+statementList.get(j+1).getStmtDate()+"]");
								//交易日期大于下一个账单日不回溯
								if(txnHst.getPostDate().compareTo(statementList.get(j+1).getStmtDate()) > 0){
									continue;
								}
							}
							if (logger.isDebugEnabled()) {
								logger.debug("滞纳金回溯执行:账户编号：["+acct.getAcctNbr()+"],账户类型:["+acct.getAcctType()+"],"+
											"] 交易:seq:["+txnHst.getTxnSeq()+"],交易码:["+txnHst.getTxnCode()+
											"]交易原始入账时间:["+txnHst.getPostDate()+"],交易金额:["+txnHst.getPostAmt()+"]");
							}
							this.generateReLateFee(item, txnHst, batchFacility.getBatchDate(),newTxnPosts, bucketType);
							leteFeeAmt = leteFeeAmt.add(txnHst.getPostAmt());
						}
					}
				}
			}
		}
		return leteFeeAmt;
		
	}
	/**
	 * 查询在交易日之后入账的滞纳金
	 * @param acct
	 * @param txnDate
	 * @param loan
	 * @return
	 */
	private List<CcsTxnHst> getLateFeeTxn(S6000AcctInfo item,Date txnDate, BucketType bucketType){
		CcsAcct acct = item.getAccount();
		// 修改查找滞纳金交易对应交易码
		SysTxnCdMapping sysTxnCd = null;
		if(item.getLoans() != null && item.getLoans().size() > 0 ){
			for(CcsLoan loan : item.getLoans()){
				if(loan.getLoanType() == LoanType.MCAT){
					sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S05), SysTxnCdMapping.class);
					break;
				}else if (loan.getLoanType() == LoanType.MCEI){
					if(bucketType == BucketType.ReplaceLatePaymentCharge)
						sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.D14), SysTxnCdMapping.class);
					else
						sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.D10), SysTxnCdMapping.class);
					
					break;
				}else{
					sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S05), SysTxnCdMapping.class);
					break;
				}
			}
		}else{
			sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S05), SysTxnCdMapping.class);
		}
		// 根据交易码，查找交易码对象
		TxnCd txnCode = parameterFacility.loadParameter(sysTxnCd.txnCd, TxnCd.class);
		//获取入账日在交易日txnDate之后,上一账单日之间的的所有入账交易
		QCcsTxnHst qccsTxnHst = QCcsTxnHst.ccsTxnHst;
		List<CcsTxnHst> txnHstList = new ArrayList<CcsTxnHst>();
		txnHstList = new JPAQuery(em).from(qccsTxnHst)
			.where(qccsTxnHst.postDate.goe(txnDate)
					.and(qccsTxnHst.acctNbr.eq(acct.getAcctNbr()).and(qccsTxnHst.acctType.eq(acct.getAcctType())))
					.and(qccsTxnHst.txnCode.eq(txnCode.txnCd))
					)
			.list(qccsTxnHst);
		return txnHstList;
	}
	
	private List<CcsTxnHst> getCTxn(CcsAcct acct,Date txnDate,CcsStatement statement){
		//获取入账日在账单日之后交易日txnDate之前,所有贷记入账交易
		QCcsTxnHst qccsTxnHst = QCcsTxnHst.ccsTxnHst;
		List<CcsTxnHst> txnHstList = new ArrayList<CcsTxnHst>();
		txnHstList = new JPAQuery(em).from(qccsTxnHst)
			.where(qccsTxnHst.postDate.gt(statement.getStmtDate())
					.and(qccsTxnHst.postDate.loe(txnDate))
					.and(qccsTxnHst.dbCrInd.eq(DbCrInd.C))
					.and(qccsTxnHst.acctNbr.eq(acct.getAcctNbr()).and(qccsTxnHst.acctType.eq(acct.getAcctType())))
					)
			.list(qccsTxnHst);
		return txnHstList;
	}
	
	private List<CcsStatement> getStatement(CcsAcct acct,Date txnDate){
		//获取交易日txnDate前的账单日
		QCcsStatement qCcsStatement = QCcsStatement.ccsStatement;
		List<CcsStatement> statementList = new ArrayList<CcsStatement>();
		statementList = new JPAQuery(em).from(qCcsStatement)
			.where(qCcsStatement.acctNbr.eq(acct.getAcctNbr())
					.and(qCcsStatement.acctType.eq(acct.getAcctType())))
					.orderBy(qCcsStatement.stmtDate.asc())
			.list(qCcsStatement);
		return statementList;
	}
	
	/**
	 * 滞纳金回溯，并入账
	 * @return
	 */
	public CcsPostingTmp generateReLateFee(S6000AcctInfo item, CcsTxnHst txnHst, Date batchDate, 
			List<CcsPostingTmp> newTxnPosts, BucketType bucketType) {

		// 查找系统内部交易类型对照表
		SysTxnCdMapping sysTxnCd = null;
		if(item.getLoans() != null && item.getLoans().size() > 0 ){
			for(CcsLoan loan : item.getLoans()){
				if(loan.getLoanType() == LoanType.MCAT){
					sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S10), SysTxnCdMapping.class);
					break;
				}else if (loan.getLoanType() == LoanType.MCEI){
					if(bucketType == BucketType.ReplaceLatePaymentCharge) 
						sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.C15), SysTxnCdMapping.class);
					else 
						sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.C10), SysTxnCdMapping.class);
					break;
				}else{
					sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S10), SysTxnCdMapping.class);
					break;
				}
			}
		}else{
			sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S10), SysTxnCdMapping.class);
		}
		// 根据交易码，查找交易码对象
		TxnCd txnCode = parameterFacility.loadParameter(sysTxnCd.txnCd, TxnCd.class);
		// 根据产品代码，查找超限参数对象
		ProductCredit productCr = parameterFacility.loadParameter(item.getAccount().getProductCd(), ProductCredit.class);
				
		// 交易主键生成
		CcsTxnSeq txnSeq = new CcsTxnSeq();
		txnSeq.setOrg(item.getAccount().getOrg());
		em.persist(txnSeq);
				
		String acqAcceptorId=productCr.financeOrgNo;
		
		// 生成内部交易
		String planNbr = planManager.getPlanNbrByTxnCd(txnCode, item.getAccount().getProductCd());
		CcsPostingTmp newTxn = generateTrans(txnSeq, item, item.getAccount().getDefaultLogicCardNbr(), item.getAccount().getDefaultLogicCardNbr(), productCr.productCd, txnCode, txnHst.getPostAmt(), txnHst.getTxnDate(), planNbr, txnHst.getRefNbr(),txnHst.getTerm(),acqAcceptorId);
		
		// 数据持久化
		em.persist(newTxn);
		newTxnPosts.add(newTxn);
		// 调用内部交易入账处理
		transactionPost.posting(item, newTxn, batchDate);
		
		return newTxn;
	}
	
	/**
	 * 
	 * @param txnSeq
	 * @param item
	 * @param cardNo
	 * @param logicCardNo
	 * @param productCd
	 * @param txnCd
	 * @param amt
	 * @param origTransDate
	 * @param planNbr
	 * @param refNbr
	 * @return
	 */
	private CcsPostingTmp generateTrans(CcsTxnSeq txnSeq,S6000AcctInfo item,String cardNo,String logicCardNo,String productCd,TxnCd txnCd,BigDecimal amt,Date origTransDate,String planNbr,String refNbr,Integer term,String acqAcceptorId){
		Date batchDate = batchFacility.getBatchDate();
		CcsPostingTmp newTxn = new CcsPostingTmp();
		newTxn.setOrg(item.getAccount().getOrg()); // 机构号
		newTxn.setTxnSeq(txnSeq.getTxnSeq()); // 交易流水号
		newTxn.setAcctNbr(item.getAccount().getAcctNbr()); // 账户编号
		newTxn.setAcctType(item.getAccount().getAcctType()); // 账户类型
		newTxn.setCardNbr(cardNo); // 介质卡号
		newTxn.setLogicCardNbr(logicCardNo); // 逻辑卡号
		newTxn.setCardBasicNbr(logicCardNo); // 逻辑卡主卡卡号
		newTxn.setProductCd(productCd); // 产品代码
		newTxn.setTxnDate(origTransDate); // 交易日期
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
		newTxn.setOrigTransDate(origTransDate); // 原始交易日期
		// 如何确定分期交易原先使用的卡产品？是使用帐户上的默认卡产品，还是要根据原交易来确定？
		newTxn.setPlanNbr(planNbr); // 信用计划号
		newTxn.setRefNbr(refNbr); // 交易参考号
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
		newTxn.setTerm(term);
		return newTxn;
	}

		
	
}


