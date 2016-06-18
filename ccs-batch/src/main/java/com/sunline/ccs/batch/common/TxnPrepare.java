package com.sunline.ccs.batch.common;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsCard;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnReject;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnSeq;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.AuthProduct;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.enums.FirstCardFeeInd;
import com.sunline.ccs.param.def.enums.FirstUsageIndicator;
import com.sunline.ccs.param.def.enums.LogicMod;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ppy.dictionary.enums.PostGlIndicator;
import com.sunline.ppy.dictionary.enums.PostTxnType;
import com.sunline.ppy.dictionary.enums.PostingFlag;
import com.sunline.ppy.dictionary.exchange.GlTxnItem;
import com.sunline.ppy.dictionary.report.ccs.RptTxnItem;

/**
 * @see 类名：TxnPrepare
 * @see 描述：授权交易预处理组件
 *
 * @see 创建日期：   2015-6-24下午5:38:21
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class TxnPrepare {
	
	/**
	 * 系统日志
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@PersistenceContext
	private EntityManager em;
	
	/**
	 * 卡号映射表
	 */
	@Autowired
	private RCcsCardLmMapping rTmCardMediaMap;
	
	/**
	 * 卡档
	 */
	@Autowired
	private RCcsCard rTmCard;
	
	/**
	 * 账户档
	 */
	@Autowired
	private RCcsAcct rTmAccount;
	
	/**
	 * 获取参数类
	 */
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	
	/**
	 * 跑批日期
	 */
	@Autowired
	private BatchStatusFacility batchFacility;
	
        /**
         * @see 方法名：txnPrepare 
         * @see 描述：TODO 方法描述
         * @see 创建日期：2015-6-24下午5:38:39
         * @author ChengChun
         *  
         * @param post
         * @param inputSource
         * @return
         * @throws Exception
         * 
         * @see 修改记录： 
         * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
         */
	public TxnPrepareSet txnPrepare(CcsPostingTmp post, InputSource inputSource) throws Exception {
		
		try {
			
			//取机构号并设置上下文
			OrganizationContextHolder.setCurrentOrg(post.getOrg());
			
			//获取TxnSeq,若挂账交易则不空
			if(post.getTxnSeq() == null){
				CcsTxnSeq seq = new CcsTxnSeq();
				seq.setOrg(post.getOrg());
				em.persist(seq);
				post.setTxnSeq(seq.getTxnSeq());
			}
			//各渠道交易送入的字段不同，可能还需要添加【若空则默认】的逻辑
			if(post.getPostTxnType() == null){
				post.setPostTxnType(PostTxnType.M);//非积分交易都为金融交易
			}
			if(post.getInterchangeFee() == null){
				post.setInterchangeFee(BigDecimal.ZERO);//若空则0
			}
			if(post.getOrigPmtAmt() == null){
				post.setOrigPmtAmt(BigDecimal.ZERO);//若空则0
			}
			if(post.getPoints() == null){
				post.setPoints(BigDecimal.ZERO);//若空则0
			}
			if(post.getPostDate() == null){
				post.setPostDate(batchFacility.getBatchDate());//若空则跑批日期
			}
			if(post.getRelPmtAmt() == null){
				post.setRelPmtAmt(BigDecimal.ZERO);//若空则0
			}
			
			
			//更新往日挂账标识
			if(post.getPostingFlag() != null){
				// 指定借据贷款还款未找到原贷款，需要手工调整，一直挂账
				if(post.getPostingFlag() == PostingFlag.F62){
					saveTmTxnReject(post, PostingFlag.F62);
					return createI5001OutputItem(post, null, null, PostingFlag.F62, inputSource);
				}
				// 行方还款文件姓名不匹配，需要手工调整，一直挂账
				else if(post.getPostingFlag() == PostingFlag.F12){
					saveTmTxnReject(post, PostingFlag.F12);
					return createI5001OutputItem(post, null, null, PostingFlag.F12, inputSource);
				}else{ 
					post.setPrePostingFlag(post.getPostingFlag());
					post.setPostingFlag(PostingFlag.F00);
				}
				post.setPostingFlag(PostingFlag.F00);
			}else if(post.getPostingFlag() == PostingFlag.F62){
				saveTmTxnReject(post, PostingFlag.F62);
				return createI5001OutputItem(post, null, null, PostingFlag.F62, inputSource);
			}else{
				post.setPrePostingFlag(PostingFlag.F00);
				post.setPostingFlag(PostingFlag.F00);
			}
			
			
			//获取逻辑卡号
			String logicCardNbr = post.getLogicCardNbr();
			if(StringUtils.isBlank(logicCardNbr))
			{
				//通过介质卡号查逻辑卡号
				CcsCardLmMapping cardMap = rTmCardMediaMap.findOne(post.getCardNbr());
				if(cardMap == null)
				{
					//写入拒绝重入账交易临时表
					saveTmTxnReject(post, PostingFlag.F41);
					return createI5001OutputItem(post, null, null, PostingFlag.F41, inputSource);
				}
				logicCardNbr = cardMap.getLogicCardNbr();
				post.setLogicCardNbr(logicCardNbr);
			}
			
			//获取卡片
			CcsCard card = rTmCard.findOne(logicCardNbr);
			if(card == null)
			{
				throw new IllegalArgumentException("逻辑卡不存在:{}" + logicCardNbr);
			}
			if(StringUtils.isBlank(post.getCardBasicNbr())){
				post.setCardBasicNbr(card.getCardBasicNbr());
			}
			if(post.getAcctNbr() == null){
				post.setAcctNbr(card.getAcctNbr());
			}
			if(StringUtils.isBlank(post.getProductCd())){
				post.setProductCd(card.getProductCd());
			}
			post.setCardBlockCode(card.getBlockCode());
			
			//更新首次用卡标识
			ProductCredit productCredit = parameterFacility.retrieveParameterObject(post.getProductCd(), ProductCredit.class);
			//更改为“首次借记交易更新收年费日期”
			if(card.getFirstUsageFlag() == FirstUsageIndicator.A && post.getDbCrInd() == DbCrInd.D){
				//若未用卡,则首次用卡
				card.setFirstUsageFlag(FirstUsageIndicator.B);
				//首次用卡日期
				card.setFirstUsageDate(batchFacility.getBatchDate());
				//若首次用卡收取年费
				if(productCredit.fee.firstCardFeeInd == FirstCardFeeInd.T)
				{
					card.setNextCardFeeDate(batchFacility.getBatchDate());
				}
			}
			
			if (logger.isDebugEnabled()) {
				logger.debug("LogicalCardNo["+post.getLogicCardNbr()
						+"],Org["+post.getOrg()
						+"],ProductCd["+post.getProductCd()
						+"]");
			}
			logger.debug("本币账户参数标识:" + productCredit.accountAttributeId);
			AccountAttribute acctAttr = parameterFacility.loadParameter(String.valueOf(productCredit.accountAttributeId), AccountAttribute.class);
			AccountAttribute dualAcctAttr = null;
			if(productCredit.dualAccountAttributeId != null)
			{
				dualAcctAttr = parameterFacility.loadParameter(String.valueOf(productCredit.dualAccountAttributeId), AccountAttribute.class);
			}
			
			//获取账户
			CcsAcctKey key = new CcsAcctKey();
			key.setAcctNbr(card.getAcctNbr());
			key.setAcctType(acctAttr.accountType);
			
			CcsAcct acct = rTmAccount.findOne(key);
			if(acct == null)
			{
				throw new IllegalArgumentException("账号不存在:{},{}" + key.getAcctNbr() + key.getAcctType());
			}
			
			//校验入账币种是否合法
			if(dualAcctAttr == null){
				if(!post.getPostCurrency().equals(acctAttr.accountType.getCurrencyCode())){
					saveTmTxnReject(post, PostingFlag.F44);
					return createI5001OutputItem(post, null, acct, PostingFlag.F44, inputSource);
				}
			}else{
				if(!post.getPostCurrency().equals(acctAttr.accountType.getCurrencyCode()) 
						&& !post.getPostCurrency().equals(dualAcctAttr.accountType.getCurrencyCode())){
					saveTmTxnReject(post, PostingFlag.F44);
					return createI5001OutputItem(post, null, acct, PostingFlag.F44, inputSource);
				}
			}
			
			if(post.getPostCurrency().equals(acctAttr.accountType.getCurrencyCode())){
				post.setAcctType(acctAttr.accountType);
			}else{
				post.setAcctType(dualAcctAttr.accountType);
			}
			
			// 获取TxnCd相关信息
			TxnCd txnCd = parameterFacility.retrieveParameterObject(post.getTxnCode(), TxnCd.class);
			if(txnCd == null)
			{
				throw new IllegalArgumentException("交易挂账原因太严重啦 :TxnSeq["+post.getTxnSeq()
						+"],TxnCode["+post.getTxnCode()
						+"],LogicalCardNo["+post.getLogicCardNbr()
						+"],RefNbr["+post.getRefNbr()
						+"],PostingFlag["+PostingFlag.F04+"]");
//				saveTmTxnReject(post, PostingFlag.F04);
//				return createI5001OutputItem(post, null, acct, PostingFlag.F04, inputSource);
			}
			
			// 交易装载时，已经将TXN_DESC赋值，且不覆盖
			if(StringUtils.isBlank(post.getTxnDesc())){
				post.setTxnDesc(txnCd.description);
			}
			post.setTxnShortDesc(txnCd.shortDesc);
			
			//校验信用计划模板是否存在(部分交易为结转交易、积分交易等，的确无需信用计划)
			PlanTemplate planTemplate = null;
			if(post.getPostTxnType() == PostTxnType.M)
			{
				if(txnCd.planType != null)
				{
//					try{
					logger.debug("txnCd.planType=" +txnCd.planType);
					
					String planNbr = productCredit.planNbrList.get(txnCd.planType);
					logger.debug("planNbr=" +planNbr);
					for (Entry<PlanType, String> entry : productCredit.planNbrList.entrySet()) {
						logger.debug("key="+entry.getKey() +";value=" +entry.getValue());
					}
					//信用计划类型对应信用计划号不存在挂账
					if(StringUtils.isBlank(planNbr)){
						saveTmTxnReject(post, PostingFlag.F16);
						return createI5001OutputItem(post, planTemplate, acct, PostingFlag.F16, inputSource);
					}
					
					planTemplate = parameterFacility.retrieveParameterObject(planNbr, PlanTemplate.class);
					logger.debug("planTemplate=" +planTemplate);
//					}catch (Exception e) {
//						throw e;
//					}
					
					//信用计划模板不存在挂账
					if(planTemplate == null){
						saveTmTxnReject(post, PostingFlag.F15);
						return createI5001OutputItem(post, planTemplate, acct, PostingFlag.F15, inputSource);
					}
					
					//若信用计划号为空，则填充
					if(StringUtils.isBlank(post.getPlanNbr())){
						post.setPlanNbr(planNbr);
					}
				}
				//信用计划类型不存在挂账
				else
				{
					saveTmTxnReject(post, PostingFlag.F14);
					return createI5001OutputItem(post, planTemplate, acct, PostingFlag.F14, inputSource);
				}
			}
			
			//0金额交易，不记账
			logger.debug("还款金额txnAmt="+post.getTxnAmt());
			if(post.getTxnAmt().compareTo(BigDecimal.ZERO) !=0){
				em.persist(post);
				//em.merge(post);
			}
			
			return createI5001OutputItem(post, planTemplate, acct, PostingFlag.F00, inputSource);
			
		} catch (Exception e) {
			logger.error("金融交易预处理异常, 交易流水号{}",post.getTxnSeq());
			throw e;
		}
		
	}

	/**
	 * 输出对象，报表及总账
	 * 
	 * @param post
	 * @param planTemplate
	 * @param acct
	 * @param postingFlag
	 * @param inputSource
	 * @return
	 */
	private TxnPrepareSet createI5001OutputItem(CcsPostingTmp post, PlanTemplate planTemplate, CcsAcct acct, PostingFlag postingFlag, InputSource inputSource) {
		//获取TxnCd相关信息
		TxnPrepareSet output = new TxnPrepareSet();
		
		//送报表
		output.setRptTxnItem(makeRptTxnItem(post, postingFlag));
		
		//0金额交易，不记账
		if(post.getTxnAmt().compareTo(BigDecimal.ZERO) !=0){
			
			//不曾挂账,就送总账
			if(post.getPrePostingFlag() == PostingFlag.F00)
			{
				//拆分发卡方应收手续费送总账
				if(post.getFeeProfit().compareTo(BigDecimal.ZERO) != 0){
					switch (inputSource){
					case BANK:
						output.getGlTxnItemList().add(createGlTxnItem(post, null, acct, null, SysTxnCd.S27,false, null));
						break;
					case CUP:
						output.getGlTxnItemList().add(createGlTxnItem(post, null, acct, null, SysTxnCd.S20,false, null));
						break;
					case THIR:
						output.getGlTxnItemList().add(createGlTxnItem(post, null, acct, null, SysTxnCd.S59,false, null));
						break;
					default: throw new IllegalArgumentException("不支持的交易来源类型:" + inputSource);
					}
				} 
				//拆分发卡方应付手续费送总账
				if(post.getFeePayout().compareTo(BigDecimal.ZERO) != 0 ){
					switch (inputSource){
					case CUP:
						output.getGlTxnItemList().add(createGlTxnItem(post, null, acct, null, SysTxnCd.S21,false, null));
						break;
					case BANK:
						output.getGlTxnItemList().add(createGlTxnItem(post, null, acct, null, SysTxnCd.S99,false, null));
						break;
					case THIR: 
						output.getGlTxnItemList().add(createGlTxnItem(post, null, acct, null, SysTxnCd.S60,false, null));
						break;
					default: throw new IllegalArgumentException("不支持的交易来源类型:" + inputSource);
					}
				}
				
				// V卡 + 银联渠道 + 消费类交易 = 生成捐献交易
				if(acct!=null && isVCard(acct) && inputSource==InputSource.CUP){
					SysTxnCd cd=null;
					boolean needGen = false;
					if(isAuth(post)){
						cd=SysTxnCd.S68;
						needGen = true;
					}else if(isReturn(post)){
						cd=SysTxnCd.S79;
						needGen = true;
					}
					if(needGen){
						BigDecimal donateAmt = calcDonateAmt(post.getPostAmt(), post.getFeeProfit().subtract(post.getFeePayout()).abs());
						if(donateAmt.compareTo(BigDecimal.ZERO) >0){
							output.getGlTxnItemList().add(createGlTxnItem(post, null, acct, null,cd,false, donateAmt));
						}
					}
				}
				
				//挂账交易送总账
				if(postingFlag != PostingFlag.F00) 
				{
					if(postingFlag == PostingFlag.F04){
						if(post.getDbCrInd() == DbCrInd.D && inputSource == InputSource.BANK){
							output.getGlTxnItemList().add(createGlTxnItem(post, planTemplate, acct, null, SysTxnCd.S31,true, null));
						}else if(post.getDbCrInd() == DbCrInd.C && inputSource == InputSource.BANK){
							output.getGlTxnItemList().add(createGlTxnItem(post, planTemplate, acct, null, SysTxnCd.S32,true, null));
						}else if(post.getDbCrInd() == DbCrInd.D && inputSource == InputSource.CUP){
							output.getGlTxnItemList().add(createGlTxnItem(post, planTemplate, acct, null, SysTxnCd.S33,true, null));
						}else if(post.getDbCrInd() == DbCrInd.C && inputSource == InputSource.CUP){
							output.getGlTxnItemList().add(createGlTxnItem(post, planTemplate, acct, null, SysTxnCd.S34,true, null));
						}else if(post.getDbCrInd() == DbCrInd.D && inputSource == InputSource.THIR){
							output.getGlTxnItemList().add(createGlTxnItem(post, planTemplate, acct, null, SysTxnCd.S61,true, null));
						}else if(post.getDbCrInd() == DbCrInd.C && inputSource == InputSource.THIR){
							output.getGlTxnItemList().add(createGlTxnItem(post, planTemplate, acct, null, SysTxnCd.S62,true, null));
						}else if(post.getDbCrInd() == DbCrInd.D && inputSource == InputSource.THIR){
							output.getGlTxnItemList().add(createGlTxnItem(post, planTemplate, acct, null, SysTxnCd.S61,true, null));
						}else if(post.getDbCrInd() == DbCrInd.C && inputSource == InputSource.THIR){
							output.getGlTxnItemList().add(createGlTxnItem(post, planTemplate, acct, null, SysTxnCd.S62,true, null));
						}else if(post.getDbCrInd() == DbCrInd.C && inputSource == InputSource.SUNS){
							//TODO
						}else if(post.getDbCrInd() == DbCrInd.D && inputSource == InputSource.SUNS){
							//TODO
						}else{
							throw new IllegalArgumentException("借贷记标识非法:"+ post.getDbCrInd() +"或交易渠道非法:" + inputSource);
						}
					}else{
						output.getGlTxnItemList().add(createGlTxnItem(post, planTemplate, acct, post.getTxnCode(), null,true, null));
					}
				}
			}
		}
		
		return output;
		
	}
	
	/**
	 * 支持V卡签到签退交易的，都是V卡
	 * 
	 * @param acct
	 * @return
	 */
	private boolean isVCard(CcsAcct acct) {
		AuthProduct aProduct = parameterFacility.retrieveParameterObject(acct.getProductCd(), AuthProduct.class);
		if(aProduct!=null && aProduct.isSupportVCardSign!=null){
			return aProduct.isSupportVCardSign;
		}else{
			return false;
		}
	}

	/**
	 * 消费类交易 = 逻辑模块L01 + 消费信用计划
	 * 
	 * @param post 
	 * @return
	 */
	private boolean isAuth(CcsPostingTmp post) {
		TxnCd txnCd = parameterFacility.retrieveParameterObject(post.getTxnCode(), TxnCd.class);
		if(txnCd!=null && txnCd.logicMod == LogicMod.L01 && txnCd.planType == PlanType.R){
			return true;
		}else{
			return false;
		}
	}
	
	
	
	/**
	 * 消费类交易 = 逻辑模块L02 
	 * 
	 * @param post 
	 * @return
	 */
	private boolean isReturn(CcsPostingTmp post) {
		TxnCd txnCd = parameterFacility.retrieveParameterObject(post.getTxnCode(), TxnCd.class);
		if(txnCd!=null && txnCd.logicMod == LogicMod.L02){
			return true;
		}else{
			return false;
		}
	}
	

	/**
	 * 计算V卡捐献金额
	 * @param bigDecimal 
	 * 
	 * @param bigDecimal
	 * @return
	 */
	private static BigDecimal calcDonateAmt(BigDecimal postAmt, BigDecimal feeProfit) {
		BigDecimal baseDonateAmt = new BigDecimal(0.05);
		BigDecimal baseStepAmt = new BigDecimal(50);
		// 起步50以下不捐, 50以上(含50)每50，捐0.05
		BigDecimal step = postAmt.divide(baseStepAmt, 0, RoundingMode.DOWN);
		BigDecimal calcDonateAmt = baseDonateAmt.multiply(step).setScale(2, RoundingMode.HALF_UP);
		// 捐献金额10封顶
		if(calcDonateAmt.compareTo(BigDecimal.TEN) >0){
			calcDonateAmt = BigDecimal.TEN;
		}
		// 捐献金额>手续费 =手续费
		if(calcDonateAmt.compareTo(feeProfit) >0){
			calcDonateAmt = feeProfit;
		}
		return calcDonateAmt;
	}
	
	public static void main(String[] args) {
		BigDecimal b = calcDonateAmt(new BigDecimal(1111111), new BigDecimal(9));
		System.out.println(b);
	}


	/**
	 * @see 方法名：makeRptTxnItem 
	 * @see 描述：挂账交易送报表接口
	 * @see 创建日期：2015-6-24下午5:39:06
	 * @author ChengChun
	 *  
	 * @param post
	 * @param flag
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public RptTxnItem makeRptTxnItem(CcsPostingTmp post, PostingFlag flag) {
		
		RptTxnItem rpt = new RptTxnItem();
		
		rpt.org = post.getOrg();
		rpt.ccsTxnSeq = post.getTxnSeq();
		rpt.acctNo = post.getAcctNbr();
		rpt.acctType = post.getAcctType();
		rpt.cardNo = post.getCardNbr();
		rpt.logicalCardNo = post.getLogicCardNbr();
		rpt.bscLogiccardNo = post.getCardBasicNbr();
		rpt.productCd = post.getProductCd();
		rpt.txnDate = post.getTxnDate();
		rpt.txnTime = post.getTxnTime();
		rpt.postTxnType = post.getPostTxnType();
		rpt.txnCode = post.getTxnCode();
		rpt.postAmt = post.getPostAmt();
		rpt.postDate = post.getPostDate();
		rpt.authCode = post.getAuthCode();
		rpt.postCurrCd = post.getPostCurrency();
		rpt.planNbr = post.getPlanNbr();
		rpt.refNbr = post.getRefNbr();
		rpt.point = post.getPoints();
		rpt.stmtDate = post.getStmtDate();
		rpt.prePostingFlag = post.getPrePostingFlag();
		rpt.postingFlag = flag;
		
		return rpt;
	}


	/**
	 * 挂账交易送总账接口/手续费拆分送总账接口
	 * 
	 * @param post
	 * @param planTemplate
	 * @param acct
	 * @param txnCd
	 * @param sysTxnCd
	 * @return
	 */
	private GlTxnItem createGlTxnItem(CcsPostingTmp post, PlanTemplate planTemplate, CcsAcct acct, String txnCd, SysTxnCd sysTxnCd,boolean isRejectTxn, BigDecimal donateAmt)
	{
		
		GlTxnItem gl = new GlTxnItem();
		
		if(acct == null){
			gl.ageCd = "0";
			gl.owningBranch = null;
		}else{
			gl.ageCd = acct.getAgeCode();
			gl.owningBranch = acct.getOwningBranch();
		}
		if(isRejectTxn){
			gl.ageCd = "0";
		}
		
		gl.planNbr = planTemplate == null ? "999999" : planTemplate.planNbr;
		gl.cardNo = post.getCardNbr();
		gl.txnDesc = post.getTxnDesc();
		gl.org = post.getOrg();
		gl.currCd = post.getPostCurrency();
		gl.txnSeq = String.valueOf(post.getTxnSeq());
		gl.acctNo = post.getAcctNbr();
		gl.acctType = post.getAcctType();
		gl.dbCrInd = post.getDbCrInd();
		gl.postDate = post.getPostDate();
		gl.postGlInd = PostGlIndicator.S;
		gl.acqBranchId = post.getAcqBranchIq();
		gl.bucketType = BucketType.Pricinpal;
		gl.txnCode = txnCd;
		gl.postAmount = post.getPostAmt();
		
		if(sysTxnCd != null){
			SysTxnCdMapping sysTxnCdMapping = parameterFacility.loadParameter(String.valueOf(sysTxnCd), SysTxnCdMapping.class);
			switch (sysTxnCd){
			case S20:
			case S27:
			case S59:
				gl.postAmount = post.getFeeProfit();
				gl.txnCode = sysTxnCdMapping.txnCd;
				gl.postGlInd = PostGlIndicator.N;
				gl.ageCd = "0";
				break;
			case S21:
			case S99:
			case S60:
				gl.postAmount = post.getFeePayout();
				gl.txnCode = sysTxnCdMapping.txnCd;
				gl.postGlInd = PostGlIndicator.N;
				gl.ageCd = "0";
				break;
			case S31:
			case S32:
			case S33:
			case S34:
			case S61:
			case S62:
				gl.txnCode = sysTxnCdMapping.txnCd;
				break;
			case S68:
			case S79:
				gl.postAmount = donateAmt;
				gl.txnCode = sysTxnCdMapping.txnCd;
				gl.postGlInd = PostGlIndicator.N;
				gl.ageCd = "0";
				break;
			default:throw new RuntimeException("必须指定内部交易码");
			}
			
		}
		return gl;
	}


	/**
	 * 拒绝交易写入拒绝重入账交易临时表
	 * 
	 * @param post
	 * @param flag
	 */
	private void saveTmTxnReject(CcsPostingTmp post, PostingFlag flag) {
		
		CcsTxnReject rej = new CcsTxnReject();
		
		rej.updateFromMap(post.convertToMap());
		rej.setPostingFlag(flag);
		
		//往日挂账or新挂账，故用merge
		em.merge(rej);
	}

}
