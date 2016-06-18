package com.sunline.ccs.batch.cc6000;

import java.math.BigDecimal;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.utils.DateUtils;
import com.sunline.ccs.batch.cc6000.common.TransactionGenerator;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.param.def.LatePaymentCharge;
import com.sunline.ccs.param.def.Organization;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.CalcBaseInd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.PaymentStatus;


/** 
 * @see 类名：P6034GenerateLateChargeFee
 * @see 描述：收取滞纳金
 *
 * @see 创建日期：   2015年6月25日 下午2:19:52
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class P6034GenerateLateChargeFee implements ItemProcessor<S6000AcctInfo, S6000AcctInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private TransactionGenerator generatorTransaction;
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	@Autowired
    private BatchStatusFacility batchFacility;

	@Override
	public S6000AcctInfo process(S6000AcctInfo item) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("收取滞纳金：Org["+item.getAccount().getOrg()
					+"],AcctNo["+item.getAccount().getAcctNbr()
					+"],AcctType["+item.getAccount().getAcctType()
					+"],WaiveLatefeeInd["+item.getAccount().getWaiveLatefeeInd()
					+"]");
		}
		// 检查账户锁定码免除滞纳金标志
		if (blockCodeUtils.getMergedLateFeeWaiveInd(item.getAccount().getBlockCode())) return item;
		// 检查账户滞纳金免除标示
		if (item.getAccount().getWaiveLatefeeInd() == Indicator.Y) return item;
		//	未经过首次账单日，直接返回
		if (item.getAccount().getPmtDueDate() == null) return item;
		
		// 获取滞纳金参数
		ProductCredit productCr = parameterFacility.loadParameter(item.getAccount().getProductCd(), ProductCredit.class);
		Organization org = parameterFacility.loadParameter(null, Organization.class);
		// 滞纳金参数
		LatePaymentCharge latePayCharge = null;
		// 小额贷款账户使用LoanFeeDef上的定义
//		if(item.getAccount().getAcctType() == AccountType.E){
//			return item; //TODO 小额贷款滞纳金需要按plan计算暂不实现
//		}else{
//			// 贷记账户本币
//			if (org.baseCurrCd.equals(item.getAccount().getCurrency())){
//				latePayCharge = productCr.latePaymentCharge;
//			}
//			// 外币
//			else{ 
//				latePayCharge = productCr.dualLatePaymentCharge;
//			}
//		}
		// 暂不区分贷款账户和贷记账户，都使用产品上的滞纳金参数
		if (org.baseCurrCd.equals(item.getAccount().getCurrency())){
			latePayCharge = productCr.latePaymentCharge;
		}
		// 外币
		else{ 
			latePayCharge = productCr.dualLatePaymentCharge;
		}
		
		
		// 增加宽限天数后的滞纳金收取日
		Date assessDay = DateUtils.addDays(item.getAccount().getPmtDueDate(), latePayCharge.assessDays);
		// 滞纳金收取日=当前批量日期，或上一批量日期<滞纳金收取日<当前批量日期，否则不收取滞纳金
		if (!batchFacility.shouldProcess(assessDay)) return item;
		
		// 宽限日时判定，还款状态C>N>U>B>D
		String paymentHist = item.getAccount().getPaymentHst();
		if(item.getAccount().getQualGraceBal().compareTo(BigDecimal.ZERO) ==0){
			paymentHist = addPaymentHist(PaymentStatus.C, paymentHist);
		}else if(item.getAccount().getCtdRepayAmt().compareTo(BigDecimal.ZERO) ==0){
			paymentHist = addPaymentHist(PaymentStatus.N, paymentHist);
		}else if(item.getAccount().getTotDueAmt().compareTo(BigDecimal.ZERO) >0){
			paymentHist = addPaymentHist(PaymentStatus.U, paymentHist);
		}else if(item.getAccount().getGraceDaysFullInd() == Indicator.Y){
			paymentHist = addPaymentHist(PaymentStatus.B, paymentHist);
		}else{
			paymentHist = addPaymentHist(PaymentStatus.D, paymentHist);
		}
		item.getAccount().setPaymentHst(paymentHist);
		
		// 账户的账龄<滞纳金参数表中的最小账龄，则不收取滞纳金
		if (item.getAccount().getAgeCode().compareTo(latePayCharge.minAgeCd) < 0) return item;
		// 最小还款额<=零，则不收取滞纳金
		if (item.getAccount().getTotDueAmt().compareTo(BigDecimal.ZERO) < 1) return item;		
		
		// 滞纳金基准金额
		BigDecimal latePayAmt = BigDecimal.ZERO;
		// 滞纳金计算基准金额指示： 
		CalcBaseInd calcBaseInd = latePayCharge.calcBaseInd;
		// T - 用总的最小还款额剩余部分（total due）
		if (CalcBaseInd.T.equals(calcBaseInd)) {
			latePayAmt = item.getAccount().getTotDueAmt();
		}
		// L - 用往期最小还款额剩余部分（last due）
		else if (CalcBaseInd.L.equals(calcBaseInd)) {
			latePayAmt = item.getAccount().getPastDueAmt1()
					.add(item.getAccount().getPastDueAmt2())
					.add(item.getAccount().getPastDueAmt3())
					.add(item.getAccount().getPastDueAmt4())
					.add(item.getAccount().getPastDueAmt5())
					.add(item.getAccount().getPastDueAmt6())
					.add(item.getAccount().getPastDueAmt7())
					.add(item.getAccount().getPastDueAmt8());
		}
		// C - 用当期最小还款额剩余部分（ctd due）
		else if (CalcBaseInd.C.equals(calcBaseInd)) {
			latePayAmt = item.getAccount().getCurrDueAmt();
		}
		
		// 滞纳金基准金额小于等于免收滞纳金阈值，直接返回
		if (latePayCharge.threshold != null && latePayAmt.compareTo(latePayCharge.threshold) <= 0) return item;
				
		// 如果滞纳金基准金额大于零，则调用交易生成和入账逻辑
		if (latePayAmt.compareTo(BigDecimal.ZERO) > 0) {
			// 生成一笔滞纳金收取交易，并入账
			CcsPostingTmp txnPost = generatorTransaction.generateLateChargeFee(item, latePayAmt, batchFacility.getBatchDate(), false);
			if (txnPost == null) return item;

			// 本年滞纳金收取金额
			item.getAccount().setYtdLateFeeAmt(txnPost.getPostAmt().add(item.getAccount().getYtdLateFeeAmt()));
			// 本年滞纳金收取笔数
			item.getAccount().setYtdLateFeeCnt(item.getAccount().getYtdLateFeeCnt() + 1);
		}
		
		return item;
	}
	
	public String addPaymentHist(PaymentStatus payment, String paymentHist){
		if(paymentHist == null){
			paymentHist = payment.toString();
		}else if(paymentHist.length() >=24){
			paymentHist = payment.toString() + paymentHist.substring(0,23);
		}else{
			paymentHist = payment.toString() + paymentHist;
		}
		
		return paymentHist;
	}
}
