package com.sunline.ccs.batch.cc6000.common;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ppy.dictionary.enums.PostTxnType;
import com.sunline.ppy.dictionary.enums.UnmatchStatus;


/**
 * @see 类名：AuthMemoMatch
 * @see 描述：授权匹配处理
 *
 * @see 创建日期：   2015-6-24下午6:57:12
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class AuthMemoMatcher {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private UnifiedParameterFacility parameterFacility;

	/**
	 * @see 方法名：authorizeMatch 
	 * @see 描述： 授权匹配处理
	 * @see 创建日期：2015-6-24下午6:57:43
	 * @author ChengChun
	 *  
	 * @param txnPost
	 * @param unmatchs
	 * @param unmatchStates
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void authorizeMatch(CcsPostingTmp txnPost, List<CcsAuthmemoO> unmatchs, List<UnmatchStatus> unmatchStates) {
		if (logger.isDebugEnabled()) {
			logger.debug("授权匹配-清算信息:PostAmt["+txnPost.getPostAmt()
					+"],AuthCode["+txnPost.getAuthCode()
					+"],CardNo["+txnPost.getCardNbr()
					+"],DbCrInd["+txnPost.getDbCrInd()
					+"],ProductCd["+txnPost.getProductCd()
					+"],PostTxnType["+txnPost.getPostTxnType()
					+"]");
		}
		// MEMO类交易，或积分交易不做匹配
		if (DbCrInd.M == txnPost.getDbCrInd() || PostTxnType.P == txnPost.getPostTxnType()) return;
		// 交易中，授权码为NULL则不做匹配
		if (txnPost.getAuthCode() == null) return;
		// 交易中，借贷标识为NULL则不做匹配
		if (txnPost.getDbCrInd() == null) return;
		// 授权匹配逻辑:
		for (int i = 0; i < unmatchs.size(); i++) {
			if (logger.isDebugEnabled()) {
				logger.debug("授权匹配-授权信息:Org["+unmatchs.get(i).getOrg()
						+"],LogKv["+unmatchs.get(i).getLogKv()
						+"],LogicalCardNo["+unmatchs.get(i).getLogicCardNbr()
						+"],ChbTxnAmt["+unmatchs.get(i).getChbTxnAmt()
						+"],AuthCode["+unmatchs.get(i).getAuthCode()
						+"],B002["+unmatchs.get(i).getB002CardNbr()
						+"],FinalUpdDirection["+unmatchs.get(i).getFinalUpdDirection()
						+"],FinalAction["+unmatchs.get(i).getFinalAction()
						+"],B007["+unmatchs.get(i).getB007TxnTime()
						+"]");
				logger.debug("授权匹配-交易信息:Org["+txnPost.getOrg()
						+"],RefNbr["+txnPost.getRefNbr()
						+"],LogicalCardNo["+txnPost.getLogicCardNbr()
						+"],ChbTxnAmt["+txnPost.getTxnAmt()
						+"],AuthCode["+txnPost.getAuthCode()
						+"],CardNbr["+txnPost.getCardNbr()
						+"],DbCrInd["+txnPost.getDbCrInd()
						+"],ProductCd["+txnPost.getProductCd()
						+"],TxnDate["+txnPost.getTxnDate()
						+"]");
			}
			// 标记授权匹配交易的匹配状态，U-未匹配
			//if (unmatchStates.size() == i) unmatchStates.add(UnmatchStatus.U);
			//交易时间不能为空
			if (StringUtils.isBlank(unmatchs.get(i).getB007TxnTime()) || txnPost.getTxnDate() == null){ 
				if(logger.isDebugEnabled()){
					logger.debug("授权匹配失败-authmemo或txnPost交易时间不能为空");
				}
				continue;
			}
			// 交易日期相同，挂账调整时间会导致交易不匹配
			SimpleDateFormat sdf = new SimpleDateFormat("MMdd");
			if (!sdf.format(txnPost.getTxnDate()).equals(unmatchs.get(i).getB007TxnTime().substring(0, 4))){
				if(logger.isDebugEnabled()){
					logger.debug("授权匹配失败-authmemo和txnPost交易日期相同不相等");
				}
				continue;
			}
//			//交易refnbr不为空
//			if (StringUtils.isBlank(unmatchs.get(i).getB037RefNbr()) || txnPost.getRefNbr() == null) continue;
//			//交易refnbr相同
//			if (!txnPost.getRefNbr().equals(unmatchs.get(i).getB037RefNbr())) continue;
			// 交易授权通过
			if (!AuthAction.A.equals(unmatchs.get(i).getFinalAction())){
				if(logger.isDebugEnabled()){
					logger.debug("授权匹配失败-authmemo最终行动不是通过A");
				}
				continue;
			}
			// 成功匹配
			if (unmatchStates.get(i) == UnmatchStatus.M){
				if(logger.isDebugEnabled()){
					logger.debug("授权匹配失败-重复匹配");
				}
				continue;
			}
			// 卡号相等
			if (!txnPost.getCardNbr().equals(unmatchs.get(i).getB002CardNbr())){
				if(logger.isDebugEnabled()){
					logger.debug("授权匹配失败-authmemo和txnPost卡号不相等");
				}
				continue;
			}
			// 授权码相等
			if (!txnPost.getAuthCode().equals(unmatchs.get(i).getAuthCode())){
				if(logger.isDebugEnabled()){
					logger.debug("授权匹配失败-authmemo和txnPost授权码不相等");
				}
				continue;
			}
			// 借贷方向相同
			if (!txnPost.getDbCrInd().toString().equals(unmatchs.get(i).getFinalUpdDirection())){
				if(logger.isDebugEnabled()){
					logger.debug("授权匹配失败-authmemo和txnPost借贷方向不相等");
				}
				continue;
			}

			ProductCredit productC = parameterFacility.loadParameter(txnPost.getProductCd(), ProductCredit.class);
			// 交易金额在授权金额的80%-120%之间
			if (!(txnPost.getTxnAmt().compareTo(unmatchs.get(i).getTxnAmt()
						.multiply(BigDecimal.ONE.subtract(productC.athMatchTolRt))) >= 0
					&& txnPost.getTxnAmt().compareTo(unmatchs.get(i).getTxnAmt()
						.multiply(BigDecimal.ONE.add(productC.athMatchTolRt))) <= 0)){
				if(logger.isDebugEnabled()){
					logger.debug("授权匹配失败-txnPost交易金额超出authmemo交易金额*(1+/-授权匹配容忍比例[{}])",productC.athMatchTolRt);
				}
				continue;
			}
			// 匹配成功，M-成功匹配
			unmatchStates.set(i, UnmatchStatus.M);
			
			return;
		}
	}
}
