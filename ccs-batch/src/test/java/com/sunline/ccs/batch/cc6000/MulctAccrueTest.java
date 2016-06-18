package com.sunline.ccs.batch.cc6000;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ccs.batch.cc6000.interestaccrue.MulctAccrue;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnHst;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.pcm.service.sdk.ParameterServiceMock;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.BucketType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-context-front.xml")
@Transactional
public class MulctAccrueTest {
	
	private static final Logger logger = LoggerFactory.getLogger(MulctAccrueTest.class);
	
	
	@Autowired
	private ParameterServiceMock parameterMock;
	@Autowired
	private MulctAccrue mulctAccrue;
	@PersistenceContext
	private EntityManager em;

	
	@Before
	public void setup(){
		// 参数
//		LoanFeeDef df  = BatchParameter.genLoanFeeDef();
		LoanPlan lp = BatchParameter.genLoanPlan();
		parameterMock.putParameter(lp.loanCode, lp);
		
		SysTxnCdMapping st = BatchParameter.genSysTxnCdMapping();
		parameterMock.putParameter(st.sysTxnCd.toString(), st);
		TxnCd tc = BatchParameter.genTxnCd();
		parameterMock.putParameter(tc.txnCd, tc);
	}
	
	@Test
	public void testCase() throws Exception{
		logger.info("开始测试----------------------------------");
		
		S6000AcctInfo info = new S6000AcctInfo();
		//acct
		CcsAcct acct  = BatchData.genAcct();
		info.setAccount(acct);
		//交易
		CcsPostingTmp txn = BatchData.genCcsPostingTmp();
		//loan
		List<CcsLoan> loans = new ArrayList<CcsLoan>();
		CcsLoan loan = BatchData.genLoan();
		loans.add(loan);
		info.setLoans(loans);
		
		//交易
		List<CcsTxnHst> txnHstList = BatchData.genTxnHstList();
		for(CcsTxnHst c :txnHstList){
			logger.info("CcsTxnHst :"+c.getPostDate()+"  "+c.getPostAmt());
			em.merge(c);
		}
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		QCcsTxnHst qccsTxnHst = QCcsTxnHst.ccsTxnHst;
		List<CcsTxnHst> txnHstList1 = new JPAQuery(em).from(qccsTxnHst)
			.where(qccsTxnHst.postDate.after(sf.parse("2015-06-01"))
					.and(qccsTxnHst.acctNbr.eq(1L).and(qccsTxnHst.acctType.eq(AccountType.E)))
					.and(qccsTxnHst.refNbr.eq(loan.getRefNbr()))
					.and(qccsTxnHst.txnCode.eq("T904"))
					)
			.orderBy(qccsTxnHst.txnTime.asc())
			.list(qccsTxnHst);
		for(CcsTxnHst c :txnHstList1){
			logger.info(c.getPostAmt()+"");
		}
		
		List<CcsPlan>  plans = BatchData.genPlans();
		info.setPlans(plans);
		
		List<CcsPostingTmp> newTxnPosts = new ArrayList<CcsPostingTmp>();
		logger.info("数据准备完毕-------------------------------");
		if(txn.getPostDate().compareTo(txn.getTxnDate())>0){
			if (logger.isDebugEnabled()) {
				logger.debug("还款交易，执行罚金回溯:PostAmt["+txn.getPostAmt()
						+"],CurrBal["+info.getAccount().getCurrBal()
						+"]");
			}
			mulctAccrue.accumulateMulct(info, txn.getPostAmt(), txn.getTxnDate(), newTxnPosts);
		}
		
		
		logger.info("数据校验----------------------------------");
		
		
		

		
	}
	
}
