package com.sunline.ccs.service.handler.sub;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.MapBuilder;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.FetchSmsNbrFacility;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsCssfeeReg;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsCssfeeReg;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsPointsReg;
import com.sunline.ccs.infrastructure.shared.model.CcsStatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsStatement;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.param.def.enums.PinBlockResetInd;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ppy.api.MediumService;
import com.sunline.ppy.api.MmCardService;
import com.sunline.ppy.dictionary.enums.CntType;
import com.sunline.ppy.dictionary.enums.PointAdjustIndicator;
import com.sunline.ppy.dictionary.enums.PostTxnType;
import com.sunline.ppy.dictionary.exception.ProcessException;
//import com.sunline.smsd.service.sdk.DownMsgFacility;

/**
 * @see 类名：PointComm
 * @see 描述：TODO 中文描述
 *
 * @see 创建日期： 2015-6-25下午12:06:19
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class Common {
	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;
	@Autowired
    public GlobalManagementService globalManagementService;
/*	@Autowired
    public DownMsgFacility downMsgFacility;
*/    @Autowired
    public FetchSmsNbrFacility fetchMsgCdService;
	
	@Resource(name = "mmCardService")
    public MmCardService mmCardService;
    @Resource(name = "mediumService")
    public MediumService mediumService;
	
	@Autowired
    private RCcsCustomer rCcsCustomer;
	@Autowired
	private RCcsCssfeeReg rCcsCssfeeReg;
	@PersistenceContext
	private EntityManager em;
	
	QCcsCustomer qCcsCustomer = QCcsCustomer.ccsCustomer;
	QCcsStatement qCcsStatement = QCcsStatement.ccsStatement;

	/**
	 * @see 方法名：countAddPoint
	 * @see 描述：计算当天增加的积分 countAddPoint(这里用一句话描述这个方法的作用) (这里描述这个方法适用条件 – 可选)
	 * @see 创建日期：2015-6-25上午11:59:13
	 * @author ChengChun
	 * 
	 * @param CcsAcct
	 * @return
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BigDecimal countAddPoint(CcsAcct CcsAcct) {
		List<CcsPointsReg> tmPointList = queryFacility.getPointsRegByacctNbrAcctType(CcsAcct, PointAdjustIndicator.I);
		BigDecimal points = BigDecimal.ZERO;
		for (CcsPointsReg tmPointReg : tmPointList) {
			points = points.add(tmPointReg.getPoints());
		}
		return points;
	}

	/**
	 * @see 方法名：countReducePoint
	 * @see 描述：计算积分减少和兑换的总和 countReducePoint
	 * @see 创建日期：2015-6-25上午11:46:14
	 * @author ChengChun
	 * 
	 * @param CcsAcct
	 * @return
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BigDecimal countReducePoint(CcsAcct CcsAcct) {
		List<CcsPointsReg> tmPointList = queryFacility.getPointsRegByacctNbrAcctType(CcsAcct, PointAdjustIndicator.A, PointAdjustIndicator.D);
		BigDecimal points = BigDecimal.ZERO;
		for (CcsPointsReg tmPointReg : tmPointList) {
			points = points.add(tmPointReg.getPoints());
		}
		return points;
	}

	/**
	 * @see 方法名：subtractPoint
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：2015-6-25下午12:01:51
	 * @author ChengChun
	 * 
	 * @param cardNbr
	 * @param CcsAcct
	 * @param points
	 * @param adjustInd
	 * @param refNbr
	 * @return
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public CcsPointsReg subtractPoint(String cardNbr, CcsAcct CcsAcct, Integer points, PointAdjustIndicator adjustInd, String refNbr) throws ProcessException {
		if (CcsAcct.getPointsBal().subtract(countReducePoint(CcsAcct)).intValue() < points.intValue()) {
			throw new ProcessException(Constants.ERRB071_CODE, Constants.ERRB071_MES);
		}
		CcsPointsReg tmPointReg = genCcsPointsReg(CcsAcct, cardNbr, points, adjustInd, refNbr);
		return tmPointReg;
	}

	/**
	 * @see 方法名：genCcsPointsReg
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：2015-6-25下午12:08:01
	 * @author ChengChun
	 * 
	 * @param CcsAcct
	 * @param cardNbr
	 * @param points
	 * @param adjustInd
	 * @param refNbr
	 * @return
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private CcsPointsReg genCcsPointsReg(CcsAcct CcsAcct, String cardNbr, Integer points, PointAdjustIndicator adjustInd, String refNbr) {
		CcsPointsReg tmPointReg = new CcsPointsReg();
		tmPointReg.setAcctNbr(CcsAcct.getAcctNbr());
		tmPointReg.setAcctType(CcsAcct.getAcctType());
		tmPointReg.setPoints(new BigDecimal(points));
		tmPointReg.setAdjInd(adjustInd);
		tmPointReg.setCardNbr(cardNbr);
		tmPointReg.setOrg(CcsAcct.getOrg());
		tmPointReg.setPostTxnType(PostTxnType.P);
		tmPointReg.setTxnDate(unifiedParameterFacilityProvide.BusinessDate());
		tmPointReg.setRequestTime(new Date());
		tmPointReg.setRefNbr(refNbr);
		return tmPointReg;
	}
	
	
	/**
	 * @see 方法名：cssfeeReg
	 * @see 描述：杂项费用 - 客服费通知
	 * @see 创建日期：2015年6月25日下午5:33:38
	 * @author yanjingfeng
	 * 
	 * @param cardNbr
	 * @param serviceNbr
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void cssfeeReg(String cardNbr, String serviceNbr) {
		CcsCssfeeReg cssFeeReg = new CcsCssfeeReg();
		cssFeeReg.setOrg(OrganizationContextHolder.getCurrentOrg());
		cssFeeReg.setServiceNbr(serviceNbr);
		cssFeeReg.setCardNbr(cardNbr);
		cssFeeReg.setTxnDate(globalManagementService.getSystemStatus().getBusinessDate());
		cssFeeReg.setRequestTime(new Date());
		rCcsCssfeeReg.save(cssFeeReg);
	}
	
	/**
	 * @see 方法名：validateInqPwd
	 * @see 描述：查询密码验证
	 * @see 创建日期：2015年6月25日下午5:36:28
	 * @author yanjingfeng
	 * 
	 * @param cardNbr
	 * @param qpin
	 * @param product
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@Transactional(noRollbackFor = { Throwable.class })
	public void validateInqPwd(String cardNbr, String qpin, ProductCredit product) throws ProcessException {
		CcsCardO CcsCardO = queryFacility.getCardOByCardNbr(cardNbr);
		Date date = new Date();
		PinBlockResetInd pinBlockResetInd = product.pinBlockResetInd;
		if (pinBlockResetInd == PinBlockResetInd.D) {
			// 隔日查询错误次数清空
			if (CcsCardO.getLastInqPinTriesTime() != null && date.after(DateUtils.addDays(CcsCardO.getLastInqPinTriesTime(), 1))) {
				CcsCardO.setInqPinTries(0);
			}
		}
		if (!mediumService.isValidQueryPin(cardNbr, qpin)) {
			try {
				if (product.maxInqPinTry <= CcsCardO.getInqPinTries()) {
					throw new ProcessException(Constants.ERRB097_CODE, Constants.ERRB097_MES);
				}
				throw new ProcessException(Constants.ERRB062_CODE, Constants.ERRB062_MES);
			} catch (ProcessException e) {
				throw new ProcessException(e.getErrorCode(), e.getMessage());
			} finally {
				CcsCardO.setInqPinTries(CcsCardO.getInqPinTries() + 1);
				CcsCardO.setLastInqPinTriesTime(new Date());
			}
		} else {
			if (product.maxInqPinTry <= CcsCardO.getInqPinTries()) {
				throw new ProcessException(Constants.ERRB097_CODE, Constants.ERRB097_MES);
			}
			CcsCardO.setInqPinTries(0);
		}
	}

	/**
	 * @see 方法名：validatePPwd
	 * @see 描述：交易密码验证
	 * @see 创建日期：2015年6月25日下午5:36:43
	 * @author yanjingfeng
	 * 
	 * @param cardNbr
	 * @param pin
	 * @param product
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@Transactional(noRollbackFor = { Throwable.class })
	public void validatePPwd(String cardNbr, String pin, ProductCredit product) throws ProcessException {
		CcsCardO CcsCardO = queryFacility.getCardOByCardNbr(cardNbr);
		Date date = new Date();
		PinBlockResetInd pinBlockResetInd = product.pinBlockResetInd;
		if (pinBlockResetInd == PinBlockResetInd.D) {
			// 隔天交易错误次数清空
			if (CcsCardO.getLastPinTriesTime() != null && date.after(DateUtils.addDays(CcsCardO.getLastPinTriesTime(), 1))) {
				CcsCardO.setPinTries(0);
			}
		}
		if (!mediumService.isValidTransPin(cardNbr, pin)) {
			try {
				if (product.pinTry <= CcsCardO.getPinTries()) {
					throw new ProcessException(Constants.ERRB098_CODE, Constants.ERRB098_MES);
				}

				throw new ProcessException(Constants.ERRB063_CODE, Constants.ERRB063_MES);
			} finally {
				CcsCardO.setPinTries(CcsCardO.getPinTries() + 1);
				CcsCardO.setLastPinTriesTime(new Date());
			}

		} else {
			// 交易密码正确但是，之前已经失败次数超过最大设置次数也抛异常
			if (product.pinTry <= CcsCardO.getPinTries()) {
				throw new ProcessException(Constants.ERRB098_CODE, Constants.ERRB098_MES);
			}

			CcsCardO.setPinTries(0);
		}
	}

	/**
	 * @see 方法名：sendMsg
	 * @see 描述：发短信
	 * @see 创建日期：2015年6月25日下午5:36:13
	 * @author yanjingfeng
	 * 
	 * @param productCD
	 * @param custId
	 * @param cardNbr
	 * @param cpsMsgCategory
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void sendMsg(String productCD, Long custId, String cardNbr, CPSMessageCategory cpsMsgCategory) {
		CcsCustomer ccsCustomer = rCcsCustomer.findOne(qCcsCustomer.custId.eq(Long.valueOf(custId)));
/*		downMsgFacility.sendMessage(fetchMsgCdService.fetchMsgCd(productCD, cpsMsgCategory), cardNbr, ccsCustomer.getName(), ccsCustomer.getGender(),
				ccsCustomer.getMobileNo(), new Date(), new MapBuilder<String, Object>().build());
*/	}


	// //校验查询密码错误次数是否超过系统参数设置的最大次数,这种方法影响整体OperateServiceImpl这个类纯粹作为一个service的用途，待后期放到util类中
	// private final void validateQPasswordErrorCount(CcsCardO
	// CcsCardO,ProductCredit product) throws ProcessException{
	// CheckUtil.rejectNull(CcsCardO,
	// Constants.ERRB001_CODE,Constants.ERRB001_MES);
	// if(CcsCardO.getInqPinTries() >= product.maxInqPinTry){
	// throw new ProcessException(Constants.ERRB097_CODE,Constants.ERRB097_MES);
	// }
	//
	//
	//
	// }
	// //校验交易密码错误次数是否超过系统参数设置的最大次数
	// private final void validatePPasswordErrorCount(CcsCardO
	// CcsCardO,ProductCredit product) throws ProcessException{
	// CheckUtil.rejectNull(CcsCardO,
	// Constants.ERRB001_CODE,Constants.ERRB001_MES);
	//
	// if(CcsCardO.getPinTries() >= product.pinTry){
	// throw new ProcessException(Constants.ERRB098_CODE,Constants.ERRB098_MES);
	// }
	// }

	/**
	 * @see 方法名：cvv2IsNotOverSetZero
	 * @see 描述：判断如果cvv2正确，错误次数 没有超限，则清零
	 * @see 创建日期：2015年6月25日下午5:34:06
	 * @author yanjingfeng
	 * 
	 * @param cardno
	 * @param expiryDate
	 * @param product
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void cvv2IsNotOverSetZero(String cardno, String expiryDate, ProductCredit product) {
		Map<CntType, Integer> ms3106 = mmCardService.MS3106(cardno, expiryDate);
		if (ms3106 != null) {
			if (ms3106.get(CntType.cvn2) != null) {
				if (product.cvv2Try != null && product.cvv2Try > ms3106.get(CntType.cvn2)) {
					mmCardService.MS3401(cardno, expiryDate, CntType.cvn2);
				} else if (product.cvv2Try != null && product.cvv2Try <= ms3106.get(CntType.cvn2)) {
					throw new ProcessException(Constants.ERRB130_CODE, Constants.ERRB130_MES);
				}
			}
		}
	}

	/**
	 * @see 方法名：cvv2IsOver
	 * @see 描述：非金融cvv2次数超限报警
	 * @see 创建日期：2015年6月25日下午5:34:22
	 * @author yanjingfeng
	 * 
	 * @param cardno
	 * @param expiryDate
	 * @param product
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void cvv2IsOver(String cardno, String expiryDate, ProductCredit product) {
		Map<CntType, Integer> ms3106 = mmCardService.MS3106(cardno, expiryDate);
		if (ms3106 != null) {
			if (product.cvv2Try != null && ms3106.get(CntType.cvn2) != null && product.cvv2Try < ms3106.get(CntType.cvn2)) {
				throw new ProcessException(Constants.ERRB130_CODE, Constants.ERRB130_MES);
			}
		}
	}
	
	
	/** 
	 * @see 方法名：getLastStmtHst 
	 * @see 描述：获取最近一期账单
	 * @see 创建日期：2015年6月25日上午12:28:23
	 * @author yuyang
	 *  
	 * @param acct
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public CcsStatement getLastStmtHst(CcsAcct acct) {
		JPAQuery query = new JPAQuery(em);
		CcsStatement statement = query.from(qCcsStatement).where(qCcsStatement.acctNbr.eq(acct.getAcctNbr()).and(qCcsStatement.acctType.eq(acct.getAcctType()))).orderBy(qCcsStatement.stmtDate.desc())
				.singleResult(qCcsStatement);
		return statement;
	}

	/** 
	 * @see 方法名：genMessageApplySuccess 
	 * @see 描述：分期付款申请成功提醒
	 * @see 创建日期：2015年6月25日上午12:28:36
	 * @author yuyang
	 *  
	 * @param cardNbr
	 * @param acct
	 * @param loanReg
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void genMessageApplySuccess(String cardNbr, CcsAcct acct, CcsLoanReg loanReg) {
		CcsCustomer customer = rCcsCustomer.findOne(acct.getCustId());
		String msgCd = fetchMsgCdService.fetchMsgCd(acct.getProductCd(), CPSMessageCategory.CPS032);
/* 		downMsgFacility.sendMessage(msgCd, 
				cardNbr,
				customer.getName(),
				customer.getGender(),
				customer.getMobileNo(),
				new Date(),
				new MapBuilder<String, Object>().add("loanType", loanReg.getLoanType()).add("amt", loanReg.getLoanInitPrin()).add("term", loanReg.getLoanInitTerm())
						.add("loanFee", loanReg.getLoanInitFee().setScale(2, BigDecimal.ROUND_HALF_UP))
						.add("nextPayment", loanReg.getLoanFirstTermPrin().add(loanReg.getLoanFirstTermFee()).setScale(2, BigDecimal.ROUND_HALF_UP))
						.add("loanFixedFee", loanReg.getLoanFixedFee().setScale(2, BigDecimal.ROUND_HALF_UP)).build());
*/	}

	
	
	/**
	 * 判断账户逾期状态
	 * @param time1	toltime月内超过time1次逾期
	 * @param time2	toltime月内连续超过time2次逾期
	 * @param toltime
	 * @return false为未逾期，true为逾期
	 */
	public boolean isOverdue(String PaymentHist, int time1, int time2, int toltime) {
		String payHis = "";
		char u = 'U';// U|还款未达最小还款额
		char n = 'N';// N|未还款
		boolean overdue = false;
		if (PaymentHist == null) PaymentHist = "";
		if (PaymentHist.length() >= toltime) {
			payHis = PaymentHist.substring(0, toltime);
		} else {
			payHis = PaymentHist;
		}
		int times = counterTimes(payHis, u, n);
		if (times >= time1) {
			overdue = true;
		}
		times = counterMaxTimes(payHis, u, n);
		if (times >= time2) {
			overdue = true;
		}
		return overdue;
	}
	
	private int counterTimes(String s, char c, char b) {
		int count = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == c || s.charAt(i) == b) count++;
		}
		return count;
	}
	
	private int counterMaxTimes(String s, char c, char b) {
		int count = 0;
		int maxTimes = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == c || s.charAt(i) == b) {
				count++;
			} else {
				count = 0;
			}
			if (count > maxTimes) {
				maxTimes = count;
			}
		}
		return maxTimes;
	}
}
