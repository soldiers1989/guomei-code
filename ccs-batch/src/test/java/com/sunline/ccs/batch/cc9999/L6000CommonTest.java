package com.sunline.ccs.batch.cc9999;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.item.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ccs.batch.cc6000.S6000AcctInfo;
import com.sunline.ccs.batch.cc6000.interestaccrue.MulctAccrue;
import com.sunline.ccs.batch.cc6000.interestaccrue.ReplaceMulctAccrue;
import com.sunline.ccs.batch.common.DateUtils;
import com.sunline.ccs.facility.tools.ObjectCompareCommon;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnPost;
import com.sunline.pcm.service.sdk.ParameterServiceMock;
import com.sunline.ppy.dictionary.enums.BucketType;

/**
 * 批量6000公共测试
 * @author Lisy
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/cc9999/test-context-9999.xml")
@Transactional
public class L6000CommonTest {
	@Autowired
	private ParameterServiceMock parameterMock;
    @Autowired
    private GlobalManagementService globalManagementService;
    @Autowired
    private MulctAccrue mulctAccrue;
    @Autowired
    private ReplaceMulctAccrue replaceMulctAccrue;
    @PersistenceContext
    private EntityManager em;
    
	@Before
	public void setup() throws ParseException {
		globalManagementService.getSystemStatus().setProcessDate(new Date());
		globalManagementService.getSystemStatus().setLastProcessDate(DateUtils.addDays(new Date(), -1));
		L6000ParamMockCommon mock = new L6000ParamMockCommon();
		mock.putParams(parameterMock);
	}
	
	/**
	 * 罚金/代收罚金回溯测试
	 * @throws Exception 
	 */
    @Test
    public void run() throws Exception{
    	S6000AcctInfo item = new L6000AcctInfoCommonItem().getItemMCEI();
    	//已经逾期
    	item.getLoans().get(0).setOverdueDate(DateUtils.addDays(new Date(), -8));
    	item.getLoans().get(0).setCpdBeginDate(DateUtils.addDays(new Date(), -8));
    	
		//逾期账龄
		item.getAccount().setAgeCode("1");
		
		//罚金入账交易
		CcsTxnHst txnHst1 = new CcsTxnHst();
		txnHst1.setTxnSeq(1L);
		txnHst1.setAcctNbr(item.getAccount().getAcctNbr());
		txnHst1.setAcctType(item.getAccount().getAcctType());
		txnHst1.setTxnCode("T73");
		txnHst1.setPostDate(DateUtils.addDays(new Date(), -3));
		//代收罚金入账交易历史
		CcsTxnHst txnHst2 = new CcsTxnHst();
		txnHst2.setTxnSeq(2L);
		txnHst2.setAcctNbr(item.getAccount().getAcctNbr());
		txnHst2.setAcctType(item.getAccount().getAcctType());
		txnHst2.setTxnCode("D12");
		txnHst2.setPostDate(DateUtils.addDays(new Date(), -5));
		CcsTxnHst txnHst3 = new CcsTxnHst();
		txnHst3.setTxnSeq(3L);
		txnHst3.setAcctNbr(item.getAccount().getAcctNbr());
		txnHst3.setAcctType(item.getAccount().getAcctType());
		txnHst3.setTxnCode("D12");
		txnHst3.setPostDate(DateUtils.addDays(new Date(), -1));
		
		em.persist(txnHst1);
		em.persist(txnHst2);
		em.persist(txnHst3);
		
		em.find(CcsTxnHst.class, 1L).getAcctNbr();
		//延迟回盘
    	List<CcsPostingTmp> newTxnPosts = new ArrayList<CcsPostingTmp>();
		Date overDueDate = item.getLoans().get(0).getOverdueDate();
		Date cpdBeginDate = item.getLoans().get(0).getCpdBeginDate();
		mulctAccrue.accumulateMulct(item,new BigDecimal(1000),DateUtils.addDays(new Date(), -8),newTxnPosts);
		replaceMulctAccrue.accumulateMulct(item,new BigDecimal(1000),DateUtils.addDays(new Date(), -8),newTxnPosts,overDueDate,cpdBeginDate);
		ObjectCompareCommon.compare(new L6000AcctInfoCommonItem().getItemMCEI(), item);
    }
}
