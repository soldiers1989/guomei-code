package com.sunline.ccs.batch.cc7000;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCashloanDepoLog;
import com.sunline.ccs.infrastructure.shared.model.CcsCashloanDepoLogHst;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsCashloanDepoLog;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.param.def.LoanLendWay;
import com.sunline.ccs.param.def.Organization;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.UnmatchStatus;
import com.sunline.ppy.dictionary.exchange.DdOnlineSuccIntefaceItem;
import com.sunline.ppy.dictionary.exchange.DdRequestInterfaceItem;
//import com.sunline.smsd.service.sdk.CashLoanDirectCreditMsgItem;

/**
 * @see 类名：P7011CashLoan
 * @see 描述：现金分期交易 批量放款出 现金放款文件及短信 实时放款出放款成功文件
 *
 * @see 创建日期：   2015-6-24下午2:08:59
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P7011CashLoan implements ItemProcessor<S7000TxnProduce, S7000TxnProduce> {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@PersistenceContext
	private EntityManager em;

	@Override
	public S7000TxnProduce process(S7000TxnProduce i6600) throws Exception {
		
		try {
			CcsTxnHst txn = i6600.getTmTxnHst();
			SysTxnCdMapping sysTxnCdMapping = parameterFacility.retrieveParameterObject(SysTxnCd.S36.toString(), SysTxnCdMapping.class);
			Organization org = parameterFacility.loadParameter(null ,Organization.class);
			
			// refNbr与loan相匹配的现金分期交易，放款
			if(txn.getRefNbr()!=null && sysTxnCdMapping!=null && sysTxnCdMapping.txnCd.equals(txn.getTxnCode()) ){
				logger.debug("现金分期交易处理，账号{}，交易参考号{}", txn.getAcctNbr()+""+txn.getAcctType(), txn.getRefNbr());
				
				QCcsLoan qTmLoan = QCcsLoan.ccsLoan;
				QCcsCashloanDepoLog qTmLendingLog = QCcsCashloanDepoLog.ccsCashloanDepoLog;
				JPAQuery query = new JPAQuery(em);
				CcsLoan loan = query.from(qTmLoan)
						.where(qTmLoan.acctNbr.eq(txn.getAcctNbr())
								.and(qTmLoan.refNbr.eq(txn.getRefNbr()))
								.and(qTmLoan.loanType.eq(LoanType.C)))
						.singleResult(qTmLoan);
				if(loan != null){
					CcsAcct acct = queryFacility.getAcctByAcctNbr(txn.getAcctType(), txn.getAcctNbr());
					
					if(org.cashLoanSendMode == LoanLendWay.O) {
						JPAQuery queryLend = new JPAQuery(em); 
						CcsCashloanDepoLog lend = queryLend.from(qTmLendingLog).where(qTmLendingLog.refNbr.eq(txn.getRefNbr())
								.and(qTmLendingLog.b102AcctIdent1.eq(txn.getCardNbr()))).singleResult(qTmLendingLog);
						if(lend!=null){
							i6600.setOnlineSuccIntefaceFile(generateOnlineSuccIntefaceFile(lend));
							
							CcsCashloanDepoLogHst lendHst = new CcsCashloanDepoLogHst();
							lendHst.updateFromMap(lend.convertToMap());
							lendHst.setMemoStatus(UnmatchStatus.M);
							
							em.persist(lendHst);
							em.remove(lend);
							
							logger.debug("导出实时放款对账文件");
							
						}else{
							logger.error("未匹配到TmLendingLog");
						}
					}else{
						i6600.setCashLoanDirectCreditFile(generateCashLoanDirectCreditFile(acct, txn));
//						i6600.setCashLoanDirectCreditMsg(generateCashLoanDirectCreditMsg(acct, loan));
						
						logger.debug("导出批量放款文件");
					}
				}else{
					//以后考虑出报表
					logger.debug("未匹配到分期记录");
				}
			}
			
			return i6600;
		} catch (Exception e) {
			logger.error("现金分期交易出现金放款文件or实时放款成功文件or短信异常，交易参考号{}", i6600.getTmTxnHst().getRefNbr());
			throw e;
		}
	}

	
	/**
	 * @see 方法名：generateCashLoanDirectCreditFile 
	 * @see 描述：生成现金分期放款文件
	 * @see 创建日期：2015-6-24下午2:09:41
	 * @author ChengChun
	 *  
	 * @param acct
	 * @param txn
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private DdRequestInterfaceItem generateCashLoanDirectCreditFile(CcsAcct acct, CcsTxnHst txn){
		DdRequestInterfaceItem item = new DdRequestInterfaceItem();
		
		item.acctNo = acct.getAcctNbr();
		item.acctType = acct.getAcctType();
		item.name = acct.getName();
		item.dbBankAcctName = acct.getDdBankAcctName();
		item.ddAmount = txn.getTxnAmt();
		item.ddBankAcctNo = acct.getDdBankAcctNbr();
		item.ddBankBranch = acct.getDdBankBranch();
		item.ddBankName = acct.getDdBankName();
		item.owningBranch = acct.getOwningBranch();
		item.ddDate = acct.getDdDate();
		//	TODO 逻辑卡和客户物理卡号可能不一致，后来需要考虑如何获取物理卡号
		item.defaultCardNo = acct.getDefaultLogicCardNbr();
		item.directDbInd = acct.getDdInd();
		item.org = acct.getOrg();
		
		return item;
	}
	
	
	/**
	 * @see 方法名：generateCashLoanDirectCreditMsg 
	 * @see 描述：生成现金分期放款批量短信
	 * @see 创建日期：2015-6-24下午2:10:29
	 * @author ChengChun
	 *  
	 * @param acct
	 * @param loan
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
/*	private CashLoanDirectCreditMsgItem generateCashLoanDirectCreditMsg(CcsAcct acct, CcsLoan loan) {
		CashLoanDirectCreditMsgItem item = new CashLoanDirectCreditMsgItem();
		CcsCard card = queryFacility.getCardByCardNbr(loan.getCardNbr());
		
		item.org = acct.getOrg();
		item.custName = acct.getName();
		item.gender = acct.getGender();
		item.cardNo = batchUtils.getCardNoBySendMsgCardType(acct, card);
		item.mobileNo = acct.getMobileNo();
		item.loanInitPrin = loan.getLoanInitPrin();
		item.loanInitTerm = loan.getLoanInitTerm();
		item.loanFixedPmtPrin = loan.getLoanFixedPmtPrin();
		if(loan.getLoanFeeMethod() == LoanFeeMethod.F){
			item.msgCd = fetchMsgCdService.fetchMsgCd(card.getProductCd(), CPSMessageCategory.CPS044);
			item.loanInitFee1 = loan.getLoanInitFee();
		}else if(loan.getLoanFeeMethod() == LoanFeeMethod.E){
			item.msgCd = fetchMsgCdService.fetchMsgCd(card.getProductCd(), CPSMessageCategory.CPS045);
			item.loanFixedFee1 = loan.getLoanFixedFee();
		}
		return item;
	}*/
	
	
	/**
	 * @see 方法名：generateOnlineSuccIntefaceFile 
	 * @see 描述：生成现金分期实时放款对账文件
	 * @see 创建日期：2015-6-24下午2:10:57
	 * @author ChengChun
	 *  
	 * @param lend
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private DdOnlineSuccIntefaceItem generateOnlineSuccIntefaceFile(CcsCashloanDepoLog lend){
		DdOnlineSuccIntefaceItem item = new DdOnlineSuccIntefaceItem();
		
		item.org = lend.getOrg();
		item.messageType = lend.getMti();
		item.messProcessingCode = lend.getB003ProcCode();
		item.outSendOrg = lend.getB032AcqInst();
		item.outProxyOrg = lend.getB033FwdIns();
		item.outFlowSeq = lend.getB011Trace();
		item.liquidateDate = lend.getSettleDate();
		item.cardNo = lend.getB002CardNbr();
		item.transAmt = lend.getB004Amt();
		item.txnCurrCd = lend.getB049CurrCode();
		item.liquidateCurrCd = lend.getSettleCode();
		item.transDate = lend.getB007TxnTime();
		item.authCode = lend.getAuthCode();
		item.responseCode = lend.getB039RtnCode();
		item.merchantType = lend.getMcc();
		item.acceptOrgName = lend.getRlBankName();
		item.acceptOrgNumber = lend.getRlBankBranch();
		item.acceptDAcctName = lend.getRlBankAcctName();
		item.dCardNo = lend.getB102AcctIdent1();
		item.cardNoMapingName = lend.getB103AcctIdent2();
		item.origTransInfo = lend.getB090OrigData();
		item.prepareFiled = lend.getReserved();
		
		return item;
	}
}
