package com.sunline.ccs.batch.cc7000;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
//import com.sunline.smsd.service.sdk.DdSucessMessInterfaceItem;
//import com.sunline.smsd.service.sdk.LoanRepaymentMsgInterfaceItem;

/**
 * @see 类名：P7001RepayMsg
 * @see 描述：账单交易生成还款短信文件接口
 *          1. 生成约定还款成功短信
 *          2. 生成指定借据还款成功短信
 *
 * @see 创建日期：   2015-6-24下午2:20:08
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P7001RepayMsg implements ItemProcessor<CcsTxnHst, S7000TxnProduce> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	/**
	 * 获取参数工具类
	 */
	@Autowired
	private UnifiedParameterFacility parameterFacility;

	@Autowired
	private CustAcctCardFacility queryFaiclity;
	
	@Override
	public S7000TxnProduce process(CcsTxnHst txnHst) throws Exception {
		
		S7000TxnProduce i6600 = new S7000TxnProduce();
		i6600.setTmTxnHst(txnHst);
		
		try {
			OrganizationContextHolder.setCurrentOrg(txnHst.getOrg());
			
			//获取交易码映射
			SysTxnCdMapping directDebit = parameterFacility.loadParameter(SysTxnCd.S11.toString(), SysTxnCdMapping.class);
			SysTxnCdMapping loanRepaymentByReceiptNbr =  parameterFacility.retrieveParameterObject(SysTxnCd.S57.toString(), SysTxnCdMapping.class);
			SysTxnCdMapping loanRepayment = parameterFacility.retrieveParameterObject(SysTxnCd.S58.toString(), SysTxnCdMapping.class);

			
			//约定还款短信
			if (directDebit.txnCd.equals(txnHst.getTxnCode())) {
				CcsAcct acct = queryFaiclity.getAcctByAcctNbr(txnHst.getAcctType(), txnHst.getAcctNbr());
				if(txnHst.getTxnAmt().compareTo(acct.getLastDdAmt()) < 0){
					//如果入账金额小于上期约定还款金额则发扣款金额小于约定还款金额短信接口内容
//					i6600.setDdSucessMess(generateDdSucessMessInterfaceItemPartPay(txnHst, acct));
				}else {
					//如果入账金额大于等于上期约定还款金额，发约定还款成功短信
//					i6600.setDdSucessMess(generateDdSucessMessInterfaceItem(txnHst, acct));
				}
			}
			//指定借据还款短信
			else if (loanRepaymentByReceiptNbr!=null && loanRepaymentByReceiptNbr.txnCd.equals(txnHst.getTxnCode())) {
//				i6600.setLoanRepaymentMsg(generateLoanRepaymentMsg(txnHst));
			}
			else if (loanRepayment!=null && loanRepayment.txnCd.equals(txnHst.getTxnCode())) {
//				i6600.setLoanRepaymentMsg(generateLoanRepaymentMsg(txnHst));
			}

			return i6600;
			
		} catch (Exception e) {
			logger.error("账单交易生成还款短信文件异常，卡号后四位{}, 交易参考号{}", CodeMarkUtils.subCreditCard(txnHst.getLogicCardNbr()), txnHst.getRefNbr());
			throw e;
		}
		
	}

	/**
	 * @see 方法名：generateDdSucessMessInterfaceItem 
	 * @see 描述：创建约定还款成功短信接口内容
	 * @see 创建日期：2015-6-24下午2:11:32
	 * @author ChengChun
	 *  
	 * @param txnHst
	 * @param acct
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
/*	private DdSucessMessInterfaceItem generateDdSucessMessInterfaceItem(CcsTxnHst txnHst, CcsAcct acct) {
		DdSucessMessInterfaceItem ddSucessMessInterfaceItem = new DdSucessMessInterfaceItem();
		CcsCard card = queryFaiclity.getCardByCardNbr(txnHst.getCardNbr());
		
		ddSucessMessInterfaceItem.cardNo = batchUtils.getCardNoBySendMsgCardType(acct, card);
		ddSucessMessInterfaceItem.custName = acct.getName();
		ddSucessMessInterfaceItem.gender = acct.getGender();
		ddSucessMessInterfaceItem.mobileNo = acct.getMobileNo();
		ddSucessMessInterfaceItem.org = txnHst.getOrg();
		ddSucessMessInterfaceItem.txnAmt = txnHst.getTxnAmt();
		ddSucessMessInterfaceItem.txnDate = txnHst.getTxnDate();
		ddSucessMessInterfaceItem.msgCd = fetchMsgCdService.fetchMsgCd(txnHst.getProductCd(), CPSMessageCategory.CPS042);
		
		return ddSucessMessInterfaceItem;
	}*/
	/**
	 * @see 方法名：generateLoanRepaymentMsg 
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：2015-6-24下午2:12:38
	 * @author ChengChun
	 *  
	 * @param txnHst
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
/*	private LoanRepaymentMsgInterfaceItem generateLoanRepaymentMsg(CcsTxnHst txnHst) {
		LoanRepaymentMsgInterfaceItem loanRepaymentMsg = new LoanRepaymentMsgInterfaceItem();
		CcsAcct acct = queryFaiclity.getAcctByAcctNbr(txnHst.getAcctType(), txnHst.getAcctNbr());
		CcsCard card = queryFaiclity.getCardByCardNbr(txnHst.getCardNbr());
		
		loanRepaymentMsg.cardNo = batchUtils.getCardNoBySendMsgCardType(acct, card);
		loanRepaymentMsg.custName = acct.getName();
		loanRepaymentMsg.gender = acct.getGender();
		loanRepaymentMsg.mobileNo = acct.getMobileNo();
		loanRepaymentMsg.org = txnHst.getOrg();
		loanRepaymentMsg.txnAmt = txnHst.getTxnAmt();
		loanRepaymentMsg.txnDate = txnHst.getTxnDate();
		loanRepaymentMsg.msgCd = fetchMsgCdService.fetchMsgCd(txnHst.getProductCd(), CPSMessageCategory.CPS051);
		
		//根据refNbr查找贷款信息并填入借据号
		if(txnHst.getRefNbr()!=null){
			JPAQuery query = new JPAQuery(em);
			QCcsLoan qTmLoan = QCcsLoan.ccsLoan;
			CcsLoan loan = query.from(qTmLoan)
					.where(qTmLoan.cardNbr.eq(txnHst.getCardNbr())
							.and(qTmLoan.refNbr.eq(txnHst.getRefNbr())))
					.singleResult(qTmLoan);
			loanRepaymentMsg.loanReceiptNbr = loan.getDueBillNo();
		}else{
			loanRepaymentMsg.loanReceiptNbr = "未指定";
		}
		
		return loanRepaymentMsg;
	}*/
	
	/**
	 * @see 方法名：generateDdSucessMessInterfaceItemPartPay 
	 * @see 描述：创建约定还款成功--扣款金额小于约定还款金额短信接口内容
	 * @see 创建日期：2015-6-24下午2:12:13
	 * @author ChengChun
	 *  
	 * @param txnHst
	 * @param acct
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
/*	private DdSucessMessInterfaceItem generateDdSucessMessInterfaceItemPartPay(CcsTxnHst txnHst,CcsAcct acct) {
		DdSucessMessInterfaceItem ddSucessMessInterfaceItem = new DdSucessMessInterfaceItem();
		CcsCard card = queryFaiclity.getCardByCardNbr(txnHst.getCardNbr());
		
		ddSucessMessInterfaceItem.cardNo = batchUtils.getCardNoBySendMsgCardType(acct, card);
		ddSucessMessInterfaceItem.custName = acct.getName();
		ddSucessMessInterfaceItem.gender = acct.getGender();
		ddSucessMessInterfaceItem.mobileNo = acct.getMobileNo();
		ddSucessMessInterfaceItem.org = txnHst.getOrg();
		ddSucessMessInterfaceItem.txnAmt = acct.getLastDdAmt();
		ddSucessMessInterfaceItem.txnDate = txnHst.getTxnDate();
		ddSucessMessInterfaceItem.payAmt = txnHst.getTxnAmt();
		ddSucessMessInterfaceItem.notPayAmt = acct.getLastDdAmt().subtract(txnHst.getTxnAmt()).setScale(2, BigDecimal.ROUND_HALF_UP);
		ddSucessMessInterfaceItem.msgCd = fetchMsgCdService.fetchMsgCd(txnHst.getProductCd(), CPSMessageCategory.CPS060);
		
		return ddSucessMessInterfaceItem;
	}*/
}
