package com.sunline.ccs.batch.cc2900;

import java.math.BigDecimal;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.ppy.dictionary.enums.PostTxnType;
import com.sunline.ppy.dictionary.enums.PostingFlag;
import com.sunline.ppy.dictionary.report.ccs.PointsRptItem;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ccs.batch.common.TxnPrepare;
import com.sunline.ccs.infrastructure.server.repos.RCcsPointsReg;
import com.sunline.ccs.infrastructure.shared.model.CcsPointsReg;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.OrganizationContextHolder;

/**
 * 
 * @see 类名：P2901PointsAdjImp
 * @see 描述：* CMD045 积分调整交易生成
 *            根据联机积分调整通知文件在批量中生成积分调整金融交易
                                          输入接口：积分调整通知临时表（与概要设计有出入）
                                          输出接口：积分交易金融流水（TtTxnPrepare）
                                         相关主表：帐户主表，逻辑卡主表等
 * @see 创建日期：   2015-6-23下午7:32:15
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P2901PointsAdjImp implements ItemProcessor<CcsPointsReg, PointsRptItem> {
	/**
	 * 获取参数工具类
	 */
	@Autowired
	private UnifiedParameterFacility parameterFacility;

	/**
	 * 积分调整临时表
	 */
	@Autowired
	private RCcsPointsReg rPointsReg;

	/**
	 * 批量日期
	 */
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	@Autowired
	private TxnPrepare txnPostPreEdit;
	
	@Override
	public PointsRptItem process(CcsPointsReg pointReg) throws Exception {
		OrganizationContextHolder.setCurrentOrg(pointReg.getOrg());
		
		// 生成TtTxnPost记录
		// 创建金融交易预处理临时对象
		CcsPostingTmp ttTxnPost = new CcsPostingTmp();
		ttTxnPost.setOrg(pointReg.getOrg());
		ttTxnPost.setAcctNbr(null);
		ttTxnPost.setAcctType(null);
		ttTxnPost.setCardNbr(pointReg.getCardNbr());
		ttTxnPost.setLogicCardNbr(null);
		ttTxnPost.setCardBasicNbr(null);
		ttTxnPost.setProductCd(null);
		ttTxnPost.setTxnDate(pointReg.getTxnDate());
		ttTxnPost.setTxnTime(pointReg.getRequestTime());
		ttTxnPost.setPostTxnType(PostTxnType.P);
		switch (pointReg.getAdjInd()) {
		case A:
			SysTxnCdMapping txnCdMapping1 = parameterFacility.loadParameter(SysTxnCd.S14.toString(), SysTxnCdMapping.class);
			ttTxnPost.setTxnCode(txnCdMapping1.txnCd);
			ttTxnPost.setOrigTxnCode(txnCdMapping1.txnCd);
			TxnCd txnCd1 = parameterFacility.loadParameter(txnCdMapping1.txnCd, TxnCd.class);
			ttTxnPost.setTxnDesc(txnCd1.description);
			ttTxnPost.setTxnShortDesc(txnCd1.shortDesc);
			break;
		case D:
			SysTxnCdMapping txnCdMapping2 = parameterFacility.loadParameter(SysTxnCd.S16.toString(), SysTxnCdMapping.class);
			ttTxnPost.setTxnCode(txnCdMapping2.txnCd);
			ttTxnPost.setOrigTxnCode(txnCdMapping2.txnCd);
			TxnCd txnCd2 = parameterFacility.loadParameter(txnCdMapping2.txnCd, TxnCd.class);
			ttTxnPost.setTxnDesc(txnCd2.description);
			ttTxnPost.setTxnShortDesc(txnCd2.shortDesc);
			break;
		case I:
			SysTxnCdMapping txnCdMapping3 = parameterFacility.loadParameter(SysTxnCd.S15.toString(), SysTxnCdMapping.class);
			ttTxnPost.setTxnCode(txnCdMapping3.txnCd);
			ttTxnPost.setOrigTxnCode(txnCdMapping3.txnCd);
			TxnCd txnCd3 = parameterFacility.loadParameter(txnCdMapping3.txnCd, TxnCd.class);
			ttTxnPost.setTxnDesc(txnCd3.description);
			ttTxnPost.setTxnShortDesc(txnCd3.shortDesc);
			break;
		default:
			break;
		}
		ttTxnPost.setTxnAmt(pointReg.getPoints());
		ttTxnPost.setOrigSettAmt(pointReg.getPoints());
		ttTxnPost.setOrigTxnAmt(pointReg.getPoints());
		ttTxnPost.setPostAmt(pointReg.getPoints());
		ttTxnPost.setPostDate(null);
		ttTxnPost.setAuthCode(null);
		ttTxnPost.setCardBlockCode(null);
		ttTxnPost.setTxnCurrency(pointReg.getAcctType().getCurrencyCode());
		ttTxnPost.setPostCurrency(pointReg.getAcctType().getCurrencyCode());
		ttTxnPost.setOrigTransDate(batchStatusFacility.getBatchDate());
		ttTxnPost.setRefNbr(pointReg.getRefNbr());
		ttTxnPost.setPoints(BigDecimal.ZERO);
		ttTxnPost.setPostingFlag(PostingFlag.F00);
		ttTxnPost.setPrePostingFlag(PostingFlag.F00);
		ttTxnPost.setRelPmtAmt(BigDecimal.ZERO);
		ttTxnPost.setOrigPmtAmt(BigDecimal.ZERO);
		ttTxnPost.setAcqBranchIq(null);
		ttTxnPost.setAcqTerminalId(null);
		ttTxnPost.setAcqAddress(null);
		ttTxnPost.setAcqAcceptorId(null);
		ttTxnPost.setMcc(null);
		ttTxnPost.setFeePayout(BigDecimal.ZERO);
		ttTxnPost.setInterchangeFee(BigDecimal.ZERO);
		ttTxnPost.setFeeProfit(BigDecimal.ZERO);
		ttTxnPost.setLoanIssueProfit(BigDecimal.ZERO);
		ttTxnPost.setStmtDate(null);
		ttTxnPost.setVoucherNo(null);
		ttTxnPost.setOrigTxnCode(ttTxnPost.getTxnCode());
		
		txnPostPreEdit.txnPrepare(ttTxnPost, null);
		rPointsReg.delete(pointReg);
		
		// 生成积分报表接口
		PointsRptItem rptItem = new PointsRptItem();
		rptItem.procDate = batchStatusFacility.getBatchDate();
		rptItem.org = pointReg.getOrg();
		rptItem.cardNo = pointReg.getCardNbr();
		rptItem.acctNo = pointReg.getAcctNbr();
		rptItem.adjInd = pointReg.getAdjInd();
		rptItem.txnCd = ttTxnPost.getTxnCode();
		rptItem.points = pointReg.getPoints();
		rptItem.refNbr = pointReg.getRefNbr();
		
		return rptItem;
	}
}
