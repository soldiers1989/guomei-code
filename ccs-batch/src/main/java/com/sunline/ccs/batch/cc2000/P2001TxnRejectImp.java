package com.sunline.ccs.batch.cc2000;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.ppy.dictionary.enums.PostingFlag;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ccs.batch.cc0100.U0101SetupLoanP;
import com.sunline.ccs.batch.cc0100.U0101SetupLoanP.LoanRegInfo;
import com.sunline.ccs.batch.common.EnumUtils;
import com.sunline.ccs.batch.common.TxnPrepare;
import com.sunline.ccs.batch.common.TxnPrepareSet;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnReject;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ark.support.OrganizationContextHolder;

/**
 * 
 * @see 类名：P2001TxnRejectImp
 * @see 描述：往日挂账交易重新待入账预处理
 *
 * @see 创建日期：   2015-6-23下午7:27:37
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P2001TxnRejectImp implements ItemProcessor<CcsTxnReject, TxnPrepareSet> {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private U0101SetupLoanP u0001SetupLoanP;
	@Autowired
	private TxnPrepare txnPrepare;
	
	@Override
	public TxnPrepareSet process(CcsTxnReject rej) throws Exception {
		
		try
		{
			CcsPostingTmp post = new CcsPostingTmp();
			post.updateFromMap(rej.convertToMap());
			TxnPrepareSet output = new TxnPrepareSet();
			
			// 指定POS分期开卡交易特殊处理
			if(EnumUtils.in(rej.getPostingFlag(), 
					PostingFlag.F70, PostingFlag.F71, PostingFlag.F72, PostingFlag.F73, PostingFlag.F74, 
					PostingFlag.F75, PostingFlag.F76, PostingFlag.F77, PostingFlag.F78, PostingFlag.F79, 
					PostingFlag.F80, PostingFlag.F81, PostingFlag.F82, PostingFlag.F83, PostingFlag.F84, 
					PostingFlag.F85, PostingFlag.F86, PostingFlag.F87))
			{
				OrganizationContextHolder.setCurrentOrg(post.getOrg());
				
				CcsAcct acct = queryFacility.getAcctByAcctNbr(post.getAcctType(), post.getAcctNbr());
				ProductCredit productCredit = parameterFacility.loadParameter(acct.getProductCd(), ProductCredit.class);
				LoanRegInfo loanRegInfo = u0001SetupLoanP.generateLoanReg(productCredit, acct, post);
				
				if(loanRegInfo.getTmLoanReg()!=null){
					post.setPrePostingFlag(post.getPostingFlag());
					post.setPostingFlag(PostingFlag.F00);
					
					em.remove(rej);
					em.persist(post);
					em.persist(loanRegInfo.getTmLoanReg());
					
					logger.debug("指定POS开卡交易成功，交易参考号：{}", post.getRefNbr());
				}else{
					rej.setPrePostingFlag(post.getPostingFlag());
					rej.setPostingFlag(loanRegInfo.getResult());
					
					output.setRptTxnItem(txnPrepare.makeRptTxnItem(post, null));
					
					logger.debug("指定POS开卡交易挂账{}，交易参考号：{}", loanRegInfo.getResult(), post.getRefNbr());
				}
			}else{
				em.remove(rej);
				output = txnPrepare.txnPrepare(post, null);
			}
			
			return output;
		}catch (Exception e) {
			logger.error("交易转换异常,检索参考号{}", rej.getTxnSeq());
			throw e;
		}
	}
}
