package com.sunline.ccs.batch.cc6000;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.PostTxnType;
import com.sunline.ppy.dictionary.enums.PostingFlag;
import com.sunline.ppy.dictionary.report.ccs.TxnPointsRptItem;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ccs.batch.cc6000.common.LogicalModuleExecutor;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.param.def.MccCtrl;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.enums.LogicMod;


/**
 * @see 类名：P6012PointsPosting
 * @see 描述： 交易积分入账
 *      范围：需要累计积分的交易（TXN_CODE【交易码参数表】；MCC_CTL【MCC参数控制表】；BLOCK_CD【锁定码参数表】）
 *
 * @see 创建日期：   2015-6-24上午10:05:43
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class P6012PointsPosting implements ItemProcessor<S6000AcctInfo, S6000AcctInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	@Autowired
	private LogicalModuleExecutor logicalModuleExecutor;

	@Override
	public S6000AcctInfo process(S6000AcctInfo item) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("交易积分入账：Org["+item.getAccount().getOrg()
					+"],AcctType["+item.getAccount().getAcctType()
					+"],AcctNo["+item.getAccount().getAcctNbr()
					+"],TxnPost.size["+item.getTxnPosts().size()
					+"]");
		}

		// 有锁定码指示不做积分的帐户，则所有交易不去积分
		if (!blockCodeUtils.getMergedPointEarnInd(item.getAccount().getBlockCode())) return item;

		for (CcsPostingTmp txnPost : item.getTxnPosts()) {
			// 交易码参数
			TxnCd txnCd = parameterFacility.loadParameter(txnPost.getTxnCode(), TxnCd.class);
			
			// 金融交易累计积分
			if(txnPost.getPostTxnType()==PostTxnType.M
					// 锁定码指示判断累计积分
					&& blockCodeUtils.getMergedPointEarnInd(txnPost.getCardBlockCode())
					// 成功入账交易累计积分
					&& txnPost.getPostingFlag() == PostingFlag.F00
					// 交易码参数设定判断累计积分
					&& txnCd.bonusPntInd == Indicator.Y){
				
				// MCC参数设定判断累计积分. 若MCC不存在, 默认累计积分
				MccCtrl mcc = parameterFacility.retrieveParameterObject(txnPost.getMcc()+"|CUP", MccCtrl.class);
				if (mcc != null && !mcc.bonusPntInd) continue;
				
				// 积分入账(61:积分增加; 62:积分减少; 63:积分兑换)
				if (DbCrInd.D == txnCd.logicMod.getDbCrInd()) {
					// 积分交易入账：61：积分增加；62：积分减少；63：积分兑换
					logicalModuleExecutor.executeLogicalModule(LogicMod.L61, item, txnPost, null);
				}
				else if (DbCrInd.C == txnCd.logicMod.getDbCrInd()) {
					// 积分交易入账：61：积分增加；62：积分减少；63：积分兑换
					logicalModuleExecutor.executeLogicalModule(LogicMod.L62, item, txnPost, null);
				}
	
				// 增加内部生成积分交易报表
				this.generateTxnPoints(item, txnPost);
			}
		}

		return item;
	}
	
	/**
	 * 内部生成积分交易报表
	 * @param item
	 * @param txnPost
	 * @param point 交易积分
	 */
	public void generateTxnPoints(S6000AcctInfo item, CcsPostingTmp txnPost) {
		
		TxnPointsRptItem txnPoints = new TxnPointsRptItem();
		txnPoints.org = item.getAccount().getOrg();
		txnPoints.acctNo = item.getAccount().getAcctNbr();
		txnPoints.acctType = item.getAccount().getAcctType();
		txnPoints.cardNo = txnPost.getCardNbr();
		txnPoints.txnDate = txnPost.getTxnDate();
		txnPoints.txnTime = txnPost.getTxnTime();
		txnPoints.txnCode = txnPost.getTxnCode();
		txnPoints.glPostAmt = txnPost.getPostAmt();
		txnPoints.refNbr = txnPost.getRefNbr();
		txnPoints.point = txnPost.getPoints();
		
		item.getTxnPointss().add(txnPoints);
	}
}
