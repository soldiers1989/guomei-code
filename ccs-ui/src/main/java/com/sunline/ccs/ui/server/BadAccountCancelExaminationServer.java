/**
 * 
 */
package com.sunline.ccs.ui.server;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ark.support.OrganizationContextHolder;
//import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
//import com.sunline.ccs.infrastructure.server.repos.RCcsAcctO;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctO;
//import com.sunline.ccs.infrastructure.server.repos.RCcsPlan;
import com.sunline.ccs.infrastructure.server.repos.RCcsPlan;
//import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
//import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
//import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
//import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
//import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
//import com.sunline.ccs.infrastructure.shared.model.QCcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcctO;
//import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
//import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.param.def.BlockCode;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.ccs.ui.client.pages.badaccount.BadAccountCancelExaminationConstants;
import com.sunline.ccs.ui.server.commons.BlockCodeUtil;
import com.sunline.ccs.ui.server.commons.CPSBusProvide;
import com.sunline.ccs.ui.server.commons.CPSConstants;
import com.sunline.ccs.ui.server.commons.CheckUtil;
import com.sunline.ccs.ui.shared.PublicConst;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.ark.client.utils.StringUtils;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.api.GlService;
import com.sunline.ppy.dictionary.entity.GlTxnAdj;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ppy.dictionary.enums.IdType;
import com.sunline.ppy.dictionary.enums.PostGlIndicator;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 呆账核销
 * 
 * @author fanghj
 *
 */
@Controller
@RequestMapping(value = "/t3309Server")
public class BadAccountCancelExaminationServer {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

	@PersistenceContext
	private EntityManager em;
	@Autowired
	private RCcsPlan rTmPlan;
	@Autowired
	private RCcsAcct rTmAccount;
	@Autowired
	private RCcsAcctO rTmAccountO;
	@Autowired
	private CPSBusProvide cpsBusProvide;
	@Autowired
	private UnifiedParameterFacility unifiedParameterService;
	@Autowired
	private GlService glService;

	@Autowired
	private OpeLogUtil opeLogUtil;
	@Autowired
	private UnifiedParameterFacility unifiedParameter;

	private QCcsAcct qTmAccount = QCcsAcct.ccsAcct;
	private QCcsAcctO qTmAccountO = QCcsAcctO.ccsAcctO;
	private QCcsPlan qTmPlan = QCcsPlan.ccsPlan;
	private QCcsCustomer qTmCustomer = QCcsCustomer.ccsCustomer;
	private BooleanExpression exp;
	
	@SuppressWarnings("all")
	@ResponseBody()
	@RequestMapping(value = "/getCreditList", method = { RequestMethod.POST })
	public FetchResponse getCreditList (@RequestBody String acctNo,@RequestBody String acctType) {
		//String acctNo = parameters.getString(CcsPlan.P_AcctNo);
		//String acctType = parameters.getString(CcsPlan.P_AcctType);
		AccountType emuAccountType = AccountType.valueOf(acctType);
		JPAQuery query = new JPAQuery(em);
		List<CcsPlan> creditPlans = query.from(qTmPlan).where(qTmPlan.acctNbr.eq(Long.parseLong(acctNo)).and(qTmPlan.acctType.eq(emuAccountType))).list(qTmPlan);
		FetchResponse response = new FetchResponse();
		response.setRows(creditPlans);
		return response;
	}
	
/*	@SuppressWarnings("all")
	@ResponseBody()
	@RequestMapping(value = "/getAcctList", method = { RequestMethod.POST })
	public FetchResponse getAcctList (@RequestBody FetchRequest request) {
		String cardNo = (String)request.getParameter(CcsCardLmMapping.P_CardNbr);
		List<Map<String,Serializable>> response = new ArrayList<Map<String,Serializable>>();
		JPAQuery query = new JPAQuery(em);
		CheckUtil.checkCardNo(cardNo);
		// 查找卡片记录
		CcsCard tmCard = cpsBusProvide.getTmCardTocardNbr(cardNo);
		exp = qTmAccount.acctNbr
				.eq(qTmAccountO.acctNbr)
				.and(qTmAccount.acctType.eq(qTmAccountO.acctType))
				.and(qTmAccount.org.eq(OrganizationContextHolder
				.getCurrentOrg())).and(qTmAccount.acctNbr.eq(tmCard.getAcctNbr()));
		//return query.from(qTmAccount, qTmAccountO).where(exp).list(qTmAccount, qTmAccountO);
		List<Tuple> tuples = query.from(qTmAccount, qTmAccountO).where(exp).list(qTmAccount, qTmAccountO);
		for(Tuple tuple : tuples){
			CcsAcct acct = tuple.get(qTmAccount);
			CcsAcctO acctonline = tuple.get(qTmAccountO);
			Map<String,Serializable> result1 = acct.convertToMap();
			Map<String,Serializable> result2 = acctonline.convertToMap();
			result1.putAll(result2);
			response.add(result1);
		}
		FetchResponse fetch = new FetchResponse();
		fetch.setRows(response);
		return fetch;
	}*/

	/**
	 * 根据查询条件获取账户列表信息
	 */
	@ResponseBody()
	@RequestMapping(value = "/getAcctList", method = { RequestMethod.POST })
//	public List<Map<String, Serializable>> getAcctListV_1 (@RequestBody String cardNo,@RequestBody String idType, @RequestBody String idNo, @RequestBody String telphone) {
	public FetchResponse getAcctListV_1 (@RequestBody FetchRequest request) {
		String cardNbr = (String) request.getParameter(CcsCardLmMapping.P_CardNbr);
		String idType = (String) request.getParameter(CcsCustomer.P_IdType);
		String idNo = (String) request.getParameter(CcsCustomer.P_IdNo);
		String telphone = (String) request.getParameter(CcsCustomer.P_MobileNo);
		// 查询账户信息
		List<Tuple> acctList = new ArrayList<Tuple>();
		JPAQuery query = new JPAQuery(em);
		exp = qTmAccount.acctNbr
				.eq(qTmAccountO.acctNbr)
				.and(qTmAccount.acctType.eq(qTmAccountO.acctType))
				.and(qTmAccount.org.eq(OrganizationContextHolder
						.getCurrentOrg()));

		Map<String, BlockCode> allBlockCodeMap = unifiedParameterService.retrieveParameterObject(BlockCode.class);

		if (StringUtils.isNotEmpty(cardNbr)) { // 根据卡号获取账户列表
			// 检查卡号
			CheckUtil.checkCardNo(cardNbr);

			// 查找卡片记录
			CcsCard tmCard = cpsBusProvide.getTmCardTocardNbr(cardNbr);

			exp = exp.and(qTmAccount.acctNbr.eq(tmCard.getAcctNbr()));
			acctList = query.from(qTmAccount, qTmAccountO).where(exp).list(qTmAccount, qTmAccountO);
			if (acctList.size() == 0) {
				throw new ProcessException("卡号[" + cardNbr + "]查询不到对应的账户信息");
			}
		} else if (StringUtils.isNotEmpty(idType)
				&& StringUtils.isNotBlank(idNo)) { // 根据证件类型和证据号码获取账户列表信息
			// 检查证件类型和证据号码
			CheckUtil.checkId(idType, idNo);
			exp = exp.and(qTmAccount.custId.eq(qTmCustomer.custId)
					.and(qTmCustomer.idType.eq((IdType.valueOf(idType))))
					.and(qTmCustomer.idNo.eq(idNo)));
			acctList = query.from(qTmAccount, qTmAccountO, qTmCustomer)
					.where(exp).list(qTmAccount, qTmAccountO);

			if (acctList.size() == 0) {
				throw new ProcessException("证件类型[" + idType + "],证件号码[" + idNo
						+ "]查询不到对应的账户信息");
			}
		} else if (StringUtils.isNotEmpty(telphone)) { // 根据手机号码获取账户列表信息

			exp = exp.and(qTmAccount.custId.eq(qTmCustomer.custId).and(
					qTmCustomer.mobileNo.eq(telphone)));
			acctList = query.from(qTmAccount, qTmAccountO, qTmCustomer)
					.where(exp).list(qTmAccount, qTmAccountO);

			if (acctList.size() == 0) {
				throw new ProcessException("手机号码[" + telphone + "]查询不到对应的账户信息");
			}
		}

		List<Map<String, Serializable>> acctMapList = new ArrayList<Map<String, Serializable>>();
		for (Tuple objs : acctList) {
			CcsAcct tmAccount = objs.get(qTmAccount);
			CcsAcctO tmAccountO = objs.get(qTmAccountO);

			Map<String, Serializable> acctMap = tmAccountO.convertToMap();
			acctMap.putAll(tmAccount.convertToMap());

			List<Map<String, String>> blockCodeList = new ArrayList<Map<String, String>>();
			BlockCode blockCode = null;
			if (StringUtils.isNotEmpty(tmAccount.getBlockCode())) {
				for (char c : tmAccount.getBlockCode().toCharArray()) {
					blockCode = allBlockCodeMap.get(c+"");
					Map<String, String> blockCodeMap = new HashMap<String, String>();
					blockCodeMap.put("blockcode", String.valueOf(c));
					if(blockCode != null) {
					    blockCodeMap.put("description", blockCode.description);
					} else {
					    blockCodeMap.put("description", "");
					}
					blockCodeList.add(blockCodeMap);
				}
			}

			acctMap.put(PublicConst.KEY_BLOCKCODE_LIST,
					(Serializable) blockCodeList);

			// 相同字段，以TM_ACCOUNT_O数据为准
			acctMap.put(CcsAcctO.P_MemoDb, tmAccountO.getMemoDb());
			acctMap.put(CcsAcctO.P_MemoCash, tmAccountO.getMemoCash());
			acctMap.put(CcsAcctO.P_MemoCr, tmAccountO.getMemoCr());

			// 呆账核销查询
			SysTxnCdMapping txnCdMapping = unifiedParameter.loadParameter(
					SysTxnCd.S65, SysTxnCdMapping.class);
			List<GlTxnAdj> glTxnAdjList = glService.G1003(txnCdMapping.txnCd, null);

			// FIXME 初始化GLS数据 TEST 用
/*			List<GlTxnAdj> glTxnAdjList =new ArrayList<GlTxnAdj>();
			GlTxnAdj glTxnAdj = new GlTxnAdj();
			glTxnAdjList.add(glTxnAdj);
			glTxnAdj.setAdjId(1);
			glTxnAdj.setDbCrInd(DbCrInd.M);
			glTxnAdj.setPostCurrCd("156");
			glTxnAdj.setOwningBranch("123");
			glTxnAdj.setPostAmt(BigDecimal.TEN);
			glTxnAdj.setTxnCode("1235");
			glTxnAdj.setAgeGroup("234");
			glTxnAdj.setBucketType(BucketType.Penalty);
			glTxnAdj.setPlanNbr("1234");
			glTxnAdj.setPostGlInd(PostGlIndicator.S);*/

			acctMap.put(BadAccountCancelExaminationConstants.TOWRITEOFFBADDEBTS,(Serializable)(glTxnAdjList==null?new ArrayList<GlTxnAdj>():glTxnAdjList));

			// 本金余额和费用余额
			String bjAndFy = getBjAndFy(tmAccount.getAcctNbr(),tmAccount.getAcctType());
			acctMap.put(BadAccountCancelExaminationConstants.BJANDFY, bjAndFy);

			acctMapList.add(acctMap);
		}

		FetchResponse fetch = new FetchResponse();
		fetch.setRows(acctMapList);
		return fetch;
	}

	/**
	 * 
	 * @see 方法名：getBjAndFy 
	 * @see 描述：根据账户编号，账户类型查询账户列表中本金余额和费用余额
	 * @see 创建日期：Jun 25, 20154:05:57 PM
	 * @author Liming.Feng
	 *  
	 * @param acctNo
	 * @param accountType
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public String getBjAndFy(Long acctNo, AccountType accountType) {
		String s = new String();
		BigDecimal bjBigDecimal = BigDecimal.ZERO;// 本金余额
		BigDecimal fyBigDecimal = BigDecimal.ZERO;// 费用余额
		List<CcsPlan> tmPlanList = new ArrayList<CcsPlan>();
		tmPlanList = this.getPlanList(acctNo, accountType);

		for (CcsPlan tmPlan : tmPlanList) {
			bjBigDecimal = bjBigDecimal.add(tmPlan.getPastPrincipal()).add(
					tmPlan.getCtdPrincipal());
			fyBigDecimal = fyBigDecimal.add(tmPlan.getPastCardFee())
					.add(tmPlan.getPastOvrlmtFee()).add(tmPlan.getPastLateFee())
					.add(tmPlan.getPastNsfundFee()).add(tmPlan.getPastTxnFee())
					.add(tmPlan.getPastSvcFee()).add(tmPlan.getPastInsurance())
					.add(tmPlan.getPastUserFee1())
					.add(tmPlan.getPastUserFee2())
					.add(tmPlan.getPastUserFee3())
					.add(tmPlan.getPastUserFee4())
					.add(tmPlan.getPastUserFee5())
					.add(tmPlan.getPastUserFee6()).add(tmPlan.getCtdCardFee())
					.add(tmPlan.getCtdOvrlmtFee()).add(tmPlan.getCtdLateFee())
					.add(tmPlan.getCtdNsfundFee()).add(tmPlan.getCtdSvcFee())
					.add(tmPlan.getCtdTxnFee()).add(tmPlan.getCtdInsurance())
					.add(tmPlan.getCtdUserFee1()).add(tmPlan.getCtdUserFee2())
					.add(tmPlan.getCtdUserFee3()).add(tmPlan.getCtdUserFee4())
					.add(tmPlan.getCtdUserFee5()).add(tmPlan.getCtdUserFee6());
		}
		s = bjBigDecimal.toString() + "--" + fyBigDecimal.toString();

		return s;
	}

	public List<CcsPlan> getPlanList(Long acctNo, AccountType acctType) {
		logger.info("getPlanList:acctNo[" + acctNo + "], acctType[" + acctType
				+ "]");
		CheckUtil.rejectNull(acctNo, "账号不允许为空");
		CheckUtil.rejectNull(acctType, "账户类型不允许为空");

		Iterator<CcsPlan> tmPlanIter = rTmPlan.findAll(
				qTmPlan.acctNbr
						.eq(acctNo)
						.and(qTmPlan.acctType.eq(acctType))
						.and(qTmPlan.org.eq(OrganizationContextHolder
								.getCurrentOrg()))).iterator();

		ArrayList<CcsPlan> tmPlanList = new ArrayList<CcsPlan>();
		while (tmPlanIter.hasNext()) {
			CcsPlan tmplan = tmPlanIter.next();
			tmPlanList.add(tmplan);
		}
		return tmPlanList;
	}
	
	/**
	 * 
	 * @see 方法名：saveWriteOffApply 
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：Jun 25, 20154:06:39 PM
	 * @author Liming.Feng
	 *  
	 * @param sysTxnCd
	 * @param dbCrInd
	 * @param selectedCurrCd
	 * @param writeOffAmoutForBJ
	 * @param writeOffAmoutForFy
	 * @param selectedOwningBranch
	 * @param selectedAcctNo
	 * @param selectedAcctType
	 * @param selectedCardNo
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@ResponseBody()
	@RequestMapping(value = "/saveWriteOffApply", method = { RequestMethod.POST })
	public List<GlTxnAdj> saveWriteOffApply(@RequestBody String sysTxnCd,@RequestBody String dbCrInd,
			@RequestBody String selectedCurrCd, @RequestBody String writeOffAmoutForBJ,
			@RequestBody String writeOffAmoutForFy, @RequestBody String selectedOwningBranch,
			@RequestBody String selectedAcctNo, @RequestBody String selectedAcctType,
			@RequestBody String selectedCardNo) {
		logger.info("核销申请开始");
		String[] str = new String[2];
		str[0] = writeOffAmoutForBJ;// 本金核销金额
		str[1] = writeOffAmoutForFy;// 费用核销金额

        // 判断TmAccount 和TmAccountO表是否有呆账核销锁定码，如果有，返回已做呆账核销，如果没有，则设置呆账核销锁定码
        CcsAcct tmAccount = cpsBusProvide.getTmAccountTocardNbr(
                selectedCardNo, AccountType.valueOf(selectedAcctType));
        CcsAcctO tmAccountO = cpsBusProvide.getTmAccountOTocardNbr (
                selectedCardNo, AccountType.valueOf(selectedAcctType));
        if (BlockCodeUtil.hasBlockCode(tmAccount.getBlockCode(),
                CPSConstants.BLOCKCODE_W)) {
            logger.error("卡号:[" + selectedCardNo + "],账户类型:["
                    + selectedAcctType + "],已做过呆账核销申请");
            throw new ProcessException("卡号:[" + selectedCardNo + "],账户类型:["
                    + selectedAcctType + "],已做过呆账核销申请");
        } else {
            tmAccount.setBlockCode(BlockCodeUtil.addBlockCode(
                    tmAccount.getBlockCode(), CPSConstants.BLOCKCODE_W));
            rTmAccount.save(tmAccount);
        }
        if (BlockCodeUtil.hasBlockCode(tmAccountO.getBlockCode(),
                CPSConstants.BLOCKCODE_W)) {
            logger.error("卡号:[" + selectedCardNo + "],账户类型:["
                    + selectedAcctType + "],已做过呆账核销申请");
            throw new ProcessException("卡号:[" + selectedCardNo + "],账户类型:["
                    + selectedAcctType + "],已做过呆账核销申请");
        } else {
            tmAccountO.setBlockCode(BlockCodeUtil.addBlockCode(
                    tmAccountO.getBlockCode(), CPSConstants.BLOCKCODE_W));
            rTmAccountO.save(tmAccountO);
        }

		List<GlTxnAdj> glTxnAdjList = new ArrayList<GlTxnAdj>();

		SysTxnCdMapping txnCdMapping = unifiedParameter.loadParameter(sysTxnCd, SysTxnCdMapping.class);
		// 反射模拟自动注入
//		CommonAutowired<SysTxnCdMapping> autowired = new CommonAutowired<SysTxnCdMapping>();
//		SysTxnCdMapping txnCdMapping = autowired.simulator(SysTxnCdMapping.class, "txnCd", "L3");
		// 反射模拟自动注入

		for (int i = 0; i < str.length; i++) {
			if (!"".equals(str[i])) {
				GlTxnAdj glTxnAdj = new GlTxnAdj();
				glTxnAdj.setDbCrInd(DbCrInd.valueOf(dbCrInd));// 借贷记标志
				glTxnAdj.setPostCurrCd(selectedCurrCd);// 币种
				glTxnAdj.setPostAmt(new BigDecimal(str[i]));// 金额
				glTxnAdj.setOwningBranch(selectedOwningBranch);// 发卡网点
				glTxnAdj.setTxnCode(txnCdMapping.txnCd);// 交易码
				// glTxnAdj.setTxnCode(sysTxnCd.toString());

				glTxnAdj.setAgeGroup("W");// 账龄组
				// glTxnAdj.setBucketType(BucketType.Pricinpal);//余额成分
				glTxnAdj.setBucketType(i == 0 ? BucketType.Pricinpal
						: BucketType.TXNFee);// 余额成分
				glTxnAdj.setPlanNbr("999999");// 信用计划号
				glTxnAdj.setOrg(OrganizationContextHolder.getCurrentOrg());// 机构号
				glTxnAdj.setPostGlInd(PostGlIndicator.N);// 总账入账标志
				glTxnAdj.setAcctNo(Integer.parseInt(selectedAcctNo));// 账户编号
				glTxnAdj.setAcctType(AccountType.valueOf(selectedAcctType));// 账户类型
				glTxnAdj.setCardNo(selectedCardNo);// 介质卡号

				glTxnAdjList.add(glTxnAdj);
			}
		}

		glService.G1007(glTxnAdjList);

		// SysTxnCdMapping txnCdMapping =
		// unifiedParameter.loadParameter(SysTxnCd.S65, SysTxnCdMapping.class);
		List<GlTxnAdj> glTxnAdjListOne = glService.G1003(txnCdMapping.txnCd,
				null);

		// FIXME TEST 用
//		List<GlTxnAdj> glTxnAdjListOne = glTxnAdjList;

		logger.info("核销申请结束，返回新增值");
		return glTxnAdjListOne;
		// TODO Auto-generated method stub

	}

}
