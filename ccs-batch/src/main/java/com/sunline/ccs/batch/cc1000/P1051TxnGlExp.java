package com.sunline.ccs.batch.cc1000;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.enums.PostGlIndicator;
import com.sunline.ppy.dictionary.exchange.GlTxnItem;
import com.sunline.pcm.param.def.CurrencyCd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.batch.LineItem;


/**
 * @see 类名：P1051TxnGlExp
 * @see 描述：送入的回佣类总账接口文件补填字段
 *
 * @see 创建日期：   2015-6-23下午7:18:51
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P1051TxnGlExp implements ItemProcessor<LineItem<GlTxnItem>, GlTxnItem> {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private CustAcctCardFacility queryFacility;
	
	@Override
	public GlTxnItem process(LineItem<GlTxnItem> line) throws Exception {
		GlTxnItem in = line.getLineObject();

		try{
			
			//取机构号并设置上下文
			OrganizationContextHolder.setCurrentOrg(in.org);
			CurrencyCd postCurrencyCd = parameterFacility.loadParameter(in.currCd, CurrencyCd.class);
			
			//获取卡片
			CcsCard card = queryFacility.getCardByCardNbr(in.cardNo);
			CcsAcct acct = null;
			if(card != null){
				//获取产品
				ProductCredit product = parameterFacility.retrieveParameterObject(card.getProductCd(), ProductCredit.class);
				AccountAttribute accountAttr = parameterFacility.loadParameter(product.accountAttributeId, AccountAttribute.class);
				acct = queryFacility.getAcctByAcctNbr(accountAttr.accountType, card.getAcctNbr());
				
			}
			
			//输出文件
			GlTxnItem out = new GlTxnItem();
			out.org = in.org;
			out.cardNo = in.cardNo;
			out.txnSeq = in.txnSeq;
			out.currCd = in.currCd;
			out.txnCode = in.txnCode;
			out.txnDesc = in.txnDesc;
			out.dbCrInd = in.dbCrInd;
			out.postDate = in.postDate;
			out.postAmount =  formatAmt(in.postAmount, postCurrencyCd.exponent);
			out.postGlInd = PostGlIndicator.N;
			out.acqBranchId = in.acqBranchId;
			out.planNbr = "999999";
			out.bucketType = BucketType.Pricinpal;
			out.ageCd = "0";
			if(acct == null){
				out.acctNo = null;
				out.acctType = null;
				out.owningBranch = null;
			}else{
				out.acctNo = acct.getAcctNbr();
				out.acctType = acct.getAcctType();
				out.owningBranch = acct.getOwningBranch();
			}

			return out;
			
		}catch (Exception e) {
			logger.error("送入的回佣类总账接口文件补填字段异常, 介质卡号{}", in.cardNo);
			throw e;
		}
	}
	
	
	/**
	 * @see 方法名：formatAmt 
	 * @see 描述：范型金额字段
	 * @see 创建日期：2015-6-23下午7:19:15
	 * @author ChengChun
	 *  
	 * @param amt
	 * @param exponent
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private BigDecimal formatAmt(BigDecimal amt, Integer exponent){
		return amt.movePointLeft(exponent);
	}
	
}
