package com.sunline.ccs.batch.front;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.pcm.param.def.FinancialOrg;
import com.sunline.pcm.param.def.Split;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.OrderStatus;

/**
 * 追偿拆分代扣
 * @author zhangqiang
 *
 */
@Component
public class P5000Subrogation implements ItemProcessor<SFrontInfo, SFrontInfo> {
	
	private static final Logger logger = LoggerFactory.getLogger(P5000Subrogation.class);
	
	@Autowired
	private FrontBatchUtil frontBatchUtil;
	
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	public SFrontInfo process(SFrontInfo info) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("追偿代扣：Org["+info.getAcct().getOrg()
					+"],AcctType["+info.getAcct().getAcctType()
					+"],AcctNo["+info.getAcct().getAcctNbr()
					+"],DueBillNo["+info.getLoan().getDueBillNo()
					+"]");
		}
		
		OrganizationContextHolder.setCurrentOrg(info.getAcct().getOrg());
		// 生成批量追偿原订单
		CcsOrder origOrder = genOrigOrder(info.getrOrder());
		// 获取追偿拆分规则
		ProductCredit pc = unifiedParameterFacility.loadParameter(info.getAcct().getProductCd(), ProductCredit.class);
		FinancialOrg finanicalOrg = unifiedParameterFacility.loadParameter(pc.financeOrgNo, FinancialOrg.class);
		Split split = unifiedParameterFacility.loadParameter(finanicalOrg.splitTableId, Split.class);
		// 待拆分金额  = 联机追偿失败金额
		BigDecimal needSplitAmt = info.getrOrder().getTxnAmt();
		
		// 减去豁免金额
		needSplitAmt = needSplitAmt.subtract(frontBatchUtil.getTxnWaiveAmt(info.getLoan().getLoanId()));
		
		//*如果扣款银行为光大银行
		if("0303".equals(origOrder.getOpenBankId())){
			BigDecimal splitAmt1 = needSplitAmt.multiply(new BigDecimal(0.8)).setScale(2,BigDecimal.ROUND_HALF_UP);
			CcsOrder splitOrder = frontBatchUtil.initOrder(info.getAcct(), info.getCust(), info.getLoan(), LoanUsage.S, splitAmt1, null);
			splitOrder.setOriOrderId(origOrder.getOrderId());
			CcsOrder splitOrder2 = frontBatchUtil.initOrder(info.getAcct(), info.getCust(), info.getLoan(), LoanUsage.S, needSplitAmt.subtract(splitAmt1), null);
			splitOrder2.setOriOrderId(origOrder.getOrderId());
		}else{
			// 拆分后金额
			List<BigDecimal> splitAmts = frontBatchUtil.splitPayment(needSplitAmt, split);
			
			for(BigDecimal splitAmt : splitAmts){
				CcsOrder splitOrder = frontBatchUtil.initOrder(info.getAcct(), info.getCust(), info.getLoan(), LoanUsage.S, splitAmt, null);
				splitOrder.setOriOrderId(origOrder.getOrderId());
			}
		}
		// 将联机追偿的状态改为已失效
		info.getrOrder().setOrderStatus(OrderStatus.V);
		
		return info;
	}

	private CcsOrder genOrigOrder(CcsOrder rOrder) {
		CcsOrder origOrder = new CcsOrder();
		origOrder.updateFromMap(rOrder.convertToMap());
		
		origOrder.setOrderId(null);
		origOrder.setOriOrderId(rOrder.getOrderId());
		origOrder.setOrderStatus(OrderStatus.G);
		origOrder.setOnlineFlag(Indicator.N);
		origOrder.setMatchInd(Indicator.N);
		origOrder.setSuccessAmt(BigDecimal.ZERO);
		origOrder.setFailureAmt(BigDecimal.ZERO);
		
		em.persist(origOrder);
		
		return origOrder;
	}


}
