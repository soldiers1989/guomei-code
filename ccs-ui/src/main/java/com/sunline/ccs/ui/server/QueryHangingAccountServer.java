package com.sunline.ccs.ui.server;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
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
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsAuthmemoO;
import com.sunline.ccs.infrastructure.server.repos.RCcsTxnReject;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnReject;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnReject;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.ui.server.commons.CPSBusProvide;
import com.sunline.ccs.ui.server.commons.CheckUtil;
import com.sunline.kylin.web.ark.client.JPAQueryFetchResponseBuilder;
import com.sunline.kylin.web.ark.client.data.SelectOptionEntry;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.pcm.param.def.CurrencyCd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 挂账交易查询与维护
* @author fanghj
 *
 */
@Controller
@RequestMapping(value="/t3403Server")
public class QueryHangingAccountServer {

	private static final long serialVersionUID = 1L;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private UnifiedParameterFacility unifiedParameterService;
	@Autowired
	private RCcsTxnReject rTmTxnReject;
	
	//修改交易时间
	private QCcsAuthmemoO qCcsAuthmemoO = QCcsAuthmemoO.ccsAuthmemoO;
	private QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
	@Autowired
	private RCcsAuthmemoO rCcsAuthmemoO;

	@Autowired
	private CPSBusProvide cpsBusProvide;
	
	@Autowired
	private GlobalManagementService globalManagementService;
	
	private QCcsTxnReject qTmtxnReject = QCcsTxnReject.ccsTxnReject;
	
	private BooleanExpression exp;
	
	private CurrencyCdComparator currencyCdComparator = new CurrencyCdComparator();

	private TxnCdComparator txnCdComparator = new TxnCdComparator();
	@ResponseBody()
	@RequestMapping(value="/getAuthUnTranList",method={RequestMethod.POST})
	public FetchResponse getAuthUnTranList(@RequestBody FetchRequest request)  {
		Date beginDate = null;
		Date finishDate = null;
		long paramBeginDate = 0l;
		long paramEndDate = 0l;
		logger.info("beginDate"+request.getParameter("beginDate"));
		if(request.getParameter("beginDate") == null){
			beginDate = null;
		} else {
			paramBeginDate = Long.parseLong((String)request.getParameter("beginDate"));
			beginDate = new Date(paramBeginDate);
		}
		if(request.getParameter("endDate") == null){
			finishDate = null;
		} else {
			paramEndDate = Long.parseLong((String)request.getParameter("endDate"));
			finishDate = new Date(paramEndDate);
		}
		String contrNbr = (String) request.getParameter(CcsAcct.P_ContrNbr);
		String guarantyId = (String) request.getParameter(CcsAcct.P_GuarantyId);
		String acctNbr = null;
		AccountType acctType = null;
		List<CcsAcct> acctList = new ArrayList<CcsAcct>();
		
		if(contrNbr != null || guarantyId != null){
			JPAQuery q1 = new JPAQuery(em).from(qCcsAcct);
			if(contrNbr != null){
				q1 = q1.where(qCcsAcct.contrNbr.eq(contrNbr));
			}
			if(guarantyId != null){
				q1 = q1.where(qCcsAcct.guarantyId.eq(guarantyId));
			}
			acctList= q1.list(qCcsAcct);
			if(acctList != null && acctList.size() > 0){
			acctNbr = acctList.get(0).getAcctNbr().toString();
			acctType = acctList.get(0).getAcctType();
		}
		}
		
	if(acctNbr != null){
		exp = qTmtxnReject.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qTmtxnReject.
				acctNbr.eq(Long.parseLong(acctNbr))).and(qTmtxnReject.acctType.eq(acctType));
		
			if (null != beginDate) {
				exp = exp.and(qTmtxnReject.txnDate.goe(beginDate));
			}
			if (null != finishDate) {
				exp = exp.and(qTmtxnReject.txnDate.loe(finishDate));
			}
			JPAQuery query = new JPAQuery(em);
			query.from(qTmtxnReject).where(exp);
			FetchResponse response = this.fetchRes(request, query);
			return response;
			
		}else {
		    	logger.info("---------------------开始日期"+beginDate+"--------------------------");
			exp = qTmtxnReject.org.eq(OrganizationContextHolder.getCurrentOrg())
					.and(qTmtxnReject.txnDate.goe(beginDate));
			if (null != finishDate) {
				exp = exp.and(qTmtxnReject.txnDate.loe(finishDate));
			}
			JPAQuery query = new JPAQuery(em);
			query.from(qTmtxnReject).where(exp);
			return this.fetchRes(request, query);
		} 
	}
	
	/**
	 * 返回查询结果
	 * @param request
	 * @param query
	 * @return
	 */
	public FetchResponse fetchRes(FetchRequest request, JPAQuery query) {
		return new JPAQueryFetchResponseBuilder(request, query)
				.addFieldMapping(CcsTxnReject.P_AcctNbr,
						qTmtxnReject.acctNbr)
				.addFieldMapping(CcsTxnReject.P_AcctType, 
						qTmtxnReject.acctType)
				.addFieldMapping(CcsTxnReject.P_AcqAcceptorId, 
						qTmtxnReject.acqAcceptorId)
				.addFieldMapping(CcsTxnReject.P_AcqBranchIq, 
						qTmtxnReject.acqBranchIq)
				.addFieldMapping(CcsTxnReject.P_AcqAddress, 
						qTmtxnReject.acqAddress)
				.addFieldMapping(CcsTxnReject.P_AcqTerminalId, 
						qTmtxnReject.acqTerminalId)
				.addFieldMapping(CcsTxnReject.P_AuthCode, 
						qTmtxnReject.authCode)
				.addFieldMapping(CcsTxnReject.P_CardBasicNbr, 
						qTmtxnReject.cardBasicNbr)
				.addFieldMapping(CcsTxnReject.P_CardBlockCode, 
						qTmtxnReject.cardBlockCode)
				.addFieldMapping(CcsTxnReject.P_CardNbr, 
						qTmtxnReject.cardNbr)
				.addFieldMapping(CcsTxnReject.P_DbCrInd, 
						qTmtxnReject.dbCrInd)
				.addFieldMapping(CcsTxnReject.P_FeeProfit, 
						qTmtxnReject.feeProfit)
				.addFieldMapping(CcsTxnReject.P_OrigSettAmt, 
						qTmtxnReject.origSettAmt)	
				.addFieldMapping(CcsTxnReject.P_OrigTxnAmt, 
						qTmtxnReject.origTxnAmt)
				.addFieldMapping(CcsTxnReject.P_OrigTxnCode, 
						qTmtxnReject.origTxnCode)
				.addFieldMapping(CcsTxnReject.P_FeePayout, 
						qTmtxnReject.feePayout)
				.addFieldMapping(CcsTxnReject.P_InterchangeFee, 
						qTmtxnReject.interchangeFee)
				.addFieldMapping(CcsTxnReject.P_LoanIssueProfit, 
						qTmtxnReject.loanIssueProfit)		
				.addFieldMapping(CcsTxnReject.P_LogicCardNbr, 
						qTmtxnReject.logicCardNbr)
				.addFieldMapping(CcsTxnReject.P_Mcc, 
						qTmtxnReject.mcc)
				.addFieldMapping(CcsTxnReject.P_Org, 
						qTmtxnReject.org)
				.addFieldMapping(CcsTxnReject.P_OrigPmtAmt, 
						qTmtxnReject.origPmtAmt)
				.addFieldMapping(CcsTxnReject.P_OrigTransDate, 
						qTmtxnReject.origTransDate)
				.addFieldMapping(CcsTxnReject.P_TxnDate, 
						qTmtxnReject.txnDate)
				.addFieldMapping(CcsTxnReject.P_PlanNbr, 
						qTmtxnReject.planNbr)
				.addFieldMapping(CcsTxnReject.P_Points, 
						qTmtxnReject.points)
				.addFieldMapping(CcsTxnReject.P_PostAmt, 
						qTmtxnReject.postAmt)
				.addFieldMapping(CcsTxnReject.P_PostCurrency, 
						qTmtxnReject.postCurrency)
				.addFieldMapping(CcsTxnReject.P_PostDate, 
						qTmtxnReject.postDate)
				.addFieldMapping(CcsTxnReject.P_PostingFlag, 
						qTmtxnReject.postingFlag)
				.addFieldMapping(CcsTxnReject.P_PostTxnType, 
						qTmtxnReject.postTxnType)
				.addFieldMapping(CcsTxnReject.P_PrePostingFlag, 
						qTmtxnReject.prePostingFlag)
				.addFieldMapping(CcsTxnReject.P_ProductCd, 
						qTmtxnReject.productCd)
				.addFieldMapping(CcsTxnReject.P_RefNbr, 
						qTmtxnReject.refNbr)
				.addFieldMapping(CcsTxnReject.P_RelPmtAmt, 
						qTmtxnReject.relPmtAmt)
				.addFieldMapping(CcsTxnReject.P_StmtDate, 
						qTmtxnReject.stmtDate)
				.addFieldMapping(CcsTxnReject.P_TxnAmt, 
						qTmtxnReject.txnAmt)
				.addFieldMapping(CcsTxnReject.P_TxnCode, 
						qTmtxnReject.txnCode)
				.addFieldMapping(CcsTxnReject.P_TxnCurrency, 
						qTmtxnReject.txnCurrency)
				.addFieldMapping(CcsTxnReject.P_TxnDate, 
						qTmtxnReject.txnDate)
				.addFieldMapping(CcsTxnReject.P_TxnDesc, 
						qTmtxnReject.txnDesc)
				.addFieldMapping(CcsTxnReject.P_TxnSeq, 
						qTmtxnReject.txnSeq)
				.addFieldMapping(CcsTxnReject.P_TxnShortDesc, 
						qTmtxnReject.txnShortDesc)
				.addFieldMapping(CcsTxnReject.P_TxnTime, 
						qTmtxnReject.txnTime)
				.addFieldMapping(CcsTxnReject.P_VoucherNo,
						qTmtxnReject.voucherNo).build();

	}

	@Transactional
	@ResponseBody
	@RequestMapping(value="/alterInfo",method={RequestMethod.POST})
	public void alterInfo(@RequestBody String cardNo, @RequestBody String txnCode, @RequestBody String txnDate,
			@RequestBody String postAmtParam, @RequestBody String postCurrCd, @RequestBody String refNbr,@RequestBody String txnSeq) {
		
		logger.info("alterInfo:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNo) + "], txnSeq[" + txnSeq + "], txnCode[" + txnCode 
				+ "], txnDate["+txnDate+"], postAmt["+postAmtParam+"], postCurrCd["+postCurrCd+"]");
		BigDecimal decimal = new BigDecimal(postAmtParam);
		/*---- 1.校验输入项目是否合法 ---*/
		CheckUtil.checkCardNo(cardNo);
		CheckUtil.rejectNull(txnCode, "交易代码不允许为空");
		CheckUtil.rejectNull(txnDate, "日期不允许为空");
		CheckUtil.rejectNull(postAmtParam, "入账金额不允许为空");
		CheckUtil.rejectNull(postCurrCd, "入账币种不允许为空");
		
		long paramTxnDate = Long.parseLong(txnDate);
		Date date = new Date(paramTxnDate);
		
		List<CcsAcct> tmAccounts = cpsBusProvide.getTmAccountTocardNbr(cardNo);
		if(tmAccounts == null || tmAccounts.size() == 0){
			throw new ProcessException("未找到账户记录");	
		}
		
		//根据输入的交易码读取交易码参数表，如果交易码不存在则返回提示信息
		//暂时屏蔽
		//TxnCd txnCd = unifiedParameterService.retrieveParameterObject(txnCode, TxnCd.class);
		//CheckUtil.rejectNull(txnCd, "该交易代码["+txnCode+"]不存在");
		
		//比较当天业务日期和输入的交易日期
		Date currDate = globalManagementService.getSystemStatus().getBusinessDate();
		if (DateUtils.truncatedCompareTo(date, currDate, Calendar.DATE) > 0){
			throw new ProcessException("输入的交易日期不能大于当天业务日期");
		}

		//查找币种参数记录
		//暂时屏蔽
		//CurrencyCtrl currencyCtrl = unifiedParameterService.retrieveParameterObject(postCurrCd, CurrencyCtrl.class);
		//CheckUtil.rejectNull(currencyCtrl, "币种:[" + postCurrCd + "]查询不到对应的参数配置");
		
		//查找卡产品参数
		//暂时屏蔽
		//ProductCredit product = unifiedParameterService.retrieveParameterObject(tmCard.getProductCd(), ProductCredit.class);
		//CheckUtil.rejectNull(product, "卡产品:[" + tmCard.getProductCd() + "]查询不到对应的参数配置");
		//if(!postCurrCd.equals(product.postCurrCd) && !postCurrCd.equals(product.dualCurrCd)){
		//	throw new ProcessException("入账币种["+postCurrCd+"]不正确!");
		//}
		
		//查找信用计划模板参数
//		String planNbr = product.planNbrList.get(txnCd.planType);
//		CheckUtil.rejectNull(planNbr, "卡产品号:[" + tmCard.getProductCd() + "], 交易代码:[" + txnCd.txnCd +"] 查询不到对应的参数配置");
//		PlanTemplate  planTemplate = unifiedParameterService.retrieveParameterObject(planNbr,  PlanTemplate.class);
//		CheckUtil.rejectNull(planTemplate, "卡产品号:[" + tmCard.getProductCd() + "], 交易代码:[" + txnCd.txnCd +"] 查询不到对应的信用计划模板参数配置");
//		if(PlanType.O == planTemplate.planType || PlanType.I == planTemplate.planType) {
//			CheckUtil.rejectNull(refNbr, "交易检索参考号不允许为空");
//		}
		
		//如果调整的交易为分期类交易，那么参考号是必须输的
		//暂时屏蔽
//		if(PlanType.O == txnCd.planType || PlanType.I == txnCd.planType) {
//			CheckUtil.rejectNull(refNbr, "调整的交易为分期类交易，交易参考号不允许为空");
//		}
		
		//查找挂账交易流水
		CcsTxnReject  tmTxnReject = rTmTxnReject.findOne(Long.parseLong(txnSeq));
		CheckUtil.rejectNull(tmTxnReject, "未找到挂账交易流水记录["+txnSeq+"]");
		
		JPAQuery query = new JPAQuery(em);
		List<CcsAuthmemoO> list = new ArrayList<CcsAuthmemoO>();
		BooleanExpression b = null;
		
		//由于SimpleDateFormat本身是非线程安全的，当页面的操作频繁时可能造成线程死锁，此处用Calendar取代
		Calendar calendar =Calendar.getInstance();
		String dateStr = null;
		String dateStr2 = null;
		if(calendar instanceof GregorianCalendar){
			calendar.setTime(tmTxnReject.getTxnTime());
			//补足Calendar时间位数
			//用于匹配AuthMemoO
			dateStr =timeFormat(calendar.get(Calendar.MONTH)+1)+
					timeFormat(calendar.get(Calendar.DAY_OF_MONTH))+
					timeFormat(calendar.get(Calendar.HOUR_OF_DAY))+
					timeFormat(calendar.get(Calendar.MINUTE))+
					timeFormat(calendar.get(Calendar.SECOND));
			//用于修改AuthMemoO
			calendar.setTime(date);
			dateStr2 =timeFormat(calendar.get(Calendar.MONTH)+1)+
					timeFormat(calendar.get(Calendar.DAY_OF_MONTH))+
					timeFormat(calendar.get(Calendar.HOUR_OF_DAY))+
					timeFormat(calendar.get(Calendar.MINUTE))+
					timeFormat(calendar.get(Calendar.SECOND));
		}
		
		//B007域内容为MMddHHmmss
		b=qCcsAuthmemoO.b007TxnTime.eq(dateStr);
		if(tmTxnReject.getAuthCode()!=null){
//				b=b.and(qCcsAuthmemoO.b011Trace.eq(tmTxnReject.getAuthCode()));
			// 20160421--lisy
			// 使用memoO表authCode匹配
			// 注：memoO表 B011:根据REF_NBR倒数9位至倒数3位生成
			//	   		  AuthCode:交易成功时，根据logKv生成
			// 	   reject表 authCode:根据logKv生成
			b=b.and(qCcsAuthmemoO.authCode.eq(tmTxnReject.getAuthCode()));
		}
		query = query.from(qCcsAuthmemoO).where(b);
		
		list = query.list(qCcsAuthmemoO);
		if(list != null && list.size() > 0){
			for(CcsAuthmemoO ccsAuthmemoO : list){
				ccsAuthmemoO.setB007TxnTime(dateStr2);
				rCcsAuthmemoO.saveAndFlush(ccsAuthmemoO);          
			}
		}
				
		//更新挂账交易流水信息
		tmTxnReject.setCardNbr(cardNo);
		tmTxnReject.setTxnCode(txnCode);
		tmTxnReject.setTxnDate(date);
		tmTxnReject.setPostAmt(decimal);
		tmTxnReject.setPostCurrency(postCurrCd);
		tmTxnReject.setRefNbr(refNbr);
		tmTxnReject.setTxnSeq(Long.parseLong(txnSeq));
		
		rTmTxnReject.saveAndFlush(tmTxnReject);
	}

	
	public List<CurrencyCd> getCurrencyCdList(String cardNo)
			 {
		logger.info("getCurrencyCdList:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNo) + "]");
		
		CheckUtil.checkCardNo(cardNo);
		CcsCard tmCard = cpsBusProvide.getTmCardTocardNbr(cardNo);
		
		ArrayList<CurrencyCd> currencyCdList = new ArrayList<CurrencyCd>();
		//获取所有币种代码参数
		Map<String, CurrencyCd> currencyCdMap = unifiedParameterService.retrieveParameterObject(CurrencyCd.class);
		//获取卡产品参数
		ProductCredit product = unifiedParameterService.retrieveParameterObject(tmCard.getProductCd(), ProductCredit.class);
		
		//将卡产品中的本币和外币币种代码返回
		if(product != null) {
			if(StringUtils.isNotBlank(product.postCurrCd)) {
				currencyCdList.add(currencyCdMap.get(product.postCurrCd));
			}
			
			if(StringUtils.isNotBlank(product.dualCurrCd)) {
				currencyCdList.add(currencyCdMap.get(product.dualCurrCd));
			}
		}
		
		//按照币种代码对币种代码列表进行排序
		Collections.sort(currencyCdList, currencyCdComparator);
		
		return currencyCdList;
	}
	//获取所有的交易码-区别于账户调整的交易码
	@ResponseBody()
	@RequestMapping(value = "/getTxnCd", method = { RequestMethod.POST })
	public List<SelectOptionEntry> getTxnCdList() {
		Map<String, TxnCd> txnCdMap = unifiedParameterService
				.retrieveParameterObject(TxnCd.class);
		List<TxnCd> txnCdList = new ArrayList<TxnCd>();
		for (TxnCd code : txnCdMap.values()) {
				txnCdList.add(code);
		}

		// 按照交易码对交易码列表进行排序
		Collections.sort(txnCdList, txnCdComparator);

		List<SelectOptionEntry> uiShowData = new ArrayList<SelectOptionEntry>();
		if(txnCdList != null && !txnCdList.isEmpty()) {
			for(TxnCd txnCd : txnCdList) {
				SelectOptionEntry entry = new SelectOptionEntry(txnCd.txnCd,txnCd.description );
				uiShowData.add(entry);
			}
		}
		return uiShowData;
	}
	
	/**
	 * 补足字符串位数-不足2位则补足
	 * @return
	 */
	public static String timeFormat(int i){
		return (i+"").length()==1?"0"+i:""+i;
	}
	
	public static void main(String argsp[]) throws ParseException{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		System.out.println(format.format(new Date(1469980800000L)));
	}
////		System.out.println(format.parse("2014-12-17 13:55:39").getTime());
//		//验证Calendar
//		//由于SimpleDateFormat本身是非线程安全的，当页面的操作频繁时可能造成线程死锁，此处用Calendar取代
//		Calendar calendar =Calendar.getInstance();
//		String dateStr = null;
//		if(calendar instanceof GregorianCalendar){
//			calendar.setTime(new Date(1418795739000L));
//			//补足Calendar时间位数
//			//用于匹配AuthMemoO
//			dateStr =timeFormat(calendar.get(Calendar.MONTH)+1)+
//					timeFormat(calendar.get(Calendar.DAY_OF_MONTH))+
//					timeFormat(calendar.get(Calendar.HOUR_OF_DAY))+
//					timeFormat(calendar.get(Calendar.MINUTE))+
//					timeFormat(calendar.get(Calendar.SECOND));
//			System.out.println(dateStr);
//			System.out.println(new Date(1418795739000L));
//			//用于修改AuthMemoO
//			calendar.setTime(new Date());
//			String dateStr2 =timeFormat(calendar.get(Calendar.MONTH)+1)+
//					timeFormat(calendar.get(Calendar.DAY_OF_MONTH))+
//					dateStr.substring(4);
//			System.out.println(dateStr2);
//		}
//		
//	}
}
