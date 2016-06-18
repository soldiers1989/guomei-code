package com.sunline.ccs.service.auth.test.verifier;
//package com.sunline.ccs.service.auth.test.verifier;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//import javax.persistence.EntityManager;
//import javax.persistence.PersistenceContext;
//
//import junit.framework.Assert;
//
//import org.apache.commons.lang.time.DateUtils;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.sunline.ppy.api.AuthorizationService;
//import com.sunline.ppy.api.CustomAttributesKey;
//import com.sunline.ppy.api.MediumInfo;
//import com.sunline.ppy.dictionary.enums.ExpiryDateFlag;
//import com.sunline.ppy.dictionary.enums.Indicator;
//import com.sunline.ppy.dictionary.enums.InputSource;
//import com.sunline.ppy.dictionary.enums.PasswordVerifyResult;
//import com.sunline.pcm.param.def.Product;
//import com.sunline.pcm.param.test.BmpParamGenerator;
//import com.sunline.pcm.service.sdk.ParameterServiceMock;
//import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
//import com.sunline.ccs.param.def.AuthProduct;
//import com.sunline.ccs.param.def.enums.CheckType;
//import com.sunline.ccs.service.auth.test.mock.MediumServiceMock;
//import com.sunline.ccs.test.CPSDataSet;
//import com.sunline.ccs.test.DefaultParamGenerator;
//import com.sunline.ccs.test.TestDataGenerator;
//import com.sunline.acm.service.sdk.GlobalManagementServiceMock;
//import com.sunline.ark.support.OrganizationContextHolder;
//import com.sunline.ark.support.service.YakMessage;
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration("/test-service.xml")
//@Transactional
//public class AuthInqTest {
//	
//	@Autowired
//	private AuthorizationService authService;
//	
//	@PersistenceContext
//	private EntityManager em;
//	
//	@Autowired
//	private MediumServiceMock mediumMock;
//	
//	@Autowired
//	private GlobalManagementServiceMock globalMock;
//	
//	@Autowired
//	private TestDataGenerator dataGenerator;
//	
//	@Autowired
//	private ParameterServiceMock parameterMock;
//
//	@Autowired
//	private UnifiedParameterFacility parameterFacility;
//	/**
//	 * 默认参数生成工具
//	 */
//	@Autowired
//	private DefaultParamGenerator defaultParamGenerator;
//
//	/**
//	 * bmp参数生成工具
//	 */
//	@Autowired
//	private BmpParamGenerator bmpParamGenerator;
//
//	private CPSDataSet card;
//	
//	private Product product;
//	
//	@Before
//	public void setup() throws ParseException
//	{
//		OrganizationContextHolder.setCurrentOrg("123456789012");
//		Date today = new SimpleDateFormat("yyyyMMdd").parse("20120919");
//		globalMock.setupBatchDate(today, DateUtils.addDays(today, -1));
//		
//		product = bmpParamGenerator.loadParameters(parameterMock, null);
//		defaultParamGenerator.loadParameters(parameterMock, product.productCode);
//		
//		card = dataGenerator.createActMainCard(product.productCode, today);
//		
//		MediumInfo info = new MediumInfo();
//		info.setArqcVerifyResult(PasswordVerifyResult.Approve);
//		info.setLogicCardNbr(card.getCcsCardO().get(0).getLogicCardNbr());
//		mediumMock.setMediumInfo(info);
//	}
//	
//	/**
//	 * 测试目的 : 查询交易
//	 * 测试组装的基本要素 :  无
//	 * 测试判断标准:  返回正常
//	 *  
//	 */
//	@Test
//	public void AuthInqTest()
//	{
//		 
//		YakMessage msg = new YakMessage();
//		msg.getHeadAttributes().put(11, "0100");
//		
//		msg.getBodyAttributes().put(2, "6200480204984198");
//		msg.getBodyAttributes().put(3, "30");
//		msg.getBodyAttributes().put(4, "1");
//		msg.getBodyAttributes().put(6, "");
//		msg.getBodyAttributes().put(14, "1234");
//		msg.getBodyAttributes().put(18, "6011");
//		msg.getBodyAttributes().put(19, "156");
//		msg.getBodyAttributes().put(22, "021");
//		msg.getBodyAttributes().put(23, "");
//		msg.getBodyAttributes().put(35, "6200480204984198=12345678901234567890");
//		msg.getBodyAttributes().put(38, "600001");
//		msg.getBodyAttributes().put(42, "10001");
//		msg.getBodyAttributes().put(45, "6012323232323232");
//		msg.getBodyAttributes().put(48, "ASIN0101234567890");
//		msg.getBodyAttributes().put(49, "156");
//		msg.getBodyAttributes().put(51, "");
//		msg.getBodyAttributes().put(52, "10");
//		msg.getBodyAttributes().put(61, "1168461641684964494984949849489497479468497940");
//
//		msg.getCustomAttributes().put("cardActiveFlag", Indicator.Y);
//		msg.getCustomAttributes().put("blockCodes", "1234ABCD");
//		msg.getCustomAttributes().put("expiryDateFlag", ExpiryDateFlag.Space);
//		msg.getCustomAttributes().put("passwordVerifyResult",	PasswordVerifyResult.Approve);
//		msg.getCustomAttributes().put(CustomAttributesKey.BUSINESS_DATE_KEY_NAME,new Date());
//		msg.getCustomAttributes().put(CustomAttributesKey.INPUT_SOURCE,InputSource.CUP);
//		msg.getCustomAttributes().put(CustomAttributesKey.MTI, "0100");
//
//		AuthProduct authProduct = parameterFacility.loadParameter(product.productCode, AuthProduct.class);
//		// [Ccm_005] 检查针对MCC的交易限制
//		authProduct.checkEnabled.put(CheckType.MccCodeCheckFlag, false);
//		// [Ccm_001] 检查针对国家的交易限制
//		authProduct.checkEnabled.put(CheckType.CountryCodeCheckFlag, false);
//
//
//		
//		YakMessage yak = authService.authorize(msg);
//		Assert.assertEquals("00",yak.getBodyAttributes().get(39));
//		 
//		
//	}
//
//}
