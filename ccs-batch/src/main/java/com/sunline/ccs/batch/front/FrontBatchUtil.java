package com.sunline.ccs.batch.front;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnWaiveLog;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnReject;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnWaiveLog;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.AdjState;
import com.sunline.pcm.param.def.FinancialOrg;
import com.sunline.pcm.param.def.Mulct;
import com.sunline.pcm.param.def.Split;
import com.sunline.pcm.service.sdk.AddressHelperFacility;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.CommandType;
import com.sunline.ppy.dictionary.enums.IdType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.OrderStatus;

@Component
public class FrontBatchUtil {
	
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	
	@Autowired
	private MicroCreditRescheduleUtils rescheduleUtils;
	
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	@Autowired
	private AddressHelperFacility addressFacility;
	
	@PersistenceContext
	private EntityManager em;
	
	/**
	 * 新建并保存订单
	 * @param acct
	 * @param cust
	 * @param loan
	 * @param loanUsage
	 * @param txnAmt
	 */
	public CcsOrder initOrder(CcsAcct acct, CcsCustomer cust, CcsLoan loan, LoanUsage loanUsage, BigDecimal txnAmt, FinancialOrg finanicalOrg){
		if(acct!=null)
			OrganizationContextHolder.setCurrentOrg(acct.getOrg());
		
		CcsOrder order = new CcsOrder();
		
		Date busiDate = new Date();
		
		setNullInfo(order);
		setAcctInfo(acct, order);
		setCustInfo(cust, order);
		setLoanInfo(loan, order);

		order.setCommandType(CommandType.BDB);
		// 回盘文件只返回成功或失败的订单, 故订单直接设置为处理中
		order.setOrderStatus(OrderStatus.W);
		order.setOnlineFlag(Indicator.N);
		order.setTxnType(AuthTransType.AgentCredit);
		order.setTxnAmt(txnAmt);
		order.setSuccessAmt(BigDecimal.ZERO);
		order.setFailureAmt(BigDecimal.ZERO);
		order.setCardType("0");
		order.setLoanUsage(loanUsage);// 用途
		order.setBusinessDate(batchStatusFacility.getSystemStatus().getBusinessDate());
		order.setSendTime(busiDate); //发送时间
		order.setSetupDate(busiDate); //创建日期
		order.setOrderTime(busiDate); //创建日期
		order.setOptDatetime(busiDate);//操作时间
		order.setJpaVersion(0);
		order.setFlag("00"); // 对私00 对公01
		order.setMatchInd(Indicator.N); // 批扣订单不对账
		order.setComparedInd(Indicator.N);
		order.setPriv1("T110E5"); // 私有域 固定值
		
		if(finanicalOrg == null){
			ProductCredit pc = unifiedParameterFacility.loadParameter(acct.getProductCd(), ProductCredit.class);
			finanicalOrg = unifiedParameterFacility.loadParameter(pc.financeOrgNo, FinancialOrg.class);
		}
		if(StringUtils.isBlank(order.getAcqId())) {
			order.setAcqId(finanicalOrg.acqAcceptorId);
		}
		switch(loanUsage){
			case N:
				order.setPurpose("正常扣款");
				break;
			case O:
				order.setPurpose("逾期扣款");
				break;
			case M:
				order.setPurpose("提前结清");
				break;
			case C:
				order.setFlag("01");
				order.setUsrName(finanicalOrg.financialOrgName);
				order.setCardNo(finanicalOrg.financialOrgPayNo);
				order.setCertType("06");
				order.setCertId(finanicalOrg.acqAcceptorId);
				order.setOpenBank(finanicalOrg.payBank);
				order.setSubBank(finanicalOrg.payBranchBank);
				order.setOpenBankId(finanicalOrg.payOpenBankId);
				order.setState(getCityNameFromCode(finanicalOrg.acctProvince)); // 省份?
				order.setCity(getCityNameFromCode(finanicalOrg.acctCity)); // 城市?
				order.setPurpose("理赔结清");
				break;
			case S:
				order.setPurpose("追偿代扣");
				break;
			case B:
				order.setChannelId(finanicalOrg.inputSource);
				order.setFlag("01");
				order.setCommandType(CommandType.SPA);
				order.setOnlineFlag(Indicator.N);
				order.setTxnType(AuthTransType.AgentDebit);
				order.setCurrency(finanicalOrg.currencyCd);
				order.setOpenBank(finanicalOrg.acctBank);
				order.setCardNo(finanicalOrg.financialOrgAcctNo);
				order.setUsrName(finanicalOrg.financialOrgName);
				order.setCertType("06");
				order.setCertId(finanicalOrg.acqAcceptorId);
				order.setSubBank(finanicalOrg.acctBranchBank);
				order.setState(getCityNameFromCode(finanicalOrg.acctProvince)); // 省份
				order.setCity(getCityNameFromCode(finanicalOrg.acctCity)); // 城市
				order.setPayChannelId("1");
				order.setPayBizCode("1");
				order.setPurpose("结算");
				order.setOpenBankId(finanicalOrg.acctOpenBankId);
				order.setBusinessDate(batchStatusFacility.getBatchDate());
				break;
			case P:
				order.setPurpose("PTP扣款");
				break;
			case D:
				order.setCommandType(CommandType.BDA);
				order.setGuarantyId(acct.getGuarantyId());
				order.setTxnType(AuthTransType.AgentDebit);
				order.setPurpose("溢缴款转出");
				break;
			default:
				throw new IllegalArgumentException("不支持的订单用途");
		}
		
		em.persist(order);
		
		return order;
	}

	private String getCityNameFromCode(String code) {
		String cityName = addressFacility.loadChineseAddress().get(code);
		return cityName;
	}

	private void setNullInfo(CcsOrder order) {
		order.setLogKv(null);
		order.setOrderBrief(null);
		order.setOrberFailTime(null);
		order.setMerId(null);
		order.setMerName(null);
		order.setProductType(null);
		order.setState(null); // 省份?
		order.setCity(null); // 城市?
		order.setStatus(null);
		order.setCode(null);
		order.setMessage(null);
		order.setOriOrderId(null);
		order.setOptDatetime(null);
	}

	private void setLoanInfo(CcsLoan loan, CcsOrder order) {
		if(loan != null){
			order.setGuarantyId(loan.getGuarantyId());
			order.setDueBillNo(loan.getDueBillNo());
			order.setLoanAmt(loan.getLoanInitPrin());
		}
	}

	private void setCustInfo(CcsCustomer cust, CcsOrder order) {
		if(cust != null){
			order.setCertType(idTypeTransfer(cust.getIdType()));
			order.setCertId(cust.getIdNo());
		}
	}

	private void setAcctInfo(CcsAcct acct, CcsOrder order) {
		if(acct != null){
			order.setOrg(acct.getOrg());
			order.setAcctNbr(acct.getAcctNbr());
			order.setAcctType(acct.getAcctType());
			order.setCurrency(acct.getCurrency());
			order.setOpenBankId(acct.getDdBankBranch());
			order.setOpenBank(acct.getDdBankName());
			order.setCardNo(acct.getDdBankAcctNbr());
			order.setUsrName(acct.getDdBankAcctName());
			order.setSubBank(acct.getOwningBranch());
			order.setChannelId(acct.getCustSource()); // 渠道
			order.setContrNbr(acct.getContrNbr());
			order.setAcqId(acct.getAcqId());
			order.setState(acct.getDdBankProvince()); // 省份
			order.setCity(acct.getDdBankCity()); // 城市
		}
	}

	/**
	 * nova - 马上   证件类型转换
	 * @param idType
	 * @return
	 */
	private String idTypeTransfer(IdType idType) {
		switch(idType){
			case I:
			case S:
			case P:
				return idType.getIdTypeVal();
			case R:
				return "04";
			case H:
				return "05";
			default:
				return "06";
		}
	}
	
	/**
	 * 查询账户是否有处理中订单
	 * @param acctNbr
	 * @param acctType
	 * @return
	 */
	public int getWOrderCount(Long acctNbr, AccountType acctType){
		QCcsOrder o = QCcsOrder.ccsOrder;
		return new JPAQuery(em).from(o)
				.where(o.acctNbr.eq(acctNbr).and(o.acctType.eq(acctType))
					.and(o.orderStatus.eq(OrderStatus.W))
					.and(o.loanUsage.notIn(LoanUsage.A, LoanUsage.L)))
				.list(o.orderId).size();
	}
	
	/**
	 * 查询账户当日成功的豁免交易
	 * @param loanId
	 * @return
	 */
	public List<CcsTxnWaiveLog> getTxnWaiveLog(Long loanId){
		QCcsTxnWaiveLog qCcsTxnWaiveLog = QCcsTxnWaiveLog.ccsTxnWaiveLog;
		return new JPAQuery(em).from(qCcsTxnWaiveLog)
				.where(qCcsTxnWaiveLog.loanId.eq(loanId).and(qCcsTxnWaiveLog.adjState.eq(AdjState.A)))
				.list(qCcsTxnWaiveLog);
	}
	
	/**
	 * 查询账户当日成功的豁免金额
	 * @param loanId
	 * @return
	 */
	public BigDecimal getTxnWaiveAmt(Long loanId){
		
		List<CcsTxnWaiveLog> txnWaiveLogList = getTxnWaiveLog(loanId);
		BigDecimal amt = BigDecimal.ZERO;
		if (txnWaiveLogList != null) {
			for (CcsTxnWaiveLog txnWaiveLog : txnWaiveLogList) {
				amt = amt.add(txnWaiveLog.getTxnAmt());
			}
		}
		return amt;
	}
	
	/**
	 * 按账户查找业务日期当日成功订单
	 * @param acctNbr
	 * @param acctType
	 * @param loanUsage
	 * @param isOnline
	 * @param orderStatuses
	 * @return
	 */
	public int getOrderCount(Long acctNbr, AccountType acctType, Indicator isOnline,Date endBusiDate, LoanUsage... loanUsages){
		QCcsOrder o = QCcsOrder.ccsOrder;
		JPAQuery query = new JPAQuery(em).from(o);
		// 由于订单更新时间为时间戳, 在与日期比较时会有问题, 故改为在业务日期当天的时间段内
		// 业务日期0点
		Date busiStartDate = DateUtils.truncate(batchStatusFacility.getSystemStatus().getBusinessDate(), Calendar.DATE);
		// 业务日期第二天0点, 在此范围内的为业务日期当日更新的订单
		Date busiEndDate = DateUtils.addDays(busiStartDate, 1);
		BooleanExpression booleanExpression = o.acctNbr.eq(acctNbr).and(o.acctType.eq(acctType))
				.and(o.optDatetime.goe(busiStartDate))
				.and(o.optDatetime.lt(busiEndDate))
				.and(o.orderStatus.eq(OrderStatus.S));
		if(loanUsages.length>0)
			booleanExpression = booleanExpression.and(o.loanUsage.in(loanUsages));
		if(isOnline!=null)
			booleanExpression = booleanExpression.and(o.onlineFlag.eq(isOnline));
		if(endBusiDate!=null)
			booleanExpression = booleanExpression.and(o.businessDate.before(endBusiDate));
		
		return query.where(booleanExpression).list(o.orderId).size();
	}
	
	/**
	 * 按照拆分规则参数拆分金额
	 * @param needSplitAmt
	 * @param split
	 * @return
	 */
	public List<BigDecimal> splitPayment(BigDecimal needSplitAmt, Split split){
		List<BigDecimal> amts = new ArrayList<BigDecimal>();
		BigDecimal txnAmt = needSplitAmt;
		BigDecimal splitAmt = BigDecimal.ZERO;
		switch(split.splitMethod){
			case A:
				while(needSplitAmt.compareTo(split.splitMinAMT) >= 0
						&& (needSplitAmt.multiply(split.splitRate)).compareTo(split.splitMinAMT) >= 0
						&& (needSplitAmt.subtract(needSplitAmt.multiply(split.splitRate))).compareTo(split.splitMinAMT) >= 0){
					// 拆分金额
					amts.add(needSplitAmt.multiply(split.splitRate).setScale(2, RoundingMode.HALF_UP));
					// 拆分金额累计
					splitAmt = splitAmt.add(needSplitAmt.multiply(split.splitRate).setScale(2, RoundingMode.HALF_UP));
					// 拆分后剩余金额
					needSplitAmt = needSplitAmt.subtract(needSplitAmt.multiply(split.splitRate));
				}
				// 最后一份=总金额-拆分累计金额
				amts.add(txnAmt.subtract(splitAmt));
				break;
			case B:
				while(needSplitAmt.compareTo(split.splitAMT) > 0){
					// 拆分金额
					amts.add(split.splitAMT);
					needSplitAmt = needSplitAmt.subtract(split.splitAMT);
				}
				amts.add(needSplitAmt);
				break;
			default : throw new IllegalArgumentException("未知的拆分方式");
		}
		
		switch(split.splitAmtSort){
			case A:
				Collections.sort(amts);
				break;
			case D:
				Collections.sort(amts, Collections.reverseOrder());
				break;
			default : throw new IllegalArgumentException("未知的拆分后金额排序");
		}
		return amts;
	}
	
	/**
	 * 获取dpd容差
	 * @param loan
	 * @return
	 */
	public BigDecimal getDpdToleLmt(CcsLoan loan){
		OrganizationContextHolder.setCurrentOrg(loan.getOrg());
		// DPD容差
		LoanFeeDef loanFeeDef = rescheduleUtils.getLoanFeeDef(loan.getLoanCode(), loan.getLoanInitTerm(), 
				loan.getLoanInitPrin(),loan.getLoanFeeDefId());
//		Mulct mulct = unifiedParameterFacility.loadParameter(loanFeeDef.mulctTableId, Mulct.class);
		return loanFeeDef.dpdToleLmt==null ? BigDecimal.ZERO : loanFeeDef.dpdToleLmt;
	}
	
	/**
	 * 计算当日入账交易的数量
	 * @param acctNbr
	 * @param acctType
	 * @return int
	 */
	public int getExistPostingCount(Long acctNbr,AccountType acctType){
		QCcsPostingTmp q = QCcsPostingTmp.ccsPostingTmp;
		return new JPAQuery(em).from(q)
				.where(q.acctNbr.eq(acctNbr)
						.and(q.acctType.eq(acctType)))
				.list(q.txnSeq).size();
	}
	
	/**
	 * 计算挂账交易的数量
	 * @param acctNbr
	 * @param acctType
	 * @return int
	 */
	public int getTxnRejectCount(Long acctNbr,AccountType acctType){
		QCcsTxnReject q = QCcsTxnReject.ccsTxnReject;
		return new JPAQuery(em).from(q)
				.where(q.acctNbr.eq(acctNbr)
						.and(q.acctType.eq(acctType)))
				.list(q.txnSeq).size();
	}
	
	/**
	 * 查询当天是否有退货成功的交易
	 */
	public boolean isExistRefund(Long acctNbr,AccountType acctType,Date validDate){
		QCcsLoanReg q = QCcsLoanReg.ccsLoanReg;
		return new JPAQuery(em).from(q)
				.where(q.acctNbr.eq(acctNbr).and(q.acctType.eq(acctType))
						.and(q.loanAction.eq(LoanAction.T).and(q.loanRegStatus.eq(LoanRegStatus.A)))
						.and(q.validDate.eq(validDate))).count()>0?true:false;
				
	}
	
	/**
	 * 获取优惠券扣款订单金额
	 */
	public CcsOrder getCouponAmout(Long acctNbr,AccountType acctType){
		QCcsOrder q = QCcsOrder.ccsOrder;
		// 优惠券只存在一张，同时考虑查询效率问题
		return new JPAQuery(em).from(q)
				.where(q.acctNbr.eq(acctNbr).and(q.acctType.eq(acctType))
						.and(q.loanUsage.eq(LoanUsage.Q))).singleResult(q);
	}
}
