package com.sunline.ccs.ui.server;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.CodeMarkUtils;


import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
//import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoHst;
//import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
//import com.sunline.ccs.infrastructure.shared.model.QCcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardLmMapping;
//import com.sunline.ccs.infrastructure.shared.model.QCcsTxnUnstatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnUnstatement;
//import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
//import com.sunline.ccs.infrastructure.shared.model.CcsTxnUnstatement;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnUnstatement;




import com.sunline.ccs.ui.server.commons.CPSBusProvide;
import com.sunline.ccs.ui.server.commons.CPSConstants;
import com.sunline.ccs.ui.server.commons.CheckUtil;
import com.sunline.kylin.web.ark.client.JPAQueryFetchResponseBuilder;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.ark.client.utils.StringUtils;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.ppy.dictionary.enums.AccountType;
/**
 * 未出账单交易查询
* @author dch
 */
@Controller
@RequestMapping(value="/t1306Server")
public class UnBilledPageServer {
	
    private Logger log = LoggerFactory.getLogger(this.getClass());
//    QCcsAuthmemoHst qTmAuthHst = QCcsAuthmemoHst.ccsAuthmemoHst;
//    QCcsCard qTmCard = QCcsCard.ccsCard;
//    QCcsCardLmMapping qTmCardMediaMap =  QCcsCardLmMapping.ccsCardLmMapping;
    QCcsTxnUnstatement qTmTxnUnstmt = QCcsTxnUnstatement.ccsTxnUnstatement;
    QCcsAcct qCcsAcct=QCcsAcct.ccsAcct;
    @Autowired
    private RCcsAcct rCcsAcct;
    @PersistenceContext
    private EntityManager em;
    
    @Autowired
	private CPSBusProvide cpsBusProvide;
    
	@ResponseBody()
	@RequestMapping(value="/getUnstmtTranList",method={RequestMethod.POST})
	public FetchResponse getUnstmtTranList(@RequestBody FetchRequest request) throws FlatException {
		String contrNbr=(String)request.getParameter(CcsAcct.P_ContrNbr);
		
		JPAQuery query=new JPAQuery(em).from(qTmTxnUnstmt).where(qTmTxnUnstmt.org.eq(OrganizationContextHolder.getCurrentOrg()));
		if(contrNbr!=null&&!"null".equals(contrNbr)){
			Long acctNbr=rCcsAcct.findOne(qCcsAcct.contrNbr.eq(contrNbr)).getAcctNbr();
			query=query.where(qTmTxnUnstmt.acctNbr.eq(acctNbr));
		}
		return getJpaFRB(request,query);
	}
		
//		// 卡号
//		String cardNo = request.getParameter(CcsTxnUnstatement.P_CardNbr) == null ? null 
//				: request.getParameter(CcsTxnUnstatement.P_CardNbr).toString();
//		// 卡账标识
//		String cardAccountIndicator = request.getParameter("cardAccountIndicator") == null ? null 
//				: request.getParameter("cardAccountIndicator").toString();
//		// 币种
//		String currencyCode = request.getParameter("currCd") == null ? null : request.getParameter("currCd").toString();
//		// 交易码
//		String txnCode = request.getParameter(CcsTxnUnstatement.P_TxnCode) == null ? null 
//				: request.getParameter(CcsTxnUnstatement.P_TxnCode).toString();
		
//		log.info("NF4104:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNo) + "],交易卡帐指示[" + cardAccountIndicator + "]");
//		CheckUtil.checkCardNo(cardNo);
//		CheckUtil.rejectNull(cardAccountIndicator, "交易卡帐指示不能为空");
//		//查找卡片记录
//		cpsBusProvide.getTmCardOTocardNbr(cardNo);
//		CcsAcct tmAccount = cpsBusProvide.getTmAccountTocardNbrAndcurrencyCode(cardNo, currencyCode);
//		AccountType accountType = cardNoTOProdctAcctFacility.CardNoCurrCdToAccoutType(cardNo, currencyCd);
//		if (cardAccountIndicator.equals(CPSConstants.TRANSTYPE_A)) {
//                  return getJpaFRB(request, NF4104_A(request, cardNo, tmAccount.getAcctType(),txnCode));
//		} else if (cardAccountIndicator.equals(CPSConstants.TRANSTYPE_C)) {
//			  return getJpaFRB(request, NF4104_C(request, cardNo, tmAccount.getAcctType(), txnCode));
//		} else {
//			throw new FlatException("无效的交易卡帐指示");
//		}
//	}

//     //通过账户来查找账单的query
//   public JPAQuery NF4104_A(FetchRequest request,String cardNo,AccountType accountType, String txnCode) throws FlatException{
//	   JPAQuery query = new JPAQuery(em);
//	   BooleanExpression booleanExpression = qTmCardMediaMap.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qTmCardMediaMap.cardNbr.eq(cardNo).and(qTmCardMediaMap.logicCardNbr.eq(qTmCard.logicCardNbr))).and(qTmCard.acctNbr.eq(qTmTxnUnstmt.acctNbr))
//				.and(qTmTxnUnstmt.acctType.eq(accountType));
//	   if(StringUtils.isNotEmpty(txnCode)){
//		   log.info("卡账标志["+accountType+"]，卡号后四位["+CodeMarkUtils.subCreditCard(cardNo)+"]，交易码["+txnCode+"]");
//		   booleanExpression = booleanExpression.and(qTmTxnUnstmt.txnCode.eq(txnCode));
//	   }
//	   query.from(qTmTxnUnstmt., qTmCardMediaMap, qTmCard).orderBy(qTmTxnUnstmt.acctNbr.asc()).where(booleanExpression)
////	   .offset(request.getStartRow()).limit(request.getEndRow())
//	   .list(qTmTxnUnstmt);
//       return query;
//	   
//   }
   /*不使用-lsy0920
   //通过卡号来查找账单的query
   private JPAQuery NF4104_C(FetchRequest request, String cardNo,AccountType accountType, String txnCode) throws FlatException {
		JPAQuery query = new JPAQuery(em);
	    BooleanExpression booleanExpression = qTmCardMediaMap.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qTmCardMediaMap.cardNbr.eq(cardNo).and(qTmCardMediaMap.logicCardNbr.eq(qTmTxnUnstmt.logicCardNbr))).and(qTmTxnUnstmt.acctType.eq(accountType));
	    if(StringUtils.isNotEmpty(txnCode)){
		   log.info("卡账标志["+accountType+"]，卡号后四位["+CodeMarkUtils.subCreditCard(cardNo)+"]，交易码["+txnCode);
		   booleanExpression = booleanExpression.and(qTmTxnUnstmt.txnCode.eq(txnCode));
		}
	    query.from(qTmTxnUnstmt, qTmCardMediaMap).orderBy(qTmTxnUnstmt.acctNbr.asc()).where(booleanExpression)
//	    .offset(request.getStartRow()).limit(request.getEndRow())
	    .list(qTmTxnUnstmt); 
	    return query;
	}
	*/
   //通过查询条件来获取查询信息FetchResponse
   @Transactional
   public FetchResponse getJpaFRB(FetchRequest request,JPAQuery query){
	  
	   
	   return  new JPAQueryFetchResponseBuilder(request,query)/*.addFieldMapping(CcsTxnUnstatement.P_CardNbr, qTmTxnUnstmt.cardNbr)*/
				.addFieldMapping(CcsTxnUnstatement.P_TxnDesc, qTmTxnUnstmt.txnDesc)
				.addFieldMapping(CcsTxnUnstatement.P_AcctType, qTmTxnUnstmt.acctType)
				.addFieldMapping(CcsTxnUnstatement.P_TxnDate, qTmTxnUnstmt.txnDate)
				.addFieldMapping(CcsTxnUnstatement.P_TxnCode, qTmTxnUnstmt.txnCode)
				.addFieldMapping(CcsTxnUnstatement.P_TxnTime, qTmTxnUnstmt.txnTime)
				.addFieldMapping(CcsTxnUnstatement.P_TxnCurrency, qTmTxnUnstmt.txnCurrency)
				.addFieldMapping(CcsTxnUnstatement.P_DbCrInd, qTmTxnUnstmt.dbCrInd)
				.addFieldMapping(CcsTxnUnstatement.P_TxnAmt, qTmTxnUnstmt.txnAmt)
				.addFieldMapping(CcsTxnUnstatement.P_AuthCode, qTmTxnUnstmt.authCode)
				.addFieldMapping(CcsTxnUnstatement.P_PostCurrency, qTmTxnUnstmt.postCurrency)
				.addFieldMapping(CcsTxnUnstatement.P_PostAmt, qTmTxnUnstmt.postAmt)
				.addFieldMapping(CcsTxnUnstatement.P_PostDate, qTmTxnUnstmt.postDate)
				.addFieldMapping(CcsTxnUnstatement.P_TxnShortDesc, qTmTxnUnstmt.txnShortDesc)
				.addFieldMapping(CcsTxnUnstatement.P_AcqAddress, qTmTxnUnstmt.acqAddress)
				.addFieldMapping(CcsTxnUnstatement.P_AcctNbr, qTmTxnUnstmt.acctNbr).build();
   }
}
