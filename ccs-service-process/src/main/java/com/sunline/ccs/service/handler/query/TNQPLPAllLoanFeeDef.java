package com.sunline.ccs.service.handler.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.msdentity.STNQPLPAllLoanFeeDefReq;
import com.sunline.ccs.service.msdentity.STNQPLPAllLoanFeeDefResp;
import com.sunline.ccs.service.msdentity.STNQPLPAllLoanFeeDefSubLoanFeeDef;
import com.sunline.ccs.service.msdentity.STNQPLPAllLoanFeeDefSubLoanPlan;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ccs.service.util.DateFormatUtil;
import com.sunline.pcm.param.def.FinancialOrg;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 贷款子产品列表查询
 * 
 * @author zhengjf
 * 
 */
@Service
public class TNQPLPAllLoanFeeDef {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	UnifiedParameterFacility unifiedParameterService;
	@Autowired
	AppapiCommService appapiCommService;
	@Autowired
	UnifiedParameterFacility unifiedParameterFacility;

	public STNQPLPAllLoanFeeDefResp handler(STNQPLPAllLoanFeeDefReq req) {
		log.debug("贷款子产品列表查询(金融匹配):" + req);
		TxnInfo txnInfo = new TxnInfo();
		// 定义所有要使用变量
		STNQPLPAllLoanFeeDefResp resp = new STNQPLPAllLoanFeeDefResp();
		List<STNQPLPAllLoanFeeDefSubLoanPlan> loanPlanList = new ArrayList<STNQPLPAllLoanFeeDefSubLoanPlan>();
		Map<String, LoanPlan> productMap = unifiedParameterService
				.retrieveParameterObject(LoanPlan.class);

		try {

			// 增加合作机构编号判断如果为空则执行原来的逻辑
			if (StringUtils.isBlank(req.getIdAcq())) {

				// 如果传进来的loancode为空则查找全部的产品
				if (req.getLoanCode() == null) {
					for (String loanCode : productMap.keySet()) {
						log.debug("产品Code:" + loanCode);
						LoanPlan loanPlan = productMap.get(loanCode);
						if (LoanType.MCAT != loanPlan.loanType
								&& LoanType.MCEI != loanPlan.loanType
								&& LoanType.MCEP != loanPlan.loanType) {
							continue;
						}
						STNQPLPAllLoanFeeDefSubLoanPlan subLoanPlan = new STNQPLPAllLoanFeeDefSubLoanPlan();
						subLoanPlan = instSubLoanPlan(subLoanPlan, loanPlan);
						subLoanPlan.setLoanFeeDefList(getSubLoanFeeDefList(
								loanPlan.loanFeeDefMap, req));
						loanPlanList.add(subLoanPlan);
					}

				} else {
					STNQPLPAllLoanFeeDefSubLoanPlan subLoanPlan = new STNQPLPAllLoanFeeDefSubLoanPlan();
					LoanPlan loanPlan = productMap.get(req.loanCode);
					if (LoanType.MCAT != loanPlan.loanType
							&& LoanType.MCEI != loanPlan.loanType
							&& LoanType.MCEP != loanPlan.loanType) {
						throw new ProcessException(MsRespCode.E_1052.getCode(),
								"所查产品 非随借随还/等额本息/等额本金的产品");
					}
					subLoanPlan = instSubLoanPlan(subLoanPlan, loanPlan);
					subLoanPlan.setLoanFeeDefList(getSubLoanFeeDefList(
							loanPlan.loanFeeDefMap, req));
					loanPlanList.add(subLoanPlan);
				}
				resp.setLoanPlanList(loanPlanList);
			} else {
				String acceptorId = req.getIdAcq();
				Map<String, FinancialOrg> orgMap = unifiedParameterFacility
						.retrieveParameterObject(FinancialOrg.class);
				FinancialOrg financialOrg = null;
				for (FinancialOrg o : orgMap.values()) {
					if (acceptorId.equals(o.acqAcceptorId)) {
						financialOrg = o;
						break;
					}
				}
				if (financialOrg == null) {
					throw new ProcessException(MsRespCode.E_1008.getCode(),
							"合作机构编号输入有误");
				}
				Map<String, ProductCredit> proMap = unifiedParameterFacility
						.retrieveParameterObject(ProductCredit.class);
				List<ProductCredit> productCreditList = new ArrayList<ProductCredit>();
				String numb = financialOrg.financialOrgNO;
				for (ProductCredit p : proMap.values()) {
					if (numb.equals(p.financeOrgNo)) {
						productCreditList.add(p);
					}
				}
				for (ProductCredit pc : productCreditList) {
					LoanPlan loanPlan = null;
					String loanCd = null;
					Map<LoanType, String> lpMap = pc.loanPlansMap;
					loanCd = lpMap.get(pc.defaultLoanType);
					if (StringUtils.isNotBlank(req.getLoanCode())
							&& !req.getLoanCode().equals(loanCd)) {
						continue;
					}
					loanPlan = unifiedParameterFacility.loadParameter(loanCd,
							LoanPlan.class);
					Map<Integer, LoanFeeDef> lfdMap = loanPlan.loanFeeDefMap;
					STNQPLPAllLoanFeeDefSubLoanPlan subLoanPlan = new STNQPLPAllLoanFeeDefSubLoanPlan();
					subLoanPlan = instSubLoanPlan(subLoanPlan, loanPlan);
					subLoanPlan.setLoanFeeDefList(getSubLoanFeeDefList(lfdMap,
							req));
					loanPlanList.add(subLoanPlan);
				}
				resp.setLoanPlanList(loanPlanList);
			}
		} catch (ProcessException pe) {
			if (log.isErrorEnabled())
				log.error(pe.getMessage(), pe);
			appapiCommService.preException(pe, pe, txnInfo);
		} catch (Exception e) {
			if (log.isErrorEnabled())
				log.error(e.getMessage(), e);
			appapiCommService.preException(e, null, txnInfo);
		} finally {
			LogTools.printLogger(log, "STNQAAcctsbyCustUUIDResp", "合同列表查询",
					resp, false);
		}
		setResponse(resp, txnInfo);
		return resp;
	}

	/**
	 * 对subLoanFeeDef进行赋值
	 * 
	 * @param subLoanFeeDef
	 * @param loanFeeDef
	 * @return
	 */
	public STNQPLPAllLoanFeeDefSubLoanFeeDef instSubLoanFeeDef(
			STNQPLPAllLoanFeeDefSubLoanFeeDef subLoanFeeDef,
			LoanFeeDef loanFeeDef) {

		subLoanFeeDef.setInitTerm(loanFeeDef.initTerm);
		subLoanFeeDef.setLoanFeeDefId(loanFeeDef.loanFeeDefId);
		subLoanFeeDef.setLoanFeeDefStatus(loanFeeDef.loanFeeDefStatus);
		subLoanFeeDef.setMinAmount(loanFeeDef.minAmount);
		subLoanFeeDef.setMaxAmount(loanFeeDef.maxAmount);

		return subLoanFeeDef;
	}

	/**
	 * 对subLoanPlan进行赋值
	 * 
	 * @param subLoanFeeDef
	 * @param loanFeeDef
	 * @return
	 */
	public STNQPLPAllLoanFeeDefSubLoanPlan instSubLoanPlan(
			STNQPLPAllLoanFeeDefSubLoanPlan subLoanPlan, LoanPlan loanPlan) {

		subLoanPlan.setLoanCode(loanPlan.loanCode);
		subLoanPlan.setDesc(loanPlan.description);
		subLoanPlan.setLoantype(loanPlan.loanType);
		subLoanPlan.setLoanPlanValidity(DateFormatUtil
				.format(loanPlan.loanValidity));
		subLoanPlan.setLoanPlanStatus(loanPlan.loanStaus);

		return subLoanPlan;
	}

	public List<STNQPLPAllLoanFeeDefSubLoanFeeDef> getSubLoanFeeDefList(
			Map<Integer, LoanFeeDef> loanFeeDefMap, STNQPLPAllLoanFeeDefReq req) {
		List<STNQPLPAllLoanFeeDefSubLoanFeeDef> loanFeeDefList = new ArrayList<STNQPLPAllLoanFeeDefSubLoanFeeDef>();
		for (Integer term : loanFeeDefMap.keySet()) {
			STNQPLPAllLoanFeeDefSubLoanFeeDef subLoanFeeDef = new STNQPLPAllLoanFeeDefSubLoanFeeDef();

			LoanFeeDef loanFeeDef = loanFeeDefMap.get(term);
			// 如果传入金额不为空
			if (req.loanAmt != null) {
				if (req.loanAmt.compareTo(loanFeeDef.minAmount) >= 0
						&& req.loanAmt.compareTo(loanFeeDef.maxAmount) <= 0) {
					subLoanFeeDef = instSubLoanFeeDef(subLoanFeeDef, loanFeeDef);
					loanFeeDefList.add(subLoanFeeDef);
				}
			} else {
				subLoanFeeDef = instSubLoanFeeDef(subLoanFeeDef, loanFeeDef);
				loanFeeDefList.add(subLoanFeeDef);
			}
		}
		return loanFeeDefList;
	}

	/**
	 * 组装响应报文
	 * 
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(MsResponseInfo resp, TxnInfo txnInfo) {

		resp.setErrorCode(txnInfo.getResponsCode());
		resp.setErrorMessage(txnInfo.getResponsDesc());
		if (StringUtils.equals(MsRespCode.E_0000.getCode(),
				txnInfo.getResponsCode())) {
			resp.setStatus("S");// 交易成功
		} else {
			resp.setStatus("F");// 交易失败
		}
	}
}
