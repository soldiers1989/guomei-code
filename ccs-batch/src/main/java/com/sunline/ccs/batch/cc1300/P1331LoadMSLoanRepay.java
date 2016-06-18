package com.sunline.ccs.batch.cc1300;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.LineItem;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.batch.common.MessageUtils;
import com.sunline.ccs.batch.common.TxnPrepare;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsOutsideDdTxn;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.CcsSettleClaim;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnSeq;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsOutsideDdTxn;
import com.sunline.ccs.infrastructure.shared.model.QCcsSettleClaim;
import com.sunline.ccs.loan.LoanUtil;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.ccs.service.api.Constants;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.CommandType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ppy.dictionary.enums.Ownership;
import com.sunline.ppy.dictionary.enums.PostTxnType;
import com.sunline.ppy.dictionary.enums.PostingFlag;
import com.sunline.ppy.dictionary.exchange.MSLoanMsgItem;
import com.sunline.ppy.dictionary.exchange.MSLoanRepaymentInterfaceItem;

/**
 * 马上消费贷款还款回盘文件处理
 * @author 刘启
 *
 */
public class P1331LoadMSLoanRepay implements ItemProcessor<LineItem<MSLoanRepaymentInterfaceItem>, MSLoanMsgItem> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private TxnPrepare txnPrepare;
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private BatchStatusFacility batchFacility;
	@Autowired
    private GlobalManagementService globalManagementService;
	@Autowired
	private LoanUtil loanUtil;
	@Autowired
	private MessageUtils messageUtils;
	private final String systemType = "CCS";
    @Value("#{env.instanceName}")
    private String instanceName;
	
	@Override
	public MSLoanMsgItem process(LineItem<MSLoanRepaymentInterfaceItem> item) throws Exception {
	    logger.info("马上消费贷款还款回盘文件处理开始......");
		try
		{
			MSLoanRepaymentInterfaceItem loanRepay = item.getLineObject();
			if(loanRepay.channelSerial==null && "".equals(loanRepay.channelSerial)){
				throw new IllegalArgumentException("渠道流水号-订单号不允许为空");
			}
			if(loanRepay.msDdReturnCode==null && "".equals(loanRepay.msDdReturnCode)){
				throw new IllegalArgumentException("订单号="+loanRepay.channelSerial+"渠道流水号-还款结果不允许为空");
			}
			if(loanRepay.txnAmt==null && "".equals(loanRepay.txnAmt)){
				throw new IllegalArgumentException("订单号="+loanRepay.channelSerial+"渠道流水号-还款金额不允许为空");
			}
			logger.debug("渠道流水号-订单号="+loanRepay.channelSerial+"还款结果msDdReturnCode="+loanRepay.msDdReturnCode);
			
			//取机构号并设置上下文
			List<String> orgs = globalManagementService.getServeOrg(systemType, instanceName);
			if(orgs == null || orgs.size() == 0){
				throw new IllegalArgumentException("未取得机构号");
			}
			for(String org :orgs){
				logger.debug("机构号=["+org+"]");
				if(!"root".equals(org)){
					OrganizationContextHolder.setCurrentOrg(org);
					logger.debug("找到机构号=["+org+"]");
					break;
				}
			}
			
			// 扣款失败短信通知接口
			MSLoanMsgItem msgItem = null;
			
			//根据渠道流水号（订单号）获取订单，更新状态的返回码，和返回业务时间
			CcsOrder order = matchCcsOrder(loanRepay);
			if(order == null){
				logger.error("根据渠道流水号-订单号{}未获得订单", item.getLineObject().channelSerial);
				throw new RuntimeException("根据渠道流水号-订单号{"+loanRepay.channelSerial+"}未获得订单");
			}
			
			if(order.getCode() !=null && order.getCommandType() == CommandType.BDB){
				throw new IllegalArgumentException("订单号="+loanRepay.channelSerial+",回盘文件重复订单编号,回盘返回码=["+loanRepay.msDdReturnCode+"],订单原有code=["+order.getCode()+"]");
			}else{
				logger.debug("订单号="+loanRepay.channelSerial+",回盘返回码=["+loanRepay.msDdReturnCode+"],订单原有code=["+order.getCode()+"]");
			}
			
			CcsOrder oOrder = null;
			//存在原始订单编号，此订单是拆分后的订单
			if(order.getOriOrderId() != null && !"".equals(order.getOriOrderId())){
				oOrder = matchOrigOrder(order);
				if(oOrder==null)
					throw new IllegalArgumentException("根据订单借据号-借据号{"+order.getOrderId()+"}未获得原始订单,原始订单号：["+order.getOriOrderId()+"]");
			}
				
			order.setCode(loanRepay.msDdReturnCode);
			order.setOptDatetime(batchFacility.getBatchDate());
			
			//如果交易成功，并且用途不为R/S/L/A/T/B 代偿交易不入账  生成入账流水 
			if("0".equals(loanRepay.msDdReturnCode) && order.getLoanUsage() != null){
				order.setOrderStatus(OrderStatus.S);
				order.setSuccessAmt(loanRepay.txnAmt);
				
				//生成金融交易;
				CcsLoanReg loanReg = null;
				switch(order.getLoanUsage()){
				case C:
					//查找原订单号等于当前订单的子订单
					if(order.getOriOrderId() == null || "".equals(order.getOriOrderId())){
						List<CcsOrder> orders = new ArrayList<CcsOrder>();
						orders = this.matchCcsOrder(order.getOrderId());
						for(CcsOrder o:orders){
							o.setOrderStatus(OrderStatus.S);
							o.setSuccessAmt(o.getTxnAmt());
							o.setCode(loanRepay.msDdReturnCode);
							o.setOptDatetime(batchFacility.getBatchDate());
							//理赔
							this.createTtTxnPost(o, SysTxnCd.S92);
							////如果是逾期扣款的和理赔的，需要更新loanreg中的扣款状态
							loanReg = matchCcsLoanReg(o);
							if(loanReg !=null ){
								loanReg.setDdRspFlag(Indicator.Y);
							}else{
								CcsSettleClaim ccsSettleClaim = matchSettleClaim(o);
								if(ccsSettleClaim != null){
									ccsSettleClaim.setSettleFlag(Indicator.Y);
									ccsSettleClaim.setSettleSucDate(batchFacility.getBatchDate());
								}
							}
						}
					}else{
						//理赔
						this.createTtTxnPost(order, SysTxnCd.S92);
						////如果是逾期扣款的和理赔的，需要更新loanreg中的扣款状态
						loanReg = matchCcsLoanReg(order);
						if(loanReg !=null ){
							loanReg.setDdRspFlag(Indicator.Y);
						}else{
							CcsSettleClaim ccsSettleClaim = matchSettleClaim(order);
							if(ccsSettleClaim != null){
								ccsSettleClaim.setSettleFlag(Indicator.Y);
								ccsSettleClaim.setSettleSucDate(batchFacility.getBatchDate());
							}
						}
					}
					break;
				case O:
					//逾期代扣,子订单不生成交易
					if(order.getOriOrderId() == null){
						this.createTtTxnPost(order, SysTxnCd.S93);
					}
					break;
				case N:
					//正常代扣
					this.createTtTxnPost(order, SysTxnCd.S94);
					break;
				case M:
					//结清扣款
					this.createTtTxnPost(order, SysTxnCd.S95);
					////如果是逾期扣款的和理赔的，需要更新loanreg中的扣款状态
					loanReg = matchCcsLoanReg(order);
					if(loanReg !=null ){
						loanReg.setValidDate(batchFacility.getBatchDate());
						loanReg.setDdRspFlag(Indicator.Y);
						loanReg.setAdvPmtAmt(order.getTxnAmt());
					}else{
						throw new IllegalArgumentException("根据订单借据号-借据号{"+order.getDueBillNo()+"}未获得loanReg,扣款用途["+order.getLoanUsage()+"]");
					}
					break;
				case S:
					//追偿代扣
					//不是子订单更新累加理赔代偿表中的的代偿金额
					if(order.getOriOrderId() == null){
						CcsSettleClaim ccsSettleClaim = matchSettleClaim(order);
						if(ccsSettleClaim == null){
							throw new IllegalArgumentException("根据订单借据号-借据号{"+order.getDueBillNo()+"}未获得SettleClaim");
							
						}
						ccsSettleClaim.setCompensatoryAmt(ccsSettleClaim.getCompensatoryAmt().add(order.getTxnAmt()));
						ccsSettleClaim.setLastCompensatoryDate(batchFacility.getBatchDate());
					}
					
					break;
				case A:
					//放款重提
					break;
				case B:
					//结算
					break;
				case L:
					//放款申请
					break;
				case E:
					//代付确认
					break;
				case F:
					//代扣确认
					break;
				case P:
					//PTP扣款
					//正常代扣
					this.createTtTxnPost(order, SysTxnCd.S91);
					CcsOutsideDdTxn outsideDdTxn = matchOutsideDdTxn(order);
					if(outsideDdTxn != null){
						outsideDdTxn.setDdRspFlag(Indicator.Y);
					}
					break;
				default : throw new IllegalArgumentException("订单用途不存在，订单号：" +order.getOrderId()+"，用途：" +order.getLoanUsage());
				}
				
				//更新原订单的金额和状态
				if(oOrder!=null){
					if(oOrder.getSuccessAmt() == null){
						oOrder.setSuccessAmt(BigDecimal.ZERO);
					}
					oOrder.setSuccessAmt(oOrder.getSuccessAmt().add(order.getTxnAmt()));
					oOrder.setOptDatetime(batchFacility.getBatchDate());
				}
			}else{
				//扣款失败,更新订单状态
				order.setOrderStatus(OrderStatus.E);
				order.setMessage(loanRepay.returnMessage);
				order.setFailureAmt(loanRepay.txnAmt);
				CcsLoanReg loanReg= null;
				switch(order.getLoanUsage()){
				case C:
					//查找原订单号等于当前订单的子订单
					if(order.getOriOrderId() == null || "".equals(order.getOriOrderId())){
						List<CcsOrder> orders = new ArrayList<CcsOrder>();
						orders = this.matchCcsOrder(order.getOrderId());
						for(CcsOrder o:orders){
							o.setOrderStatus(OrderStatus.E);
							o.setFailureAmt(o.getTxnAmt());
							o.setCode(loanRepay.msDdReturnCode);
							o.setOptDatetime(batchFacility.getBatchDate());
							loanReg = matchCcsLoanReg(o);
							if(loanReg !=null ){
								loanReg.setDdRspFlag(Indicator.N);
							}
						}
					}else{
						loanReg = matchCcsLoanReg(order);
						if(loanReg !=null ){
							loanReg.setDdRspFlag(Indicator.N);
						}
					}
					break;
				case M:
					loanReg = matchCcsLoanReg(order);
					if(loanReg !=null ){
						loanReg.setDdRspFlag(Indicator.N);
						loanReg.setAdvPmtAmt(BigDecimal.ZERO);
					}else{
						throw new IllegalArgumentException("根据订单借据号-借据号{"+order.getDueBillNo()+"}未获得loanReg,扣款用途["+order.getLoanUsage()+"]");
					}
					break;
				case P:
					//PTP扣款
					CcsOutsideDdTxn outsideDdTxn = matchOutsideDdTxn(order);
					if(outsideDdTxn !=null){
						outsideDdTxn.setDdRspFlag(Indicator.N);
					}
					break;
				default :break;
				}
					
				//更新原订单的金额和状态
				if(oOrder!=null){
					if(oOrder.getFailureAmt() == null){
						oOrder.setFailureAmt(BigDecimal.ZERO);
					}
					oOrder.setFailureAmt(oOrder.getFailureAmt().add(order.getTxnAmt()));
					oOrder.setOptDatetime(batchFacility.getBatchDate());
				}
					
				// 正常及提前扣款失败短信通知
				if(order.getLoanUsage().equals(LoanUsage.C) || order.getLoanUsage().equals(LoanUsage.S) ){
					//代位追偿和理赔不发短信
				}else{
					QCcsLoan qLoan = QCcsLoan.ccsLoan;
					CcsLoan loan = new JPAQuery(em).from(qLoan)
							.where(qLoan.contrNbr.eq(order.getContrNbr()).and(qLoan.dueBillNo.eq(order.getDueBillNo())))
							.singleResult(qLoan);
					LoanPlan loanPlan = parameterFacility.loadParameter(loan.getLoanCode(), LoanPlan.class);
					if((order.getLoanUsage()==LoanUsage.N || order.getLoanUsage()==LoanUsage.M) && Ownership.O == loanPlan.ownership){
						msgItem = new MSLoanMsgItem();
						msgItem.serialNo = messageUtils.getMsgSerialNo(order.getAcctNbr(), batchFacility.getBatchDate());
						CcsAcct acct = queryFacility.getAcctByAcctNbr(order.getAcctType(), order.getAcctNbr());
						
						//手Q短信模板:{客户姓名},{失败原因},失败原因取回盘文件
						//其他:{失败原因}
						msgItem.msgParams = (loan.getLoanCode().equals("4103")?
								acct.getName()+Constants.BATCH_SMS_SEPARATOR:"")
								+loanRepay.returnMessage;
						
						msgItem.phoneNbr = acct.getMobileNo();
						//批量短信文件接口新增四个字段
						msgItem.sourceBizSystem = Constants.SOURCE_BIZ_SYSTEM;	//业务系统
						msgItem.acqId = order.getAcqId();						//来源结构编号
						//如果合同是建户未进行提款的状态，ccsloan里没有值，这时候要先进行判断
						String loanCode = loanUtil.findLoanCode(acct);
						msgItem.loanCode = loanCode;				//贷款产品代码
						msgItem.sourceBizType = Constants.FROM_FAILURE;			//业务类型
					}
				}
					
			}
			//若存在原始交易,且原始交易金额=扣款成功金额+扣款失败金额，该订单为已完成
			if(oOrder!=null){
				if(oOrder.getFailureAmt() == null){
					oOrder.setFailureAmt(BigDecimal.ZERO);
				}
				if(oOrder.getSuccessAmt() == null){
					oOrder.setSuccessAmt(BigDecimal.ZERO);
				}
				if(oOrder.getTxnAmt().compareTo(oOrder.getSuccessAmt().add(oOrder.getFailureAmt()))==0){
					//如果是拆分处理中，更新为拆分已完成
					if(oOrder.getOrderStatus().equals(OrderStatus.G)){
						oOrder.setOrderStatus(OrderStatus.D);
						if(oOrder.getLoanUsage().equals(LoanUsage.O)){
							this.createTtTxnPost(oOrder, SysTxnCd.S93);
						}else if(oOrder.getLoanUsage().equals(LoanUsage.S)){
							CcsSettleClaim ccsSettleClaim = matchSettleClaim(order);
							if(ccsSettleClaim == null){
								throw new IllegalArgumentException("根据订单借据号-借据号{"+order.getDueBillNo()+"}未获得SettleClaim");
								
							}
							ccsSettleClaim.setCompensatoryAmt(ccsSettleClaim.getCompensatoryAmt().add(oOrder.getSuccessAmt()));
							ccsSettleClaim.setLastCompensatoryDate(batchFacility.getBatchDate());
						}
					}else{
						//订单为已完成
						oOrder.setOrderStatus(OrderStatus.S);
						this.createTtTxnPost(oOrder, SysTxnCd.S93);
					}
					
					oOrder.setOptDatetime(batchFacility.getBatchDate());
				}
				
			}
			
			return msgItem;
		}catch (Exception e) {
			logger.error("贷款还款文件处理异常, 渠道流水号-订单号{}", item.getLineObject().channelSerial);
			throw e;
		}
	}

	private CcsOrder matchCcsOrder(MSLoanRepaymentInterfaceItem loanRepay) {
		JPAQuery query = new JPAQuery(em);
		QCcsOrder qCcsOrder = QCcsOrder.ccsOrder;
		CcsOrder order = query.from(qCcsOrder)
				.where(qCcsOrder.orderId.eq(Long.parseLong(loanRepay.channelSerial))
						.and(qCcsOrder.txnAmt.eq(loanRepay.txnAmt)))
				.singleResult(qCcsOrder);
		return order;
	}
	
	private List<CcsOrder> matchCcsOrder(Long orgOrderId) {
		JPAQuery query = new JPAQuery(em);
		QCcsOrder qCcsOrder = QCcsOrder.ccsOrder;
		List<CcsOrder> orders = query.from(qCcsOrder)
				.where(qCcsOrder.oriOrderId.eq(orgOrderId))
				.list(qCcsOrder);
		return orders;
	}
	
	private CcsOrder matchOrigOrder(CcsOrder order) {
		JPAQuery query = new JPAQuery(em);
		QCcsOrder qCcsOrder = QCcsOrder.ccsOrder;
		CcsOrder origorder = query.from(qCcsOrder)
				.where(qCcsOrder.orderId.eq(order.getOriOrderId()))
				.singleResult(qCcsOrder);
		return origorder;
	}
	
	private CcsSettleClaim matchSettleClaim(CcsOrder order) {
		JPAQuery query = new JPAQuery(em);
		QCcsSettleClaim qCcsSettleClaim = QCcsSettleClaim.ccsSettleClaim;
		CcsSettleClaim settleClaim = query.from(qCcsSettleClaim)
				.where(qCcsSettleClaim.acctNbr.eq(order.getAcctNbr())
						.and(qCcsSettleClaim.guarantyId.eq(order.getGuarantyId()))
						.and(qCcsSettleClaim.dueBillNo.eq(order.getDueBillNo())))
				.singleResult(qCcsSettleClaim);
		return settleClaim;
	}
	
	private CcsOutsideDdTxn matchOutsideDdTxn(CcsOrder order) {
		JPAQuery query = new JPAQuery(em);
		QCcsOutsideDdTxn qCcsOutsideDdTxn = QCcsOutsideDdTxn.ccsOutsideDdTxn;
		CcsOutsideDdTxn outsideDdTxn = query.from(qCcsOutsideDdTxn)
				.where(qCcsOutsideDdTxn.acctNbr.eq(order.getAcctNbr())
						.and(qCcsOutsideDdTxn.acctType.eq(order.getAcctType()))
						.and(qCcsOutsideDdTxn.dueBillNo.eq(order.getDueBillNo()))
						.and(qCcsOutsideDdTxn.errInd.eq(Indicator.N)))
				.singleResult(qCcsOutsideDdTxn);
		return outsideDdTxn;
	}
	
	private CcsLoanReg matchCcsLoanReg(CcsOrder order) {
		JPAQuery query = new JPAQuery(em);
		QCcsLoanReg qccsLoanReg = QCcsLoanReg.ccsLoanReg;
		CcsLoanReg ccsLoanReg = query.from(qccsLoanReg)
				.where(qccsLoanReg.dueBillNo.eq(order.getDueBillNo())
						.and(qccsLoanReg.acctNbr.eq(order.getAcctNbr()))
						.and(qccsLoanReg.acctType.eq(order.getAcctType())))
				.singleResult(qccsLoanReg);
		return ccsLoanReg;
	}
	


	private CcsPostingTmp createTtTxnPost(CcsOrder order, SysTxnCd sysTxnCd) throws Exception {
		CcsPostingTmp txnPost = new CcsPostingTmp();
		CcsAcct acct = queryFacility.getAcctByAcctNbr(order.getAcctType(), order.getAcctNbr());
		// 交易主键生成
		CcsTxnSeq txnSeq = new CcsTxnSeq();
		txnSeq.setOrg(acct.getOrg());
		em.persist(txnSeq);
		
		txnPost.setTxnSeq(txnSeq.getTxnSeq());
		txnPost.setOrg(order.getOrg());
		txnPost.setAcctNbr(order.getAcctNbr());
		txnPost.setAcctType(order.getAcctType());
		txnPost.setCardNbr(acct.getDefaultLogicCardNbr());
		txnPost.setTxnDate(order.getBusinessDate());
		txnPost.setTxnTime(order.getSendTime());
		txnPost.setPostTxnType(PostTxnType.M);
		SysTxnCdMapping txnCdMapping = parameterFacility.loadParameter(sysTxnCd.toString(), SysTxnCdMapping.class);
		TxnCd txnCd = parameterFacility.loadParameter(txnCdMapping.txnCd, TxnCd.class);
		txnPost.setTxnCode(txnCdMapping.txnCd);
		txnPost.setOrigTxnCode(txnCdMapping.txnCd);
		txnPost.setDbCrInd(txnCd.logicMod.getDbCrInd());
		txnPost.setPostDate(batchFacility.getBatchDate());
		txnPost.setTxnAmt(order.getSuccessAmt());
		txnPost.setOrigTxnAmt(order.getTxnAmt());
		txnPost.setOrigSettAmt(order.getTxnAmt());
		txnPost.setPostAmt(order.getSuccessAmt());
		txnPost.setTxnCurrency(order.getCurrency());
		txnPost.setPostCurrency(order.getCurrency());
		txnPost.setOrigTransDate(null);//FIXME
		txnPost.setRelPmtAmt(BigDecimal.ZERO);
		txnPost.setOrigPmtAmt(BigDecimal.ZERO);//FIXME
		txnPost.setInterchangeFee(BigDecimal.ZERO);
		txnPost.setFeePayout(BigDecimal.ZERO);
		txnPost.setFeeProfit(BigDecimal.ZERO);
		txnPost.setLoanIssueProfit(BigDecimal.ZERO);
//		txnPost.setRefNbr()
		
		
		//设置渠道编码
		txnPost.setAcqAcceptorId(order.getAcqId());
		txnPost.setPostingFlag(PostingFlag.F00);
		txnPrepare.txnPrepare(txnPost, null);
		
		return txnPost;
	}
}
