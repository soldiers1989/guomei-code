package com.sunline.ccs.batch.cc6000;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.report.ccs.OverLimitAccountRptItem;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ccs.batch.cc6000.common.Calculator;
import com.sunline.ccs.batch.cc6000.common.TransactionGenerator;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.ChargeDateInd;
import com.sunline.acm.service.sdk.BatchStatusFacility;


/** 
 * @see 类名：P6030GenerateOverLimitFee
 * @see 描述：收取超限费
 *
 * @see 创建日期：   2015年6月25日 下午2:13:38
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class P6030GenerateOverLimitFee implements ItemProcessor<S6000AcctInfo, S6000AcctInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private TransactionGenerator generatorTransaction;
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	@Autowired
	private Calculator calculator;
	@Autowired
    private BatchStatusFacility batchFacility;

	@Override
	public S6000AcctInfo process(S6000AcctInfo item) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("收取超限费：Org["+item.getAccount().getOrg()
					+"],AcctNo["+item.getAccount().getAcctNbr()
					+"],AcctType["+item.getAccount().getAcctType()
					+"],BlockCode["+item.getAccount().getBlockCode()
					+"],WaiveOvlfeeInd["+item.getAccount().getWaiveOvlfeeInd()
					+"]");
		}
		// 检查账户锁定码免除超限费标志
		if (blockCodeUtils.getMergedOvrlmtFeeWaiveInd(item.getAccount().getBlockCode())) return item;
		// 检查账户超限费免除标示
		if (item.getAccount().getWaiveOvlfeeInd() == Indicator.Y) return item;
		
		// 获取参数
		ProductCredit productCredit = parameterFacility.loadParameter(item.getAccount().getProductCd(), ProductCredit.class);
		// 计算超限金额
		BigDecimal overAmt = calculator.getOverLimitAmt(item.getAccount(), item.getPlans(), batchFacility.getBatchDate());
		// 当期最高超限金额
		if (overAmt.compareTo(item.getAccount().getCtdMaxOvrlmtAmt()) > 0) item.getAccount().setCtdMaxOvrlmtAmt(overAmt);

		// 超限当日收取超限费
		if (ChargeDateInd.P.equals(productCredit.overlimitCharge.chargeDateInd)) {
			// 需要在维护主表时，判断是否超限，如果不超限将账户的超限日期更新为空
			if (item.getAccount().getOvrlmtDate() != null) return item;
		}
		// 账单日收取超限费
		else if (ChargeDateInd.C.equals(productCredit.overlimitCharge.chargeDateInd)) {
			// 下一账单日=当前批量日期，或上一批量日期<下一账单日<当前批量日期，否则退出
			if (!batchFacility.shouldProcess(item.getAccount().getNextStmtDate())) return item;
			// 超限费计算方法
			switch (productCredit.overlimitCharge.calcInd) {
			case P: break;
			case H: overAmt = item.getAccount().getCtdMaxOvrlmtAmt(); break;
			default: throw new IllegalArgumentException("卡产品参数中，超限费计算方法：["+productCredit.overlimitCharge.calcInd+"]不存在!");
			}
		} 
		else throw new IllegalArgumentException("卡产品参数中，超限费收取日期：["+productCredit.overlimitCharge.chargeDateInd+"]不存在!");

		if (logger.isDebugEnabled()) {
			logger.debug("超限费计算:chargeDateInd["+productCredit.overlimitCharge.chargeDateInd
					+"],OvrlmtDate["+item.getAccount().getOvrlmtDate()
					+"],NextStmtDate["+item.getAccount().getNextStmtDate()
					+"],overAmt["+overAmt
					+"],CtdHiOvrlmtAmt["+item.getAccount().getCtdMaxOvrlmtAmt()
					+"],YtdOvrlmtFeeAmt["+item.getAccount().getYtdOvrlmtFeeAmt()
					+"],YtdOvrlmtFeeCnt["+item.getAccount().getYtdOvrlmtFeeCnt()
					+"]");
		}
		// 如果超限金额大于零，则调用交易生成和入账逻辑
		if (overAmt.compareTo(BigDecimal.ZERO) > 0) {
			// 生成一笔超限费交易，并入账
			CcsPostingTmp txnPost = generatorTransaction.generateOverLimitFee(item, overAmt, batchFacility.getBatchDate());
			// 超限日期
			if (item.getAccount().getOvrlmtDate() == null) item.getAccount().setOvrlmtDate(batchFacility.getBatchDate());
			
			if (txnPost == null) return item;

			// 本年超限费收取金额
			item.getAccount().setYtdOvrlmtFeeAmt(txnPost.getPostAmt().add(item.getAccount().getYtdOvrlmtFeeAmt()));
			// 本年超限费收取笔数
			item.getAccount().setYtdOvrlmtFeeCnt(item.getAccount().getYtdOvrlmtFeeCnt() + 1);
			// 生成超限账户报表记录
			this.addOverLimitAccount(item, overAmt, txnPost.getPostAmt(), calculator.getCurrCreditLimit(item.getAccount(), batchFacility.getBatchDate()));
		}
		
		return item;
	}
	
	/**
	 * 超限账户报表
	 * @param acctInfo
	 * @param overAmt 超限部分金额
	 * @param overLimitFee 超限费
	 * @param currCreditLimit 当前账户有效额度
	 */
	public void addOverLimitAccount(S6000AcctInfo acctInfo, BigDecimal overAmt, BigDecimal overLimitFee, BigDecimal currCreditLimit) {
		
		OverLimitAccountRptItem overLimitAccount = new OverLimitAccountRptItem();
		overLimitAccount.org = acctInfo.getAccount().getOrg();
		overLimitAccount.acctNo = acctInfo.getAccount().getAcctNbr();
		overLimitAccount.acctType = acctInfo.getAccount().getAcctType();
		overLimitAccount.defaultLogicalCardNo = acctInfo.getAccount().getDefaultLogicCardNbr();
		overLimitAccount.currBal = acctInfo.getAccount().getCurrBal();
		overLimitAccount.overLimitAmt = overAmt;
		overLimitAccount.overLimitFee = overLimitFee;
		overLimitAccount.currCreditLimit = currCreditLimit;
		overLimitAccount.CurrencyCode = acctInfo.getAccount().getAcctType().getCurrencyCode();
		
		acctInfo.getOverLimitAccounts().add(overLimitAccount);
	}
}
