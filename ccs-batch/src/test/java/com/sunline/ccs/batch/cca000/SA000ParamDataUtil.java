package com.sunline.ccs.batch.cca000;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sunline.ccs.batch.sdk.ContextUtil;
import com.sunline.ccs.batch.sdk.AbstractPrepareData;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.EarlyRepayDef;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.enums.PrepaymentFeeMethod;
import com.sunline.pcm.param.def.FinancialOrg;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.service.sdk.ParameterServiceMock;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.thoughtworks.xstream.XStream;

@Service
public class SA000ParamDataUtil  {
	private Logger logger =LoggerFactory.getLogger(SA000ParamDataUtil.class);

	@Autowired
	private ParameterServiceMock param;
	
	@Autowired
	private ContextUtil context;

	@Value("#{env.batchWorkDir}")
	private String batchWorkDir = "H:\\BatchWorkDir\\ccs";
	
	public void prepareData(String jobOrStepName) throws Exception{
		if(StringUtils.isBlank(jobOrStepName)){
			throw new IllegalArgumentException("方法参数jobName为必输项");
		}
		String dataBeanName = jobOrStepName+"Data";
		AbstractPrepareData dataPrepare = (AbstractPrepareData)context.getBean(dataBeanName);
		if(dataPrepare == null){
			throw new IllegalArgumentException(dataBeanName + "数据准备Bean不存在");
		}else{
			dataPrepare.prepareData();
		}
	}
	/**
	 * 从param文件夹中加载所有参数文件
	 */
	public void loadParamFromDir(){
		loadParamFromDir(null);
	}
	/**
	 * 从文件夹中加载所有参数文件
	 * @param relativeParamDir 保存参数文件的文件夹
	 */
	public void loadParamFromDir(String relativeParamDir){
		String dirName = StringUtils.isBlank(relativeParamDir)? "param":relativeParamDir;
		File dir = new File(new File(batchWorkDir).getParent() + File.separator + dirName);
		Iterator<File> paramFileIt = FileUtils.listFiles(dir, new String[]{"xml"}, true).iterator();
		while(paramFileIt.hasNext()){
			File paramFile = paramFileIt.next();
			String fileName = paramFile.getName();
			String[] fileNameArray = fileName.split("[_\\.]");
			
			logger.info("读取参数：" + Arrays.toString(fileNameArray));
			String key = fileNameArray[1];
			if(StringUtils.isBlank(key)) {
				key = "*";
			}
			try {
//				Class<?> clazz = Class.forName(paramName);
//				Object paramObject = genParamFromFile(key, clazz, paramFile);
//				logger.info("参数类型[{}]", paramObject.getClass());
				FileInputStream fis = new FileInputStream(paramFile);
				Object object = new XStream().fromXML(fis);
				param.putParameter(key, object);
			} catch(FileNotFoundException e){
				e.printStackTrace();
			}
		}
	}
	
	public void prepareParam() {
		ProductCredit productYg = genProductCredit("000301" );
		productYg.preClaimStartDays = 70;
		productYg.preClaimEndDays = 79;

		param.putParameter(productYg.productCd, productYg);
		
		genProductCredit("000401" );
		genProductCredit("000421" );
		genProductCredit("004102" );
		genProductCredit("001101" );
		genProductCredit("004101" );
		
		genFinancialOrg("001");
		genFinancialOrg("002");
		
		AccountAttribute aa2 = genParamFromFile("2", AccountAttribute.class);
		param.putParameter(aa2.accountAttributeId.toString(), aa2);
		
		AccountAttribute aa4 = genParamFromFile("4", AccountAttribute.class);
		param.putParameter(aa4.accountAttributeId.toString(), aa4);
		
		AccountAttribute aa5 = genParamFromFile("5", AccountAttribute.class);
		param.putParameter(aa5.accountAttributeId.toString(), aa5);
		
		SysTxnCdMapping sysTxnCdMapping = genParamFromFile("S10", SysTxnCdMapping.class);
		param.putParameter("S10", sysTxnCdMapping);
		SysTxnCdMapping sysTxnCdMappingS74 = genParamFromFile("S74", SysTxnCdMapping.class);
		param.putParameter("S74", sysTxnCdMappingS74);
		SysTxnCdMapping sysTxnCdMappingS76 = genParamFromFile("S76", SysTxnCdMapping.class);
		param.putParameter("S76", sysTxnCdMappingS76);
		SysTxnCdMapping sysTxnCdMappingS93 = genParamFromFile("S93", SysTxnCdMapping.class);
		param.putParameter("S93", sysTxnCdMappingS93);
		SysTxnCdMapping sysTxnCdMappingS94 = genParamFromFile("S94", SysTxnCdMapping.class);
		param.putParameter("S94", sysTxnCdMappingS94);
		SysTxnCdMapping sysTxnCdMappingS95 = genParamFromFile("S95", SysTxnCdMapping.class);
		param.putParameter("S95", sysTxnCdMappingS95);
		
	}
	private ProductCredit genProductCredit(String code){
		ProductCredit pc = genParamFromFile(code, ProductCredit.class);
		param.putParameter(code, pc);
		
		Product p = genParamFromFile(code, Product.class);
		param.putParameter(code, p);
		
		pc.loanPlansMap.get(pc.defaultLoanType);
		LoanPlan loanPlan = genLoanPlan(pc.loanPlansMap.get(pc.defaultLoanType) , pc.defaultLoanType, "");
		param.putParameter(loanPlan.loanCode, loanPlan);
		
		return pc;
	}
	private FinancialOrg genFinancialOrg(String code) {
		File paramFile = getParamFile(code, FinancialOrg.class);
		FinancialOrg finOrg = null;
		if(paramFile.exists()){
			finOrg = genParamFromFile(code, FinancialOrg.class);
		}else{
			finOrg = new FinancialOrg();
			finOrg.financialOrgNO = "yg001";
			finOrg.adFeeScale = new BigDecimal("0.50");
		}
		param.putParameter(code, finOrg);
		return finOrg;
	}
	
	public LoanFeeDef genLoanFeeDef(){
		LoanFeeDef loanFeeDef = new LoanFeeDef();
		
		loanFeeDef.prepaymentFeeMethod = PrepaymentFeeMethod.A;
		
		List<EarlyRepayDef> earlyRepayDefs = new ArrayList<EarlyRepayDef>();
		loanFeeDef.earlyRepayDefs = earlyRepayDefs;
		
		return loanFeeDef;
	}
	
	public LoanPlan genYGLoanPlan(){
		LoanPlan loanPlan = new LoanPlan();
		
		loanPlan.loanCode = "123";
		Map<Integer,LoanFeeDef> m = new HashMap<Integer, LoanFeeDef>();
		m.put(12, genLoanFeeDef());
		loanPlan.loanFeeDefMap = m;
		
		return loanPlan;
	}
	
	public LoanPlan genLoanPlan(String code, LoanType loanType, String desc){
		
		File paramFile = getParamFile(code, LoanPlan.class);
		LoanPlan loanPlan = null;
		if(paramFile.exists()){
			loanPlan = genParamFromFile(code, LoanPlan.class);
		}else{
			loanPlan= new LoanPlan();
			loanPlan.loanCode = code;
			loanPlan.loanType = loanType;
			loanPlan.description = desc;
		}
		return loanPlan;
	}
	
	/**
	 * 从默认参数路径文件读取一个参数
	 * @param key
	 * @param paramClazz
	 * @return
	 */
	public <T> T genParamFromFile(Object key, Class<T> paramClazz){
		return genParamFromFile( key, paramClazz, null);
	}
	/**
	 * 从参数文件读取一个参数
	 * @param key
	 * @param paramClazz
	 * @param paramFile
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T genParamFromFile(Object key, Class<T> paramClazz, File paramFile){
		try {
			if(paramFile == null)
				paramFile = getParamFile(key, paramClazz);
			FileInputStream fis = new FileInputStream(paramFile);
			
			return (T) new XStream().fromXML(fis);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 按  ParamClazz_key.xml格式  读取参数文件
	 * 目录为  batchWorkDir/param
	 * @param key
	 * @param paramClazz
	 * @return
	 */
	private <T> File getParamFile(Object key, Class<T> paramClazz) {
		File workDir = new File(batchWorkDir);
		
		String name = paramClazz.toString();
		String simName = name.subSequence(name.lastIndexOf(".") + 1, name.length()).toString();
		
		File paramFile = new File(workDir.getParent() + File.separator + "param" + File.separator 
				+ simName + "_" + key + ".xml");
		logger.info("从文件[{}]获取参数[{}]ID[{}]", paramFile.getName(),simName,key);
		return paramFile;
	}
	

}
