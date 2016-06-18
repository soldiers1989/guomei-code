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
import com.sunline.ccs.batch.cc6000.common.AgeController;
import com.sunline.ccs.batch.cc6000.common.PlanManager;
import com.sunline.ccs.batch.cc6000.common.TransactionPost;
import com.sunline.ccs.batch.common.BatchUtils;
import com.sunline.ccs.facility.Card2ProdctAcctFacility;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnSeq;
import com.sunline.ccs.infrastructure.shared.model.QCcsStatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnHst;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.pcm.param.def.Mulct;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.PostTxnType;
import com.sunline.ppy.dictionary.enums.PostingFlag;



/**
 * @see 类名：MulctAccrue
 * @see 描述： 罚金回溯
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class MulctAccrue{
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
	@Autowired
	private MicroCreditRescheduleUtils rescheduleUtils;
	@Autowired
	private BatchUtils batchUtils;
	@Autowired
	private AgeController ageController;
	@Autowired
	private Card2ProdctAcctFacility productFacility;
	/**
	 * 罚金回溯
	 * @param item
	 * @param postAmt 还款的金额
	 */
	public void accumulateMulct(S6000AcctInfo item,BigDecimal postAmt,Date txnDate, List<CcsPostingTmp> newTxnPosts)
	{

		List<CcsLoan> loans = item.getLoans();
		for(CcsLoan loan:loans){
			LoanFeeDef loanFeeDef = rescheduleUtils.getLoanFeeDef(loan.getLoanCode(), loan.getLoanInitTerm(),
					loan.getLoanInitPrin(),loan.getLoanFeeDefId());
			Mulct fine = parameterFacility.loadParameter(loanFeeDef.mulctTableId, Mulct.class);
			if(loanFeeDef.mulctTableId == null || "".equals(loanFeeDef.mulctTableId)){
				logger.debug("无对应的罚金利率表，不回溯罚金");
			}else if(item.getAccount().getAgeCode().equals("C") || item.getAccount().getAgeCode().equals("0")){
				logger.debug("账户账龄=["+item.getAccount().getAgeCode()+"]，无需回溯罚金");
			}else if(fine.isReviewMulct.equals(Indicator.N)){
				logger.debug("罚金参数设置isReviewMulct=["+fine.isReviewMulct+"]，无需回溯罚金");
			}else{
				List<CcsTxnHst> txnHstList = this.getMulctTxn(item.getAccount(), txnDate, loan);
				
				if(txnHstList.size()>0){	
					AccountAttribute acctAttr = productFacility.CardNoTOAccountAttribute(loan.getCardNbr(), loan.getAcctType().getCurrencyCode());
					switch (fine.mulctMethod) {
					case CPD:
						// CPD的罚金回溯
						this.accumulateCPDMulct(item, loan, postAmt, txnDate, newTxnPosts, txnHstList,loanFeeDef,acctAttr);
						break;
					case DPD:
						//  DPD的罚金回溯
						this.accumulateDPDMulct(item, loan, postAmt, txnDate, newTxnPosts,txnHstList,loanFeeDef,acctAttr);
						break;
					default:
						break;
					}
				}else{
					logger.debug("无对应的罚金回溯交易！");
				}
			}
			
		}
	}
	private List<CcsTxnHst> getMulctTxn(CcsAcct acct,Date txnDate,CcsLoan loan){
		SysTxnCdMapping sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S73), SysTxnCdMapping.class);
		// 根据交易码，查找交易码对象
		TxnCd txnCode = parameterFacility.loadParameter(sysTxnCd.txnCd, TxnCd.class);
		//获取入账日在交易日txnDate之后的所有入账罚金交易
		QCcsTxnHst qccsTxnHst = QCcsTxnHst.ccsTxnHst;
		List<CcsTxnHst> txnHstList = new ArrayList<CcsTxnHst>();
		txnHstList = new JPAQuery(em).from(qccsTxnHst)
			.where(qccsTxnHst.postDate.goe(txnDate)
					.and(qccsTxnHst.acctNbr.eq(acct.getAcctNbr()).and(qccsTxnHst.acctType.eq(acct.getAcctType())))
					.and(qccsTxnHst.refNbr.eq(loan.getRefNbr()))
					.and(qccsTxnHst.txnCode.eq(txnCode.txnCd))
					)
			.list(qccsTxnHst);
		return txnHstList;
	}
	
	/**
	 * 获取所有借记类交易，判断回溯钱余额时，应将所有借记类交易金额减掉
	 * @param acct
	 * @param txnDate
	 * @param loan
	 * @return
	 */
	private List<CcsTxnHst> getDbTxn(CcsAcct acct,Date txnDate,CcsLoan loan){
		//获取入账日在交易日txnDate之后的所有借记入账交易
		QCcsTxnHst qccsTxnHst = QCcsTxnHst.ccsTxnHst;
		List<CcsTxnHst> txnHstList = new ArrayList<CcsTxnHst>();
		txnHstList = new JPAQuery(em).from(qccsTxnHst)
			.where(qccsTxnHst.postDate.goe(txnDate)
					.and(qccsTxnHst.acctNbr.eq(acct.getAcctNbr()).and(qccsTxnHst.acctType.eq(acct.getAcctType())))
					.and(qccsTxnHst.refNbr.eq(loan.getRefNbr()))
					.and(qccsTxnHst.dbCrInd.eq(DbCrInd.D))
					)
			.list(qccsTxnHst);
		return txnHstList;
	}
	/**
	 * 执行DPD的罚金回溯
	 * @param item
	 * @param loan
	 * @param postAmt
	 * @param txnDate
	 * @param newTxnPosts
	 * @param txnHstList
	 */
	private void accumulateDPDMulct(S6000AcctInfo item,CcsLoan loan,BigDecimal postAmt,Date txnDate, List<CcsPostingTmp> newTxnPosts,List<CcsTxnHst> txnHstList,LoanFeeDef loanFeeDef,AccountAttribute acctAttr){
		//贷款的逾期起始日期不为空才执行利息回溯,不为空说明有逾期
		if(loan.getOverdueDate() != null){
			Date oldOverdueDate = loan.getOverdueDate();
			if (logger.isDebugEnabled()) {
				logger.debug("DPD罚金回溯：loanid：["+loan.getLoanId()+"],旧的的DPD逾期起始日期：oldDPDOverdueDate：["+oldOverdueDate+"]");
			}
				
			//重新计算逾期起始日期
			Date newOverdueDate = this.updateDPDOverdueDate(loan, item.getPlans(),postAmt,txnHstList,txnDate,loanFeeDef, item.getAccount(), acctAttr);
				
			if(newOverdueDate == null){
				newOverdueDate =batchFacility.getBatchDate();
			}
			if (logger.isDebugEnabled()) {
				logger.debug("DPD罚金回溯:loanid：["+loan.getLoanId()+"],新的DPD逾期起始日期：newDPDOverdueDate:["+newOverdueDate+"]");
			}
			//如果新的逾期起始日期比旧的晚，开始回溯
			if(oldOverdueDate.compareTo(newOverdueDate)<0){
				if (logger.isDebugEnabled()) {
					logger.debug("DPD罚金回溯:loanid：["+loan.getLoanId()+"] 开始回溯交易");
				}
				//生成根据入账罚金交易生成回溯交易
				for(CcsTxnHst txnHst : txnHstList  ){
					if (logger.isDebugEnabled()) {
						logger.debug("DPD罚金回溯交易:loanid：["+loan.getLoanId()+"] 交易:seq:["+txnHst.getTxnSeq()+"],交易码:["+txnHst.getTxnCode()+
								"]交易原始入账时间:["+txnHst.getPostDate()+"],交易金额:["+txnHst.getPostAmt()+"]");
					}
					this.generateReMulct(item, txnHst, batchFacility.getBatchDate(),newTxnPosts);
				}
				loan.setLastPenaltyDate(newOverdueDate);
			}
		}
	}
	/**
	 * 执行CPD的罚金回溯
	 * @param item
	 * @param loan
	 * @param postAmt
	 * @param txnDate
	 * @param newTxnPosts
	 * @param txnHstList
	 */
	private void accumulateCPDMulct(S6000AcctInfo item,CcsLoan loan,BigDecimal postAmt,Date txnDate, List<CcsPostingTmp> newTxnPosts,List<CcsTxnHst> txnHstList,LoanFeeDef loanFeeDef,AccountAttribute acctAttr){
		//贷款的逾期起始日期不为空才执行利息回溯,不为空说明有逾期
		if(loan.getCpdBeginDate() != null){
			Date oldOverdueDate = loan.getCpdBeginDate();
			if (logger.isDebugEnabled()) {
				logger.debug("CPD罚金回溯：loanid：["+loan.getLoanId()+"],旧的的CPD逾期起始日期：oldCPDOverdueDate：["+oldOverdueDate+"]");
			}
				
			//重新计算逾期起始日期
			Date newOverdueDate = this.updateCPDOverdueDate(loan, item.getPlans(),postAmt,txnHstList,txnDate,loanFeeDef,item.getAccount(), acctAttr);
			if(newOverdueDate == null){
				newOverdueDate =batchFacility.getBatchDate();
			}
			if(newOverdueDate.compareTo(oldOverdueDate)>0){
				if (logger.isDebugEnabled()) {
					logger.debug("CPD罚金回溯:loanid：["+loan.getLoanId()+"],新的CPD逾期起始日期：newCPDOverdueDate:["+newOverdueDate+"] 开始回溯交易");
				}
				//生成根据入账罚金交易生成回溯交易
				for(CcsTxnHst txnHst : txnHstList  ){
					if (logger.isDebugEnabled()) {
						logger.debug("CPD罚金回溯交易:loanid：["+loan.getLoanId()+"] 交易:seq:["+txnHst.getTxnSeq()+"],交易码:["+txnHst.getTxnCode()+
								"]交易原始入账时间:["+txnHst.getPostDate()+"],交易金额:["+txnHst.getPostAmt()+"]");
					}
					this.generateReMulct(item, txnHst, batchFacility.getBatchDate(),newTxnPosts);
				}
				loan.setCpdBeginDate(newOverdueDate);
				loan.setLastPenaltyDate(newOverdueDate);
			}
		}
	}
	
	/**
	 * 试算loan的DPD逾期日期
	 * @param loan
	 * @param plans
	 * @param postAmt 参与试算的金额
	 * @param txnHstList 上一逾期日期后收取的罚金
	 */
	public Date updateDPDOverdueDate(CcsLoan loan, List<CcsPlan> plans,BigDecimal postAmt,List<CcsTxnHst> txnHstList,Date txnDate,LoanFeeDef loanFeeDef,CcsAcct acct,AccountAttribute acctAttr){
		
		//如果是随借随还
		if(loan.getLoanType() == LoanType.MCAT){
			String oldAgeCode = acct.getAgeCode();
			String newAgeCode = updateAgeCode(acct, acctAttr, postAmt);
			logger.debug("罚金回溯，更新DPD,loanType:["+loan.getLoanType()+"],oldAgeCode:["+oldAgeCode+"],newAgeCode:["+newAgeCode+"]");
			if(newAgeCode.equals("C") || newAgeCode.equals("0")){
				return null;
			}else{
				return rescheduleUtils.getNextPaymentDay(acct.getProductCd(), ageController.getCycleDate(acct.getNextStmtDate(), acct.getCycleDay(), newAgeCode));
			}
		}else{
			Date earlest = null;//拖欠最早期数
			plans = this.sortCcsPlan(plans);//对plan进行排序
			for(CcsPlan plan : plans){
				int term = plan.getTerm()==null?0:plan.getTerm();
				if(loan.getRefNbr().equals(plan.getRefNbr()) &&  plan.getPlanType().isXfrIn() && loan.getCurrTerm()>=term){
					BigDecimal mulctAmt = BigDecimal.ZERO;
					//最新的dpd规则是不算罚金
					mulctAmt=plan.getCtdMulctAmt().add(plan.getPastMulctAmt());
					BigDecimal penaltyAmt = BigDecimal.ZERO;
					//最新的dpd规则是不算罚xi
					penaltyAmt=penaltyAmt.add(plan.getCtdPenalty().add(plan.getPastPenalty()));
					//最新的dpd规则是不算复利
					penaltyAmt=penaltyAmt.add(plan.getCtdCompound().add(plan.getPastCompound()));
					//最新的dpd规则是不算滞纳金
					penaltyAmt=penaltyAmt.add(plan.getCtdLateFee().add(plan.getPastLateFee()));
					//不计算代收罚金、代收滞纳金、代收罚息（DPD规则只关注期款是否偿还）
					penaltyAmt=penaltyAmt.add(plan.getCtdReplaceMulct().add(plan.getPastReplaceMulct())
							.add(plan.getCtdReplacePenalty().add(plan.getPastReplacePenalty()))
							.add(plan.getCtdReplaceLateFee().add(plan.getPastReplaceLateFee())));
					//扣减此部分的罚金，扣减还款金额,得出失算的金额
					BigDecimal planCurrBal = plan.getCurrBal();
					if(planCurrBal.compareTo(postAmt)<0){
						postAmt = postAmt.subtract(planCurrBal);
						planCurrBal = BigDecimal.ZERO;
					}else{
						planCurrBal = planCurrBal.subtract(postAmt);
						postAmt=BigDecimal.ZERO;
					}
					if(planCurrBal.subtract(loanFeeDef.dpdToleLmt.add(mulctAmt).add(penaltyAmt)).compareTo(BigDecimal.ZERO)>0){
						earlest = rescheduleUtils.getNextPaymentDay(acct.getProductCd(), plan.getPlanAddDate());
						break;
					}
				}
			}
			if(earlest != null){
				return earlest;
			}else{
				return null;
			}
		}
	
	}
	/**
	 * 试算loan的CPD逾期日期
	 * @param loan
	 * @param plans
	 * @param postAmt 参与试算的金额
	 * @param txnHstList 上一逾期日期后收取的罚金
	 */
	public Date updateCPDOverdueDate(CcsLoan loan, List<CcsPlan> plans,BigDecimal postAmt,List<CcsTxnHst> txnHstList,Date txnDate,LoanFeeDef loanFeeDef,CcsAcct acct,AccountAttribute acctAttr){
		//如果是随借随还
		if(loan.getLoanType() == LoanType.MCAT){
			String oldAgeCode = acct.getAgeCode();
			String newAgeCode = updateAgeCode(acct, acctAttr, postAmt);
			logger.debug("罚金回溯，更新CPD,loanType:["+loan.getLoanType()+"],oldAgeCode:["+oldAgeCode+"],newAgeCode:["+newAgeCode+"]");
			if(newAgeCode.equals("C") || newAgeCode.equals("0")){
				return null;
			}else{
				if(Integer.parseInt(newAgeCode) <= this.getStatement(acct, txnDate)){
					return null;
				}else{
					return loan.getCpdBeginDate();
				}
			}
		}else{
			Date earlest = null;//拖欠最早期数
			plans = this.sortCcsPlan(plans);//对plan进行排序
			// 获取交易日期后发生的所有借记交易
			List<CcsTxnHst> dbTxnList = this.getDbTxn(acct, txnDate, loan);
			for(CcsPlan plan : plans){
				int term = plan.getTerm()==null?0:plan.getTerm();
				if(loan.getRefNbr().equals(plan.getRefNbr()) &&  plan.getPlanType().isXfrIn() && loan.getCurrTerm()>=term){
					//转入计划的期数在贷款当前期数内，并且是转入计划
					//获取当前期数的下一期日期
					Date nextTermDate = this.getNextTermPlan(loan, plans, term+1);
					if(nextTermDate == null){
						nextTermDate = batchFacility.getBatchDate();
					}
/*					BigDecimal mulctAmt = BigDecimal.ZERO;
					//该计划的下一期的日期小于交易日期，此计划的余额全部是应收的
					if(nextTermDate.compareTo(txnDate)>0){
						for(CcsTxnHst ccsTxnHst: txnHstList){
							//循环上一逾期日期之后的所有罚金交易，如果入账日期大于当前plan的建立日期，并且小于下一plan的建立日期
							//说明该笔罚金是在扣款日之后生成入到该笔plan的罚金，余额中减去此部分
							Date beginDate = plan.getPlanAddDate();
							if(txnDate.compareTo(beginDate)>0){
								beginDate= txnDate;
							}
							if(ccsTxnHst.getPostDate().compareTo(beginDate)>=0 && ccsTxnHst.getPostDate().compareTo(nextTermDate)<=0){
								mulctAmt=mulctAmt.add(ccsTxnHst.getPostAmt());
							}
						}
					}*/
					
					BigDecimal dbAmt = BigDecimal.ZERO;
					for(CcsTxnHst ccsTxnHst: dbTxnList){
						//说明该笔罚金是在扣款日之后生成入到该笔plan的罚金，余额中减去该罚金和其他借记金额，加上贷记金额
						Date beginDate = plan.getPlanAddDate();
						if(txnDate.compareTo(beginDate)>0){
							beginDate= txnDate;
						}
						if(ccsTxnHst.getPostDate().compareTo(beginDate)>=0 && ccsTxnHst.getPostDate().compareTo(nextTermDate)<=0){
							dbAmt=dbAmt.add(ccsTxnHst.getPostAmt());
						}
					}
					
					BigDecimal planCurrBal = plan.getCurrBal().subtract(dbAmt);
					if(planCurrBal.compareTo(postAmt)<0){
						postAmt = postAmt.subtract(planCurrBal);
						planCurrBal = BigDecimal.ZERO;
					}else{
						planCurrBal = planCurrBal.subtract(postAmt);
						postAmt=BigDecimal.ZERO;
					}
					if(planCurrBal.compareTo(loanFeeDef.cpdToleLmt)>0){
						earlest = rescheduleUtils.getNextPaymentDay(acct.getProductCd(), plan.getPlanAddDate());
						break;
					}
					
				}
			}
			if(earlest != null){
				if(txnDate.compareTo(earlest)>=0){
					return loan.getCpdBeginDate();
				}else{
					return earlest;
				}
			}else{
				return null;
			}
		}
		
	}
	 /**
	 * 信用计划建立日期进行排序
	 * @param arr
	 * @return
	 */
   private List<CcsPlan> sortCcsPlan(List<CcsPlan> plans) { // 交换排序->冒泡排序
	   CcsPlan plan = null;
        boolean exchange = false;
        for (int i = 0; i < plans.size(); i++) {
            exchange = false;
            for (int j = plans.size() - 2; j >= i; j--) {
                if (plans.get(j + 1).getPlanAddDate().compareTo(plans.get(j).getPlanAddDate()) <= 0) {
                	plan = plans.get(j + 1);
                	plans.set(j + 1, plans.get(j));
                	plans.set(j, plan);
                    exchange = true;
                }
            }
            if (!exchange)
                break;
        }
        return plans;
    }
	public Date getNextTermPlan(CcsLoan loan, List<CcsPlan> plans,int nextTerm){
		
		for(CcsPlan plan : plans){
			int term = plan.getTerm()==null?0:plan.getTerm();
			if(loan.getRefNbr().equals(plan.getRefNbr()) && nextTerm==term){
				return plan.getPlanAddDate();
			}
		}
		return batchFacility.getBatchDate();
		
	}
	
   	/**
   	 * 根据金额试算账户的账龄
   	 * @param acct
   	 * @param acctAttr
   	 * @param txnAmt
   	 * @return
   	 */
	private String updateAgeCode(CcsAcct acct,AccountAttribute acctAttr,BigDecimal txnAmt){
		// 新账龄
		String newAgeCd = null;
		BigDecimal currbal = acct.getCurrBal().subtract(txnAmt);
		// 账龄列表索引号
		for (char c : "987654321".toCharArray()){
			BigDecimal totDue = BigDecimal.ZERO;
			// 累加该账期未还款最小还款额
			totDue = ageController.getMinDueByAgeCd(acct, c);
			// 判断已累加未还款最小还款额是否大于账龄阈值
			if(totDue.compareTo(txnAmt)<=0){
				txnAmt = txnAmt.subtract(totDue);
				totDue = BigDecimal.ZERO;
			}else{
				totDue = totDue.subtract(txnAmt);
				txnAmt = BigDecimal.ZERO;
			}
			if (totDue.compareTo(acctAttr.delqTol) > 0 ){
				newAgeCd = String.valueOf(c);
				break;
			}
		}
	
		// 如果新账龄仍为null,则根据当前余额的数值判断账龄代码
		if (newAgeCd == null){
			switch (currbal.compareTo(BigDecimal.ZERO)){
			case -1 : 
				// 有溢缴款
				newAgeCd = "C"; break;
			case 0 :
				// 当前未欠款 newAgeCd为age0
			case 1 :
				// 当前有欠款
				newAgeCd = "0"; break;
			}
		}
		return newAgeCd;
		
	}
	private int getStatement(CcsAcct acct,Date txnDate){
		//获取交易日txnDate前的账单日
		QCcsStatement qCcsStatement = QCcsStatement.ccsStatement;
		int size = 0;
		size = new JPAQuery(em).from(qCcsStatement)
			.where(qCcsStatement.acctNbr.eq(acct.getAcctNbr())
					.and(qCcsStatement.acctType.eq(acct.getAcctType()))
					.and(qCcsStatement.stmtDate.goe(txnDate)))
			.list(qCcsStatement).size();
		return size;
	}
	
	/**
	 * 罚金回溯，并入账
	 * @return
	 */
	public CcsPostingTmp generateReMulct(S6000AcctInfo item, CcsTxnHst txnHst, Date batchDate, List<CcsPostingTmp> newTxnPosts) {

		// 查找系统内部交易类型对照表
		SysTxnCdMapping sysTxnCd = parameterFacility.loadParameter(String.valueOf(SysTxnCd.S74), SysTxnCdMapping.class);
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


