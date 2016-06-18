package com.sunline.ccs.batch.cc6100;

import java.math.BigDecimal;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.batch.common.PrintStmtUtils;
import com.sunline.ccs.infrastructure.server.repos.RCcsTxnUnstatement;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnUnstatement;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.Indicator;
//import com.sunline.smsd.service.sdk.StmtMsgInterfaceItem;

/**
 * @see 类名：P6101Statement
 * @see 描述：账单交易生成
 *          生成实体账单当期交易接口文件（在账单日时生成、过滤掉MEMO交易）
 *          输入：账单日为当前批量日期的账单汇总信息，和未出账单交易信息
 *          输出：账单交易接口文件列表
 *
 * @see 创建日期：   2015-6-24上午10:38:18
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P6101Statement implements ItemProcessor<U6101Statement, S6101Statement> {
	// 积分交易是否出账单?暂时先出

	@Autowired
	private RCcsTxnUnstatement rTmTxnUnstmt;
	@Autowired
	private PrintStmtUtils printStmtUtils;
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	
	@Override
	public S6101Statement process(U6101Statement item) throws Exception {
		OrganizationContextHolder.setCurrentOrg(item.getStmtHst().getOrg());
		
		S6101Statement outItem = new S6101Statement();
		
		// 判断是否出账单
		if (item.getStmtHst().getStmtFlag() == Indicator.Y) {
			
			// 账单提醒短信
			if(item.getStmtHst().getQualGraceBal().compareTo(BigDecimal.ZERO) > 0){
				
//				outItem.getStmtMsgInterfaceItems().add(generateStmtMsg(item.getStmtHst(), acct));
			}
			
			// 账单汇总信息
			outItem.getStmtInterfaceItems().add(printStmtUtils.makeStmtItem(item.getStmtHst()));
		}
		
		 // 未出账单交易
		for (CcsTxnUnstatement txnUnstmt : item.getTxnUnstmts()) {
			
			// 出账单则出交易明细
			if (item.getStmtHst().getStmtFlag() == Indicator.Y) {
				// 输出到实体账单交易当期接口文件
				TxnCd txnCd = parameterFacility.loadParameter(txnUnstmt.getTxnCode(), TxnCd.class);
				if (txnCd.stmtInd) { // 非memo交易，需要输出账单
					outItem.getStmttxnInterfaceItems().add(printStmtUtils.makeStmttxnItem(txnUnstmt));
				}
			}
			
			// 清理未出账单交易历史表TM_TXN_UNSTMT
			rTmTxnUnstmt.delete(txnUnstmt);
		}
		
		return outItem;
	}

	/**
	 * @see 方法名：generateStmtMsg 
	 * @see 描述：创建账单提醒短信内容
	 * @see 创建日期：2015-6-24上午10:40:57
	 * @author ChengChun
	 *  
	 * @param stmtHst
	 * @param acct
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
/*	private StmtMsgInterfaceItem generateStmtMsg(CcsStatement stmtHst, CcsAcct acct) {
		StmtMsgInterfaceItem item = new StmtMsgInterfaceItem();
		
		// 获取卡片
		CcsCard card = queryFacility.getCardByLogicCardNbr(stmtHst.getDefaultLogicCardNbr());
		
		item.cardNo = batchUtils.getCardNoBySendMsgCardType(acct, card);
		item.msgCd = fetchMsgCdService.fetchMsgCd(acct.getProductCd(), CPSMessageCategory.CPS037);
		item.currencyCd = stmtHst.getCurrency();
		item.custName = stmtHst.getName();
		item.due = stmtHst.getTotDueAmt();
		item.gender = stmtHst.getGender();
		item.graceBalance = stmtHst.getQualGraceBal().compareTo(BigDecimal.ZERO)>0 ? stmtHst.getQualGraceBal() : stmtHst.getStmtCurrBal(); //无欠款取STMT_CURR_BAL
		item.mobileNo = stmtHst.getMobileNo();
		item.org = stmtHst.getOrg();
		item.paymentDate = stmtHst.getPmtDueDate();
		item.stmtDate = stmtHst.getStmtDate();
		return item;
	}*/
}
