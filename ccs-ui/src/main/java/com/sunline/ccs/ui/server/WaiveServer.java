package com.sunline.ccs.ui.server;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mysema.query.jpa.impl.JPAQuery;

import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.infrastructure.server.repos.RCcsTxnWaiveLog;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnWaiveLog;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnWaiveLog;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.enums.AdjState;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ppy.dictionary.enums.PlanType;

@Controller
@RequestMapping(value = "/waiveServer")
public class WaiveServer {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private GlobalManagementService globalManagementService;
	
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	
	@Autowired
	private RCcsTxnWaiveLog rCcsTxnWaiveLog;
	
	private QCcsOrder qCcsOrder = QCcsOrder.ccsOrder;
	private QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
	private QCcsPlan qCcsPlan = QCcsPlan.ccsPlan;
	private QCcsTxnWaiveLog qCcsTxnWaiveLog = QCcsTxnWaiveLog.ccsTxnWaiveLog;
	
	/**
	* @Description 根据合同号获取合同信息
	* @author 鹏宇
	* @date 2015-11-4 上午10:25:13
	 */
	@ResponseBody()
	@RequestMapping(value = "/getLoanList" , method={RequestMethod.POST})
	public List<CcsLoan> getLoanList(@RequestBody String contrNbr)throws FlatException{
		String org = OrganizationContextHolder.getCurrentOrg();               //机构号
		List<CcsLoan> loanList = new ArrayList<CcsLoan>();
		JPAQuery query = new JPAQuery(em);
		logger.info("合同号["+contrNbr+"]查询分期计划列表");
		loanList = query.from(qCcsLoan).where(qCcsLoan.contrNbr.eq(contrNbr).and(qCcsLoan.org.eq(org))).list(qCcsLoan);
		if(loanList == null || loanList.size() == 0){
			throw new FlatException("查询不到相关合同信息");
		}
		return loanList;
	}
	
	/**
	* @Description 根据账户号和账户类型获取合同信息
	* @author 鹏宇
	* @date 2015-11-13 上午11:07:29
	 */
	@ResponseBody()
	@RequestMapping(value = "/getLoanListByAcctNbr" , method={RequestMethod.POST})
	public List<CcsLoan> getLoanListByAcctNbr(@RequestBody String  acctNbr, @RequestBody String acctType)throws FlatException{
		String org = OrganizationContextHolder.getCurrentOrg();               //机构号
		List<CcsLoan> loanList = new ArrayList<CcsLoan>();
		JPAQuery query5 = new JPAQuery(em);
		AccountType accountType = AccountType.valueOf(acctType);
		logger.info("账户号["+acctNbr+"],账户类型"+acctType+"查询分期计划列表");
		loanList = query5.from(qCcsLoan).where(qCcsLoan.acctNbr.eq(Long.parseLong(acctNbr)).and(qCcsLoan.acctType.eq(accountType)).and(qCcsLoan.org.eq(org))).list(qCcsLoan);
		if(loanList == null || loanList.size() == 0){
			throw new FlatException("查询不到相关合同信息");
		}
		return loanList;
	}
	
	@ResponseBody()
	@RequestMapping(value = "/getPlanList" , method= {RequestMethod.POST})
	public List<CcsPlan> getPlanList(@RequestBody String acctNbr ,  @RequestBody String acctType)throws FlatException{
		AccountType accountType = AccountType.valueOf(acctType);
		String org = OrganizationContextHolder.getCurrentOrg();               //机构号
		List<CcsPlan> list = new ArrayList<CcsPlan>();
		JPAQuery query = new JPAQuery(em);
		list = query.from(qCcsPlan).where(qCcsPlan.acctNbr.eq(Long.parseLong(acctNbr)).and(qCcsPlan.org.eq(org))
				.and(qCcsPlan.acctType.eq(accountType))
				).list(qCcsPlan);
		if(list == null || list.size() == 0){
			throw new FlatException("查询不到该合同相关的分期计划信息");
		}
		return list;
	}
	
	@ResponseBody()
	@RequestMapping(value="/getExempt" , method={RequestMethod.POST})
	public List<CcsTxnWaiveLog> getgetExempt(@RequestBody String AcctNbr)throws FlatException{
		String org = OrganizationContextHolder.getCurrentOrg();               //机构号
		List<CcsTxnWaiveLog> list = new ArrayList<CcsTxnWaiveLog>();
		JPAQuery query = new JPAQuery(em);
		logger.info("账户号["+AcctNbr+"]查询CcsTxnWaiveLog");
		list = query.from(qCcsTxnWaiveLog).where(qCcsTxnWaiveLog.acctNbr.eq(Long.parseLong(AcctNbr)).and(qCcsTxnWaiveLog.org.eq(org))).list(qCcsTxnWaiveLog);
//		if(list == null || list.size() == 0){
//			throw new FlatException("查询不到该合同的相关豁免信息");
//		}
		return list;
	}
	
	/**
	* @Description 得到批量处理中的
	* @author 鹏宇
	* @date 2015-12-3 下午4:30:06
	 */
	public int getWOrderCount(Long acctNbr, AccountType acctType){
		return new JPAQuery(em).from(qCcsOrder)
				.where(qCcsOrder.acctNbr.eq(acctNbr).and(qCcsOrder.acctType.eq(acctType))
					.and(qCcsOrder.orderStatus.eq(OrderStatus.W))
					.and(qCcsOrder.loanUsage.notIn(LoanUsage.A, LoanUsage.L)))
				.list(qCcsOrder.orderId).size();
	}
	
	@Transactional
	@ResponseBody()
	@RequestMapping(value="/exemptWaive" , method={RequestMethod.POST})
	public String exemptWaive(@RequestBody Map planMap , @RequestBody String loanId)throws FlatException{
		Long planId = Long.parseLong((String) planMap.get("planId"));
		String remark = (String) planMap.get("remark");
		BucketType bucketType = BucketType.valueOf((String) planMap.get("bucketType"));
		BigDecimal  inputMoney = new BigDecimal(planMap.get("inputMoney").toString());       //输入金额
		BigDecimal zero = BigDecimal.ZERO;             //0值
		
		BigDecimal exemptMoney = BigDecimal.ZERO;               //已经豁免成功的钱
		
		SysTxnCd txnCd = null;              //交易码
		
		if(! (inputMoney.compareTo(zero) == 1)){
			return "输入的金额不合法";
		}
		
		JPAQuery query = new JPAQuery(em);
		CcsLoan ccsLoan = query.from(qCcsLoan).where(qCcsLoan.loanId.eq(Long.parseLong(loanId))).list(qCcsLoan).get(0);     //合同
		
		JPAQuery query1 = new JPAQuery(em);
		CcsPlan ccsPlan = query1.from(qCcsPlan).where(qCcsPlan.planId.eq(planId)).list(qCcsPlan).get(0);     //计划
		
		JPAQuery query2 = new JPAQuery(em);
		List<CcsOrder> ccsOrderList = query2.from(qCcsOrder).where(qCcsOrder.acctNbr.eq(ccsLoan.getAcctNbr())
				.and(qCcsOrder.acctType.eq(ccsLoan.getAcctType()))
				.and(qCcsOrder.contrNbr.eq(ccsLoan.getContrNbr()))
				.and(qCcsOrder.orderStatus.eq(OrderStatus.W))
				.and(qCcsOrder.txnType.eq(AuthTransType.TransferCredit))                 //判断是否存在在途的扣款
				).list(qCcsOrder);
		
		if(ccsOrderList != null && ccsOrderList.size() > 0){
					return "存在在途扣款不能豁免，操作失败";
		}
		
		int num = this.getWOrderCount(ccsLoan.getAcctNbr(), ccsLoan.getAcctType());           //FrontBatchUtil    判断是否有处理中的订单
		if(num > 0){
			return "账户存在处理中的订单不能豁免，操作失败";
		}
		
		//查到这个合同已经豁免的 交易类型的金额的所有记录
		JPAQuery query6 = new JPAQuery(em);
		List<CcsTxnWaiveLog> moneyList = query6.from(qCcsTxnWaiveLog).where(qCcsTxnWaiveLog.loanId.eq(Long.parseLong(loanId))
				.and(qCcsTxnWaiveLog.bucketType.eq(bucketType))
				.and(qCcsTxnWaiveLog.adjState.eq(AdjState.A))
				).list(qCcsTxnWaiveLog);
		if(moneyList != null && moneyList.size() > 0){
			for(CcsTxnWaiveLog c : moneyList){
				exemptMoney = exemptMoney.add(c.getTxnAmt());
			}
		}
		
		if(planMap.get("term") == null){
			JPAQuery query3 = new JPAQuery(em);
			List<CcsTxnWaiveLog> ccsTxnWaiveLogList = query3.from(qCcsTxnWaiveLog).where(qCcsTxnWaiveLog.loanId.eq(Long.parseLong(loanId))
					.and(qCcsTxnWaiveLog.bucketType.eq(bucketType))
					.and(qCcsTxnWaiveLog.adjState.eq(AdjState.W))
					).list(qCcsTxnWaiveLog);
			if(ccsTxnWaiveLogList.size() > 0){
				return "合同存在待审批的豁免申请记录，操作失败";
			}
		}else{
			Integer term = Integer.parseInt((String) planMap.get("term"));
			JPAQuery query4 = new JPAQuery(em);
			List<CcsTxnWaiveLog> ccsTxnWaiveLogList = query4.from(qCcsTxnWaiveLog).where(qCcsTxnWaiveLog.loanId.eq(Long.parseLong(loanId))
					.and(qCcsTxnWaiveLog.bucketType.eq(bucketType))
					.and(qCcsTxnWaiveLog.term.eq(term))
					.and(qCcsTxnWaiveLog.adjState.eq(AdjState.W))
					)
					.list(qCcsTxnWaiveLog);
			if(ccsTxnWaiveLogList.size() > 0){
				return "合同存在待审批的豁免申请记录，操作失败";
		}
		}
		
//		手工豁免不判断本金
//		if(ccsPlan.getPastPrincipal().compareTo(zero) == 1 || ccsPlan.getCtdPrincipal().compareTo(zero) == 1){
//			return "存在未还本金,不能豁免";
//		}
		
		//先取planType   再根据选的哪个豁免  取交易码
		// J  L  Q  P
		PlanType planType = PlanType.valueOf(ccsPlan.getPlanType().toString());
		
		if(bucketType.equals(BucketType.CardFee)){       //年费
			BigDecimal cardFee = ccsPlan.getPastCardFee().add(ccsPlan.getCtdCardFee());
			if(inputMoney.add(exemptMoney).compareTo(cardFee) == 1){
				return "冲减金额不能大于余额成分金额";
			}
			switch(planType){
			case J:
			case L:
			case P:
			case Q:  txnCd = SysTxnCd.C02;  break;           //年费贷调
			default:
			}
		}
		
		if(bucketType.equals(BucketType.SVCFee)){
			BigDecimal svcfee = ccsPlan.getPastSvcFee().add(ccsPlan.getCtdSvcFee());
			if(inputMoney.add(exemptMoney).compareTo(svcfee) == 1){
				return "冲减金额不能大于余额成分金额";
			}
			switch(planType){
			case J:
			case L:
			case P:
			case Q:   txnCd = SysTxnCd.S78;  break;             //服务费贷调
			default:
			}
		}
		
		if(bucketType.equals(BucketType.LatePaymentCharge)){
			BigDecimal late = ccsPlan.getPastLateFee().add(ccsPlan.getCtdLateFee());
			if(inputMoney.add(exemptMoney).compareTo(late) == 1){
				return "冲减金额不能大于余额成分金额";
			}
			switch(planType){
			case J:
			case L:
			case P:
			case Q:  txnCd = SysTxnCd.S10;  break;             //滞纳金贷调
			default:
			}
		}
		
		if(bucketType.equals(BucketType.TXNFee)){
			BigDecimal txnfee = ccsPlan.getPastTxnFee().add(ccsPlan.getCtdTxnFee());
			if(inputMoney.add(exemptMoney).compareTo(txnfee) == 1){
				return "冲减金额不能大于余额成分金额";
			}
			switch(planType){
			case J:
			case L:
			case P:
			case Q: txnCd = SysTxnCd.C01; break;            //交易费贷调
			default:
			}
		}
		
		if(bucketType.equals(BucketType.Interest)){
			BigDecimal interest = ccsPlan.getPastInterest().add(ccsPlan.getCtdInterest());
			if(inputMoney.add(exemptMoney).compareTo(interest) == 1){
				return "冲减金额不能大于余额成分金额";
			}
			switch(planType){
			case J:
			case L:
			case P:
			case Q:  txnCd = SysTxnCd.C03;  break;                      
			default:
			}
		}
		
		if(bucketType.equals(BucketType.LifeInsuFee)){
			BigDecimal lifefee = ccsPlan.getPastLifeInsuAmt().add(ccsPlan.getCtdLifeInsuAmt());
			if(inputMoney.add(exemptMoney).compareTo(lifefee) == 1){
				return "冲减金额不能大于余额成分金额";
			}
			switch(planType){
			case J:
			case L:
			case P:
			case Q: txnCd = SysTxnCd.S76; break;                //寿险计划包费贷调
			default:
			}
		}
		
		if(bucketType.equals(BucketType.Mulct)){
			BigDecimal mulct = ccsPlan.getPastMulctAmt().add(ccsPlan.getCtdMulctAmt());
			if(inputMoney.add(exemptMoney).compareTo(mulct) == 1){
				return "冲减金额不能大于余额成分金额";
			}
			switch(planType){
			case J:
			case L:
			case P:
			case Q: txnCd = SysTxnCd.S74; break;              //等额本息罚金贷调
			default:
			}
		}
		
		if(bucketType.equals(BucketType.Penalty)){
			BigDecimal penalty = ccsPlan.getPastPenalty().add(ccsPlan.getCtdPenalty());
			if(inputMoney.add(exemptMoney).compareTo(penalty) == 1){
				return "冲减金额不能大于余额成分金额";
			}
			switch(planType){
			case J:
			case L:
			case P:
			case Q: txnCd = SysTxnCd.C05;  break;            //等额本息罚息贷调
			default:
			}
		}
		
		logger.info("合同号"+ccsLoan.getContrNbr()+"申请豁免");
		
		SysTxnCdMapping sysTxnCdMapping = parameterFacility.loadParameter(txnCd.toString(), SysTxnCdMapping.class);
//		TxnCd txnCd = parameterFacility.loadParameter(sysTxnCdMapping.txnCd, TxnCd.class);
		
		CcsTxnWaiveLog c = new CcsTxnWaiveLog();
		c.setOrg(ccsPlan.getOrg());
		c.setPlanNbr(ccsPlan.getPlanNbr());              //计划号  重要
		c.setOpTime(globalManagementService.getSystemStatus().getBusinessDate());
		c.setOpId(OrganizationContextHolder.getUsername());
		c.setAcctNbr(ccsPlan.getAcctNbr());
		c.setAcctType(ccsPlan.getAcctType());
		c.setLoanId(ccsLoan.getLoanId());
		c.setLoanType(ccsLoan.getLoanType());
		c.setContrNbr(ccsLoan.getContrNbr());
		c.setCardNbr(ccsLoan.getCardNbr());
		c.setTxnCode(sysTxnCdMapping.txnCd);                                  //入账交易码
		c.setTxnAmt(inputMoney);                           //交易金额  重要
		c.setBucketType(bucketType);
		c.setTxnDate(globalManagementService.getSystemStatus().getBusinessDate());
		c.setCurrency("156");                                    //币种
		c.setRefNbr(ccsPlan.getRefNbr());
		c.setTerm(ccsPlan.getTerm());
		c.setAdjState(AdjState.W);                           //等待审批
		c.setCreateTime(globalManagementService.getSystemStatus().getBusinessDate());
		c.setCreateUser(OrganizationContextHolder.getUsername());
		c.setLstUpdTime(globalManagementService.getSystemStatus().getBusinessDate());
		c.setLstUpdUser(OrganizationContextHolder.getUsername());
		c.setJpaVersion(0);                                        //乐观锁版本号存0
		c.setRemark(remark);
		c.setLogBizDate(globalManagementService.getSystemStatus().getBusinessDate());     //联机业务日期
		c.setDbCrInd(DbCrInd.C);
		
		rCcsTxnWaiveLog.save(c);
		return "操作成功";
	}
	

}
