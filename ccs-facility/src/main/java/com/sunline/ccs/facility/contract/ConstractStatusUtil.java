package com.sunline.ccs.facility.contract;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;






import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.facility.order.OrderFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsOrderHst;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.ContraBizSituation;
import com.sunline.ccs.param.def.enums.ContraStatus;
import com.sunline.ccs.param.def.enums.DelqTolInd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.MsPayfrontError;

/*
 * 获取合同相关信息
 */
@Service
public class ConstractStatusUtil {
	@Autowired
	UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	@Autowired
	UnifiedParameterFacility unifiedParameterFacility;
	@Autowired
	private OrderFacility orderFacility;
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	public ContraStatus getConstractStatus(CcsAcct acct) {

		ContraStatus contraStatus;

		if (StringUtils.isNotEmpty(acct.getBlockCode())
				&& acct.getBlockCode().toUpperCase().indexOf("T") > -1) {
			contraStatus = ContraStatus.F;
		} else if (null != acct.getAcctExpireDate()
				&& acct.getAcctExpireDate().compareTo(
						unifiedParamFacilityProvide.BusinessDate()) < 0) {
			contraStatus = ContraStatus.X;
		} else {
			contraStatus = ContraStatus.N;
		}

		return contraStatus;
	}

	/*
	 * 判断合同是否处于逾期状态 return: y-逾期 n-未逾期
	 */
	public Indicator isContractOverDue(CcsAcct acct, CcsAcctO acctO) {
		Date bizDate = unifiedParamFacilityProvide.BusinessDate();
		ProductCredit productCredit = unifiedParameterFacility.loadParameter(
				acct.getProductCd(), ProductCredit.class);
		AccountAttribute accountAttribute = unifiedParameterFacility
				.loadParameter(productCredit.accountAttributeId,
						AccountAttribute.class);

		// 最小还款额拖欠容忍金额
		BigDecimal delqAmt = BigDecimal.ZERO;
		if (accountAttribute.delqTolInd == DelqTolInd.A) {
			delqAmt = accountAttribute.delqTol;
		}
		// 前8期最小还款额之和
		BigDecimal pastDueAmtSum = BigDecimal.ZERO;
		pastDueAmtSum = pastDueAmtSum.add(acct.getPastDueAmt1()).add(acct.getPastDueAmt2())
				.add(acct.getPastDueAmt3()).add(acct.getPastDueAmt4())
				.add(acct.getPastDueAmt5()).add(acct.getPastDueAmt6())
				.add(acct.getPastDueAmt7()).add(acct.getPastDueAmt8());

		// 前8期最小还款额之和 > 未匹配贷方金额+拖欠容忍金额 则逾期
		if (pastDueAmtSum.compareTo(acctO.getMemoCr().add(delqAmt)) > 0) {
			return Indicator.Y;
		}
		// 前8期若未逾期则判断当期是否逾期
//		if (acct.getGraceDate().compareTo(bizDate) > 0) {
//			return Indicator.N;
//		} 
		//使用还款日判断逾期状态，不应该让客户感知宽限期---20151109
		if (acct.getPmtDueDate()==null||acct.getPmtDueDate().compareTo(bizDate) >= 0) {
			return Indicator.N;
		}
		else if (acct.getCurrDueAmt().add(pastDueAmtSum)
				.compareTo(acctO.getMemoCr().add(delqAmt)) > 0) {
			// 若已过宽限期 则判断当期最小还款额是否未还，若未还则逾期
			return Indicator.Y;
		}
		// else if()
		return Indicator.N;
	}

	/*
	 * 返回合同所处业务场景 提供前端显示合同状态 
	 * "A|合同贷款结清",
	 * "B|合同逾期", 
	 * "C|已放款(未逾期) 当前有应还款额",
	 * "D|已放款（未结清，已跑批生成还款计划），但当前无应还款额", 
	 * "E|放款成功但未跑批", 
	 * "F|放款联机交易处理中", 
	 * "G|开户但未放款"
	 * "H|开户未放款且合同已过期"
	 * "I|退货"
	 * @param CcsAcct , CcsAcctO , CcsLoan 未放款或放款未跑批时传null CcsLoanReg 未放款
	 * 或放款成功已跑批时传空
	 */
	public ContraBizSituation getContraBizSituation(CcsAcct acct,
			CcsAcctO accto, List<CcsLoan> loanlist, List<CcsLoanReg> loanReglist) {

		ContraBizSituation bizSituation = ContraBizSituation.G;
		
		Indicator  isOverDueFlag = isContractOverDue(acct, accto);
		
		List<CcsOrder> orderlist = orderFacility.getCcsOrderList(acct.getContrNbr(), null, null);
		List<CcsOrderHst> orderHstlist = orderFacility.getCcsOrderHstList(acct.getContrNbr(), null, null);
		
		//判断是否为退货的交易
		for (CcsOrder ccsOrder : orderlist) {
			if ("TFCRefund".equals(ccsOrder.getServiceId()) 
					&& MsPayfrontError.S_0.getRespCode().equals(ccsOrder.getResponseCode())) {
				return ContraBizSituation.I;
			}
		}
		
		for (CcsOrderHst ccsOrderHst : orderHstlist) {
			if ("TFCRefund".equals(ccsOrderHst.getServiceId())
					&& MsPayfrontError.S_0.getRespCode().equals(ccsOrderHst.getResponseCode())) {
				return ContraBizSituation.I;
			}
		}
		
		/*
		 *  还清日期非空	                                                   合同贷款结清
			合同逾期标志	                                                   合同逾期
			合同全部应还款额>0	                                     已放款(未结清未逾期)当前有应还款额"
			loan已生成单合同全部应还款额<=0	               已放款（已跑批），但当前无应还款额
			loanReg已生成且loanRegStatus为 N或C	        放款成功但未跑批
			loanReg已生成且loanRegStatus为 A或S	        放款联机交易处理中
			未生成loan 和 状态为（N/C/A/S）loanReg	开户但未放款
		 */
		if (loanlist.size() == 0
		&& loanReglist.size() > 0){
			for(CcsLoanReg loanreg : loanReglist){
				if(!loanreg.getLoanAction().equals(LoanAction.O )){
					if(loanreg.getLoanRegStatus().equals(LoanRegStatus.A) || 
					   loanreg.getLoanRegStatus().equals(LoanRegStatus.S)){
						//没有loan 有loanreg 且状态是成功（只要有一笔成功的loanReg就算开户放过款了）
						bizSituation = ContraBizSituation.E;
						break;
					}
					else if( (loanreg.getLoanRegStatus() == LoanRegStatus.C 
							   || loanreg.getLoanRegStatus() == LoanRegStatus.N)){
						//没有Loan 有loanreg且状态是处理中，继续遍历看有没有成功的
						bizSituation = ContraBizSituation.F;
					}
				}
				if (null != acct.getAcctExpireDate()
						&& acct.getAcctExpireDate().compareTo(
								unifiedParamFacilityProvide.BusinessDate()) < 0){
				//	"H|开户未放款且合同已过期"
					bizSituation = ContraBizSituation.H;
				}else {
					//没有loan 也没有loanreg(提前还款预约记录不算)
					bizSituation = ContraBizSituation.G;
				}
			}
		}
		else if(loanlist.size() == 0
			 && loanReglist.size() == 0){
			if (null != acct.getAcctExpireDate()
					&& acct.getAcctExpireDate().compareTo(
							unifiedParamFacilityProvide.BusinessDate()) < 0) {
//				"H|开户未放款且合同已过期"
				bizSituation = ContraBizSituation.H;
			} else {
				bizSituation = ContraBizSituation.G;
			}
		}
		else if(loanlist.size() >0 ){
			//已经生成了loan
			Indicator isPaidOutInd = Indicator.Y;//是否已结清标志
			for(CcsLoan loan : loanlist){
				if(loan.getPaidOutDate() == null){
					//只要有一笔未结清的loan则不是已结清
					isPaidOutInd = Indicator.N;
					break;
				}
			}
			
			if(isPaidOutInd == Indicator.Y){
				bizSituation = ContraBizSituation.A;
			} else if (isOverDueFlag == Indicator.Y) {
				bizSituation = ContraBizSituation.B;
			} else if (acct.getQualGraceBal().compareTo(acct.getCtdRepayAmt()) > 0) {
				//暂先用全部最小还款额判断 若最小还款额不是全部应还款额需修改
				bizSituation = ContraBizSituation.C;
			} else if (acct.getQualGraceBal().compareTo(acct.getCtdRepayAmt()) <= 0) {
				//暂先用全部最小还款额判断 若最小还款额不是全部应还款额需修改
				bizSituation = ContraBizSituation.D;
			}		
		}
		
		logger.info("合同业务场景相关，账户应还款额[{}],是否逾期[{}],还清日期[{}],loan是否为空[{}]"+
					"loanreg是否为空[{}],loanRegStatus[{}]",
					acct.getQualGraceBal(),isOverDueFlag,
					loanlist.size()==0?null:loanlist.get(0).getPaidOutDate(),
					loanlist.size()==0?Indicator.Y:Indicator.N,
					loanReglist.size()==0?Indicator.Y:Indicator.N,
					loanReglist.size()==0?null:loanReglist.get(0).getLoanRegStatus());

		return bizSituation;

	}

	/*
	 * 计算当期待还款金额
	 */
	// public BigDecimal getContractCurrTermAmt(CcsAcct acct,CcsAcctO accto){
	// BigDecimal currTermAmt= BigDecimal.ZERO;
	//
	// if(isContractOverDue(acct, accto)== Indicator.Y){
	// currTermAmt = acct.getloan
	// }
	//
	//
	// return currTermAmt;
	// }

}
