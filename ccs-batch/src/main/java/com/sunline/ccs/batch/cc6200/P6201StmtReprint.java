package com.sunline.ccs.batch.cc6200;

import java.util.Iterator;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.batch.cc6100.S6101Statement;
import com.sunline.ccs.batch.common.PrintStmtUtils;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsStatement;
import com.sunline.ccs.infrastructure.server.repos.RCcsStmtReprintReg;
import com.sunline.ccs.infrastructure.server.repos.RCcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsStatement;
import com.sunline.ccs.infrastructure.shared.model.CcsStmtReprintReg;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsStatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnHst;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;

/**
 * @see 类名：P6201StmtReprint
 * @see 描述：补打账单接口文件生成
 *          输入：补打账单通知文件
 *          输出：账单汇总信息接口、账单的账单交易接口
 *
 * @see 创建日期：   2015-6-24下午2:07:06
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P6201StmtReprint implements ItemProcessor<CcsStmtReprintReg, S6101Statement> {
	/**
	 * TM_STMT_HST 账单汇总历史表
	 */
	@Autowired
	private RCcsStatement rTmStmtHst;

	/**
	 * TM_TXN_HST 入账交易历史表
	 */
	@Autowired
	private RCcsTxnHst rTmTxnHst;

	@Autowired
	private RCcsAcct rTmAccount;
	
	@Autowired
	private RCcsStmtReprintReg rTmReprintReg;

	@Autowired
	private PrintStmtUtils printStmtUtils;
	
	/**
	 * 获取参数类
	 */
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	
	@Override
	public S6101Statement process(CcsStmtReprintReg item) throws Exception {
		OrganizationContextHolder.setCurrentOrg(item.getOrg());

		/*
		 * 根据补打账单记录，取得指定账户指定账单日的账单汇总历史表TM_STMT_HST记录
		 */
		QCcsStatement qTmStmtHst = QCcsStatement.ccsStatement;
		Iterable<CcsStatement> tmStmtHstIterable = rTmStmtHst.findAll(
				qTmStmtHst.org.eq(item.getOrg()).and(
				qTmStmtHst.acctNbr.eq(item.getAcctNbr()).and(
				qTmStmtHst.stmtDate.eq(item.getStmtDate()))));

		/*
		 * 根据补打账单记录，获取指定账户指定账单日的入账交易历史表TM_TXN_HST记录
		 */
		QCcsTxnHst qTmTxnHst = QCcsTxnHst.ccsTxnHst;
		Iterable<CcsTxnHst> txnHstIterable = rTmTxnHst.findAll(
				qTmTxnHst.org.eq(item.getOrg()).and(
				qTmTxnHst.acctNbr.eq(item.getAcctNbr()).and(
				qTmTxnHst.stmtDate.eq(item.getStmtDate()))));

		/*
		 * 生成账单交易历史记录 
		 */
		S6101Statement outputItem = new S6101Statement();
		CcsTxnHst tmTxnHst = null;
		for (Iterator<CcsTxnHst> iterator = txnHstIterable.iterator(); iterator.hasNext();) { // 循环账户的未出账单交易
			tmTxnHst = iterator.next();

			// 记录账单交易历史，并清理未出账单交易历史表
			// 输出到实体账单交易当期接口文件
			TxnCd txnCd = parameterFacility.loadParameter(tmTxnHst.getTxnCode(), TxnCd.class);
			if (txnCd.stmtInd) { // 非memo交易，需要输出账单
				outputItem.getStmttxnInterfaceItems().add(printStmtUtils.makeStmttxnItem(tmTxnHst));
			}
		}

		// 生成补打账单汇总信息接口文件
		CcsAcctKey accountKey = null;
		CcsAcct account = null;
		CcsStatement stmtHst = null;
		for (Iterator<CcsStatement> iterator = tmStmtHstIterable.iterator(); iterator.hasNext();) { // 循环账户的未出账单交易
			stmtHst = iterator.next();
			accountKey = new CcsAcctKey();
			accountKey.setAcctNbr(stmtHst.getAcctNbr());
			accountKey.setAcctType(stmtHst.getAcctType());
			account = rTmAccount.findOne(accountKey);
			assert account != null;
			outputItem.getStmtInterfaceItems().add(printStmtUtils.makeStmtItem(stmtHst, account));
		}

		rTmReprintReg.delete(item);
		
		return outputItem;
	}
}
