package com.sunline.ccs.service.auth.test.verifier;
//package com.sunline.ccs.service.auth.test.verifier;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//import junit.framework.Assert;
//
//import org.apache.commons.lang.time.DateUtils;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.annotation.DirtiesContext.ClassMode;
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
//import com.sunline.ccs.param.def.CountryCtrl;
//import com.sunline.ccs.param.def.enums.AuthVerifyAction;
//import com.sunline.ccs.param.def.enums.CheckType;
//import com.sunline.ccs.param.def.enums.VerifyEnum;
//import com.sunline.ccs.service.auth.test.mock.MediumServiceMock;
//import com.sunline.ccs.test.CPSDataSet;
//import com.sunline.ccs.test.DefaultParamGenerator;
//import com.sunline.ccs.test.TestDataGenerator;
//import com.sunline.acm.service.sdk.GlobalManagementServiceMock;
//import com.sunline.ark.support.OrganizationContextHolder;
//import com.sunline.ark.support.service.YakMessage;
//
///**
// * cup-response-code-modify
// * 
//* @author fanghj
// */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration("/test-service.xml")
//@Transactional
//@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
//public class AuthIcSecureCheckTest {
//
//	@Autowired
//	private AuthorizationService authService;
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
//	public void setup() throws ParseException {
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
//	 * 生成YakMsg数据
//	 * 
//	 * @return
//	 */
//	public YakMessage createYakMsg() {
//		YakMessage msg = new YakMessage();
//		msg.getHeadAttributes().put(11, "0100");
//		
//		msg.getBodyAttributes().put(2, "6200480204984198");
//		msg.getBodyAttributes().put(3, "30");
//		msg.getBodyAttributes().put(4, "1");
//		msg.getBodyAttributes().put(6, "1");
//		msg.getBodyAttributes().put(14, "1234");
//		msg.getBodyAttributes().put(18, "6011");
//		msg.getBodyAttributes().put(19, "156");
//		msg.getBodyAttributes().put(22, "661");
//		msg.getBodyAttributes().put(23, "");
//		msg.getBodyAttributes().put(35,"6200480204984198=12345678901234567890");
//		msg.getBodyAttributes().put(38, "600001");
//		
//		// 特殊交易商户控制表(Key)
//		msg.getBodyAttributes().put(42, "10001");
//		
//		msg.getBodyAttributes().put(45, "6012323232323232");
//		msg.getBodyAttributes().put(48, "ASIN0101234567890");
//		msg.getBodyAttributes().put(49, "156");
//		msg.getBodyAttributes().put(52, "10");
//		msg.getBodyAttributes().put(60, "1234577512555901234567890");
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
//		return msg;
//	}
//
//	/**
//	 * 初始化AuthProduct
//	 * @return
//	 */
//	public AuthProduct initAuthParameter(){
//		AuthProduct authProduct = parameterFacility.loadParameter(product.productCode, AuthProduct.class);
//		// [Ccm_005] 检查针对MCC的交易限制
//		authProduct.checkEnabled.put(CheckType.MccCodeCheckFlag, false);
//
//		// [Ccm_001] 检查针对国家的交易限制
//		authProduct.checkEnabled.put(CheckType.CountryCodeCheckFlag, true);	
//		CountryCtrl countryCtrl = parameterFacility.loadParameter("156", CountryCtrl.class);
//		countryCtrl.validInd = false;	
//		
//		return authProduct;
//	}
//	
//	/**
//	 * 测试目的 : [Ic_001] 禁止fallback交易
//	 * 测试结果 : 无法测试（由于Cro_002、Cro_003会命中此交易，因此目前无法覆盖）
//	 */
//	//	@Test
//	public void authIcSecure001CheckTest() {
//	}
//	
//	/**
//	 * 测试目的 : [Ic_002] icvn验证
//	 * 测试组装的基本要素 : icvnVerifyResult == PasswordVerifyResult.Decline && icInd == Indicator.Y
//	 * 测试判断标准: 返回Reanso: I001
//	 * 测试控制台输出：1. 命中原因[I001]，对应行为[D]；2. 排序后reasoncd =I001优先级= 9000
//	 */
//	public String authIcSecure002CheckTest() {
//		YakMessage msg = createYakMsg();
//		initAuthParameter();
//		// 规则条件
//		mediumMock.getMediumInfo().setIcvnVerifyResult(PasswordVerifyResult.Decline);
//		mediumMock.getMediumInfo().setIcInd(Indicator.Y);
//		
//		return authService.authorize(msg).getBody(39);
//	}
//	
//	/**
//	 * 测试目的 : [Ic_003] arqc错验证
//	 * 测试组装的基本要素 : arqcVerifyResult == PasswordVerifyResult.Decline  && icInd == Indicator.Y
//	 * 测试判断标准: 返回Reanso: I005
//	 * 测试控制台输出：1. 命中原因[I005]，对应行为[D]；2. 排序后reasoncd =I005优先级= 9000
//	 */
//	public String authIcSecure003CheckTest() {
//		YakMessage msg = createYakMsg();
//		initAuthParameter();
//		// 准备数据
//		mediumMock.getMediumInfo().setIcvnVerifyResult(PasswordVerifyResult.Approve);
//		// 规则条件
//		mediumMock.getMediumInfo().setArqcVerifyResult(PasswordVerifyResult.Decline);
//		mediumMock.getMediumInfo().setIcInd(Indicator.Y);
//		
//		return authService.authorize(msg).getBody(39);
//	}
//	
//	/**
//	 * 测试目的 : [Ic_004] arqc未上送验证
//	 * 测试组装的基本要素 : arqcVerifyResult == PasswordVerifyResult.NoThisField && icInd == Indicator.Y
//	 * 测试判断标准: 返回Reanso: I006
//	 * 测试控制台输出：1. 命中原因[I006]，对应行为[D]；2. 排序后reasoncd =I006优先级= 9000
//	 */
//	public String authIcSecure004CheckTest() {
//		YakMessage msg = createYakMsg();
//		// 准备数据
//		mediumMock.getMediumInfo().setIcvnVerifyResult(PasswordVerifyResult.Approve);
//		// 规则条件
//		mediumMock.getMediumInfo().setArqcVerifyResult(PasswordVerifyResult.NoThisField);
//		mediumMock.getMediumInfo().setIcInd(Indicator.Y);
//		initAuthParameter().verifyActions.put(VerifyEnum.IcArqcVerify, AuthVerifyAction.Must);
//		
//		return authService.authorize(msg).getBody(39);
//	}
//	
//	/**
//	 * 测试目的 : [Ic_005] atc验证
//	 * 测试组装的基本要素 : atcVerifyResult == PasswordVerifyResult.Decline  && icInd == Indicator.Y
//	 * 测试判断标准: 返回Reanso: I002
//	 * 测试控制台输出：1. 命中原因[I002]，对应行为[D]；2. 排序后reasoncd =I002优先级= 9000
//	 */
//	public String authIcSecure005CheckTest() {
//		YakMessage msg = createYakMsg();
//		initAuthParameter();
//		// 准备数据
//		mediumMock.getMediumInfo().setIcvnVerifyResult(PasswordVerifyResult.Approve);
//		mediumMock.getMediumInfo().setArqcVerifyResult(PasswordVerifyResult.Approve);
//		// 规则条件
//		mediumMock.getMediumInfo().setAtcVerifyResult(PasswordVerifyResult.Decline);
//		mediumMock.getMediumInfo().setIcInd(Indicator.Y);
//		
//		return authService.authorize(msg).getBody(39);
//	}
//	
//	/**
//	 * 测试目的 : [Ic_006] cvr验证
//	 * 测试组装的基本要素 : cvrVerifyResult == PasswordVerifyResult.Decline  && icInd == Indicator.Y
//	 * 测试判断标准: 返回Reanso: I008
//	 * 测试控制台输出：1. 命中原因[I008]，对应行为[D]；2. 排序后reasoncd =I008优先级= 9000
//	 */
//	public String authIcSecure006CheckTest() {
//		YakMessage msg = createYakMsg();
//		initAuthParameter();
//		// 准备数据
//		mediumMock.getMediumInfo().setIcvnVerifyResult(PasswordVerifyResult.Approve);
//		mediumMock.getMediumInfo().setArqcVerifyResult(PasswordVerifyResult.Approve);
//		mediumMock.getMediumInfo().setAtcVerifyResult(PasswordVerifyResult.Approve);
//		// 规则条件
//		mediumMock.getMediumInfo().setCvrVerifyResult(PasswordVerifyResult.Decline);
//		mediumMock.getMediumInfo().setIcInd(Indicator.Y);
//		
//		return authService.authorize(msg).getBody(39);
//	}
//	
//	/**
//	 * 测试目的 : [Ic_007] tvr验证
//	 * 测试组装的基本要素 : tvrVerifyResult == PasswordVerifyResult.Decline  && icInd == Indicator.Y 
//	 * 测试判断标准: 返回Reanso: I007
//	 * 测试控制台输出：1. 命中原因[I007]，对应行为[D]；2. 排序后reasoncd =I007优先级= 9000
//	 */
//	public String authIcSecure007CheckTest() {
//		YakMessage msg = createYakMsg();
//		initAuthParameter();
//		// 准备数据
//		mediumMock.getMediumInfo().setIcvnVerifyResult(PasswordVerifyResult.Approve);
//		mediumMock.getMediumInfo().setArqcVerifyResult(PasswordVerifyResult.Approve);
//		mediumMock.getMediumInfo().setAtcVerifyResult(PasswordVerifyResult.Approve);
//		mediumMock.getMediumInfo().setCvrVerifyResult(PasswordVerifyResult.Approve);
//		// 规则条件
//		mediumMock.getMediumInfo().setTvrVerifyResult(PasswordVerifyResult.Decline);
//		mediumMock.getMediumInfo().setIcInd(Indicator.Y);
//		
//		return authService.authorize(msg).getBody(39);
//	}
//	
//	/**
//	 * 测试目的 :  [Ic_001-007] 验证
//	 * 测试组装的基本要素 : PasswordVerifyResult.Approve
//	 * 测试判断标准: 返回responsCode = 00 通过
//	 */
//	public String authIcSecure008CheckTest() {
//		YakMessage msg = createYakMsg();
//		initAuthParameter();
//		// 规则条件
//		mediumMock.getMediumInfo().setIcvnVerifyResult(PasswordVerifyResult.Approve);
//		mediumMock.getMediumInfo().setArqcVerifyResult(PasswordVerifyResult.Approve);
//		mediumMock.getMediumInfo().setAtcVerifyResult(PasswordVerifyResult.Approve);
//		mediumMock.getMediumInfo().setCvrVerifyResult(PasswordVerifyResult.Approve);
//		mediumMock.getMediumInfo().setTvrVerifyResult(PasswordVerifyResult.Approve);
//		mediumMock.getMediumInfo().setIcInd(Indicator.N);
//		initAuthParameter().verifyActions.put(VerifyEnum.IcArqcVerify, AuthVerifyAction.Exist);
//		
//		return authService.authorize(msg).getBody(39);
//	}
//	
//	@Test
//	public void authIcSecureCheckTest(){
//		// [Ic_002] icvn验证
//		Assert.assertEquals("05", authIcSecure002CheckTest());
//		// [Ic_003] arqc错验证
//		Assert.assertEquals("05", authIcSecure003CheckTest());
//		// [Ic_004] arqc未上送验证
//		Assert.assertEquals("05", authIcSecure004CheckTest());
//		// [Ic_005] atc验证
//		Assert.assertEquals("05", authIcSecure005CheckTest());
//		// [Ic_006] cvr验证
//		Assert.assertEquals("05", authIcSecure006CheckTest());
//		// [Ic_007] tvr验证
//		Assert.assertEquals("05", authIcSecure007CheckTest());
//		// [Ic_001-007] 验证
//		Assert.assertEquals("00", authIcSecure008CheckTest());
//	}
//}
