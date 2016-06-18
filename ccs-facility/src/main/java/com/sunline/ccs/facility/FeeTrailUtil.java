package com.sunline.ccs.facility;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sunline.ccs.param.def.RateDef;
import com.sunline.ccs.param.def.enums.TierInd;

@Component
public class FeeTrailUtil {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	/**
	 * @see 方法名：computeFeeAmount 
	 * @see 描述：分段比例计算金额公用方法
	 * @see 创建日期：2015-6-24下午7:13:07
	 * @author ChengChun
	 *  
	 * @param tierInd
	 * @param chargeRates
	 * @param calcAmount
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BigDecimal getFeeAmount(TierInd tierInd, List<RateDef> chargeRates, BigDecimal calcAmount){

		// 检测输入参数是否为空
		if (tierInd ==null || chargeRates == null || calcAmount == null){
			throw new IllegalArgumentException("输入的参数为null，无法处理");
		}

		// 检测多级费率的最大金额是否按照从小到大排列
		BigDecimal preRateCeil = BigDecimal.ZERO;
		for (RateDef rateDef : chargeRates)
		{
			if (rateDef.rateCeil.compareTo(preRateCeil) < 0)
			{
				throw new IllegalArgumentException("参数中最大值列表并未按照从小到大排序");
			}
			preRateCeil = rateDef.rateCeil;	
		}
		
		// 开始计算
		BigDecimal feeAmount = BigDecimal.ZERO;
		
		// 根据分段计费类型进行不同的计算
		switch(tierInd){
		case F: 
			// 使用全部金额作为计算金额
			for (int i = 0 ; i < chargeRates.size(); i ++){
				if (logger.isDebugEnabled()) {
					logger.debug("多级费率:rateCeils-"+i+"["+chargeRates.get(i).rateCeil
							+"],rates-"+i+"["+chargeRates.get(i).rate
							+"]");
				}
				if (calcAmount.compareTo(chargeRates.get(i).rateCeil) <= 0){
					if (logger.isDebugEnabled()) {
						logger.debug("多级费率:rateCeils-"+i+"["+chargeRates.get(i).rateCeil
								+"],rates-"+i+"["+chargeRates.get(i).rate
								+"]");
					}
					feeAmount = calcAmount.multiply(chargeRates.get(i).rate);
					// 如果存在基准金额，则附加基准金额
					if (chargeRates.get(i).rateBase != null){
						feeAmount = feeAmount.add(chargeRates.get(i).rateBase);
					}
					return feeAmount;
				}
			}
			// 如果计算基础金额大于参数中配置的最大交易金额，则抛出异常
			throw new IllegalArgumentException("实际交易金额大于参数配置的最大交易金额");
		case T: 
			// 采用分段金额作为计算金额
			// 尚未计算金额
			BigDecimal current = calcAmount;

			for (int i = 0 ; (i < chargeRates.size()) && (current.signum() == 1) ; i ++)
			{
				BigDecimal minus = chargeRates.get(i).rateCeil.compareTo(current) > 0 ? current : chargeRates.get(i).rateCeil;
				feeAmount = feeAmount.add(minus.multiply(chargeRates.get(i).rate));
				current = current.subtract(minus);
				if (chargeRates.get(i).rateBase != null){
					feeAmount = feeAmount.add(chargeRates.get(i).rateBase);
				}
			}
			if (current.compareTo(BigDecimal.ZERO) > 0){
				throw new IllegalArgumentException("实际交易金额大于参数配置的最大交易金额");
			}
			return feeAmount;
		default: throw new IllegalArgumentException("无法处理的分段计费类型 tierInd:[" + tierInd.toString() + "]");
		}
	}
}
