/**
 * 
 */
package com.sunline.ccs.ui.server;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.api.GlService;
import com.sunline.ppy.dictionary.entity.GlTxnAdj;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ppy.dictionary.enums.PostGlIndicator;

/**
 * 
 * @see 类名：ExtractionServer
 * @see 描述：计提申请
 *
 * @see 创建日期：   Jul 1, 201510:11:24 AM
 * @author tangls
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@SuppressWarnings("all")
@Controller
@RequestMapping(value="/t3308Server")
public class ExtractionServer  {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());//日志对象。
	
	@Autowired
	private GlService glService;
	
	@Autowired
	private UnifiedParameterFacility unifiedParameter;
	
	/**
	 * 查询当日计提申请列表
	 */
	@ResponseBody()
	@RequestMapping(value="/getGlTxnAdjList",method={RequestMethod.POST})
	public List<GlTxnAdj> getGlTxnAdjList(@RequestBody SysTxnCd sysTxnCd,
			@RequestBody String theCardNetwork) {
		log.info("查询当日申请列表开始");
		SysTxnCdMapping txnCdMapping = unifiedParameter.loadParameter(sysTxnCd.toString(), SysTxnCdMapping.class);
//		glTxnAdj.setTxnCode(txnCdMapping.txnCd);//交易码
		List<GlTxnAdj> glTxnAdjList =  glService.G1003(txnCdMapping.txnCd, theCardNetwork);
		log.info("查询当日申请列表结束");
		return glTxnAdjList;
	}
	/**
	 * 查询账龄组列表
	 */
	@ResponseBody()
	@RequestMapping(value="/getAgeGroupList",method={RequestMethod.POST})
	public Map<String, String> getAgeGroupList() throws FlatException {
		log.info("查询账龄组列表开始");
		Map<String, String> ageGroupMap =  glService.G1005();
		log.info("查询账龄组列表结束");
		return ageGroupMap;
	}
	/**
	 * 计提试算
	 */
	@ResponseBody()
	@RequestMapping(value="/provisionForTrial",method={RequestMethod.POST})
	public Map<String, BigDecimal> provisionForTrial(@RequestBody Map<String, BigDecimal> map)
			throws FlatException {
		log.info("计提试算开始");
		Map<String, BigDecimal> provisionForTrialMap = glService.G1006(map);
		log.info("计提试算结束");
		return provisionForTrialMap;
	}
	/**
	 * 计提/解缴/核销申请开始
	 * 返回增加后的list列表
	 */
	@ResponseBody()
	@RequestMapping(value="/provisionForApply",method={RequestMethod.POST})
	public List<GlTxnAdj> provisionForApply(@RequestBody Map map) {
		String sysTxnCd=(String) map.get("txnCd");
		DbCrInd dbCrInd=(DbCrInd) map.get("dbcrInd");
		String currency=(String) map.get("currcy");
		String theCardNetwork=(String) map.get("cardNetWorkItem");
		 BigDecimal total=(BigDecimal) map.get("total");
		log.info("计提/解缴申请开始");
//		System.out.println("=========================="+sysTxnCd);
		GlTxnAdj glTxnAdj = new GlTxnAdj();
		glTxnAdj.setDbCrInd(dbCrInd);//借贷记标志
		glTxnAdj.setPostCurrCd(currency);//币种
		glTxnAdj.setPostAmt(total);//金额
		glTxnAdj.setOwningBranch(theCardNetwork);//发卡网点
		
//		SysTxnCdMapping txnCdMapping = unifiedParameter.loadParameter(sysTxnCd.toString(), SysTxnCdMapping.class);
		SysTxnCdMapping txnCdMapping = unifiedParameter.loadParameter(sysTxnCd.toString(), SysTxnCdMapping.class);

		glTxnAdj.setTxnCode(txnCdMapping.txnCd);//交易码
		
//		glTxnAdj.setTxnCode(sysTxnCd.toString());
		
		glTxnAdj.setAgeGroup("W");//账龄组
		glTxnAdj.setBucketType(BucketType.Pricinpal);//余额成分
		
		glTxnAdj.setPlanNbr("999999");//信用计划号
		
		glTxnAdj.setOrg(OrganizationContextHolder.getCurrentOrg());//机构号
		glTxnAdj.setPostGlInd(PostGlIndicator.N);//总账入账标志
		
//		glTxnAdj.setAcctNo(acctNo);//账户编号
//		glTxnAdj.setAcctType(acctType);//账户类型
//		glTxnAdj.setBizDate(bizDate);//交易日期
//		glTxnAdj.setCardNo(cardNo);//介质卡号
//		glTxnAdj.setTxnDesc(txnDesc);//交易描述
		
		glService.G1001(glTxnAdj);
		List<GlTxnAdj> glTxnAdjList =  glService.G1003(txnCdMapping.txnCd, null);
		log.info("计提/解缴申请结束");
		return glTxnAdjList;
		
	}
	/**
	 * 解缴申请
	 */
	@ResponseBody()
	@RequestMapping(value="/remitForApply",method={RequestMethod.POST})
	public void remitForApply(@RequestBody GlTxnAdj tmGlTxnAdj) {
		log.info("解缴申请开始");
		GlTxnAdj glTxnAdj = new GlTxnAdj();
		glService.G1002(glTxnAdj);
		log.info("解缴申请结束");
	}
	/**
	 * 删除记录
	 */
	@ResponseBody()
	@RequestMapping(value="/deleteRecord",method={RequestMethod.POST})
	public List<GlTxnAdj> deleteRecord(@RequestBody GlTxnAdj tmGlTxnAdj){
		log.info("删除记录开始!");
		glService.G1004(tmGlTxnAdj);
		log.info("删除记录结束!");
		List<GlTxnAdj> glTxnAdjList =  glService.G1003(tmGlTxnAdj.getTxnCode(), null);
		return glTxnAdjList;
	}
	/**
	 * 获取网点编号
	 * @return
	 */
	@ResponseBody()
	@RequestMapping(value="/getBranchList",method={RequestMethod.POST})
	public LinkedHashMap<String, String> getBranchList() {
//		Map<String,Branch> map = unifiedParameter.retrieveParameterObject(Branch.class); // 分行网点号
		LinkedHashMap<String, String> branch = new LinkedHashMap<String, String>();
		
		// 循环发卡网点
//		for(Entry<String,Branch> enty : map.entrySet()) {
//			branch.put(enty.getKey(), enty.getKey()+"-"+enty.getValue().name);
//		}
		branch.put("00012", "测试网点");
		branch.put("000012", "后台取参数写死，记得放开");
		return branch;
	}
	
}
