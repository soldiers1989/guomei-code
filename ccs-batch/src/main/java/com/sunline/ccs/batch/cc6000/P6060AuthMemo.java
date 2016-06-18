package com.sunline.ccs.batch.cc6000;

import java.math.BigDecimal;
import java.util.Calendar;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.utils.DateUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoDelTmp;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.UnmatchStatus;
import com.sunline.ppy.dictionary.report.ccs.ExpiredAuthJournalRptItem;
import com.sunline.ppy.dictionary.report.ccs.MatchAuthJournalRptItem;
import com.sunline.ppy.dictionary.report.ccs.UnmatchAuthJournalRptItem;


/** 
 * @see 类名：P6060AuthMemo
 * @see 描述：统计账户层未达授权，输出匹配成功及过期授权
 *
 * @see 创建日期：   2015年6月25日 下午2:37:03
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class P6060AuthMemo implements ItemProcessor<S6000AcctInfo, S6000AcctInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private BatchStatusFacility batchFacility;
	@PersistenceContext
	private EntityManager em;

	@Override
	public S6000AcctInfo process(S6000AcctInfo item) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("未达授权处理：Org["+item.getAccount().getOrg()
					+"],AcctNo["+item.getAccount().getAcctNbr()
					+"],AcctType["+item.getAccount().getAcctType()
					+"],Unmatchs.size["+item.getUnmatchs().size()
					+"],UnmatchStatuses.size["+item.getUnmatchStatuses().size()
					+"]");
		}
		ProductCredit productC = parameterFacility.loadParameter(item.getAccount().getProductCd(), ProductCredit.class);
		
		// 清空授权未达账金额累计字段；初始化UNMATCH_DB、UNMATCH_CR、UNMATCH_CASH
		item.getAccount().setMemoCr(BigDecimal.ZERO);
		item.getAccount().setMemoDb(BigDecimal.ZERO);
		item.getAccount().setMemoCash(BigDecimal.ZERO);

		for (int i = 0; i < item.getUnmatchs().size(); i++) {
			if (logger.isDebugEnabled()) {
				logger.debug("Org["+item.getUnmatchs().get(i).getOrg()
						+"],LogKv["+item.getUnmatchs().get(i).getLogKv()
						+"],LogicalCardNo["+item.getUnmatchs().get(i).getLogicCardNbr()
						+"],LogBizDate["+item.getUnmatchs().get(i).getLogBizDate()
						+"],ChbTxnAmt["+item.getUnmatchs().get(i).getChbTxnAmt()
						+"],AuthCode["+item.getUnmatchs().get(i).getAuthCode()
						+"],B002["+item.getUnmatchs().get(i).getB002CardNbr()
						+"],TxnType["+item.getUnmatchs().get(i).getTxnType()
						+"],FinalAction["+item.getUnmatchs().get(i).getFinalAction()
						+"]");
			}
			/*
			 * 未达授权交易过期处理逻辑
			 * 判断未达授权交易（只针对未匹配成功的授权）的交易日期，若当天批量处理日期>=授权交易日期+授权交易保存天数，
			 * 则认为该笔授权过期，为该笔授权交易置上“授权交易过期标志”
			 */
			
			// 授权交易是否过期(reader只取正常+孤立)：
			// 预授权交易
			if(item.getUnmatchs().get(i).getTxnType() == AuthTransType.PreAuth) {
				//跑批日期 >= 授权交易日期+预授权交易保存天数，则过期 
				if(DateUtils.truncatedCompareTo(
						batchFacility.getBatchDate(), 
						DateUtils.addDays(item.getUnmatchs().get(i).getLogBizDate(), productC.preathRtnPrd), 
						Calendar.DATE) >= 0){
					item.getUnmatchStatuses().set(i, UnmatchStatus.E);
				}
			}
			// 其他授权交易
			else {
				//  贷记交易,"Credit|存款","AgentCredit|代付","TransferCredit|转入",
				if (AuthTransType.Credit == item.getUnmatchs().get(i).getTxnType()
						|| AuthTransType.AgentCredit == item.getUnmatchs().get(i).getTxnType()
						|| AuthTransType.TransferCredit == item.getUnmatchs().get(i).getTxnType()) {
					//跑批日期 >= 授权交易日期+授权交易保存天数，则过期 
					if(DateUtils.truncatedCompareTo(
							batchFacility.getBatchDate(), 
							DateUtils.addDays(item.getUnmatchs().get(i).getLogBizDate(), productC.unmatchCrRtnPrd), 
							Calendar.DATE) >= 0){
						item.getUnmatchStatuses().set(i, UnmatchStatus.E);
					}
				} else {
					//跑批日期 >= 授权交易日期+授权交易保存天数，则过期 
					if(DateUtils.truncatedCompareTo(
							batchFacility.getBatchDate(), 
							DateUtils.addDays(item.getUnmatchs().get(i).getLogBizDate(), productC.unmatchDbRtnPrd), 
							Calendar.DATE) >= 0){
						item.getUnmatchStatuses().set(i, UnmatchStatus.E);
					}
				}

			}
			
			
			/*
			 * 账务授权未达账交易统计逻辑
				将同时满足如下条件的交易纳入授权未达账的统计
				1、	授权交易未成功匹配
				2、	非已做预授权完成的预授权交易
				3、	非已过期的预授权交易
				4、	非撤销类、冲正类授权交易
				将符合如上条件的账户内授权交易按如下逻辑进行统计
				初始化UNMATCH_DB、UNMATCH_CR、UNMATCH_CASH和NBR_UNMATCH_DB、NBR_UNMATCH_CR、NBR_UNMATCH_CASH
				贷记交易；
				     1，每一笔授权加1到NBR_UNMATCH_CR；加交易金额到UNMATCH_CR；
				借记交易；
				     1，每一笔授权加1到NBR_UNMATCH_DB；加交易金额到UNMATCH_DB；
				     2，如果是现金交易还需要更新NBR_UNMATCH_CASH 、UNMATCH_CASH；
			 */
			// 授权未达账的统计，U-未匹配
			if (item.getUnmatchStatuses().get(i) == UnmatchStatus.U) {
				//  贷记交易,"Credit|存款","AgentCredit|代付","TransferCredit|转入",
				if (AuthTransType.Credit == item.getUnmatchs().get(i).getTxnType()
						|| AuthTransType.AgentCredit == item.getUnmatchs().get(i).getTxnType()
						|| AuthTransType.TransferCredit == item.getUnmatchs().get(i).getTxnType()) {
					// 成功的交易包含（未被确认的贷记交易、成功的确认交易、成功的孤立确认交易），重复确认状态异常不会到此
					// 增加账户未匹配贷记交易
					item.getAccount().setMemoCr(item.getAccount().getMemoCr().add(item.getUnmatchs().get(i).getChbTxnAmt()));
				} 
				// 借记交易
				else {
					// 成功的交易包括（未被撤冲的借记交易、匹配成功的预授权完成和结算通知交易、无原交易的预授权完成和结算通知[原交易金额记为零]、未被完成的预授权）
					// 已完成的预授权不会到此
					// 包含处理中的放款交易
					item.getAccount().setMemoDb(item.getAccount().getMemoDb().add(item.getUnmatchs().get(i).getChbTxnAmt()));
					// "Cash|取现"
					if (AuthTransType.Cash == item.getUnmatchs().get(i).getTxnType()
							|| AuthTransType.TransferDeditDepos == item.getUnmatchs().get(i).getTxnType()) {
						// 增加账户未匹配取现交易
						item.getAccount().setMemoCash(item.getAccount().getMemoCash().add(item.getUnmatchs().get(i).getChbTxnAmt()));
					}
				}
			}
			
			/*
			 *  待备份授权交易临时表生成逻辑
				将满足如下条件之一的交易输出到待备份授权交易临时表（需有标志说明备份并删除的原因，包括匹配、过期等）
				√	成功匹配授权交易
				√	已做预授权完成的预授权交易
				√	已过期的预授权交易
				√	撤销类、冲正类授权交易
			 */
			// 成功匹配授权交易
			if (item.getUnmatchStatuses().get(i) == UnmatchStatus.M) {
				// 授权成功匹配报表
				this.addMatchAuthJournal(item, item.getUnmatchs().get(i));
				// 授权删除表
				CcsAuthmemoDelTmp unmatchDelete = new CcsAuthmemoDelTmp();
				unmatchDelete.setOrg(item.getUnmatchs().get(i).getOrg());
				unmatchDelete.setLogKv(item.getUnmatchs().get(i).getLogKv());
				// 数据持久化
				em.persist(unmatchDelete);
				// 增加授权删除交易
				item.getUnmatchDeletes().add(unmatchDelete);
			}
			// 过期授权交易
			else if (item.getUnmatchStatuses().get(i) == UnmatchStatus.E) {
				// 过期授权报表
				this.addExpiredAuthJournal(item, item.getUnmatchs().get(i));
				// 授权删除表
				CcsAuthmemoDelTmp unmatchDelete = new CcsAuthmemoDelTmp();
				unmatchDelete.setOrg(item.getUnmatchs().get(i).getOrg());
				unmatchDelete.setLogKv(item.getUnmatchs().get(i).getLogKv());
				// 数据持久化
				em.persist(unmatchDelete);
				// 增加授权删除交易
				item.getUnmatchDeletes().add(unmatchDelete);
			}
			// 未匹配授权交易
			else if (item.getUnmatchStatuses().get(i) == UnmatchStatus.U){
				// 授权未匹配报表
				this.addUnmatchAuthJournal(item, item.getUnmatchs().get(i));
			}
		}
		// 对于小于零的未达账金额累计字段修正为零，减少业务风险；修正[UNMATCH_DB、UNMATCH_CASH]；UNMATCH_CR控制OTB没有风险，不需要修正
		// TODO zhengpy对于小于零的情况需要出异常报表
		if (item.getAccount().getMemoDb().compareTo(BigDecimal.ZERO) < 0) {
			item.getAccount().setMemoDb(BigDecimal.ZERO);
		}
		if (item.getAccount().getMemoCash().compareTo(BigDecimal.ZERO) < 0) {
			item.getAccount().setMemoCash(BigDecimal.ZERO);
		}

		return item;
	}

	/**
	 * 授权成功匹配报表
	 * @param acctInfo
	 * @param unmatchO
	 */
	public void addMatchAuthJournal(S6000AcctInfo acctInfo, CcsAuthmemoO unmatchO) {
		
		MatchAuthJournalRptItem matchAuthJournal = new MatchAuthJournalRptItem();
		matchAuthJournal.org = unmatchO.getOrg();
		matchAuthJournal.acctNo = unmatchO.getAcctNbr();
		matchAuthJournal.acctType = unmatchO.getAcctType();
		matchAuthJournal.cardNo = unmatchO.getB002CardNbr();
		matchAuthJournal.logOlTime = unmatchO.getLogOlTime();
		matchAuthJournal.glPostAmt = unmatchO.getChbTxnAmt();
		matchAuthJournal.authCode = unmatchO.getAuthCode();
		matchAuthJournal.txnStatus = unmatchO.getAuthTxnStatus();
		matchAuthJournal.txnType = unmatchO.getTxnType();
		matchAuthJournal.unmatchStatus = UnmatchStatus.M;
		
		acctInfo.getMatchAuthJournals().add(matchAuthJournal);
	}

	/**
	 * 过期授权报表
	 * @param acctInfo
	 * @param unmatchO
	 */
	public void addExpiredAuthJournal(S6000AcctInfo acctInfo, CcsAuthmemoO unmatchO) {
		
		ExpiredAuthJournalRptItem expiredAuthJournal = new ExpiredAuthJournalRptItem();
		expiredAuthJournal.org = unmatchO.getOrg();
		expiredAuthJournal.acctNo = unmatchO.getAcctNbr();
		expiredAuthJournal.acctType = unmatchO.getAcctType();
		expiredAuthJournal.cardNo = unmatchO.getB002CardNbr();
		expiredAuthJournal.logOlTime = unmatchO.getLogOlTime();
		expiredAuthJournal.glPostAmt = unmatchO.getChbTxnAmt();
		expiredAuthJournal.authCode = unmatchO.getAuthCode();
		expiredAuthJournal.txnStatus = unmatchO.getAuthTxnStatus();
		expiredAuthJournal.txnType = unmatchO.getTxnType();
		expiredAuthJournal.unmatchStatus = UnmatchStatus.E;
		
		acctInfo.getExpiredAuthJournals().add(expiredAuthJournal);
	}

	/**
	 * 授权未匹配报表
	 * @param acctInfo
	 * @param unmatchO
	 */
	public void addUnmatchAuthJournal(S6000AcctInfo acctInfo, CcsAuthmemoO unmatchO) {
		
		UnmatchAuthJournalRptItem unmatchAuthJournal = new UnmatchAuthJournalRptItem();
		unmatchAuthJournal.org = unmatchO.getOrg();
		unmatchAuthJournal.acctNo = unmatchO.getAcctNbr();
		unmatchAuthJournal.acctType = unmatchO.getAcctType();
		unmatchAuthJournal.cardNo = unmatchO.getB002CardNbr();
		unmatchAuthJournal.logOlTime = unmatchO.getLogOlTime();
		unmatchAuthJournal.txnAmt = unmatchO.getChbTxnAmt();
		unmatchAuthJournal.authCode = unmatchO.getAuthCode();
		unmatchAuthJournal.txnStatus = unmatchO.getAuthTxnStatus();
		unmatchAuthJournal.txnType = unmatchO.getTxnType();
		unmatchAuthJournal.unmatchStatus = UnmatchStatus.U;
		
		acctInfo.getUnmatchAuthJournals().add(unmatchAuthJournal);
	}
}
