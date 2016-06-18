package com.sunline.ccs.batch.cc1300;

import java.math.BigDecimal;
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
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnSeq;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.loan.LoanUtil;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.CommandType;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ppy.dictionary.enums.PostTxnType;
import com.sunline.ppy.dictionary.enums.PostingFlag;
import com.sunline.ppy.dictionary.exchange.MSLoanMsgItem;
import com.sunline.ppy.dictionary.exchange.MSLoanRepaymentInterfaceItem;

/**
 * 马上消费贷款批量代付回盘文件处理
 * @author 孟翔
 *
 */
public class P1341LoadMSDisburse implements ItemProcessor<LineItem<MSLoanRepaymentInterfaceItem>, MSLoanMsgItem> {
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
	    
		logger.info("马上消费批量代付回盘文件处理开始......");
		try
		{
			MSLoanRepaymentInterfaceItem loanRepay = item.getLineObject();
			
			if(loanRepay.channelSerial==null && "".equals(loanRepay.channelSerial)){
				throw new IllegalArgumentException("渠道流水号-订单号不允许为空");
			}
			if(loanRepay.msDdReturnCode==null && "".equals(loanRepay.msDdReturnCode)){
				throw new IllegalArgumentException("订单号="+loanRepay.channelSerial+"渠道流水号-应答码不允许为空");
			}
			if(loanRepay.txnAmt==null && "".equals(loanRepay.txnAmt)){
				throw new IllegalArgumentException("订单号="+loanRepay.channelSerial+"渠道流水号-交易金额不允许为空");
			}
			
			logger.debug("渠道流水号-订单号="+loanRepay.channelSerial+"应答码msDdReturnCode="+loanRepay.msDdReturnCode);
			
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
			
			//根据渠道流水号（订单号）获取订单，更新状态的返回码，和返回业务时间
			CcsOrder order = matchCcsOrder(loanRepay);
			if(order == null){
				logger.error("根据渠道流水号-订单号{}未获得订单", item.getLineObject().channelSerial);
				throw new RuntimeException("根据渠道流水号-订单号{"+loanRepay.channelSerial+"}未获得订单");
			}
			
			if(order.getCode() !=null && order.getCommandType() == CommandType.BDA){
				throw new IllegalArgumentException("订单号="+loanRepay.channelSerial+",回盘文件重复订单编号,回盘返回码=["+loanRepay.msDdReturnCode+"],订单原有code=["+order.getCode()+"]");
			}else{
				logger.debug("订单号="+loanRepay.channelSerial+",回盘返回码=["+loanRepay.msDdReturnCode+"],订单原有code=["+order.getCode()+"]");
			}
			
			order.setCode(loanRepay.msDdReturnCode);
			order.setOptDatetime(batchFacility.getBatchDate());
			
			//如果交易成功，并且用途不为R/S/L/A/T/B 代偿交易不入账  生成入账流水 
			if("0".equals(loanRepay.msDdReturnCode) && order.getLoanUsage() != null){
				
				order.setOrderStatus(OrderStatus.S);
				order.setSuccessAmt(loanRepay.txnAmt);
				
				if (LoanUsage.D.equals(order.getLoanUsage())) {
					// 溢缴款转出
					this.createTtTxnPost(order, SysTxnCd.D01);
				}
				
			}else{
				//扣款失败,更新订单状态
				order.setOrderStatus(OrderStatus.E);
				order.setMessage(loanRepay.returnMessage);
				order.setFailureAmt(loanRepay.txnAmt);
			}
			return null;
		}catch (Exception e) {
			logger.error("批量代付文件处理异常, 渠道流水号-订单号{}", item.getLineObject().channelSerial);
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
		
		//设置渠道编码
		txnPost.setAcqAcceptorId(order.getAcqId());
		txnPost.setPostingFlag(PostingFlag.F00);
		txnPrepare.txnPrepare(txnPost, null);
		
		return txnPost;
	}
}
