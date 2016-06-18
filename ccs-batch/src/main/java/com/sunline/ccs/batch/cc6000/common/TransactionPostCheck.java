package com.sunline.ccs.batch.cc6000.common;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ppy.dictionary.enums.PostTxnType;
import com.sunline.ppy.dictionary.enums.PostingFlag;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.param.def.BlockCode;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.enums.PostAvailiableInd;


/** 
 * @see 类名：PostTransactionCheck
 * @see 描述：入账交易检查
 *
 * @see 创建日期：   2015年6月25日 下午3:06:21
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class TransactionPostCheck {

	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	

	/**
	 * 交易检查的实现逻辑
	 * @param account 入账账户
	 * @param txnPost 入账交易
	 * @return 返回入账结果代码
	 */
	public PostingFlag postTransactionCheck(CcsAcct account, CcsPostingTmp txnPost, Date batchDate) {
		// TXN_DATE小于开户日；TXN_DATE大于当前批量日期
		if (txnPost.getTxnDate().before(account.getSetupDate())
				|| txnPost.getTxnDate().after(batchDate)) {
			// 05：交易日期不合法
			return PostingFlag.F05;
		}
		
		// 交易码不存在于TXN_CD参数中
		TxnCd txnCd = parameterFacility.retrieveParameterObject(txnPost.getTxnCode(), TxnCd.class);
		if (txnCd == null || txnCd.txnCd == null) {
			// 04：交易代码不合法
			return PostingFlag.F04;
		}
		
		// 入账交易借贷方向不匹配，积分交易不检查借贷方向
		if (!PostTxnType.P.equals(txnPost.getPostTxnType()) && txnCd.logicMod.getDbCrInd() != txnPost.getDbCrInd()) {
			// 08：交易借贷方向不匹配
			return PostingFlag.F08;
		}
		
		// 入账是否要检查BlockCode
		if (txnCd.blkcdCheckInd){
			boolean isPost = this.checkPostInd(account.getBlockCode(), txnPost);
			if(!isPost){
				return PostingFlag.F01;
			}else{
				isPost = this.checkPostInd(txnPost.getCardBlockCode(), txnPost);
				if(!isPost){
					return PostingFlag.F02;
				}
			}
		}
		return null;
	}
	
	/**
	 * 检查锁定码是否允许入账
	 * 
	 * @param blockCodes
	 * @param txnPost
	 * @return
	 */
	public boolean checkPostInd(String blockCodes, CcsPostingTmp txnPost) {
		boolean isPost = true;
		if(StringUtils.isNotBlank(blockCodes)){
			// 获取优先级最高的锁定码
			BlockCode block = blockCodeUtils.getFirstByPriority(blockCodes);
			// 判断入账许可指示
			if (block.postInd == PostAvailiableInd.R) {
				isPost = false;
			} else if (block.postInd == PostAvailiableInd.D
					&& (txnPost.getPostTxnType()==PostTxnType.P || txnPost.getDbCrInd()==DbCrInd.D)) {
				isPost = false;
			}
		}
		return isPost;
	}
}
