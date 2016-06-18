package com.sunline.ccs.batch.rpt.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.pcm.param.def.FinancialOrg;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.Ownership;
import com.sunline.ppy.dictionary.exception.ProcessException;

@Service
public class RptParamFacility {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	@Autowired
	private RptBatchUtil rptBatchUtil;
	/**
	 * 随借随还转出放款交易码
	 */
	public final String MCAT_LOANING_TXN_CD = "L835";
	
	/**
	 * 合作机构代码 - 阳光保险
	 */
	public final String YG_ACQ_ACCEPTOR_ID = "00130000";
	
	public <T extends Serializable> T retrieveParam(String key, Class<T> clazz){
		rptBatchUtil.setCurrOrgNoToContext();
		return unifiedParameterFacility.retrieveParameterObject(key, clazz);
	}
	
	public <T extends Serializable> T retrieveParam(String key, Class<T> clazz, String org){
		rptBatchUtil.setCurrOrgNoToContext(org);
		return unifiedParameterFacility.retrieveParameterObject(key, clazz);
	}
	
	public <T extends Serializable> T loadParameter(Object key, Class<T> clazz){
		rptBatchUtil.setCurrOrgNoToContext();
		return unifiedParameterFacility.loadParameter(key, clazz);
	}
	public <T extends Serializable> T loadParameter(Object key, Class<T> clazz, String org){
		OrganizationContextHolder.setCurrentOrg(org);
		return unifiedParameterFacility.loadParameter(key, clazz);
	}
	public List<String> loadProductCdList(Ownership ownerShip, LoanType loanType, String acceptorId){
		rptBatchUtil.setCurrOrgNoToContext();
		return loadProductAndLoanCdList(ownerShip, loanType, acceptorId).productCdList;
	}
	
	public List<String>	loadLoanCdList(Ownership ownerShip, LoanType loanType, String acceptorId){
		rptBatchUtil.setCurrOrgNoToContext();
		return loadProductAndLoanCdList(ownerShip, loanType, acceptorId).loanCdList;
	}
	
	public Product loadProduct(String productCd) {
		rptBatchUtil.setCurrOrgNoToContext();
		return unifiedParameterFacility.loadParameter(productCd, Product.class);
	}

	public LoanPlan loadLoanPlan(String loanCode) {
		rptBatchUtil.setCurrOrgNoToContext();
		return unifiedParameterFacility.loadParameter(loanCode, LoanPlan.class);
	}

	public ProductCredit loadProductCredit(String productCd) {
		rptBatchUtil.setCurrOrgNoToContext();
		return unifiedParameterFacility.loadParameter(productCd, ProductCredit.class);
	}
	
	private ProductLoanPlanListInfo loadProductAndLoanCdList(Ownership ownerShip, LoanType loanType, String acceptorId){
		ProductLoanPlanListInfo listInfo = new ProductLoanPlanListInfo();
		if(ownerShip == null || loanType == null){
			throw new ProcessException("产品所属，贷款类型为必须值");
		}
		if(Ownership.O.equals(ownerShip)){
			logger.info("获取自有产品贷款类型[{}][{}]贷款参数码列表", loanType.getDescription(), loanType.name());
			Map<String, LoanPlan> loanPlanMap = unifiedParameterFacility.retrieveParameterObject(LoanPlan.class);
			
			for(LoanPlan itemLoanPlan : loanPlanMap.values()){
				logger.info("LoanPlan[{}][{}]", itemLoanPlan.loanCode, itemLoanPlan.description);
				if(!(LoanType.MCAT.equals(itemLoanPlan.loanType) || LoanType.MCEI.equals(itemLoanPlan.loanType) || LoanType.MCEP.equals(itemLoanPlan.loanType))){
					continue;
				}
				if(ownerShip.equals(itemLoanPlan.ownership) && loanType.equals(itemLoanPlan.loanType)){
					ProductCredit productCredit = unifiedParameterFacility.loadParameter(itemLoanPlan.productCode, ProductCredit.class);
					listInfo.productCdList.add(productCredit.productCd);
					listInfo.loanCdList.add(itemLoanPlan.loanCode);
					logger.info("ProductCd[{}]LoanPlan[{}][{}]", productCredit.productCd, itemLoanPlan.loanCode, itemLoanPlan.description);
				}
			}
		}else if(Ownership.P.equals(ownerShip) && YG_ACQ_ACCEPTOR_ID.equals(acceptorId)){
			FinancialOrg financialOrg = null;
			
			logger.info("获取合作产品贷款类型[{}][{}]acceptorId[{}]贷款参数码列表", loanType.getDescription(), loanType.name(), acceptorId);
			if(StringUtils.isBlank(acceptorId)){
				throw new IllegalArgumentException("合作机构代码为必须值");
			}
			Map<String, FinancialOrg> orgMap = unifiedParameterFacility.retrieveParameterObject(FinancialOrg.class);
			for(FinancialOrg o : orgMap.values()){
				if(acceptorId.equals(o.acqAcceptorId)){
					financialOrg = o;
				}
			}
			Map<String, LoanPlan> loanPlanMap = unifiedParameterFacility.retrieveParameterObject(LoanPlan.class);
			for(LoanPlan itemLoanPlan : loanPlanMap.values()){
				if(!(LoanType.MCAT.equals(itemLoanPlan.loanType) || LoanType.MCEI.equals(itemLoanPlan.loanType) || LoanType.MCEP.equals(itemLoanPlan.loanType))){
					continue;
				}
				if(itemLoanPlan.ownership.equals(ownerShip) && itemLoanPlan.loanType.equals(loanType)){
					ProductCredit productCredit = unifiedParameterFacility.loadParameter( itemLoanPlan.productCode, ProductCredit.class);
					if(financialOrg.financialOrgNO.equals(productCredit.financeOrgNo)){
						listInfo.productCdList.add(productCredit.productCd);
						listInfo.loanCdList.add(itemLoanPlan.loanCode);
						logger.info("ProductCd[{}]LoanPlan[{}][{}]", productCredit.productCd, itemLoanPlan.loanCode, itemLoanPlan.description);
					}
				}
			}
		}
		
		return listInfo;
	}
	
/*	public List<String> getExcludeTxnCd(String org){
		
		if(StringUtils.isBlank(org))
			rptBatchUtil.setCurrOrgNoToContext();
		else
			rptBatchUtil.setCurrOrgNoToContext(org);
		
		List<String> excludeTxnCdList = new ArrayList<String>();
		for(SysTxnCd sysTxnCd : getSysTxnCdList()){
			SysTxnCdMapping txnCdMapping = unifiedParameterFacility.retrieveParameterObject(sysTxnCd.toString(), SysTxnCdMapping.class);
			if(txnCdMapping != null){
				excludeTxnCdList.add(txnCdMapping.txnCd);
			}
		}
		return excludeTxnCdList;
	}
	private List<SysTxnCd> getSysTxnCdList(){
		List<SysTxnCd> list = new ArrayList<SysTxnCd>();
		list.add(SysTxnCd.S74);//等额本息(金)罚金贷调
		list.add(SysTxnCd.S75);//等额本息(金)罚金贷调
		list.add(SysTxnCd.S76);//寿险计划包费贷调
		list.add(SysTxnCd.S96);//逾期提前结清或者理赔利息回溯
		list.add(SysTxnCd.S10);//随借随还滞纳金贷调
		list.add(SysTxnCd.S78);//服务费贷调
		list.add(SysTxnCd.C01);//随借随还交易费贷调
		list.add(SysTxnCd.C02);//年费贷调
		list.add(SysTxnCd.C03);//等额本息(金)利息贷调
		list.add(SysTxnCd.C05);//等额本息(金)罚息贷调
		list.add(SysTxnCd.C07);//等额本息(金)本金贷调
		list.add(SysTxnCd.C08);//随借随还本金贷调
		list.add(SysTxnCd.C09);//代收付服务费贷调
		
		return list;
	}*/
	
	class ProductLoanPlanListInfo{
		public List<String> productCdList = new ArrayList<String>();
		public List<String> loanCdList = new ArrayList<String>();
	}


}
