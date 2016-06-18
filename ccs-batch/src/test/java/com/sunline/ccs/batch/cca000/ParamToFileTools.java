package com.sunline.ccs.batch.cca000;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.pcm.service.api.UnifiedParameter;
import com.sunline.pcm.service.api.UnifiedParameterService;

/**
 * 将参数保存到文件
 * For pcm-service-impl, 需要拷贝到pcm
 * 配合SA000ParamUtil.loadParamFromDir()
 * @author wanghl
 *
 */
public class ParamToFileTools {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("/test-service.xml");
		ParamToFileTools t = context.getBean(ParamToFileTools.class);
		t.loadParamToFile();
		context.close();
	}
	
	@Autowired
	private UnifiedParameterService unifiedParamSerivce;
	
	private String paramDir = "f:\\BatchWorkDir\\param";
	
	public void loadParamToFile(){
		Map<String, List<String>> params = paramToLoad();

		
		for(String typeCanonicalName : params.keySet()){
			
			List<String> keyList = params.get(typeCanonicalName);
			
			Map<String, UnifiedParameter> paramMap = loadParam( keyList, typeCanonicalName);
			
			saveParamFileList(typeCanonicalName, paramMap);
		}
		
	}

	private Map<String, List<String>> paramToLoad() {
		Map<String, List<String>> params = new HashMap<String, List<String>>();
		
		params.put("com.sunline.ccs.param.def.SysTxnCdMapping", arrayToList(
				new String[]{"D14","C15", "S30", "S82", "D11"}
				));
//		params.put("com.sunline.ccs.param.def.TxnCd", arrayToList(
//				new String[]{"T920","T948"}
//				));
//		params.put("com.sunline.ccs.param.def.PlanTemplate", arrayToList(
//				new String[]{"100001","100002","100003","110003","500001","510001","600001","610001","200001","555555"}
//				));
//		params.put("com.sunline.ccs.param.def.AccountAttribute", arrayToList(
//				new String[]{"1","2","3","4","5","6","7","8","9"}
//		));
		params.put("com.sunline.ccs.param.def.InterestTable", null);
		params.put("com.sunline.pcm.param.def.Mulct", null);
		params.put("com.sunline.pcm.param.def.Product", null);
		params.put("com.sunline.ccs.param.def.ProductCredit", null);
		params.put("com.sunline.ccs.param.def.LoanPlan", null);
		params.put("com.sunline.pcm.param.def.FinancialOrg", null);
		params.put("com.sunline.ccs.param.def.Organization", null);
		
		/*****************************************/
//		params.put("com.sunline.ccs.param.def.Product", arrayToList(
//				new String[]{"001201"}
//		));
//		
//		params.put("com.sunline.ccs.param.def.ProductCredit", arrayToList(
//				new String[]{"001201"}
//		));
	//		
//		params.put("com.sunline.ccs.param.def.LoanPlan", arrayToList(
//				new String[]{"1201"}
//		));
//		
//		params.put("com.sunline.pcm.param.def.FinancialOrg", arrayToList(
//				new String[]{"001", "002", "003"}
//		));
		
	//	String[] blockCodes = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "C", "F", "H", "J", "K", "L", "O", "P", "Q", "R", "S", "T", "V", "W", "Y", "D", "I"};
	//	params.put("com.sunline.ccs.param.def.BlockCode", arrayToList(blockCodes));
		
		
	
		return params;
	}

	private List<String> arrayToList(String[] arr) {
		List<String> strs = new ArrayList<String>();
		for(int i = 0;i < arr.length; i++){
			strs.add(arr[i]);
		}
		return strs;
	}

	private void saveParamFileList(String typeCanonicalName,
			Map<String, UnifiedParameter> paramMap) {
		String typeName = typeCanonicalName.substring(typeCanonicalName.lastIndexOf(".") + 1);
		for(String key : paramMap.keySet()){
			UnifiedParameter param = paramMap.get(key);
			if(param == null){
				logger.info("参数为空[{}][{}]", typeCanonicalName, key);
				continue;
			}
//			logger.info("保存参数[{}],[{}]", key, param.getParameterXML());
			logger.info("保存参数[{}],[{}]", typeCanonicalName, key);
			try {
				saveParamFile(key, param, typeName );
			} catch (IOException e) {
				logger.error("参数[{}][{}]写入文件失败!", typeCanonicalName, key);
				e.printStackTrace();
				continue;
			}
		}
	}
	
	/**
	 * @param keyList 参数主键
	 * @param typeCanonicalName 参数类型完整类名
	 * @return keyList 为空或为空列表，返回改类型所有参数，否则返回对应主键参数
	 */
	public Map<String, UnifiedParameter> loadParam(List<String> keyList, String typeCanonicalName){
		Map<String, UnifiedParameter> paramMap = null;
		
		OrganizationContextHolder.setCurrentOrg("000000000001");
		
		if(keyList != null && keyList.size()>0 ){
			paramMap = new HashMap<String, UnifiedParameter>();
			for(String key : keyList){
				UnifiedParameter unifiedParameter = unifiedParamSerivce.retrieveParameter(key, typeCanonicalName);
				if(unifiedParameter == null){
					logger.error("参数未取到[{}][{}]", typeCanonicalName, key);
				}else{
					paramMap.put(key, unifiedParameter );
				}
			}
		}else{
			paramMap = unifiedParamSerivce.retrieveParameter(typeCanonicalName);
			if(paramMap.isEmpty()){
				logger.error("参数没取到[{}]", typeCanonicalName);
			}
		}
		
		return paramMap;
	}
	public void saveParamFile( String key, UnifiedParameter param, String typeName) throws IOException{
		String filePath = null;
		if("*".equals(key))
			filePath = paramDir + File.separator + typeName + "_.xml";
		else
			filePath = paramDir + File.separator + typeName + "_" + key + ".xml";
	
		OutputStreamWriter osw = new OutputStreamWriter(
				new BufferedOutputStream(
						new FileOutputStream(
								new File(filePath)
								)
						), "UTF-8");
		
		osw.write(param.getParameterXML());
		osw.close();
		
	}

}
