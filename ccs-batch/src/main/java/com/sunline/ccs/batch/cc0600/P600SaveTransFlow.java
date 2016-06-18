package com.sunline.ccs.batch.cc0600;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnPost;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnPostHst;
import com.sunline.pcm.param.def.CurrencyCd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.exchange.TpsTranFlow;

/**
 * @see 类名：P600SaveTxnGoing
 * @see 描述：TODO 中文描述
 *
 * @see 创建日期： 2015年6月18日下午4:25:36
 * @author songyanchao
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P600SaveTransFlow implements ItemProcessor<CcsTxnPost, TpsTranFlow> {
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	
    @Override
    public TpsTranFlow process(CcsTxnPost item) throws Exception {

    	OrganizationContextHolder.setCurrentOrg(item.getOrg());
    	TpsTranFlow tpsTranFlow = getTpsTranFlow(item);

		return tpsTranFlow;
    }

    /** 
     * @see 方法名：setTpsTranFlow 
     * @see 描述：TODO 方法描述
     * @see 创建日期：2015年6月18日下午4:33:19
     * @author songyanchao
     *  
     * @param tpsTranFlow
     * @param item
     * 
     * @see 修改记录： 
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */

    private TpsTranFlow getTpsTranFlow(CcsTxnPost txnPost) {
    	
    	TpsTranFlow tpsTranFlow = null;
    	
    	if(txnPost.getExpInd().equals(Indicator.N)
    			&& txnPost.getIsRevoke().equals(Indicator.N) 
    			&& (txnPost.getB039RtnCode().equals("00") 
    					|| txnPost.getB039RtnCode().equals("11") 
    					|| txnPost.getB039RtnCode().equals("0000"))){
    		CurrencyCd currencyCd = parameterFacility.loadParameter(txnPost.getTxnCurrenctCode(), CurrencyCd.class);
    		
    		tpsTranFlow = new TpsTranFlow();
    		tpsTranFlow.recType = "DT";
    	   	tpsTranFlow.orgId = txnPost.getOrg();
    	   	tpsTranFlow.mti = txnPost.getMti();  //
    	   	tpsTranFlow.processingCode = txnPost.getB003ProcCode();   //交易处理码
    	   	tpsTranFlow.conditionCode = txnPost.getB025Entrycond(); //服务点条件码
    	   	tpsTranFlow.srcChannel = txnPost.getSrcChnl().toString();   //来源渠道
    	   	tpsTranFlow.inputSource = txnPost.getTxnSource();  //交易来源
    	   	tpsTranFlow.inputTxnCode = txnPost.getTxnCode();  
    	   	tpsTranFlow.dbCrInd = txnPost.getDbCrInd()==null?"":txnPost.getDbCrInd().name();
    	   	tpsTranFlow.cardNo = txnPost.getCardNbr();
    	   	tpsTranFlow.tranAmt = txnPost.getTxnAmt().movePointRight(currencyCd.exponent).toString();  //FIXME  金额是否有格式？？
    	   	tpsTranFlow.txnCurrencyCode = txnPost.getTxnCurrenctCode();
    	   	tpsTranFlow.settAmt = txnPost.getSettAmt().movePointRight(currencyCd.exponent).toString();  //清算金额   和入账币种金额？？
    	   	tpsTranFlow.settCurrencyCode = txnPost.getSettCurrencyCode();  //清算货币代码   和入账币种？？
    	   	tpsTranFlow.txnDateTime = txnPost.getTxnTime(); 
    	   	tpsTranFlow.refNbr = txnPost.getRefNbr();
    	   	tpsTranFlow.authCode = txnPost.getAuthCode();
    	   	tpsTranFlow.acqTerminalId = txnPost.getAcqAcceptorId();
    	   	tpsTranFlow.acqAcceptorId = txnPost.getAcqAcceptorId();
    	   	tpsTranFlow.acqNameAddr = txnPost.getAcqAddress();
    	   	tpsTranFlow.merchCategoryCode = txnPost.getMerchCategoryCode();
    	   	tpsTranFlow.origTxnMess = txnPost.getOrigTxnInfo(); //原始交易信息
    	   	tpsTranFlow.voucherNo = txnPost.getVoucherNo();
    	   	tpsTranFlow.txnFeeAmt = txnPost.getTxnFeeAmt().movePointRight(currencyCd.exponent).toString();  //交易手续费
    	   	tpsTranFlow.feeProfit = txnPost.getFeeProfit().movePointRight(currencyCd.exponent).toString();//FIXME 金额是否有格式？？
    	   	tpsTranFlow.b032 = txnPost.getAcqAcceptorId();
    	   	tpsTranFlow.b033 = txnPost.getB033FwdIns();//发送机构标识码
    	   	tpsTranFlow.transTerminal = txnPost.getB040TermId();
    	   	tpsTranFlow.loanCode = txnPost.getLoanCode();
    	   	txnPost.setExpInd(Indicator.Y);
    	}
	   	
	   	//转移历史表
	   	this.addTxnPostHst(txnPost);
	    em.remove(txnPost);
	    
	    return tpsTranFlow;
   	
     } 
    
	/**
	 * txnpost历史表
	 * 
	 * @param item
	 * @param txnPost
	 */

	private void addTxnPostHst(CcsTxnPost txnPost) {
		
		CcsTxnPostHst txnPostHst = new CcsTxnPostHst();
		txnPostHst.updateFromMap(txnPost.convertToMap());
		
		// 数据持久化
		em.persist(txnPostHst);
	}
}

