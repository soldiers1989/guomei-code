/**
 * 
 */
package com.sunline.ccs.ui.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
//import com.sunline.ccs.infrastructure.server.repos.RTmPlan;
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
import com.sunline.ccs.ui.server.commons.CPSBusProvide;
import com.sunline.ccs.ui.server.commons.CheckUtil;
import com.sunline.ccs.ui.shared.PublicConst;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.ark.client.utils.StringUtils;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.IdType;

/**
 * 账户信息查询
* @author fanghj
 *
 */
@Controller
@RequestMapping(value="/accountInfoServer")
public class AccountInfoServer  {
	private static final long serialVersionUID = 1L;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private RCcsPlan rTmPlan;
	@Autowired
	private CPSBusProvide cpsBusProvide;
	@Autowired
	private UnifiedParameterFacility unifiedParameterService;
	
	@Autowired
	private OpeLogUtil opeLogUtil;
	
	private QCcsAcct qTmAccount = QCcsAcct.ccsAcct;
	private QCcsAcctO qTmAccountO = QCcsAcctO.ccsAcctO;
	private QCcsPlan qTmPlan = QCcsPlan.ccsPlan;
	private QCcsCustomer qTmCustomer = QCcsCustomer.ccsCustomer;
	private BooleanExpression exp;
	
	/**
	 * 根据查询条件获取账户列表信息
	 */
	@SuppressWarnings("rawtypes")
	/*
	 * @RequestBody String cardNbr,
			@RequestBody String idType, @RequestBody String idNo,@RequestBody String telphone
	 */
	@ResponseBody()
	@RequestMapping(value="/getAcctList",method={RequestMethod.POST})
	public FetchResponse getAcctList(@RequestBody FetchRequest request) throws FlatException{
		String contrNbr = (String)request.getParameter(CcsAcct.P_ContrNbr);
		String idType = (String)request.getParameter(CcsCustomer.P_IdType);
		String idNo = (String)request.getParameter(CcsCustomer.P_IdNo);
		String telphone = (String)request.getParameter(CcsCustomer.P_MobileNo);
		String guarantyId=(String)request.getParameter(CcsAcct.P_GuarantyId);
		List<Tuple> acctList = new ArrayList<Tuple>();
		
		if("null".equals(idType)||idType==null) idType=null;
		if("null".equals(idNo)||idNo==null) idNo=null;
		if("null".equals(telphone)||telphone==null) telphone=null;		
		if("null".equals(guarantyId)||guarantyId==null) guarantyId=null;
		if("null".equals(contrNbr)||contrNbr==null) contrNbr=null;

		//卡号不再使用
		String cardNbr=null;
		
		JPAQuery query = new JPAQuery(em);
		exp = qTmAccount.acctNbr.eq(qTmAccountO.acctNbr)
				.and(qTmAccount.acctType.eq(qTmAccountO.acctType))
				.and(qTmAccount.org.eq(OrganizationContextHolder.getCurrentOrg()));
		Map<String, BlockCode> allBlockCodeMap = new HashMap<String,BlockCode>();
		allBlockCodeMap=unifiedParameterService.retrieveParameterObject(BlockCode.class);
		if(StringUtils.isNotEmpty(cardNbr)){ //根据卡号获取账户列表
			//检查卡号
			CheckUtil.checkCardNo(cardNbr);

			//查找卡片记录
			CcsCard tmCard = cpsBusProvide.getTmCardTocardNbr(cardNbr);
			
			exp = exp.and(qTmAccount.acctNbr.eq(tmCard.getAcctNbr()));
			acctList = query.from(qTmAccount, qTmAccountO).where(exp)
					.list(qTmAccount, qTmAccountO);
			
			if (acctList.size() == 0){
				throw new FlatException("卡号[" + cardNbr +"]查询不到对应的账户信息");
			}
		}else if(StringUtils.isNotEmpty(idType) && StringUtils.isNotBlank(idNo)){ //根据证件类型和证据号码获取账户列表信息
			//检查证件类型和证据号码
			CheckUtil.checkId(idType, idNo);
			exp = exp.and(qTmAccount.custId.eq(qTmCustomer.custId)
					.and(qTmCustomer.idType.eq((IdType.valueOf(idType))))
					.and(qTmCustomer.idNo.eq(idNo)));
			acctList = query.from(qTmAccount, qTmAccountO, qTmCustomer).where(exp)
					.list(qTmAccount, qTmAccountO);
			
			if (acctList.size() == 0){
				throw new FlatException("证件类型["+ idType +"],证件号码["+idNo+"]查询不到对应的账户信息");
			}
		}else if(StringUtils.isNotEmpty(telphone)){ //根据手机号码获取账户列表信息
			
			exp = exp.and(qTmAccount.custId.eq(qTmCustomer.custId)
					.and(qTmCustomer.mobileNo.eq(telphone)));
			acctList = query.from(qTmAccount, qTmAccountO, qTmCustomer).where(exp)
					.list(qTmAccount, qTmAccountO);
			
			if (acctList.size() == 0){
				throw new FlatException("手机号码["+ telphone +"]查询不到对应的账户信息");
			}
		}else if(StringUtils.isNotEmpty(guarantyId)){
			exp = exp.and(qTmAccount.custId.eq(qTmCustomer.custId)
					.and(qTmAccount.guarantyId.eq(guarantyId)));
			acctList = query.from(qTmAccount, qTmAccountO, qTmCustomer).where(exp)
					.list(qTmAccount, qTmAccountO);
			if (acctList.size() == 0){
				throw new FlatException("保单号["+ guarantyId +"]查询不到对应的账户信息");
			}
		}else if(StringUtils.isNotEmpty(contrNbr)){
			exp = exp.and(qTmAccount.custId.eq(qTmCustomer.custId)
					.and(qTmAccount.contrNbr.eq(contrNbr)));
			acctList = query.from(qTmAccount, qTmAccountO, qTmCustomer).where(exp)
					.list(qTmAccount, qTmAccountO);
			if (acctList.size() == 0){
				throw new FlatException("合同号["+ contrNbr +"]查询不到对应的账户信息");
			}
		}
		
		List<Map<String, Serializable>> acctMapList = new ArrayList<Map<String, Serializable>>();
		for(Tuple objs : acctList) {
			CcsAcct tmAccount = objs.get(qTmAccount);
			CcsAcctO tmAccountO = objs.get(qTmAccountO);
			
			Map<String, Serializable> acctMap = tmAccountO.convertToMap();
			acctMap.putAll(tmAccount.convertToMap());
			
			List<Map<String,String>> blockCodeMapList = new ArrayList<Map<String,String>>();
			BlockCode blockCode = null;
			if (StringUtils.isNotEmpty(tmAccount.getBlockCode())) {
				for (char c : tmAccount.getBlockCode().toCharArray()) {
					Map<String, String> blockCodeList = new LinkedHashMap<String, String>();
					blockCode = allBlockCodeMap.get(c+"");
					//需要与前端字段相匹配
					if(blockCode != null) {
						blockCodeList.put("blockcode",String.valueOf(c));
						blockCodeList.put("description", blockCode.description);
						blockCodeMapList.add(blockCodeList);
					} else {
						blockCodeList.put("blockcode",String.valueOf(c));
						blockCodeList.put("description","");
						blockCodeMapList.add(blockCodeList);

					}
				}
			}
			
			acctMap.put(PublicConst.KEY_BLOCKCODE_LIST,(Serializable) blockCodeMapList);
			
			//相同字段，以TM_ACCOUNT_O数据为准
			acctMap.put(CcsAcctO.P_MemoDb, tmAccountO.getMemoDb());
			acctMap.put(CcsAcctO.P_MemoCash, tmAccountO.getMemoCash());
			acctMap.put(CcsAcctO.P_MemoCr, tmAccountO.getMemoCr());
			
			
			acctMapList.add(acctMap);
		}
		FetchResponse response = new FetchResponse();
		response.setRows(acctMapList);
		return response;
	}
	//@RequestBody String acctNbr,@RequestBody AccountType acctType
	@ResponseBody()
	@RequestMapping(value="/getPlanList",method={RequestMethod.POST})
	public FetchResponse getPlanList(@RequestBody FetchRequest request) throws FlatException {
		String acctNbr = (String)request.getParameter(CcsAcct.P_AcctNbr);
		String acctType = (String)request.getParameter(CcsAcct.P_AcctType);
		logger.info("getPlanList:acctNbr[" + acctNbr + "], acctType[" + acctType + "]");
		CheckUtil.rejectNull(acctNbr, "账号不允许为空");
		CheckUtil.rejectNull(acctType, "账户类型不允许为空");
		Long varAcctNbr = Long.parseLong(acctNbr);
		Iterator<CcsPlan> tmPlanIter = rTmPlan.findAll(qTmPlan.acctNbr.eq(varAcctNbr)
				.and(qTmPlan.acctType.eq(AccountType.valueOf(acctType)))
				.and(qTmPlan.org.eq(OrganizationContextHolder.getCurrentOrg()))).iterator();
		
		ArrayList<CcsPlan> tmPlanList = new ArrayList<CcsPlan>();
		while (tmPlanIter.hasNext()) {
			CcsPlan tmplan = tmPlanIter.next();
			tmPlanList.add(tmplan);
		}
		Collections.sort(tmPlanList,new Comparator<CcsPlan>() {

			@Override
			public int compare(CcsPlan o1, CcsPlan o2) {
				return o1.getPlanId().compareTo(o2.getPlanId());
			}
			
		});
		List<Map<String, Serializable>> planMapList = new ArrayList<Map<String, Serializable>>();
		for(CcsPlan plan:tmPlanList){
			Map<String, Serializable> planMap = plan.convertToMap();
			planMapList.add(planMap);
		}
		FetchResponse response = new FetchResponse();
		response.setRows(planMapList);
		return response;
	}
}
