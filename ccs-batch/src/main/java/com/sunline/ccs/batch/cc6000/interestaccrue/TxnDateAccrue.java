package com.sunline.ccs.batch.cc6000.interestaccrue;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sunline.ccs.batch.cc6000.S6000AcctInfo;
import com.sunline.ccs.facility.DateUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.enums.Indicator;


/**
 * @see 类名：TxnDateAccrue
 * @see 描述：交易日起息
 *
 * @see 创建日期：   2015-6-24下午6:56:30
 * @author liuqi
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class TxnDateAccrue extends Accrue {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public void accure(CcsPlan plan, BucketType type, PlanTemplate template, 
			int days, Indicator paidOut, Boolean isOverduePrin, Boolean needGraceIntAcruBackstage) {
		//TODO
	}
	/**
	 * 交易日补记息
	 * 实现简单的计息规则，在交易入账时，计息，计息金额入账金额（溢缴款不考虑）
	 * 计息天数使用交易日和入账日之间的间隔天数，计息不考虑罚息、复利，逻辑同入账日计息
	 * @param item
	 * @param plan
	 * @param template
	 * @param type
	 * @param txnPost
	 * @return
	 */
	public BigDecimal txnAccure(S6000AcctInfo item,CcsPlan plan,PlanTemplate template,BucketType type,CcsPostingTmp txnPost){
		if(template.intParameterBuckets.get(type) == null) return null;
		if (template.intParameterBuckets.get(type).intTableId == null) return null;
		
		// 补计利息天数,直接取交易日和入账的间隔天数
		int days = DateUtils.getIntervalDays(txnPost.getTxnDate(),txnPost.getPostDate());
		//金额 要考虑溢缴款
		BigDecimal bal = txnPost.getPostAmt();
		logger.debug("金融交易:计息金额=["+bal+"],计息天数=["+days+"]" +
				"交易日=["+txnPost.getTxnDate()+"],入账日=["+txnPost.getPostDate()+"]");
		//TODO
		// 判断该余额成份是否享受免息期
		Boolean b = template.intParameterBuckets.get(type).intWaive;
		b =( b==null?false:b);	
			
		if (b) {
			if(type == BucketType.Pricinpal){
				if(plan.getUsePlanRate() == Indicator.Y){
					//使用plan利率，不在区分是否全额还款，全计入非延时利息
					plan.setNodefbnpIntAcru(plan.getNodefbnpIntAcru().add(computeInterest(plan.getInterestRate(), bal, days)));
				}else{
					plan.setCtdDefbnpIntAcru(plan.getCtdDefbnpIntAcru().add(computeInterest(getInterestTable(type, template), days, bal)));
				}
				//不考虑罚息
			}else if(isInterest(type)){
				//不考虑复利
			}else{
				plan.setCtdDefbnpIntAcru(plan.getCtdDefbnpIntAcru().add(computeInterest(getInterestTable(type, template), days, bal)));
			}
		} else {
			// 不可免利息累积后,记入非延迟利息
			if(type == BucketType.Pricinpal ){
				if(plan.getUsePlanRate() == Indicator.Y){
					//本金产生的利息,都计入非延时利息
					plan.setNodefbnpIntAcru(plan.getNodefbnpIntAcru().add(computeInterest(plan.getInterestRate(), bal, days)));
				}else{
					plan.setNodefbnpIntAcru(plan.getNodefbnpIntAcru().add(computeInterest(getInterestTable(type, template), days, bal)));
				}
				//不考虑罚息
			}else if(isInterest(type) ){
				//不考虑复利
			}else{
				//其他余额成分都计入非延时利息
				plan.setNodefbnpIntAcru(plan.getNodefbnpIntAcru().add(computeInterest(getInterestTable(type, template), days, bal)));
			}
		}
		
		return null;
	}

}
