package com.sunline.ccs.batch.cc1300;

import java.math.BigDecimal;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ppy.dictionary.enums.DdReturnCode;
import com.sunline.ppy.dictionary.enums.PostTxnType;
import com.sunline.ppy.dictionary.enums.PostingFlag;
import com.sunline.ppy.dictionary.exchange.LoanRepaymentInterfaceItem;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ccs.batch.common.TxnPrepare;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.ark.support.OrganizationContextHolder;
//import com.sunline.smsd.service.sdk.DdFailMessInterfaceItem;
import com.sunline.ark.batch.LineItem;
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.mysema.query.jpa.impl.JPAQuery;

/**
 * 
 * @see 类名：P1311LoadLoanRepay
 * @see 描述：贷款还款回盘文件处理
 *
 * @see 创建日期：   2015-6-23下午7:24:43
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P1311LoadLoanRepay implements ItemProcessor<LineItem<LoanRepaymentInterfaceItem>, Object> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private TxnPrepare txnPrepare;
	@PersistenceContext
	private EntityManager em;
	
	@Override
	public Object process(LineItem<LoanRepaymentInterfaceItem> item) throws Exception {
	    logger.info("贷款还款回盘文件处理开始......");
		try
		{
			LoanRepaymentInterfaceItem loanRepay = item.getLineObject();
			logger.info("还款结果ddReturnCode="+loanRepay.ddReturnCode);
			logger.info("卡号="+loanRepay.defaltCardNo);
			//取机构号并设置上下文
			OrganizationContextHolder.setCurrentOrg(loanRepay.org);
			
			//预留返回码，默认都返回R00成功
			if(loanRepay.ddReturnCode == DdReturnCode.R00){
				if(StringUtils.isNotBlank(loanRepay.loanReceiptNbr)){
					//根据借据号查找贷款信息并填入refNbr
					CcsLoan loan = matchTmLoan(loanRepay);
					logger.info("贷款查询结果loan="+loan);
					if(loan != null){
						//指定借据还款生成金融交易; 批量短信待入账后生成
						CcsPostingTmp txnPost = createTtTxnPost(loanRepay, SysTxnCd.S57);
						txnPost.setRefNbr(loan.getRefNbr());
						txnPost.setPostingFlag(checkName(loanRepay));
						
						txnPrepare.txnPrepare(txnPost, null);
					}else{
						//TODO 增加参数，未找到借据号还款是否挂账，暂时默认挂账(若不挂账则按普通还款处理)
						CcsPostingTmp txnPost = createTtTxnPost(loanRepay, SysTxnCd.S57);
						txnPost.setPostingFlag(PostingFlag.F62);
						
						txnPrepare.txnPrepare(txnPost, null);
					}
				}else{
					//非指定借据还款生成金融交易
					CcsPostingTmp txnPost = createTtTxnPost(loanRepay, SysTxnCd.S58);
					txnPost.setPostingFlag(checkName(loanRepay));
					
					txnPrepare.txnPrepare(txnPost, null);
				}
			}
			
			return null;
			
		}catch (Exception e) {
			logger.error("指定借据还款文件处理异常, 卡号{}", CodeMarkUtils.subCreditCard(item.getLineObject().defaltCardNo));
			throw e;
		}
	}

	private CcsLoan matchTmLoan(LoanRepaymentInterfaceItem loanRepay) {
		JPAQuery query = new JPAQuery(em);
		QCcsLoan qTmLoan = QCcsLoan.ccsLoan;
		CcsLoan loan = query.from(qTmLoan)
				.where(qTmLoan.cardNbr.eq(loanRepay.defaltCardNo)
						.and(qTmLoan.dueBillNo.eq(loanRepay.loanReceiptNbr)))
				.singleResult(qTmLoan);
		return loan;
	}
	
	/**
	 * 校验还款文件中姓名与持卡人姓名是否一致，否则挂账
	 * 
	 * @param cs
	 * @return
	 */
	private PostingFlag checkName(LoanRepaymentInterfaceItem loanRepay) {
		CcsCustomer cust = queryFacility.getCustomerByCardNbr(loanRepay.defaltCardNo);
		if(cust!=null && StringUtils.isNotBlank(loanRepay.custName) && loanRepay.custName.trim().equals(cust.getName().trim())){
			return PostingFlag.F00;
		}else{
			return PostingFlag.F12;
		}
	}

	private CcsPostingTmp createTtTxnPost(LoanRepaymentInterfaceItem loanRepay, SysTxnCd sysTxnCd) {
		CcsPostingTmp txnPost = new CcsPostingTmp();
		
		txnPost.setOrg(loanRepay.org);
		txnPost.setAcctNbr(null);
		txnPost.setAcctType(null);
		txnPost.setCardNbr(loanRepay.defaltCardNo);
		txnPost.setTxnDate(loanRepay.txnDate);
		txnPost.setTxnTime(loanRepay.txnDate);
		txnPost.setPostTxnType(PostTxnType.M);
		SysTxnCdMapping txnCdMapping = parameterFacility.loadParameter(sysTxnCd.toString(), SysTxnCdMapping.class);
		txnPost.setTxnCode(txnCdMapping.txnCd);
		txnPost.setOrigTxnCode(txnCdMapping.txnCd);
		txnPost.setDbCrInd(DbCrInd.C);
		txnPost.setTxnAmt(loanRepay.txnAmt);
		txnPost.setOrigTxnAmt(loanRepay.txnAmt);
		txnPost.setOrigSettAmt(loanRepay.txnAmt);
		txnPost.setPostAmt(loanRepay.txnAmt);
		txnPost.setTxnCurrency(loanRepay.txnCurrCd);
		txnPost.setPostCurrency(loanRepay.txnCurrCd);
		txnPost.setOrigTransDate(null);//FIXME
		txnPost.setRelPmtAmt(BigDecimal.ZERO);
		txnPost.setOrigPmtAmt(BigDecimal.ZERO);//FIXME
		txnPost.setInterchangeFee(BigDecimal.ZERO);
		txnPost.setFeePayout(BigDecimal.ZERO);
		txnPost.setFeeProfit(BigDecimal.ZERO);
		txnPost.setLoanIssueProfit(BigDecimal.ZERO);
		
		return txnPost;
	}
}
